package main.ai.rules.base;

import main.GameStatus;
import main.events.CoOrdinate;

public abstract class StartRule extends RuleStats {

	public StartRule(String name, String description, GameStatus gameStatus) {
		super(name, description,gameStatus);
	}
	
	public StartRule(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus) {
		super(name, description, weighting, order, actor, gameStatus);
	}
	
	public abstract CoOrdinate getBestStart();
}
