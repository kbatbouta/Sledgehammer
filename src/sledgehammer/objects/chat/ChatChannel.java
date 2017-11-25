package sledgehammer.objects.chat;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.manager.ChatManager;
import sledgehammer.module.core.ModuleChat;
import sledgehammer.object.LuaArray;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;
import sledgehammer.objects.send.SendChatChannel;
import sledgehammer.objects.send.SendChatMessage;
import sledgehammer.objects.send.SendChatMessagePlayer;
import sledgehammer.objects.send.SendRemoveChatChannel;
import sledgehammer.objects.send.SendRenameChatChannel;

/**
 * TODO: Document.
 * Class designed to store and manage all chat messages for a channel (tab).
 * 
 * @author Jab
 */
public class ChatChannel extends LuaTable  {
	
	public static final int CHANNEL_HISTORY_SIZE = 512;

	private Map<String, Player> mapPlayersSent;
	private LinkedList<ChatMessage> listMessages;
	private SendChatChannel send;
	private SendRemoveChatChannel sendRemove;
	private SendRenameChatChannel sendRename;
	private SendChatMessage sendMessage;
	private SendChatMessagePlayer sendMessagePlayer;
	private ChatChannelComparator comparator;
	private ChannelProperties properties;
	private String channelName;
	private int id;
	
	/**
	 * Dynamic Load constructor.
	 * 
	 * @param name
	 *            The <String> name of the <ChatChannel>.
	 */
	public ChatChannel(String name) {
		super("chatChannel");
		setChannelName(name);
		init();
		properties = new ChannelProperties();
	}
	
	/**
	 * Lua Load constructor.
	 * 
	 * @param table
	 *            The <KahluaTable> that contains the information of the
	 *            <ChatChannel>.
	 */
	public ChatChannel(KahluaTable table) {
		super("chatChannel", table);
	}

	/**
	 * Main constructor.
	 * 
	 * @param name
	 *            The <String> name of the <ChatChannel>.
	 * @param description
	 *            The <String> description of the <ChatChannel>.
	 * @param context
	 *            The <String> context of the <ChatChannel>.
	 */
	public ChatChannel(String name, String description, String context) {
		super("chatChannel");
		properties = new ChannelProperties();
		setChannelName(name);
		properties.setDescription(description);
		properties.setContext(context);
	}
	
	public void init() {
		listMessages = new LinkedList<ChatMessage>();
		mapPlayersSent = new HashMap<>();
		send = new SendChatChannel(this);
		sendRemove = new SendRemoveChatChannel(this);
		sendRename = new SendRenameChatChannel();
		
		sendMessage = new SendChatMessage();
		sendMessagePlayer = new SendChatMessagePlayer();
		comparator = new ChatChannelComparator();
	}

	public void addPlayerMessage(ChatMessagePlayer chatMessagePlayer) {
		
		// Only add new messages.
		if(!listMessages.contains(chatMessagePlayer)) {
			listMessages.add(chatMessagePlayer);
			if(listMessages.size() > CHANNEL_HISTORY_SIZE) {
				listMessages.removeFirst();
			}
		}
		
		for(Player player : SledgeHammer.instance.getPlayers()) {
			if(player.hasPermission(properties.getContext())) {				
				sendMessage(chatMessagePlayer, player);
			}
		}
	}
	
	public void sendMessage(ChatMessage message, Player player) {
		sendMessage.setChatMessage(message);
		SledgeHammer.instance.send(sendMessage, player);
	}
	
	public void sendMessagePlayer(ChatMessagePlayer message, Player player) {
		sendMessagePlayer.setChatMessage(message);
		SledgeHammer.instance.send(sendMessagePlayer, player);
	}
	
	public void addMessage(ChatMessage chatMessage) {
		// Only add new messages.
		if(!listMessages.contains(chatMessage)) {
			listMessages.add(chatMessage);
			if(listMessages.size() > CHANNEL_HISTORY_SIZE) {
				listMessages.removeFirst();
			}
		}
		
		if(chatMessage instanceof ChatMessagePlayer) {			
			for(Player player : SledgeHammer.instance.getPlayers()) {
				if(this.getChannelName().toLowerCase().equals("local")) {
					if(player.isWithinLocalRange(((ChatMessagePlayer)chatMessage).getPlayer())){
						sendMessagePlayer((ChatMessagePlayer) chatMessage, player);						
					}
				} else {					
					sendMessagePlayer((ChatMessagePlayer) chatMessage, player);
				}
			}
		} else {
			for(Player player : SledgeHammer.instance.getPlayers()) {
				sendMessage(chatMessage, player);
			}
		}
	}
	
	/**
	 * Returns a List container of LuaObject_ChatMessagePlayer from a given player's ID.
	 * @param playerID
	 * @return
	 */
	public List<ChatMessagePlayer> getMessagesForPlayer(UUID uniqueId) {
		List<ChatMessagePlayer> listMessages = new LinkedList<>();
		for(ChatMessage message : this.listMessages) {
			if(message instanceof ChatMessagePlayer) {
				ChatMessagePlayer messagePlayer = (ChatMessagePlayer)message;
				if(messagePlayer.getPlayer().getMongoPlayer().getUniqueId().equals(uniqueId)) {
					listMessages.add(messagePlayer);
				}
			}
		}
		return listMessages;
	}
	
	public void deleteMessagesForPlayer(UUID uniqueId) {
		List<ChatMessagePlayer> listMessages = getMessagesForPlayer(uniqueId);
		
		this.listMessages.removeAll(listMessages);
		
		// TODO: Broadcast deleted messages.
	}
	
