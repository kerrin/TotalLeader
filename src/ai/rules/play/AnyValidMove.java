package ai.rules.play;

import board.Square;
import player.Player;
import main.Logger;
import main.Main;
import config.Config;
import events.CoOrdinate;
import ai.ComputerMove;
import ai.rules.ComputerUtils;
import ai.rules.base.PlayRule;

public class AnyValidMove extends PlayRule {
	private static String NAME = "AnyValidMove";
	private static String DESCRIPTION = "Find the first valid move. A last resort.";
	
	public AnyValidMove() {
		super(NAME, DESCRIPTION);
	}

	public AnyValidMove(int weighting, int order, ACTOR actor) {
		super(NAME, DESCRIPTION, weighting, order, actor);
	}

	@Override
	public ComputerMove getBestMove() {
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = Main.players[Main.currentPlayerIndex];
		Square[][] board = Main.board.getBoard();
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=board[fromX][fromY].getUnits();
				if(!board[fromX][fromY].getOwner().equals(currentPlayer)) continue;
				if(fromUnits < 1) continue;
				
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
					// Check not outside board
					if(toX >= boardWidth || toX < 0 || toY > boardHeight || toY < 0) continue;
					// Check not sea with less than enough to build a bridge
					if(board[toX][toY].getOwner().equals(Main.players[Main.seaPlayerIndex]) && 
							fromUnits <= Main.config.getInt(Config.KEY.BUILD_BRIDGE.getKey())) continue;
					// Check not moving to our own maxed out square
					short toUnits = board[toX][toY].getUnits();
					if(toUnits == Main.config.getInt(Config.KEY.MAX_UNITS.getKey()) && 
							board[toX][toY].getOwner().equals(currentPlayer)) continue;
					// Check we aren't moving a 1 into a max number
					short maxUnits = (short)Main.config.getInt(Config.KEY.MAX_UNITS.getKey());
					if(fromUnits == 1 && toUnits == maxUnits) continue;
					// Valid move found
					ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
					Logger.debug(Main.currentPlayerIndex+") "+name+":"+move);
					return move;
				}
			}
		}
		return null;
	}

	
}
