/**
 * 
 */
package org.cryptonomicon;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cryptonomicon.Main.ReportLogFormatter;
import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.Configuration.ConfigurationError;
import org.cryptonomicon.configuration.Configuration.Parameter;
import org.cryptonomicon.configuration.KeyDerivationParameters;
import org.cryptonomicon.mixers.Mixer;
import org.cryptonomicon.mixers.ShuffledInterlaceMixer;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 *         Example (far too simple) command line driver for Wilkins
 * 
 */
public class Main {

	Configuration configuration;
	Random secureRandom = new SecureRandom();

	protected ArrayList<ByteArray> passwords = new ArrayList<>();
	protected ArrayList<File> files = new ArrayList<>();
	
	protected PassPhraseStrength passPhraseStrength = null;

	protected static final String HELP_PREAMBLE = "wilkins [OPTIONS]... [input-file-path]...\n"
			+ "Wraps two or more input data files each AES encrypted using corresponding passphrases"
			+ " together with a configurable set of random filler material in secure container.";

	protected static SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	protected static FileHandler logFileHandler = null;

	protected static Logger logger = Logger.getLogger("wilkins");

	// major mode selection
	protected static final Parameter CREATE_OPTION = new Parameter("c", "create", false, "create a haystack file (default)");
	protected static final Parameter EXTRACT_OPTION = new Parameter("x", "extract", false, "extract a file from a haystack");

	// secondary function mode selection
	protected static final Parameter HELP_OPTION = new Parameter("h", "help", false, "print these instructions");
	protected static final Parameter SUGGEST_OPTION = new Parameter(null, "suggest-passwords", true, "#", 4, 0, 1000);
	protected static final Parameter SUGGEST_WORDS_OPTION = new Parameter(null, "suggest-word-count", true,
			"#; number of words in suggestions", 4, 1, 11);

	// other arguments
	protected static final Parameter EVALUATE_PASSWORD_OPTION = new Parameter( null, "evaluate-passwords", false, "report on the quality of passwords used");
	protected static final Parameter FILE_PASSWORDS = new Parameter(null, "file-passwords", true, "[passphrase1,passphrase2,...]");
	protected static final Parameter FORCE_OPTION = new Parameter(null, "force", false, "silently overwrite any existing output file");
	protected static final Parameter OUTPUT_FILE_OPTION = new Parameter("o", "output-file", true,
			"output file path (implies create)");
	protected static final Parameter STRAW_COUNT_OPTION = new Parameter("n", "straw-count", true, "number of random filler files",
			0, 1, 10 ); // zero indicates random 2x-3x
	protected static final Parameter PADDING_OPTION = new Parameter("s", "size-multiplier", true, "(float) final file size multiplier",
			0, 1, 1_000_000 ); // zero indicates random 10% to 50% padding
			
	protected static Options getOptions() {
		Options options = new Options();
		options.addOption(HELP_OPTION);
		options.addOption(SUGGEST_OPTION);
		options.addOption(SUGGEST_WORDS_OPTION);
		options.addOption(CREATE_OPTION);
		options.addOption(EXTRACT_OPTION);
		FILE_PASSWORDS.setArgs(Option.UNLIMITED_VALUES);
		FILE_PASSWORDS.setValueSeparator(',');
		options.addOption(FILE_PASSWORDS);
		options.addOption(FORCE_OPTION);
		options.addOption(OUTPUT_FILE_OPTION);
		options.addOption(EVALUATE_PASSWORD_OPTION);
		options.addOption(STRAW_COUNT_OPTION);
		options.addOption(PADDING_OPTION);
		// --erase-inputs
		// --erase-inputs-wipe (W/RV 0, W/RV 1, W/RV random)
		// --gpg-camouflage (add gpg header and permute bytes using seed; write
		// as .gpg)
		// --mixer-shuffled
		// --mixer-randomized
		// --reader-allocated
		// --reader-streamed
		// --log-level
		return options;
	}

