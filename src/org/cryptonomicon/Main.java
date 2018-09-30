/**
 * 
 */
package org.cryptonomicon;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author lintondf
 *
 * Example (far too simple) command line driver for Wilkins
 * 
 */
public class Main {
	
	protected static final String HELP_PREAMBLE = "wilkins [OPTIONS]... output-file-path\n" +
	  "Wraps two or more input data files each AES encrypted using corresponding passphrases" +
	  " together with a configurable set of random filler material in secure container.";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println( Execute.run("/usr/local/bin/gpg --help") );
		Options options = new Options();
		options.addOption("fpw", "file-password", true, "[path-to-datafile passphrase] 1..n");
		options.getOption("fpw").setArgs(Option.UNLIMITED_VALUES);
		options.addOption("h", "help", true, "print these instructions");
		options.addOption("", "argon-type", true, "ARGON type [i|d|id]");
		options.addOption("c", "key-derivation-color", true, "method used to convert passphrases to keys");
		
	    CommandLineParser parser = new DefaultParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        if (args.length == 0 || line.hasOption("help")) {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( HELP_PREAMBLE, options );
	        }
	        if ( line.hasOption( "fpw" ) ) {
	            // initialise the member variable
	            String[] values = line.getOptionValues("fpw");
	            for (String value : values) 
	            	System.out.println(value);
	        }
	        if (line.hasOption("argon-type")) {
	        	System.out.println(line.getOptionValue("argon-type"));
	        }
	    } catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
	}
}
