package org.cryptonomicon.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.cryptonomicon.Wilkins;
import org.cryptonomicon.configuration.Configuration.ConfigurationError;
import org.cryptonomicon.configuration.Configuration.Parameter;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyDerivationParameters holds configuration data for passphrase 
 * to binary cryptographic key processing.  Information in objects of this 
 * class is stored to and loaded from the Haystack FileHeader.
 */
public class KeyDerivationParameters {
	
	protected Configuration configuration;
	
	/** The key size. */
	protected int keySize;
	
	/** PBKDF2 iterations. */
	protected int pbkdf2Iterations;
	
	/** The argon parameters. */
	protected KeyDerivationParameters.ArgonParameters argonParameters = null;
	
	/** The bcrypt parameters. */
	protected KeyDerivationParameters.BCryptParameters bCryptParameters = null;
	
	/** The scrypt parameters. */
	protected KeyDerivationParameters.SCryptParameters sCryptParameters = null;
	
	protected static final Configuration.Parameter DERIVATION_KEY_LENGTH = 
			new Parameter("derivation-key-length", "cryptographic key length in bits", 256, 128, 256 );
	
	protected static final Configuration.Parameter PBKDF2_ITERATIONS = 
			new Parameter("pbkdf2-iterations", "PBKDF2 iterations", 20_000, 10_000, 1_000_000 );

	protected static final Configuration.Parameter ARGON_MEMORY_COST  = 
			new Parameter("argon-memory-cost", "ARGON2 memory cost in KB", 16*1024, 1*1024, 1024*1024 );
	protected static final Configuration.Parameter ARGON_TIME_COST = 
			new Parameter("argon-timeCost", "Argon2 timeCost", 64, 0, 1024 ); 
	protected static final Configuration.Parameter ARGON_PARALLELISM = 
			new Parameter("argon-parallelism", "Argon2 parallelism", 4, 1, 128 );
	
	protected static final Configuration.Parameter BCRYPT_ROUNDS = 
			new Parameter("bcrypt-rounds", "BCRYPT rounds <integer>", 14, 4, 30 );
	
