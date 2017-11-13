package sledgehammer.database;

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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public abstract class MongoDocument {
	
	private DBCollection collection;
	private String fieldId;
	
	public MongoDocument(DBCollection collection, String fieldId) {
		setCollection(collection);
		setFieldId(fieldId);
	}
	
	public String getFieldId() {
		return this.fieldId;
	}
	
	private void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}
	
	private void setCollection(DBCollection collection) {
		this.collection = collection;
	}
	
	public DBCollection getCollection() {
		return this.collection;
	}
	
	/**
	 * Saves the <MongoDocument> with a given field to identify the document, if it
	 * already exists.
	 * 
	 * @param field
	 *            The <String> field used to identify the document, if it already
	 *            exists.
	 */
	public void save() {
		DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
		onSave(object);
		MongoDatabase.upsert(getCollection(), getFieldId(), object);
	}
	
	public void delete() {
		MongoDatabase.delete(getCollection(), getFieldId(), getFieldValue());
	}

	public abstract void onLoad(DBObject object);
	
	public abstract void onSave(DBObject object);
	
	public abstract Object getFieldValue();
}
