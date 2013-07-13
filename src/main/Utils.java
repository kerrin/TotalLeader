package main;

public class Utils {

	public static String niceStackTrace(StackTraceElement[] stackTrace) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < stackTrace.length; i++) {
			sb.append(stackTrace[i].getClassName());
			sb.append("@");
			sb.append(stackTrace[i].getLineNumber());
			sb.append("\n");
		}
		return sb.toString();
	}

}
