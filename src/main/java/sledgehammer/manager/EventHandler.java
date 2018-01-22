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

package sledgehammer.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    boolean ignoreCancelled() default false;
    int eventPriority() default 0;
    String moduleId() default "";
}

/*
Object x, y; String s; int i;
MethodType mt; MethodHandle mh;
MethodHandles.Lookup lookup = MethodHandles.lookup();
// mt is (char,char)String
mt = MethodType.methodType(String.class, char.class, char.class);
mh = lookup.findVirtual(String.class, "replace", mt);
s = (String) mh.invokeExact("daddy",'d','n');
// invokeExact(Ljava/lang/String;CC)Ljava/lang/String;
assertEquals(s, "nanny");
 */