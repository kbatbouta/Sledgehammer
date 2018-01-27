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

package sledgehammer.event;

import sledgehammer.Settings;
import sledgehammer.annotations.EventHandler;
import sledgehammer.interfaces.Cancellable;
import sledgehammer.interfaces.Listener;
import sledgehammer.util.ClassUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * CommandHandlerContainer is a container class for the event handler Annotation. This is to help
 * cache and handle data and operations for event handlers in Sledgehammer. The method of invocation
 * is through the MethodHandle API introduced in the JDK 1.7 version in order to handle methodical
 * invocation that pre-compiles the data required to execute the method using the traditional
 * reflection API, allowing the execution of the event handler to execute with the least amount of
 * computations possible in the JVM.
 *
 * <p>event handlers must contain one parameter for the method, which is the Event being interpreted
 * and handled. If the parameters are not setup to only have the event being handled, the container
 * will not be enabled. The handler will still be registered for debugging purposes.
 *
 * <p>An example event handler is as follows:
 *
 * <p><code>
 * EventHandler(ignoreCancelled = true, priority = 5)
 * private void on(ChatMessageEvent event) {
 * // Handle Event here.
 * }
 * </code>
 *
 * @author Jab
 */
public class EventHandlerContainer {

  private static MethodHandles.Lookup lookup = MethodHandles.lookup();

  private EventHandler annotation;
  private Class<?>[] methodParameters;
  private Method method;
  private MethodHandle methodHandle;
  private MethodType methodType;
  private Listener container;
  private Object[] methodArgumentsCache;
  private String methodName;
  private boolean isStatic;
  private boolean isEnabled;
  private Class<? extends Event> classEvent;
  private long timeCreated;

