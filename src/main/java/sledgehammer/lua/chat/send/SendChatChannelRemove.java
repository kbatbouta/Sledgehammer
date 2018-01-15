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
 * Send Class for ChatChannels being removed.
 * 
 * Exports a LuaTable:
 * {
 *   - "channel_id": (String) ChatChannel Unique ID.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendChatChannelRemove extends Send {

    /**
     * The Unique ID of the ChatChannel being removed.
     */
    private UUID channelId;

    /**
     * Main constructor.
     */
    public SendChatChannelRemove() {
        super("core.chat", "sendChatChannelRemove");
    }

    @Override
    public void onExport() {
        set("channel_id", getChannelId().toString());
    }

    /**
     * @return Returns the Unique ID of the ChatChannel being removed.
     */
    public UUID getChannelId() {
        return this.channelId;
    }

    /**
     * Sets the Unique ID of the ChatChannel being removed.
     *
     * @param channelId The Unique ID of the ChatChannel.
     */
    public void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }
}