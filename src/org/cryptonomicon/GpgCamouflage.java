/**
 * 
 */
package org.cryptonomicon;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * @author lintondf
 *
 */
public class GpgCamouflage {
	
	/**
		gpg: AES encrypted data
		gpg: encrypted with 1 passphrase
		# off=0 ctb=8c tag=3 hlen=2 plen=13
		:symkey enc packet: version 4, cipher 7, s2k 3, hash 2
			salt 783FE12337560647, count 11010048 (213)
		# off=15 ctb=d2 tag=18 hlen=6 plen=1201954 new-ctb
		:encrypted data packet:
			length: 1201954
			mdc_method: 2

		0000000    8c  0d  04  07  03  02  78  3f  e1  23  37  56  06  47  d5  d2
		0000020    ff  00  12  57  22  01 
		
	 */
	
	public static final int GPG_HEADER_SIZE = 22;
	
	protected static final byte[] gpgHeaderPart1 = BaseEncoding.base16().lowerCase().decode("8c0d04070302");
	private static final int gpgHeaderPart2_Size = 7;

	private static final int SIZE_START = gpgHeaderPart1.length + 8 + 1 + 1 + 1;
	
	public static int getGpgHeaderSize() {
		return gpgHeaderPart1.length + 8 + 1 + gpgHeaderPart2_Size;
	}
	
	protected byte[]  gpgHeader;
	
	public GpgCamouflage( RandomAccessFile file, int fileSize ) {
		byte[] size = Ints.toByteArray(fileSize);
		ByteBuffer byteBuffer = ByteBuffer.allocate( getGpgHeaderSize() );
		byteBuffer.put( gpgHeaderPart1 );
		long seed = BigInteger.probablePrime(8*8-1, new Random()).longValue(); // the mark of the beast
		byteBuffer.putLong(seed);
		byteBuffer.put( (byte) 0xd5 );
		byteBuffer.put( (byte) 0xd2 );
		byteBuffer.put( (byte) 0xff );
		byteBuffer.put(size);
		byteBuffer.put( (byte) 0x01 );
		gpgHeader = byteBuffer.array();
		try {
			file.seek(0L);
			file.write(gpgHeader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public GpgCamouflage( RandomAccessFile file ) {
		gpgHeader = new byte[getGpgHeaderSize()]; 
		try {
			file.seek(0L);
			file.read(gpgHeader, 0, gpgHeader.length);
		} catch (IOException e) {
			e.printStackTrace();
			gpgHeader = null;
		}
	}
	
	protected GpgCamouflage( byte[] test) {
		gpgHeader = test;
	}
	
	public boolean isValid() {
		if (gpgHeader == null)
			return false;
		if (!Arrays.equals(gpgHeaderPart1, Arrays.copyOf(gpgHeader, gpgHeaderPart1.length))) 
			return false;
		byte[] seedBytes = Arrays.copyOfRange(gpgHeader, gpgHeaderPart1.length, gpgHeaderPart1.length+8);
		BigInteger seed = new BigInteger(seedBytes);
		return (seed.isProbablePrime(10));
	}
	
	public byte[] getSeed() {
		return Arrays.copyOfRange(gpgHeader, gpgHeaderPart1.length, gpgHeaderPart1.length+8);
	}
	
	public int getSize() {
		// header, seed, d5, d2, ff
		int end = SIZE_START + 4;
		return Ints.fromByteArray( Arrays.copyOfRange(gpgHeader,  SIZE_START, end));
	}
	
	public void setSize( int size ) {
		int end = SIZE_START + 4;
		byte[] src = Ints.toByteArray(size);
		System.arraycopy(src, 0, gpgHeader, SIZE_START, src.length);
	}

	public boolean update( RandomAccessFile file ) {
		try {
			file.seek(0L);
			file.write(gpgHeader);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @return the gpgHeader
	 */
	public byte[] getGpgHeader() {
		return gpgHeader;
	}

	/**
	 * @param gpgHeader the gpgHeader to set
	 */
	public void setGpgHeader(byte[] gpgHeader) {
		this.gpgHeader = gpgHeader;
	}
	
}
