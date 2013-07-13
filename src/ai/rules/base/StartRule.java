package ai.rules.base;

import events.CoOrdinate;

public abstract class StartRule extends RuleStats {

	public StartRule(String name, String description) {
		super(name, description);
	}
	
	public StartRule(String name, String description, int weighting, int order, ACTOR actor) {
		super(name, description, weighting, order, actor);
	}
	
	public abstract CoOrdinate getBestStart();
}
