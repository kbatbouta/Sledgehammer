package sledgehammer.language;

public enum Language {
	English(0, "en");

	private int id;
	private String abbreviation;

	private Language(int id, String abbreviation) {
		setId(id);
		setAbbreviation(abbreviation);
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	private void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public int getId() {
		return this.id;
	}

	private void setId(int id) {
		this.id = id;
	}

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