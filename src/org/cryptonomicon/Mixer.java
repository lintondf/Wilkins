/**
 * 
 */
package org.cryptonomicon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.CipherOutputStream;

/**
 * @author lintondf
 */
public interface Mixer {
	public boolean readBlocks( PayloadFileGuidance fileGuidance, Random random, RandomAccessFile inFile, OutputStream cos ) throws IOException;
	public boolean writeBlocks( Random random, int maxBlocks, ArrayList<BlockedFile> allFiles, RandomAccessFile writer ) throws IOException;
}

