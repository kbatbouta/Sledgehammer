package sledgehammer.module;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.CommandEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.npc.BehaviorSurvive;
import sledgehammer.npc.NPC;
import sledgehammer.util.Result;
import sledgehammer.wrapper.Player;
import zombie.Lua.LuaManager;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.characters.SurvivorFactory;
import zombie.iso.IsoCell;
import zombie.network.DataBaseBuffer;

public class ModuleNPC extends SQLModule {

	public static final String ID = "sledgehammer_npc";
	
	private Map<NPC, IsoGameCharacter> mapSpawners;
	
	public ModuleNPC() {
		super(DataBaseBuffer.getDatabaseConnection());
	}

	public void onLoad() {
		mapSpawners = new HashMap<>();
		LuaManager.exposer.exposeClass(NPC.class);
	}

	public void onStart() {
		register(new CommandListener() {

			public String[] getCommands() {
				return new String[] { "addnpc" };
			}

			public void onCommand(CommandEvent c) {
				String command = c.getCommand();
				String[] args = c.getArguments();
				if(command.equalsIgnoreCase("addnpc")) {
					if(args.length == 1) {
						IsoPlayer player = c.getPlayer().get();
						float x = 0, y = 0, z = 0;
						if(player != null) {
							x = player.x;
							y = player.y;
							z = player.z;							
						}
						String name = args[0];
						NPC fakePlayer = createFakePlayer(name, x, y, z);
						println("Adding fake player \"" + name + " at (" + x + "," + y + "," + z + "). PlayerIndex: " + fakePlayer.PlayerIndex + " OnlineID: " + fakePlayer.OnlineID);

						BehaviorSurvive behavior = new BehaviorSurvive(fakePlayer);
						behavior.followDefault(player);
						behavior.setActive(true);
						fakePlayer.addBehavior(behavior);
						
						
						
						mapSpawners.put(fakePlayer, player);
						
						c.setResponse(Result.SUCCESS, "Fake player created.");
					} else {
						c.setResponse(Result.FAILURE, onTooltip(c.getPlayer(), command));
					}
				}
			}

			public String onTooltip(Player player, String command) {
				if(player.isAdmin()) {
					if(command.equalsIgnoreCase("addnpc")) {
						return "Adds a fake player at current location. ex: /addnpc \"name\"";
					}
				}
				return null;
			}

			public String getPermissionContext(String command) {
				
				if(command.equalsIgnoreCase("addnpc")) {
					return "sledgehammer.npc.addnpc";
				}
				
				return null;
			}
		});
	}
	
	public NPC createFakePlayer(String name, float x, float y, float z) {
		SurvivorDesc desc = SurvivorFactory.CreateSurvivor();
		System.out.println("SurvivorDesc ID: " + desc.getID());
		NPC npc = new NPC((IsoCell) null, desc, name, (int) x, (int) y, (int) z);
		return SledgeHammer.instance.getNPCEngine().addNPC(npc);
	}

	public void onUpdate(long delta) {}
	public void onStop() {}
	public void onUnload() {}
	public String getModuleName() { return "FakePlayer"; }
	public String getVersion()    { return "1.00";       }

	public String getModuleID() {
		return ID;
	}

}
