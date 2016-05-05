package sledgehammer.interfaces;

import sledgehammer.event.CommandEvent;
import sledgehammer.wrapper.Player;

public interface CommandListener {
	String[] getCommands();
	
	public void onCommand(CommandEvent command);
	
	public String onTooltip(Player player, String command);
	
	public String getPermissionContext(String command);
}
