/**
 * 
 */
package org.cryptonomicon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Verifier;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * IPME - Interleaved, Permuted, Multiple Encryption Container This class
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

	private Type type = Type.ARGON2d;
	private Version version = Version.V13;
	private int memoryCost = 65536;
	private int timeCost = 3;
	protected int keyLength = 256;
	protected int hashLength = keyLength / 8;
	private Cipher cipher;


	public Wilkins() {
		//System.out.println( type.toString() + " " + version.toString() );
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean addDataFile(String path, FileHeader fileHeader, String passPhrase) {
		try {
			File file = new File(path);
			if (!file.exists() && file.isFile() && file.length() > 0L)
				return false;
			byte[] key = fileHeader.getHasher().password(passPhrase.getBytes()).rawHash();
			//System.out.printf("%-16s %s %s\n", path, toString(key), passPhrase );
			BlockedFile pair = new BlockedFile(file, key);
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
	
	public boolean read( File inputFile, OutputStream os, String passPhrase ) throws IOException {
		FileInputStream fis = new FileInputStream(inputFile);
		BufferedInputStream bis = new BufferedInputStream( fis );
		BufferedOutputStream bos = new BufferedOutputStream( os );
		
		FileHeader fileHeader = new FileHeader(bis);
		if (! fileHeader.isValid()) {
			return false;
		}
		System.out.println(fileHeader.toString() );
		type = fileHeader.getType();
		version = fileHeader.getVersion();
		memoryCost = fileHeader.getMemoryCost();
		timeCost = fileHeader.getTimeCost();
		
		keyLength = fileHeader.getKeySize();
		hashLength = keyLength/8;
		
		byte[] iv = fileHeader.getIV();
		//System.out.printf("IV %s\n", toString(iv));
		byte[] key = fileHeader.getHasher()
				.password(passPhrase.getBytes()).rawHash();
		//System.out.printf("Key, Pass = %s %s\n", toString(key), passPhrase );
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		
		int fileIndex = 0;
		FileGuidance targetGuidance = null;
		while (bis.available() > 0) {
			FileGuidance fileGuidance = new FileGuidance(bis);
			if (fileGuidance.decode(cipher, secretKey, iv)) {
				if (fileGuidance.isValid()) {
					targetGuidance = fileGuidance;
					break;
				}
			}
			fileIndex++;
		}
		if (targetGuidance == null || bis.available() == 0)
			return false;
		System.out.println(fileIndex + ": " + targetGuidance.toString());
		while (fileIndex < targetGuidance.getFileCount()) {
			new FileGuidance(bis);
			fileIndex++;
		}
		
		baseRandom.setSeed( targetGuidance.getSeed() );

		return readBlocks( baseRandom, targetGuidance.getFileCount(), targetGuidance.getLength(), targetGuidance.getFileOrdinal(), targetGuidance.getMaxBlocks(), bis, bos );
	}
	
	

	public boolean write( File outputFile, FileHeader fileHeader ) throws IOException {
		FileOutputStream fos = new FileOutputStream(outputFile);
		BufferedOutputStream bos = new BufferedOutputStream( fos );
		//System.out.printf("HEADER: (%d) %s\n", fileHeader.header.length, BaseEncoding.base16().lowerCase().encode(fileHeader.header));
		//System.out.println( fileHeader.toString() );
		bos.write( fileHeader.header );
		
		// deflate and encrypt all data files.
		// determine maximum size in blocks
		int maxBlocks = 0;
		for (BlockedFile file : dataFiles) {
			int nBlocks = file.deflate(-1);
			nBlocks = file.encrypt( fileHeader.getIV() );
			maxBlocks = Math.max(maxBlocks, nBlocks);
		}
		
		// pad all files to maximum size with random data
		for (BlockedFile file : dataFiles) {
			file.pad(maxBlocks);
		}
		
		// deflate and encrypt all filler files
		for (BlockedFile file : fillerFiles) {
			file.deflate(maxBlocks-1);
			file.encrypt(fileHeader.getIV());
			file.pad(maxBlocks);
		}
		
		// generate random files to bring filler count up to specified value
		if (fillerCount > fillerFiles.size()) {
			for (int i = fillerFiles.size(); i < fillerCount; i++) {
				byte[] key = new byte[hashLength];
				secureRandom.nextBytes(key);
				BlockedFile file = new BlockedFile( key, maxBlocks );
				file.deflate(maxBlocks);
				file.encrypt(fileHeader.getIV());
				fillerFiles.add(file);
			}
		}
		
		// combine data and filler files into a combined list
		ArrayList<BlockedFile> allFiles = new ArrayList<>();
		allFiles.addAll(dataFiles);
		allFiles.addAll(fillerFiles);
		//allFiles.forEach( System.out::println );
		
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
		for (int modulus : moduli) {
			BlockedFile file = allFiles.get(modulus);
			FileGuidance fileGuidance = new FileGuidance(maxBlocks, moduli.length, modulus, seed, file.length );
			System.out.printf("FILE %d: (%d) %s\n", modulus, fileGuidance.guidance.length, 
					BaseEncoding.base16().lowerCase().encode(fileGuidance.guidance));
			//System.out.println( fileGuidance.toString() );
			fileGuidance.encode( cipher, file.secretKey, fileHeader.getIV() );
			bos.write( fileGuidance.getCipherText() );
			//System.out.printf("      : %s\n", toString(fileGuidance.getCipherText()));
		}
		
		baseRandom.setSeed(seed);
		return writeBlocks( baseRandom, maxBlocks, allFiles, bos );
	}
	
	protected boolean writeBlocks( Random random, int maxBlocks, ArrayList<BlockedFile> allFiles, BufferedOutputStream bos ) throws IOException {
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
		
		for (int iBlock = 0; iBlock < maxBlocks; iBlock++) {
			permute( random, shuffled );
			for (Block.BlockListIterator it : shuffled) {
				Block block = it.next();
				//System.out.printf( "W%d %s\n", iBlock, block.toString() );
				bos.write( block.contents, 0, block.count );
			}
		}
		bos.close();
		return true;
	}

	protected class BlockReader {
		BufferedInputStream bis;
		long                remaining;
		Block               current;
		
		public BlockReader( BufferedInputStream bis, long length ) {
			this.bis = bis;
			this.remaining = length;
		}
		
		public Block read() throws IOException {
			current = new Block();
			long n = (remaining > Block.BLOCK_SIZE) ? Block.BLOCK_SIZE : remaining;
			current.count = bis.read( current.contents, 0, (int) n );
			if (current.count < 0)
				current = null;
			remaining -= n;
			return current;
		}
		
		public Block getLast() {
			return current;
		}
	}
	
	protected boolean readBlocks( Random random, int nFiles, long length, int fileModulus, int maxBlocks, BufferedInputStream bis, BufferedOutputStream bos ) throws IOException {
		ArrayList<BlockReader> readers = new ArrayList<>();
		for (int i = 0; i < nFiles+1; i++) {
			readers.add( new BlockReader(bis, length ) );
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
	
	
	public void report() {
		//System.out.println(BaseEncoding.base16().lowerCase().encode(salt));
		dataFiles.forEach(System.out::println);
		fillerFiles.forEach(System.out::println);
		System.out.printf("%d  %d\n", maxLength, fillerCount);
	}

	/**
	 * Encryption Approach Given data files D1, D2, ... Dn and asscoiated
	 * passphrases P1, P2, ... Pn, filler files F1, F2, .. Fm. Convert
	 * passphrases to keys K1, K2, ... Kn. XOR all keys and fold result to
	 * 48-bit integer S0. Seed standard Java Random with S0. Determine lengths
	 * of files and set L0 as max of set. Compute L1, L2, .. Ln as lengths of
	 * required padding for each file. Output file size will be [L0 +
	 * sizeof(L1)] * [n + m] Generate a random permutation of 1 .. [L0] O0
	 * Random keys R1, R2, ... Rm. For each data file Di, concatenate [Li,
	 * contents of Di, Li random bytes] and AES encrypt with Ki yielding Ci. For
	 * each filler file Fi, concatenate [Li, contents of Fi, Li random bytes]
	 * and AES encrypt with Ri yielding Ei. Concatenate all Ci and Ei. Permute
	 * IAW O0. Write permutation to output file.
	 * 
	 */

	/**
	 * FILE FORMAT
	 * 32*8/File Header
	 *  8/argon type,
	 *  8/argon version,
	 *  8/memory cost/1024,
	 *  8/time cost,
	 *  8/key size (bits)
	 *  2*8/0
	 *  24*8/IV
	 * [n+m] * 256/encoded-guidance [encoded with corresponding Ki or Ri keys]
	 *  32/maxBlocks (same for each key),
	 *  8/[n+m] (same for each key),
	 *  8/content modulus [0 .. n+m) (unique for each key),
	 *  48/permutation seed (same for each key)
	 *  64/file length,
	 *  64/random fill (varies for each key) 
	 *  32/CRC32 of preceeding
	 * [n+m]*L0 contents bytes in 1024 byte blocks ordered randomly per permutation
	 */
	
	protected static class FileHeader {
		
		public byte[] header = new byte[64];
		
		public FileHeader( Type type, Version version, int memoryCost, int timeCost, int keySize, byte[] salt) {
			secureRandom.nextBytes(header);
			header[0] = (byte) type.ordinal();
			header[1] = (byte) version.ordinal();
			header[2] = (byte) (memoryCost/1024);
			header[3] = (byte) timeCost;
			header[4] = (byte) (keySize / 8);
			for (int i = 0; i < salt.length; i++) {
				header[32+i] = salt[i];
			}
		}
		
		public FileHeader(BufferedInputStream bis) {
			try {
				bis.read(header);
				//System.out.println(IPMEC.toString(header));
			} catch (IOException e) {
				header = null;
			}
		}
		
		private final int parallelism = 4;
		
		public Hasher getHasher() {
			return 	com.kosprov.jargon2.api.Jargon2.jargon2Hasher().type(getType()).version(getVersion())
					.memoryCost(getMemoryCost()).timeCost(getTimeCost()).parallelism(parallelism)
					.hashLength(getKeySize()/8)
					.salt(getSalt());
		}
		
		public boolean isValid() {
			return header != null;
		}

		public Type getType() {
			return Type.values()[header[0]];
		}
		
		public Version getVersion() {
			return Version.values()[header[1]];
		}
		
		public int getMemoryCost() {
			return 1024 * header[2];
		}
		
		public int getTimeCost() {
			return header[3];
		}
		
		public int getKeySize() {
			return 8*header[4];
		}
		
		public byte[] getSalt() {
			int hashLength = getKeySize()/8;
			byte[] salt = new byte[hashLength];
			for (int i = 0; i < hashLength; i++) {
				salt[i] = header[32+i];
			}
			return salt;
		}
		
		public byte[] getIV() {
			return Arrays.copyOfRange(header, 32, 32+Wilkins.AES_IV_BYTES);
		}
		
		public String toString() {
			return String.format("%s %s %d %d %d %s", getType().toString(), getVersion().toString(), getMemoryCost(), getTimeCost(), getKeySize(),
					BaseEncoding.base16().lowerCase().encode(getIV()) );
					
		}
	}
	
	protected static class FileGuidance {
		
		public byte[] guidance = new byte[32];
		
		public FileGuidance(int maxBlocks, int nm, int modulus, long seed, long length ) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				bos.write( Ints.toByteArray(maxBlocks) ); // 0 .. 3  4 bytes 
				bos.write( (byte) nm );                   // 4       1 byte
				bos.write( (byte) modulus );              // 5       1 byte
				bos.write( Longs.toByteArray(seed) );     // 6 ..13  8 bytes
				bos.write( Longs.toByteArray(length) );   //14 ..21  8 bytes
				byte[] filler = new byte[6];              //22 ..27  6 bytes
				secureRandom.nextBytes(filler);
				bos.write( filler );
				guidance = bos.toByteArray();             // 28 bytes
				
				Checksum checksum = new CRC32();
				checksum.update( guidance, 0, guidance.length );
				byte[] crc = Longs.toByteArray( checksum.getValue() );
				bos.write(crc, 4, 4 );                    //28 ..31  4 bytes
				guidance = bos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				guidance = null;
			}
		}
		
		public FileGuidance(BufferedInputStream bis) {
			try {
				bis.read(guidance);
				//System.out.println(BaseEncoding.base16().lowerCase().encode(guidance));
			} catch (IOException e) {
				guidance = null;
			}
		}
		
		public boolean decode( Cipher cipher, SecretKey key, byte[] iv ) {
			//.out.println("DECODE: " + IPMEC.toString(key.getEncoded()));
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			try {
				//System.out.println( IPMEC.toString( guidance ) );
				cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
				byte[] cipherText = cipher.doFinal(guidance);
				//System.out.println( IPMEC.toString( cipherText ) );
				guidance = cipherText;
				return true;
			} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		public boolean encode( Cipher cipher, SecretKey key, byte[] iv) {
			//System.out.println("ENCODE: " + IPMEC.toString(key.getEncoded()));
			IvParameterSpec parameterSpec = new IvParameterSpec(iv);
			try {
				cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
				byte[] cipherText = cipher.doFinal(guidance);
				guidance = cipherText;
				return true;
			} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		public boolean isValid() {
			if (guidance == null)
				return false;
			byte[] content = Arrays.copyOf(guidance, 28);
			Checksum checksum = new CRC32();
			checksum.update( content, 0, content.length );
			byte[] crc = Longs.toByteArray( checksum.getValue() );
			crc = Arrays.copyOfRange(crc, 4, 8);
			return Arrays.equals(crc, Arrays.copyOfRange(guidance, 28, 32));
		}
		
		
		public byte[] getCipherText() {
			return guidance;
		}
		
		public byte[] getPlainText() {
			return guidance;
		}
		
		public int getMaxBlocks() {
			return Ints.fromByteArray( Arrays.copyOfRange(guidance, 0, 4));
		}
		
		public int getFileCount() {
			return guidance[4];
		}
		
		public int getFileOrdinal() {
			return guidance[5];
		}
		
		public Long getSeed() {
			return Longs.fromByteArray(Arrays.copyOfRange(guidance, 6, 14));
		}
		
		public long getLength() {
			return Ints.fromByteArray(Arrays.copyOfRange(guidance, 14, 22));
		}
		
		public String toString() {
			return String.format("%d %d %d %d %d %b", getMaxBlocks(), getFileCount(), getFileOrdinal(), getSeed(), getLength(), isValid() );
		}
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
	
	public static void test_writeReadBlocks( String[] args) {
		//	protected boolean writeBlocks( Random random, int maxBlocks, ArrayList<BlockedFile> allFiles, BufferedOutputStream bos ) throws IOException {
		Random random = new Random();
		random.setSeed(0L);
		int maxBlocks = 1;
		ArrayList<BlockedFile> allFiles = new ArrayList<>();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(out);
		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
		for (int i = 0; i < 3; i++) {
			BlockedFile file = new BlockedFile( new byte[32], 1 );
			Block block = file.blocks.getList().get(0);
			block.count = 16;
			System.out.printf( "I%d: %s\n", i, block.toString() );
			//Arrays.fill(block.contents, (byte) i);
			allFiles.add(file);
		}
		Wilkins ipmec = new Wilkins();
		try {
			ipmec.writeBlocks( random, maxBlocks, allFiles, bos );
			byte[] result = out.toByteArray();
			String block = BaseEncoding.base16().lowerCase().encode(result);
			for (int i = 0; i < block.length(); i+=16*2) {
				System.out.printf("D%2d: (%d) %s\n", i,  result.length, block.substring(i, i+16*2));
			}
			for (int iFile = 0; iFile < 3; iFile++) {
				random.setSeed(0L);
				ByteArrayInputStream inp = new ByteArrayInputStream( result );
				BufferedInputStream bis = new BufferedInputStream( inp );
				out = new ByteArrayOutputStream();
				bos = new BufferedOutputStream(out);
				ipmec.readBlocks(random, 3, 16, iFile, 1, bis, bos);
				byte[] output = out.toByteArray();
				block = BaseEncoding.base16().lowerCase().encode(output);
				for (int i = 0; i < block.length(); i+=16*2) {
					int n = Math.min( block.length(), i+16*2);
					System.out.printf("O%d: (%d) %s\n", iFile, output.length, block.substring(i, n));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void test_write(String[] args) {
		Wilkins ipmec = new Wilkins();
		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
		secureRandom.nextBytes(iv);

		FileHeader fileHeader = new FileHeader(ipmec.type, ipmec.version, ipmec.memoryCost, ipmec.timeCost, ipmec.keyLength, iv );

		ipmec.addDataFile("data1.txt", fileHeader, "key1");
		ipmec.addDataFile("data2.txt", fileHeader, "key2");
		ipmec.addFillerFile("filler1.txt");
		ipmec.setRandomFillerCount(3);
		try {
			File out = new File("output.ipmec");
			ipmec.write(out, fileHeader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ipmec.report();
	}
	
	public static void test_read(String[] args) {
		Wilkins ipmec = new Wilkins();
		try {
			File in = new File("output.ipmec");
			ipmec.read(in, System.out, "key1");
			ipmec.report();
		} catch (Exception x) {
			
		}
	}
	
	public static void test_fileGuidance( String[] args) {
		//FileGuidance(int maxBlocks, int nm, int modulus, long seed, long length )
		Wilkins ipmec = new Wilkins();
		FileGuidance g = new FileGuidance( 1, 2, 3, 4L, 5L );
		System.out.println(g.toString());
		System.out.println( toString(g.getPlainText() ) );
		byte[] key = new byte[ipmec.hashLength];
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[Wilkins.AES_IV_BYTES];
		g.encode(ipmec.cipher, secretKey, iv);
		System.out.println( toString( g.getCipherText()) );
		g.decode(ipmec.cipher, secretKey, iv);
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
