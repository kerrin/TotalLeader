package main.events;



import main.GameStatus;
import main.Logger;
import main.Main;
import main.NextPlayerThread;
import main.board.Board;
import main.board.Square;
import main.config.Config;
import main.enums.GameState;
import main.input.Keyboard;
import main.player.Player;
import main.player.Player.TYPE;

public class EventManager {
	private CoOrdinate currentSelected = null;
	private final GameStatus gameStatus;
	private final Board board;
	
	public EventManager(GameStatus gameStatus, Board board) {
		super();
		this.gameStatus = gameStatus;
		this.board = board;
	}

	/**
	 * A square was selected
	 * 
	 * @param x
	 * @param y
	 */
	public void selected(CoOrdinate selectedCoordinates) {
		Logger.debug("Selected "+ selectedCoordinates);
		Square selectedSquare = board.getBoard()[selectedCoordinates.x][selectedCoordinates.y];
		if(Main.getGameState() == GameState.START_LOCATIONS) {
			Logger.debug("Start Location");
			int boardHeight = board.getHeight();
			int boardWidth = board.getWidth();
			if(selectedCoordinates.x >= 1 && selectedCoordinates.x < boardWidth-1 && 
					selectedCoordinates.y >= 1 || selectedCoordinates.y < boardHeight-1) {
				gameStatus.players[gameStatus.currentPlayerIndex].setStart(selectedCoordinates);
			}
			selectedSquare.setSelected(false);
			return;
		}
		
		Player currentPlayer = gameStatus.players[gameStatus.currentPlayerIndex];
		if(Main.getGameState() == GameState.PLAYING_RECRUITMENT) {
			Logger.debug("Recruitment");
			selectedSquare.setSelected(false);
			if(selectedSquare.getOwner().equals(currentPlayer)) {
				short maxUnits = (short)gameStatus.config.getInt(Config.KEY.MAX_UNITS.getKey());
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
			Logger.debug(gameStatus.currentPlayerIndex+")New!");
			return;
		}
		if(selectedCoordinates.x < currentSelected.x-1 || selectedCoordinates.x > currentSelected.x+1 || 
				selectedCoordinates.y < currentSelected.y-1 || selectedCoordinates.y > currentSelected.y+1 ) {
			if(currentPlayer.getType() == TYPE.COMPUTER) {
				Logger.info(gameStatus.currentPlayerIndex+")Too Far:" + currentSelected+","+selectedCoordinates);
				System.exit(1);
			}
			changeSelection(selectedCoordinates);
			Logger.debug(gameStatus.currentPlayerIndex+")Far!");
			return;
		}
		if(selectedCoordinates.x != currentSelected.x && selectedCoordinates.y != currentSelected.y) {
			// Not orthogonal (one of the 4 basic directions)
			changeSelection(selectedCoordinates);
			Logger.debug(gameStatus.currentPlayerIndex+")Diag!");
			return;
		}
		
		// The square was adjacent to the previously selected square
		Logger.debug(gameStatus.currentPlayerIndex+")Adjacent!");
		
		// Deselect both
		Square fromSquare = board.getBoard()[currentSelected.x][currentSelected.y];
		Square toSquare = board.getBoard()[selectedCoordinates.x][selectedCoordinates.y];
		fromSquare.setSelected(false);
		toSquare.setSelected(false);
		
		short numUnits = -1;
		if(currentPlayer.getType() == TYPE.COMPUTER) {
			numUnits = gameStatus.computerAi[gameStatus.currentPlayerIndex].moveUnits;
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
		Logger.debug(gameStatus.currentPlayerIndex+")Deselect");
		currentSelected = null;
	}

	private void changeSelection(CoOrdinate coordinates) {
		// Not adjacent to current selected, so deselect current selection
		board.getBoard()[currentSelected.x][currentSelected.y].setSelected(false);
		// and mark the new selection
		currentSelected = coordinates;
	}
}
