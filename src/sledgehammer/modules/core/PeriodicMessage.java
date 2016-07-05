package sledgehammer.modules.core;

import sledgehammer.SledgeHammer;
import sledgehammer.util.ChatTags;

/**
 * Class designed to handle periodic messages on the server.
 * 
 * @author Jab
 *
 */
public class PeriodicMessage {

	/**
	 * The name of the message. (Used for identification)
	 */
	private String name;

	/**
	 * The message content.
	 */
	private String content;

	/**
	 * The color of the message broadcasted.
	 */
	private String color = ChatTags.COLOR_WHITE;

	/**
	 * Whether or not to broadcast the message.
	 */
	private boolean enabled = false;

	/**
	 * Whether or not to save the message. (This is for third-party plug-ins).
	 */
	private boolean save = false;

	/**
	 * Whether or not to broadcast the message on the screen.
	 */
	private boolean broadcast = false;

	/**
	 * The time setting. (In minutes. E.G: 15 = 15 minutes)
	 */
	private int time = 15;

	/**
	 * Delta used for calculating the time offset.
	 */
	private long timeThen = 0L;

	/**
	 * Main constructor.
	 * 
	 * @param name
	 * @param content
	 */
	public PeriodicMessage(String name, String content) {
		this.name = name;
		this.content = content;
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
				
				System.out.println("Periodic Message Displayed: " + content);
				
				if (isBroadcasted()) {
					// Broadcast it as a /broadcast message.
					SledgeHammer.instance.getChatManager().broadcastMessage(content, actualColor);
				} else {
					// Send it in-chat.
					SledgeHammer.instance.getChatManager().messageGlobal("", ChatTags.COLOR_WHITE, getContent(), actualColor, false);
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

	/**
	 * Returns whether or not this message is enabled.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled flag for this message.
	 * 
	 * @param flag
	 */
	public void setEnabled(boolean flag) {
		enabled = flag;
	}

	/**
	 * Returns this message's content.
	 * 
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the name associated with this message. (the ID of the message)
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns how long (in minutes), this message waits until execution.
	 *
	 * @return
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Sets the time (in minutes), this message waits until execution.
	 * 
	 * @param time
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * Returns whether or not this message is broadcasted on the screen.
	 * 
	 * @return
	 */
	public boolean isBroadcasted() {
		return broadcast;
	}

	/**
	 * Sets whether or not this message is broadcasted on the screen.
	 * 
	 * @param flag
	 */
	public void setBroadcasted(boolean flag) {
		broadcast = flag;
	}

	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}

}
