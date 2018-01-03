package sledgehammer.enums;

/**
 * Enumeration to handle types of <LogEvents> with their <Integer> ids.
 * 
 * @author Jab
 */
public enum LogType {
	// @formatter:off	
	INFO(0),
	WARN(1),
	ERROR(2),
	CHEAT(3),
	STAFF(4);
	// @formatter:on

	/** The <Integer> id of the <LogType>. */
	private int id;

	/**
	 * Main constructor.
	 * 
	 * @param id
	 *            The <Integer> id of the <LogType>.
	 */
	private LogType(int id) {
		setId(id);
	}

	/**
	 * @return Returns the <Integer> id of the <LogType>.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <Integer> id of the <LogType>.
	 * 
	 * @param id
	 *            The <Integer> id to set.
	 */
	private void setId(int id) {
		this.id = id;
	}
}