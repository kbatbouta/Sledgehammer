package sledgehammer.language;

/**
 * Enumeration to identify a Language and access its properties.
 *
 * @author Jab
 */
public enum Language {
    // @formatter:off
	English(0, "en");
	// @formatter:on

    /**
     * The Integer id of the Language.
     */
    private int id;
    /**
     * The String abbreviation of the Language.
     */
    private String abbreviation;

    /**
     * Main constructor.
     *
     * @param id           The Integer id of the Language.
     * @param abbreviation The abbreviation of the Language.
     */
    Language(int id, String abbreviation) {
        setId(id);
        setAbbreviation(abbreviation);
    }

    /**
     * @return Returns the String abbreviation of the Language.
     */
    public String getAbbreviation() {
        return this.abbreviation;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String abbreviation of the Language.
     *
     * @param abbreviation The String abbreviation to set.
     */
    private void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * @return Returns the id of the Language.
     */
    public int getId() {
        return this.id;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the id of the Language.
     *
     * @param id The id to set.
     */
    private void setId(int id) {
        this.id = id;
    }

    /**
     * @param id The id of the Language.
     * @return Returns a Language with the given id. If no Language
     * identifies with the id given, null is returned.
     */
    public static Language getLanguage(int id) {
        Language returned = null;
        for (Language language : Language.values()) {
            if (language.getId() == id) {
                returned = language;
                break;
            }
        }
        return returned;
    }

    /**
     * @param name The name of the Language.
     * @return Returns A Language with the given name. If no Language
     * identifies with the name given, null is returned.
     */
    public static Language getLanguage(String name) {
        Language returned = null;
        for (Language language : Language.values()) {
            if (language.name().equalsIgnoreCase(name)) {
                returned = language;
                break;
            }
        }
        return returned;
    }

    /**
     * @param abbreviation The abbreviation of the Language.
     * @return Returns a Language with the given abbreviation. If no
     * Language identifies with the abbreviation given, null is returned.
     */
    public static Language getLanguageWithAbbreviation(String abbreviation) {
        Language returned = null;
        for (Language language : Language.values()) {
            if (language.getAbbreviation().equalsIgnoreCase(abbreviation)) {
                returned = language;
                break;
            }
        }
        return returned;
    }
}