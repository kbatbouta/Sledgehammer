package sledgehammer.util;

public class Response {
	String response;
	String log;
	Result result;

	public Response(String response, String log, Result result) {
		this.response = response;
		this.log = log;
		this.result = result;
	}
	
	public String getResponse() {
		return this.response;
	}
	
	public String getLogMessage() {
		if(log == null  ) return null;
		if(log.isEmpty()) return null;
		return this.log;
	}
	
	public Result getResult() {
		return result;
	}
}
