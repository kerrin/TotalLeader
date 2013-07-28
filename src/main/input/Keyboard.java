package main.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import main.GameStatus;
import main.Logger;

public class Keyboard implements KeyListener {
	private static HashMap<Integer,Boolean> keys;
	
	private final GameStatus gameStatus;
	
	public Keyboard(GameStatus gameStatus) {
		super();
		this.gameStatus = gameStatus;
		keys = new HashMap<Integer, Boolean>();
	}

	@Override
	public void keyPressed(KeyEvent key) {
		int keyCode = key.getKeyCode();
		keys.put(keyCode, true);
		Logger.checkChangeLogLevel(keyCode);
		if(keyCode == KeyEvent.VK_P) gameStatus.pause = !gameStatus.pause;
		if(keyCode == KeyEvent.VK_F1) gameStatus.showDebug = !gameStatus.showDebug;
	}

	@Override
	public void keyReleased(KeyEvent key) {
		int keyCode = key.getKeyCode();
		keys.remove(keyCode);
	}

	@Override
	public void keyTyped(KeyEvent key) {
		// Nothing
	}
	
	/** 
	 * Is the key pressed at the moment
	 * 
	 * @param keyCode
	 * @return
	 */
	public static boolean keyPressed(int keyCode) {
		return keys.containsKey(keyCode);
	}
	
	/** 
	 * Is the key pressed at the moment
	 * 
	 * @param keyCode
	 * @return
	 */
	public static short numKeyPressed() {
		for(short i=0; i<=9; i++) {
			if(keys.containsKey(KeyEvent.VK_0+i)) return i;
		}
		return -1;
	}
}
