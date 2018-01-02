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
package sledgehammer.manager;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.PermissionListener;
import sledgehammer.lua.core.Player;

/**
 * Manager class designed to handle permissions for modules and core functions.
 * 
 * TODO: Implement.
 * 
 * TODO: Document.
 * 
 * @author Jab
 */
public class PermissionsManager extends Manager {

	public static final String NAME = "PermissionsManager";
	public static boolean DEBUG = false;
	private PermissionListener permissionListener;

	/**
	 * Main constructor.
	 */
	public PermissionsManager() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	public boolean hasRawPermission(Player player, String node) {
		// Validate the Player argument.
		if (player == null) {
			throw new IllegalArgumentException("Player given is null");
		}
		// Validate the node argument.
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		// Format the node.
		node = node.toLowerCase();
		// The flag to return.
		boolean returned = false;
		if (hasPermissionListener()) {
			PermissionListener permissionListener = getPermissionListener();
			try {
				returned = permissionListener.hasPermission(player, node);
				if (!returned) {
					returned = permissionListener.hasDefaultPermission(node);
				}
			} catch (Exception e) {
				errorln("The assigned PermissionListener failed to execute properly.");
				if (DEBUG) {
					e.printStackTrace();
				}
			}
		} else {
			throw new IllegalStateException(
					"No PermissionHandlers are registered for SledgeHammer, so permissions cannot be tested.");
		}
		// If no permissions handler identified as true, return false.
		return returned;
	}

	public void setRawPermission(Player player, String node, boolean flag) {
		// Validate the Player argument.
		if (player == null) {
			throw new IllegalArgumentException("Player given is null");
		}
		// Validate the node argument.
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		// Format the node.
		node = node.toLowerCase();
		if (hasPermissionListener()) {
			getPermissionListener().setPermission(player, node, flag);
		} else {
			throw new IllegalStateException(
					"No PermissionHandlers are registered for SledgeHammer, so permissions cannot be set.");
		}
	}

	public void setPermissionListener(PermissionListener handler) {
		this.permissionListener = handler;
	}

	public boolean hasPermissionListener() {
		return getPermissionListener() != null;
	}

	public PermissionListener getPermissionListener() {
		return this.permissionListener;
	}

	public String getPermissionDeniedMessage() {
		return SledgeHammer.instance.getSettings().getPermissionDeniedMessage();
	}

	public void addDefaultPlayerPermission(String node) {
		addDefaultPlayerPermission(node, true);
	}

	public void addDefaultPlayerPermission(String node, boolean flag) {
		getPermissionListener().addDefaultPermission(node, flag);
	}
}