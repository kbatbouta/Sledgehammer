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
 *
 */
public class GenericEvent extends Event {

	/**
	 * The ID of the Event.
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
	 * The type. Used to clarify in a module-like way what the event is, or what
	 * group of events this is.
	 */
	private String type = null;

	/**
	 * The logger message. If not null, will be logged, depending on the logger
	 * implementation.
	 */
	private String logMessage = null;

	private Map<String, String> mapVariables;

	/**
	 * Main constructor.
	 * 
	 * @param module
	 * 
	 * @param type
	 * 
	 * @param context
	 */
	public GenericEvent(Module module, String type, String context) {

		// Set the variables to those given.
		this.moduleSent = module;
		this.type = type;
		this.context = context;

		mapVariables = new HashMap<>();
	}

	/**
	 * Returns the type of this <GenericEvent>. This is used to clarify in a
	 * module-like way what the event is, or what group of events this is.
	 *
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the context of this <GenericEvent>. This usually carries data, or a
	 * simple string.
	 * 
	 * @return
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Returns the <Module> that authored this <GenericEvent>.
	 *
	 * @return
	 */
	public Module getModule() {
		return moduleSent;
	}

	/**
	 * Sets a generic variable with a value.
	 * 
	 * @param key
	 * 
	 * @param value
	 */
	public void setVariable(String key, String value) {
		mapVariables.put(key, value);
	}

	/**
	 * Returns a generic variable, with a given key.
	 * 
	 * @param key
	 * 
	 * @return
	 */
	public String getVariable(String key) {
		return mapVariables.get(key);
	}

	/**
	 * Returns a <Map> of generic variables.
	 * 
	 * @return
	 */
	public Map<String, String> getVariables() {
		return mapVariables;
	}

	@Override
	public String getLogMessage() {
		return logMessage;
	}

	/**
	 * Executes a command on the module that send this <GenericEvent>.
	 * 
	 * @param type
	 * 
	 * @param context
	 */
	public void executeCommand(String type, String context) {
		Module module = getModule();
		if (module != null) {
			module.executeCommand(type, context);
		}
	}

	/**
	 * Sets the logged message for this <GenericEvent>. If null, the event will not
	 * be logged.
	 * 
	 * @param message
	 */
	public void setLogMessage(String message) {
		logMessage = message;
	}

	@Override
	public String getID() {
		return ID;
	}

}
