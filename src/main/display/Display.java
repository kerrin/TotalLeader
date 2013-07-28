package main.display;

import java.awt.FlowLayout;

import main.GameStatus;
import main.board.Board;


public class Display extends FlowLayout {
	private static final long serialVersionUID = 9146394266397723467L;
	private DisplayFrame displayFrame;
	
	private boolean initialised = false;
	private GameStatus gameStatus;

	public void init(GameStatus gameStatus, Board board) {
		this.gameStatus = gameStatus;
		if(initialised) return;
		initialised = true;
		
		displayFrame = new DisplayFrame();
		displayFrame.init(gameStatus, board, this);
	}
	
	public void newBoard(Board board) {
		displayFrame.newBoard(board);
	}

	public void outputDebug(String message) {
		displayFrame.outputDebug(message);
	}

	public void repaint() {
		displayFrame.showDebug(gameStatus.showDebug);
		displayFrame.repaint();
	}
}
