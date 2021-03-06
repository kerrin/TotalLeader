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
	public MoveToEdge(String[] parts, int weight, int order, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weight, order, ACTOR.ADD, gameStatus, board, ourPlayerIndex);
		for(int i=1; i < parts.length; i++) {
			String[] kv = parts[i].split("=");
			if(kv[0].equals("cond")) {
				this.condition = CONDITIONS.valueOf(kv[1]);
			}
		}
		
		setAdditionalDescription();
	}
	
	/**
	 * 
	 * @param condition
	 */
	public MoveToEdge(CONDITIONS condition, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, gameStatus, board, ourPlayerIndex);
		this.condition = condition;
		setAdditionalDescription();
	}
	
	/**
	 * 
	 * @param condition
	 * @param weighting
	 * @param order
	 * @param actor
	 */
	public MoveToEdge(CONDITIONS condition, int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weighting, order, actor, gameStatus, board, ourPlayerIndex);
		this.condition = condition;
		setAdditionalDescription();
	}

	/**
	 * 
	 */
	protected void setAdditionalDescription() {
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
	public static String getConfigDescriptorForFile(CONDITIONS cond) {
		StringBuffer sb = new StringBuffer(":");
		sb.append("cond=");
		sb.append(cond);
		return sb.toString();
	}
	
	@Override
	public ComputerMove getBestMove() {
		// We need a different rule for self moves, or we'll mess up the stats
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		Square[][] boardArray = board.getBoard();
		LoopUpOrDown fromX = new LoopUpOrDown(0,boardWidth-1);
		while(fromX.next()) {
			LoopUpOrDown fromY = new LoopUpOrDown(0,boardHeight-1);
			while(fromY.next()) {
				short fromUnits=boardArray[fromX.getIndex()][fromY.getIndex()].getUnits();
				if(fromUnits <= 0) continue;
				// Only care about it if it is our square
				if(!boardArray[fromX.getIndex()][fromY.getIndex()].getOwner().equals(currentPlayer)) continue;
				int[][] diffs1 = ComputerUtils.getRandomOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX.getIndex()+diffs1[i][0];
					int toY = fromY.getIndex()+diffs1[i][1];
					// Check valid board square
					if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
					// Check we own the to square too
					if(!boardArray[toX][toY].getOwner().equals(currentPlayer)) continue;
					int[][] diffs2 = ComputerUtils.getRandomOrgtagonalMovesArray();
					for(int j = 0; j < 4; j++) {
						int nearX = toX+diffs2[j][0];
						int nearY = toY+diffs2[j][1];
						// Check the to board location is on the board
						if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
						Player nearOwner = boardArray[nearX][nearY].getOwner();
						if(!nearOwner.equals(currentPlayer)) {
							switch (condition) {
							case NOT_ONLY_SEA:
								if(nearOwner.equals(gameStatus.players[gameStatus.seaPlayerIndex])) {
									Logger.trace(fromX+","+fromY+": Sea not allowed");
									continue;
								}
								break;
							case ONLY_NATIVES:
								if(!nearOwner.equals(gameStatus.players[gameStatus.nativePlayerIndex]))  {
									Logger.trace(fromX+","+fromY+": Need Natives");
									continue;
								}
								break;
							default:
								break;
							}
							// Check valid to move units
							if(boardArray[toX][toY].getUnits() >= gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey())) continue;
							// Found and edge space
							ComputerMove move = new ComputerMove(new CoOrdinate(fromX.getIndex(),fromY.getIndex()), new CoOrdinate(toX,toY), fromUnits);
							Logger.debug(gameStatus.currentPlayerIndex+") "+name+":"+move);
							return move;
						}
					}
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
