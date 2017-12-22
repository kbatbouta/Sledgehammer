/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class StringUtils {

	/**
	 * Creates a MD5 hash for a String with a provided salt (optional).
	 * 
	 * @param input
	 * @return
	 */
	public static String md5(String input, String salt) {
		String md5 = null;
		if (null == input)
			return null;
		if (salt != null)
			input += salt;
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