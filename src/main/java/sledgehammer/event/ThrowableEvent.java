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

package sledgehammer.event;

import sledgehammer.event.Event;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Event to process and handle thrown Throwables for the Sledgehammer engine.
 *
 * @author Jab
 */
public class ThrowableEvent extends Event {

  private Throwable throwable;

  /**
   * Main constructor.
   *
   * @param throwable The Throwable that was thrown.
   */
  public ThrowableEvent(Throwable throwable) {
    setThrowable(throwable);
  }

  /** @return Returns a PrintWriter render of the Throwable. */
  public String printStackTrace() {
    return getStackTrace(getThrowable());
  }

  /** @return Returns the Throwable Object that was thrown. */
  public Throwable getThrowable() {
    return this.throwable;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Throwable for the Event.
   *
   * @param throwable The Throwable to set.
   */
  private void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  /**
   * Prints a formal StackTrace into a String.
   *
   * @param aThrowable The Throwable to print.
   * @return Returns a printed String of the StackTrace.
   */
  public static String getStackTrace(Throwable aThrowable) {
    Writer stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    aThrowable.printStackTrace(printWriter);
    String stackTrace = stringWriter.toString();
    try {
      stringWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stackTrace;
  }
}
