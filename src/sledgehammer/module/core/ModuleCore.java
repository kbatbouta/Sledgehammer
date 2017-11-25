package sledgehammer.module.core;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoPeriodicMessage;
import sledgehammer.database.SledgehammerDatabase;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.CommandEvent;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.module.Module;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.Command;
import sledgehammer.objects.send.SendPlayer;
import sledgehammer.requests.RequestInfo;
import zombie.network.ServerWorldDatabase;

public class ModuleCore extends Module {

	public static final String ID      = "sledgehammer_core";
	public static final String NAME    = "Core"             ;
	public static final String MODULE  = "Core"             ;
	public static final String VERSION = "1.00"             ;

	public static long LONG_SECOND = 1000L;
	public static long LONG_MINUTE = LONG_SECOND * 60L;
	public static long LONG_HOUR   = LONG_MINUTE * 60L;
	public static long LONG_DAY    = LONG_HOUR   * 24L;
	
	private Map<String, MongoPeriodicMessage> mapPeriodicMessages;
	private List<MongoPeriodicMessage> listPeriodicMessages;

	private CoreClientListener clientListener;
	private CoreCommandListener commandListener;
	private CoreEventListener eventListener;
	private DBCollection collectionPeriodicMessages;
	private SendPlayer sendPlayer;
	
	private long timeThenPeriodicMessages   = 0L;
	private long timeThenCheckAccountExpire = 0L;
	private long delayCheckAccountExpire = LONG_DAY;
	private int delayPeriodicMessages   =   60000;
	
	public ModuleCore() {
		super();
		sendPlayer = new SendPlayer();
	}
	
	@Override
	public void onLoad() {
		SledgehammerDatabase database = SledgeHammer.instance.getDatabase();
		collectionPeriodicMessages = database.getCollection("sledgehammer_periodic_messages");
		
//		 Initialize the listeners.
		commandListener = new CoreCommandListener(this);
		eventListener   = new CoreEventListener(this);
		clientListener  = new CoreClientListener(this);
		
		// Initialize the Lists & Maps.
		listPeriodicMessages = new ArrayList<>();
		mapPeriodicMessages  = new HashMap<>();
		
		loadPeriodicMessages();
	}

	/**
	 * FIXME: Convert to MongoDB.
	 * @throws SQLException
	 */
	private void loadPeriodicMessages() {
		DBCursor cursor = collectionPeriodicMessages.find();
		if(cursor.hasNext()) {
			MongoPeriodicMessage message = new MongoPeriodicMessage(collectionPeriodicMessages, cursor.next());
			// Add the PeriodicMessage to the collection.
			addPeriodicMessage(message);
			if(SledgeHammer.DEBUG) {				
				println("Periodic Message added: " + message.getName());
			}
		}
		cursor.close();
	}

	/**
	 * Adds a PeriodicMessage to the core, which will be displayed peridically
	 * to all players who have global-chat enabled.
	 * 
	 * @param periodicMessage
	 */
	private void addPeriodicMessage(MongoPeriodicMessage periodicMessage) {
		String name = periodicMessage.getName();
		MongoPeriodicMessage mapCheck = mapPeriodicMessages.get(name);
		if(mapCheck != null) {
			throw new IllegalArgumentException("PeriodicMessage already exists: " + name + ".");
		}
		mapPeriodicMessages.put(name, periodicMessage);
		if(!listPeriodicMessages.contains(periodicMessage)) {
			listPeriodicMessages.add(periodicMessage);
		}
	}
	
