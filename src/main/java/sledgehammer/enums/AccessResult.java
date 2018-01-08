package sledgehammer.enums;

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

    /**
     * The Id of the Result.
     */
    private int id;

    /**
     * Main constructor.
     *
     * @param id The id of the Result.
     */
    AccessResult(int id) {
        setId(id);
    }

    /**
     * @return Returns the id of the Result.
     */
    public int getId() {
        return this.id;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the id of the Result.
     *
     * @param id The id to set.
     */
    private void setId(int id) {
        this.id = id;
    }
}