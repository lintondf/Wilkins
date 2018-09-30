/**
 * 
 */
package org.cryptonomicon;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;

/**
 * @author lintondf
 *
 */
public class Configuration {

	protected static class KeyDerivationParameters {
		
		protected int keySize;
		protected ArgonParameters argonParameters = null;
		protected BCryptParameters bCryptParameters = null;
		protected SCryptParameters sCryptParameters = null;
		
		/**
		 * @return the keySize
		 */
		public int getKeySize() {
			return keySize;
		}
	
		/**
		 * @return the argonParameters
		 */
		public ArgonParameters getArgonParameters() {
			return argonParameters;
		}
	
		/**
		 * @param argonParameters the argonParameters to set
		 */
		public void setArgonParameters(ArgonParameters argonParameters) {
			this.argonParameters = argonParameters;
		}
	
		/**
		 * @return the bCryptParameters
		 */
		public BCryptParameters getBCryptParameters() {
			return bCryptParameters;
		}
	
		/**
		 * @param bCryptParameters the bCryptParameters to set
		 */
		public void setBCryptParameters(BCryptParameters bCryptParameters) {
			this.bCryptParameters = bCryptParameters;
		}
	
		/**
		 * @return the sCryptParameters
		 */
		public SCryptParameters getSCryptParameters() {
			return sCryptParameters;
		}
	
		/**
		 * @param sCryptParameters the sCryptParameters to set
		 */
		public void setSCryptParameters(SCryptParameters sCryptParameters) {
			this.sCryptParameters = sCryptParameters;
		}
	
		public KeyDerivationParameters( int keySize ) {
			this.keySize = keySize;
		}
		
		public KeyDerivationParameters( InputStream bis ) throws IOException {
			byte[] array = new byte[4];
			bis.read(array);
			this.keySize = Ints.fromByteArray(array);
			this.argonParameters = new ArgonParameters(bis);
			this.bCryptParameters = new BCryptParameters(bis);
			this.sCryptParameters = new SCryptParameters(bis);
		}
		
		public void write( OutputStream bos ) throws IOException {
			bos.write( Ints.toByteArray(this.keySize));
			this.argonParameters.write(bos);
			this.bCryptParameters.write(bos);
			this.sCryptParameters.write(bos);
		}
		
		public static KeyDerivationParameters getDefaults() {
			KeyDerivationParameters parameters = new KeyDerivationParameters(256);
			parameters.setArgonParameters( ArgonParameters.getDefaults() );
			parameters.setBCryptParameters( BCryptParameters.getDefaults() );
			parameters.setSCryptParameters( SCryptParameters.getDefaults() );
			return parameters;
		}
		
		
		public static class ArgonParameters {
			
			protected Type type;
			protected Version version;
			protected int memoryCost;
			protected short timeCost;
			protected short parallelism;
			
			/**
			 * @return the type
			 */
			public Type getType() {
				return type;
			}
	
			/**
			 * @return the version
			 */
			public Version getVersion() {
				return version;
			}
	
			/**
			 * @return the memoryCost
			 */
			public int getMemoryCost() {
				return memoryCost;
			}
	
			/**
			 * @return the timeCost
			 */
			public int getTimeCost() {
				return timeCost;
			}
	
			/**
			 * @return the parallelism
			 */
			public int getParallelism() {
				return parallelism;
			}
	
			public ArgonParameters( Type type, Version version, int memoryCost, int timeCost, int parallelism ) {
				this.type = type;
				this.version = version;
				this.memoryCost = memoryCost;
				this.timeCost = (short) timeCost;
				this.parallelism = (short) parallelism;
			}
			
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
			
			public void write(OutputStream bos ) throws IOException {
				bos.write( (byte) type.ordinal());               
				bos.write( (byte) version.ordinal() );          
				bos.write( Ints.toByteArray( memoryCost ) ); 
				bos.write( Shorts.toByteArray( timeCost ) );  
				bos.write( Shorts.toByteArray( parallelism ) );		
			}
			
			public static ArgonParameters getDefaults() {
				return new ArgonParameters(Type.ARGON2id,
				Version.V13,
				16*1024,
				32,
				2 );
	
			}
		}
		
		public static class BCryptParameters {
			
			protected short rounds;
			
			/**
			 * @return the rounds
			 */
			public int getRounds() {
				return rounds;
			}
	
			public BCryptParameters(int rounds) {
				this.rounds = (short) rounds;
			}
			
			public BCryptParameters( InputStream bis ) throws IOException {
				byte[] array = new byte[2];
				bis.read(array);
				this.rounds = Shorts.fromByteArray(array);
			}
			
			public void write( OutputStream bos ) throws IOException {
				bos.write( Shorts.toByteArray(this.rounds));
			}
			
			public static BCryptParameters getDefaults() {
				return new BCryptParameters(14);
			}
	
		}
	
		public static class SCryptParameters {
			
			protected int N;
			protected short r;
			protected short p;
			
			/**
			 * @return the n
			 */
			public int getN() {
				return N;
			}
	
			/**
			 * @return the r
			 */
			public int getR() {
				return r;
			}
	
			/**
			 * @return the p
			 */
			public int getP() {
				return p;
			}
	
			public SCryptParameters(int N, int r, int p) {
				this.N = N;
				this.r = (short) r;
				this.p = (short) p;
			}
			
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
			
			public void write( OutputStream bos ) throws IOException {
				bos.write( Ints.toByteArray(this.N));
				bos.write( Shorts.toByteArray(this.r));
				bos.write( Shorts.toByteArray(this.p));
			}
			
			
			public static SCryptParameters getDefaults() {
				return new SCryptParameters( 32*1024, 4, 2 );
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			kdp.write(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println( bos.toByteArray().length );
		FileHeader fh = new FileHeader( kdp, new byte[32] );
	}

}
