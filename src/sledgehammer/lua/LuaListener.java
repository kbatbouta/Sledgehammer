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
package sledgehammer.lua;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.event.Event;
import sledgehammer.event.ScriptEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.util.LuaEvent;
import sledgehammer.util.Printable;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoLivingCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoMovingObject;

/**
 * Example EventListener showing how to interface with the ScriptEvent.
 * 
 * (Note: This should not be used because iterating over every single LuaEvent
 * is slow)
 * 
 * (Note: OnTick, OnTickEvenPaused, OnObjectCollide, and onChatacterCollide are
 * the most common LuaEvents called)
 * 
 * @author Jab
 */
public class LuaListener extends Printable implements EventListener {

	public static final String NAME = "LuaListener";

	@Override
	public void onEvent(Event e) {
		boolean validArguments = true;
		ScriptEvent event = (ScriptEvent) e;
		String context = event.getContext();
		Object[] arguments = event.getArguments();
		if (LuaEvent.OnGameBoot.equals(context)) {
			onGameBoot();
			validArguments = arguments.length == 0;
		} else if (LuaEvent.OnPreGameStart.equals(context)) {
			onPreGameStart();
			validArguments = arguments.length == 0;
		} else if (LuaEvent.OnTick.equals(context)) {
			validArguments = _OnTick(arguments);
		} else if (LuaEvent.OnTickEvenPaused.equals(context)) {
			validArguments = _OnTickEvenPaused(arguments);
		} else if (LuaEvent.OnGameStart.equals(context)) {
			onGameStart();
			validArguments = arguments.length == 0;
		} else if (LuaEvent.OnCharacterCollide.equals(context)) {
			validArguments = _OnCharacterCollide(arguments);
		} else if (LuaEvent.OnObjectCollide.equals(context)) {
			validArguments = _OnObjectCollide(arguments);
		} else if (LuaEvent.OnPlayerUpdate.equals(context)) {
			validArguments = _OnPlayerUpdate(arguments);
		} else if (LuaEvent.OnZombieUpdate.equals(context)) {
			validArguments = _OnZombieUpdate(arguments);
		} else if (LuaEvent.OnPlayerUpdate.equals(context)) {
			validArguments = _OnPlayerUpdate(arguments);
		} else if (LuaEvent.OnLoadMapZones.equals(context)) {
			onLoadMapZones();
			validArguments = arguments.length == 0;
		} else if (LuaEvent.OnCreateLivingCharacter.equals(context)) {
			validArguments = _OnCreateLivingCharacter(arguments);
		} else if (LuaEvent.OnDoTileBuilding2.equals(context)) {
			validArguments = _OnDoTileBuilding2(arguments);
		} else if (LuaEvent.OnClientCommand.equals(context)) {
			validArguments = _OnClientCommand(arguments);
		}
		if (!validArguments) {
			println("Invalid number of arguments on ScriptEvent: \"" + context + "\".");
		}
	}

	@Override
	public String[] getTypes() {
		return new String[] { ScriptEvent.ID };
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean runSecondary() {
		return false;
	}

	private boolean _OnTick(Object[] arguments) {
		Double numberOfTicks = 0D;
		if (arguments.length != 1) {
			return false;
		}
		numberOfTicks = (Double) arguments[0];
		onTick(numberOfTicks);
		return true;
	}

	private boolean _OnTickEvenPaused(Object[] arguments) {
		Double numberOfTicks = 0D;
		if (arguments.length != 1) {
			return false;
		}
		numberOfTicks = (Double) arguments[0];
		onTickEvenPaused(numberOfTicks);
		return true;
	}

	private boolean _OnCharacterCollide(Object[] arguments) {
		IsoGameCharacter char1 = null;
		IsoGameCharacter char2 = null;
		if (arguments.length != 2) {
			return false;
		}
		char1 = (IsoGameCharacter) arguments[0];
		char2 = (IsoGameCharacter) arguments[1];
		onCharacterCollide(char1, char2);
		return true;
	}

	private boolean _OnObjectCollide(Object[] arguments) {
		IsoMovingObject obj1 = null;
		IsoObject obj2 = null;
		if (arguments.length != 2) {
			return false;
		}
		obj1 = (IsoMovingObject) arguments[0];
		obj2 = (IsoObject) arguments[1];
		onObjectCollide(obj1, obj2);
		return true;
	}

	private boolean _OnPlayerUpdate(Object[] arguments) {
		IsoPlayer player = null;
		if (arguments.length != 1) {
			return false;
		}
		player = (IsoPlayer) arguments[0];
		onPlayerUpdate(player);
		return true;
	}

	private boolean _OnZombieUpdate(Object[] arguments) {
		return true;
	}

	private boolean _OnCreateLivingCharacter(Object[] arguments) {
		IsoLivingCharacter livingCharacter = null;
		SurvivorDesc descriptor = null;
		if (arguments.length == 2) {
			return false;
		}
		livingCharacter = (IsoLivingCharacter) arguments[0];
		descriptor = (SurvivorDesc) arguments[1];
		onCreateLivingCharacter(livingCharacter, descriptor);
		return true;
	}

	private boolean _OnDoTileBuilding2(Object[] arguments) {
		KahluaTable drag = null;
		Boolean render = false;
		Integer pickedTileX;
		Integer pickedTileY;
		Integer camCharacterZ;
		IsoGridSquare square;
		if (arguments.length != 6) {
			return false;
		}
		drag = (KahluaTable) arguments[0];
		render = (Boolean) arguments[1];
		pickedTileX = (Integer) arguments[2];
		pickedTileY = (Integer) arguments[3];
		camCharacterZ = (Integer) arguments[4];
		square = (IsoGridSquare) arguments[5];
		onDoTileBuilding2(drag, render, pickedTileX, pickedTileY, camCharacterZ, square);
		return true;
	}

	private boolean _OnClientCommand(Object[] arguments) {
		String module = null;
		String command = null;
		IsoPlayer player = null;
		KahluaTable table = null;
		if (arguments.length != 4) {
			return false;
		}
		module = (String) arguments[0];
		command = (String) arguments[1];
		player = (IsoPlayer) arguments[2];
		table = (KahluaTable) arguments[3];
		onClientCommand(module, command, player, table);
		return true;
	}

	public void onGameBoot() {
	}

	public void onPreGameStart() {
	}

	public void onTick(double tickCount) {
	}

	public void onTickEvenPaused(double tickCount) {
	}

	public void onGameStart() {
	}

	public void onCharacterCollide(IsoGameCharacter characterColider, IsoGameCharacter characterColidedWith) {
	}

	public void onObjectCollide(IsoMovingObject objectColider, IsoObject objectColidedWith) {
	}

	public void onPlayerUpdate(IsoPlayer player) {
	}

	public void onLoadMapZones() {
	}

	public void onCreateLivingCharacter(IsoLivingCharacter livingCharacter, SurvivorDesc descriptor) {
	}

	public void onClientCommand(String module, String command, IsoPlayer player, KahluaTable table) {
	}

	public void onDoTileBuilding2(KahluaTable drag, boolean render, int pickedTileX, int pickedTiley, int cameraZ,
			IsoGridSquare square) {
	}
}