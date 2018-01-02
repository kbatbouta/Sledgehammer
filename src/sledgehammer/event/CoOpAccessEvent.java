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

/**
 * CoOpEvent to handle Access requests to the PZ server.
 * 
 * @author Jab
 */
public class CoOpAccessEvent extends CoOpEvent {

	/** The String ID of the Event. */
	public static final String ID = "CoOpAccessEvent";

	/** The <Result> of the CopAccessEvent. */
	private Result result = null;
	/** The <String> reason provided for the <Result>. */
	private String reason = null;

	/**
	 * Main constructor.
	 * 
	 * @param result
	 *            The <Result> of the Co-Op Access Request.
	 */
	public CoOpAccessEvent(Result result) {
		this.result = result;
	}

	/**
	 * Alternative constructor for providing a <String> reason for the <Result>
	 * given.
	 * 
	 * @param result
	 *            The <Result> of the Co-Op Access Request.
	 * @param reason
	 *            The <String> reason for the <Result> given.
	 */
	public CoOpAccessEvent(Result result, String reason) {
		this.result = result;
		this.reason = reason;
	}

	@Override
	public String getLogMessage() {
		return null;
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * @return Returns the <String> reason for the <Result> given.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @return Returns the <Result> of the Co-Op Access Request.
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * Enumeration for CoOpAccessEvent results.
	 * 
	 * @author Jab
	 */
	public static enum Result {
		// @formatter:off
		GRANTED(0),
		DENIED(1);
		// @formatter:on

		/** The <Integer> Id of the <Result>. */
		private int id;

		/**
		 * Main constructor.
		 * 
		 * @param id
		 *            The <Integer> id of the <Result>.
		 */
		private Result(int id) {
			setId(id);
		}

		/**
		 * @return Returns the <Integer> id of the <Result>.
		 */
		public int getId() {
			return this.id;
		}

		/**
		 * (Private Method)
		 * 
		 * Sets the <Integer> id of the <Result>.
		 * 
		 * @param id
		 *            The <Integer> id to set.
		 */
		private void setId(int id) {
			this.id = id;
		}
	}
}