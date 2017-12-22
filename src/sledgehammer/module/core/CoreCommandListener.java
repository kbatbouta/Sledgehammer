package sledgehammer.module.core;

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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.LogEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.lua.chat.Broadcast;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.chat.ChatMessagePlayer;
import sledgehammer.lua.chat.Command;
import sledgehammer.lua.chat.SendBroadcast;
import sledgehammer.lua.core.Player;
import sledgehammer.manager.core.PermissionsManager;
import sledgehammer.util.ChatTags;
import sledgehammer.util.Printable;
import sledgehammer.util.Response;
import sledgehammer.util.Result;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.core.znet.SteamUtils;
import zombie.network.ServerWorldDatabase;
import zombie.sledgehammer.PacketHelper;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

public class CoreCommandListener extends Printable implements CommandListener {

	private static final boolean DEBUG = true;

	private ModuleCore module;

	private Map<String, String> mapContexts;
	private Map<String, String> mapTooltips;

	private SendBroadcast sendBroadcast;

	public static final Command commandProperties = new Command("properties");

	public CoreCommandListener(ModuleCore module) {
		this.module = module;
		sendBroadcast = new SendBroadcast((Broadcast) null);
		// @formatter:off
		mapTooltips = new HashMap<>();
		mapTooltips.put("colors", "Displays all supported colors on this server.");
		mapTooltips.put("pm", "Private messages a player. ex: /pm \"player\" \"message\"");
		mapTooltips.put("warn", "Warns a player. ex: /warn \"player\" \"message\"");
		mapTooltips.put("broadcast", "Broadcasts a message to the server. ex: /broadcast \"red\" \"message\"");
		mapTooltips.put("commitsuicide", "End your character's life.");
		mapTooltips.put("espanol", "Adds you to the Spanish chat channel.");
		mapTooltips.put("properties", "Lists a player's properties. ex: /properties rj.");
		mapTooltips.put("ban", "Bans a player. Flags:" 
				+ NEW_LINE + " -s: SteamID flag (No ID required, but must be online!) ex: /ban -U \"username\" -s" 
				+ NEW_LINE + " -S: SteamID flag (ID required!) ex: /ban -S \"11330\"" 
				+ NEW_LINE + " -U: Username flag (Required unless \"-S\" or \"-I\") ex: /ban -U \"username\""
				+ NEW_LINE + " -i: IP flag (No IP required, but must be online!)" 
				+ NEW_LINE + " -I: IP flag (IP required!) ex: /ban -I \"127.0.0.1\" (Note: without -U given, To undo this ban, the IP will be manditory as an argument!)");
		mapTooltips.put("unban", "Unbans a player. Flags:"
				+ NEW_LINE + " -U: Username flag (Required!) ex: /unban -U \"username\""
				+ NEW_LINE + " -S: SteamID flag (ID required!) ex: /unban -S \"11330\""
				+ NEW_LINE + " -I: IP flag (IP required!) ex: /unban -I \"127.0.0.1\"");
		// mapTooltips.put("muteglobal" , "Toggles global chat.");
		// mapTooltips.put("purge", "Purges zombies that are dead and not removed from
		// the list.");
		mapContexts = new HashMap<>();
		mapContexts.put("pm"           , "sledgehammer.core.basic.pm"             );
		mapContexts.put("colors"       , "sledgehammer.core.basic.colors"         );
		mapContexts.put("espanol"      , "sledgehammer.core.chat.espanol"         );
		mapContexts.put("commitsuicide", "sledgehammer.core.basic.commitsuicide"  );
		mapContexts.put("properties"   , "sledgehammer.core.moderation.properties");
		mapContexts.put("ban"          , "sledgehammer.core.moderation.ban"       );
		mapContexts.put("warn"         , "sledgehammer.core.moderation.warn"      );
		mapContexts.put("unban"        , "sledgehammer.core.moderation.unban"     );
		mapContexts.put("broadcast"    , "sledgehammer.core.moderation.broadcast" );
		PermissionsManager managerPermissions = module.getPermissionsManager();
		managerPermissions.addDefaultPlayerPermission(getPermissionNode("pm"));
		managerPermissions.addDefaultPlayerPermission(getPermissionNode("colors"));
		managerPermissions.addDefaultPlayerPermission(getPermissionNode("espanol"));
		managerPermissions.addDefaultPlayerPermission(getPermissionNode("commitsuicide"));
		// @formatter:on
	}

