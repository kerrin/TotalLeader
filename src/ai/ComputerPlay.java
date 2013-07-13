package ai;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import config.Config;

import events.CoOrdinate;

import main.FileManager;
import main.GameState;
import main.Logger;
import main.Main;
import main.Utils;
import ai.rules.base.PlayRule;
import ai.rules.base.RecruitRule;
import ai.rules.play.AnyValidMove;
import ai.rules.play.BuildBridge;
import ai.rules.play.FindANumberNearMe;
import ai.rules.play.FindANumberThatICanBuildLandTo;
import ai.rules.play.MoveToEdge;
import ai.rules.recruit.AnyValidRecruit;
import ai.rules.recruit.FindNumber;
import ai.ComputerMove;

/**
 * A computer player
 * Sets up the rules
 * Picks a rule and runs the move during the game
 * 
 * @author Kerrin
 *
 */
public class ComputerPlay implements Runnable {
	public enum TYPE {
		BEST, 		// The highest ranked player. Must be first or POTLUCK breaks
		NEW, 		// A random new player
		RANDOM, 	// A random player
		MERGE, 		// Combine 2 players to be come a new player
		MODIFIED, 	// A random player with random modifications
		POTLUCK;	// Randomly pick from other type (except Best);

		public static TYPE fromString(String typeStr) {
			TYPE[] values = TYPE.values();
			for(TYPE type:values) {
				if(typeStr.equalsIgnoreCase(type.name())) return type;
			}
			return POTLUCK;
		}
	}

	/** Player ID to use for configs be mean me */
	private static final int CONFIG_US_PLAYER_ID = 0;
	/** Player ID to use for configs be mean an opponent */
	private static final int CONFIG_OPPONENT_PLAYER_ID = 1;

	/** Track what locked the computer thread */
	public static String computerThreadLockedBy = null;

	/** Did this Computer Player win */
	public boolean winner = false;
	
	/** The filename of this Computer Player, if this was loaded from a file and unmodified */
	public String filename;
	/** The scores this player has got previously */
	private int[] previousScores;

	/** Count the turns this computer player has had */
	private int turns = 0;
	/** What is my index in the players array */
	private int playerIndex;
	/** All the rules for a standard move */
	private Vector<PlayRule> playRules;
	/** All the rules for recruit placement */
	private Vector<RecruitRule> recruitRules;
	/** The rule to find a valid move if all else fails. Will probably never run */
	private AnyValidMove anyValidMove;
	/** The rule to find anywhere to place a recruit. Will probably never run */
	private AnyValidRecruit anyValidRecruit;
	/** The number of units this player wants to move */
	public short moveUnits;
	/** The total of all weightings for the play rules, so we can determine the chance of each rule running */
	private long totalPlayRuleWeights = 0;
	/** The total of all weightings for the recruit rules, so we can determine the chance of each rule running */
	private long totalRecruitRuleWeights = 0;
	
	
	public ComputerPlay(String filename, String configContent) {
		this.playerIndex = -1;
		this.filename = filename;
		ComputerPlayConfig config = new ComputerPlayConfig(configContent);
		setUpRulesFromConfigFile(config);
	}
	
