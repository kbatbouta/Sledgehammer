package zirc.interfaces;

import zirc.wrapper.PermissionObject;

public interface GroupPermissionHandler extends PermissionHandler {
	public PermissionObject getGroup(String username);
}
