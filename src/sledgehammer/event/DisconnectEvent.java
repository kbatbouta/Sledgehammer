package sledgehammer.event;

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

import sledgehammer.wrapper.Player;
import zombie.core.raknet.UdpConnection;

public class DisconnectEvent extends PlayerEvent {

	public static final String ID = "DisconnectEvent";
	
	private String message;
	private DisconnectType disconnectType = DisconnectType.DISCONNECT_MISC;

	public DisconnectEvent(Player player) {
		super(player);
	}
	
	public DisconnectEvent(Player player, String message) {
		super(player);
		this.message = message;
	}
	
	public DisconnectEvent(Player player, DisconnectType type) {
		super(player);
		if(player == null) throw new IllegalArgumentException("Player is null!");
		this.disconnectType = type;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public static enum DisconnectType {
			DISCONNECT_USERNAME_ALREADY_LOGGED_IN,
			DISCONNECT_USERNAME_EMPTY,
			DISCONNECT_USERNAME_BANNED,
			DISCONNECT_SERVER_FULL,
			DISCONNECT_IP_BANNED,
			DISCONNECT_STEAM_BANNED,
			DISCONNECT_KICKED,
			DISCONNECT_EXITED_GAME,
			DISCONNECT_MISC,
	}

	public DisconnectType getDisconnectType() {
		return this.disconnectType;
	}

	@Override
	public String getLogMessage() {
		Player player = getPlayer();
		UdpConnection connection = player.getConnection();
		if(disconnectType == DisconnectType.DISCONNECT_USERNAME_EMPTY) {
			return connection.ip + " disconnected. (Username is Empty)";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_KICKED) {
			return player.getUsername() + " disconnected. (Kicked from Server)";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_USERNAME_BANNED) {
			return player.getUsername() + " disconnected. (Username Banned)";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_IP_BANNED) {
			return player.getUsername() + " disconnected. (IP Banned: " + connection.ip + ")";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_USERNAME_ALREADY_LOGGED_IN) {
			return player.getUsername() + " disconnected. (User already logged in)";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_STEAM_BANNED) {
			return player.getUsername() + " disconnected. (Steam ID Banned)";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_SERVER_FULL) {
			return player.getUsername() + " disconnected. (Server Full)";
		} else
		if(disconnectType == DisconnectType.DISCONNECT_MISC) {
			return player.getUsername() + " disconnected. (Unknown)";
		}
		return player.getUsername() + " disconnected.";
	}

	@Override
	public String getID() {
		return ID;
	}
}
