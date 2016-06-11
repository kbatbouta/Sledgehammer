package sledgehammer.event;

import java.util.List;

import sledgehammer.ChatManager;
import sledgehammer.wrapper.Player;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatColor.*;

public class ChatEvent extends PlayerEvent {

	public static final String ID = "ChatEvent";

	private String input;
	private String header;
	private String headerColor;
	private String textColor;
	private boolean global = false;
	private boolean say = false;
	private byte chatType = -1;
	private List<String> listMutedUsers;
	

	public ChatEvent(Player player, String input) {
		super(player);
		if(player == null) {
			throw new IllegalArgumentException("Player given is null!");
		}
		if(input == null || input.isEmpty()) {
			throw new IllegalArgumentException("Input given is null or empty!");
		}

		this.input = input;
		headerColor = COLOR_WHITE;
		textColor   = COLOR_WHITE;
		
		setHeader(player.getUsername() + ": ");
	}

	public String getText() {
		return input;
	}
	
	public String getHeader() {
		return this.header;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public void setText(String input) {
		this.input = input;
	}
	
	public String getHeaderColor() {
		return this.headerColor;
	}
	
	public void setHeaderColor(String color) {
		this.headerColor = color;
	}
	
	public String getTextColor() {
		return this.textColor;
	}
	
	public void setTextColor(String color) {
		this.textColor = color;
	}

	public void setGlobal(boolean global) {
		this.global  = global;
	}
	
	public boolean isGlobal() {
		return this.global;
	}

	public byte getChatType() {
		return this.chatType;
	}
	
	public void setChatType(byte chatType) {
		this.chatType  = chatType;
	}
	
	public List<String> getMutedUsers() {
		return this.listMutedUsers;
	}

	public void setMutedUsers(List<String> listMutedUsers) {
		this.listMutedUsers = listMutedUsers;
	}
	
	
	public boolean sayIt() {
		return this.say;
	}
	
	public void setSayIt(boolean say) {
		this.say = say;
	}

	@Override
	public String getLogMessage() {
		if(isGlobal()) {
			return "(Global) " + ChatManager.getStripped(getHeader() + getText(), false);
		} else {
			return "(Local) " + ChatManager.getStripped(getHeader() + getText(), false);
		}
	}

	@Override
	public String getID() {
		return ID;
	}
	
}
