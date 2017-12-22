package sledgehammer.lua;

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

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.util.Printable;
import zombie.Lua.LuaManager;

/**
 * TODO: Document.
 * 
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
	 * 
	 * @param value
	 * @return
	 */
	public static Object processValue(Object value) {
		Object result = value;

		if (value instanceof Number) {
			result = ((Number) value).doubleValue();
		} else if (value instanceof LuaObject) {
			result = ((LuaObject) value).export();
		}

		if (DEBUG && VERBOSE) {
			if (value == null) {
				System.out.println("LuaObject: Processed Value is null.");
			} else {
				System.out.println("LuaObject: Processed Result of \'" + value.getClass() + "\' = " + value + ": \'"
						+ result.getClass() + "\' = " + result);
			}
		}

		return result;
	}

}
