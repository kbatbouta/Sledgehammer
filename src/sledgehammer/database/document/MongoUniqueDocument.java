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
		setUniqueId(UUID.randomUUID(), false);
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
		setUniqueId(uniqueId, false);
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
		UUID uniqueId = null;
		Object oUniqueId = object.get("id");
		if(oUniqueId instanceof UUID) {
			uniqueId = (UUID) oUniqueId;
		} else if(oUniqueId instanceof String) {
			uniqueId = UUID.fromString(oUniqueId.toString());
		}
		setUniqueId(uniqueId, false);
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
	 * @param save 
	 */
	public void setUniqueId(UUID uniqueId, boolean save) {
		this.uniqueId = uniqueId;
		delete();
		if(save) {
			save();
		}
	}

	@Override
	public Object getFieldValue() {
		return getUniqueId();
	}
}
