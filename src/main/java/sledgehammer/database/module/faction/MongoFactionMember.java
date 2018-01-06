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
package sledgehammer.database.module.faction;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoDocument;

public class MongoFactionMember extends MongoDocument {

    private UUID playerId;
    private UUID factionId;

    public MongoFactionMember(MongoCollection collection, DBObject object) {
        super(collection, "playerId");
        onLoad(object);
    }

    public MongoFactionMember(MongoCollection collection, UUID playerId, UUID factionId) {
        super(collection, "playerId");
        setPlayerId(playerId);
        setFactionId(factionId, false);
    }

    @Override
    public void onLoad(DBObject object) {
        setPlayerId(object.get("playerId").toString());
        setFactionId(object.get("factionId").toString(), false);
    }

    @Override
    public void onSave(DBObject object) {
        object.put("factionId", getFactionId().toString());
    }

    @Override
    public Object getFieldValue() {
        return getPlayerId().toString();
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    private void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    private void setPlayerId(String playerId) {
        setPlayerId(UUID.fromString(playerId));
    }

    public UUID getFactionId() {
        return this.factionId;
    }

    public void setFactionId(UUID factionId, boolean save) {
        this.factionId = factionId;
        if (save)
            save();
    }

    public void setFactionId(String factionId, boolean save) {
        setFactionId(UUID.fromString(factionId), save);
    }

}