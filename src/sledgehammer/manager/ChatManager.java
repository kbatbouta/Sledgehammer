package sledgehammer.manager;

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

import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.ChatMessagePlayer;
import sledgehammer.requests.RequestChatChannels;
import sledgehammer.util.ZUtil;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.sledgehammer.PacketHelper;

// Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

/**
 * Manager class designed to handle chat-packet operations.
 * 
 * @author Jab
 *
 */
public class ChatManager extends Manager implements EventListener {

	public static final String NAME = "ChatManager";
	
	private UdpEngine udpEngine;
	
	public Map<String, ChatChannel> mapChannels;
	
	public ChatManager(SledgeHammer sledgeHammer) {
		mapChannels = new HashMap<>();
	}
	
	public void startChat() {
		SledgeHammer.instance.register(this);
		addChatChannel("Global");
		addChatChannel("Local");
		addChatChannel("Faction");
		addChatChannel("Admin");
	}
	
	public void stopChat() {
		SledgeHammer.instance.unregister(this);
	}
	
	private void addChatChannel(String name) {
		ChatChannel channel = new ChatChannel(name);
		mapChannels.put(name, channel);
		
		if(SledgeHammer.instance.isStarted()) {
			broadcastChannel(channel);
		}
	}

	private void broadcastChannel(ChatChannel channel) {
		
		if(channel == null) {
			throw new IllegalArgumentException("ChatChannel given is null!");
		}
	
		// Go through each player, and verify if the chat is visible.
		for(Player player : SledgeHammer.instance.getPlayers()) {
			channel.sendToPlayer(player);
		}
	}

