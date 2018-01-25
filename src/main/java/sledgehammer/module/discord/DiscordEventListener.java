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
import sledgehammer.annotations.EventHandler;
import sledgehammer.enums.LogType;
import sledgehammer.event.Event;
import sledgehammer.event.LogEvent;
import sledgehammer.event.chat.ChatEvent;
import sledgehammer.event.chat.ChatMessageEvent;
import sledgehammer.event.core.ThrowableEvent;
import sledgehammer.event.core.command.CommandEvent;
import sledgehammer.event.core.player.CheaterEvent;
import sledgehammer.event.core.player.pvp.PVPAttackEvent;
import sledgehammer.interfaces.Listener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Command;
import sledgehammer.util.Response;
import sledgehammer.util.TickTask;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class DiscordEventListener implements Listener {

    private ModuleDiscord module;

    private long timeSinceLastException = 0L;

    /**
     * Main constructor.
     *
     * @param module The Discord Module instance using the EventListener.
     */
    DiscordEventListener(ModuleDiscord module) {
        setModule(module);
    }

    @EventHandler(id = "core.discord.event.chatmessage")
    private void on(ChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        ChatChannel chatChannel = event.getChatChannel();
        String chatChannelName = chatChannel.getChannelName().toLowerCase();
        ModuleDiscord module = getModule();
        DiscordBot bot = module.getBot();
        if (chatChannelName.equalsIgnoreCase("global")) {
            String compiled = message.getMessage();
            Player player = message.getPlayer();
            if (player != null) {
                compiled = player.getName() + ": " + message.getMessage();
            }
            bot.say(module.getPublicChannelName(), false, ChatTags.stripTags
                    (compiled, false));
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
                String channel = chatChannel.getChannelName();
                bot.say("console", false, "[" + channel + "] : " + compiled);
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
            bot.say("console", false, "[" + chatChannel.getChannelName() + "] : " + compiled);
        }
    }

    @EventHandler(id = "core.discord.event.log")
    public void on(LogEvent logEntry) {
        Player player;
        Event event = logEntry.getEvent();
        String eventType = event.getID();
        DiscordBot bot = module.getBot();
        // This event spams too much.
        if (event instanceof PVPAttackEvent) {
            return;
        }
        if (eventType.equals(CommandEvent.ID)) {
            CommandEvent command = (CommandEvent) event;
            Command com = command.getCommand();
            Response r = command.getResponse();
            player = com.getPlayer();
            String loggedMessage = command.getResponse().getLogMessage();
            if (loggedMessage == null) {
                loggedMessage = "Issued command: " + com.getRaw();
            }
            if (r.getLogType() == LogType.STAFF) {
                bot.staff(player.getUsername(), loggedMessage);
            } else if (r.getLogType() == LogType.INFO) {
                bot.info(false, loggedMessage);
            } else if (r.getLogType() == LogType.WARN || r.getLogType() == LogType.ERROR) {
                bot.warn(true, loggedMessage);
            }
        } else if (eventType.equals(ChatEvent.ID)) {
            ChatEvent chatEvent = (ChatEvent) event;
            player = chatEvent.getPlayer();
            if (chatEvent.isGlobal()) {
                if (player.getProperty("muteglobal").equals("1")) return;
                bot.say(module.getPublicChannelName(), false, ChatTags.stripTags(chatEvent
                        .getHeader() + chatEvent.getText(), false));
            } else if (!chatEvent.getText().equalsIgnoreCase("ZzzZZZzzzz")) {
                bot.say(module.getConsoleChannelName(), false, "[Local]:" + ChatTags
                        .stripTags(chatEvent.getHeader() + chatEvent.getText(), false));
            }
        } else if (eventType.equals(CheaterEvent.ID))

        {
            bot.info(true, logEntry.getLogMessage());
        } else

        {
            bot.info(false, logEntry.getLogMessage());
        }

    }

    @EventHandler(id = "core.discord.event.throwable")
    public void on(ThrowableEvent event) {
        // Grab the current time in milli-seconds.
        long timeCurrent = System.currentTimeMillis();
        // If the delta is greater than OR equal to the cooldownTime.
        if (timeCurrent - timeSinceLastException >= 3000L) {
            // Set the time.
            timeSinceLastException = timeCurrent;
            final DiscordBot bot = module.getBot();
            if (bot.isConnected()) {
                final String statementFinal = "```python\n " + event.printStackTrace() + "\n" +
                        "```";
                (new TickTask() {
                    @Override
                    public boolean run() {
                        try {
                            Channel console = bot.getConsoleChannel();
                            console.sendMessage("An error occurred:").get();
                            console.sendMessage(statementFinal).get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                }).runTaskLater(getModule(), 1);
            }
        }
    }

    public ModuleDiscord getModule() {
        return this.module;
    }

    private void setModule(ModuleDiscord module) {
        this.module = module;
    }
}
