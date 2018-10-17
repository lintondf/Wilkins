/**
 * 
 */
package org.cryptonomicon;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.Configuration.Parameter;

/**
 * @author lintondf
 *
 */
public class Logged extends Main {

	public static class ReportLogFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(record.getMillis());
			String msg = String.format("%-8s", record.getLevel())
					+ " , "
					+ Logged.logTime.format(cal.getTime())
					+ ", "
					+ record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf(".") + 1,
							record.getSourceClassName().length()) + "::" + record.getSourceMethodName() + ", "
					+ record.getMessage() + System.lineSeparator();
			if (record.getThrown() != null) {
				StringBuffer sb = new StringBuffer();
				Throwable t = record.getThrown();
				sb.append(t.getMessage());
				sb.append('\n');
				msg += sb.toString();
			}
			return msg;
		}
	}

	private static SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static FileHandler logFileHandler = null;
	private static Logger logger = null;

	public static void log(Level level, String msg) {
		if (logger != null) {
			logger.log(level, msg);
		}
	}

	public static void log(Level level, String msg, Throwable thrown) {
		if (logger != null) {
			logger.log(level, msg, thrown);
		}
	}

	// Only for testing
	public Logged() {
		super();
		logger = Logger.getLogger("wilkins");
		initializeLogging();
	}

	public Logged(Configuration configuration, Options options, String[] args) {
		super(configuration, options, args);
	}

	static void initializeLogging() {
		try {
			org.cryptonomicon.Logged.logFileHandler = new FileHandler("wilkins.log");
			// logFileHandler = new FileHandler("wilkins-%g.log", 1*1024*1024,
			// 10);
		} catch (Exception e) {
			org.cryptonomicon.Logged.log(Level.SEVERE, "EXCEPTION: ", e);
			return;
		}
		org.cryptonomicon.Logged.logFileHandler.setFormatter(new org.cryptonomicon.Logged.ReportLogFormatter());
		org.cryptonomicon.Logged.logger.addHandler(org.cryptonomicon.Logged.logFileHandler);
		org.cryptonomicon.Logged.logger.setUseParentHandlers(false); // no
																		// console
																		// logging
		Handler[] handlers = org.cryptonomicon.Logged.logger.getParent().getHandlers();
		for (Handler handler : handlers) {
			handler.setFormatter(new org.cryptonomicon.Logged.ReportLogFormatter());
		}
		org.cryptonomicon.Logged.logger.setLevel(Level.ALL); 
	}

	private static Level[] levels = { Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER,
			Level.FINEST, Level.ALL };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger = Logger.getLogger("wilkins");
		initializeLogging();
		Configuration configuration = new Configuration();
		String[] levelNames = new String[levels.length];
		for (int i = 0; i < levels.length; i++) {
			levelNames[i] = levels[i].getName();
		}
		Main.LOG_LEVEL.setDescription(Arrays.toString(levelNames));
		Options options = getOptions();

		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			logger.setLevel(Level.OFF);
			if (Main.LOG_LEVEL.specified(line)) {
				String target = Main.LOG_LEVEL.getValue();
				for (Level level : levels) {
					if (level.getName().equals(target)) {
						logger.setLevel(level);
						break;
					}
				}
				if (!logger.getLevel().getName().equals(target)) {
					System.err.println("Wilkins Haystack ERROR: Unknown log-level: " + target);
					System.exit(0);
				}
			}
		} catch (ParseException e) {
		}

		configuration.addOptions(options);
		Logged main = new Logged(configuration, options, args);
	}

}
