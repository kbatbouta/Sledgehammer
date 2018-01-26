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

package sledgehammer.lua.chat;

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.chat.MongoChatMessage;
import sledgehammer.lua.MongoLuaObject;
import sledgehammer.lua.core.Player;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class ChatMessage extends MongoLuaObject<MongoChatMessage> {

  // @formatter:off
  public static final String ORIGIN_CLIENT = "client";
  public static final String ORIGIN_SERVER = "server";
  public static final String ORIGIN_MODULE = "module";
  public static final String ORIGIN_CORE = "core";
  public static final String ORIGIN_DISCORD = "discord";
  // @formatter:on

  private Player player;

  public ChatMessage(MongoChatMessage mongoDocument) {
    super(mongoDocument, "ChatMessage");
  }

  public ChatMessage(MongoChatMessage mongoDocument, KahluaTable table) {
    super(mongoDocument, "ChatMessage");
    onLoad(table);
  }

  public ChatMessage(
      MongoChatMessage mongoDocument,
      UUID channelId,
      UUID playerId,
      UUID editorId,
      UUID deleterId,
      String origin,
      String playerName,
      String message,
      String messageOriginal,
      String timestampPrinted,
      long timestamp,
      long timestampModified,
      int type) {
    super(mongoDocument, "ChatMessage");
    setChannelId(channelId, false);
    setPlayerId(playerId, false);
    setEditorId(editorId, false);
    setDeleterId(deleterId, false);
    setOrigin(origin, false);
    setCachedPlayerName(playerName, false);
    setMessage(message, false);
    setOriginalMessage(messageOriginal, false);
    setPrintedTimestamp(timestampPrinted, false);
    setTimestamp(timestamp, false);
    setModifiedTimestamp(timestampModified, false);
    setType(type, false);
  }

  @Override
  public void onLoad(KahluaTable table) {
    UUID uniqueId;
    Object oId = table.rawget("id");
    // Set the ID if it exists.
    if (oId != null) {
      uniqueId = UUID.fromString(oId.toString());
    }
    // This is a new ChatMessage. Create a new UUID.
    else {
      uniqueId = UUID.randomUUID();
    }
    setUniqueId(uniqueId, false);
    Object oChannelId = table.rawget("channel_id");
    if (oChannelId != null) {
      setChannelId(UUID.fromString(oChannelId.toString()), false);
    } else {
      throw new IllegalArgumentException("channel_id provided is null.");
    }
    Object oPlayerId = table.rawget("player_id");
    if (oPlayerId != null) {
      setPlayerId(UUID.fromString(oPlayerId.toString()), false);
    }
    Object oEditorId = table.rawget("editor_id");
    if (oEditorId != null) {
      setEditorId(UUID.fromString(oEditorId.toString()), false);
    }
    Object oDeleterId = table.rawget("deleter_id");
    if (oDeleterId != null) {
      setDeleterId(UUID.fromString(oDeleterId.toString()), false);
    }
    setOrigin(table.rawget("origin").toString(), false);
    setCachedPlayerName(table.rawget("player_name").toString(), false);
    setMessage(table.rawget("message").toString(), false);
    setOriginalMessage(table.rawget("message_original").toString(), false);
    // Check to see if the printed timestamp is given. If not, the method
    // with a null String passed will generate one instead.
    String timestampPrinted = null;
    Object oTimestampPrinted = table.rawget("timestamp_printed");
    if (oTimestampPrinted != null) {
      timestampPrinted = oTimestampPrinted.toString();
    }
    setPrintedTimestamp(timestampPrinted, false);
    // Check to see if a timestamp is given. If not, create one.
    Object oTimestamp = table.rawget("timestamp");
    if (oTimestamp != null) {
      long timestamp = Double.doubleToLongBits((Double.parseDouble(oTimestamp.toString())));
      setTimestamp(timestamp, false);
    } else {
      createTimestamp(false);
    }
    // Check to see if a timestamp is given. If the timestamp is 0, assign one.
    Object oTimestampModified = table.rawget("timestamp_modified");
    if (oTimestampModified != null) {
      long timestampModified =
          Double.doubleToLongBits((Double.parseDouble(oTimestampModified.toString())));
      if (timestampModified > 0) {
        setModifiedTimestamp(timestampModified, false);
      } else if (timestampModified == 0) {
        createModifiedTimestamp(false);
      }
    }
    setType(((Double) table.rawget("message_type")).intValue(), false);
    setEdited((Boolean) table.rawget("edited"), false);
    setDeleted((Boolean) table.rawget("deleted"), false);
  }

  @Override
  public void onExport() {
    // @formatter:off
    UUID uniqueId = getUniqueId();
    UUID channelId = getChannelId();
    UUID playerId = getPlayerId();
    UUID editorId = getEditorId();
    UUID deleterId = getDeleterId();
    String uniqueIdAsString = uniqueId.toString();
    String channelIdAsString = channelId == null ? null : channelId.toString();
    String playerIdAsString = playerId == null ? null : playerId.toString();
    String editorIdAsString = editorId == null ? null : editorId.toString();
    String deleterIdAsString = deleterId == null ? null : deleterId.toString();
    set("id", uniqueIdAsString);
    set("channel_id", channelIdAsString);
    set("player_id", playerIdAsString);
    set("editor_id", editorIdAsString);
    set("deleter_id", deleterIdAsString);
    set("origin", getOrigin());
    set("player_name", getCachedPlayerName());
    set("message", getMessage());
    set("message_original", getOriginalMessage());
    set("timestamp", getTimestamp());
    set("timestamp_modified", getModifiedTimestamp());
    set("timestamp_printed", getPrintedTimestamp());
    set("message_type", getType());
    set("edited", isEdited());
    set("deleted", isDeleted());
    set("player", getPlayer());
    // @formatter:on
  }

  @Override
  public boolean equals(Object other) {
    boolean returned = false;
    if (other instanceof ChatMessage) {
      returned = ((ChatMessage) other).getUniqueId().equals(getUniqueId());
    }
    return returned;
  }

  /**
   * Deep-Clones a Chat-Message, creating a new MongoChatMessage, however it is not saved to the
   * MongoDB database.
   */
  @Override
  public ChatMessage clone() {
    MongoCollection collectionChatMessages = getMongoDocument().getCollection();
    MongoChatMessage mongoChatMessage = new MongoChatMessage(collectionChatMessages);
    UUID channelId = getChannelId();
    UUID playerId = getPlayerId();
    UUID editorId = getEditorId();
    UUID deleterId = getDeleterId();
    String origin = getOrigin();
    String playerName = getCachedPlayerName();
    String message = getMessage();
    String messageOriginal = getOriginalMessage();
    String timestampPrinted = getPrintedTimestamp();
    long timestamp = getTimestamp();
    long timestampModified = getModifiedTimestamp();
    int type = getType();
    return new ChatMessage(
        mongoChatMessage,
        channelId,
        playerId,
        editorId,
        deleterId,
        origin,
        playerName,
        message,
        messageOriginal,
        timestampPrinted,
        timestamp,
        timestampModified,
        type);
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player, boolean save) {
    this.player = player;
    setPlayerId(player.getUniqueId(), save);
  }

  public UUID getPlayerId() {
    return getMongoDocument().getPlayerId();
  }

  public void setPlayerId(UUID playerId, boolean save) {
    getMongoDocument().setPlayerId(playerId, save);
    if (playerId != null) {
      player = SledgeHammer.instance.getPlayer(playerId);
    }
  }

  public boolean isDeleted() {
    return getMongoDocument().isDeleted();
  }

  public void setDeleted(boolean deleted, boolean save) {
    getMongoDocument().setDeleted(deleted, save);
  }

  public boolean isEdited() {
    return getMongoDocument().isEdited();
  }

  public void setEdited(boolean edited, boolean save) {
    getMongoDocument().setEdited(edited, save);
  }

  public int getType() {
    return getMongoDocument().getType();
  }

  public void setType(int type, boolean save) {
    getMongoDocument().setType(type, save);
  }

  public long getModifiedTimestamp() {
    return getMongoDocument().getModifiedTimestamp();
  }

  public void setModifiedTimestamp(long timestampModified, boolean save) {
    getMongoDocument().setModifiedTimestamp(timestampModified, save);
  }

  public void createModifiedTimestamp(boolean save) {
    getMongoDocument().setModifiedTimestamp(System.currentTimeMillis(), save);
  }

  public long getTimestamp() {
    return getMongoDocument().getTimestamp();
  }

  public void setTimestamp(long timestamp, boolean save) {
    getMongoDocument().setTimestamp(timestamp, save);
  }

  public void createTimestamp(boolean save) {
    getMongoDocument().setTimestamp(System.currentTimeMillis(), save);
  }

  public String getPrintedTimestamp() {
    return getMongoDocument().getPrintedTimestamp();
  }

  public void setPrintedTimestamp(String timestampPrinted, boolean save) {
    if (timestampPrinted == null) {
      setPrintedTimestamp(save);
    }
    getMongoDocument().setPrintedTimestamp(timestampPrinted, save);
  }

  public void setPrintedTimestamp(boolean save) {
    getMongoDocument().setPrintedTimestamp(save);
  }

  public String getOriginalMessage() {
    return getMongoDocument().getOriginalMessage();
  }

  public void setOriginalMessage(String messageOriginal, boolean save) {
    getMongoDocument().setOriginalMessage(messageOriginal, save);
  }

  public String getMessage() {
    return getMongoDocument().getMessage();
  }

  public void setMessage(String message, boolean save) {
    getMongoDocument().setMessage(message, save);
  }

  public String getCachedPlayerName() {
    Player player = getPlayer();
    if (player != null) {
      return player.getName();
    }
    return getMongoDocument().getCachedPlayerName();
  }

  private void setCachedPlayerName(String playerName, boolean save) {
    getMongoDocument().setCachedPlayerName(playerName, save);
  }

  public String getOrigin() {
    return getMongoDocument().getOrigin();
  }

  public void setOrigin(String origin, boolean save) {
    getMongoDocument().setOrigin(origin, save);
  }

  public UUID getDeleterId() {
    return getMongoDocument().getDeleterId();
  }

  private void setDeleterId(UUID deleterId, boolean save) {
    getMongoDocument().setDeleterId(deleterId, save);
  }

  public UUID getEditorId() {
    return getMongoDocument().getEditorId();
  }

  private void setEditorId(UUID editorId, boolean save) {
    getMongoDocument().setEditorId(editorId, save);
  }

  public UUID getChannelId() {
    return getMongoDocument().getChannelId();
  }

  public void setChannelId(UUID channelId, boolean save) {
    getMongoDocument().setChannelId(channelId, save);
  }

  public UUID getUniqueId() {
    return getMongoDocument().getUniqueId();
  }

  private void setUniqueId(UUID uniqueId, boolean save) {
    getMongoDocument().setUniqueId(uniqueId, save);
  }

  public void save() {
    getMongoDocument().save();
  }

  public void delete() {
    getMongoDocument().delete();
  }
}
