/**
 * 
 */
package org.cryptonomicon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author lintondf
 */
public interface Mixer {
	public boolean readBlocks( PayloadFileGuidance fileGuidance, Random random, RandomAccessFile inFile, BufferedOutputStream bos ) throws IOException;
	public boolean writeBlocks( Random random, int maxBlocks, ArrayList<BlockedFile> allFiles, RandomAccessFile writer ) throws IOException;
}

