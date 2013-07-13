package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import main.Logger;
import main.Main;

public class Keyboard implements KeyListener {
	private static HashMap<Integer,Boolean> keys;
	
	public Keyboard() {
		super();
		keys = new HashMap<Integer, Boolean>();
	}

	@Override
	public void keyPressed(KeyEvent key) {
		int keyCode = key.getKeyCode();
		keys.put(keyCode, true);
		Logger.checkChangeLogLevel(keyCode);
		if(keyCode == KeyEvent.VK_P) Main.pause = !Main.pause;
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
