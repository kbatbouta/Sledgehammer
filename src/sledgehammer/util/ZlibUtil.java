package sledgehammer.util;

import java.util.zip.*;
import java.io.*;

public class ZlibUtil {

	public static void compressFile(File uncompressed, File compressed) throws IOException {
		InputStream inputStream = new FileInputStream(uncompressed);
		OutputStream outputStream = new DeflaterOutputStream(new FileOutputStream(compressed));
		bufferIO(inputStream, outputStream);
		inputStream.close();
		outputStream.close();
	}

	public static void decompressFile(File compressed, File uncompressed) throws IOException {
		InputStream inputStream = new InflaterInputStream(new FileInputStream(compressed));
		OutputStream outputStream = new FileOutputStream(uncompressed);
		bufferIO(inputStream, outputStream);
		inputStream.close();
		outputStream.close();
	}

	private static void bufferIO(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[1000];
		int len;
		while ((len = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, len);
		}
	}

}