package sledgehammer.interfaces;

import sledgehammer.wrapper.PermissionObject;

public interface GroupPermissionsHandler extends PermissionsHandler {
	public PermissionObject getGroup(String username);
}
