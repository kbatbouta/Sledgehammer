package sledgehammer.database.transaction;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.MongoDatabase;

public class MongoDocumentTransactionUpsert extends MongoDocumentTransaction {

	private DBObject object;
	private String field;
	private Object lock;

	public MongoDocumentTransactionUpsert(MongoCollection collection, DBObject object, String field, Object lock) {
		super(collection);
		setObject(object);
		setField(field);
		setLock(lock);
	}

	@Override
	public void run() {
		String field = getField();
		DBObject object = getObject();
		BasicDBObject append = new BasicDBObject();
		append.append("$set", object);
		getMongoCollection().getDBCollection().update(new BasicDBObject(field, object.get(field)), append, true, false);
	}

	public Object getLock() {
		return this.lock;
	}

	private void setLock(Object lock) {
		this.lock = lock;
	}

	public DBObject getObject() {
		return this.object;
	}

	private void setObject(DBObject object) {
		this.object = object;
	}

	public String getField() {
		return this.field;
	}

	private void setField(String field) {
		this.field = field;
	}
}