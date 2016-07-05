package sledgehammer.modules.vanilla;

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
	public String getName() { return NAME; }
}