/**
 * 
 */
package org.cryptonomicon;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.cryptonomicon.configuration.Configuration;
import org.cryptonomicon.configuration.KeyDerivationParameters;
import org.mindrot.BCrypt;

import com.google.common.primitives.Longs;
import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.ByteArray;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.lambdaworks.crypto.SCrypt;

public class KeyDerivation {

	protected Configuration configuration;

	static final String algorithm = "PBKDF2WithHmacSHA1";
	protected SecretKeyFactory factory = null;

	protected ArrayList<DerivationStep> derivationSteps = new ArrayList<>();

	public KeyDerivation(Configuration configuration) {
		this.configuration = configuration;
		try {
			factory = SecretKeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	protected abstract class DerivationStep {

		protected int derivedKeyLength;

		public DerivationStep(int derivedKeyLength) {
			this.derivedKeyLength = derivedKeyLength;
		}

		public abstract ByteArray derive(ByteArray password, ByteArray salt) throws GeneralSecurityException ;
	}

	protected class ArgonDerivationStep extends DerivationStep {

		protected Hasher hasher;

		public ArgonDerivationStep(Hasher hasher, int derivedKeyLength) {
			super(derivedKeyLength);
			this.hasher = hasher;
		}

		@Override
		public ByteArray derive(ByteArray password, ByteArray salt) {
			return Jargon2.toByteArray(hasher.password(password).salt(salt)
					.rawHash());
		}
	}

	protected class BCryptDerivationStep extends DerivationStep {

		protected BCrypt bCrypt = new BCrypt();
		protected int rounds;

		public BCryptDerivationStep(int derivedKeyLength, int rounds) {
			super(derivedKeyLength);
			this.rounds = rounds;
		}

		@Override
		public ByteArray derive(ByteArray password, ByteArray salt) {
			byte[] bsalt = Arrays.copyOf(salt.getBytes(), BCrypt.BCRYPT_SALT_LEN);
			return Jargon2.toByteArray(bCrypt.crypt_raw(password.getBytes(),
					bsalt, rounds, BCrypt.getMagicNumbers()));
		}
	}

	protected class SCryptDerivationStep extends DerivationStep {
		protected int N = 16 * 1024;
		protected int r = 8;
		protected int p = 2;

		public SCryptDerivationStep(int derivedKeyLength, int N, int r, int p) {
			super(derivedKeyLength);
			this.N = N;
			this.r = r;
			this.p = p;
		}

		@Override
		public ByteArray derive(ByteArray password, ByteArray salt) throws GeneralSecurityException {
			return Jargon2.toByteArray(SCrypt.scryptJ(password.getBytes(),
					salt.getBytes(), N, r, p, derivedKeyLength / 8));
		}
	}

	protected class Pbkdp2DerivationStep extends DerivationStep {

		protected int iterations = 20000;

		public Pbkdp2DerivationStep(int derivedKeyLength, int iterations) {
			super(derivedKeyLength);
			this.derivedKeyLength = derivedKeyLength;
			this.iterations = iterations;
		}

		@Override
		public ByteArray derive(ByteArray password, ByteArray salt) throws GeneralSecurityException
				{
			char[] passwordAsChar = new char[password.getBytes().length];
			for (int i = 0; i < passwordAsChar.length; i++)
				passwordAsChar[i] = (char) password.getBytes()[i];
			final PBEKeySpec cipherSpec = new PBEKeySpec(passwordAsChar,
					salt.getBytes(), iterations, derivedKeyLength);
			Arrays.fill(passwordAsChar, ' ');
			SecretKey cipherKey = factory.generateSecret(cipherSpec);
			cipherSpec.clearPassword();
			return Jargon2.toByteArray(cipherKey.getEncoded());
		}
	}
	
	protected Pbkdp2DerivationStep  loadDerivationSteps(ByteArray salt) {
		derivationSteps.clear();
		KeyDerivationParameters kdp = configuration
				.getKeyDerivationParameters();

		Pbkdp2DerivationStep pbkdp2 = new Pbkdp2DerivationStep(
				kdp.getKeySize(), kdp.getPbkdf2Iterations());
		ArgonDerivationStep argon = new ArgonDerivationStep(kdp
				.getArgonParameters().getHasher(salt), kdp.getKeySize());
		BCryptDerivationStep bcrypt = new BCryptDerivationStep(
				kdp.getKeySize(), kdp.getBCryptParameters().getRounds());
		SCryptDerivationStep scrypt = new SCryptDerivationStep(
				kdp.getKeySize(), kdp.getSCryptParameters().getN(), kdp
						.getSCryptParameters().getR(), kdp
						.getSCryptParameters().getP());

		derivationSteps.add(pbkdp2);
		derivationSteps.add(argon);
		derivationSteps.add(bcrypt);
		derivationSteps.add(scrypt);	
		return pbkdp2;
	}
	
	protected void permuteDerivationSteps( Pbkdp2DerivationStep pbkdp2, ByteArray password, ByteArray salt ) throws GeneralSecurityException {
		long permutationSeed = 0L;

		ByteArray permutationSeedArray = pbkdp2.derive(password, salt);
		permutationSeed = Longs.fromByteArray(Arrays.copyOf(
				permutationSeedArray.getBytes(), 8));
		permutationSeed &= (1L << 48) - 1;

		Random random = new Random(permutationSeed);
		Util.permute(random, derivationSteps);
	}

	public ByteArray deriveKey(ByteArray password, ByteArray salt)
			throws GeneralSecurityException {
		if (factory == null)
			throw new GeneralSecurityException(
					"SecretKeyFactory not initialized");
		
		Pbkdp2DerivationStep pbkdp2 = loadDerivationSteps( salt );

		ByteArray value = password;
		
		permuteDerivationSteps( pbkdp2, value, salt );

		for (DerivationStep step : derivationSteps) {
			value = step.derive(value, salt);
		}
		return value.finalizable();
	}

}
