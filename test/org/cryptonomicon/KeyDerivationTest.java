/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

import org.cryptonomicon.KeyDerivation.Pbkdp2DerivationStep;
import org.cryptonomicon.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public class KeyDerivationTest {
	
	Configuration configuration = new Configuration();
	KeyDerivation keyDerivation = new KeyDerivation( configuration );

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
	 * Test method for {@link org.cryptonomicon.KeyDerivation#loadDerivationSteps(com.kosprov.jargon2.api.Jargon2.ByteArray)}.
	 */
	@Test
	public void testLoadDerivationSteps() {
		ByteArray salt = Jargon2.toByteArray( new byte[ 16 ] );
		keyDerivation.loadDerivationSteps(salt);
//		keyDerivation.derivationSteps.forEach( System.out::println);
		assertTrue( keyDerivation.derivationSteps.get(0).getClass().getName().contains("Pbkdp2") );
		assertTrue( keyDerivation.derivationSteps.get(1).getClass().getName().contains("Argon") );
		assertTrue( keyDerivation.derivationSteps.get(2).getClass().getName().contains("BCrypt") );
		assertTrue( keyDerivation.derivationSteps.get(3).getClass().getName().contains("SCrypt") );
	}

	/**
	 * Test method for {@link org.cryptonomicon.KeyDerivation#permuteDerivationSteps(org.cryptonomicon.KeyDerivation.Pbkdp2DerivationStep, com.kosprov.jargon2.api.Jargon2.ByteArray, com.kosprov.jargon2.api.Jargon2.ByteArray)}.
	 * @throws GeneralSecurityException 
	 */
	@Test
	public void testPermuteDerivationSteps() throws GeneralSecurityException {
		ByteArray salt = Jargon2.toByteArray( new byte[ 16 ] );
		keyDerivation.loadDerivationSteps(salt);
		ByteArray password = Jargon2.toByteArray( "Hello".getBytes() );
		Pbkdp2DerivationStep pbkdp2 = (Pbkdp2DerivationStep) keyDerivation.derivationSteps.get(0);
		keyDerivation.permuteDerivationSteps(pbkdp2, password, salt);

		assertTrue( keyDerivation.derivationSteps.get(1).getClass().getName().contains("Pbkdp2") );
		assertTrue( keyDerivation.derivationSteps.get(2).getClass().getName().contains("Argon") );
		assertTrue( keyDerivation.derivationSteps.get(3).getClass().getName().contains("BCrypt") );
		assertTrue( keyDerivation.derivationSteps.get(0).getClass().getName().contains("SCrypt") );
		
		password = Jargon2.toByteArray( "Goodbye".getBytes() );
		keyDerivation.permuteDerivationSteps(pbkdp2, password, salt);
		//keyDerivation.derivationSteps.forEach( System.out::println);

		assertTrue( keyDerivation.derivationSteps.get(2).getClass().getName().contains("Pbkdp2") );
		assertTrue( keyDerivation.derivationSteps.get(0).getClass().getName().contains("Argon") );
		assertTrue( keyDerivation.derivationSteps.get(3).getClass().getName().contains("BCrypt") );
		assertTrue( keyDerivation.derivationSteps.get(1).getClass().getName().contains("SCrypt") );
	}

	/**
	 * Test method for {@link org.cryptonomicon.KeyDerivation#deriveKey(com.kosprov.jargon2.api.Jargon2.ByteArray, com.kosprov.jargon2.api.Jargon2.ByteArray)}.
	 * @throws GeneralSecurityException 
	 */
	@Test
	public void testDeriveKey() throws GeneralSecurityException {
		ByteArray salt = Jargon2.toByteArray( new byte[ 16 ] );
		keyDerivation.loadDerivationSteps(salt);
		String[] passwords = {"Hello", "Goodbye", "Password"};
		String[] expected = {
			"(32) d4c986df076f248c9a3e9697d74fe4426eeed1e1d608adfe4b669a998a017743",
			"(32) 7c53cafc466a2423f2128c4ffcc1273363a76613ccdb4781b16814dba65f6558",
			"(32) 8f1517c45d29366ef4b15e79488e351db1db79d3bc7734f238c131248de673c0"
		};
		for (int i = 0; i < passwords.length; i++) {
			ByteArray password = Jargon2.toByteArray( passwords[i].getBytes() );
			long start = System.nanoTime();
			ByteArray key = keyDerivation.deriveKey(password, salt);
			double elapsed = 1e-9 * (double)(System.nanoTime() - start);
			assertTrue( elapsed > 2.0 && elapsed < 16.0 );
			//System.out.printf( "%10.3f %s\n", elapsed, Wilkins.toString( key.getBytes() ) );
			assertTrue( Util.toString(key.getBytes()).equals(expected[i]) );
		}
	}

}
