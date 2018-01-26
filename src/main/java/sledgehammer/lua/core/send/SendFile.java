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

package sledgehammer.lua.core.send;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.Send;
import sledgehammer.lua.core.Player;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Set 'CHAR_MODE' for text-based files.
 *
 * <p>TODO: Document.
 *
 * @author Jab
 */
public class SendFile extends Send {

  // @formatter:off
  private static final int PACKET_START = 0;
  private static final int PACKET_PART = 1;
  private static final int PACKET_END = 2;
  public static final int TYPE_CHARS = 0;
  public static final int TYPE_BYTES = 1;
  // @formatter:on

  private File file;
  private String path;
  private ExplodedFile fileExploded;

  /**
   * Main constructor.
   *
   * @param module The String Client ID of the Module.
   * @param file The file to read and send to clients.
   * @param path The local path inside of the Lua cache folder to save the file as.
   */
  public SendFile(String module, File file, String path) {
    super(module, "sendFile");
    setFile(file);
    setPath(path);
  }

  private int offset = 0;
  private int packetType = 0;

  private int mode = TYPE_CHARS;

  @Override
  public void onExport() {
    if (packetType == 0) {
      set("explodedFile", fileExploded.export());
    } else {
      set("explodedFile", null);
    }
    set("data", fileExploded.getSection(offset));
    set("packet", packetType);
  }

  public void send(Player player) {
    if (fileExploded == null) {
      if (mode == TYPE_BYTES) {
        fileExploded = readFileBytes(getFile(), getPath());
      } else {
        fileExploded = readFileChars(getFile(), getPath());
      }
    }
    for (int packet = 0; packet < fileExploded.size(); packet++) {
      offset = packet;
      packetType =
          packet == 0 ? PACKET_START : packet == fileExploded.size() - 1 ? PACKET_END : PACKET_PART;
      SledgeHammer.instance.sendServerCommand(
          player, "sledgehammer.module." + getModule(), getCommand(), export());
    }
  }

  public File getFile() {
    return this.file;
  }

  public void setFile(File file) {
    this.file = file;
    this.fileExploded = null;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  private static ExplodedFile readFileChars(File file, String path) {
    if (file == null) {
      throw new IllegalArgumentException("File given is null.");
    }
    int chunkSize = 8192;
    return new ExplodedFile(path);
  }

  private static ExplodedFile readFileBytes(File file, String path) {
    if (file == null) {
      throw new IllegalArgumentException("File given is null.");
    }
    int chunkSize = 2048;
    ExplodedFile explodedFile = new ExplodedFile(path);
    StringBuilder stringBuilder = new StringBuilder();
    try {
      FileInputStream fis = new FileInputStream(file);
      DataInputStream dis = new DataInputStream(fis);
      int originalLength = dis.available();
      int offset = 0;
      for (int index = 0; index < originalLength; index++) {
        if (offset < chunkSize) {
          offset++;
          stringBuilder.append(dis.readByte() + 128).append(",");
        } else {
          offset = 0;
          String data = stringBuilder.toString();
          data = data.substring(0, data.length() - 1);
          explodedFile.addSection(TYPE_BYTES, data);
          stringBuilder = new StringBuilder();
        }
      }
      dis.close();
      fis.close();
      if (stringBuilder.length() > 0) {
        String data = stringBuilder.toString();
        data = data.substring(0, data.length() - 1);
        explodedFile.addSection(TYPE_BYTES, data);
      }
    } catch (IOException e) {
      SledgeHammer.instance.stackTrace(e);
    }
    return explodedFile;
  }

  /**
   * Class to handle exploding and transmitting the data for a File being sent to a Player.
   *
   * @author Jab
   */
  static class ExplodedFile extends LuaTable {

    private List<String> listSections;
    private List<Integer> listSectionTypes;
    private String path;

    /**
     * Main constructor.
     *
     * @param path The module-directory path for the File to be saved on the Player's Lua cache
     *     directory.
     */
    ExplodedFile(String path) {
      super("ExplodedFile");
      setPath(path);
      listSections = new ArrayList<>();
      listSectionTypes = new ArrayList<>();
    }

    @Override
    public void onExport() {
      KahluaTable table = newTable();
      for (int index = 0; index < listSectionTypes.size(); index++) {
        table.rawset(index, (Double) (double) listSectionTypes.get(index));
      }
      set("path", path);
      set("segments", size());
      set("segmentTypes", table);
    }

    /**
     * @param index The index the chunk of data is stored as.
     * @return Returns the String formatted data for a section of the file.
     */
    public String getSection(int index) {
      return listSections.get(index);
    }

    /**
     * Adds a chunk of data to the ExplodedFile, with the provided type.
     *
     * @param type The type of data format used for the chunk.
     * @param section The chunk of data to store.
     */
    void addSection(int type, String section) {
      println("Adding section:");
      println("\ttype: " + (type == 0 ? "TYPE_CHARS" : "TYPE_BYTES"));
      listSections.add(section);
      listSectionTypes.add(type);
    }

    /**
     * Sets the path for the File to be stored at in the Player's Lua cache directory relative to
     * the Module.
     *
     * @param path The path to set.
     */
    private void setPath(String path) {
      this.path = path;
    }

    /** @return Returns the amount of sections in the ExplodedFile. */
    public int size() {
      return listSections.size();
    }
  }
}
