package se.krka.kahlua.j2se;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import zombie.GameWindow;
import zombie.Lua.LuaManager;
import zombie.Lua.LuaManager.GlobalObject;
import zombie.core.Core;
import zombie.ui.UIManager;

public class KahluaTableImpl implements KahluaTable {
  private final Map delegate;
  private KahluaTable metatable;
  private KahluaTable reloadReplace;
  private static final byte SBYT_NO_SAVE = -1;
  private static final byte SBYT_STRING = 0;
  private static final byte SBYT_DOUBLE = 1;
  private static final byte SBYT_TABLE = 2;
  private static final byte SBYT_BOOLEAN = 3;

  public KahluaTableImpl(Map delegate) {
    this.delegate = delegate;
  }

  public void setMetatable(KahluaTable metatable) {
    this.metatable = metatable;
  }

  public KahluaTable getMetatable() {
    return this.metatable;
  }

  public int size() {
    return this.delegate.size();
  }

  public void rawset(Object key, Object value) {
    if (this.reloadReplace != null) {
      this.reloadReplace.rawset(key, value);
    }

    Object lastVal = null;
    if (Core.bDebug
        && LuaManager.thread != null
        && LuaManager.thread.hasDataBreakpoint(this, key)) {
      lastVal = this.rawget(key);
    }

    if (value == null) {
      if (Core.bDebug
          && LuaManager.thread != null
          && LuaManager.thread.hasDataBreakpoint(this, key)
          && lastVal != null) {
        UIManager.debugBreakpoint(LuaManager.thread.currentfile, (long) LuaManager.thread.lastLine);
      }

      this.delegate.remove(key);
    } else {
      if (Core.bDebug
          && LuaManager.thread != null
          && LuaManager.thread.hasDataBreakpoint(this, key)
          && !value.equals(lastVal)) {
        int a = GlobalObject.getCurrentCoroutine().currentCallFrame().pc;
        if (a < 0) {
          a = 0;
        }

        UIManager.debugBreakpoint(
            LuaManager.thread.currentfile,
            (long)
                (GlobalObject.getCurrentCoroutine().currentCallFrame().closure.prototype.lines[a]
                    - 1));
      }

      this.delegate.put(key, value);
    }
  }

  public Object rawget(Object key) {
    if (this.reloadReplace != null) {
      return this.reloadReplace.rawget(key);
    } else if (key == null) {
      return null;
    } else {
      if (Core.bDebug
          && LuaManager.thread != null
          && LuaManager.thread.hasReadDataBreakpoint(this, key)) {
        int a = GlobalObject.getCurrentCoroutine().currentCallFrame().pc;
        if (a < 0) {
          a = 0;
        }

        UIManager.debugBreakpoint(
            LuaManager.thread.currentfile,
            (long)
                (GlobalObject.getCurrentCoroutine().currentCallFrame().closure.prototype.lines[a]
                    - 1));
      }

      return !this.delegate.containsKey(key) && this.metatable != null
          ? this.metatable.rawget(key)
          : this.delegate.get(key);
    }
  }

  public void rawset(int key, Object value) {
    this.rawset(KahluaUtil.toDouble((long) key), value);
  }

  public String rawgetStr(Object key) {
    return (String) this.rawget(key);
  }

  public int rawgetInt(Object key) {
    return this.rawget(key) != null ? ((Double) this.rawget(key)).intValue() : -1;
  }

  public boolean rawgetBool(Object key) {
    return this.rawget(key) != null ? ((Boolean) this.rawget(key)).booleanValue() : false;
  }

  public Object rawget(int key) {
    return this.rawget(KahluaUtil.toDouble((long) key));
  }

  public int len() {
    return KahluaUtil.len(this, 0, 2 * this.delegate.size());
  }

  public KahluaTableIterator iterator() {
    final Iterator iterator = this.delegate.entrySet().iterator();
    return new KahluaTableIterator() {
      private Object curKey;
      private Object curValue;

      public int call(LuaCallFrame callFrame, int nArguments) {
        return this.advance() ? callFrame.push(this.getKey(), this.getValue()) : 0;
      }

      public boolean advance() {
        if (iterator.hasNext()) {
          Entry value = (Entry) iterator.next();
          this.curKey = value.getKey();
          this.curValue = value.getValue();
          return true;
        } else {
          this.curKey = null;
          this.curValue = null;
          return false;
        }
      }

      public Object getKey() {
        return this.curKey;
      }

      public Object getValue() {
        return this.curValue;
      }
    };
  }

  public boolean isEmpty() {
    return this.delegate.isEmpty();
  }

  public void wipe() {
    this.delegate.clear();
  }

  public String toString() {
    return "table 0x" + System.identityHashCode(this);
  }

  public void save(ByteBuffer output) {
    KahluaTableIterator it = this.iterator();
    int count = 0;

    while (it.advance()) {
      if (canSave(it.getKey(), it.getValue())) {
        ++count;
      }
    }

    it = this.iterator();
    output.putInt(count);

    while (it.advance()) {
      byte keyByte = getKeyByte(it.getKey());
      byte valueByte = getValueByte(it.getValue());
      if (keyByte != -1 && valueByte != -1) {
        this.save(output, keyByte, it.getKey());
        this.save(output, valueByte, it.getValue());
      }
    }
  }

