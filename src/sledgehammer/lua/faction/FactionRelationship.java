package sledgehammer.lua.faction;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.module.faction.MongoFactionRelationship;
import sledgehammer.lua.LuaTable;

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

public class FactionRelationship extends LuaTable {

	private MongoFactionRelationship mongoFactionRelationship;

	public FactionRelationship(MongoFactionRelationship mongoFactionRelationship) {
		super("FactionRelationship");
		setMongoDocument(mongoFactionRelationship);
	}

	@Override
	public void onLoad(KahluaTable table) {
		//TODO: Implement
	}

	@Override
	public void onExport() {
		//TODO: Implement
	}

	public MongoFactionRelationship getMongoDocument() {
		return this.mongoFactionRelationship;
	}
	
	private void setMongoDocument(MongoFactionRelationship mongoFactionRelationship) {
		this.mongoFactionRelationship = mongoFactionRelationship;
	}
}
