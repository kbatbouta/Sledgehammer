package sledgehammer.database.module.chat;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoUniqueDocument;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class MongoChatChannel extends MongoUniqueDocument {

	private String channelName;
	private String channelDescription;
	private String permissionNode;

	private boolean isGlobalChannel = false;
	private boolean isPublicChannel = true;
	private boolean isCustomChannel = true;
	private boolean saveHistory = true;
	private boolean canSpeak = true;
	private boolean explicit = false;

	public MongoChatChannel(MongoCollection mongoCollection, String channelName, String channelDescription,
			String permissionNode, boolean isGlobalChannel, boolean isPublicChannel, boolean isCustomChannel,
			boolean saveHistory, boolean canSpeak) {
		super(mongoCollection);
		setChannelName(channelName, false);
		setChannelDescription(channelDescription, false);
		setPermissionNode(permissionNode, false);
		setGlobalChannel(isGlobalChannel, false);
		setPublicChannel(isPublicChannel, false);
		setCustomChannel(isCustomChannel, false);
		setSaveHistory(saveHistory, false);
		setCanSpeak(canSpeak, false);
	}

	public MongoChatChannel(MongoCollection mongoCollection, DBObject object) {
		super(mongoCollection, object);
		onLoad(object);
	}

	@Override
	public void onLoad(DBObject object) {
		setChannelName((String) object.get("name"), false);
		setChannelDescription((String) object.get("description"), false);
		setPermissionNode((String) object.get("node"), false);
		loadFlags(object);
	}

	@Override
	public void onSave(DBObject object) {
		// @formatter:off
		object.put("name"       , getChannelName()       );
		object.put("description", getChannelDescription());
		object.put("node"       , getPermissionNode()    );
		object.put("flags"      , saveFlags()            );
		// @formatter:on
	}

	private void loadFlags(DBObject objectMain) {
		DBObject object = (DBObject) objectMain.get("flags");
		setGlobalChannel((boolean) object.get("global"), false);
		setPublicChannel((boolean) object.get("public"), false);
		setCustomChannel((boolean) object.get("custom"), false);
		setCanSpeak((boolean) object.get("speak"), false);
		setSaveHistory((boolean) object.get("history"), false);
		Object oExplicit = object.get("explicit");
		if (oExplicit != null) {
			setExplicit((boolean) oExplicit, false);
		}
	}

	private DBObject saveFlags() {
		// @formatter:off
		DBObject object = new BasicDBObject();
		object.put("global"  , isGlobalChannel());
		object.put("public"  , isPublicChannel());
		object.put("custom"  , isCustomChannel());
		object.put("speak"   , canSpeak()       );
		object.put("history" , saveHistory()    );
		object.put("explicit", isExplicit()     );
		// @formatter:on
		return object;
	}

	public String getPermissionNode() {
		return this.permissionNode;
	}

	public void setPermissionNode(String permissionNode, boolean save) {
		this.permissionNode = permissionNode;
		if (save) {
			save();
		}
	}

	public String getChannelName() {
		return this.channelName;
	}

	public void setChannelName(String channelName, boolean save) {
		this.channelName = channelName;
		if (save) {
			save();
		}
	}

	public String getChannelDescription() {
		return this.channelDescription;
	}

	public void setChannelDescription(String channelDescription, boolean save) {
		this.channelDescription = channelDescription;
		if (save) {
			save();
		}
	}

	public boolean isGlobalChannel() {
		return this.isGlobalChannel;
	}

	public void setGlobalChannel(boolean isGlobalChannel, boolean save) {
		this.isGlobalChannel = isGlobalChannel;
		if (save) {
			save();
		}
	}

	public boolean isPublicChannel() {
		return this.isPublicChannel;
	}

	public void setPublicChannel(boolean isPublicChannel, boolean save) {
		this.isPublicChannel = isPublicChannel;
		if (save) {
			save();
		}
	}

	public boolean canSpeak() {
		return this.canSpeak;
	}

	public void setCanSpeak(boolean canSpeak, boolean save) {
		this.canSpeak = canSpeak;
		if (save) {
			save();
		}
	}

	public boolean isCustomChannel() {
		return this.isCustomChannel;
	}

	public void setCustomChannel(boolean isCustomChannel, boolean save) {
		this.isCustomChannel = isCustomChannel;
		if (save) {
			save();
		}
	}

	public boolean saveHistory() {
		return this.saveHistory;
	}

	public void setSaveHistory(boolean saveHistory, boolean save) {
		this.saveHistory = saveHistory;
		if (save) {
			save();
		}
	}

	public boolean isExplicit() {
		return this.explicit;
	}

	public void setExplicit(boolean explicit, boolean save) {
		this.explicit = explicit;
		if (save) {
			save();
		}
	}
}