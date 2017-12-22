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
package sledgehammer.lua.faction;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;
import sledgehammer.module.faction.ModuleFactions;

/**
 * Container class for Faction <ChatChannel> operations.
 *
 * @author Jab
 */
public class FactionChatChannel extends ChatChannel {

	/**
	 * Lua Load constructor.
	 * 
	 * @param table
	 */
	public FactionChatChannel(KahluaTable table) {
		super(table);
	}

	/**
	 * MongoDB Load constructor.
	 * 
	 * @param name
	 */
	public FactionChatChannel(String name) {
		super(name);
		setProperties(loadChannelProperties());
	}

	@Override
	public boolean canSee(Player player) {
		return super.canSee(player);
	}

	/**
	 * Main constructor.
	 * 
	 * @param name
	 *            The <String> name of the <ChatChannel>.
	 * @param description
	 *            The <String> description of the <ChatChannel>.
	 * @param context
	 *            The <String> context of the <ChatChannel>.
	 */
	public FactionChatChannel(String name, String description, String context) {
		super(name, description, context);
	}

	/**
	 * @return Returns the loaded <ModuleFactions> instance.
	 */
	public ModuleFactions getModule() {
		return (ModuleFactions) SledgeHammer.instance.getPluginManager().getModule(ModuleFactions.class);
	}
}