package sledgehammer.lua.faction;

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

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.faction.MongoFactionMember;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;
import sledgehammer.module.faction.ModuleFactions;

public class FactionMember extends LuaTable {

	private MongoFactionMember mongoFactionMember;
	
	private Faction faction;
	
	public FactionMember(MongoFactionMember mongoFactionMember) {
		super("FactionMember");
		setMongoDocument(mongoFactionMember);
	}
	
	@Override
	public void onLoad(KahluaTable table) {
		//TODO: implement.
	}

	@Override
	public void onExport() {
		//TODO: implement.
	}
	
	public MongoFactionMember getMongoDocument() {
		return this.mongoFactionMember;
	}
	
	private void setMongoDocument(MongoFactionMember mongoFactionMember) {
		this.mongoFactionMember = mongoFactionMember;
	}

	public UUID getPlayerId() {
		return getMongoDocument().getPlayerId();
	}

	public UUID getFactionId() {
		return getMongoDocument().getFactionId();
	}
	
	public Faction getFaction() {
		return this.faction;
	}

	/**
	 * Sets the new Faction.
	 * @param faction
	 * @param save
	 */
	public void setFaction(Faction faction, boolean save) {
		boolean success = false;
		Faction factionOld = getFaction();
		if(factionOld != null) {
			factionOld.removeMember(this);
			success = true;
		} else {
			success = true;
		}
		if(success) {			
			if(faction != null) {
				success = faction.addMember(this);
				if(success) {			
					this.faction = faction;
					getMongoDocument().setFactionId(faction.getUniqueId(), save);
					Player player = SledgeHammer.instance.getPlayer(getPlayerId());
					if(player != null) {
						player.setNickname("[" + faction.getFactionTag() + "] " + player.getUsername());
						player.setColor(Color.getColor(faction.getFactionRawColor()));
					}
				}
			} else {
				Player player = SledgeHammer.instance.getPlayer(getPlayerId());
				if(player != null) {
					player.setNickname(null);
					player.setColor(Color.WHITE);
				}
			}
		}
	}

	/**
	 * Removes the <FactionMember> from the <Faction>.
	 */
	public void leaveFaction() {
		ModuleFactions moduleFactions = (ModuleFactions) SledgeHammer.instance.getPluginManager().getModule(ModuleFactions.class);
		moduleFactions.removeFactionMember(this);
	}

}
