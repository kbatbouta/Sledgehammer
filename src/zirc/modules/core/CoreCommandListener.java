package zirc.modules.core;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import zirc.ZIRC;
import zirc.event.CommandEvent;
import zirc.event.LogEvent;
import zirc.interfaces.CommandListener;
import zirc.util.Chat;
import zirc.util.Result;
import zirc.wrapper.Player;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.znet.SteamUtils;
import zombie.network.PacketTypes;
import zombie.network.ServerWorldDatabase;


public class CoreCommandListener implements CommandListener {

	private ModuleCore module;

	private Map<String, String> mapContexts;
	private Map<String, String> mapTooltips;
	
	public CoreCommandListener(ModuleCore module) {
		this.module = module;
		
		mapTooltips = new HashMap<>();
		mapTooltips.put("colors"       , "Displays all supported colors on this server.");
		mapTooltips.put("pm"           , "Private messages a player. ex: /pm \"player\" \"message\"");
		mapTooltips.put("warn"         , "Warns a player. ex: /warn \"player\" \"message\"");
		mapTooltips.put("broadcast"    , "Broadcasts a message to the server. ex: /broadcast \"red\" \"message\"");
		mapTooltips.put("commitsuicide", "End your character's life.");
		mapTooltips.put("muteglobal"   , "Toggles global chat.");
		mapTooltips.put("ban"          , 
				"Bans a player. Flags:" + Chat.CHAT_LINE + 
				" -s: SteamID flag (No ID required, but must be online!) ex: /ban -U \"username\" -s" + Chat.CHAT_LINE + 
				" -S: SteamID flag (ID required!) ex: /ban -S \"11330\"" + Chat.CHAT_LINE + 
				" -U: Username flag (Required unless \"-S\" or \"-I\") ex: /ban -U \"username\"" + Chat.CHAT_LINE + 
				" -i: IP flag (No IP required, but must be online!)" + Chat.CHAT_LINE + 
				" -I: IP flag (IP required!) ex: /ban -I \"127.0.0.1\" (Note: without -U given, To undo this ban, the IP will be manditory as an argument!)");
		mapTooltips.put("unban"        , 
				"Unbans a player. Flags:" + Chat.CHAT_LINE + 
				" -U: Username flag (Required!) ex: /unban -U \"username\"" + Chat.CHAT_LINE + 
				" -S: SteamID flag (ID required!) ex: /unban -S \"11330\"" + Chat.CHAT_LINE +
				" -I: IP flag (IP required!) ex: /unban -I \"127.0.0.1\"");
		
		mapContexts = new HashMap<>();
		mapContexts.put("pm"           , "zirc.core.basic.pm"            );
		mapContexts.put("colors"       , "zirc.core.basic.colors"        );
		mapContexts.put("muteglobal"   , "zirc.core.basic.muteglobal"    );
		mapContexts.put("commitsuicide", "zirc.core.basic.commitsuicide" );
		mapContexts.put("ban"          , "zirc.core.moderation.ban"      );
		mapContexts.put("warn"         , "zirc.core.moderation.warn"     );
		mapContexts.put("unban"        , "zirc.core.moderation.unban"    );
		mapContexts.put("broadcast"    , "zirc.core.moderation.broadcast");
	}
	
	@Override
	public String onTooltip(Player player, String command) {
		if(player == null) return null;
		if(command == null || command.isEmpty()) return null;
		
		String username = player.getUsername();
		
		if(module.hasPermission(username, getPermissionContext(command))) {
			return mapTooltips.get(command);
		}
		
		return null;
	}
	
	public String[] getCommands() {
		return new String[] {
				"colors",
				"pm",
				"warn",
				"broadcast",
				"commitsuicide",
				"ban",
				"unban",
				"muteglobal",
		};
	}
	
	public String getPermissionContext(String command) {
		if(command == null) return null;
		
		command = command.toLowerCase().trim();
		
		return mapContexts.get(command);
	}

