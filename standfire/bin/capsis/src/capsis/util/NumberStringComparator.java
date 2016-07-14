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

import java.util.Comparator;

/**	Comparator for Strings which start with a Number (ex: "11", "3.45", "12 (dbh=3.45)").
*	@author F. de Coligny - march 2004
*/
public class NumberStringComparator implements Comparator {
	
	public int compare (Object o1, Object o2) {
		String n1 = (String) o1;
		int i1 = n1.indexOf (" ");
		if (i1 != -1) {n1 = n1.substring (0, i1);}
		
		String n2 = (String) o2;
		int i2 = n2.indexOf (" ");
		if (i2 != -1) {n2 = n2.substring (0, i2);}
	
		double d1 = new Double (n1).doubleValue ();
		double d2 = new Double (n2).doubleValue ();
	
		if (d1 < d2) {
			return -1;
		} else if (d1 > d2) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public boolean equals (Object o) {
		return super.equals (o);
	}
	
}

