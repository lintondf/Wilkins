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
import org.cryptonomicon.block.BlockedFileList;
import org.cryptonomicon.block.allocated.AllocatedBlockedFile;
import org.cryptonomicon.block.allocated.AllocatedBlockedFileList;
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
	
	protected AllocatedBlockedFileList dataFiles = new AllocatedBlockedFileList();
	protected AllocatedBlockedFileList fillerFiles = new AllocatedBlockedFileList();
	protected long maxLength = 0L;
	protected int fillerCount = 3;
	
	protected int keyLength = 256;
	
	protected int hashLength = keyLength / 8;
	private Cipher cipher;
	
	protected KeyDerivation keyDerivation;
	
	protected Mixer mixer;
	
	protected Configuration configuration;
	protected KeyDerivationParameters parameters;

			
	public Wilkins(Configuration configuration, Mixer mixer) {
		Main.initializeLogging();
		this.configuration = configuration;
		this.parameters = configuration.getKeyDerivationParameters();
		this.keyDerivation = new KeyDerivation(configuration);
		try {
			setCipher(Cipher.getInstance("AES/CBC/NoPadding"));
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, "Unable to configure cryptography", e);
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
			if (!file.exists() && file.isFile() && file.length() > 0L)
				return false;
			//byte[] key = fileHeader.getHasher().password(passPhrase.getBytes()).rawHash();
			ByteArray key = keyDerivation.deriveKey(passPhrase, fileHeader.getSalt() );

			AllocatedBlockedFile pair = new AllocatedBlockedFile(file, key);
			Main.getLogger().info(String.format("adding %-16s %s %s %d", path, toString(key.getBytes()), passPhrase, pair.getLength() ));
			maxLength = Math.max(maxLength, pair.getLength());
			dataFiles.add(pair);
			return true;
		} catch (Exception x) {
			return false;
		}
	}

	public boolean addFillerFile(String path) {
		try {
			File file = new File(path);
			if (!file.exists() && file.isFile() && file.length() > 0L)
				return false;
			byte[] key = new byte[hashLength];
			secureRandom.nextBytes(key);
			AllocatedBlockedFile pair = new AllocatedBlockedFile(file, Jargon2.toByteArray(key).finalizable().clearSource());
			fillerFiles.add(pair);
			fillerCount++;
			return true;
		} catch (Exception x) {
			return false;
		}
	}

	public void setRandomFillerCount(int m) {
		fillerCount = m;
	}
	
	public boolean read( RandomAccessFile file, OutputStream os, ByteArray passPhrase ) throws IOException, GeneralSecurityException {
		Configuration configuration = new Configuration();  // TODO hoist to class
		InflaterOutputStream ios = new InflaterOutputStream( os );
		
		FileHeader fileHeader = new FileHeader(configuration, file);
		if (! fileHeader.isValid()) {
			return false;
		}
		System.out.printf("Read 0: %d\n", file.getFilePointer());
		System.out.println(fileHeader.toString() );
		ArgonParameters argonParameters = configuration.getKeyDerivationParameters().getArgonParameters(); /* fileHeader.getType(),
			fileHeader.getVersion(),
			fileHeader.getMemoryCost(),
			fileHeader.getTimeCost(),
			2 );*/
		parameters.setArgonParameters(argonParameters);
		
		keyLength = configuration.getKeyDerivationParameters().getKeySize();
		hashLength = keyLength/8;
		
		//System.out.printf("IV %s\n", toString(iv));
//		byte[] key = fileHeader.getHasher()
//				.password(passPhrase.getBytes()).rawHash();
//		//System.out.printf("Key, Pass = %s %s\n", toString(key), passPhrase );
//		SecretKey secretKey = new SecretKeySpec(key, "AES");
		
		ByteArray key = keyDerivation.deriveKey(passPhrase, fileHeader.getSalt() );
		SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
		
		int fileIndex = 0;
		PayloadFileGuidance targetGuidance = null;
		while (file.getFilePointer() < file.length()) {
			PayloadFileGuidance fileGuidance = new PayloadFileGuidance(file);
			if (fileGuidance.decode(getCipher(), secretKey, fileHeader.getIV(fileIndex++))) {
				if (fileGuidance.isValid()) {
					targetGuidance = fileGuidance;
					break;
				}
			}
		}
		if (targetGuidance == null || file.getFilePointer() >= file.length())
			return false;
		System.out.println("FOUND: " + fileIndex + ": " + targetGuidance.toString());
		while (fileIndex < targetGuidance.getFileCount()) {
			new PayloadFileGuidance(file);
			fileIndex++;
		}
		System.out.printf("Read 1: %d\n", file.getFilePointer());
		
		baseRandom.setSeed( targetGuidance.getSeed() );
		//TODO add mixer selection to payloadfileguidance
		try {
			IvParameterSpec parameterSpec = new IvParameterSpec(fileHeader.getIV(targetGuidance.getFileOrdinal()));
			System.out.println( toString(parameterSpec.getIV()));
			System.out.println( toString(secretKey.getEncoded()));
			getCipher().init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
			CipherOutputStream cos = new CipherOutputStream( ios, getCipher() );
			return mixer.readBlocks( targetGuidance, baseRandom, file, cos );
		} catch (Exception x) {
			x.printStackTrace();
			return false;
		}
	}
	
	AllocatedBlockedFileList allFiles = new AllocatedBlockedFileList();
	int maxBlocks = 0;
	
	public boolean load( RandomAccessFile writer, FileHeader fileHeader ) throws IOException {
		// deflate and encrypt all data files.
		// determine maximum size in blocks
		int fileOrdinal = 0;
		for (BlockedFile file : dataFiles) {
			int nBlocks = file.deflate(-1);
			nBlocks = file.encrypt( fileHeader.getIV(fileOrdinal++) );
			//if (fileOrdinal == 0+1) System.out.println( "F0B0: " + file.blocks.getList().get(0).toString() );
			maxBlocks = Math.max(maxBlocks, nBlocks);
		}
		
//		for (Block block : dataFiles.get(0).blocks.getList()) {
//			System.out.println( block.toString() );
//		}
		// pad all files to maximum size with random data
		for (BlockedFile file : dataFiles) {
			file.pad(maxBlocks);
		}
		
		// deflate and encrypt all filler files
		for (BlockedFile file : fillerFiles) {
			file.deflate(maxBlocks-1);
			file.encrypt(fileHeader.getIV(fileOrdinal++));
			file.pad(maxBlocks);
		}
		
		// generate random files to bring filler count up to specified value
		if (fillerCount > fillerFiles.size()) {
			for (int i = fillerFiles.size(); i < fillerCount; i++) {
				byte[] key = new byte[hashLength];
				secureRandom.nextBytes(key);
				AllocatedBlockedFile file = new AllocatedBlockedFile( Jargon2.toByteArray(key).finalizable().clearSource(), maxBlocks );
				file.deflate(maxBlocks);
				file.encrypt(fileHeader.getIV(fileOrdinal++));
				fillerFiles.add(file);
			}
		}
		
		// combine data and filler files into a combined list
		
		allFiles.addAll(dataFiles);
		allFiles.addAll(fillerFiles);
		//allFiles.forEach( System.out::println );
		System.out.println( maxBlocks + " / " + allFiles.size() );
		return true;
	}

	public boolean write( RandomAccessFile writer, FileHeader fileHeader ) throws IOException {
		//System.out.printf("HEADER: (%d) %s\n", fileHeader.header.length, BaseEncoding.base16().lowerCase().encode(fileHeader.header));
		//System.out.println( fileHeader.toString() );
		System.out.printf("Write 0: %d\n", writer.getFilePointer());
		writer.write( fileHeader.getPlainText() );
		System.out.printf("Write 1: %d\n", writer.getFilePointer());
		
		// randomly permute the order of the files
		int[] moduli = new int[allFiles.size()];
		for (int i = 0; i < moduli.length; i++) {
			moduli[i] = i;
		}
		moduli = Util.permute( secureRandom, moduli );
		
		// generate a common simpleRandom seed for block order permutations
		long seed = secureRandom.nextLong() & 0xFFFFFFFFFFFFL;
		//System.out.printf("IV %s\n", toString(fileHeader.getIV()));

		
		// write guidance for each file encrypted with the individual file key in permuted order
		int guidanceOrdinal = 0;
		for (int modulus : moduli) {
			BlockedFile file = allFiles.get(modulus);
			PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, moduli.length, modulus, seed, (int) file.getLength() );
			System.out.printf("FILE %d: (%d) %s\n", modulus, fileGuidance.getPlainText().length, 
					BaseEncoding.base16().lowerCase().encode(fileGuidance.getPlainText()));
			//System.out.println( fileGuidance.toString() );
			fileGuidance.encode( getCipher(), file.getSecretKey(), fileHeader.getIV(guidanceOrdinal++) );
			writer.write( fileGuidance.getCipherText() );
			//System.out.printf("      : %s\n", toString(fileGuidance.getCipherText()));
		}
		System.out.printf("Write 2: %d\n", writer.getFilePointer());
		
		baseRandom.setSeed(seed);
		return mixer.writeBlocks( baseRandom, maxBlocks, allFiles, writer );
	}
	
	
	public void report() {
		//System.out.println(BaseEncoding.base16().lowerCase().encode(salt));
//		dataFiles.forEach(System.out::println);
//		fillerFiles.forEach(System.out::println);
		System.out.printf("%d  %d\n", maxLength, fillerCount);
	}

	/**
	 * @return the secureRandom
	 */
	public static SecureRandom getSecureRandom() {
		return secureRandom;
	}

	private static Random baseRandom = new Random();
	private static SecureRandom secureRandom = new SecureRandom();

	/**
	 * @param args
	 */
	protected static void test_Jargon(String[] args) {
		byte[] salt = new byte[128 / 8];
		secureRandom.nextBytes(salt);
		System.out.printf("Salt: %s%n", BaseEncoding.base16().lowerCase()
				.encode(salt));
		byte[] password = "this is a password".getBytes();

		Type type = Type.ARGON2d;
		Version version = Version.V13;
		int memoryCost = 65536;
		int timeCost = 3;
		int parallelism = 4;
		int hashLength = 128 / 8;
		
		// Configure the hasher
		Hasher hasher = com.kosprov.jargon2.api.Jargon2.jargon2Hasher()
				.type(type).memoryCost(memoryCost).timeCost(timeCost)
				.parallelism(parallelism).hashLength(hashLength).version(version);

		// Configure the verifier with the same settings as the hasher
		Verifier verifier = com.kosprov.jargon2.api.Jargon2.jargon2Verifier()
				.type(type).memoryCost(memoryCost).timeCost(timeCost)
				.parallelism(parallelism);

		// Set the salt and password to calculate the raw hash
		byte[] rawHash = hasher.salt(salt).password(password).rawHash();

		System.out.printf("Hash: %s%n", BaseEncoding.base16().lowerCase().encode(rawHash));

		// Set the raw hash, salt and password and verify
		boolean matches = verifier.hash(rawHash).salt(salt).password(password)
				.verifyRaw();

		System.out.printf("Matches: %s%n", matches);

	}
	
	public static String toString( byte[] array ) {
		return String.format("(%d) %s", array.length, BaseEncoding.base16().lowerCase().encode(array) );
	}
	
	public static void test_Permute(String[] args) {
		int[] array = new int[20];
		for (int i = 1; i <= array.length; i++)
			array[i - 1] = i;
		System.out.println(Arrays.toString(array));
		baseRandom.setSeed(1L);
		int n = array.length;
		while (n > 1) {
			int k = baseRandom.nextInt(n--); // decrements after using the value
			int temp = array[n];
			array[n] = array[k];
			array[k] = temp;
		}
		System.out.println(Arrays.toString(array));

		for (int i = 1; i <= array.length; i++)
			array[i - 1] = i;
		baseRandom.setSeed(1L);
		for (int i = 0; i < array.length - 2; i++) {
			int k = i + baseRandom.nextInt(array.length - i); // decrements
																// after using
																// the value
			int temp = array[i];
			array[i] = array[k];
			array[k] = temp;
		}
		System.out.println(Arrays.toString(array));
	}
	
