package zirc.interfaces;

import zirc.event.LogEvent;

public interface LogListener {
	public void onLogEntry(LogEvent logEntry);
}
