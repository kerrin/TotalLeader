package ai;

import events.CoOrdinate;
import ai.rules.base.StartRule;
import ai.rules.start.BasedOnPlayerNumber;
import ai.rules.start.PickAreaWithLowNumbers;

/**
 * Selects a start position
 * 
 * @author Kerrin
 *
 */
public class ComputerStart implements Runnable {
	// TODO: Start Place Rules
	@Override
	public void run() {
		// Do computer stuff
		// TODO: FarFromSea()
		// TODO: NearNumber()
		// TODO: FarFromOthers()
		// TODO: NearSea
		StartRule rule = new PickAreaWithLowNumbers();
		CoOrdinate loc = rule.getBestStart();
		if(loc == null) {
			rule = new BasedOnPlayerNumber();
			loc = rule.getBestStart();
		}
		ComputerSelectThread.spawnComputerSelectThread(loc);
	}
}
