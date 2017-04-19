package sledgehammer.objects;

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
		
		// Create the native table.
		this.table = constructTable();
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
	
	public static final byte COPY_OVERWRITE = (byte) 0;
	public static final byte COPY_UNDERWRITE = (byte) 1;
	
	/**
	 * Copies data from another LuaObject
	 * @param other
	 * @param flag
	 */
	public void copy(LuaObject other, byte flag) {
		
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
	
	/**
	 * Sets a field's data.
	 * @param field
	 * @param object
	 */
	public void set(String field, Object object) {
		this.data.put(field, object);
		this.table.rawset(field, object);
	}
	
	/**
	 * Returns a field's data.
	 * @param field
	 * @return
	 */
	public Object get(String field) {
		return this.table.rawget(field);
	}
	
	/**
	 * Returns the raw dataset for the LuaObject.
	 * @return
	 */
	public Map<String, Object> getData() {
		return this.data;
	}
	
	/**
	 * Defines the LuaObject's structure.
	 * @return
	 */
	public abstract Map<String, Object> getDefinitions();
}
