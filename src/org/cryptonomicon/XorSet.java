/**
 * 
 */
package org.cryptonomicon;

import java.util.ArrayList;

/**
 * @author lintondf
 *
 */
public class XorSet {
	
	int maxBlocks;
	BlockList xorOfAll;
	ArrayList<BlockList> xorExcept;
	ArrayList<BlockListIterator> iterators;
	
	public XorSet( int maxBlocks, ArrayList<BlockedFile> allFiles ) {
		ArrayList<BlockList> allLists = new ArrayList<>();
		for (BlockedFile file : allFiles) {
			allLists.add( file.blocks );
		}
		xorOfAll = BlockList.xor(allLists);
		xorExcept = new ArrayList<>();
		iterators = new ArrayList<>();
		for (int iList = 0; iList < allLists.size(); iList++) {
			BlockList blockList = BlockList.xor( xorOfAll, allLists.get(iList) );
			xorExcept.add( blockList );
			iterators.add( blockList.getIterator() );
		}
		iterators.add( xorOfAll.getIterator() ); // in file order
	}

	/**
	 * @return the maxBlocks
	 */
	public int getMaxBlocks() {
		return maxBlocks;
	}

	/**
	 * @param maxBlocks the maxBlocks to set
	 */
	public void setMaxBlocks(int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	/**
	 * @return the xorOfAll
	 */
	public BlockList getXorOfAll() {
		return xorOfAll;
	}

	/**
	 * @param xorOfAll the xorOfAll to set
	 */
	public void setXorOfAll(BlockList xorOfAll) {
		this.xorOfAll = xorOfAll;
	}

	/**
	 * @return the xorExcept
	 */
	public ArrayList<BlockList> getXorExcept() {
		return xorExcept;
	}

	/**
	 * @param xorExcept the xorExcept to set
	 */
	public void setXorExcept(ArrayList<BlockList> xorExcept) {
		this.xorExcept = xorExcept;
	}

	/**
	 * @return the iterators
	 */
	public ArrayList<BlockListIterator> getIterators() {
		return iterators;
	}

	/**
	 * @param iterators the iterators to set
	 */
	public void setIterators(ArrayList<BlockListIterator> iterators) {
		this.iterators = iterators;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
