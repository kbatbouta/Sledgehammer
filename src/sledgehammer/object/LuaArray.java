package sledgehammer.object;

import java.util.LinkedList;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;

public class LuaArray<T> extends LuaObject {
	private List<T> array;
	
	public LuaArray() {
		super("Array");
		array = new LinkedList<>();
	}
	
	public LuaArray(List<T> other) {
		super("Array");
		array = new LinkedList<>(other);
	}
	
	public LuaArray(KahluaTable table) {
		super("Array");
		array = new LinkedList<>();
		
		for(int index = 0; index < table.size(); index++) {
			Object nextObject = table.rawget(index);
			if(nextObject != null) {
				add((T) nextObject);
			}
		}
	}

	public LuaArray(T[] args) {
		super("Array");
		for(T t : args) {
			add(t);
		}
	}

	public void add(T t) {
		if(!array.contains(t)) {
			array.add(t);
		}
	}
	
	public void remove(T t) {
		if(array.contains(t)) {
			array.remove(t);
		}
	}
	
	public T get(int index) {
		return array.get(index);
	}
	
	@Override
	public KahluaTable export() {
		
		if(DEBUG) {			
			println("Exporting LuaArray: " + getName());
		}
		
		KahluaTable outTable = newTable();
		for(int index = 0; index < array.size(); index++) {
			Object value = processValue(array.get(index));
			outTable.rawset(index, value);
		}
		return outTable;
	}
	
	public int size() {
		return array.size();
	}

	public T[] toArray() {
		Object[] array = new Object[this.array.size()];
		array = this.array.toArray(array);
		return (T[])array;
	}
}
