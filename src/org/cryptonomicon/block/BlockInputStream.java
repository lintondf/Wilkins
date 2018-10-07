package org.cryptonomicon.block;

import java.io.IOException;
import java.io.InputStream;

public class BlockInputStream extends InputStream {
	
	protected BlockList blocks;
	protected BlockListIterator it;
	protected Block block;
	protected int i = Block.BLOCK_SIZE;
	
	public BlockInputStream( BlockList blocks ) {
		this.blocks = blocks;
		this.it = blocks.getIterator();
	}
	
	
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