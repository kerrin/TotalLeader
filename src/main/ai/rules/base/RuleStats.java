package main.ai.rules.base;

import main.GameStatus;
import main.Logger;
import main.ai.rules.ComputerUtils;
import main.config.Config;

public abstract class RuleStats {
	public enum ACTOR {
		ADD, SUBTRACT, MULTIPLY, DIVIDE
	}
	
	/** The id to pass if we don't want to save this rule to a config */
	public static final int CONFIG_DONT_SAVE = -2;
	/** The id to pass if we don't want to save this rule to a config */
	public static final int CONFIG_ANY_PLAYER = -1;
	/** Player ID to use for configs be mean me */
	public static final int CONFIG_US_PLAYER_ID = 0;
	/** Player ID to use for configs be mean an opponent */
	public static final int CONFIG_OPPONENT_PLAYER_ID = 1;
	/** Player ID to use for configs be mean the natives */
	public static final int CONFIG_NATIVE_PLAYER_ID = 2;
	/** Player ID to use for configs be mean the sea */
	public static final int CONFIG_SEA_PLAYER_ID = 3;
	
	/** The current status of the game */
	public final GameStatus gameStatus;
	/** The nice name of the rule */
	public String name;
	/** The nice name of the rule without attribute details */
	private final String baseName;
	/** The description of the rule */
	public String description;
	/** The description of the rule without attribute details */
	private final String baseDescription;
	/** The unique identifier for the attributes of this rule */
	public String configDescriptor;
	/** The weighting used to decide if we are going to use this rule */
	public int weighting;
	/** Not used */
	public int order;
	/** Not used */
	public ACTOR actor;
	/** The player index of the computer player with this rule */
	private final int ourPlayerIndex;
	/** A list of playerIndexs of the opponents */
	protected int[] opponentPlayerIndexes;
	
	/**
	 * C'tor
	 * 
	 * @param name				Nice name for rule
	 * @param description		Description of rule
	 * @param weighting			How likely are we to run the rule out of 100
	 * @param order				Not used
	 * @param actor				Not used
	 * @param gameStatus		The game status details
	 * @param ourPlayerIndex	The player index of the Computer Player using this rule
	 */
	public RuleStats(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus, int ourPlayerIndex) {
		super();
		this.name = name;
		this.baseName = name;
		this.description = description;
		this.baseDescription = description;
		this.weighting = weighting;
		this.order = order;
		this.actor = actor;
		this.gameStatus = gameStatus;
		this.ourPlayerIndex = ourPlayerIndex;
		populateOpponentPlayerIndexes(gameStatus, ourPlayerIndex);
		configDescriptor = "";
	}

	/**
	 * C'tor
	 * 
	 * @param name
	 * @param description
	 * @param gameStatus
	 * @param ourPlayerIndex
	 */
	public RuleStats(String name, String description, GameStatus gameStatus, int ourPlayerIndex) {
		super();
		this.name = name;
		this.baseName = name;
		this.description = description;
		this.baseDescription = description;
		this.gameStatus = gameStatus;
		this.ourPlayerIndex = ourPlayerIndex;
		populateOpponentPlayerIndexes(gameStatus, ourPlayerIndex);
		configDescriptor = "";
		randomize();
	}

	/**
	 * Populate the opponent player indexes array
	 * 
	 * @param gameStatus
	 * @param ourPlayerIndex
	 */
	private void populateOpponentPlayerIndexes(GameStatus gameStatus,
			int ourPlayerIndex) {
		int numberOfPlayers = gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
		opponentPlayerIndexes = new int[numberOfPlayers-1];
		int playerIndex = 0;
		int i = 0;
		while(playerIndex < numberOfPlayers) {
			if(playerIndex == ourPlayerIndex) {
				playerIndex++;
				continue;
			}
			opponentPlayerIndexes[i++] = playerIndex;
			playerIndex++;
		}
	}

	/**
	 * Randomise the weight, order and actor
	 */
	public void randomize() {
		weighting = (int) (Math.random() * 100);
		order = (int) (Math.random() * 100);
		int rand = (int) (Math.random() * ACTOR.values().length);
		actor = ACTOR.values()[rand];
	}
	
	/**
	 * 
	 * @param additionalDetails
	 */
	public void addAdditionalNameDetails(String additionalDetails) {
		name += additionalDetails;
	}
	
	/**
	 * 
	 * @param additionalDetails
	 */
	public void addAdditionalDescritionDetails(String additionalDetails) {
		description += additionalDetails;
	}
	
	/**
	 * 
	 * @param descriptor
	 */
	public void addConfigDescriptor(String descriptor) {
		configDescriptor += ":"+descriptor;
	}
	
