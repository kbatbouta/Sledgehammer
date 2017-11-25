package sledgehammer.manager;

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

import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.module.core.ModuleCore;
import sledgehammer.util.Printable;
import zombie.core.raknet.UdpConnection;

/**
 * Super-Class for all Managers. This class organizes and sets a standard for
 * Manager <-> SledgeHammer, to keep consistency. This class is designed to
 * handle any redundant methods required in multiple managers.
 * 
 * @author Jab
 *
 */
public abstract class Manager extends Printable {

	/**
	 * Returns a list of active connections.
	 * 
	 * @return
	 */
	public List<UdpConnection> getConnections() {
		return getSledgeHammer().getConnections();
	}
	
	public ModuleCore getCoreModule() {
		return getSledgeHammer().getModuleManager().getCoreModule();
	}
	
	public SledgeHammer getSledgeHammer() {
		return SledgeHammer.instance;
	}

	/**
	 * Executed when loading the module. Objects and Permission contexts (from
	 * listeners), should be defined here. It is HIGHLY recommended to use this
	 * area for declarations, as it is more organized and helps with handling
	 * unloading and reloading module-tier objects. This is not mandatory.
	 *
	 * @param debug Flag for debug testing.
	 */
	public abstract void onLoad(boolean debug);

	/**
	 * Executed when starting the module. Listeners should be registered here.
	 */
	public abstract void onStart();

	/**
	 * Executed during every update tick of the server. Update-sensitive code
	 * should use this method to execute. Event-based code should be handled
	 * through listeners.
	 */
	public abstract void onUpdate();

	/**
	 * Executed when unloading, and shutting down the module. It is HIGHLY
	 * recommended to nullify module-tier objects at this point. If the module
	 * is restarted, onLoad() is called again.
	 */
	public abstract void onShutDown();

}
