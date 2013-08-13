package test.ai;

import java.awt.Color;

import test.TestRig;

import main.GameStatus;
import main.Logger;
import main.ai.ComputerPlay;
import main.board.Board;
import main.config.Config;
import main.events.CoOrdinate;
import main.player.Player;
import main.player.Player.TYPE;
import junit.framework.TestCase;

public class TestComputerPlay extends TestCase {
	private GameStatus gameStatus;
	private Board board;
	public void setUp() {
		int playerNumber = 4;
		int maxTurns = 100;
		gameStatus = new GameStatus(); 
		gameStatus.config = new Config(TestRig.DEFAULT_CONFIG_DIR);
		gameStatus.config.setValue(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey(), TestRig.DEFAULT_CONFIG_DIR);
		gameStatus.config.setValue(Config.KEY.NUMBER_PLAYERS.getKey(), ""+playerNumber);
		gameStatus.config.setValue(Config.KEY.GAME_TURNS.getKey(), ""+maxTurns);
		gameStatus.players = new Player[playerNumber+2];
		gameStatus.currentTurn = maxTurns;
		gameStatus.nativePlayerIndex = playerNumber;
		gameStatus.seaPlayerIndex = playerNumber+1;
		for(int i=0; i < playerNumber; i++) {
			gameStatus.players[i] = new Player(i,TYPE.COMPUTER,"Comp"+i,new Color(i*255*i*64),gameStatus,board);
			gameStatus.players[i].setStart(new CoOrdinate(1+(i*2),1+(i*2)));
			gameStatus.players[i].modifyTotalUnits((short)90);
			gameStatus.players[i].modifySquares((short)20);
			gameStatus.players[i].score();
		}
		gameStatus.players[gameStatus.nativePlayerIndex] = 
			new Player(gameStatus.nativePlayerIndex, Player.TYPE.NATIVE, "Natives", Color.GREEN, gameStatus, board);
		gameStatus.players[gameStatus.seaPlayerIndex] = 
			new Player(gameStatus.seaPlayerIndex, Player.TYPE.NATIVE, "SEA", Color.BLUE, gameStatus, board);
		board = new Board(10,10,gameStatus);
	}
	
	public void tearDown() {
	}
	
	public void testGetConfigFileContents() {
		ComputerPlay comp = new ComputerPlay(0,gameStatus,board);
		
		assertTrue("getConfigFileContents returned different results for comp!", 
				comp.getConfigFileContents(false).equals(comp.getConfigFileContents(false)));
		
		String compConfig = comp.getConfigFileContents(false);
		ComputerPlay compFromConfig = new ComputerPlay("test", compConfig, 0, gameStatus,board);
		
		String compFromConfigConfig = compFromConfig.getConfigFileContents(false);
		String compConf = comp.getConfigFileContents(false);
		compFromConfigConfig = TestRig.stripSecondScore(compFromConfigConfig);
		
		if(!compFromConfigConfig.equals(comp.getConfigFileContents(false))) {
			Logger.info(compFromConfigConfig);
			Logger.info(compConf);
		}

		assertTrue("getConfigFileContents returned different results for compFromConfig!", 
				compFromConfig.getConfigFileContents(false).equals(compFromConfig.getConfigFileContents(false)));
		
		assertTrue("getConfigFileContents returned different results for comp and compFromConfig!", 
				compFromConfigConfig.equals(compConf));
	}

	
}
