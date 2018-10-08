/**
 * 
 */
package org.cryptonomicon.block;

import java.util.ArrayList;

import org.cryptonomicon.block.BlockedFile;

// TODO: Auto-generated Javadoc
/**
 * The Class XorSet.
 *
 * @author lintondf
 */
public class XorSet {
	
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
	public XorSet( int maxBlocks, ArrayList<BlockedFile> allFiles ) {
		this.maxBlocks = maxBlocks;
		ArrayList<BlockList> allLists = new ArrayList<>();
		for (BlockedFile file : allFiles) {
			allLists.add( file.getBlockList() );
		}
		xorOfAll = allFiles.get(0).getBlockList().make();
		xorOfAll.xor(allLists);
		xorExcept = new ArrayList<>();
		iterators = new ArrayList<>();
		for (int iList = 0; iList < allLists.size(); iList++) {
			BlockList blockList = xorOfAll.make();
			blockList.xor( xorOfAll, allLists.get(iList) );
			xorExcept.add( blockList );
			iterators.add( blockList.getIterator() );
		}
		iterators.add( xorOfAll.getIterator() ); // in file order
	}

	/**
	 * Gets the max blocks.
	 *
	 * @return the maxBlocks
	 */
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

	/**
	 * Gets the xor of all.
	 *
	 * @return the xorOfAll
	 */
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

	/**
	 * Gets the xor except.
	 *
	 * @return the xorExcept
	 */
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

	/**
	 * Gets the iterators.
	 *
	 * @return the iterators
	 */
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

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
