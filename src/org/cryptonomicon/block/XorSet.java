package org.cryptonomicon.block;

import java.util.ArrayList;

public interface XorSet {

	/**
	 * Gets the max blocks.
	 *
	 * @return the maxBlocks
	 */
	public abstract int getMaxBlocks();

	/**
	 * Gets the xor of all.
	 *
	 * @return the xorOfAll
	 */
	public abstract BlockList getXorOfAll();

	/**
	 * Gets the xor except.
	 *
	 * @return the xorExcept
	 */
	public abstract ArrayList<BlockList> getXorExcept();

	/**
	 * Gets the iterators.
	 *
	 * @return the iterators
	 */
	public abstract ArrayList<BlockListIterator> getIterators();

}