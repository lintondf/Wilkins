/**
 * 
 */
package org.cryptonomicon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.zip.InflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.cryptonomicon.block.BlockedFile;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;
import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.KeyDerivationParameters;
import org.cryptonomicon.configuration.KeyDerivationParameters.ArgonParameters;
import org.cryptonomicon.mixers.Mixer;
import org.cryptonomicon.mixers.ShuffledInterlaceMixer;
import org.mindrot.BCrypt;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Longs;
import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Verifier;
import com.kosprov.jargon2.api.Jargon2.Version;
import com.kosprov.jargon2.internal.ByteArrayImpl;
import com.lambdaworks.crypto.SCrypt;

/**
 * Wilkins - Haystack Cryptographic Container 
 * Encrypts two or more data files and one or more filler files into a single
 * output file. A keyphrase is specified for each data file which will retrieve
 * its contents from the output file. Filler file sections are encrypted with a
 * randomly selected key which is not retrievable.
 * 
 * The container stores the file encrypted stream in XOR-set form in differing 
 * permuted orders depending upon the Mixer specified.  An XOR-set is composed of
 * a BlockList generated by XOR'ing the corresponding blocks of all deflated and
 * encrypted files plus an additional BlockList associated with each input file
 * generated by XOR'ing the corresponding block of that file with the all-files 
 * XOR corresponding block.
 * 
 * 		ShuffledInterlaceMixer - Permutes the output/input order of the XOR-set 
 *                               block by block.
 * 
 * 		FullyPermutedMixer - Permutes the order and position of all XOR-set blocks.
 * 
 * One use case is to allow the producer of an output file to choose which
 * keyphrase(s) to admit if placed under duress.
 * 
 */
public class Wilkins {
	
	protected List<BlockedFile> dataFiles = new ArrayList<>();
	protected List<BlockedFile> fillerFiles = new ArrayList<>();
	protected long maxLength = 0L;
	protected int fillerCount = 3;
	protected int padding = 0;
	
	private Cipher cipher;
	private static Random baseRandom = new Random();
	
	protected KeyDerivation keyDerivation;
	
	protected Mixer mixer;
	
	protected Configuration configuration;
	protected KeyDerivationParameters parameters;

			
	public Wilkins(Configuration configuration, Mixer mixer) {
		this.configuration = configuration;
		this.mixer = mixer;
		this.parameters = configuration.getKeyDerivationParameters();
		this.keyDerivation = new KeyDerivation(configuration);
		try {
			setCipher(Cipher.getInstance("AES/CBC/NoPadding"));
		} catch (Exception e) {
			Logged.log(Level.SEVERE, "Unable to configure cryptography", e);
			System.exit(0);
		}
	}
	
	public Wilkins() {
		this( new Configuration(), new ShuffledInterlaceMixer() );
	}
	
	/**
	 * @return the cipher
	 */
	public Cipher getCipher() {
		return cipher;
	}

	/**
	 * @param cipher the cipher to set
	 */
	public void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

	public boolean addDataFile(String path, FileHeader fileHeader, ByteArray passPhrase) {
		try {
			File file = new File(path);
			if (file.exists() && file.isFile() && file.length() > 0L) {
				ByteArray key = keyDerivation.deriveKey(passPhrase, fileHeader.getSalt() );
	
				AllocatedBlockedFile pair = new AllocatedBlockedFile(file, key);
				Logged.log(Level.INFO, String.format("adding %-16s %s %s %d/%d", path, Util.toString(key.getBytes()), passPhrase, pair.getOriginalLength(), pair.getCompressedLength() ));
				maxLength = Math.max(maxLength, pair.getCompressedLength());
				dataFiles.add(pair);
				return true;
			} else {
				return false;
			}
		} catch (Exception x) {
			x.printStackTrace();
			return false;
		}
	}

	public void setRandomFillerCount(int m) {
		fillerCount = m;
	}
	
	public void setPadding(int padding) {
		this.padding = padding;
	}

