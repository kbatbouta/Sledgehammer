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
package sledgehammer.database.transaction;

import com.mongodb.BasicDBObject;

import sledgehammer.database.MongoCollection;

public class MongoDocumentTransactionDelete extends MongoDocumentTransaction {

    private String field;
    private Object value;

    public MongoDocumentTransactionDelete(MongoCollection collection, String field, Object value) {
        super(collection);
        setField(field);
        setValue(value);
    }

    @Override
    public void run() {
        String field = getField();
        Object value = getValue();
        getMongoCollection().getDBCollection().remove(new BasicDBObject(field, value));
    }

    public String getField() {
        return this.field;
    }

    private void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return this.value;
    }

    private void setValue(Object value) {
        this.value = value;
    }

}
