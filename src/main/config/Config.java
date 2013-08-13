package main.config;

import java.util.HashMap;

import main.FileManager;

public class Config {
	public enum KEY {
		/** The starting debug level */
		DEBUG_LEVEL("debug_level","info"),

		/** Number of turns before the game ends */
		GAME_TURNS("game_turns","199"),
		
		/** The number of players in a game */
		NUMBER_PLAYERS("number_players", "5"), 

		/** The default types of players (Player or Computer) */
		PLAYER_TYPE("player_type",new String[]{"Computer","Computer","Computer","Computer","Computer"}),
		
		/** 
		 * The types of computer players:
		 * Best 				The highest ranked player
		 * Top10/50/100 		One of the highest 10/50/100 ranked players
		 * New					A random new player
		 * Random				A random player (not best)
		 * Merge				Combine 2 players to be come a new player
		 * Modified				A random player with random modifications
		 * PotLuck				Randomly pick from other type (except Best/TopX)
		 * MergeTop2/5/10/50	Merge two of the top 2/5/10/50 scoring players
		 * NotNew				Same as Pot Luck, but also excludes NEW
		 */
		COMPUTER_PLAYER_TYPE("computer_player_type",new String[]{"Best","Modified","NotNew","Top50","MergeTop10"}),
		
		/** The colors of the players */
		PLAYER_COLOR("player_color",new String[]{"16711680","8421376","8388736","32896","16581375"}),
		
		/** Where to save and load the computer configs from */
		BASE_COMPUTER_CONFIG_PATH("computer_config_path","C:\\tl-config\\"),
		
		/** Y Axis Size */
		BOARD_HEIGHT("board_height", "20"), 
		/** X Axis Size */
		BOARD_WIDTH("board_width", "20"),
		
		/** The maximum units allowed per square */
		MAX_UNITS("max_units", "9"),
		/** Units cost to build land */
		BUILD_BRIDGE("build_bridge", "7"),
		/** The number of units that are need to defend */
		BASE_DEFENCE_UNITS("base_defence_units", "2"),
		/** Number of turns that recruitments are available */
		RECRUITMENT_TURNS("recruitment_turns","5"), 
		/** Number of squares required to get 1 recruit */
		RECRUIT_SQUARES("recruit_squares","3"),
		
		/** Chance a square is land */
		LAND_CHANCE("land_chance", "40"),
		/** Chance increase of land if the left or up square are land (if both this is used twice */
		LAND_CLUMPING_CHANCE("land_clumping_chance", "20"),
		
		/** The number of pixels a square is displayed on screen as (height and width) */
		SQUARE_SIZE_PIXELS("square_size_pixels", "20"),
		/** The font to use for units */
		UNIT_FONT("unit_font", "Ariel"), 
		/** The font size to use for units */
		UNIT_FONT_SIZE("units_font_size", "18"), 
		
		/** The score each square is worth at the end of the game */
		SQUARE_SCORE("square_score","9"),
		/** The score each unit is worth at the end of the game */
		UNIT_SCORE("unit_score","1"),
		
		/** If the game is to play it self to generate good computer players */
		AUTO_PLAY("auto_play","1"),
		/** The chance we won't modify another play rule during randomisation (1/x that we stop) */
		MODIFY_PLAY_GENE_CHANCE("modify_gene_chance_play","150"),
		/** The chance we won't modify another play rule during randomisation (1/x that we stop) */
		MODIFY_RECRUIT_GENE_CHANCE("modify_gene_chance_recruit","200"), 
		
		/** 
		 * If the computer player once picked a place to recruit, keeps adding to it until full before picking a new square
		 * This will speed up the computer turn a lot
		 */
		COMPUTER_QUICK_RECRUIT("computer_quick_recruit","1"), 
		/** The number of milliseconds to pause on the computer actions if the game is being played by a real person */
		INTERACTIVE_COMPUTER_PAUSE("interactive_computer_pause","500"), 
		/** If the computer configs should be consolidated on start up */
		CONSOLIDATE_ON_STARTUP("consolidate_computer_configs","1"),
		/** The number of computers to hold in memory before pergin during consoldiation (so we don't run out of memory) */
		MAX_IN_MEMORY_COMPUTERS_DURING_CONSOLIDATION("max_computers_during_consolidation","100"),
		/** If we should perge the in memory computers when the maximum is reached */
		PERGE_ON_MAX_COMPUTERS_DURING_PURGE("perge_on_max_computers","1"),
		/** Remove duplicate files when we save (takes longer) */
		CHECK_DUPLICATES_ON_SAVE("check_duplicates_on_save","1");
		
