///*
// * This file is part of Sledgehammer.
// *
// *    Sledgehammer is free software: you can redistribute it and/or modify
// *    it under the terms of the GNU Lesser General Public License as published by
// *    the Free Software Foundation, either version 3 of the License, or
// *    (at your option) any later version.
// *
// *    Sledgehammer is distributed in the hope that it will be useful,
// *    but WITHOUT ANY WARRANTY; without even the implied warranty of
// *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *    GNU Lesser General Public License for more details.
// *
// *    You should have received a copy of the GNU Lesser General Public License
// *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
// *
// *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
// *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
// */
//
//package sledgehammer.lua;
//
//import se.krka.kahlua.vm.KahluaTable;
//import sledgehammer.enums.LuaEvent;
//import sledgehammer.event.Event;
//import sledgehammer.event.ScriptEvent;
//import sledgehammer.interfaces.EventListener;
//import sledgehammer.interfaces.Listener;
//import sledgehammer.util.Printable;
//import zombie.characters.IsoGameCharacter;
//import zombie.characters.IsoLivingCharacter;
//import zombie.characters.IsoPlayer;
//import zombie.characters.SurvivorDesc;
//import zombie.iso.IsoGridSquare;
//import zombie.iso.IsoObject;
//import zombie.iso.IsoMovingObject;
//
///**
// * Example EventListener showing how to interface with the ScriptEvent.
// * <p>
// * (Note: This should not be used because iterating over every single LuaEvent
// * is slow)
// * <p>
// * (Note: OnTick, OnTickEvenPaused, OnObjectCollide, and onCharacterCollide are
// * the most common LuaEvents called)
// *
// * @author Jab
// */
//public class LuaListener extends Printable implements Listener {
//
//    public static final String NAME = "LuaListener";
//
//    @Override
//    public void onEvent(Event e) {
//        boolean validArguments = true;
//        ScriptEvent event = (ScriptEvent) e;
//        String context = event.getContext();
//        Object[] arguments = event.getArguments();
//        if (LuaEvent.OnGameBoot.is(context)) {
//            onGameBoot();
//            validArguments = arguments.length == 0;
//        } else if (LuaEvent.OnPreGameStart.is(context)) {
//            onPreGameStart();
//            validArguments = arguments.length == 0;
//        } else if (LuaEvent.OnTick.is(context)) {
//            validArguments = _OnTick(arguments);
//        } else if (LuaEvent.OnTickEvenPaused.is(context)) {
//            validArguments = _OnTickEvenPaused(arguments);
//        } else if (LuaEvent.OnGameStart.is(context)) {
//            onGameStart();
//            validArguments = arguments.length == 0;
//        } else if (LuaEvent.OnCharacterCollide.is(context)) {
//            validArguments = _OnCharacterCollide(arguments);
//        } else if (LuaEvent.OnObjectCollide.is(context)) {
//            validArguments = _OnObjectCollide(arguments);
//        } else if (LuaEvent.OnPlayerUpdate.is(context)) {
//            validArguments = _OnPlayerUpdate(arguments);
//        } else if (LuaEvent.OnZombieUpdate.is(context)) {
//            validArguments = _OnZombieUpdate(arguments);
//        } else if (LuaEvent.OnPlayerUpdate.is(context)) {
//            validArguments = _OnPlayerUpdate(arguments);
//        } else if (LuaEvent.OnLoadMapZones.is(context)) {
//            onLoadMapZones();
//            validArguments = arguments.length == 0;
//        } else if (LuaEvent.OnCreateLivingCharacter.is(context)) {
//            validArguments = _OnCreateLivingCharacter(arguments);
//        } else if (LuaEvent.OnDoTileBuilding2.is(context)) {
//            validArguments = _OnDoTileBuilding2(arguments);
//        } else if (LuaEvent.OnClientCommand.is(context)) {
//            validArguments = _OnClientCommand(arguments);
//        }
//        if (!validArguments) {
//            println("Invalid number of arguments on ScriptEvent: \"" + context + "\".");
//        }
//    }
//
//    @Override
//    public String[] getTypes() {
//        return new String[]{ScriptEvent.ID};
//    }
//
//    @Override
//    public String getName() {
//        return NAME;
//    }
//
//    @Override
//    public boolean runSecondary() {
//        return false;
//    }
//
//    private boolean _OnTick(Object[] arguments) {
//        Double numberOfTicks;
//        if (arguments.length != 1) {
//            return false;
//        }
//        numberOfTicks = (Double) arguments[0];
//        onTick(numberOfTicks);
//        return true;
//    }
//
//    private boolean _OnTickEvenPaused(Object[] arguments) {
//        Double numberOfTicks;
//        if (arguments.length != 1) {
//            return false;
//        }
//        numberOfTicks = (Double) arguments[0];
//        onTickEvenPaused(numberOfTicks);
//        return true;
//    }
//
//    private boolean _OnCharacterCollide(Object[] arguments) {
//        IsoGameCharacter char1;
//        IsoGameCharacter char2;
//        if (arguments.length != 2) {
//            return false;
//        }
//        char1 = (IsoGameCharacter) arguments[0];
//        char2 = (IsoGameCharacter) arguments[1];
//        onCharacterCollide(char1, char2);
//        return true;
//    }
//
//    private boolean _OnObjectCollide(Object[] arguments) {
//        IsoMovingObject obj1;
//        IsoObject obj2;
//        if (arguments.length != 2) {
//            return false;
//        }
//        obj1 = (IsoMovingObject) arguments[0];
//        obj2 = (IsoObject) arguments[1];
//        onObjectCollide(obj1, obj2);
//        return true;
//    }
//
//    private boolean _OnPlayerUpdate(Object[] arguments) {
//        IsoPlayer player;
//        if (arguments.length != 1) {
//            return false;
//        }
//        player = (IsoPlayer) arguments[0];
//        onPlayerUpdate(player);
//        return true;
//    }
//
//    private boolean _OnZombieUpdate(Object[] arguments) {
//        return true;
//    }
//
//    private boolean _OnCreateLivingCharacter(Object[] arguments) {
//        IsoLivingCharacter livingCharacter;
//        SurvivorDesc descriptor;
//        if (arguments.length == 2) {
//            return false;
//        }
//        livingCharacter = (IsoLivingCharacter) arguments[0];
//        descriptor = (SurvivorDesc) arguments[1];
//        onCreateLivingCharacter(livingCharacter, descriptor);
//        return true;
//    }
//
//    private boolean _OnDoTileBuilding2(Object[] arguments) {
//        KahluaTable drag;
//        Boolean render;
//        Integer pickedTileX;
//        Integer pickedTileY;
//        Integer camCharacterZ;
//        IsoGridSquare square;
//        if (arguments.length != 6) {
//            return false;
//        }
//        drag = (KahluaTable) arguments[0];
//        render = (Boolean) arguments[1];
//        pickedTileX = (Integer) arguments[2];
//        pickedTileY = (Integer) arguments[3];
//        camCharacterZ = (Integer) arguments[4];
//        square = (IsoGridSquare) arguments[5];
//        onDoTileBuilding2(drag, render, pickedTileX, pickedTileY, camCharacterZ, square);
//        return true;
//    }
//
//    private boolean _OnClientCommand(Object[] arguments) {
//        String module;
//        String command;
//        IsoPlayer player;
//        KahluaTable table;
//        if (arguments.length != 4) {
//            return false;
//        }
//        module = (String) arguments[0];
//        command = (String) arguments[1];
//        player = (IsoPlayer) arguments[2];
//        table = (KahluaTable) arguments[3];
//        onClientCommand(module, command, player, table);
//        return true;
//    }
//
//    public void onGameBoot() {
//    }
//
//    public void onPreGameStart() {
//    }
//
//    public void onTick(double tickCount) {
//    }
//
//    public void onTickEvenPaused(double tickCount) {
//    }
//
//    public void onGameStart() {
//    }
//
//    public void onCharacterCollide(IsoGameCharacter characterColider, IsoGameCharacter characterColidedWith) {
//    }
//
//    public void onObjectCollide(IsoMovingObject objectColider, IsoObject objectColidedWith) {
//    }
//
//    public void onPlayerUpdate(IsoPlayer player) {
//    }
//
//    public void onLoadMapZones() {
//    }
//
//    public void onCreateLivingCharacter(IsoLivingCharacter livingCharacter, SurvivorDesc descriptor) {
//    }
//
//    public void onClientCommand(String module, String command, IsoPlayer player, KahluaTable table) {
//    }
//
//    public void onDoTileBuilding2(KahluaTable drag, boolean render, int pickedTileX, int pickedTileY, int cameraZ,
//                                  IsoGridSquare square) {
//    }
//}