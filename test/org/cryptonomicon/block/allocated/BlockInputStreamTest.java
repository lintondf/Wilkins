/**
 * 
 */
package org.cryptonomicon.block.allocated;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.allocated.AllocatedBlock;
import org.cryptonomicon.block.allocated.AllocatedBlockInputStream;
import org.cryptonomicon.block.allocated.AllocatedBlockList;
import org.junit.Test;

/**
 * @author lintondf
 *
 */
public class BlockInputStreamTest {

	/**
	 * Test method for {@link org.cryptonomicon.block.allocated.AllocatedBlockInputStream#read()}.
	 */
	@Test
	public void testRead() {
		byte[] array = new byte[Block.BLOCK_SIZE];
		AllocatedBlockList l1 = new AllocatedBlockList();
		Arrays.fill(array, (byte) 0x01 );
		l1.add( new AllocatedBlock(array) );
		Arrays.fill(array, (byte) 0x02 );
		l1.add( new AllocatedBlock(array) );
		Arrays.fill(array, (byte) 0x03 );
		l1.add( new AllocatedBlock(array) );
		
		byte[] input = new byte[Block.BLOCK_SIZE];
		try {
			AllocatedBlockInputStream bis = new AllocatedBlockInputStream( l1 );
			assertTrue( bis.available() > 0);
			int where = 0;
			for (int i = 0; i < 8; i++) {
				int n = bis.read(input, where, Block.BLOCK_SIZE/8 );
				assertTrue( n == Block.BLOCK_SIZE/8);
				where += n;
			}
			assertTrue( bis.available() > 0);
			Arrays.fill(array, (byte) 0x01 );
			assertTrue( Arrays.equals( array, input ) );
			bis.read( input, 0, Block.BLOCK_SIZE);
			assertTrue( bis.available() > 0);
			Arrays.fill(array, (byte) 0x02 );
			assertTrue( Arrays.equals( array, input ) );
			bis.read( input, 0, Block.BLOCK_SIZE);
			assertTrue( bis.available() == 0);
			Arrays.fill(array, (byte) 0x03 );
			assertTrue( Arrays.equals( array, input ) );
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}
