package main;

import main.Main;

public class NextPlayerThread implements Runnable {
	public static String playerTurnLockedBy = null;
	
	private NextPlayerThread() {
		if(playerTurnLockedBy == null) {
			throw new RuntimeException("Need to get playerDone lock");
		}
	}

	@Override
	public void run() {
		Main.nextPlayer();
		freePlayerDone();
	}

	private synchronized void freePlayerDone() {
		Logger.debug("Free playerDone");
		playerTurnLockedBy = null;
	}

	/**
	 * Lock the thread on selecting
	 */
	private static synchronized void lockOnPlayerDone() {
		waitOnPlayerDone();
		Logger.debug("Got playerDone");
		playerTurnLockedBy = Utils.niceStackTrace(new Throwable().getStackTrace());
	}

	/**
	 * Wait for the player to finish
	 */
	public static synchronized void waitOnPlayerDone() {
		waitOnPlayerDone(-1);
	}
	
	/**
	 * Wait for the player to finish
	 * 
	 * @param timeout 	Maximum milliseconds to wait
	 * 
	 * @return	Forced unlock
	 */
	public static synchronized boolean waitOnPlayerDone(int timeout) {
		long startWaiting = System.currentTimeMillis();
		Logger.debug("Waiting on playerDone");
		boolean output = false;
		while(playerTurnLockedBy != null && (timeout == -1 || startWaiting + timeout < System.currentTimeMillis())) {
			if(!output) {
				output = Logger.trace("Waiting on "+playerTurnLockedBy);
			}
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(playerTurnLockedBy != null) {
			playerTurnLockedBy = null;
			return true;
		} else {
			return false;
		}
	}
	
	public static synchronized void spawnNextPlayerThread() {
		lockOnPlayerDone();
		new Thread(new NextPlayerThread()).start();
	}
}
