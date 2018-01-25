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

package sledgehammer.module.vanilla;

import sledgehammer.annotations.EventHandler;
import sledgehammer.event.LogEvent;
import sledgehammer.interfaces.Listener;
import zombie.core.logger.LoggerManager;

/**
 * LogListener that forwards LogEvents to Project Zomboid's administrator
 * ZLogger instance.
 *
 * @author Jab
 */
class VanillaEventListener implements Listener {

    /**
     * The String name of the Module.
     */
    public static final String NAME = "VanillaLogListener";

    @EventHandler(id = "core.vanilla.event.log")
    public void on(LogEvent event) {
        String message = event.getLogMessage();
        boolean important = event.isImportant();
        if (important) {
            LoggerManager.getLogger("admin").write(message, "IMPORTANT");
        } else {
            LoggerManager.getLogger("admin").write(message);
        }
    }
}