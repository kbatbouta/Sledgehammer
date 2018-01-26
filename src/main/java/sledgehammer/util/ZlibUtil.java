/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.util;

import java.util.zip.*;
import java.io.*;

/**
 * TODO: Document.
 *
 * @author Jab
 */
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

  private static void bufferIO(InputStream inputStream, OutputStream outputStream)
      throws IOException {
    byte[] buffer = new byte[1000];
    int len;
    while ((len = inputStream.read(buffer)) > 0) {
      outputStream.write(buffer, 0, len);
    }
  }
}
