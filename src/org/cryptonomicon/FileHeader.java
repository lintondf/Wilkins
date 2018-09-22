package org.cryptonomicon;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import com.google.common.io.BaseEncoding;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * FILE FORMAT
 * 64*8/File Header
 *  8/argon type,
 *  8/argon version,
 *  8/memory cost/1024,
 *  8/time cost,
 *  8/key size (bits)
 *  27*8/random
 *  32*8/IV
 * [n+m]*L0 contents bytes in 1024 byte blocks ordered randomly per permutation
 */

class FileHeader {
	
	public static final int SIZE = 64;
	
	public byte[] header = new byte[SIZE];
	
	public FileHeader( Type type, Version version, int memoryCost, int timeCost, int keySize, byte[] salt) {
		Wilkins.secureRandom.nextBytes(header);
		header[0] = (byte) type.ordinal();
		header[1] = (byte) version.ordinal();
		header[2] = (byte) (memoryCost/1024);
		header[3] = (byte) timeCost;
		header[4] = (byte) (keySize / 8);
		for (int i = 0; i < salt.length; i++) {
			header[32+i] = salt[i];
		}
	}
	
	public FileHeader(RandomAccessFile file) {
		try {
			file.read(header);
		} catch (IOException e) {
			header = null;
		}
	}
	
	private final int parallelism = 4;
	
	public Hasher getHasher() {
		return 	com.kosprov.jargon2.api.Jargon2.jargon2Hasher().type(getType()).version(getVersion())
				.memoryCost(getMemoryCost()).timeCost(getTimeCost()).parallelism(parallelism)
				.hashLength(getKeySize()/8)
				.salt(getSalt());
	}
	
	public boolean isValid() {
		return header != null;
	}

	public Type getType() {
		return Type.values()[header[0]];
	}
	
	public Version getVersion() {
		return Version.values()[header[1]];
	}
	
	public int getMemoryCost() {
		return 1024 * header[2];
	}
	
	public int getTimeCost() {
		return header[3];
	}
	
	public int getKeySize() {
		return 8*header[4];
	}
	
	public byte[] getSalt() {
		int hashLength = getKeySize()/8;
		byte[] salt = new byte[hashLength];
		for (int i = 0; i < hashLength; i++) {
			salt[i] = header[32+i];
		}
		return salt;
	}
	
	public byte[] getIV(int offset) {
		byte[] salt = getSalt();
		byte[] output = new byte[Wilkins.AES_IV_BYTES];
		for (int i = 0; i < Wilkins.AES_IV_BYTES; i++) {
			output[i] = salt[ (i + offset) % salt.length];
		}
		return Arrays.copyOfRange(header, 32, 32+Wilkins.AES_IV_BYTES);
	}
	
	public String toString() {
		return String.format("%s %s %d %d %d %s", getType().toString(), getVersion().toString(), getMemoryCost(), getTimeCost(), getKeySize(),
				BaseEncoding.base16().lowerCase().encode(getSalt()) );
				
	}
}