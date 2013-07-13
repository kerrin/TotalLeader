package ai;

import main.Logger;
import main.Main;
import main.Utils;
import events.CoOrdinate;

public class ComputerSelectThread implements Runnable {
	private static String computerSelectLockedBy = null;
	
	private CoOrdinate coordinates;
	
	private ComputerSelectThread(CoOrdinate coordiante) {
		this.coordinates = coordiante;
		if(computerSelectLockedBy == null) {
			throw new RuntimeException("Need to get computerSelectDone lock");
		}
	}

	@Override
	public void run() {
		Main.eventManager.selected(coordinates);
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
	
	public static synchronized void spawnComputerSelectThread(CoOrdinate coordiante) {
		lockOnComputerSelect();
		new Thread(new ComputerSelectThread(coordiante)).start();
	}
}
