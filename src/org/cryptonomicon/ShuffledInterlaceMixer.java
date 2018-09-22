/**
 * 
 */
package org.cryptonomicon;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

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
			RandomAccessFile file, BufferedOutputStream bos)
			throws IOException {
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
		for (int iBlock = 0; iBlock < maxBlocks; iBlock++) {
			permute( random, shuffled );
			for (BlockReader reader : shuffled) {
				reader.read();
				//System.out.printf( "R%d %s\n", iBlock, reader.getLast().toString() );
			}
			Block allXor = readers.get(nFiles).getLast();
			Block allButTarget = readers.get(fileModulus).getLast();
			allXor = allXor.xor( allButTarget );
			bos.write(allXor.contents);
		}
		bos.close();
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
		//System.out.printf("WriteBlocks %d %d @ %d\n", maxBlocks, iterators.size(), writer.getFilePointer() );
		
		for (int iBlock = 0; iBlock < maxBlocks; iBlock++) {
			permute( random, shuffled );
			for (Block.BlockListIterator it : shuffled) {
				Block block = it.next();
				//System.out.printf( "W%d %s\n", iBlock, block.toString() );
				writer.write( block.contents, 0, block.count );
				//System.out.printf("%d,%d @ %d\n", iBlock, iterators.indexOf(it), writer.getFilePointer() );
			}
		}
		//System.out.printf("WriteBlocks Final: %d\n", writer.getFilePointer());
		writer.close();
		return true;
	}

}
