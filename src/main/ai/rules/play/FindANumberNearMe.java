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

public class FindANumberNearMe extends PlayRule {
	/** Nice name for rule */
	private static String NAME = "Find";
	/** Description of rule */
	private static String DESCRIPTION = "Find a square";
	
	/** 'Const' for maximum number of units on a square */
	private short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
	
	/** Number of adjacent squares that must be us (1 is guaranteed) */
	private int requiredAdjacentSquareThatAreUs = 1;
	/** The number on the from square to find */
	private int numberToFind = 1;
	/** Of a specific player (-1 is any player) to find on the too square */
	private int rulePlayerIndex = -1;
	
	/**
	 * C'tor from config entry for rule
	 * 
	 * @param parts
	 * @param weight
	 */
	public FindANumberNearMe(String[] parts, int weight, int order, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weight, order, ACTOR.ADD, gameStatus, board, ourPlayerIndex);
		for(int i=1; i < parts.length; i++) {
			String[] kv = parts[i].split("=");
			if(kv[0].equals("ntf")) {
				this.numberToFind = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("pi")) {
				this.rulePlayerIndex = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("rastau")) {
				this.requiredAdjacentSquareThatAreUs = Integer.parseInt(kv[1]);
			} else {
				Logger.error("Unknown key in config "+kv[0]);
			}
		}
	}
	
	// C'tors with random weightings
	public FindANumberNearMe(int numberToFind, int rulePlayerIndex, int requiredAdjacentSquareThatAreUs, GameStatus gameStatus, Board board, int ourPlayerIndex) {		
		super(NAME, DESCRIPTION, gameStatus, board, ourPlayerIndex);
		this.numberToFind = numberToFind;
		this.rulePlayerIndex = rulePlayerIndex;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}
	
	// C'tors with assigned weightings
	public FindANumberNearMe(int numberToFind, int rulePlayerIndex, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME,DESCRIPTION, weighting, order, actor, gameStatus, board, ourPlayerIndex);
		this.numberToFind = numberToFind;
		this.rulePlayerIndex = rulePlayerIndex;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}

	/**
	 * Copy the passed in rule, but use the passed in playerIndex
	 * 
	 * @param copyRule			Rule to copy
	 * @param configPlayerIndex	PlayerIndex
	 */
	public FindANumberNearMe(FindANumberNearMe copyRule, int configPlayerIndex, int ourPlayerIndex) {
		this(copyRule.numberToFind, configPlayerIndex, 
				copyRule.requiredAdjacentSquareThatAreUs, 
				copyRule.weighting, copyRule.order, copyRule.actor, 
				copyRule.gameStatus, copyRule.board,
				ourPlayerIndex);
	}

	/**
	 * Set the Player Index
	 * 
	 * @param playerIndex	Player Index
	 */
	public void setPlayerIndex(int playerIndex) {
		this.rulePlayerIndex = playerIndex;
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
	 * Set the additional descriptions based on the attributes
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
	 * Get the config descriptor from the values
	 * 
	 * @param ntf		numberToFind
	 * @param pi		playerIndex
	 * @param sm		selfMove
	 * @param rastau	requiredAdjacentSquareThatAreUs
	 * 
	 * @return	ConfigDescriptor
	 */
	public static String getConfigDescriptorForFile(int ntf, int pi, int rastau, int ourPlayerIndex, GameStatus gameStatus) {
		StringBuffer sb = new StringBuffer();
		sb.append(":rastau=");
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
		int[] playerIndexes = getPlayerIndexes(rulePlayerIndex);
		for(int checkPlayerIndex:playerIndexes) {
			if(checkPlayerIndex >= 0) playerToFind = gameStatus.players[checkPlayerIndex];
			Square[][] boardArray = board.getBoard();
			
			LoopUpOrDown fromX = new LoopUpOrDown(0,boardWidth-1);
			while(fromX.next()) {
				LoopUpOrDown fromY = new LoopUpOrDown(0,boardHeight-1);
				while(fromY.next()) {
					short fromUnits=boardArray[fromX.getIndex()][fromY.getIndex()].getUnits();
					// Only care about it if we have more than what we are looking for
					if(!boardArray[fromX.getIndex()][fromY.getIndex()].getOwner().equals(currentPlayer) || fromUnits <= numberToFind) continue;
					int[][] diffs1 = ComputerUtils.getRandomOrgtagonalMovesArray();
					for(int i = 0; i < 4; i++) {
						int toX = fromX.getIndex()+diffs1[i][0];
						int toY = fromY.getIndex()+diffs1[i][1];
						// Check the to board location is on the board
						if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
						Player toOwner = boardArray[toX][toY].getOwner();
						// Check if we need a specific player
						if(playerToFind != null && !toOwner.equals(playerToFind)) {
							Logger.trace(toX+","+toY+": Not player "+playerToFind.getName());
							continue;
						}
						// This rule can't build bridges
						if(toOwner.equals(gameStatus.players[gameStatus.seaPlayerIndex])) {
							Logger.trace(toX+","+toY+": Is Sea");
							continue;
						}
						// Check if we are moving to our selves and moving to a max number
						short toUnits = boardArray[toX][toY].getUnits();
						if(toOwner.equals(currentPlayer) && toUnits == maxUnits) {
							Logger.trace(toX+","+toY+": To is Mine and has max units");
							continue;
						}
						// Check if we found the right number
						if(toUnits == numberToFind) {
							if(requiredAdjacentSquareThatAreUs > 1) {
								int leftToFind = requiredAdjacentSquareThatAreUs;
								int[][] diffs2 = ComputerUtils.getRandomOrgtagonalMovesArray();
								for(int j = 0; j < 4; j++) {
									int nearX = toX+diffs2[j][0];
									int nearY = toY+diffs2[j][1];
									// Check the to board location is on the board
									if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
									if(boardArray[nearX][nearY].getOwner().equals(currentPlayer)) leftToFind--;
								}
								if(leftToFind > 0) {
									Logger.trace(toX+","+toY+": Needed to find "+leftToFind+" more");
									continue;
								}
							}
							ComputerMove move = new ComputerMove(new CoOrdinate(fromX.getIndex(),fromY.getIndex()), new CoOrdinate(toX,toY), fromUnits);
							Logger.debug(name+":"+move);
							return move;
						} else {
							Logger.trace(toX+","+toY+": Wrong number, was "+toUnits);
						}
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public int getWeighting(boolean real) {
		if(real) return weighting;
		int modifiedWeighting = weighting;
		switch(rulePlayerIndex) {
		case CONFIG_OPPONENT_PLAYER_ID: modifiedWeighting *= 4; break;
		case CONFIG_NATIVE_PLAYER_ID: modifiedWeighting *= 2; break;
		}
		switch(numberToFind) {
		case 0: modifiedWeighting *= 2; break;
		case 1: modifiedWeighting *= 4; break;
		case 2: modifiedWeighting *= 3; break;
		}
		return modifiedWeighting;
	}
}
