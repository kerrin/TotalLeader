package main.ai.rules.base;

import main.GameStatus;
import main.events.CoOrdinate;

public abstract class StartRule extends RuleStats {

	public StartRule(String name, String description, GameStatus gameStatus, int ourPlayerIndex) {
		super(name, description,gameStatus, ourPlayerIndex);
	}
	
	public StartRule(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus, int ourPlayerIndex) {
		super(name, description, weighting, order, actor, gameStatus, ourPlayerIndex);
	}
	
	public abstract CoOrdinate getBestStart();
}