	@Override
	public String onTooltip(Player player, Command c) {
		String command = c.getCommand();
		if (player == null)
			return null;
		if (command == null || command.isEmpty())
			return null;
		command = command.toLowerCase();
		String context = getPermissionNode(command);
		boolean hasPermission = player.hasPermission(context);
		if (hasPermission) {
			return mapTooltips.get(command);
		}
		return null;
	}

	public String[] getCommands() {
		// @formatter:off
		return new String[] { 
				"colors", 
				"pm", 
				"warn", 
				"broadcast", 
				"commitsuicide", 
				"properties", 
				"ban", 
				"unban",
				"espanol"
				// "purge"
		};
		// @formatter:on
	}

	public String getPermissionNode(String command) {
		if (command == null)
			return null;
		command = command.toLowerCase().trim();
		return mapContexts.get(command);
	}

	public void onCommand(Command com, Response r) {
		Player player = com.getPlayer();
		String username = player.getUsername();
		String command = com.getCommand();
		String[] args = com.getArguments();
		String response = null;

		if (DEBUG) {
			println("Command fired by " + username + ": " + com.getRaw());
		}
		if (command.startsWith("espanol")) {
			ChatChannel channel = module.getChatManager().getChannel("Espanol");
			String property = player.getProperty("espanol");
			if (property != null && property.equals("1")) {
				player.setProperty("espanol", "0");
				channel.removePlayer(player);
				r.set(Result.SUCCESS, "You have been removed from the Espanol channel.");
			} else {
				player.setProperty("espanol", "1");
				channel.sendToPlayer(player);
				r.set(Result.SUCCESS, "You are now added to the Espanol channel.");
			}

			return;
		}
		if (command.startsWith("colors")) {
			if (player.hasPermission(getPermissionNode("colors"))) {
				r.set(Result.SUCCESS, ChatTags.listColors());
				return;
			} else {
				r.deny();
				return;
			}
		}
		if (command.startsWith("pm")) {
			if (player.hasPermission(getPermissionNode("pm"))) {
				if (args.length >= 2) {
					String playerName = args[0];
					IsoPlayer playerPM = SledgeHammer.instance.getIsoPlayerDirty(playerName);
					String commanderName = player.getNickname();
					if (commanderName == null) {
						commanderName = player.getUsername();
					}
					if (playerPM == null) {
						r.set(Result.FAILURE, "Could not find player: " + playerName);
						return;
					}
					String msg = com.getRaw().split(args[0])[1].trim();
					Player playerDirty = SledgeHammer.instance.getPlayerDirty(username);
					if (playerDirty != null) {
						// FIXME: Add database entry for PMs.
						ChatMessagePlayer chatMessage = new ChatMessagePlayer(com.getPlayer(), msg);
						chatMessage.setOrigin(ChatMessage.ORIGIN_CLIENT);
						chatMessage.setChannel("PMs");
						chatMessage.setTime();
						playerDirty.sendMessage(chatMessage);
						r.set(Result.SUCCESS, "Message sent.");
						r.log(LogEvent.LogType.INFO, commanderName + " Private-Messaged " + playerDirty.getName()
								+ " with message: \"" + msg + "\".");
					}
					return;
				} else {
					response = "/pm [player] [message...]";
					r.set(Result.SUCCESS, response);
					return;
				}
			} else {
				r.deny();
				return;
			}
		} else if (command.startsWith("warn")) {
			if (player.hasPermission(getPermissionNode("warn"))) {
				if (player.isAdmin()) {
					if (args.length >= 2) {
						String playerName = args[0];
						String msg = "";
						for (int x = 1; x < args.length; x++) {
							msg += args[x] + " ";
						}
						msg = msg.substring(0, msg.length() - 1);

						Player playerDirty = SledgeHammer.instance.getPlayerDirty(playerName);
						if (playerDirty != null) {
							if (playerDirty.isConnected()) {

								ChatMessagePlayer message = new ChatMessagePlayer(com.getPlayer(),
										"You have been warned. Reason: " + msg);
								playerDirty.sendMessageAllChannels(message);
								response = "Player warned.";
								r.set(Result.SUCCESS, response);
								r.log(LogEvent.LogType.STAFF,
										"WARNED " + playerDirty.getName() + " with message: \"" + msg + "\".");
								return;
							} else {
								response = "player is not Online: \"" + playerDirty.getName() + "\"";
								r.set(Result.FAILURE, response);
								return;
							}
						} else {
							response = "Player not found: " + playerName;
							r.set(Result.FAILURE, response);
							return;
						}
					} else {
						response = "/warn [player] [message...]";
						r.set(Result.FAILURE, response);
						return;
					}
				} else {
					response = "Permission denied.";
					r.set(Result.FAILURE, response);
					return;
				}
			} else {
				r.deny();
				return;
			}
		} else if (command.startsWith("broadcast")) {
			if (player.hasPermission(getPermissionNode("broadcast"))) {
				if (args.length > 1) {
					String color = ChatTags.getColor(args[0]);
					if (color == null)
						color = COLOR_LIGHT_RED;

					Broadcast broadcast = new Broadcast(args[0] + args[1]);
					sendBroadcast.setBroadcast(broadcast);

					SledgeHammer.instance.send(sendBroadcast);

					response = "Broadcast sent.";
					r.set(Result.SUCCESS, response);
					r.log(LogEvent.LogType.STAFF, player.getUsername() + " broadcasted message: \"" + args[1] + "\".");
					return;
				} else {
					response = "/broadcast \"color\" \"message\"...";
					r.set(Result.FAILURE, response);
					return;
				}
			} else {
				r.deny();
				return;
			}
		} else if (command.startsWith("commitsuicide")) {
			if (player.hasPermission(getPermissionNode("commitsuicide"))) {
				IsoPlayer iso = player.getIso();
				if (iso != null) {
					iso.setHealth(-1.0F);
					iso.DoDeath(iso.bareHands, iso, true);
				}
				response = "Done.";
				r.set(Result.SUCCESS, response);
				r.log(LogEvent.LogType.INFO, player.getUsername() + " commited suicide.");

				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("properties")) {
			if (player.hasPermission(getPermissionNode("properties"))) {
				Player playerProperties = null;

				if (args.length == 0) {
					playerProperties = player;
				} else if (args.length == 1) {
					playerProperties = SledgeHammer.instance.getPlayer(username);
				} else {
					response = onTooltip(com.getPlayer(), com);
					r.set(Result.FAILURE, response);
					return;
				}

				if (playerProperties != null) {
					Map<String, String> properties = playerProperties.getProperties();

					response = "Properties for player \"" + playerProperties + "\":" + ChatTags.NEW_LINE + " ";

					for (String key : properties.keySet()) {
						String value = properties.get(key);
						response += key + ": " + value + ChatTags.NEW_LINE + " ";
					}

					r.set(Result.SUCCESS, response);
					r.log(LogEvent.LogType.INFO,
							username + " looked up properties for player \"" + playerProperties.getUsername() + "\".");

				} else {
					response = onTooltip(com.getPlayer(), com);
					r.set(Result.FAILURE, response);
					return;
				}

			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("ban")) {
			if (player.hasPermission(getPermissionNode("ban"))) {
				if (args.length > 0) {
					ban(com, r, args);
					return;
				} else {
					response = onTooltip(com.getPlayer(), com);
					r.set(Result.FAILURE, response);
					return;
				}
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("unban")) {
			if (player.hasPermission(getPermissionNode("unban"))) {
				if (args.length > 0) {
					try {
						unban(com, r, args);
						return;
					} catch (SQLException e) {
						errorln("Database Error on command: Unban");
						e.printStackTrace();
					}
				} else {
					response = onTooltip(com.getPlayer(), com);
					r.set(Result.FAILURE, response);
					return;
				}
			} else {
				r.deny();
				return;
			}
		}
		/*
		 * else if(command.equalsIgnoreCase("muteglobal")) {
		 * if(module.hasPermission(username, getPermissionContext("muteglobal"))) {
		 * 
		 * String muted = player.getProperty("muteglobal");
		 * 
		 * if(muted.equals("1")) { muted = "0"; response = "Global mute disabled."; }
		 * else { muted = "1"; response =
		 * "Global mute enabled. To disable it, type \"/muteglobal\""; }
		 * 
		 * player.setProperty("muteglobal", muted);
		 * 
		 * String toggle = "on"; if(muted.equals("0")) toggle = "off";
		 * 
		 * r.set(Result.SUCCESS, response); r.log(LogEvent.LogType.INFO, username +
		 * " turned " + toggle + " global chat."); return; } else { r.deny(); return; }
		 * }
		 */
	}

	private void ban(Command com, Response r, String[] args) {
		String response = null;
		String commander = com.getPlayer().getUsername();

		if (args.length > 1) {
			String username = null;
			String IP = null;
			String SteamID = null;
			String reason = null;
			boolean bUsername = false;
			boolean bIP = false;
			boolean bSteamID = false;

			for (int x = 0; x < args.length; x++) {
				String arg = args[x];
				String argN = ((x + 1) < args.length) ? args[x + 1] : null;
				if ((arg.startsWith("-U") || arg.startsWith("-u")) && argN != null && !argN.startsWith("-")) {
					bUsername = true;
					username = argN;
					x++;
				} else if ((arg.startsWith("-R") || arg.startsWith("-r")) && argN != null && !argN.startsWith("-")) {
					reason = argN;
					x++;
				} else if (arg.startsWith("-i")) {
					if (!SteamUtils.isSteamModeEnabled()) {
						bIP = true;
					} else {
						response = "Cannot infer IP-Ban in Steam mode.";
						r.set(Result.FAILURE, response);
						return;
					}
				} else if (arg.startsWith("-s")) {
					if (SteamUtils.isSteamModeEnabled()) {
						bSteamID = true;
					} else {
						response = "Cannot infer SteamID Ban in Non-Steam mode.";
						r.set(Result.FAILURE, response);
						return;
					}
				} else if (arg.startsWith("-I") && argN != null && !argN.startsWith("-")) {
					if (!SteamUtils.isSteamModeEnabled()) {
						bIP = true;
						IP = argN;
						x++;
					} else {
						response = "Cannot IP-Ban in Steam mode.";
						r.set(Result.FAILURE, response);
						return;
					}
				} else if (arg.startsWith("-S") && argN != null && !argN.startsWith("-")) {
					if (SteamUtils.isSteamModeEnabled()) {
						bSteamID = true;
						SteamID = argN;
						x++;
					} else {
						response = "Cannot SteamID Ban in Non-Steam mode.";
						r.set(Result.FAILURE, response);
						return;
					}
				} else if ((arg.startsWith("-S") || arg.startsWith("-s") || arg.startsWith("-I") || arg.startsWith("-i")
						|| arg.startsWith("-U") || arg.startsWith("-u") || arg.startsWith("-R") || arg.startsWith("-r"))
						&& (argN == null || argN.startsWith("-"))) {
					response = onTooltip(com.getPlayer(), com);
					r.set(Result.FAILURE, response);
					return;
				}
			}

			if (!bIP && !bSteamID && !bUsername) {
				response = onTooltip(com.getPlayer(), com);
				r.set(Result.FAILURE, response);
				return;
			}

			Player playerBanned = SledgeHammer.instance.getPlayer(username);
			UdpConnection connectionBanned = playerBanned.getConnection();

			if (bIP && IP != null && !IP.isEmpty()) {
				if (SteamUtils.isSteamModeEnabled()) {
					response = "Cannot IP ban when the server is in Steam mode.";
					r.set(Result.FAILURE, response);
					return;
				}

				if (reason == null)
					reason = "Banned. (IP)";

				try {
					ServerWorldDatabase.instance.banIp(IP, username == null || username.isEmpty() ? "NULL" : username,
							reason, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				response = "Banned IP." + username != null ? ""
						: " You must use /unban -I \"" + IP + "\" in order to unban this IP.";
				kickUser(connectionBanned, reason);
				r.set(Result.SUCCESS, response);
				r.setLoggedImportant(true);
				r.log(LogEvent.LogType.STAFF, commander + " banned " + username + ". IP=(" + IP + ")");
				return;
			}

			if (bSteamID && SteamID != null && !SteamID.isEmpty()) {
				if (!SteamUtils.isSteamModeEnabled()) {
					response = "Cannot Steam-Ban a user while NOT in Steam mode.";
					r.set(Result.FAILURE, response);
					return;
				}
				if (!SteamUtils.isValidSteamID(SteamID)) {
					response = "Invalid SteamID: \"" + SteamID + "\".";
					r.set(Result.FAILURE, response);
					return;
				}
				if (reason == null) {
					reason = "Banned. (Steam)";
				}
				try {
					ServerWorldDatabase.instance.banSteamID(SteamID,
							username == null || username.isEmpty() ? "NULL" : username, reason, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				response = "Steam-Banned Player.";
				kickUser(connectionBanned, reason);
				r.set(Result.SUCCESS, response);
				r.setLoggedImportant(true);
				r.log(LogEvent.LogType.STAFF, commander + " banned " + username + ". SteamID=(" + SteamID + ")");
				return;
			}
			if (!bUsername) {
				response = "Must have -u \"username\" to use this command!";
				r.set(Result.FAILURE, response);
				return;
			}
			// Implied. Requires -U
			if (bIP) {
				if (SteamUtils.isSteamModeEnabled()) {
					response = "Cannot IP ban when the server is in Steam mode.";
					r.set(Result.FAILURE, response);
					return;
				}

				if (connectionBanned == null || !connectionBanned.connected) {
					response = "User must be online in order to imply IP ban.";
					r.set(Result.FAILURE, response);
					return;
				}
				IP = connectionBanned.ip;

				if (reason == null) {
					reason = "Banned. (IP)";
				}
				try {
					ServerWorldDatabase.instance.banIp(IP, username, reason, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				kickUser(connectionBanned, reason);
				response = "IP-Banned Player.";
				r.set(Result.SUCCESS, response);
				r.setLoggedImportant(true);
				r.log(LogEvent.LogType.STAFF, commander + " banned " + username + ". IP=(" + IP + ")");
				return;

			} else if (bSteamID) {
				if (!SteamUtils.isSteamModeEnabled()) {
					response = "Cannot Steam-Ban a user while NOT in Steam mode.";
					r.set(Result.FAILURE, response);
					return;

				}

				if (connectionBanned == null || !connectionBanned.connected) {
					response = "User must be online in order to imply Steam-ban.";
					r.set(Result.FAILURE, response);
					return;
				}
				SteamID = "" + connectionBanned.steamID;

				if (!SteamUtils.isValidSteamID(SteamID)) {
					response = "Invalid SteamID: \"" + SteamID + "\".";
					r.set(Result.FAILURE, response);
					return;
				}
				if (reason == null) {
					reason = "Banned. (Steam)";
				}
				try {
					response = ServerWorldDatabase.instance.banSteamID(SteamID, username, reason, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				kickUser(connectionBanned, reason);
				r.set(Result.SUCCESS, response);
				r.setLoggedImportant(true);
				r.log(LogEvent.LogType.STAFF, commander + " banned " + username + ". SteamID=(" + SteamID + ")");
				return;
			} else {
				if (reason == null) {					
					reason = "Banned.";
				}
				try {
					response = ServerWorldDatabase.instance.banUser(username, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				kickUser(connectionBanned, reason);
				r.set(Result.SUCCESS, response);
				r.setLoggedImportant(true);
				r.log(LogEvent.LogType.STAFF, commander + " banned " + username + ".");
				return;
			}
		} else {
			response = onTooltip(com.getPlayer(), com);
			r.set(Result.FAILURE, response);
			return;
		}

	}

	private void kickUser(UdpConnection connection, String reason) {
		PacketHelper.kickUser(connection, reason);
	}

	private void unban(Command com, Response r, String[] args) throws SQLException {

		boolean bUsername = false;
		boolean bIP = false;
		boolean bSteamID = false;

		String response = null;
		String commander = com.getPlayer().getUsername();

		String username = null;
		String IP = null;
		String SteamID = null;

		for (int x = 0; x < args.length; x++) {
			String arg = args[x];
			String argN = (x + 1) < args.length ? args[x + 1] : null;
			if ((arg.startsWith("-U") || arg.startsWith("-u")) && argN != null && !argN.startsWith("-")) {
				bUsername = true;
				username = argN;
			} else if (arg.startsWith("-I") && argN != null && !argN.startsWith("-")) {
				bIP = true;
				IP = argN;
				x++;
			} else if (arg.startsWith("-S") && argN != null && !argN.startsWith("-")) {
				bSteamID = true;
				SteamID = argN;
				x++;
			} else if ((arg.startsWith("-I") || arg.startsWith("-S") || arg.startsWith("-U") || arg.startsWith("-u"))
					&& (argN == null || argN.startsWith("-"))) {
				response = onTooltip(com.getPlayer(), com);
				r.set(Result.FAILURE, response);
				return;
			}
		}

		if (!bIP && !bSteamID && !bUsername) {
			response = onTooltip(com.getPlayer(), com);
			r.set(Result.FAILURE, response);
			return;
		}

		if (bSteamID) {
			ServerWorldDatabase.instance.banSteamID(SteamID, username == null || username.isEmpty() ? null : username,
					false);
			response = "SteamID unbanned.";
		}

		if (bIP) {
			ServerWorldDatabase.instance.banIp(IP, username == null || username.isEmpty() ? null : username,
					(String) null, false);
			response = "IP unbanned.";
		}

		if (bUsername) {
			ServerWorldDatabase.instance.banUser(username, false);
			response = "Player unbanned.";
		}

		r.set(Result.SUCCESS, response);
		r.setLoggedImportant(true);
		r.log(LogEvent.LogType.STAFF, commander + " unbanned " + username + ".");
		return;
	}

	@Override
	public String getName() {
		return "Core";
	}
}