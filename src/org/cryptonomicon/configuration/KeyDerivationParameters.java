package org.cryptonomicon.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.cryptonomicon.configuration.Configuration.ConfigurationError;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyDerivationParameters holds configuration data for passphrase 
 * to binary cryptographic key processing.  Information in objects of this 
 * class is stored to and loaded from the Haystack FileHeader.
 */
public class KeyDerivationParameters {
	
	/** The key size. */
	protected int keySize;
	
	/** The argon parameters. */
	protected KeyDerivationParameters.ArgonParameters argonParameters = null;
	
	/** The bcrypt parameters. */
	protected KeyDerivationParameters.BCryptParameters bCryptParameters = null;
	
	/** The scrypt parameters. */
	protected KeyDerivationParameters.SCryptParameters sCryptParameters = null;
	
	public static final String DERIVATION_KEY_LENGTH = "derivation-key-length";
	
	public void addOptions(Options options) {
		options.addOption("k", KeyDerivationParameters.DERIVATION_KEY_LENGTH, true, "cryptographic key length in bits");
		this.argonParameters.addOptions(options);
		this.bCryptParameters.addOptions(options);
		this.sCryptParameters.addOptions(options);
	}
	
	public void set(CommandLine line) throws ConfigurationError {
		if (line.hasOption(DERIVATION_KEY_LENGTH)) {
			String value = line.getOptionValue(DERIVATION_KEY_LENGTH);
			int keyLength = Integer.parseInt( value );
			if (keyLength == 128 || keyLength == 256) {
				this.keySize = keyLength;
			} else {
				throw new Configuration.ConfigurationError(DERIVATION_KEY_LENGTH, value, "Key length must be 128 or 256");
			}
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
	 */
	public KeyDerivationParameters( int keySize ) {
		this.keySize = keySize;
	}
	
	/**
	 * Instantiates a new key derivation parameters.
	 *
	 * @param bis the bis
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public KeyDerivationParameters( InputStream bis ) throws IOException {
		byte[] array = new byte[4];
		bis.read(array);
		this.keySize = Ints.fromByteArray(array);
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
		bos.write( Ints.toByteArray(this.keySize));
		this.argonParameters.write(bos);
		this.bCryptParameters.write(bos);
		this.sCryptParameters.write(bos);
	}
	
	/**
	 * Gets the defaults.
	 *
	 * @return the defaults
	 */
	public static KeyDerivationParameters getDefaults() {
		KeyDerivationParameters parameters = new KeyDerivationParameters(256);
		parameters.setArgonParameters( ArgonParameters.getDefaults() );
		parameters.setBCryptParameters( BCryptParameters.getDefaults() );
		parameters.setSCryptParameters( SCryptParameters.getDefaults() );
		return parameters;
	}
	
	
	/**
	 * The Class ArgonParameters.
	 */
	public static class ArgonParameters {
		
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
			byte[] input = new byte[4];
			bis.read(input);
			memoryCost = Ints.fromByteArray(input);
			bis.read(input);
			timeCost = Shorts.fromByteArray(input);
			bis.read(input);
			parallelism = Shorts.fromByteArray(input);
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
			bos.write( Ints.toByteArray( memoryCost ) ); 
			bos.write( Shorts.toByteArray( timeCost ) );  
			bos.write( Shorts.toByteArray( parallelism ) );		
		}
		
		/**
		 * Gets the defaults.
		 *
		 * @return the defaults
		 */
		public static KeyDerivationParameters.ArgonParameters getDefaults() {
			return new ArgonParameters(
					Type.ARGON2id,
					Version.V13,
					DEFAULT_ARGON_MEMORY_COST,
					DEFAULT_ARGON_TIME_COST,
					DEFAULT_ARGON_PARALLELISM );

		}
		
		public static final int DEFAULT_ARGON_MEMORY_COST = 16*1024;
		public static final int DEFAULT_ARGON_TIME_COST = 32;
		public static final int DEFAULT_ARGON_PARALLELISM = 2;
		
		public static final String ARGON_TYPE = "argon-type";
		public static final String ARGON_VERSION = "argon-value";
		public static final String ARGON_MEMORY_COST = "argon-memoryCost";
		public static final String ARGON_TIME_COST = "argon-timeCost";
		public static final String ARGON_PARALLELISM = "argon-parallelism";
		
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
			if (line.hasOption(ARGON_MEMORY_COST)) {
				String value = line.getOptionValue(ARGON_MEMORY_COST);
				int cost = Integer.parseInt( value );
				if (cost <= 0 || cost >= 1024*1024) {
					this.memoryCost = cost;
				} else {
					throw new Configuration.ConfigurationError(ARGON_MEMORY_COST, value, "memory cost in KB must be > 0 and < 1024^2");
				}
			}
			if (line.hasOption(ARGON_TIME_COST)) {
				String value = line.getOptionValue(ARGON_TIME_COST);
				int cost = Integer.parseInt( value );
				if (cost <= 0 || cost >= 1024) {
					this.timeCost = (short) cost;
				} else {
					throw new Configuration.ConfigurationError(ARGON_TIME_COST, value, "time cost must be > 0 and < 1024");
				}
			}
			if (line.hasOption(ARGON_PARALLELISM)) {
				String value = line.getOptionValue(ARGON_PARALLELISM);
				int cost = Integer.parseInt( value );
				if (cost <= 0 || cost >= 128) {
					this.parallelism = (short) cost;
				} else {
					throw new Configuration.ConfigurationError(ARGON_PARALLELISM, value, "parallelism must be > 0 and < 128");
				}
			}
		}
		
		public void addOptions(Options options) {
			options.addOption(null, ARGON_TYPE, true, "ARGON type [ARGON2d|ARGON2i|ARGON2id]; default ARGON2id");
			options.addOption(null, ARGON_VERSION, true, "ARGON version [V10|V13]; default V13");
			options.addOption(null, ARGON_MEMORY_COST, true, "ARGON memory cost <integer>; default " + DEFAULT_ARGON_MEMORY_COST);
			options.addOption(null, ARGON_TIME_COST, true, "ARGON time cost <integer>; default " +  DEFAULT_ARGON_TIME_COST );
			options.addOption(null, ARGON_PARALLELISM, true, "ARGON parallelism <integer>; default " + DEFAULT_ARGON_PARALLELISM);
		}
	}
	
