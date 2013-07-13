package display;

import input.Keyboard;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import main.Main;
import config.Config;

import board.Board;

public class Display extends JFrame {
	/**  */
	private static final long serialVersionUID = 6641650747729359220L;
	private static final int FRAME_WIDTH = 16;
	private static final int FRAME_HEIGHT = 38;
	public static final int SIDE_GUI_WIDTH = 40;
	public static final int BOTTOM_GUI_HEIGHT = 20;
	private BoardDisplay boardDisplay;
	private JTextArea inputText;
	private boolean initialised = false;

	public void init(Board board) {
		if(initialised) return;
		initialised = true;
		setTitle("Total Leader");

		int squarePixels = Main.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
        setSize(board.getWidth()*squarePixels+FRAME_WIDTH+SIDE_GUI_WIDTH, board.getHeight()*squarePixels+FRAME_HEIGHT+BOTTOM_GUI_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
		inputText = new JTextArea();
		inputText.addKeyListener(new Keyboard());
		add(inputText);
		
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                setVisible(true);
            }
        });
	}
	
	public void newBoard(Board board) {
		boardDisplay = new BoardDisplay(board);
		add(boardDisplay);
	}
}
