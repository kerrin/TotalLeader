package main.display;

import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.JTextArea;



public class DebugPanel extends JPanel {
	/**  */
	private static final long serialVersionUID = 628994246369182191L;
	
	private JTextArea debugText;
	
	public DebugPanel(LayoutManager layout, int sizeX, int sizeY) {
		super();
		debugText = new JTextArea();
		debugText.setEditable(false);
		debugText.setSize(sizeX, sizeY);
		debugText.setAutoscrolls(true);
		debugText.setLayout(layout);
		add(debugText);
	}
	
	/**
	 * 
	 * @param message
	 */
	public void outputDebug(String message) {
		String text = debugText.getText();
		while(!message.isEmpty()) {
			int messageLength = message.length();
			if(messageLength > 70) messageLength = 70;
			text = message.substring(message.length()- messageLength,message.length())+"\n"+text;
			message = message.substring(0,message.length()- messageLength);
		}
		int length = text.length();
		if(length > 5000) length = 5000;
		debugText.setText(text.substring(0,length));
	}
}
