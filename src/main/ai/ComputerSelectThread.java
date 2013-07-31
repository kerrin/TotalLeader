package main.ai;

import main.GameStatus;
import main.Logger;
import main.Utils;
import main.board.Board;
import main.events.CoOrdinate;

public class ComputerSelectThread implements Runnable {
	private static String computerSelectLockedBy = null;
	
	private CoOrdinate coordinates;
	
	private final GameStatus gameStatus;
	
	private ComputerSelectThread(CoOrdinate coordiante, GameStatus gameStatus) {
		this.coordinates = coordiante;
		this.gameStatus = gameStatus;
		if(computerSelectLockedBy == null) {
			throw new RuntimeException("Need to get computerSelectDone lock");
		}
	}

	@Override
	public void run() {
		gameStatus.eventManager.selected(coordinates);
		freeComputerSelectDone();
	}

	private synchronized void freeComputerSelectDone() {
		computerSelectLockedBy = null;
	}

	/**
	 * Lock the thread on selecting
	 */
	public static synchronized void lockOnComputerSelect() {
		waitOnComputerSelect();
		Logger.debug("Got computerSelectDone");
		computerSelectLockedBy = Utils.niceStackTrace(new Throwable().getStackTrace());
	}

	/**
	 * Wait for the computer select lock to be freed
	 */
	public static synchronized void waitOnComputerSelect() {
		Logger.debug("Waiting on computerSelectDone");
		boolean output = false;
		while(computerSelectLockedBy != null) {
			if(!output) {
				output = Logger.trace("Waiting on "+computerSelectLockedBy);
			}
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Start a new computer select thread
	 * 
	 * @param coordinate
	 * @param gameStatus
	 * @param board
	 */
	public static synchronized void spawnComputerSelectThread(CoOrdinate coordinate, GameStatus gameStatus, Board board) {
		lockOnComputerSelect();
		new Thread(new ComputerSelectThread(coordinate, gameStatus)).start();
		board.getBoard()[coordinate.x][coordinate.y].setSelected(true);
		Utils.pauseForHumanToSee(gameStatus);
	}
}
