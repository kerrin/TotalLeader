package main.ai;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;



import main.FileManager;
import main.GameStatus;
import main.Logger;
import main.Main;
import main.NextPlayerThread;
import main.Utils;
import main.ai.ComputerMove;
import main.ai.rules.base.PlayRule;
import main.ai.rules.base.RecruitRule;
import main.ai.rules.base.RuleStats;
import main.ai.rules.play.AnyValidMove;
import main.ai.rules.play.BuildBridge;
import main.ai.rules.play.FindANumberNearMe;
import main.ai.rules.play.FindANumberThatICanBuildLandTo;
import main.ai.rules.play.MoveToEdge;
import main.ai.rules.recruit.AnyValidRecruit;
import main.ai.rules.recruit.FindNumber;
import main.board.Board;
import main.config.Config;
import main.enums.GameState;
import main.events.CoOrdinate;

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
		BEST, 		// The highest ranked player. Must be top 4 or POTLUCK/NOTNEW breaks
		TOP10, 		// The 10 highest ranked players. Must be top 4 or POTLUCK/NOTNEW breaks
		TOP50, 		// The 50 highest ranked players. Must be top 4 or POTLUCK/NOTNEW breaks
		TOP100,		// The 100 highest ranked players. Must be top 4 or POTLUCK/NOTNEW breaks
		NEW, 		// A random new player, Must be 5th or NOTNEW breaks
		RANDOM, 	// A random player
		MERGE, 		// Combine 2 players to be come a new player
		MERGETOP2, 	// Combine 2 players to be come a new player (selected from top 2 scoring players)
		MERGETOP5, 	// Combine 2 players to be come a new player (selected from top 5 scoring players)
		MERGETOP10, // Combine 2 players to be come a new player (selected from top 10 scoring players)
		MERGETOP50, // Combine 2 players to be come a new player (selected from top 50 scoring players)
		MODIFIED, 	// A random player with random modifications
		POTLUCK,	// Randomly pick from other type (except Best, Top 10)
		NOTNEW;		// Randomly pick from other type (except Best, Top 10 and New)

		public static TYPE fromString(String typeStr) {
			TYPE[] values = TYPE.values();
			for(TYPE type:values) {
				if(typeStr.equalsIgnoreCase(type.name())) return type;
			}
			return POTLUCK;
		}
	}
	
	private final GameStatus gameStatus;
	
	private final Board board;
	
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
	
	/**
	 * 
	 * @param filename
	 * @param configContent
	 * @param playerIndex
	 * @param gameStatus
	 */
	public ComputerPlay(String filename, String configContent, int playerIndex, GameStatus gameStatus, Board board) {
		this.playerIndex = playerIndex;
		this.filename = filename;
		this.gameStatus = gameStatus;
		this.board = board;
		ComputerPlayConfig config = new ComputerPlayConfig(configContent, gameStatus, board, playerIndex);
		setUpRulesFromConfigFile(config);
	}
	
	/**
	 * C'tor
	 * 
	 * @param playerIndex	My index in the players array
	 * @param gameStatus
	 * @param board
	 */
	public ComputerPlay(int playerIndex, GameStatus gameStatus, Board board) {
		this.playerIndex = playerIndex;
		this.gameStatus = gameStatus;
		this.board = board;
		int maxUnits = gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
		playRules = new Vector<PlayRule>();
		for(int near=4; near > 0; near--) { // Surrounded by me
			for(int playerId = RuleStats.CONFIG_US_PLAYER_ID; playerId <= RuleStats.CONFIG_SEA_PLAYER_ID; playerId++) { // Move to a players square
				for(int i=0; i <= maxUnits; i++) playRules.add(new FindANumberNearMe(i, playerId, near, gameStatus, board, playerIndex));
			}
		}
		for(int near=4; near > 0; near--) {
			for(int playerId = RuleStats.CONFIG_US_PLAYER_ID; playerId <= RuleStats.CONFIG_NATIVE_PLAYER_ID; playerId++) {
				for(int i=0; i <= maxUnits; i++) playRules.add(new FindANumberThatICanBuildLandTo(i, playerId, near, gameStatus, board, playerIndex));
			}
		}
		playRules.add(new BuildBridge(gameStatus, board, playerIndex));
		for(MoveToEdge.CONDITIONS condition:MoveToEdge.CONDITIONS.values()) {
			playRules.add(new MoveToEdge(condition, gameStatus, board, playerIndex));
		}
		
		Logger.debug("Generated " + playRules.size() + " play rules");
		
		// Get the total weightings for play rules
		for(PlayRule rule:playRules) totalPlayRuleWeights += rule.weighting;
		anyValidMove = new AnyValidMove(gameStatus, board, playerIndex);
		
		recruitRules = new Vector<RecruitRule>();
		for(int findUs=maxUnits-1; findUs >= 0; findUs--) {
			for(int findOpponent=0; findOpponent <= maxUnits; findOpponent++) {
				for(int adjPlayerId = RuleStats.CONFIG_OPPONENT_PLAYER_ID; adjPlayerId <= RuleStats.CONFIG_NATIVE_PLAYER_ID; adjPlayerId++) {
					for(int maxAdj=4; maxAdj > -1; maxAdj--) {
						for(int minAdj=4; minAdj > -1; minAdj--) {
							if(maxAdj == -1 || minAdj == -1 || maxAdj>=minAdj) {
								Logger.trace("N"+findUs+",O"+findOpponent+","+minAdj+"to"+maxAdj);
								recruitRules.add(new FindNumber(findUs,findOpponent,maxAdj,minAdj,adjPlayerId, gameStatus, board, playerIndex));
							}
						}
					}
				}
			}
		}
		
		Logger.debug("Generated " + recruitRules.size() + " recruit rules");
		
		// Get the total weightings for play rules
		for(RecruitRule rule:recruitRules) totalRecruitRuleWeights += rule.weighting;
		anyValidRecruit = new AnyValidRecruit(gameStatus, board, playerIndex);
	}
	/**
	 * Set up the rules based on a gene config file
	 * 
	 * @param config
	 */
	private void setUpRulesFromConfigFile(ComputerPlayConfig config) {
		HashMap<String, PlayRule> configPlayRule = config.getPlayRules();
		HashMap<String, RecruitRule> configRecruitRule = config.getRecruitRules();
		int maxUnits = gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
		previousScores = config.getScores();
		playRules = new Vector<PlayRule>();
		for(int near=4; near > 0; near--) { // Surrounded by me
			for(int playerId = RuleStats.CONFIG_US_PLAYER_ID; playerId <= RuleStats.CONFIG_SEA_PLAYER_ID; playerId++) {// Move to a players square
				for(int units=0; units <= maxUnits; units++) {
					setUpFindANumberNearMe(configPlayRule, units, playerId, near);
				}
			}
		}
		for(int near=4; near > 0; near--) {
			for(int playerId = RuleStats.CONFIG_US_PLAYER_ID; playerId <= RuleStats.CONFIG_NATIVE_PLAYER_ID; playerId++) {
				for(int units=0; units <= maxUnits; units++) {
					setUpFindANumberThatICanBuildLandTo(configPlayRule, units, playerId, near);
				}
			}
		}
		setUpBuildBridge(configPlayRule);
		for(MoveToEdge.CONDITIONS condition:MoveToEdge.CONDITIONS.values()) {
			setUpMoveToEdge(configPlayRule, condition);
		}
		
		Logger.debug("Generated " + playRules.size() + " play rules");
		
		// Get the total weightings for play rules
		for(PlayRule rule:playRules) totalPlayRuleWeights += rule.weighting;
		anyValidMove = new AnyValidMove(gameStatus, board, playerIndex);
		
		recruitRules = new Vector<RecruitRule>();
		for(int findUs=maxUnits-1; findUs >= 0; findUs--) {
			for(int findOpponent=0; findOpponent <= maxUnits; findOpponent++) {
				for(int adjPlayerId = RuleStats.CONFIG_OPPONENT_PLAYER_ID; adjPlayerId <= RuleStats.CONFIG_NATIVE_PLAYER_ID; adjPlayerId++) {
					for(int maxAdj=4; maxAdj >= 0; maxAdj--) {
						for(int minAdj=4; minAdj >= 0; minAdj--) {
							if(maxAdj == -1 || minAdj == -1 || maxAdj>=minAdj) {
								Logger.trace("U"+findUs+",O"+findUs+","+minAdj+"to"+maxAdj);
								setUpFindNumber(configRecruitRule, findUs,findOpponent,maxAdj,minAdj,adjPlayerId);
							}
						}
					}
				}
			}
		}
		
		Logger.debug("Generated " + recruitRules.size() + " recruit rules");
		
		// Get the total weightings for play rules
		for(RecruitRule rule:recruitRules) totalRecruitRuleWeights += rule.weighting;
		anyValidRecruit = new AnyValidRecruit(gameStatus, board, playerIndex);
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
			int recruits = gameStatus.players[gameStatus.currentPlayerIndex].getRecruits();
			Logger.debug("Placing "+recruits+" recruits");
			CoOrdinate place = null;
			short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
			boolean quickRecruit = gameStatus.config.getInt(Config.KEY.COMPUTER_QUICK_RECRUIT.getKey()) == 1;
			for(int i=0; i < recruits; i++) {
				ComputerSelectThread.waitOnComputerSelect();
				if(quickRecruit && place != null && board.getBoard()[place.x][place.y].getUnits() >= maxUnits) {
					place = null;
				}
				Vector<RecruitRule> thisTurnsRules = (Vector<RecruitRule>)recruitRules.clone();
				while(!thisTurnsRules.isEmpty() && place == null) {
					long pickRule = (long)(Math.random()*totalRecruitRuleWeights);
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
				// Make sure the uncommon issue of a player having no where to place recruits is dealt with
				if(place == null) {
					Logger.info("Player "+playerIndex+" unable to place all their recruits");
					recruits = 0;
					Logger.debug("Next Player");
					NextPlayerThread.spawnNextPlayerThread();
				} else {
					ComputerSelectThread.spawnComputerSelectThread(place, gameStatus, board);
				}
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
			if(move == null) {
				Logger.info("Player "+playerIndex+" unable to move");
				Logger.debug("Next Player");
				NextPlayerThread.spawnNextPlayerThread();
			} else {
				Logger.debug(gameStatus.currentPlayerIndex+")Moving "+move.units+" from "+move.from.x+","+move.from.y+"=>"+move.to.x+","+move.to.y);
				moveUnits = move.units;
				ComputerSelectThread.spawnComputerSelectThread(move.from, gameStatus, board);
				ComputerSelectThread.spawnComputerSelectThread(move.to, gameStatus, board);
			}
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
		Utils.pauseForHumanToSee(gameStatus);
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
		waitComputerPlayer(-1);
	}
	/**
	 * Return when the lock is free
	 * Calling function must also be synchronised or the lock may be taken
	 * 
	 * @param timeout	Maximum milliseconds to wait
	 * 
	 * @return Forced unlock
	 */
	public static synchronized boolean waitComputerPlayer(int timeout) {
		Logger.debug("Waiting on playerDone");
		long startWaiting = System.currentTimeMillis();
		boolean output = false;
		while(computerThreadLockedBy != null && (timeout == -1 || startWaiting + timeout < System.currentTimeMillis())) {
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
		if(computerThreadLockedBy != null) {
			computerThreadLockedBy = null;
			return true;
		} else {
			return false;
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
	 * @return	Config for writing to file
	 */
	public String getConfigFileContents() {
		// Select an arbitrary player for the opponent
		StringBuffer sb = new StringBuffer("");
		
		sb.append("SCORE=");
		sb.append(gameStatus.players[playerIndex].getScore());
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
			/*
			if(rule instanceof FindANumberNearMe) {
				FindANumberNearMe r = (FindANumberNearMe) rule;
				int configPlayerIndex = r.playerIndexForRule(playerIndex);
				if(configPlayerIndex == RuleStats.CONFIG_DONT_SAVE) {
					int rulePlayerIndex = r.getPlayerIndex();
					Logger.trace("Skiping rule "+rule.getClass().getSimpleName()+rule.configDescriptor + 
							" pi:"+rulePlayerIndex);
					continue;
				}
				r.setPlayerIndex(configPlayerIndex);
				r.resetAdditionalDescriptionAndConfigDescriptor();
			} else if(rule instanceof FindANumberThatICanBuildLandTo) {
				FindANumberThatICanBuildLandTo r = (FindANumberThatICanBuildLandTo) rule;
				int configPlayerIndex = r.playerIndexForRule(playerIndex);
				if(configPlayerIndex == RuleStats.CONFIG_DONT_SAVE) {
					int rulePlayerIndex = r.getPlayerIndex();
					Logger.trace("Skiping rule "+rule.getClass().getSimpleName()+rule.configDescriptor + 
							" pi:"+rulePlayerIndex);
					continue;
				}
				r.setPlayerIndex(configPlayerIndex);
				r.resetAdditionalDescriptionAndConfigDescriptor();
			}
			*/
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
			/*if(rule instanceof FindNumber) {
				FindNumber r = (FindNumber) rule;
				int configPlayerIndex = r.playerIndexForRule(playerIndex);
				if(configPlayerIndex == RuleStats.CONFIG_DONT_SAVE) {
					int rulePlayerIndex = r.getRequiredAnAdjacentSquareThatIsPlayer();
					Logger.trace("Skiping rule "+rule.getClass().getSimpleName()+rule.configDescriptor + 
							" pi:"+rulePlayerIndex);
					continue;
				}
				r.setRequiredAnAdjacentSquareThatIsPlayer(configPlayerIndex);
				r.resetAdditionalDescriptionAndConfigDescriptor();
			}*/
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
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configPlayRule
	 * @param ntf				Number to find
	 * @param rulePlayerIndex	Player to be near
	 * @param selfMove			Is a self move
	 * @param near				Required Adjacent Square That Are Us
	 */
	private void setUpFindANumberNearMe(HashMap<String, PlayRule> configPlayRule, int ntf, int rulePlayerIndex, int near) {
		FindANumberNearMe configRule = (FindANumberNearMe)configPlayRule.get(
				"FindANumberNearMe"+
				FindANumberNearMe.getConfigDescriptorForFile(ntf, rulePlayerIndex, near, playerIndex, gameStatus));
		if(configRule != null) {
			FindANumberNearMe rule = new FindANumberNearMe(configRule, rulePlayerIndex, playerIndex);
			playRules.add(rule);
		} else {
			Logger.warn("Could not find FindANumberNearMe rule "+
					FindANumberNearMe.getConfigDescriptorForFile(ntf, rulePlayerIndex, near, playerIndex, gameStatus)+
					" in config "+(filename!=null?filename:"UNKNOWN"));

			playRules.add(new FindANumberNearMe(ntf, rulePlayerIndex, near, gameStatus, board, playerIndex));
		}
	}
	
	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configPlayRule
	 * @param ntf				Number to find
	 * @param rulePlayerIndex	Player to be near
	 * @param near				Required Adjacent Square That Are Us
	 */
	private void setUpFindANumberThatICanBuildLandTo(HashMap<String, PlayRule> configPlayRule, int ntf, int rulePlayerIndex, int near) {
		
		FindANumberThatICanBuildLandTo configRule = (FindANumberThatICanBuildLandTo)configPlayRule.get(
				"FindANumberThatICanBuildLandTo"+
				FindANumberThatICanBuildLandTo.getConfigDescriptor(ntf, rulePlayerIndex, near, playerIndex, gameStatus));
		if(configRule != null) {
			FindANumberThatICanBuildLandTo rule = new FindANumberThatICanBuildLandTo(configRule, rulePlayerIndex, playerIndex);
			playRules.add(rule);
		} else {
			Logger.warn("Could not find FindANumberThatICanBuildLandTo rule "+
					FindANumberThatICanBuildLandTo.getConfigDescriptor(ntf, rulePlayerIndex, near, playerIndex, gameStatus)+
					" in config "+(filename!=null?filename:"UNKNOWN"));

			playRules.add(new FindANumberThatICanBuildLandTo(ntf, rulePlayerIndex, near, gameStatus, board, playerIndex));
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
				"MoveToEdge"+MoveToEdge.getConfigDescriptorForFile(cond));
		if(configRule != null) {
			playRules.add(configRule);
		} else {
			Logger.warn("Could not find MoveToEdge rule "+
					MoveToEdge.getConfigDescriptorForFile(cond)+
					" in config "+(filename!=null?filename:"UNKNOWN"));
			playRules.add(new MoveToEdge(cond, gameStatus, board, playerIndex));
		}
	}
	
	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configPlayRule
	 * @param cond
	 */
	private void setUpBuildBridge(HashMap<String, PlayRule> configPlayRule) {
		BuildBridge configRule = (BuildBridge)configPlayRule.get("BuildBridge");
		if(configRule != null) {
			playRules.add(configRule);
		} else {
			Logger.warn("Could not find BuildBridge rule in config "+(filename!=null?filename:"UNKNOWN"));
			playRules.add(new BuildBridge(gameStatus, board, playerIndex));
		}
	}
	/**
	 * Use the config entry for this rule, or if one not found create a new random rule
	 * 
	 * @param configRecruitRule
	 * @param ntfu				Number to find Us
	 * @param ntfo				Number to find opponent
	 * @param mastau			Maximum Adjacent Square That Are Us
	 * @param rastau			Min Required Adjacent Square That Are Us
	 * @param ruleRaastip			Require an adjacent square that is player
	 */
	private void setUpFindNumber(HashMap<String, RecruitRule> configRecruitRule, int ntfu, int ntfo, int mastau, int rastau, int ruleRaastip) {
		FindNumber configRule = (FindNumber)configRecruitRule.get(
				"FindNumber"+
				FindNumber.getConfigDescriptor(ntfu, ntfo, mastau, rastau, ruleRaastip, playerIndex, gameStatus));
		if(configRule != null) {
			FindNumber rule = new FindNumber(configRule, ruleRaastip, playerIndex);
			recruitRules.add(rule);
		} else {
			Logger.warn("Could not find FindNumber rule "+
					FindNumber.getConfigDescriptor(ntfu, ntfo, mastau, rastau, ruleRaastip, playerIndex, gameStatus)+
					" in config "+(filename!=null?filename:"UNKNOWN"));
			recruitRules.add(new FindNumber(ntfu, ntfo, mastau, rastau, ruleRaastip, gameStatus, board, playerIndex));
		}
	}

	/**
	 * Get a computer player
	 * 
	 * @param playerIndex
	 * @return
	 */
	public static ComputerPlay getComputerPlayer(int playerIndex, GameStatus gameStatus, Board board) {
		/** 
		 * The types of players:
		 * Best 		The highest ranked player
		 * New			A random new player
		 * Random		A random player (not best)
		 * Merge		Combine 2 players to be come a new player
		 * Modified		A random player with random modifications
		 * PotLuck		Randomly pick from other type (except Best)
		 */
		String typeStr = gameStatus.config.getString(Config.KEY.COMPUTER_PLAYER_TYPE.getKey(),playerIndex);
		ComputerPlay.TYPE type = ComputerPlay.TYPE.fromString(typeStr);
		File[] files = FileManager.listAllFiles(gameStatus);
		Logger.info("Computer Player "+playerIndex+" is type " + type.name());
		while(type == TYPE.POTLUCK || type == TYPE.NOTNEW) {
			int exclude = 4;
			if(type == TYPE.NOTNEW) exclude = 5;
			int random = (int)(Math.random()*(TYPE.values().length-(exclude+1)))+exclude;
			type = TYPE.values()[random];
			Logger.info("Computer Player "+playerIndex+" is now type " + type.name());
		}
		// If there are no configs, only new will work
		if(files.length < 1) {
			type = TYPE.NEW;
		}
		switch (type) {
		case NEW:
			return new ComputerPlay(playerIndex, gameStatus, board);
		
		case BEST: case TOP10: case TOP50: case TOP100:
			HashMap<Integer,Vector<File>> bestScores = new HashMap<Integer,Vector<File>>();
			int highest = getTopScores(files, bestScores);
			int howMany = 1;
			if(type == TYPE.TOP10) howMany = 10;
			if(type == TYPE.TOP50) howMany = 50;
			if(type == TYPE.TOP100) howMany = 100;
			if(howMany > files.length) howMany = files.length;
			int random = (int)(Math.random()*howMany);
			Vector<File> filesAtScore = new Vector<File>();
			while(random >= 0 && highest > 0) {
				if(bestScores.containsKey(highest)) {
					filesAtScore = bestScores.get(highest);
					random--;
				}
				highest--;
			}
			random = (int)(Math.random()*filesAtScore.size());
			return FileManager.loadComputerPlayer(filesAtScore.get(random).getName(), playerIndex, gameStatus, board);
		
		case RANDOM: 
			random = (int)(Math.random()*files.length);
			ComputerPlay randComp = FileManager.loadComputerPlayer(files[random].getName(), playerIndex, gameStatus, board);
			return randComp;
		
		case MODIFIED:
			random = (int)(Math.random()*files.length);
			ComputerPlay modComp = FileManager.loadComputerPlayer(files[random].getName(), playerIndex, gameStatus, board);
			modComp.modifyGene();
			return modComp;
			
		case MERGE:
			random = (int)(Math.random()*files.length);
			int random2 = random;
			while(random == random2) random2 = (int)(Math.random()*files.length);
			ComputerPlay mergeComp1 = FileManager.loadComputerPlayer(files[random].getName(), playerIndex, gameStatus, board);
			ComputerPlay mergeComp2 = FileManager.loadComputerPlayer(files[random2].getName(), playerIndex, gameStatus, board);
			mergeComp1.mergeGene(mergeComp2);
			return mergeComp1;
		case MERGETOP2: case MERGETOP5: case MERGETOP10: case MERGETOP50:
			bestScores = new HashMap<Integer,Vector<File>>();
			highest = getTopScores(files, bestScores);
			howMany = 2;
			if(type == TYPE.MERGETOP5) howMany = 5;
			if(type == TYPE.MERGETOP10) howMany = 10;
			if(type == TYPE.MERGETOP50) howMany = 50;
			if(howMany > files.length) howMany = files.length;
			random = (int)(Math.random()*howMany);
			random2 = random;
			while(random == random2) random2 = (int)(Math.random()*howMany);
			mergeComp1 = FileManager.loadComputerPlayer(files[random].getName(), playerIndex, gameStatus, board);
			mergeComp2 = FileManager.loadComputerPlayer(files[random2].getName(), playerIndex, gameStatus, board);
			mergeComp1.mergeGene(mergeComp2);
			return mergeComp1;
		default:
			return new ComputerPlay(playerIndex, gameStatus, board);
		}
	}

	private static int getTopScores(File[] files, HashMap<Integer,Vector<File>> bestScores) { 
		int highest = 0;
		for(File file:files) {
			String[] parts = file.getName().split("-");
			int thisScore = Integer.parseInt(parts[0]);
			if(thisScore > highest) highest = thisScore;
			Vector<File> filesAtScore;
			if(bestScores.containsKey(thisScore)) {
				filesAtScore = bestScores.get(thisScore);
			} else {
				filesAtScore = new Vector<File>();
			}
			filesAtScore.add(file);
			bestScores.put(thisScore, filesAtScore);
		}
		return highest;
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
		int modifyChance = gameStatus.config.getInt(Config.KEY.MODIFY_PLAY_GENE_CHANCE.getKey());
		int random = 1;
		while(random % modifyChance != 0) {
			random = (int)(Math.random()*playRules.size()*modifyChance);
			playRules.get(random/modifyChance).randomize();
		}
		
		modifyChance = gameStatus.config.getInt(Config.KEY.MODIFY_RECRUIT_GENE_CHANCE.getKey());
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
		int averageScore = gameStatus.players[playerIndex].getScore();
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

	/**
	 * Get all the play rules
	 * 
	 * @return
	 */
	public Vector<PlayRule> getPlayRules() {
		return playRules;
	}
}
