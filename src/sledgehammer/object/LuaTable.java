package sledgehammer.object;

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

import java.util.HashMap;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

/**
 * Abstract definitions class for KahluaTables.
 * TODO: document.
 * @author Jab
 *
 */
public abstract class LuaTable extends LuaObject {
	
	public static final byte COPY_OVERWRITE  = (byte) 0;
	public static final byte COPY_UNDERWRITE = (byte) 1;
	
	private boolean dirty = true;
	
	// private KahluaTable lastTable;
	
	/**
	 * Non-converted data
	 */
	private Map<Object, Object> data;
	
	/**
	 * Main constructor.
	 * @param name Name of the LuaObject.
	 */
	public LuaTable(String name) {
		super(name);
		
		// Initialize the raw data Map.
		this.data = new HashMap<>();
	}
	
	public LuaTable(String name, KahluaTable table) {
		super(name);
		
		// Initialize the raw data Map.
		this.data = new HashMap<>();
		
		// Let the implementation decide what to do with the table.
		onLoad(table);
	}

	public KahluaTable export() {

		KahluaTable outTable;
		
		// Call 'onExport()' to make sure the data is accurate.
		onExport();
		
		if(DEBUG) {			
			println("Exporting LuaTable: " + getName());
		}
		
		outTable = newTable();
		
		for (Object key : this.data.keySet()) {
			Object value = this.data.get(key);
			
			if(DEBUG) {				
				println("Exporting Key: " + key + ", Value:" + value);
			}
			
			value = processValue(value);
			if(value != null) {				
				outTable.rawset(key, value);
			} else {
				println("Processed value for key '" + key + "' is null.");
			}
		}
		
		// Mark clean as last table is assigned for any further exports
		// without changes.
		markClean();
		
		// Set the name of the object.
		outTable.rawset("__name", getName());
		
		// Return the table.
		return outTable;
	}
	
	/**
	 * Copies data from another LuaObject
	 * @param other
	 * @param flag
	 */
	public void copy(LuaTable other, byte flag) {
		
		// Check to make sure that LuaObjects are the same.
		if (getName() != other.getName()) {
			errorln("LuaObject Class cannot be copied, because given class is different:"
					+ "(LuaObject: \"" + getName() + "\", Given: \"" + other.getName() + "\").");
			return;
		}
		
		// Grab the data from the other LuaObject.
		Map<Object, Object> dataOther = other.getData();
		
		// Grab the data from this LuaObject.
		Map<Object, Object> data = getData();
		
		// Go through each field in the other LuaObject.
		for(Object field : dataOther.keySet()) {
			if (flag == COPY_OVERWRITE) {				
				data.put(field, dataOther.get(field));
			} else
			if (flag == COPY_UNDERWRITE) {
				if(data.get(field) == null) {
					data.put(field, dataOther.get(field));
				}
			}
		}
	}
	
	/**
	 * Sets a field's data.
	 * @param field
	 * @param value
	 */
	public void set(String field, Object value) {
		
		// All Lua values must be Doubles.
		if (value instanceof Number) {
			
			// Set the double version of the value;
			this.data.put(field, ((Number)value).doubleValue());
		} else {
			
			// Set the raw data.
			this.data.put(field, value);
		}
	}
	
	/**
	 * Sets the value at a given index.
	 * @param index
	 * @param value
	 */
	public void set(int index, Object value) {

		// All Lua values must be Doubles.
		if (value instanceof Number) {

			// Set the raw data.
			this.data.put(index, ((Number)value).doubleValue());
			
		} else {
			
			// Set the raw data.
			this.data.put(index, value);			
		}
		this.markDirty();
	}
	
	/**
	 * Creates a shallow copy of a KahluaTable instance.
	 * @param other
	 * @return
	 */
	public static KahluaTable copyTable(KahluaTable other) {
		// Create a new table.
		KahluaTable table = newTable();
		
		// Set the metatable from the other metatable.
		table.setMetatable(other.getMetatable());

		// Grab the length of the table to copy.
		int length = other.len();
		
		// Go through each field entry.
		for(int index = 0; index < length; index++) {
			
			// Set the data.
			table.rawset(index, other.rawget(index));
		}
		
		// Return the copied table.
		return table;
	}
	
	/**
	 * Wipes the data map associated used to export the LuaTable.
	 */
	public void wipe() {
		// Initialize the raw data Map.
		data = new HashMap<>();
	}
	
	/**
	 * Returns a field's data.
	 * @param field
	 * @return
	 */
	public Object get(Object key) {
		return this.data.get(key);
	}
	
	/**
	 * Marks the LuaObject dirty. This method is for save purposes.
	 */
	public void markDirty() {
		dirty = true;
	}
	
	/**
	 * Marks the LuaObject as clean. This method is for save purposes.
	 */
	public void markClean() {
		dirty = false;
	}
	
	/**
	 * Returns the raw dataset for the LuaObject.
	 * @return
	 */
	public Map<Object, Object> getData() {
		return this.data;
	}
	
	/**
	 * Returns whether or not the LuaTable is considered dirty (Data has changed without saving it).
	 * @return
	 */
	public boolean isDirty() {
		return this.dirty;
	}
	
	/**
	 * returns the size of the table.
	 * @return
	 */
	public int getSize() {
		return data.size();
	}
	
	public String getString(KahluaTable table, String key) {
		return (String) table.rawget(key);
	}
	
	public abstract void onLoad(KahluaTable table);
	public abstract void onExport();
}