	/**
	 * The Class BCryptParameters.
	 */
	public static class BCryptParameters {
		
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
			byte[] array = new byte[2];
			bis.read(array);
			this.rounds = Shorts.fromByteArray(array);
		}
		
		/**
		 * Write.
		 *
		 * @param bos the bos
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void write( OutputStream bos ) throws IOException {
			bos.write( Shorts.toByteArray(this.rounds));
		}
		
		/**
		 * Gets the defaults.
		 *
		 * @return the defaults
		 */
		public static KeyDerivationParameters.BCryptParameters getDefaults() {
			return new BCryptParameters(14);
		}

		public void set(CommandLine line) {
			// TODO Auto-generated method stub
			
		}

		public void addOptions(Options options) {
			// TODO Auto-generated method stub
			
		}

	}

	/**
	 * The Class SCryptParameters.
	 */
	public static class SCryptParameters {
		
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
		 * Instantiates a new s crypt parameters.
		 *
		 * @param bis the bis
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public SCryptParameters( InputStream bis ) throws IOException {
			byte[] array = new byte[4];
			bis.read(array);
			this.N = Ints.fromByteArray(array);
			array = new byte[2];
			bis.read(array);
			this.r = Shorts.fromByteArray(array);
			bis.read(array);
			this.p = Shorts.fromByteArray(array);
		}
		
		/**
		 * Write.
		 *
		 * @param bos the bos
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void write( OutputStream bos ) throws IOException {
			bos.write( Ints.toByteArray(this.N));
			bos.write( Shorts.toByteArray(this.r));
			bos.write( Shorts.toByteArray(this.p));
		}
		
		
		/**
		 * Gets the defaults.
		 *
		 * @return the defaults
		 */
		public static KeyDerivationParameters.SCryptParameters getDefaults() {
			return new SCryptParameters( 32*1024, 4, 2 );
		}

		public void set(CommandLine line) {
			// TODO Auto-generated method stub
			
		}

		public void addOptions(Options options) {
			// TODO Auto-generated method stub
			
		}
	}

}