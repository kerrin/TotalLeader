package ai.rules.start;

import board.Square;
import main.Logger;
import main.Main;
import config.Config;
import events.CoOrdinate;
import ai.rules.ComputerUtils;
import ai.rules.base.StartRule;

public class PickAreaWithLowNumbers extends StartRule {
	/** Nice name for rule */
	private static String NAME = "Low Numbers";
	/** Description of rule */
	private static String DESCRIPTION = "Start Location Where Area Has Low Numbers";
	
	/**
	 * C'tor
	 */
	public PickAreaWithLowNumbers() {
		super(NAME, DESCRIPTION);
	}

	/**
	 * C'tor
	 * 
	 * @param weighting
	 * @param order
	 * @param actor
	 */
	public PickAreaWithLowNumbers(int weighting, int order, ACTOR actor) {
		super(NAME, DESCRIPTION, weighting, order, actor);
	}

	/** Find the best starting location */
	@Override
	public CoOrdinate getBestStart() {
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		short maxUnits = (short)Main.config.getInt(Config.KEY.MAX_UNITS.getKey());
		
		int bestScoreX = -1;
		int bestScoreY = -1;
		int[][] locationScore = new int[boardWidth][boardHeight]; 
		for(int x=1; x < boardWidth - 1; x++) {
			for(int y=1; y < boardWidth - 1; y++) {
				locationScore[x][y] = 0;
			}
		}
		for(int n=1; n <= maxUnits; n++) {
			for(int x=1; x < boardWidth - 1; x++) {
				for(int y=1; y < boardWidth - 1; y++) {
					Square square = Main.board.getBoard()[x][y];
					// Only score squares that are native owned
					if(!square.getOwner().equals(Main.players[Main.nativePlayerIndex])) continue;
					// Don't score squares that overlap other players
					if(square.startHasPlayerOverlap()) continue;
					short units = square.getUnits();
					locationScore[x][y] += 1+(maxUnits - units);
					// Bonus for really low numbers (special numbers)
					if(units <= 2) locationScore[x][y] += 9;
					
				}	
			}
		}
		
		int bestScore = -1;
		for(int x=1; x < boardWidth - 1; x++) {
			for(int y=1; y < boardWidth - 1; y++) {
				int startScore = getStartScore(locationScore, x,y);
				if(bestScore < startScore) {
					bestScore = startScore;
					bestScoreX = x;
					bestScoreY = y;
				}
			}
		}
		
		Logger.debug("Best Score was: "+bestScore+ " at "+bestScoreX+ ","+bestScoreY);
		return new CoOrdinate(bestScoreX,bestScoreY);
	}

	/**
	 * Calculate the squares start location score
	 * 
	 * @param locationScore		The array of location scores
	 * @param x					The x coordinate to get the start score for
	 * @param y					The y coordinate to get the start score for
	 * 
	 * @return					The start score
	 */
	private int getStartScore(int[][] locationScore, int x, int y) {
		int squareScore = 0;
		int[][] diffs = ComputerUtils.getOrgtagonalMovesArray();
		for(int i = 0; i < 4; i++) {
			int willBeMineX = x+diffs[i][0];
			int willBeMineY = y+diffs[i][1];
			for(int j = 0; j < 4; j++) {
				// Don't look at the center of the start location
				if(diffs[i][0] == - diffs[j][0]) continue;
				if(diffs[i][1] == - diffs[j][1]) continue;
				
				int willBeNextToMineX = willBeMineX+diffs[j][0];
				int willBeNextToMineY = willBeMineY+diffs[j][1];
				
				squareScore += locationScore[willBeNextToMineX][willBeNextToMineY];
			}
		}
		return squareScore;
	}
}
