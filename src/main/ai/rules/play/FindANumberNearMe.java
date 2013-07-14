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
	private int playerIndex = -1;
	/** Only move to own square */
	private boolean selfMove = false;
	
	/**
	 * C'tor from config entry for rule
	 * 
	 * @param parts
	 * @param weight
	 */
	public FindANumberNearMe(String[] parts, int weight, GameStatus gameStatus, Board board) {
		super(NAME,DESCRIPTION, gameStatus, board);
		for(int i=1; i < parts.length; i++) {
			String[] kv = parts[i].split("=");
			if(kv[0].equals("ntf")) {
				this.numberToFind = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("pi")) {
				this.playerIndex = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("sm")) {
				this.selfMove = Integer.parseInt(kv[1]) == 1;
			} else if(kv[0].equals("rastau")) {
				this.requiredAdjacentSquareThatAreUs = Integer.parseInt(kv[1]);
			} else {
				Logger.error("Unknown key in config "+kv[0]);
			}
		}
		this.weighting = weight;
	}
	
	// C'tors with random weightings
	public FindANumberNearMe(int numberToFind, int playerIndex, boolean selfMove, int requiredAdjacentSquareThatAreUs, GameStatus gameStatus, Board board) {		
		super(NAME, DESCRIPTION, gameStatus, board);
		this.numberToFind = numberToFind;
		this.playerIndex = playerIndex;
		this.selfMove = selfMove;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}
	
	// C'tors with assigned weightings
	public FindANumberNearMe(int numberToFind, int playerIndex, boolean selfMove, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board) {
		super(NAME,DESCRIPTION, weighting, order, actor, gameStatus, board);
		this.numberToFind = numberToFind;
		this.playerIndex = playerIndex;
		this.selfMove = selfMove;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}

	/**
	 * Copy the passed in rule, but use the passed in playerIndex
	 * 
	 * @param copyRule			Rule to copy
	 * @param configPlayerIndex	PlayerIndex
	 */
	public FindANumberNearMe(FindANumberNearMe copyRule, int configPlayerIndex) {
		this(copyRule.numberToFind, configPlayerIndex, copyRule.selfMove, 
				copyRule.requiredAdjacentSquareThatAreUs, 
				copyRule.weighting, copyRule.order, copyRule.actor, 
				copyRule.gameStatus, copyRule.board);
	}

	/**
	 * Set the Player Index
	 * 
	 * @param playerIndex	Player Index
	 */
	public void setPlayerIndex(int playerIndex) {
		this.playerIndex = playerIndex;
	}
	
	/**
	 * Get the player index value for this rule
	 * 
	 * @return
	 */
	public int getPlayerIndex() {
		return playerIndex;
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
		if(playerIndex >= 0) {
			String playerName = gameStatus.players[playerIndex].getName();
			addAdditionalNameDetails(", "+playerName);
			addAdditionalDescritionDetails(" only "+playerName);
		}
		if(selfMove) {
			addAdditionalNameDetails(", consolidate");
			addAdditionalDescritionDetails(" consolidate");
		}
		
		addConfigDescriptor("rastau="+requiredAdjacentSquareThatAreUs);
		addConfigDescriptor("ntf="+numberToFind);
		addConfigDescriptor("pi="+playerIndex);
		addConfigDescriptor("sm="+(selfMove?"1":"0"));
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
	public static String getConfigDescriptor(int ntf, int pi, boolean sm, int rastau) {
		StringBuffer sb = new StringBuffer();
		sb.append(":rastau=");
		sb.append(rastau);
		sb.append(":ntf=");
		sb.append(ntf);
		sb.append(":pi=");
		sb.append(pi);
		sb.append(":sm=");
		sb.append((sm?"1":"0"));
		return sb.toString();
	}
	
	@Override
	public ComputerMove getBestMove() {
		// Discard this rule if it is self move and the player number doesn't match
		if(playerIndex != -1 && (gameStatus.currentPlayerIndex == playerIndex) != selfMove) return null;
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		Player playerToFind = null;
		if(playerIndex >= 0) playerToFind = gameStatus.players[playerIndex];
		Square[][] boardArray = board.getBoard();
		
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=boardArray[fromX][fromY].getUnits();
				// Only care about it if we have more than what we are looking for
				if(!boardArray[fromX][fromY].getOwner().equals(currentPlayer) || fromUnits <= numberToFind) continue;
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
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
					// Check if we are moving to our selves and if that is correct
					if(toOwner.equals(currentPlayer) != selfMove)  {
						Logger.trace(toX+","+toY+":"+(selfMove?"Need Self":"Need Not Self"));
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
							for(int j = 0; j < 4; j++) {
								int nearX = toX+diffs[j][0];
								int nearY = toY+diffs[j][1];
								// Check the to board location is on the board
								if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
								if(boardArray[nearX][nearY].getOwner().equals(currentPlayer)) leftToFind--;
							}
							if(leftToFind > 0) {
								Logger.trace(toX+","+toY+": Needed to find "+leftToFind+" more");
								continue;
							}
						}
						ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
						Logger.debug(name+":"+move);
						return move;
					} else {
						Logger.trace(toX+","+toY+": Wrong number, was "+toUnits);
					}
				}
			}
		}
		return null;
	}
}
