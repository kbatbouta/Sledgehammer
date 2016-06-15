package sledgehammer.interfaces;

import sledgehammer.wrapper.PermissionObject;

public interface GroupPermissionHandler extends PermissionsHandler {
	public PermissionObject getGroup(String username);
}
