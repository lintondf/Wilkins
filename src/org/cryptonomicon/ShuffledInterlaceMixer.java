/**
 * 
 */
package org.cryptonomicon;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.InflaterOutputStream;

import javax.crypto.CipherOutputStream;

/**
 * @author lintondf
 *
 */
public class ShuffledInterlaceMixer implements Mixer {
	
	private <T> void permute( Random random, ArrayList<T> iterators ) {
		int n = iterators.size();
		if (n > 0) return; ///////????????????????????????
		while (n > 1) {
			int k = random.nextInt(n--); // decrements after using the value
			T temp = iterators.get(n);
			iterators.set(n,  iterators.get(k) );
			iterators.set(k,  temp);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.Mixer#readBlocks(org.cryptonomicon.Wilkins.FileGuidance, java.util.Random, java.io.BufferedInputStream, java.io.BufferedOutputStream)
	 */
	@Override
	public boolean readBlocks(PayloadFileGuidance fileGuidance, Random random,
			RandomAccessFile file, OutputStream cos)
			throws IOException {
		//TODO set seed from guidance?
		int nFiles = fileGuidance.getFileCount(); 
		long length = fileGuidance.getLength();
		int fileModulus = fileGuidance.getFileOrdinal();
		int maxBlocks = fileGuidance.getMaxBlocks();
		ArrayList<BlockReader> readers = new ArrayList<>();
		for (int i = 0; i < nFiles+1; i++) {
			readers.add( new BlockReader(file, length ) );
		}
		ArrayList<BlockReader> shuffled = new ArrayList<>();
		shuffled.addAll(readers);
		int nBlocks = (int) length / Block.BLOCK_SIZE;  //TODO hoist to method
		if ( ((int) length % Block.BLOCK_SIZE) > 0)
			nBlocks++;
		int remaining = (int) length;
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			permute( random, shuffled );
			for (BlockReader reader : shuffled) {
				reader.readFull();
				if (iBlock >= 13 && iBlock <= 14) System.out.printf( "R%d,%d %s\n", iBlock, readers.indexOf(reader), reader.getLast().toString() );
			}
			Block allXor = readers.get(nFiles).getLast();
			//if (iBlock == 0) System.out.println( "readBlocks XORALL B0: " + allXor.toString() );
			Block allButTarget = readers.get(fileModulus).getLast();
			//if (iBlock == 0) System.out.println( "readBlocks XORBUT B0: " + allButTarget.toString() );
			allXor = allXor.xor( allButTarget );
			//if (iBlock == 0) System.out.println( "readBlocks output B0: " + allXor.toString() );
			//System.out.printf("%3d %3d  %8d / %s\n", iBlock, nBlocks, remaining, allXor.toString() );
			cos.write(allXor.contents, 0, (remaining > Block.BLOCK_SIZE) ? Block.BLOCK_SIZE : remaining );
			remaining -= Block.BLOCK_SIZE;
		}
		cos.close();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.Mixer#writeBlocks(java.util.Random, int, java.util.ArrayList, java.io.BufferedOutputStream)
	 */
	@Override
	public boolean writeBlocks(Random random, int maxBlocks,
			ArrayList<BlockedFile> allFiles, RandomAccessFile writer)
			throws IOException {
		// generate xor'd data blocks: {for-each-i {xor(all but i)}, xor all}
		ArrayList<Block.BlockList> allLists = new ArrayList<>();
		for (BlockedFile file : allFiles) {
			allLists.add( file.blocks );
		}
		Block.BlockList xorOfAll = Block.xor(allLists);
		ArrayList<Block.BlockList> xorExcept = new ArrayList<>();
		ArrayList<Block.BlockListIterator> iterators = new ArrayList<>();
		ArrayList<Block.BlockListIterator> shuffled = new ArrayList<>();
		for (int iList = 0; iList < allLists.size(); iList++) {
			Block.BlockList blockList = Block.xor( xorOfAll, allLists.get(iList) );
			xorExcept.add( blockList );
			iterators.add( blockList.getIterator() );
		}
		iterators.add( xorOfAll.getIterator() ); // in file order
		shuffled.addAll( iterators );
		System.out.printf("WriteBlocks %d %d @ %d\n", maxBlocks, iterators.size(), writer.getFilePointer() );
		
		for (int iBlock = 0; iBlock < maxBlocks; iBlock++) {
			permute( random, shuffled );
			for (Block.BlockListIterator it : shuffled) {
				Block block = it.next();
				if (iBlock >= 13 && iBlock <= 14) System.out.printf( "W%d,%d %s\n", iBlock, iterators.indexOf(it), block.toString() );
				writer.write( block.contents, 0, Block.BLOCK_SIZE );
				//System.out.printf("%d,%d @ %d\n", iBlock, iterators.indexOf(it), writer.getFilePointer() );
			}
		}
		//System.out.printf("WriteBlocks Final: %d\n", writer.getFilePointer());
		writer.close();
		return true;
	}
	

	public static void main( String[] args ) {
		ShuffledInterlaceMixer mixer = new ShuffledInterlaceMixer();
		Random random = new Random();
		random.setSeed(0L);
		int maxBlocks = 1;
		byte[] iv = new byte[16];
		ArrayList<BlockedFile> allFiles = new ArrayList<>();
		ArrayList<byte[]> contentsList = new ArrayList<>();
		contentsList.add(new byte[] {1, 1, 1, 1, 1});
		contentsList.add(new byte[] {2, 2, 2, 2, 2});
		for (byte[] contents : contentsList) {
			allFiles.add( new BlockedFile(contents , new byte[16] ));
		}
		for (BlockedFile file : allFiles) {
			file.deflate(-1);
			file.encrypt(iv);
		}
		try {
			RandomAccessFile raf = new RandomAccessFile("test.bin", "rw");
			mixer.writeBlocks( random, maxBlocks, allFiles, raf );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < contentsList.size(); i++) {
			try {
				PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, allFiles.size(), i, 0L, (int) allFiles.get(i).length );
				random.setSeed(0L);
				RandomAccessFile raf = new RandomAccessFile("test.bin", "rw");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				OutputStream cos = allFiles.get(i).getOutputStream(bos, iv);
				mixer.readBlocks( fileGuidance, random, raf, cos );
				System.out.println( i + ": " + Wilkins.toString( bos.toByteArray() ));
				if (! Arrays.equals(bos.toByteArray(), contentsList.get(i)))
					System.err.println("No Match");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}