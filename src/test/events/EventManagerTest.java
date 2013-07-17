package test.events;

import java.awt.Color;

import test.PrivateAccessor;
import main.GameStatus;
import main.Main;
import main.ai.ComputerPlay;
import main.board.Board;
import main.board.Square;
import main.config.Config;
import main.enums.GameState;
import main.events.CoOrdinate;
import main.events.EventManager;
import main.player.Player;
import main.player.Player.TYPE;
import junit.framework.TestCase;

public class EventManagerTest extends TestCase {
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
		gameStatus.config.setValue(Config.KEY.BOARD_HEIGHT.getKey(), "10");
		gameStatus.config.setValue(Config.KEY.BOARD_WIDTH.getKey(), "10");
		gameStatus.config.setValueList(Config.KEY.COMPUTER_PLAYER_TYPE.getKey(),new String[]{"New","New","New","New","New"});
		gameStatus.players = new Player[playerNumber+2];
		gameStatus.currentTurn = maxTurns;
		
		gameStatus.nativePlayerIndex = playerNumber;
		gameStatus.seaPlayerIndex = playerNumber+1;
		board = new Board(10,10,gameStatus);
		
		for(int i=0; i < playerNumber; i++) {
			gameStatus.players[i] = new Player(i,TYPE.COMPUTER,"Comp"+i,new Color(i*255*i*64),gameStatus,board);
			//gameStatus.players[i].setStart(new CoOrdinate(1+(i*2),1+(i*2)));
			//gameStatus.players[i].modifyTotalUnits((short)90);
			//gameStatus.players[i].modifySquares((short)20);
			gameStatus.players[i].score();
		}
		gameStatus.players[gameStatus.nativePlayerIndex] = 
			new Player(gameStatus.nativePlayerIndex, Player.TYPE.NATIVE, "Natives", Color.GREEN, gameStatus, board);
		gameStatus.players[gameStatus.seaPlayerIndex] = 
			new Player(gameStatus.seaPlayerIndex, Player.TYPE.NATIVE, "SEA", Color.BLUE, gameStatus, board);
		board.init();
		gameStatus.eventManager = new EventManager(gameStatus, board);
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		gameStatus.computerAi = new ComputerPlay[gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey())];
		gameStatus.computerAi[gameStatus.currentPlayerIndex] = ComputerPlay.getComputerPlayer(gameStatus.currentPlayerIndex, gameStatus, board);
		Main.gameStatus = gameStatus;
	}
	
	public void tearDown() {
		// outputBoard
		int boardWidth = board.getWidth();
		int boardHeight = board.getHeight();
		Square[][] thisBoard = board.getBoard();
		for(int y=0; y < boardHeight; y++) {
			for(int x=0; x < boardWidth; x++) {
				System.out.print(thisBoard[x][y].getUnits());
			}
			System.out.println("");
		}
	}
	
	public void testRecruit() {
		setUpBoard();
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456789012
		// 45678*0123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		gameStatus.players[0].setStart(new CoOrdinate(5,5));
		board.startPlayer(gameStatus.players[0]);
		
		checkTotals(0, 45, 5);
		Player nativePlayer = gameStatus.players[gameStatus.nativePlayerIndex];
		int totalNativeUnits = nativePlayer.getTotalUnits();
		int totalNativeSquares = nativePlayer.getTotalSquares();
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		CoOrdinate selectedCoordinates = new CoOrdinate(5,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (0)
		selectedCoordinates = new CoOrdinate(6,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456789012
		// 456780*123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(5,4, 0, 0);
		checkBoard(6,4, 9, 0);
		checkTotals(0, 45, 6);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING_RECRUITMENT;

		gameStatus.players[gameStatus.currentPlayerIndex].recruitments();
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 1;
		// Player (0)
		selectedCoordinates = new CoOrdinate(5,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456781012
		// 4567801123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(5,4, 1, 0);
		checkTotals(0, 46, 6);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
	}
	public void testMove() {
		setUpBoard();
		gameStatus.gameState = GameState.PLAYING;
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456789012
		// 45678*0123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		gameStatus.players[0].setStart(new CoOrdinate(5,5));
		board.startPlayer(gameStatus.players[0]);
		
		checkTotals(0, 45, 5);
		Player nativePlayer = gameStatus.players[gameStatus.nativePlayerIndex];
		int totalNativeUnits = nativePlayer.getTotalUnits();
		int totalNativeSquares = nativePlayer.getTotalSquares();
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		CoOrdinate selectedCoordinates = new CoOrdinate(5,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (0)
		selectedCoordinates = new CoOrdinate(6,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456789012
		// 456780*123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(5,4, 0, 0);
		checkBoard(6,4, 9, 0);
		checkTotals(0, 45, 6);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;

		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		selectedCoordinates = new CoOrdinate(6,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (9)
		selectedCoordinates = new CoOrdinate(6,3);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456781012
		// 4567801123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(6,4, 1, 0);
		checkBoard(6,3, 1, gameStatus.nativePlayerIndex);
		checkTotals(0, 37, 6);
		totalNativeUnits -= 8;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 1;
		// Player (1)
		selectedCoordinates = new CoOrdinate(5,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (1)
		selectedCoordinates = new CoOrdinate(5,3);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456781012
		// 4567801123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(6,4, 1, 0);
		checkBoard(6,3, 1, gameStatus.nativePlayerIndex);
		checkTotals(0, 37, 6);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);

		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 1;
		// Player (1)
		selectedCoordinates = new CoOrdinate(6,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Player (0)
		selectedCoordinates = new CoOrdinate(5,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456781012
		// 4567810123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(6,4, 0, 0);
		checkBoard(5,4, 1, 0);
		checkTotals(0, 37, 6);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 1;
		// Player (1)
		selectedCoordinates = new CoOrdinate(5,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (8)
		selectedCoordinates = new CoOrdinate(5,3);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791012
		// 4567800123
		// 5678***234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(5,4, 0, 0);
		checkBoard(5,3, 9, gameStatus.nativePlayerIndex);
		checkTotals(0, 36, 6);
		checkTotals(gameStatus.nativePlayerIndex, ++totalNativeUnits, totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		selectedCoordinates = new CoOrdinate(4,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (8)
		selectedCoordinates = new CoOrdinate(3,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791012
		// 4567800123
		// 56730**234
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(4,5, 0, 0);
		checkBoard(3,5, 3, 0);
		checkTotals(0, 30, 7);
		totalNativeUnits -= 8;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		selectedCoordinates = new CoOrdinate(6,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (2)
		selectedCoordinates = new CoOrdinate(7,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791012
		// 4567800123
		// 56730*0*34
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(6,5, 0, 0);
		checkBoard(7,5, 9, 0);
		checkTotals(0, 30, 8);
		totalNativeUnits -= 2;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		selectedCoordinates = new CoOrdinate(7,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (3)
		selectedCoordinates = new CoOrdinate(8,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791012
		// 4567800123
		// 56730*0084
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(7,5, 0, 0);
		checkBoard(8,5, 8, 0);
		checkTotals(0, 29, 9);
		totalNativeUnits -= 3;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 8;
		// Player (8)
		selectedCoordinates = new CoOrdinate(8,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (4)
		selectedCoordinates = new CoOrdinate(9,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791012
		// 4567800123
		// 56730*0006
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(8,5, 0, 0);
		checkBoard(9,5, 6, 0);
		checkTotals(0, 27, 10);
		totalNativeUnits -= 4;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 6;
		// Player (6)
		selectedCoordinates = new CoOrdinate(9,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (3)
		selectedCoordinates = new CoOrdinate(9,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791012
		// 4567800125
		// 56730*0000
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(9,5, 0, 0);
		checkBoard(9,4, 5, 0);
		checkTotals(0, 26, 11);
		totalNativeUnits -= 3;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 5;
		// Player (5)
		selectedCoordinates = new CoOrdinate(9,4);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (2)
		selectedCoordinates = new CoOrdinate(9,3);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678901
		// 3456791015
		// 4567800120
		// 56730*0000
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(9,4, 0, 0);
		checkBoard(9,3, 5, 0);
		checkTotals(0, 26, 12);
		totalNativeUnits -= 2;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 5;
		// Player (5)
		selectedCoordinates = new CoOrdinate(9,3);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (1)
		selectedCoordinates = new CoOrdinate(9,2);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567890
		// 2345678906
		// 3456791010
		// 4567800120
		// 56730*0000
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(9,3, 0, 0);
		checkBoard(9,2, 6, 0);
		checkTotals(0, 27, 13);
		totalNativeUnits -= 1;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 6;
		// Player (6)
		selectedCoordinates = new CoOrdinate(9,2);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (0)
		selectedCoordinates = new CoOrdinate(9,1);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456789
		// 1234567896
		// 2345678900
		// 3456791010
		// 4567800120
		// 56730*0000
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(9,2, 0, 0);
		checkBoard(9,1, 6, 0);
		checkTotals(0, 27, 14);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, --totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 6;
		// Player (6)
		selectedCoordinates = new CoOrdinate(9,1);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Native (9)
		selectedCoordinates = new CoOrdinate(9,0);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456785
		// 1234567890
		// 2345678900
		// 3456791010
		// 4567800120
		// 56730*0000
		// 67890*2345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(9,1, 0, 0);
		checkBoard(9,0, 5, gameStatus.nativePlayerIndex);
		checkTotals(0, 21, 14);
		totalNativeUnits -= 4;
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		

		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		makeSea(5,7);
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 9;
		// Player (9)
		selectedCoordinates = new CoOrdinate(5,6);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Sea
		selectedCoordinates = new CoOrdinate(5,7);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456785
		// 1234567890
		// 2345678900
		// 3456791010
		// 4567800120
		// 56730*0000
		// 6789002345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(5,6, 0, 0);
		checkBoard(5,7, 2, 0);
		checkTotals(0, 14, 15);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 8;
		// Player (8)
		selectedCoordinates = new CoOrdinate(5,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Player (0)
		selectedCoordinates = new CoOrdinate(4,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456785
		// 1234567890
		// 2345678900
		// 3456791010
		// 4567800120
		// 5673810000
		// 6789002345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(5,5, 1, 0);
		checkBoard(4,5, 8, 0);
		checkTotals(0, 14, 15);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
		
		// ------------------------------------------
		
		gameStatus.currentPlayerIndex = 0;
		gameStatus.gameState = GameState.PLAYING;
		makeSea(3, 5);
		
		gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits = 8;
		// Player (8)
		selectedCoordinates = new CoOrdinate(4,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// Player (0)
		selectedCoordinates = new CoOrdinate(3,5);
		gameStatus.eventManager.selected(selectedCoordinates);
		// 0123456785
		// 1234567890
		// 2345678900
		// 3456791010
		// 4567800120
		// 5673810000
		// 6789002345
		// 7890123456
		// 8901234567
		// 9012345678
		checkBoard(4,5, 0, 0);
		checkBoard(3,5, 1, 0);
		checkTotals(0, 7, 16);
		checkTotals(gameStatus.nativePlayerIndex, totalNativeUnits, totalNativeSquares);
	}

	private void makeSea(int x, int y) {
		// Make it sea
		Square[][] thisBoard = board.getBoard();
		thisBoard[x][y].setOwner(gameStatus.players[gameStatus.seaPlayerIndex]);
		thisBoard[x][y].setUnits((short)0);
		PrivateAccessor.setPrivateField(thisBoard[x][y],"type",main.board.Square.TYPE.SEA);
	}
	
	private void checkTotals(int playerIndex, int totalUnits, int totalSquare) {
		Player checkPlayer = gameStatus.players[playerIndex];
		assertEquals(totalUnits, checkPlayer.getTotalUnits());
		assertEquals(totalSquare, checkPlayer.getTotalSquares());
	}

	private void checkBoard(int x, int y, int units, int ownerId) {
		Square[][] thisBoard = board.getBoard();
		assertEquals(units, thisBoard[x][y].getUnits());
		assertEquals(ownerId, thisBoard[x][y].getOwner().getPlayerIndex());		
	}

	private void setUpBoard() {
		int boardWidth = board.getWidth();
		int boardHeight = board.getHeight();
		Square[][] thisBoard = board.getBoard();
		for(int x=0; x < boardWidth; x++) {
			for(int y=0; y < boardHeight; y++) {
				thisBoard[x][y].makeLand();
				thisBoard[x][y].setOwner(gameStatus.players[gameStatus.nativePlayerIndex]);
				thisBoard[x][y].setUnits((short)((x+y)%10));
			}	
		}
	}
}
