package sledgehammer.event;

public class CoOpAccessEvent extends CoOpEvent {
	
	public static final String ID = "CoOpAccessEvent";
	
	public static enum Result {
		GRANTED,
		DENIED
	}
	
	private Result result = null;
	
	private String reason = null;
	
	public CoOpAccessEvent(Result result) {
		this.result = result;
	}
	
	public CoOpAccessEvent(Result result, String reason) {
		this.result = result;
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}
	
	public Result getResult() {
		return result;
	}

	@Override
	public String getLogMessage() {
		//TODO: LogEvent message;
		return null;
	}

	@Override
	public String getID() { return ID; }
}
