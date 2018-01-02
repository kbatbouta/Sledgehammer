package sledgehammer.util;

/**
 * Enumeration for CoOpAccessEvent results.
 * 
 * @author Jab
 */
public enum AccessResult {
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
	private AccessResult(int id) {
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