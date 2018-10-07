package org.cryptonomicon.block;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface BlockList.
 */
public interface BlockList {

	/**
	 * Gets the iterator.
	 *
	 * @return the iterator
	 */
	public abstract BlockListIterator getIterator();

	/**
	 * Xor.
	 *
	 * @param blockList1 the block list 1
	 * @param blockList2 the block list 2
	 */
	public abstract void xor(BlockList blockList1, BlockList blockList2 );

	/**
	 * Xor.
	 *
	 * @param allLists the all lists
	 * @return the block list
	 */
	public abstract BlockList xor(List<BlockList> allLists);

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public abstract int size();
	
	/**
	 * Gets the.
	 *
	 * @param which the which
	 * @return the block
	 */
	public abstract Block get( int which );

	/**
	 * Make.
	 *
	 * @return the block list
	 */
	public abstract BlockList make();


}