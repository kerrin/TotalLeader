package player;

import java.awt.Color;

import main.Logger;
import main.Main;
import config.Config;

import events.CoOrdinate;

/**
 * A player details, could be human, computer or the native/sea
 * 
 * @author Kerrin
 *
 */
public class Player {
	public enum TYPE {
		PLAYER,
		COMPUTER,
		NATIVE; // Also sea is native

		public static TYPE fromString(String typeStr) {
			TYPE[] values = TYPE.values();
			for(TYPE type:values) {
				if(typeStr.equalsIgnoreCase(type.name())) return type;
			}
			return NATIVE;
		}
	}

	/** Constant for no score set yet */
	private static final int NO_SCORE = -1;
	/** Constant for the player got knocked out */
	private static final int KNOCKEDOUT = -2;

	/** The starting location of the player X */
	private int startLocationX = -1;
	/** The starting location of the player Y */
	private int startLocationY = -1;
	
	/** This players index in the players array */
	private final int playerIndex;
	/** The type of player this is */
	private TYPE type;
	/** The string name of this player */
	private String name;
	/** The color this is displayed as in game */
	private Color color;
	/** The number of board squares the player owns */
	private int ownSquareCount = 0;
	/** The number of units the player owns */
	private int ownUnitsCount = 0;
	/** The number of recruits the player has yet to assign on a recruit turn */
	private int recruits = 0;
	
	/** The players score, only set by the score() function */
	private int score = NO_SCORE;
	
	/**
	 * C'tor
	 * 
	 * @param playerIndex	This players index in the players array
	 * @param type			The type of player this is
	 * @param name			The string name of this player
	 * @param color			The color to display this as in game
	 */
	public Player(int playerIndex, TYPE type, String name, Color color) {
		this.playerIndex = playerIndex;
		this.type = type;
		this.name = name;
		this.color = color;
	}

	/**
	 * Get the type of player this is
	 * 
	 * @return
	 */
	public TYPE getType() {
		return type;
	}
	
	/**
	 * Get the name of this player
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the color this player is displayed as
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Set the start location and assign the starting squares and units
	 * 
	 * @param coordinates
	 */
	public void setStart(CoOrdinate coordinates) {
		if(startLocationX != -1 || startLocationY != -1) {
			throw new RuntimeException("Tried to reset player start");
		}
		startLocationX = coordinates.x;
		startLocationY = coordinates.y;
	}
	
	/**
	 * Get the X location of the starting square of this player
	 * 
	 * @return
	 */
	public int getStartLocationX() {
		return startLocationX;
	}

	/**
	 * Get the Y location of the starting square of this player
	 * 
	 * @return
	 */
	public int getStartLocationY() {
		return startLocationY;
	}

	/**
	 * Is this the same player?
	 * 
	 * @param player	Player to compare to
	 * 
	 * @return	Same?
	 */
	public boolean equals(Player player) {
		return this.playerIndex == player.playerIndex;
	}

	/**
	 * Adjust the total units for this player
	 * 
	 * @param units
	 */
	public void modifyTotalUnits(short units) {
		ownUnitsCount += units;
	}

	/**
	 * Adjust the total squares for this player
	 * 
	 * @param squares
	 */
	public void modifySquares(int squares) {
		ownSquareCount += squares;
	}

	/**
	 * Get the total units for this player
	 * 
	 * @return
	 */
	public int getTotalUnits() {
		return ownUnitsCount;
	}

	/**
	 * Get the total squares for this player
	 * 
	 * @return
	 */
	public int getTotalSquares() {
		return ownSquareCount;
	}
	
	/**
	 * Get the player index in the players array of this player
	 * 
	 * @return
	 */
	public int getPlayerIndex() {
		return playerIndex;
	}
	
	/**
	 * Decrease the recruits and return if they have none left
	 * 
	 * @return
	 */
	public boolean decrementRecruitsLeft() {
		recruits--;
		return recruits<=0;
	}

	/**
	 * Calculate the players recruits and assign them the allowance
	 */
	public void recruitments() {
		recruits = ownSquareCount/Main.config.getInt(Config.KEY.RECRUIT_SQUARES.getKey());
	}

	/**
	 * Get how many recruits the player has to assign
	 * 
	 * @return
	 */
	public int getRecruits() {
		return recruits;
	}

	/**
	 * Calculate the players score
	 */
	public void score() {
		if(ownUnitsCount <= 0) {
			score = KNOCKEDOUT;
		} else {
			score = ownSquareCount*Main.config.getInt(Config.KEY.SQUARE_SCORE.getKey());
			score += ownUnitsCount*Main.config.getInt(Config.KEY.UNIT_SCORE.getKey());
		}
		// Factor in the length of the game, if the game didn't go to full moves, scale it up
		score *= (Main.config.getInt(Config.KEY.GAME_TURNS.getKey()) / (Main.currentTurn*1.0));
		Logger.info(name+": "+score);
	}

	/**
	 * Get the player score, and score() has been called (once the game is over)
	 * 
	 * @return	The player score
	 */
	public int getScore() {
		return score;
	}
}
