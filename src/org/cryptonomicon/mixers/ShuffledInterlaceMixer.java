/**
 * 
 */
package org.cryptonomicon.mixers;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.zip.InflaterOutputStream;

import javax.crypto.CipherOutputStream;

import org.cryptonomicon.Logged;
import org.cryptonomicon.PayloadFileGuidance;
import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.cryptonomicon.block.BlockReader;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.allocated.AllocatedBlock;
import org.cryptonomicon.block.allocated.AllocatedBlockList;
import org.cryptonomicon.block.allocated.AllocatedBlockReader;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;

/**
 * @author lintondf
 *
 */
public class ShuffledInterlaceMixer implements Mixer {
	
	private <T> void permute( Random random, ArrayList<T> iterators ) {
		int n = iterators.size();
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
		//System.out.println("readBlocks: " + fileGuidance.toString() );
		int nFiles = fileGuidance.getFileCount(); 
		long length = fileGuidance.getLength();
		int fileModulus = fileGuidance.getFileOrdinal();
		ArrayList<AllocatedBlockReader> readers = new ArrayList<>();
		for (int i = 0; i < nFiles+1; i++) {
			readers.add( new AllocatedBlockReader(file, length ) );
		}
		ArrayList<AllocatedBlockReader> shuffled = new ArrayList<>();
		shuffled.addAll(readers);
		int nBlocks = (int) length / Block.BLOCK_SIZE;  //TODO hoist to method
		if ( ((int) length % Block.BLOCK_SIZE) > 0)
			nBlocks++;
		int remaining = (int) length;
		Logged.log(Level.FINE, String.format("Read Blocks %d from %d of %d", nBlocks, fileModulus, length ));
		for (int iBlock = 0; iBlock < nBlocks; iBlock++) {
			permute( random, shuffled );
			for (BlockReader reader : shuffled) {
				reader.readFull();
				Logged.log(Level.FINEST, String.format( "R%d,%d %s", iBlock, readers.indexOf(reader), reader.getLast().toString() ) );
			}
			Block allXor = readers.get(nFiles).getLast();
			AllocatedBlock allButTarget = readers.get(fileModulus).getLast();
			allXor = allXor.xor( allButTarget );
			Logged.log(Level.FINER, String.format("%3d %3d  %8d", iBlock, nBlocks, remaining ));
			allXor.write( cos, (remaining > Block.BLOCK_SIZE) ? Block.BLOCK_SIZE : remaining);
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
			List<BlockedFile> allFiles, RandomAccessFile writer)
			throws IOException {
		// generate xor'd data blocks: {for-each-i {xor(all but i)}, xor all}
		for (BlockedFile file : allFiles) {
			Logged.log(Level.FINEST, String.format("of %d/%d", file.getOriginalLength(), file.getCompressedLength() ) );
		}
		ArrayList<BlockList> allLists = new ArrayList<>();
		for (BlockedFile file : allFiles) {
			allLists.add( file.getBlockList() );
		}
		AllocatedBlockList xorOfAll = new AllocatedBlockList();
		xorOfAll.xor(allLists);
		ArrayList<AllocatedBlockList> xorExcept = new ArrayList<>();
		ArrayList<BlockListIterator> iterators = new ArrayList<>();
		ArrayList<BlockListIterator> shuffled = new ArrayList<>();
		for (int iList = 0; iList < allLists.size(); iList++) {
			AllocatedBlockList blockList = new AllocatedBlockList();
			blockList.xor( xorOfAll, allLists.get(iList) );
			xorExcept.add( blockList );
			iterators.add( blockList.getIterator() );
		}
		iterators.add( xorOfAll.getIterator() ); // in file order
		shuffled.addAll( iterators );
		Logged.log(Level.FINEST, String.format("WriteBlocks %d %d @ %d", maxBlocks, iterators.size(), writer.getFilePointer() ) );
		
		for (int iBlock = 0; iBlock < maxBlocks; iBlock++) {
			permute( random, shuffled );
			for (BlockListIterator it : shuffled) {
				Block block = it.next();
				block.write( writer, Block.BLOCK_SIZE );
				//Main.getLogger().log(Level.FINEST, String.format("%d,%d @ %d", iBlock, iterators.indexOf(it), writer.getFilePointer() ) );
			}
		}
		Logged.log(Level.FINEST, String.format("WriteBlocks Final: %d", writer.getFilePointer()));
		writer.close();
		return true;
	}
	

//	public static void main( String[] args ) {
//		ShuffledInterlaceMixer mixer = new ShuffledInterlaceMixer();
//		Random random = new Random();
//		random.setSeed(0L);
//		int maxBlocks = 1;
//		byte[] iv = new byte[16];
//		ArrayList<BlockedFile> allFiles = new ArrayList<>();
//		ArrayList<byte[]> contentsList = new ArrayList<>();
//		contentsList.add(new byte[] {1, 1, 1, 1, 1});
//		contentsList.add(new byte[] {2, 2, 2, 2, 2});
//		for (byte[] contents : contentsList) {
//			allFiles.add( new BlockedFile(contents , new byte[16] ));
//		}
//		for (BlockedFile file : allFiles) {
//			file.deflate(-1);
//			file.encrypt(iv);
//		}
//		try {
//			RandomAccessFile raf = new RandomAccessFile("test.bin", "rw");
//			mixer.writeBlocks( random, maxBlocks, allFiles, raf );
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		for (int i = 0; i < contentsList.size(); i++) {
//			try {
//				PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, allFiles.size(), i, 0L, (int) allFiles.get(i).length );
//				random.setSeed(0L);
//				RandomAccessFile raf = new RandomAccessFile("test.bin", "rw");
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				OutputStream cos = allFiles.get(i).getOutputStream(bos, iv);
//				mixer.readBlocks( fileGuidance, random, raf, cos );
//				//System.out.println( i + ": " + Wilkins.toString( bos.toByteArray() ));
//				if (! Arrays.equals(bos.toByteArray(), contentsList.get(i)))
//					System.err.println("No Match");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
