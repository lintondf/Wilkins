package org.cryptonomicon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class BlockTest {

	@Test
	public void testBlock() {
		Block block = new Block();
		assertTrue( block.count == 0 );
		assertTrue( block.contents != null && block.contents.length == Block.BLOCK_SIZE );
		assertTrue( Arrays.equals(block.contents, new byte[Block.BLOCK_SIZE]));
	}

	@Test
	public void testBlockBlock() {
		Random random = new Random();
		Block block = new Block();
		byte[] values = new byte[512];
		random.nextBytes(values);
		Block b2 = new Block( values );
		Block b3 = new Block( b2 );
		assertTrue( b3.count == values.length );
		assertTrue( b3.contents != null && b3.contents.length == Block.BLOCK_SIZE );
		assertTrue( Arrays.equals(values, Arrays.copyOf(b3.contents, values.length)));
	}

	@Test
	public void testBlockByteArray() {
		Random random = new Random();
		Block block = new Block();
		byte[] values = new byte[512];
		random.nextBytes(values);
		Block b2 = new Block( values );
		assertTrue( b2.count == values.length );
		assertTrue( b2.contents != null && b2.contents.length == Block.BLOCK_SIZE );
		assertTrue( Arrays.equals(values, Arrays.copyOf(b2.contents, values.length)));
	}

	@Test
	public void testPad() {
		Random random = new Random();
		byte[] values = new byte[512];
		random.nextBytes(values);
		Block block = new Block(values);
		int n = Block.BLOCK_SIZE-values.length;
		assertTrue( Arrays.equals(values, Arrays.copyOf(block.contents, values.length)));
		assertTrue( Arrays.equals(new byte[n], Arrays.copyOfRange(block.contents, values.length, Block.BLOCK_SIZE)));
		block.pad();
		assertTrue( Arrays.equals(values, Arrays.copyOf(block.contents, values.length)));
		assertFalse( Arrays.equals(new byte[n], Arrays.copyOfRange(block.contents, values.length, n)));
	}

	@Test
	public void testXorBlock() {
		Block b1 = new Block();
		b1.count = Block.BLOCK_SIZE;
		Arrays.fill(b1.contents, (byte) 0x01 );
		Block b2 = new Block();
		b2.count = Block.BLOCK_SIZE;
		Arrays.fill(b2.contents, (byte) 0x03 );
		Block b3 = b2.xor(b1);
		assertTrue( b2.count == Block.BLOCK_SIZE );
		byte[] check = new byte[Block.BLOCK_SIZE];
		Arrays.fill(check, (byte) (0x01 ^ 0x03) );
		
		assertTrue( Arrays.equals(b3.contents, check));
	}

	@Test
	public void testToString() {
		Block b1 = new Block();
		b1.count = 5;
		Arrays.fill(b1.contents, (byte) 0x01 );
		String result = b1.toString();
		//System.out.println( b1.toString() );
		assertTrue( result.equals("   5: 0101010101") );
	}
}
