package sledgehammer.modules.core;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

import sledgehammer.interfaces.ContextListener;
import sledgehammer.objects.Player;
import sledgehammer.util.Printable;
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
