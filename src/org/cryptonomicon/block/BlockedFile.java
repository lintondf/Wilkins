/*
 * 
 */
package org.cryptonomicon.block;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;

// TODO: Auto-generated Javadoc
/**
 * The Class BlockedFile is used to store payload and filler file contents
 * organized as a BlockList.  Cryptographic keys for the content are stored
 * and used.
 */
public class BlockedFile {
	
	/** The source file. Null if random content. */
	public File file;
	
	/** The secret key. */
	public SecretKey secretKey; 
	
	/** The length. */
	public long length;

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
	public State state;
	
	/** The list of blocks. */
	public BlockList blocks;
	
	/** The cipher. */
	protected static Cipher cipher = null;
	
	/**
	 * Default constructor for a new blocked file.  Handles static initialization.
	 * Use this() required in all public constructors.
	 */
	private BlockedFile() {
		if (cipher == null)	try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (Exception x) {
			x.printStackTrace();
		}
	}


	/**
	 * Instantiates a new blocked file from file content.
	 *
	 * @param f the file to load
	 * @param key the cryptographic key
	 */
	public BlockedFile(File f, ByteArray key) {
		this();
		file = f;
		secretKey = new SecretKeySpec(key.getBytes(), "AES");
		length = f.length();
		blocks = null;
		state = State.IDLE;
	}

	/**
	 * Instantiates a new blocked file with random content.
	 *
	 * @param key the cryptographic key
	 * @param nBlocks the n blocks
	 */
	public BlockedFile(ByteArray key, int nBlocks) {
		this();
		file = null;
		secretKey = new SecretKeySpec(key.getBytes(), "AES");
		length = nBlocks * Block.BLOCK_SIZE;
		blocks = new BlockList();
		BlockList.pad(blocks, nBlocks);
		state = State.RAW;
	}
	
	/**
	 * Instantiates a one block test file with specified contents
	 *
	 * @param contents the contents
	 * @param key the cryptographic key
	 */
	public BlockedFile( byte[] contents, ByteArray key ) {
		this();
		file = null;
		secretKey = new SecretKeySpec(key.getBytes(), "AES");
		length = contents.length;
		blocks = new BlockList();
		Block block = new Block(contents);
		blocks.add(block);
		state = State.RAW;
	}

	/**
	 *  pad final data block and add random blocks to file.
	 *
	 * @param count - number of random blocks to add
	 */
	public void pad(int count) {
		blocks.getList().get(blocks.getList().size() - 1).pad();
		BlockList.pad(blocks, count);
		length = Block.BLOCK_SIZE * count;
	}
	
