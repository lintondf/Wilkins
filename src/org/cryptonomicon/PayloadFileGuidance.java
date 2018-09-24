package org.cryptonomicon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * Encryption Approach Given data files D1, D2, ... Dn and asscoiated
 * passphrases P1, P2, ... Pn, filler files F1, F2, .. Fm. Convert
 * passphrases to keys K1, K2, ... Kn. XOR all keys and fold result to
 * 48-bit integer S0. Seed standard Java Random with S0. Determine lengths
 * of files and set L0 as max of set. Compute L1, L2, .. Ln as lengths of
 * required padding for each file. Output file size will be [L0 +
 * sizeof(L1)] * [n + m] Generate a random permutation of 1 .. [L0] O0
 * Random keys R1, R2, ... Rm. For each data file Di, concatenate [Li,
 * contents of Di, Li random bytes] and AES encrypt with Ki yielding Ci. For
 * each filler file Fi, concatenate [Li, contents of Fi, Li random bytes]
 * and AES encrypt with Ri yielding Ei. Concatenate all Ci and Ei. Permute
 * IAW O0. Write permutation to output file.
 * 
 */

/**
 * [n+m] * 256/encoded-guidance [encoded with corresponding Ki or Ri keys]
 *  32/maxBlocks (same for each key),
 *  8/[n+m] (same for each key),
 *  8/content modulus [0 .. n+m) (unique for each key),
 *  48/permutation seed (same for each key)
 *  64/file length,
 *  64/random fill (varies for each key) 
 *  32/CRC32 of preceeding
 */

class PayloadFileGuidance {
	
	public byte[] guidance = new byte[32];
	
	public PayloadFileGuidance(int maxBlocks, int nm, int modulus, long seed, int length ) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write( Ints.toByteArray(maxBlocks) ); // 0 .. 3  4 bytes 
			bos.write( (byte) nm );                   // 4       1 byte
			bos.write( (byte) modulus );              // 5       1 byte
			bos.write( Longs.toByteArray(seed) );     // 6 ..13  8 bytes
			bos.write( Ints.toByteArray(length) );    //14 ..17  4 bytes
			byte[] filler = new byte[10];             //18 ..27  10 bytes
			Wilkins.secureRandom.nextBytes(filler);
			bos.write( filler );
			guidance = bos.toByteArray();             // 28 bytes
			
			Checksum checksum = new CRC32();
			checksum.update( guidance, 0, guidance.length );
			byte[] crc = Longs.toByteArray( checksum.getValue() );
			bos.write(crc, 4, 4 );                    //28 ..31  4 bytes
			guidance = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			guidance = null;
		}
	}
	
	public PayloadFileGuidance(RandomAccessFile file) {
		try {
			file.read(guidance);
			//System.out.println(BaseEncoding.base16().lowerCase().encode(guidance));
		} catch (IOException e) {
			guidance = null;
		}
	}
	
	public boolean decode( Cipher cipher, SecretKey key, byte[] iv ) {
		IvParameterSpec parameterSpec = new IvParameterSpec(iv);
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
			byte[] cipherText = cipher.doFinal(guidance);
			guidance = cipherText;
			return true;
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean encode( Cipher cipher, SecretKey key, byte[] iv) {
		IvParameterSpec parameterSpec = new IvParameterSpec(iv);
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
			byte[] cipherText = cipher.doFinal(guidance);
			guidance = cipherText;
			return true;
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isValid() {
		if (guidance == null)
			return false;
		byte[] content = Arrays.copyOf(guidance, 28);
		Checksum checksum = new CRC32();
		checksum.update( content, 0, content.length );
		byte[] crc = Longs.toByteArray( checksum.getValue() );
		crc = Arrays.copyOfRange(crc, 4, 8);
		return Arrays.equals(crc, Arrays.copyOfRange(guidance, 28, 32));
	}
	
	
	public byte[] getCipherText() {
		return guidance;
	}
	
	public byte[] getPlainText() {
		return guidance;
	}
	
	public int getMaxBlocks() {
		return Ints.fromByteArray( Arrays.copyOfRange(guidance, 0, 4));
	}
	
	public int getFileCount() {
		return guidance[4];
	}
	
	public int getFileOrdinal() {
		return guidance[5];
	}
	
	public Long getSeed() {
		return Longs.fromByteArray(Arrays.copyOfRange(guidance, 6, 14));
	}
	
	public int getLength() {
		return Ints.fromByteArray(Arrays.copyOfRange(guidance, 14, 18));
	}
	
	public String toString() {
		return String.format("%d %d %d %d %d %b", getMaxBlocks(), getFileCount(), getFileOrdinal(), getSeed(), getLength(), isValid() );
	}
}