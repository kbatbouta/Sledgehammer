/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.database.transaction;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.database.MongoDatabase;

/**
 * Runnable interface that handles ordered transactions for MongoDocuments.
 *
 * @author Jab
 */
public class MongoDatabaseTransactionWorker implements Runnable {

    /**
     * The amount of time to sleep when idle.
     */
    private long sleepTimer = 10L;
    /**
     * The MongoDatabase using the worker to execute transactions.
     */
    private MongoDatabase database;

    /**
     * Main constructor.
     *
     * @param database The MongoDatabase containing MongoDocumentTransactions.
     */
    public MongoDatabaseTransactionWorker(MongoDatabase database) {
        // Set the database using the worker.
        setMongoDatabase(database);
    }

    @Override
    public void run() {
        // The database to work with.
        MongoDatabase database = getMongoDatabase();
        // The list to work with outside of the main list from the database.
        List<MongoDocumentTransaction> listToTransact = new ArrayList<>();
        // Loop through until the database is flagged to shut down. If there are queued
        // transactions waiting to be processed during a shutdown, those will be ran
        // first before exiting the loop.
        while (!database.isShutDown() || (database.isShutDown() && database.getTransactionQueueSize() > 0)) {
            // Clear the local list.
            listToTransact.clear();
            // Grab the current list of transactions.
            List<MongoDocumentTransaction> listTransactions = database.getTransactions();
            // Ensure that no transactions are added during the transfer of transactions to
            // the local list.
            synchronized (listTransactions) {
                // Add every transaction currently on the database list to the local list.
                listToTransact.addAll(listTransactions);
                // Clear the database list.
                listTransactions.clear();
            }
            // Make sure we have transactions to process.
            if (listToTransact.size() > 0) {
                // Go through each transaction.
                for (MongoDocumentTransaction transaction : listToTransact) {
                    // Dispatch the transaction method to handle the operation.
                    transaction.run();
                }
            }
            // Sleep to ensure not to start process resources.
            try {
                Thread.sleep(sleepTimer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Returns the MongoDatabase using the worker.
     */
    public MongoDatabase getMongoDatabase() {
        return this.database;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the MongoDatabase using the worker.
     *
     * @param database The MongoDatabase to set.
     */
    private void setMongoDatabase(MongoDatabase database) {
        this.database = database;
    }
}