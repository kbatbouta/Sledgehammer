package sledgehammer.manager;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.wrapper.Player;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

// Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * Manager class designed to handle chat-packet operations.
 * 
 * @author Jab
 *
 */
public class ChatManager extends Manager {

	public static final String NAME = "ChatManager";
	
	private UdpEngine udpEngine;

	private SledgeHammer sledgeHammer;
	
	public ChatManager(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
	}
	
	public String messagePlayer(String username, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		try {
			Player player = SledgeHammer.instance.getPlayer(username);
			if(player != null) {
				return messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);			
			} else {
				return "Player not found: " + username + ".";
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
//	public String messagePlayer(Player player, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
//		
//		if(player != null) {
//			return messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);			
//		} else {
//			return "Connection does not exist.";
//		}
//	}
	
	public String privateMessage(String commander, String username, String text) {
		Player player = sledgeHammer.getPlayer(username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayer(String commander, String username, String text) {
		Player player = sledgeHammer.getPlayer(username);
		return messagePlayer(player, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}
	
	public String privateMessage(String commander, UdpConnection connection, String text) {
		Player player = sledgeHammer.getPlayer(connection.username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public void localMessage(UdpConnection connection, int playerID, String text, byte chatType, byte sayIt) {
		ByteBufferWriter bufferWriter = connection.startPacket();
		PacketTypes.doPacket(PacketTypes.Chat, bufferWriter);
		bufferWriter.putInt(playerID);
		bufferWriter.putByte(chatType);
		bufferWriter.putUTF(text);
		bufferWriter.putByte(sayIt);
		connection.endPacketImmediate();
	}

	public String messagePlayer(Player player, String header, String headerColor, String text, String textColor,  boolean addTimeStamp, boolean bypassMute) {
		
		System.out.println("Messaging player: " + player);
		
		UdpConnection connection = player.getConnection();
		
		if(!bypassMute && player.getProperty("muteglobal").equals("1")) return "User muted their global chat.";
		
		String message = "";
		if(addTimeStamp) message += "[T]";

		if(header != null && !header.isEmpty()) message += headerColor + " " + header;
		
		if(textColor != null && !textColor.isEmpty()) message += textColor + " ";
		
		message += text + COLOR_WHITE + " ";
		
		ByteBufferWriter b2 = connection.startPacket();
		PacketTypes.doPacket(PacketTypes.ReceiveCommand, b2);
		b2.putUTF(message);
		connection.endPacketImmediate();
		
		return "Message sent.";
	}

	public void messageGlobal(String message) {
		if(udpEngine == null) return;
		for (Player player : sledgeHammer.getPlayers()) {
			messagePlayer(player, null, null, message, null, true, false);
		}
	}
	
	public void messageGlobal(String header, String message) {
		if(udpEngine == null) return;
		for (Player player : sledgeHammer.getPlayers()) {
			messagePlayer(player, header, COLOR_WHITE, message, COLOR_WHITE, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor) {
		if(udpEngine == null) {
			println("UdpEngine is null in messageGlobal");
			return;
		}
		for (Player player : sledgeHammer.getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor, boolean timeStamp) {
		if(udpEngine == null) return;
		
		for (Player player : sledgeHammer.getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, timeStamp, false);
		}
	}
	
	public void broadcastMessage(String message, String messageColor) {
		if(messageColor == null || messageColor.isEmpty()) messageColor = COLOR_LIGHT_RED;
		String messageOut = "[B]" + messageColor + " " + message;
		
		for (UdpConnection connection : udpEngine.connections) {
			ByteBufferWriter bufferWriter = connection.startPacket();
			PacketTypes.doPacket((byte) 81, bufferWriter);
			bufferWriter.putUTF(messageOut);
			connection.endPacketImmediate();
		}
	}
	
	public String messagePlayerDirty(String username, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		try {
			Player player = SledgeHammer.instance.getPlayerDirty(username);
			if(player != null) {
				return messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);			
			} else {
				return "Player not found: " + username + ".";
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	public String privateMessageDirty(String commander, String username, String text) {
		return messagePlayerDirty(username, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayerDirty(String commander, String username, String text) {
		return messagePlayerDirty(username, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}

	@Override
	public String getName() { return NAME; }

	public void setUdpEngine(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
	}

	@Override
	public void onLoad() {}

	@Override
	public void onStart() {}

	@Override
	public void onUpdate() {}

	@Override
	public void onShutDown() {}

}
