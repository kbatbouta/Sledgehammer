package zirc.interfaces;

import zirc.event.CommandEvent;
import zirc.wrapper.Player;

public interface CommandListener {
	String[] getCommands();
	public void onCommand(CommandEvent command);
	public String onTooltip(Player player, String command);
}
