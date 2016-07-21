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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import jeeb.lib.util.Identifiable;

/**
 * A Map containing Collections of items. 
 * The keys of the Map are Objects (ex, String, Integer...). 
 * The values of the Map are Collections.
 * The Collections contain items (Objects).
 * 
 * @author F. de Coligny - march 2004
 */
public class SortedSetMap extends TreeMap implements Serializable {
	
	/**	Constructor 1.
	*/
	public SortedSetMap () {super ();}
	
	/**	Constructor 2.
	*/
	//~ public SortedSetMap (int initialCapacity) {super (initialCapacity);}
	
	/**	Constructor 3.
	*/
	//~ public SortedSetMap (Map map) {super (map);}
	
	/**	Constructor 4.
	*/
	//~ public SortedSetMap (int initialCapacity, float loadFactor) {super (initialCapacity, loadFactor);}

	/**	Add an item for this key. Create entry in the Map if key is unknown.
	*/
	public void addItem (Object key, Object item) {
		Collection c = (Collection) this.get (key);		// null if not found
		if (c == null) {
			c = new ArrayList ();			// keeps thing in the insertion order
			this.put (key, c);
		}
		c.add (item);
	}
	
	/**	Get the available keys in this SortedSetMap.
	*/
	public Set getKeys () {return this.keySet ();}
	
	/**	Get the items for this key. Return empty collection if key is unknown.
	*/
	public Collection getItems (Object key) {
		//~ if (!this.containsKey (key)) {return null;}
		if (!this.containsKey (key)) {return new ArrayList ();}	// fc - 23.4.2004
		return (Collection) this.get (key);
	}
	
	/**	Gives a String short representation of the SortedSetMap.
	*/
	public String toString () {
		StringBuffer b = new StringBuffer ("SortedSetMap ");
		if (isEmpty ()) {
			b.append ("(empty)");
		} else {
			for (Iterator i = keySet ().iterator (); i.hasNext ();) {
				Object key = i.next ();
				Collection c = (Collection) this.get (key);
				b.append (""+key);
				b.append ("(");
				b.append (c.size ());
				b.append (")" );
			}
		}
		return b.toString ();
	}
	
	/**	Gives an array representation of the SortedSetMap.
	*/
	public String[][] toArray () {
		int nLin = this.size ();
		int nCol = 0;
		for (Iterator i = values ().iterator (); i.hasNext ();) {
			Collection value = (Collection) i.next ();
			nCol = Math.max (nCol, value.size ());
		}
		String[][] array = new String[nLin][nCol+1];
		
		int line = 0;
		for (Iterator i = keySet ().iterator (); i.hasNext ();) {
			Object key = i.next ();
			Collection c = (Collection) this.get (key);
			array[line][0] = ""+key;
			
			int column = 1;
			for (Iterator j = c.iterator (); j.hasNext ();) {
				Object item = j.next ();
				String s = ""+item;
				if (item instanceof Identifiable) {s = ""+((Identifiable) item).getId ();}
				array[line][column++] = s;
			}
			line++;
		}
		return array;
	}
	
}