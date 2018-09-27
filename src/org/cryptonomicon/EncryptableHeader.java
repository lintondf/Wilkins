/**
 * 
 */
package org.cryptonomicon;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author lintondf
 *
 */
public abstract class EncryptableHeader {
	
	protected byte[] plainText;
	protected byte[] cipherText;

	public EncryptableHeader( int size ) {
		plainText = new byte[size];
		plainText = new byte[size];
	}
	
	public abstract boolean isValid();
	
	public boolean decode( Cipher cipher, SecretKey key, byte[] iv ) {
		IvParameterSpec parameterSpec = new IvParameterSpec(iv);
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
			plainText = cipher.doFinal(cipherText);
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
			cipherText = cipher.doFinal(plainText);
			return true;
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @return the plainText
	 */
	public byte[] getPlainText() {
		return plainText;
	}

	/**
	 * @param plainText the plainText to set
	 */
	public void setPlainText(byte[] plainText) {
		this.plainText = plainText;
	}

	/**
	 * @return the cipherText
	 */
	public byte[] getCipherText() {
		return cipherText;
	}

	/**
	 * @param cipherText the cipherText to set
	 */
	public void setCipherText(byte[] cipherText) {
		this.cipherText = cipherText;
	}
	
}
