package main;

import main.config.Config;
import main.enums.GameState;

public class Utils {
	/**
	 * Output a human readable stack trace
	 * 
	 * @param stackTrace
	 * @return
	 */
	public static String niceStackTrace(StackTraceElement[] stackTrace) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < stackTrace.length; i++) {
			sb.append(stackTrace[i].getClassName());
			sb.append("@");
			sb.append(stackTrace[i].getLineNumber());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Wait for the slow human to see what the computer player is doing
	 */
	public static void pauseForHumanToSee(GameStatus gameStatus) {
		// If a human is playing, slow the computer down so they can see the turns
		if(gameStatus.humanPlaying && Main.getGameState() == GameState.PLAYING) {
			try {
				Thread.yield();
				Thread.sleep(gameStatus.config.getInt(Config.KEY.INTERACTIVE_COMPUTER_PAUSE.getKey()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
