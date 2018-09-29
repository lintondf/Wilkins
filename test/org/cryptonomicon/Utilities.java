/**
 * 
 */
package org.cryptonomicon;

/**
 * @author lintondf
 *
 */
public class Utilities {

	
	//https://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public enum  OS_Name {UNKNOWN, WINDOWS, MACOS, UNIX};
	
	public static OS_Name getOS() {
		if (isWindows())
			return OS_Name.WINDOWS;
		if (isMac())
			return OS_Name.MACOS;
		if (isUnix())
			return OS_Name.UNIX;
		return OS_Name.UNKNOWN;
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
