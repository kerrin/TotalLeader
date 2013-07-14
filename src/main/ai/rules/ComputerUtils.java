package main.ai.rules;

public class ComputerUtils {
	/**
	 * Gets an array of the 4 valid move deltas from the current square
	 * 
	 * @return	int[4][0=x/1=y]
	 * 	2
	 * 1x3
	 *  4
	 */
	public static int[][] getOrgtagonalMovesArray() {
		int[][] diffs = new int[4][2]; // [][0=x/1=y]
		diffs[0][0] = -1; diffs[0][1] = 0;
		diffs[1][0] = 0; diffs[1][1] = -1;
		diffs[2][0] = +1; diffs[2][1] = 0;
		diffs[3][0] = 0; diffs[3][1] = +1;
		return diffs;
	}
}
