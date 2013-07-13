package display;

import input.Mouse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

import player.Player;

import config.Config;

import main.GameState;
import main.Main;

import board.Board;
import board.Square;

public class BoardDisplay extends JPanel {
	/**  */
	private static final long serialVersionUID = 628994246369182191L;
	
	private Board board;
	private int squarePixels;
	private String font;
	private int fontSize;
	
	public BoardDisplay(Board board) {
		super();
		this.board = board;
		squarePixels = Main.config.getInt(Config.KEY.SQUARE_SIZE_PIXELS.getKey());
		font = Main.config.getString(Config.KEY.UNIT_FONT.getKey());
		fontSize = Main.config.getInt(Config.KEY.UNIT_FONT_SIZE.getKey());
		addMouseListener(new Mouse());
	}

	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
           RenderingHints.KEY_ANTIALIASING,                
           RenderingHints.VALUE_ANTIALIAS_ON);
		int boardHeight = board.getHeight();
		int boardWidth = board.getWidth();
		
        displayBoard(g, boardHeight, boardWidth);
        displayCurrentPlayerBorder(g, boardHeight, boardWidth);        
        displayPercentageBars(g, boardHeight, boardWidth);
        displayCurrentPlayerDetails(g, boardHeight, boardWidth);
        if(Main.getGameState() == GameState.GAME_OVER) {
        	displayScores(g, boardHeight, boardWidth);
        }
    }

	private void displayScores(Graphics g, int boardHeight, int boardWidth) {
		g.setColor(new Color(255,255,255,192));
    	g.fillRect(10, 10, (squarePixels*boardWidth)-10, (squarePixels*boardHeight)-10);
    	
    	int numPlayers = Main.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
    	g.setColor(Color.BLACK);
        g.setFont(new Font(font, Font.BOLD, fontSize));
    	for(int i=0; i < numPlayers; i++) {
    		int y = 30 + (i*(squarePixels*boardHeight/numPlayers));
    		Player currentPlayer = Main.players[i];
	        g.drawString("Player: " + i + " scored " + currentPlayer.getScore(), 30, y);
	        g.drawString("Units: " + currentPlayer.getTotalUnits() + ", Squares: " + currentPlayer.getTotalSquares(), 30, y+30);
    	}
	}

	private void displayCurrentPlayerDetails(Graphics g, int boardHeight, int boardWidth) {
		int bottomGuiY = squarePixels*boardHeight;
		if(Main.getGameState() == GameState.GAME_OVER) {
			if(Main.winner != null) showTurnStatus(g, bottomGuiY, "Winner was:" + Main.winner.getName());
		} else {
			Player currentPlayer = Main.players[Main.currentPlayerIndex];
	    	g.setColor(currentPlayer.getColor());
	    	g.fillRect(67, bottomGuiY, squarePixels, squarePixels);
	    	
			g.setColor(Color.BLACK);
	        g.setFont(new Font(font, Font.BOLD, fontSize));
	        g.drawString("Player: " + (Main.currentPlayerIndex+1), 4, bottomGuiY+Display.BOTTOM_GUI_HEIGHT-4);
	        
			if(Main.currentTurn < 1) {
				showTurnStatus(g, bottomGuiY, "Select Start Location");
			} else {
				g.drawString("Turn: " + Main.currentTurn, 110, bottomGuiY+Display.BOTTOM_GUI_HEIGHT-4);
				if(Main.getGameState() == GameState.PLAYING_RECRUITMENT) {
					showTurnStatus(g, bottomGuiY, "Recruits: "+currentPlayer.getRecruits());
				} else {
					showTurnStatus(g, bottomGuiY, "Units: "+currentPlayer.getTotalUnits()+",Sq: "+currentPlayer.getTotalSquares());
				}
			}
		}
		
		g.setColor(Color.BLACK);
		g.drawLine(0, squarePixels*boardHeight, squarePixels*boardWidth+Display.SIDE_GUI_WIDTH, squarePixels*boardHeight);
	}

	private void showTurnStatus(Graphics g, int bottomGuiY, String message) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(font, Font.BOLD, fontSize));
		g.drawString(message, 200, bottomGuiY+Display.BOTTOM_GUI_HEIGHT-4);
	}

	private void displayPercentageBars(Graphics g, int boardHeight, int boardWidth) {
		int landBarY = 0;
        int unitsBarY = 0;
		int totalUnits = 0;
		int totalSquares = boardWidth*boardHeight;
        for(int player=0; player <= Main.seaPlayerIndex; player++) {
        	Player thisPlayer = Main.players[player];
        	if(thisPlayer == null) continue;
        	totalUnits += thisPlayer.getTotalUnits();
        }
        for(int player=0; player <= Main.seaPlayerIndex; player++) {
        	Player thisPlayer = Main.players[player];
        	if(thisPlayer == null) continue;
        	g.setColor(thisPlayer.getColor());
        	int playerSquares = thisPlayer.getTotalSquares();
        	int landBarYEnd = landBarY;
        	double landPercent = 0;
        	if(playerSquares > 0) {
        		landPercent = playerSquares/(totalSquares*1.0);
        	}
    	 	landBarYEnd += (boardHeight*squarePixels*landPercent);
    	 	//System.out.println(player+"=>Land: PS:"+playerSquares+",bY:"+landBarY+",byE:"+landBarYEnd+",lp:"+landPercent);
    	 	g.fillRect(squarePixels*boardWidth, landBarY, Display.SIDE_GUI_WIDTH/2, landBarYEnd-landBarY);
    	 	landBarY = landBarYEnd;
    	 	
    	 	int playerUnits = thisPlayer.getTotalUnits();
        	int unitsBarYEnd = unitsBarY;
        	double unitsPercent = 0;
        	if(playerUnits > 0) {
        		unitsPercent = playerUnits/(totalUnits*1.0);
        	}
    	 	unitsBarYEnd += (boardHeight*squarePixels*unitsPercent);
    	 	//System.out.println(player+"=>Units P:"+playerUnits+",bY:"+unitsBarY+",byE:"+unitsBarYEnd+",up:"+unitsPercent);
    	 	g.fillRect(squarePixels*boardWidth+(Display.SIDE_GUI_WIDTH/2), unitsBarY, Display.SIDE_GUI_WIDTH/2, unitsBarYEnd-unitsBarY);
    	 	unitsBarY = unitsBarYEnd;
        }
        g.setColor(Color.BLACK);
        g.drawLine(squarePixels*boardWidth, 0, squarePixels*boardWidth, squarePixels*boardHeight-1+Display.BOTTOM_GUI_HEIGHT);
        g.drawLine(squarePixels*boardWidth+(Display.SIDE_GUI_WIDTH/2), 0, squarePixels*boardWidth+(Display.SIDE_GUI_WIDTH/2), squarePixels*boardHeight-1+Display.BOTTOM_GUI_HEIGHT);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font(font, Font.BOLD, fontSize));
        g.drawString("L", squarePixels*boardWidth+4, squarePixels*boardHeight-4+Display.BOTTOM_GUI_HEIGHT);
        g.drawString("U", squarePixels*boardWidth+(Display.SIDE_GUI_WIDTH/2)+4, squarePixels*boardHeight-4+Display.BOTTOM_GUI_HEIGHT);
	}

	private void displayCurrentPlayerBorder(Graphics g, int boardHeight, int boardWidth) {
		if(Main.getGameState() == GameState.PLAYING) {
        	Player currentPlayer = Main.players[Main.currentPlayerIndex];
        	g.setColor(currentPlayer.getColor());
	        g.drawRect(0, 0, (squarePixels*boardWidth)-1, (squarePixels*boardHeight)-1);
	        g.drawRect(1, 1, (squarePixels*boardWidth)-2, (squarePixels*boardHeight)-2);
        }
	}

	private void displayBoard(Graphics g, int boardHeight, int boardWidth) {
		for(int y=0; y < boardHeight; y++) {
			for(int x=0; x < boardWidth; x++) {
				Square square = board.getBoard()[x][y];
		        int squareX = x*squarePixels;
		        int squareY = y*squarePixels;
		        int units = square.getUnits();
		        Color bgColor = square.getOwner().getColor();
		        
		        //draw background
		        g.setColor(bgColor);
		        g.fillRect(squareX, squareY, squarePixels, squarePixels);
		        if(square.selected()) {
			        g.setColor(Color.BLACK);
			        g.drawRect(squareX, squareY, squarePixels-1, squarePixels-1);
		        }
		        if(units > 0) {
			        // Show Units
			        g.setColor(Color.BLACK);
			        g.setFont(new Font(font, Font.BOLD, fontSize));
			        int fontX = squareX + ((squarePixels-fontSize) / 2) + 4;
			        int fontY = squareY+squarePixels - ((squarePixels-fontSize) / 2) - 3;
			        g.drawString(String.valueOf(units), fontX, fontY);
		        }
			}
        }
	}
}
