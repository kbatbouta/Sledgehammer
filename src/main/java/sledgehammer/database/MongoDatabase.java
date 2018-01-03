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
package sledgehammer.database;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import sledgehammer.database.transaction.MongoDatabaseTransactionWorker;
import sledgehammer.database.transaction.MongoDocumentTransaction;
import sledgehammer.util.Printable;

/**
 * TODO: Document
 * 
 * @author Jab
 */
public abstract class MongoDatabase extends Printable {

	public static boolean DEBUG = true;

	private volatile boolean shutdown = false;

	private DB db;
	private MongoClient client = null;
	private List<MongoDocumentTransaction> listTransactions;

	private MongoDatabaseTransactionWorker worker;

	public MongoDatabase() {
		listTransactions = new ArrayList<>();
		worker = new MongoDatabaseTransactionWorker(this);
	}

	public DB getDatabase() {
		return this.db;
	}

	public void setDatabase(DB db) {
		this.db = db;
	}

	public void connect(String url) {
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		if (client == null) {
			client = new MongoClient(new MongoClientURI(url));
			onConnection(client);
			// Start the worker thread.
			(new Thread(worker)).start();
		}
	}

	public MongoClient getClient() {
		return this.client;
	}

	public void addTransaction(final MongoDocumentTransaction transaction) {
		// Run this operation outside of the main thread.
		(new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (listTransactions) {
					listTransactions.add(transaction);
				}
			}
		})).start();
	}

	public int getTransactionQueueSize() {
		synchronized (listTransactions) {
			return listTransactions.size();
		}
	}

	public MongoCollection createMongoCollection(String name) {
		return new MongoCollection(this, getDatabase().getCollection(name));
	}

	public void shutDown() {
		reset();
		setShutDown(true);
	}

	public boolean isShutDown() {
		return this.shutdown;
	}

	private void setShutDown(boolean flag) {
		this.shutdown = flag;
	}

	public List<MongoDocumentTransaction> getTransactions() {
		return this.listTransactions;
	}

	public abstract void reset();

	public abstract void onConnection(MongoClient client);
}