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

package sledgehammer.enums;

/**
 * Enumeration to handle types of LogEvents.
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

  /** The id of the LogType. */
  private int id;

  /**
   * Main constructor.
   *
   * @param id The id of the LogType.
   */
  LogType(int id) {
    setId(id);
  }

  /** @return Returns the id of the LogType. */
  public int getId() {
    return this.id;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the id of the LogType.
   *
   * @param id The id to set.
   */
  private void setId(int id) {
    this.id = id;
  }
}
