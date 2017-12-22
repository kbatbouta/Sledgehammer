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
package sledgehammer.manager.core;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.chat.ChatMessagePlayer;
import sledgehammer.lua.core.Player;
import sledgehammer.manager.Manager;
import sledgehammer.module.core.ModuleChat;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Manager class designed to handle chat-packet operations.
 * 
 * TODO: Document
 * 
 * @author Jab
 */
public class ChatManager extends Manager implements EventListener {

	public static final String NAME = "ChatManager";

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

	@Override
	public void handleEvent(Event event) {
		if (event.getID() == ClientEvent.ID) {
			handleClientEvent((ClientEvent) event);
		} else if (event.getID() == HandShakeEvent.ID) {
			handleHandShakeEvent((HandShakeEvent) event);
		} else if (event.getID() == DisconnectEvent.ID) {
			handleDisconnectEvent((DisconnectEvent) event);
		}
	}

	@Override
	public boolean runSecondary() {
		return false;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String[] getTypes() {
		return new String[] { ClientEvent.ID, HandShakeEvent.ID, DisconnectEvent.ID };
	}

	public void startChat() {
		SledgeHammer.instance.register(this);
	}

	public void stopChat() {
		SledgeHammer.instance.unregister(this);
	}

	public void addChatChannelNoBroadcast(ChatChannel channel) {
		mapChannels.put(channel.getChannelName().toLowerCase(), channel);
		ModuleChat moduleChat = (ModuleChat) SledgeHammer.instance.getPluginManager().getModule(ModuleChat.class);
		moduleChat.addChannel(channel);

	}

	public void addChatChannel(ChatChannel channel) {
		mapChannels.put(channel.getChannelName().toLowerCase(), channel);
	}

	public ChatChannel addChatChannel(String name) {
		ChatChannel channel = new ChatChannel(name);
		addChatChannel(channel);
		return channel;
	}

	/**
	 * @param player
	 * @return Returns the accepted List of chat channels for a given player.
	 */
	public List<ChatChannel> getChannelsForPlayer(Player player) {
		List<ChatChannel> list = new LinkedList<>();
		// Go through each ChatChannel.
		for (String channelName : mapChannels.keySet()) {
			// Grab the next channel in the list.
			ChatChannel nextChannel = mapChannels.get(channelName);
			// Check to make sure the player has access to this channel.
			if (player.hasPermission(nextChannel.getProperties().getContext())) {
				// If so, then add it to the list to return.
				list.add(nextChannel);
			}
		}
		// Return the result list of channels for the player.
		return list;
	}

	/**
	 * Digests a player's message, sending it to other clients.
	 * 
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
	 * 
	 * @param chatMessage
	 */
	public void digestMessage(ChatMessage chatMessage) {
		// Grab channel.
		String channel = chatMessage.getChannel();
		ChatChannel chatChannel = mapChannels.get(channel);
		chatChannel.addMessage(chatMessage);
		mapMessagesByID.put(chatMessage.getMessageID(), chatMessage);
	}

	private void handleDisconnectEvent(DisconnectEvent event) {
		Player player = event.getPlayer();
		for (ChatChannel channel : this.mapChannels.values()) {
			channel.onDisconnect(player);
		}
	}

	public void addMessageToCache(ChatMessage message) {
		ChatMessage msg = mapMessagesByID.get(message.getMessageID());
		if (msg == null) {
			mapMessagesByID.put(message.getMessageID(), message);
		}
	}

	/**
	 * Attempts to broadcast all channels to a player.
	 * 
	 * @param player
	 */
	void broadcastChannels(Player player) {
		for (ChatChannel channel : mapChannels.values()) {
			channel.sendToPlayer(player);
		}
	}

	public void saveMessage(ChatMessage chatMessage) {
		ModuleChat moduleChat = (ModuleChat) SledgeHammer.instance.getPluginManager().getModule(ModuleChat.class);
		moduleChat.saveMessage(chatMessage);
	}

	public void renameChatChannel(ChatChannel chatChannel, String nameOld, String nameNew) {
		this.mapChannels.put(nameOld.toLowerCase(), null);
		this.mapChannels.put(nameNew.toLowerCase(), chatChannel);
	}

	public void removeChatChannel(ChatChannel channel) {
		this.mapChannels.remove(channel.getChannelName().toLowerCase());
		channel.removeAllPlayers();
	}

	public Collection<ChatChannel> getChannels() {
		return mapChannels.values();
	}

	public ChatChannel getChannel(String channelName) {
		return mapChannels.get(channelName.toLowerCase());
	}

	public ChatMessage getMessageFromCache(long messageID) {
		return mapMessagesByID.get(messageID);
	}

	private void handleHandShakeEvent(HandShakeEvent event) {
	}

	private void handleClientEvent(ClientEvent event) {
	}
}