/**
 * 
 */
package org.cryptonomicon.configuration;

import static org.junit.Assert.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cryptonomicon.configuration.Configuration.ConfigurationError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 */

/** TODO
 * 
 * http://www.vogella.com/tutorials/Mockito/article.html
 *
 */
public class ConfigurationTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.Configuration#addOptions(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testAddOptions() {
		Options options = new Options();
		Configuration configuration = new Configuration();
		configuration.addOptions(options);
		assertTrue( options.getOptions().size() > 0 );
//		options.getOptions().forEach(System.out::println);
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.Configuration#set(org.apache.commons.cli.CommandLine)}.
	 */
	@Test
	public void testSet() {
		String[] args = {
				        "--derivation-key-length", "128", //  [ARG] :: cryptographic key length in bits; default 256; range 128..256 :: class java.lang.String ]
						"--pbkdf2-iterations", "10000",   //  [ARG] :: PBKDF2 iterations; default 20000; range 10000..1000000 :: class java.lang.String ]
						"--argon-type", "ARGON2d",        //  [ARG] :: ARGON type [ARGON2d|ARGON2i|ARGON2id]; default ARGON2id :: class java.lang.String ]
						"--argon-value", "V10",           //  [ARG] :: ARGON version [V10|V13]; default V13 :: class java.lang.String ]
						"--argon-memory-cost", "1024",    //  [ARG] :: ARGON2 memory cost in KB; default 16384; range 1024..1048576 :: class java.lang.String ]
						"--argon-timeCost", "1",          // [ARG] :: Argon2 timeCost; default 64; range 0..1024 :: class java.lang.String ]
						"--argon-parallelism", "1",       // [ARG] :: Argon2 parallelism; default 4; range 1..128 :: class java.lang.String ]
						"--bcrypt-rounds", "4",           // [ARG] :: BCRYPT rounds <integer>; default 14; range 4..30 :: class java.lang.String ]
						"--scrypt-n", "1024",             // [ARG] :: SCRYPT N <integer>; default 32768; range 0..131072 :: class java.lang.String ]
						"--scrypt-r", "1",                // [ARG] :: SCRYPT r <integer>; default 2; range 0..128 :: class java.lang.String ]
						"--scrypt-p", "1",                // [ARG] :: SCRYPT p <integer>; default 16; range 0..64 :: class java.lang.String ]
  				
		};
		Options options = new Options();
		Configuration configuration = new Configuration();
		configuration.addOptions(options);
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine line = parser.parse(options, args);
			
			configuration.set( line );
			
			KeyDerivationParameters kdp = configuration.getKeyDerivationParameters();
			
			assertTrue( kdp.getKeySize() == 128 );
			assertTrue( kdp.getPbkdf2Iterations() == 10000 );
			assertTrue( kdp.getArgonParameters().getType() == Type.ARGON2d );
			assertTrue( kdp.getArgonParameters().getVersion() == Version.V10 );
			assertTrue( kdp.getArgonParameters().getMemoryCost() == 1024 );
			assertTrue( kdp.getArgonParameters().getTimeCost() == 1 );
			assertTrue( kdp.getArgonParameters().getParallelism() == 1 );
			assertTrue( kdp.getBCryptParameters().getRounds() == 4 );
			assertTrue( kdp.getSCryptParameters().getN() == 1024 );
			assertTrue( kdp.getSCryptParameters().getR() == 1 );
			assertTrue( kdp.getSCryptParameters().getP() == 1 );
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ConfigurationError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.Configuration#getKeyDerivationParameters()}.
	 */
	@Test
	public void testGetKeyDerivationParameters() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp0 = configuration.getKeyDerivationParameters();
		KeyDerivationParameters kdp = new KeyDerivationParameters(configuration, 128, 10000);
		configuration.setKeyDerivationParameters(kdp);
		KeyDerivationParameters kdp1 = configuration.getKeyDerivationParameters();	
		assertFalse( kdp0.toString().equals(kdp.toString()) );
		assertTrue( kdp1.toString().equals(kdp.toString()) );
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.Configuration#optionAsInteger(org.apache.commons.cli.CommandLine, java.lang.String, int, int, java.lang.String)}.
	 */
	@Test
	public void testOptionAsIntegerCommandLineStringIntIntString() {
		Options options = new Options();
		options.addOption("t", true, "test");
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "100"});
			int value = Configuration.optionAsInteger(line, "t", 10, 5, 100, "test");
			assertTrue( value == 100 );
			
			line = parser.parse( options, new String[] {});
			value = Configuration.optionAsInteger(line, "t", 10, 5, 100, "test");
			assertTrue( value == 10 );
			
			line = parser.parse(options, new String[] {"-t", "5"});
			value = Configuration.optionAsInteger(line, "t", 10, 5, 100, "test");
			assertTrue( value == 5 );
			
		} catch (ParseException | ConfigurationError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "4"});
			int value = Configuration.optionAsInteger(line, "t", 10, 5, 100, "test");
			fail( "Out of bounds value not detected");
			
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ConfigurationError e) {
		}
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "101"});
			int value = Configuration.optionAsInteger(line, "t", 10, 5, 100, "test");
			fail( "Out of bounds value not detected");
			
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ConfigurationError e) {
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.Configuration#optionAsInteger(org.apache.commons.cli.CommandLine, org.cryptonomicon.configuration.Configuration.Parameter)}.
	 */
	@Test
	public void testOptionAsIntegerCommandLineParameter() {
		Configuration.Parameter parameter = new Configuration.Parameter("t", "test", true, "test", 10, 5, 100 );
		Options options = new Options();
		options.addOption(parameter);
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "100"});
			int value = Configuration.optionAsInteger(line, parameter);
			assertTrue( value == 100 );
			
			line = parser.parse( options, new String[] {});
			value = Configuration.optionAsInteger(line, parameter);
			assertTrue( value == 10 );
			
			line = parser.parse(options, new String[] {"-t", "5"});
			value = Configuration.optionAsInteger(line, parameter);
			assertTrue( value == 5 );
			
		} catch (ParseException | ConfigurationError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "4"});
			int value = Configuration.optionAsInteger(line, parameter);
			fail( "Out of bounds value not detected");
			
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ConfigurationError e) {
		}
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "101"});
			int value = Configuration.optionAsInteger(line, parameter);
			fail( "Out of bounds value not detected");
			
		} catch (ParseException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ConfigurationError e) {
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.Configuration#optionAsString(org.apache.commons.cli.CommandLine, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testOptionAsString() {
		Options options = new Options();
		options.addOption("t", true, "test");
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, new String[] {"-t", "100"});
			String value = Configuration.optionAsString(line, "t", "test");
			assertTrue( value.equals("100") );
			
		} catch (ParseException | ConfigurationError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


}
