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

package sledgehammer.module.chat;

import sledgehammer.Settings;
import sledgehammer.event.core.ThrowableEvent;
import sledgehammer.util.YamlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChatSettings {

  private ModuleChat module;
  private Map map;
  private File file;
  private List<ChannelDefinition> listChannelDefinitions;

  ChatSettings(ModuleChat module) {
    setModule(module);
  }

  void load() {
    listChannelDefinitions = new LinkedList<>();
    file = new File(module.getModuleDirectory(), "config.yml");
    if (!file.exists()) {
      return;
    }
    try {
      FileInputStream fis = new FileInputStream(file);
      map = YamlUtil.getYaml().load(fis);
      fis.close();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    if (map.isEmpty()) {
      return;
    }
    Map channels = (Map) map.get("channels");
    if (channels != null) {
      try {
        loadChatChannels(channels);
      } catch (Exception e) {
        System.err.println("Failed to load ChannelDefinitions.");
        if (Settings.getInstance().isDebug()) {
          System.err.println(ThrowableEvent.getStackTrace(e));
        }
      }
    }
  }

  private void loadChatChannels(Map channels) {
    for (Object o : channels.keySet()) {
      try {
        String channelName = (String) o.toString();
        Map mapChannel = (Map) channels.get(o);
        ChannelDefinition channelDefinition = loadChatChannel(channelName, mapChannel);
        listChannelDefinitions.add(channelDefinition);
      } catch (Exception e) {
        System.err.println("Failed to load ChannelDefinition.");
        if (Settings.getInstance().isDebug()) {
          System.err.println(ThrowableEvent.getStackTrace(e));
        }
      }
    }
  }

  private ChannelDefinition loadChatChannel(String channelName, Map mapChannel) {
    String permission = (String) mapChannel.get("permission");
    permission = permission != null ? permission : "*";
    Boolean isPublic = (Boolean) mapChannel.get("public");
    isPublic = isPublic != null ? isPublic : false;
    Boolean isGlobal = (Boolean) mapChannel.get("global");
    isGlobal = isGlobal != null ? isGlobal : false;
    Boolean saveHistory = (Boolean) mapChannel.get("history");
    saveHistory = saveHistory != null ? saveHistory : false;
    Boolean canSpeak = (Boolean) mapChannel.get("speak");
    canSpeak = canSpeak != null ? canSpeak : true;
    return new ChannelDefinition(
        channelName, permission, isPublic, isGlobal, saveHistory, canSpeak);
  }

  List<ChannelDefinition> getChannelDefinitions() {
    return this.listChannelDefinitions;
  }

  ModuleChat getModule() {
    return this.module;
  }

  private void setModule(ModuleChat module) {
    this.module = module;
  }
}

class ChannelDefinition {
  private String permission;
  private String name;
  private boolean isPublic;
  private boolean isGlobal;
  private boolean saveHistory;
  private boolean canSpeak;

  ChannelDefinition(
      String name,
      String permission,
      boolean isPublic,
      boolean isGlobal,
      boolean saveHistory,
      boolean canSpeak) {
    setName(name);
    setPermission(permission);
    setPublic(isPublic);
    setGlobal(isGlobal);
    setHistory(saveHistory);
    setSpeak(canSpeak);
  }

  String getName() {
    return this.name;
  }

  private void setName(String name) {
    this.name = name;
  }

  boolean isPublic() {
    return this.isPublic;
  }

  private void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  boolean isGlobal() {
    return this.isGlobal;
  }

  private void setGlobal(boolean isGlobal) {
    this.isGlobal = isGlobal;
  }

  public boolean saveHistory() {
    return this.saveHistory;
  }

  void setHistory(boolean saveHistory) {
    this.saveHistory = saveHistory;
  }

  boolean canSpeak() {
    return this.canSpeak;
  }

  private void setSpeak(boolean canSpeak) {
    this.canSpeak = canSpeak;
  }

  String getPermission() {
    return this.permission;
  }

  private void setPermission(String permission) {
    this.permission = permission;
  }
}
