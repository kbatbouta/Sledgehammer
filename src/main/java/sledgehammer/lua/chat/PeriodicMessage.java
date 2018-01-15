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

import sledgehammer.lua.LuaTable;
import sledgehammer.util.ChatTags;

/**
 * Class designed to handle periodic messages on the server.
 * <p>
 * TODO: Change String Color to Color object.
 * <p>
 * TODO: Implement PeriodicMessage on the Lua-end of Sledgehammer's ModuleChat.
 *
 * @author Jab
 */
public class PeriodicMessage extends LuaTable {

    /** The name of the message. (Used for identification) */
    private String name;
    /** The message content. */
    private String content;
    /** The color of the message broad-casted. */
    private String color = ChatTags.COLOR_WHITE;
    /** Whether or not to broadcast the message. */
    private boolean enabled = false;
    /** Whether or not to broadcast the message on the screen. */
    private boolean broadcast = false;
    /** The time setting. (In minutes. E.G: 15 = 15 minutes) */
    private int time = 15;

    /**
     * Main constructor.
     *
     * @param name
     *            The String identifier of the PeriodicMessage.
     * @param content
     *            The String content of the PeriodicMessage.
     */
    public PeriodicMessage(String name, String content) {
        super("PeriodicMessage");
        this.name = name;
        this.content = content;
    }

    @Override
    public void onExport() {
        // @formatter:off
		set("name"   , getName()   );
		set("message", getMessage());
		set("enabled", isEnabled() );
		set("time"   , getTime()   );
		set("color"  , getColor()  );
		// @formatter:on
    }

    /**
     * @return Returns whether or not the PeriodicMessage is enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the enabled flag for this message.
     *
     * @param flag The flag to set.
     */
    public void setEnabled(boolean flag) {
        this.enabled = flag;
    }

    /**
     * @return Returns the String content of the PeriodicMessage.
     */
    public String getMessage() {
        return this.content;
    }

    /**
     * @return Returns the String name associated with this message. (the ID of
     * the message)
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return Returns how long (in minutes), this message waits until execution.
     */
    public int getTime() {
        return this.time;
    }

    /**
     * Sets the Integer time (in minutes), this message waits until execution.
     *
     * @param time The Integer time to set.
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * @return Returns whether or not this message is broad-casted on the screen.
     */
    public boolean isBroadcasted() {
        return this.broadcast;
    }

    /**
     * Sets whether or not this message is broad-casted on the screen.
     *
     * @param flag The flag to set.
     */
    public void setBroadcasted(boolean flag) {
        this.broadcast = flag;
    }

    /**
     * @return Returns the String Color of the PeriodicMessage.
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the String Color of the PeriodicMessage.
     *
     * @param color The String Color to set.
     */
    public void setColor(String color) {
        this.color = color;
    }
}