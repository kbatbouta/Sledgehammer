package sledgehammer.module;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.github.jamm.MemoryMeter;

import sledgehammer.event.CommandEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.util.Result;
import sledgehammer.util.ZUtil;
import sledgehammer.wrapper.Player;
import zombie.iso.IsoWorld;

public class ModuleMonitor extends Module {
	
	public static final String ID = "sledgehammer_monitor";
	
	private static final String newLine = System.getProperty("line.separator");
	long timeThen = 0L;
	
	private MemoryMeter meter;
	
	public void onLoad() {
		this.mapMaps = new HashMap<>();
		this.mapSize = new HashMap<>();
		this.mapSizeInv = new HashMap<>();
	}

	public void onStart() {
		if(!ZUtil.isClass("org.github.jamm.MemoryMeter")) {
			println("JAMM must be set as Java Agent in order to use this module.");
			unload();
			return;
		}
		if(!MemoryMeter.isInitialized()) {
			println("JAMM must be set as Java Agent in order to use this module.");
			unload();
			return;
		}
		
		meter = new MemoryMeter();
		println("Global Map monitoring can be done using /gcdump (With some lag).","Logs will be avaliable in the \"dumps/\" folder inside of your server folder.");
	
		register(new CommandListener() {

			@Override
			public String[] getCommands() {
				return new String[] {"gcdump"};
			}

			@Override
			public void onCommand(CommandEvent c) {
				String command = c.getCommand();
				Player player = c.getPlayer();
				if(hasPermission(player.getUsername(), getPermissionContext("gcdump"))) {
					if(command.equalsIgnoreCase("gcdump")) {
						gcdump();
						c.setResponse(Result.SUCCESS, "Dumping global Map object information to file.");
					}
				}
				return;
			}

			public String onTooltip(Player player, String command) {
				return null;
			}

			public String getPermissionContext(String command) {
				if(command.equalsIgnoreCase("gcdump")) {
					return "sledgehammer.monitor.gcdump";
				}
				return null;
			}
			
		});
	
	}
	
	public void gcdump() {
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				java.util.Date date= new java.util.Date();
				String timeStamp = new Timestamp(date.getTime()).toString().replace(" ", "T").replace(":", ".");			
				File folder = new File("dumps" + File.separator);
				if(!folder.exists()) folder.mkdirs();
				String fileName = folder.getAbsolutePath() + File.separator + "Global_Maps___(" + timeStamp + ").txt";
				File file = new File(fileName);
				println("Dumping global Map object information to file: \"" + fileName + "\".");
				
				FileWriter writer = null;
				try {
					writer = new FileWriter(file);
					printMaps(writer);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		};
		
		(new Thread(runnable)).run();
		
	}
	

	public void onUpdate(long delta) {
		
	}

