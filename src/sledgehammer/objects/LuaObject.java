package sledgehammer.objects;

import java.util.HashMap;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.util.Printable;
import zombie.Lua.LuaManager;

/**
 * Abstract definitions class for KahluaTables.
 * @author Jab
 *
 */
public abstract class LuaObject extends Printable {
	
	public static final byte COPY_OVERWRITE  = (byte) 0;
	public static final byte COPY_UNDERWRITE = (byte) 1;
	
	private boolean constructed = false;
	
	/**
	 * The name of the LuaObject.
	 */
	private String name;
	
	/**
	 * The native KahluaTable instance for the object.
	 */
	private KahluaTable table;
	
	/**
	 * Non-converted data
	 */
	private Map<String, Object> data;
	
	/**
	 * Main constructor.
	 * @param name Name of the LuaObject.
	 */
	public LuaObject(String name) {
		
		// Set the name of the LuaObject.
		this.name = name;
		
		// Initialize the raw data Map.
		data = new HashMap<>();
	}
	
	/**
	 * Returns the name of the LuaObject.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	// Returns the raw table.
	public KahluaTable get() {
		
		// If the table hasn't been defined yet.
		if (!this.constructed) validate();
		
		return this.table;
	}
	
	/**
	 * Initializes and returns a constructed KahluaTable definition of the LuaObject.
	 * @return
	 */
	public KahluaTable constructTable() {
		
		// Create a new table.
		KahluaTable table = LuaManager.platform.newTable();
		
		// Grab implemented definitions to define.
		Map<String, Object> definitions = getDefinitions();
		
		for(String field : definitions.keySet()) {
			Object o = definitions.get(field);
			
			// If this is one of our own defined LuaObjects.
			if (o instanceof LuaObject) {
				// This allows recursive definitions to properly manifest.
				this.table.rawset(field, ((LuaObject)o).get());
			} else {
				
				// Generic Object definition.
				this.table.rawset(field, o);				
			}
		}
		
		return table;
	}
	
	/**
	 * Copies data from another LuaObject
	 * @param other
	 * @param flag
	 */
	public void copy(LuaObject other, byte flag) {
		
		// If the table hasn't been defined yet.
		if (!this.constructed) validate();
		
		// Check to make sure that LuaObjects are the same.
		if (getName() != other.getName()) {
			errorln("LuaObject Class cannot be copied, because given class is different:"
					+ "(LuaObject: \"" + getName() + "\", Given: \"" + other.getName() + "\").");
			return;
		}
		
		// Grab the data from the other LuaObject.
		Map<String, Object> dataOther = other.getData();
		
		// Grab the data from this LuaObject.
		Map<String, Object> data = getData();
		
		// Go through each field in the other LuaObject.
		for(String field : dataOther.keySet()) {
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
	
	public void reset() {
		// Initialize the raw data Map.
		data = new HashMap<>();
		
		// Revalidates the table.
		validate();
	}
	
	/**
	 * Sets a field's data.
	 * @param field
	 * @param object
	 */
	public void set(String field, Object object) {
		
		// If the table hasn't been defined yet.
		if (!this.constructed) validate();
		
		// Set the raw data.
		this.data.put(field, object);
		
		// Set the Lua data.
		this.table.rawset(field, object);
	}
	
	/**
	 * Returns a field's data.
	 * @param field
	 * @return
	 */
	public Object get(String field) {
		
		// If the table hasn't been defined yet.
		if (!this.constructed) validate();
		
		return this.table.rawget(field);
	}
	
	/**
	 * Returns the raw dataset for the LuaObject.
	 * @return
	 */
	public Map<String, Object> getData() {
		
		// If the table hasn't been defined yet.
		if (!this.constructed) validate();
		
		return this.data;
	}
	
	/** 
	 * Validates table construction.
	 */
	public void validate() {
		// If the table hasn't been defined yet.
		if (!this.constructed) {
			
			// Create the table.
			this.table = constructTable();
			
			// Set constructed flag to true to let the object know the table is defined.
			this.constructed = true;
		}
	}
	
	/**
	 * Creates a shallow copy of a KahluaTable instance.
	 * @param other
	 * @return
	 */
	public static KahluaTable copyTable(KahluaTable other) {
		// Create a new table.
		KahluaTable table = LuaManager.platform.newTable();
		
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
	 * Defines the LuaObject's structure.
	 * @return
	 */
	public abstract Map<String, Object> getDefinitions();
}
