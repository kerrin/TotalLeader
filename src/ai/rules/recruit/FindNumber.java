package ai.rules.recruit;

import board.Square;
import player.Player;
import main.Logger;
import main.Main;
import config.Config;
import events.CoOrdinate;
import ai.rules.ComputerUtils;
import ai.rules.base.RecruitRule;

public class FindNumber extends RecruitRule {
	/** The name of this rule */
	private static String NAME = "Find Number Recruit";
	/** A description of this rule */
	private static String DESCRIPTION = "Find the first valid place to put a recruit. A last resort.";
	
	/** 'Const' for maximum units a  square can have */
	private short maxUnits = (short)Main.config.getInt(Config.KEY.MAX_UNITS.getKey());
	
	/** Number of adjacent squares that must be us (1 is guaranteed) */
	private int requiredAdjacentSquareThatAreUs = 1;
	/** Number of adjacent squares that are allowed to be us (4 is maximum possible) */
	private int maxAdjacentSquareThatAreUs = 4;
	/** Number of adjacent squares that are a specific player (-1 no restriction) */
	private int requiredAnAdjacentSquareThatIsPlayer = -1;
	/** The number on the square to look for */
	private int numberToFindUs = 1;
	/** The number to find on the move to square */
	private int numberToFindOponent = 1;
	
	/**
	 * C'tor from config entry for rule
	 * 
	 * @param parts
	 * @param weight
	 */
	public FindNumber(String[] parts, int weight) {
		super(NAME,DESCRIPTION);
		for(int i=1; i < parts.length; i++) {
			String[] kv = parts[i].split("=");
			if(kv[0].equals("rastau")) {
				this.requiredAdjacentSquareThatAreUs = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("mastau")) {
				this.maxAdjacentSquareThatAreUs = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("raastup")) {
				this.requiredAnAdjacentSquareThatIsPlayer = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("ntfu")) {
				this.numberToFindUs = Integer.parseInt(kv[1]);
			} else if(kv[0].equals("ntfo")) {
				this.numberToFindOponent = Integer.parseInt(kv[1]);
			}
		}
		this.weighting = weight;
	}

	// C'tors for random weightings
	public FindNumber(int numberToFindUs) {
		this(numberToFindUs, 1);
	}
	public FindNumber(int numberToFindUs, int requiredAdjacentSquareThatAreUs) {
		this(numberToFindUs, -1, requiredAdjacentSquareThatAreUs);
	}	
	public FindNumber(int numberToFindUs, int maxAdjacentSquareThatAreUs, int requiredAdjacentSquareThatAreUs) {
		this(numberToFindUs, -1, maxAdjacentSquareThatAreUs, requiredAdjacentSquareThatAreUs);
	}
	public FindNumber(int numberToFindUs, int numberToFindOponent, int maxAdjacentSquareThatAreUs, int requiredAdjacentSquareThatAreUs) {
		this(numberToFindUs, numberToFindOponent, maxAdjacentSquareThatAreUs, requiredAdjacentSquareThatAreUs, -1);
	}
	public FindNumber(int numberToFindUs, int numberToFindOponent, int maxAdjacentSquareThatAreUs, int requiredAdjacentSquareThatAreUs, int requiredAnAdjacentSquareThatIsPlayer) {
		super(NAME,DESCRIPTION);
		this.numberToFindUs = numberToFindUs;
		this.numberToFindOponent = numberToFindOponent;
		this.maxAdjacentSquareThatAreUs = maxAdjacentSquareThatAreUs;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		this.requiredAnAdjacentSquareThatIsPlayer = requiredAnAdjacentSquareThatIsPlayer;
		setAdditionalDescription();
	}
	
	// C'tors for assigned weightings
	public FindNumber(int numberToFindUs, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor) {
		this(numberToFindUs, -1, requiredAdjacentSquareThatAreUs, weighting, order, actor);
	}
	public FindNumber(int numberToFindUs, int maxAdjacentSquareThatAreUs, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor) {
		this(numberToFindUs, -1, maxAdjacentSquareThatAreUs, requiredAdjacentSquareThatAreUs, weighting, order, actor);
	}
	public FindNumber(int numberToFindUs, int numberToFindOponent, int maxAdjacentSquareThatAreUs, int requiredAdjacentSquareThatAreUs, int weighting, int order, ACTOR actor) {
		this(numberToFindUs, numberToFindOponent, maxAdjacentSquareThatAreUs, requiredAdjacentSquareThatAreUs, -1, weighting, order, actor);
	}
	public FindNumber(int numberToFindUs, int numberToFindOponent, int maxAdjacentSquareThatAreUs, int requiredAdjacentSquareThatAreUs, int requiredAnAdjacentSquareThatIsPlayer, int weighting, int order, ACTOR actor) {
		super(NAME,DESCRIPTION, weighting, order, actor);
		this.numberToFindUs = numberToFindUs;
		this.numberToFindOponent = numberToFindOponent;
		this.maxAdjacentSquareThatAreUs = maxAdjacentSquareThatAreUs;
		this.requiredAdjacentSquareThatAreUs = requiredAdjacentSquareThatAreUs;
		this.requiredAnAdjacentSquareThatIsPlayer = requiredAnAdjacentSquareThatIsPlayer;
		setAdditionalDescription();
	}

	/**
	 * Set the RequireAnAdjacentSquareThisIsPlayer
	 * 
	 * @param raastip	RequireAnAdjacentSquareThisIsPlayer
	 */
	public void setRequiredAnAdjacentSquareThatIsPlayer(int raastip) {
		this.requiredAnAdjacentSquareThatIsPlayer = raastip;
	}
	
