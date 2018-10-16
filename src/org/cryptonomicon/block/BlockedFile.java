package org.cryptonomicon.block;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

// TODO: Auto-generated Javadoc
/**
 * The Abstract Class BlockedFile.
 */
public abstract class BlockedFile {

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

	/** The content state. */
	protected State state;
	
	/** The secret key. */
	protected SecretKey secretKey; 
	
	/** The cipher. */
	protected static Cipher cipher = null;
	
	/**
	 * Default constructor for a new blocked file.  Handles static initialization.
	 * Use super() required in all public constructors.
	 */
	protected BlockedFile() {
		state = State.IDLE;
		if (cipher == null)	try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (Exception x) {
			x.printStackTrace();
		}
	}


	/**
	 * Gets the block list.
	 *
	 * @return the block list
	 */
	public abstract BlockList getBlockList();

	/**
	 *  pad final data block and add random blocks to file.
	 *
	 * @param count - number of random blocks to add
	 */
	public abstract void pad(int count);

	public InputStream getInputStream(InputStream is, byte[] iv) {
		try {
			DeflaterInputStream dis = new DeflaterInputStream( is );
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
			CipherInputStream cis = new CipherInputStream( dis, cipher );
			state = State.ENCRYPTED;
			return cis;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
		
	}
	
	public OutputStream getOutputStream(OutputStream os, byte[] iv) {
		if (state == State.RAW)
			return os;
		OutputStream ios = new InflaterOutputStream( os );
		if (state == State.ZIPPED)
			return ios;
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
			CipherOutputStream cos = new CipherOutputStream( ios, cipher );
			return cos;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	/**
	 * Deflate the BlockedFile content.
	 *
	 * @param blockLimit the maximum number of blocks to generate; -1 for no limit
	 * @return the number of blocks of content after deflation
	 */
	public abstract int deflate(int blockLimit);

	/**
	 * Inflate the content, outputting to the specified file.
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
	 * Gets the file.
	 *
	 * @return the file
	 */
	public abstract File getFile();

	/**
	 * Gets the secret key.
	 *
	 * @return the secretKey
	 */
	public abstract SecretKey getSecretKey();

	/**
	 * Gets the length after compression and encryption
	 *
	 * @return the length
	 */
	public abstract long getCompressedLength();

	/**
	 * Gets the length as input.
	 *
	 * @return the length
	 */
	public abstract long getOriginalLength();

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public abstract State getState();

}