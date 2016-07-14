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
package capsis.lib.dendrometry;

import java.util.Collection;
import java.util.Iterator;

import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;

/**
 * Dendrometry calculation methods. 
 * Pay attention to the types of the trees in the collection : Numberable or not.
 *
 * @author F. de Coligny - march 2004
 */
public class Dendro {

	/**	N 
	*	The collection can contain Indidual and/or Numberable trees.
	*	B. Courbaud, F. de Coligny - march 2004
	*/
	static public int getN (Collection trees) throws Exception {
		if (trees == null) {throw new Exception ("Dendro.getN error: trees = null");}
		if (trees.isEmpty ()) {return 0;}
		int n = 0;
		for (Iterator i = trees.iterator (); i.hasNext ();) {
			Object t = i.next ();
			if (t instanceof Numberable) {
				Numberable nt = (Numberable) t;
				n += nt.getNumber ();
			} else {
				n++;	// Individual trees
			}
		}
		return n;
	}
	
	/**	G
	*	The collection can contain Indidual and/or Numberable trees.
	*	B. Courbaud, F. de Coligny - march 2004
	*/
	static public double getG (Collection trees) throws Exception {
		if (trees == null) {throw new Exception ("Dendro.getG error: trees = null");}
		if (trees.isEmpty ()) {return 0;}
		double g = 0;
		for (Iterator i = trees.iterator (); i.hasNext ();) {
			Tree t = (Tree) i.next ();
			double area = Math.PI * t.getDbh () * t.getDbh () / 4;
			if (t instanceof Numberable) {
				Numberable nt = (Numberable) t;
				area *= nt.getNumber ();
			}
			g += area;
		}
		return g;
	}
	
	/**	Kg
	*	The collections can contain Indidual and/or Numberable trees.
	*	B. Courbaud, F. de Coligny - march 2004
	*/
	static public double getKg (Collection treesBeforeThinning, Collection thinnedTrees) throws Exception {
		if (treesBeforeThinning == null
				|| thinnedTrees == null) {throw new Exception (
				"Dendro.getKg error: treesBeforeThinning and thinnedTrees must not be null");}
		if (treesBeforeThinning.isEmpty ()) {throw new Exception (
				"Dendro.getKg error: treesBeforeThinning must not be empty");}
		if (thinnedTrees.isEmpty ()) {return 0;}
		
		double thinnedMeanG = getG (thinnedTrees) / getN (thinnedTrees);
		double beforeMeanG = getG (treesBeforeThinning) / getN (treesBeforeThinning);
		return thinnedMeanG / beforeMeanG;
	}
	
	
	
	
}
