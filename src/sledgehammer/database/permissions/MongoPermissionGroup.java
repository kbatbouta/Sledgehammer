package sledgehammer.database.permissions;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.node.MongoUniqueNodeDocument;

/**
 * MongoDocument class designed to store and process data for <PermissionGroup>.
 * 
 * @author Jab
 */
public class MongoPermissionGroup extends MongoUniqueNodeDocument {

	/** The parent's unique ID for the group. */
	private UUID parentId;
	/** The <String> name of the group. */
	private String name;

	/**
	 * MongoDB constructor.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
	 * @param object
	 *            The <DBObject> storing the data.
	 */
	public MongoPermissionGroup(MongoCollection collection, DBObject object) {
		super(collection, object);
		onLoad(object);
	}

	/**
	 * New constructor.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
	 * @param groupName
	 *            The <String> name of the group.
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
	 * @return Returns the <UUID> of the Parent <PermissonGroup>. Returns null if
	 *         the group has no parent.
	 */
	private UUID getParentId() {
		return this.parentId;
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the <UUID> parentId of the <PermissionGroup>.
	 * 
	 * @param uniqueIdAsString
	 *            The <String> representation of the <UUID>.
	 */
	private void setParentId(String uniqueIdAsString) {
		setParentId(UUID.fromString(uniqueIdAsString), false);
	}

	/**
	 * Sets the <UUID> parentId of the <PermissionGroup>.
	 * 
	 * @param uniqueIdAsString
	 *            The <UUID> identifier of the parent <PermissionGroup>.
	 * @param save
	 *            Flag to save the document after changing the id.
	 */
	public void setParentId(UUID parentId, boolean save) {
		this.parentId = parentId;
		if (save)
			save();
	}

	/**
	 * @return Returns the group name of the <PermissionGroup>.
	 */
	public String getGroupName() {
		return this.name;
	}

	/**
	 * Sets the <String> name of the <PermissionGroup>.
	 * 
	 * @param name
	 *            The <String> name to set for the <PermissionGroup>.
	 * @param save
	 *            Flag to save the document after changing the name.
	 */
	public void setGroupName(String name, boolean save) {
		this.name = name;
		if (save)
			save();
	}
}