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
 * Enumeration for CoOpAccessEvent results.
 *
 * @author Jab
 */
public enum AccessResult {
  // @formatter:off
  GRANTED(0),
  DENIED(1);
  // @formatter:on

  /** The Id of the Result. */
  private int id;

  /**
   * Main constructor.
   *
   * @param id The id of the Result.
   */
  AccessResult(int id) {
    setId(id);
  }

  /** @return Returns the id of the Result. */
  public int getId() {
    return this.id;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the id of the Result.
   *
   * @param id The id to set.
   */
  private void setId(int id) {
    this.id = id;
  }
}
