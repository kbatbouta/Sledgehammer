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
   * <p>
   *
   * <p>Permission nodes can be defined as such:
   *
   * <p>1) "plugin.module.command.mycommand"
   *
   * <p>The preferred syntax is the plugin, the module, noting that a command is represented by the
   * permission node, and the command's name. This doesn't have to be followed, but is highly
   * recommended for people to know the nature of the permission node in question.
   *
   * <p>
   *
   * <p>2) "plugin.module.command.mycommand.*"
   *
   * <p>This syntax has a wild-card '*' at the end of the permission node. This queries for any
   * sub-permission-node that is set to true for the player in question. If the player has the
   * permission node "plugin.module.command.mycommand.subcommand" set to true, then the wildcard
   * will grant permission to the command. If no sub-permission nodes are defined, or all
   * sub-permission nodes defined are false, then the wildcard permission node will return false.
   *
   * <p>
   *
   * <p>3) "group:mypermissiongroup"
   *
   * <p>This syntax requests the player to be in a specific group. This is a strict syntax, due to
   * the explicit check for the permission group to be assigned to a permission user that is defined
   * for the commanding player. If the player is in the default permission group (with or without a
   * permission user defined), and the permission node is "group:default", then the player will be
   * granted permission. If the player is in another permission group however, or the permission
   * group is defined as a sub-group to the parent being the default group, the permission will not
   * be granted.
   *
   * <p>
   *
   * <p>The additional prefix logic 'not' or invert character '!' can be applied to the
   * permission-node to invert the result.
   *
   * <p>
   *
   * <p>Multiple permission nodes can be assigned to a command. This is useful when checking
   * multiple permission groups, or checking multiple permission nodes.
   *
   * @return Returns the permission node for the command. The default is "*", which is accepted
   *     universally for all situations.
   */
  String[] permission() default "*";

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
