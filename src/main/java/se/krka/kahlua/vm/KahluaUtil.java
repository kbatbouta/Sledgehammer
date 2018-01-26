/*
 * https://github.com/krka/kahlua2
 *
 * =License=
 * Kahlua is distributed under the MIT licence which is the same as standard Lua
 * which means you can
 * pretty much use it in any way you want.
 * However, I would very much appreciate bug reports, bug fixes, optimizations
 * or simply any good idea that might improve Kahlua.
 */

package se.krka.kahlua.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import se.krka.kahlua.integration.expose.LuaJavaInvoker;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.vm.Coroutine;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import se.krka.kahlua.vm.Prototype;
import zombie.Lua.LuaManager;
import zombie.core.Core;
import zombie.core.textures.Texture;
import zombie.debug.DebugLog;
import zombie.ui.UIManager;

@SuppressWarnings({"unused"})
public class KahluaUtil {
  private static final Object WORKER_THREAD_KEY = new Object();
  private static final String TYPE_NIL = "nil";
  private static final String TYPE_STRING = "string";
  private static final String TYPE_NUMBER = "number";
  private static final String TYPE_BOOLEAN = "boolean";
  private static final String TYPE_FUNCTION = "function";
  private static final String TYPE_TABLE = "table";
  private static final String TYPE_COROUTINE = "coroutine";
  private static final String TYPE_USERDATA = "userdata";
  static Double[] Intlookup = new Double[10000];

  public static double fromDouble(Object o) {
    return ((Double) o).doubleValue();
  }

  public static Double toDouble(double d) {
    return d + 5000.0D < 10000.0D && d + 5000.0D >= 0.0D && (double) ((int) d) == d
        ? Intlookup[(int) d + 5000]
        : new Double(d);
  }

  public static Double toDouble(long d) {
    return toDouble((double) d);
  }

  public static Boolean toBoolean(boolean b) {
    return b ? Boolean.TRUE : Boolean.FALSE;
  }

  public static boolean boolEval(Object o) {
    return o != null && o != Boolean.FALSE;
  }

