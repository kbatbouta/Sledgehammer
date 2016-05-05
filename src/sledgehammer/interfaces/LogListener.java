package sledgehammer.interfaces;

import sledgehammer.event.LogEvent;

public interface LogListener {
	public void onLogEntry(LogEvent logEntry);
}
