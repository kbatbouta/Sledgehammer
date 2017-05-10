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
	
	private boolean dirty = true;
	
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
		
		// Initialize the raw data Map.
		this.data = new HashMap<>();

		// Set the name of the LuaObject.
		setObjectName(name);
		
	}
	
	public LuaObject(String name, KahluaTable table) {
		
		// Set the name of the LuaObject.
		setObjectName(name);
		
		// Initialize the raw data Map.
		this.data = new HashMap<>();
		
		this.table = newTable();
		
		// Let the implementation decide what to do with the table.
		load(table);
	}

	/**
	 * Returns the name of the LuaObject.
	 * @return
	 */
	public String getObjectName() {
		return this.name;
	}
	
	public void setObjectName(String name) {
		
		if(this.name == null || !this.name.equals(name)) {
			this.name = name;
			set("__name", name);
		}
	}
	
	// Returns the raw table.
	public KahluaTable get() {
		
		// If the table hasn't been defined yet.
		if (!this.constructed) validate();
		
		return this.table;
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
	 * Initializes and returns a constructed KahluaTable definition of the LuaObject.
	 * @return
	 */
	public KahluaTable constructTable() {
		
		// Create a new table.
		KahluaTable table = newTable();
		
		// Create definitions map.
		Map<String, Object> definitions = new HashMap<>();
		
		// Grab implemented definitions to define.
		construct(definitions);
		
		for(String field : definitions.keySet()) {
			Object o = definitions.get(field);
			
			// If this is one of our own defined LuaObjects.
			if (o instanceof LuaObject) {
				// This allows recursive definitions to properly manifest.
				table.rawset(field, ((LuaObject)o).get());
			} else {
				
				// Generic Object definition.
				table.rawset(field, o);				
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
		
		if (this.data == null) validate();
		
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
	 * Returns whether or not the LuaObject is considered dirty (Data has changed without saving it).
	 * @return
	 */
	public boolean isDirty() {
		return this.dirty;
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
	
	public static KahluaTable newTable() {
		return LuaManager.platform.newTable();
	}
	
	/**
	 * Defines the LuaObject's structure.
	 * @return
	 */
	public abstract void construct(Map<String, Object> definitions);
	public abstract void load(KahluaTable table);
	
	@Override
	public String getName() {
		return getObjectName();
	}
}