	/**
	 * Generates a chain of streams based the supplied input stream to encrypt and deflate the content
	 *
	 * @param is the base InputStream
	 * @param iv the AES initial value
	 * @return the resulting input stream
	 */
	public InputStream getInputStream(InputStream is, byte[] iv) {
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
			CipherInputStream cis = new CipherInputStream( is, cipher );
			DeflaterInputStream dis = new DeflaterInputStream( cis );
			return dis;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Generates a chain of output streams ending with the suppled stream to decrypt and inflate the content.
	 *
	 * @param os the destination OutputStream
	 * @param iv the AES initial value
	 * @return the resulting output stream
	 */
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
	 * Deflate the BlockedFile content
	 *
	 * @param blockLimit the maximum number of blocks to generate; -1 for no limit
	 * @return the number of blocks of content after deflation
	 */
	public int deflate(int blockLimit) { // -1 for unlimited
		// Compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		DeflaterInputStream dis;
		try {
			if (file != null) {
				dis = new DeflaterInputStream(new FileInputStream(file), compressor);
			} else {
				dis = new DeflaterInputStream(new BlockInputStream(blocks), compressor);
			}
			BlockList blocks = new BlockList();
			BufferedInputStream bis = new BufferedInputStream(dis);

			// Compress the data
			while (true) {
				Block block = new Block();
				int length = bis.read(block.getContents());
				if (length < 0)
					break;
				if (length > 0) {
					block.setCount(length);
					blocks.add(block);
				}
				if (blockLimit > 0 && blocks.size() >= blockLimit) {
					blocks.getList().get(blockLimit - 1).pad();
					break;
				}
			}
			dis.close();
			bis.close();
			state = State.ZIPPED;
			this.blocks = blocks;
			length = blocks.length();
			return blocks.size();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Inflate the content, outputting to the specified file
	 *
	 * @param output the output file
	 */
	public void inflate(File output) {
		try {
			FileOutputStream fos = new FileOutputStream(output);
			Inflater inflater = new Inflater();
			BlockListIterator it = blocks.getIterator();
			if (it.hasNext()) {
				Block block = it.next();
				inflater.setInput(block.getContents(), 0, block.getCount());
				byte[] result = new byte[1024*1024];
				int resultLength = inflater.inflate(result, 0, result.length);
				fos.write(result, 0, resultLength);
				while (it.hasNext()) {
					block = it.next();
					inflater.setInput(block.getContents(), 0, block.getCount());
					resultLength = inflater.inflate(result, 0, result.length);
					fos.write(result, 0, resultLength);
				}
				inflater.end();
				fos.close();
			}
			state = State.RAW;
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	/**
	 * Encrypt/Decrypt subroutine
	 *
	 * @param mode ENCRYPT_MODE or DECRYPT_MODE
	 * @param iv the AES initial value
	 * @return the number of blocks of content after encryption/decryption
	 */
	protected int crypt( int mode, byte[] iv ) {
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			cipher.init(mode, secretKey, parameterSpec);
			BlockInputStream bis = new BlockInputStream(blocks);
			CipherInputStream cis = new CipherInputStream( bis, cipher );
			BlockList output = new BlockList();
			Block block = new Block();
			while (true) {
				int length = cis.read(block.getContents(), block.getCount(), Block.BLOCK_SIZE - block.getCount() );
				if (length < 0)
					break;
				block.setCount(block.getCount() + length);
				if (block.getCount() >= Block.BLOCK_SIZE) {
					output.add(block);
					block = new Block();
				}
			}
			if (block.getCount() > 0) {
				output.add(block);				
			}
			blocks = output;
			cis.close();
			return blocks.size();
			
		} catch (Exception x) {
			x.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Encrypt.
	 *
	 * @param iv the iv
	 * @return the int
	 */
	public int encrypt( byte[] iv ) {
		crypt( Cipher.ENCRYPT_MODE, iv );
		length = blocks.length();
		state = State.ENCRYPTED;
		return blocks.size();
	}

	/**
	 * Decrypt.
	 *
	 * @param iv the iv
	 */
	public void decrypt( byte[] iv ) {
		crypt( Cipher.DECRYPT_MODE, iv );
		state = State.ZIPPED;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String path = "Filler";
		if (file != null)
			path = file.getAbsolutePath();
		return String.format("%s %s %d %d", path, secretKey.toString(), length, (blocks!=null) ? blocks.size() : 0);
	}
	
	
//	public static void main( String[] args) {
//		File file = new File("data0.txt"); //"/Users/lintondf/Downloads/torrent-file-editor-0.3.12.dmg");
//		//BlockedFile blockedFile = new BlockedFile( new byte[16 ], 9915);
//		BlockedFile blockedFile = new BlockedFile(file, Jargon2.toByteArray(new byte[16]).finalizable());
//		blockedFile.deflate(-1);
//		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
//		int blocks = blockedFile.encrypt( iv );
//		System.out.println(blocks);
//		File out = new File("test.dgz");
//		try {
//			FileOutputStream fos = new FileOutputStream( out );
//			for (Block block : blockedFile.blocks.getList()) {
//				fos.write( block.contents, 0, block.count);
//			}
//			fos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		InflaterOutputStream ios = new InflaterOutputStream(bos);
//		try {
//			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
//			cipher.init(Cipher.DECRYPT_MODE, blockedFile.secretKey, parameterSpec);
//			CipherOutputStream cos = new CipherOutputStream( ios, cipher );
//			for (Block block : blockedFile.blocks.getList()) {
//				cos.write( block.contents, 0, block.count);
//			}
//			String output = new String( bos.toByteArray() );
//			System.out.println(output);
//			cos.close();
//		} catch (Exception x) {
//			x.printStackTrace();
//		}
//
//		blockedFile.decrypt( iv );
//		blockedFile.inflate( new File("test.out"));
//	}
}