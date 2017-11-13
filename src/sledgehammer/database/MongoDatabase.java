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