	// /////////////////////////////////////////////////////////////////
	// Mode Processing
	// /////////////////////////////////////////////////////////////////

	protected void processHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(80);
		// sort the options with short cuts first (long-only options are less
		// used)
		formatter.setOptionComparator(new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) {
				if (o1.getOpt() == null) {
					if (o2.getOpt() == null) {
						return o1.getLongOpt().compareTo(o2.getLongOpt());
					} else {
						return 1;
					}
				} else {
					if (o2.getOpt() == null) {
						return -1;
					} else {
						return o1.getOpt().compareTo(o2.getOpt());
					}
				}
			}
		});
		formatter.printHelp(HELP_PREAMBLE, options);
	}

	protected void processEvaluatePasswords(List<String> list) {
		for (String pass : list) {
			System.out.println(pass);
			System.out.println( passPhraseStrength.evaluate(pass) );
		}
	}

	protected void processSuggestPasswords(CommandLine line, boolean evaluate) throws ConfigurationError {
		int count = Configuration.optionAsInteger(line, SUGGEST_OPTION);
		int words = Configuration.optionAsInteger(line, SUGGEST_WORDS_OPTION);
		passPhraseStrength.suggest(count, words, evaluate);
	}

	protected boolean processCreate(CommandLine line) {
		
		String badPath = addFiles(line);
		
		if (badPath != null) {
			System.err.printf("Wilkins Haystack ERROR: input file not found: %s", badPath);
			return false;
		}
		
		if (! OUTPUT_FILE_OPTION.specified(line)) {
			System.err.println("Wilkins Haystack ERROR: output file option (-o) must be specified.");
			return false;
		}
		
		String outputPath = OUTPUT_FILE_OPTION.getValue();
		if (outputPath == null) {
			System.err.println("Wilkins Haystack ERROR: output file path must be specified.");
			return false;
		}
		
		File outputFile = new File( outputPath );
		try {
			if (outputFile.createNewFile()) {
				if (! FORCE_OPTION.specified(line) ) {
					System.err.println("Wilkins Haystack ERROR: specified output file already exists.");
					return false;
				}
				outputFile.delete();
				outputFile.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile( outputFile, "rw" );

			if (passwords.isEmpty()) {
				inputPasswords(line);
			}

			// if the user entered command line passwords the counts must match
			if (files.size() != passwords.size()) {
				System.err.printf("Wilkins Haystack ERROR: file and password counts must match. (%d vs %d)\n", files.size(),
						passwords.size());
				raf.close();
				return false;
			}
			
			Mixer mixer = new ShuffledInterlaceMixer();
			
			Wilkins wilkins = new Wilkins(configuration, mixer);
			byte[] iv = new byte[Configuration.AES_IV_BYTES];
			secureRandom.nextBytes(iv);

			FileHeader fileHeader = new FileHeader(configuration.getKeyDerivationParameters(), Jargon2.toByteArray(iv) );
			
			for (int i = 0; i < files.size(); i++) {
				wilkins.addDataFile(files.get(i).getAbsolutePath(), fileHeader, passwords.get(i) );
			}
			int fillCount = STRAW_COUNT_OPTION.asInteger(line);
			if (fillCount <= 0) {
				fillCount = files.size() + secureRandom.nextInt() % files.size();
			}
			wilkins.setRandomFillerCount(fillCount);
			
			int padding = PADDING_OPTION.asInteger(line);
			wilkins.setPadding( padding );
			
			wilkins.load( fileHeader );
			
			wilkins.write(raf, fileHeader);
			raf.close();
			return true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	protected boolean processExtract(CommandLine line) { 
		return true;
	}

	// /////////////////////////////////////////////////////////////////

	protected void addCommandLinePasswords(CommandLine line) {
		String[] values = line.getOptionValues(FILE_PASSWORDS.getOptionName());
		for (String value : values) {
			passwords.add(Jargon2.toByteArray(value.toCharArray()).finalizable().clearSource());
		}
	}

	protected String addFiles(CommandLine line) {
		for (String filePath : line.getArgList()) {
			File file = new File(filePath);
			if (file.exists() && file.isFile()) {
				files.add(file);
			} else {
				return filePath;
			}
		}
		return null;
	}

	protected void inputPasswords(CommandLine line) {
		for (String fileName : line.getArgs()) {
			System.out.printf("For file %s\n", fileName);
			char[] passwordArray;
			while (true) {
				passwordArray = System.console().readPassword("Enter passphrase: ");
				char check[] = System.console().readPassword("Reenter passphrase: ");
				if (Arrays.equals(passwordArray, check)) {
					Arrays.fill(check, ' ');
					break;
				}
			}
			passwords.add(Jargon2.toByteArray(passwordArray).finalizable().clearSource());
		}
	}

	public static Logger getLogger() {
		return Main.logger;
	}

	public static void setLevel(Level level) {
		Main.logger.setLevel(level);
	}

	protected static class ReportLogFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(record.getMillis());
			String msg = String.format("%-8s", record.getLevel())
					+ " , "
					+ Main.logTime.format(cal.getTime())
					+ ", "
					+ record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf(".") + 1,
							record.getSourceClassName().length()) + "::" + record.getSourceMethodName() + ", "
					+ record.getMessage() 
					+ System.lineSeparator();
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

	protected void initializeLogging() {
		try {
			Main.logFileHandler = new FileHandler("wilkins.log");
			// logFileHandler = new FileHandler("wilkins-%g.log", 1*1024*1024,
			// 10);
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, "EXCEPTION: ", e);
			return;
		}
		Main.logFileHandler.setFormatter(new Main.ReportLogFormatter());
		Main.logger.addHandler(Main.logFileHandler);
		Main.logger.setUseParentHandlers(false); // no console logging
		Handler[] handlers = Main.logger.getParent().getHandlers();
		for (Handler handler : handlers) {
			handler.setFormatter(new Main.ReportLogFormatter());
		}
		Main.logger.setLevel(Level.FINE); // changed by configuration
	}
	
	// Only for testing
	public Main() {
		initializeLogging();
	}
	
	public Main(Configuration configuration, Options options, String[] args) {
		this.configuration = configuration;
		initializeLogging();
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			
//			System.out.printf( "opt: %d; args: %d: ", line.getOptions().length, line.getArgs().length );
//			for (String arg : args) {
//				System.out.printf("%s, ",arg );
//			}
//			System.out.println();

			if (args.length == 0 || HELP_OPTION.specified(line)) {
				processHelp(options);
				return;  // ------->
			}
			if (EVALUATE_PASSWORD_OPTION.specified(line)) {
				passPhraseStrength = new PassPhraseStrength();
				if (line.getOptions().length == 1) {
					processEvaluatePasswords(line.getArgList() );
					return;  // ------->
				}
			}
			if (SUGGEST_OPTION.specified(line)) {
				passPhraseStrength = new PassPhraseStrength();
				processSuggestPasswords(line, EVALUATE_PASSWORD_OPTION.specified(line) );
				return;  // ------->
			}

			if (FILE_PASSWORDS.specified(line)) {
				System.out.println("Wilkins Haystack WARNING: use of command line passwords is rarely wise.");
				addCommandLinePasswords(line);
			}

			configuration.set(line);

			if (EXTRACT_OPTION.specified(line)) {
				processExtract( line );
				return;  // ------->
			}
			
			processCreate( line );

		} catch (ParseException exp) {
			System.err.println("Wilkins Haystack ERROR: Parsing failed.  Reason: " + exp.getMessage());
		} catch (ConfigurationError exp) {
			System.err.println("Wilkins Haystack ERROR: configuration failed.  Reason: " + exp.getMessage());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		Options options = getOptions();
		configuration.addOptions(options);
		Main main = new Main(configuration, options, args);
	}
}
