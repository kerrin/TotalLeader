package ai.rules.base;

import ai.ComputerMove;

public abstract class PlayRule extends RuleStats {
	public PlayRule(String name, String description) {
		super(name, description);
	}
	
	public PlayRule(String name, String description, int weighting, int order, ACTOR actor) {
		super(name, description, weighting, order, actor);
	}
	
	/**
	 * Look for the best move using the rule
	 * 
	 * @return	Best move, or null if no valid move
	 */
	public abstract ComputerMove getBestMove();
	
	public String toString() {
		StringBuffer sb = new StringBuffer("PlayRule: ");
		sb.append(name);
		sb.append(": ");
		sb.append(description);
		
		return sb.toString();
	}
}
