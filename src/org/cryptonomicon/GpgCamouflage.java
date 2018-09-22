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
	
	public static final byte[] gpgHeader1 = BaseEncoding.base16().lowerCase()
			.decode("8c0d04070302");
	
	private static final int gpgHeader2_Size = 7;
	
	public static int getGpgHeaderSize() {
		return gpgHeader1.length + 8 + 1 + gpgHeader2_Size;
	}
	
	protected byte[]  gpgHeader;
	
	public GpgCamouflage( int fileSize ) {
		byte[] size = Ints.toByteArray(fileSize);
		ByteBuffer byteBuffer = ByteBuffer.allocate( getGpgHeaderSize() );
		byteBuffer.put( gpgHeader1 );
		long seed = BigInteger.probablePrime(8*8-1, new Random()).longValue(); // the mark of the beast
		byteBuffer.putLong(seed);
		byteBuffer.put( (byte) 0xd5 );
		byteBuffer.put( (byte) 0xd2 );
		byteBuffer.put( (byte) 0xff );
		byteBuffer.put(size);
		byteBuffer.put( (byte) 0x01 );
		gpgHeader = byteBuffer.array();
	}
	
	public GpgCamouflage( RandomAccessFile file ) {
		gpgHeader = new byte[getGpgHeaderSize()]; 
		try {
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
		if (!Arrays.equals(gpgHeader1, Arrays.copyOf(gpgHeader, gpgHeader1.length))) 
			return false;
		byte[] seedBytes = Arrays.copyOfRange(gpgHeader, gpgHeader1.length, gpgHeader1.length+8);
		BigInteger seed = new BigInteger(seedBytes);
		return (seed.isProbablePrime(10));
	}
	
	public byte[] getSeed() {
		return Arrays.copyOfRange(gpgHeader, gpgHeader1.length, gpgHeader1.length+8);
	}
	
	public int getSize() {
		int start = gpgHeader1.length + 8 + 1 + 1 + 1;  // header, seed, d5, d2, ff
		int end = start + 4;
		return Ints.fromByteArray( Arrays.copyOfRange(gpgHeader,  start, end));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GpgCamouflage gC = new GpgCamouflage( 1000 );
		System.out.println( gC.isValid() + " " + gC.getSize() );
	}

}
