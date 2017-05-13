package sledgehammer.objects.send;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public abstract class Send extends LuaTable {

	private String module;
	
	public Send(String module, String command) {
		super(command);
		setModule(module);
	}
	
	private void setModule(String module) {
		this.module = module;
	}

	public String getModule() {
		return this.module;
	}
	
	public String getCommand() {
		return getName();
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + ": Module=" + getModule() + "; Command=" + getCommand() + ";";
	}
	
	// Server authored only.
	public void onLoad(KahluaTable table) {}
}
