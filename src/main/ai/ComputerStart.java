package main.ai;

import main.GameStatus;
import main.ai.rules.base.StartRule;
import main.ai.rules.start.BasedOnPlayerNumber;
import main.ai.rules.start.PickAreaWithLowNumbers;
import main.board.Board;
import main.events.CoOrdinate;

/**
 * Selects a start position
 * 
 * @author Kerrin
 *
 */
public class ComputerStart implements Runnable {
	private final GameStatus gameStatus;
	private final Board board;
	
	public ComputerStart(GameStatus gameStatus, Board board) {
		super();
		this.gameStatus = gameStatus;
		this.board = board;
	}

	// TODO: Start Place Rules
	@Override
	public void run() {
		// Do computer stuff
		// TODO: FarFromSea()
		// TODO: NearNumber()
		// TODO: FarFromOthers()
		// TODO: NearSea
		StartRule rule = new PickAreaWithLowNumbers(gameStatus, board);
		CoOrdinate loc = rule.getBestStart();
		if(loc == null) {
			rule = new BasedOnPlayerNumber(gameStatus);
			loc = rule.getBestStart();
		}
		ComputerSelectThread.spawnComputerSelectThread(loc, gameStatus, board);
	}
}
