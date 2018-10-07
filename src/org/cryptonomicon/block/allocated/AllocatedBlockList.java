package org.cryptonomicon.block.allocated;

import java.util.ArrayList;
import java.util.List;

import org.cryptonomicon.Wilkins;
import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;

public class AllocatedBlockList implements BlockList {
	
	protected ArrayList<Block> list;
	
	public AllocatedBlockList() {
		list = new ArrayList<>();
	}
	
	
	protected synchronized void add( Block abstractBlock ) {
		list.add(abstractBlock);
	}
	
	protected int length() {
		int n = 0;
		for (Block  block : list) {
			n += block.getCount();
		}
		return n;
	}

	public int size() {
		return list.size();
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.IBlockList#getIterator()
	 */
	@Override
	public BlockListIterator getIterator() {
		return new BlockListIterator(list.iterator());
	}
	
	@Override
	public Block get(int which) {
		return list.get(which);
	}
	protected Block getFirst() {
		return list.get(0);
	}
	
	protected Block getLast() {
		return list.get(list.size()-1);
	}
	
	protected List<Block> getList() {
		return list;
	}


	protected static void pad( AllocatedBlockList those, int n ) {
		List<Block> blocks = those.getList();
		int m = blocks.size();
		for (int i = m; i < n; i++) {
			AllocatedBlock block = new AllocatedBlock();
			Wilkins.getSecureRandom().nextBytes(block.getContents());
			block.setCount(block.getContents().length);
			blocks.add( block );
		}
	}


//	public static AllocatedBlockList xor( List<AllocatedBlockList> those) {
//		AllocatedBlockList output = new AllocatedBlockList();
//		int nBlocks = those.get(0).size();
//		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
//			Block block = new AllocatedBlock(those.get(0).getList().get(iBlock));
//			for (int jList = 1; jList < those.size(); jList++) {
//				block = block.xor( those.get(jList).getList().get(iBlock));
//			}
//			output.add(block);
//		}
//		return output;
//	}


	public static AllocatedBlockList xor( AllocatedBlockList one, AllocatedBlockList two) {
		AllocatedBlockList output = new AllocatedBlockList();
		int nBlocks = one.size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			Block block = new AllocatedBlock( one.getList().get(iBlock) );
			output.add(block.xor( two.getList().get(iBlock)));
		}
		return output;
	}


	@Override
	public void xor(BlockList one, BlockList two) {
		int nBlocks = one.size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			Block block = new AllocatedBlock( one.get(iBlock) );
			this.add(block.xor( two.get(iBlock)));
		}
	}

	@Override
	public BlockList xor(List<BlockList> all) {
		int nBlocks = all.get(0).size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			Block block = new AllocatedBlock(all.get(0).get(iBlock));
			for (int jList = 1; jList < all.size(); jList++) {
				block = block.xor( all.get(jList).get(iBlock));
			}
			this.add(block);
		}
		return this;
	}


	@Override
	public BlockList make() {
		return new AllocatedBlockList();
	}

}