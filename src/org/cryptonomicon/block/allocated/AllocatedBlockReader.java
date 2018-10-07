package org.cryptonomicon.block.allocated;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockReader;

public class AllocatedBlockReader implements BlockReader {
	RandomAccessFile    raf;
	long                remaining;
	AllocatedBlock               current;
	
	public AllocatedBlockReader( RandomAccessFile file, long length ) {
		this.raf = file;
		this.remaining = length;
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.IBlockReader#read()
	 */
	@Override
	public AllocatedBlock read() throws IOException {
		current = new AllocatedBlock();
		long n = (remaining > Block.BLOCK_SIZE) ? Block.BLOCK_SIZE : remaining;
		current.setCount(raf.read( current.getContents(), 0, (int) n ));
		if (current.getCount() < 0)
			current = null;
		remaining -= n;
		return current;
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.IBlockReader#readFull()
	 */
	@Override
	public AllocatedBlock readFull() throws IOException {
		current = new AllocatedBlock();
		current.setCount(raf.read( current.getContents(), 0, Block.BLOCK_SIZE ));
		if (current.getCount() < 0)
			current = null;
		remaining -= Block.BLOCK_SIZE;
		return current;
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.IBlockReader#getLast()
	 */
	@Override
	public AllocatedBlock getLast() {
		return current;
	}
}