		/** The key to be used in the config file */
		private String key;
		/** The value to use if the key is not found in the config file */
		private String defaultValue = null;
		/** The values to use if the key is not found in the config file, for multi value keys */
		private String[] defaultValues = null;
		
		/**
		 * C'tor
		 * 
		 * @param key
		 * @param defaultValue
		 */
		private KEY(String key, String defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}
		
		/**
		 * C'tor
		 * 
		 * @param key
		 * @param defaultValues
		 */
		private KEY(String key, String[] defaultValues) {
			this.key = key;
			this.defaultValues = defaultValues;
		}

		/**
		 * Get the key name
		 * 
		 * @return
		 */
		public String getKey() {
			return key;
		}

		/**
		 * Get the default value for this value
		 * 
		 * @return
		 */
		public String getDefaultValue() {
			return defaultValue;
		}
	}
	
	/** The configuration */
	private static HashMap<String,String> config;
	
	/**
	 * c'tor
	 */
	public Config(String defaultConfigDir) {
		config = new HashMap<String,String>();
		for(KEY key:KEY.values()) {
			if(key.defaultValue != null) {
				config.put(key.key, key.defaultValue);
			} else {
				for(int i=0; i < key.defaultValues.length; i++) {
					config.put(key.key+i, key.defaultValues[i]);
				}
			}
		}
		char[] data = FileManager.loadFile(defaultConfigDir+"tl.config", false);
		if(data == null) return;
		parseConfigFile(new String(data));
	}
	
	/**
	 * Parse a config file
	 * 
	 * @param configString
	 */
	private void parseConfigFile(String configString) {
		String[] lines = configString.split("\n");
		for(int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
			lines[lineIndex] = lines[lineIndex].trim();
			if(lines[lineIndex].isEmpty() || lines[lineIndex].startsWith("--")) continue;
			String[] parts = lines[lineIndex].split("=");
			String[] values = parts[1].split(",");
			if(values.length > 1) {
				for(int valueIndex = 0; valueIndex < values.length; valueIndex++) {
					String key = parts[0]+valueIndex;
					String value = values[valueIndex];
					if(config.containsKey(key)) {				
						config.put(key, value);
					}
				}
			} else {
				String key = parts[0];
				String value = values[0];
				if(config.containsKey(key)) {				
					config.put(key, value);
				}
			}
		}
	}

	/**
	 * Change a config value
	 * 
	 * @param key
	 * @param value
	 */
	public void setValue(String key, String value) {
		config.put(key, value);
	}
	
	/**
	 * Change a config value list
	 * 
	 * @param key
	 * @param values
	 */
	public void setValueList(String key, String[] values) {
		for(int i=0; i < values.length; i++) {
			config.put(key+i, values[i]);
		}
	}
	
	/**
	 * Get an config item as the string value
	 * 
	 * @param key	config item to get
	 * 
	 * @return	Config value (or null if no config item)
	 */
	public String getString(String key) {
		return config.get(key);
	}

	/**
	 * Get an config item as the string value
	 * 
	 * @param key	config item to get
	 * @param index	The index in to the array to get
	 * @return
	 */
	public String getString(String key, int index) {
		return config.get(key+index);
	}
	
	/**
	 * Get an config item list element as the string value
	 * 
	 * @param key	config item to get
	 * 
	 * @return	Config value (-1 if not a int value)
	 */
	public int getInt(String key) {
		try {
			return Integer.parseInt(config.get(key));
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	/**
	* Get an config item list element as the int value
	 * 
	 * @param key	config item to get
	 * @param index	The index in to the array to get
	 * 
	 * @return
	 */
	public int getInt(String key, int index) {
		try {
			return Integer.parseInt(config.get(key+index));
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}
}
