package main.ai;

import java.util.HashMap;


import main.GameStatus;
import main.Logger;
import main.ai.rules.base.PlayRule;
import main.ai.rules.base.RecruitRule;
import main.ai.rules.base.RuleStats;
import main.ai.rules.base.RuleStats.ACTOR;
import main.ai.rules.play.BuildBridge;
import main.ai.rules.play.FindANumberNearMe;
import main.ai.rules.play.FindANumberThatICanBuildLandTo;
import main.ai.rules.play.MoveToEdge;
import main.ai.rules.recruit.FindNumber;
import main.board.Board;

public class ComputerPlayConfig {
	private enum SECTION {NONE,PLAY,RECRUIT}
	private int[] scores=new int[0];
	private HashMap<String,PlayRule> playRules;
	private HashMap<String,RecruitRule> recruitRules;
	private SECTION section = SECTION.NONE;
	private long[] checksums = new long[]{-1,-1};
	
	public ComputerPlayConfig(String config, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		playRules = new HashMap<String,PlayRule>();
		recruitRules = new HashMap<String,RecruitRule>();
		String[] lines = config.split("\n");
		int i = 0;
		lines[i] = lines[i].trim();
		ChecksumData checkSum = readChecksum(lines, i);
		if(!checkSum.found) {
			Logger.error("Can't find checksum in ComputerPlayConfig");
			throw new RuntimeException("Invalid ComputerPlayConfig Config");
		}
		i = checkSum.index;
		checksums = checkSum.checksums;
		if(i >= lines.length) {
			Logger.error("Can't find score in ComputerPlayConfig");
			throw new RuntimeException("Invalid ComputerPlayConfig Config");
		}
		String[] kv = lines[i].split("=");
		String[] scoresStrings = kv[1].split(",");
		scores = new int[scoresStrings.length];
		int j=0;
		for(String scoreString:scoresStrings) {
			scores[j++] = Integer.parseInt(scoreString);
		}
		
		for(i++;i < lines.length;i++) {
			lines[i] = lines[i].trim();
			if(lines[i].isEmpty()) {
				continue;
			} if(lines[i].startsWith("PLAY")) {
				section = SECTION.PLAY;
				continue;
			} else if(lines[i].startsWith("RECRUIT")) {
				section = SECTION.RECRUIT;
				continue;
			}
			String[] partsAndValues = lines[i].split(">");
			String[] valuesKV = partsAndValues[1].split(":");
			int weight = (int) (Math.random() * RuleStats.MAX_RULE_WEIGHT);
			int order = (int) (Math.random() * RuleStats.MAX_RULE_ORDER);
			for(String values:valuesKV) {
				String[] valueKV = values.split("=");
				if(valueKV[0].equalsIgnoreCase("w")) {
					weight = Integer.parseInt(valueKV[1]);
				} else if(valueKV[0].equalsIgnoreCase("o")) {
					order = Integer.parseInt(valueKV[1]);
				}
			}
			String[] parts = partsAndValues[0].split(":");
			if(section == SECTION.PLAY) {
				PlayRule rule = null;
				String className = parts[0];
				if(className.equals("FindANumberNearMe")) {
					rule = new FindANumberNearMe(parts, weight, order, gameStatus, board, ourPlayerIndex);
				} else if(className.equals("FindANumberThatICanBuildLandTo")) {
					rule = new FindANumberThatICanBuildLandTo(parts, weight, order, gameStatus, board, ourPlayerIndex);
				} else if(className.equals("MoveToEdge")) {
					rule = new MoveToEdge(parts, weight, order, gameStatus, board, ourPlayerIndex);
				} else if(className.equals("BuildBridge")) {
					rule = new BuildBridge(weight, order, ACTOR.ADD, gameStatus, board, ourPlayerIndex);
				} else {
					Logger.error("Unknown rule class in config "+className);
				}
				if(rule != null) {
					rule.resetAdditionalDescriptionAndConfigDescriptor();
					playRules.put(className+rule.configDescriptor, rule);
				}
			} else if(section == SECTION.RECRUIT) {
				RecruitRule rule = null;
				String className = parts[0];
				if(className.equals("FindNumber")) {
					rule = new FindNumber(parts,weight, gameStatus, board, ourPlayerIndex);
				} else {
					Logger.warn("Unknown rule class config "+className);
				}
				if(rule != null) {
					rule.resetAdditionalDescriptionAndConfigDescriptor();
					recruitRules.put(className+rule.configDescriptor, rule);
				}
			}
		}
	}

	/**
	 * Read in the checksum
	 * 
	 * @param lines
	 * @param index
	 * @return
	 */
	public static ChecksumData readChecksum(String[] lines, int index) {
		ChecksumData checkSum = new ChecksumData();
		int localIndex = index;
		while(!lines[localIndex].startsWith("SCORE") && localIndex < lines.length) {
			if(lines[localIndex].startsWith("CHECKSUM=")) {
				String[] kv = lines[localIndex].split("=");
				String[] checksumsString = kv[1].split(",");
				if(checksumsString.length > 0) {
					checkSum.checksums[0] = Long.parseLong(checksumsString[0]);
					if(checksumsString.length > 1) {
						checkSum.checksums[1] = Long.parseLong(checksumsString[1]);
					} else {
						checkSum.checksums[1] = -1;
					}
					checkSum.found = true;
				} else {
					checkSum.checksums[0] = -1;
					checkSum.checksums[1] = -1;
				}
			}
			localIndex++;
			lines[localIndex] = lines[localIndex].trim();
		}
		if(!checkSum.found) {
			if(lines[localIndex].startsWith("SCORE")) {
				// No checksum, so use default
				checkSum.checksums[0] = -1;
				checkSum.checksums[1] = -1;
				checkSum.found = true;
			}
		}
		checkSum.index = localIndex;
		return checkSum;
	}

	public int[] getScores() {
		return scores;
	}

	public HashMap<String, PlayRule> getPlayRules() {
		return playRules;
	}

	public HashMap<String, RecruitRule> getRecruitRules() {
		return recruitRules;
	}
	
	public long[] getCheckSums() {
		return checksums;
	}
}
