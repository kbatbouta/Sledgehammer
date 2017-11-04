package sledgehammer.database;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public abstract class MongoDocument {
	
	private DBCollection collection;
	
	public MongoDocument(DBCollection collection) {
		setCollection(collection);
	}
	
	private void setCollection(DBCollection collection) {
		this.collection = collection;
	}
	
	public DBCollection getCollection() {
		return this.collection;
	}
	
	/*
	 * Returns a set-overwrite <BasicDBObject> for a MongoDB Object.
	 * 
	 * @param object
	 * @return
	 */
	/*public static DBObject set(DBObject object) {
		return new BasicDBObject("$set", object);
	}*/
	
	public abstract void load(DBObject object);
	
	public abstract void save();
}
