package main.ai.rules.base;

import main.GameStatus;
import main.events.CoOrdinate;

public abstract class RecruitRule extends RuleStats {
	public RecruitRule(String name, String description, GameStatus gameStatus) {
		super(name, description, gameStatus);
	}
	
	public RecruitRule(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus) {
		super(name, description, weighting, order, actor, gameStatus);
	}
	
	/**
	 * Look for the place to add a recruit using the rule
	 * 
	 * @return	Best location, or null if no valid move
	 */
	public abstract CoOrdinate getBestRecruit();
}
