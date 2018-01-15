/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.lua.faction;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.module.faction.MongoFactionRelationship;
import sledgehammer.lua.MongoLuaObject;

/**
 * MongoLuaObject that handles faction relationships data and operations for the
 * Factions Module.
 * <p>
 * TODO: Implement.
 *
 * @author Jab
 */
public class FactionRelationship extends MongoLuaObject<MongoFactionRelationship> {

    /**
     * Main constructor.
     *
     * @param mongoDocument The MongoDocument to set.
     */
    public FactionRelationship(MongoFactionRelationship mongoDocument) {
        super(mongoDocument, "FactionRelationship");
    }

    @Override
    public void onLoad(KahluaTable table) {
        // TODO: Implement
    }

    @Override
    public void onExport() {
        // TODO: Implement
    }
}