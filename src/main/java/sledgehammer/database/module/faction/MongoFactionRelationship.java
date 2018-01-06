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

public class MongoFactionRelationship extends MongoDocument {

    private UUID uniqueId;

    public MongoFactionRelationship(MongoCollection collection) {
        super(collection, "id");
    }

    @Override
    public void onLoad(DBObject object) {
        setUniqueId(object.get("id").toString());
    }

    @Override
    public void onSave(DBObject object) {

    }

    @Override
    public Object getFieldValue() {
        return getUniqueId().toString();
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    private void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    private void setUniqueId(String uniqueId) {
        setUniqueId(UUID.fromString(uniqueId));
    }

}