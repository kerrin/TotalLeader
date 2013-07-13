package ai;

import java.util.HashMap;

import ai.rules.base.PlayRule;
import ai.rules.base.RecruitRule;
import ai.rules.play.BuildBridge;
import ai.rules.play.FindANumberNearMe;
import ai.rules.play.FindANumberThatICanBuildLandTo;
import ai.rules.play.MoveToEdge;
import ai.rules.recruit.FindNumber;

import main.Logger;

public class ComputerPlayConfig {
	private enum SECTION {NONE,PLAY,RECRUIT}
	private int[] scores=new int[0];
	private HashMap<String,PlayRule> playRules;
	private HashMap<String,RecruitRule> recruitRules;
	private SECTION section = SECTION.NONE;
	
	public ComputerPlayConfig(String config) {
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
					rule = new FindANumberNearMe(parts,weight);
				} else if(className.equals("FindANumberThatICanBuildLandTo")) {
					rule = new FindANumberThatICanBuildLandTo(parts,weight);
				} else if(className.equals("MoveToEdge")) {
					rule = new MoveToEdge(parts,weight);
				} else if(className.equals("BuildBridge")) {
					rule = new BuildBridge();
					rule.weighting = weight;
				}
				if(rule != null) {
					playRules.put(className+rule.configDescriptor, rule);
				}
			} else if(section == SECTION.RECRUIT) {
				RecruitRule rule = null;
				String className = parts[0];
				if(className.equals("FindNumber")) {
					rule = new FindNumber(parts,weight);
				}
				if(rule != null) {
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
