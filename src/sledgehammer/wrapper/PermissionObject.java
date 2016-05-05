package sledgehammer.wrapper;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;

public class PermissionObject {
	
	/**
	 * The identified name of the permission object.
	 */
	private String name;
	
	/**
	 * The ID of the permission object (Used in SQLite).
	 */
	private int id;
	
	/**
	 * The map containing the context permissions.
	 */
	private Map<String, Boolean> mapPermissions;
	
	/**
	 * Main constructor.
	 * @param name
	 */
	public PermissionObject(String name, int id) {
		this.id = id;
		this.name = name;
		mapPermissions = new HashMap<>();
		
		if(SledgeHammer.DEBUG) System.out.println("PermissionObject: " + "CREATE: ID: " + getID() + " NAME: " + getName());
	}
	
	/**
	 * Adds a context permission to the group.
	 * @param context
	 * @param allow
	 */
	public void setPermission(String context, boolean allow) {
		
		if(SledgeHammer.DEBUG) System.out.println("PermissionObject: " + getName() + ": Adding permission \"" + context + "\": " + allow + ".");
		
		// Force context to lowercase to avoid any inconsistencies.
		context = context.toLowerCase();
		
		// Add to the map, regardless of pre-existing settings for context.
		mapPermissions.put(context, allow);
	}
	
	/**
	 * Removes a context permission from the permissions object.
	 * @param context
	 */
	public void removePermission(String context) {
		
		if(hasPermission(context)) {

			// Force context to lowercase to avoid any inconsistencies.
			context = context.toLowerCase();
			
			mapPermissions.remove(context);

		}
	
	}
	
	/**
	 * Returns whether or not the permissions object has a context permission definition.
	 * @param context
	 * @return
	 */
	public boolean hasPermission(String context) {
		
		// Force context to lowercase to avoid any inconsistencies.
		context = context.toLowerCase();
		
		return mapPermissions.containsKey(context);

	}
	
	/**
	 * Returns true if any sub-context permission is granted.
	 * @param contextRoot
	 * @return
	 */
	public boolean hasAnyPermission(String contextRoot) {
		
		// Clean up the string if any spaces or upper-case is present.
		contextRoot = contextRoot.toLowerCase().trim();
		
		// Grab the group's permissions.
		Map<String, Boolean> permissions = getPermissions();
		
		// Go through each permission context.
		for(String context : permissions.keySet()) {
			
			// If the context given contains the contextRoot,
			if(context.toLowerCase().contains(contextRoot)) {
				
				// And if the context is true, then return true.
				if(permissions.get(context)) {
					return true;
				}
				
				// Else, keep looping until there is a true context.
			}
		}
		
		// If no contexts are present returning true, then return false.
		return false;
	}
	
	/**
	 * Returns the map of context permissions for the group.
	 * @return
	 */
	public Map<String, Boolean> getPermissions() {
		return mapPermissions;
	}
	
	public int getID() {
		return this.id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
