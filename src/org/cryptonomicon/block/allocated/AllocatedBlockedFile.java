/*
 * 
 */
package org.cryptonomicon.block.allocated;

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

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.cryptonomicon.block.BlockedFile;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;

// TODO: Auto-generated Javadoc
/**
 * The Class BlockedFile is used to store payload and filler file contents
 * organized as a BlockList.  Cryptographic keys for the content are stored
 * and used.
 */
public class AllocatedBlockedFile extends BlockedFile {
	
	/** The source file. Null if random content. */
	private File file;
	
	/** The length. */
	private long length;

	/** The list of blocks. */
	private AllocatedBlockList blocks;
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#getBlockList()
	 */
	@Override
	public BlockList getBlockList() {
		return blocks;
	}
	
	/**
	 * Instantiates a new blocked file from file content.
	 *
	 * @param f the file to load
	 * @param key the cryptographic key
	 */
	public AllocatedBlockedFile(File f, ByteArray key) {
		super();
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
	public AllocatedBlockedFile(ByteArray key, int nBlocks) {
		super();
		file = null;
		secretKey = new SecretKeySpec(key.getBytes(), "AES");
		length = nBlocks * Block.BLOCK_SIZE;
		blocks = new AllocatedBlockList();
		AllocatedBlockList.pad(blocks, nBlocks);
		state = State.RAW;
	}
	
	/**
	 * Instantiates a one block test file with specified contents
	 *
	 * @param contents the contents
	 * @param key the cryptographic key
	 */
	public AllocatedBlockedFile( byte[] contents, ByteArray key ) {
		super();
		file = null;
		secretKey = new SecretKeySpec(key.getBytes(), "AES");
		length = contents.length;
		blocks = new AllocatedBlockList();
		AllocatedBlock block = new AllocatedBlock(contents);
		blocks.add(block);
		state = State.RAW;
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#pad(int)
	 */
	@Override
	public void pad(int count) {
		blocks.getList().get(blocks.getList().size() - 1).pad();
		AllocatedBlockList.pad(blocks, count);
		length = Block.BLOCK_SIZE * count;
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#deflate(int)
	 */
	@Override
	public int deflate(int blockLimit) { // -1 for unlimited
		// Compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		DeflaterInputStream dis;
		try {
			if (file != null) {
				dis = new DeflaterInputStream(new FileInputStream(file), compressor);
			} else {
				dis = new DeflaterInputStream(new AllocatedBlockInputStream(blocks), compressor);
			}
			AllocatedBlockList blocks = new AllocatedBlockList();
			BufferedInputStream bis = new BufferedInputStream(dis);

			// Compress the data
			while (true) {
				AllocatedBlock block = new AllocatedBlock();
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

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#inflate(java.io.File)
	 */
	@Override
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
			AllocatedBlockInputStream bis = new AllocatedBlockInputStream(blocks);
			CipherInputStream cis = new CipherInputStream( bis, cipher );
			AllocatedBlockList output = new AllocatedBlockList();
			AllocatedBlock block = new AllocatedBlock();
			while (true) {
				int length = cis.read(block.getContents(), block.getCount(), Block.BLOCK_SIZE - block.getCount() );
				if (length < 0)
					break;
				block.setCount(block.getCount() + length);
				if (block.getCount() >= Block.BLOCK_SIZE) {
					output.add(block);
					block = new AllocatedBlock();
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
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#encrypt(byte[])
	 */
	@Override
	public int encrypt( byte[] iv ) {
		crypt( Cipher.ENCRYPT_MODE, iv );
		length = blocks.length();
		state = State.ENCRYPTED;
		return blocks.size();
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#decrypt(byte[])
	 */
	@Override
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


	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#getFile()
	 */
	@Override
	public File getFile() {
		return file;
	}


	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}


	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#getSecretKey()
	 */
	@Override
	public SecretKey getSecretKey() {
		return secretKey;
	}


	/**
	 * @param secretKey the secretKey to set
	 */
	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}


	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#getLength()
	 */
	@Override
	public long getLength() {
		return length;
	}


	/**
	 * @param length the length to set
	 */
	public void setLength(long length) {
		this.length = length;
	}


	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockedFile#getState()
	 */
	@Override
	public State getState() {
		return state;
	}


	/**
	 * @param state the state to set
	 */
	public void setState(State state) {
		this.state = state;
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