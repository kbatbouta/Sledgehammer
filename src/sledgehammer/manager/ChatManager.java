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
import sledgehammer.modules.core.ModuleChat;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.ChatMessagePlayer;
import zombie.core.raknet.UdpEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private LinkedHashMap<Long, ChatMessage> mapMessagesByID;
	
	public static ChatChannel chatChannelAll = new ChatChannel("*");
	
	public ChatManager(SledgeHammer sledgeHammer) {

		mapChannels = new HashMap<>();
		
		int maxSize = 0x800;
		
		mapMessagesByID = new LinkedHashMap<Long, ChatMessage>() {
			private static final long serialVersionUID = 1L;

			@Override
	        protected boolean removeEldestEntry(final Map.Entry<Long, ChatMessage> eldest) {
	            return size() > maxSize;
	        }
		};
	}
	
	public void startChat() {
		SledgeHammer.instance.register(this);
	}
	
	public void stopChat() {
		SledgeHammer.instance.unregister(this);
	}
	
	public void addChatChannelNoBroadcast(ChatChannel channel) {
		mapChannels.put(channel.getChannelName().toLowerCase(), channel);
		ModuleChat moduleChat = (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
		moduleChat.addChannel(channel);
		
	}
	
	public void addChatChannel(ChatChannel channel) {
		mapChannels.put(channel.getChannelName().toLowerCase(), channel);
		
//		if(SledgeHammer.instance.isStarted()) {
//			broadcastChannel(channel);
//		}
	}
	
	public ChatChannel addChatChannel(String name) {
		ChatChannel channel = new ChatChannel(name);
		addChatChannel(channel);
		return channel;
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
			if(player.hasPermission(nextChannel.getProperties().getContext())) {
				
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
		
		mapMessagesByID.put(chatMessagePlayer.getMessageID(), chatMessagePlayer);
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
		
		mapMessagesByID.put(chatMessage.getMessageID(), chatMessage);
	}
	
	public void saveMessage(ChatMessage chatMessage) {
		ModuleChat moduleChat = (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
		moduleChat.saveMessage(chatMessage);
	}
	
	/**
	 * Attempts to broadcast all channels to a player.
	 * @param player
	 */
	void broadcastChannels(Player player) {
		for(ChatChannel channel : mapChannels.values()) {
			channel.sendToPlayer(player);
		}
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
		
//		if (module.equalsIgnoreCase("core.chat")) {
//			
//			if(command.equalsIgnoreCase("getChatChannels")) {
//				
//				List<ChatChannel> channels = SledgeHammer.instance.getChatManager().getChannelsForPlayer(player);
//				RequestChatChannels request = new RequestChatChannels();
//				
//				for(ChatChannel channel : channels) {
//					request.addChannel(channel);
//				}
//				
//				event.respond(request);
//			} else if(command.equalsIgnoreCase("sendChatMessagePlayer")) {
//				// Get the arguments.
//				KahluaTable table = event.getTable();
//				KahluaTable tableMessage = (KahluaTable) table.rawget("message");
//				ChatMessagePlayer message = new ChatMessagePlayer(tableMessage, System.nanoTime());
//				String channelName = (String) tableMessage.rawget("channel");
//				
//				ChatChannel channel = mapChannels.get(channelName);
//				channel.addMessage(message);
//			}
//		}
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
	
	public ChatChannel getChannel(String channelName) {
		return mapChannels.get(channelName.toLowerCase());
	}

	public ChatMessage getMessageFromCache(long messageID) {
		return mapMessagesByID.get(messageID);
	}
	
	public void addMessageToCache(ChatMessage message) {
		ChatMessage msg = mapMessagesByID.get(message.getMessageID());
		if(msg == null) {
			mapMessagesByID.put(message.getMessageID(), message);
		}
	}
	
	public void removeChatChannel(ChatChannel channel) {
		this.mapChannels.remove(channel.getChannelName().toLowerCase());
		channel.removeAllPlayers();
	}

	public Collection<ChatChannel> getChannels() {
		return mapChannels.values();
	}

	public boolean runSecondary() { return false; }
	
	public String getName() { return NAME; }
	public void onLoad() {}
	public void onStart() {}
	public void onUpdate() {}
	public void onShutDown() {}

	public void renameChatChannel(ChatChannel chatChannel, String nameOld, String nameNew) {
		this.mapChannels.put(nameOld.toLowerCase(), null);
		this.mapChannels.put(nameNew.toLowerCase(), chatChannel);
	}
	
/*	*//**
	 * @deprecated LEGACY FORMAT
	 * @param username
	 * @param header
	 * @param headerColor
	 * @param text
	 * @param textColor
	 * @param addTimeStamp
	 * @param bypassMute
	 * @return
	 *//*
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
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param commander
	 * @param username
	 * @param text
	 * @return
	 *//*
	public String privateMessage(String commander, String username, String text) {
		Player player = getSledgeHammer().getPlayer(username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param commander
	 * @param username
	 * @param text
	 * @return
	 *//*
	public String warnPlayer(String commander, String username, String text) {
		Player player = getSledgeHammer().getPlayer(username);
		return messagePlayer(player, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param commander
	 * @param connection
	 * @param text
	 * @return
	 *//*
	public String privateMessage(String commander, UdpConnection connection, String text) {
		Player player = getSledgeHammer().getPlayer(connection.username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param connection
	 * @param playerID
	 * @param text
	 * @param chatType
	 * @param sayIt
	 *//*
	public void localMessage(UdpConnection connection, int playerID, String text, byte chatType, byte sayIt) {
		PacketHelper.localMessage(connection, playerID, text, chatType, sayIt);
	}

	*//**
	 * @deprecated LEGACY FORMAT
	 * @param player
	 * @param header
	 * @param headerColor
	 * @param text
	 * @param textColor
	 * @param addTimeStamp
	 * @param bypassMute
	 * @return
	 *//*
	public String messagePlayer(Player player, String header, String headerColor, String text, String textColor,  boolean addTimeStamp, boolean bypassMute) {
		if(player.isConnected()) {			
			return PacketHelper.messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);
		}
		return null;
	}

	*//**
	 * @deprecated LEGACY FORMAT
	 * @param message
	 *//*
	public void messageGlobal(String message) {
		if(udpEngine == null) return;
		for (Player player : getSledgeHammer().getPlayers()) {
			if(player.isConnected()) {
				messagePlayer(player, null, null, message, null, true, false);
			}
		}
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param header
	 * @param message
	 *//*
	public void messageGlobal(String header, String message) {
		if(udpEngine == null) return;
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, COLOR_WHITE, message, COLOR_WHITE, true, false);
		}
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param header
	 * @param headerColor
	 * @param message
	 * @param messageColor
	 *//*
	public void messageGlobal(String header, String headerColor, String message, String messageColor) {
		if(udpEngine == null) {
			println("UdpEngine is null in messageGlobal");
			return;
		}
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, true, false);
		}
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param header
	 * @param headerColor
	 * @param message
	 * @param messageColor
	 * @param timeStamp
	 *//*
	public void messageGlobal(String header, String headerColor, String message, String messageColor, boolean timeStamp) {
		if(udpEngine == null) return;
		
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, timeStamp, false);
		}
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param message
	 * @param messageColor
	 *//*
	public void broadcastMessage(String message, String messageColor) {
		PacketHelper.broadcastMessage(message, messageColor);
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param username
	 * @param header
	 * @param headerColor
	 * @param text
	 * @param textColor
	 * @param addTimeStamp
	 * @param bypassMute
	 * @return
	 *//*
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
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param commander
	 * @param username
	 * @param text
	 * @return
	 *//*
	public String privateMessageDirty(String commander, String username, String text) {
		return messagePlayerDirty(username, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	*//**
	 * @deprecated LEGACY FORMAT
	 * @param commander
	 * @param username
	 * @param text
	 * @return
	 *//*
	public String warnPlayerDirty(String commander, String username, String text) {
		return messagePlayerDirty(username, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}*/
	
	
}
