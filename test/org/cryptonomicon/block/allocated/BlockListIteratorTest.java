package org.cryptonomicon.block.allocated;

import static org.junit.Assert.*;

import org.cryptonomicon.block.BlockListIterator;
import org.cryptonomicon.block.allocated.AllocatedBlock;
import org.cryptonomicon.block.allocated.AllocatedBlockList;
import org.junit.Test;

public class BlockListIteratorTest {

	@Test
	public void testBlockListIterator() {
		AllocatedBlockList blockList = new AllocatedBlockList();
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
		AllocatedBlockList blockList = new AllocatedBlockList();
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
