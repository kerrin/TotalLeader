package main;

import java.awt.Color;

import main.Logger.LEVEL;

import player.Player;
import player.Player.TYPE;
import ai.ComputerPlay;
import ai.ComputerStart;
import board.Board;
import config.Config;
import display.Display;
import events.EventManager;

/**
 * 
 * @author Kerrin
 *
 * To configure the game, edit
 * src/config/Config.java
 * 
 * Controls:
 * Start of Game: Select a square with the mouse to start on, sea will be converted to land
 * Recruiting: Select a square with the mouse to add a unit until your allotment is exhuasted
 * Normal Turn: Select a square to move from, then a square to move to, the maximum units allowed will be sent
 * 		Build land: You can build land by moving 7* units in to the sea (* is configurable)
 * Squares have a maximum number of units they can contain which is 9* (* is configurable)
 * Attacking (moving to a square you don't own):
 *  If the attacker wins, they gain ownership of the square
 *  Attacking a square will cause the lower player to lose all units and the higher player to lose units equal to the 
 *  	lower players units minus 2* (* is configurable)
 *  If both sides have the same number of units, then both sides units are set to 1
 *  If the lower side has 1 unit, then the other side gains that unit
 *  If the lower side has 0 or 2 units, the higher side loses no units 
 * 
 * Keys:
 * 1 to 9 - Hold down while selecting the destination sets the maximum units sent to that number
 * P - Pauses the game (useful when the computer is taking turns)
 * E,W,I,D,F,T - Change debug level
 *
 */
public class Main {
	private static final long TOO_LONG = 100000;
	public static Config config;
	public static Board board;
	public static Display display;
	public static EventManager eventManager;
	private static GameState gameState = GameState.INIT;
	public static int currentPlayerIndex;
	public static Player[] players;
	public static int currentTurn = 0;
	public static int nativePlayerIndex;
	public static int seaPlayerIndex;
	public static ComputerPlay[] computerAi;
	public static boolean pause = false;
	public static Player winner;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		config = new Config();
		Logger.setLevel(LEVEL.fromString(config.getString(Config.KEY.DEBUG_LEVEL.getKey())));
		computerAi = new ComputerPlay[config.getInt(Config.KEY.NUMBER_PLAYERS.getKey())];
		board = new Board(
				config.getInt(Config.KEY.BOARD_HEIGHT.getKey()),
				config.getInt(Config.KEY.BOARD_WIDTH.getKey())
				);
		
		int numPlayers = config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
		players = new Player[numPlayers+2];
		nativePlayerIndex = numPlayers;
		seaPlayerIndex = numPlayers+1;
		
		players[nativePlayerIndex] = new Player(nativePlayerIndex, Player.TYPE.NATIVE, "Natives", Color.GREEN);
		players[seaPlayerIndex] = new Player(seaPlayerIndex, Player.TYPE.NATIVE, "SEA", Color.BLUE);
		
