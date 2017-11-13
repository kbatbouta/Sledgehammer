package sledgehammer.event;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;

public class RequestChannelsEvent extends PlayerEvent {

	public static final String ID = "RequestChannelsEvent";
	
	private List<ChatChannel> listChatChannels;
	
	public RequestChannelsEvent(Player player) {
		super(player);
		listChatChannels = new ArrayList<>();
	}
	
	public void addChatChannel(ChatChannel chatChannel) {
		if(!listChatChannels.contains(chatChannel)) {			
			listChatChannels.add(chatChannel);
		}
	}
	
	public void removeChatChannel(ChatChannel chatChannel) {
		if(listChatChannels.contains(chatChannel)) {
			listChatChannels.remove(chatChannel);
		}
	}
	
	public List<ChatChannel> getChatChannels() {
		return this.listChatChannels;
	}

	public String getLogMessage() { return null; }
	public String getID() { return ID; }
}
