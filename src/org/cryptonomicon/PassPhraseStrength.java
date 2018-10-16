/**
 * 
 */
package org.cryptonomicon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

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
		loadExamples();
	}

	public void suggest(int count, int words, boolean evaluate) {
		for (int i = 0; i < count; i++) {
			String pass1 = Generator.generatePassphrase("-", words);
			System.out.printf("%s\n", pass1);
			if (evaluate) {
				System.out.println( evaluate(pass1) );
			}
		}
	}
	
	public String evaluate( String pass ) {
		Result result = checker.estimate(pass);
		double entropy = result.getEntropy();
		int index = Math.min( 100, Math.max(1, (int) entropy ) );
		return String.format( "  Entropy: %5.1f bits; roughly as good as: %s", entropy, examples.get(index));
	}

	public String report(Result result) {
		// Get formatted values for time to crack based on the values we
		// input in our configuration (we used default values in this example)
		String timeToCrackOn = TimeEstimate.getTimeToCrackFormatted(result, "OFFLINE_BCRYPT_14");

		BigDecimal crackTimeSecs = TimeEstimate.getTimeToCrack(result, "OFFLINE_BCRYPT_14");
		crackTimeSecs = crackTimeSecs.divide(new BigDecimal(1e6 / 20e3), RoundingMode.CEILING);
		BigDecimal crackTimeYears = crackTimeSecs.divide(new BigDecimal(364.25 * 86400.0), RoundingMode.CEILING);
		System.out.println(crackTimeYears.doubleValue());
		System.out.println(result.getEntropy());

		// Check if the password met the minimum set within the configuration
		if (result.isMinimumEntropyMet()) {
			// Start building success message
			StringBuilder successMessage = new StringBuilder();
			successMessage.append("Password has met the minimum strength requirements.");
			successMessage.append("<br>Time to crack - BCrypt(14): ").append(timeToCrackOn);

			// Example "success message" that would be displayed to the user
			// This is obviously just a contrived example and would have to
			// be tailored to each front-end
			return (successMessage.toString());
		} else {
			// Get the feedback for the result
			// This contains hints for the user on how to improve their password
			// It is localized based on locale set in configuration
			Feedback feedback = result.getFeedback();

			// Start building error message
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Password does not meet the minimum strength requirements.");
			errorMessage.append("<br>Time to crack - BCrypt(14): ").append(timeToCrackOn);

			if (feedback != null) {
				if (feedback.getWarning() != null)
					errorMessage.append("<br>Warning: ").append(feedback.getWarning());
				for (String suggestion : feedback.getSuggestion()) {
					errorMessage.append("<br>Suggestion: ").append(suggestion);
				}
			}
			// Example "error message" that would be displayed to the user
			// This is obviously just a contrived example and would have to
			// be tailored to each front-end
			return (errorMessage.toString());
		}
	}
	
	protected void loadExamples() {
		for (int i = 0; i <= 100; i++) {
			scores.add(null);
			examples.add(null);
		}
		int i = 1;
		scores.set(i,1.); examples.set(i,"password"); i++;
		scores.set(i,2.); examples.set(i,"qwerty"); i++;
		scores.set(i,3.); examples.set(i,"Qwerty"); i++;
		scores.set(i,4.); examples.set(i,"letmein"); i++;
		scores.set(i,5.); examples.set(i,"Letmein"); i++;
		scores.set(i,5.6); examples.set(i,"iloveyou"); i++;
		scores.set(i,7.2); examples.set(i,"welcome"); i++;
		scores.set(i,8.2); examples.set(i,"Welcome"); i++;
		scores.set(i,8.9); examples.set(i,"login"); i++;
		scores.set(i,10.); examples.set(i,"admin"); i++;
		scores.set(i,11.5); examples.set(i,"NElL"); i++;
		scores.set(i,12.7); examples.set(i,"hS3"); i++;
		scores.set(i,13.8); examples.set(i,"b3ckm"); i++;
		scores.set(i,14.1); examples.set(i,"JRd"); i++;
		scores.set(i,15.1); examples.set(i,"Inww"); i++;
		scores.set(i,16.6); examples.set(i,"e4jf"); i++;
		scores.set(i,17.4); examples.set(i,"nWu4"); i++;
		scores.set(i,18.4); examples.set(i,"udj0"); i++;
		scores.set(i,19.5); examples.set(i,"2jRA3"); i++;
		scores.set(i,20.7); examples.set(i,"aELpU"); i++;
		scores.set(i,21.8); examples.set(i,"fP0l"); i++;
		scores.set(i,22.1); examples.set(i,"7HqKu"); i++;
		scores.set(i,23.5); examples.set(i,"AnQDK"); i++;
		scores.set(i,24.3); examples.set(i,"1emn5N"); i++;
		scores.set(i,25.6); examples.set(i,"e1mlT4"); i++;
		scores.set(i,26.8); examples.set(i,"1jvgdF"); i++;
		scores.set(i,27.8); examples.set(i,"gdNUbE"); i++;
		scores.set(i,28.2); examples.set(i,"bSdBBF"); i++;
		scores.set(i,29.5); examples.set(i,"jDPB9d"); i++;
		scores.set(i,30.1); examples.set(i,"B7SRC6G"); i++;
		scores.set(i,31.5); examples.set(i,"2TdgKRh"); i++;
		scores.set(i,32.9); examples.set(i,"WXqBsOQ"); i++;
		scores.set(i,33.9); examples.set(i,"wM2VnO8"); i++;
		scores.set(i,34.8); examples.set(i,"qBtww76f"); i++;
		scores.set(i,35.7); examples.set(i,"YTfGXJwd"); i++;
		scores.set(i,36.2); examples.set(i,"tTGQ2gNO"); i++;
		scores.set(i,37.6); examples.set(i,"zJyCeyVP"); i++;
		scores.set(i,38.2); examples.set(i,"IFIkU67C9"); i++;
		scores.set(i,39.5); examples.set(i,"0GjEWlRJ2"); i++;
		scores.set(i,40.3); examples.set(i,"KMsoVO3S"); i++;
		scores.set(i,41.5); examples.set(i,"Qs3vwDP355"); i++;
		scores.set(i,42.9); examples.set(i,"9e37yjGiKR"); i++;
		scores.set(i,43.8); examples.set(i,"w4moOYQwU"); i++;
		scores.set(i,44.6); examples.set(i,"zVIDDCFbTG"); i++;
		scores.set(i,45.6); examples.set(i,"br0PD3zSH"); i++;
		scores.set(i,46.3); examples.set(i,"YsWXgL4fISH"); i++;
		scores.set(i,47.); examples.set(i,"VGWNOOAoFM"); i++;
		scores.set(i,48.5); examples.set(i,"kV8s4p8OrMA"); i++;
		scores.set(i,49.3); examples.set(i,"mONdslM6dyBR"); i++;
		scores.set(i,50.9); examples.set(i,"hO69RUsDQdeK"); i++;
		scores.set(i,51.7); examples.set(i,"FD4AggGsytFCZ"); i++;
		scores.set(i,52.6); examples.set(i,"j0ql4eHuYQe"); i++;
		scores.set(i,53.6); examples.set(i,"9KiXAlVIw0LE"); i++;
		scores.set(i,54.8); examples.set(i,"YIMGg1q2neSo"); i++;
		scores.set(i,55.); examples.set(i,"RUdM5JTTBdPQ"); i++;
		scores.set(i,56.4); examples.set(i,"ZdQhBeHZgtPM"); i++;
		scores.set(i,57.2); examples.set(i,"hKZ38d1tu3d4s"); i++;
		scores.set(i,58.9); examples.set(i,"1si4yZUbS430RK"); i++;
		scores.set(i,59.7); examples.set(i,"dcBChNZmOxg3Y"); i++;
		scores.set(i,60.3); examples.set(i,"B06fu0jGuh4DXY"); i++;
		scores.set(i,61.7); examples.set(i,"Mcyt3QIOe0Jgy3"); i++;
		scores.set(i,62.2); examples.set(i,"gR92d2Yx9U6c0Uu"); i++;
		scores.set(i,63.); examples.set(i,"Rb3lzzoTQQa0YF"); i++;
		scores.set(i,64.4); examples.set(i,"GexmtptIzgv2Yu"); i++;
		scores.set(i,65.2); examples.set(i,"JF1yB3NEGIDZReL"); i++;
		scores.set(i,66.4); examples.set(i,"5Ow7tS7NTgbdicp"); i++;
		scores.set(i,67.7); examples.set(i,"pIfRplC38XEPPkO"); i++;
		scores.set(i,68.3); examples.set(i,"g0xR1LTIv80m1kYF"); i++;
		scores.set(i,69.7); examples.set(i,"7X0rrwhD2xhlIKe3"); i++;
		scores.set(i,70.5); examples.set(i,"HijRzChBoJgqmLR"); i++;
		scores.set(i,71.1); examples.set(i,"7bVQILrEM2fdhH0F"); i++;
		scores.set(i,72.5); examples.set(i,"hb9XBwSNVx9uvBJT"); i++;
		scores.set(i,73.8); examples.set(i,"nDoce4ziyQpfvywQ"); i++;
		scores.set(i,74.4); examples.set(i,"ANVZWz6kgG946wheD"); i++;
		scores.set(i,75.8); examples.set(i,"jHggoJ9gIOAoEem74"); i++;
		scores.set(i,76.3); examples.set(i,"SppG6M8rMc20w6b1Jr"); i++;
		scores.set(i,77.2); examples.set(i,"tRkQKDrapuNdxP47W"); i++;
		scores.set(i,78.5); examples.set(i,"CENLwIBg9yeIzhOAh"); i++;
		scores.set(i,79.7); examples.set(i,"5sDh2fu3VRt59DK72vc"); i++;
		scores.set(i,80.5); examples.set(i,"xbzy94rXqQrbda4HQU"); i++;
		scores.set(i,81.9); examples.set(i,"Ic5dg7SxhNkbtJwoJb"); i++;
		scores.set(i,82.4); examples.set(i,"a8L4UzfFFg30c2pGdhE"); i++;
		scores.set(i,83.2); examples.set(i,"uGApLUwFTbGMfmzWJ5"); i++;
		scores.set(i,84.4); examples.set(i,"uHhztf7v9e281Ln73dXp"); i++;
		scores.set(i,85.2); examples.set(i,"nAjBs9aNAaMxdVH0T8C"); i++;
		scores.set(i,86.6); examples.set(i,"SVQP1xV5WnKECzcieNx"); i++;
		scores.set(i,87.1); examples.set(i,"CAxfutK9v00q8v1rxOkQ"); i++;
		scores.set(i,88.5); examples.set(i,"WijGSp4EFc1K60wvuQLs"); i++;
		scores.set(i,89.9); examples.set(i,"PIN2JGW4sCNsIaPJ6cYL"); i++;
		scores.set(i,90.4); examples.set(i,"X9CMHlw1KEit8J0dUY7H7"); i++;
		scores.set(i,91.8); examples.set(i,"vx6pRzatk58HvV3f6EcsC"); i++;
		scores.set(i,92.6); examples.set(i,"C4PBVUXFQeVFtmoLpcFi"); i++;
		scores.set(i,93.2); examples.set(i,"WmGQQBLjRbX0DseZ45C1s"); i++;
		scores.set(i,94.6); examples.set(i,"5EUyiz6Xh2qUNrNMuoRWa"); i++;
		scores.set(i,96.); examples.set(i,"6ej7smjyBtpMzswKsztVx"); i++;
		scores.set(i,96.5); examples.set(i,"n1hZR0R8ipxHRbHP5KDF7o"); i++;
		scores.set(i,97.3); examples.set(i,"NFN0QOuxqUkRksFNaIOOa"); i++;
		scores.set(i,98.7); examples.set(i,"fHHeELszxbRZMARPKVqSa"); i++;
		scores.set(i,99.3); examples.set(i,"QUiFdIl1QcZICM6c6fEWRo"); i++;
		scores.set(i,100.7); examples.set(i,"lnUPt5HujGGXJNH3vHLjAK"); i++;		
	}

	ArrayList<Double> scores = new ArrayList<>();
	ArrayList<String> examples = new ArrayList<>();

	protected void generateExamples() {
		Nbvcxz checker = new Nbvcxz();
		for (int i = 0; i <= 100; i++) {
			scores.add(null);
			examples.add(null);
		}
		int i = 0;
		scores.set(i, 0.0);
		examples.set(i, "123456");
		i++;
		scores.set(i, 1.0);
		examples.set(i, "password");
		i++;
		scores.set(i, 2.0);
		examples.set(i, "qwerty");
		i++;
		scores.set(i, 3.0);
		examples.set(i, "Qwerty");
		i++;
		scores.set(i, 4.0);
		examples.set(i, "letmein");
		i++;
		scores.set(i, 5.0);
		examples.set(i, "Letmein");
		i++;
		scores.set(i, 5.643856189774724);
		examples.set(i, "iloveyou");
		i++;
		scores.set(i, 7.169925001442313);
		examples.set(i, "welcome");
		i++;
		scores.set(i, 8.169925001442312);
		examples.set(i, "Welcome");
		i++;
		scores.set(i, 8.92184093707449);
		examples.set(i, "login");
		i++;
		scores.set(i, 9.98299357469431);
		examples.set(i, "admin");
		i++;
		int added = i;
		int tried = 0;
		while (added < examples.size()) {
			for (int len = 3; len < 25; len++) {
				String pass = Generator.generateRandomPassword(Generator.CharacterTypes.ALPHANUMERIC, len);
				Result result = checker.estimate(pass);
				double entropy = result.getEntropy();
				int index = (int) entropy;
				tried++;
				if (index > 2 && index <= 100) {
					if (examples.get(index) == null) {
						examples.set(index, pass);
						scores.set(index, entropy);
						added++;
					}
				}
			}
			System.out.print(tried + " " + added + ": ");
			if (added > 80) {
				for (i = 1; i <= 100; i++) {
					if (scores.get(i) == null)
						System.out.printf("%d,", i);
				}
			}
			System.out.println();
			if (tried > 50_000)
				break;
		}
		for (i = 1; i <= 100; i++) {
			System.out.printf("%5.1f,%s\n", scores.get(i), examples.get(i));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PassPhraseStrength passPhraseStrength = new PassPhraseStrength();
		passPhraseStrength.loadExamples();
//		passPhraseStrength.generateExamples();
//		Nbvcxz checker = new Nbvcxz();
//		String[] words = { "123456", // 0
//				"password", // 1
//				"qwerty", // 2
//				"Qwerty", // 3
//				"letmein", // 4
//				"Letmein", // 5
//				"iloveyou", // 6
//				"welcome", // 7
//				"Welcome", // 8
//				"login", // 9
//				"admin", // 10
//		};
//		for (String word : words) {
//			Result result = checker.estimate(word);
//			System.out.println(result.getEntropy() + " " + word);
//		}
	}

}
