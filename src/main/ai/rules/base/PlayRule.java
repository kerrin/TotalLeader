package main.ai.rules.base;

import main.GameStatus;
import main.ai.ComputerMove;
import main.board.Board;

public abstract class PlayRule extends RuleStats {
	public final Board board;
	public PlayRule(String name, String description, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(name, description, gameStatus, ourPlayerIndex);
		this.board = board;
	}
	
	public PlayRule(String name, String description, int weighting, int order, ACTOR actor, GameStatus gameStatus, Board board, int ourPlayerIndex) {
		super(name, description, weighting, order, actor, gameStatus, ourPlayerIndex);
		this.board = board;
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
