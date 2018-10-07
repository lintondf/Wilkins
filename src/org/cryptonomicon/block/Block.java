package org.cryptonomicon.block;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

// TODO: Auto-generated Javadoc
/**
 * The Interface Block.
 */
public interface Block {

	/** The Constant BLOCK_SIZE. */
	public static final int BLOCK_SIZE = 1024;


	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public abstract int getCount();
	
	/**
	 * Gets the contents.
	 *
	 * @return the contents
	 */
	public abstract byte[] getContents();

	/**
	 * Pad.
	 */
	public abstract void pad();

	/**
	 * Xor.
	 *
	 * @param that the that
	 * @return the block
	 */
	public abstract Block xor(Block that);

	/**
	 * Write.
	 *
	 * @param os the os
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void write(OutputStream os) throws IOException;

	/**
	 * Write.
	 *
	 * @param os the os
	 * @param n the n
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void write(OutputStream os, int n) throws IOException;

	/**
	 * Write.
	 *
	 * @param writer the writer
	 * @param blockSize the block size
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void write(RandomAccessFile writer, int blockSize)
			throws IOException;

	/**
	 * To string.
	 *
	 * @return the string
	 */
	public abstract String toString();
}