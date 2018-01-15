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
 * Class to store input values for LanguagePackage.
 *
 * @author Jab
 */
public class EntryField {

    /**
     * The String key of the Entry. (The ID)
     */
    private String key;
    /**
     * The Object value of the Entry.
     */
    private Object value;

    /**
     * Main constructor.
     *
     * @param key   The String key of the Entry. (The ID)
     * @param value The Object value.
     */
    public EntryField(String key, Object value) {
        setKey(key);
        setValue(value);
    }

    /**
     * @param key The String key being tested.
     * @return Returns true if the given String key matches the one defined in the
     * EntryField.
     */
    public boolean isKey(String key) {
        return this.key.equalsIgnoreCase(key);
    }

    /**
     * @return The String key of the Entry.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Sets the String key of the Entry.
     *
     * @param key The String key to set.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return Returns the value of the Entry.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the value of the Entry.
     *
     * @param value The Object value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }
}