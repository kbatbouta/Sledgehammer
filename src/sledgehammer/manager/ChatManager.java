package sledgehammer.manager;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.util.Printable;
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
public class ChatManager extends Printable {

	public static final String NAME = "ChatManager";
	
	private List<String> listGlobalMuters;
	private UdpEngine udpEngine;
	
	public ChatManager() {
		this.listGlobalMuters = new ArrayList<>();
	}
	
	public List<String> getGloballyMutedUsernames() {
		return this.listGlobalMuters;
	}
	
	public String messagePlayer(String username, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		try {
			IsoPlayer player = SledgeHammer.instance.getPlayer(username);
			if(player != null) {
				return messagePlayer(GameServer.getPlayerByUserName(username), header, headerColor, text, textColor, addTimeStamp, bypassMute);			
			} else {
				return "Player not found: " + username + ".";
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	public String messagePlayer(IsoPlayer player, String header, String headerColor, String text, String textColor, boolean addTimeStamp, boolean bypassMute) {
		UdpConnection connection = GameServer.getConnectionFromPlayer(player);
		if(connection != null) {
			return messagePlayer(connection, header, headerColor, text, textColor, addTimeStamp, bypassMute);			
		} else {
			return "Connection does not exist.";
		}
	}
	
	public String privateMessage(String commander, String username, String text) {
		return messagePlayer(username, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayer(String commander, String username, String text) {
		return messagePlayer(username, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}
	
	public String privateMessage(String commander, UdpConnection connection, String text) {
		return messagePlayer(connection, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
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

	public String messagePlayer(UdpConnection connection, String header, String headerColor, String text, String textColor,  boolean addTimeStamp, boolean bypassMute) {
		
		if(!bypassMute && listGlobalMuters.contains(connection.username.toLowerCase())) return "User muted their global chat.";
		
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
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, null, null, message, null, true, false);
		}
	}
	
	public void messageGlobal(String header, String message) {
		if(udpEngine == null) return;
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, header, COLOR_WHITE, message, COLOR_WHITE, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor) {
		if(udpEngine == null) {
			println("UdpEngine is null in messageGlobal");
			return;
		}
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, header, headerColor, message, messageColor, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor, boolean timeStamp) {
		if(udpEngine == null) return;
		for (UdpConnection connection : udpEngine.connections) {
			messagePlayer(connection, header, headerColor, message, messageColor, timeStamp, false);
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
			IsoPlayer player = SledgeHammer.instance.getPlayerDirty(username);
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

}
