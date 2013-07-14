package main.ai.rules.base;

import main.GameStatus;

public abstract class RuleStats {
	public enum ACTOR {
		ADD, SUBTRACT, MULTIPLY, DIVIDE
	}
	/** The current status of the game */
	public final GameStatus gameStatus;
	/** The nice name of the rule */
	public String name;
	/** The description of the rule */
	public String description;
	/** The unique identifier for the attributes of this rule */
	public String configDescriptor;
	/** The weighting used to decide if we are going to use this rule */
	public int weighting;
	/** Not used */
	public int order;
	/** Not used */
	public ACTOR actor;
	
	public RuleStats(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus) {
		super();
		this.name = name;
		this.description = description;
		this.weighting = weighting;
		this.order = order;
		this.actor = actor;
		this.gameStatus = gameStatus;
		configDescriptor = "";
	}
	
	public RuleStats(String name, String description, GameStatus gameStatus) {
		super();
		this.name = name;
		this.description = description;
		this.gameStatus = gameStatus;
		configDescriptor = "";
		randomize();
	}


	public void randomize() {
		weighting = (int) (Math.random() * 100);
		order = (int) (Math.random() * 100);
		int rand = (int) (Math.random() * ACTOR.values().length);
		actor = ACTOR.values()[rand];
	}
	
	public void addAdditionalNameDetails(String additionalDetails) {
		name += additionalDetails;
	}
	
	public void addAdditionalDescritionDetails(String additionalDetails) {
		description += additionalDetails;
	}
	
	public void addConfigDescriptor(String descriptor) {
		configDescriptor += ":"+descriptor;
	}
	
	/**
	 * Set up the additional details strings:
	 * additionalDetails
	 * configDescriptor
	 */
	protected abstract void setAdditionalDescription();
	
	/**
	 * Resets the configDescriptor
	 */
	public void resetAdditionalDescriptionAndConfigDescriptor() {
		configDescriptor = "";
		setAdditionalDescription();
	}
}
