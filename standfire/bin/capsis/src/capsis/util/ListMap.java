/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jeeb.lib.util.Identifiable;

/**
 * A Map containing Lists of items. 
 * The keys of the Map are Objects (ex, String, Integer...). 
 * The values of the Map are Lists.
 * The Lists contain items (Objects).
 * See SetMap, the lists here accept dupplicates and keep insertion order.
 * 
 * @author F. de Coligny - october 2006
 */
public class ListMap<K,V> extends HashMap<K,List<V>> implements Serializable {
	
	/**	Constructor 1.
	*/
	public ListMap () {super ();}
	
	/**	Constructor 2.
	*/
	public ListMap (int initialCapacity) {super (initialCapacity);}
	
	/**	Constructor 3.
	*/
	public ListMap (Map<K,List<V>> map) {super (map);}
	
	/**	Constructor 4.
	*/
	public ListMap (int initialCapacity, float loadFactor) {super (initialCapacity, loadFactor);}

	/**	Remove an item for this key.
	*/
	public void removeItem (Object key, Object item) {	// fc - 30.5.2006
	if (key == null || item == null) {return;}
		List<V> l = this.get (key);		// null if not found
		if (l != null) {
			l.remove (item);
			
			// fc - 31.8.2006 - if set is empty after removal, delete entry for this key
			if (l.isEmpty ()) {this.remove (key);}
		}
	}

	/**	Add an item for this key. Create entry in the Map if key is unknown.
	*/
	public void addItem (K key, V item) {
		List<V> l = (List<V>) this.get (key);		// null if not found
		if (l == null) {
			l = new ArrayList<V> ();			// A List = duplicates accepted, insertion order kept
			this.put (key, l);
		}
		l.add (item);
	}
	
	/**	Get the available keys in this ListMap.
	*/
	public Set<K> getKeys () {return this.keySet ();}
	
	/**	Get the items for this key. Return empty collection if key is unknown.
	*/
	public Collection<V> getItems (Object key) {
		//~ if (!this.containsKey (key)) {return null;}
		if (!this.containsKey (key)) {return new ArrayList<V> ();}	// fc - 23.4.2004
		return this.get (key);
	}
	
	/**	Gives a String short representation of the ListMap.
	*/
	public String toString () {
		StringBuffer b = new StringBuffer ("ListMap ");
		if (isEmpty ()) {
			b.append ("(empty)");
		} else {
			for (K key : keySet ()) {
				List<V> l = this.get (key);
				b.append (""+key);
				b.append ("(");
				b.append (l.size ());
				b.append (")" );
			}
		}
		return b.toString ();
	}
	
	/**	Gives an array representation of the ListMap.
	*/
	public String[][] toArray () {
		int nLin = this.size ();
		int nCol = 0;
		// Get number of column
		for (Iterator<List<V>> i = values ().iterator (); i.hasNext ();) {
			List<V> value = i.next ();
			nCol = Math.max (nCol, value.size ());
		}
		String[][] array = new String[nLin][nCol+1];
		
		// For each key
		int line = 0;
		TreeSet<K> keys = new TreeSet<K> (keySet ());	// ordered
		for (Iterator<K> i = keys.iterator (); i.hasNext ();) {
			K key = i.next ();
			List<V> l = this.get (key);
			array[line][0] = "" + key;
			
			// For each element in the list
			int column = 1;
			for (Iterator<V> j = l.iterator (); j.hasNext ();) {
				V item = j.next ();
				String s = "" + item;
				if (item instanceof Identifiable) {s = "" + ((Identifiable) item).getId ();}
				array[line][column++] = s;
			}
			
			for (int k = column; k < nCol+1; k++) {
				array[line][k] = "-";
			}
			
			line++;
		}
		return array;
	}
	
}