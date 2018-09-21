package org.cryptonomicon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.io.BaseEncoding;

class Block {
	public static final int BLOCK_SIZE = 1024;

	byte[] contents;
	public int count;
	
	public Block() {
		contents = new byte[BLOCK_SIZE];
	}
	
	public Block( Block that) {
		contents = Arrays.copyOf(that.contents, that.count);
		count = that.count;
	}
	
	public void pad() {
		if (count < BLOCK_SIZE) {
			byte[] padding = new byte[BLOCK_SIZE-count];
			Wilkins.secureRandom.nextBytes(padding);
			for (int i = 0; i < padding.length; i++) {
				contents[count+i] = padding[i];
			}
			count = BLOCK_SIZE;
		}
	}
	
	public Block xor( Block that) {
		Block output = new Block(that);
		for (int i = 0; i < output.count; i++) {
			output.contents[i] ^= contents[i];
		}
		return output;
	}
	
	public String toString() {
		return String.format("%4d: %s", count, BaseEncoding.base16().lowerCase().encode(Arrays.copyOf(contents, count)));
	}
	
	public static Block.BlockList xor( Block.BlockList one, Block.BlockList two) {
		Block.BlockList output = new Block.BlockList();
		int nBlocks = one.size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			Block block = new Block( one.getList().get(iBlock) );
			output.add(block.xor( two.getList().get(iBlock)));
		}
		return output;
	}
	
	public static Block.BlockList xor( List<Block.BlockList> those) {
		Block.BlockList output = new Block.BlockList();
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
	
	public static void pad( Block.BlockList those, int n ) {
		List<Block> blocks = those.getList();
		int m = blocks.size();
		for (int i = m; i < n; i++) {
			Block block = new Block();
			Wilkins.secureRandom.nextBytes(block.contents);
			block.count = block.contents.length;
			blocks.add( block );
		}
	}
	
	public static class BlockListIterator implements Iterator<Block> {
		protected Iterator<Block> it;
		protected Block current;
		
		public BlockListIterator( Iterator<Block> it ) {
			this.it = it;
		}
		
		@Override
		public synchronized boolean hasNext() {
			return it.hasNext();
		}


		@Override
		public synchronized Block next() {
			current = it.next();
			return current;
		}
		
		public Block current() {
			return current;
		}
		
		
	}
	public static class BlockList {
		
		protected ArrayList<Block> list;
		
		public BlockList() {
			list = new ArrayList<>();
		}
		
		
		public synchronized void add( Block block ) {
			list.add(block);
		}

		public int size() {
			return list.size();
		}

		public BlockListIterator getIterator() {
			return new BlockListIterator(list.iterator());
		}
		
		public List<Block> getList() {
			return list;
		}
	}
	
	public static class BlockInputStream extends InputStream {
		
		protected Block.BlockList blocks;
		protected BlockListIterator it;
		protected Block block;
		protected int i = BLOCK_SIZE;
		
		public BlockInputStream( Block.BlockList blocks ) {
			this.blocks = blocks;
			this.it = blocks.getIterator();
		}
		
		
		@Override
		public int read() throws IOException {
			if (i >= BLOCK_SIZE) {
				if (it.hasNext()) {
					block = it.next();
					i = 0;
				} else {
					return -1;
				}
			}
			if (i >= block.count)
				return -1;
			return 0xFF & ((int) block.contents[i++]);
		}


		@Override
		public int available() throws IOException {
			if (block != null)
				return (it.hasNext()) ? BLOCK_SIZE : block.count - i;
			else if (it.hasNext())
				return BLOCK_SIZE;
			else
				return 0;
		}
	}
	
	public static void main(String[] args) {
		Block.BlockList blocks = new Block.BlockList();
		Block block = new Block();
		Arrays.fill(block.contents, (byte)100);
		block.count = Block.BLOCK_SIZE;
		blocks.add(block);
		block = new Block();
		for (int i = 0; i < 10; i++)
			block.contents[i] = (byte) i;
		block.count = 10;
		blocks.add(block);
		try {
			BlockInputStream bis = new BlockInputStream( blocks );
			System.out.println( bis.available() );
			while (bis.available() > 0) {
				int ch = bis.read();
				System.out.println( ch + " " + bis.available() );
				if (ch == -1)
					break;
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}