  public static LuaClosure loadByteCodeFromResource(String name, KahluaTable environment) {
    InputStream stream = null;
    try {
      stream = new FileInputStream(new File(name + ".lbc"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (stream == null) {
      return null;
    } else {
      try {
        return Prototype.loadByteCode(stream, environment);
      } catch (IOException arg3) {
        throw new RuntimeException(arg3.getMessage());
      }
    }
  }

  public static void luaAssert(boolean b, String msg) {
    if (!b) {
      fail(msg);
    }
  }

  public static void fail(String msg) {
    if (Core.bDebug && UIManager.defaultthread == LuaManager.thread) {
      DebugLog.log(msg);
      UIManager.debugBreakpoint(
          LuaManager.thread.currentfile, (long) (LuaManager.thread.currentLine - 1));
    }

    throw new RuntimeException(msg);
  }

  public static double round(double x) {
    if (x < 0.0D) {
      return -round(-x);
    } else {
      x += 0.5D;
      double x2 = Math.floor(x);
      return x2 == x ? x2 - (double) ((long) x2 & 1L) : x2;
    }
  }

  public static long ipow(long base, int exponent) {
    if (exponent <= 0) {
      return 1L;
    } else {
      long b = 1L;
      b = (exponent & 1) != 0 ? base : 1L;

      for (exponent >>= 1; exponent != 0; exponent >>= 1) {
        base *= base;
        if ((exponent & 1) != 0) {
          b *= base;
        }
      }

      return b;
    }
  }

  public static boolean isNegative(double vDouble) {
    return Double.doubleToLongBits(vDouble) < 0L;
  }

  public static KahluaTable getClassMetatables(Platform platform, KahluaTable env) {
    return getOrCreateTable(platform, env, "__classmetatables");
  }

  public static KahluaThread getWorkerThread(Platform platform, KahluaTable env) {
    Object workerThread = env.rawget(WORKER_THREAD_KEY);
    if (workerThread == null) {
      workerThread = new KahluaThread(platform, env);
      env.rawset(WORKER_THREAD_KEY, workerThread);
    }

    return (KahluaThread) workerThread;
  }

  public static void setWorkerThread(KahluaTable env, KahluaThread thread) {
    env.rawset(WORKER_THREAD_KEY, thread);
  }

  public static KahluaTable getOrCreateTable(Platform platform, KahluaTable env, String name) {
    Object t = env.rawget(name);
    if (t == null || !(t instanceof KahluaTable)) {
      t = platform.newTable();
      env.rawset(name, t);
    }

    return (KahluaTable) t;
  }

  public static void setupLibrary(KahluaTable env, KahluaThread workerThread, String library) {
    LuaClosure closure = loadByteCodeFromResource(library, env);
    if (closure == null) {
      fail("Could not load " + library + ".lbc");
    }

    workerThread.call(closure, (Object) null, (Object) null, (Object) null);
  }

  public static String numberToString(Double num) {
    if (num.isNaN()) {
      return "nan";
    } else if (num.isInfinite()) {
      return isNegative(num.doubleValue()) ? "-inf" : "inf";
    } else {
      double n = num.doubleValue();
      return Math.floor(n) == n && Math.abs(n) < 1.0E14D
          ? String.valueOf(num.longValue())
          : num.toString();
    }
  }

  public static String type(Object o) {
    return o == null
        ? "nil"
        : (o instanceof String
            ? "string"
            : (o instanceof Double
                ? "number"
                : (o instanceof Boolean
                    ? "boolean"
                    : (!(o instanceof JavaFunction) && !(o instanceof LuaClosure)
                        ? (o instanceof KahluaTable
                            ? "table"
                            : (o instanceof Coroutine ? "coroutine" : "userdata"))
                        : "function"))));
  }

  public static String tostring(Object o, KahluaThread thread) {
    if (o == null) {
      return "nil";
    } else if (o instanceof String) {
      return (String) o;
    } else if (o instanceof Double) {
      return rawTostring(o);
    } else if (o instanceof Boolean) {
      return o == Boolean.TRUE ? "true" : "false";
    } else if (o instanceof LuaClosure) {
      return "closure 0x" + System.identityHashCode(o);
    } else if (o instanceof JavaFunction) {
      return "function 0x" + System.identityHashCode(o);
    } else {
      if (thread != null) {
        Object tostringFun = thread.getMetaOp(o, "__tostring");
        if (tostringFun != null) {
          String res = (String) thread.call(tostringFun, o, (Object) null, (Object) null);
          return res;
        }
      }

      return o.toString();
    }
  }

  public static Double tonumber(String s) {
    return tonumber(s, 10);
  }

  public static Double tonumber(String s, int radix) {
    if (radix >= 2 && radix <= 36) {
      try {
        return radix == 10 ? Double.valueOf(s) : toDouble((long) Integer.parseInt(s, radix));
      } catch (NumberFormatException arg2) {
        s = s.toLowerCase();
        return s.endsWith("nan")
            ? toDouble(Double.NaN)
            : (s.endsWith("inf")
                ? (s.charAt(0) == 45
                    ? toDouble(Double.NEGATIVE_INFINITY)
                    : toDouble(Double.POSITIVE_INFINITY))
                : null);
      }
    } else {
      throw new RuntimeException("base out of range");
    }
  }

  public static String rawTostring(Object o) {
    return o instanceof String
        ? (String) o
        : (o instanceof Double ? numberToString((Double) o) : null);
  }

  public static String rawTostring2(Object o) {
    if (o instanceof String) {
      return "\"" + (String) o + "\"";
    } else if (o instanceof Texture) {
      return "Texture: \"" + ((Texture) o).getName() + "\"";
    } else if (o instanceof Double) {
      return numberToString((Double) o);
    } else if (o instanceof LuaClosure) {
      LuaClosure arg5 = (LuaClosure) o;
      return arg5.toString2(0);
    } else if (o instanceof LuaCallFrame) {
      LuaCallFrame arg4 = (LuaCallFrame) o;
      return arg4.toString2();
    } else if (o instanceof LuaJavaInvoker) {
      if (o.toString().equals("breakpoint")) {
        return null;
      } else {
        LuaJavaInvoker inv = (LuaJavaInvoker) o;
        MethodDebugInformation ooo = inv.getMethodDebugData();
        String params = "";

        for (int n = 0; n < ooo.getParameters().size(); ++n) {
          if (ooo.getParameters().get(n) != null) {
            params = params + ooo.getParameters().get(n);
          }
        }

        return "Java: " + ooo.getReturnType() + " " + o.toString() + "(" + params + ")";
      }
    } else {
      return o != null ? o.toString() : null;
    }
  }

  public static Double rawTonumber(Object o) {
    return o instanceof Double ? (Double) o : (o instanceof String ? tonumber((String) o) : null);
  }

  public static String getStringArg(LuaCallFrame callFrame, int n, String function) {
    Object o = getArg(callFrame, n, function);
    String res = rawTostring(o);
    if (res == null) {
      fail(n, function, "string", type(res));
    }

    return res;
  }

  public static String getOptionalStringArg(LuaCallFrame callFrame, int n) {
    Object o = getOptionalArg(callFrame, n);
    return rawTostring(o);
  }

  public static Double getNumberArg(LuaCallFrame callFrame, int n, String function) {
    Object o = getArg(callFrame, n, function);
    Double res = rawTonumber(o);
    if (res == null) {
      fail(n, function, "double", type(res));
    }

    return res;
  }

  public static Double getOptionalNumberArg(LuaCallFrame callFrame, int n) {
    Object o = getOptionalArg(callFrame, n);
    return rawTonumber(o);
  }

  private static void fail(int n, String function, String wantedType, String gotten) {
    throw new RuntimeException(
        "bad argument #"
            + n
            + " to \'"
            + function
            + "\' ("
            + wantedType
            + " expected, got "
            + gotten
            + ")");
  }

  public static void assertArgNotNull(Object o, int n, String type, String function) {
    if (o == null) {
      fail(n, function, type, "null");
    }
  }

  public static Object getOptionalArg(LuaCallFrame callFrame, int n) {
    int top = callFrame.getTop();
    int index = n - 1;
    return index >= top ? null : callFrame.get(n - 1);
  }

  public static Object getArg(LuaCallFrame callFrame, int n, String function) {
    Object res = getOptionalArg(callFrame, n);
    if (res == null) {
      throw new RuntimeException("missing argument #" + n + "to \'" + function + "\'");
    } else {
      return res;
    }
  }

  public static int len(KahluaTable kahluaTable, int low, int high) {
    while (low < high) {
      int middle = high + low + 1 >> 1;
      Object value = kahluaTable.rawget(middle);
      if (value == null) {
        high = middle - 1;
      } else {
        low = middle;
      }
    }

    while (kahluaTable.rawget(low + 1) != null) {
      ++low;
    }

    return low;
  }

  public static double getDoubleArg(LuaCallFrame callFrame, int i, String name) {
    return getNumberArg(callFrame, i, name).doubleValue();
  }

  static {
    for (int n = 0; n < 10000; ++n) {
      Intlookup[n] = new Double((double) (n - 5000));
    }
  }
}
