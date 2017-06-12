package sledgehammer.objects.chat;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;

public class ChannelProperties extends LuaTable {
	
	public static final String DEFAULT_CONTEXT = "sledgehammer.chat.channel";
	
	private String _name = "Untitled_Channel";
	private String _context = "";
	private String _description = "";
	private boolean _public = true;
	private boolean _speak = true;
	private boolean _history = true;
	private boolean _custom = true;
	private boolean _global = false;
	
	public ChannelProperties() {
		super("ChannelProperties");
	}

	@Override
	public void onLoad(KahluaTable table) {
		// TODO: Future Implement.
	}
	
	public String getChannelName() {
		return this._name;
	}
	
	public void setChannelName(String name) {
		this._name = name;
	}
	
	public boolean streamToGlobal() {
		return this._global;
	}
	
	public void streamGlobal(boolean flag) {
		this._global = flag;
	}

	public boolean isPublic() {
		return this._public;
	}

	public void setPublic(boolean flag) {
		this._public = flag;
	}
	
	public boolean canSpeak() {
		return this._speak;
	}
	
	public void setSpeak(boolean flag) {
		this._speak = flag;
	}
	
	public String getContext() {
		return this._context;
	}
	
	public void setContext(String context) {
		this._context = context;
	}
	
	public String getDescription() {
		return this._description;
	}
	
	public void setDescription(String description) {
		this._description = description;
	}
	
	public boolean showHistory() {
		return this._history;
	}
	
	public void setHistory(boolean flag) {
		this._history = flag;
	}
	
	public boolean isCustom() {
		return this._custom;
	}
	
	public void setCustom(boolean flag) {
		this._custom = flag;
	}

	@Override
	public void onExport() {
		set("name", getChannelName());
		set("public", isPublic());
		set("speak", canSpeak());
		set("context", getContext());
		set("description", getDescription());
		set("history", showHistory());
		set("custom", isCustom());
		set("global", streamToGlobal());
	}

}
