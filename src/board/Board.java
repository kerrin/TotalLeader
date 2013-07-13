package board;

import player.Player;
import config.Config;
import events.CoOrdinate;
import main.Main;

public class Board {
	private final int height;
	private final int width;
	private Square[/*x*/][/*y*/] board;
	
	public Board(int height, int width) {
		this.height = height;
		this.width = width;
		
		board = new Square[height][width];
	}
	
	public void init() {
		int landChance = Main.config.getInt(Config.KEY.LAND_CHANCE.getKey());
		int landClumpChance = Main.config.getInt(Config.KEY.LAND_CLUMPING_CHANCE.getKey());
		for(int y=0; y < height; y++) {
			for(int x=0; x < width; x++) {
				int chance = landChance;
				if(x > 0 && board[x-1][y].isLand()) chance += landClumpChance;
				if(y > 0 && board[x][y-1].isLand()) chance += landClumpChance;
				int rand = (int)(Math.random()*100) + 1; // 1 to 100
				Square.TYPE squareType = Square.TYPE.SEA;
				short units = 0;
				Player player;
				if(rand <= chance) {
					squareType = Square.TYPE.LAND;
					units = (short)((Math.random() * 9) + 1); // 1 to 9
					player = Main.players[Main.nativePlayerIndex];
					player.modifyTotalUnits(units);
				} else {
					player = Main.players[Main.seaPlayerIndex];
				}
				player.modifySquares(1);
				board[x][y] = new Square(squareType, player, units, new CoOrdinate(x,y));
			}	
		}
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public Square[][] getBoard() {
		return board;
	}

	public void startPlayer(Player player) {
		int x = player.getStartLocationX();
		int y = player.getStartLocationY();
		setStartSpace(player, x, y);
		setStartSpace(player, x-1, y);
		setStartSpace(player, x+1, y);
		setStartSpace(player, x, y-1);
		setStartSpace(player, x, y+1);
		board[x][y].setSelected(false);
	}

	private void setStartSpace(Player player, int x, int y) {
		short maxUnits = (short)Main.config.getInt(Config.KEY.MAX_UNITS.getKey());
		board[x][y].playerLoses();
		board[x][y].makeLand();
		board[x][y].setOwner(player);
		board[x][y].setUnits(maxUnits);
		player.modifySquares(1);
		player.modifyTotalUnits(maxUnits);
	}
}
