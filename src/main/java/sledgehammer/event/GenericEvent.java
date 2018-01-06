/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.event;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.plugin.Module;

/**
 * This is an Event sub-type that allows Modules to author commands, and send
 * them to other modules for evaluation and execution.
 *
 * @author Jab
 */
public class GenericEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "GENERIC_EVENT";

    /**
     * The module that created this Event.
     */
    private Module moduleSent;
    /**
     * The context of the Event. Usually carrying data or a simple string.
     */
    private String context = null;
    /**
     * The type. This is used to clarify in a Module-like way what the Event is,
     * or what group of Event the GenericEvent is.
     */
    private String type = null;
    /**
     * The logger message. If not null, will be logged, depending on the logger
     * implementation.
     */
    private String logMessage = null;
    /**
     * The Map of String key->value generic variables to set for the Event.
     */
    private Map<String, String> mapVariables;

    /**
     * Main constructor.
     *
     * @param moduleSent The Module authoring the GenericEvent.
     * @param type       The String type of GenericEvent as an identification.
     * @param context    The String context of the GenericEvent. This Usually carries
     *                   data, or is a String.
     */
    public GenericEvent(Module moduleSent, String type, String context) {
        setModuleSent(moduleSent);
        setType(type);
        setContext(context);
        mapVariables = new HashMap<>();
    }

    @Override
    public String getLogMessage() {
        return this.logMessage;
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * @return Returns the type of this GenericEvent. This is used to clarify in a
     * module-like way what the event is, or what group of events this is.
     */
    public String getType() {
        return type;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String type of GenericEvent.
     *
     * @param type The String type to set.
     */
    private void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the context of this GenericEvent. This usually carries
     * data, or a simple string.
     */
    public String getContext() {
        return context;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String context of the GenericEvent.
     *
     * @param context The String context to set.
     */
    private void setContext(String context) {
        this.context = context;
    }

    /**
     * @return Returns the Module that authored this GenericEvent.
     */
    public Module getModule() {
        return moduleSent;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Module that authored the GenericEvent.
     *
     * @param moduleSent The Module to set.
     */
    private void setModuleSent(Module moduleSent) {
        this.moduleSent = moduleSent;
    }

    /**
     * Sets a generic variable with a value.
     *
     * @param key   The String key to pair with the String value.
     * @param value The String value for the String key given.
     */
    public void setVariable(String key, String value) {
        mapVariables.put(key, value);
    }

    /**
     * @param key The String key paired with the String value to return.
     * @return Returns a String variable, with a given String key.
     */
    public String getVariable(String key) {
        return mapVariables.get(key);
    }

    /**
     * @return Returns a Map of generic variables.
     */
    public Map<String, String> getVariables() {
        return mapVariables;
    }

    /**
     * Executes a command on the module that send this GenericEvent.
     *
     * @param type    The String type.
     * @param context The String context data.
     */
    public void executeCommand(String type, String context) {
        Module module = getModule();
        if (module != null) {
            module.executeCommand(type, context);
        }
    }

    /**
     * Sets the logged message for this GenericEvent. If null, the Event will not
     * be logged.
     *
     * @param message The String message to log.
     */
    public void setLogMessage(String message) {
        logMessage = message;
    }
}