package sledgehammer.modules.vanilla;

import sledgehammer.event.LogEvent;
import sledgehammer.interfaces.LogListener;
import zombie.core.logger.LoggerManager;

public class VanillaLogListener implements LogListener {

		@Override
		public void onLogEntry(LogEvent logEntry) {
			String message = logEntry.getLogMessage();
			boolean important = logEntry.isImportant();
			if(important) {
				LoggerManager.getLogger("admin").write(message, "IMPORTANT");			
			} else {			
				LoggerManager.getLogger("admin").write(message);
			}
		}
	}