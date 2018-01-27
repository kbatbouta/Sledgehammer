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

package sledgehammer.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtil {

  public static boolean isSubClass(Class subClass, Class superClass) {
    boolean returned = false;
    Class c = subClass;
    while ((c = c.getSuperclass()) != null) {
      if (superClass.equals(c)) {
        returned = true;
        break;
      }
    }
    return returned;
  }

  public static boolean isClass(Class<?> class1, Class<?> class2) {
    return class1.equals(class2);
  }

  public static Method[] getAllDeclaredMethods(Class clazz) {
    List<Method> listDeclaredMethods = new ArrayList<>();
    Class c = clazz;
    do {
      listDeclaredMethods.addAll(Arrays.asList(c.getDeclaredMethods()));
    } while ((c = c.getSuperclass()) != null);
    Method[] m = new Method[listDeclaredMethods.size()];
    for (int index = 0; index < listDeclaredMethods.size(); index++) {
      m[index] = listDeclaredMethods.get(index);
    }
    return m;
  }

  public static String getClassName(Object o) {
    Class c = o instanceof Class ? (Class) o : o.getClass();
    String name = c.toString();
    if (name.contains(".")) {
      String[] split = c.toString().split("\\.");
      return split[split.length - 1];
    } else {
      return name;
    }
  }
}
