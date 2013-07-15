package test;

import java.awt.Color;
import java.io.File;
import java.util.Vector;

import main.FileManager;
import main.GameStatus;
import main.Logger;
import main.ai.ComputerPlay;
import main.ai.rules.base.PlayRule;
import main.board.Board;
import main.config.Config;
import main.events.CoOrdinate;
import main.player.Player;
import main.player.Player.TYPE;
import junit.framework.TestCase;

public class TestFileManager extends TestCase {
	private GameStatus gameStatus;
	private Board board;
	public void setUp() {
		int playerNumber = 4;
		int maxTurns = 100;
		gameStatus = new GameStatus(); 
		gameStatus.config = new Config();
		gameStatus.config.setValue(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey(), "G:\\Users\\Kerrin\\GIT\\TotalLeader\\test\\computerconfigs\\");
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
		File[] files = FileManager.listAllFiles(gameStatus);
		for(File file:files) {
			if(file.getName().startsWith("270")) {
				file.delete();
			}
		}
	}
	
	public void testLoadComputer() {
		ComputerPlay comp = FileManager.loadComputerPlayer("test.tl-gene", 0, gameStatus, board);
		assertTrue("Null From Load", comp != null);
	}
	
	public void testSaveComputer() {
		ComputerPlay comp = new ComputerPlay(0,gameStatus,board);
		FileManager.saveComputerPlayer(comp, gameStatus, board);
		ComputerPlay compLoaded = FileManager.loadComputerPlayer(comp.filename, 0, gameStatus, board);
		FileManager.saveComputerPlayer(compLoaded, gameStatus, board);
		ComputerPlay compLoaded2 = FileManager.loadComputerPlayer(compLoaded.filename, 0, gameStatus, board);
		deepCompareComputer(compLoaded,compLoaded2, 1);
		Logger.info("Compare to original computer");
		deepCompareComputer(comp,compLoaded2, 2);
	}

	private void deepCompareComputer(ComputerPlay comp1, ComputerPlay comp2, int fromConfigCount) {
		Vector<PlayRule> playRules1 = comp1.getPlayRules();
		Vector<PlayRule> playRules2 = comp2.getPlayRules();
		assertEquals(playRules1.size(), playRules2.size());
		for(int i=0; i < playRules1.size(); i++) {
			PlayRule saved = playRules1.get(i);
			PlayRule loaded = playRules2.get(i);
			assertTrue("Rule difference: " +
					saved.name + " " +
					saved.configDescriptor + " -> " +
					saved.weighting + " != " +
					loaded.name + " " +
					loaded.configDescriptor + " -> " +
					loaded.weighting, 
					saved.equals(loaded));
		}
		String config1 = comp1.getConfigFileContents();
		String config2 = comp2.getConfigFileContents();
		while(fromConfigCount > 0) {
			config2 = TestRig.stripSecondScore(config2);
			fromConfigCount--;
		}
		if(!config1.equals(config2)) {
			Logger.info(config1);
			Logger.info(config2);
		}
		assertTrue("Saved file did not recreate config correctly", 
				config1.equals(config2));
	}
}
