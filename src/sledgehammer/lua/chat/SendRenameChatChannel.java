package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

public class SendRenameChatChannel extends Send {

	private ChatChannel channel;
	private String nameOld;
	private String nameNew;
	
	public SendRenameChatChannel() {
		super("core.chat", "sendRenameChatChannel");
	}
	
	public void set(ChatChannel channel, String nameOld, String nameNew) {
		this.channel = channel;
		this.nameOld = nameOld;
		this.nameNew = nameNew;
	}
	
	public ChatChannel getChatChannel() {
		return this.channel;
	}
	
	public String getOldName() {
		return this.nameOld;
	}
	
	public String getNewName() {
		return this.nameNew;
	}

	@Override
	public void onExport() {
		set("channel", getChatChannel());
		set("nameNew", getNewName());
		set("nameOld", getOldName());
	}
}