	public void onCommand(CommandEvent c) {
		Chat chat = ZIRC.instance.getChat();
		Player player = c.getPlayer();
		String username = player.getUsername();
		String command = c.getCommand();
		String[] args = c.getArguments();
		String response = null;
		if(command.startsWith("colors")) {
			if(module.hasPermission(username, getPermissionContext("colors"))) {				
				c.setResponse(Result.SUCCESS, Chat.listColors());
				return;
			} else {
				c.deny();
				return;
			}
		}
		if(command.startsWith("pm")) {
    		if(module.hasPermission(username, getPermissionContext("pm"))) {    			
    			if(args.length >= 2) {
    				String playerName = args[0];
    				String msg = "";
    				for(int x = 1; x < args.length; x++) {
    					msg += args[x] + " ";
    				}
    				msg = msg.substring(0, msg.length() - 1);
    				response = chat.privateMessage(username, playerName, msg);
    				c.setResponse(Result.SUCCESS, response);
    				c.setLoggedMessage(LogEvent.LogType.INFO, player.getUsername() + " Private-Messaged " + playerName + " with message: \"" + msg + "\".");
    				return;
    			} else {
    				response = "/pm [player] [message...]";
    			}
    		} else {
    			c.deny();
    			return;
    		}
        } else
		if (command.startsWith("warn")) {
			if(module.hasPermission(username, getPermissionContext("warn"))) {
				if (player.isAdmin()) {
					if (args.length >= 2) {
						String playerName = args[0];
						String msg = "";
						for (int x = 1; x < args.length; x++) {
							msg += args[x] + " ";
						}
						msg = msg.substring(0, msg.length() - 1);
						response = chat.warnPlayer(username, playerName, msg);
						c.setResponse(Result.SUCCESS, response);
						c.setLoggedMessage(LogEvent.LogType.STAFF,
								"WARNED " + playerName + " with message: \"" + msg + "\".");
						return;
					} else {
						response = "/warn [player] [message...]";
						c.setResponse(Result.FAILURE, response);
						return;
					}
				} else {
					response = "Permission denied.";
					c.setResponse(Result.FAILURE, response);
					return;
				}
			} else {
				c.deny();
				return;
			}
        } else
		if(command.startsWith("broadcast")) {
        	if(module.hasPermission(username, getPermissionContext("broadcast"))) {
        		if(args.length > 1) {
        			String color = Chat.getColor(args[0]);
        			if(color == null) color = Chat.CHAT_COLOR_LIGHT_RED;
        			chat.broadcastChat(args[1], color);        		
        			
        			response = "Broadcast sent.";
        			c.setResponse(Result.SUCCESS, response);
        			c.setLoggedMessage(LogEvent.LogType.STAFF, player.getUsername() + " broadcasted message: \"" + args[1] + "\".");
        			return;
        		} else {
        			response = "/broadcast \"color\" \"message\"...";
        			c.setResponse(Result.FAILURE, response);
        			return;
        		}
        	} else {
        		c.deny();
        		return;
        	}
        } else
        if(command.startsWith("commitsuicide")) {
        	if(module.hasPermission(username, getPermissionContext("commitsuicide"))) {        		
        		IsoPlayer iso = player.get();
        		if(iso != null) {        		
        			iso.setHealth(-1.0F);
        			iso.DoDeath(iso.bareHands, iso, true);
        		}
        		response = "Done.";
        		c.setResponse(Result.SUCCESS, response);
        		c.setLoggedMessage(LogEvent.LogType.INFO, player.getUsername() + " commited suicide.");
        		
        		return;
        	} else {
        		c.deny();
        		return;
        	}
        } else
        if(command.equalsIgnoreCase("ban")) {
        	if(module.hasPermission(username, getPermissionContext("ban"))) {        		
        		if(args.length > 0) {        		
        			try {
        				ban(c, args);
        				return;
        			} catch (SQLException e) {
        				ZIRC.println("SQL Error!");
        				e.printStackTrace();
        			}
        		} else {
        			response = onTooltip(c.getPlayer(),"ban");
        			c.setResponse(Result.FAILURE, response);
        			return;
        		}
        	} else {
        		c.deny();
        		return;
        	}
        } else
		if(command.equalsIgnoreCase("unban")) {
        	if(module.hasPermission(username, getPermissionContext("unban"))) {        		
        		if(args.length > 0) {        		
        			try {
        				unban(c, args);
        				return;
        			} catch (SQLException e) {
        				ZIRC.println("SQL Error!");
        				e.printStackTrace();
        			}
        		} else {
        			response = onTooltip(c.getPlayer(), "unban");
        			c.setResponse(Result.FAILURE, response);
        			return;
        		}
        	} else {
        		c.deny();
        		return;
        	}
        } else
    	if(command.equalsIgnoreCase("muteglobal")) {
    		if(module.hasPermission(username, getPermissionContext("muteglobal"))) {    			
    			response = module.toggleGlobalMute(username);
    			String toggle = "on";
    			if(chat.getGlobalMuters().contains(username.toLowerCase())) toggle = "off";
    			c.setResponse(Result.SUCCESS, response);
    			c.setLoggedMessage(LogEvent.LogType.INFO, username + " turned " + toggle + " global chat.");
    			return;
    		} else {
    			c.deny();
    			return;
    		}
		}
	}
	
