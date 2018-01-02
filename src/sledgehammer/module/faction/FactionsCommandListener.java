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
package sledgehammer.module.faction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.faction.Faction;
import sledgehammer.lua.faction.FactionMember;
import sledgehammer.manager.PermissionsManager;
import sledgehammer.util.Command;
import sledgehammer.util.LogType;
import sledgehammer.util.Printable;
import sledgehammer.util.Response;
import sledgehammer.util.Result;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * Class designed to handle commands from players for the Factions Module.
 * 
 * TODO: Document.
 * 
 * @author Jab
 */
public class FactionsCommandListener extends Printable implements CommandListener {

	/**
	 * Module instance using this Command Handler.
	 */
	private ModuleFactions module;

	private Map<String, String> mapContexts;
	private Map<String, String> mapTooltips = new HashMap<>();

	// private boolean doHeader = false;

	public FactionsCommandListener(ModuleFactions module) {
		this.module = module;
		// @formatter:off
		mapContexts = new HashMap<>();
		mapContexts.put("faction"                , "sledgehammer.factions"                );
		mapContexts.put("faction help"           , "sledgehammer.factions.help"           );
		mapContexts.put("faction create"         , "sledgehammer.factions.create"         );
		mapContexts.put("faction disband"        , "sledgehammer.factions.disband"        );
		mapContexts.put("faction join"           , "sledgehammer.factions.join"           );
		mapContexts.put("faction leave"          , "sledgehammer.factions.leave"          );
		mapContexts.put("faction invite"         , "sledgehammer.factions.invite"         );
		mapContexts.put("faction accept"         , "sledgehammer.factions.accept"         );
		mapContexts.put("faction reject"         , "sledgehammer.factions.reject"         );
		mapContexts.put("faction kick"           , "sledgehammer.factions.kick"           );
		mapContexts.put("faction change"         , "sledgehammer.factions.change"         );
		mapContexts.put("faction change tag"     , "sledgehammer.factions.change.tag"     );
		mapContexts.put("faction change name"    , "sledgehammer.factions.change.name"    );
		mapContexts.put("faction change color"   , "sledgehammer.factions.change.color"   );
		mapContexts.put("faction change password", "sledgehammer.factions.change.password");
		
		String params = "Make sure to put quote-tags around parameters! (\")";
		int tagCharactersMinimum = module.getTagMinimumCharacterCount();
		int tagCharactersMaximum = module.getTagMaximumCharacterCount();
		
		mapTooltips.put("faction"                , "Type \"/faction help\" for a list of faction commands.");
		mapTooltips.put("faction create"         , "Creates a faction. "                                                             + params + NEW_LINE + " /faction create \"name\" \"tag[" + tagCharactersMinimum + "-" + tagCharactersMaximum + " characer range]\" [password]");
		mapTooltips.put("faction disband"        , "Disbands a Faction. All players from this Faction will be removed as well."               + NEW_LINE + " /faction disband");
		mapTooltips.put("faction join"           , "Joins a faction, with a given password. "                                        + params + NEW_LINE + " /faction join \"name\" \"password\"");
		mapTooltips.put("faction leave"          , "Leave the faction."                                                                       + NEW_LINE + " /faction leave");
		mapTooltips.put("faction invite"         , "Invites a player to a faction (Must be owner!) "                                 + params + NEW_LINE + " /faction invite \"player\"");
		mapTooltips.put("faction accept"         , "Accepts an invite to a faction. Also clears other invites from other factions. " + params + NEW_LINE + " /faction accept \"faction\"");
		mapTooltips.put("faction reject"         , "Rejects invites to a faction."                                                   + params + NEW_LINE + " /faction reject \"faction\" (Use \"all\" to reject all invitations)");
		mapTooltips.put("faction change"         , "Changes a setting for a faction. (Must be owner!) "                              + params + NEW_LINE + " /faction change [tag|name|password]");
		mapTooltips.put("faction change name"    , "Changes the tag shown for a faction. (Must be owner!) "                          + params + NEW_LINE + " /faction change name \"name\"");
		mapTooltips.put("faction change tag"     , "Changes the tag shown for a faction. (Must be owner!) "                          + params + NEW_LINE + " /faction change tag \"tag[" + tagCharactersMinimum + "-" + tagCharactersMaximum + " characer range]\"");
		mapTooltips.put("faction change password", "Changes the password for a faction. (Must be owner!) "                           + params + NEW_LINE + " /faction change password \"old password\" \"new password\"");
		mapTooltips.put("faction change color"   , "Changes the color of the faction tag. (Must be owner!) "                         + params + NEW_LINE + " /faction change color \"color\"");
	
		// Add all commands to the default permissions list.
		PermissionsManager managerPermissions = module.getPermissionsManager();
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction"                ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction help"           ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction create"         ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction disband"        ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction join"           ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction leave"          ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction invite"         ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction accept"         ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction reject"         ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction kick"           ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction change"         ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction change tag"     ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction change name"    ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction change color"   ));
		managerPermissions.addDefaultPlayerPermission(mapContexts.get("faction change password"));
		// @formatter:on
	}

	public String[] getCommands() {
		return new String[] { "faction" };
	}

	public String getPermissionNode(String command) {
		if (command == null) {
			return null;
		}
		command = command.toLowerCase().trim();
		return mapContexts.get(command);
	}

	public String onTooltip(Player player, Command c) {
		String command = c.getCommand();
		if (player == null) {
			return null;
		}
		if (command == null || command.isEmpty()) {
			return null;
		}
		if (command.equalsIgnoreCase("faction") || command.equalsIgnoreCase("all")) {
			String builder = "";
			boolean addedLine = false;
			for (String com : mapTooltips.keySet()) {
				if (!com.equalsIgnoreCase("faction") || !com.equalsIgnoreCase("faction change")) {
					String context = getPermissionNode(com);
					if (player.hasPermission(context)) {
						String comTip = mapTooltips.get(com);
						addedLine = true;
						builder += comTip + NEW_LINE + " " + NEW_LINE + " ";
					}
				}
			}
			if (addedLine) {
				// Remove the new-line & white-space at the end. " <LINE> ";
				builder = builder.substring(0, builder.length() - 8);
			}
			return builder;
		} else {
			command = command.toLowerCase().trim();
			String context = getPermissionNode(command);
			if (player.hasPermission(context)) {
				return mapTooltips.get(command);
			}
		}
		return null;
	}

	public void onCommand(Command c, Response r) {

		if (!c.getCommand().equalsIgnoreCase("faction"))
			return;

		FactionActions actions = module.getActions();

		Player player = c.getPlayer();
		UUID playerId = player.getUniqueId();
		Result result = Result.FAILURE;
		String response = null;
		Response fresponse = null;

		String command = c.getCommand();
		String[] oldArgs = c.getArguments();
		String[] args = oldArgs;
		if (oldArgs.length == 0) {
			response = "Type \"/faction help\" for a list of faction commands.";
			r.set(result, response);
			return;
		}
		if (oldArgs.length >= 1) {
			command = oldArgs[0];
			args = new String[oldArgs.length - 1];
			for (int x = 1; x < oldArgs.length; x++)
				args[x - 1] = oldArgs[x];
		}

		if (command.equalsIgnoreCase("help")) {
			if (player.hasPermission(getPermissionNode("faction help"))) {
				response = onTooltip(player, new Command("all", null));
				r.set(result, response);
				return;
			} else {
				r.deny();
				return;
			}
		}

		if (command.equalsIgnoreCase("create")) {
			if (player.hasPermission(getPermissionNode("faction create"))) {
				if (args.length < 3) {
					response = onTooltip(player, new Command("faction create", null));
					r.set(result, response);
					return;
				}
				// @formatter:off
				String name     = args[0];
				String tag      = args[1];
				String password = args[2];
				// @formatter:on
				fresponse = actions.createFaction(name, tag, password, player);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("disband")) {
			if (player.hasPermission(getPermissionNode("faction disband"))) {
				fresponse = actions.disbandFaction(player);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("join")) {
			if (player.hasPermission(getPermissionNode("faction join"))) {
				if (args.length < 2) {
					response = onTooltip(player, new Command("faction join", null));
					r.set(result, response);
					return;
				}
				String password = args[1];
				Faction faction = module.getFactionByName(args[0]);
				if (faction == null) {
					r.set(Result.FAILURE, "Faction does not exist: \"" + args[0] + "\".");
					return;
				}
				fresponse = actions.joinFaction(faction, player, password);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("leave")) {
			if (player.hasPermission(getPermissionNode("faction leave"))) {
				fresponse = actions.leaveFaction(player);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("invite")) {
			if (player.hasPermission(getPermissionNode("faction invite"))) {
				// Make sure the argument(s) are provided.
				if (args.length < 1) {
					// Grab the example command
					response = onTooltip(player, new Command("faction invite", null));
					r.set(result, response);
					return;
				}
				// Grab the username argument.
				String usernameInvited = args[0];
				// Make sure the username argument is valid.
				if (usernameInvited == null || usernameInvited.isEmpty()) {
					r.set(Result.FAILURE, "Username provided is invalid.");
					return;
				}
				// Grab the Player if he is online.
				Player playerInvited = SledgeHammer.instance.getPlayer(usernameInvited);
				// If the Player is not online, grab the offline version.
				if (playerInvited == null) {
					playerInvited = SledgeHammer.instance.getOfflinePlayer(usernameInvited);
				}
				// If the Player is still null, the Player does not exist.
				if (playerInvited == null) {
					r.set(Result.FAILURE, "Player does not exist: \"" + usernameInvited + "\".");
					return;
				}
				// Attempt to invite the Player.
				fresponse = actions.inviteToFaction(player, playerInvited);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				// Prompt the user the proper arguments.
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("accept")) {
			if (player.hasPermission(getPermissionNode("faction accept"))) {
				if (args.length < 1) {
					response = onTooltip(player, new Command("faction accept", null));
					r.set(result, response);
					return;
				}
				Faction faction = module.getFactionByName(args[0]);
				if (faction == null) {
					r.set(Result.FAILURE, "Faction does not exist: \"" + args[0] + "\".");
					return;
				}
				fresponse = actions.acceptInvite(player, faction);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("reject")) {
			if (player.hasPermission(getPermissionNode("faction reject"))) {
				if (args.length < 1) {
					response = onTooltip(player, new Command("faction reject", null));
					r.set(result, response);
					return;
				}
				Faction faction = null;
				String factionName = args[0];
				if (!factionName.equalsIgnoreCase("all")) {
					faction = module.getFactionByName(args[0]);
					if (faction == null) {
						r.set(Result.FAILURE, "Faction does not exist: \"" + args[0] + "\".");
						return;
					}
				}
				fresponse = actions.rejectInvites(player, faction);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("kick")) {
			if (player.hasPermission(getPermissionNode("faction kick"))) {
				if (args.length < 1) {
					response = onTooltip(player, new Command("faction kick", null));
					r.set(result, response);
					return;
				}
				String usernameKick = args[0];
				// Grab the Player if he is online.
				Player playerKick = SledgeHammer.instance.getPlayer(usernameKick);
				// If the Player is not online, grab the offline version.
				if (playerKick == null) {
					playerKick = SledgeHammer.instance.getOfflinePlayer(usernameKick);
				}
				// If the Player is still null, the Player does not exist.
				if (playerKick == null) {
					r.set(Result.FAILURE, "Player does not exist: \"" + usernameKick + "\".");
					return;
				}
				fresponse = actions.kickFromFaction(player, playerKick);
				r.set(fresponse.getResult(), fresponse.getResponse());
				r.log(LogType.INFO, fresponse.getLogMessage());
				return;
			} else {
				r.deny();
				return;
			}
		} else if (command.equalsIgnoreCase("change")) {

			// @formatter:off
			// To check and see if any permission is present.
			boolean hasAnyPermission = 
					   player.hasPermission(getPermissionNode("faction change"         ))
					|| player.hasPermission(getPermissionNode("faction change password"))
					|| player.hasPermission(getPermissionNode("faction change tag"     ))
					|| player.hasPermission(getPermissionNode("faction change name"    ))
					|| player.hasPermission(getPermissionNode("faction change color"   ));
			// @formatter:on
			if (!hasAnyPermission) {
				r.deny();
				return;
			}

			if (args.length < 1) {
				if (hasAnyPermission) {
					response = onTooltip(player, new Command("faction change", null));
					r.set(result, response);
					return;
				}
			}

			boolean isOwner = true;

			FactionMember factionMember = module.getFactionMember(playerId);
			Faction faction = null;
			if (factionMember != null) {
				faction = factionMember.getFaction();
				isOwner = faction.isOwner(factionMember);
			} else {
				isOwner = false;
			}

			if (!isOwner) {
				response = "You do not own a faction.";
				r.set(result, response);
				return;
			}

			String property = args[0];
			if (property.equalsIgnoreCase("password")) {
				if (player.hasPermission(getPermissionNode("faction change password"))) {
					if (args.length < 3) {
						response = onTooltip(player, new Command("faction password", null));
						r.set(result, response);
						return;
					}
					fresponse = actions.changeFactionPassword(player, args[1], args[2]);
					r.set(fresponse.getResult(), fresponse.getResponse());
					r.log(LogType.INFO, fresponse.getLogMessage());
					return;
				} else {
					r.deny();
					return;
				}
			}

			if (args.length < 2) {
				if (hasAnyPermission) {
					response = onTooltip(player, new Command(command + " " + property, null));
					r.set(result, response);
					return;
				} else {
					r.deny();
					return;
				}
			}

			String value = args[1];

			if (property.equalsIgnoreCase("tag")) {
				if (player.hasPermission(getPermissionNode("faction change tag"))) {
					fresponse = actions.changeFactionTag(player, value);
					r.set(fresponse.getResult(), fresponse.getResponse());
					r.log(LogType.INFO, fresponse.getLogMessage());
					return;
				} else {
					r.deny();
					return;
				}
			} else if (property.equalsIgnoreCase("name")) {
				if (player.hasPermission(getPermissionNode("faction change name"))) {
					fresponse = actions.changeFactionName(player, value);
					r.set(fresponse.getResult(), fresponse.getResponse());
					r.log(LogType.INFO, fresponse.getLogMessage());
					return;
				} else {
					r.deny();
					return;
				}
			} else if (property.equalsIgnoreCase("color")) {
				if (player.hasPermission(getPermissionNode("faction change color"))) {
					fresponse = actions.changeFactionColor(player, value);
					r.set(fresponse.getResult(), fresponse.getResponse());
					r.log(LogType.INFO, fresponse.getLogMessage());
					return;
				} else {
					r.deny();
					return;
				}
			}
			if (hasAnyPermission) {
				response = onTooltip(player, new Command(command + " " + property, null));
				r.set(result, response);
				return;
			} else {
				r.deny();
				return;
			}
		}
	}

	@Override
	public String getName() {
		return "Factions";
	}
}