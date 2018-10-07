package org.cryptonomicon.block;

import java.util.ArrayList;
import java.util.List;

public interface BlockList {

	public abstract BlockListIterator getIterator();

	public abstract void xor(BlockList blockList1, BlockList blockList2 );

	public abstract BlockList xor(List<BlockList> allLists);

	public abstract int size();
	
	public abstract Block get( int which );

	public abstract BlockList make();


}