	/**
	 * Get the RequiredAnAdjacentSquareThatIsPlayer
	 * 
	 * @return
	 */
	public int getRequiredAnAdjacentSquareThatIsPlayer() {
		return requiredAnAdjacentSquareThatIsPlayer;
	}

	/**
	 * Set the additional descriptions based on configuration of rule
	 */
	public void setAdditionalDescription() {
		if(requiredAdjacentSquareThatAreUs > 0) {
			addAdditionalNameDetails(", N<="+requiredAdjacentSquareThatAreUs);
			addAdditionalDescritionDetails(" with at least "+requiredAdjacentSquareThatAreUs+" of us as a neigbour");
		}
		if(maxAdjacentSquareThatAreUs > 0) {
			addAdditionalNameDetails(", N>="+maxAdjacentSquareThatAreUs);
			addAdditionalDescritionDetails(" with at most "+maxAdjacentSquareThatAreUs+" of us as a neigbour");
		}
		if(numberToFindUs >= 0) {
			addAdditionalNameDetails(", U#"+numberToFindUs);
			addAdditionalDescritionDetails(" us find "+numberToFindUs);
		}
		if(numberToFindOponent >= 0) {
			addAdditionalNameDetails(", O#"+numberToFindOponent);
			addAdditionalDescritionDetails(" oponent find "+numberToFindOponent);
		}
		if(requiredAnAdjacentSquareThatIsPlayer >= 0) {
			addAdditionalNameDetails(", OP"+requiredAnAdjacentSquareThatIsPlayer);
			addAdditionalDescritionDetails(" oponent player "+requiredAnAdjacentSquareThatIsPlayer);
		}
		addConfigDescriptor("rastau="+requiredAdjacentSquareThatAreUs);
		addConfigDescriptor("mastau="+maxAdjacentSquareThatAreUs);
		addConfigDescriptor("ntfu="+numberToFindUs);
		addConfigDescriptor("ntfo="+numberToFindOponent);
		addConfigDescriptor("raastip="+requiredAnAdjacentSquareThatIsPlayer);
	}
	
	/**
	 * Get the config descriptor from the values
	 * 
	 * @param ntfu		numberToFindUs
	 * @param ntfo		numberToFindOponent
	 * @param mastau	maxAdjacentSquareThatAreUs
	 * @param rastau	requiredAdjacentSquareThatAreUs
	 * @param raastip	requiredAnAdjacentSquareThatIsPlayer
	 * 
	 * @return	ConfigDescriptor
	 */
	public static String getConfigDescriptor(int ntfu, int ntfo, int mastau, int rastau, int raastip) {
		StringBuffer sb = new StringBuffer(":");
		sb.append("rastau=");
		sb.append(rastau);
		sb.append("mastau=");
		sb.append(mastau);
		sb.append("ntfu=");
		sb.append(ntfu);
		sb.append("ntfo=");
		sb.append(ntfo);
		sb.append("raastip=");
		sb.append(raastip);
		return sb.toString();
	}
	
	@Override
	public CoOrdinate getBestRecruit() {
		// Discard this rule if it is self move and the player number doesn't match
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = Main.players[Main.currentPlayerIndex];
		Square[][] board = Main.board.getBoard();
		
		for(int fromX = 0; fromX < boardWidth; fromX++) {
			for(int fromY = 0; fromY < boardHeight; fromY++) {
				short fromUnits=board[fromX][fromY].getUnits();
				// Check we can assign a unit here (not full)
				if(fromUnits >= maxUnits) {
					Logger.trace(fromX+","+fromY+": Too many units");
					continue;
				}
				// If we are looking for a specific number, check this is it
				if(numberToFindUs != -1 && fromUnits != numberToFindUs) {
					Logger.trace(fromX+","+fromY+": Needed number, but got " + fromUnits);
					continue;
				}
				// Only care about it is our square
				if(!board[fromX][fromY].getOwner().equals(currentPlayer)) continue;
				int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
				int leftToFind = requiredAdjacentSquareThatAreUs;
				int foundUs = 0;
				boolean foundOponentNumber = (numberToFindOponent == -1); // -1 means we don't care
				boolean foundSpecificOponent = (requiredAnAdjacentSquareThatIsPlayer == -1); // -1 means we don't care
				for(int i = 0; i < 4; i++) {
					int nearX = fromX+diffs[i][0];
					int nearY = fromY+diffs[i][1];
					// Check the to board location is on the board
					if(nearX >= boardWidth || nearX < 0 || nearY >= boardHeight || nearY < 0) continue;
					
					Player nearOwner = board[nearX][nearY].getOwner();
					
					if(requiredAdjacentSquareThatAreUs > 1) {
						if(nearOwner.equals(currentPlayer)) leftToFind--;
					}
					if(maxAdjacentSquareThatAreUs > 1) {
						if(nearOwner.equals(currentPlayer)) foundUs++;
					}
					
					if(!nearOwner.equals(currentPlayer) && board[nearX][nearY].getUnits() == numberToFindOponent) {
						foundOponentNumber = true;
					}
					
					if(!nearOwner.equals(Main.players[requiredAnAdjacentSquareThatIsPlayer])) {
						foundSpecificOponent = true;
					}
				}
				if(leftToFind > 0) {
					Logger.trace(fromX+","+fromY+": Needed to find "+leftToFind+" more");
					continue;
				}
				if(foundUs > maxAdjacentSquareThatAreUs) {
					Logger.trace(fromX+","+fromY+": Found "+foundUs+" > " + maxAdjacentSquareThatAreUs);
					continue;
				}
				if(!foundOponentNumber) continue;
				if(!foundSpecificOponent) continue;
				CoOrdinate move = new CoOrdinate(fromX,fromY);
				Logger.debug(name+":"+move);
				return move;
			}
		}
		return null;
	}
}
