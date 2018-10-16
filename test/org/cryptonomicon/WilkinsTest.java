/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;
import htsjdk.samtools.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.BlockedFile.State;
import org.cryptonomicon.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public class WilkinsTest {
	
	Wilkins wilkins = null;
	static Main main = null;
	
	@BeforeClass
	public static void setUpOnce() {
		main = new Main(); // initialize logging only
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		wilkins = new Wilkins();
		CommandLineParser parser = new DefaultParser();
		Options options = Main.getOptions();
		wilkins.configuration.addOptions(options);
		String[] args = { // faster testing
				"--pbkdf2-iterations", "10000",
				"--argon-memory-cost", "1024",
				"--argon-parallelism", "1",
				"--argon-timeCost", "2",
				"--bcrypt-rounds", "4",
				"--scrypt-n", "1024",
				"--scrypt-p", "2",
				"--scrypt-r", "1"
		};
		CommandLine line = parser.parse(options, args);
		wilkins.configuration.getKeyDerivationParameters().set( line );
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.cryptonomicon.Wilkins#addDataFile(java.lang.String, org.cryptonomicon.FileHeader, com.kosprov.jargon2.api.Jargon2.ByteArray)}.
	 */
	@Test
	public void testAddDataFile() {
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1")) );
		assertTrue( wilkins.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2")) );
		
		assertTrue( wilkins.dataFiles.size() == 2);
	}

	/**
	 * Test method for {@link org.cryptonomicon.Wilkins#setRandomFillerCount(int)}.
	 */
	@Test
	public void testSetRandomFillerCount() {
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1")) );
		assertTrue( wilkins.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2")) );
		
		wilkins.setPadding(2);
		wilkins.setRandomFillerCount(2);
		try {
			assertTrue( wilkins.load( fileHeader ) );
			
			assertTrue( wilkins.allFiles.size() == 4);
			assertTrue( wilkins.maxBlocks == 57 );
			for (BlockedFile file : wilkins.allFiles ) {
				assertTrue( file.getSecretKey() != null );
				assertTrue( file.getState() == State.ENCRYPTED);
				assertTrue( file.getBlockList().size() == 57 );
			}
		} catch (IOException e) {
			fail( e.getMessage() );
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.Wilkins#setPadding(int)}.
	 */
	@Test
	public void testSetPadding_Count() {
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1")) );
		assertTrue( wilkins.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2")) );
		
		wilkins.setPadding(2);
		wilkins.setRandomFillerCount(1);
		try {
			assertTrue( wilkins.load( fileHeader ) );
			
			assertTrue( wilkins.allFiles.size() == 3);
			assertTrue( wilkins.maxBlocks == 57 );
			for (BlockedFile file : wilkins.allFiles ) {
				assertTrue( file.getSecretKey() != null );
				assertTrue( file.getState() == State.ENCRYPTED);
				assertTrue( file.getBlockList().size() == 57 );
			}
		} catch (IOException e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void testSetPadding_Random() {
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1")) );
		assertTrue( wilkins.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2")) );
		
		wilkins.setPadding(0);
		wilkins.setRandomFillerCount(1);
		try {
			assertTrue( wilkins.load( fileHeader ) );
			
			assertTrue( wilkins.allFiles.size() == 3);
			assertTrue( wilkins.maxBlocks >= 61 );
			assertTrue( wilkins.maxBlocks <= 83 );
			for (BlockedFile file : wilkins.allFiles ) {
				assertTrue( file.getSecretKey() != null );
				assertTrue( file.getState() == State.ENCRYPTED);
				assertTrue( file.getBlockList().size() == wilkins.maxBlocks );
			}
		} catch (IOException e) {
			fail( e.getMessage() );
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.Wilkins#read(java.io.RandomAccessFile, java.io.OutputStream, com.kosprov.jargon2.api.Jargon2.ByteArray)}.
	 */
	@Test
	public void testRead() {
		ByteArray[] keys = {
				Jargon2.toByteArray("key1"),
				Jargon2.toByteArray("key2"),
		};
		String[] fileNames = {
			"data1.txt",
			"data2.txt",
		};
		
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile(fileNames[0], fileHeader, keys[0]) );
		assertTrue( wilkins.addDataFile(fileNames[1], fileHeader, keys[1]) );
		
		wilkins.setPadding(1);
		wilkins.setRandomFillerCount(1);
		File test = null;
		try {
			test = File.createTempFile("Wilkins_Write", ".haystack");
			assertTrue( wilkins.load( fileHeader ) );
			
			assertTrue( wilkins.allFiles.size() == 3);
			assertTrue( wilkins.maxBlocks == 56 );
			
			RandomAccessFile raf = new RandomAccessFile( test, "rw" );
			assertTrue( wilkins.write(raf, fileHeader) );
			raf.close();
			
			assertTrue( test.length() == 160+56*(3+1)*Block.BLOCK_SIZE );
			
			for (int i = 0; i < keys.length; i++) {
				ByteArray passPhrase = keys[i];
				raf = new RandomAccessFile( test, "rw" );
				File result = File.createTempFile("Wilkins_Write", "check");
				FileOutputStream os = new FileOutputStream( result );
				
				assertTrue( wilkins.read(raf, os, passPhrase) );
				
				//System.out.println( result.length() );
				raf.close();
				String expected = IOUtil.readFully(new FileInputStream(fileNames[i]));
				String actual = IOUtil.readFully(new FileInputStream(result));
				//System.out.println(i  +": "+ fileNames[i] + " / " + expected.length() + " " + actual.length() );
				assertTrue( actual.equals(expected) );
			}
		} catch (IOException | GeneralSecurityException e) {
			fail( e.getMessage() );
		} finally {
			if (test != null) test.delete();
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.Wilkins#load(java.io.RandomAccessFile, org.cryptonomicon.FileHeader)}.
	 */
	@Test
	public void testLoad() {
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1")) );
		assertTrue( wilkins.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2")) );
		
		wilkins.setPadding(1);
		wilkins.setRandomFillerCount(1);
		try {
			assertTrue( wilkins.load( fileHeader ) );
			
			assertTrue( wilkins.allFiles.size() == 3);
			assertTrue( wilkins.maxBlocks == 56 );
			for (BlockedFile file : wilkins.allFiles ) {
				assertTrue( file.getSecretKey() != null );
				assertTrue( file.getState() == State.ENCRYPTED);
				assertTrue( file.getBlockList().size() == 56 );
			}
			
		} catch (IOException e) {
			fail( e.getMessage() );
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.Wilkins#write(java.io.RandomAccessFile, org.cryptonomicon.FileHeader)}.
	 */
	@Test
	public void testWrite() {
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		Configuration.getSecureRandom().nextBytes(iv);

		FileHeader fileHeader = new FileHeader(wilkins.parameters, Jargon2.toByteArray(iv) );

		assertTrue( wilkins.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1")) );
		assertTrue( wilkins.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2")) );
		
		wilkins.setPadding(1);
		wilkins.setRandomFillerCount(1);
		File test = null;
		try {
			test = File.createTempFile("Wilkins_Write", ".haystack");
			assertTrue( wilkins.load( fileHeader ) );
			
			assertTrue( wilkins.allFiles.size() == 3);
			assertTrue( wilkins.maxBlocks == 56 );
			
			RandomAccessFile raf = new RandomAccessFile( test, "rw" );
			assertTrue( wilkins.write(raf, fileHeader) );
			raf.close();
			
			assertTrue( test.length() == 160+56*(3+1)*Block.BLOCK_SIZE );
			
		} catch (IOException e) {
			fail( e.getMessage() );
		} finally {
			if (test != null) test.delete();
		}
	}

}