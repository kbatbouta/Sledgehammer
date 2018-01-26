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

package sledgehammer.test;

import java.util.Scanner;

import sledgehammer.SledgeHammer;
import sledgehammer.plugin.Module;
import sledgehammer.util.Printable;
import zombie.core.Core;

/**
 * Class designed to handle common operations for testing Modules.
 *
 * @param <M> The Module being tested.
 * @author Jab
 */
public abstract class TestModule<M extends Module> extends Printable {

  /** The Module being tested. */
  private M module;

  private Scanner scanner = new Scanner(System.in);

  /** Initializes SledgeHammer operations in a debug setting. */
  public void initializeSledgehammer() {
    Core.GameSaveWorld = "servertest";
    SledgeHammer.instance = new SledgeHammer(true);
    SledgeHammer.instance.init();
    M module = createModule();
    if (module != null) {
      setModule(module);
      // SledgeHammer.instance.getPluginManager().registerModule(getModule());
    }
    SledgeHammer.instance.getPluginManager().onLoad(true);
    SledgeHammer.instance.start();
  }

  public void runTest() {
    run();
    SledgeHammer.instance.stop();
  }

  /**
   * Pauses the execution of the test. Often used to observe data-sets affected during the course of
   * the test.
   */
  public void pause() {
    println("Press ENTER to continue the test.");
    do {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } while (!scanner.hasNextLine());
    scanner.nextLine();
  }

  /** @return Returns the Module being tested. */
  public M getModule() {
    return this.module;
  }

  /**
   * (Protected Method)
   *
   * <p>Sets the Module being tested.
   *
   * @param module The Module to set.
   */
  protected void setModule(M module) {
    this.module = module;
  }

  /**
   * Use this method to create and assign the Module being tested.
   *
   * @return The Module instance to be tested.
   */
  public abstract M createModule();

  /** This method executes the test. */
  public abstract void run();
}
