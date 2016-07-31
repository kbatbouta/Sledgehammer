package sledgehammer.util;

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

import java.io.*;
import java.security.MessageDigest;

public class MD5 {

	public static byte[] createChecksumFromFile(String fileName) throws Exception {
		InputStream fileInputStream = new FileInputStream(fileName);

		byte[] bytes = new byte[1024];
		MessageDigest md5 = MessageDigest.getInstance("MD5");

		int result;
		do {
			result = fileInputStream.read(bytes);
			if (result > 0) md5.update(bytes, 0, result);
		} while (result != -1);

		fileInputStream.close();
		return md5.digest();
	}

	public static byte[] createChecksumFromString(String string) throws Exception {
		byte[] bytes = string.getBytes();
		MessageDigest complete = MessageDigest.getInstance("MD5");
		return complete.digest(bytes);
	}

	public static String getMD5Checksum(String string) throws Exception {
		byte[] b = createChecksumFromString(string);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static String getMD5ChecksumFromFile(String filename) throws Exception {
		byte[] b = createChecksumFromFile(filename);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

}