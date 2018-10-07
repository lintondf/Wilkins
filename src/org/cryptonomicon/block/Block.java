package org.cryptonomicon.block;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public interface Block {

	public static final int BLOCK_SIZE = 1024;


	public abstract int getCount();
	
	public abstract byte[] getContents();

	public abstract void pad();

	public abstract Block xor(Block that);

	public abstract void write(OutputStream os) throws IOException;

	public abstract void write(OutputStream os, int n) throws IOException;

	public abstract void write(RandomAccessFile writer, int blockSize)
			throws IOException;

	public abstract String toString();
}