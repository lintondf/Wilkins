/**
 * 
 */
package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Longs;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 */
public class GpgCamouflageTest {

	final static byte[] goodHeader = BaseEncoding.base16().lowerCase().decode("8c0d0407030257ee6dcd22b46efdd5d2ff000003e801");
	
	/**
	 * Test method for {@link org.cryptonomicon.GpgCamouflage#GpgCamouflage(java.io.RandomAccessFile, int)}.
	 */
	@Test
	public void testGpgCamouflageRandomAccessFileInt() {
		File file = null;
		
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );

			GpgCamouflage gC = new GpgCamouflage( raf, 1000 );
			assertTrue( gC.isValid() );
			assertTrue( gC.getSize() == 1000 );
			
			raf.close();
			assertTrue( file.length() == GpgCamouflage.GPG_HEADER_SIZE );
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
	 * Test method for {@link org.cryptonomicon.GpgCamouflage#GpgCamouflage(java.io.RandomAccessFile)}.
	 */
	@Test
	public void testGpgCamouflageRandomAccessFile() {
		File file = null;
		
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );
			GpgCamouflage gC = new GpgCamouflage( raf, 1000 );
			assertTrue( gC.isValid() );
			assertTrue( gC.getSize() == 1000 );
			
			GpgCamouflage gC2 = new GpgCamouflage( raf );
			assertTrue( gC2.isValid() );
			assertTrue( gC2.getSize() == 1000 );
			
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
	 * Test method for {@link org.cryptonomicon.GpgCamouflage#isValid()}.
	 */
	@Test
	public void testIsValid() {
		GpgCamouflage gC = new GpgCamouflage( goodHeader );
		assertTrue( gC.isValid() );
		BigInteger seed = new BigInteger(gC.getSeed());
		assertTrue( seed.isProbablePrime(10));
		byte[] bad  = gC.getGpgHeader();
		bad[0] = (byte) (bad[0] ^ 0x01);
		gC.setGpgHeader( bad );
		assertFalse( gC.isValid() );
		byte[] nonPrime = Arrays.copyOf(goodHeader, goodHeader.length);
		byte[] src = Longs.toByteArray( 2L*5L*9L );
		System.arraycopy(src, 0, nonPrime, GpgCamouflage.gpgHeaderPart1.length, 8);
		gC = new GpgCamouflage( nonPrime );
		assertTrue( Longs.fromByteArray(gC.getSeed()) == 2L*5L*9L );
		assertFalse( gC.isValid() );
		gC.setGpgHeader(null);
		assertFalse( gC.isValid() );
	}

	/**
	 * Test method for {@link org.cryptonomicon.GpgCamouflage#update(java.io.RandomAccessFile)}.
	 */
	@Test
	public void testUpdate() {
		File file = null;
		
		try {
			file = File.createTempFile("testFileHeader", "bin");
			RandomAccessFile raf = new RandomAccessFile( file, "rw" );
			GpgCamouflage gC = new GpgCamouflage( raf, 0 );
			assertTrue( gC.isValid() );
			assertTrue( gC.getSize() == 0 );
			
			gC.setSize(1000);
			gC.update(raf);
			
			GpgCamouflage gC2 = new GpgCamouflage( raf );
			assertTrue( gC2.isValid() );
			assertTrue( gC2.getSize() == 1000 );
			
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

}
