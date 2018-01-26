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

package sledgehammer.lua.chat.send;

import java.util.UUID;

import sledgehammer.lua.Send;

// @formatter:off
/**
 * Send Class for ChatMessages being renamed.
 *
 * <p>Exports a LuaTable: { - "channel_id": (String) The UUID String of the ChatChannel. -
 * "name_old": (String) The old ChatChannel name. - "name_new": (String) The new ChatChannel name. }
 *
 * @author Jab
 */
// @formatter:on
public class SendChatChannelRename extends Send {

  /** The Unique ID channelId of the ChatChannel. */
  private UUID channelId;
  /** The String old name of the ChatChannel. */
  private String nameOld;
  /** The String new name of the ChatChannel. */
  private String nameNew;

  /** Public constructor. */
  public SendChatChannelRename() {
    super("core.chat", "sendChatChannelRename");
  }

  @Override
  public void onExport() {
    // @formatter:off
    set("channel_id", getChannelId().toString());
    set("name_old", getOldName());
    set("name_new", getNewName());
    // @formatter:on
  }

  /** @return Returns the Unique ID of the ChatChannel to send. */
  public UUID getChannelId() {
    return this.channelId;
  }

  /**
   * Sets the Unique ID of the ChatChannel to send.
   *
   * @param channelId The Unique ID of the ChatChannel.
   */
  public void setChannelId(UUID channelId) {
    this.channelId = channelId;
  }

  /** @return Returns the String new name of the ChatChannel. */
  public String getNewName() {
    return this.nameNew;
  }

  /**
   * Sets the String new name of the ChatChannel being sent.
   *
   * @param nameNew The String new name of the ChatChannel.
   */
  public void setNewName(String nameNew) {
    this.nameNew = nameNew;
  }

  /** @return Returns the String old name of the ChatChannel. */
  public String getOldName() {
    return this.nameOld;
  }

  /**
   * Sets the String old name of the ChatChannel being sent.
   *
   * @param nameOld The String old name of the ChatChannel.
   */
  public void setOldName(String nameOld) {
    this.nameOld = nameOld;
  }
}
