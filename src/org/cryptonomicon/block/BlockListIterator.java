package org.cryptonomicon.block;

import java.util.Iterator;

// TODO: Auto-generated Javadoc
/**
 * The Class BlockListIterator.
 */
public class BlockListIterator implements Iterator<Block> {
	
	/** The it. */
	protected Iterator<Block> it;
	
	/** The current. */
	protected Block current;
	
	/**
	 * Instantiates a new block list iterator.
	 *
	 * @param iterator the iterator
	 */
	public BlockListIterator( Iterator<Block> iterator ) {
		this.it = iterator;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public synchronized boolean hasNext() {
		return it.hasNext();
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public synchronized Block next() {
		current = it.next();
		return current;
	}
	
	/**
	 * Current.
	 *
	 * @return the block
	 */
	public Block current() {
		return current;
	}
	
	
}