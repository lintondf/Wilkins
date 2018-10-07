package org.cryptonomicon.block;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.junit.Test;

public class BlockListTest {

	@Test
	public void testBlockList() {
		BlockList blockList = new BlockList();
		assertTrue( blockList.getList() != null );
		assertTrue( blockList.getList().size() == 0 );
		assertTrue( blockList.length() == 0 );
	}

	@Test
	public void testAdd() {
		BlockList blockList = new BlockList();
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		assertTrue( blockList.getList().size() == 1 );
		assertTrue( blockList.size() == 1 );
		assertTrue( blockList.length() == 10 );
		assertTrue( blockList.getFirst().equals(block ));
		
	}

	@Test
	public void testLength() {
		BlockList blockList = new BlockList();
		assertTrue( blockList.length() == 0 );
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		assertTrue( blockList.length() == 10 );
		block = new Block();
		block.setCount(20);
		blockList.add(block);
		assertTrue( blockList.length() == 30 );
	}

	@Test
	public void testSize() {
		BlockList blockList = new BlockList();
		assertTrue( blockList.size() == 0 );
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		assertTrue( blockList.size() == 1 );
		block = new Block();
		block.setCount(20);
		blockList.add(block);
		assertTrue( blockList.size() == 2 );
	}

	@Test
	public void testGetIterator() {
		BlockList blockList = new BlockList();
		assertTrue( blockList.size() == 0 );
		BlockListIterator it = blockList.getIterator();
		assertFalse( it.hasNext() );
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		block = new Block();
		block.setCount(20);
		blockList.add(block);
		block = new Block();
		block.setCount(30);
		blockList.add(block);
		it = blockList.getIterator();
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 10 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 20 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 30 );
		assertFalse( it.hasNext() );
	}

	@Test
	public void testGetFirst() {
		BlockList blockList = new BlockList();
		assertTrue( blockList.size() == 0 );
		BlockListIterator it = blockList.getIterator();
		assertFalse( it.hasNext() );
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		block = new Block();
		block.setCount(20);
		blockList.add(block);
		block = new Block();
		block.setCount(30);
		blockList.add(block);
		assertTrue( blockList.getFirst().getCount() == 10);
	}

	@Test
	public void testGetLast() {
		BlockList blockList = new BlockList();
		assertTrue( blockList.size() == 0 );
		BlockListIterator it = blockList.getIterator();
		assertFalse( it.hasNext() );
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		block = new Block();
		block.setCount(20);
		blockList.add(block);
		block = new Block();
		block.setCount(30);
		blockList.add(block);
		assertTrue( blockList.getLast().getCount() == 30);
	}

	@Test
	public void testGetList() {
		BlockList blockList = new BlockList();
		List<AbstractBlock> list = blockList.getList();
		assertTrue( list != null );
		assertTrue( list.size() == 0 );
	}

	@Test
	public void testPad() {
		BlockList blockList = new BlockList();
		Block block = new Block();
		block.setCount(10);
		blockList.add(block);
		block = new Block();
		block.setCount(20);
		blockList.add(block);
		block = new Block();
		block.setCount(30);
		blockList.add(block);
		assertTrue( blockList.size() == 3 );
		BlockList.pad( blockList, 5);
		assertTrue( blockList.size() == 5 );
		BlockListIterator it = blockList.getIterator();
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 10 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 20 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 30 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == AbstractBlock.BLOCK_SIZE );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == AbstractBlock.BLOCK_SIZE );
		assertFalse( it.hasNext() );		
	}

	@Test
	public void testXorListOfBlockList() {
		byte[] array = new byte[AbstractBlock.BLOCK_SIZE];
		BlockList l1 = new BlockList();
		Arrays.fill(array, (byte) 0x01 );
		l1.add( new Block(array) );
		Arrays.fill(array, (byte) 0x02 );
		l1.add( new Block(array) );
		Arrays.fill(array, (byte) 0x03 );
		l1.add( new Block(array) );
		
		BlockList l2 = new BlockList();
		Arrays.fill(array, (byte) 0x04 );
		l2.add( new Block(array) );
		Arrays.fill(array, (byte) 0x05 );
		l2.add( new Block(array) );
		Arrays.fill(array, (byte) 0x06 );
		l2.add( new Block(array) );
		
		BlockList l3 = new BlockList();
		Arrays.fill(array, (byte) 0x07 );
		l3.add( new Block(array) );
		Arrays.fill(array, (byte) 0x08 );
		l3.add( new Block(array) );
		Arrays.fill(array, (byte) 0x09 );
		l3.add( new Block(array) );
		
		List<BlockList> all = new ArrayList<>();
		all.add(l1);
		all.add(l2);
		all.add(l3);
		BlockList lo = BlockList.xor( all );
		
		assertTrue( lo.size() == 3 );
		Arrays.fill(array, (byte) (0x01 ^ 0x04 ^ 0x07) );
		BlockListIterator it = lo.getIterator();
		assertTrue( Arrays.equals(array, it.next().getContents() ) );
		Arrays.fill(array, (byte) (0x02 ^ 0x05 ^ 0x08) );
		assertTrue( Arrays.equals(array, it.next().getContents() ) );
		Arrays.fill(array, (byte) (0x03 ^ 0x06 ^ 0x09) );
		assertTrue( Arrays.equals(array, it.next().getContents() ) );
	}

	@Test
	public void testXorBlockListBlockList() {
		byte[] array = new byte[AbstractBlock.BLOCK_SIZE];
		BlockList l1 = new BlockList();
		Arrays.fill(array, (byte) 0x01 );
		l1.add( new Block(array) );
		Arrays.fill(array, (byte) 0x02 );
		l1.add( new Block(array) );
		Arrays.fill(array, (byte) 0x03 );
		l1.add( new Block(array) );
		
		BlockList l2 = new BlockList();
		Arrays.fill(array, (byte) 0x04 );
		l2.add( new Block(array) );
		Arrays.fill(array, (byte) 0x05 );
		l2.add( new Block(array) );
		Arrays.fill(array, (byte) 0x06 );
		l2.add( new Block(array) ); 
		
		BlockList lo = BlockList.xor( l1, l2 );
		
		assertTrue( lo.size() == 3 );
		Arrays.fill(array, (byte) (0x01 ^ 0x04) );
		BlockListIterator it = lo.getIterator();
		assertTrue( Arrays.equals(array, it.next().getContents() ) );
		Arrays.fill(array, (byte) (0x02 ^ 0x05) );
		assertTrue( Arrays.equals(array, it.next().getContents() ) );
		Arrays.fill(array, (byte) (0x03 ^ 0x06) );
		assertTrue( Arrays.equals(array, it.next().getContents() ) );
	}

}
