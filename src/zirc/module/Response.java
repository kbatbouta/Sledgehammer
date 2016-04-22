package zirc.module;

import zirc.event.CommandEvent.Result;

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
		return this.log;
	}
	
	public Result getResult() {
		return result;
	}
}
