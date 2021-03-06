package org.cryptonomicon.block.allocated;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.allocated.AllocatedBlock;
import org.junit.Test;

public class BlockTest {

	@Test
	public void testBlock() {
		AllocatedBlock block = new AllocatedBlock();
		assertTrue( block.getCount() == 0 );
		assertTrue( block.getContents() != null && block.getContents().length == Block.BLOCK_SIZE );
		assertTrue( Arrays.equals(block.getContents(), new byte[Block.BLOCK_SIZE]));
	}

	@Test
	public void testBlockBlock() {
		Random random = new Random();
		Block block = new AllocatedBlock();
		byte[] values = new byte[512];
		random.nextBytes(values);
		AllocatedBlock b2 = new AllocatedBlock( values );
		AllocatedBlock b3 = new AllocatedBlock( b2 );
		assertTrue( b3.getCount() == values.length );
		assertTrue( b3.getContents() != null && b3.getContents().length == Block.BLOCK_SIZE );
		assertTrue( Arrays.equals(values, Arrays.copyOf(b3.getContents(), values.length)));
	}

	@Test
	public void testBlockByteArray() {
		Random random = new Random();
		Block block = new AllocatedBlock();
		byte[] values = new byte[512];
		random.nextBytes(values);
		AllocatedBlock b2 = new AllocatedBlock( values );
		assertTrue( b2.getCount() == values.length );
		assertTrue( b2.getContents() != null && b2.getContents().length == Block.BLOCK_SIZE );
		assertTrue( Arrays.equals(values, Arrays.copyOf(b2.getContents(), values.length)));
	}

	@Test
	public void testPad() {
		Random random = new Random();
		byte[] values = new byte[512];
		random.nextBytes(values);
		AllocatedBlock block = new AllocatedBlock(values);
		int n = Block.BLOCK_SIZE-values.length;
		assertTrue( Arrays.equals(values, Arrays.copyOf(block.getContents(), values.length)));
		assertTrue( Arrays.equals(new byte[n], Arrays.copyOfRange(block.getContents(), values.length, Block.BLOCK_SIZE)));
		block.pad();
		assertTrue( Arrays.equals(values, Arrays.copyOf(block.getContents(), values.length)));
		assertFalse( Arrays.equals(new byte[n], Arrays.copyOfRange(block.getContents(), values.length, n)));
	}

	@Test
	public void testXorBlock() {
		AllocatedBlock b1 = new AllocatedBlock();
		b1.setCount(Block.BLOCK_SIZE);
		Arrays.fill(b1.getContents(), (byte) 0x01 );
		AllocatedBlock b2 = new AllocatedBlock();
		b2.setCount(Block.BLOCK_SIZE);
		Arrays.fill(b2.getContents(), (byte) 0x03 );
		Block b3 = b2.xor(b1);
		assertTrue( b2.getCount() == Block.BLOCK_SIZE );
		byte[] check = new byte[Block.BLOCK_SIZE];
		Arrays.fill(check, (byte) (0x01 ^ 0x03) );
		
		assertTrue( Arrays.equals(b3.getContents(), check));
	}

	@Test
	public void testToString() {
		AllocatedBlock b1 = new AllocatedBlock();
		b1.setCount(5);
		Arrays.fill(b1.getContents(), (byte) 0x01 );
		String result = b1.toString();
		//System.out.println( b1.toString() );
		assertTrue( result.equals("   5: 0101010101") );
	}
}
