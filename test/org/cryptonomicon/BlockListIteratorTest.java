package org.cryptonomicon;

import static org.junit.Assert.*;

import org.junit.Test;

public class BlockListIteratorTest {

	@Test
	public void testBlockListIterator() {
		BlockList blockList = new BlockList();
		Block block = new Block();
		block.count = 10;
		blockList.add(block);
		block = new Block();
		block.count = 20;
		blockList.add(block);
		block = new Block();
		block.count = 30;
		blockList.add(block);
		
		BlockListIterator it = new BlockListIterator( blockList.getList().iterator() );
		
		assertTrue( it.hasNext() );
		assertTrue( it.next().count == 10 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().count == 20 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().count == 30 );
		assertFalse( it.hasNext() );
	}

	@Test
	public void testCurrent() {
		BlockList blockList = new BlockList();
		Block block = new Block();
		block.count = 10;
		blockList.add(block);
		block = new Block();
		block.count = 20;
		blockList.add(block);
		block = new Block();
		block.count = 30;
		blockList.add(block);
		
		BlockListIterator it = new BlockListIterator( blockList.getList().iterator() );
		
		assertTrue( it.hasNext() );
		assertTrue( it.next().count == 10 );
		assertTrue( it.current().count == 10 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().count == 20 );
		assertTrue( it.current().count == 20 );
		assertTrue( it.hasNext() );
		assertTrue( it.next().count == 30 );
		assertTrue( it.current().count == 30 );
		assertFalse( it.hasNext() );
	}

}
