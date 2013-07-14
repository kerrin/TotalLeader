package main.ai.rules.play;

import main.GameStatus;
import main.Logger;
import main.ai.ComputerMove;
import main.ai.rules.ComputerUtils;
import main.ai.rules.base.PlayRule;
import main.board.Board;
import main.board.Square;
import main.config.Config;
import main.events.CoOrdinate;
import main.player.Player;

public class AnyValidMove extends PlayRule {
	private static String NAME = "AnyValidMove";
	private static String DESCRIPTION = "Find the first valid move. A last resort.";
	
	public AnyValidMove(GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, gameStatus, board, ourPlayerIndex);
	}

	public AnyValidMove(int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weighting, order, actor, gameStatus, board, ourPlayerIndex);
	}

	@Override
	public ComputerMove getBestMove() {
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		Square[][] boardArray = board.getBoard();
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=boardArray[fromX][fromY].getUnits();
				if(!boardArray[fromX][fromY].getOwner().equals(currentPlayer)) continue;
				if(fromUnits < 1) continue;
				
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
					// Check not outside board
					if(toX >= boardWidth || toX < 0 || toY > boardHeight || toY < 0) continue;
					// Check not sea with less than enough to build a bridge
					if(boardArray[toX][toY].getOwner().equals(gameStatus.players[gameStatus.seaPlayerIndex]) && 
							fromUnits <= gameStatus.config.getInt(Config.KEY.BUILD_BRIDGE.getKey())) continue;
					// Check not moving to our own maxed out square
					short toUnits = boardArray[toX][toY].getUnits();
					if(toUnits == gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey()) && 
							boardArray[toX][toY].getOwner().equals(currentPlayer)) continue;
					// Check we aren't moving a 1 into a max number
					short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
					if(fromUnits == 1 && toUnits == maxUnits) continue;
					// Valid move found
					ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
					Logger.debug(gameStatus.currentPlayerIndex+") "+name+":"+move);
					return move;
				}
			}
		}
		return null;
	}

	@Override
	protected void setAdditionalDescription() {
		// Nothing to add
	}
}