	/**
	 * C'tor
	 * 
	 * @param playerIndex	My index in the players array
	 */
	public ComputerPlay(int playerIndex) {
		this.playerIndex = playerIndex;
		playRules = new Vector<PlayRule>();
		for(int b=0; b < 2; b++) { // Self Move
			for(int near=4; near > 0; near--) { // Surrounded by me
				for(int playerId = Main.nativePlayerIndex; playerId >= -1; playerId--) { // Move to a players square
					playRules.add(new FindANumberNearMe(1, playerId, b==1, near));
					playRules.add(new FindANumberNearMe(2, playerId, b==1, near));
					playRules.add(new FindANumberNearMe(0, playerId, b==1, near));
					// Look for the rest in order
					for(int i=3; i <= 9; i++) playRules.add(new FindANumberNearMe(i, playerId, b==1, near));
				}
			}
		}
		for(int b=0; b < 2; b++) {
			for(int near=4; near > 0; near--) {
				for(int playerId = Main.nativePlayerIndex; playerId >= -1; playerId--) {
					playRules.add(new FindANumberThatICanBuildLandTo(1, playerId, b==1, near));
					playRules.add(new FindANumberThatICanBuildLandTo(2, playerId, b==1, near));
					playRules.add(new FindANumberThatICanBuildLandTo(0, playerId, b==1, near));
					// Look for the rest in order
					for(int i=3; i <= 9; i++) playRules.add(new FindANumberThatICanBuildLandTo(i, playerId, b==1, near));
				}
			}
		}
		playRules.add(new BuildBridge());
		for(MoveToEdge.CONDITIONS condition:MoveToEdge.CONDITIONS.values()) {
			playRules.add(new MoveToEdge(condition));
		}
		
		// Get the total weightings for play rules
		for(PlayRule rule:playRules) totalPlayRuleWeights += rule.weighting;
		anyValidMove = new AnyValidMove();
		
		recruitRules = new Vector<RecruitRule>();
		for(int i=8; i >= 0; i--) {
			for(int j=0; j <= 9; j++) {
				for(int player=Main.nativePlayerIndex; player >= 0; player--) {
					for(int max=4; max > -1; max--) {
						for(int min=4; min > -1; min--) {
							if(max == -1 || min == -1 || max>=min) {
								Logger.trace("N"+i+","+min+"to"+max);
								recruitRules.add(new FindNumber(i,j,max,min,player));
							}
						}
					}
				}
			}
		}
		// Get the total weightings for play rules
		for(RecruitRule rule:recruitRules) totalRecruitRuleWeights += rule.weighting;
		anyValidRecruit = new AnyValidRecruit();
	}
	
	/**
	 * Set the player index on a ComputerPlay object that hasn't got one yet
	 * @param index
	 */
	public void setPlayerIndex(int index) {
		if(playerIndex != -1) throw new RuntimeException("Tried to reset player index");
		playerIndex = index;
	}
	
	/**
	 * Get the player index this computer player is for
	 * 
	 * @return
	 */
	public int getPlayerIndex() {
		return playerIndex;
	}

	/**
	 * Take a move
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		// Do computer stuff
		if(Main.getGameState() == GameState.PLAYING_RECRUITMENT) {
			int recruits = Main.players[Main.currentPlayerIndex].getRecruits();
			Logger.debug("Placing "+recruits+" recruits");
			CoOrdinate place = null;
			short maxUnits = (short)Main.config.getInt(Config.KEY.MAX_UNITS.getKey());
			boolean quickRecruit = Main.config.getInt(Config.KEY.COMPUTER_QUICK_RECRUIT.getKey()) == 1;
			for(int i=0; i < recruits; i++) {
				ComputerSelectThread.waitOnComputerSelect();
				if(quickRecruit && place != null && Main.board.getBoard()[place.x][place.y].getUnits() >= maxUnits) {
					place = null;
				}
				Vector<RecruitRule> thisTurnsRules = (Vector<RecruitRule>)recruitRules.clone();
				while(!thisTurnsRules.isEmpty() && place == null) {
					long pickRule = (long)(Math.random()*totalPlayRuleWeights);
					Iterator<RecruitRule> iter = thisTurnsRules.iterator();
					while(place == null && iter.hasNext()) {
						RecruitRule rule = iter.next();
						pickRule -= rule.weighting;
						if(pickRule > 0) continue;
						place = rule.getBestRecruit();
						if(place == null) {
							iter.remove();
							Logger.fine("xxx Failed Recruit Rule: "+rule.name+" xxx");
							break;
						}
					}
				}
				if(place == null) place = anyValidRecruit.getBestRecruit();
				ComputerSelectThread.spawnComputerSelectThread(place);
			}
			Logger.debug("Computer "+playerIndex+" recruited on turn "+turns);
		} else {
			ComputerSelectThread.waitOnComputerSelect();
			ComputerMove move = null;
			Vector<PlayRule> thisTurnsRules = (Vector<PlayRule>)playRules.clone();
			while(!thisTurnsRules.isEmpty() && move == null) {
				long pickRule = (long)(Math.random()*totalPlayRuleWeights);
				Iterator<PlayRule> iter = thisTurnsRules.iterator();
				while(move == null && iter.hasNext()) {
					PlayRule rule = iter.next();
					pickRule -= rule.weighting;
					if(pickRule > 0) continue;
					move = rule.getBestMove();
					if(move == null) {
						iter.remove();
						Logger.fine("xxx Failed Rule: "+rule.name+" xxx");
						break;
					}
				}
			}
			if(move == null) move = anyValidMove.getBestMove();
			Logger.debug(Main.currentPlayerIndex+")Moving "+move.units+" from "+move.from.x+","+move.from.y+"=>"+move.to.x+","+move.to.y);
			moveUnits = move.units;
			ComputerSelectThread.spawnComputerSelectThread(move.from);
			ComputerSelectThread.spawnComputerSelectThread(move.to);
			turns++;
			Logger.debug("Computer "+playerIndex+" had turn "+turns);
		}
		freeComputerPlayer();
	}
	
	/**
	 * Release the computer player lock
	 */
	private synchronized void freeComputerPlayer() {
		Logger.debug("Free computerThreadLockedBy");
		computerThreadLockedBy = null;
	}

