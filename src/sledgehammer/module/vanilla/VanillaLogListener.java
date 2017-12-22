package sledgehammer.module.vanilla;

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

import sledgehammer.event.LogEvent;
import sledgehammer.interfaces.LogListener;
import sledgehammer.util.Printable;
import zombie.core.logger.LoggerManager;

public class VanillaLogListener extends Printable implements LogListener {

	public static final String NAME = "VanillaLogListener";

	@Override
	public void onLogEntry(LogEvent logEntry) {
		String message = logEntry.getLogMessage();

		boolean important = logEntry.isImportant();
		if (important) {
			LoggerManager.getLogger("admin").write(message, "IMPORTANT");
		} else {
			LoggerManager.getLogger("admin").write(message);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}
}