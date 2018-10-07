/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.KeyDerivationParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public class FileHeaderTest {

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

//	File file = null;
//	byte[] salt = new byte[256/8];
//	// TODO from KDP
//	FileHeader fileHeader = new FileHeader( Type.ARGON2i, Version.V10, 1024, 5, 256, salt );
//	
//	try {
//		file = File.createTempFile("testFileHeader", "bin");
//		RandomAccessFile raf = new RandomAccessFile( file, "rw" );
//		raf.write( fileHeader.getPlainText(), 0, fileHeader.getPlainText().length);
//		raf.seek(0L);
//		
////		FileHeader h2 = new FileHeader( raf );
////		
////		assertTrue( h2.isValid() );
//////		assertTrue( h2.getType() == Type.ARGON2i);
//////		assertTrue( h2.getVersion() == Version.V10 );
//////		assertTrue( h2.getMemoryCost() == 1024 );
//////		assertTrue( h2.getTimeCost() == 5 );
//////		assertTrue( h2.getKeySize() == 256 );
////		byte[] check = h2.getSalt();
////		assertTrue( check != null && Arrays.equals(salt,  check) );
//		
//		raf.close();
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail( e.getMessage() );
//	} finally {
//		if (file != null) {
//			file.delete();
//		}
//	}
	
	
	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#FileHeader(org.cryptonomicon.configuration.KeyDerivationParameters, com.kosprov.jargon2.api.Jargon2.ByteArray)}.
	 */
	@Test
	public void testFileHeaderKeyDerivationParametersByteArray() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters parameters = configuration.getKeyDerivationParameters();
		Random random = new Random();
		byte[] saltArray = new byte[256/8];
		random.nextBytes(saltArray);
		ByteArray salt = Jargon2.toByteArray( saltArray );
		FileHeader fh = new FileHeader( parameters, salt );
		assertTrue( fh.isValid() );
		KeyDerivationParameters kdp2 = fh.getKeyDerivationParameters();
		assertTrue( parameters.toString().equals(kdp2.toString()) );
		assertTrue( Arrays.equals(saltArray, fh.getSalt().getBytes() ) );
	}

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#FileHeader(org.cryptonomicon.configuration.Configuration, java.io.RandomAccessFile)}.
	 */
	@Test
	public void testFileHeaderConfigurationRandomAccessFile() {
		File file = null;
		Configuration configuration = new Configuration();
		KeyDerivationParameters parameters = configuration.getKeyDerivationParameters();
		Random random = new Random();
		byte[] saltArray = new byte[256/8];
		random.nextBytes(saltArray);
		ByteArray salt = Jargon2.toByteArray( saltArray );
		FileHeader fh = new FileHeader( parameters, salt );
		
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );
			fh.write(raf);
			raf.seek(0L);
			
			FileHeader h2 = new FileHeader( configuration, raf );
			assertTrue( h2.isValid() );
			KeyDerivationParameters kdp2 = h2.getKeyDerivationParameters();
			assertTrue( parameters.toString().equals(kdp2.toString()) );
			assertTrue( Arrays.equals(saltArray, h2.getSalt().getBytes() ) );
			
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#isValid()}.
	 */
	@Test
	public void testIsValid() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters parameters = configuration.getKeyDerivationParameters();
		Random random = new Random();
		byte[] saltArray = new byte[256/8];
		random.nextBytes(saltArray);
		ByteArray salt = Jargon2.toByteArray( saltArray );
		FileHeader fh = new FileHeader( parameters, salt );
		assertTrue( fh.isValid() );
		fh.plainText = new byte[ fh.plainText.length-1];
		assertFalse( fh.isValid() );
		fh.plainText = null;
		assertFalse( fh.isValid() );		
	}

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#getIV(int)}.
	 */
	@Test
	public void testGetIV() {
		Configuration configuration = new Configuration();
		KeyDerivationParameters parameters = configuration.getKeyDerivationParameters();
		Random random = new Random();
		byte[] saltArray = new byte[256/8];
		random.nextBytes(saltArray);
		ByteArray salt = Jargon2.toByteArray( saltArray );
		FileHeader fh = new FileHeader( parameters, salt );
		final int n = Wilkins.AES_IV_BYTES;
		for (int i = 0; i < saltArray.length-n; i++) {
			byte[] iv = fh.getIV(i);
			assertTrue( iv.length == n );
			assertTrue( Arrays.equals(iv, Arrays.copyOfRange(saltArray, i, i+n)));
		}
	}

}
