/**
 * 
 */
package org.cryptonomicon.mixers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.CipherOutputStream;

import org.cryptonomicon.PayloadFileGuidance;
import org.cryptonomicon.block.BlockedFile;

/**
 * @author lintondf
 */
public interface Mixer {
	public boolean readBlocks( PayloadFileGuidance fileGuidance, Random random, RandomAccessFile inFile, OutputStream cos ) throws IOException;
	public boolean writeBlocks( Random random, int maxBlocks, List<BlockedFile> allFiles, RandomAccessFile writer ) throws IOException;
}

