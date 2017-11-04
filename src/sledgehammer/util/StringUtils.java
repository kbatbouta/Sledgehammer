package sledgehammer.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

	/**
	 * Creates a MD5 hash for a String with a provided salt (optional).
	 * 
	 * @param input
	 * @return
	 */
	public static String md5(String input, String salt) {
		String md5 = null;
		if (null == input) return null;
		if (salt != null) input += salt;
		try {
			// Create MessageDigest object for MD5
			MessageDigest digest = MessageDigest.getInstance("MD5");
			// Update input string in message digest
			digest.update(input.getBytes(), 0, input.length());
			// Converts message digest value in base 16 (hex)
			md5 = new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5;
	}

	/**
	 * Creates a MD5 hash for a String.
	 * 
	 * @param input
	 * @return
	 */
	public static String md5(String input) {
		return md5(input, (String) null);
	}
	
	public static void main(String[] args) {
		String password = "Test123";
		String username = "MyUsername";
		String hash = md5(password, username);
		System.out.println("Username: " + password);
		System.out.println("Password: " + password);
		System.out.println("Hash: " + hash);
	}
}