//	public static void test_writeReadBlocks( String[] args) {
//		//	protected boolean writeBlocks( Random random, int maxBlocks, ArrayList<BlockedFile> allFiles, BufferedOutputStream bos ) throws IOException {
//		Random random = new Random();
//		random.setSeed(0L);
//		int maxBlocks = 1;
//		ArrayList<BlockedFile> allFiles = new ArrayList<>();
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		BufferedOutputStream bos = new BufferedOutputStream(out);
//		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
//		for (int i = 0; i < 3; i++) {
//			BlockedFile file = new BlockedFile( new byte[32], 1 );
//			Block block = file.blocks.getList().get(0);
//			block.count = 16;
//			System.out.printf( "I%d: %s\n", i, block.toString() );
//			//Arrays.fill(block.contents, (byte) i);
//			allFiles.add(file);
//		}
//		Wilkins ipmec = new Wilkins();
//		try {
//			ipmec.writeBlocks( random, maxBlocks, allFiles, bos );
//			byte[] result = out.toByteArray();
//			String block = BaseEncoding.base16().lowerCase().encode(result);
//			for (int i = 0; i < block.length(); i+=16*2) {
//				System.out.printf("D%2d: (%d) %s\n", i,  result.length, block.substring(i, i+16*2));
//			}
//			for (int iFile = 0; iFile < 3; iFile++) {
//				random.setSeed(0L);
//				ByteArrayInputStream inp = new ByteArrayInputStream( result );
//				BufferedInputStream bis = new BufferedInputStream( inp );
//				out = new ByteArrayOutputStream();
//				bos = new BufferedOutputStream(out);
//				ipmec.readBlocks(random, 3, 16, iFile, 1, bis, bos);
//				byte[] output = out.toByteArray();
//				block = BaseEncoding.base16().lowerCase().encode(output);
//				for (int i = 0; i < block.length(); i+=16*2) {
//					int n = Math.min( block.length(), i+16*2);
//					System.out.printf("O%d: (%d) %s\n", iFile, output.length, block.substring(i, n));
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
	public static void test_write(String[] args) {
		Wilkins ipmec = new Wilkins();
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		secureRandom.nextBytes(iv);

		FileHeader fileHeader = new FileHeader(ipmec.parameters, Jargon2.toByteArray(iv) );

		ipmec.addDataFile("data1.txt", fileHeader, Jargon2.toByteArray("key1"));
		ipmec.addDataFile("data2.txt", fileHeader, Jargon2.toByteArray("key2"));
		ipmec.addFillerFile("filler1.txt");
		ipmec.setRandomFillerCount(3);
		
		File file = new File("output.gpg");
		try {
			RandomAccessFile writer = new RandomAccessFile(file, "rw");
			ipmec.load(writer, fileHeader );
			ipmec.camouflage( writer );
			ipmec.write(writer, fileHeader );
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Path outputPath = file.toPath();
//		try (FileChannel fileChannel = (FileChannel) Files
//				  .newByteChannel(outputPath, EnumSet.of(
//						    StandardOpenOption.READ, 
//						    StandardOpenOption.WRITE, 
//						    StandardOpenOption.TRUNCATE_EXISTING))) {
//			
//		    MappedByteBuffer mappedByteBuffer = fileChannel
//		    	      .map(FileChannel.MapMode.READ_WRITE, 0, charBuffer.length());
//
//			ipmec.write(out, fileHeader);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		ipmec.report();
	}
	
	public void camouflage(RandomAccessFile writer) {
		// compute total file size
	
	}

	public static void test_read(String[] args) {
		Wilkins ipmec = new Wilkins();
		try {
			File in = new File("output.gpg");
			RandomAccessFile file = new RandomAccessFile( in, "r");
			ipmec.read( file, new FileOutputStream(new File("test_read.out")), Jargon2.toByteArray("key1"));
			ipmec.report();
		} catch (Exception x) {
			
		}
	}
	
	public static void test_fileGuidance( String[] args) {
		//FileGuidance(int maxBlocks, int nm, int modulus, long seed, long length )
		Wilkins ipmec = new Wilkins();
		PayloadFileGuidance g = new PayloadFileGuidance( 1, 2, 3, 4L, 5 );
		System.out.println(g.toString());
		System.out.println( toString(g.getPlainText() ) );
		byte[] key = new byte[ipmec.hashLength];
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[Configuration.AES_IV_BYTES];
		g.encode(ipmec.getCipher(), secretKey, iv);
		System.out.println( toString( g.getCipherText()) );
		g.decode(ipmec.getCipher(), secretKey, iv);
		System.out.println( toString( g.getPlainText()) );
		System.out.println(g.toString());		
	}
	
	
	public static void main( String[] args ) {
		//https://commons.apache.org/proper/commons-cli/usage.html
		//test_fileGuidance( args );
		//test_writeReadBlocks(args);
//		test_write(args);
//		test_read(args);
	}

}
