/**
 * 
 */
package org.cryptonomicon.configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cryptonomicon.FileHeader;
import org.cryptonomicon.Wilkins;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.Type;


// TODO: Auto-generated Javadoc
/**
 * The Class Configuration.
 *
 * @author lintondf
 */
public class Configuration {

	protected SecureRandom random = new SecureRandom();

	
	public static class ConfigurationError extends Exception {
		public ConfigurationError(String option, String value, String message ) {
			super(String.format("%s %s: %s", option, value, message ));
		}
		
		public ConfigurationError( Option option, String message) {
			this(option.getLongOpt(), option.getValue(), message);
		}
	}
	
	public static class Parameter {
		
		String optionName;
		String baseMessage;
		int defaultValue;
		int minValue;
		int maxValue;
		
		public Parameter( String optionName, String baseMessage, int defaultValue, int minValue, int maxValue) {
			this.optionName = optionName;
			this.baseMessage = baseMessage;
			this.defaultValue = defaultValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		public String getHelpMessage() {
			return String.format( "%s; default %d; range %d..%d", baseMessage, defaultValue, minValue, maxValue );
		}

		/**
		 * @return the optionName
		 */
		public String getOptionName() {
			return optionName;
		}

		/**
		 * @return the parameterName
		 */
		public String getBaseMessage() {
			return baseMessage;
		}

		/**
		 * @return the defaultValue
		 */
		public int getDefaultValue() {
			return defaultValue;
		}

		/**
		 * @return the minValue
		 */
		public int getMinValue() {
			return minValue;
		}

		/**
		 * @return the maxValue
		 */
		public int getMaxValue() {
			return maxValue;
		}

		public String getOptionValue(CommandLine line) {
			return line.getOptionValue(getOptionName());
		}
	}
	
	public Configuration() {
		keyDerivationParameters = KeyDerivationParameters.getDefaults(this);
	}
	
	public void addOptions(Options options) {
		this.keyDerivationParameters.addOptions(options);
	}

	public void set( CommandLine line ) throws ConfigurationError {
		this.keyDerivationParameters.set( line );
	}
	
	protected KeyDerivationParameters keyDerivationParameters;
	public static final int AES_IV_BYTES = 128/8;

	/**
	 * @return the keyDerivationParameters
	 */
	public KeyDerivationParameters getKeyDerivationParameters() {
		return keyDerivationParameters;
	}

	/**
	 * @param keyDerivationParameters the keyDerivationParameters to set
	 */
	public void setKeyDerivationParameters(
			KeyDerivationParameters keyDerivationParameters) {
		this.keyDerivationParameters = keyDerivationParameters;
	}
	
	public static int optionAsInteger( CommandLine line, String optionName, int floor, int ceiling, String parameterName ) throws ConfigurationError {
		String value = line.getOptionValue(optionName);
		int cost = Integer.parseInt( value );
		if (cost <= 0 || cost >= 128) {
			return cost;
		} else {
			String message = String.format("%s must be > %d and < %d", parameterName, floor, ceiling );
			throw new ConfigurationError(optionName, value, message);
		}
	}
	
	public static int optionAsInteger( CommandLine line, Parameter parameter ) throws ConfigurationError {
		return optionAsInteger( line, parameter.getOptionName(), parameter.getMinValue(), parameter.getMaxValue(), parameter.getBaseMessage() );
	}
	
	public static String optionAsString( CommandLine line, String optionName, String parameterName ) throws ConfigurationError {
		String value = line.getOptionValue(optionName);
		if (value != null && !value.isEmpty()) {
			return value;
		} else {
			String message = String.format("%s must provided", parameterName );
			throw new ConfigurationError(optionName, value, message);
		}
	}
	
	
	public int maskInt( int value, int min, int max) {
		int n = 1 + max - min;
		int modulus = (n == 1) ? 1 : Integer.highestOneBit(n - 1) * 2;
		int adder = modulus*random.nextInt( Integer.MAX_VALUE/(modulus*2) );
		return (value - min) + adder;
	}
	
