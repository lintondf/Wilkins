/**
 * 
 */
package org.cryptonomicon.block.allocated;

import java.util.ArrayList;
import java.util.List;

import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.XorSet;

// TODO: Auto-generated Javadoc
/**
 * The Class XorSet.
 *
 * @author lintondf
 */
public class AllocatedXorSet implements XorSet {
	
	/** The max blocks. */
	int maxBlocks;
	
	/** The xor of all. */
	BlockList xorOfAll;
	
	/** The xor except. */
	ArrayList<BlockList> xorExcept;
	
	/** The iterators. */
	ArrayList<BlockListIterator> iterators;
	
	/**
	 * Instantiates a new xor set.
	 *
	 * @param maxBlocks the max blocks
	 * @param allFiles the all files
	 */
	public AllocatedXorSet( int maxBlocks, List<BlockedFile> allFiles ) {
		this.maxBlocks = maxBlocks;
		ArrayList<BlockList> allLists = new ArrayList<>();
		for (BlockedFile file : allFiles) {
			allLists.add( file.getBlockList() );
		}
		xorOfAll = new AllocatedBlockList();
		xorOfAll.xor(allLists);
		xorExcept = new ArrayList<>();
		iterators = new ArrayList<>();
		for (int iList = 0; iList < allLists.size(); iList++) {
			BlockList blockList = new AllocatedBlockList();
			blockList.xor( xorOfAll, allLists.get(iList) );
			xorExcept.add( blockList );
			iterators.add( blockList.getIterator() );
		}
		iterators.add( xorOfAll.getIterator() ); // in file order
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.allocated.XorSet#getMaxBlocks()
	 */
	@Override
	public int getMaxBlocks() {
		return maxBlocks;
	}

	/**
	 * Sets the max blocks.
	 *
	 * @param maxBlocks the maxBlocks to set
	 */
	public void setMaxBlocks(int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.allocated.XorSet#getXorOfAll()
	 */
	@Override
	public BlockList getXorOfAll() {
		return xorOfAll;
	}

	/**
	 * Sets the xor of all.
	 *
	 * @param xorOfAll the xorOfAll to set
	 */
	public void setXorOfAll(BlockList xorOfAll) {
		this.xorOfAll = xorOfAll;
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.allocated.XorSet#getXorExcept()
	 */
	@Override
	public ArrayList<BlockList> getXorExcept() {
		return xorExcept;
	}

	/**
	 * Sets the xor except.
	 *
	 * @param xorExcept the xorExcept to set
	 */
	public void setXorExcept(ArrayList<BlockList> xorExcept) {
		this.xorExcept = xorExcept;
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.allocated.XorSet#getIterators()
	 */
	@Override
	public ArrayList<BlockListIterator> getIterators() {
		return iterators;
	}

	/**
	 * Sets the iterators.
	 *
	 * @param iterators the iterators to set
	 */
	public void setIterators(ArrayList<BlockListIterator> iterators) {
		this.iterators = iterators;
	}


}
