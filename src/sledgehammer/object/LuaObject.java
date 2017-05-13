package sledgehammer.object;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.util.Printable;
import zombie.Lua.LuaManager;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public abstract class LuaObject extends Printable {
	
	public static boolean DEBUG = false;
	public static boolean VERBOSE = false;
	
	private String name;
	
	public LuaObject(String name) {
		this.name = name;
	}
	
	public abstract KahluaTable export();
	
	public String getName() {
		return this.name;
	}
	
	public static KahluaTable newTable() {
		return LuaManager.platform.newTable();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Validates a value for Lua.
	 * @param value
	 * @return
	 */
	public static Object processValue(Object value) {
		Object result = value;
		
		if (value instanceof Number) {
			result = ((Number)value).doubleValue();
		} else if(value instanceof LuaObject) {
			result = ((LuaObject)value).export();
		}
		
		if (DEBUG && VERBOSE) {
			if(value == null) {
				System.out.println("LuaObject: Processed Value is null.");
			} else {				
				System.out.println("LuaObject: Processed Result of \'" 
						+ value.getClass()  + "\' = " + value + ": \'" 
						+ result.getClass() + "\' = " + result);		
			}
		}
		
		return result;
	}

}
