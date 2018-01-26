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

import java.io.File;

import sledgehammer.language.LanguagePackage;
import sledgehammer.plugin.Module;
import zombie.sledgehammer.module.vanilla.VanillaCommandListener;

/**
 * Module to handle Vanilla data and operations for the Core Plug-in.
 *
 * @author Jab
 */
public class ModuleVanilla extends Module {

  /** The CommandListener instance for the Module. */
  private VanillaCommandListener commandListener;

  private VanillaEventListener eventListener;

  private LanguagePackage languagePackage;

  @Override
  public void onLoad() {
    loadLanguagePackage();
    commandListener = new VanillaCommandListener(this);
    eventListener = new VanillaEventListener();
    register(eventListener);
  }

  @Override
  public void onUnload() {
    unregister(eventListener);
  }

  private void loadLanguagePackage() {
    File langDir = getLanguageDirectory();
    boolean override = !isLangOverriden();
    saveResourceAs("lang/vanilla_en.yml", new File(langDir, "vanilla_en.yml"), override);
    languagePackage = new LanguagePackage(getLanguageDirectory(), "vanilla");
  }

  /** @return Returns the VanillaCommandListener for the Vanilla Module. */
  public VanillaCommandListener getCommandListener() {
    return commandListener;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the VanillaCommandListener for the Vanilla Module.
   *
   * @param listener The VanillaCommandListener to set.
   */
  public void setCommandListener(VanillaCommandListener listener) {
    this.commandListener = listener;
  }

  public LanguagePackage getLanguagePackage() {
    return languagePackage;
  }
}
