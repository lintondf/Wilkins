package org.cryptonomicon.block;

import java.util.Iterator;

public class BlockListIterator implements Iterator<AbstractBlock> {
	protected Iterator<AbstractBlock> it;
	protected AbstractBlock current;
	
	public BlockListIterator( Iterator<AbstractBlock> iterator ) {
		this.it = iterator;
	}
	
	@Override
	public synchronized boolean hasNext() {
		return it.hasNext();
	}


	@Override
	public synchronized AbstractBlock next() {
		current = it.next();
		return current;
	}
	
	public AbstractBlock current() {
		return current;
	}
	
	
}