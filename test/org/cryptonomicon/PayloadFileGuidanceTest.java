package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.cryptonomicon.configuration.Configuration;
import org.junit.Test;

import com.google.common.io.BaseEncoding;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

public class PayloadFileGuidanceTest {

	@Test
	public void testPayloadFileGuidanceIntIntIntLongInt() {
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		assertTrue( g.isValid() );
		assertTrue( g.getMaxBlocks() == 1 );
		assertTrue( g.getFileCount() == 2 );
		assertTrue( g.getFileOrdinal() == 3 );
		assertTrue( g.getSeed() == 4L );
		assertTrue( g.getLength() == 5 );
	}

	@Test
	public void testPayloadFileGuidanceRandomAccessFile() {
		File file = null;
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );
			raf.write( g.getPlainText(), 0, g.getPlainText().length);
			raf.seek(0L);
			
			PayloadFileGuidance g2 = new PayloadFileGuidance( raf );
			
			assertTrue( g2.isValid() );
			assertTrue( g2.getMaxBlocks() == 1 );
			assertTrue( g2.getFileCount() == 2 );
			assertTrue( g2.getFileOrdinal() == 3 );
			assertTrue( g2.getSeed() == 4L );
			assertTrue( g2.getLength() == 5 );
			
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail( e.getMessage() );
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}
	
	private static byte[] expectedCipherText = BaseEncoding.base16().lowerCase().decode("2c8595a5de21c3f9a8549f0251021120e3a7a27cd5f1ecc874afefccc5eebf2f");
	private static byte[] expectedPlainText = BaseEncoding.base16().lowerCase().decode("0000000102030000000000000004000000052a42c2e089f70d46fd4500444f2c");

	@Test
	public void testDecode() {
		Wilkins ipmec = new Wilkins();
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		g.setCipherText(expectedCipherText);
		byte[] key = new byte[ipmec.configuration.getKeyDerivationParameters().getKeySize()/8];
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		g.decode(ipmec.getCipher(), secretKey, iv);
		assertTrue( Arrays.equals( g.getPlainText(), expectedPlainText));
	}

	@Test
	public void testEncode() {
		Wilkins ipmec = new Wilkins();
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		g.setPlainText(expectedPlainText);
		byte[] key = new byte[ipmec.configuration.getKeyDerivationParameters().getKeySize()/8];
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		g.encode(ipmec.getCipher(), secretKey, iv);
		assertTrue( Arrays.equals( g.getCipherText(), expectedCipherText));
	}

	@Test
	public void testIsValid() {
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		assertTrue( g.isValid() );
		byte[] b = g.getPlainText();
		b[0] = (byte) (b[0] ^ 0x01);
		g.setPlainText(b);
		assertFalse( g.isValid() );
		g.setPlainText( new byte[10] );
		assertFalse( g.isValid() );
		g.setPlainText(null);
		assertFalse( g.isValid() );
	}

	@Test
	public void testToString() {
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		assertTrue( g.toString().equals("MB 1; FC 2; FO 3; Seed 4; Length 5; Valid true"));
	}

}