	private void ban(CommandEvent c, String[] args) throws SQLException {
		String response = null;
		String commander = c.getPlayer().getUsername();
		
		if(args.length > 1) {
			String username = null;
			boolean bUsername = false;
			String IP = null;
			boolean bIP = false;
			String SteamID = null;
			boolean bSteamID = false;
			String reason = null;
			
			for(int x = 0; x < args.length; x++) {
				String arg = args[x];
				String argN = ((x + 1) < args.length)?args[x+1]:null;
				if((arg.startsWith("-U") || arg.startsWith("-u")) && argN != null && !argN.startsWith("-")) {
					bUsername = true;
					username = argN;
					x++;
				} else
				if((arg.startsWith("-R") || arg.startsWith("-r")) && argN != null && !argN.startsWith("-")) {
					reason = argN;
					x++;
				} else 
				if(arg.startsWith("-i")) {
					if(!SteamUtils.isSteamModeEnabled()) {
						bIP = true;
					} else {
						response = "Cannot infer IP-Ban in Steam mode.";
						c.setResponse(Result.FAILURE, response);
						return;
					}
				} else
				if(arg.startsWith("-s")) {
					if(SteamUtils.isSteamModeEnabled()) {
						bSteamID = true;
					} else {
						response = "Cannot infer SteamID Ban in Non-Steam mode.";
						c.setResponse(Result.FAILURE, response);
						return;
					}
				} else
				if(arg.startsWith("-I") && argN != null && !argN.startsWith("-")) {
					if(!SteamUtils.isSteamModeEnabled()) {
						bIP = true;
						IP = argN;
						x++;
					} else {
						response = "Cannot IP-Ban in Steam mode.";
						c.setResponse(Result.FAILURE, response);
						return;
					}
				} else
				if(arg.startsWith("-S") && argN != null && !argN.startsWith("-")) {
					if(SteamUtils.isSteamModeEnabled()) {
						bSteamID = true;
						SteamID = argN;
						x++;
					} else {
						response = "Cannot SteamID Ban in Non-Steam mode.";
						c.setResponse(Result.FAILURE, response);
						return;
					}
				} else
				if( (arg.startsWith("-S") || arg.startsWith("-s") || arg.startsWith("-I") || arg.startsWith("-i") || arg.startsWith("-U") || arg.startsWith("-u") || arg.startsWith("-R") || arg.startsWith("-r"))
						&& (argN == null || argN.startsWith("-")) ) {
					response = onTooltip(c.getPlayer(), "ban");
					c.setResponse(Result.FAILURE, response);
					return;
				}
			}
			
			if(!bIP && !bSteamID && !bUsername) {
				response = onTooltip(c.getPlayer(), "ban");
				c.setResponse(Result.FAILURE, response);
				return;
			}
			
			Player playerBanned = new Player(username);
			UdpConnection connectionBanned = playerBanned.getConnection();

			if(bIP && IP != null && !IP.isEmpty()) {
				if(SteamUtils.isSteamModeEnabled()) {
					response = "Cannot IP ban when the server is in Steam mode.";
					c.setResponse(Result.FAILURE, response);
					return;
				}
				
				if(reason == null) reason = "Banned. (IP)";
				
				ServerWorldDatabase.instance.banIp(IP, username==null||username.isEmpty()?"NULL":username, reason, true);
				response = "Banned IP." + username!=null?"":" You must use /unban -I \"" + IP + "\" in order to unban this IP.";
				kickUser(connectionBanned, reason);
				c.setResponse(Result.SUCCESS, response);
				c.setLoggedImportant(true);
				c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned " + username + ". IP=(" + IP + ")");
				return;
			}
			
			if(bSteamID && SteamID != null && !SteamID.isEmpty()) {
				if(!SteamUtils.isSteamModeEnabled()) {
					response = "Cannot Steam-Ban a user while NOT in Steam mode.";
					c.setResponse(Result.FAILURE, response);
					return;
				}
				
				if(!SteamUtils.isValidSteamID(SteamID)) {
					response = "Invalid SteamID: \"" + SteamID + "\".";
					c.setResponse(Result.FAILURE, response);
					return;					
				}
				
				if(reason == null) reason = "Banned. (Steam)";
				
				ServerWorldDatabase.instance.banSteamID(SteamID, username==null||username.isEmpty()?"NULL":username, reason, true);
				response = "Steam-Banned Player.";
				kickUser(connectionBanned, reason);
				c.setResponse(Result.SUCCESS, response);
				c.setLoggedImportant(true);
				c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned " + username + ". SteamID=(" + SteamID + ")");
				return;
			}
			
			if(!bUsername) {
				response = "Must have -u \"username\" to use this command!";
				c.setResponse(Result.FAILURE, response);
				return;
			}
			
			// Implied. Requires -U
			if(bIP) {
				if(SteamUtils.isSteamModeEnabled()) {
					response = "Cannot IP ban when the server is in Steam mode.";
					c.setResponse(Result.FAILURE, response);
					return;
				}
				
				if(connectionBanned == null || !connectionBanned.connected) {
					response = "User must be online in order to imply IP ban.";
					c.setResponse(Result.FAILURE, response);
					return;
				}
				IP = connectionBanned.ip;
				
				if(reason == null) reason = "Banned. (IP)";
				
				ServerWorldDatabase.instance.banIp(IP, username, reason, true);
				kickUser(connectionBanned, reason);
				response = "IP-Banned Player.";
				c.setResponse(Result.SUCCESS, response);
				c.setLoggedImportant(true);
				c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned " + username + ". IP=(" + IP + ")");
				return;
				
			} else
			if(bSteamID) {
				if(!SteamUtils.isSteamModeEnabled()) {
					response = "Cannot Steam-Ban a user while NOT in Steam mode.";
					c.setResponse(Result.FAILURE, response);
					return;
					
				}
				
				if(connectionBanned == null || !connectionBanned.connected) {
					response = "User must be online in order to imply Steam-ban.";
					c.setResponse(Result.FAILURE, response);
					return;
				}
				SteamID = "" + connectionBanned.steamID;
				
				if(!SteamUtils.isValidSteamID(SteamID)) {
					response = "Invalid SteamID: \"" + SteamID + "\".";
					c.setResponse(Result.FAILURE, response);
					return;					
				}
				
				if(reason == null) reason = "Banned. (Steam)";
				
				response = ServerWorldDatabase.instance.banSteamID(SteamID, username, reason, true);
				kickUser(connectionBanned, reason);
				c.setResponse(Result.SUCCESS, response);
				c.setLoggedImportant(true);
				c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned " + username + ". SteamID=(" + SteamID + ")");
				return;
			} else {
				if(reason == null) reason = "Banned.";
				response = ServerWorldDatabase.instance.banUser(username, true);
				kickUser(connectionBanned, reason);
				c.setResponse(Result.SUCCESS, response);
				c.setLoggedImportant(true);
				c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned " + username + ".");
				return;
			}
		} else {
			response = onTooltip(c.getPlayer(),"ban");
			c.setResponse(Result.FAILURE, response);
			return;
		}
		
	}
	
