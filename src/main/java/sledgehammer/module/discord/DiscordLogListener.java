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

import sledgehammer.enums.LogType;
import sledgehammer.event.chat.ChatEvent;
import sledgehammer.event.core.player.CheaterEvent;
import sledgehammer.event.core.CommandEvent;
import sledgehammer.event.Event;
import sledgehammer.event.LogEvent;
import sledgehammer.event.core.player.pvp.PVPAttackEvent;
import sledgehammer.interfaces.LogEventListener;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

public class DiscordLogListener implements LogEventListener {

    private ModuleDiscord module;

    public DiscordLogListener(ModuleDiscord module) {
        this.module = module;
    }

    public void onLogEvent(LogEvent logEntry) {
        Player player;
        Event event = logEntry.getEvent();
        String eventType = event.getID();

        DiscordBot bot = module.getBot();
        // This event is too spammy.
        if (event instanceof PVPAttackEvent) return;

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
                bot.say(module.getPublicChannelName(), false, ChatTags.stripTags(chatEvent.getHeader() + chatEvent.getText(), false));
            } else {
                if (!chatEvent.getText().equalsIgnoreCase("ZzzZZZzzzz")) {
                    bot.say(module.getConsoleChannelName(), false, "[Local]:" + ChatTags.stripTags(chatEvent.getHeader() + chatEvent.getText(), false));
                }
            }
        } else if (eventType.equals(CheaterEvent.ID)) {
            bot.info(true, logEntry.getLogMessage());
        } else {
            bot.info(false, logEntry.getLogMessage());
        }
    }

}
