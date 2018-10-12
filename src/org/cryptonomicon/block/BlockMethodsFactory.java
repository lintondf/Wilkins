/**
 * 
 */
package org.cryptonomicon.block;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.cryptonomicon.block.allocated.AllocatedBlockedFile;

import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public interface BlockMethodsFactory {

	public BlockReader getBlockReaderInstance(RandomAccessFile file, long length);
	public XorSet getXorSetInstance( int maxBlocks, List<BlockedFile> allFiles );
	public Block getBlockInstance();
	public BlockedFile getBlockedFileInstance(ByteArray key, int nBlocks);
	public BlockedFile getBlockedFileInstance(File f, ByteArray key);
	public BlockInputStream getBlockInputStreamInstance(BlockList blocks);
	public BlockList getBlockListInstance();
	
}
