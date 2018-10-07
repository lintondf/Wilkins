package org.cryptonomicon.block;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.cryptonomicon.Wilkins;

import com.google.common.io.BaseEncoding;

public class Block {
	public static final int BLOCK_SIZE = 1024;

	private byte[] contents;
	private int count;
	
	protected Block() {
		contents = new byte[BLOCK_SIZE];
	}
	
	protected Block( int count ) {
		this.count = count;
		contents = new byte[BLOCK_SIZE];
		Wilkins.getSecureRandom().nextBytes(contents);
	}
	
	public static Block getTestBlock( int count ) {
		return new Block(count);
	}
	
	protected Block( Block that) {
		contents = Arrays.copyOf(that.contents, BLOCK_SIZE);
		count = that.count;
	}
	
	protected Block( byte[] test ) {
		contents = Arrays.copyOf( test, BLOCK_SIZE );
		count = test.length;
	}
	
	protected void pad() {
		if (count < BLOCK_SIZE) {
			byte[] padding = new byte[BLOCK_SIZE-count];
			Wilkins.getSecureRandom().nextBytes(padding);
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
	
	public void write( OutputStream os ) throws IOException {
		os.write( contents, 0, count );
	}
	
	public void write( OutputStream os, int n ) throws IOException {
		os.write( contents, 0, n );
	}
	
	public void write(RandomAccessFile writer, int blockSize) throws IOException {
		writer.write( contents, 0, blockSize );
		
	}
	
	public String toString() {
		return String.format("%4d: %s", count, BaseEncoding.base16().lowerCase().encode(Arrays.copyOf(contents, count)));
	}
	
	/**
	 * @return the count
	 */
	protected int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
    protected void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the contents
	 */
	protected byte[] getContents() {
		return contents;
	}

	/**
	 * @param contents the contents to set 
	 */
	protected void setContents(byte[] contents) {
		this.contents = contents;
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
			bis.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}