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

public class MoveToEdge extends PlayRule {
	/** The nice name for the rule */
	private static String NAME = "Move To Edge";
	/** The description of the rule */
	private static String DESCRIPTION = "Move a unit to the edge of our area that was not";
	
	/** Possible restrictions */
	public enum CONDITIONS {NONE, NOT_ONLY_SEA, ONLY_NATIVES}
	/** The restriction on the to square to enforce */
	private CONDITIONS condition = CONDITIONS.NONE;
	
	/**
	 * C'tor from config entry for rule
	 * 
	 * @param parts
	 * @param weight
	 */
	public MoveToEdge(String[] parts, int weight) {
		super(NAME,DESCRIPTION);
		for(int i=1; i < parts.length; i++) {
			String[] kv = parts[i].split("=");
			if(kv[0].equals("cond")) {
				this.condition = CONDITIONS.valueOf(kv[1]);
			}
		}
		this.weighting = weight;
		
		setAdditionalDescription();
	}
	
	public MoveToEdge(CONDITIONS condition) {
		super(NAME, DESCRIPTION);
		this.condition = condition;
		setAdditionalDescription();
	}
	
	public MoveToEdge(CONDITIONS condition, int weighting, int order, ACTOR actor) {
		super(NAME, DESCRIPTION, weighting, order, actor);
		this.condition = condition;
		setAdditionalDescription();
	}

	private void setAdditionalDescription() {
		addAdditionalNameDetails(", C"+condition.name());
		addAdditionalDescritionDetails(" with "+condition.name());
		addConfigDescriptor("cond="+condition.name());
	}
	
	/**
	 * Get the config descriptor from the values
	 * 
	 * @param cond		Condition
	 * 
	 * @return	ConfigDescriptor
	 */
	public static String getConfigDescriptor(CONDITIONS cond) {
		StringBuffer sb = new StringBuffer(":");
		sb.append("cond=");
		sb.append(cond);
		return sb.toString();
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
				if(fromUnits <= 0) continue;
				// Only care about it if it is our square
				if(!board[fromX][fromY].getOwner().equals(currentPlayer)) continue;
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
					// Check valid board square
					if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
					// Check we own the to square too
					if(!board[toX][toY].getOwner().equals(currentPlayer)) continue;
					for(int j = 0; j < 4; j++) {
						int nearX = toX+diffs[j][0];
						int nearY = toY+diffs[j][1];
						// Check the to board location is on the board
						if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
						Player nearOwner = board[nearX][nearY].getOwner();
						if(!nearOwner.equals(currentPlayer)) {
							switch (condition) {
							case NOT_ONLY_SEA:
								if(nearOwner.equals(Main.players[Main.seaPlayerIndex])) {
									Logger.trace(fromX+","+fromY+": Sea not allowed");
									continue;
								}
								break;
							case ONLY_NATIVES:
								if(!nearOwner.equals(Main.players[Main.nativePlayerIndex]))  {
									Logger.trace(fromX+","+fromY+": Need Natives");
									continue;
								}
								break;
							default:
								break;
							}
							// Check valid to move units
							if(board[toX][toY].getUnits() >= Main.config.getInt(Config.KEY.MAX_UNITS.getKey())) continue;
							// Found and edge space
							ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
							Logger.debug(Main.currentPlayerIndex+") "+name+":"+move);
							return move;
						}
					}
				}
			}
		}
		return null;
	}
}
