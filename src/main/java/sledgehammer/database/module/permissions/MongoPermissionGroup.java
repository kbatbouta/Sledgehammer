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
package sledgehammer.database.module.permissions;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoUniqueNodeDocument;

/**
 * MongoDocument designed to store and process data for PermissionGroup.
 *
 * @author Jab
 */
public class MongoPermissionGroup extends MongoUniqueNodeDocument {

    /**
     * The parent's Unique ID for the group.
     */
    private UUID parentId;
    /**
     * The String name of the group.
     */
    private String name;

    /**
     * MongoDB constructor.
     *
     * @param collection The MongoCollection storing the MongoDocument.
     * @param object     The DBObject storing the data.
     */
    public MongoPermissionGroup(MongoCollection collection, DBObject object) {
        super(collection, object);
        onLoad(object);
    }

    /**
     * New constructor.
     *
     * @param collection The MongoCollection storing the MongoDocument.
     * @param groupName  The String name of the group.
     */
    public MongoPermissionGroup(MongoCollection collection, String groupName) {
        super(collection);
        setGroupName(groupName, false);
    }

    @Override
    public void onLoad(DBObject object) {
        Object oParentId = object.get("parentId");
        if (oParentId != null) {
            setParentId(oParentId.toString());
        }
        setGroupName(object.get("name").toString(), false);
    }

    @Override
    public void onSave(DBObject object) {
        object.put("name", getGroupName());
        String parentIdAsString = null;
        UUID parentId = getParentId();
        if (parentId != null) {
            parentIdAsString = parentId.toString();
        }
        object.put("parentId", parentIdAsString);
    }

    /**
     * @return Returns the Unique ID of the Parent PermissonGroup. Returns null if
     * the group has no parent.
     */
    private UUID getParentId() {
        return this.parentId;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the parent Unique ID for the PermissionGroup.
     *
     * @param uniqueIdAsString The String representation of the Unique ID.
     */
    private void setParentId(String uniqueIdAsString) {
        setParentId(UUID.fromString(uniqueIdAsString), false);
    }

    /**
     * Sets the parent Unique ID for the PermissionGroup.
     *
     * @param parentId The Unique ID of the parent PermissionGroup.
     * @param save     Flag to save the document after changing the Unique ID.
     */
    public void setParentId(UUID parentId, boolean save) {
        this.parentId = parentId;
        if (save)
            save();
    }

    /**
     * @return Returns the group name of the PermissionGroup.
     */
    public String getGroupName() {
        return this.name;
    }

    /**
     * Sets the String name of the PermissionGroup.
     *
     * @param name The String name to set for the PermissionGroup.
     * @param save Flag to save the document after changing the name.
     */
    public void setGroupName(String name, boolean save) {
        this.name = name;
        if (save)
            save();
    }
}