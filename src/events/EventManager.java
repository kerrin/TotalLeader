package events;

import config.Config;
import player.Player;
import player.Player.TYPE;
import input.Keyboard;

import board.Square;

import main.GameState;
import main.Logger;
import main.Main;
import main.NextPlayerThread;

public class EventManager {
	private CoOrdinate currentSelected = null;
	
	/**
	 * A square was selected
	 * 
	 * @param x
	 * @param y
	 */
	public void selected(CoOrdinate selectedCoordinates) {
		Logger.debug("Selected "+ selectedCoordinates);
		Square selectedSquare = Main.board.getBoard()[selectedCoordinates.x][selectedCoordinates.y];
		if(Main.getGameState() == GameState.START_LOCATIONS) {
			Logger.debug("Start Location");
			int boardHeight = Main.board.getHeight();
			int boardWidth = Main.board.getWidth();
			if(selectedCoordinates.x >= 1 && selectedCoordinates.x < boardWidth-1 && 
					selectedCoordinates.y >= 1 || selectedCoordinates.y < boardHeight-1) {
				Main.players[Main.currentPlayerIndex].setStart(selectedCoordinates);
			}
			selectedSquare.setSelected(false);
			return;
		}
		
		Player currentPlayer = Main.players[Main.currentPlayerIndex];
		if(Main.getGameState() == GameState.PLAYING_RECRUITMENT) {
			Logger.debug("Recruitment");
			selectedSquare.setSelected(false);
			if(selectedSquare.getOwner().equals(currentPlayer)) {
				short maxUnits = (short)Main.config.getInt(Config.KEY.MAX_UNITS.getKey());
				if(selectedSquare.getUnits() < maxUnits) {
					selectedSquare.setUnits((short)(selectedSquare.getUnits()+1));
					currentPlayer.modifyTotalUnits((short)1);
					if(currentPlayer.decrementRecruitsLeft()) {
						Logger.debug("Next Player");
						NextPlayerThread.spawnNextPlayerThread();
					}
				} else {
					Logger.info("Too many units there! "+selectedSquare.getCoordinate());
				}
			} else {
				Logger.debug("Not Your Square Mate!");
			}
			Logger.debug("Recruitment Done");
			return;
		}
		
		if(currentSelected == null) {
			if(!selectedSquare.getOwner().equals(currentPlayer) || selectedSquare.getUnits() < 1) {
				selectedSquare.setSelected(false);
				return;
			}
			currentSelected = selectedCoordinates;
			Logger.debug(Main.currentPlayerIndex+")New!");
			return;
		}
		if(selectedCoordinates.x < currentSelected.x-1 || selectedCoordinates.x > currentSelected.x+1 || 
				selectedCoordinates.y < currentSelected.y-1 || selectedCoordinates.y > currentSelected.y+1 ) {
			if(currentPlayer.getType() == TYPE.COMPUTER) {
				Logger.info(Main.currentPlayerIndex+")Too Far:" + currentSelected+","+selectedCoordinates);
				System.exit(1);
			}
			changeSelection(selectedCoordinates);
			Logger.debug(Main.currentPlayerIndex+")Far!");
			return;
		}
		if(selectedCoordinates.x != currentSelected.x && selectedCoordinates.y != currentSelected.y) {
			// Not orthogonal (one of the 4 basic directions)
			changeSelection(selectedCoordinates);
			Logger.debug(Main.currentPlayerIndex+")Diag!");
			return;
		}
		
		// The square was adjacent to the previously selected square
		Logger.debug(Main.currentPlayerIndex+")Adjacent!");
		
		// Deselect both
		Square fromSquare = Main.board.getBoard()[currentSelected.x][currentSelected.y];
		Square toSquare = Main.board.getBoard()[selectedCoordinates.x][selectedCoordinates.y];
		fromSquare.setSelected(false);
		toSquare.setSelected(false);
		
		short numUnits = -1;
		if(currentPlayer.getType() == TYPE.COMPUTER) {
			numUnits = Main.computerAi[Main.currentPlayerIndex].moveUnits;
		} else {
			numUnits = Keyboard.numKeyPressed();
		}
		if(numUnits > 0) {
			short amount = numUnits;
			if(!fromSquare.moveTo(toSquare, amount)) {
				// Invalid move
				currentSelected = null;
				return;
			}
		} else {
			// Move as much as you can
			if(!fromSquare.moveTo(toSquare)) {
				// Invalid move
				currentSelected = null;
				return;
			}
		}
		currentSelected = null;
		NextPlayerThread.spawnNextPlayerThread();
	}
	
	public void deselected(CoOrdinate selectedCoordinates) {
		Logger.debug(Main.currentPlayerIndex+")Deselect");
		currentSelected = null;
	}

	private void changeSelection(CoOrdinate coordinates) {
		// Not adjacent to current selected, so deselect current selection
		Main.board.getBoard()[currentSelected.x][currentSelected.y].setSelected(false);
		// and mark the new selection
		currentSelected = coordinates;
	}
}
