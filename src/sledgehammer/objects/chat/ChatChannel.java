package sledgehammer.objects.chat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;
import sledgehammer.objects.send.SendChatChannel;

/**
 * TODO: Document.
 * Class designed to store and manage all chat messages for a channel (tab).
 * 
 * @author Jab
 *
 */
public class ChatChannel extends LuaTable {

	private String channelName;
	
	private String context = "sledgehammer.chat.channel";
	
	private Map<String, Player> mapPlayersSent;

	private List<ChatMessage> listMessages;
	
	private SendChatChannel send;
	
	public ChatChannel(String name) {
		super("chatChannel");
		setChannelName(name);
		listMessages = new LinkedList<>();
		mapPlayersSent = new HashMap<>();
		send = new SendChatChannel(this);
	}
	
	public ChatChannel(KahluaTable table) {
		super("chatChannel", table);
	}

	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		if(!context.equals(this.context)) {
			this.context = context;
			set("context", context);
		}
	}

	private void setChannelName(String name) {
		this.channelName = name;
	}

	public void addPlayerMessage(ChatMessagePlayer chatMessagePlayer) {
		
		// Only add new messages.
		if(!listMessages.contains(chatMessagePlayer)) {
			listMessages.add(chatMessagePlayer);
		}
		
		for(Player player : SledgeHammer.instance.getPlayers()) {
			if(player.getID() != chatMessagePlayer.getPlayerID()) {					
				SledgeHammer.instance.sendServerCommand(player, "Sledgehammer.Core.Chat", "S2C", chatMessagePlayer);
			}
		}
	}
	
	public void addMessage(ChatMessage chatMessage) {
		// Only add new messages.
		if(!listMessages.contains(chatMessage)) {
			listMessages.add(chatMessage);
		}
		
		for(Player player : SledgeHammer.instance.getPlayers()) {
			SledgeHammer.instance.sendServerCommand(player, "Sledgehammer.Core.Chat", "S2C", chatMessage);
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
				if(messagePlayer.getPlayerID() == playerID) {
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
	}

	@Override
	public void onExport() {
		set("channelName", getChannelName());
		set("context", getContext());
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

	
}