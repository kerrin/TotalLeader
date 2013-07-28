package main.display;

import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import main.GameStatus;
import main.input.Keyboard;



public class InputPanel extends JPanel {
	/**  */
	private static final long serialVersionUID = 628994246369182191L;
	
	private JTextArea inputText;
	
	public InputPanel(LayoutManager layout, GameStatus gameStatus) {
		super();
		inputText = new JTextArea();
		inputText.setEditable(false);
		inputText.setSize(1, 1);
		inputText.setAutoscrolls(true);
		inputText.setLayout(layout);
		inputText.addKeyListener(new Keyboard(gameStatus));
		add(inputText);
	}
}
