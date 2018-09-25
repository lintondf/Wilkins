package org.cryptonomicon;

import java.util.ArrayList;
import java.util.List;

public class BlockList {
	
	protected ArrayList<Block> list;
	
	public BlockList() {
		list = new ArrayList<>();
	}
	
	
	public synchronized void add( Block block ) {
		list.add(block);
	}
	
	public int length() {
		int n = 0;
		for (Block block : list) {
			n += block.count;
		}
		return n;
	}

	public int size() {
		return list.size();
	}

	public BlockListIterator getIterator() {
		return new BlockListIterator(list.iterator());
	}
	
	public Block getFirst() {
		return list.get(0);
	}
	
	public Block getLast() {
		return list.get(list.size()-1);
	}
	
	public List<Block> getList() {
		return list;
	}


	public static void pad( BlockList those, int n ) {
		List<Block> blocks = those.getList();
		int m = blocks.size();
		for (int i = m; i < n; i++) {
			Block block = new Block();
			Wilkins.secureRandom.nextBytes(block.contents);
			block.count = block.contents.length;
			blocks.add( block );
		}
	}


	public static BlockList xor( List<BlockList> those) {
		BlockList output = new BlockList();
		int nBlocks = those.get(0).size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			Block block = new Block(those.get(0).getList().get(iBlock));
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
			Block block = new Block( one.getList().get(iBlock) );
			output.add(block.xor( two.getList().get(iBlock)));
		}
		return output;
	}
}