package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
		return folder.listFiles();
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
		FileInputStream istream;
		String fileAndPath = gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey())+filename;
		File f = new File(fileAndPath);
		try {
			istream = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			Logger.info("File not found: "+fileAndPath);
			e.printStackTrace();
			return new ComputerPlay(-1, gameStatus, board);
		}
		InputStreamReader reader = new InputStreamReader( istream );
		int size = (int) f.length();
		char[] data = new char[size]; // allocate char array of right size
		try {
			reader.read( data, 0, size );
			reader.close();
		} catch (IOException e) {
			Logger.info("IO Exception: "+fileAndPath);
			e.printStackTrace();
			return new ComputerPlay(-1, gameStatus, board);
		}   // read into char array
		return new ComputerPlay(filename, new String(data), playerIndex, gameStatus, board);
	}
	
	/**
	 * Write out a ComputerPlay file to disk
	 * 
	 * @param comp	The ComputerPlay to write out
	 */
	public static void saveComputerPlayer(ComputerPlay comp, GameStatus gameStatus, Board board) {
		// Only save computer players
		if(gameStatus.players[comp.getPlayerIndex()].getType() != TYPE.COMPUTER) return;
		// e.g. Low: 
		//		600 = 20 * 20 / 4 * 6
		//		480 = 20 * 20 / 5 * 6
		//		400 = 20 * 20 / 6 * 6
		// High:
		// 		800 = 20 * 20 / 4 * 8
		//		740 = 20 * 20 / 5 * 8
		//		533 = 20 * 20 / 6 * 8
		double lowScoreThreshold = board.getHeight() * board.getWidth() / (gameStatus.config.getInt(Config.KEY.NUMBER_PLAYERS.getKey()));
		double highScoreThreshold = lowScoreThreshold * 8.0;
		lowScoreThreshold *= 6.0;

		int compScore = comp.getAverageScore();
		// Save the winner always, and save back any unchanged players with the new score, or high scores
		if(comp.filename == null && !comp.winner && compScore < highScoreThreshold) {
			Logger.info("Not saving "+ gameStatus.players[comp.getPlayerIndex()].getName()+" as it didn't win and score was below "+highScoreThreshold+".");
			return;
		}
		// If the average score is low, throw away the computer player
		if(compScore < lowScoreThreshold) {
			Logger.info("Not saving "+ gameStatus.players[comp.getPlayerIndex()].getName()+ " as the score was below "+lowScoreThreshold+".");
			return;
		}
		// Good enough to keep, so save it
		String config = comp.getConfigFileContents();
		String computerConfigFolder = gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey());
		File f = new File(computerConfigFolder+comp.getConfigFilename());
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
	}

	/**
	 *  Delete the previous file for this Computer player
	 *  
	 * @param computerPlay
	 */
	public static void deletePreviousFile(ComputerPlay computerPlay, GameStatus gameStatus) {
		if(computerPlay.filename == null) return;
		File deleteFile = new File(gameStatus.config.getString(Config.KEY.BASE_COMPUTER_CONFIG_PATH.getKey())+computerPlay.filename);
		deleteFile.delete();
	}

}
