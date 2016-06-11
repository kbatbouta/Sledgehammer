package sledgehammer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.interfaces.PermissionHandler;
import sledgehammer.util.Printable;
import zombie.core.Core;
import zombie.network.DataBaseBuffer;

public class PermissionsManager extends Printable {
	
	/**
	 * Debug boolean, used for verbose output.
	 */
	public static boolean DEBUG = false;

	/**
	 * The instance of SledgeHammer using this Manager.
	 */
	private SledgeHammer sledgeHammer = null;
	
	/**
	 * List of registered PermissionHandler interfaces.
	 */
	private List<PermissionHandler> listPermissionHandlers;
	
	
	private Map<String, Boolean> mapDefaultPlayerPermissions;
	
	/**
	 * Permission Denied message to send to players.
	 */
	private String permissionDeniedMessage = "Permission denied.";

	public PermissionsManager(SledgeHammer instance) {
		sledgeHammer           = instance         ;
		listPermissionHandlers = new ArrayList<>();
		mapDefaultPlayerPermissions = new HashMap<>();
	}

	/**
   	 * Returns whether or not the vanilla whitelist has a user set to admin.
   	 * @param username
   	 * @return
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
	 * @param username
	 * @param context
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
			for(PermissionHandler handler : listPermissionHandlers) {
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
	 * @param handler
	 */
	public void registerPermissionHandler(PermissionHandler handler) {
		if(handler != null) {			
			if(!listPermissionHandlers.contains(handler)) listPermissionHandlers.add(handler);
		}
	}
	
	/**
	 * Returns whether or not SledgeHammer has a valid PermissionHandler.
	 * @return
	 */
	public boolean hasPermissionModule() {
		for(PermissionHandler handler : listPermissionHandlers) {
			if(handler != null) return true;
		}
		return false;
	}
	
	SledgeHammer getSledgeHammer() {
		return sledgeHammer;
	}
	
	public String getPermissionDeniedMessage() {
		return this.permissionDeniedMessage;
	}
	
	public void setPermissionDeniedMessage(String string) {
		this.permissionDeniedMessage = string;
	}

	public void addDefaultPlayerPermission(String permissionContext) {
		addDefaultPlayerPermission(permissionContext, true);
	}
	
	public void addDefaultPlayerPermission(String permissionContext, boolean flag) {
		this.mapDefaultPlayerPermissions.put(permissionContext, flag);
	}

	@Override
	public String getName() { return "Permissions"; }
	
}