	private void kickUser(UdpConnection connection, String reason) {
		if(connection == null) return;
		if(reason == null) reason = "Kicked.";
		ByteBufferWriter bufferWriter = connection.startPacket();
		PacketTypes.doPacket((byte) 83, bufferWriter);
		bufferWriter.putUTF("You have been kicked from this server. " + reason);
		connection.endPacketImmediate();
		connection.forceDisconnect();
	}
	
	private void unban(CommandEvent c, String[] args) throws SQLException {
		
		boolean bUsername = false;
		boolean bIP = false;
		boolean bSteamID = false;
		
		String response = null;
		String commander = c.getPlayer().getUsername();
		
		String username = null;
		String IP = null;
		String SteamID = null;
		
		for(int x = 0; x < args.length; x++) {
			String arg  = args[x];
			String argN = (x + 1) < args.length ? args[x + 1] : null;
			if((arg.startsWith("-U") || arg.startsWith("-u")) && argN != null && !argN.startsWith("-")) {
				bUsername = true;
				username = argN;
			} else
			if(arg.startsWith("-I") && argN != null && !argN.startsWith("-")) {
				bIP = true;
				IP = argN;
				x++;
			} else
			if(arg.startsWith("-S") && argN != null && !argN.startsWith("-")) {
				bSteamID = true;
				SteamID = argN;
				x++;
			} else
			if( (arg.startsWith("-I") || arg.startsWith("-S") || arg.startsWith("-U") || arg.startsWith("-u"))
					&& (argN == null || argN.startsWith("-")) ) {
				response = onTooltip(c.getPlayer(), "ban");
				c.setResponse(Result.FAILURE, response);
				return;
			}
		}
		
		if(!bIP && !bSteamID && !bUsername) {
			response = onTooltip(c.getPlayer(), "unban");
			c.setResponse(Result.FAILURE, response);
			return;
		}

		if(bSteamID) {
			ServerWorldDatabase.instance.banSteamID(SteamID, username==null||username.isEmpty()?null:username, false);
			response = "SteamID unbanned.";
		}
		
		if(bIP) {
			ServerWorldDatabase.instance.banIp(IP, username==null||username.isEmpty()?null:username, (String)null, false);
			response = "IP unbanned.";
		}
		
		if(bUsername) {
			ServerWorldDatabase.instance.banUser(username, false);
			response = "Player unbanned.";
		}
		
		c.setResponse(Result.SUCCESS, response);
		c.setLoggedImportant(true);
		c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " unbanned " + username + ".");
		return;
	}
	
}
