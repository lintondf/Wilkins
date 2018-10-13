/**
 * 
 */
package org.cryptonomicon;

import java.util.ArrayList;
import java.util.Random;

import com.google.common.io.BaseEncoding;

/**
 * @author lintondf
 *
 */
public class Util {

	public static int[] permute( Random random, int[] array ) {
		int n = array.length;
		while (n > 1) {
			int k = random.nextInt(n--); // decrements after using the value
			int temp = array[n];
			array[n] = array[k];
			array[k] = temp;
		}
		return array;
	}

	public static <T> void permute( Random random, ArrayList<T> iterators ) {
		int n = iterators.size();
		while (n > 1) {
			int k = random.nextInt(n--); // decrements after using the value
			T temp = iterators.get(n);
			iterators.set(n,  iterators.get(k) );
			iterators.set(k,  temp);
		}
		
	}

	public static String toString( byte[] array ) {
		return String.format("(%d) %s", array.length, BaseEncoding.base16().lowerCase().encode(array) );
	}
	
	

}
