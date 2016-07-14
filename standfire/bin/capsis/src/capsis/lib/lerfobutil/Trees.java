/*
 * This file is part of the Lerfob modules for Capsis4.
 *
 * Copyright (C) 2009-2010 Jean-Fran�ois Dh�te, Patrick Vallet,
 * Jean-Daniel Bontemps, Fleur Longuetaud, Fr�d�ric Mothe,
 * Laurent Saint-Andr�, Ingrid Seynave.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.lib.lerfobutil;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;

/**
 * Trees : utility methods for managing a collection of trees
 * All the methods are static
 *
 * @author F. Mothe - november 2009
 */
public class Trees {

	/**
	 * Tree comparator by id
	 * @seealso capsis.util.GTreeIdComparator
	 */
	public static class IdComparator <T extends Tree> implements java.util.Comparator <T> {
		private boolean ascending;
		public IdComparator () {this (true);}
		public IdComparator (boolean ascending) {this.ascending = ascending;}
		public int compare (T t1, T t2) {
			int r = Integer.signum (t1.getId () - t2.getId ());
			return ascending ? r : - r;
		}
	}

	/**
	 * Tree comparator by dbh then id
	 * @seealso capsis.util.TreeDbhThenIdComparator
	 */
	public static class DbhThenIdComparator <T extends Tree> implements java.util.Comparator <T> {
		private boolean ascending;
		public DbhThenIdComparator () {this (true);}
		public DbhThenIdComparator (boolean ascending) {this.ascending = ascending;}
		public int compare (T t1, T t2) {
			int r = Double.compare (t1.getDbh (), t2.getDbh ());
			if (r == 0) {r = Integer.signum (t1.getId () - t2.getId ());}
			return ascending ? r : - r;
		}
	}

	/**
	* Returns a new collection of trees sorted by id
	*/
	public static <T extends Tree> Collection <T> collectionSortedById (Collection <T> trees)  {
		TreeSet <T> set = new TreeSet <T> (new IdComparator <T> (true));
		set.addAll (trees);
		return set;
	}

	/**
	* Returns a new collection of trees sorted by dbh (then id)
	*/
	public static <T extends Tree> Collection <T> collectionSortedByDbh (Collection <T> trees, boolean ascending)  {
		TreeSet <T> set = new TreeSet <T> (new DbhThenIdComparator <T> (ascending));
		set.addAll (trees);
		return set;
	}

	/**
	* Returns a new array of trees sorted by id
	*/
	public static Tree [] arrraySortedById (Collection <? extends Tree> trees)  {
		Tree [] sortedTrees = trees.toArray (new Tree [0]);
		Arrays.sort (sortedTrees, new IdComparator <Tree> (true));
		return sortedTrees;
	}

	/**
	* Returns a new array of trees sorted by dbh (then id)
	*/
	public static Tree [] arraySortedByDbh (Collection <? extends Tree> trees, boolean ascending)  {
		Tree [] sortedTrees = trees.toArray (new Tree [0]);
		Arrays.sort (sortedTrees, new DbhThenIdComparator <Tree> (ascending));
		return sortedTrees;
	}

	/**
	* Returns a new collection of trees alive before and missing after.
	* The collection is sorted by id.
	*/
	public static Collection <Tree> removedTrees (TreeList before, TreeList after) {
		TreeSet <Tree> removed = new TreeSet <Tree> (new IdComparator <Tree> (true));
		for (Tree t : before.getTrees ()) {
			if (after.getTree (t.getId ()) == null) {
				removed.add (t);
			}
		}
		return removed;
	}

}
