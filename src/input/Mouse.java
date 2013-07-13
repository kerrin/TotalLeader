package input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import config.Config;

import main.Main;

public class Mouse implements MouseListener {
	int squarePixels = Main.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
	int boardSizeX = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
	int boardSizeY = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
	
	@Override
	public void mouseClicked(MouseEvent mouse) {
		// Nothing
	}

	@Override
	public void mouseEntered(MouseEvent mouse) {
		// Nothing, mouse entered game frame
	}

	@Override
	public void mouseExited(MouseEvent mouse) {
		// Nothing, mouse exited game frame
	}

	@Override
	public void mousePressed(MouseEvent mouse) {
		// Nothing
	}

	@Override
	public void mouseReleased(MouseEvent mouse) {
		int selectedX = mouse.getX()/squarePixels;
		int selectedY = mouse.getY()/squarePixels;
		
		if(selectedX < boardSizeX && selectedY < boardSizeY) {
			Main.board.getBoard()[selectedX][selectedY].mouseClicked();
		}
	}
	
}
