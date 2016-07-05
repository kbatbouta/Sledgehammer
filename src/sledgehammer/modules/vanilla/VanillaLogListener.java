package sledgehammer.modules.vanilla;

import sledgehammer.event.LogEvent;
import sledgehammer.interfaces.LogListener;
import sledgehammer.util.Printable;
import zombie.core.logger.LoggerManager;

public class VanillaLogListener extends Printable implements LogListener {

		@Override
		public void onLogEntry(LogEvent logEntry) {
			String message = logEntry.getLogMessage();
			println(message);
			
			if(message == null) {
				stackTrace();
			}
			
			boolean important = logEntry.isImportant();
			if(important) {
				LoggerManager.getLogger("admin").write(message, "IMPORTANT");			
			} else {			
				LoggerManager.getLogger("admin").write(message);
			}
		}

		@Override
		public String getName() {
			return "VanillaLogListener";
		}
	}