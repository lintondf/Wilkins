package org.cryptonomicon;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.cryptonomicon.mixers.ShuffledInterlaceMixer;
import org.junit.Test;

import com.google.common.io.BaseEncoding;
import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.internal.ByteArrayImpl;

public class ShuffledInterlaceMixerTest {
	
	static final String testFileName = "test.out";
	
	private static String toString( byte[] array ) {
		return BaseEncoding.base16().lowerCase().encode(array);
	}
	
	private static byte[] toBytes( String str ) {
		return BaseEncoding.base16().lowerCase().decode(str);
	}


	private static final String[] contentStrings = new String[] {
			"0101010101",
			"020202020202"
	};
	
	private static final String[] output = new String[] {
		"030303030302",
		"010101010100",
		"020202020200",
	};
	
	private static final String[] outputDeflated = new String [] {
		"000000060603000024000b",
		"78da636404020000140006",
		"78da63620201000030000d",
	};
	private static final String[] outputDeflatedEncrypted = new String[] {
			"95e014e5c6eddfcaaee05e8b376de6cf",
			"a5190db0fc07dfcb6d9621452c9bd60a",
			"30f919553aea0001c3767fce1bf630c5",
	};
	
	private static void writeInputFile( String[] inputs ) {
		try {
			FileOutputStream fos = new FileOutputStream( new File(testFileName) );
			for (String input : inputs) {
				Block block = new Block();
				block.contents = Arrays.copyOf(toBytes(input), Block.BLOCK_SIZE);
				block.count = Block.BLOCK_SIZE;
				fos.write( block.contents );
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadBlocksBasic() {
		testReadBlocks( false, false, output, new int[]{5, 6});
	}
	
	@Test
	public void testReadBlocksZipped() {
		testReadBlocks( true, false, outputDeflated, new int[]{11, 11});
	}
	
	@Test
	public void testReadBlocksZippedEncrypted() {
		testReadBlocks( true, true, outputDeflatedEncrypted, new int[]{16, 16});
	}
	
	
	@Test
	public void testWriteBlocksBasic() {
		testWriteBlocks( false, false, output );
	}
	
	
	@Test
	public void testWriteBlocksDeflate() {
		testWriteBlocks( true, false, outputDeflated );
	}
	
	
	@Test
	public void testWriteBlocksDeflateEncrypt() {
		testWriteBlocks( true, true, outputDeflatedEncrypted );
	}
	
	
	private void testReadBlocks(boolean deflate, boolean encrypt, String[] inputs, int[] lengths ) {
		ShuffledInterlaceMixer mixer = new ShuffledInterlaceMixer();
		Random random = new Random();
		random.setSeed(0L);
		int maxBlocks = 1;
		byte[] iv = new byte[16];
		ByteArray key = Jargon2.toByteArray(new byte[16]).finalizable();
		
		BlockedFile dummy = new BlockedFile(key, 3);
		dummy.state = BlockedFile.State.RAW;
		if (deflate)
			dummy.state = BlockedFile.State.ZIPPED;
		if (encrypt)
			dummy.state = BlockedFile.State.ENCRYPTED;
		
		writeInputFile( inputs );
		
		for (int i = 0; i < contentStrings.length; i++) {
			try {
				PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, contentStrings.length, i, 
						0L, lengths[i] );
				random.setSeed(0L);
				RandomAccessFile raf = new RandomAccessFile(testFileName, "rw");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				OutputStream cos = dummy.getOutputStream(bos, iv);
				mixer.readBlocks( fileGuidance, random, raf, cos );
				//System.out.println( i + ": " + Wilkins.toString( bos.toByteArray() ));
				assertTrue( Arrays.equals(bos.toByteArray(), toBytes(contentStrings[i])));
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}	
	}

	private void testWriteBlocks(boolean deflate, boolean encrypt, String[] expected ) {
		ShuffledInterlaceMixer mixer = new ShuffledInterlaceMixer();
		Random random = new Random();
		random.setSeed(0L);
		int maxBlocks = 1;
		byte[] iv = new byte[16];
		ByteArray key = Jargon2.toByteArray( new byte[16] ).finalizable();
		ArrayList<BlockedFile> allFiles = new ArrayList<>();
		ArrayList<byte[]> contentsList = new ArrayList<>();
		for (String str : contentStrings) {
			contentsList.add( toBytes(str) );
		}
		for (byte[] contents : contentsList) {
			allFiles.add( new BlockedFile(contents, key ));
		}
		for (BlockedFile file : allFiles) {
			if (deflate) file.deflate(-1);
			if (encrypt) file.encrypt(iv);
			//System.out.println(deflate + ", " + encrypt + " len = " + file.length);
		}
		try {
			RandomAccessFile raf = new RandomAccessFile(testFileName, "rw");
			mixer.writeBlocks( random, maxBlocks, allFiles, raf );
			File file = new File(testFileName);
			String output = toString(IOUtils.readFully( new FileInputStream(file), (int) file.length()));
			
			assertTrue( output.substring(0*1024*2).startsWith( expected[0] ) );
			assertTrue( output.substring(1*1024*2).startsWith( expected[1] ) );
			assertTrue( output.substring(2*1024*2).startsWith( expected[2] ) );
//			byte[] output2 = IOUtils.readFully( new FileInputStream(file), (int) file.length());
//			while (output2.length > 0) {
//				System.out.println( "    "+ Wilkins.toString(Arrays.copyOf(output2, 1024)));
//				//System.out.println( Wilkins.toString(Arrays.copyOfRange(output, 16, 1024)));
//				output2 = Arrays.copyOfRange(output2, 1024, output2.length);
//			}
//			if (output2.length > 0) System.out.println( Wilkins.toString(output2));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
