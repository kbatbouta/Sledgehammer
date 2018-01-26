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

package sledgehammer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation handles the assignment and identification of a method to handle commands passed
 * to the Sledgehammer engine.
 *
 * @author Jab
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {

  /**
   * Set the command here. Commands are case-sensitive, however they are interpreted as lower-case.
   *
   * @return Returns the command to handle.
   */
  String[] command();

  /**
   * Set the permission for the command here. Commands require permissions in order to determine
   * whether or not the commanding player has access to the command.
   *
   * @return Returns the permission node for the command.
   */
  String permission();

  /**
   * If this is set to true, the permission will be granted to the default permission group for
   * players who aren't registered as permission users with a group or a specific definition. This
   * means that the permission can be explicitly denied to players with defined permissions, while
   * allowing quicker access to public commands.
   *
   * @return Returns true if the command's permission node is granted by default to players.
   */
  boolean defaultPermission() default false;

  /**
   * The CommandHandler priority is a means of determining the importance of the CommandHandler when
   * compared to other handlers. If the number set is higher than another command handler, then the
   * command handler will run first. The highest number is the most important.
   *
   * @return Returns 0 by default.
   */
  int priority() default 0;

  /**
   * The string ID defined is useful for determining the origin of the command handler, or the
   * associated ID of the command handler. This is an optional setting.
   *
   * @return Returns an empty string by default.
   */
  String id() default "";

  /**
   * Set this to true if the command handler should handle commands that are handled prior to being
   * handled by the command handler.
   *
   * @return Returns false by default.
   */
  boolean ignoreHandled() default false;
}
