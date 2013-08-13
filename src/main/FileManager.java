package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import main.ai.ComputerPlay;
import main.board.Board;
import main.config.Config;
import main.player.Player.TYPE;


public class FileManager {
	
	/**
	 * Get all the gene config files in the config directory
	 * 
	 * @return
	 */
	public static File[] listAllFiles(GameStatus gameStatus) {
		String computerConfigFolder = gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey());
		File folder = new File(computerConfigFolder);
		TLConfigFilter filter = new TLConfigFilter();
		return folder.listFiles(filter);
	}
	
	/**
	 * Loads a computer player in from file
	 * 
	 * @param filename		The file to load
	 * @param playerIndex	the player index
	 * 
	 * @return
	 */
	public static ComputerPlay loadComputerPlayer(String filename, int playerIndex, GameStatus gameStatus, Board board) {
		char[] data = loadFile(gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey())+filename);
		if(data == null) {
			return new ComputerPlay(playerIndex, gameStatus, board);
		}
		return new ComputerPlay(filename, new String(data), playerIndex, gameStatus, board);
	}

	/**
	 * Load a file
	 * 
	 * @param filenameAndPath
	 * @return
	 */
	public static char[] loadFile(String filenameAndPath) {
		return loadFile(filenameAndPath, true);
	}
	public static char[] loadFile(String filenameAndPath, boolean careAboutFileNotFound) {
		Logger.debug("Loading: "+filenameAndPath);
		FileInputStream istream;
		File f = new File(filenameAndPath);
		try {
			istream = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			if(careAboutFileNotFound) {
				Logger.info("File not found: "+filenameAndPath);
				e.printStackTrace();
			}
			return null;
		}
		InputStreamReader reader = new InputStreamReader( istream );
		int size = (int) f.length();
		char[] data = new char[size]; // allocate char array of right size
		try {
			reader.read( data, 0, size );
			reader.close();
		} catch (IOException e) {
			Logger.info("IO Exception: "+filenameAndPath);
			e.printStackTrace();
			return null;
		}   // read into char array
		return data;
	}
	
	/**
	 * Write out a ComputerPlay file to disk
	 * 
	 * @param saveComp			The ComputerPlay to write out
	 * @param gameStatus
	 * @param board
	 * @param force
	 * @param noCurrentScore
	 * @param checkForDuplicateFiles
	 */
	public static void saveComputerPlayer(ComputerPlay saveComp, GameStatus gameStatus, Board board, boolean force, boolean noCurrentScore, boolean checkForDuplicateFiles) {
		// Only save computer players
		if(gameStatus.players[saveComp.getPlayerIndex()].getType() != TYPE.COMPUTER) return;
		// e.g. Low: 
		//		900 = (20 * 20 / 4) * 9
		//		720 = (20 * 20 / 5) * 9
		//		600 = (20 * 20 / 6) * 9
		// High:
		// 		1100 = 20 * 20 / 4 * 11
		//		880  = 20 * 20 / 5 * 11
		//		733  = 20 * 20 / 6 * 11
		double lowScoreThreshold = board.getHeight() * board.getWidth() / (gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey()));
		// e.g. 
		// 4p = 100
		// 5p = 80
		// 6p = 66.6
		double highScoreThreshold = lowScoreThreshold * 11.0;
		lowScoreThreshold *= 9.0;

		int compScore = saveComp.getAverageScore(noCurrentScore);
		// Save the winner always, and save back any unchanged players with the new score, or high scores
		if(!force && saveComp.filename == null && !saveComp.winner && compScore < highScoreThreshold) {
			Logger.info("Not saving "+ gameStatus.players[saveComp.getPlayerIndex()].getName()+" as it didn't win and score was below hi threashold of "+highScoreThreshold+".");
			return;
		}
		// If the average score is low, throw away the computer player
		if(!force && compScore < lowScoreThreshold) {
			Logger.info("Not saving "+ gameStatus.players[saveComp.getPlayerIndex()].getName()+ " as the score was below "+lowScoreThreshold+".");
			return;
		}
		
		ArrayList<String> deleteFiles = new ArrayList<String>();
		if(checkForDuplicateFiles) {
			Logger.info("Checking for duplicates");
			File[] allFiles = FileManager.listAllFiles(gameStatus);
			int i=0;
			for(File file:allFiles) {
				ComputerPlay compareComputer = FileManager.loadComputerPlayer(file.getName(), 0, gameStatus, board);
				if(i%50 == 0) Logger.info("Loading file "+(i+1)+" of "+allFiles.length+": "+file.getName());
				if(saveComp.sameConfig(compareComputer)) {
					saveComp.mergeScores(compareComputer);
					deleteFiles.add(compareComputer.filename);
				}
				i++;
			}
		}
		
		// Good enough to keep, so save it
		String config = saveComp.getConfigFileContents(noCurrentScore);
		String computerConfigFolder = gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey());
		File f = new File(computerConfigFolder+saveComp.getConfigFilename(noCurrentScore));
		Logger.info("Saving as: "+f.getName());
		FileOutputStream ostr;
		try {
			ostr = new FileOutputStream(f);
		} catch (FileNotFoundException e1) {
			Logger.error("Unable to open output ComputerPlay file: "+f.getAbsolutePath());
			e1.printStackTrace();
			return;
		} 
		OutputStreamWriter owtr = new OutputStreamWriter( ostr ); // promote
		 
		try {
			owtr.write( config, 0, config.length() );
			owtr.close();
		} catch (IOException e) {
			Logger.error("Unable to save ComputerPlay file: "+f.getAbsolutePath());
			e.printStackTrace();
			return;
		}
		saveComp.filename = f.getName();
		for(String filename:deleteFiles) {
			Logger.info("Deleting duplicate " + filename);
			deletePreviousFile(filename, gameStatus);
		}
	}

	/**
	 *  Delete the previous file for this Computer player
	 *  
	 * @param computerPlay
	 */
	public static void deletePreviousFile(String filename, GameStatus gameStatus) {
		if(filename == null) return;
		File deleteFile = new File(gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey())+filename);
		deleteFile.delete();
	}

	/**
	 * Check if we can read any configs
	 * 
	 * @param configFolder
	 * @return
	 */
	public static boolean canReadConfigs(String configFolder) {
		File folder = new File(configFolder);
		TLConfigFilter filter = new TLConfigFilter();
		File[] files = folder.listFiles(filter);
		return files != null && files.length > 0;
	}

	public static void createConfigDir(GameStatus gameStatus, Board board) {
		File configDirFile = new File(gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey()));
		Logger.info("Config Dir: "+configDirFile);
		configDirFile.mkdirs();
		gameStatus.currentTurn = gameStatus.config.getInt(Config.KEY.GAME_TURNS.getKey());
		// Create two computer player configs (2 so the merge type works)
		for(int i=0; i <= 1; i++) {
			gameStatus.players[i].modifySquares(10);
			gameStatus.players[i].modifyTotalUnits((short)90);
			gameStatus.players[i].score();
			gameStatus.computerAi[i] = new ComputerPlay(0,gameStatus,board);
			FileManager.saveComputerPlayer(gameStatus.computerAi[i], gameStatus, board, true, false, false);
		}
	}
	
	private static class TLConfigFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			String lowercaseName = name.toLowerCase();
			if (lowercaseName.endsWith(".tl-gene")) {
				return true;
			} else {
				return false;
			}
		}
	};
}
