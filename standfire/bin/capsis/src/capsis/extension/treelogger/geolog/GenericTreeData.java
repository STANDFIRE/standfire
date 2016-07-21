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

package capsis.extension.treelogger.geolog;

import java.util.Collection;

import capsis.defaulttype.Tree;
import capsis.extension.treelogger.geolog.util.TreeHistory;
import capsis.kernel.Step;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**	
 * The GenericTreeData class handles all the profile required to process the Tree instance in GeoLog.
 * @author F. Mothe - august 2006
 * @author M. Fortin - November 2011 (refactoring)
 */
public class GenericTreeData extends GeoLogTreeData {

	/**
	 * Constructor.
	 * @param tree a Tree instance
	 * @param stepsFromRoot a Collection of Step from the root to the current step
	 * @param mp a TreeRadius_cmProvider instance
	 * @param species a String
	 */
	public GenericTreeData(Tree tree, Collection <Step> stepsFromRoot, TreeRadius_cmProvider mp, String species) {
		super (tree, null, mp, species);
		setTreeHistory(new TreeHistory(tree, stepsFromRoot).getHistory());
		initKnotProfile (mp);
	}

}

