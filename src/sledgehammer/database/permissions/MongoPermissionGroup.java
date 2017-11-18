package sledgehammer.database.permissions;

import java.util.UUID;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import sledgehammer.database.UniqueMongoNodeDocument;

/**
 * MongoDocument class designed to store and process data for <PermissionGroup>.
 * 
 * @author Jab
 */
public class MongoPermissionGroup extends UniqueMongoNodeDocument {

	/** The parent's unique ID for the group. */
	private UUID parentId;
	/** The <String> name of the group. */
	private String name;

	/**
	 * MongoDB constructor.
	 * 
	 * @param collection
	 *            The <DBCollection> storing the document.
	 * @param object
	 *            The <DBObject> storing the data.
	 */
	public MongoPermissionGroup(DBCollection collection, DBObject object) {
		super(collection, object);
		onLoad(object);
	}

	/**
	 * New constructor.
	 * 
	 * @param collection
	 *            The <DBCOllection> storing the document.
	 */
	public MongoPermissionGroup(DBCollection collection) {
		super(collection);
	}

	@Override
	public void onLoad(DBObject object) {
		setParentId(object.get("parentId").toString());
		setGroupName(object.get("name").toString(), false);
		loadNodes(object);
	}

	@Override
	public void onSave(DBObject object) {
		object.put("name", getGroupName());
		object.put("parentId", getParentId().toString());
		saveNodes(object);
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
		if (save) save();
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
		if (save) save();
	}
}