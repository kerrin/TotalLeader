package ai.rules.start;

import main.Main;
import config.Config;
import events.CoOrdinate;
import ai.rules.base.StartRule;

public class BasedOnPlayerNumber extends StartRule {
	private static String NAME = "BasedPlayer";
	private static String DESCRIPTION = "Start Location Based On The Player Number";
	
	public BasedOnPlayerNumber() {
		super(NAME, DESCRIPTION);
	}

	public BasedOnPlayerNumber(int weighting, int order, ACTOR actor) {
		super(NAME, DESCRIPTION, weighting, order, actor);
	}

	@Override
	public CoOrdinate getBestStart() {
		int boardWidth = Main.config.getInt(Config.KEY.BOARD_WIDTH.getKey());
		int boardHeight = Main.config.getInt(Config.KEY.BOARD_HEIGHT.getKey());
		int x = boardWidth / 2;
		int y = boardHeight / 2;
		if((Main.currentPlayerIndex+1) % 2 == 0) {
			x += boardWidth/4;
		} else {
			x -= boardWidth/4;
		}
		if(Main.currentPlayerIndex >= 2) {
			y += boardHeight/4;
		} else {
			y -= boardHeight/4;
		}
		return new CoOrdinate(x,y);
	}
}
