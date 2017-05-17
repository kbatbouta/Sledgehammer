package sledgehammer.objects.chat;

import java.util.ArrayList;
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

/**
 * TODO: Document.
 * Class designed to store and manage all chat messages for a channel (tab).
 * 
 * @author Jab
 *
 */
public class ChatChannel extends LuaTable {
	
	public static final int CHANNEL_HISTORY_SIZE = 512;
	
	public static final String DEFAULT_CONTEXT = "sledgehammer.chat.channel";

	private int id;
	
	private String channelName;
	
	private String description = "";
	
	private String context = DEFAULT_CONTEXT;
	
	private Map<String, Player> mapPlayersSent;

	private LinkedList<ChatMessage> listMessages;
	
	private SendChatChannel send;
	
	private SendChatMessage sendMessage;
	
	private SendChatMessagePlayer sendMessagePlayer;
	
	public ChatChannel(String name) {
		super("chatChannel");
		setChannelName(name);
		listMessages = new LinkedList<ChatMessage>();
		mapPlayersSent = new HashMap<>();
		send = new SendChatChannel(this);
		sendMessage = new SendChatMessage();
		sendMessagePlayer = new SendChatMessagePlayer();
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
		
		println("Adding Message: " + chatMessage.getMessage());
		
		// Only add new messages.
		if(!listMessages.contains(chatMessage)) {
			listMessages.add(chatMessage);
			if(listMessages.size() > CHANNEL_HISTORY_SIZE) {
				listMessages.removeFirst();
			}
		}
		
		if(chatMessage instanceof ChatMessagePlayer) {			
			for(Player player : SledgeHammer.instance.getPlayers()) {
				sendMessagePlayer((ChatMessagePlayer) chatMessage, player);
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
	}
	
	public LuaArray<ChatMessage> getLastMessages(int amount) {
		LuaArray<ChatMessage> listLastMessages = new LuaArray<>();
		int size = listMessages.size();
		
		for(int index = size - 1; index > size - 33; index--) {
			if(index < 0) break;
			listLastMessages.add(listMessages.get(index));
		}
		
		return listLastMessages;
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
	
}