		display = new Display();
		while(gameState == GameState.INIT) {		
			winner = null;
			currentTurn = 0;
			board.init();
	
			display.init(board);
			display.newBoard(board);
			
			eventManager = new EventManager();
			setGameState(GameState.START_LOCATIONS);
			for(currentPlayerIndex = 0; currentPlayerIndex < numPlayers; currentPlayerIndex++) {
				int color = config.getInt(Config.KEY.PLAYER_COLOR.getKey(),currentPlayerIndex);
				String typeStr = config.getString(Config.KEY.PLAYER_TYPE.getKey(),currentPlayerIndex);
				Player.TYPE type = Player.TYPE.fromString(typeStr);
				players[currentPlayerIndex] = new Player(currentPlayerIndex, type, "Player "+currentPlayerIndex, new Color(color));
			}
			for(currentPlayerIndex = 0; currentPlayerIndex < numPlayers; currentPlayerIndex++) {
				Player currentPlayer = players[currentPlayerIndex];
				if(currentPlayer.getType() == TYPE.COMPUTER) {
					new Thread(new ComputerStart()).start();
					computerAi[currentPlayerIndex] = ComputerPlay.getComputerPlayer(currentPlayerIndex);
					String filename = computerAi[currentPlayerIndex].filename;
					Logger.info("Using Computer "+(filename==null?"New":filename));
				} else {
					// In case we change player type progmatically
					computerAi[currentPlayerIndex] = null;
				}
				while(currentPlayer.getStartLocationX() < 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				board.startPlayer(players[currentPlayerIndex]);
				display.repaint();
			}
			currentPlayerIndex = 0;
			setGameState(GameState.PLAYING);
			currentTurn++;
			// Catch the first player being a computer at the game start
			if(players[currentPlayerIndex].getType() == TYPE.COMPUTER) {
				ComputerPlay.spawnComputerPlayThread(computerAi[currentPlayerIndex]);
			}
			int lastTurn = 0;
			long lastTurnChange = System.currentTimeMillis();
			while(gameState != GameState.GAME_OVER) {
				if(lastTurn < currentTurn) {
					lastTurn = currentTurn;
					lastTurnChange = System.currentTimeMillis();
				} else if(lastTurnChange + TOO_LONG < System.currentTimeMillis()) {
					if(Logger.setLevel(LEVEL.TRACE)) Logger.trace("Delayed, so setting logger to trace");
				}
				display.repaint();
			}
			ComputerPlay.waitComputerPlayer();
			NextPlayerThread.waitOnPlayerDone();
			Logger.info("=============Game Over=============");
			// Show scores
			winner = players[0];
			for(int i=0; i < numPlayers; i++) {
				players[i].score();
				if(players[i].getScore() > winner.getScore()) winner = players[i];
			}
			display.repaint();
			// Mark the winning computer player
			if(players[winner.getPlayerIndex()].getType() == TYPE.COMPUTER) {
				computerAi[winner.getPlayerIndex()].winner = true;
			}
			for(ComputerPlay comp:computerAi) {
				FileManager.saveComputerPlayer(comp);
				FileManager.deletePreviousFile(comp);
			}
			if(config.getInt(Config.KEY.AUTO_PLAY.getKey()) > 0) gameState = GameState.INIT;
		}
		pause = true;
		while(pause) {
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	/**
	 * Previous player finished, move the player on
	 */
	public static void nextPlayer() {
		while(pause) {
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		boolean skipPlayer = true;
		while(skipPlayer) {
			skipPlayer = false;
			Logger.debug("Next Player");
			int numPlayers = config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
			currentPlayerIndex++;
			if(currentPlayerIndex >= numPlayers) {
				currentPlayerIndex = 0;
				if(gameState == GameState.PLAYING_RECRUITMENT) {
					setGameState(GameState.PLAYING);
				} else {
					currentTurn++;
					Logger.debug("Turn "+currentTurn);
	
					if(Main.currentTurn % Main.config.getInt(Config.KEY.RECRUITMENT_TURNS.getKey()) == 0) {
						setGameState(GameState.PLAYING_RECRUITMENT);
					}
					checkGameOver();
					if(gameState == GameState.GAME_OVER) return;
				}
			}
			Logger.debug("Player "+currentPlayerIndex);
	
			if(gameState == GameState.PLAYING_RECRUITMENT) {
				players[currentPlayerIndex].recruitments();
				if(players[currentPlayerIndex].getRecruits() <= 0) {
					skipPlayer = true;
				}
			}
			// Players with no units don't get a turn
			if(skipPlayer || players[currentPlayerIndex].getTotalUnits() <= 0) {
				skipPlayer = true;
			} else {
				if(players[currentPlayerIndex].getType() == TYPE.COMPUTER) {
					ComputerPlay.spawnComputerPlayThread(computerAi[currentPlayerIndex]);
				}
			}
		}
	}

	/**
	 * Check if the end game conditions are met
	 */
	private static void checkGameOver() {
		if(currentTurn > config.getInt(Config.KEY.GAME_TURNS.getKey())) {
			setGameState(GameState.GAME_OVER);
		}
		int activePlayers = 0;
		int lastActivePlayer = -1;
		for(int i=0; i < config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());i++) {
			if(players[i].getTotalUnits() > 0) {
				activePlayers++;
				lastActivePlayer = i;
			}
		}
		if(activePlayers <= 1) {
			setGameState(GameState.GAME_OVER);
			Logger.debug("=============Last Player Was: "+(lastActivePlayer>=0?lastActivePlayer+1:"No one")+"=============");
		}
		try {
			Thread.sleep(10);
			Thread.yield();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change the game state, unless the game is over
	 * 
	 * @param newState
	 */
	public static void setGameState(GameState newState) {
		if(gameState == GameState.GAME_OVER && newState != GameState.INIT) return;
		gameState = newState;
	}

	/**
	 * Get the game state
	 * 
	 * @return
	 */
	public static GameState getGameState() {
		return gameState;
	}
}
