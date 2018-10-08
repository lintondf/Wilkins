/**
 * 
 */
package org.cryptonomicon.block.allocated;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.cryptonomicon.Wilkins;
import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.BlockedFile.State;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;
import org.cryptonomicon.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.ClearableSourceByteArray;

/**
 * @author lintondf
 *
 */
public class BlockedFileTest {

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
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#BlockedFile(java.io.File, ByteArray)}.
	 */
	@Test
	public void testBlockedFileFileByteArray() {
		ByteArray salt = Jargon2.toByteArray(new byte[32]);
		File file = new File("roro.zot");
		AllocatedBlockedFile bf = new AllocatedBlockedFile( file, salt );
		assertTrue( bf.getFile().equals(file) );
		assertTrue( Arrays.equals( bf.getSecretKey().getEncoded(), salt.getBytes() ) );
		assertTrue( bf.getLength() == file.length() );
		assertTrue( bf.getBlockList() == null );
		assertTrue( bf.getState() == State.IDLE );
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#BlockedFile(ByteArray, int)}.
	 */
	@Test
	public void testBlockedFileByteArrayInt() {
		ByteArray salt = Jargon2.toByteArray(new byte[32]);
		AllocatedBlockedFile bf = new AllocatedBlockedFile( salt, 2 );
		assertTrue( bf.getFile() == null );
		assertTrue( Arrays.equals( bf.getSecretKey().getEncoded(), salt.getBytes() ) );
		assertTrue( bf.getLength() == 2 * Block.BLOCK_SIZE );
		assertTrue( bf.getBlockList() != null && bf.getBlockList().size() == 2 );
		assertTrue( bf.getState() == State.RAW );
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#pad(int)}.
	 */
	@Test
	public void testPad() {
		ByteArray salt = Jargon2.toByteArray(new byte[32]);
		AllocatedBlockedFile bf = new AllocatedBlockedFile( salt, 2 );
		bf.pad(3);
		assertTrue( bf.getLength() == 3 * Block.BLOCK_SIZE );
		assertTrue( bf.getBlockList() != null && bf.getBlockList().size() == 3 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#getInputStream(java.io.InputStream, byte[])}.
	 */
	@Test
	public void testGetInputStream() {
		File inFile = null;
		File outFile = null;
		ByteArray salt = Jargon2.toByteArray( new byte[256/8] );
		byte[] iv = new byte[Configuration.AES_IV_BYTES];

		try {
			inFile = File.createTempFile("testFileHeaderIn", "bin");
			outFile = File.createTempFile("testFileHeaderOut", "bin");
			RandomAccessFile raf = new RandomAccessFile( inFile, "rw" );
			byte[] block = new byte[Block.BLOCK_SIZE];
			for (int i = 0; i < 3; i++) {
				Arrays.fill( block, (byte) i );
				raf.write(block);
			}
			Arrays.fill(block, (byte) 4 );
			raf.write(block, 0, 10);
			raf.close();
			
			BlockedFile bf = new AllocatedBlockedFile( inFile, salt );
			InputStream is = bf.getInputStream( new FileInputStream(inFile), iv);

			byte[] result = IOUtils.toByteArray(is);
			assertTrue( result.length == 48 );
			final String expectedDE = "(48) 33bad306982656f13130872281a011ba219cf5d1cae77558c4bea09e772479e1bda20cd08c959e7a3d61e5236d2a5dec";
			assertTrue( expectedDE.equals(Wilkins.toString(result)));
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			OutputStream os = bf.getOutputStream(bos, iv);
			os.write(result);
			os.close();
			
			result = bos.toByteArray();
			//System.out.println( Wilkins.toString(result));
			
			for (int i = 0; i < 3; i++) {
				Arrays.fill( block, (byte) i );
				byte[] expected = Arrays.copyOfRange(result, i*Block.BLOCK_SIZE, (i+1)*Block.BLOCK_SIZE);
				assertTrue( Arrays.equals(block, expected));
			}
			Arrays.fill(block, (byte) 4 );
			byte[] expected = Arrays.copyOfRange(result, 3*Block.BLOCK_SIZE, 10+3*Block.BLOCK_SIZE);
			assertTrue( Arrays.equals(Arrays.copyOf(block, 10), expected));
			
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		} finally {
			if (inFile != null) {
				inFile.delete();
			}
			if (outFile != null) {
				outFile.delete();
			}
		}
	}

	
	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#deflate(int)}.
	 */
	@Test
	public void testDeflate() {
		File inFile = null;
		File outFile = null;
		ByteArray salt = Jargon2.toByteArray( new byte[256/8] );
		try {
			inFile = File.createTempFile("testFileHeaderIn", "bin");
			outFile = File.createTempFile("testFileHeaderOut", "bin");
			RandomAccessFile raf = new RandomAccessFile( inFile, "rw" );
			byte[] block = new byte[Block.BLOCK_SIZE];
			for (int i = 0; i < 3; i++) {
				Arrays.fill( block, (byte) i );
				raf.write(block);
			}
			Arrays.fill(block, (byte) 4 );
			raf.write(block, 0, 10);
			raf.close();
			
			BlockedFile bf = new AllocatedBlockedFile( inFile, salt );
			bf.deflate(6);
			
			assertTrue( bf.getState() == State.ZIPPED );
			assertTrue( bf.getBlockList().size() == 1 );
			assertTrue( bf.getBlockList().get(0).getCount() == 32 );
			
			bf.inflate(outFile);
			byte[] result = IOUtils.toByteArray(new FileReader(outFile) );
			//System.out.println(result.length);
			assertTrue( result.length == 3*Block.BLOCK_SIZE + 10 );
			
			for (int i = 0; i < 3; i++) {
				Arrays.fill( block, (byte) i );
				byte[] expected = Arrays.copyOfRange(result, i*Block.BLOCK_SIZE, (i+1)*Block.BLOCK_SIZE);
				assertTrue( Arrays.equals(block, expected));
			}
			Arrays.fill(block, (byte) 4 );
			byte[] expected = Arrays.copyOfRange(result, 3*Block.BLOCK_SIZE, 10+3*Block.BLOCK_SIZE);
			assertTrue( Arrays.equals(Arrays.copyOf(block, 10), expected));
			
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		} finally {
			if (inFile != null) {
				inFile.delete();
			}
			if (outFile != null) {
				outFile.delete();
			}
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#encrypt(byte[])}.
	 */
	@Test
	public void testEncrypt() {
		File inFile = null;
		File outFile = null;
		ByteArray salt = Jargon2.toByteArray( new byte[256/8] );
		try {
			inFile = File.createTempFile("testFileHeaderIn", "bin");
			outFile = File.createTempFile("testFileHeaderOut", "bin");
			RandomAccessFile raf = new RandomAccessFile( inFile, "rw" );
			byte[] iv = new byte[Configuration.AES_IV_BYTES];
			byte[] block = new byte[Block.BLOCK_SIZE];
			for (int i = 0; i < 3; i++) {
				Arrays.fill( block, (byte) i );
				raf.write(block);
			}
			Arrays.fill(block, (byte) 4 );
			raf.write(block, 0, 10);
			raf.close();
			
			BlockedFile bf = new AllocatedBlockedFile( inFile, salt );
			bf.deflate(6);
			
			assertTrue( bf.getState() == State.ZIPPED );
			assertTrue( bf.getBlockList().size() == 1 );
			assertTrue( bf.getBlockList().get(0).getCount() == 32 );
			
			bf.encrypt(iv);
			assertTrue( bf.getState() == State.ENCRYPTED );
			assertTrue( bf.getBlockList().size() == 1 );
			assertTrue( bf.getBlockList().get(0).getCount() == 48 );
			
			bf.decrypt(iv);
			
			assertTrue( bf.getState() == State.ZIPPED );
			assertTrue( bf.getBlockList().size() == 1 );
			assertTrue( bf.getBlockList().get(0).getCount() == 32 );
			
			bf.inflate(outFile);
			byte[] result = IOUtils.toByteArray(new FileReader(outFile) );
			//System.out.println(result.length + " " + Wilkins.toString(result));
			assertTrue( result.length == 3*Block.BLOCK_SIZE + 10 );
			
			for (int i = 0; i < 3; i++) {
				byte[] expected = Arrays.copyOfRange(result, i*Block.BLOCK_SIZE, (i+1)*Block.BLOCK_SIZE);
				Arrays.fill( block, (byte) i );
				assertTrue( Arrays.equals(block, expected));
			}
			Arrays.fill(block, (byte) 4 );
			byte[] expected = Arrays.copyOfRange(result, 3*Block.BLOCK_SIZE, 10+3*Block.BLOCK_SIZE);
			assertTrue( Arrays.equals(Arrays.copyOf(block, 10), expected));
			
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		} finally {
			if (inFile != null) {
				inFile.delete();
			}
			if (outFile != null) {
				outFile.delete();
			}
		}
	}

}
