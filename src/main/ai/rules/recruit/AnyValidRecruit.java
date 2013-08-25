package main.ai.rules.recruit;

import main.GameStatus;
import main.Logger;
import main.ai.rules.base.RecruitRule;
import main.board.Board;
import main.board.Square;
import main.config.Config;
import main.events.CoOrdinate;
import main.player.Player;

public class AnyValidRecruit extends RecruitRule {
	private static String NAME = "Any Valid Recruit";
	private static String DESCRIPTION = "Find the first valid place to put a recruit. A last resort.";
	
	private final Board board;
	
	public AnyValidRecruit(GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, gameStatus, ourPlayerIndex);
		this.board = board;
	}

	public AnyValidRecruit(int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weighting, order, actor, gameStatus, ourPlayerIndex);
		this.board = board;
	}

	@Override
	public CoOrdinate getBestRecruit() {
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		Square[][] boardArray = board.getBoard();
		for(int x = 0; x < boardWidth; x++) {
			for(int y = 0; y < boardHeight; y++) {
				if(!boardArray[x][y].getOwner().equals(currentPlayer)) continue;
				// Check not maxed out square
				if(boardArray[x][y].getUnits() == gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey())) continue;
				// Valid move found
				CoOrdinate move = new CoOrdinate(x,y);
				Logger.debug(gameStatus.currentPlayerIndex+") "+name+":"+move);
				return move;
			}
		}
		return null;
	}

	@Override
	protected void setAdditionalDescription() {
		// Nothing
	}

	@Override
	public int getWeighting(boolean real) {
		return 0;
	}
}
