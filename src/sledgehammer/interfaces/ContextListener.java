package sledgehammer.interfaces;

import sledgehammer.objects.Player;

public interface ContextListener {
	
	public String onContext(String context, String source);
	public String onPlayerContext(Player player, String context, String source);
	public String getContext(String context);
}