	/**
	 * Lock the thread on selecting
	 */
	private static synchronized void lockComputerPlayer() {
		waitComputerPlayer();
		Logger.debug("Got computerThreadLockedBy");
		computerThreadLockedBy = Utils.niceStackTrace(new Throwable().getStackTrace());
	}

	/**
	 * Return when the lock is free
	 * Calling function must also be synchronised or the lock may be taken
	 */
	public static synchronized void waitComputerPlayer() {
		Logger.debug("Waiting on playerDone");
		boolean output = false;
		while(computerThreadLockedBy != null) {
			if(!output) {
				output = Logger.trace("Waiting on "+computerThreadLockedBy);
			}
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Spawn a computer player thread to take a move
	 * 
	 * @param computerPlayer
	 */
	public static synchronized void spawnComputerPlayThread(ComputerPlay computerPlayer) {
		lockComputerPlayer();
		new Thread(computerPlayer).start();
	}

	/**
	 * Generate a unique filename for this ComputerPlay
	 * 
	 * @return	Filename
	 */
	public String getConfigFilename() {
		StringBuffer sb = new StringBuffer("");
		int averageScore = getAverageScore();
		sb.append(averageScore);
		sb.append("-");
		sb.append(System.currentTimeMillis());

		sb.append(".tl-gene");
		return sb.toString();
	}

	/**
	 * Generate the config for this ComputerPlay
	 * 
	 * @return	Config for writting to file
	 */
	public String getConfigFileContents() {
		int maxPlayers = Main.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey());
		int anOponent = 0;
		if(anOponent == playerIndex) anOponent++;
		StringBuffer sb = new StringBuffer("");
		
		sb.append("SCORE=");
		sb.append(Main.players[playerIndex].getScore());
		if(previousScores != null) {
			for(int score:previousScores) {
				sb.append(",");
				sb.append(score);
			}
		}
		sb.append("\n");
		sb.append("PLAY\n");
		Iterator<PlayRule> playIter = playRules.iterator();
		while(playIter.hasNext()) {
			PlayRule rule = playIter.next();
			if(rule instanceof FindANumberNearMe) {
				FindANumberNearMe r = (FindANumberNearMe) rule;
				int rulePlayerIndex = r.getPlayerIndex();
				if(rulePlayerIndex >=0 && rulePlayerIndex < maxPlayers) {
					if(rulePlayerIndex == playerIndex) {
						r.setPlayerIndex(CONFIG_US_PLAYER_ID);
					} else {
						if(rulePlayerIndex != anOponent) continue;
						r.setPlayerIndex(CONFIG_OPPONENT_PLAYER_ID);
					}
				}
			} else if(rule instanceof FindANumberThatICanBuildLandTo) {
				FindANumberThatICanBuildLandTo r = (FindANumberThatICanBuildLandTo) rule;
				int rulePlayerIndex = r.getPlayerIndex();
				if(rulePlayerIndex >=0 && rulePlayerIndex < maxPlayers) {
					if(rulePlayerIndex == playerIndex) {
						r.setPlayerIndex(CONFIG_US_PLAYER_ID);
					} else {
						if(rulePlayerIndex != anOponent) continue;
						r.setPlayerIndex(CONFIG_OPPONENT_PLAYER_ID);
					}
				}
			}
			sb.append(rule.getClass().getSimpleName());
			sb.append(rule.configDescriptor);
			sb.append(">");
			sb.append("w=");
			sb.append(rule.weighting);
			sb.append("\n");
		}
		sb.append("RECRUIT\n");
		Iterator<RecruitRule> recruitIter = recruitRules.iterator();
		while(recruitIter.hasNext()) {
			RecruitRule rule = recruitIter.next();
			if(rule instanceof FindNumber) {
				FindNumber r = (FindNumber) rule;
				int rulePlayerIndex = r.getRequiredAnAdjacentSquareThatIsPlayer();
				if(rulePlayerIndex >=0 && rulePlayerIndex < maxPlayers) {
					if(rulePlayerIndex == playerIndex) {
						r.setRequiredAnAdjacentSquareThatIsPlayer(CONFIG_US_PLAYER_ID);
					} else {
						if(rulePlayerIndex != anOponent) continue;
						r.setRequiredAnAdjacentSquareThatIsPlayer(CONFIG_OPPONENT_PLAYER_ID);
					}
				}
			}
			sb.append(rule.getClass().getSimpleName());
			sb.append(rule.configDescriptor);
			sb.append(">");
			sb.append("w=");
			sb.append(rule.weighting);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Set up the rules based on a gene config file
	 * 
	 * @param config
	 */
	private void setUpRulesFromConfigFile(ComputerPlayConfig config) {
		HashMap<String, PlayRule> configPlayRule = config.getPlayRules();
		HashMap<String, RecruitRule> configRecruitRule = config.getRecruitRules();
		previousScores = config.getScores();
		playRules = new Vector<PlayRule>();
		for(int b=0; b < 2; b++) { // Self Move
			for(int near=4; near > 0; near--) { // Surrounded by me
				for(int playerId = Main.nativePlayerIndex; playerId >= -1; playerId--) { // Move to a players square
					setUpFindANumberNearMe(configPlayRule, 1, playerId, b==1, near);
					setUpFindANumberNearMe(configPlayRule, 2, playerId, b==1, near);
					setUpFindANumberNearMe(configPlayRule, 0, playerId, b==1, near);
					
					// Look for the rest in order
					for(int i=3; i <= 9; i++) setUpFindANumberNearMe(configPlayRule, i, playerId, b==1, near);
				}
			}
		}
		for(int b=0; b < 2; b++) {
			for(int near=4; near > 0; near--) {
				for(int playerId = Main.nativePlayerIndex; playerId >= -1; playerId--) {
					setUpFindANumberThatICanBuildLandTo(configPlayRule, 1, playerId, b==1, near);
					setUpFindANumberThatICanBuildLandTo(configPlayRule, 2, playerId, b==1, near);
					setUpFindANumberThatICanBuildLandTo(configPlayRule, 0, playerId, b==1, near);
					// Look for the rest in order
					for(int i=3; i <= 9; i++) setUpFindANumberThatICanBuildLandTo(configPlayRule, i, playerId, b==1, near);
				}
			}
		}
		playRules.add(new BuildBridge());
		for(MoveToEdge.CONDITIONS condition:MoveToEdge.CONDITIONS.values()) {
			setUpMoveToEdge(configPlayRule, condition);
		}
		
		// Get the total weightings for play rules
		for(PlayRule rule:playRules) totalPlayRuleWeights += rule.weighting;
		anyValidMove = new AnyValidMove();
		
		recruitRules = new Vector<RecruitRule>();
		for(int i=8; i >= 0; i--) {
			for(int j=0; j <= 9; j++) {
				for(int player=Main.nativePlayerIndex; player >= 0; player--) {
					for(int max=4; max > -1; max--) {
						for(int min=4; min > -1; min--) {
							if(max == -1 || min == -1 || max>=min) {
								Logger.trace("N"+i+","+min+"to"+max);
								setUpFindNumber(configRecruitRule, i,j,max,min,player);
							}
						}
					}
				}
			}
		}
		// Get the total weightings for play rules
		for(RecruitRule rule:recruitRules) totalRecruitRuleWeights += rule.weighting;
		anyValidRecruit = new AnyValidRecruit();
	}

	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configPlayRule
	 * @param ntf				Number to find
	 * @param selfMove			Is a self move
	 * @param near				Required Adjacent Square That Are Us
	 * @param playerIndex			Player to be near
	 */
	private void setUpFindANumberNearMe(HashMap<String, PlayRule> configPlayRule, int ntf, int playerIndex, boolean selfMove, int near) {
		int playerIndexConfig;
		if(playerIndex < Main.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey())) {
			if(playerIndex == playerIndex) {
				playerIndexConfig = CONFIG_US_PLAYER_ID;
			} else {
				playerIndexConfig = CONFIG_OPPONENT_PLAYER_ID;
			}
		} else {
			playerIndexConfig = playerIndex;
		}
		FindANumberNearMe configRule = (FindANumberNearMe)configPlayRule.get(
				FindANumberNearMe.getConfigDescriptor(ntf, playerIndexConfig, selfMove, near));
		if(configRule != null) {
			configRule.setPlayerIndex(playerIndex);
			configRule.setAdditionalDescription();
			playRules.add(configRule);
		} else {
			playRules.add(new FindANumberNearMe(ntf, playerIndex, selfMove, near));
		}
	}
	
	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configPlayRule
	 * @param ntf				Number to find
	 * @param playerIndex			Player to be near
	 * @param selfMove			Is a self move
	 * @param near				Required Adjacent Square That Are Us
	 */
	private void setUpFindANumberThatICanBuildLandTo(HashMap<String, PlayRule> configPlayRule, int ntf, int playerIndex, boolean selfMove, int near) {
		int playerIndexConfig;
		if(playerIndex < Main.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey())) {
			if(playerIndex == playerIndex) {
				playerIndexConfig = CONFIG_US_PLAYER_ID;
			} else {
				playerIndexConfig = CONFIG_OPPONENT_PLAYER_ID;
			}
		} else {
			playerIndexConfig = playerIndex;
		}
		FindANumberThatICanBuildLandTo configRule = (FindANumberThatICanBuildLandTo)configPlayRule.get(
				FindANumberThatICanBuildLandTo.getConfigDescriptor(ntf, playerIndexConfig, selfMove, near));
		if(configRule != null) {
			configRule.setPlayerIndex(playerIndex);
			configRule.setAdditionalDescription();
			playRules.add(configRule);
		} else {
			playRules.add(new FindANumberThatICanBuildLandTo(ntf, playerIndex, selfMove, near));
		}
	}
	
	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configPlayRule
	 * @param cond
	 */
	private void setUpMoveToEdge(HashMap<String, PlayRule> configPlayRule, MoveToEdge.CONDITIONS cond) {
		MoveToEdge configRule = (MoveToEdge)configPlayRule.get(
				MoveToEdge.getConfigDescriptor(cond));
		if(configRule != null) {
			playRules.add(configRule);
		} else {
			playRules.add(new MoveToEdge(cond));
		}
	}
	
	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configRecruitRule
	 * @param ntfu				Number to find Us
	 * @param ntfo				Number to find opponent
	 * @param mastau			Maximum Adjacent Square That Are Us
	 * @param rastau			Required Adjacent Square That Are Us
	 * @param raastip			Require an adjacent square that is player
	 */
	private void setUpFindNumber(HashMap<String, RecruitRule> configRecruitRule, int ntfu, int ntfo, int mastau, int rastau, int raastip) {
		int raastipConfig;
		if(raastip < Main.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey())) {
			if(raastip == playerIndex) {
				raastipConfig = CONFIG_US_PLAYER_ID;
			} else {
				raastipConfig = CONFIG_OPPONENT_PLAYER_ID;
			}
		} else {
			raastipConfig = raastip;
		}
		FindNumber configRule = (FindNumber)configRecruitRule.get(
				FindNumber.getConfigDescriptor(ntfu, ntfo, mastau, rastau, raastipConfig));
		if(configRule != null) {
			configRule.setRequiredAnAdjacentSquareThatIsPlayer(raastip);
			configRule.setAdditionalDescription();
			recruitRules.add(configRule);
		} else {
			recruitRules.add(new FindNumber(ntfu, ntfo, mastau, rastau, raastip));
		}
	}

