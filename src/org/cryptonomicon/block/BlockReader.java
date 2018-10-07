package org.cryptonomicon.block;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BlockReader {
	RandomAccessFile    raf;
	long                remaining;
	AllocatedBlock               current;
	
	public BlockReader( RandomAccessFile file, long length ) {
		this.raf = file;
		this.remaining = length;
	}
	
	public AllocatedBlock read() throws IOException {
		current = new AllocatedBlock();
		long n = (remaining > Block.BLOCK_SIZE) ? Block.BLOCK_SIZE : remaining;
		current.setCount(raf.read( current.getContents(), 0, (int) n ));
		if (current.getCount() < 0)
			current = null;
		remaining -= n;
		return current;
	}
	
	public AllocatedBlock readFull() throws IOException {
		current = new AllocatedBlock();
		current.setCount(raf.read( current.getContents(), 0, Block.BLOCK_SIZE ));
		if (current.getCount() < 0)
			current = null;
		remaining -= Block.BLOCK_SIZE;
		return current;
	}
	
	public AllocatedBlock getLast() {
		return current;
	}
}