	public int maskInt(int value, Parameter parameter) {
		return maskInt( value, parameter.getMinValue(), parameter.getMaxValue() );
	}
	
	public int maskShort( short value, int min, int max) {
		int n = 1 + max - min;
		int modulus = (n == 1) ? 1 : Integer.highestOneBit(n - 1) * 2;
		int adder = modulus*random.nextInt( Short.MAX_VALUE/(modulus*2) );
		return (value - min) + adder;
	}
	
	public int maskShort(short value, Parameter parameter) {
		return maskShort( value, parameter.getMinValue(), parameter.getMaxValue() );
	}
	
	public void writeMaskedInt( OutputStream bos, int value, Parameter parameter ) throws IOException  {
		int masked = maskInt( value, parameter );
		//System.out.printf("writeMaskedInt %d %d %s\n", value, masked, Wilkins.toString(Ints.toByteArray(masked)));
		bos.write( Ints.toByteArray(masked) );
	}

	public void writeMaskedShort( OutputStream bos, short value, Parameter parameter ) throws IOException  {
		int masked = maskShort( value, parameter );
		//System.out.printf("writeMaskedShort %d %d %s\n", value, masked, Wilkins.toString(Shorts.toByteArray((short) masked)));
		bos.write( Shorts.toByteArray((short) masked) );
	}

	public int unmaskInt( int masked, int min, int max ) {
		int n = 1 + max - min;
		int modulus = (n == 1) ? 1 : Integer.highestOneBit(n - 1) * 2;
		return min + (masked % modulus);
	}
	
	public int unmaskInt(int masked, Parameter parameter) {
		return unmaskInt( masked, parameter.getMinValue(), parameter.getMaxValue() );
	}
	
	public short unmaskShort( short masked, int min, int max ) {
		int n = 1 + max - min;
		int modulus = (n == 1) ? 1 : Integer.highestOneBit(n - 1) * 2;
		return (short) (min + (masked % modulus));
	}
	
	public short unmaskShort(short masked, Parameter parameter) {
		return unmaskShort( masked, parameter.getMinValue(), parameter.getMaxValue() );
	}
	
	public int readMaskedInt( InputStream bis, Parameter parameter ) throws IOException {
		byte[] array = new byte[4];
		bis.read(array);
		int masked = Ints.fromByteArray(array);
		//System.out.printf("readMaskedInt %s %d %d\n", Wilkins.toString(array), masked, unmaskInt( masked, parameter ));
		return unmaskInt( masked, parameter );
	}

	public short readMaskedShort( InputStream bis, Parameter parameter ) throws IOException {
		byte[] array = new byte[2];
		bis.read(array);
		short masked = Shorts.fromByteArray(array);
		//System.out.printf("readMaskedShort %s %d %d\n", Wilkins.toString(array), masked, unmaskShort( masked, parameter ));
		return unmaskShort( masked, parameter );
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		
//		for (int i = 128; i <= 256; i += 8) {
//			int masked = configuration.maskInt(i, 128, 256);
//			System.out.printf( "%5d %15d %5d\n", i, masked, configuration.unmaskInt(masked, 128, 256 ) );
//		}

		KeyDerivationParameters kdp = KeyDerivationParameters.getDefaults(configuration);
		String r1 = kdp.toString();
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		try {
//			kdp.write(bos);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println( bos.toByteArray().length );
		byte[] iv = new byte[32];
		configuration.random.nextBytes(iv);
		FileHeader fh = new FileHeader( kdp, Jargon2.toByteArray(iv) );
		System.out.println( Wilkins.toString( fh.getPlainText() ) );
		KeyDerivationParameters kdp2 = fh.getKeyDerivationParameters();
		String r2 = kdp2.toString();
		System.out.println( r1.equals(r2));
	}

}
