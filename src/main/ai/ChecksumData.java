package main.ai;

public class ChecksumData {
	/** The checksums */
	public long[] checksums;
	/** If we have found a checksum */
	public boolean found = false;
	/** The line index we got to when we found the checksum */
	public int index;

	public ChecksumData() {
		checksums = new long[2];
	}
}
