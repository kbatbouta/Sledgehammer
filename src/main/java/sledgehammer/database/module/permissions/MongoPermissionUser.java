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

package sledgehammer.database.module.permissions;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoUniqueNodeDocument;

/**
 * MongoDocument class to handle loading and storing data for PermissionUser.
 *
 * @author Jab
 */
public class MongoPermissionUser extends MongoUniqueNodeDocument {

  /** The Unique ID of the group the user is in. Null if no group is assigned. */
  private UUID groupId;

  /**
   * MongoDB constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param object The DBObject storing the data.
   */
  public MongoPermissionUser(MongoCollection collection, DBObject object) {
    super(collection, object);
    onLoad(object);
  }

  /**
   * New constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param playerId the Unique ID of the Player.
   */
  public MongoPermissionUser(MongoCollection collection, UUID playerId) {
    super(collection, playerId);
  }

  @Override
  public void onLoad(DBObject object) {
    Object oGroupId = object.get("groupId");
    if (oGroupId != null) {
      setGroupId(oGroupId.toString());
    }
  }

  @Override
  public void onSave(DBObject object) {
    String groupId = null;
    if (getGroupId() != null) {
      groupId = getGroupId().toString();
    }
    object.put("groupId", groupId);
  }

  /**
   * @return Returns the Unique ID for the PermissionGroup that the user is assigned to. Returns
   *     null if the user is not assigned to a group.
   */
  public UUID getGroupId() {
    return this.groupId;
  }

  /**
   * Sets the PermissionGroup Unique ID for the PermissionUser.
   *
   * @param groupId The Unique ID of the PermissionGroup being assigned to the user.
   * @param save Flag to save the document.
   */
  public void setGroupId(UUID groupId, boolean save) {
    this.groupId = groupId;
    if (save) save();
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Unique ID for the PermissionUser.
   *
   * @param groupIdAsString The String version of the Unique ID to set.
   */
  private void setGroupId(String groupIdAsString) {
    setGroupId(UUID.fromString(groupIdAsString), false);
  }
}
