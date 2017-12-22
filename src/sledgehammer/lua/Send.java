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

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public abstract class Send extends LuaTable {

	private String module;

	public Send(String module, String command) {
		super(command);
		setModule(module);
	}

	private void setModule(String module) {
		this.module = module;
	}

	public String getModule() {
		return this.module;
	}

	public String getCommand() {
		return getName();
	}

	public String toString() {
		return this.getClass().getSimpleName() + ": Module=" + getModule() + "; Command=" + getCommand() + ";";
	}

	// Server authored only.
	public void onLoad(KahluaTable table) {
	}
}
