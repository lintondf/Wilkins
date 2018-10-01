/**
 * 
 */
package org.cryptonomicon;

import java.io.Console;
import java.io.IOException;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.Configuration.ConfigurationError;
import org.cryptonomicon.configuration.KeyDerivationParameters;

import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 * Example (far too simple) command line driver for Wilkins
 * 
 */
public class Main {
	
	protected static final String HELP_PREAMBLE = "wilkins [OPTIONS]... [input-file-path]...\n" +
	  "Wraps two or more input data files each AES encrypted using corresponding passphrases" +
	  " together with a configurable set of random filler material in secure container.";

	protected static final String FILE_PASSWORDS = "file-passwords";
	
	
	protected static void processHelp(Options options) {
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		for (Type version : Type.values() ) {
//			System.out.println(version.name());
//		}
//		System.out.println( Execute.run("/usr/local/bin/gpg --help") );
		Configuration configuration = new Configuration();
		Options options = new Options();
		options.addOption(null, FILE_PASSWORDS, true, "[passphrase1,passphrase2,...]");
		options.getOption(FILE_PASSWORDS).setArgs(Option.UNLIMITED_VALUES);
		options.getOption(FILE_PASSWORDS).setValueSeparator(',');
		options.addOption("h", "help", false, "print these instructions");
		options.addRequiredOption("o", "output-file", true, "output file path");

		configuration.addOptions( options );
		
	    CommandLineParser parser = new DefaultParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        if (args.length == 0 || line.hasOption("help")) {
	        	processHelp( options );
	        }
	        
	        if ( line.hasOption( FILE_PASSWORDS ) ) {
	        	System.out.println("Wilkins Haystack WARNING: use of command line passwords is rarely wise.");
	            // initialize the member variable
	            String[] values = line.getOptionValues(FILE_PASSWORDS);
	            for (String value : values) 
	            	System.out.println(value);
	        }
	        
			configuration.set(line);
			
	        for (String arg : line.getArgs() ) {
	        	System.out.println(arg);
	        }
	    } catch( ParseException | ConfigurationError exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	        
	    }

	    
	}
}
