package main.display;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import main.GameStatus;
import main.board.Board;
import main.config.Config;
import main.input.Keyboard;


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
	
	private GameStatus gameStatus;

	public void init(GameStatus gameStatus, Board board) {
		this.gameStatus = gameStatus;
		if(initialised) return;
		initialised = true;
		setTitle("Total Leader");

		int squarePixels = gameStatus.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
        setSize(board.getWidth()*squarePixels+FRAME_WIDTH+SIDE_GUI_WIDTH, board.getHeight()*squarePixels+FRAME_HEIGHT+BOTTOM_GUI_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
		inputText = new JTextArea();
		inputText.addKeyListener(new Keyboard(gameStatus));
		inputText.setEditable(false);
		inputText.setSize(1, 1);
		inputText.setAutoscrolls(true);
		
		
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                setVisible(true);
            }
        });
	}
	
	public void newBoard(Board board) {
		boardDisplay = new BoardDisplay(board, gameStatus);
		add(inputText);
		add(boardDisplay);
	}

	public void outputDebug(String message) {
		String text = inputText.getText();
		text = message+"\n"+text;
		int length = text.length();
		if(length > 800) length = 800;
		inputText.setText(text.substring(0,length));
	}
}
