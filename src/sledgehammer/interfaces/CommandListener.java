package sledgehammer.interfaces;

import sledgehammer.lua.core.Player;
import sledgehammer.objects.chat.Command;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public interface CommandListener {
	
	String[] getCommands();
	
	public void onCommand(Command command, Response response);
	
	public String onTooltip(Player player, Command command);
	
	public String getPermissionNode(String command);
}
