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
			bos.write( Arrays.copyOf(salt.getBytes(),  Configuration.AES_IV_BYTES ) ); 
			byte[] filler = new byte[SIZE - bos.size()];       
			Configuration.getSecureRandom().nextBytes(filler);
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
			this.salt = Jargon2.toByteArray( new byte[Configuration.AES_IV_BYTES] );
			bis.read( this.salt.getBytes() );
		} catch (IOException e) {
			plainText = null;
		}
	}
	
	public void write( RandomAccessFile file ) throws IOException {
		file.write( plainText );
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

	public byte[] getIV() {
		return Arrays.copyOfRange(this.salt.getBytes(), 0, Configuration.AES_IV_BYTES);
	}
	
	public String toString() {
		String kdpStr = this.keyDerivationParameters.toString().replaceAll(System.lineSeparator(), "; ");
		
		return String.format("%s %s", kdpStr,
				BaseEncoding.base16().lowerCase().encode(getSalt().getBytes()) );
				
	}
	
	

}