package sledgehammer.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;

import sledgehammer.util.Printable;

public abstract class MongoDatabase extends Printable {

	private DB db;
	private MongoClient client = null;
	
	public MongoDatabase() {
	}
	
	public DB getDatabase() {
		return this.db;
	}
	
	public void setDatabase(DB db) {
		this.db = db;
	}
	
	public void connect(String url) {
		if(client == null) {
			client = new MongoClient(new MongoClientURI(url));
			onConnection(client);
		}
	}

	public MongoClient getClient() {
		return this.client;
	}
	
	/**
	 * Adds a proxy method for upserting into a MongoDB instance.
	 * 
	 * @param collection
	 * @param field
	 * @param identity
	 * @param object
	 * @return
	 */
	public static void upsert(final DBCollection collection, final String field, final DBObject object) {
		(new Thread(new Runnable() {			
			@Override
			public void run() {
				BasicDBObject append = new BasicDBObject();				
				append.append("$set", object);
				collection.update(new BasicDBObject(field, object.get(field)), append, true, false);
			}
		})).start();
	}
	
	public static WriteResult delete(DBCollection collection, String field, Object value) {
		return collection.remove(new BasicDBObject(field, value));
	}
	
	public abstract void reset();
	public abstract void onConnection(MongoClient client);
}
