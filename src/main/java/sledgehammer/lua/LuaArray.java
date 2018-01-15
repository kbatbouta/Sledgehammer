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

package sledgehammer.lua;

import java.util.LinkedList;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;

/**
 * A very convenient LuaObject that stores elements as an ArrayList,
 * allowing exporting into proper KahluaTables without any boilerplate code.
 *
 * @param <T> The Serialized Class to be defined for the Array.
 * @author Jab
 */
public class LuaArray<T> extends LuaObject {

    /**
     * The List to store the Elements.
     */
    private List<T> array;

    /**
     * Main constructor.
     */
    public LuaArray() {
        super("Array");
        array = new LinkedList<>();
    }

    /**
     * Clone constructor.
     *
     * @param other The other List to shallow-copy.
     */
    public LuaArray(List<T> other) {
        super("Array");
        array = new LinkedList<>(other);
    }

    /**
     * KahluaTable constructor.
     *
     * @param table The KahluaTable to convert to a LuaArray.
     */
    @SuppressWarnings("unchecked")
    public LuaArray(KahluaTable table) {
        super("Array");
        array = new LinkedList<>();
        for (int index = 0; index < table.size(); index++) {
            Object nextObject = table.rawget(index);
            if (nextObject != null) {
                add((T) nextObject);
            }
        }
    }

    /**
     * Primitive Array constructor.
     *
     * @param args The Array of values to convert to a LuaArray.
     */
    public LuaArray(T[] args) {
        super("Array");
        for (T t : args) {
            add(t);
        }
    }

    @Override
    public KahluaTable export() {
        KahluaTable outTable = newTable();
        for (int index = 0; index < array.size(); index++) {
            Object value = processValue(array.get(index));
            outTable.rawset(index, value);
        }
        return outTable;
    }

    /**
     * Adds an Element to the LuaArray.
     *
     * @param t The element to add.
     */
    public void add(T t) {
        if (!array.contains(t)) {
            array.add(t);
        }
    }

    /**
     * Removes an element from the LuaArray.
     *
     * @param t The element to remove.
     */
    public void remove(T t) {
        if (array.contains(t)) {
            array.remove(t);
        }
    }

    /**
     * @param index The int offset in the Array.
     * @return Returns the element located at the index of the Array. If nothing is
     * defined at this index, null is returned.
     */
    public T get(int index) {
        return array.get(index);
    }

    /**
     * @return Returns the Integer length of the LuaArray.
     */
    public int size() {
        return array.size();
    }

    /**
     * @return Returns a primitive Array of the elements stored in the LuaArray.
     */
    @SuppressWarnings({"unchecked", "hiding"})
    public T[] toArray() {
        Object[] array = new Object[this.array.size()];
        array = this.array.toArray(array);
        return (T[]) array;
    }

    /**
     * @param element The element being tested.
     * @return Returns true if the element given is contained within the LuaArray.
     */
    public boolean contains(T element) {
        return array.contains(element);
    }

    /**
     * Clears the LuaArray.
     */
    public void clear() {
        array.clear();
    }
}
