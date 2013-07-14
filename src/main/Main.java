package main;

import java.awt.Color;

import main.Logger.LEVEL;
import main.ai.ComputerPlay;
import main.ai.ComputerStart;
import main.board.Board;
import main.config.Config;
import main.display.Display;
import main.enums.GameState;
import main.events.EventManager;
import main.player.Player;
import main.player.Player.TYPE;


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
	public static final long TOO_LONG = 100000;
	
	public static GameStatus gameStatus;
	public static Board board;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		gameStatus = new GameStatus();
		gameStatus.config = new Config();
		Logger.setLevel(LEVEL.fromString(gameStatus.config.getString(Config.KEY.DEBUG_LEVEL.getKey())));
		gameStatus.computerAi = new ComputerPlay[gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey())];
		board = new Board(
				gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey()),
				gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey()),
				gameStatus
				);
		
		int numPlayers = gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
		gameStatus.players = new Player[numPlayers+2];
		gameStatus.nativePlayerIndex = numPlayers;
		gameStatus.seaPlayerIndex = numPlayers+1;
		
		gameStatus.display = new Display();
		while(gameStatus.gameState == GameState.INIT) {		
			gameStatus.players[gameStatus.nativePlayerIndex] = 
				new Player(gameStatus.nativePlayerIndex, Player.TYPE.NATIVE, "Natives", Color.GREEN, gameStatus, board);
			gameStatus.players[gameStatus.seaPlayerIndex] = 
				new Player(gameStatus.seaPlayerIndex, Player.TYPE.NATIVE, "SEA", Color.BLUE, gameStatus, board);
			
			gameStatus.winner = null;
			gameStatus.currentTurn = 0;
			board.init();
	
			gameStatus.display.init(gameStatus, board);
			gameStatus.display.newBoard(board);
			
			gameStatus.eventManager = new EventManager(gameStatus, board);
			setGameState(GameState.START_LOCATIONS);
			for(gameStatus.currentPlayerIndex = 0; gameStatus.currentPlayerIndex < numPlayers; gameStatus.currentPlayerIndex++) {
				int color = gameStatus.config.getInt(Config.KEY.PLAYER_COLOR.getKey(),gameStatus.currentPlayerIndex);
				String typeStr = gameStatus.config.getString(Config.KEY.PLAYER_TYPE.getKey(),gameStatus.currentPlayerIndex);
				Player.TYPE type = Player.TYPE.fromString(typeStr);
				gameStatus.players[gameStatus.currentPlayerIndex] = 
					new Player(gameStatus.currentPlayerIndex, type, "Player "+gameStatus.currentPlayerIndex, new Color(color), gameStatus, board);
				gameStatus.humanPlaying = gameStatus.humanPlaying || type == TYPE.PLAYER;
			}
			for(gameStatus.currentPlayerIndex = 0; gameStatus.currentPlayerIndex < numPlayers; gameStatus.currentPlayerIndex++) {
				Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
				if(currentPlayer.getType() == TYPE.COMPUTER) {
					new Thread(new ComputerStart(gameStatus, board)).start();
					gameStatus.computerAi[gameStatus.currentPlayerIndex] = ComputerPlay.getComputerPlayer(gameStatus.currentPlayerIndex, gameStatus, board);
					String filename = gameStatus.computerAi[gameStatus.currentPlayerIndex].filename;
					Logger.info("Using Computer "+(filename==null?"New":filename));
				} else {
					// In case we change player type progmatically
					gameStatus.computerAi[gameStatus.currentPlayerIndex] = null;
				}
				while(currentPlayer.getStartLocationX() < 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				board.startPlayer(gameStatus.players[gameStatus.currentPlayerIndex]);
				gameStatus.display.repaint();
			}
			gameStatus.currentPlayerIndex = 0;
			setGameState(GameState.PLAYING);
			gameStatus.currentTurn++;
			// Catch the first player being a computer at the game start
			if(gameStatus.players[gameStatus.currentPlayerIndex].getType() == TYPE.COMPUTER) {
				ComputerPlay.spawnComputerPlayThread(gameStatus.computerAi[gameStatus.currentPlayerIndex]);
			}
			int lastTurn = 0;
			long lastTurnChange = System.currentTimeMillis();
			while(gameStatus.gameState != GameState.GAME_OVER) {
				if(lastTurn < gameStatus.currentTurn) {
					lastTurn = gameStatus.currentTurn;
					lastTurnChange = System.currentTimeMillis();
				} else if(lastTurnChange + TOO_LONG < System.currentTimeMillis()) {
					if(Logger.setLevel(LEVEL.TRACE)) Logger.trace("Delayed, so setting logger to trace");
				}
				gameStatus.display.repaint();
			}
			ComputerPlay.waitComputerPlayer();
			NextPlayerThread.waitOnPlayerDone();
			Logger.info("=============Game Over=============");
			// Show scores
			gameStatus.winner = gameStatus.players[0];
			for(int i=0; i < numPlayers; i++) {
				gameStatus.players[i].score();
				if(gameStatus.players[i].getScore() > gameStatus.winner.getScore()) gameStatus.winner = gameStatus.players[i];
			}
			gameStatus.display.repaint();
			// Mark the winning computer player
			if(gameStatus.players[gameStatus.winner.getPlayerIndex()].getType() == TYPE.COMPUTER) {
				gameStatus.computerAi[gameStatus.winner.getPlayerIndex()].winner = true;
			}
			for(ComputerPlay comp:gameStatus.computerAi) {
				FileManager.saveComputerPlayer(comp, gameStatus, board);
				FileManager.deletePreviousFile(comp, gameStatus);
			}
			if(gameStatus.config.getInt(Config.KEY.AUTO_PLAY.getKey()) > 0) gameStatus.gameState = GameState.INIT;
		}
		gameStatus.pause = true;
		while(gameStatus.pause) {
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
		while(gameStatus.pause) {
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
			int numPlayers = gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
			gameStatus.currentPlayerIndex++;
			if(gameStatus.currentPlayerIndex >= numPlayers) {
				gameStatus.currentPlayerIndex = 0;
				if(gameStatus.gameState == GameState.PLAYING_RECRUITMENT) {
					setGameState(GameState.PLAYING);
				} else {
					gameStatus.currentTurn++;
					Logger.debug("Turn "+gameStatus.currentTurn);
	
					if(gameStatus.currentTurn % Main.gameStatus.config.getInt(Config.KEY.RECRUITMENT_TURNS.getKey()) == 0) {
						setGameState(GameState.PLAYING_RECRUITMENT);
					}
					checkGameOver();
					if(gameStatus.gameState == GameState.GAME_OVER) return;
				}
			}
			Logger.debug("Player "+gameStatus.currentPlayerIndex);
	
			if(gameStatus.gameState == GameState.PLAYING_RECRUITMENT) {
				gameStatus.players[gameStatus.currentPlayerIndex].recruitments();
				if(gameStatus.players[gameStatus.currentPlayerIndex].getRecruits() <= 0) {
					skipPlayer = true;
				}
			}
			// Players with no units don't get a turn
			if(skipPlayer || gameStatus.players[gameStatus.currentPlayerIndex].getTotalUnits() <= 0) {
				skipPlayer = true;
			} else {
				if(gameStatus.players[gameStatus.currentPlayerIndex].getType() == TYPE.COMPUTER) {
					ComputerPlay.spawnComputerPlayThread(gameStatus.computerAi[gameStatus.currentPlayerIndex]);
				}
			}
		}
	}

	/**
	 * Check if the end game conditions are met
	 */
	private static void checkGameOver() {
		if(gameStatus.currentTurn > gameStatus.config.getInt(Config.KEY.GAME_TURNS.getKey())) {
			setGameState(GameState.GAME_OVER);
		}
		int activePlayers = 0;
		int lastActivePlayer = -1;
		for(int i=0; i < gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());i++) {
			if(gameStatus.players[i].getTotalUnits() > 0) {
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
		if(gameStatus.gameState == GameState.GAME_OVER && newState != GameState.INIT) return;
		gameStatus.gameState = newState;
	}

	/**
	 * Get the game state
	 * 
	 * @return
	 */
	public static GameState getGameState() {
		return gameStatus.gameState;
	}
}
