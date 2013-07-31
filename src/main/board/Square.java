package main.board;

import main.GameStatus;
import main.Logger;
import main.ai.rules.ComputerUtils;
import main.config.Config;
import main.events.CoOrdinate;
import main.player.Player;

public class Square {
	public enum TYPE {LAND,SEA};
	private Player owner;
	private short units;
	private TYPE type;
	private final CoOrdinate coordinate;
	private boolean selected = false;
	private final GameStatus gameStatus;
	
	public Square(Player owner, short units, CoOrdinate coordinate, GameStatus gameStatus) {
		type = TYPE.LAND;
		this.owner = owner;
		this.units = units;
		this.coordinate = coordinate;
		this.gameStatus = gameStatus;
	}
	
	public Square(TYPE type, Player owner, short units, CoOrdinate coordinate, GameStatus gameStatus) {
		this.type = type;
		this.owner = owner;
		this.units = units;
		this.coordinate = coordinate;
		this.gameStatus = gameStatus;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public short getUnits() {
		return units;
	}

	/**
	 * Attempt to set the number of units on a land square
	 * 
	 * @param units
	 * 
	 * @return	success
	 */
	public boolean setUnits(short units) {
		if(type == TYPE.SEA) return false;
		this.units = units;
		return true;
	}
	
	/**
	 * Attempt to build land
	 * 
	 * @param units	Units attempting to make land
	 * 
	 * @return	success
	 */
	public boolean makeLand(short units) {
		if(type != TYPE.SEA) return false;
		short cost = (short)gameStatus.config.getInt(Config.KEY.BUILD_BRIDGE.getKey());
		if(units < cost) return false;
		
		this.units = (short)(units - cost);
		this.type = TYPE.LAND;
		return true;
	}
	/**
	 * Only for use during selecting start locations
	 */
	public void makeLand() {
		this.type = TYPE.LAND;
	}

	/**
	 * Is this square land
	 * 
	 * @return
	 */
	public boolean isLand() {
		return type == TYPE.LAND;
	}

	public void mouseClicked() {
		// Only accept mouse input on a players turn
		if(gameStatus.players[gameStatus.currentPlayerIndex].getType() != Player.TYPE.PLAYER) return;
		
		selected = !selected;
		if(selected) {
			gameStatus.eventManager.selected(coordinate);
		} else {
			gameStatus.eventManager.deselected(coordinate);
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Is the mouse currently over this square
	 * @return
	 */
	public boolean selected() {
		return selected;
	}

	/**
	 * Get my co-ordinates
	 * @return
	 */
	public CoOrdinate getCoordinate() {
		return coordinate;
	}

	/** 
	 * Move a number of units from here to a new location
	 * 
	 * @param toSquare	Square to move units too
	 * @param amount	Number to move
	 * 
	 * @return success
	 */
	public boolean moveTo(Square toSquare, short amount) {
		if(!toSquare.isLand()) {
			return buildLand(toSquare, amount);
		}
		if(!toSquare.owner.equals(owner)) {
			return attack(toSquare, amount);
		}
		short toUnits = toSquare.getUnits();
		short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
		short fromUnits = amount;
		if(fromUnits > units) fromUnits = units;
		if(toUnits+fromUnits > maxUnits) {
			fromUnits = (short)(maxUnits - toUnits); 
			toUnits = maxUnits;
		} else {
			toUnits += fromUnits;
		}
		
		if(fromUnits <= 0) {
			Logger.debug("Can't move 0 units from "+this.coordinate+" to "+toSquare.coordinate);
			return false;
		}
		
		toSquare.units = toUnits;
		units -= fromUnits;
		return true;
	}

	/** 
	 * Move all units we can from here to a new location
	 * 
	 * @param toSquare	Square to move units too
	 * 
	 * @return success
	 */
	public boolean moveTo(Square toSquare) {
		return moveTo(toSquare, units);
	}
	
	/**
	 * Attempt to build land
	 * 
	 * @param toSquare
	 * @param amount
	 * @return
	 */
	private boolean buildLand(Square toSquare, short amount) {
		Logger.debug(gameStatus.currentPlayerIndex+")Land Building with "+amount);
		short cost = (short)gameStatus.config.getInt(Config.KEY.BUILD_BRIDGE.getKey());
		if(amount > units) amount = units;
		if(amount < cost) return false;
		boolean success = toSquare.makeLand(amount);
		if(success) {
			toSquare.playerLoses();
			toSquare.owner = owner;
			units -= amount;
			owner.modifySquares(1);
			owner.modifyTotalUnits((short)-cost);
		}
		return success;
	}
	
	/**
	 * Make the current player lose the square and all units on it
	 * Call this before you modify the owner or units
	 */
	public void playerLoses() {
		owner.modifySquares(-1);
		owner.modifyTotalUnits((short)-units);
	}

	/** 
	 * Move all units we can from here to a new location
	 * 
	 * @param toSquare	Square to move units too
	 */
	public boolean attack(Square toSquare, short amount) {
		Logger.debug(gameStatus.currentPlayerIndex+")Attack!");
		short defUnits = toSquare.getUnits();
		short attackUnits = amount;
		if(attackUnits > units) attackUnits = units;
		short baseDefenceUnits = (short)gameStatus.config.getInt(Config.KEY.BASE_DEFENCE_UNITS.getKey());
		if(defUnits == attackUnits) {
			// Tie, both go down to 1 unit
			owner.modifyTotalUnits((short)-(attackUnits-1));
			toSquare.owner.modifyTotalUnits((short)-(attackUnits-1));
			units = 1; 
			toSquare.units = 1;
		} else if(defUnits == 0) {
			// No defence, attacker just moves
			toSquare.playerLoses();
			units -= amount; 
			toSquare.units = amount;
			owner.modifySquares(1);
			toSquare.owner = owner;
		} else {
			if(defUnits < attackUnits) {
				// Attacker wins
				toSquare.playerLoses();
				owner.modifySquares(1);
				owner.modifyTotalUnits((short) -(defUnits - baseDefenceUnits));
				toSquare.owner = owner;
				toSquare.units = (short)(attackUnits - (defUnits - baseDefenceUnits));
				units -= attackUnits;
				short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
				// Catch if the attacker actually gained a unit with a MAX_UNITS attack
				if(toSquare.units > maxUnits) {
					units += (toSquare.units - maxUnits);
					toSquare.units = maxUnits;
				}
			} else {
				// Defender wins
				short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
				if(attackUnits == 1 && defUnits == maxUnits) {
					Logger.debug("Can't move 1 unit from "+this.coordinate+" to "+toSquare.coordinate+" as destination is maxed out");
					return false;
				}
				owner.modifyTotalUnits((short) -attackUnits);
				toSquare.owner.modifyTotalUnits((short) -(attackUnits - baseDefenceUnits));
				units -= attackUnits;
				toSquare.units -= (short)(attackUnits - baseDefenceUnits);
			}
		}
		return true;
	}
	


	/**
	 * Check if the start location would overlap another player
	 * 
	 * @param x	The x location of this square
	 * @param y
	 * @return
	 */
	public boolean startHasPlayerOverlap(Board board) {
		int[][] diffs = ComputerUtils.getRandomOrgtagonalMovesArray();
		for(int i = 0; i < 4; i++) {
			int willBeMineX = coordinate.x+diffs[i][0];
			int willBeMineY = coordinate.y+diffs[i][1];
			Square square = board.getBoard()[willBeMineX][willBeMineY];
			// Is this square owned by a player
			if(!square.getOwner().equals(gameStatus.players[gameStatus.nativePlayerIndex]) && 
					!square.getOwner().equals(gameStatus.players[gameStatus.seaPlayerIndex])) return true;
		}
		return false;
	}
}
