package sledgehammer.database.permissions;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.node.MongoUniqueNodeDocument;

/**
 * MongoDocument class to handle loading and storing data for <PermissionUser>.
 * 
 * @author Jab
 */
public class MongoPermissionUser extends MongoUniqueNodeDocument {

	/** The <UUID> of the group the user is in. Null if no group is assigned. */
	private UUID groupId;

	/**
	 * MongoDB constructor.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
	 * @param object
	 *            The <DBObject> storing the data.
	 */
	public MongoPermissionUser(MongoCollection collection, DBObject object) {
		super(collection, object);
		onLoad(object);
	}

	/**
	 * New constructor.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
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
	 * @return Returns the <UUID> identifier of the <PermissionGroup> that the user
	 *         is assigned to. Returns null if the user is not assigned to a group.
	 */
	public UUID getGroupId() {
		return this.groupId;
	}

	/**
	 * Sets the <UUID> group identifier for the <PermissionUser>
	 * 
	 * @param groupId
	 *            The <UUID> of the <PermissionGroup> being assigned to the user.
	 * @param save
	 *            Flag to save the document after setting the group <UUID>.
	 */
	public void setGroupId(UUID groupId, boolean save) {
		this.groupId = groupId;
		if (save)
			save();
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the <UUID> group identifier for the <Permissionuser>
	 * 
	 * @param groupIdAsString
	 *            The <String> version of the <UUID> to set.
	 */
	private void setGroupId(String groupIdAsString) {
		setGroupId(UUID.fromString(groupIdAsString), false);
	}
}