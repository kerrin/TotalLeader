package test.ai;

import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;

import test.TestRig;

import main.GameStatus;
import main.Logger;
import main.ai.ComputerPlay;
import main.ai.ComputerPlayConfig;
import main.ai.rules.base.PlayRule;
import main.board.Board;
import main.config.Config;
import main.events.CoOrdinate;
import main.player.Player;
import main.player.Player.TYPE;
import junit.framework.TestCase;

public class TestComputerPlayConfig extends TestCase {
	private GameStatus gameStatus;
	private Board board;
	public void setUp() {
		int playerNumber = 5;
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
			new Player(gameStatus.seaPlayerIndex, Player.TYPE.NATIVE, "Sea", Color.BLUE, gameStatus, board);
		board = new Board(10,10,gameStatus);
	}
	
	public void tearDown() {
	}
	
	public void testGetConfigFileContents() {
		ComputerPlay comp = new ComputerPlay(0,gameStatus,board);
		
		String compConfigStr = comp.getConfigFileContents(false);
		ComputerPlayConfig compConf = new ComputerPlayConfig(compConfigStr,gameStatus, board, 0);
		HashMap<String, PlayRule> playRulesHash = compConf.getPlayRules();
		
		Vector<PlayRule> playRules2 = comp.getPlayRules();
		outputConfig(playRulesHash);
		//assertEquals(playRulesHash.size(), playRules2.size());
		for(int i=0; i < playRules2.size(); i++) {
			PlayRule playRule2 = playRules2.get(i);
			PlayRule playRule1 = playRulesHash.get(playRule2.getClass().getSimpleName()+playRule2.configDescriptor);
			assertTrue(i+") Can't find matching hash rule for "+
					playRule2.getClass().getSimpleName()+playRule2.configDescriptor,
					playRule1!=null);
			assertTrue("Rule difference: " +
					playRule1.name + " " +
					playRule1.configDescriptor + " -> " +
					playRule1.weighting + " != " +
					playRule2.name + " " +
					playRule2.configDescriptor + " -> " +
					playRule2.weighting, 
					playRule1.equals(playRule2));
		}
	}

	private void outputConfig(HashMap<String, PlayRule> playRulesHash) {
		for(String key:playRulesHash.keySet()) {
			Logger.info(key);
		}
	}
}
