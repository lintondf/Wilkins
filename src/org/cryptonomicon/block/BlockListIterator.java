package org.cryptonomicon.block;

import java.util.Iterator;

public class BlockListIterator implements Iterator<Block> {
	protected Iterator<Block> it;
	protected Block current;
	
	public BlockListIterator( Iterator<Block> it ) {
		this.it = it;
	}
	
	@Override
	public synchronized boolean hasNext() {
		return it.hasNext();
	}


	@Override
	public synchronized Block next() {
		current = it.next();
		return current;
	}
	
	public Block current() {
		return current;
	}
	
	
}