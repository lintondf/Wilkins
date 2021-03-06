package org.cryptonomicon.mixers;

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
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.cryptonomicon.PayloadFileGuidance;
import org.cryptonomicon.Wilkins;
import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.allocated.AllocatedBlock;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;
import org.cryptonomicon.mixers.ShuffledInterlaceMixer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.BaseEncoding;
import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.internal.ByteArrayImpl;

public class ShuffledInterlaceMixerTest {
	
	private static Wilkins wilkins = null;
	private static File testFile = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		wilkins = new Wilkins();
		testFile = File.createTempFile("test", ".mixer");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
		testFile.delete();
	}

	
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
	
	private void writeInputFile( String[] inputs ) {
		try {
			FileOutputStream fos = new FileOutputStream( testFile.getAbsolutePath() );
			for (String input : inputs) {
				Block block = new AllocatedBlock( toBytes(input) );
//				System.out.println(input + " " + block.toString());
				block.pad();
				block.write( fos );
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
		
		AllocatedBlockedFile dummy = new AllocatedBlockedFile(key, 3);
		dummy.setState(BlockedFile.State.RAW);
		if (deflate)
			dummy.setState(BlockedFile.State.ZIPPED);
		if (encrypt)
			dummy.setState(BlockedFile.State.ENCRYPTED);
		//System.out.println( deflate + " " + encrypt + " " + dummy.getState() );
		
		writeInputFile( inputs );
		
		for (int i = 0; i < contentStrings.length; i++) {
			try {
				PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, contentStrings.length, i, 
						0L, lengths[i] );
				random.setSeed(0L);
				RandomAccessFile raf = new RandomAccessFile(testFile, "rw");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				OutputStream cos = dummy.getOutputStream(bos, iv);
				mixer.readBlocks( fileGuidance, random, raf, cos );
				//System.out.println(i + ": " + Wilkins.toString(bos.toByteArray()) + " vs " + contentStrings[i] );
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
		List<BlockedFile> allFiles = new ArrayList<>();
		ArrayList<byte[]> contentsList = new ArrayList<>();
		for (String str : contentStrings) {
			contentsList.add( toBytes(str) );
		}
		for (byte[] contents : contentsList) {
			allFiles.add( new AllocatedBlockedFile(contents, key ));
		}
		for (BlockedFile file : allFiles) {
			if (deflate) file.deflate(-1);
			if (encrypt) file.encrypt(iv);
			//System.out.println(deflate + ", " + encrypt + " len = " + file.length);
		}
		try {
			RandomAccessFile raf = new RandomAccessFile(testFile, "rw");
			mixer.writeBlocks( random, maxBlocks, allFiles, raf );
			String output = toString(IOUtils.readFully( new FileInputStream(testFile), (int) testFile.length()));
			
			assertTrue( output.substring(0*1024*2).startsWith( expected[0] ) );
			assertTrue( output.substring(1*1024*2).startsWith( expected[1] ) );
			assertTrue( output.substring(2*1024*2).startsWith( expected[2] ) );
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
