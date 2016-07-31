package sledgehammer.manager;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

import sledgehammer.SledgeHammer;
import sledgehammer.wrapper.Player;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.sledgehammer.PacketHelper;

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

	public ChatManager(SledgeHammer sledgeHammer) {
		super(sledgeHammer);
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
	
	public String privateMessage(String commander, String username, String text) {
		Player player = getSledgeHammer().getPlayer(username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public String warnPlayer(String commander, String username, String text) {
		Player player = getSledgeHammer().getPlayer(username);
		return messagePlayer(player, "[WARNING]["+ commander + "]: ", COLOR_LIGHT_RED, text, COLOR_LIGHT_RED, true, true);
	}
	
	public String privateMessage(String commander, UdpConnection connection, String text) {
		Player player = getSledgeHammer().getPlayer(connection.username);
		return messagePlayer(player, "[PM][" + commander + "]: ", COLOR_LIGHT_GREEN, text, COLOR_LIGHT_GREEN, true, true);
	}
	
	public void localMessage(UdpConnection connection, int playerID, String text, byte chatType, byte sayIt) {
		PacketHelper.localMessage(connection, playerID, text, chatType, sayIt);
	}

	public String messagePlayer(Player player, String header, String headerColor, String text, String textColor,  boolean addTimeStamp, boolean bypassMute) {
		return PacketHelper.messagePlayer(player, header, headerColor, text, textColor, addTimeStamp, bypassMute);
	}

	public void messageGlobal(String message) {
		if(udpEngine == null) return;
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, null, null, message, null, true, false);
		}
	}
	
	public void messageGlobal(String header, String message) {
		if(udpEngine == null) return;
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, COLOR_WHITE, message, COLOR_WHITE, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor) {
		if(udpEngine == null) {
			println("UdpEngine is null in messageGlobal");
			return;
		}
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, true, false);
		}
	}
	
	public void messageGlobal(String header, String headerColor, String message, String messageColor, boolean timeStamp) {
		if(udpEngine == null) return;
		
		for (Player player : getSledgeHammer().getPlayers()) {
			messagePlayer(player, header, headerColor, message, messageColor, timeStamp, false);
		}
	}
	
	public void broadcastMessage(String message, String messageColor) {
		PacketHelper.broadcastMessage(message, messageColor);
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
