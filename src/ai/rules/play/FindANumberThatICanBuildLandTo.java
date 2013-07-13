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

public class FindANumberThatICanBuildLandTo extends PlayRule {
	/** Nice name of rule */
	private static String NAME = "Find Square I Can Build Land To";
	/** Description of rule */
	private static String DESCRIPTION = "Find a square that I can build land to get to";
	
	/** 'Const' for cost to build land/bridge */
	private short bridgeCost = (short)Main.config.getInt(Config.KEY.BUILD_BRIDGE.getKey());
	
	/** The number of adjacent squares that must be us (1 is guaranteed) */
	private int requiredAdjacentSquareThatAreUs = 1;
	/** The number to find on the from square */
	private int numberToFind = 1;
	/** Of a specific player (-1 is any player) to find on the to square */
	private int playerIndex = -1;
	/** Is this to only find turns that consolidate units */
	private boolean selfMove = false;
	
	/**
	 * C'tor from config entry for rule
	 * 
	 * @param parts
	 * @param weight
	 */
	public FindANumberThatICanBuildLandTo(String[] parts, int weight) {
		super(NAME,DESCRIPTION);
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
			}
		}
		this.weighting = weight;
	}
	
	// C'tors for random weightings
	public FindANumberThatICanBuildLandTo(int numberToFind) {
		this(numberToFind, 1);
	}
	public FindANumberThatICanBuildLandTo(int numberToFind, int requiredAdjacentSquareThatAreUs) {
		this(numberToFind, -1, requiredAdjacentSquareThatAreUs);
	}	
	public FindANumberThatICanBuildLandTo(int numberToFind, int playerIndex, int requiredAdjacentSquareThatAreUs) {
		this(numberToFind, playerIndex, false, requiredAdjacentSquareThatAreUs);
	}
	public FindANumberThatICanBuildLandTo(int numberToFind, int playerIndex, boolean selfMove, int requiredAdjacentSquareThatAreUs) {		
		super(NAME,DESCRIPTION);
		this.numberToFind = numberToFind;
		this.playerIndex = playerIndex;
		this.selfMove = selfMove;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}
	
	// C'tors for assigned weightings
	public FindANumberThatICanBuildLandTo(int numberToFind, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor) {
		this(numberToFind, -1, requiredAdjacentSquareThatAreUs, weighting, order, actor);
	}
	public FindANumberThatICanBuildLandTo(int numberToFind, int playerIndex, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor) {
		this(numberToFind, playerIndex, false, requiredAdjacentSquareThatAreUs, weighting, order, actor);
	}
	public FindANumberThatICanBuildLandTo(int numberToFind, int playerIndex, boolean selfMove, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor) {
		super(NAME,DESCRIPTION, weighting, order, actor);
		this.numberToFind = numberToFind;
		this.playerIndex = playerIndex;
		this.selfMove = selfMove;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		setAdditionalDescription();
	}

	/**
	 * Set the additional description text based on attributes
	 */
	public void setAdditionalDescription() {
		if(requiredAdjacentSquareThatAreUs > 0) {
			addAdditionalNameDetails(", N"+requiredAdjacentSquareThatAreUs);
			addAdditionalDescritionDetails(" with "+requiredAdjacentSquareThatAreUs+" of us as a neigbour");
		}
		if(numberToFind >= 0) {
			addAdditionalNameDetails(", #"+numberToFind);
			addAdditionalDescritionDetails(" find "+numberToFind);
		}
		if(playerIndex >= 0) {
			String playerName = Main.players[playerIndex].getName();
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
		StringBuffer sb = new StringBuffer(":");
		sb.append("rastau=");
		sb.append(rastau);
		sb.append("ntf=");
		sb.append(ntf);
		sb.append("pi=");
		sb.append(pi);
		sb.append("sm=");
		sb.append((sm?"1":"0"));
		return sb.toString();
	}
	
	@Override
	public ComputerMove getBestMove() {
		// Discard this rule if it is self move and the player number doesn't match
		if(playerIndex != -1 && (Main.currentPlayerIndex == playerIndex) != selfMove) return null;
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = Main.players[Main.currentPlayerIndex];
		Player playerToFind = null;;
		if(playerIndex >= 0) playerToFind = Main.players[playerIndex];
		Square[][] board = Main.board.getBoard();
		
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=board[fromX][fromY].getUnits();
				if(fromUnits < bridgeCost) continue;
				// Only care about it if we have more than what we are looking for
				if(!board[fromX][fromY].getOwner().equals(currentPlayer) || fromUnits <= numberToFind) continue;
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				for(int i = 0; i < 4; i++) {
					int toX = fromX+diffs[i][0];
					int toY = fromY+diffs[i][1];
					// Check the to board location is on the board
					if(toX >= boardWidth || toX < 0 || toY >= boardHeight || toY < 0) continue;
					Player toOwner = board[toX][toY].getOwner();
					// Check if we need a specific player
					if(playerToFind != null && !toOwner.equals(playerToFind)) continue;
					// This rule can't build bridges
					if(!toOwner.equals(Main.players[Main.seaPlayerIndex])) continue;
					// Now check that squares adjacent 4 squares
					for(int s = 0; s < 4; s++) {
						int nextToSeaX = toX+diffs[s][0];
						int nextToSeaY = toY+diffs[s][1];
						// Check the to board location is on the board
						if(nextToSeaX >= boardWidth || nextToSeaX < 0 || nextToSeaY >= boardHeight || nextToSeaY < 0) continue;
						// Check if we are moving to our selves and if that is correct
						if(board[nextToSeaX][nextToSeaY].getOwner().equals(currentPlayer) != selfMove) {
							//Logger.trace(selfMove?"Need Self":"Need Not Self");
							continue;
						}
						// Check if we are looking at the from square, if so, that doesn't count
						if(nextToSeaX == fromX && nextToSeaY == fromY) continue;
						// Check if we found the right number
						if(board[nextToSeaX][nextToSeaY].getUnits() == numberToFind) {
							if(requiredAdjacentSquareThatAreUs > 1) {
								int leftToFind = requiredAdjacentSquareThatAreUs;
								for(int j = 0; j < 4; j++) {
									int nearX = toX+diffs[j][0];
									int nearY = toY+diffs[j][1];
									// Check the to board location is on the board
									if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
									if(board[nearX][nearY].getOwner().equals(currentPlayer)) leftToFind--;
								}
								if(leftToFind > 0) {
									Logger.trace("Needed to find "+leftToFind+" more");
									continue;
								}
							}
							ComputerMove move = new ComputerMove(new CoOrdinate(fromX,fromY), new CoOrdinate(toX,toY), fromUnits);
							Logger.debug(Main.currentPlayerIndex+") "+name+":"+move);
							return move;
						} else {
							//Logger.trace("Wrong number, was "+board[nextToSeaX][nextToSeaY].getUnits());
						}			
					}
				}
			}
		}
		return null;
	}
}
