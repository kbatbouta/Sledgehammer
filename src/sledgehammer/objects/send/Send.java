package sledgehammer.objects.send;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public abstract class Send extends LuaTable {

	public Send(String name) {
		super(name);
	}

	// Server authored only.
	public void onLoad(KahluaTable table) {}
}
