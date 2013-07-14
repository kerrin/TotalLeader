package main.ai;

import java.util.HashMap;


import main.GameStatus;
import main.Logger;
import main.ai.rules.base.PlayRule;
import main.ai.rules.base.RecruitRule;
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
	
	public ComputerPlayConfig(String config, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		playRules = new HashMap<String,PlayRule>();
		recruitRules = new HashMap<String,RecruitRule>();
		String[] lines = config.split("\n");
		int i = 0;
		lines[i] = lines[i].trim();
		while(!lines[i].startsWith("SCORE") && i < lines.length) {
			i++;
			lines[i] = lines[i].trim();
		}
		if(i >= lines.length) {
			Logger.error("Can't find score in ComputerPlayConfig");
			throw new RuntimeException("Invalid ComputerPlayConfig Config");
		}
		String[] kv = lines[i].split("=");
		String[] scoresStrings = kv[1].split(",");
		scores = new int[scoresStrings.length];
		i=0;
		for(String scoreString:scoresStrings) {
			scores[i++] = Integer.parseInt(scoreString);
		}
		
		for(/*I set already*/;i < lines.length;i++) {
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
			String[] partsAndWeight = lines[i].split(">");
			String[] weightKV = partsAndWeight[1].split("=");
			int weight = Integer.parseInt(weightKV[1]);
			String[] parts = partsAndWeight[0].split(":");
			if(section == SECTION.PLAY) {
				PlayRule rule = null;
				String className = parts[0];
				if(className.equals("FindANumberNearMe")) {
					rule = new FindANumberNearMe(parts, weight, gameStatus, board, ourPlayerIndex);
				} else if(className.equals("FindANumberThatICanBuildLandTo")) {
					rule = new FindANumberThatICanBuildLandTo(parts, weight, gameStatus, board, ourPlayerIndex);
				} else if(className.equals("MoveToEdge")) {
					rule = new MoveToEdge(parts,weight, gameStatus, board, ourPlayerIndex);
				} else if(className.equals("BuildBridge")) {
					rule = new BuildBridge(gameStatus, board, ourPlayerIndex);
					rule.weighting = weight;
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

	public int[] getScores() {
		return scores;
	}

	public HashMap<String, PlayRule> getPlayRules() {
		return playRules;
	}

	public HashMap<String, RecruitRule> getRecruitRules() {
		return recruitRules;
	}
}
