package sledgehammer.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.interfaces.PermissionsHandler;
import sledgehammer.util.Printable;
import zombie.core.Core;
import zombie.network.DataBaseBuffer;

/**
 * Manager class designed to handle permissions for modules and core functions.
 * 
 * @author Jab
 *
 */
public class PermissionsManager extends Manager {
	
	public static final String NAME = "PermissionsManager";
	
	/**
	 * Debug boolean, used for verbose output.
	 */
	public static boolean DEBUG = false;

	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer = null;
	
	/**
	 * List of registered PermissionHandler interfaces.
	 */
	private List<PermissionsHandler> listPermissionHandlers;
	
	/**
	 * Map of registered default permissions.
	 */
	private Map<String, Boolean> mapDefaultPlayerPermissions;
	
	/**
	 * Main constructor.
	 * 
	 * @param instance
	 */
	public PermissionsManager(SledgeHammer instance) {
		sledgeHammer = instance;
		listPermissionHandlers = new ArrayList<>();
		mapDefaultPlayerPermissions = new HashMap<>();
		
	}

	/**
   	 * Returns whether or not the vanilla white-list has a user set to admin.
   	 * 
   	 * @param username
   	 * 
   	 * @return
   	 * 
   	 * @throws SQLException
   	 */
	public boolean isUserAdmin(String username) {		
		
		String admin = "";

		try {
			// Create a statement with the vanilla database file, in whitelist where the admin status is stored.
			PreparedStatement stat = DataBaseBuffer.getDatabaseConnection().prepareStatement("SELECT * FROM whitelist WHERE world = ?");
			stat.setString(1, Core.GameSaveWorld);
			
			// Execute the query, and grab the results.
			ResultSet rs = stat.executeQuery();
	
			// Go through each user entry.
			while(rs.next()) {
				
				// Grab the name and set to lowercase to match.
				String name = rs.getString("username").toLowerCase().trim();
				
				// This is the username. Grab admin status and break.
				if(name.equalsIgnoreCase(username)) {
					admin = rs.getString("admin");
					break;
				}
			}
			
			// Close the SQLite handlers.
			rs.close();
			stat.close();
		} catch(SQLException e) {
			stackTrace("Failure to check if user is admin: " + username, e);
		}
		
		// Return whether or not the result is a boolean true.
		return admin.equalsIgnoreCase("true") || admin.equalsIgnoreCase("1");
	}
	
	/**
	 * Returns whether or not a user has a allowed permissions context.
	 * 
	 * @param username
	 * 
	 * @param context
	 * 
	 * @return
	 */
	public boolean hasPermission(String username, String context) {
		
		if(context == null) {
			println("Plug-in is asking permissions for a null context.");
			stackTrace();
		}
		
		if(isUserAdmin(username)) return true;
		
		boolean hasPermissionsHandler = hasPermissionModule();
		
		if(hasPermissionsHandler) {			
			
			// Loop through each handler and if any returns true, return true.
			for(PermissionsHandler handler : listPermissionHandlers) {
				
				try {				
					if(handler.hasPermission(username, context)) return true;
				} catch(Exception e) {
					stackTrace("Error handling permission check: " + handler.getClass().getName(), e);
				}
				
			}
			
		} else {
			
			Boolean result = mapDefaultPlayerPermissions.get(context.toLowerCase());
			
			if (result != null && result.booleanValue() == true) {
				return true;
			}
		}
		
		// If no permissions handler identified as true, return false.
		return false;
	}
	
	/**
	 * Registers a PermissionHandler interface.
	 * 
	 * @param handler
	 */
	public void registerPermissionsHandler(PermissionsHandler handler) {
		if(handler != null) {			
			if(!listPermissionHandlers.contains(handler)) listPermissionHandlers.add(handler);
		}
	}
	
	/**
	 * Unregisters a PermissionsHandler interface.
	 * 
	 * @param handler
	 */
	public void unregister(PermissionsHandler handler) {
		if(handler != null) {
			listPermissionHandlers.remove(handler);
		}
	}
	
	/**
	 * Returns whether or not SledgeHammer has a valid PermissionHandler.
	 * 
	 * @return
	 */
	public boolean hasPermissionModule() {
		for(PermissionsHandler handler : listPermissionHandlers) {
			if(handler != null) return true;
		}
		return false;
	}
	
	/**
	 * Returns the SledgeHammer settings-defined response when a player is
	 * denied permission to a context.
	 * 
	 * @return
	 */
	public String getPermissionDeniedMessage() {
		return sledgeHammer.getSettings().getPermissionDeniedMessage();
	}
	
	/**
	 * Sets the global permission-denied message for all Permission queries &
	 * implementations.
	 * 
	 * @param string
	 */
	public void setPermissionDeniedMessage(String string) {
		sledgeHammer.getSettings().setPermissionDeniedMessage(string);
	}

	/**
	 * Adds a default permission, that will be permitted to players when no
	 * Permission implementation is registered.
	 * 
	 * @param permissionContext
	 */
	public void addDefaultPlayerPermission(String permissionContext) {
		addDefaultPlayerPermission(permissionContext, true);
	}
	
	/**
	 * Adds a permission to be referenced when a permissions interface is not present.
	 * 
	 * @param permissionContext
	 * 
	 * @param flag
	 */
	public void addDefaultPlayerPermission(String permissionContext, boolean flag) {
		this.mapDefaultPlayerPermissions.put(permissionContext, flag);
	}

	/**
	 * Returns SledgeHammer instance running this manager.
	 * 
	 * @return
	 */
	SledgeHammer getSledgeHammer() {
		return sledgeHammer;
	}

	@Override
	public String getName() { return NAME; }

	@Override
	public void onLoad() {}

	@Override
	public void onStart() {}

	@Override
	public void onUpdate() {}

	@Override
	public void onShutDown() {}
	
}
