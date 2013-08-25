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

public class BuildBridge extends PlayRule {
	/** Nice name of rule */
	private static String NAME = "Build Bridge";
	/** Description of rule */
	private static String DESCRIPTION = "Find an opponent square with a 1 on, next to one of ours with more than one";
	
	/** 'Const' for cost to build land/bridge */
	private short bridgeCost = (short)gameStatus.config.getInt(Config.KEY.BUILD_BRIDGE.getKey());
	
	// C'tors
	public BuildBridge(GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, gameStatus, board, ourPlayerIndex);
	}
	
	public BuildBridge(int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weighting, order, actor, gameStatus, board, ourPlayerIndex);
	}

	@Override
	protected void setAdditionalDescription() {
		// Nothing to add
	}
	
	@Override
	public ComputerMove getBestMove() {
		// We need a different rule for self moves, or we'll mess up the stats
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		Square[][] boardArray = board.getBoard();
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=boardArray[fromX][fromY].getUnits();
				// Only care about it if we have more than what we are looking for
				if(!boardArray[fromX][fromY].getOwner().equals(currentPlayer) || fromUnits < bridgeCost) continue;
				int[][] diffs = ComputerUtils.getRandomOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
					if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
					if(!boardArray[toX][toY].getOwner().equals(gameStatus.players[gameStatus.seaPlayerIndex])) continue;
					ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
					Logger.debug(gameStatus.currentPlayerIndex+") "+name+":"+move);
					return move;
				}
			}
		}
		return null;
	}
	
	@Override
	public int getWeighting(boolean real) {
		return weighting;
	}
}
