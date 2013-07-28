package main.ai.rules;

public class ComputerUtils {
	/**
	 * Gets an array of the 4 valid move deltas from the current square
	 * This array will be in a different order each time it is called, so store locally
	 * 
	 * @return	int[4][0=x/1=y]
	 * 	2
	 * 1x3
	 *  4
	 *  
	 *  Randomized order
	 */
	public static int[][] getRandomOrgtagonalMovesArray() {
		int[][] diffs = new int[4][2]; // [][0=x/1=y]
		diffs[0][0] = -1; diffs[0][1] = 0;
		diffs[1][0] = 0; diffs[1][1] = -1;
		diffs[2][0] = +1; diffs[2][1] = 0;
		diffs[3][0] = 0; diffs[3][1] = +1;
		
		while(Math.random() > 0.25) {
			int from = (int)(Math.random()*4);
			int to = (int)(Math.random()*4);
			while(from == to) to = (int)(Math.random()*4);
			swap(from, to, diffs);
		}
		
		return diffs;
	}

	/**
	 * Swap set at indexes "from" and "to"
	 * @param from
	 * @param to
	 * @param diffs
	 */
	private static void swap(int from, int to, int[][] diffs) {
		int[] temp = new int[2];
		temp[0] = diffs[from][0];
		temp[1] = diffs[from][1];
		diffs[from][0] = diffs[to][0];
		diffs[from][1] = diffs[to][1];
		diffs[to][0] = temp[0];
		diffs[to][1] = temp[1];
	}

	/**
	 * 
	 * @param array
	 */
	public static void randomiseArray(int[] array) {
		int temp;
		while(Math.random() > 0.25) {
			int from = (int)(Math.random()*array.length);
			int to = (int)(Math.random()*array.length);
			while(from == to) to = (int)(Math.random()*array.length);
			temp = array[from];
			array[from] = array[to];
			array[to] = temp;
		}
	}
}
