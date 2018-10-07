package org.cryptonomicon.block;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.cryptonomicon.Wilkins;

import com.google.common.io.BaseEncoding;

public class Block implements AbstractBlock {
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
	
	public static AbstractBlock getTestBlock( int count ) {
		return new Block(count);
	}
	
	protected Block( AbstractBlock that) {
		contents = Arrays.copyOf(that.getContents(), BLOCK_SIZE);
		count = that.getCount();
	}
	
	protected Block( byte[] test ) {
		contents = Arrays.copyOf( test, BLOCK_SIZE );
		count = test.length;
	}
	
	public void pad() {
		if (count < BLOCK_SIZE) {
			byte[] padding = new byte[BLOCK_SIZE-count];
			Wilkins.getSecureRandom().nextBytes(padding);
			for (int i = 0; i < padding.length; i++) {
				contents[count+i] = padding[i];
			}
			//count = BLOCK_SIZE;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.AbstractBlock#xor(org.cryptonomicon.block.Block)
	 */
	@Override
	public AbstractBlock xor( AbstractBlock that) {
		Block output = new Block(that);
		for (int i = 0; i < output.count; i++) {
			output.contents[i] ^= contents[i];
		}
		return output;
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.AbstractBlock#write(java.io.OutputStream)
	 */
	@Override
	public void write( OutputStream os ) throws IOException {
		os.write( contents, 0, count );
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.AbstractBlock#write(java.io.OutputStream, int)
	 */
	@Override
	public void write( OutputStream os, int n ) throws IOException {
		os.write( contents, 0, n );
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.AbstractBlock#write(java.io.RandomAccessFile, int)
	 */
	@Override
	public void write(RandomAccessFile writer, int blockSize) throws IOException {
		writer.write( contents, 0, blockSize );
		
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.AbstractBlock#toString()
	 */
	@Override
	public String toString() {
		return String.format("%4d: %s", count, BaseEncoding.base16().lowerCase().encode(Arrays.copyOf(contents, count)));
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
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
	public byte[] getContents() {
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
		block.count = AbstractBlock.BLOCK_SIZE;
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