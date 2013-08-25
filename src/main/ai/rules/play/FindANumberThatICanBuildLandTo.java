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

public class FindANumberThatICanBuildLandTo extends PlayRule {
	/** Nice name of rule */
	private static String NAME = "Find Square I Can Build Land To";
	/** Description of rule */
	private static String DESCRIPTION = "Find a square that I can build land to get to";
	
	/** 'Const' for cost to build land/bridge */
	private short bridgeCost = (short)gameStatus.config.getInt(Config.KEY.BUILD_BRIDGE.getKey());
	
	/** The number of adjacent squares that must be us (1 is guaranteed) */
	private int requiredAdjacentSquareThatAreUs = 1;
	/** The number to find on the from square */
	private int numberToFind = 1;
	/** Of a specific player (-1 is any player) to find on the to square */
	private int rulePlayerIndex = -1;
	
	/**
	 * C'tor from config entry for rule
	 * 
	 * @param parts
	 * @param weight
	 */
	public FindANumberThatICanBuildLandTo(String[] parts, int weight, int order, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weight, order, ACTOR.ADD, gameStatus, board, ourPlayerIndex);
		for(int i=1; i < parts.length; i++) {
			String[] kv = parts[i].split("=");
			if(kv[0].equals("ntf")) {
				this.numberToFind = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("pi")) {
				this.rulePlayerIndex = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("rastau")) {
				this.requiredAdjacentSquareThatAreUs = Integer.parseInt(kv[1]);
			}
		}
	}
	
	// C'tors for random weightings
	public FindANumberThatICanBuildLandTo(int numberToFind, int playerIndex, int requiredAdjacentSquareThatAreUs, GameStatus gameStatus, Board board, int ourPlayerIndex) {		
		super(NAME,DESCRIPTION, gameStatus, board, ourPlayerIndex);
		this.numberToFind = numberToFind;
		this.rulePlayerIndex = playerIndex;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}
	
	// C'tors for assigned weightings
	public FindANumberThatICanBuildLandTo(int numberToFind, int playerIndex, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME,DESCRIPTION, weighting, order, actor, gameStatus, board, ourPlayerIndex);
		this.numberToFind = numberToFind;
		this.rulePlayerIndex = playerIndex;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}

	/**
	 * Copy the passed in rule, but use the passed in playerIndex
	 * 
	 * @param copyRule			Rule to copy
	 * @param configPlayerIndex	PlayerIndex
	 */
	public FindANumberThatICanBuildLandTo(FindANumberThatICanBuildLandTo copyRule, int configPlayerIndex, int ourPlayerIndex) {
		this(copyRule.numberToFind, configPlayerIndex, copyRule.requiredAdjacentSquareThatAreUs, copyRule.weighting, copyRule.order, copyRule.actor, copyRule.gameStatus, copyRule.board, ourPlayerIndex);
	}

	/**
	 * Set the additional description text based on attributes
	 */
	protected void setAdditionalDescription() {
		if(requiredAdjacentSquareThatAreUs > 0) {
			addAdditionalNameDetails(", N"+requiredAdjacentSquareThatAreUs);
			addAdditionalDescritionDetails(" with "+requiredAdjacentSquareThatAreUs+" of us as a neigbour");
		}
		if(numberToFind >= 0) {
			addAdditionalNameDetails(", #"+numberToFind);
			addAdditionalDescritionDetails(" find "+numberToFind);
		}
		if(rulePlayerIndex >= 0) {
			String playerName = gameStatus.players[rulePlayerIndex].getName();
			addAdditionalNameDetails(", "+playerName);
			addAdditionalDescritionDetails(" only "+playerName);
		}
		addConfigDescriptor("rastau="+requiredAdjacentSquareThatAreUs);
		addConfigDescriptor("ntf="+numberToFind);
		addConfigDescriptor("pi="+rulePlayerIndex);
	}

	/**
	 * Set the Player Index
	 * 
	 * @param rulePlayerIndex	Player Index
	 */
	public void setPlayerIndex(int rulePlayerIndex) {
		this.rulePlayerIndex = rulePlayerIndex;
	}
	
	/**
	 * Get the player index value for this rule
	 * 
	 * @return
	 */
	public int getPlayerIndex() {
		return rulePlayerIndex;
	}
	
	/**
	 * Get the config descriptor from the values
	 * 
	 * @param ntf		numberToFind
	 * @param pi		playerIndex
	 * @param sm		selfMove
	 * @param rastau	requiredAdjacentSquareThatAreUs
	 * 
	 * @return	ConfigDescriptor
	 */
	public static String getConfigDescriptor(int ntf, int pi, int rastau, int ourPlayerIndex, GameStatus gameStatus) {
		StringBuffer sb = new StringBuffer(":");
		sb.append("rastau=");
		sb.append(rastau);
		sb.append(":ntf=");
		sb.append(ntf);
		sb.append(":pi=");
		sb.append(pi);
		return sb.toString();
	}
	
	@Override
	public ComputerMove getBestMove() {
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		Player playerToFind = null;
		if(rulePlayerIndex >= 0) playerToFind = gameStatus.players[rulePlayerIndex];
		Square[][] boardArray = board.getBoard();
		int[] playerIndexes = getPlayerIndexes(rulePlayerIndex);
		for(int checkPlayerIndex:playerIndexes) {
			for(int fromX = 0; fromX < boardWidth; fromX++) {
				for(int fromY = 0; fromY < boardHeight; fromY++) {
					short fromUnits=boardArray[fromX][fromY].getUnits();
					if(fromUnits < bridgeCost) continue;
					// Only care about it if we have more than what we are looking for
					if(!boardArray[fromX][fromY].getOwner().equals(currentPlayer) || fromUnits <= numberToFind) continue;
					int[][] diffs = ComputerUtils.getRandomOrgtagonalMovesArray();
					for(int i = 0; i < 4; i++) {
						int toX = fromX+diffs[i][0];
						int toY = fromY+diffs[i][1];
						// Check the to board location is on the board
						if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
						Player toOwner = boardArray[toX][toY].getOwner();
						// Check if we need a specific player
						if(playerToFind != null && !toOwner.equals(playerToFind)) continue;
						// This rule can't build bridges
						if(!toOwner.equals(gameStatus.players[gameStatus.seaPlayerIndex])) continue;
						// Now check that squares adjacent 4 squares
						for(int s = 0; s < 4; s++) {
							int nextToSeaX = toX+diffs[s][0];
							int nextToSeaY = toY+diffs[s][1];
							// Check the to board location is on the board
							if(nextToSeaX >= boardWidth || nextToSeaX < 0 || nextToSeaY >= boardHeight || nextToSeaY < 0) continue;
							// Check if we are looking at the from square, if so, that doesn't count
							if(nextToSeaX == fromX && nextToSeaY == fromY) continue;
							// Check if we found the right number
							if(boardArray[nextToSeaX][nextToSeaY].getUnits() == numberToFind) {
								if(requiredAdjacentSquareThatAreUs > 1) {
									int leftToFind = requiredAdjacentSquareThatAreUs;
									for(int j = 0; j < 4; j++) {
										int nearX = toX+diffs[j][0];
										int nearY = toY+diffs[j][1];
										// Check the to board location is on the board
										if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
										if(boardArray[nearX][nearY].getOwner().equals(checkPlayerIndex)) leftToFind--;
									}
									if(leftToFind > 0) {
										Logger.trace("Needed to find "+leftToFind+" more");
										continue;
									}
								}
								ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
								Logger.debug(gameStatus.currentPlayerIndex+") "+name+":"+move);
								return move;
							} else {
								//Logger.trace("Wrong number, was "+board[nextToSeaX][nextToSeaY].getUnits());
							}			
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
