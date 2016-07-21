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
import java.util.HashSet;

import jeeb.lib.util.Log;
import mustard.model.MustForest;
import mustard.model.MustManagementUnit;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.Cell;
import capsis.kernel.GScene;

/**
 * Group knows what composite objects can be grouped in Capsis. A composite is
 * an object containing a Collection of individuals. The individuals must be
 * instance of Identifiable. Composite type is the name of one individual in the
 * composite (ex: TREE, CELL...). The "which" methods make it possible to find
 * the Collection of individuals in the composite and their type. Ex: If the
 * composite is a GTCStand (instanceof TreeCollection), composite type is TREE
 * and Collection is ((TreeCollection) composite).getTrees (). The Collection of
 * individuals can be grouped with Groupers (see Grouper, Identifier and
 * Filtrer).
 * 
 * @author F. de Coligny - april 2004
 */
public class Group implements Serializable {
	// fc - 6.1.2009 - corrected bug #147 by Frederic Mothe - WQW and Viewing
	// toolkit should not depend on mustard model

	// Possible types for individuals concerned by grouping.
	// Each name should be translated in capsis/Labels_fr/en.properties.
	//
	// ~ static public final String ALL_TYPES = "allTypes"; // fc - 3.6.2008 -
	// see GrouperChooser
	static public final String UNKNOWN = "unknown";
	static public final String TREE = "tree"; // TreeCollection contains GTree
	static public final String CELL = "cell"; // GPlot contains GCell
	static public final String FISH = "fish"; // FishComposite contains GFish
	static public final String REACH = "reach"; // ReachComposite contains
												// GReach
	static public final String WEIR = "weir"; // WeirComposite contains GWeir
	static public final String MANAGEMENT_UNIT = "managementUnit"; // Mustard
																	// module -
																	// fc -
																	// 3.6.2008

	static public final byte AND = 0; // AND constant, see ComplexGrouper
	static public final byte OR = 1; // OR constant, see ComplexGrouper

	static private Collection possibleTypes = new HashSet();
	static {
		possibleTypes.add(TREE);
		possibleTypes.add(CELL);
		possibleTypes.add(FISH);
		possibleTypes.add(REACH);
		possibleTypes.add(WEIR);
		possibleTypes.add(MANAGEMENT_UNIT);
	}

	/**
	 * Return true if given stand is known by the grouping system.
	 */
	static public boolean isGroupable(GScene stand) {
		if (stand instanceof TreeCollection) {
			return true;
		} // TREE
		if (stand.getPlot() != null) {
			return true;
		} // CELL
		if (stand instanceof FishComposite) {
			return true;
		} // FISH
		if (stand instanceof ReachComposite) {
			return true;
		} // REACH
		if (stand instanceof WeirComposite) {
			return true;
		} // WEIR
		try { // fc - 6.1.2009
			if (stand instanceof MustForest) {
				return true;
			} // MANAGEMENT_UNIT
		} catch (Error e) {
		}
		return false;
	}

	/**
	 * Return the Collection of possible group types.
	 */
	static public Collection getPossibleTypes() {
		return possibleTypes;
	}

	/**
	 * Return the Collection of possible group types for this stand.
	 */
	static public Collection getPossibleTypes(GScene stand) {
		Collection c = new ArrayList();
		if (stand instanceof TreeCollection) {
			c.add(Group.TREE);
		}
		if (stand.getPlot() != null) {
			c.add(Group.CELL);
		}
		if (stand instanceof FishComposite) {
			c.add(Group.FISH);
		}
		if (stand instanceof ReachComposite) {
			c.add(Group.REACH);
		}
		if (stand instanceof WeirComposite) {
			c.add(Group.WEIR);
		}
		try { // fc - 6.1.2009
			if (stand instanceof MustForest) {
				c.add(Group.MANAGEMENT_UNIT);
			}
		} catch (Error e) {
		}
		return c;
	}

	/**
	 * Return the possible types of individuals the grouping system can handle.
	 * Referent must be an individual.
	 */
	static public String whichType(Object individual) { // must return a
														// possible type or
														// UNKNOWN
		if (individual instanceof Tree) {
			return Group.TREE;
		}
		if (individual instanceof Cell) {
			return Group.CELL;
		}
		if (individual instanceof GFish) {
			return Group.FISH;
		}
		if (individual instanceof GReach) {
			return Group.REACH;
		}
		if (individual instanceof GWeir) {
			return Group.WEIR;
		}
		try { // fc - 6.1.2009
			if (individual instanceof MustManagementUnit) {
				return Group.MANAGEMENT_UNIT;
			}
		} catch (Error e) {
		}
		Log.println(Log.ERROR, "Group.whichType ()", "can not find type for individual: " + individual
				+ ", returned Group.UNKNOWN");
		return Group.UNKNOWN;
	}

	/**
	 * Return the Collection of individuals of the given type in the stand.
	 * Note: individuals must be instance of Identifiable.
	 */
	static public Collection whichCollection(GScene stand, String type) {
		if (type == null) {
			return new ArrayList();
		} // fc - 2.6.2008
		if (type.equals(TREE) && stand instanceof TreeCollection) {
			return ((TreeCollection) stand).getTrees();
		}
		if (type.equals(CELL) && stand.getPlot() != null) {
			return stand.getPlot().getCells();
		}
		if (type.equals(FISH) && stand instanceof FishComposite) {
			return ((FishComposite) stand).getFishes();
		}
		if (type.equals(REACH) && stand instanceof ReachComposite) {
			return ((ReachComposite) stand).getReachMap().values();
		}
		if (type.equals(WEIR) && stand instanceof WeirComposite) {
			return ((WeirComposite) stand).getWeirMap().values();
		}
		try { // fc - 6.1.2009
			if (type.equals(MANAGEMENT_UNIT) && stand instanceof MustForest) {
				return ((MustForest) stand).getManagementUnits();
			}
		} catch (Error e) {
		}
		Log.println(Log.ERROR, "Group.whichCollection ()", "can not find Collection of type: " + type + " in stand: "
				+ stand + ", returned null");
		return null;
	}

}
