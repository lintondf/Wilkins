/**
 * 
 */
package org.cryptonomicon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.mindrot.BCrypt;

import com.kosprov.jargon2.api.Jargon2;
import com.lambdaworks.crypto.SCrypt;
import com.lambdaworks.crypto.SCryptUtil;

import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.resources.Feedback;
import me.gosimple.nbvcxz.resources.Generator;
import me.gosimple.nbvcxz.scoring.Result;
import me.gosimple.nbvcxz.scoring.TimeEstimate;

/**
 * @author lintondf
 *
 */
public class PassPhraseStrength {
	
	Nbvcxz checker = new Nbvcxz();
	
	public PassPhraseStrength() {
		Result result = checker.estimate("reclusive-divorcee-lands");
		System.out.println( report(result) );
		for (int i = 0; i < 10; i++) {
			String pass1 = Generator.generatePassphrase("-", 4);
			Result result1 = checker.estimate(pass1);
			System.out.printf("%10.3f %s\n", result1.getEntropy(), pass1 );
		}
	}
	
	public String report(Result result) {
		// Get formatted values for time to crack based on the values we 
		// input in our configuration (we used default values in this example)
		String timeToCrackOn = TimeEstimate.getTimeToCrackFormatted(result, "OFFLINE_BCRYPT_14");
		
		BigDecimal crackTimeSecs = TimeEstimate.getTimeToCrack(result, "OFFLINE_BCRYPT_14");
		crackTimeSecs = crackTimeSecs.divide( new BigDecimal( 1e6 / 20e3 ), RoundingMode.CEILING);
		BigDecimal crackTimeYears = crackTimeSecs.divide( new BigDecimal( 364.25 * 86400.0 ), RoundingMode.CEILING );
		System.out.println( crackTimeYears.doubleValue());
		System.out.println( result.getEntropy() );
		
		// Check if the password met the minimum set within the configuration
		if(result.isMinimumEntropyMet())
		{
		    // Start building success message
		    StringBuilder successMessage = new StringBuilder();
		    successMessage.append("Password has met the minimum strength requirements.");
		    successMessage.append("<br>Time to crack - BCrypt(14): ").append(timeToCrackOn);
		    
		    // Example "success message" that would be displayed to the user
		    // This is obviously just a contrived example and would have to
		    // be tailored to each front-end
		    return (successMessage.toString());
		}
		else
		{
		    // Get the feedback for the result
		    // This contains hints for the user on how to improve their password
		    // It is localized based on locale set in configuration
		    Feedback feedback = result.getFeedback();
		    
		    // Start building error message
		    StringBuilder errorMessage = new StringBuilder();
		    errorMessage.append("Password does not meet the minimum strength requirements.");
		    errorMessage.append("<br>Time to crack - BCrypt(14): ").append(timeToCrackOn);
		    
		    if(feedback != null)
		    {
		        if (feedback.getWarning() != null)
		            errorMessage.append("<br>Warning: ").append(feedback.getWarning());
		        for (String suggestion : feedback.getSuggestion())
		        {
		            errorMessage.append("<br>Suggestion: ").append(suggestion);
		        }
		    }
		    // Example "error message" that would be displayed to the user
		    // This is obviously just a contrived example and would have to
		    // be tailored to each front-end
		    return (errorMessage.toString());
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PassPhraseStrength passPhraseStrength = new PassPhraseStrength();
	}

}
