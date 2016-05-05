package sledgehammer.interfaces;

import sledgehammer.wrapper.PermissionObject;

public interface GroupPermissionHandler extends PermissionHandler {
	public PermissionObject getGroup(String username);
}
