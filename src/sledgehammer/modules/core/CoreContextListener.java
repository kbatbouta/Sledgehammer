package sledgehammer.modules.core;

import sledgehammer.interfaces.ContextListener;
import sledgehammer.util.Printable;
import sledgehammer.wrapper.Player;
import zombie.core.Translator;

public class CoreContextListener extends Printable implements ContextListener {

	@Override
	public String onContext(String context, String source) {
		try {
			if(context != null) {				
				String result = Translator.getText(context);
				if(result != null && !result.isEmpty()) return result;
			}
		} catch(Exception e) {
			stackTrace(e);
		}
		return source;
	}

	@Override
	public String onPlayerContext(Player player, String context, String source) {
		return onContext(context, source);
	}


	@Override public String getContext(String context) { return null; }
	@Override public String getName() { return "Core-Translator"; }
}
