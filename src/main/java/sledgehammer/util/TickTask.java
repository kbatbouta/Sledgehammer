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

package sledgehammer.util;

import sledgehammer.SledgeHammer;
import sledgehammer.plugin.Module;

/**
 * Class to handle the execution of Tasks that are either one-time executions, delayed executions, or timed executions.
 *
 * @author Jab
 */
public abstract class TickTask extends Printable {

    private TaskType type;
    private int delayTicksStart = 0;
    private int delayTicksTimer = 1;
    private int delayTicks = 0;
    private boolean alive = false;

    /**
     * Executes a TickTask implementation directly. This is called from the Sledgehammer engine directly, and should not
     * be ran by a third-party, although it can be used in this manner.
     * <p>
     * If the TickTask is a timer-task and an Exception is thrown, then the TickTask is cancelled.
     *
     * @return Returns true if the task is a timer-task and has successfully ran. If false is returned, the TickTask is
     * unregistered from the TaskManager.
     */
    public boolean runTask() {
        // If cancelled, return false to unregister the TickTask.
        if (!isAlive()) {
            return false;
        }
        // If the TickTask is delayed on start or is a timer-task, then delay until the timer reaches 0.
        if (delayTicks > 0) {
            delayTicks--;
            return true;
        }
        // Initially set to return true if the task is a timer-task.
        boolean returned = type == TaskType.TIMER;
        try {
            // If this is a timer-task, then set the result of the run method as the boolean.
            if (returned) {
                returned = run();
            }
            // The result does not matter if the task is only ran once.
            else {
                run();
            }
            if (returned) {
                delayTicks = this.delayTicksTimer;
            }
        }
        // If the TickTask fails to execute, then make sure that ThrowableListeners pick it up by stackTracing.
        // If the TickTask is a timer-task, cancel it.
        catch (Exception e) {
            returned = false;
            stackTrace(e);
            SledgeHammer.instance.handle("TickTask failed to execute.", e);
        }
        // Set the flag for being alive. This flag is synonymous with the returned boolean value.
        this.alive = returned;
        // Return the result from the execution of the TickTask.
        return returned;
    }

    /**
     * Registers and runs the TickTask as a one-time operation.
     * <p>
     * Throws an IllegalArgumentException if the following occurs:
     * - The Module provided is null or not loaded.
     * <p>
     * Throws an IllegalStateException if the following occurs:
     * - The TickTask is already running.
     *
     * @param module The Module to register the TickTask to the TaskManager.
     */
    public void runTask(Module module) {
        if (module == null || !module.isLoaded()) {
            throw new IllegalArgumentException("Module provided is null or isn't loaded.");
        }
        if (isAlive()) {
            throw new IllegalStateException("TickTask is already running.");
        }
        // @formatter:off
        this.delayTicksStart = 0;
        this.delayTicksTimer = 1;
        this.delayTicks      = 0;
        // @formatter:on
        SledgeHammer.instance.getTaskManager().register(module, this);
    }

    /**
     * Registers and runs the TickTask as a one-time operation, with a delayed start.
     * <p>
     * Throws an IllegalArgumentException if the following occurs:
     * - The Module provided is null or not loaded.
     * - The delayTicksStart argument is less than 0.
     * <p>
     * Throws an IllegalStateException if the following occurs:
     * - The TickTask is already running.
     *
     * @param module          The Module to register the TickTask to the TaskManager.
     * @param delayTicksStart The delay in ticks to execute the TickTask the first time.
     */
    public void runTaskLater(Module module, int delayTicksStart) {

    }

    /**
     * Registers and runs the TickTask as timed operation.
     * <p>
     * Throws an IllegalArgumentException if the following occurs:
     * - The Module provided is null or not loaded.
     * - The delayTicksStart argument is less than 0.
     * - The delayTicksTimer argument is less than or equal to 0.
     * <p>
     * Throws an IllegalStateException if the following occurs:
     * - The TickTask is already running.
     *
     * @param module          The Module to register the TickTask to the TaskManager.
     * @param delayTicksStart The delay in ticks to execute the TickTask the first time.
     * @param delayTicksTimer The interval in ticks to execute the TickTask.
     */
    public void runTaskTimer(Module module, int delayTicksStart, int delayTicksTimer) {
        if (module == null || !module.isLoaded()) {
            throw new IllegalArgumentException("Module provided is null or isn't loaded.");
        }
        if (isAlive()) {
            throw new IllegalStateException("TickTask is already running.");
        }
        if (delayTicksStart < 0) {
            throw new IllegalArgumentException("delayTicksStart cannot be less than 0.");
        }
        if (delayTicksTimer <= 0) {
            throw new IllegalArgumentException("delayTicksTimer cannot be less than 1.");
        }
        // @formatter:off
        this.delayTicks      = delayTicksStart;
        this.delayTicksStart = delayTicksStart;
        this.delayTicksTimer = delayTicksTimer;
        this.type            = TaskType.TIMER ;
        this.alive           = true           ;
        // @formatter:on
        SledgeHammer.instance.getTaskManager().register(module, this);
    }

    /**
     * Cancels the TickTask.
     * <p>
     * The method throws an IllegalStateException if the TickTask is not running.
     */
    public void cancel() {
        // Make sure that the TickTask is actually running.
        if (!this.alive) {
            throw new IllegalStateException("The TickTask to cancel is not running.");
        }
        // @formatter:off
        this.alive           = false;
        this.delayTicks      = 0    ;
        this.delayTicksTimer = 1    ;
        this.delayTicksStart = 0    ;
        // @formatter:on
    }

    /**
     * @return Returns true if the TickTask is used by the TaskManager.
     */
    public boolean isAlive() {
        return this.alive;
    }

    /**
     * The execution method for the TickTask implementation.
     *
     * @return The only importance of returning a boolean is for timer-tasks. If true is returned, the timer will keep
     * running. If false is returned, the TickTask will be unregistered from the TaskManager.
     */
    public abstract boolean run();
}