package org.cryptonomicon;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
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
		contents = Arrays.copyOf(that.contents, BLOCK_SIZE);
		count = that.count;
	}
	
	protected Block( byte[] test ) {
		contents = Arrays.copyOf( test, BLOCK_SIZE );
		count = test.length;
	}
	
	public void pad() {
		if (count < BLOCK_SIZE) {
			byte[] padding = new byte[BLOCK_SIZE-count];
			Wilkins.secureRandom.nextBytes(padding);
			for (int i = 0; i < padding.length; i++) {
				contents[count+i] = padding[i];
			}
			//count = BLOCK_SIZE;
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
	
	public static BlockList xor( BlockList one, BlockList two) {
		BlockList output = new BlockList();
		int nBlocks = one.size();
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			Block block = new Block( one.getList().get(iBlock) );
			output.add(block.xor( two.getList().get(iBlock)));
		}
		return output;
	}
	
	public static BlockList xor( List<BlockList> those) {
		BlockList output = new BlockList();
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
	
	public static void pad( BlockList those, int n ) {
		List<Block> blocks = those.getList();
		int m = blocks.size();
		for (int i = m; i < n; i++) {
			Block block = new Block();
			Wilkins.secureRandom.nextBytes(block.contents);
			block.count = block.contents.length;
			blocks.add( block );
		}
	}
	
	public static void main(String[] args) {
		BlockList blocks = new BlockList();
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