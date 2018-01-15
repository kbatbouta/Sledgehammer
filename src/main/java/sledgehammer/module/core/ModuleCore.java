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

package sledgehammer.module.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mongodb.DBCursor;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.chat.MongoPeriodicMessage;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.event.core.player.ClientEvent;
import sledgehammer.event.core.CommandEvent;
import sledgehammer.event.core.player.HandShakeEvent;
import sledgehammer.event.core.player.PlayerJoinEvent;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.core.request.RequestInfo;
import sledgehammer.lua.core.send.SendLua;
import sledgehammer.lua.core.send.SendPlayer;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.plugin.Module;
import sledgehammer.util.Command;
import sledgehammer.util.TickTask;
import zombie.network.ServerWorldDatabase;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class ModuleCore extends Module {

    // @formatter:off
	public static long LONG_SECOND = 1000L;
	public static long LONG_MINUTE = LONG_SECOND * 60L;
	public static long LONG_HOUR   = LONG_MINUTE * 60L;
	public static long LONG_DAY    = LONG_HOUR   * 24L;
	// @formatter:on

    private Map<String, MongoPeriodicMessage> mapPeriodicMessages;
    private List<MongoPeriodicMessage> listPeriodicMessages;
    private CoreCommandListener commandListener;
    private CoreEventListener eventListener;
    private MongoCollection collectionPeriodicMessages;
    private SendPlayer sendPlayer;
    private LanguagePackage lang;
    private long timeThenPeriodicMessages = 0L;
    private long timeThenCheckAccountExpire = 0L;
    private long delayCheckAccountExpire = LONG_DAY;
    private SendLua sendLuaCore;
    private File fileCoreModule;

    @Override
    public void onLoad() {
        loadLua();
        boolean langOverwrite = !isLangOverriden();
        // Make sure that the core language file(s) are provided.
        saveResourceAs("lang/core_en.yml", new File(getLanguageDirectory(), "core_en.yml"), langOverwrite);
        // Load the LanguagePackage.
        lang = new LanguagePackage(getLanguageDirectory(), "core");
        lang.load();
        sendPlayer = new SendPlayer();
        SledgehammerDatabase database = SledgeHammer.instance.getDatabase();
        collectionPeriodicMessages = database.createMongoCollection("sledgehammer_periodic_messages");
        // Initialize the listeners.
        commandListener = new CoreCommandListener(this);
        eventListener = new CoreEventListener(this);
        // Initialize the Lists & Maps.
        listPeriodicMessages = new ArrayList<>();
        mapPeriodicMessages = new HashMap<>();
        loadPeriodicMessages();
    }

    @Override
    public void onUpdate(long delta) {
        int delayPeriodicMessages = 60000;
        eventListener.getPlayerTimeStamps().clear();
        // Grab the current time.
        long timeNow = System.currentTimeMillis();
        // If it has been a minute since the last check.
        if (timeNow - timeThenPeriodicMessages > delayPeriodicMessages) {
            // Go through each PeriodicMessage instance.
            for (MongoPeriodicMessage message : listPeriodicMessages) {
                // Update the list.
                message.update();
            }
            // Set the time to reset the delta.
            timeThenPeriodicMessages = timeNow;
        }
        int days = SledgeHammer.instance.getSettings().getAccountIdleExpireTime();
        if (days > 0) {
            if (timeNow - timeThenCheckAccountExpire > delayCheckAccountExpire) {
                println("Checking for expired accounts (Inactive for over " + days + " days)");
                List<String> exclusions = SledgeHammer.instance.getSettings().getExcludedIdleAccounts();
                Map<String, Long> mapPlayers = SledgeHammer.instance.getDatabase().getAllMongoPlayers();
                for (String username : mapPlayers.keySet()) {
                    boolean skip = false;
                    if (exclusions != null) {
                        for (String ex : exclusions) {
                            if (username.equalsIgnoreCase(ex)) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (skip) {
                        continue;
                    }
                    long lastConnection = mapPlayers.get(username);
                    long d = timeNow - lastConnection;
                    if (d > (LONG_DAY * days)) {
                        // delete account.
                        println("Account: \"" + username + "\" has an expired account. (" + (d / LONG_DAY) + " days)");
                        ServerWorldDatabase.instance.removePlayer(username);
                    }
                }
                // Set the time to reset the delta.
                timeThenCheckAccountExpire = timeNow;
            }
        }
    }

    @Override
    public void onStop() {
        for (MongoPeriodicMessage message : listPeriodicMessages) {
            message.save();
        }
    }

    @Override
    public void onBuildLua(SendLua send) {
        send.append(sendLuaCore);
    }

    @Override
    public void onClientCommand(ClientEvent event) {
        // Get event content.
        // String module = event.getModule();
        String clientCommand = event.getCommand();
        final Player player = event.getPlayer();
        if (clientCommand.equalsIgnoreCase("handshake")) {
            // Grab the Lua code from all Modules and send it to the Player.
            SendLua sendLua = getPluginManager().getLua(player);
            sendLua.send();
            // We just want to ping back to the client saying we received the request.
            event.respond();
            // Create a HandShakeEvent.
            HandShakeEvent handshakeEvent = new HandShakeEvent(player);
            // Handle the event.
            SledgeHammer.instance.handle(handshakeEvent);
            SendPlayer sendPlayer = new SendPlayer(true);
            sendPlayer.setPlayer(player);
            SledgeHammer.instance.send(sendPlayer, player);
            // Delay the task of sending a PlayerJoinEvent after the handshake by one tick.
            (new TickTask() {
                @Override
                public boolean run() {
                    PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player);
                    SledgeHammer.instance.handle(playerJoinEvent);
                    return false;
                }
            }).runTask(this);

        } else if (clientCommand.equalsIgnoreCase("requestInfo")) {
            RequestInfo info = new RequestInfo();
            info.setSelf(player);
            event.respond(info);
        } else if (clientCommand.equalsIgnoreCase("sendCommand")) {
            KahluaTable command_table = (KahluaTable) event.getTable().rawget("command");
            Object oRaw = command_table.rawget("raw");
            if (oRaw == null) {
                errln("Warning: Player " + player.getName() + " sent a undefined command.");
                return;
            }
            String raw = oRaw.toString();
            Object oChannelId = command_table.rawget("channel_id");
            if (oChannelId == null) {
                errln("Warning: Player " + player.getName() + " sent a command with a undefined ChatChannel ID.");
                return;
            }
            UUID channelId = UUID.fromString(command_table.rawget("channel_id").toString());
            Command command = new Command(raw);
            command.setChannelId(channelId);
            command.setPlayer(event.getPlayer());
            command.debugPrint();
            CommandEvent _event = SledgeHammer.instance.handleCommand(command);
            ModuleChat moduleChat = getChatModule();
            Player commander = event.getPlayer();
            if (_event.isHandled()) {
                ChatMessage message = getChatModule().createChatMessage(_event.getResponse().getResponse());
                message.setPrintedTimestamp(false);
                message.setOrigin(ChatMessage.ORIGIN_SERVER, false);
                message.setChannelId(channelId, false);
                commander.sendChatMessage(message);
            } else {
                ChatMessage message = createChatMessage("Unknown command: " + command.getCommand());
                message.setPrintedTimestamp(false);
                message.setOrigin(ChatMessage.ORIGIN_SERVER, false);
                // Checks if the origin Channel is available.
                // This can sometimes be affected by the command fired.
                ChatChannel channel = getChatModule().getChatChannel(channelId);
                if (channel == null) {
                    channel = moduleChat.getGlobalChatChannel();
                }
                message.setChannelId(channel.getUniqueId(), false);
                commander.sendChatMessage(message);
            }
        }
    }

    private void loadLua() {
        File lua = getLuaDirectory();
        boolean overwrite = !isLuaOverriden();
        fileCoreModule = new File(lua, "ModuleCore.lua");
        saveResourceAs("lua/module/core/ModuleCore.lua", fileCoreModule, overwrite);
        sendLuaCore = new SendLua(fileCoreModule);
    }

    /**
     * Loads the PeriodicMessages from the MongoDB database.
     */
    private void loadPeriodicMessages() {
        DBCursor cursor = collectionPeriodicMessages.find();
        if (cursor.hasNext()) {
            MongoPeriodicMessage message = new MongoPeriodicMessage(collectionPeriodicMessages, cursor.next());
            // Add the PeriodicMessage to the collection.
            addPeriodicMessage(message);
            if (SledgeHammer.DEBUG) {
                println("Periodic Message added: " + message.getName());
            }
        }
        cursor.close();
    }

    /**
     * Adds a PeriodicMessage to the core, which will be displayed periodically to
     * all players who have global-chat enabled.
     *
     * @param periodicMessage The MongoPeriodicMessage to add.
     */
    private void addPeriodicMessage(MongoPeriodicMessage periodicMessage) {
        String name = periodicMessage.getName();
        MongoPeriodicMessage mapCheck = mapPeriodicMessages.get(name);
        if (mapCheck != null) {
            throw new IllegalArgumentException("PeriodicMessage already exists: " + name + ".");
        }
        mapPeriodicMessages.put(name, periodicMessage);
        if (!listPeriodicMessages.contains(periodicMessage)) {
            listPeriodicMessages.add(periodicMessage);
        }
    }

    /**
     * @param player The Player to update.
     */
    public void updatePlayer(Player player) {
        sendPlayer.setPlayer(player);
        SledgeHammer.instance.send(sendPlayer);
    }

    public LanguagePackage getLanguagePackage() {
        return this.lang;
    }

    public CoreCommandListener getCommandListener() {
        return this.commandListener;
    }

    public CoreEventListener getEventListener() {
        return this.eventListener;
    }
}