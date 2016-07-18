package sledgehammer.modules.core;

import sledgehammer.interfaces.StringTranslator;
import sledgehammer.util.Printable;
import sledgehammer.wrapper.Player;
import zombie.core.Translator;

public class CoreTranslator extends Printable implements StringTranslator {

	@Override
	public String translate(String context, String source) {
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
	public String translate(Player player, String context, String source) {
		return translate(context, source);
	}

	@Override
	public String getName() { return "Core-Translator"; }
}