  private void save(ByteBuffer output, byte sbyt, Object o) throws RuntimeException {
    output.put(sbyt);
    if (sbyt == 0) {
      GameWindow.WriteString(output, (String) o);
    } else if (sbyt == 1) {
      output.putDouble(((Double) o).doubleValue());
    } else if (sbyt == 3) {
      output.put((byte) (((Boolean) o).booleanValue() ? 1 : 0));
    } else {
      if (sbyt != 2) {
        throw new RuntimeException("invalid lua table type " + sbyt);
      }

      ((KahluaTableImpl) o).save(output);
    }
  }

  public void save(DataOutputStream output) throws IOException {
    KahluaTableIterator it = this.iterator();
    int count = 0;

    while (it.advance()) {
      if (canSave(it.getKey(), it.getValue())) {
        ++count;
      }
    }

    it = this.iterator();
    output.writeInt(count);

    while (it.advance()) {
      byte keyByte = getKeyByte(it.getKey());
      byte valueByte = getValueByte(it.getValue());
      if (keyByte != -1 && valueByte != -1) {
        this.save(output, keyByte, it.getKey());
        this.save(output, valueByte, it.getValue());
      }
    }
  }

  private void save(DataOutputStream output, byte sbyt, Object o)
      throws IOException, RuntimeException {
    output.writeByte(sbyt);
    if (sbyt == 0) {
      GameWindow.WriteString(output, (String) o);
    } else if (sbyt == 1) {
      output.writeDouble(((Double) o).doubleValue());
    } else if (sbyt == 3) {
      output.writeByte(((Boolean) o).booleanValue() ? 1 : 0);
    } else {
      if (sbyt != 2) {
        throw new RuntimeException("invalid lua table type " + sbyt);
      }

      ((KahluaTableImpl) o).save(output);
    }
  }

  public void load(ByteBuffer input, int WorldVersion) {
    int count = input.getInt();
    this.wipe();
    int n;
    byte valueByte;
    if (WorldVersion >= 25) {
      for (n = 0; n < count; ++n) {
        valueByte = input.get();
        Object key = this.load(input, WorldVersion, valueByte);
        byte value = input.get();
        Object value1 = this.load(input, WorldVersion, value);
        this.rawset(key, value1);
      }
    } else {
      for (n = 0; n < count; ++n) {
        valueByte = input.get();
        String var9 = GameWindow.ReadString(input);
        Object var10 = this.load(input, WorldVersion, valueByte);
        this.rawset(var9, var10);
      }
    }
  }

  public Object load(ByteBuffer input, int WorldVersion, byte sbyt) throws RuntimeException {
    if (sbyt == 0) {
      return GameWindow.ReadString(input);
    } else if (sbyt == 1) {
      return Double.valueOf(input.getDouble());
    } else if (sbyt == 3) {
      return Boolean.valueOf(input.get() == 1);
    } else if (sbyt == 2) {
      KahluaTableImpl v = (KahluaTableImpl) LuaManager.platform.newTable();
      v.load(input, WorldVersion);
      return v;
    } else {
      throw new RuntimeException("invalid lua table type " + sbyt);
    }
  }

  public void load(DataInputStream input, int WorldVersion) throws IOException {
    int count = input.readInt();
    int n;
    byte valueByte;
    if (WorldVersion >= 25) {
      for (n = 0; n < count; ++n) {
        valueByte = input.readByte();
        Object key = this.load(input, WorldVersion, valueByte);
        byte value = input.readByte();
        Object value1 = this.load(input, WorldVersion, value);
        this.rawset(key, value1);
      }
    } else {
      for (n = 0; n < count; ++n) {
        valueByte = input.readByte();
        String var9 = GameWindow.ReadString(input);
        Object var10 = this.load(input, WorldVersion, valueByte);
        this.rawset(var9, var10);
      }
    }
  }

  public Object load(DataInputStream input, int WorldVersion, byte sbyt)
      throws IOException, RuntimeException {
    if (sbyt == 0) {
      return GameWindow.ReadString(input);
    } else if (sbyt == 1) {
      return Double.valueOf(input.readDouble());
    } else if (sbyt == 3) {
      return Boolean.valueOf(input.readByte() == 1);
    } else if (sbyt == 2) {
      KahluaTableImpl v = (KahluaTableImpl) LuaManager.platform.newTable();
      v.load(input, WorldVersion);
      return v;
    } else {
      throw new RuntimeException("invalid lua table type " + sbyt);
    }
  }

  public String getString(String string) {
    return (String) this.rawget(string);
  }

  public KahluaTableImpl getRewriteTable() {
    return (KahluaTableImpl) this.reloadReplace;
  }

  public void setRewriteTable(Object value) {
    for (KahluaTableImpl toChange = this;
        toChange != null;
        toChange = toChange.getRewriteTable()) {;
    }

    this.reloadReplace = (KahluaTableImpl) value;
  }

  private static byte getKeyByte(Object o) {
    return (byte) (o instanceof String ? 0 : (o instanceof Double ? 1 : -1));
  }

  private static byte getValueByte(Object o) {
    return (byte)
        (o instanceof String
            ? 0
            : (o instanceof Double
                ? 1
                : (o instanceof Boolean ? 3 : (o instanceof KahluaTableImpl ? 2 : -1))));
  }

  public static boolean canSave(Object key, Object value) {
    return getKeyByte(key) != -1 && getValueByte(value) != -1;
  }
}
