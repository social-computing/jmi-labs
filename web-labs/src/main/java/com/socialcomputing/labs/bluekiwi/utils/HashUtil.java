/**
 * 
 */
package com.socialcomputing.labs.bluekiwi.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 * 
 * Utility class to generate a SHA1 hash of a String as
 * the sha1() php function does
 */
public class HashUtil {
	
    public static byte[] createHash(String text, String method) 
    		throws NoSuchAlgorithmException {
        byte[] b = text.getBytes();
        MessageDigest algorithm = MessageDigest.getInstance(method );
        algorithm.reset();
        algorithm.update(b);
        byte messageDigest[] = algorithm.digest();
        return messageDigest;
    }
    
    public static String getPHPSha1(String text) {
        byte[] b = null;
		try {
			b = createHash(text, "SHA-1");
		} 
		catch (NoSuchAlgorithmException e) {
			// Will not happen so don't handle it
			
		}
        return asHex(b);
    }
    
    /**
     * asHex
     * @param bytes to change to hexidecimal. Did you know there is a faster version of this somewhere?
     * http://www.rgagnon.com/javadetails/java-0596.html (In case you're board.)
     */
    public static String asHex(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
            Integer.toString(( b[i] & 0xff ) + 0x100, 16).substring(1 );
        }
        return result;
    }
}
