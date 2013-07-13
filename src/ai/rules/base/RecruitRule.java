package ai.rules.base;

import events.CoOrdinate;

public abstract class RecruitRule extends RuleStats {
	public RecruitRule(String name, String description) {
		super(name, description);
	}
	
	public RecruitRule(String name, String description, int weighting, int order, ACTOR actor) {
		super(name, description, weighting, order, actor);
	}
	
	/**
	 * Look for the place to add a recruit using the rule
	 * 
	 * @return	Best location, or null if no valid move
	 */
	public abstract CoOrdinate getBestRecruit();
}
