package ai.rules.recruit;

import board.Square;
import player.Player;
import main.Logger;
import main.Main;
import config.Config;
import events.CoOrdinate;
import ai.rules.base.RecruitRule;

public class AnyValidRecruit extends RecruitRule {
	private static String NAME = "Any Valid Recruit";
	private static String DESCRIPTION = "Find the first valid place to put a recruit. A last resort.";
	
	public AnyValidRecruit() {
		super(NAME, DESCRIPTION);
	}

	public AnyValidRecruit(int weighting, int order, ACTOR actor) {
		super(NAME, DESCRIPTION, weighting, order, actor);
	}

	@Override
	public CoOrdinate getBestRecruit() {
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = Main.players[Main.currentPlayerIndex];
		Square[][] board = Main.board.getBoard();
		for(int x = 0; x < boardWidth; x++) {
			for(int y = 0; y < boardHeight; y++) {
				if(!board[x][y].getOwner().equals(currentPlayer)) continue;
				// Check not maxed out square
				if(board[x][y].getUnits() == Main.config.getInt(Config.KEY.MAX_UNITS.getKey())) continue;
				// Valid move found
				CoOrdinate move = new CoOrdinate(x,y);
				Logger.debug(Main.currentPlayerIndex+") "+name+":"+move);
				return move;
			}
		}
		return null;
	}

	
}
