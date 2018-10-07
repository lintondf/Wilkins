/**
 * 
 */
package org.cryptonomicon.block;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.BlockedFile.State;
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
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#BlockedFile(java.io.File, ByteArray)}.
	 */
	@Test
	public void testBlockedFileFileByteArray() {
		ByteArray salt = Jargon2.toByteArray(new byte[32]);
		File file = new File("roro.zot");
		BlockedFile bf = new BlockedFile( file, salt );
		assertTrue( bf.file.equals(file) );
		assertTrue( Arrays.equals( bf.secretKey.getEncoded(), salt.getBytes() ) );
		assertTrue( bf.length == file.length() );
		assertTrue( bf.blocks == null );
		assertTrue( bf.state == State.IDLE );
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#BlockedFile(ByteArray, int)}.
	 */
	@Test
	public void testBlockedFileByteArrayInt() {
		ByteArray salt = Jargon2.toByteArray(new byte[32]);
		BlockedFile bf = new BlockedFile( salt, 2 );
		assertTrue( bf.file == null );
		assertTrue( Arrays.equals( bf.secretKey.getEncoded(), salt.getBytes() ) );
		assertTrue( bf.length == 2 * Block.BLOCK_SIZE );
		assertTrue( bf.blocks != null && bf.blocks.size() == 2 );
		assertTrue( bf.state == State.RAW );
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#pad(int)}.
	 */
	@Test
	public void testPad() {
		ByteArray salt = Jargon2.toByteArray(new byte[32]);
		BlockedFile bf = new BlockedFile( salt, 2 );
		bf.pad(3);
		assertTrue( bf.length == 3 * Block.BLOCK_SIZE );
		assertTrue( bf.blocks != null && bf.blocks.size() == 3 );
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#getInputStream(java.io.InputStream, byte[])}.
	 */
	@Test
	public void testGetInputStream() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#getOutputStream(java.io.OutputStream, byte[])}.
	 */
	@Test
	public void testGetOutputStream() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#deflate(int)}.
	 */
	@Test
	public void testDeflate() {
		File file = null;
		ByteArray salt = Jargon2.toByteArray( new byte[256/8] );
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );
			byte[] block = new byte[Block.BLOCK_SIZE];
			for (int i = 0; i < 3; i++) {
				Arrays.fill( block, (byte) i );
				raf.write(block);
			}
			Arrays.fill(block, (byte) 4 );
			raf.write(block, 0, 10);
			raf.close();
			
			BlockedFile bf = new BlockedFile( file, salt );
			bf.deflate(6);
			
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
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#inflate(java.io.File)}.
	 */
	@Test
	public void testInflate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#encrypt(byte[])}.
	 */
	@Test
	public void testEncrypt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#decrypt(byte[])}.
	 */
	@Test
	public void testDecrypt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockedFile#toString()}.
	 */
	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
