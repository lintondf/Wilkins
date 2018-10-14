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
	
	@Test
	public void testSuggestOption() {
		for (int count = 1; count < 6; count++) {
			for (int wordCount = 2; wordCount < 6; wordCount++) {
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
	 * Test method for {@link org.cryptonomicon.Main#getLogger()}.
	 */
	@Test
	public void testGetLogger() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#setLevel(java.util.logging.Level)}.
	 */
	@Test
	public void testSetLevel() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#initializeLogging()}.
	 */
	@Test
	public void testInitializeLogging() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.Main#Main(org.cryptonomicon.configuration.Configuration, org.apache.commons.cli.Options, java.lang.String[])}.
	 */
	@Test
	public void testMain() {
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
