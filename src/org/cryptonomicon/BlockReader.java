package org.cryptonomicon;

import java.io.IOException;
import java.io.RandomAccessFile;

class BlockReader {
	RandomAccessFile bis;
	long                remaining;
	Block               current;
	
	public BlockReader( RandomAccessFile file, long length ) {
		this.bis = file;
		this.remaining = length;
	}
	
	public Block read() throws IOException {
		current = new Block();
		long n = (remaining > Block.BLOCK_SIZE) ? Block.BLOCK_SIZE : remaining;
		current.count = bis.read( current.contents, 0, (int) n );
		if (current.count < 0)
			current = null;
		remaining -= n;
		return current;
	}
	
	public Block getLast() {
		return current;
	}
}