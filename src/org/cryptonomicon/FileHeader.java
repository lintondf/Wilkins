package org.cryptonomicon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.cryptonomicon.configuration.KeyDerivationParameters;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
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

// TODO encrypt with SHA-256 digest of entire file x gpg and this header.
public class FileHeader extends EncryptableHeader {
	
	public static final int SIZE = 64;
	private static final int CRC_SIZE = 4;
	
	public FileHeader( Type type, Version version, int memoryCost, int timeCost, int keySize, byte[] salt) {
		super(SIZE);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write( (byte) type.ordinal());                       // 0
			bos.write( (byte) version.ordinal() );                   // 1
			bos.write( Ints.toByteArray( memoryCost ) );     // 2
			bos.write( Ints.toByteArray( timeCost ) );       // 6
			bos.write( Ints.toByteArray( keySize ) );       // 10
			bos.write( Arrays.copyOf(salt,  keySize/8 ) );           // 14 .. 14-1+keySize/8
			byte[] filler = new byte[SIZE - bos.size() - CRC_SIZE];       
			Wilkins.secureRandom.nextBytes(filler);
			bos.write( filler );
			plainText = bos.toByteArray();             
			
			Checksum checksum = new CRC32();
			checksum.update( plainText, 0, plainText.length );
			byte[] crc = Longs.toByteArray( checksum.getValue() );
			bos.write(crc, 4, 4 );                    
			plainText = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			plainText = null;
		}
		
	}
	
	public FileHeader( KeyDerivationParameters parameters, byte[] salt) {
		super(SIZE);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			parameters.write(bos);
			bos.write( Arrays.copyOf(salt,  parameters.getKeySize()/8 ) ); 
			System.out.println( bos.size() );
			byte[] filler = new byte[SIZE - bos.size() - CRC_SIZE];       
			Wilkins.secureRandom.nextBytes(filler);
			bos.write( filler );
			plainText = bos.toByteArray();             
			
			Checksum checksum = new CRC32();
			checksum.update( plainText, 0, plainText.length );
			byte[] crc = Longs.toByteArray( checksum.getValue() );
			bos.write(crc, 4, 4 );                    
			plainText = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			plainText = null;
		}
		
	}
	
	public FileHeader(RandomAccessFile file) {
		super(SIZE);
		try {
			file.read(plainText);
		} catch (IOException e) {
			plainText = null;
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
		if ( plainText == null || plainText.length != SIZE)
			return false;
		byte[] content = Arrays.copyOf(plainText, SIZE - CRC_SIZE);
		Checksum checksum = new CRC32();
		checksum.update( content, 0, content.length );
		byte[] crc = Longs.toByteArray( checksum.getValue() );
		crc = Arrays.copyOfRange(crc, 4, 8);
		return Arrays.equals(crc, Arrays.copyOfRange(plainText, SIZE - CRC_SIZE, SIZE));		
	}

	public Type getType() {
		return Type.values()[plainText[0]];
	}
	
	public Version getVersion() {
		return Version.values()[plainText[1]];
	}
	
	public int getMemoryCost() {
		return Ints.fromByteArray( Arrays.copyOfRange(plainText, 2, 6) );
	}
	
	public int getTimeCost() {
		return Ints.fromByteArray( Arrays.copyOfRange(plainText, 6, 10) );
	}
	
	public int getKeySize() {
		return Ints.fromByteArray( Arrays.copyOfRange(plainText, 10, 14) );
	}
	
	public int getIterations() {
		// TODO Auto-generated method stub
		return 100000;
	}

	public byte[] getSalt() {
		int hashLength = getKeySize()/8;
		return Arrays.copyOfRange(plainText,  14, 14+hashLength);
	}
	
	public byte[] getIV(int offset) {
		return Arrays.copyOfRange(plainText, 14, 14+Wilkins.AES_IV_BYTES);
	}
	
	public String toString() {
		return String.format("%s %s %d %d %d %s", getType().toString(), getVersion().toString(), getMemoryCost(), getTimeCost(), getKeySize(),
				BaseEncoding.base16().lowerCase().encode(getSalt()) );
				
	}

}