	public int[] getPlayerIndexes(int rulePlayerIndex) {
		int[] rulePlayerIndexes;
		switch(rulePlayerIndex) {
		case CONFIG_US_PLAYER_ID: 
			rulePlayerIndexes = new int[1];
			rulePlayerIndexes[0] = ourPlayerIndex;
			break;
		case CONFIG_NATIVE_PLAYER_ID:
			rulePlayerIndexes = new int[1];
			rulePlayerIndexes[0] = gameStatus.nativePlayerIndex;
			break;
		case CONFIG_SEA_PLAYER_ID:
			rulePlayerIndexes = new int[1];
			rulePlayerIndexes[0] = gameStatus.seaPlayerIndex;
			break;
		case CONFIG_ANY_PLAYER:
			rulePlayerIndexes = new int[gameStatus.nativePlayerIndex];
			for(int playerIndex=0; playerIndex < gameStatus.nativePlayerIndex-1; playerIndex++) {
				rulePlayerIndexes[playerIndex] = playerIndex;
			}
			ComputerUtils.randomiseArray(rulePlayerIndexes);
			break;
		case CONFIG_OPPONENT_PLAYER_ID:
			rulePlayerIndexes = new int[gameStatus.nativePlayerIndex-1];
			int i=0;
			for(int playerIndex=0; playerIndex < gameStatus.nativePlayerIndex-1; playerIndex++) {
				if(playerIndex == ourPlayerIndex) continue;
				rulePlayerIndexes[i++] = playerIndex;
			}
			ComputerUtils.randomiseArray(rulePlayerIndexes);
			break;
		
		default:
			Logger.error("Invalid player index ("+rulePlayerIndex+") for rule");
			rulePlayerIndexes = new int[0];
		}
		
		return rulePlayerIndexes;
	}

	/**
	 * Get the player ID to use in the rule
	 * 
	 * @param rulePlayerIndex
	 * @return
	 */
	public int playerIndexForRule(int rulePlayerIndex) {
		return RuleStats.playerIndexForRule(rulePlayerIndex, ourPlayerIndex, gameStatus);
	}
	
	/**
	 * Get the player ID to save to a config file
	 * 
	 * @param rulePlayerIndex
	 * @param ourPlayerIndex
	 * @param gameStatus
	 * @return
	 */
	public static int playerIndexForRule(int rulePlayerIndex, int ourPlayerIndex, GameStatus gameStatus) {
		int anOponent = 1;
		if(anOponent == ourPlayerIndex) anOponent=2;
		if(rulePlayerIndex >=0 && rulePlayerIndex < gameStatus.nativePlayerIndex) {
			if(rulePlayerIndex == ourPlayerIndex) {
				return CONFIG_US_PLAYER_ID;
			} else {
				if(rulePlayerIndex != anOponent) return CONFIG_DONT_SAVE;
				return CONFIG_OPPONENT_PLAYER_ID;
			}
		} else if(rulePlayerIndex == gameStatus.nativePlayerIndex) {
			return CONFIG_NATIVE_PLAYER_ID;
		} else if(rulePlayerIndex == gameStatus.seaPlayerIndex) {
			return CONFIG_SEA_PLAYER_ID;
		}
		Logger.error("Unknown player ID" + rulePlayerIndex);
		return CONFIG_DONT_SAVE;
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
		name = baseName;
		description = baseDescription;
		setAdditionalDescription();
	}

	public boolean equals(RuleStats rule) {
		return (
				configDescriptor.equals(rule.configDescriptor) &&
				name.equals(rule.name) && 
				description.equals(rule.description) &&
				weighting == rule.weighting
				);
	}
	
	/**
	 * Randomly loop up or down within the range defined
	 * 
	 * @author Kerrin
	 *
	 */
	public class LoopUpOrDown{
		private int change;
		private int end;
		private int index;
		
		/**
		 * C'tor
		 * 
		 * @param min	The minimum VALID value
		 * @param max	The maximum VALID value
		 */
		public LoopUpOrDown(int min, int max) {
			change = (Math.random()>= 0.5?1:-1);
			if(change > 0) {
				index = min-1;
				end = max + 1;
			} else {
				index = max + 1;
				end = min-1;
			}
		}

		/**
		 * Move on, and return if we are done
		 * Call next at start of loop!
		 * 
		 * @return
		 */
		public boolean next() {
			index += change;
			return index != end;
		}
		
		/**
		 * Get the current index
		 * 
		 * @return
		 */
		public int getIndex() {
			return index;
		}
	}
}
