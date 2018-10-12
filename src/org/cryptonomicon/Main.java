/**
 * 
 */
package org.cryptonomicon;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cryptonomicon.Main.ReportLogFormatter;
import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.Configuration.ConfigurationError;
import org.cryptonomicon.configuration.KeyDerivationParameters;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 * Example (far too simple) command line driver for Wilkins
 * 
 */
public class Main {
	
	protected static class ReportLogFormatter extends Formatter {
	    @Override
	    public String format(LogRecord record) {
	        Calendar cal = new GregorianCalendar();
	        cal.setTimeInMillis(record.getMillis());
	        String msg = String.format("%-8s", record.getLevel()) + " , "
	                + Main.logTime.format(cal.getTime())
	                + ", "
	                + record.getSourceClassName().substring(
	                        record.getSourceClassName().lastIndexOf(".")+1,
	                        record.getSourceClassName().length())
	                + "::"
	                + record.getSourceMethodName()
	                + ", "
	                + record.getMessage() + "\n";
	        if (record.getThrown() != null) {
	        	StringBuffer sb = new StringBuffer();
	        	Throwable t = record.getThrown();
	        	sb.append( t.getMessage() );
	        	sb.append('\n');
	        	msg += sb.toString();
	        }
	        return msg;
	    }
	}

	protected static final String HELP_PREAMBLE = "wilkins [OPTIONS]... [input-file-path]...\n" +
	  "Wraps two or more input data files each AES encrypted using corresponding passphrases" +
	  " together with a configurable set of random filler material in secure container.";

	protected static final String FILE_PASSWORDS = "file-passwords";
	
	
	protected void processHelp(Options options) {
    	// automatically generate the help statement
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.setWidth(80);
    	// sort the options with short cuts first (long-only options are less used)
    	formatter.setOptionComparator( new Comparator<Option>() {
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
    	formatter.printHelp( HELP_PREAMBLE, options );
	}
	
	protected void addCommandLinePasswords( CommandLine line ) {
        String[] values = line.getOptionValues(FILE_PASSWORDS);
        for (String value : values)  {
        	passwords.add( Jargon2.toByteArray(value.toCharArray()).finalizable().clearSource() );
        }
	}
	
	protected String addFiles( CommandLine line ) {
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
	
	protected void inputPasswords( CommandLine line ) {
        for (String fileName : line.getArgs() ) {
        	System.out.printf("For file %s\n", fileName );
        	char[] passwordArray;
        	while (true) {
        		passwordArray = System.console().readPassword("Enter passphrase: ");
        		char check[] = System.console().readPassword("Reenter passphrase: ");
        		if (Arrays.equals(passwordArray, check)) {
		        	Arrays.fill(check, ' ');
        			break;
        		}
        	}
        	passwords.add( Jargon2.toByteArray(passwordArray).finalizable().clearSource() );
        }
	}
	
    public static Logger getLogger() {
		return Main.logger;
	}

	public static void setLevel(Level level) {
		Main.logger.setLevel(level);
	}

	protected static void initializeLogging() {
		    try {
		        Main.logFileHandler = new FileHandler("wilkins.log");
	//	        logFileHandler = new FileHandler("wilkins-%g.log", 1*1024*1024, 10);
		    } catch (Exception e) {
				Main.getLogger().log(Level.SEVERE, "EXCEPTION: ", e );
		        return;
		    }
		    Main.logFileHandler.setFormatter(new Main.ReportLogFormatter());
		    Main.logger.addHandler(Main.logFileHandler);
		    Main.logger.setUseParentHandlers(true);  // console logging
		    Handler[] handlers = Main.logger.getParent().getHandlers();
		    for (Handler handler : handlers ) {
		    	handler.setFormatter(new Main.ReportLogFormatter());
		    }
		    Main.logger.setLevel(Level.ALL);  // changed by configuration
		}

	protected ArrayList<ByteArray> passwords = new ArrayList<>();
    protected ArrayList<File>   files = new ArrayList<>();

	protected static SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	protected static FileHandler logFileHandler = null;

	protected static Logger logger = Logger.getLogger("wilkins");
    
	public Main( Configuration configuration, Options options, String[] args ) {
	    CommandLineParser parser = new DefaultParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        if (args.length == 0 || line.hasOption("help")) {
	        	processHelp( options );
	        }
	        
	        if ( line.hasOption( FILE_PASSWORDS ) ) {
	        	System.out.println("Wilkins Haystack WARNING: use of command line passwords is rarely wise.");
	        	addCommandLinePasswords( line );
	        }
	        
			configuration.set(line);
			
			String badPath = addFiles( line );
	
			if ( badPath != null  ) {
				System.err.printf("Wilkins Haystack ERROR: file not found: %s", badPath );
				System.exit(1);				
			}
			
			if (passwords.isEmpty()) {
				inputPasswords( line );
			}
			
			// if the user entered command line passwords they counts must match
			if (files.size() != passwords.size()) {
				System.err.printf("Wilkins Haystack ERROR: file and password counts must match. (%d vs %d)\n", 
						files.size(), passwords.size() );
				System.exit(1);				
			}
			
			
	    } catch( ParseException exp ) {
	        System.err.println( "Wilkins Haystack ERROR: Parsing failed.  Reason: " + exp.getMessage() );
	    } catch ( ConfigurationError exp ) {
	        System.err.println( "Wilkins Haystack ERROR: configuration failed.  Reason: " + exp.getMessage() );
	    }
	}
	
	
	protected static Options getOptions() {
		Options options = new Options();
		options.addOption("c", "create", false, "create a haystack file (default)");
		options.addOption("x", "extract", false, "extract a file from a haystack");
		options.addOption(null, FILE_PASSWORDS, true, "[passphrase1,passphrase2,...]");
		options.getOption(FILE_PASSWORDS).setArgs(Option.UNLIMITED_VALUES);
		options.getOption(FILE_PASSWORDS).setValueSeparator(',');
		options.addOption("h", "help", false, "print these instructions");
		options.addRequiredOption("o", "output-file", true, "output file path (implies create)");
		// -n --straw-count number of random filler files (default is random 2..3xinput count)
		// -s --size-multiplier (float) final file size multiplier
		// --measure filename EXITS
		// --check-passwords
		// --suggest-passwords EXITS
		// --erase-inputs
		// --erase-inputs-wipe  (W/RV 0, W/RV 1, W/RV random)
		// --gpg-camouflage  (add gpg header and permute bytes using seed; write as .gpg)
		// --mixer-shuffled
		// --mixer-randomized
		// --reader-allocated
		// --reader-streamed
		// --log-level
		
		return options;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		Options options = getOptions();
		configuration.addOptions( options );
		Main main = new Main( configuration, options, args );
	}
}
