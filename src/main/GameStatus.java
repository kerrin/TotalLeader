package main;

import main.ai.ComputerPlay;
import main.config.Config;
import main.display.Display;
import main.enums.GameState;
import main.events.EventManager;
import main.player.Player;

public class GameStatus {
	public Config config;
	public Display display;
	public EventManager eventManager;
	public GameState gameState = GameState.INIT;
	public int currentPlayerIndex;
	public Player[] players;
	public int currentTurn = 0;
	public int nativePlayerIndex;
	public int seaPlayerIndex;
	public ComputerPlay[] computerAi;
	public boolean pause = false;
	public Player winner;
	public boolean humanPlaying = false;
	/** Show the debug panel instead of the game */
	public boolean showDebug = false;
}
