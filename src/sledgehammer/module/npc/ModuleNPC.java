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
package sledgehammer.module.npc;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.lua.core.Player;
import sledgehammer.npc.behavior.BehaviorSurvive;
import sledgehammer.plugin.Module;
import sledgehammer.util.Command;
import sledgehammer.util.Response;
import sledgehammer.util.Result;
import sledgehammer.util.ZUtil;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.characters.SurvivorFactory;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.network.ServerMap;
import zombie.sledgehammer.npc.NPC;

/**
 * TODO: Implement.
 * 
 * TODO: Document.
 * 
 * @author Jab
 */
public class ModuleNPC extends Module {

	public static final boolean DEBUG = true;

	private Map<NPC, IsoGameCharacter> mapSpawners;
	private CommandListener commandListener = null;

	public void onLoad() {
		mapSpawners = new HashMap<>();
		// LuaManager.exposer.exposeClass(NPC.class);

		commandListener = new CommandListener() {

			public String[] getCommands() {
				return new String[] { "addnpc", "destroynpcs" };
			}

			public void onCommand(Command c, Response r) {
				String command = c.getCommand();
				String[] args = c.getArguments();
				Player commander = c.getPlayer();
				if (command.equalsIgnoreCase("addnpc")) {
					if (commander.hasPermission(getPermissionNode("addnpc"))) {
						if (args.length == 1) {
							IsoPlayer player = c.getPlayer().getIso();
							IsoGridSquare square = null;
							float x = 0, y = 0, z = 0;
							if (player != null) {

								int attempts = 0;
								int maxAttempts = 50;

								while (square == null) {
									x = player.x + ZUtil.random.nextInt(11) - 5;
									y = player.y + ZUtil.random.nextInt(11) - 5;
									z = player.z;
									square = ServerMap.instance.getGridSquare((int) Math.floor(x), (int) Math.floor(y),
											(int) Math.floor(z));
									if (attempts >= maxAttempts) {
										x = player.x;
										y = player.y;
										z = player.z;
										square = ServerMap.instance.getGridSquare((int) Math.floor(x),
												(int) Math.floor(y), (int) Math.floor(z));
										if (square == null) {
											r.set(Result.FAILURE, "Could not find solid ground to spawn NPC on.");
											return;
										}
									}
									attempts++;
								}
							}
							String name = args[0];
							NPC fakePlayer = createFakePlayer(name, x, y, z);
							println("Adding fake player \"" + name + " at (" + x + "," + y + "," + z
									+ "). PlayerIndex: " + fakePlayer.PlayerIndex + " OnlineID: "
									+ fakePlayer.OnlineID);

							BehaviorSurvive behavior = new BehaviorSurvive(fakePlayer);
							behavior.setDefaultTarget(player);
							behavior.setActive(true);
							fakePlayer.addBehavior(behavior);

							mapSpawners.put(fakePlayer, player);

							r.set(Result.SUCCESS, "NPC created.");
							return;
						} else {
							r.set(Result.FAILURE, onTooltip(c.getPlayer(), c));
							return;
						}
					} else {
						r.set(Result.FAILURE, getPermissionDeniedMessage());
						return;
					}
				} else if (command.equalsIgnoreCase("destroynpcs")) {
					if (commander.hasPermission(getPermissionNode("destroynpcs"))) {
						SledgeHammer.instance.getNPCManager().destroyNPCs();
						r.set(Result.SUCCESS, "NPCs destroyed.");
					} else {
						r.set(Result.FAILURE, getPermissionDeniedMessage());
						return;
					}
				}
			}

			public String onTooltip(Player player, Command command) {
				if (player.hasPermission(getPermissionNode(command.getCommand()))) {
					if (command.getCommand().equalsIgnoreCase("addnpc")) {
						return "Adds a fake player at current location. ex: /addnpc \"name\"";
					} else if (command.getCommand().equalsIgnoreCase("destroynpcs")) {
						return "Destroys all active NPCs.";
					}
				}
				return null;
			}

			public String getPermissionNode(String command) {
				if (command.equalsIgnoreCase("addnpc")) {
					return "sledgehammer.npc.add";
				} else if (command.equalsIgnoreCase("destroynpcs")) {
					return "sledgehammer.npc.remove";
				}
				return null;
			}

		};

		register(commandListener);
	}

	public void onUnload() {
		unregister(commandListener);
	}

	public NPC createFakePlayer(String name, float x, float y, float z) {
		SurvivorDesc desc = SurvivorFactory.CreateSurvivor();
		System.out.println("SurvivorDesc ID: " + desc.getID());
		NPC npc = new NPC((IsoCell) null, desc, name, (int) x, (int) y, (int) z);
		return SledgeHammer.instance.getNPCManager().addNPC(npc);
	}

}
