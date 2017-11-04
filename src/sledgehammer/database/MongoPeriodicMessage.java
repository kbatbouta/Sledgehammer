package sledgehammer.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import sledgehammer.SledgeHammer;
import sledgehammer.modules.core.ModuleChat;
import sledgehammer.util.ChatTags;

public class MongoPeriodicMessage extends MongoDocument {

	private String name;
	private String message;
	private String color;
	private long timeThen = -1L;
	private int time;
	private boolean enabled;
	private boolean broadcast;
	private boolean save = true;
	
	public MongoPeriodicMessage(DBCollection collection, String name, String message, String color, int time, boolean enabled, boolean broadcast) {
		super(collection);
		setName(name);
		setMessage(message);
		setColor(color);
		setTime(time);
		setEnabled(enabled);
		setBroadcast(broadcast);
	}
	
	public MongoPeriodicMessage(DBCollection collection, DBObject object) {
		super(collection);
		load(object);
	}

	@Override
	public void load(DBObject object) {
		Object oName = object.get("name");
		if(oName != null) {
			setName(oName.toString());
		}
		Object oMessage = object.get("message");
		if(oMessage != null) {
			setMessage(oMessage.toString());
		}
		Object oColor = object.get("color");
		if(oColor != null) {
			setColor(oColor.toString());
		}
		Object oTime = object.get("time");
		if(oTime != null) {
			setTime(Integer.parseInt(oTime.toString()));
		}
		Object oEnabled = object.get("enabled");
		if(oEnabled != null) {
			setEnabled(oEnabled.toString().equals("1"));
		}
		Object oBroadcast = object.get("broadcast");
		if(oBroadcast != null) {
			setBroadcast(oBroadcast.toString().equals("1"));
		}
	}

	@Override
	public void save() {
		if(shouldSave()) {			
			// @formatter:off
			DBObject object = new BasicDBObject();
			object.put("name"     , getName()                    );
			object.put("message"  , getMessage()                 );
			object.put("color"    , getColor()                   );
			object.put("time"     , getTime() + ""               );
			object.put("enabled"  , isEnabled()       ? "1" : "0");
			object.put("broadcast", shouldBroadcast() ? "1" : "0");
			MongoDatabase.upsert(getCollection(), "name", object);
			// @formatter:on
		}
	}
	
	/**
	 * Updates the periodic message. Handles executing the message, and checking
	 * the time between executions.
	 */
	public void update() {
		// If the PeriodicMessage is currently active.
		if (isEnabled()) {
			// Grab the current time.
			long timeNow = System.currentTimeMillis();
			// If the delta time is larger than the time setting(time * one
			// minute)
			if (timeNow - timeThen >= (time * 60000)) {
				String actualColor = ChatTags.getColor(getColor());
				if(actualColor == null) color = ChatTags.COLOR_WHITE;
				if (shouldBroadcast()) {
					// Broadcast it as a /broadcast message.
					// SledgeHammer.instance.getChatManager().broadcastMessage(content, actualColor);
				} else {
					// Send it in-chat.
					ModuleChat module = (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
					module.sendGlobalMessage(actualColor + " " + getMessage());
				}
				// Mark the current time as last, to reset the delta.
				timeThen = timeNow;
			}
		}
	}
	
	/**
	 * Whether or not to save to the database.
	 * 
	 * @return
	 */
	public boolean shouldSave() {
		return save;
	}

	/**
	 * Returns whether or not this message is saved to the database.
	 * 
	 * @param flag
	 */
	public void setShouldSave(boolean flag) {
		save = flag;
	}

	
	public boolean shouldBroadcast() {
		return this.broadcast;
	}
	
	public void setBroadcast(boolean flag) {
		this.broadcast = flag;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void setEnabled(boolean flag) {
		this.enabled = flag;
	}
	
	public int getTime() {
		return this.time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public String getColor() {
		return this.color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	private void setMessage(String message) {
		this.message = message;
	}
	
	public String getName() {
		return this.name;
	}
	
	private void setName(String name) {
		this.name = name;
	}
}