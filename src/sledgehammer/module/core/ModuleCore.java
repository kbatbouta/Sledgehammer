/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
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
import sledgehammer.event.ClientEvent;
import sledgehammer.event.CommandEvent;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.RequestInfo;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.core.SendPlayer;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.plugin.Module;
import sledgehammer.util.Command;
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
	private int delayPeriodicMessages = 60000;

	@Override
	public void onLoad() {

		File directory = getModuleDirectory();
		if (!directory.exists()) {
			directory.mkdirs();
		}
		File directoryLang = new File(directory, "lang");
		if(!directoryLang.exists()) {
			directoryLang.mkdirs();
		}
		saveResourceAs("lang/core_en.yml", "lang/core_en.yml", false);

		lang = new LanguagePackage(directoryLang, "core");
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
					if (skip)
						continue;
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
	public void onStart() {
	}

	@Override
	public void onStop() {
		for (MongoPeriodicMessage message : listPeriodicMessages) {
			message.save();
		}
	}

	@Override
	public void onClientCommand(ClientEvent e) {
		// Cast to proper Event sub-class.
		ClientEvent event = (ClientEvent) e;
		// Get event content.
		// String module = event.getModule();
		String command = event.getCommand();
		Player player = event.getPlayer();
		if (command.equalsIgnoreCase("handshake")) {
			// We just want to ping back to the client saying we received the request.
			event.respond();
			// Create a HandShakeEvent.
			HandShakeEvent handshakeEvent = new HandShakeEvent(player);
			// Handle the event.
			SledgeHammer.instance.handle(handshakeEvent);
		} else if (command.equalsIgnoreCase("requestInfo")) {
			RequestInfo info = new RequestInfo();
			info.setSelf(player);
			event.respond(info);
		} else if (command.equalsIgnoreCase("sendCommand")) {
			KahluaTable command_table = (KahluaTable) e.getTable().rawget("command");
			Object oRaw = command_table.rawget("raw");
			if (oRaw == null) {
				errorln("Warning: Player " + player.getName() + " sent a undefined command.");
				return;
			}
			String raw = oRaw.toString();
			Object oChannelId = command_table.rawget("channel_id");
			if (oChannelId == null) {
				errorln("Warning: Player " + player.getName() + " sent a command with a undefined ChatChannel ID.");
				return;
			}
			UUID channelId = UUID.fromString(command_table.rawget("channel_id").toString());
			Command _command = new Command(raw);
			_command.setChannelId(channelId);
			_command.setPlayer(e.getPlayer());
			_command.debugPrint();
			CommandEvent _event = SledgeHammer.instance.handleCommand(_command);
			ModuleChat moduleChat = getChatModule();
			Player _player = e.getPlayer();
			if (_event.isHandled()) {
				ChatMessage message = getChatModule().createChatMessage(_event.getResponse().getResponse());
				message.setPrintedTimestamp(false);
				message.setOrigin(ChatMessage.ORIGIN_SERVER, false);
				ChatChannel channel = moduleChat.getChatChannel(channelId);
				if (channel == null) {
					channel = moduleChat.getGlobalChatChannel();
				}
				message.setChannelId(channelId, false);
				_player.sendChatMessage(message);
			} else {
				ChatMessage message = createChatMessage("Unknown command: " + command);
				message.setPrintedTimestamp(false);
				message.setOrigin(ChatMessage.ORIGIN_SERVER, false);
				// Checks if the origin Channel is available.
				// This can sometimes be affected by the command fired.
				ChatChannel channel = getChatModule().getChatChannel(channelId);
				if (channel == null) {
					channel = moduleChat.getGlobalChatChannel();
				}
				message.setChannelId(channel.getUniqueId(), false);
				_player.sendChatMessage(message);
			}
		}
	}

	/**
	 * FIXME: Convert to MongoDB.
	 * 
	 * @throws SQLException
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
	 * @param periodicMessage
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

	public void updatePlayer(Player player) {
		sendPlayer.setPlayer(player);
		SledgeHammer.instance.send(sendPlayer);
	}

	public CoreCommandListener getCommandListener() {
		return this.commandListener;
	}

	public CoreEventListener getEventListener() {
		return this.eventListener;
	}
}