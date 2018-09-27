/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lintondf
 *
 */
public class BlockReaderTest {
	
	private File file = null;
	private RandomAccessFile raf;
	private long length;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			file = File.createTempFile("testFileHeader", "bin");
			raf = new RandomAccessFile( file, "rw" );
			Block block = new Block();
			for (int i = 0; i < 5; i++) {
				block.count = Block.BLOCK_SIZE;
				Arrays.fill(block.contents, (byte) i );
				raf.write(block.contents, 0, block.count );
			}
			block.count = 100;
			Arrays.fill(block.contents, (byte) 10 );
			raf.write(block.contents, 0, block.count );
			length = raf.getFilePointer();
		} catch (Exception x) {
			x.printStackTrace();
			fail("setup failed");
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (file != null) {
			file.delete();
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.BlockReader#read()}.
	 */
	@Test
	public void testRead() {
		try {
			raf.seek(0);
			BlockReader blockReader = new BlockReader( raf, length );
			Block block = blockReader.read();
			int iBlock = 0;
			byte[] check = new byte[Block.BLOCK_SIZE];
			while (block.count > 0) {
				Arrays.fill(check, (byte) ((iBlock < 5) ? iBlock : 10) );
				assertTrue( block.count == ((iBlock < 5) ? Block.BLOCK_SIZE : 100) );
				assertTrue( Arrays.equals(Arrays.copyOf(check, block.count), Arrays.copyOf(block.contents, block.count)));
				Block b2 = blockReader.getLast();
				assertTrue( b2 == block);
				block = blockReader.read();
				iBlock++;
			}
			assertTrue( iBlock == 6 );
			
			raf.seek(0);
			blockReader = new BlockReader( raf, 10 );
			block = blockReader.read();
			assertTrue(block.count == 10);
			Arrays.fill(check, (byte) 0x00 );
			assertTrue( Arrays.equals(Arrays.copyOf(check, block.count), Arrays.copyOf(block.contents, block.count)));
			block = blockReader.read();
			assertTrue(block.count == 0);
			
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}

	/**
	 * Test method for {@link org.cryptonomicon.BlockReader#readFull()}.
	 */
	@Test
	public void testReadFull() {
		try {
			raf.seek(0);
			BlockReader blockReader = new BlockReader( raf, length );
			Block block = blockReader.readFull();
			int iBlock = 0;
			byte[] check = new byte[Block.BLOCK_SIZE];
			while (block != null && block.count > 0) {
				Arrays.fill(check, (byte) ((iBlock < 5) ? iBlock : 10) );
				assertTrue( block.count == ((iBlock < 5) ? Block.BLOCK_SIZE : 100) );
				assertTrue( Arrays.equals(Arrays.copyOf(check, block.count), Arrays.copyOf(block.contents, block.count)));
				Block b2 = blockReader.getLast();
				assertTrue( b2 == block);
				block = blockReader.readFull();
				iBlock++;
			}
			assertTrue( iBlock == 6 );
			
			raf.seek(0);
			blockReader = new BlockReader( raf, 10 );
			block = blockReader.readFull();
			assertTrue(block.count == Block.BLOCK_SIZE);
			Arrays.fill(check, (byte) 0x00 );
			assertTrue( Arrays.equals(Arrays.copyOf(check, block.count), Arrays.copyOf(block.contents, block.count)));
			
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}

}
