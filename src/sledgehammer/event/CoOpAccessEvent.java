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