  /**
   * Main constructor.
   *
   * @param container The Object to identify with the event handler. This is typically the declaring
   *     class.
   * @param method The Method that is the event handler to invoke.
   * @param annotation The event handler Annotation that contains the information important to the
   *     functions of the event handler.
   */
  public EventHandlerContainer(Listener container, Method method, EventHandler annotation) {
    setTimeCreated(System.currentTimeMillis());
    setContainer(container);
    setMethod(method);
    setAnnotation(annotation);
    // Setup the MethodHandle.
    createMethodHandler();
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Method: \"").append(methodName);
    stringBuilder.append("\" container = \"").append(ClassUtil.getClassName(getContainer()));
    stringBuilder.append("\" static = \"").append(isStatic);
    Object[] parameters = getMethodParameters();
    if (parameters != null && parameters.length > 0) {
      stringBuilder.append("\" args =");
      for (int index = 0; index < parameters.length; index++) {
        Object param = parameters[index];
        if (param != null) {
          String name = ClassUtil.getClassName(param);
          stringBuilder.append(" (").append(index).append("): (").append(name).append(")");
        } else {
          stringBuilder.append(" (").append(index).append("): (NULL)");
        }
      }
    }
    return stringBuilder.toString();
  }

  /** Handles setting up the MethodHandle cache to invoke when executing an event handler. */
  private void createMethodHandler() {
    // In order to invoke the Method, the Method must be accessible, otherwise an
    // IllegalAccessException is thrown.
    method.setAccessible(true);
    // We need to make sure that we can identify the Method for any debugging purposes.
    methodName = method.getName();
    // Grab the parameters of the Method to make sure that the event handler is valid.
    methodParameters = method.getParameterTypes();
    // If there's no parameters or excess parameters, we cannot enable the event handler.
    if (methodParameters.length != 1) {
      return;
    }
    // Make sure that the first parameter given is an Event Object.
    if (!ClassUtil.isSubClass(methodParameters[0], Event.class)) {
      if (Settings.getInstance().isDebug()) {
        System.err.println("The EventHandler parameters are invalid: " + toString());
      }
      return;
    }
    // Grab the identifying Event to pass to the event handler.
    classEvent = (Class<? extends Event>) methodParameters[0];
    // Make note if the event handler is static.
    isStatic = Modifier.isStatic(method.getModifiers());
    // This will expressly note the parameters to pass to the EventHandle, and the type of
    // Class expected to return.
    methodType = MethodType.methodType(method.getReturnType(), methodParameters);
    try {
      // We will use the lookup factory to un-package and assemble the method into a
      // MethodHandle reference. This is similar to pointers in C / C++.
      methodHandle = lookup.unreflect(method);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    // If the event handler method is static, the parameter cache does not require an instance
    // of the declaring class or anonymous class to invoke.
    if (isStatic) {
      // In this case, the event handler will require only 1 parameter:
      // [1] -> Event
      methodArgumentsCache = new Object[1];
    }
    // We will setup the parameter cache for a non-static (invokeVirtual) method. These
    // methods are what is expected to be used as event handlers.
    else {
      // In this case, the event handler will require 2 parameters:
      // [0] -> self
      // [1] -> Event
      methodArgumentsCache = new Object[2];
      // Set the self instance as the Container.
      methodArgumentsCache[0] = getContainer();
    }
    // We are now good to start invoking the event handler. Let the EventManagerOld know this.
    setEnabled(true);
  }

  /**
   * Handles an Event. If the event handler is not enabled, then we return without attempting to
   * handle the Event.
   *
   * @param event The Event passed to handle.
   * @throws Throwable Thrown if the event handler fails to handle the Event, or the MethodHandle
   *     fails to invoke.
   */
  public void handleEvent(Event event) throws Throwable {
    // Make sure that the event handler is enabled before invoking the event handler.
    if (!isEnabled()) {
      return;
    }
    // If the Event is cancelled, and the event handler does not ignore this, do not handle
    // the Event.
    if (!this.ignoreCancelled()
        && event instanceof Cancellable
        && ((Cancellable) event).isCancelled()) {
      return;
    }
    // If the method is static, we do not need to reference the instance of the declaring class.
    if (isStatic()) {
      // The method is static, so we need to only pass the Event instance being handled.
      methodArgumentsCache[0] = event;
      // Finally, invoke the method.
      methodHandle.invokeWithArguments(methodArgumentsCache);
      // Clear the cache of references to the arguments given.
      methodArgumentsCache[0] = null;
    }
    // The method is dynamic, so we need to pass the instance of the declaring class.
    else {
      // The declaring class instance is already defined in the first index of the
      // parameter array. All we need to do is assign the Event instance being handled.
      methodArgumentsCache[1] = event;
      // Finally, invoke the method.
      methodHandle.invokeWithArguments(methodArgumentsCache);
      // Clear the cache of references to the arguments given.
      methodArgumentsCache[1] = null;
    }
  }

  /** @return Returns the Method that is the event handler being invocated when handling Events. */
  public Method getMethod() {
    return this.method;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Method that is the event handler being invocated when handling Events.
   *
   * @param method The Method to set.
   */
  private void setMethod(Method method) {
    this.method = method;
  }

  /** @return Returns the Annotation instance storing the meta-data for the event handler. */
  public EventHandler getAnnotation() {
    return this.annotation;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Annotation instance for the event handler.
   *
   * @param annotation The Annotation instance to set.
   */
  private void setAnnotation(EventHandler annotation) {
    this.annotation = annotation;
  }

  /** @return Returns the container for the Method to invoke. */
  public Listener getContainer() {
    return this.container;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the container for the Method to invoke.
   *
   * @param container The declaring class instance to set.
   */
  private void setContainer(Listener container) {
    this.container = container;
  }

  /**
   * @return Returns the priority index for the event handler. The higher the number, the more
   *     priority the event handler has over other event handlers, and will be invoked first.
   */
  public int getPriority() {
    return getAnnotation().priority();
  }

  /**
   * @return Returns true if the event handler handles the Event, even if the event is cancelled by
   *     a prior event handler.
   */
  public boolean ignoreCancelled() {
    return getAnnotation().ignoreCancelled();
  }

  /** @return Returns the String ID (if defined), for the event handler. */
  public String getId() {
    return getAnnotation().id();
  }

  /** @return Returns true if the event handler is declared as a static method. */
  public boolean isStatic() {
    return this.isStatic;
  }

  /** @return Returns the Parameters for the Method. */
  public Object[] getMethodParameters() {
    return this.methodParameters;
  }

  /**
   * @return Returns true if the event handler is enabled. When this is disabled, Events will not be
   *     passed and handled.
   */
  public boolean isEnabled() {
    return this.isEnabled;
  }

  /**
   * Sets the event handler as enabled. When this is disabled. Events will not be passed and
   * handled.
   *
   * @param flag The status to set.
   */
  public void setEnabled(boolean flag) {
    this.isEnabled = flag;
  }

  /** @return Returns the Class of the Event to handle. */
  public Class<? extends Event> getEventClass() {
    return this.classEvent;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Class of the Event to handle.
   *
   * @param classEvent The Class to set.
   */
  private void setEventClass(Class<? extends Event> classEvent) {
    this.classEvent = classEvent;
  }

  /**
   * @return Returns the UNIX Timestamp for the container to identify when the contained was
   *     created.
   */
  public long getTimeCreated() {
    return this.timeCreated;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the UNIX Timestamp for the container to identify when the container was created.
   *
   * @param timeCreated The UNIX Timestamp to set.
   */
  private void setTimeCreated(long timeCreated) {
    this.timeCreated = timeCreated;
  }
}
