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

/**
 * Event to dispatch when a native LuaEvent is fired on the PZ Server.
 *
 * @author Jab
 */
public class ScriptEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "ScriptEvent";

    /**
     * The String context for the ScriptEvent.
     */
    private String context = null;

    /**
     * The generic Object Array arguments for the ScriptEvent.
     */
    private Object[] arguments = null;

    /**
     * Main constructor.
     *
     * @param context   The String context for the ScriptEvent.
     * @param arguments The Generic Object Array arguments for the ScriptEvent.
     */
    public ScriptEvent(String context, Object... arguments) {
        super();
        setContext(context);
        setArguments(arguments);
    }

    @Override
    public String getLogMessage() {
        return null;
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * @return Returns the String context of the ScriptEvent.
     */
    public String getContext() {
        return context;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String context for the ScriptEvent.
     *
     * @param context The String context to set.
     */
    private void setContext(String context) {
        this.context = context;
    }

    /**
     * @return Returns the generic Object Array of arguments for the ScriptEvent.
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Sets generic Object arguments for the ScriptEvent.
     *
     * @param arguments The Object Array to set.
     */
    public void setArguments(Object... arguments) {
        this.arguments = arguments;
    }
}