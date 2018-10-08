package org.cryptonomicon.block.allocated;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.cryptonomicon.Wilkins;
import org.cryptonomicon.block.Block;

import com.google.common.io.BaseEncoding;

public class AllocatedBlock implements Block {
	private byte[] contents;
	private int count;
	
	protected AllocatedBlock() {
		contents = new byte[BLOCK_SIZE];
		count = 0;
	}
	
	protected AllocatedBlock( int count ) {
		this.count = count;
		contents = new byte[BLOCK_SIZE];
		Wilkins.getSecureRandom().nextBytes(contents);
	}
	
	public static Block getTestBlock( int count ) {
		return new AllocatedBlock(count);
	}
	
	public AllocatedBlock( byte[] data ) {
		this();
		this.contents = Arrays.copyOf( data, BLOCK_SIZE );
		this.count = data.length;		
	}
	
	protected AllocatedBlock( Block that) {
		contents = Arrays.copyOf(that.getContents(), BLOCK_SIZE);
		count = that.getCount();
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
	public Block xor( Block that) {
		AllocatedBlock output = new AllocatedBlock(that);
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
		AllocatedBlockList blocks = new AllocatedBlockList();
		AllocatedBlock block = new AllocatedBlock();
		Arrays.fill(block.contents, (byte)100);
		block.count = Block.BLOCK_SIZE;
		blocks.add(block);
		block = new AllocatedBlock();
		for (int i = 0; i < 10; i++)
			block.contents[i] = (byte) i;
		block.count = 10;
		blocks.add(block);
		try {
			AllocatedBlockInputStream bis = new AllocatedBlockInputStream( blocks );
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