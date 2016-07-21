/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003  Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.modeltool.amapsim2;

import java.io.Serializable;
import java.util.Comparator;

import capsis.lib.amapsim.AMAPsimTreeStep;

/**
 * TreeStepsComparator.
 * 
 * @author F. de Coligny - december 2002
 */
public class TreeStepsComparator implements Comparator, Serializable {

	public int compare (Object o1, Object o2) throws ClassCastException {
		if (!(o1 instanceof AMAPsimTreeStep)) {
				throw new ClassCastException ("o1 is not an AMAPsimTreeStep : "+o1);}
		if (!(o2 instanceof AMAPsimTreeStep)) {
				throw new ClassCastException ("o2 is not an AMAPsimTreeStep : "+o2);}
				
		float v1 = ((AMAPsimTreeStep) o1).age;
		float v2 = ((AMAPsimTreeStep) o2).age;
		
		// Descenging order
		if (v1 > v2) {
			return -1;		// o1 < o2
		} else if (v1 < v2) {
			return 1;		// o1 > o2
		} else {
			return 0;		// o1 == o2
		}
		
		// Ascending order
		//~ if (v1 < v2) {
			//~ return -1;		// o1 < o2
		//~ } else if (v1 > v2) {
			//~ return 1;		// o1 > o2
		//~ } else {
			//~ return 0;		// o1 == o2
		//~ }
	}
	
	/**
	 * Extract of java 1.3.1 api / java.util.Comparator, about equals () : 
	 * "Note that it is always safe not to override Object.equals(Object). However, overriding 
	 * this method may, in some cases, improve performance by allowing programs to 
	  * determine that two distinct Comparators impose the same order".
	  */
	//public boolean equals (Object o) {return this.equals (o);}	// do not override - fc

}


