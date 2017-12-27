package sledgehammer.database.module.chat;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoUniqueDocument;

public class MongoChatBroadcast extends MongoUniqueDocument {

	public MongoChatBroadcast(MongoCollection collection) {
		super(collection);
	}

	@Override
	public void onLoad(DBObject object) {
		
	}

	@Override
	public void onSave(DBObject object) {
		
	}

}
