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
package sledgehammer.database.module.chat;

import com.mongodb.DBObject;

import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoDocument;
import sledgehammer.module.core.ModuleChat;
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

	public MongoPeriodicMessage(MongoCollection collection, String name, String message, String color, int time,
			boolean enabled, boolean broadcast) {
		super(collection, "name");
		setName(name);
		setMessage(message);
		setColor(color);
		setTime(time);
		setEnabled(enabled);
		setBroadcast(broadcast);
	}

	public MongoPeriodicMessage(MongoCollection collection, DBObject object) {
		super(collection, "name");
		onLoad(object);
	}

	@Override
	public void onLoad(DBObject object) {
		Object oName = object.get("name");
		if (oName != null) {
			setName(oName.toString());
		}
		Object oMessage = object.get("message");
		if (oMessage != null) {
			setMessage(oMessage.toString());
		}
		Object oColor = object.get("color");
		if (oColor != null) {
			setColor(oColor.toString());
		}
		Object oTime = object.get("time");
		if (oTime != null) {
			setTime(Integer.parseInt(oTime.toString()));
		}
		Object oEnabled = object.get("enabled");
		if (oEnabled != null) {
			setEnabled(oEnabled.toString().equals("1"));
		}
		Object oBroadcast = object.get("broadcast");
		if (oBroadcast != null) {
			setBroadcast(oBroadcast.toString().equals("1"));
		}
	}

	@Override
	public void onSave(DBObject object) {
		if (shouldSave()) {
			// @formatter:off
			object.put("name"     , getName()                    );
			object.put("message"  , getMessage()                 );
			object.put("color"    , getColor()                   );
			object.put("time"     , getTime() + ""               );
			object.put("enabled"  , isEnabled()       ? "1" : "0");
			object.put("broadcast", shouldBroadcast() ? "1" : "0");
			// @formatter:on
		}
	}

	@Override
	public Object getFieldValue() {
		return getName();
	}

	/**
	 * Updates the periodic message. Handles executing the message, and checking the
	 * time between executions.
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
				if (actualColor == null)
					color = ChatTags.COLOR_WHITE;
				if (shouldBroadcast()) {
					// Broadcast it as a /broadcast message.
					// SledgeHammer.instance.getChatManager().broadcastMessage(content,
					// actualColor);
				} else {
					// Send it in-chat.
					ModuleChat module = (ModuleChat) SledgeHammer.instance.getPluginManager()
							.getModule(ModuleChat.class);
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