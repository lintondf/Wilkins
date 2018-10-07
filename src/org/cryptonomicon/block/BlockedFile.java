package org.cryptonomicon.block;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.SecretKey;

public interface BlockedFile {

	/**
	 * Enumeration of file states.
	 */
	public enum State {
		 /** No Content. */
		 IDLE, 
		 /** Content as input. */
		 RAW, 
		 /** Content is deflated. */
		 ZIPPED, 
		 /** Content is deflated and encrypted. */
		 ENCRYPTED
	};

	public abstract BlockList getBlockList();

	/**
	 *  pad final data block and add random blocks to file.
	 *
	 * @param count - number of random blocks to add
	 */
	public abstract void pad(int count);

	/**
	 * Generates a chain of streams based the supplied input stream to encrypt and deflate the content
	 *
	 * @param is the base InputStream
	 * @param iv the AES initial value
	 * @return the resulting input stream
	 */
	public abstract InputStream getInputStream(InputStream is, byte[] iv);

	/**
	 * Generates a chain of output streams ending with the suppled stream to decrypt and inflate the content.
	 *
	 * @param os the destination OutputStream
	 * @param iv the AES initial value
	 * @return the resulting output stream
	 */
	public abstract OutputStream getOutputStream(OutputStream os, byte[] iv);

	/**
	 * Deflate the BlockedFile content
	 *
	 * @param blockLimit the maximum number of blocks to generate; -1 for no limit
	 * @return the number of blocks of content after deflation
	 */
	public abstract int deflate(int blockLimit);

	/**
	 * Inflate the content, outputting to the specified file
	 *
	 * @param output the output file
	 */
	public abstract void inflate(File output);

	/**
	 * Encrypt.
	 *
	 * @param iv the iv
	 * @return the int
	 */
	public abstract int encrypt(byte[] iv);

	/**
	 * Decrypt.
	 *
	 * @param iv the iv
	 */
	public abstract void decrypt(byte[] iv);

	/**
	 * @return the file
	 */
	public abstract File getFile();

	/**
	 * @return the secretKey
	 */
	public abstract SecretKey getSecretKey();

	/**
	 * @return the length
	 */
	public abstract long getLength();

	/**
	 * @return the state
	 */
	public abstract State getState();

}