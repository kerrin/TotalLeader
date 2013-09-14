package main;

import java.io.InputStreamReader;

/**
 * Store the details about an input file stream
 * 
 * @author Kerrin
 *
 */
public class StreamData {
	public InputStreamReader reader;
	public int fileSize;
	
	
	public StreamData(InputStreamReader reader, int fileSize) {
		this.reader = reader;
		this.fileSize = fileSize;
	}
}
