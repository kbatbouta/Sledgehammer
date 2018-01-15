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

import sledgehammer.lua.Send;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatHistory;

/**
 * Send Class for ChatChannels.
 *
 * @author Jab
 */
public class SendChatChannel extends Send {

    /**
     * The ChatChannel being sent.
     */
    private ChatChannel chatChannel;

    /**
     * Main constructor.
     *
     * @param chatChannel The ChatChannel to send.
     */
    public SendChatChannel(ChatChannel chatChannel) {
        super("core.chat", "sendChatChannel");
        setChatChannel(chatChannel);
    }

    @Override
    public void onExport() {
        set("channel", getChatChannel());
        set("history", getChatHistory());
    }

    /**
     * @return Returns the ChatChannel being sent.
     */
    public ChatChannel getChatChannel() {
        return this.chatChannel;
    }

    /**
     * @return Returns the ChatHistory assigned to the set ChatChannel.
     */
    public ChatHistory getChatHistory() {
        return getChatChannel().getHistory();
    }

    /**
     * Sets the ChatChannel to be sent.
     *
     * @param chatChannel The ChatChannel being sent.
     */
    private void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }
}
