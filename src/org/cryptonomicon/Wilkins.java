/**
 * 
 */
package org.cryptonomicon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.InflaterOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.BaseEncoding;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Verifier;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * Wilkins - Interleaved, Permuted, Multiple Encryption Container This class
 * encrypts two or more data files and zero or more filler files into a single
 * output file. A keyphrase is specified for each data file which will retrieve
 * its contents from the output file. Filler file sections are encrypted with a
 * randomly selected key which is not retrievable.
 * 
 * One use case is to allow the producer of an output file to choose which
 * keyphrase(s) to admit if placed under duress.
 * 
 * @author lintondf
 *
 */
public class Wilkins {
	
	
	protected ArrayList<BlockedFile> dataFiles = new ArrayList<>();
	protected ArrayList<BlockedFile> fillerFiles = new ArrayList<>();
	protected long maxLength = 0L;
	protected int fillerCount = 3;
	
	public static final int AES_IV_BYTES = 128/8;

	private Type type = Type.ARGON2id;
	private Version version = Version.V13;
	private int memoryCost = 65536;
	private int timeCost = 10;
	private int parallelism = 10;
	protected int keyLength = 256;
	protected int hashLength = keyLength / 8;
	private Cipher cipher;
	
	protected static Logger logger = Logger.getLogger("wilkins");
	protected static FileHandler logFileHandler = null;
	protected static SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static Logger getLogger() {
		return logger;
	}
	
	public static void setLevel(Level level) {
		logger.setLevel(level);
	}

	protected Mixer mixer = new ShuffledInterlaceMixer();

	protected Hasher defaultHasher =  com.kosprov.jargon2.api.Jargon2.jargon2Hasher().type(type).version(version)
			.memoryCost(memoryCost).timeCost(timeCost).parallelism(parallelism)
			.hashLength(hashLength);
	
