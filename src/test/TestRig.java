package test;

import test.ai.TestComputerPlay;
import test.ai.TestComputerPlayConfig;
import test.events.EventManagerTest;
import junit.framework.TestSuite;

public class TestRig extends TestSuite {
	public void testSuite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestFileManager());
		suite.addTest(new TestComputerPlay());
		suite.addTest(new TestComputerPlayConfig());
		suite.addTest(new EventManagerTest());
	}
	
	public static String stripSecondScore(String compConfig) {
		int firstEquals = compConfig.indexOf("=");
		int firstComma = compConfig.indexOf(",");
		String temp = compConfig.substring(0,firstEquals+1);
		temp += compConfig.substring(firstComma+1);
		return temp;
	}
}
