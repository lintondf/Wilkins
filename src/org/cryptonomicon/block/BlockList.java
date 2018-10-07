package org.cryptonomicon.block;

import java.util.ArrayList;
import java.util.List;

import org.cryptonomicon.Wilkins;

public class BlockList {
	
	protected ArrayList<AbstractBlock> list;
	
	protected BlockList() {
		list = new ArrayList<>();
	}
	
	
	protected synchronized void add( AbstractBlock abstractBlock ) {
		list.add(abstractBlock);
	}
	
	protected int length() {
		int n = 0;
		for (AbstractBlock  block : list) {
			n += block.getCount();
		}
		return n;
	}

	protected int size() {
		return list.size();
	}

	public BlockListIterator getIterator() {
		return new BlockListIterator(list.iterator());
	}
	
	protected AbstractBlock getFirst() {
		return list.get(0);
	}
	
	protected AbstractBlock getLast() {
		return list.get(list.size()-1);
	}
	
	protected List<AbstractBlock> getList() {
		return list;
	}


	protected static void pad( BlockList those, int n ) {
		List<AbstractBlock> blocks = those.getList();
		int m = blocks.size();
		for (int i = m; i < n; i++) {
			Block block = new Block();
			Wilkins.getSecureRandom().nextBytes(block.getContents());
			block.setCount(block.getContents().length);
			blocks.add( block );
		}
	}


	public static BlockList xor( List<BlockList> those) {
		BlockList output = new BlockList();
		int nBlocks = those.get(0).size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			AbstractBlock block = new Block(those.get(0).getList().get(iBlock));
			for (int jList = 1; jList < those.size(); jList++) {
				block = block.xor( those.get(jList).getList().get(iBlock));
			}
			output.add(block);
		}
		return output;
	}


	public static BlockList xor( BlockList one, BlockList two) {
		BlockList output = new BlockList();
		int nBlocks = one.size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			AbstractBlock block = new Block( one.getList().get(iBlock) );
			output.add(block.xor( two.getList().get(iBlock)));
		}
		return output;
	}
}