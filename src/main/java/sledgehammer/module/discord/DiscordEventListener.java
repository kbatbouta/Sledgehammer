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

package sledgehammer.module.discord;

import de.btobastian.javacord.entities.Channel;
import sledgehammer.event.chat.ChatMessageEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class DiscordEventListener implements EventListener {

    private ModuleDiscord module;

    /**
     * Main constructor.
     *
     * @param module The Discord Module instance using the EventListener.
     */
    DiscordEventListener(ModuleDiscord module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getID().equals(ChatMessageEvent.ID)) {
            handleChatEvent((ChatMessageEvent) event);
        }
    }

    @Override
    public String[] getTypes() {
        return new String[]{ChatMessageEvent.ID};
    }

    @Override
    public boolean runSecondary() {
        return false;
    }

    private void handleChatEvent(ChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        ChatChannel chatChannel = event.getChatChannel();
        String chatChannelName = chatChannel.getChannelName().toLowerCase();
        if (chatChannelName.equalsIgnoreCase("global")) {
            String compiled = message.getMessage();
            Player player = message.getPlayer();
            if (player != null) {
                compiled = player.getName() + ": " + message.getMessage();
            }
            module.getBot().say(module.getPublicChannelName(), false, ChatTags.stripTags(compiled, false));
        } else {
            String compiled = message.getMessage();
            Player player = message.getPlayer();
            if (player != null) {
                compiled = player.getName() + ": " + message.getMessage();
            }
            compiled = ChatTags.stripTags(compiled, false);
            if (chatChannelName.equalsIgnoreCase("local")) {
                if (message.getMessage().equalsIgnoreCase("ZzzZZZzzzz")) {
                    return;
                }
                module.getBot().say("console", false, "[" + chatChannel.getChannelName() + "] : " + compiled);
                return;
            } else {
                String channelName = DiscordBot.toAsciiString(chatChannel.getChannelName());
                Channel channel = module.getBot().getChannel("channel_" + channelName);
                if (channel == null) {
                    channel = module.getBot().getChannel(channelName);
                }
                if (channel != null) {
                    module.getBot().say(channelName, false, compiled);
                    return;
                }
            }
            module.getBot().say("console", false, "[" + chatChannel.getChannelName() + "] : " + compiled);
        }
    }
}
