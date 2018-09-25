package org.cryptonomicon;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

import com.google.common.io.BaseEncoding;

class BlockedFile {
	public File file;
	public SecretKey secretKey; 
	public long length;

	public enum State {
		IDLE, RAW, ZIPPED, ENCRYPTED
	};

	public State state;
	public BlockList blocks;
	
	protected static Cipher cipher = null;
	
	static {
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (Exception x) {
			x.printStackTrace();
		}
	}


	public BlockedFile(File f, byte[] key) {
		file = f;
		secretKey = new SecretKeySpec(key, "AES");
		length = f.length();
		blocks = null;
		state = State.IDLE;
	}

	public BlockedFile(byte[] key, int nBlocks) {
		file = null;
		secretKey = new SecretKeySpec(key, "AES");
		length = nBlocks * Block.BLOCK_SIZE;
		blocks = new BlockList();
		Block.pad(blocks, nBlocks);
		state = State.RAW;
	}
	
	/*
	 * Create a one block test file with specified contents
	 */
	protected BlockedFile( byte[] contents, byte[] key ) {
		file = null;
		secretKey = new SecretKeySpec(key, "AES");
		length = contents.length;
		blocks = new BlockList();
		Block block = new Block(contents);
		blocks.add(block);
		state = State.RAW;
	}

	public void pad(int count) {
		blocks.getList().get(blocks.getList().size() - 1).pad();
		Block.pad(blocks, count);
	}
	
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
				int length = bis.read(block.contents);
				if (length < 0)
					break;
				if (length > 0) {
					block.count = length;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public void inflate(File output) {
		try {
			FileOutputStream fos = new FileOutputStream(output);
			Inflater inflater = new Inflater();
			BlockListIterator it = blocks.getIterator();
			if (it.hasNext()) {
				Block block = it.next();
				inflater.setInput(block.contents, 0, block.count);
				byte[] result = new byte[1024*1024];
				int resultLength = inflater.inflate(result, 0, result.length);
				fos.write(result, 0, resultLength);
				while (it.hasNext()) {
					block = it.next();
					inflater.setInput(block.contents, 0, block.count);
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
	
	protected int crypt( int mode, byte[] iv ) {
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			cipher.init(mode, secretKey, parameterSpec);
			BlockInputStream bis = new BlockInputStream(blocks);
			CipherInputStream cis = new CipherInputStream( bis, cipher );
			BlockList output = new BlockList();
			Block block = new Block();
			while (true) {
				int length = cis.read(block.contents, block.count, Block.BLOCK_SIZE - block.count );
				if (length < 0)
					break;
				block.count += length;
				if (block.count >= Block.BLOCK_SIZE) {
					output.add(block);
					block = new Block();
				}
			}
			if (block.count > 0) {
				output.add(block);				
			}
			blocks = output;
			return blocks.size();
			
		} catch (Exception x) {
			x.printStackTrace();
			return -1;
		}
	}
	
	public int encrypt( byte[] iv ) {
//		System.out.println( file );
//		System.out.println( Wilkins.toString(iv));
//		System.out.println( Wilkins.toString(secretKey.getEncoded()));

		crypt( Cipher.ENCRYPT_MODE, iv );
//		System.out.println( blocks.getList().get(0).toString() );
		length = blocks.length();
		state = State.ENCRYPTED;
		return blocks.size();
	}

	public void decrypt( byte[] iv ) {
		crypt( Cipher.DECRYPT_MODE, iv );
		state = State.ZIPPED;
	}

	public String toString() {
		String path = "Filler";
		if (file != null)
			path = file.getAbsolutePath();
		return String.format("%s %s %d %d", path, secretKey.toString(), length, (blocks!=null) ? blocks.size() : 0);
	}
	
	
	public static void main( String[] args) {
		File file = new File("data0.txt"); //"/Users/lintondf/Downloads/torrent-file-editor-0.3.12.dmg");
		//BlockedFile blockedFile = new BlockedFile( new byte[16 ], 9915);
		BlockedFile blockedFile = new BlockedFile(file, new byte[16 ]);
		blockedFile.deflate(-1);
		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
		int blocks = blockedFile.encrypt( iv );
		System.out.println(blocks);
		File out = new File("test.dgz");
		try {
			FileOutputStream fos = new FileOutputStream( out );
			for (Block block : blockedFile.blocks.getList()) {
				fos.write( block.contents, 0, block.count);
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		InflaterOutputStream ios = new InflaterOutputStream(bos);
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, blockedFile.secretKey, parameterSpec);
			CipherOutputStream cos = new CipherOutputStream( ios, cipher );
			for (Block block : blockedFile.blocks.getList()) {
				cos.write( block.contents, 0, block.count);
			}
			String output = new String( bos.toByteArray() );
			System.out.println(output);
		} catch (Exception x) {
			x.printStackTrace();
		}

		blockedFile.decrypt( iv );
		blockedFile.inflate( new File("test.out"));
	}
}