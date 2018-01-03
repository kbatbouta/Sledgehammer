package sledgehammer.module.npc;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.lua.core.Player;
import sledgehammer.npc.behavior.BehaviorSurvive;
import sledgehammer.util.Command;
import sledgehammer.util.Printable;
import sledgehammer.util.Response;
import sledgehammer.util.Result;
import sledgehammer.util.ZUtil;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoGridSquare;
import zombie.network.ServerMap;
import zombie.sledgehammer.npc.NPC;

/**
 * CommandListener to handle NPC <Command>'s for the NPC Module for the Core
 * plug-in.
 * 
 * @author Jab
 */
public class NPCCommandListener extends Printable implements CommandListener {

	/** The <ModuleNPC> instance using the <CommandListener>. */
	private ModuleNPC module;

	/**
	 * Main constructor.
	 * 
	 * @param module
	 *            The <ModuleNPC> instance using the <CommandListener>.
	 */
	public NPCCommandListener(ModuleNPC module) {
		setModule(module);
	}

	@Override
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
								square = ServerMap.instance.getGridSquare((int) Math.floor(x), (int) Math.floor(y),
										(int) Math.floor(z));
								if (square == null) {
									r.set(Result.FAILURE, "Could not find solid ground to spawn NPC on.");
									return;
								}
							}
							attempts++;
						}
					}
					String name = args[0];
					NPC fakePlayer = module.createFakePlayer(name, x, y, z);
					println("Adding fake player \"" + name + " at (" + x + "," + y + "," + z + "). PlayerIndex: "
							+ fakePlayer.PlayerIndex + " OnlineID: " + fakePlayer.OnlineID);

					BehaviorSurvive behavior = new BehaviorSurvive(fakePlayer);
					behavior.setDefaultTarget(player);
					behavior.setActive(true);
					fakePlayer.addBehavior(behavior);

					module.mapSpawners.put(fakePlayer, player);

					r.set(Result.SUCCESS, "NPC created.");
					return;
				} else {
					r.set(Result.FAILURE, onTooltip(c.getPlayer(), c));
					return;
				}
			} else {
				r.set(Result.FAILURE, module.getPermissionDeniedMessage());
				return;
			}
		} else if (command.equalsIgnoreCase("destroynpcs")) {
			if (commander.hasPermission(getPermissionNode("destroynpcs"))) {
				SledgeHammer.instance.getNPCManager().destroyNPCs();
				r.set(Result.SUCCESS, "NPCs destroyed.");
			} else {
				r.set(Result.FAILURE, module.getPermissionDeniedMessage());
				return;
			}
		}
	}

	@Override
	public String getName() {
		return "NPCCommandListener";
	}

	@Override
	public String[] getCommands() {
		// @formatter:off
		return new String[] { 
			"addnpc", 
			"destroynpcs" 
		};
		// @formatter:on
	}

	@Override
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

	@Override
	public String getPermissionNode(String command) {
		if (command.equalsIgnoreCase("addnpc")) {
			return "sledgehammer.npc.add";
		} else if (command.equalsIgnoreCase("destroynpcs")) {
			return "sledgehammer.npc.remove";
		}
		return null;
	}

	/**
	 * @return Returns the <ModuleNPC> instance using the <CommandListener>.
	 */
	public ModuleNPC getModule() {
		return this.module;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <ModuleNPC> instance using the <CommandListener>.
	 * 
	 * @param module
	 *            The <ModuleNPC> instance to set.
	 */
	private void setModule(ModuleNPC module) {
		this.module = module;
	}
}