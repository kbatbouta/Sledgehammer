package sledgehammer.test;

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

import zombie.Lua.LuaManager;
import zombie.core.Core;
import zombie.core.znet.SteamUtils;
import zombie.network.ServerWorldDatabase;
import sledgehammer.SledgeHammer;
import sledgehammer.module.faction.ModuleFactions;
import sledgehammer.util.Printable;

/**
 * 
 * @author Jab
 *
 */
public class TestFactionRelationships extends Printable {
	
	public int faction1ID = 1;
	public int faction2ID = 127;
	public String faction1Name = "Developer";
	public String faction2Name = "Gonder";
	
	private ModuleFactions module;
	
	public TestFactionRelationships() {
		
	}
	
	public void run() {
		module = new ModuleFactions();
		SledgeHammer.instance.getPluginManager().registerModule(module);
		module.onLoad();
		module.onStart();
		
//		test1();
		// test2();
		
		module.onStop();
		module.onUnload();
	}
	
	public void test1() {
//		println("Test 1: Invoke Relationship.");
//		module.getActions().invokeRelationship(faction1Name, faction2Name, false, false, ModuleFactions.RELATIONSHIP_WAR, null);
		// println("\tDeleting Relationship.");
		// module.deleteRelationship(faction1ID, faction2ID);
//		println();
	}
	
	public void test2() {
//		println("Test 2: Invoke Relationship Request.");
//		module.getActions().invokeRelationship(faction1Name, faction2Name, false, false, ModuleFactions.RELATIONSHIP_ALLIED, "Please?");
//		println("\tDeleting Relationship Request.");
//		module.getActions().deleteRelationshipRequest(faction1ID, faction2ID);
//		println();
	}
	
	public void test3() {
		
	}
	
	public static void main(String[] args) {
		try {
			
			// Start the database.
			ServerWorldDatabase.instance.create();
			Core.GameSaveWorld = "servertest";
			
			SteamUtils.init();
			LuaManager.init();
			
			// Start Sledgehammer in debug mode.
			SledgeHammer.instance = new SledgeHammer(false);
			SledgeHammer.instance.init();
			
			TestFactionRelationships test = new TestFactionRelationships();
			test.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "TEST-FACTIONS-RELATIONSHIPS";
	}
}
