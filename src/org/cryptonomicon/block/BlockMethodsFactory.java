/**
 * 
 */
package org.cryptonomicon.block;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.cryptonomicon.block.allocated.AllocatedBlockedFile;

import com.kosprov.jargon2.api.Jargon2.ByteArray;

/**
 * @author lintondf
 *
 */
public interface BlockMethodsFactory {

	public BlockReader getBlockReaderInstance(RandomAccessFile file, long length);
	public <L extends BlockedFileList> XorSet getXorSetInstance( int maxBlocks, L allFiles );
	public Block getBlockInstance();
	public BlockedFile getBlockedFileInstance(ByteArray key, int nBlocks);
	public BlockedFile getBlockedFileInstance(File f, ByteArray key);
	public BlockInputStream getBlockInputStreamInstance(BlockList blocks);
	public BlockList getBlockListInstance();
	
}
