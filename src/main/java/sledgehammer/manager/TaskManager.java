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

package sledgehammer.manager;

import sledgehammer.SledgeHammer;
import sledgehammer.plugin.Module;
import sledgehammer.util.TickTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TaskManager extends Manager {

    private Map<Module, List<TickTask>> mapTickTasks;
    Map<Module, List<TickTask>> mapTickTasksTemporary;
    private List<TickTask> listTasksRemove;

    @Override
    public void onLoad(boolean bDebug) {
        mapTickTasks = new HashMap<>();
        mapTickTasksTemporary = new HashMap<>();
        listTasksRemove = new ArrayList<>();
    }

    @Override
    public void onUpdate() {
        if (mapTickTasks.size() > 0) {
            // Copy the current map to avoid concurrence.
            mapTickTasksTemporary.putAll(mapTickTasks);
            // Go through each Module List.
            for (Module module : mapTickTasksTemporary.keySet()) {
                List<TickTask> listTasks = mapTickTasksTemporary.get(module);
                // Check if the Module has any registered TickTasks.
                if (listTasks != null) {
                    // Create a temporary list to avoid concurrence.
                    List<TickTask> listTasksTemporary = new ArrayList<>(listTasks);
                    for (TickTask tickTask : listTasksTemporary) {
                        // If the result of 'runTask()' is false, the TickTask is complete, and needs to be removed.
                        boolean remove;
                        try {
                            remove = !tickTask.runTask();
                        } catch (Exception e) {
                            remove = true;
                            stackTrace(e);
                            SledgeHammer.instance.handle("TickTask failed to execute.", e);
                        }
                        if (remove) {
                            listTasksRemove.add(tickTask);
                        }
                    }
                    // Remove any TickTasks that are flagged for removal.
                    if (listTasksRemove.size() > 0) {
                        listTasks.removeAll(listTasksRemove);
                        listTasksRemove.clear();
                    }
                }
            }
            mapTickTasksTemporary.clear();
        }
    }

    @Override
    public void onShutDown() {
        for (Module module : mapTickTasks.keySet()) {
            unregister(module);
        }
        mapTickTasks.clear();
    }

    @Override
    public String getName() {
        return "TaskManager";
    }

    public void unregister(Module module) {
        List<TickTask> listTickTasks = mapTickTasks.get(module);
        if (listTickTasks != null) {
            for (TickTask tickTask : listTickTasks) {
                tickTask.cancel();
            }
            listTickTasks.clear();
        }
    }

    /**
     * Registers a TickTask to the TaskManager. If the TickTask is already registered or is alive, the TickTask will not
     * be registered.
     *
     * @param module   The Module to register the TickTask.
     * @param tickTask The TickTask to register.
     */
    public void register(Module module, TickTask tickTask) {
        List<TickTask> list = mapTickTasks.get(module);
        if (list == null) {
            list = new ArrayList<>();
            mapTickTasks.put(module, list);
        }
        if (!list.contains(tickTask)) {
            list.add(tickTask);
        }
    }
}
