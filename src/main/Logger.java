package main;

import java.awt.event.KeyEvent;

public class Logger {
	enum LEVEL {
		ERROR(0),
		WARN(1),
		INFO(2),
		DEBUG(3),
		FINE(4),
		TRACE(5);
		private int id;
		private LEVEL(int levelId) {
			this.id = levelId;
		}
		public static LEVEL fromString(String levelString) {
			for(LEVEL level:LEVEL.values()) {
				if(levelString.equalsIgnoreCase(level.name())) return level;
			}
			return INFO;
		}
	}
	
	private static GameStatus gameStatus;
	
	private static LEVEL level = LEVEL.INFO;
	
	public static void init(GameStatus thisGameStatus) {
		gameStatus = thisGameStatus;
	}
	
	public static boolean setLevel(LEVEL level) {
		if(level == Logger.level) return false;
		Logger.level = level;
		return true;
	}
	
	private static boolean message(LEVEL messageLevel, String message) {
		if(messageLevel.id <= level.id) {
			System.out.println(getLevelName(messageLevel)+getLocation()+message);
			if(gameStatus != null) {
				gameStatus.display.outputDebug(message);
			}
			return true;
		}
		return false;
	}
	
	private static String getLevelName(LEVEL messageLevel) {
		return messageLevel.name()+": ";
	}

	public static boolean error(String message) {
		return message(LEVEL.ERROR, message);
	}
	
	public static boolean warn(String message) {
		return message(LEVEL.WARN, message);
	}
	
	public static boolean info(String message) {
		return message(LEVEL.INFO, message);
	}
	
	public static boolean debug(String message) {
		return message(LEVEL.DEBUG, message);
	}
	
	public static boolean fine(String message) {
		return message(LEVEL.FINE, message);
	}
	
	public static boolean trace(String message) {
		return message(LEVEL.TRACE, message);
	}

	private static String getLocation() {
		StackTraceElement stack = new Throwable().getStackTrace()[3];
		return stack.getClassName()+"@"+stack.getLineNumber()+": ";
	}

	public static void checkChangeLogLevel(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_I:
			if(Logger.setLevel(LEVEL.INFO)) Logger.info("Set Log Level To Info");
			break;
		case KeyEvent.VK_D:
			if(Logger.setLevel(LEVEL.DEBUG)) Logger.info("Set Log Level To Debug");
			break;
		case KeyEvent.VK_F:
			if(Logger.setLevel(LEVEL.FINE)) Logger.info("Set Log Level To Fine Debug");
			break;
		case KeyEvent.VK_T:
			if(Logger.setLevel(LEVEL.TRACE)) Logger.info("Set Log Level To Trace");
			break;
		default:
			break;
		}
	}
}
