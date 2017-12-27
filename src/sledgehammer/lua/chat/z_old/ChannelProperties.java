/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.lua.chat.z_old;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.MongoCollection;
import sledgehammer.lua.LuaTable;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
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

	public void load(DBObject object) {
		setChannelName(object.get("name").toString());
		setDescription(object.get("description").toString());
		setContext(object.get("context").toString());
		setPublic(object.get("public").toString().equals("1"));
		setSpeak(object.get("speak").toString().equals("1"));
	}

	private void onSave(DBObject object) {
		object.put("name", getChannelName());
		object.put("description", getDescription());
		object.put("context", getContext());
		object.put("public", isPublic() ? "1" : "0");
		object.put("speak", canSpeak() ? "1" : "0");
	}

	public void save(MongoCollection collection) {
		DBObject object = new BasicDBObject();
		onSave(object);
		collection.upsert(object, "name", this);
	}

	public void rename(MongoCollection collection, String nameNew) {
		collection.delete("name", getChannelName());
		setChannelName(nameNew);
		save(collection);
	}
}