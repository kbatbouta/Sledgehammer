package sledgehammer.objects.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.object.LuaArray;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;
import sledgehammer.objects.send.SendChatChannel;
import sledgehammer.objects.send.SendChatMessage;
import sledgehammer.objects.send.SendChatMessagePlayer;
import sledgehammer.objects.send.SendRemoveChatChannel;

/**
 * TODO: Document.
 * Class designed to store and manage all chat messages for a channel (tab).
 * 
 * @author Jab
 *
 */
public class ChatChannel extends LuaTable  {
	
	public static final int CHANNEL_HISTORY_SIZE = 512;
	
	public static final String DEFAULT_CONTEXT = "sledgehammer.chat.channel";

	private int id;
	
	private String channelName;
	
	private String description = "";
	
	private String context = DEFAULT_CONTEXT;
	
	private Map<String, Player> mapPlayersSent;

	private LinkedList<ChatMessage> listMessages;
	
	private SendChatChannel send;
	
	private SendRemoveChatChannel sendRemove;
	
	private SendChatMessage sendMessage;
	
	private SendChatMessagePlayer sendMessagePlayer;
	
	private ChatChannelComparator comparator;
	
	private boolean showHistory = true;
	
	private boolean allowChat = true;
	
	private boolean isPublic = false;
	
	public ChatChannel(String name) {
		super("chatChannel");
		setChannelName(name);
		listMessages = new LinkedList<ChatMessage>();
		mapPlayersSent = new HashMap<>();
		send = new SendChatChannel(this);
		sendRemove = new SendRemoveChatChannel(this);
		
		sendMessage = new SendChatMessage();
		sendMessagePlayer = new SendChatMessagePlayer();
		comparator = new ChatChannelComparator();
	}
	
	public ChatChannel(KahluaTable table) {
		super("chatChannel", table);
	}

	public ChatChannel(String name, String desc, String cont) {
		super("chatChanel");
		setChannelName(name);
		setDescription(desc);
		setContext(cont);
	}
	
	public boolean showHistory() {
		return this.showHistory;
	}
	
	public void setShowHistory(boolean flag) {
		this.showHistory = flag;
	}
	
	/**
	 * If the channel has its own context permission, 
	 * then it is running as white-listed.
	 * @return
	 */
	public boolean isWhitelisted() {
		return !getContext().equals(DEFAULT_CONTEXT);
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		if(!context.equals(this.context)) {
			this.context = context;
		}
	}

	private void setChannelName(String name) {
		this.channelName = name;
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
			if(player.hasPermission(getContext())) {				
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
	public List<ChatMessagePlayer> getMessagesForPlayer(int playerID) {
		List<ChatMessagePlayer> listMessages = new LinkedList<>();
		for(ChatMessage message : this.listMessages) {
			if(message instanceof ChatMessagePlayer) {
				ChatMessagePlayer messagePlayer = (ChatMessagePlayer)message;
				if(messagePlayer.getPlayer().getID() == playerID) {
					listMessages.add(messagePlayer);
				}
			}
		}
		return listMessages;
	}
	
	public void editMessage() {
		//TODO: implement
	}
	
	public void deleteMessagesForPlayer(int playerID) {
		List<ChatMessagePlayer> listMessages = getMessagesForPlayer(playerID);
		
		this.listMessages.removeAll(listMessages);
		
		// TODO: Broadcast deleted messages.
	}
	
	public void deleteMessages(List<ChatMessage> listMessages) {
		// TODO: Broadcast deleted messages.
	}
	
	public String getChannelName() {
		return this.channelName;
	}

	@Override
	public void onLoad(KahluaTable table) {
		channelName = table.rawget("channelName").toString();
		// TODO: Future-Implement when clients can create channels.
	}

	@Override
	public void onExport() {
		set("channelName", getChannelName());
		set("context", getContext());
		set("description", getDescription());
		set("history", getLastMessages(32));
		set("public", isPublic());
		set("showHistory", showHistory());
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public void setPublic(boolean flag) {
		this.isPublic = flag;
	}

	public LinkedList<ChatMessage> getAllMessages() {
		return this.listMessages;
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

	public boolean canPlayerSee(Player player) {
		return player.hasPermission(getContext());
	}

	public void sendToPlayer(Player player) {
		if(!mapPlayersSent.containsKey(player.getName())) {
			if(canPlayerSee(player)) {
				SledgeHammer.instance.send(send);
				mapPlayersSent.put(player.getName(), player);
			}
		}
	}
	
	public boolean hasAlreadySentPlayer(Player player) {
		return mapPlayersSent.get(player.getName()) != null;
	}

	public void onDisconnect(Player player) {
		mapPlayersSent.remove(player.getName());
		
		// TODO: Broadcast player leaving.
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
	
	public void removeAllPlayers() {
		
		for(Player player : SledgeHammer.instance.getPlayers()) {
			if(this.canPlayerSee(player)) {
				SledgeHammer.instance.send(sendRemove, player);
			}
		}
		
//		for(Player player : mapPlayersSent.values()) {
//			// Send a command to remove the channel.
//			SledgeHammer.instance.send(sendRemove, player);
//		}
		// Remove all players from the list.
		mapPlayersSent.clear();
	}
	
	public void removePlayer(Player player) {
		
		// If the player has the channel loaded.
		if(mapPlayersSent.get(player.getName()) != null) {
			
			// Send a command to remove the channel.
			SledgeHammer.instance.send(sendRemove, player);
			
			// Remove the player from the list.
			mapPlayersSent.remove(player.getName());
		}
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
	
	public void setAlllowChat(boolean flag) {
		this.allowChat = flag;
	}
	
	public boolean allowsChat() {
		return this.allowChat;
	}

	public void addMessage(String string) {
		addMessage(new ChatMessage(string));
	}

	public void setAllMessages(LinkedList<ChatMessage> messages) {
		this.listMessages = messages;
	}
	
}