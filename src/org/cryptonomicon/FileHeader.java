package org.cryptonomicon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.KeyDerivationParameters;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

public class FileHeader extends EncryptableHeader {
	
	public static final int SIZE = 64;
	
	protected KeyDerivationParameters keyDerivationParameters;
	protected ByteArray salt;
	
	public FileHeader( KeyDerivationParameters parameters, ByteArray salt) {
		super(SIZE);
		this.keyDerivationParameters = parameters;
		this.salt = salt;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			parameters.write(bos);
			bos.write( Arrays.copyOf(salt.getBytes(),  parameters.getKeySize()/8 ) ); 
			byte[] filler = new byte[SIZE - bos.size()];       
			Wilkins.getSecureRandom().nextBytes(filler);
			bos.write( filler );
			plainText = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			plainText = null;
		}
		
	}
	
	public FileHeader(Configuration configuration, RandomAccessFile file) {
		super(SIZE);
		try {
			file.read(plainText);
			ByteArrayInputStream bis = new ByteArrayInputStream( this.getPlainText() );
			this.keyDerivationParameters = new KeyDerivationParameters( configuration, bis );
			this.salt = Jargon2.toByteArray( new byte[keyDerivationParameters.getKeySize()/8] );
			bis.read( this.salt.getBytes() );
		} catch (IOException e) {
			plainText = null;
		}
	}
	
	public KeyDerivationParameters getKeyDerivationParameters() {
		return this.keyDerivationParameters;
	}
	
	public ByteArray getSalt() {
		return this.salt;
	}
	
	
	public boolean isValid() {
		if ( plainText == null || plainText.length != SIZE)
			return false;
		return true;		
	}

	public byte[] getIV(int offset) {
		return Arrays.copyOfRange(this.salt.getBytes(), 0, Wilkins.AES_IV_BYTES);
	}
	
	public String toString() {
		String kdpStr = this.keyDerivationParameters.toString().replaceAll("\n", "; ");
		
		return String.format("%s %ss", kdpStr,
				BaseEncoding.base16().lowerCase().encode(getSalt().getBytes()) );
				
	}
	
//	File file = null;
//	byte[] salt = new byte[256/8];
//	// TODO from KDP
//	FileHeader fileHeader = new FileHeader( Type.ARGON2i, Version.V10, 1024, 5, 256, salt );
//	
//	try {
//		file = File.createTempFile("testFileHeader", "bin");
//		RandomAccessFile raf = new RandomAccessFile( file, "rw" );
//		raf.write( fileHeader.getPlainText(), 0, fileHeader.getPlainText().length);
//		raf.seek(0L);
//		
////		FileHeader h2 = new FileHeader( raf );
////		
////		assertTrue( h2.isValid() );
//////		assertTrue( h2.getType() == Type.ARGON2i);
//////		assertTrue( h2.getVersion() == Version.V10 );
//////		assertTrue( h2.getMemoryCost() == 1024 );
//////		assertTrue( h2.getTimeCost() == 5 );
//////		assertTrue( h2.getKeySize() == 256 );
////		byte[] check = h2.getSalt();
////		assertTrue( check != null && Arrays.equals(salt,  check) );
//		
//		raf.close();
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail( e.getMessage() );
//	} finally {
//		if (file != null) {
//			file.delete();
//		}
//	}
	

}