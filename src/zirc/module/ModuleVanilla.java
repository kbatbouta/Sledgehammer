package zirc.module;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zirc.ZIRC;
import zirc.event.CommandEvent;
import zirc.event.LogEvent;
import zirc.event.CommandEvent.Result;
import zirc.interfaces.CommandListener;
import zirc.interfaces.LogListener;
import zirc.wrapper.Player;
import zombie.AmbientStreamManager;
import zombie.GameTime;
import zombie.VirtualZombieManager;
import zombie.ZombieConfig;
import zombie.ZombiePopulationManager;
import zombie.Lua.LuaManager;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory;
import zombie.core.Rand;
import zombie.core.logger.LoggerManager;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.core.znet.SteamGameServer;
import zombie.core.znet.SteamUtils;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.iso.objects.RainManager;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.ServerMap;
import zombie.network.ServerOptions;
import zombie.network.ServerWorldDatabase;
import zombie.scripting.ScriptManager;
import zombie.scripting.objects.Item;

public class ModuleVanilla extends Module {

	CommandListener commandListener;
	LogListener logListener;
	
	public void onLoad() {
		commandListener = new VanillaCommandListener();
		logListener = new VanillaLogListener();
		register(logListener);
	}
	
	public CommandListener getCommandListener() {
		return commandListener;
	}
	
	public void setCommandListener(CommandListener listener) {
		this.commandListener = listener;
	}
	
	public String getModuleName()  { return "Vanilla"; }
	public String getVersion()     { return "1.00";    }

	public void onStart() {}
	public void onUpdate(long delta) {}
	public void onStop() {}
	public void onUnload() {}
	
	public class VanillaCommandListener implements CommandListener {

		@Override
		public String[] getCommands() { 
			return new String[] {
				// Client commands
				"roll",
				"changepwd",
				"card",
				// Admin commands
				"addalltowhitelist",
				"additem",
				"adduser",
				"addusertowhitelist",
				"addxp",
				"alarm",
				//"banid",
				//"banuser",
				"changeoption",
				"chopper",
				"createhorde",
				"disconnect",
				"godmod",
				"grantadmin",
				"gunshot",
				"invisible",
				"kickuser",
				"noclip",
				"players",
				"quit",
				"reloadlua",
				"reload",
				"reloadOptions",
				"removeadmin",
				"removeuserfromwhitelist",
				"save",
				"sendpulse",
				"showoptions",
				"startrain",
				"stoprain",
				"teleport",
				"thunder",
				//"unbanid",
				//"unbanuser"
			}; 
		}

