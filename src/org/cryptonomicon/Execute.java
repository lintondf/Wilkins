/** blackpearl.Execute
 * Execute operating system commands safely, capturing all output streams
 */
package org.cryptonomicon;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;

public class Execute {

	/**
	 * Execute.StreamCapture Thread to capture a stream and return a String
	 */
	protected static class StreamCapture extends Thread {
		InputStream is;
		String type;
		StringBuffer out;
		Thread thread;

		StreamCapture(InputStream is, String type) {
			this.is = is;
			this.type = type;
			this.out = new StringBuffer();
			this.thread = null;
		}

		/**
		 * Get the output, waiting if necessary
		 */
		public String toString() {
			if (thread != null && thread.isAlive()) {
				try {
					thread.join();
				} catch (InterruptedException e) {
				}
			}
			return out.toString();
		}

		/**
		 * Capture the stream output
		 */
		public void run() {
			thread = Thread.currentThread();
			long startTime = (new Date()).getTime();
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					out.append(line);
					out.append('\n');
					long now = (new Date()).getTime();
					if ((now - startTime) > 60 * 1000) {
						startTime = now;
						Wilkins.getLogger().finer(line);
						System.out.println(line);
					}
				}
			} catch (IOException ioe) {
				Wilkins.getLogger().log(Level.SEVERE, "EXCEPTION: ", ioe);
			}
		}
	} // class StreamCapture

	public static void print(String[] cmd) {
		for (String token : cmd) {
			System.out.print("[" + token + "], ");
		}
		System.out.println();
	}

	/**
	 * Run a command, optionally printing the command and outputs for debug
	 * 
	 * @param cmdStr
	 * @param print
	 * @return
	 */
	public static String run(String cmdStr, boolean print) {
		// if (print)
		// System.out.println( cmdStr );
		return run(cmdStr.split(" "), print);
	}

	/**
	 * Run a command string with space-separated fields
	 * 
	 * @param cmdStr
	 * @return
	 */
	public static String run(String cmdStr) {
		return run(cmdStr.split(" "));
	}

	/**
	 * Run a command with fields in a string array
	 * 
	 * @param cmd
	 * @return
	 */
	public static String run(String[] cmd) {
		return run(cmd, false);
	}

	/**
	 * Run a command with fields in a string array, with optional debug prints
	 * 
	 * @param cmd
	 * @param print
	 * @return
	 */
	public static String run(String[] cmd, boolean print) {
		// if (print) {
		// System.out.print("run> ");
		// for (String c : cmd)
		// System.out.print(c + " ");
		// System.out.println();
		// }
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			StreamCapture outputCapture = new StreamCapture(p.getInputStream(),
					"OUTPUT");
			outputCapture.start();
			StreamCapture errorCapture = new StreamCapture(p.getErrorStream(),
					"ERROR");
			errorCapture.start();
			
			int r = p.waitFor(); // Let the process finish.
			// if (print)
			// System.out.println("run< " + r + " : " + outputCapture.toString()
			// + " : " + errorCapture.toString());
			String errors = errorCapture.toString();
			if (r == 0 || errors.isEmpty())
				return outputCapture.toString();
			else
				return errors;

		} catch (InterruptedException e) {
			return null;
		} catch (Exception e) {
			//Wilkins.getLogger().log(Level.SEVERE, "EXCEPTION: ", e);
			return "";
		}
	}

	public static String run(String[] cmd, String input) {
		// if (print) {
		// System.out.print("run> ");
		// for (String c : cmd)
		// System.out.print(c + " ");
		// System.out.println();
		// }
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			StreamCapture outputCapture = new StreamCapture(p.getInputStream(),
					"OUTPUT");
			outputCapture.start();
			StreamCapture errorCapture = new StreamCapture(p.getErrorStream(),
					"ERROR");
			errorCapture.start();
			
			BufferedOutputStream bos = new BufferedOutputStream( p.getOutputStream() );
			bos.write( input.getBytes() );
			bos.close();
			
			int r = p.waitFor(); // Let the process finish.
			// if (print)
			// System.out.println("run< " + r + " : " + outputCapture.toString()
			// + " : " + errorCapture.toString());
			String errors = errorCapture.toString();
			if (r == 0 || errors.isEmpty())
				return outputCapture.toString();
			else
				return errors;

		} catch (InterruptedException e) {
			return null;
		} catch (Exception e) {
			//Wilkins.getLogger().log(Level.SEVERE, "EXCEPTION: ", e);
			return "";
		}
	}

	public static void main(String[] args) {
		String out = Execute.run("/usr/bin/nmcli -m tabular con status"
				.split(" "));
		System.out.println(out);
		out = Execute.run("/usr/bin/deluged ".split(" "));
		System.out.println(out);
		out = Execute.run("/usr/bin/deluged ".split(" "));
		System.out.println(out);
		out = Execute.run("/usr/bin/killall deluged".split(" "));
		System.out.println(out);
	}
} // class Execute