	public boolean read( RandomAccessFile file, OutputStream os, ByteArray passPhrase ) throws IOException, GeneralSecurityException {
		InflaterOutputStream ios = new InflaterOutputStream( os );
		
		Logged.log(Level.INFO,  String.format("Read 0: %d", file.getFilePointer()) );
		FileHeader fileHeader = new FileHeader(configuration, file);
		if (! fileHeader.isValid()) {
			Logged.log( Level.SEVERE, "Invalid Haystack file header");
			return false;
		}

		Logged.log(Level.INFO,  String.format("Read 1: %d", file.getFilePointer()) );
		Logged.log(Level.INFO,  String.format(fileHeader.toString() ) );
		ArgonParameters argonParameters = configuration.getKeyDerivationParameters().getArgonParameters();
		parameters.setArgonParameters(argonParameters);
		
		ByteArray key = keyDerivation.deriveKey(passPhrase, fileHeader.getSalt() );
		SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
		
		int fileIndex = 0;
		PayloadFileGuidance targetGuidance = null;
		while (file.getFilePointer() < file.length()) {
			PayloadFileGuidance fileGuidance = new PayloadFileGuidance(file);
			Logged.log(Level.INFO,  String.format("Read 2: %d", file.getFilePointer()) );
			fileIndex++;
			if (fileGuidance.decode(getCipher(), secretKey, fileHeader.getIV())) {
				if (fileGuidance.isValid()) {
					targetGuidance = fileGuidance;
					break;
				}
			}
		}
		if (targetGuidance == null || file.getFilePointer() >= file.length()) {
			Logged.log( Level.SEVERE, "No payload file matches specified passphrase");
			return false;
		}
		Logged.log(Level.INFO,  String.format("Read 3: %d", file.getFilePointer()) );
		Logged.log(Level.INFO,  String.format("FOUND: " + fileIndex + ": " + targetGuidance.toString()) );
		while (fileIndex < targetGuidance.getFileCount()) {
			new PayloadFileGuidance(file);
			fileIndex++;
		}
		Logged.log(Level.INFO,  String.format("Read 4: %d", file.getFilePointer()) );
		
		baseRandom.setSeed( targetGuidance.getSeed() );
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(fileHeader.getIV());
			getCipher().init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
			CipherOutputStream cos = new CipherOutputStream( ios, getCipher() );
			return mixer.readBlocks( targetGuidance, baseRandom, file, cos );
		} catch (Exception x) {
			x.printStackTrace();
			Logged.log( Level.SEVERE, "Exception reading blocks: ", x);
			return false;
		}
	}
	
	List<BlockedFile> allFiles = new ArrayList<>();
	int maxBlocks = 0;
	
	public boolean load( FileHeader fileHeader ) throws IOException {
		// deflate and encrypt all data files.
		// determine maximum size in blocks
		for (BlockedFile file : dataFiles) {
			int nBlocks = file.deflate(-1);
			nBlocks = file.encrypt( fileHeader.getIV() );
			//if (fileOrdinal == 0+1) System.out.println( "F0B0: " + file.blocks.getList().get(0).toString() );
			maxBlocks = Math.max(maxBlocks, nBlocks);
		}
		
		if (padding <= 0) {
			double p = 0.10 + (0.50-0.10)*Configuration.getSecureRandom().nextDouble();
			maxBlocks += (int) Math.ceil(p*maxBlocks);
		} else {
			maxBlocks += padding;
		}
		
//		for (Block block : dataFiles.get(0).blocks.getList()) {
//			System.out.println( block.toString() );
//		}
		// pad all files to maximum size with random data
		for (BlockedFile file : dataFiles) {
			file.pad(maxBlocks);
		}
		
//		// deflate and encrypt all filler files
//		for (BlockedFile file : fillerFiles) {
//			file.deflate(maxBlocks-1);
//			file.encrypt(fileHeader.getIV(fileOrdinal++));
//			file.pad(maxBlocks);
//		}
//		
		// generate random files to bring filler count up to specified value
		if (fillerCount > fillerFiles.size()) {
			for (int i = fillerFiles.size(); i < fillerCount; i++) {
				byte[] key = new byte[parameters.getKeySize()/8];
				Configuration.getSecureRandom().nextBytes(key);
				AllocatedBlockedFile file = new AllocatedBlockedFile( Jargon2.toByteArray(key).finalizable().clearSource(), maxBlocks );
				file.deflate(maxBlocks-1);
				file.encrypt(fileHeader.getIV());
				fillerFiles.add(file);
			}
		}
		
		// combine data and filler files into a combined list
		
		allFiles.addAll(dataFiles);
		allFiles.addAll(fillerFiles);
		//allFiles.forEach( System.out::println );
		//System.out.println( maxBlocks + " / " + allFiles.size() );
		return true;
	}

	public boolean write( RandomAccessFile writer, FileHeader fileHeader ) throws IOException {
		//System.out.printf("HEADER: (%d) %s\n", fileHeader.header.length, BaseEncoding.base16().lowerCase().encode(fileHeader.header));
		//System.out.println( fileHeader.toString() );
		Logged.log(Level.INFO,  String.format("Write 0: %d", writer.getFilePointer()) );
		Logged.log(Level.INFO,  String.format(fileHeader.toString() ) );
		writer.write( fileHeader.getPlainText() );
		Logged.log(Level.INFO,  String.format("Write 1: %d", writer.getFilePointer()) );
		
		// randomly permute the order of the files
		int[] moduli = new int[allFiles.size()];
		for (int i = 0; i < moduli.length; i++) {
			moduli[i] = i;
		}
		moduli = Util.permute( Configuration.getSecureRandom(), moduli );
		Logged.log(Level.INFO,  String.format("File Order: %s", Arrays.toString(moduli)) );
		
		// generate a common simpleRandom seed for block order permutations
		long seed = Configuration.getSecureRandom().nextLong() & 0xFFFFFFFFFFFFL;
		//System.out.printf("IV %s\n", toString(fileHeader.getIV()));

		// write guidance for each file encrypted with the individual file key in permuted order
		int guidanceOrdinal = 0;
		for (int modulus : moduli) {
			BlockedFile file = allFiles.get(modulus);
			PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, moduli.length, modulus, seed, (int) file.getOriginalLength() );
			Logged.log(Level.INFO,  fileGuidance.toString() );
//			System.out.printf("FILE %d: (%d) %s\n", modulus, fileGuidance.getPlainText().length, 
//					BaseEncoding.base16().lowerCase().encode(fileGuidance.getPlainText()));
			//System.out.println( fileGuidance.toString() );
			guidanceOrdinal++;
			fileGuidance.encode( getCipher(), file.getSecretKey(), fileHeader.getIV() );
			writer.write( fileGuidance.getCipherText() );
			//System.out.printf("      : %s\n", toString(fileGuidance.getCipherText()));
		}
		Logged.log(Level.INFO,  String.format("Write 2: %d", writer.getFilePointer()) );
		
		baseRandom.setSeed(seed);
		return mixer.writeBlocks( baseRandom, maxBlocks, allFiles, writer );
	}
	
	
}
