/**
 * 
 */
package org.cryptonomicon.block;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockInputStream;
import org.cryptonomicon.block.BlockList;
import org.junit.Test;

/**
 * @author lintondf
 *
 */
public class BlockInputStreamTest {

	/**
	 * Test method for {@link org.cryptonomicon.block.BlockInputStream#read()}.
	 */
	@Test
	public void testRead() {
		byte[] array = new byte[AbstractBlock.BLOCK_SIZE];
		BlockList l1 = new BlockList();
		Arrays.fill(array, (byte) 0x01 );
		l1.add( new Block(array) );
		Arrays.fill(array, (byte) 0x02 );
		l1.add( new Block(array) );
		Arrays.fill(array, (byte) 0x03 );
		l1.add( new Block(array) );
		
		byte[] input = new byte[AbstractBlock.BLOCK_SIZE];
		try {
			BlockInputStream bis = new BlockInputStream( l1 );
			assertTrue( bis.available() > 0);
			int where = 0;
			for (int i = 0; i < 8; i++) {
				int n = bis.read(input, where, AbstractBlock.BLOCK_SIZE/8 );
				assertTrue( n == AbstractBlock.BLOCK_SIZE/8);
				where += n;
			}
			assertTrue( bis.available() > 0);
			Arrays.fill(array, (byte) 0x01 );
			assertTrue( Arrays.equals( array, input ) );
			bis.read( input, 0, AbstractBlock.BLOCK_SIZE);
			assertTrue( bis.available() > 0);
			Arrays.fill(array, (byte) 0x02 );
			assertTrue( Arrays.equals( array, input ) );
			bis.read( input, 0, AbstractBlock.BLOCK_SIZE);
			assertTrue( bis.available() == 0);
			Arrays.fill(array, (byte) 0x03 );
			assertTrue( Arrays.equals( array, input ) );
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}
