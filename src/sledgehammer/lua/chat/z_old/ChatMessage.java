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
package sledgehammer.lua.chat.z_old;

import java.util.UUID;


import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.module.chat.MongoChatMessage;
import sledgehammer.lua.LuaTable;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class ChatMessage extends LuaTable {

	// @formatter:off
	public static final String ORIGIN_CLIENT = "client";
	public static final String ORIGIN_SERVER = "server";
	public static final String ORIGIN_MODULE = "module";
	public static final String ORIGIN_CORE   = "core"  ;
	// @formatter:on

	private MongoChatMessage mongoDocument;

	public ChatMessage(MongoChatMessage mongoDocument) {
		super("ChatMessage");
		setMongoDocument(mongoDocument);
	}

	/**
	 * Constructor for loading an existing ChatMessage Object.
	 * 
	 * @param table
	 */
	public ChatMessage(MongoChatMessage mongoDocument, KahluaTable table) {
		super("ChatMessage");
		setMongoDocument(mongoDocument);
		onLoad(table);
	}

	@Override
	public void onLoad(KahluaTable table) {
		setUniqueId(UUID.fromString(table.rawget("id").toString()), false);
		setChannelId(UUID.fromString(table.rawget("channel_id").toString()), false);
		setMessage(table.rawget("message").toString(), false);
		setOriginalMessage(table.rawget("message_original").toString(), false);
		setEdited(Boolean.parseBoolean(table.rawget("edited").toString()), false);
		setEditorID(UUID.fromString(table.rawget("editor_id").toString()), false);
		setDeleted(Boolean.parseBoolean(table.rawget("deleted").toString()), false);
		setDeleterID(UUID.fromString(table.rawget("deleter_id").toString()), false);
		Double d = Double.parseDouble(table.rawget("modified_timestamp").toString());
		setModifiedTimestamp(d.longValue(), false);
		// If origin is set.
		Object o = table.rawget("origin");
		if (o != null) {
			setOrigin(o.toString(), false);
		}
	}
	
	@Override
	public void onExport() {
		// @formatter:off
		set("id"                , getUniqueId().toString());
		set("channel_id"        , getChannelId().toString());
		set("message"           , getMessage()             );
		set("message_original"  , getOriginalMessage()     );
		set("edited"            , isEdited()               );
		set("editor_id"         , getEditorID().toString() );
		set("deleted"           , isDeleted()              );
		set("deleter_id"        , getDeleterId().toString());
		set("timestamp_modified", getModifiedTimestamp()   );
		set("time"              , getPrintedTimestamp()    );
		set("origin"            , getOrigin()              );
		// @formatter:on
	}
	
	public void save() {
		getMongoDocument().save();
	}
	
//	public void save() {
		// ModuleChat module = (ModuleChat)
		// SledgeHammer.instance.getPluginManager().getModule(ModuleChat.class);
		// module.saveMessage(this);
//	}

	private void setDeleted(boolean deleted, boolean save) {
		getMongoDocument().setDeleted(deleted, save);
	}

	private void setDeleterID(UUID deleterId, boolean save) {
		getMongoDocument().setDeleterId(deleterId, save);
	}

	private void setModifiedTimestamp(long timestampModified, boolean save) {
		getMongoDocument().setModifiedTimestamp(timestampModified, save);
	}

	protected void setOriginalMessage(String messageOriginal, boolean save) {
		getMongoDocument().setOriginalMessage(messageOriginal, save);
	}

	private void setEdited(boolean edited, boolean save) {
		getMongoDocument().setEdited(edited, save);
	}

	private void setEditorID(UUID editorId, boolean save) {
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

	public void setUniqueId(UUID uniqueId, boolean save) {
		getMongoDocument().setUniqueId(uniqueId, save);
	}

	public String getMessage() {
		return getMongoDocument().getMessage();
	}

	public void setMessage(String message, boolean save) {
		getMongoDocument().setMessage(message, save);
	}

	public long getModifiedTimestamp() {
		return getMongoDocument().getModifiedTimestamp();
	}

	public UUID getDeleterId() {
		return getMongoDocument().getDeleterId();
	}

	public boolean isDeleted() {
		return getMongoDocument().isDeleted();
	}

	public UUID getEditorID() {
		return getMongoDocument().getEditorId();
	}

	public boolean isEdited() {
		return getMongoDocument().isEdited();
	}

	public String getOriginalMessage() {
		return getMongoDocument().getOriginalMessage();
	}

	public String getOrigin() {
		return getMongoDocument().getOrigin();
	}

	public void setOrigin(String origin, boolean save) {
		getMongoDocument().setOrigin(origin, save);
	}

	public void setPrintedTimestamp(String time, boolean save) {
		getMongoDocument().setPrintedTimestamp(time, save);
	}

	public String getPrintedTimestamp() {
		return getMongoDocument().getPrintedTimestamp();
	}

	public int getType() {
		return getMongoDocument().getType();
	}

	public void setType(int type) {
	}

	public MongoChatMessage getMongoDocument() {
		return this.mongoDocument;
	}

	private void setMongoDocument(MongoChatMessage mongoDocument) {
		this.mongoDocument = mongoDocument;
	}
}