	Map<String, Long> mapSize = new HashMap<>();
	Map<Long, List<String>> mapSizeInv = new HashMap<>();
	Map<String, Map<?,?>> mapMaps = new HashMap<>();
	private void printMaps(FileWriter writer) throws IOException {
		try {
			getSize("zombie.characters.professions.ProfessionFactory.ProfessionMap" ,zombie.characters.professions.ProfessionFactory.ProfessionMap);
			getSize("zombie.characters.skills.PerkFactory.PerkMap"					,zombie.characters.skills.PerkFactory.PerkMap);
			getSize("zombie.characters.traits.ObservationFactory.ObservationMap"	,zombie.characters.traits.ObservationFactory.ObservationMap);
			getSize("zombie.characters.traits.TraitFactory.TraitMap"				,zombie.characters.traits.TraitFactory.TraitMap);
			getSize("zombie.core.opengl.Shader.ShaderMap"							,zombie.core.opengl.Shader.ShaderMap);
			getSize("zombie.iso.sprite.IsoAnim.GlobalAnimMap"						,zombie.iso.sprite.IsoAnim.GlobalAnimMap);
			getSize("zombie.iso.IsoChunkMap.SharedChunks"							,zombie.iso.IsoChunkMap.SharedChunks);
			getSize("zombie.iso.IsoFloorBloodSplat.SpriteMap"						,zombie.iso.IsoFloorBloodSplat.SpriteMap);
			getSize("zombie.iso.IsoLot.InfoHeaders"									,zombie.iso.IsoLot.InfoHeaders);
			getSize("zombie.iso.IsoLot.InfoFileNames"								,zombie.iso.IsoLot.InfoFileNames);
			getSize("zombie.network.GameServer.IDToAddressMap"						,zombie.network.GameServer.IDToAddressMap);
			getSize("zombie.network.GameServer.IDToPlayerMap"						,zombie.network.GameServer.IDToPlayerMap);
			getSize("zombie.network.GameServer.PlayerToAddressMap"					,zombie.network.GameServer.PlayerToAddressMap);
			getSize("zombie.network.GameServer.PlayerToBody"						,zombie.network.GameServer.PlayerToBody);
			getSize("zombie.network.ServerMap.instance.ZombieMap"					,zombie.network.ServerMap.instance.ZombieMap);
			getSize("zombie.radio.ZomboidRadio.getInstance().getFullChannelList()"	,zombie.radio.ZomboidRadio.getInstance().getFullChannelList());
			getSize("zombie.scripting.objects.Item.NetIDToItem"                   	,zombie.scripting.objects.Item.NetIDToItem);
			getSize("zombie.scripting.objects.Item.NetItemToID"						,zombie.scripting.objects.Item.NetItemToID);
			getSize("zombie.scripting.ScriptManager.instance.TriggerMap"			,zombie.scripting.ScriptManager.instance.TriggerMap);
			getSize("zombie.scripting.ScriptManager.instance.CustomTriggerMap"		,zombie.scripting.ScriptManager.instance.CustomTriggerMap);
			getSize("zombie.scripting.ScriptManager.instance.HookMap"				,zombie.scripting.ScriptManager.instance.HookMap);
			getSize("zombie.scripting.ScriptManager.instance.ModuleAliases"			,zombie.scripting.ScriptManager.instance.ModuleAliases);
			getSize("zombie.scripting.ScriptManager.instance.MapMap"				,zombie.scripting.ScriptManager.instance.MapMap);
			getSize("zombie.ZomboidBitFlag.BitFlags"								,zombie.ZomboidBitFlag.BitFlags);
			getSize("zombie.scripting.ScriptManager.instance.ModuleMap"				,zombie.scripting.ScriptManager.instance.ModuleMap);
			getSize("zombie.characters.IsoGameCharacter.SurvivorMap"				,(Map<?,?>)getPrivate("zombie.characters.IsoGameCharacter.SurvivorMap"));
			getSize("zombie.characters.WeaponOverlayUtils.SpriteMap"				,(Map<?,?>)getPrivate("zombie.characters.WeaponOverlayUtils.SpriteMap"));
			getSize("zombie.gameStates.ChooseGameInfo.Maps"							,(Map<?,?>)getPrivate("zombie.gameStates.ChooseGameInfo.Maps"));
			getSize("zombie.gameStates.IngameState.ContainerTypes"					,(Map<?,?>)getPrivate("zombie.gameStates.IngameState.ContainerTypes"));
			getSize("zombie.iso.objects.IsoWaveSignal.deviceDataCache"				,(Map<?,?>)getPrivate("zombie.iso.objects.IsoWaveSignal.deviceDataCache"));
			getSize("zombie.core.logger.LoggerManager._loggers"						,(Map<?,?>)getPrivate("zombie.core.logger.LoggerManager._loggers"));
			getSize("zombie.iso.sprite.IsoSprite.AnimNameSet"						,(Map<?,?>)getPrivate("zombie.iso.sprite.IsoSprite.AnimNameSet"));
			getSize("zombie.iso.SpriteDetails.IsoFlagType.fromStringMap"			,(Map<?,?>)getPrivate("zombie.iso.SpriteDetails.IsoFlagType.fromStringMap"));
			getSize("zombie.Lua.LuaHookManager.EventMap"							,(Map<?,?>)getPrivate("zombie.Lua.LuaHookManager.EventMap"));
			getSize("zombie.Lua.LuaEventManager.EventMap"							,(Map<?,?>)getPrivate("zombie.Lua.LuaEventManager.EventMap"));
			getSize("zombie.network.GameServer.playerToCoordsMap"					,(Map<?,?>)getPrivate("zombie.network.GameServer.playerToCoordsMap"));
			getSize("zombie.network.GameServer.playerMovedToFastMap"				,(Map<?,?>)getPrivate("zombie.network.GameServer.playerMovedToFastMap"));
			getSize("zombie.radio.globals.RadioGlobalsManager.instance.globals"		,(Map<?,?>)getPrivate(getPrivate("zombie.radio.globals.RadioGlobalsManager.instance"),"globals"));
			getSize("zombie.radio.scripting.RadioScriptManager.instance.channels"	,(Map<?,?>)getPrivate(getPrivate("zombie.radio.scripting.RadioScriptManager.instance"),"channels"));
			getSize("zombie.radio.ZomboidRadio.getInstance().channelNames"			,(Map<?,?>)getPrivate(zombie.radio.ZomboidRadio.getInstance(),"channelNames"));
			getSize("zombie.radio.ZomboidRadio.getInstance().freqlist"				,(Map<?,?>)getPrivate(zombie.radio.ZomboidRadio.getInstance(),"freqlist"));
			getSize("zombie.radio.ZomboidRadio.getInstance().CustomTriggerLastRan"	,(Map<?,?>)getPrivate(zombie.scripting.ScriptManager.instance,"CustomTriggerLastRan"));
			getSize("zombie.scripting.ScriptManager.instance.CachedModules"			,(Map<?,?>)getPrivate(zombie.scripting.ScriptManager.instance,"CachedModules"));
			getSize("zombie.gameStates.ChooseGameInfo.Maps"							,(Map<?,?>)getPrivate("zombie.gameStates.ChooseGameInfo.Maps"));
			try {getSize("zombie.Lua.LuaManager.loadedReturn",zombie.Lua.LuaManager3427.loadedReturn);} catch(ExceptionInInitializerError e){};
			if(IsoWorld.instance != null && IsoWorld.instance.spriteManager != null) {
				getSize("IsoWorld.instance.spriteManager.IntMap"   	,IsoWorld.instance.spriteManager.IntMap);
				getSize("IsoWorld.instance.spriteManager.NamedMap" 	,IsoWorld.instance.spriteManager.NamedMap);
			}
			
			Long[] array = new Long[this.mapSizeInv.keySet().size()];
			this.mapSizeInv.keySet().toArray(array);
			Arrays.sort(array, Collections.reverseOrder());
			
			for(long size : array) {
				List<String> locations = mapSizeInv.get(size);
				if(locations != null) {
					for(String location : locations) {
						if(location != null) {
							Map<?,?> map = mapMaps.get(location);
							
							if(map == null) { 
								writer.write("Map: " + location + newLine);
								writer.write("\t" + "null" + newLine + newLine); 
								return;
							}
							String mapType = map.getClass().getName();
							writer.write(mapType.substring(mapType.lastIndexOf(".") + 1) + ": " + location + newLine);
							writer.write("\t" + "Count: " + map.size() + newLine);
							if(MemoryMeter.isInitialized()) {
								writer.write("\t" + "Memory (Bytes): " + size + newLine);
							}
						}
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | NoSuchFieldException | SecurityException e) {
			mapSize.clear();
			mapSizeInv.clear();
			mapMaps.clear();
			e.printStackTrace();
		}
		
		mapSize.clear();
		mapSizeInv.clear();
		mapMaps.clear();
	}
	
	private long getSize(String location, Map<?,?> map) {
		
		println("getSize(" + location + ")");
		long size = 0L;
		size = meter.measureDeep(map);
		this.mapSize.put(location, size);
		List<String> listLocations = this.mapSizeInv.get(size);
		if(listLocations == null) {
			listLocations = new ArrayList<>();
			mapSizeInv.put(size, listLocations);
		} 
		listLocations.add(location);
		mapMaps.put(location, map);
		println("\tSize: " + size + "\tCount:" + map.size());
		return size;
	}

	private Object getPrivate(Object inst, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(inst == null) {
			println("Instance is null for object containing the field \"" + fieldName + "\". Returning null field instance.");
			return null; 
		}
		Field f = inst.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		try{
			return f.get(inst);
		} catch(NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void onStop() {
		println("Global Map monitoring stopped.");
	}

	public void onUnload() {}
	
	public static Object getPrivate(String path)
			throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, NoSuchFieldException, SecurityException {
		int lastDot = path.lastIndexOf(".");
		String className = path.substring(0, lastDot);
		String fieldName = path.substring(lastDot + 1);
		Class<?> myClass = Class.forName(className);
		Field myField = myClass.getDeclaredField(fieldName);
		myField.setAccessible(true);
		return myField.get(null);
	}
	
	public static void main(String[] args) {
		ModuleMonitor m = new ModuleMonitor();
		m.onLoad();
		m.onUpdate(0L);
	}

	public String getModuleName() { return "Memory Monitor"; }
	public String getVersion()    { return "1.00";           }

	@Override
	public String getModuleID() {
		return ID;
	}

}