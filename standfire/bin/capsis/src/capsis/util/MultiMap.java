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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A double map : equivalent to a map with two keys.
 *
 * @author F. de Coligny - january 2002
 */
public class MultiMap {

	protected Map map;	// Integer - Collection

	public MultiMap () {map = new HashMap ();}
	
	public void clear () {if (map != null) {map.clear ();}}

	public void add (Object key, Object value) {
		if (map.containsKey (key)) {
			Collection c = (Collection) map.get (key);
			c.add (value);
		} else {
			Collection c = new ArrayList ();
			c.add (value);
			map.put (key, c);
		}
	}
	
	public Collection getCollection (Object key) {
		if (map == null || !map.containsKey (key)) {return new ArrayList ();}	// empty Collection
		return (Collection) map.get (key);
	}
	
	public boolean isEmpty () {return map == null || map.isEmpty ();}
	
	public String toString () {
		StringBuffer b = new StringBuffer ();
		b.append ("MultiMap");
		
		String s = "MultiMap";
		if (isEmpty ()) {
			b.append (" (empty)");
			return b.toString ();
		}
		b.append ("\n");
		for (Iterator i = map.keySet ().iterator (); i.hasNext ();) {
			Object key = i.next ();
			Collection col = (Collection) map.get (key);
			b.append ("(");
			b.append (key);
			b.append (")-");
			b.append (col);
			b.append ("\n");
		}
		return b.toString ();
	}
	
	
	// for test only
	public static void main (String[] a) {
		MultiMap mm = new MultiMap ();
		System.out.println ("begin: "+mm);
		mm.add ("1", "obj1");
		mm.add ("1", "obj2");
		mm.add ("1", "obj3");
		mm.add ("1", "obj4");
		mm.add ("2", "obj5");
		mm.add ("2", "obj6");
		mm.add ("3", "obj7");
		System.out.println ("end: "+mm);
	}
	
}

