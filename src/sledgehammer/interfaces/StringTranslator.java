package sledgehammer.interfaces;

import sledgehammer.wrapper.Player;

public interface StringTranslator {
	
	public String translate(String context, String source);
	public String translate(Player player, String context, String source);
}
