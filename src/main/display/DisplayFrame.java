package main.display;

import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import main.GameStatus;
import main.board.Board;
import main.config.Config;

public class DisplayFrame extends JFrame {
	/**  */
	private static final long serialVersionUID = 6641650747729359220L;
	private static final int FRAME_WIDTH = 16;
	private static final int FRAME_HEIGHT = 38;
	public static final int SIDE_GUI_WIDTH = 40;
	public static final int BOTTOM_GUI_HEIGHT = 20;
	private BoardPanel boardPanel;
	private InputPanel inputPanel;
	private DebugPanel debugPanel;
	private LayoutManager layout;
	
	private boolean initialised = false;
	
	private GameStatus gameStatus;
	private boolean isInitialised = false;

	public void init(GameStatus gameStatus, Board board, LayoutManager layout) {
		this.gameStatus = gameStatus;
		this.layout = layout;
		if(initialised) return;
		initialised = true;
		setTitle("Total Leader");
		
		int squarePixels = gameStatus.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
        setSize(board.getWidth()*squarePixels+FRAME_WIDTH+SIDE_GUI_WIDTH, board.getHeight()*squarePixels+FRAME_HEIGHT+BOTTOM_GUI_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
				
		inputPanel = new InputPanel(layout, gameStatus);
		inputPanel.setSize(0,0);
		inputPanel.setAutoscrolls(true);
		inputPanel.setVisible(true);
		inputPanel.setLayout(layout);
		
		debugPanel = new DebugPanel(layout,board.getWidth()*squarePixels+FRAME_WIDTH+SIDE_GUI_WIDTH, board.getHeight()*squarePixels+FRAME_HEIGHT+BOTTOM_GUI_HEIGHT);
		debugPanel.setSize(board.getWidth()*squarePixels+FRAME_WIDTH+SIDE_GUI_WIDTH, board.getHeight()*squarePixels+FRAME_HEIGHT+BOTTOM_GUI_HEIGHT);
		debugPanel.setAutoscrolls(true);
		debugPanel.setLayout(layout);
		
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                setVisible(true);
            }
        });
	}
	
	public void newBoard(Board board) {
		boardPanel = new BoardPanel(board, gameStatus);
		boardPanel.setLayout(layout);
		int squarePixels = gameStatus.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
		boardPanel.setSize(board.getWidth()*squarePixels+FRAME_WIDTH+SIDE_GUI_WIDTH, board.getHeight()*squarePixels+FRAME_HEIGHT+BOTTOM_GUI_HEIGHT);
		if(!isInitialised) {
			add(debugPanel);
			add(inputPanel);
			add(boardPanel);
			isInitialised  = true;
		}
	}

	/**
	 * 
	 * @param message
	 */
	public void outputDebug(String message) {
		debugPanel.outputDebug(message);
	}

	public void showDebug(boolean showDebug) {
		debugPanel.setVisible(showDebug);
		boardPanel.setVisible(!showDebug);
	}
}
