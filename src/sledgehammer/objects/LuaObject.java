package sledgehammer.objects;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import zombie.Lua.LuaManager;

/**
 * Abstract definitions class for KahluaTables.
 * @author Jab
 *
 */
public abstract class LuaObject {
	
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
