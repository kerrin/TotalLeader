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

public class BuildBridge extends PlayRule {
	/** Nice name of rule */
	private static String NAME = "Build Bridge";
	/** Description of rule */
	private static String DESCRIPTION = "Find an opponent square with a 1 on, next to one of ours with more than one";
	
	/** 'Const' for cost to build land/bridge */
	private short bridgeCost = (short)Main.config.getInt(Config.KEY.BUILD_BRIDGE.getKey());
	
	// C'tors
	public BuildBridge() {
		super(NAME, DESCRIPTION);
	}
	
	public BuildBridge(int weighting, int order, ACTOR actor) {
		super(NAME, DESCRIPTION, weighting, order, actor);
	}

	@Override
	public ComputerMove getBestMove() {
		// We need a different rule for self moves, or we'll mess up the stats
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = Main.players[Main.currentPlayerIndex];
		Square[][] board = Main.board.getBoard();
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=board[fromX][fromY].getUnits();
				// Only care about it if we have more than what we are looking for
				if(!board[fromX][fromY].getOwner().equals(currentPlayer) || fromUnits < bridgeCost) continue;
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
					if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
					if(!board[toX][toY].getOwner().equals(Main.players[Main.seaPlayerIndex])) continue;
					ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
					Logger.debug(Main.currentPlayerIndex+") "+name+":"+move);
					return move;
				}
			}
		}
		return null;
	}
}
