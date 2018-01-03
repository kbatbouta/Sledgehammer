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

package se.krka.kahlua.j2se;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.j2se.MathLib;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.stdlib.RandomLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.test.UserdataArray;
import se.krka.kahlua.threading.BlockingKahluaThread;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import zombie.core.Collections.NonBlockingHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class J2SEPlatform implements Platform {
	private static J2SEPlatform INSTANCE = new J2SEPlatform();

	public static J2SEPlatform getInstance() {
		return INSTANCE;
	}

	public double pow(double x, double y) {
		return Math.pow(x, y);
	}

	public KahluaTable newTable() {
		return new KahluaTableImpl(new NonBlockingHashMap());
	}

	public KahluaTable newEnvironment() {
		KahluaTable env = this.newTable();
		this.setupEnvironment(env);
		return env;
	}

	public void setupEnvironment(KahluaTable env) {
		env.wipe();
		env.rawset("_G", env);
		env.rawset("_VERSION", "Kahlua kahlua.major.kahlua.minor.kahlua.fix for Lua lua.version (J2SE)");
		MathLib.register(this, env);
		BaseLib.register(env);
		RandomLib.register(this, env);
		UserdataArray.register(this, env);
		StringLib.register(this, env);
		CoroutineLib.register(this, env);
		OsLib.register(this, env);
		TableLib.register(this, env);
		LuaCompiler.register(env);
		KahluaThread workerThread = this.setupWorkerThread(env);
		KahluaUtil.setupLibrary(env, workerThread, "natives/stdlib");

		try {
			LuaClosure e = LuaCompiler.loadis(new FileInputStream(new File("natives/serialize.lua")), "natives/serialize.lua", env);
			workerThread.call(e, (Object) null, (Object) null, (Object) null);
		} catch (IOException arg3) {
			throw new RuntimeException(arg3);
		}
	}

	private KahluaThread setupWorkerThread(KahluaTable env) {
		BlockingKahluaThread thread = new BlockingKahluaThread(this, env);
		KahluaUtil.setWorkerThread(env, thread);
		return thread;
	}
}