package sledgehammer.enums;

public enum DisconnectType {
	// @formatter:off
	DISCONNECT_EXITED_GAME(0),
	DISCONNECT_SERVER_FULL(1),
	DISCONNECT_USERNAME_ALREADY_LOGGED_IN(2),
	DISCONNECT_USERNAME_EMPTY(3),
	DISCONNECT_USERNAME_BANNED(4),
	DISCONNECT_KICKED(5),
	DISCONNECT_IP_BANNED(6),
	DISCONNECT_STEAM_BANNED(7),
	DISCONNECT_MISC(8);
	// @formatter:on

	/** The <Integer> id of the <DisconnectType>. */
	private int id;

	/**
	 * Main constructor.
	 * 
	 * @param id
	 *            The <Integer> id of the <DisconnectType>.
	 */
	private DisconnectType(int id) {
		setId(id);
	}

	/**
	 * @return Returns the <Integer> id of the <DisconnectType>.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <Integer> id of the <DisconnectType>.
	 * 
	 * @param id
	 *            The <Integer> id to set.
	 */
	private void setId(int id) {
		this.id = id;
	}
}