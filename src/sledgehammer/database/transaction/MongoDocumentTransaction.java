package sledgehammer.database.transaction;

import sledgehammer.database.MongoCollection;

public abstract class MongoDocumentTransaction {

	private MongoCollection mongoCollection;

	public MongoDocumentTransaction(MongoCollection mongoCollection) {
		setMongoCollection(mongoCollection);
	}

	public MongoCollection getMongoCollection() {
		return this.mongoCollection;
	}

	private void setMongoCollection(MongoCollection mongoCollection) {
		this.mongoCollection = mongoCollection;
	}

	public abstract void run();
}