	/**
	 * Get a computer player
	 * 
	 * @param playerIndex
	 * @return
	 */
	public static ComputerPlay getComputerPlayer(int playerIndex) {
		/** 
		 * The types of players:
		 * Best 		The highest ranked player
		 * New			A random new player
		 * Random		A random player (not best)
		 * Merge		Combine 2 players to be come a new player
		 * Modified		A random player with random modifications
		 * PotLuck		Randomly pick from other type (except Best)
		 */
		String typeStr = Main.config.getString(Config.KEY.COMPUTER_PLAYER_TYPE.getKey(),playerIndex);
		ComputerPlay.TYPE type = ComputerPlay.TYPE.fromString(typeStr);
		File[] files = FileManager.listAllFiles();
		Logger.info("Computer Player "+playerIndex+" is type " + type.name());
		if(type == TYPE.POTLUCK) {
			int random = (int)(Math.random()*(TYPE.values().length-2))+1;
			type = TYPE.values()[random];
			Logger.info("Computer Player "+playerIndex+" is now type " + type.name());
		}
		switch (type) {
		case NEW:
			return new ComputerPlay(playerIndex);
		
		case BEST:
			File best = null;
			int bestScore = 0;
			for(File file:files) {
				String[] parts = file.getName().split("-");
				int thisScore = Integer.parseInt(parts[0]);
				if(best == null || thisScore > bestScore) {
					best = file;
					bestScore = thisScore;
				}
			}
			ComputerPlay bestComp = FileManager.loadComputerPlayer(best.getName());
			bestComp.playerIndex = playerIndex;
			return bestComp;
		
		case RANDOM: 
			int random = (int)(Math.random()*files.length);
			ComputerPlay randComp = FileManager.loadComputerPlayer(files[random].getName());
			randComp.playerIndex = playerIndex;
			return randComp;
		
		case MODIFIED:
			random = (int)(Math.random()*files.length);
			ComputerPlay modComp = FileManager.loadComputerPlayer(files[random].getName());
			modComp.playerIndex = playerIndex;
			modComp.modifyGene();
			return modComp;
			
		case MERGE:
			random = (int)(Math.random()*files.length);
			int random2 = random;
			while(random == random2) random2 = (int)(Math.random()*files.length);
			ComputerPlay mergeComp1 = FileManager.loadComputerPlayer(files[random].getName());
			ComputerPlay mergeComp2 = FileManager.loadComputerPlayer(files[random].getName());
			mergeComp1.playerIndex = playerIndex;
			mergeComp1.mergeGene(mergeComp2);
			return mergeComp1;
		default:
			return new ComputerPlay(playerIndex);
		}
	}

