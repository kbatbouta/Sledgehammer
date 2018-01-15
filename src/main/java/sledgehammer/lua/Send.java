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

package sledgehammer.lua;

import se.krka.kahlua.vm.KahluaTable;

/**
 * LuaTable sub-class to identify the String Client ID of the Module being
 * interfaced and the String command to identify the sub-routine to interface.
 *
 * @author Jab
 */
public abstract class Send extends LuaTable {

    /**
     * The String Client ID of the Module.
     */
    private String module;

    /**
     * Main constructor.
     *
     * @param module  The String Client ID of the Module.
     * @param command The String command to identify the sub-routine to interface.
     */
    public Send(String module, String command) {
        super(command);
        setModule(module);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": Module=" + getModule() + "; Command=" + getCommand() + ";";
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String Client ID of the Module.
     *
     * @param module The String module to set.
     */
    private void setModule(String module) {
        this.module = module;
    }

    /**
     * @return Returns the String Client ID of the Module.
     */
    public String getModule() {
        return this.module;
    }

    /**
     * @return Returns the String command to identify the sub-routine to
     * interface.
     */
    public String getCommand() {
        return getName();
    }

    /**
     * Loads a sent LuaObject as a KahluaTable.
     * <p>
     * (Note: Server authored only)
     */
    public void onLoad(KahluaTable table) {
    }
}