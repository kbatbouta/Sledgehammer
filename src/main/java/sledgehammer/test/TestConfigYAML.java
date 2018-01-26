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

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import sledgehammer.util.YamlUtil;

/**
 * This is a test class for verifying the YAML for the config.yml file.
 *
 * @author Jab
 */
@SuppressWarnings("rawtypes")
public class TestConfigYAML {

  Map data;

  public void run() {
    try {
      FileInputStream fis;
      fis = new FileInputStream(new File("config.yml"));
      data = YamlUtil.getYaml().load(fis);
      System.out.println(data);
      fis.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new TestConfigYAML().run();
  }
}
