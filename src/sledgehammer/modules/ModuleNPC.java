package sledgehammer.modules;

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

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.CommandEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.module.SQLModule;
import sledgehammer.npc.behavior.BehaviorSurvive;
import sledgehammer.util.Result;
import sledgehammer.util.ZUtil;
import sledgehammer.wrapper.Player;
import zombie.Lua.LuaManager;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.characters.SurvivorFactory;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.network.DataBaseBuffer;
import zombie.network.ServerMap;
import zombie.sledgehammer.npc.NPC;

public class ModuleNPC extends SQLModule {

	public static final String ID      = "sledgehammer_npc";
	public static final String NAME    = "NPC-Spawner"     ;
	public static final String VERSION = "1.00"            ;
	public static final boolean DEBUG  = true              ;
	
	private Map<NPC, IsoGameCharacter> mapSpawners;
	
	private ModuleNPC module = null;
	
	private CommandListener commandListener = null;
	
	public ModuleNPC() {
		super(DataBaseBuffer.getDatabaseConnection());
		module = this;
	}

	public void onLoad() {
		mapSpawners = new HashMap<>();
		// LuaManager.exposer.exposeClass(NPC.class);
		
		commandListener = new CommandListener() {

			public String[] getCommands() {
				return new String[] { "addnpc", "destroynpcs"};
			}

			public void onCommand(CommandEvent c) {
				String command = c.getCommand();
				String[] args = c.getArguments();
				Player commander = c.getPlayer();
				String commanderName = commander.getUsername();
				if(command.equalsIgnoreCase("addnpc")) {
					if(module.hasPermission(commanderName, getPermissionContext("addnpc"))) {						
						if(args.length == 1) {
							IsoPlayer player = c.getPlayer().get();
							IsoGridSquare square = null;
							float x = 0, y = 0, z = 0;
							if(player != null) {
								
								int attempts = 0;
								int maxAttempts = 50;
								
								while(square == null) {									
									x = player.x + ZUtil.random.nextInt(11) - 5;
									y = player.y + ZUtil.random.nextInt(11) - 5;
									z = player.z;		
									square = ServerMap.instance.getGridSquare((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
									if(attempts >= maxAttempts) {
										x = player.x;
										y = player.y;
										z = player.z;			
										square = ServerMap.instance.getGridSquare((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
										if(square == null) {											
											c.setResponse(Result.FAILURE, "Could not find solid ground to spawn NPC on.");
											return;
										}
									}
									attempts++;
								}
							}
							String name = args[0];
							NPC fakePlayer = createFakePlayer(name, x, y, z);
							println("Adding fake player \"" + name + " at (" + x + "," + y + "," + z + "). PlayerIndex: " + fakePlayer.PlayerIndex + " OnlineID: " + fakePlayer.OnlineID);
							
							BehaviorSurvive behavior = new BehaviorSurvive(fakePlayer);
							behavior.setDefaultTarget(player);
							behavior.setActive(true);
							fakePlayer.addBehavior(behavior);
							
							
							
							mapSpawners.put(fakePlayer, player);
							
							c.setResponse(Result.SUCCESS, "NPC created.");
							return;
						} else {
							c.setResponse(Result.FAILURE, onTooltip(c.getPlayer(), command));
							return;
						}
					} else {
						c.setResponse(Result.FAILURE, getPermissionDeniedMessage());
						return;
					}
				} else if(command.equalsIgnoreCase("destroynpcs")) {
					if(module.hasPermission(commanderName, getPermissionContext("destroynpcs"))) {						
						SledgeHammer.instance.getNPCManager().destroyNPCs();
						c.setResponse(Result.SUCCESS, "NPCs destroyed.");
					} else {
						c.setResponse(Result.FAILURE, getPermissionDeniedMessage());
						return;
					}
				}
			}

			public String onTooltip(Player player, String command) {
				if(module.hasPermission(player.getUsername(), getPermissionContext(command))) {
					if(command.equalsIgnoreCase("addnpc")) {
						return "Adds a fake player at current location. ex: /addnpc \"name\"";
					} else 
					if(command.equalsIgnoreCase("destroynpcs")) {
						return "Destroys all active NPCs.";
					}
				}
				return null;
			}

			public String getPermissionContext(String command) {
				if(command.equalsIgnoreCase("addnpc")) {
					return "sledgehammer.npc.add";
				} else 
				if(command.equalsIgnoreCase("destroynpcs")) {
					return "sledgehammer.npc.remove";
				}
				return null;
			}
		};
	
		register(commandListener);
	}

	public void onStart() {}
	public void onStop() {}

	public void onUnload() {
		unregister(commandListener);
	}
	
	public NPC createFakePlayer(String name, float x, float y, float z) {
		SurvivorDesc desc = SurvivorFactory.CreateSurvivor();
		System.out.println("SurvivorDesc ID: " + desc.getID());
		NPC npc = new NPC((IsoCell) null, desc, name, (int) x, (int) y, (int) z);
		return SledgeHammer.instance.getNPCManager().addNPC(npc);
	}

	public void onUpdate(long delta) {}
	
	public String getID()      { return ID      ; }
	public String getName()    { return NAME    ; }
	public String getVersion() { return VERSION ; }


}
