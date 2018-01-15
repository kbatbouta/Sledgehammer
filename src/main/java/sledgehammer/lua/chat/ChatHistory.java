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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.LuaArray;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.chat.send.SendChatMessages;
import sledgehammer.lua.core.Player;

/**
 * Chat LuaTable designed to store ChatMessage entries for a ChatChannel.
 *
 * @author Jab
 */
public class ChatHistory extends LuaTable {

    /** The Maximum amount of messages stored in the ChatChannel's history. */
    public static final int MAX_SIZE = 1024;
    /** The LinkedList to store the ChatMessages. */
    private LinkedList<ChatMessage> listMessages;
    /** The ChatChannel using this ChatHistory. */
    private ChatChannel chatChannel;
    /** The Send Object to send the ChatMessages. */
    private SendChatMessages sendChatMessages;

    /**
     * Main constructor.
     *
     * @param chatChannel
     *            The ChatChannel using the history.
     */
    public ChatHistory(ChatChannel chatChannel) {
        super("ChatHistory");
        setChatChannel(chatChannel);
        sendChatMessages = new SendChatMessages(chatChannel.getUniqueId());
        listMessages = new LinkedList<>();
    }

    @Override
    public void onExport() {
        LuaArray<ChatMessage> listChatMessages = new LuaArray<>();
        for (ChatMessage chatMessage : listMessages) {
            listChatMessages.add(chatMessage);
        }
        String channelIdAsString = getChatChannel().getUniqueId().toString();
        // @formatter:off
		set("channel_id", channelIdAsString);
		set("messages"  , listChatMessages );
		// @formatter:on
    }

    /**
     * Adds a Collection of ChatMessage to the history.
     *
     * @param collectionChatMessages The Collection of ChatMessages to add to the history.
     */
    public void addChatMessages(Collection<ChatMessage> collectionChatMessages, boolean send) {
        boolean sendIndividually = send && !chatChannel.isGlobalChannel();
        for (ChatMessage chatMessage : collectionChatMessages) {
            addChatMessage(chatMessage, !chatChannel.isGlobalChannel() && send);
        }
        if (send && !sendIndividually) {
            sendChatMessages.clearChatMessages();
            sendChatMessages.addChatMessages(collectionChatMessages);
            SledgeHammer.instance.send(sendChatMessages, chatChannel.getPlayers());
        }
    }

    /**
     * Adds a ChatMessage to the history.
     *
     * @param chatMessage The ChatMessage being added to the history.
     */
    public void addChatMessage(ChatMessage chatMessage, boolean send) {
        // Make sure the history doesn't already contain the ChatMessage.
        if (!listMessages.contains(chatMessage)) {
            // Add the ChatMessage to the history.
            listMessages.add(chatMessage);
            if (send) {
                sendChatMessages.clearChatMessages();
                sendChatMessages.addChatMessage(chatMessage);
                if (chatChannel.isGlobalChannel()) {
                    SledgeHammer.instance.send(sendChatMessages, chatChannel.getPlayers());
                } else {
                    Player chatMessagePlayer = chatMessage.getPlayer();
                    List<Player> listPlayers = new ArrayList<>();
                    if (chatMessagePlayer != null) {
                        for (Player player : chatChannel.getPlayers()) {
                            if (player.isWithinLocalRange(chatMessagePlayer)) {
                                listPlayers.add(player);
                            }
                        }
                    }
                    if(listPlayers.size() > 0) {
                        SledgeHammer.instance.send(sendChatMessages, listPlayers);
                    }
                }
            }
            // Check if the history is at message capacity.
            if (listMessages.size() > MAX_SIZE) {
                // If it is, grab the oldest ChatMessage to the list and delete it.
                listMessages.removeFirst();
                // chatMessageRemoved.delete();
            }
        }
    }

    /**
     * Clears the history, removing and deleting all the ChatMessages from the
     * database.
     */
    public void clear() {
        for (ChatMessage chatMessage : listMessages) {
            chatMessage.delete();
        }
        listMessages.clear();
    }

    /**
     * @return Returns the ChatChannel using the ChatHistory.
     */
    public ChatChannel getChatChannel() {
        return this.chatChannel;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the ChatChannel using the ChatHistory
     *
     * @param chatChannel The ChatChannel to set.
     */
    private void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }
}