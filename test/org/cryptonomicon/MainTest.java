/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lintondf
 *
 */
public class MainTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void restoreStreams() {
	    System.setOut(originalOut);
	    System.setErr(originalErr);
	}
	
	/**
	 * Test method for {@link org.cryptonomicon.Main#processSuggestPasswords(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testSuggestOption() {
		for (int count = 1; count < 3; count++) {
			for (int wordCount = 3; wordCount < 6; wordCount++) {
				outContent.reset();
				errContent.reset();
				String[] cmd2 = { "--"+Main.SUGGEST_OPTION.getLongOpt(), Integer.toString(count),
						          "--"+Main.SUGGEST_WORDS_OPTION.getLongOpt(), Integer.toString(wordCount) };
				Main.main( cmd2 );
				String[] lines = outContent.toString().split("\n");
				assertTrue( lines.length == count );
				for (String line : lines) {
					String[] words = line.split("-");
					assertTrue( words.length == wordCount );
				}
				assertTrue( errContent.size() == 0);
			}
		}
	}
	
	/**
	 * Test method for {@link org.cryptonomicon.Main#processHelp(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testProcessHelp() {
		String[] cmd1 = {"-h"};
		Main.main( cmd1 );
		String result1 = outContent.toString();
		outContent.reset();
		String[] cmd2 = {"--help"};
		Main.main( cmd2 );
		String result2 = outContent.toString();
		assertTrue( result1 != null && ! result1.isEmpty() );
		assertTrue( result2 != null && ! result2.isEmpty() );
		assertTrue( result1.equals(result2) );
	}
	
	/**
	 * Test method for {@link org.cryptonomicon.Main#processHelp(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testProcessEvaluatePasswords() {
		String[] cmd1 = {"--evaluate-passwords", "file1"};
		Main.main( cmd1 );
		String[] results = outContent.toString().split(System.lineSeparator());
		assertTrue( results.length == 2 );
		assertTrue( results[0].equals(cmd1[1]) );
		assertTrue( results[1].contains("11.2 bits") );
		assertTrue( results[1].contains(" NElL") );
	}
	
	/**
	 * Test method for {@link org.cryptonomicon.Main#processCreate(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testProcessCreate() {		
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#processExtract(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testProcessExtract() {
		fail("Not yet implemented");		
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#addCommandLinePasswords(org.apache.commons.cli.CommandLine)}.
	 */
	@Test
	public void testAddCommandLinePasswords() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#addFiles(org.apache.commons.cli.CommandLine)}.
	 */
	@Test
	public void testAddFiles() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#inputPasswords(org.apache.commons.cli.CommandLine)}.
	 */
	@Test
	public void testInputPasswords() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#getOptions()}.
	 */
	@Test
	public void testGetOptions() {
		fail("Not yet implemented");
	}

}
