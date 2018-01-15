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

package sledgehammer.event.chat;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.event.core.player.PlayerEvent;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;

/**
 * PlayerEvent to dispatch when a Player requests ChatChannels from
 * registered Modules in the Sledgehammer engine.
 *
 * @author Jab
 */
public class RequestChannelsEvent extends PlayerEvent {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "RequestChannelsEvent";

    /**
     * The List of ChatChannel's to send to the Player.
     */
    private List<ChatChannel> listChatChannels;

    /**
     * Main constructor.
     *
     * @param player The Player requesting ChatChannels.
     */
    public RequestChannelsEvent(Player player) {
        super(player);
        listChatChannels = new ArrayList<>();
    }

    @Override
    public String getLogMessage() {
        return null;
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * Adds a ChatChannel to the List of ChatChannels to send to the Player.
     *
     * @param chatChannel The ChatChannel to add.
     */
    public void addChatChannel(ChatChannel chatChannel) {
        if (!listChatChannels.contains(chatChannel)) {
            listChatChannels.add(chatChannel);
        }
    }

    /**
     * Removes a ChatChannel from the List of ChatChannels to send to the
     * Player.
     *
     * @param chatChannel The ChatChannel to remove.
     */
    public void removeChatChannel(ChatChannel chatChannel) {
        if (listChatChannels.contains(chatChannel)) {
            listChatChannels.remove(chatChannel);
        }
    }

    /**
     * @return Returns the List of ChatChannels to send to the Player.
     */
    public List<ChatChannel> getChatChannels() {
        return this.listChatChannels;
    }
}