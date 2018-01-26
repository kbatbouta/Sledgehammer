/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

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

  /** The Integer id of the Language. */
  private int id;
  /** The String abbreviation of the Language. */
  private String abbreviation;

  /**
   * Main constructor.
   *
   * @param id The Integer id of the Language.
   * @param abbreviation The abbreviation of the Language.
   */
  Language(int id, String abbreviation) {
    setId(id);
    setAbbreviation(abbreviation);
  }

  /** @return Returns the String abbreviation of the Language. */
  public String getAbbreviation() {
    return this.abbreviation;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the String abbreviation of the Language.
   *
   * @param abbreviation The String abbreviation to set.
   */
  private void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  /** @return Returns the id of the Language. */
  public int getId() {
    return this.id;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the id of the Language.
   *
   * @param id The id to set.
   */
  private void setId(int id) {
    this.id = id;
  }

  /**
   * @param id The id of the Language.
   * @return Returns a Language with the given id. If no Language identifies with the id given, null
   *     is returned.
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
   * @return Returns A Language with the given name. If no Language identifies with the name given,
   *     null is returned.
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
   * @return Returns a Language with the given abbreviation. If no Language identifies with the
   *     abbreviation given, null is returned.
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
