package zirc.module;

import java.util.HashMap;
import java.util.Map;

import zirc.ZIRC;
import zirc.event.CommandEvent;
import zirc.event.CommandEvent.Result;
import zirc.interfaces.CommandListener;
import zirc.wrapper.NPC;
import zirc.wrapper.Player;
import zombie.Lua.LuaManager;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.characters.SurvivorFactory;
import zombie.iso.IsoCell;
import zombie.iso.IsoNPCPlayer;
import zombie.network.DataBaseBuffer;

public class ModuleNPC extends SQLModule {

	private Map<IsoNPCPlayer, IsoGameCharacter> mapSpawners;
	
	public ModuleNPC() {
		super(DataBaseBuffer.getDatabaseConnection());
	}

	public void onLoad() {
		mapSpawners = new HashMap<>();
		LuaManager.exposer.exposeClass(NPC.class);
	}

	public void onStart() {
		register(new CommandListener() {

			@Override
			public String[] getCommands() {
				return new String[] { "addfakeplayer" };
			}

			@Override
			public void onCommand(CommandEvent c) {
				String command = c.getCommand();
				String[] args = c.getArguments();
				if(command.equalsIgnoreCase("addfakeplayer")) {
					if(args.length == 1) {
						IsoPlayer player = c.getPlayer().get();
						float x = 0, y = 0, z = 0;
						if(player != null) {
							x = player.x;
							y = player.y;
							z = player.z;							
						}
						String name = args[0];
						println("Adding fake player \"" + name + " at (" + x + "," + y + "," + z + ").");
						IsoNPCPlayer fakePlayer = createFakePlayer(name, x, y, z);
						mapSpawners.put(fakePlayer, player);
						c.setResponse(Result.SUCCESS, "Fake player created.");
					} else {
						c.setResponse(Result.FAILURE, onTooltip(c.getPlayer(), command));
					}
				}
			}

			@Override
			public String onTooltip(Player player, String command) {
				if(player.isAdmin()) {
					if(command.equalsIgnoreCase("addfakeplayer")) {
						return "Adds a fake player at current location. ex: /addfakeplayer \"name\"";
					}
				}
				return null;
			}
		});
	}

	
	public NPC createFakePlayer(String name, float x, float y, float z) {
		SurvivorDesc desc = SurvivorFactory.CreateSurvivor();
		NPC npc = new NPC((IsoCell) null, desc, name, (int) x, (int) y, (int) z);
		return ZIRC.instance.addNPC(npc);
	}

	public void onUpdate(long delta) {}
	public void onStop() {}
	public void onUnload() {}
	public String getModuleName() { return "FakePlayer"; }
	public String getVersion()    { return "1.00";       }

}