	public void onUpdate(long delta) {
		eventListener.getPlayerTimeStamps().clear();
		// Grab the current time.
		long timeNow = System.currentTimeMillis();
		// If it has been a minute since the last check.
		if(timeNow - timeThenPeriodicMessages > delayPeriodicMessages) {
			// Go through each PeriodicMessage instance.
			for(MongoPeriodicMessage message : listPeriodicMessages) {
				// Update the list.
				message.update();
			}
			// Set the time to reset the delta.
			timeThenPeriodicMessages = timeNow;
		}
		short days = SledgeHammer.instance.getSettings().getAccountIdleExpireTime();
		if(days > 0) {			
			if(timeNow - timeThenCheckAccountExpire > delayCheckAccountExpire) {
				println("Checking for expired accounts (Inactive for over " + days + " days)");
				String[] exclusions = SledgeHammer.instance.getSettings().getAccountIdleExclusions();
				Map<String, Long> mapPlayers = SledgeHammer.instance.getDatabase().getAllMongoPlayers();
				for(String username: mapPlayers.keySet()) {
					boolean skip = false;
					if(exclusions != null) {							
						for(String ex: exclusions) {
							if(username.equalsIgnoreCase(ex)) {
								skip = true;
								break;
							}
						}
					}
					if(skip) continue;
					long lastConnection = mapPlayers.get(username);
					long d = timeNow - lastConnection;
					if(d > (LONG_DAY * days)) {
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

	public CoreCommandListener getCommandListener() {
		return this.commandListener;
	}
	
	public CoreEventListener getEventListener() {
		return this.eventListener;
	}

	public void onStart()  {
		register(clientListener);
	}
	
	public void onStop()   {
		for(MongoPeriodicMessage message : listPeriodicMessages) {
			message.save();
		}
		unregister(clientListener);
	}

	public void onClientCommand(ClientEvent e) {
		// Cast to proper Event sub-class.
		ClientEvent event = (ClientEvent) e;
		// Get event content.
//		String module     = event.getModule();
		String command    = event.getCommand();
		Player player     = event.getPlayer();
		if (command.equalsIgnoreCase("handshake")) {
			// We just want to ping back to the client saying we received the request.
			event.respond();
			// Create a HandShakeEvent.
			HandShakeEvent handshakeEvent = new HandShakeEvent(player);
			// Handle the event.
			SledgeHammer.instance.handle(handshakeEvent);
		} else if(command.equalsIgnoreCase("requestInfo")) {
			RequestInfo info = new RequestInfo();
			info.setSelf(player);
			event.respond(info);
		} else if(command.equalsIgnoreCase("sendCommand")) {
			KahluaTable table = (KahluaTable) e.getTable().rawget("command");
			String raw = table.rawget("raw").toString();
			String channelName = table.rawget("channel").toString();
			Command _command = new Command(raw);
			_command.setChannel(channelName);
			_command.setPlayer(e.getPlayer());
			_command.debugPrint();
			CommandEvent _event = SledgeHammer.instance.handleCommand(_command);
			if(_event.isHandled()) {				
				ChatMessage message = new ChatMessage(_event.getResponse().getResponse());
				message.setTime();
				message.setOrigin(ChatMessage.ORIGIN_SERVER);
				ChatChannel channel = getChatManager().getChannel(channelName);
				if(channel == null) {
					channelName = "Global";
				}
				message.setChannel(channelName);
				e.getPlayer().sendMessage(message);
			} else {
				ChatMessage message = new ChatMessage("Unknown command: " + command);
				message.setTime();
				message.setOrigin(ChatMessage.ORIGIN_SERVER);
				// Checks if the origin Channel is avaliable.
				// This can sometimes be affected by the command fired.
				ChatChannel channel = getChatManager().getChannel(channelName);
				if(channel == null) {
					channelName = "Global";
				}
				message.setChannel(channelName);
				e.getPlayer().sendMessage(message);
			}
		}
	}

	public void updatePlayer(Player player) {
		sendPlayer.setPlayer(player);
		SledgeHammer.instance.send(sendPlayer);
	}
	
	public void onUnload() {}
	public String getID()         { return ID     ; }
	public String getName()       { return NAME   ; }
	public String getModuleName() { return MODULE; }
	public String getVersion()    { return VERSION; }
}