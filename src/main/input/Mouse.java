package main.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


import main.GameStatus;
import main.board.Board;
import main.config.Config;

public class Mouse implements MouseListener {
	
	private final Board board;
	private int squarePixels;
	private int boardSizeX;
	private int boardSizeY;
	
	public Mouse(GameStatus gameStatus, Board board) {
		super();
		this.board = board;
		
		squarePixels = gameStatus.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
		boardSizeX = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		boardSizeY = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
	}

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
			board.getBoard()[selectedX][selectedY].mouseClicked();
		}
	}
	
}
