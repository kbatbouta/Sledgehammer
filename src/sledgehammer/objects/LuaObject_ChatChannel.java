package sledgehammer.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;

/**
 * 
 * Class designed to store and manage all chat messages for a channel (tab).
 * 
 * @author Jab
 *
 */
public class LuaObject_ChatChannel extends LuaObject {

	private String channelName;
	
	private String context = "sledgehammer.chat.channel";
	private List<LuaObject_ChatMessage> listMessages;
	
	public LuaObject_ChatChannel(String name) {
		super("chatChannel");
		setChannelName(name);
		listMessages = new LinkedList<>();
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

	public void addPlayerMessage(LuaObject_ChatMessagePlayer chatMessagePlayer) {
		
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
	
	public void addMessage(LuaObject_ChatMessage chatMessage) {
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
	public List<LuaObject_ChatMessagePlayer> getMessagesForPlayer(int playerID) {
		List<LuaObject_ChatMessagePlayer> listMessages = new LinkedList<>();
		for(LuaObject_ChatMessage message : this.listMessages) {
			if(message instanceof LuaObject_ChatMessagePlayer) {
				LuaObject_ChatMessagePlayer messagePlayer = (LuaObject_ChatMessagePlayer)message;
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
		List<LuaObject_ChatMessagePlayer> listMessages = getMessagesForPlayer(playerID);
		
		this.listMessages.removeAll(listMessages);
		
		// TODO: Broadcast deleted messages.
	}
	
	public void deleteMessages(List<LuaObject_ChatMessage> listMessages) {
		// TODO: Broadcast deleted messages.
	}
	
	public String getChannelName() {
		return this.channelName;
	}

	@Override
	public void construct(Map<String, Object> definitions) {
		definitions.put("channelName", getChannelName());
	}

	@Override
	public void load(KahluaTable table) {
		channelName = table.rawget("channelName").toString();
	}

	
}