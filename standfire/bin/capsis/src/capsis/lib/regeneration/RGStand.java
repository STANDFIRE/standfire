/*
 * The Regeneration library for Capsis4
 * 
 * Copyright (C) 2008 N. Don�s, Ph. Balandier, N. Gaudio
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package capsis.lib.regeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.SetMap;
//import rreshar.model.RRSCell; gl 01/03/2013
//import rreshar.model.RRSPlot; gl 01/03/2013
import capsis.defaulttype.TreeList;
import capsis.kernel.GScene;

/**
 * RGStand is a Stand with regeneration description.
 * 
 * @author N. Donès, Ph. Balandier, N. Gaudio - october 2008
 */
public class RGStand extends TreeList {

	/**	
	 * 
	 */
	public GScene getInterventionBase () {

		RGStand standCopy = (RGStand) super.getInterventionBase ();

		// fc+nd-24.2.2014 found a bug, the cohorts and understoreys were cloned in RGCell,
		// but they must be cloned only in case of intervention (resulted in duplicate cohorts and
		// understoreys during model growth method).
		RGPlot refPlot = (RGPlot) this.getPlot ();
		RGPlot newPlot = (RGPlot) standCopy.getPlot ();
		Collection refCells = refPlot.getCells ();

		for (Iterator i = refCells.iterator (); i.hasNext ();) {
			RGCell refCell = (RGCell) i.next ();
			RGCell newCell = (RGCell) newPlot.getCell (refCell.getId ());

			// fc-6.12.2013 restored the lines below for interventions (was missing)
			if (refCell.cohorts != null) {
				newCell.cohorts = new SetMap<RGSpecies,RGCohort> ();
				for (RGSpecies species : refCell.cohorts.keySet ()) {
					for (RGCohort co : refCell.cohorts.getObjects (species)) {
						RGCohort copy = (RGCohort) co.clone ();
						newCell.cohorts.addObject (species, copy);
					}
				}
			}

			// fc-6.12.2013 restored the lines below for interventions (was missing)
			if (refCell.understoreys != null) {
				newCell.understoreys = new ArrayList<RGUnderstorey> ();
				for (RGUnderstorey u : refCell.understoreys) {
					newCell.understoreys.add ((RGUnderstorey) u.clone ());
				}
			}

		}

		return standCopy;
	}

}