	protected static class ReportLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(record.getMillis());
            String msg = String.format("%-8s", record.getLevel()) + " , "
                    + logTime.format(cal.getTime())
                    + ", "
                    + record.getSourceClassName().substring(
                            record.getSourceClassName().lastIndexOf(".")+1,
                            record.getSourceClassName().length())
                    + "::"
                    + record.getSourceMethodName()
                    + ", "
                    + record.getMessage() + "\n";
            if (record.getThrown() != null) {
            	StringBuffer sb = new StringBuffer();
            	Throwable t = record.getThrown();
            	sb.append( t.getMessage() );
            	sb.append('\n');
            	msg += sb.toString();
            }
            return msg;
        }
	}

	
	protected static void initializeLogging() {
	    try {
	        logFileHandler = new FileHandler("wilkins.log");
//	        logFileHandler = new FileHandler("wilkins-%g.log", 1*1024*1024, 10);
	    } catch (Exception e) {
			getLogger().log(Level.SEVERE, "EXCEPTION: ", e );
	        return;
	    }
	    logFileHandler.setFormatter(new ReportLogFormatter());
	    logger.addHandler(logFileHandler);
	    logger.setUseParentHandlers(false);  // no console logging
	    Handler[] handlers = logger.getParent().getHandlers();
	    for (Handler handler : handlers ) {
	    	handler.setFormatter(new ReportLogFormatter());
	    }
	    logger.setLevel(Level.ALL);  // changed by configuration
	}


			
	public Wilkins() {
		initializeLogging();
		getLogger().info( type.toString() + " " + version.toString() );
		try {
			setCipher(Cipher.getInstance("AES/CBC/NoPadding"));
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Unable to configure cryptography", e);
		}
	}
	
	public Wilkins( Mixer mixer) {
		this();
		this.mixer = mixer;
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

	public boolean addDataFile(String path, FileHeader fileHeader, String passPhrase) {
		try {
			File file = new File(path);
			if (!file.exists() && file.isFile() && file.length() > 0L)
				return false;
			byte[] key = fileHeader.getHasher().password(passPhrase.getBytes()).rawHash();
			BlockedFile pair = new BlockedFile(file, key);
			getLogger().info(String.format("adding %-16s %s %s %d", path, toString(key), passPhrase, pair.length ));
			maxLength = Math.max(maxLength, pair.length);
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
			BlockedFile pair = new BlockedFile(file, key);
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
	
	public boolean read( RandomAccessFile file, OutputStream os, String passPhrase ) throws IOException {
		InflaterOutputStream ios = new InflaterOutputStream( os );
		
		FileHeader fileHeader = new FileHeader(file);
		if (! fileHeader.isValid()) {
			return false;
		}
		System.out.printf("Read 0: %d\n", file.getFilePointer());
		System.out.println(fileHeader.toString() );
		type = fileHeader.getType();
		version = fileHeader.getVersion();
		memoryCost = fileHeader.getMemoryCost();
		timeCost = fileHeader.getTimeCost();
		
		keyLength = fileHeader.getKeySize();
		hashLength = keyLength/8;
		
		//System.out.printf("IV %s\n", toString(iv));
		byte[] key = fileHeader.getHasher()
				.password(passPhrase.getBytes()).rawHash();
		//System.out.printf("Key, Pass = %s %s\n", toString(key), passPhrase );
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		
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
	
	ArrayList<BlockedFile> allFiles = new ArrayList<>();
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
				BlockedFile file = new BlockedFile( key, maxBlocks );
				file.deflate(maxBlocks);
				file.encrypt(fileHeader.getIV(fileOrdinal++));
				fillerFiles.add(file);
			}
		}
		
		// combine data and filler files into a combined list
		
		allFiles.addAll(dataFiles);
		allFiles.addAll(fillerFiles);
		int i = 0;
		for (BlockedFile file : allFiles ) {
			System.out.println( i + ": " + file.blocks.getList().get(13).toString() );
			System.out.println( i++ + ": " + file.blocks.getList().get(14).toString() );
		}
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
		moduli = permute( secureRandom, moduli );
		
		// generate a common simpleRandom seed for block order permutations
		long seed = secureRandom.nextLong() & 0xFFFFFFFFFFFFL;
		//System.out.printf("IV %s\n", toString(fileHeader.getIV()));

		
		// write guidance for each file encrypted with the individual file key in permuted order
		int guidanceOrdinal = 0;
		for (int modulus : moduli) {
			BlockedFile file = allFiles.get(modulus);
			PayloadFileGuidance fileGuidance = new PayloadFileGuidance(maxBlocks, moduli.length, modulus, seed, (int) file.length );
			System.out.printf("FILE %d: (%d) %s\n", modulus, fileGuidance.getPlainText().length, 
					BaseEncoding.base16().lowerCase().encode(fileGuidance.getPlainText()));
			//System.out.println( fileGuidance.toString() );
			fileGuidance.encode( getCipher(), file.secretKey, fileHeader.getIV(guidanceOrdinal++) );
			writer.write( fileGuidance.getCipherText() );
			//System.out.printf("      : %s\n", toString(fileGuidance.getCipherText()));
		}
		System.out.printf("Write 2: %d\n", writer.getFilePointer());
		
		baseRandom.setSeed(seed);
		return mixer.writeBlocks( baseRandom, maxBlocks, allFiles, writer );
	}
	
	
	public void report() {
		//System.out.println(BaseEncoding.base16().lowerCase().encode(salt));
		dataFiles.forEach(System.out::println);
		fillerFiles.forEach(System.out::println);
		System.out.printf("%d  %d\n", maxLength, fillerCount);
	}

	public int[] permute( Random random, int[] array ) {
		int n = array.length;
		while (n > 1) {
			int k = random.nextInt(n--); // decrements after using the value
			int temp = array[n];
			array[n] = array[k];
			array[k] = temp;
		}
		return array;
	}
	
	public <T> void permute( Random random, ArrayList<T> iterators ) {
		int n = iterators.size();
		while (n > 1) {
			int k = random.nextInt(n--); // decrements after using the value
			T temp = iterators.get(n);
			iterators.set(n,  iterators.get(k) );
			iterators.set(k,  temp);
		}
		
	}
	
	
	private static Random baseRandom = new Random();
	protected static SecureRandom secureRandom = new SecureRandom();

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
		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
		secureRandom.nextBytes(iv);

		FileHeader fileHeader = new FileHeader(ipmec.type, ipmec.version, ipmec.memoryCost, ipmec.timeCost, ipmec.keyLength, iv );

		ipmec.addDataFile("data1.txt", fileHeader, "key1");
		ipmec.addDataFile("data2.txt", fileHeader, "key2");
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
			ipmec.read( file, new FileOutputStream(new File("test_read.out")), "key1");
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
		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
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
		test_write(args);
		test_read(args);
	}

}
