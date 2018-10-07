/**
 * 
 */
package org.cryptonomicon.block.allocated;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.BlockedFile.State;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;
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
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#getOutputStream(java.io.OutputStream, byte[])}.
	 */
	@Test
	public void testGetOutputStream() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#deflate(int)}.
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
			
			BlockedFile bf = new AllocatedBlockedFile( file, salt );
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
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#inflate(java.io.File)}.
	 */
	@Test
	public void testInflate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#encrypt(byte[])}.
	 */
	@Test
	public void testEncrypt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#decrypt(byte[])}.
	 */
	@Test
	public void testDecrypt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockedFile#toString()}.
	 */
	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