	public String messagePlayer(String username, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		try {
			Player player = SledgeHammer.instance.getPlayer(username);
			if(player != null) {
				return messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);			
			} else {
				return "Player not found: " + username + ".";
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	public String privateMessage(String commander, String username, String text) {
		Player player = getSledgeHammer().getPlayer(username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayer(String commander, String username, String text) {
		Player player = getSledgeHammer().getPlayer(username);
		return messagePlayer(player, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}
	
	public String privateMessage(String commander, UdpConnection connection, String text) {
		Player player = getSledgeHammer().getPlayer(connection.username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public void localMessage(UdpConnection connection, int playerID, String text, byte chatType, byte sayIt) {
		PacketHelper.localMessage(connection, playerID, text, chatType, sayIt);
	}

	public String messagePlayer(Player player, String header, String headerColor, String text, String textColor,  boolean addTimeStamp, boolean bypassMute) {
		return PacketHelper.messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);
	}

	public void messageGlobal(String message) {
		if(udpEngine == null) return;
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, null, null, message, null, true, false);
		}
	}
	
	public void messageGlobal(String header, String message) {
		if(udpEngine == null) return;
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, COLOR_WHITE, message, COLOR_WHITE, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor) {
		if(udpEngine == null) {
			println("UdpEngine is null in messageGlobal");
			return;
		}
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor, boolean timeStamp) {
		if(udpEngine == null) return;
		
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, timeStamp, false);
		}
	}
	
	public void broadcastMessage(String message, String messageColor) {
		PacketHelper.broadcastMessage(message, messageColor);
	}
	
	public String messagePlayerDirty(String username, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		try {
			Player player = SledgeHammer.instance.getPlayerDirty(username);
			if(player != null) {
				return messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);			
			} else {
				return "Player not found: " + username + ".";
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	public String privateMessageDirty(String commander, String username, String text) {
		return messagePlayerDirty(username, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayerDirty(String commander, String username, String text) {
		return messagePlayerDirty(username, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}
	
	/**
	 * Attempts to broadcast all channels to a player.
	 * @param player
	 */
	private void broadcastChannels(Player player) {
		for(ChatChannel channel : mapChannels.values()) {
			channel.sendToPlayer(player);
		}
	}


	public void setUdpEngine(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
	}

	/**
	 * @param player
	 * @return Returns the accepted List of chat channels for a given player.
	 */
	public List<ChatChannel> getChannelsForPlayer(Player player) {
		List<ChatChannel> list = new LinkedList<>();
		
		// Go through each ChatChannel.
		for(String channelName : mapChannels.keySet()) {
			
			// Grab the next channel in the list.
			ChatChannel nextChannel = mapChannels.get(channelName);
			
			// Check to make sure the player has access to this channel.
			if(player.hasPermission(nextChannel.getContext())) {
				
				// If so, then add it to the list to return.
				list.add(nextChannel);
			}
		}
		
		// Return the result list of channels for the player.
		return list;
	}

	/**
	 * Digests a player's message, sending it to other clients.
	 * @param chatMessagePlayer
	 */
	public void digestPlayerMessage(ChatMessagePlayer chatMessagePlayer) {
		
		// Grab channel.
		String channel = chatMessagePlayer.getChannel();
		
		ChatChannel chatChannel = mapChannels.get(channel);
		
		chatChannel.addPlayerMessage(chatMessagePlayer);
	}
	
	/**
	 * Digests a message, sending it to other clients.
	 * @param chatMessage
	 */
	public void digestMessage(ChatMessage chatMessage) {
		// Grab channel.
		String channel = chatMessage.getChannel();
		
		ChatChannel chatChannel = mapChannels.get(channel);
		
		chatChannel.addMessage(chatMessage);
	}

	@Override
	public String[] getTypes() {
		return new String [] {ClientEvent.ID, HandShakeEvent.ID, DisconnectEvent.ID}; 
	}

	@Override
	public void handleEvent(Event event) {
		if(event.getID() == ClientEvent.ID) {
			handleClientEvent((ClientEvent)event);
		} else if(event.getID() == HandShakeEvent.ID) {
			handleHandShakeEvent((HandShakeEvent)event);
		} else if(event.getID() == DisconnectEvent.ID) {
			handleDisconnectEvent((DisconnectEvent)event);
		}
	}

	private void handleHandShakeEvent(HandShakeEvent event) {
		// Player player = event.getPlayer();
		// println("ChatManager: onHandShake() for Player: " + player.getUsername());		
		// broadcastChannels(player);
	}

	private void handleClientEvent(ClientEvent event) {

		// Get event content.
		String module     = event.getModule();
		String command    = event.getCommand();
		Player player     = event.getPlayer();
		
		if (module.equalsIgnoreCase("core.chat")) {
			
			if(command.equalsIgnoreCase("getChatChannels")) {
				
				List<ChatChannel> channels = SledgeHammer.instance.getChatManager().getChannelsForPlayer(player);
				RequestChatChannels request = new RequestChatChannels();
				
				for(ChatChannel channel : channels) {
					request.addChannel(channel);
				}
				
				event.respond(request);
			} else if(command.equalsIgnoreCase("sendChatMessagePlayer")) {
				// Get the arguments.
				KahluaTable table = event.getTable();
				KahluaTable tableMessage = (KahluaTable) table.rawget("message");
				ChatMessagePlayer message = new ChatMessagePlayer(tableMessage, System.nanoTime());
				String channelName = (String) tableMessage.rawget("channel");
				
				ChatChannel channel = mapChannels.get(channelName);
				channel.addMessage(message);
			}
		}
//		// Chat module.
//		if(event.getModule().equals("Sledgehammer.Core.Chat")) {
//			// Client-to-Server
//			if(event.getCommand().equals("C2S")) {
//				//TODO: Handle code.
//				
//				// Create & Load LuaObject.
//				ChatMessagePlayer chatMessagePlayer = new ChatMessagePlayer(event.getTable());
//				
//				// Digest message.
//				digestPlayerMessage(chatMessagePlayer);
//			}
//		}
	}

	private void handleDisconnectEvent(DisconnectEvent event) {
		Player player = event.getPlayer();
		
		for(ChatChannel channel : this.mapChannels.values()) {
			channel.onDisconnect(player);
		}
	}

	public boolean runSecondary() { return false; }
	
	public String getName() { return NAME; }
	public void onLoad() {}
	public void onStart() {}
	public void onUpdate() {}
	public void onShutDown() {}
}
