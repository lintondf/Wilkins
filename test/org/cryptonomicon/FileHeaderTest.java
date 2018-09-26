/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.junit.Test;

import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;


/**
 * @author lintondf
 *
 */
public class FileHeaderTest {

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#FileHeader(
	 *    com.kosprov.jargon2.api.Jargon2.Type, 
	 *    com.kosprov.jargon2.api.Jargon2.Version, 
	 *    int, int, int, byte[])}.
	 */
	@Test
	public void testFileHeaderTypeVersionIntIntIntByteArray() {
		byte[] salt = new byte[256/8];
		
		FileHeader fileHeader = new FileHeader( Type.ARGON2i, Version.V10, 1024, 5, 256, salt );
		
		assertTrue( fileHeader.isValid() );
		assertTrue( fileHeader.getType() == Type.ARGON2i);
		assertTrue( fileHeader.getVersion() == Version.V10 );
		assertTrue( fileHeader.getMemoryCost() == 1024 );
		assertTrue( fileHeader.getTimeCost() == 5 );
		assertTrue( fileHeader.getKeySize() == 256 );
		byte[] check = fileHeader.getSalt();
		assertTrue( check != null && Arrays.equals(salt,  check) );
	}

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#FileHeader(java.io.RandomAccessFile)}.
	 */
	@Test
	public void testFileHeaderRandomAccessFile() {
		File file = null;
		byte[] salt = new byte[256/8];
		FileHeader fileHeader = new FileHeader( Type.ARGON2i, Version.V10, 1024, 5, 256, salt );
		
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );
			raf.write( fileHeader.getPlainText(), 0, fileHeader.getPlainText().length);
			raf.seek(0L);
			
			FileHeader h2 = new FileHeader( raf );
			
			assertTrue( h2.isValid() );
			assertTrue( h2.getType() == Type.ARGON2i);
			assertTrue( h2.getVersion() == Version.V10 );
			assertTrue( h2.getMemoryCost() == 1024 );
			assertTrue( h2.getTimeCost() == 5 );
			assertTrue( h2.getKeySize() == 256 );
			byte[] check = h2.getSalt();
			assertTrue( check != null && Arrays.equals(salt,  check) );
			
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

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#getHasher()}.
	 */
	@Test
	public void testGetHasher() {
		byte[] salt = new byte[256/8];
		FileHeader fileHeader = new FileHeader( Type.ARGON2i, Version.V10, 1024, 5, 256, salt );
		Hasher hasher = fileHeader.getHasher();
		final String expectedHasher = "Hasher{backend=com.kosprov.jargon2.nativeri.backend.NativeRiJargon2Backend, options=0 item(s), type=ARGON2i, version=V10, timeCost=5, memoryCost=1024, lanes=4, threads=4, hashLength=32, saltLength=32}";
		assertTrue( hasher.toString().equals(expectedHasher));
		final String expectedEncoded = "$argon2i$m=1024,t=5,p=4$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA$iHM6BcZKjipk9LQQrWxGTjb7ok91FVMHt2SEOlVvwtk";
		assertTrue( hasher.password("hello".getBytes()).encodedHash().equals(expectedEncoded) );
	}

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#isValid()}.
	 */
	@Test
	public void testIsValid() {
		byte[] salt = new byte[256/8];
		FileHeader fileHeader = new FileHeader( Type.ARGON2i, Version.V10, 1024, 5, 256, salt );
		assertTrue( fileHeader.isValid() );
		fileHeader.getPlainText()[0] = (byte) (0x01 ^ fileHeader.getPlainText()[0]);
		assertFalse( fileHeader.isValid());
		fileHeader.setPlainText( new byte[10] );
		assertFalse( fileHeader.isValid());
		fileHeader.setPlainText( null );
		assertFalse( fileHeader.isValid());
	}

	/**
	 * Test method for {@link org.cryptonomicon.FileHeader#toString()}.
	 */
	@Test
	public void testToString() {
		byte[] salt = new byte[128/8];
		FileHeader fileHeader = new FileHeader( Type.ARGON2id, Version.V13, 32*1024, 7, 128, salt );
		//System.out.println( fileHeader.toString() );
		assertTrue( fileHeader.isValid());
		final String expected = "ARGON2id V13 32768 7 128 00000000000000000000000000000000";
		assertTrue( fileHeader.toString().equals(expected));
	}

}
