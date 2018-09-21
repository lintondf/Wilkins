/**
 * 
 */
package org.cryptonomicon;

import java.nio.ByteBuffer;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;

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
			.decode("8c0d04070302783fe12337560647d5");
	
	private static final int gpgHeader2_Size = 7;
	
	public static int getGpgHeaderSize() {
		return gpgHeader1.length + gpgHeader2_Size;
	}
	
	public static byte[] getGpgHeader( int fileSize ) {
		byte[] size = Ints.toByteArray(fileSize);
		ByteBuffer byteBuffer = ByteBuffer.allocate( getGpgHeaderSize() );
		byteBuffer.put( gpgHeader1 );
		byteBuffer.put( (byte) 0xd2 );
		byteBuffer.put( (byte) 0xff );
		byteBuffer.put(size);
		byteBuffer.put( (byte) 0x01 );
		return byteBuffer.array();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
