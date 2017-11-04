package sledgehammer.objects.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoDatabase;
import sledgehammer.manager.ChatManager;
import sledgehammer.modules.core.ModuleChat;
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
 *
 */
public class ChatChannel extends LuaTable  {
	
	public static final int CHANNEL_HISTORY_SIZE = 512;

	private int id;
	
	private String channelName;
	
	private Map<String, Player> mapPlayersSent;

	private LinkedList<ChatMessage> listMessages;
	
	private SendChatChannel send;
	
	private SendRemoveChatChannel sendRemove;
	
	private SendRenameChatChannel sendRename;
	
	private SendChatMessage sendMessage;
	
	private SendChatMessagePlayer sendMessagePlayer;
	
	private ChatChannelComparator comparator;
	
	private ChannelProperties properties;
	
	public ChatChannel(String name) {
		super("chatChannel");
		setChannelName(name);
		init();
		properties = new ChannelProperties();
	}
	
	public ChatChannel(KahluaTable table) {
		super("chatChannel", table);
	}

	public ChatChannel(String name, String desc, String cont) {
		super("chatChannel");
		properties = new ChannelProperties();
		setChannelName(name);
		properties.setDescription(desc);
		properties.setContext(cont);
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
	
	public void setProperties(ChannelProperties properties) {
		this.properties = properties;
	}
	
	public ChannelProperties getProperties() {
		return this.properties;
	}
	
	private ChatManager getChatManager() {
		return SledgeHammer.instance.getChatManager();
	}

	private ModuleChat getChatModule() {
		return (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
	}

	public boolean canSee(Player player) {
		if(getChannelName().equalsIgnoreCase("global") || getChannelName().equalsIgnoreCase("local")) {
			return true;
		} 
		if(getChannelName().equalsIgnoreCase("espanol")) {
			if(player.getProperty("espanol").equals("1")) {
				return true;
			}
		}
		println("hasRawPermission(" + getProperties().getContext() + ") = " + player.hasRawPermission(getProperties().getContext()));
		return player.hasRawPermission(getProperties().getContext());
	}
	
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
	
	@Override
	public void onExport() {
		set("channelName", getChannelName());
		set("history"    , getLastMessages(32));
		set("properties" , getProperties());
	}
}