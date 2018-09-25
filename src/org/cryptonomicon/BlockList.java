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
}