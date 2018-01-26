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

package sledgehammer.event.core.player;

import sledgehammer.event.Event;

/**
 * Event that is passed when a Player attempts to connect to the PZ server.
 *
 * @author Jab
 */
public class PreConnectEvent extends Event {

  /** The String ID of the Event. */
  public static final String ID = "LoginUsernameDefinedEvent";

  /** The String user-name of the Player attempting to connect to the PZ server. */
  private String username;

  /**
   * Main constructor.
   *
   * @param username The String user-name of the Player that is attempting to connect to the PZ
   *     server.
   */
  public PreConnectEvent(String username) {
    setUsername(username);
  }

  @Override
  public String getLogMessage() {
    return "User attempting to log in with username: \"" + username + "\".";
  }

  @Override
  public String getID() {
    return ID;
  }

  /**
   * @return Returns the String user-name of the Player that is attempting to connect to the PZ
   *     server.
   */
  public String getUsername() {
    return username;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the String user-name of the Player that is attempting to connect to the PZ server.
   *
   * @param username The String user-name to set.
   */
  private void setUsername(String username) {
    this.username = username;
  }
}
