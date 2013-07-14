package main.ai.rules.base;

import main.GameStatus;
import main.events.CoOrdinate;

public abstract class RecruitRule extends RuleStats {
	public RecruitRule(String name, String description, GameStatus gameStatus, int ourPlayerIndex) {
		super(name, description, gameStatus, ourPlayerIndex);
	}
	
	public RecruitRule(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus, int ourPlayerIndex) {
		super(name, description, weighting, order, actor, gameStatus, ourPlayerIndex);
	}
	
	/**
	 * Look for the place to add a recruit using the rule
	 * 
	 * @return	Best location, or null if no valid move
	 */
	public abstract CoOrdinate getBestRecruit();
}