	public void deleteMessages(List<ChatMessage> listMessages) {
		// TODO: Broadcast deleted messages.
	}
	
	public String getChannelName() {
		return this.channelName;
	}
	
	public LuaArray<ChatMessage> getLastMessages(int amount) {
		List<ChatMessage> listLastMessages = new ArrayList<>();
		
		int size = listMessages.size();
		
		for(int index = size - 1; index >= size - amount - 1; index--) {
			if(index < 0) break;
			listLastMessages.add(listMessages.get(index));
		}
		
		Collections.sort(listLastMessages, comparator);
		
		LuaArray<ChatMessage> array = new LuaArray<>(listLastMessages);
		
		return array;
	}

	public void sendToPlayer(Player player) {
		if(canSee(player)) {
			SledgeHammer.instance.send(send);
			mapPlayersSent.put(player.getName(), player);
		}
	}
	
	public void removeAllPlayers() {
		
		for(Player player : SledgeHammer.instance.getPlayers()) {
			if(canSee(player)) {
				SledgeHammer.instance.send(sendRemove, player);
			}
		}
		
		for(Player player : mapPlayersSent.values()) {
			SledgeHammer.instance.send(sendRemove, player);
		}
		
		mapPlayersSent.clear();
	}
	
	public void removePlayer(Player player) {

		// Send a command to remove the channel.
		SledgeHammer.instance.send(sendRemove, player);
		
		// Remove the player from the list.
		mapPlayersSent.remove(player.getName());
	}
	
	private class ChatChannelComparator implements Comparator<ChatMessage> {
		@Override
		public int compare(ChatMessage a, ChatMessage b) {
			int i = 0;
			if (a.getMessageID() < b.getMessageID()) {
				i = -1;
			} else
			if (a.getMessageID() > b.getMessageID()) {
				i = 1;
			}
			return i;
		}
	}
	
	/**
	 * Renames the <ChatChannel>
	 * 
	 * @param nameNew
	 *            The <String> new name.
	 */
	public void rename(String nameNew) {
		String nameOld = getChannelName();		
		this.setChannelName(nameNew);
		sendRename.set(this, nameOld, nameNew);
		for(Player player : SledgeHammer.instance.getPlayers()) {
			if(canSee(player)) {				
				SledgeHammer.instance.send(sendRename, player);
			}
		}
		ModuleChat module = getChatModule();
		module.renameChannelDatabase(this, nameOld, nameNew);
		getChatManager().renameChatChannel(this, nameOld, nameNew);
	}
	
	/**
	 * Sets the <ChannelProperties> data for the <ChatChannel>.
	 * 
	 * @param properties
	 *            The <ChannelProperties> provided.
	 */
	public void setProperties(ChannelProperties properties) {
		this.properties = properties;
	}
	
	/**
	 * @return Returns the <ChannelProperties> data for the <ChatChannel>.
	 */
	public ChannelProperties getProperties() {
		return this.properties;
	}
	
	/**
	 * @return Returns the <ChatManager> instance for Sledgehammer.
	 */
	private ChatManager getChatManager() {
		return SledgeHammer.instance.getChatManager();
	}

	/**
	 * @return Returns the <ModuleChat> instance for Sledgehammer.
	 */
	public ModuleChat getChatModule() {
		return (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
	}

	/**
	 * Determines whether or not a <Player> can see the <ChatChannel>.
	 * 
	 * @param player
	 *            The <Player> being tested.
	 * @return Returns true if the <Player> can see the <ChatChannel>.
	 */
	public boolean canSee(Player player) {
		if(getChannelName().equalsIgnoreCase("global") || getChannelName().equalsIgnoreCase("local")) {
			return true;
		} 
		if(getChannelName().equalsIgnoreCase("espanol")) {
			String propertyEspanol = player.getProperty("espanol");
			if(propertyEspanol != null && propertyEspanol.equals("1")) {
				return true;
			}
		}
		return player.hasRawPermission(getProperties().getContext());
	}
	
	/**
	 * @param player The <Player> being tested.
	 * @return Returns true if the <ChatChannel> has already been sent to the <Player>.
	 */
	public boolean hasAlreadySentPlayer(Player player) {
		return mapPlayersSent.get(player.getName()) != null;
	}
	
	public void onDisconnect(Player player) {
		mapPlayersSent.remove(player.getName());
		// TODO: Broadcast player leaving.
	}
	
	public void editMessage() {
		//TODO: implement
	}
	
	/**
	 * If the channel has its own context permission, 
	 * then it is running as white-listed.
	 * @return
	 */
	public boolean isWhitelisted() {
		return !getProperties().getContext().equals(ChannelProperties.DEFAULT_CONTEXT);
	}
	
	public void setChannelName(String name) {
		this.channelName = name;
	}
	
	public LinkedList<ChatMessage> getAllMessages() {
		return this.listMessages;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public Collection<Player> getPlayers() {
		return mapPlayersSent.values();
	}
	
	public void addMessage(String string) {
		addMessage(new ChatMessage(string));
	}

	public void setAllMessages(LinkedList<ChatMessage> messages) {
		this.listMessages = messages;
	}

	@Override
	public void onLoad(KahluaTable table) {
		channelName = table.rawget("channelName").toString();
		// TODO: Future-Implement when clients can create channels.
	}
	
	public ChannelProperties loadChannelProperties() {
		return getChatModule().loadChannelProperties(getChannelName());
	}
	
	@Override
	public void onExport() {
		// @formatter:off
		set("channelName", getChannelName());
		set("history"    , getLastMessages(32));
		set("properties" , getProperties());
		// @formatter:on
	}
	
}