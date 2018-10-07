package org.cryptonomicon.block;

import static org.junit.Assert.*;

import org.cryptonomicon.block.AllocatedBlock;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.junit.Test;

public class BlockListIteratorTest {

	@Test
	public void testBlockListIterator() {
		BlockList blockList = new BlockList();
		AllocatedBlock block = new AllocatedBlock();
		block.setCount(10);
		blockList.add(block);
		block = new AllocatedBlock();
		block.setCount(20);
		blockList.add(block);
		block = new AllocatedBlock();
		block.setCount(30);
		blockList.add(block);
		
		BlockListIterator it = new BlockListIterator( blockList.getList().iterator() );
		
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 10 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 20 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 30 );
		assertFalse( it.hasNext() );
	}

	@Test
	public void testCurrent() {
		BlockList blockList = new BlockList();
		AllocatedBlock block = new AllocatedBlock();
		block.setCount(10);
		blockList.add(block);
		block = new AllocatedBlock();
		block.setCount(20);
		blockList.add(block);
		block = new AllocatedBlock();
		block.setCount(30);
		blockList.add(block);
		
		BlockListIterator it = new BlockListIterator( blockList.getList().iterator() );
		
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 10 );
		assertTrue( it.current().getCount() == 10 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 20 );
		assertTrue( it.current().getCount() == 20 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().getCount() == 30 );
		assertTrue( it.current().getCount() == 30 );
		assertFalse( it.hasNext() );
	}

}
