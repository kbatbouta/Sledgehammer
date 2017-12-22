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
package sledgehammer.module.permissions;

import sledgehammer.interfaces.PermissionListener;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.permissions.PermissionGroup;
import sledgehammer.lua.permissions.PermissionUser;

/**
 * TODO: Implement.
 * 
 * TODO: Document.
 * 
 * @author Jab
 */
public class PermissionsListener implements PermissionListener {

	/** The <ModulePermissions> using the listener. */
	private ModulePermissions module;

	/**
	 * Main constructor.
	 * 
	 * @param module
	 *            The <ModulePermissions> using the listener.
	 */
	protected PermissionsListener(ModulePermissions module) {
		setModule(module);
	}

	@Override
	public boolean hasPermission(Player player, String node) {
		// Validate the Player argument.
		if (player == null) {
			throw new IllegalArgumentException("Player given is null.");
		}
		// Validate the node argument..
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		boolean granted = false;
		boolean wildcard = false;
		// If the node ends with a '*', this indicates the use of a wildcard operation.
		if (node.endsWith(".*")) {
			// Set the operation flag.
			wildcard = true;
			// Trim the node string to exclude the operator.
			node = node.substring(0, node.length() - 2);
		}
		// Check the default permissions first.
		PermissionGroup permissionGroupDefault = module.getDefaultPermissionGroup();
		granted = wildcard ? permissionGroupDefault.hasAnyPermission(node) : permissionGroupDefault.hasPermission(node);
		// If the player is assigned as a permission user, then override the default
		// flag.
		PermissionUser permissionUser = module.getPermissionUser(player);
		if (permissionUser != null) {
			granted = wildcard ? permissionUser.hasAnyPermission(node) : permissionUser.hasPermission(node);
		}
		return granted;
	}

	@Override
	public void setPermission(Player player, String node, boolean flag) {

	}

	/**
	 * @return Returns the <ModulePermissions> instance using the listener.
	 */
	public ModulePermissions getModule() {
		return this.module;
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the <ModulePermissions> instance using the listener.
	 * 
	 * @param module
	 *            The <ModulePermissions> instance to set.
	 */
	private void setModule(ModulePermissions module) {
		this.module = module;
	}

	@Override
	public void addDefaultPermission(String node, boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasDefaultPermission(String node) {
		// TODO Auto-generated method stub
		return false;
	}

}
