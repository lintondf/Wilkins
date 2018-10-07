package org.cryptonomicon.block.allocated;

import java.io.IOException;
import java.io.InputStream;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockInputStream;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;

public class AllocatedBlockInputStream extends InputStream implements BlockInputStream {
	
	protected BlockList blocks;
	protected BlockListIterator it;
	protected Block block;
	protected int i = Block.BLOCK_SIZE;
	
	public AllocatedBlockInputStream( BlockList blocks ) {
		this.blocks = blocks;
		this.it = blocks.getIterator();
	}
	
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.IBlockInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if (i >= Block.BLOCK_SIZE) {
			if (it.hasNext()) {
				block = it.next();
				i = 0;
			} else {
				return -1;
			}
		}
		if (i >= block.getCount())
			return -1;
		return 0xFF & ((int) block.getContents()[i++]);
	}


	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.IBlockInputStream#available()
	 */
	@Override
	public int available() throws IOException {
		if (block != null)
			return (it.hasNext()) ? Block.BLOCK_SIZE : block.getCount() - i;
		else if (it.hasNext())
			return Block.BLOCK_SIZE;
		else
			return 0;
	}
}