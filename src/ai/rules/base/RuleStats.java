package ai.rules.base;

public abstract class RuleStats {
	public enum ACTOR {
		ADD, SUBTRACT, MULTIPLY, DIVIDE
	}
	/**  */
	public String name;
	/**  */
	public String description;
	/**  */
	public String configDescriptor;
	/**  */
	public int weighting;
	/**  */
	public int order;
	/**  */
	public ACTOR actor;
	
	public RuleStats(String name, String description, int weighting, int order, ACTOR actor) {
		super();
		this.name = name;
		this.description = description;
		this.weighting = weighting;
		this.order = order;
		this.actor = actor;
		configDescriptor = "";
	}
	
	public RuleStats(String name, String description) {
		super();
		this.name = name;
		this.description = description;
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
}