		@Override
		public void onCommand(CommandEvent c) {
			Player player = c.getPlayer();
			if(player.isAdmin()) {
				try {
					onServerCommand(c);
				} catch (SQLException e) {
					c.setResponse(Result.FAILURE, "SQL Error (Internal Error)");
					ZIRC.println(e.getMessage());
					e.printStackTrace();
				}
			}
			if(c.getResponse() == null) {			
				onClientCommand(c);
			}
			return;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onServerCommand(CommandEvent c) throws SQLException {
			Player player = c.getPlayer();
			IsoPlayer isoCommander = player.get();
			ByteBufferWriter bufferWriter = null;
			String command = c.getCommand();
			String commander = player.getUsername();
			String[] args = c.getArguments();
			String response = null;
			String username = null;
			UdpEngine udpEngine = ZIRC.instance.getUdpEngine();
			UdpConnection connection = null;
			
			if (command.equalsIgnoreCase("quit")) {
				ZIRC.instance.stop();
				ServerMap.instance.QueueSaveAll();
	            ServerMap.instance.QueueQuit();
	            c.setResponse(Result.SUCCESS, "Quiting...");
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " closed the server.");
	            return;
			} else
			if (command.startsWith("adduser")) {
				if (args.length != 2) {
					c.setResponse(Result.FAILURE,
							(String) ServerOptions.adminOptionsList.get("adduser"));
					return;
				} else {
					username = args[0];
					String password = args[1];
					if (!ServerWorldDatabase.isValidUserName(username)) {
						response = "Invalid username \"" + username + "\"";
						c.setResponse(Result.FAILURE, response);
						return;
					} else {
						response = ServerWorldDatabase.instance.addUser(username.trim(), password.trim());
						c.setResponse(Result.SUCCESS, response);
						c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " created user " + username.trim() + ".");
						return;
					}
				}
			} else
			if (command.startsWith("banuser")) {
				String reason = null;
				String result = null;
				boolean flagIP = false;
				
				if(args.length >= 2 && args.length <= 5) {
	                username = args[1];
	                if(username != null && !username.equals("")) {
	                   reason = "";
	                   flagIP = false;
	                   if(args[1].contains("-ip")) {
	                      flagIP = true;
	                   }

	                   for(int x = 2; x < args.length; x++) {
	                      if(args[x].equals("-r")) {
	                         if(args.length <= x + 1) {
	                        	 response = (String)ServerOptions.adminOptionsList.get("banuser");
	                        	 c.setResponse(Result.FAILURE, response);
	                        	 return;
	                         }

	                         reason = " Reason : " + args[x + 1];
	                      }
	                   }

	                   result = ServerWorldDatabase.instance.banUser(username, true);
	                   c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned user " + username + (reason != null?reason:""));
	                   c.setLoggedImportant(true);

	                   for(int connection1 = 0; connection1 < udpEngine.connections.size(); ++connection1) {
	                      connection = (UdpConnection)udpEngine.connections.get(connection1);
	                      if(connection.username.equals(username)) {
	                    	  
	                         if(!SteamUtils.isSteamModeEnabled() && flagIP) {
	                            LoggerManager.getLogger("admin").write(commander + " banned ip " + connection.ip + "(" + connection.username + ")" + (reason != null?reason:""), "IMPORTANT");
	                            ServerWorldDatabase.instance.banIp(connection.ip, username, reason.replaceFirst(" Reason : ", ""), true);
	                         }

	                         if(SteamUtils.isSteamModeEnabled()) {
	                            LoggerManager.getLogger("admin").write(commander + " banned steamid " + connection.steamID + "(" + connection.username + ")" + (reason != null?reason:""), "IMPORTANT");
	                            String var54 = SteamUtils.convertSteamIDToString(connection.steamID);
	                            ServerWorldDatabase.instance.banSteamID(var54, reason.replaceFirst(" Reason : ", ""), true);
	                         }

	                         bufferWriter = connection.startPacket();
	                         PacketTypes.doPacket((byte)83, bufferWriter);
	                         bufferWriter.putUTF("You have been banned from this server." + reason);
	                         connection.endPacketImmediate();
	                         connection.forceDisconnect();
	                         break;
	                      }
	                   }
	                   c.setResponse(Result.SUCCESS, result);
	                   c.setLoggedMessage(LogEvent.LogType.STAFF, flagIP?"IP Banned " + username + "." : "Banned " + username + ".");
	                   c.setLoggedImportant(true);
	                   return;
	                } else {
	                	response = (String)ServerOptions.adminOptionsList.get("banuser");
	                	c.setResponse(Result.FAILURE, response);
	                	return;
	                }
	             } else {
	            	 response = (String)ServerOptions.adminOptionsList.get("banuser");
	             	c.setResponse(Result.FAILURE, response);
	             	return;
	             }
			} else
			if (command.startsWith("unbanuser")) {
				String result = null;
				username = args[0];
	            if(username != null && !username.isEmpty()) {
	               result = ServerWorldDatabase.instance.banUser(username, false);
	               if(!SteamUtils.isSteamModeEnabled()) {
	                  ServerWorldDatabase.instance.banIp((String)null, username, (String)null, false);

	                  for(int player1 = 0; player1 < udpEngine.connections.size(); ++player1) {
	                     connection = (UdpConnection)udpEngine.connections.get(player1);
	                     if(connection.username.equals(username)) {
	                        ServerWorldDatabase.instance.banIp(connection.ip, username, (String)null, false);
	                        break;
	                     }
	                  }
	               }
	               c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " unbanned " + username + ".");
	               c.setLoggedImportant(true);
	               c.setResponse(Result.SUCCESS, result);
	               return;
	            } else {
	            	c.setResponse(Result.FAILURE, (String)ServerOptions.adminOptionsList.get("unbanuser"));
	               return;
	            }
	       
			} else
			if (command.startsWith("banid")) {
				if(args.length != 1) {
					response = (String)ServerOptions.adminOptionsList.get("banid");
					c.setResponse(Result.FAILURE, response);
					return;
	             } else if(!SteamUtils.isSteamModeEnabled()) {
	            	 response = "Server is not in Steam mode";
	 				c.setResponse(Result.FAILURE, response);            	 
	            	 return;
	             } else {
	                String id = args[0].trim();
	                if(id.isEmpty()) {
	                	response = (String)ServerOptions.adminOptionsList.get("banid");
	    				c.setResponse(Result.FAILURE, response);
	                	return;
	                } else if(!SteamUtils.isValidSteamID(id)) {
	                	response = "Expected SteamID but got \"" + id + "\"";
	    				c.setResponse(Result.FAILURE, response);
	    				return;
	                } else {
	                   ServerWorldDatabase.instance.banSteamID(id, "", true);
	                   long var57 = SteamUtils.convertStringToSteamID(id);

	                   for(int var39 = 0; var39 < udpEngine.connections.size(); var39++) {
	                      connection = (UdpConnection)udpEngine.connections.get(var39);
	                      if(connection.steamID == var57) {
	                         bufferWriter = connection.startPacket();
	                         PacketTypes.doPacket((byte)83, bufferWriter);
	                         bufferWriter.putUTF("You have been banned from this server.");
	                         connection.endPacketImmediate();
	                         connection.forceDisconnect();
	                         c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned SteamID " + id);
	                         break;
	                      }
	                   }
	                   c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " banned SteamID " + id);
	                   c.setLoggedImportant(true);
	                   response = "SteamID " + id + " is now banned";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                }
	             }
			} else
			if (command.startsWith("unbanid")) {
				if(args.length != 1) {
	                response = (String)ServerOptions.adminOptionsList.get("unbanid");
	                c.setResponse(Result.FAILURE, response);
	                return;
				} else if(!SteamUtils.isSteamModeEnabled()) {
	                response = "Server is not in Steam mode";
	                c.setResponse(Result.FAILURE, response);
	                return;
	             } else {
	                String id = args[0].trim();
	                if(id.isEmpty()) {
	                   response = (String)ServerOptions.adminOptionsList.get("unbanid");
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                } else if(!SteamUtils.isValidSteamID(id)) {
	                   response = "Expected SteamID but got \"" + id + "\"";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                } else {
	                   ServerWorldDatabase.instance.banSteamID(id, "", false);
	                   response = "SteamID " + id + " is now unbanned";
	                   c.setResponse(Result.SUCCESS, response);
	                   c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " unbanned SteamID " + id);
	                   c.setLoggedImportant(true);
	                   return;
	                }
	             }
			} else
			if (command.startsWith("kickuser")) {
				if(args.length >= 1 && args.length <= 3) {
	                username = args[0];
	                if(username != null && !username.isEmpty()) {
	                   String reason = "";

	                   for(int player1 = 1; player1 < args.length; player1++) {
	                      if(args[player1].equals("-r")) {
	                         if(args.length <= player1 + 1) {
	                            response = (String)ServerOptions.adminOptionsList.get("kickuser");
	                         }
	                         reason = " Reason : " + args[player1 + 1];
	                      }
	                   }
	                   boolean userExists = false;

	                   for(int var39 = 0; var39 < udpEngine.connections.size(); var39++) {
	                      connection = (UdpConnection)udpEngine.connections.get(var39);

	                      for(int var49 = 0; var49 < 4; ++var49) {
	                         if(username.equals(connection.usernames[var49])) {
	                        	 userExists = true;
	                            bufferWriter = connection.startPacket();
	                            PacketTypes.doPacket((byte)83, bufferWriter);
	                            bufferWriter.putUTF("You have been kicked from this server." + reason);
	                            connection.endPacketImmediate();
	                            connection.forceDisconnect();
	                            GameServer.addDisconnect(connection);
	                            break;
	                         }
	                      }
	                   }

	                   if(userExists) {
	                      response = "User " + username + " kicked.";
	                      c.setResponse(Result.SUCCESS, response);
	                      c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " kicked user " + username + (reason != null?reason:""));
	                      c.setLoggedImportant(true);
	                      return;
	                   } else {
	                	   response = "User " + username + " doesn\'t exist.";
	                	   c.setResponse(Result.FAILURE, response);
	                	   return;
	                   }
	                } else {
	                	response = (String)ServerOptions.adminOptionsList.get("kickuser");
	                	c.setResponse(Result.FAILURE, response);
	                	return;
	                }
	             } else {
	                
	            	 response = (String)ServerOptions.adminOptionsList.get("kickuser");
	            	 c.setResponse(Result.FAILURE, response);
	            	 return;
	             }
			} else
			if (command.startsWith("disconnect")) {
				if(args.length != 1) {
	                response = (String)ServerOptions.adminOptionsList.get("disconnect");
	                c.setResponse(Result.FAILURE, response);
	                return;
	             } else {
	                try {
	                   int index = Integer.parseInt(args[0]);
	                   if(index >= 1 && index <= udpEngine.connections.size()) {
	                      connection = (UdpConnection)udpEngine.connections.get(index - 1);
	                      username = connection.username;
	                      bufferWriter = connection.startPacket();
	                      PacketTypes.doPacket((byte)83, bufferWriter);
	                      bufferWriter.putUTF("You have been kicked from this server.");
	                      connection.endPacketImmediate();
	                      connection.forceDisconnect();
	                      GameServer.addDisconnect(connection);
	                      response = "Disconnected connection=" + index + " username=\"" + username + "\"";
	                      c.setResponse(Result.SUCCESS, response);
	                      c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " disconnected index=" + index + " " + connection.idStr + " \"" + username + "\"");
	                 	 return;
	                   } else {
	                      response = "No such connection " + index + ", must be from 1 to " + udpEngine.connections.size();
	                      c.setResponse(Result.FAILURE, response);
	                 	 return;
	                   }
	                } catch (NumberFormatException var19) {
	                   response = "Expected connection number but got \"" + args[0] + "\"";
	                   c.setResponse(Result.FAILURE, response);
	              	 return;
	                }
	             }
			} else
			if (command.startsWith("grantadmin")) {
				if(args.length != 1) {
	                response = (String)ServerOptions.adminOptionsList.get("grantadmin");
	                c.setResponse(Result.FAILURE, response);
	                return;
	             } else {
	                username = args[0];
	                if(ServerWorldDatabase.instance.containsUser(username)) {
	                   IsoPlayer isoPlayer = GameServer.getPlayerByUserName(username);
	                   if(isoPlayer != null) {
	                	   isoPlayer.admin = true;
	                      connection = GameServer.getConnectionFromPlayer(isoPlayer);
	                      if(connection != null) connection.admin = true;
	                      
	                      GameServer.sendPlayerExtraInfo(isoPlayer, (UdpConnection)null);
	                   }
	                }

	                try {
	                   response = ServerWorldDatabase.instance.grantAdmin(username, true);
	                   c.setResponse(Result.SUCCESS, response);
	                   c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " granted admin on " + username);
	                   return;
	                } catch (SQLException e) {
	                   e.printStackTrace();
	                   response = "A SQL error occured";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                }
	             }
			} else
			if (command.startsWith("removeadmin")) {
	            if(args.length != 1) {
	                response = (String)ServerOptions.adminOptionsList.get("removeadmin");
	                c.setResponse(Result.FAILURE, response);
	                return;
	            } else {
	                username = args[0];
	                if(ServerWorldDatabase.instance.containsUser(username)) {
	                   IsoPlayer isoPlayer = GameServer.getPlayerByUserName(username);
	                   if(isoPlayer != null) {
	                	   isoPlayer.admin = false;
	                      connection = GameServer.getConnectionFromPlayer(isoPlayer);
	                      if(connection != null) connection.admin = false;
	                  	  GameServer.sendPlayerExtraInfo(isoPlayer, (UdpConnection)null);
	                   }
	                }

	                try {
	                	response = ServerWorldDatabase.instance.grantAdmin(username, false);
	                   c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " removed admin rights on " + username);
	                   c.setResponse(Result.SUCCESS, response);
	                   return;
	                } catch (SQLException e) {
	                   e.printStackTrace();
	                   response = "A SQL error occured";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                }
	             }
			} else
			if (command.startsWith("debugplayer")) {
				if(args.length != 1) {
	                response = "/debugplayer \"username\"";
	                c.setResponse(Result.FAILURE, response);
	                return;
				} else {
	                username = args[0];
	                IsoPlayer isoPlayer = GameServer.getPlayerByUserName(username);
	                if(isoPlayer == null) {
	                   response = "no such user";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                } else {
	                   connection = udpEngine.getActiveConnection(((Long)GameServer.IDToAddressMap.get(Integer.valueOf(isoPlayer.OnlineID))).longValue());
	                   if(connection == null) {
	                      response = "no connection for user";
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   } else if(GameServer.DebugPlayer.contains(connection)) {
	                      GameServer.DebugPlayer.remove(connection);
	                      response = "debug off";
	                      c.setResponse(Result.SUCCESS, response);
	                      return;
	                   } else {
	                	   GameServer.DebugPlayer.add(connection);
	                      response = "debug on";
	                      c.setResponse(Result.SUCCESS, response);
	                      return;
	                   }
	                }
	             }
			} else
			if (command.startsWith("addalltowhitelist")) {
	            for(int var27 = 0; var27 < udpEngine.connections.size(); ++var27) {
	               connection = (UdpConnection)udpEngine.connections.get(var27);
	               if(connection.password != null && !connection.password.equals("")) {
	                  ServerWorldDatabase.instance.addUser(connection.username, connection.password);
	               }
	            }
	            response = "Added all user(s) to the whitelist.";
	            c.setResponse(Result.SUCCESS, response);
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " added all users with passwords to the whitelist.");
	            return;
			} else
			if (command.startsWith("addusertowhitelist")) {
				if(args.length != 1) {
	                response = (String)ServerOptions.adminOptionsList.get("addusertowhitelist");
	                c.setResponse(Result.FAILURE, response);
	                return;
	             } else {
	                username = args[0];
	                if(!ServerWorldDatabase.isValidUserName(username)) {
	                   response = "Invalid username \"" + username + "\"";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                } else {
	                   for(int var27 = 0; var27 < udpEngine.connections.size(); ++var27) {
	                      connection = (UdpConnection)udpEngine.connections.get(var27);
	                      if(connection.username.equalsIgnoreCase(username)) {
	                         if(connection.password != null && !connection.password.equals("")) {
	                            response = ServerWorldDatabase.instance.addUser(connection.username, connection.password);
	                            c.setResponse(Result.SUCCESS, response);
	                            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " created user " + connection.username + " with password " + connection.password);
	                            return;
	                         }
	                         response = "User " + username + " doesn\'t have a password.";
	                         c.setResponse(Result.FAILURE, response);
	                         return;
	                      }
	                   }
	                   response = "User " + username + " not found.";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                }
	             }
			} else
			if (command.startsWith("additem")) {
				if(args.length != 0 && args.length < 4) {
	                String itemName = "";
					boolean hasItemCount = false;
	                int itemCount = 1;
	                if(args.length == 3) {
	                   try {
	                	  username = args[0];
	                	  itemName = args[1];
	                      itemCount = Integer.parseInt(args[2]);
	                      hasItemCount = true;
	                   } catch (NumberFormatException var20) {}
	                } else if(args.length == 2) {
	                	username = args[0];
	                	itemName = args[1];
	                } else if(args.length == 1) {
	                	username = commander;
	                	itemName = args[0];
	                }
	                
	                
	                if(itemName == null) itemName = args[args.length - 1];

	                boolean b = args.length == 3 || args.length == 2 && !hasItemCount;
	                username = b?args[0]:commander;
	                
	                Item item = ScriptManager.instance.FindItem(itemName);
	                
	                if(item == null) {
	                   response = "Item " + itemName + " doesn\'t exist.";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                } else {
	                   IsoPlayer isoPlayer = GameServer.getPlayerByUserName(username);
	                   if(isoPlayer != null) {
	                      connection = udpEngine.getActiveConnection(((Long)GameServer.IDToAddressMap.get(Integer.valueOf(isoPlayer.OnlineID))).longValue());
	                      if(connection != null) {
	                         bufferWriter = connection.startPacket();
	                         PacketTypes.doPacket((byte)85, bufferWriter);
	                         bufferWriter.putShort((short)isoPlayer.OnlineID);
	                         bufferWriter.putUTF(itemName);
	                         bufferWriter.putInt(itemCount);
	                         connection.endPacketImmediate();
	                         response = "Item " + itemName + " Added in " + username + "\'s inventory.";
	                         c.setResponse(Result.SUCCESS, response);
	                         c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " added item " + itemName + " in " + username + "\'s inventory");
	                         return;
	                      }
	                   }
	                   response = "User " + username + " not found.";
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                }
	             } else {
	                response = (String)ServerOptions.adminOptionsList.get("additem");
	                c.setResponse(Result.FAILURE, response);
	                return;
	             }
			} else
			if (command.startsWith("addxp")) {
				String e, user2, var45;
				int player1 = 0;
				if(args.length != 2) {
	                response = (String)ServerOptions.adminOptionsList.get("addxp");
	                c.setResponse(Result.FAILURE, response);
	                return;
	             } else {
	                e = args[0];
	                user2 = null;
	                String[] var35 = args[1].split("=", 2);
	                if(var35.length != 2) {
	                   response = (String)ServerOptions.adminOptionsList.get("addxp");
	                   c.setResponse(Result.FAILURE, response);
	                   return;
	                } else {
	                   user2 = var35[0].trim();
	                   if(PerkFactory.Perks.FromString(user2) == PerkFactory.Perks.MAX) {
	                      var45 = connection == null?"\n":" LINE ";
	                      StringBuilder var47 = new StringBuilder();

	                      for(int var50 = 0; var50 < PerkFactory.PerkList.size(); ++var50) {
	                         if(((PerkFactory.Perk)PerkFactory.PerkList.get(var50)).type != PerkFactory.Perks.Passiv) {
	                            var47.append(((PerkFactory.Perk)PerkFactory.PerkList.get(var50)).type);
	                            if(var50 < PerkFactory.PerkList.size()) {
	                               var47.append(var45);
	                            }
	                         }
	                      }

	                      response = "List of available perks :" + var45 + var47.toString();
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   } else {
	                      try {
	                         player1 = Integer.parseInt(var35[1]);
	                      } catch (NumberFormatException var21) {
	                         response = (String)ServerOptions.adminOptionsList.get("addxp");
	                         c.setResponse(Result.FAILURE, response);
	                         return;
	                      }

	                      IsoPlayer var42 = GameServer.getPlayerByUserName(e);
	                      if(var42 != null) {
	                         connection = udpEngine.getActiveConnection(((Long)GameServer.IDToAddressMap.get(Integer.valueOf(var42.OnlineID))).longValue());
	                         if(connection != null) {
	                            bufferWriter = connection.startPacket();
	                            PacketTypes.doPacket((byte)107, bufferWriter);
	                            bufferWriter.putShort((short)var42.OnlineID);
	                            bufferWriter.putInt(PerkFactory.Perks.FromString(user2).index());
	                            bufferWriter.putInt(player1);
	                            connection.endPacketImmediate();
	                            LoggerManager.getLogger("admin").write(commander + " added " + player1 + " " + user2 + " xp\'s to " + e);
	                            response = "Added " + player1 + " " + user2 + " xp\'s to " + e;
	                            c.setResponse(Result.SUCCESS, response);
	                            return;
	                         }
	                      }

	                      response = "User " + e + " not found.";
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   }
	                }
	             }
			} else
			if (command.startsWith("changeoption")) {
				if(args.length != 2) {
	              response = (String)ServerOptions.adminOptionsList.get("changeoption");
	              c.setResponse(Result.FAILURE, response);
	              return;
	           } else {
	              String result = ServerOptions.changeOption(args[0], args[1]);
	              if(args[0].equals("Password")) {
	                 udpEngine.SetServerPassword(args[1]);
	              }

	              if(SteamUtils.isSteamModeEnabled()) {
	                 SteamGameServer.SetServerName(ServerOptions.getOption("PublicName"));
	                 SteamGameServer.SetKeyValue("description", ServerOptions.getOption("PublicDescription"));
	                 SteamGameServer.SetKeyValue("open", ServerOptions.getBoolean("Open").booleanValue()?"1":"0");
	              }

	              response = result;
	              c.setResponse(Result.SUCCESS, response);
	              c.setLoggedMessage(LogEvent.LogType.STAFF, "Changed option: " + args[0] + "=" + args[1]);
	              return;
	           }
			} else
			if (command.startsWith("createhorde")) {
				connection = c.getPlayer().getConnection();
				IsoPlayer var24 = null;
				int zombieCount = 0;
				if(args.length >= 1 && args.length <= 2) {
	              var24 = null;
	              if(args.length == 2) {
	                 username = args[1];
	                 var24 = GameServer.getPlayerByUserName(username);
	                 if(var24 == null) {
	                    response = "User \"" + username + "\" not found";
	                    c.setResponse(Result.FAILURE, response);
	                    return;
	                 }
	              } else if(connection != null) {
	                 var24 = GameServer.getAnyPlayerFromConnection(connection);
	              }

	              try {
	            	  zombieCount = Integer.parseInt(args[0]);
	              } catch (NumberFormatException var22) {
	                 response = (String)ServerOptions.adminOptionsList.get("createhorde");
	                 c.setResponse(Result.FAILURE, response);
	                 return;
	              }

	              if(var24 == null) {
	                 response = "Specify a player to create the horde near to.";
	                 c.setResponse(Result.FAILURE, response);
	                 return;
	              } else {
	                 for(int player1 = 0; player1 < zombieCount; ++player1) {
	                    VirtualZombieManager.instance.choices.clear();
	                    IsoGridSquare var30 = IsoWorld.instance.CurrentCell.getGridSquare((double)Rand.Next(var24.getX() - 10.0F, var24.getX() + 10.0F), (double)Rand.Next(var24.getY() - 10.0F, var24.getY() + 10.0F), (double)var24.getZ());
	                    VirtualZombieManager.instance.choices.add(var30);
	                    VirtualZombieManager.instance.createRealZombieAlways(IsoDirections.fromIndex(Rand.Next(IsoDirections.Max.index())).index(), false);
	                 }

	                 response = "Horde spawned.";
	                 c.setResponse(Result.SUCCESS, response);
	                 c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " created a horde of " + zombieCount + " zombies near " + var24.getX() + "," + var24.getY());
	                 c.setLoggedImportant(true);
	                 return;
	              }
	           } else {
	              response = (String)ServerOptions.adminOptionsList.get("createhorde");
	              c.setResponse(Result.FAILURE, response);
	              return;
	           }
			} else
			if (command.startsWith("godmod")) {
				username = commander;
	          if(args.length == 2 || args.length == 1 && !args[0].equals("-true") && !args[0].equals("-false")) {
	             username = args[0];
	          }

	          boolean var25 = false;
	    	  boolean var51 = Boolean.valueOf(true);
	    	  try{
		    	  if(args[args.length - 1].equals("-false")) {
		    		  var51 = Boolean.valueOf(false);
		    		  var25 = true;
		    	  } else if(args[args.length - 1].equals("-true")) {
		    		  var25 = true;
		    	  }
	    	  } catch(ArrayIndexOutOfBoundsException e) {
	    		  var25 = false;
	    	  }

	          IsoPlayer var33 = GameServer.getPlayerByUserName(username);
	          if(var33 != null) {
	             if(var25) {
	                var33.godMod = var51;
	             } else {
	                var33.godMod = !var33.godMod;
	                var51 = Boolean.valueOf(var33.godMod);
	             }

	             GameServer.sendPlayerExtraInfo(var33, connection);
	             if(var51) {
	                response = "User " + username + " is now invincible.";
	                c.setResponse(Result.SUCCESS, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " enabled godmod on " + username);
	             } else {
	                response = "User " + username + " is no more invincible.";
	                c.setResponse(Result.SUCCESS, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " disabled godmod on " + username);
	             }
	             return;
	          } else {
	             response = "User " + username + " not found.";
	             c.setResponse(Result.FAILURE, response);
	             return;
	          }
			} else
			if (command.startsWith("invisible")) {
				username = commander;
	          if(args.length == 2 || args.length == 1 && !args[0].equals("-true") && !args[0].equals("-false")) {
	             username = args[0];
	          }

	          IsoPlayer var33 = GameServer.getPlayerByUserName(username);

	    	  boolean var25 = false;
	    	  boolean var51 = Boolean.valueOf(true);
	    	  try{
		    	  if(args[args.length - 1].equals("-false")) {
		    		  var51 = Boolean.valueOf(false);
		    		  var25 = true;
		    	  } else if(args[args.length - 1].equals("-true")) {
		    		  var25 = true;
		    	  }
	    	  } catch(ArrayIndexOutOfBoundsException e) {
	    		  var25 = false;
	    	  }
	    	  
	          if(var33 != null) {
	             if(var25) {
	                var33.invisible = var51;
	             } else {
	                var33.invisible = !var33.invisible;
	                var51 = Boolean.valueOf(var33.invisible);
	             }

	             var33.GhostMode = var51;
	             GameServer.sendPlayerExtraInfo(var33, connection);
	             if(var51) {
	            	 response = "User " + username + " is now invisible.";
	                c.setResponse(Result.SUCCESS, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " enabled invisibility on " + username);
	             } else {
	            	 response = "User " + username + " is no longer invisible.";
	                c.setResponse(Result.SUCCESS, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " disabled invisibility on " + username);
	             }
	             return;
	          } else {
	             response = "User " + username + " not found.";
	             c.setResponse(Result.FAILURE, response);
	             return;
	          }
			} else
			if (command.startsWith("noclip")) {
				username = commander;
	          if(args.length == 2 || args.length == 1 && !args[0].equals("-true") && !args[0].equals("-false")) {
	             username = args[0];
	          }

	          boolean var25 = false;
	    	  boolean var51 = Boolean.valueOf(true);
	    	  try{
		    	  if(args[args.length - 1].equals("-false")) {
		    		  var51 = Boolean.valueOf(false);
		    		  var25 = true;
		    	  } else if(args[args.length - 1].equals("-true")) {
		    		  var25 = true;
		    	  }
	    	  } catch(ArrayIndexOutOfBoundsException e) {
	    		  var25 = false;
	    	  }

	          IsoPlayer var33 = GameServer.getPlayerByUserName(username);
	          if(var33 != null) {
	             if(var25) {
	                var33.setNoClip(var51);
	             } else {
	                var33.setNoClip(!var33.isNoClip());
	                var51 = Boolean.valueOf(var33.isNoClip());
	             }
	             GameServer.sendPlayerExtraInfo(var33, connection);
	             if(var51) {
	                response  = "User " + username + " won\'t collide.";
	                c.setResponse(Result.FAILURE, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " enabled noclip on " + username);
	             } else {
	                response = "User " + username + " will collide.";
	                c.setResponse(Result.FAILURE, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " disabled noclip on " + username);
	             }
	             return;
	          } else {
	             response = "User " + username + " not found.";
	             c.setResponse(Result.FAILURE, response);
	             return;
	          }
			} else
			if (command.startsWith("players")) {
				List<String> var41 = new ArrayList<>();

				for (int var27 = 0; var27 < udpEngine.connections.size(); ++var27) {
					connection = (UdpConnection) udpEngine.connections.get(var27);

					for (int var39 = 0; var39 < 4; ++var39) {
						if (connection.usernames[var39] != null) {
							var41.add(connection.usernames[var39]);
						}
					}
				}

				response = "Players connected (" + var41.size() + "): ";
				String line = " <LINE> ";
				if (connection == null) {
					line = "\n";
				}

				response = response + line;

				for (int var39 = 0; var39 < var41.size(); ++var39) {
					response = response + "-" + (String) var41.get(var39) + line;
				}

				c.setResponse(Result.SUCCESS, response);
				return;
				
			} else
			if (command.startsWith("reloadlua")) {
	            if(args.length != 1) {
	                response = (String)ServerOptions.adminOptionsList.get("reloadlua");
	                c.setResponse(Result.FAILURE, response);
	                return;
	            } else {
	                String luaFile = args[0];
	                Iterator var32 = LuaManager.loaded.iterator();

	                String var36 = null;
	                
	                do {
	                   if(!var32.hasNext()) {
	                      response = "Unknown Lua file";
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   }

	                   var36 = (String)var32.next();
	                } while(!var36.endsWith(luaFile));

	                LuaManager.loaded.remove(var36);
	                LuaManager.RunLua(var36, true);
	                response = "Lua file reloaded";
	                c.setResponse(Result.SUCCESS, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, "Reloaded Lua file: \"" + var36 + "\".");
	                return;
	             }
			} else
			if (command.startsWith("removeuserfromwhitelist")) {
				if(args.length != 1) {
	              response =  (String)ServerOptions.adminOptionsList.get("removeuserfromwhitelist");
	              c.setResponse(Result.FAILURE, response);
	              return;
				} else {
	              username = args[0];
	              if(username != null && !username.equals("")) {
	                 response = ServerWorldDatabase.instance.removeUser(username);
	                 c.setResponse(Result.SUCCESS, response);
	                 c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " removed user " + username + " from whitelist");
	                 return;
	              } else {
	                 response = (String)ServerOptions.adminOptionsList.get("removeuserfromwhitelist");
	                 c.setResponse(Result.FAILURE, response);
	                 return;
	              }              
	           }
			} else
			if (command.startsWith("showoptions")) {
				connection = c.getPlayer().getConnection();
				Iterator var55 = ServerOptions.getPublicOptions().iterator();
	            String s = null;
	            String newLine = " <LINE> ";
	            if(connection == null) {
	            	newLine = "\n";
	            }
	            String line = "List of Server Options:" + newLine;
	            while(var55.hasNext()) {
	               s = (String)var55.next();
	               if(!s.equals("ServerWelcomeMessage")) {
	            	   line = line + "* " + s + "=" + (String)ServerOptions.getMap().get(s) + newLine;
	               }
	            }

	            response = line + "* ServerWelcomeMessage=" + (String)ServerOptions.getMap().get("ServerWelcomeMessage");
	            c.setResponse(Result.SUCCESS, response);
	            return;
			} else
			if (command.startsWith("teleport")) {
				if(args.length >= 1 && args.length <= 2) {
	                if(args.length == 1) {
	                	connection = c.getPlayer().getConnection();
	                   if(connection == null) {
	                      response = "Need player to teleport to, ex /teleport user1 user2";
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   } else {
	                      username = args[0];
	                      IsoPlayer player1 = GameServer.getPlayerByUserName(username);
	                      if(player1 != null) {
	                         ByteBufferWriter var34 = connection.startPacket();
	                         PacketTypes.doPacket((byte)108, var34);
	                         var34.putFloat(player1.getX());
	                         var34.putFloat(player1.getY());
	                         var34.putFloat(player1.getZ());
	                         connection.endPacketImmediate();
	                         response = "teleported to " + username + " please wait two seconds to show the map around you.";
	                         c.setResponse(Result.SUCCESS, response);
	                         c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " teleport to " + username);
	                         return;
	                      } else {
	                         response = "Can\'t find player " + username;
	                         c.setResponse(Result.FAILURE, response);
	                         return;
	                      }
	                   }
	                } else {
	                   String username1 = args[0];
	                   String username2 = args[1];
	                   IsoPlayer player1 = GameServer.getPlayerByUserName(username1);
	                   IsoPlayer player2 = GameServer.getPlayerByUserName(username2);
	                   if(player1 == null) {
	                      response = "Can\'t find player " + username1;
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   } else if(player2 == null) {
	                      response = "Can\'t find player " + username2;
	                      c.setResponse(Result.FAILURE, response);
	                      return;
	                   } else {
	                      connection = GameServer.getConnectionFromPlayer(player1);
	                      if(connection == null) {
	                         response = "No connection for player " + username1;
	                         c.setResponse(Result.FAILURE, response);
	                         return;
	                      } else {
	                         bufferWriter = connection.startPacket();
	                         PacketTypes.doPacket((byte)108, bufferWriter);
	                         bufferWriter.putFloat(player2.getX());
	                         bufferWriter.putFloat(player2.getY());
	                         bufferWriter.putFloat(player2.getZ());
	                         connection.endPacketImmediate();
	                         response =  "teleported " + username1 + " to " + username2;
	                         c.setResponse(Result.SUCCESS, response);
	                         c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " teleported " + username1 + " to " + username2);
	                         return;
	                      }
	                   }
	                }
	             } else {
	                response = (String)ServerOptions.adminOptionsList.get("teleport");
	                c.setResponse(Result.FAILURE, response);
	                return;
	             }
			} else
			if (command.equalsIgnoreCase("sendpulse")) {
			} else
			if (command.equalsIgnoreCase("save")) {
				ServerMap.instance.QueueSaveAll();
				c.setResponse(Result.SUCCESS, "World Saved.");
				return;
			} else
			if (command.equalsIgnoreCase("alarm")) {
				if(isoCommander != null && isoCommander.getSquare() != null && isoCommander.getSquare().getBuilding() != null) {
					isoCommander.getSquare().getRoom().def.bExplored = false;
	                AmbientStreamManager.instance.doAlarm(isoCommander.getSquare().getRoom().def);
	                response = "Alarm sounded";
	                c.setResponse(Result.SUCCESS, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, "Alarm sounded by.");
	                return;
	             } else {
	             	c.setResponse(Result.FAILURE, "Not in a room.");
	             	c.setLoggedMessage(LogEvent.LogType.STAFF, "Attempted to sound alarm. (Not in room) (" + isoCommander.getX() + ", " + isoCommander.getY() + ", " + isoCommander.getZ() + ")");
	                return;
	             }
			} else 
			if (command.equalsIgnoreCase("chopper")) {
	            IsoWorld.instance.helicopter.pickRandomTarget();
	            response = "Choppers launched.";
	            c.setResponse(Result.SUCCESS, response);
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " sounded chopper.");
	            return;
			} else 
			if (command.equalsIgnoreCase("gunshot")) {
	           AmbientStreamManager.instance.doGunEvent();
	           LoggerManager.getLogger("admin").write(commander + " sounded gunshot.");
	           response = "Gunshot fired.";
	           c.setResponse(Result.SUCCESS, response);
	           c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " sounded gunshot.");
	           return;
	        } else
	        if (command.equalsIgnoreCase("startrain")) {
	            RainManager.startRaining();
	            c.setResponse(Result.SUCCESS, "Started rain.");
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " started rain.");
	            return;
	        } else
	        if (command.equalsIgnoreCase("stoprain")) {
	            RainManager.stopRaining();
	            c.setResponse(Result.SUCCESS, "Stopped rain.");
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " stopped rain.");
	            return;
	        } else
	        if(command.equalsIgnoreCase("thunder")) {
	            if(args.length != 1) {
	            	c.setResponse(Result.FAILURE, onTooltip(c.getPlayer(), command));
	            	c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " attempted to toggle thunder. (Invalid parameters)");
	               return;
	            } else if(args[0].equals("start")) {
	               GameTime.getInstance().thunderStart(true);
	               response = "Started thunder.";
	               c.setResponse(Result.SUCCESS, response);
	               c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " started thunder.");
	               return;
	            } else if(args[0].equals("stop")) {
	               GameTime.getInstance().thunderStop();
	               response = "Stopped thunder.";
	               c.setResponse(Result.SUCCESS, response);
	               c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " stopped thunder.");
	               return;
	            } else {
	            	response = "Unknown argument: " + args[0] + ".";
	                c.setResponse(Result.FAILURE, response);
	                c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " Tried to use thunder. argument: " + args[0]);
	                return;
	            }
	        } else
	        if (command.equalsIgnoreCase("reload")) {
	        	ServerOptions.init();
	            ZombieConfig.instance.load();
	            ZombiePopulationManager.instance.onConfigReloaded();
	            GameServer.sendOptionsToClients();
	            ZIRC.instance.getUdpEngine().SetServerPassword(ServerOptions.getOption("Password"));
	            if (SteamUtils.isSteamModeEnabled()) {
	                SteamGameServer.SetServerName(ServerOptions.getOption("PublicName"));
	                SteamGameServer.SetKeyValue("description", ServerOptions.getOption("PublicDescription"));
	                SteamGameServer.SetKeyValue("open", ServerOptions.getBoolean("Open").booleanValue()?"1":"0");
	            }
	            ZIRC.instance.reload();
	            response = "Server reloaded.";
	            c.setResponse(Result.SUCCESS, response);
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " reloaded the server.");
	            return;
	        } else
			if (command.equalsIgnoreCase("reloadoptions")) {
	        	ServerOptions.init();
	            ZombieConfig.instance.load();
	            ZombiePopulationManager.instance.onConfigReloaded();
	            GameServer.sendOptionsToClients();
	            ZIRC.instance.getUdpEngine().SetServerPassword(ServerOptions.getOption("Password"));
	            if (SteamUtils.isSteamModeEnabled()) {
	                SteamGameServer.SetServerName(ServerOptions.getOption("PublicName"));
	                SteamGameServer.SetKeyValue("description", ServerOptions.getOption("PublicDescription"));
	                SteamGameServer.SetKeyValue("open", ServerOptions.getBoolean("Open").booleanValue()?"1":"0");
	            }
	            response = "Server options reloaded.";
	            c.setResponse(Result.SUCCESS, response);
	            c.setLoggedMessage(LogEvent.LogType.STAFF, commander + " reloaded the server.");
	            return;
	        }
		}

		private void onClientCommand(CommandEvent c) {
			String command = c.getCommand();
			String response = "";
			String[] args = c.getArguments();
			UdpConnection connection = c.getPlayer().getConnection();
			if (command.equalsIgnoreCase("roll")) {
	            try {
	               GameServer.PlayWorldSoundServer("rollDice", false, GameServer.getAnyPlayerFromConnection(connection).getCurrentSquare(), 0.0F, 3.0F, 1.0F, false);
	               response = connection.username + " rolls a " + args[0] + "-sided dice and obtains " + Rand.Next(Integer.parseInt(args[0]));
	               c.setResponse(Result.SUCCESS, response);
	               return;
	            } catch (Exception var7) {
	               response = (String)ServerOptions.clientOptionsList.get("roll");
	               c.setResponse(Result.FAILURE, response);
	               return;
	            }
			} else
			if (command.equalsIgnoreCase("card")) {
				GameServer.PlayWorldSoundServer("drawCard", false, GameServer.getAnyPlayerFromConnection(connection).getCurrentSquare(), 0.0F, 3.0F, 1.0F, false);
				response = connection.username + " drew " + ServerOptions.getRandomCard();
				c.setResponse(Result.FAILURE, response);
				return;
			} else
			if (command.equalsIgnoreCase("changepwd")) {
	            if(args.length == 2) {
	               String previousPass = args[0];
	               String newPass = args[1];
	               try {
	                  response = ServerWorldDatabase.instance.changePwd(connection.username, previousPass.trim(), newPass.trim());
	                  c.setResponse(Result.SUCCESS, response);
	                  return;
	               } catch (SQLException var8) {
	                  var8.printStackTrace();
	                  response = "A SQL error occured";
	                  c.setResponse(Result.FAILURE, response);
	                  return;
	               }
	            } else {
	               response = (String)ServerOptions.clientOptionsList.get("changepwd");
	               c.setResponse(Result.FAILURE, response);
	               return;
	            }
			}
		}

		@Override
		public String onTooltip(Player player, String command) {
			if(command.equalsIgnoreCase("changepwd")) {
				return "Use this command to change your password, use : /changepwd \"previouspwd\" \"newpwd\"";
			} else
			if(command.equalsIgnoreCase("roll")) {
				return "If you have a dice, you can roll a random number (up to 100), use /roll 6";
			} else
			if(command.equalsIgnoreCase("card")) {
				return "If you have a card deck, you can draw a random card, use /card";
			} else
			// ADMIN COMMANDS
			if(player.isAdmin()) {
				if(command.equalsIgnoreCase("addalltowhitelist")) {
					return "Add all the current users connected with a password in the whitelist, so their account is protected.";
				} else
				if(command.equalsIgnoreCase("additem")) {
					return "Add an item to a player, if no username is given the item will be added to you, count is optional, use /additem \"username\" \"module.item\" count, ex : /additem \"rj\" \"Base.Axe\" count";
				} else
				if(command.equalsIgnoreCase("adduser")) {
					return "Use this command to add a new user in a whitelisted server, use : /adduser \"username\" \"pwd\"";
				} else
				if(command.equalsIgnoreCase("addusertowhitelist")) {
					return "Add the user connected with a password in the whitelist, so his account is protected, use : /addusertowhitelist \"username\"";
				} else
				if(command.equalsIgnoreCase("addxp")) {
					return "Add experience points to a player, use : /addxp \"playername\" perkname=xp, ex /addxp \"rj\" Woodwork=2";
				} else
				if(command.equalsIgnoreCase("alarm")) {
					return "Sound a building alarm at the admin's position.  Must be in a room.";
				} else
				if(command.equalsIgnoreCase("changeoption")) {
					return "Use this to change a server option, use : /changeoption optionName \"newValue\"";
				} else
				if(command.equalsIgnoreCase("chopper")) {
					return "Start the choppers (do noise on a random player)";
				} else
				if(command.equalsIgnoreCase("connections")) {
					return "Displays info about all connections";
				} else
				if(command.equalsIgnoreCase("createhorde")) {
					return "Use this to spawn a horde near a player, use : /createhorde count \"username\", ex /createhorde 150 \"rj\", username is optional except from the server console.";
				} else
				if(command.equalsIgnoreCase("disconnect")) {
					return "Kicks a client by connection number. Use /connections to see connection numbers. use : /disconnect index, ex /disconnect 2";
				} else
				if(command.equalsIgnoreCase("godmod")) {
					return "Set a player invincible, if no username set it make you invincible, if no value it toggle it, use : /godmod \"username\" -value, ex /godmod \"rj\" -true (could be -false)";
				} else
				if(command.equalsIgnoreCase("grantadmin")) {
					return "Grant the admin rights to a user, use : /grantadmin \"username\"";
				} else
				if(command.equalsIgnoreCase("gunshot")) {
					return "Start a gunshot (do noise on a random player)";
				} else
				if(command.equalsIgnoreCase("invisible")) {
					return "Set a player invisible zombie will ignore him, if no username set it make you invisible, if no value it toggle it, use : /invisible \"username\" -value, ex /invisible \"rj\" -true (could be -false)";
				} else
				if(command.equalsIgnoreCase("kickuser")) {
					return "Kick a user, add a -r \"reason\" to specify a reason for the kick, use : /kickuser \"username\" -r \"reason\"";
				} else
				if(command.equalsIgnoreCase("nightlengthmodifier")) {
					return "Set modifier for night length (1.0 = normal, 0.5 = half, 0.0 = no night : /nightlengthmodifier delta> !";
				} else
				if(command.equalsIgnoreCase("noclip")) {
					return "A player with noclip won't collide on anything, if no value it toggle it, use : /noclip \"username\" -value, ex /noclip \"rj\" -true (could be -false)";
				} else
				if(command.equalsIgnoreCase("players")) {
					return "List the players connected.";
				} else
				if(command.equalsIgnoreCase("quit")) {
					return "Quit the server. (After saving it)";
				} else
				if(command.equalsIgnoreCase("reloadlua")) {
					return "Reload a Lua script, use : /reloadlua \"filename\"";
				} else
				if(command.equalsIgnoreCase("reloadoptions")) {
					return "Reload the options on the server (ServerOptions.ini) and send them to the clients";
				} else
				if(command.equalsIgnoreCase("removeadmin")) {
					return "Remove the admin rights to a user, use : /removeadmin \"username\"";
				} else
				if(command.equalsIgnoreCase("removeuserfromwhitelist")) {
					return "Remove the user from the whitelist, use: /removeuserfromwhitelist \"username\"";
				} else
				if(command.equalsIgnoreCase("save")) {
					return "Save the world.";
				} else
				if(command.equalsIgnoreCase("sendpulse")) {
					return "Toggle sending server performance info to this client, use : /sendpulse";
				} else
				if(command.equalsIgnoreCase("showoptions")) {
					return "Show the list of current Server options with their values.";
				} else
				if(command.equalsIgnoreCase("startrain")) {
					return "Start rain on the server.";
				} else
				if(command.equalsIgnoreCase("stoprain")) {
					return "Stop rain on the server.";
				} else
				if(command.equalsIgnoreCase("teleport")) {
					return "Teleport to a player, once teleported, wait 2 seconds to show map, use : /teleport \"playername\" or /teleport \"player1\" \"player2\", ex /teleport \"rj\" or /teleport \"rj\" \"toUser\"";
				} else
				if(command.equalsIgnoreCase("thunder")) {
					return "use: /thunder start or /thunder stop";
				}
			}
			return null;
		}
	}
	
	public class VanillaLogListener implements LogListener {

		@Override
		public void onLogEntry(LogEvent logEntry) {
			String message = logEntry.getLogMessage();
			boolean important = logEntry.isImportant();
			if(important) {
				LoggerManager.getLogger("admin").write(message, "IMPORTANT");			
			} else {			
				LoggerManager.getLogger("admin").write(message);
			}
		}
	}
}
