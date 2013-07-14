package main.enums;

public enum GameState {
	/** Game initialisation */
	INIT, 
	/** Players picking start locations */
	START_LOCATIONS,
	/** Normal turn */ 
	PLAYING,
	/** Recruitment assignment turn */ 
	PLAYING_RECRUITMENT,
	/** Game over */ 
	GAME_OVER
}
