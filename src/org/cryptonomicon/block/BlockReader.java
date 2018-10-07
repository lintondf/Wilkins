package org.cryptonomicon.block;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BlockReader {
	RandomAccessFile    raf;
	long                remaining;
	Block               current;
	
	public BlockReader( RandomAccessFile file, long length ) {
		this.raf = file;
		this.remaining = length;
	}
	
	public Block read() throws IOException {
		current = new Block();
		long n = (remaining > AbstractBlock.BLOCK_SIZE) ? AbstractBlock.BLOCK_SIZE : remaining;
		current.setCount(raf.read( current.getContents(), 0, (int) n ));
		if (current.getCount() < 0)
			current = null;
		remaining -= n;
		return current;
	}
	
	public Block readFull() throws IOException {
		current = new Block();
		current.setCount(raf.read( current.getContents(), 0, AbstractBlock.BLOCK_SIZE ));
		if (current.getCount() < 0)
			current = null;
		remaining -= AbstractBlock.BLOCK_SIZE;
		return current;
	}
	
	public Block getLast() {
		return current;
	}
}