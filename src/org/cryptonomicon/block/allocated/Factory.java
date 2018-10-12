/**
 * 
 */
package org.cryptonomicon.block.allocated;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.cryptonomicon.block.Block;
import org.cryptonomicon.block.BlockInputStream;
import org.cryptonomicon.block.BlockList;
import org.cryptonomicon.block.BlockListIterator;
import org.cryptonomicon.block.BlockMethodsFactory;
import org.cryptonomicon.block.BlockReader;
import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.XorSet;

import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public class Factory implements BlockMethodsFactory {

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getBlockInstance()
	 */
	@Override
	public Block getBlockInstance() {
		return new AllocatedBlock();
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getBlockedFileInstance()
	 */
	@Override
	public BlockedFile getBlockedFileInstance(File f, ByteArray key) {
		return new AllocatedBlockedFile( f, key );
	}
	
	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getBlockedFileInstance()
	 */
	@Override
	public BlockedFile getBlockedFileInstance(ByteArray key, int nBlocks) {
		return new AllocatedBlockedFile( key, nBlocks );
	}


	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getBlockInputStreamInstance()
	 */
	@Override
	public BlockInputStream getBlockInputStreamInstance( BlockList blocks ) {
		return new AllocatedBlockInputStream( blocks );
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getBlockListInstance()
	 */
	@Override
	public BlockList getBlockListInstance() {
		return new AllocatedBlockList();
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getBlockReaderInstance()
	 */
	@Override
	public BlockReader getBlockReaderInstance(RandomAccessFile file, long length) {
		return new AllocatedBlockReader( file, length );
	}

	/* (non-Javadoc)
	 * @see org.cryptonomicon.block.BlockMethodsFactory#getXorSetInstance()
	 */
	@Override
	public XorSet getXorSetInstance( int maxBlocks, List<BlockedFile> allFiles ) {
		// TODO Auto-generated method stub
		return new AllocatedXorSet( maxBlocks, allFiles );
	}

}
