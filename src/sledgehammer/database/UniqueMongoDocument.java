package sledgehammer.database;

import java.util.UUID;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * A <MongoDocument> implementation that handles documents with a <UUID> unique
 * identifier.
 * 
 * @author Jab
 */
public abstract class UniqueMongoDocument extends MongoDocument {

	/** The identifier for the document. */
	private UUID uniqueId;

	/**
	 * New constructor.
	 * 
	 * @param collection
	 *            The <DBCollection> storing the document.
	 */
	public UniqueMongoDocument(DBCollection collection) {
		super(collection, "id");
		setUniqueId(UUID.randomUUID());
	}

	/**
	 * MongoDB constructor.
	 * 
	 * @param collection
	 *            The <DBCollection> storing the document.
	 * @param object
	 *            The <DBObject> storing the data.
	 */
	public UniqueMongoDocument(DBCollection collection, DBObject object) {
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
