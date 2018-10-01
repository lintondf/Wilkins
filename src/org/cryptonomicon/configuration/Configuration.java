/**
 * 
 */
package org.cryptonomicon.configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cryptonomicon.FileHeader;


// TODO: Auto-generated Javadoc
/**
 * The Class Configuration.
 *
 * @author lintondf
 */
public class Configuration {
	
	public static class ConfigurationError extends Exception {
		public ConfigurationError(String option, String value, String message ) {
			super(String.format("%s %s: %s", option, value, message ));
		}
		
		public ConfigurationError( Option option, String message) {
			this(option.getLongOpt(), option.getValue(), message);
		}
	}
	
	public Configuration() {
		derivationChainColor = DerivationChainColors.BLUE;
		keyDerivationParameters = KeyDerivationParameters.getDefaults();
	}
	
	public static final String KEY_DERIVATION_COLOR = "key-derivation-color";
	
	public void addOptions(Options options) {
		options.addOption("c", Configuration.KEY_DERIVATION_COLOR, true, "method used to convert passphrases to keys");
		this.keyDerivationParameters.addOptions(options);
	}

	public void set( CommandLine line ) throws ConfigurationError {
		if (line.hasOption(KEY_DERIVATION_COLOR)) {
			String value = line.getOptionValue(KEY_DERIVATION_COLOR).toUpperCase();
			try {
				this.derivationChainColor = DerivationChainColors.valueOf(value);
			} catch (IllegalArgumentException x) {
				throw new ConfigurationError(KEY_DERIVATION_COLOR, value, "Color must be from [BLACK,RED,ORANGE,YELLOW,GREEN,BLUE]");
			}
		}
		this.keyDerivationParameters.set( line );
	}
	
	/**
	 * The Enum DerivationChainColors.
	 */
	public enum DerivationChainColors {
		BLACK,   //Bcrypt
		RED,     //Scrypt
		ORANGE,  //Argon2
		YELLOW,  //Argon2+Bcrypt
		GREEN,   //Argon2+Scrypt
		BLUE,    //Argon2+Bcrypt+Scrypt
	};
	
	// what chain of passphrase to key derivation methods to use
	protected DerivationChainColors  derivationChainColor;

	/**
	 * @return the derivationChainColor
	 */
	public DerivationChainColors getDerivationChainColor() {
		return derivationChainColor;
	}

	/**
	 * @param derivationChainColor the derivationChainColor to set
	 */
	public void setDerivationChainColor(DerivationChainColors derivationChainColor) {
		this.derivationChainColor = derivationChainColor;
	}
	
	protected KeyDerivationParameters keyDerivationParameters;

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

	/**
	 * The main method.
	 *
	 * @param args the arguments
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