	/**
	 * Merge the passed computer player with us, randomly
	 * 
	 * @param mergeComp2
	 */
	private void mergeGene(ComputerPlay mergeComp2) {
		if(mergeComp2.playRules.size() != playRules.size() || mergeComp2.recruitRules.size() != recruitRules.size()) return;
		for(int i=0; i < playRules.size(); i++) {
			int random = (int)(Math.random()*2);
			if(random == 1) {
				playRules.set(i, mergeComp2.playRules.get(i));
			}
		}
		for(int i=0; i < recruitRules.size(); i++) {
			int random = (int)(Math.random()*2);
			if(random == 1) {
				recruitRules.set(i, mergeComp2.recruitRules.get(i));
			}
		}
		filename = null;
		previousScores = new int[0];
	}

	/**
	 * Randomize some of the gene
	 */
	private void modifyGene() {
		int modifyChance = Main.config.getInt(Config.KEY.MODIFY_PLAY_GENE_CHANCE.getKey());
		int random = 1;
		while(random % modifyChance != 0) {
			random = (int)(Math.random()*playRules.size()*modifyChance);
			playRules.get(random/modifyChance).randomize();
		}
		
		modifyChance = Main.config.getInt(Config.KEY.MODIFY_RECRUIT_GENE_CHANCE.getKey());
		random = 1;
		while(random % modifyChance != 0) {
			random = (int)(Math.random()*recruitRules.size()*modifyChance);
			recruitRules.get(random/modifyChance).randomize();
		}
		filename = null;
		previousScores = new int[0];
	}

	/**
	 * Calculate the average score from all scores, including the currently finished game
	 * 
	 * @return	Mean Score
	 */
	public int getAverageScore() {
		int averageScore = Main.players[playerIndex].getScore();
		int scoreCount = 1;
		if(previousScores != null) {
			for(int score:previousScores) {
				averageScore += score;
				scoreCount++;
			}
			averageScore /= scoreCount;
		}
		return averageScore;
	}
}
