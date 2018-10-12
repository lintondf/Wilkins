/**
 * 
 */
package org.cryptonomicon.configuration;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.cli.Options;
import org.cryptonomicon.configuration.KeyDerivationParameters.ArgonParameters;
import org.cryptonomicon.configuration.KeyDerivationParameters.BCryptParameters;
import org.cryptonomicon.configuration.KeyDerivationParameters.SCryptParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 */
public class KeyDerivationParametersTest {

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
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#addOptions(org.apache.commons.cli.Options)}.
	 */
	@Test
	public void testAddOptions() {
		Options options = new Options();
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		kdp.addOptions(options);
		options.getOptions().forEach(System.out::println);
		System.out.println(options.getOptions().size());
//		assertTrue( options.getOptions().size() == 10 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#getKeySize()}.
	 */
	@Test
	public void testGetKeySize() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		assertTrue( kdp.getKeySize() == 256 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#getArgonParameters()}.
	 */
	@Test
	public void testGetArgonParameters() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		ArgonParameters ap = kdp.getArgonParameters();
		assertTrue( ap.getType() == Type.ARGON2id);
		assertTrue( ap.getVersion() == Version.V13 );
		assertTrue( ap.getMemoryCost() == 16*1024 );
		assertTrue( ap.getTimeCost() == 64 );
		assertTrue( ap.getParallelism() == 4 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#getBCryptParameters()}.
	 */
	@Test
	public void testGetBCryptParameters() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		BCryptParameters bp = kdp.getBCryptParameters();
		assertTrue( bp.getRounds() == 14 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#getSCryptParameters()}.
	 */
	@Test
	public void testGetSCryptParameters() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		SCryptParameters sp = kdp.getSCryptParameters();
		assertTrue( sp.getN() == 32*1024 );
		assertTrue( sp.getR() == 2 );
		assertTrue( sp.getP() == 16 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#KeyDerivationParameters(org.cryptonomicon.configuration.Configuration, int)}.
	 */
	@Test
	public void testKeyDerivationParametersConfigurationInt() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = new KeyDerivationParameters(configuration, 128, 10000);
		
		assertTrue( kdp.getKeySize() == 128 );
		assertTrue( kdp.getPbkdf2Iterations() == 10000);
	}

	/**
	 * Test method for {@link org.cryptonomicon.configuration.KeyDerivationParameters#KeyDerivationParameters(org.cryptonomicon.configuration.Configuration, java.io.InputStream)}.
	 */
	@Test
	public void testKeyDerivationParametersConfigurationInputStream() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			kdp.write( bos );
		} catch (IOException e) {
			fail(e.getMessage() );
		}
		ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
		try {
			KeyDerivationParameters kdp2 = new KeyDerivationParameters(configuration, bis);
			assertTrue( kdp.toString().equals(kdp2.toString()));
		} catch (IOException e) {
			fail(e.getMessage() );
		}
	}

}
