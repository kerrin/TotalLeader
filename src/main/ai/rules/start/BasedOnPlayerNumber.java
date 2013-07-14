package main.ai.rules.start;

import main.GameStatus;
import main.ai.rules.base.StartRule;
import main.config.Config;
import main.events.CoOrdinate;

public class BasedOnPlayerNumber extends StartRule {
	private static String NAME = "BasedPlayer";
	private static String DESCRIPTION = "Start Location Based On The Player Number";
	
	public BasedOnPlayerNumber(GameStatus gameStatus, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, gameStatus, ourPlayerIndex);
	}

	public BasedOnPlayerNumber(int weighting, int order, ACTOR actor, GameStatus gameStatus, int ourPlayerIndex) {
		super(NAME, DESCRIPTION, weighting, order, actor, gameStatus, ourPlayerIndex);
	}

	@Override
	public CoOrdinate getBestStart() {
		int boardWidth = gameStatus.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = gameStatus.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		int x = boardWidth / 2;
		int y = boardHeight / 2;
		if((gameStatus.currentPlayerIndex+1) % 2 == 0) {
			x += boardWidth/4;
		} else {
			x -= boardWidth/4;
		}
		if(gameStatus.currentPlayerIndex >= 2) {
			y += boardHeight/4;
		} else {
			y -= boardHeight/4;
		}
		return new CoOrdinate(x,y);
	}

	@Override
	protected void setAdditionalDescription() {
		// NOTHING
	}
}
