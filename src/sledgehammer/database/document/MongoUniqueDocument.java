package sledgehammer.database.document;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;

/**
 * A <MongoDocument> implementation that handles documents with a <UUID> unique
 * identifier.
 * 
 * @author Jab
 */
public abstract class MongoUniqueDocument extends MongoDocument {

	/** The identifier for the document. */
	private UUID uniqueId;

	/**
	 * New constructor.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
	 */
	public MongoUniqueDocument(MongoCollection collection) {
		super(collection, "id");
		setUniqueId(UUID.randomUUID());
	}

	/**
	 * New constructor with provided ID.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
	 * @param uniqueId
	 *            The <UUID> being assigned.
	 */
	public MongoUniqueDocument(MongoCollection collection, UUID uniqueId) {
		super(collection, "id");
		DBObject query = new BasicDBObject("id", uniqueId.toString());
		DBCursor cursor = collection.find(query);
		if (cursor.hasNext()) {
			cursor.close();
			throw new IllegalArgumentException(
					"New Object in collection contains ID that is already in use: \"" + uniqueId.toString() + "\".");
		}
		cursor.close();
		setUniqueId(uniqueId);
	}

	/**
	 * MongoDB constructor.
	 * 
	 * @param collection
	 *            The <MongoCollection> storing the document.
	 * @param object
	 *            The <DBObject> storing the data.
	 */
	public MongoUniqueDocument(MongoCollection collection, DBObject object) {
		super(collection, "id");
		// Grab the ID from the object first before loading.
		setUniqueId(UUID.fromString(object.get("id").toString()));
	}

	/**
	 * @return Returns the <UUID> uniqueId that represents the document.
	 */
	public UUID getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the <UUID> uniqueId for the document.
	 * 
	 * @param uniqueId
	 *            The <UUID> that will represent the document.
	 */
	private void setUniqueId(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public Object getFieldValue() {
		return getUniqueId().toString();
	}
}