	protected static final Configuration.Parameter SCRYPT_N = 
			new Parameter("scrypt-n", "SCRYPT N <integer>", 32*1024, 0, 128*1024 );
	protected static final Configuration.Parameter SCRYPT_r = 
			new Parameter("scrypt-r", "SCRYPT r <integer>", 2, 0, 128 );
	protected static final Configuration.Parameter SCRYPT_p = 
			new Parameter("scrypt-p", "SCRYPT p <integer>", 16, 0, 64 );
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("KeyDerivationParameters.keysize = %d\n", keySize) );
		sb.append(String.format("KeyDerivationParameters.PBKDF2.iterations = %d\n", pbkdf2Iterations) );
		sb.append( this.argonParameters.toString() );
		sb.append( this.bCryptParameters.toString() );
		sb.append( this.sCryptParameters.toString() );
		return sb.toString();
	}
	
	
	public void addOptions(Options options) {
		options.addOption("k", DERIVATION_KEY_LENGTH.getOptionName(), true, DERIVATION_KEY_LENGTH.getHelpMessage());
		options.addOption(null, PBKDF2_ITERATIONS.getOptionName(), true, PBKDF2_ITERATIONS.getHelpMessage());
		this.argonParameters.addOptions(options);
		this.bCryptParameters.addOptions(options);
		this.sCryptParameters.addOptions(options);
	}
	
	public void set(CommandLine line) throws ConfigurationError {
		if (line.hasOption(DERIVATION_KEY_LENGTH.getOptionName())) {
			int keyLength = Configuration.optionAsInteger(line, DERIVATION_KEY_LENGTH);
			if (keyLength == DERIVATION_KEY_LENGTH.getMinValue() || keyLength == DERIVATION_KEY_LENGTH.getMaxValue()) {
				this.keySize = keyLength;
			} else {
				throw new Configuration.ConfigurationError(DERIVATION_KEY_LENGTH.getOptionName(), 
						DERIVATION_KEY_LENGTH.getOptionValue(line), "Key length must be 128 or 256");
			}
		}
		if (line.hasOption(PBKDF2_ITERATIONS.getOptionName())) {
			this.pbkdf2Iterations = Configuration.optionAsInteger( line, PBKDF2_ITERATIONS );
		}
		this.argonParameters.set(line);
		this.bCryptParameters.set(line);
		this.sCryptParameters.set(line);
	}

	/**
	 * Gets the key size.
	 *
	 * @return the keySize
	 */
	public int getKeySize() {
		return keySize;
	}

	/**
	 * @return the pbkdf2Iterations
	 */
	public int getPbkdf2Iterations() {
		return pbkdf2Iterations;
	}


	/**
	 * @param pbkdf2Iterations the pbkdf2Iterations to set
	 */
	public void setPbkdf2Iterations(int pbkdf2Iterations) {
		this.pbkdf2Iterations = pbkdf2Iterations;
	}

	/**
	 * Gets the argon parameters.
	 *
	 * @return the argonParameters
	 */
	public KeyDerivationParameters.ArgonParameters getArgonParameters() {
		return argonParameters;
	}

	/**
	 * Sets the argon parameters.
	 *
	 * @param argonParameters the argonParameters to set
	 */
	public void setArgonParameters(KeyDerivationParameters.ArgonParameters argonParameters) {
		this.argonParameters = argonParameters;
	}

	/**
	 * Gets the b crypt parameters.
	 *
	 * @return the bCryptParameters
	 */
	public KeyDerivationParameters.BCryptParameters getBCryptParameters() {
		return bCryptParameters;
	}

	/**
	 * Sets the b crypt parameters.
	 *
	 * @param bCryptParameters the bCryptParameters to set
	 */
	public void setBCryptParameters(KeyDerivationParameters.BCryptParameters bCryptParameters) {
		this.bCryptParameters = bCryptParameters;
	}

	/**
	 * Gets the s crypt parameters.
	 *
	 * @return the sCryptParameters
	 */
	public KeyDerivationParameters.SCryptParameters getSCryptParameters() {
		return sCryptParameters;
	}

	/**
	 * Sets the s crypt parameters.
	 *
	 * @param sCryptParameters the sCryptParameters to set
	 */
	public void setSCryptParameters(KeyDerivationParameters.SCryptParameters sCryptParameters) {
		this.sCryptParameters = sCryptParameters;
	}

	/**
	 * Instantiates a new key derivation parameters.
	 *
	 * @param keySize the key size
	 * @param iterations 
	 */
	public KeyDerivationParameters( Configuration configuration, int keySize, int iterations ) {
		this.configuration = configuration;
		this.keySize = keySize;
		this.pbkdf2Iterations = iterations;
		this.argonParameters = new ArgonParameters();
		this.bCryptParameters = new BCryptParameters();
		this.sCryptParameters = new SCryptParameters();
	}
	
	/**
	 * Instantiates a new key derivation parameters.
	 *
	 * @param bis the bis
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public KeyDerivationParameters( Configuration configuration, InputStream bis ) throws IOException {
		this.configuration = configuration;
		this.keySize = configuration.readMaskedInt(bis, DERIVATION_KEY_LENGTH);
		this.pbkdf2Iterations = configuration.readMaskedInt(bis, PBKDF2_ITERATIONS );
		this.argonParameters = new ArgonParameters(bis);
		this.bCryptParameters = new BCryptParameters(bis);
		this.sCryptParameters = new SCryptParameters(bis);
	}
	
	/**
	 * Write.
	 *
	 * @param bos the bos
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void write( OutputStream bos ) throws IOException {
		configuration.writeMaskedInt(bos, keySize, DERIVATION_KEY_LENGTH);
		configuration.writeMaskedInt(bos, pbkdf2Iterations, PBKDF2_ITERATIONS);
		this.argonParameters.write(bos);
		this.bCryptParameters.write(bos);
		this.sCryptParameters.write(bos);
	}
	
	/**
	 * Gets the defaults.
	 *
	 * @return the defaults
	 */
	public static KeyDerivationParameters getDefaults(Configuration configuration) {
		KeyDerivationParameters parameters = new KeyDerivationParameters(configuration, DERIVATION_KEY_LENGTH.getDefaultValue(), PBKDF2_ITERATIONS.getDefaultValue() );
		parameters.setArgonParameters( parameters.new ArgonParameters() );
		parameters.setBCryptParameters( parameters.new BCryptParameters() );
		parameters.setSCryptParameters( parameters.new SCryptParameters() );
		return parameters;
	}
	
	
	/**
	 * The Class ArgonParameters.
	 */
	public class ArgonParameters {
		
		/** The type. */
		protected Type type;
		
		/** The version. */
		protected Version version;
		
		/** The memory cost. */
		protected int memoryCost;
		
		/** The time cost. */
		protected short timeCost;
		
		/** The parallelism. */
		protected short parallelism;
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append( String.format("Argon2 type =  %s\n", type.toString() ));
			sb.append( String.format("Argon2 version =  %s\n", version.toString() ));
			sb.append( String.format("Argon2 memoryCost =  %d\n", memoryCost ));
			sb.append( String.format("Argon2 timeCost =  %d\n", timeCost ));
			sb.append( String.format("Argon2 parallelism =  %d\n", parallelism ));
			return sb.toString();
		}
		
		public Hasher getHasher( ByteArray salt) {
			return 	com.kosprov.jargon2.api.Jargon2.jargon2Hasher().type(getType()).version(getVersion())
					.memoryCost(getMemoryCost()).timeCost(getTimeCost()).parallelism(parallelism)
					.hashLength(getKeySize()/8)
					.salt(salt);
		}


		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public Type getType() {
			return type;
		}

		/**
		 * Gets the version.
		 *
		 * @return the version
		 */
		public Version getVersion() {
			return version;
		}

		/**
		 * Gets the memory cost.
		 *
		 * @return the memoryCost
		 */
		public int getMemoryCost() {
			return memoryCost;
		}

		/**
		 * Gets the time cost.
		 *
		 * @return the timeCost
		 */
		public int getTimeCost() {
			return timeCost;
		}

		/**
		 * Gets the parallelism.
		 *
		 * @return the parallelism
		 */
		public int getParallelism() {
			return parallelism;
		}

		/**
		 * Gets the defaults.
		 *
		 * @return the defaults
		 */
		public ArgonParameters() {
			this(Type.ARGON2id,
					Version.V13,
					ARGON_MEMORY_COST.getDefaultValue(),
					ARGON_TIME_COST.getDefaultValue(),
					ARGON_PARALLELISM.getDefaultValue() );
		}

		
		/**
		 * Instantiates a new argon parameters.
		 *
		 * @param type the type
		 * @param version the version
		 * @param memoryCost the memory cost
		 * @param timeCost the time cost
		 * @param parallelism the parallelism
		 */
		public ArgonParameters( Type type, Version version, int memoryCost, int timeCost, int parallelism ) {
			this.type = type;
			this.version = version;
			this.memoryCost = memoryCost;
			this.timeCost = (short) timeCost;
			this.parallelism = (short) parallelism;
		}
		
		/**
		 * Instantiates a new argon parameters.
		 *
		 * @param bis the bis
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public ArgonParameters( InputStream bis) throws IOException {
			type = Type.values()[ bis.read() ];
			version = Version.values()[ bis.read() ];
			memoryCost = configuration.readMaskedInt(bis, ARGON_MEMORY_COST );
			timeCost = configuration.readMaskedShort(bis, ARGON_TIME_COST );
			parallelism = configuration.readMaskedShort(bis, ARGON_PARALLELISM );
		}
		
		/**
		 * Write.
		 *
		 * @param bos the bos
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void write(OutputStream bos ) throws IOException {
			bos.write( (byte) type.ordinal());               
			bos.write( (byte) version.ordinal() );
			configuration.writeMaskedInt(bos, memoryCost, ARGON_MEMORY_COST );
			configuration.writeMaskedShort(bos, timeCost, ARGON_TIME_COST );
			configuration.writeMaskedShort(bos, parallelism, ARGON_PARALLELISM );
		}
		
		
		public static final String ARGON_TYPE = "argon-type";
		public static final String ARGON_VERSION = "argon-value";
		
		public void set(CommandLine line) throws ConfigurationError {
			if (line.hasOption(ARGON_TYPE)) {
				String value = line.getOptionValue(ARGON_TYPE);
				try {
					this.type = Type.valueOf(value);
				} catch (Exception x) {
					throw new ConfigurationError(ARGON_TYPE, value, "Argon2 type not recognized.");					
				}
			}
			if (line.hasOption(ARGON_VERSION)) {
				String value = line.getOptionValue(ARGON_VERSION);
				try {
					this.version = Version.valueOf(value);
				} catch (Exception x) {
					throw new ConfigurationError(ARGON_VERSION, value, "Argon2 version not recognized.");					
				}
			}
			if (line.hasOption(ARGON_MEMORY_COST.getOptionName())) {
				this.memoryCost = Configuration.optionAsInteger( line, ARGON_MEMORY_COST );
			}
			if (line.hasOption(ARGON_TIME_COST.getOptionName())) {
				this.timeCost = (short) Configuration.optionAsInteger( line, ARGON_TIME_COST );
			}
			if (line.hasOption(ARGON_PARALLELISM.getOptionName())) {
				this.parallelism = (short) Configuration.optionAsInteger( line, ARGON_PARALLELISM );
			}
		}
		
		public void addOptions(Options options) {
			options.addOption(null, ARGON_TYPE, true, "ARGON type [ARGON2d|ARGON2i|ARGON2id]; default ARGON2id");
			options.addOption(null, ARGON_VERSION, true, "ARGON version [V10|V13]; default V13");
			options.addOption(null, ARGON_MEMORY_COST.getOptionName(), true, ARGON_MEMORY_COST.getHelpMessage() );
			options.addOption(null, ARGON_TIME_COST.getOptionName(), true, ARGON_TIME_COST.getHelpMessage() );
			options.addOption(null, ARGON_PARALLELISM.getOptionName(), true, ARGON_PARALLELISM.getHelpMessage());
		}
	}
	
	/**
	 * The Class BCryptParameters.
	 */
	public class BCryptParameters {
		
		/** The rounds. */
		protected short rounds;
		
		/**
		 * Gets the rounds.
		 *
		 * @return the rounds
		 */
		public int getRounds() {
			return rounds;
		}

		/**
		 * Instantiates a new b crypt parameters.
		 *
		 * @param rounds the rounds
		 */
		public BCryptParameters(int rounds) {
			this.rounds = (short) rounds;
		}
		
		/**
		 * Instantiates a new b crypt parameters.
		 *
		 * @param bis the bis
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public BCryptParameters( InputStream bis ) throws IOException {
			this.rounds = configuration.readMaskedShort(bis, BCRYPT_ROUNDS);
		}
		
		/**
		 * Gets the defaults.
		 *
		 * @return the defaults
		 */
		public BCryptParameters() {
			this(14);
		}
		
		public String toString() {
			return String.format("BCrypt rounds = %d\n", rounds );
		}
		
		
		/**
		 * Write.
		 *
		 * @param bos the bos
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void write( OutputStream bos ) throws IOException {
			configuration.writeMaskedShort(bos, rounds, BCRYPT_ROUNDS);
		}
		

		public void set(CommandLine line) throws ConfigurationError {
			if (line.hasOption(BCRYPT_ROUNDS.getOptionName())) {
				this.rounds = (short) Configuration.optionAsInteger( line, BCRYPT_ROUNDS );
			}
		}

		public void addOptions(Options options) {
			options.addOption(null, BCRYPT_ROUNDS.getOptionName(), true, BCRYPT_ROUNDS.getHelpMessage() );
		}

	}

	/**
	 * The Class SCryptParameters.
	 */
	public class SCryptParameters {
		
		/** The n. */
		protected int N;
		
		/** The r. */
		protected short r;
		
		/** The p. */
		protected short p;
		
		/**
		 * Gets the n.
		 *
		 * @return the n
		 */
		public int getN() {
			return N;
		}

		/**
		 * Gets the r.
		 *
		 * @return the r
		 */
		public int getR() {
			return r;
		}

		/**
		 * Gets the p.
		 *
		 * @return the p
		 */
		public int getP() {
			return p;
		}

		/**
		 * Instantiates a new s crypt parameters.
		 *
		 * @param N the n
		 * @param r the r
		 * @param p the p
		 */
		public SCryptParameters(int N, int r, int p) {
			this.N = N;
			this.r = (short) r;
			this.p = (short) p;
		}
		
		/**
		 * Gets the defaults.
		 *
		 * @return the defaults
		 */
		public SCryptParameters() {
			this( SCRYPT_N.getDefaultValue(), SCRYPT_r.getDefaultValue(), SCRYPT_p.getDefaultValue() );
		}

		/**
		 * Instantiates a new s crypt parameters.
		 *
		 * @param bis the bis
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public SCryptParameters( InputStream bis ) throws IOException {
			this.N = configuration.readMaskedInt(bis, SCRYPT_N);
			this.r = configuration.readMaskedShort(bis, SCRYPT_r);
			this.p = configuration.readMaskedShort(bis, SCRYPT_p);
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append( String.format("SCrypt N = %d\n", N ) );
			sb.append( String.format("SCrypt r = %d\n", r ) );
			sb.append( String.format("SCrypt p = %d\n", p ) );
			return sb.toString();
		}

		
		/**
		 * Write.
		 *
		 * @param bos the bos
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void write( OutputStream bos ) throws IOException {
			configuration.writeMaskedInt(bos, this.N, SCRYPT_N);
			configuration.writeMaskedShort(bos, this.r, SCRYPT_r);
			configuration.writeMaskedShort(bos, this.p, SCRYPT_p);
		}
		
		public void set(CommandLine line) throws ConfigurationError {
			if (line.hasOption(SCRYPT_N.getOptionName())) {
				this.N = Configuration.optionAsInteger( line, SCRYPT_N );
			}
			if (line.hasOption(SCRYPT_r.getOptionName())) {
				this.r = (short) Configuration.optionAsInteger( line, SCRYPT_r);
			}
			if (line.hasOption(SCRYPT_p.getOptionName())) {
				this.p = (short) Configuration.optionAsInteger( line, SCRYPT_p );
			}
		}

		public void addOptions(Options options) {
			options.addOption(null, SCRYPT_N.getOptionName(), true, SCRYPT_N.getHelpMessage() );
			options.addOption(null, SCRYPT_r.getOptionName(), true, SCRYPT_r.getHelpMessage() );
			options.addOption(null, SCRYPT_p.getOptionName(), true, SCRYPT_p.getHelpMessage() );
		}
	}

}