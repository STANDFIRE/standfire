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
import java.util.List;
import java.util.Set;
import java.util.Map;

import jeeb.lib.util.Log;
import jeeb.lib.util.SetMap;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.RectangularPlot;
import capsis.defaulttype.SquareCell;
import capsis.defaulttype.SquareCellHolder;

/**
 * RGCell is a cell in the regeneration library.
 * 
 * @author N. Donès, Ph. Balandier, N. Gaudio - october 2008
 */
public abstract class RGCell extends SquareCell implements RGLightable {

	protected SetMap<RGSpecies,RGCohort> cohorts; // SetMap: RGSpecies -> Set
													// of
													// RGCohort
	protected List<RGUnderstorey> understoreys;

	private double waterQuantity; // daily current water quantity in (mm)
	private double waterFC; // field capacity (mm)
	private double wiltingPoint; // (mm)
	private double indiceS; // daily water stress index
	private double indiceSMoy; // season average water stress
	// private double wQwRU;
	public ArrayList water;

	public RGCell (RectangularPlot plot, int id, Vertex3d origin, int iGrid, int jGrid) {
		super ((SquareCellHolder) plot, id, 0, origin, iGrid, jGrid); // motherId
																		// = 0
	}

	public SetMap<RGSpecies,RGCohort> getCohorts () {
		return cohorts;
	}

	public Set<RGCohort> getCohorts (RGSpecies s) {
		return cohorts.getObjects (s);
	}

	public List<RGUnderstorey> getUnderstoreys () {
		return understoreys;
	}

	public ArrayList getWater () {
		return water;
	}

	public double getWaterQuantity () {

		return waterQuantity;
	}

	public double getWaterFC () {
		return waterFC;
	}

	public double getWPoint () {
		return wiltingPoint;
	}

	// public double getWQWRU(){
	// return wQwRU;
	// }

	public double getIndiceS () {
		return indiceS;
	}

	public double getIndiceSMoy () {
		return indiceSMoy;
	}

	/**
	 * Clone a RGCell: first calls super.clone (), then clone the RGCell instance variables.
	 */
	public Object clone () {
		try {
			RGCell clone = (RGCell) super.clone ();

			// fc+nd - 25.4.2012 cohorts are not CLONED any more, they will
			// be added in the growth process in the module
			clone.cohorts = null;

			// ERROR fc+nd-24.2.2014 moved in RGStand.getInterventionBase ()
//			// fc-6.12.2013 restored the lines below for interventions (was missing)
//			if (cohorts != null) {
//				clone.cohorts = new SetMap<RGSpecies,RGCohort> ();
//				for (RGSpecies species : this.cohorts.keySet ()) {
//					for (RGCohort co : this.cohorts.getObjects (species)) {
//						RGCohort copy = (RGCohort) co.clone ();
//						clone.cohorts.addObject (species, copy);
//					}
//				}
//			}

			// fc+nd - 25.4.2012 Understoreys are not CLONED any more, they will
			// be added in the growth process in the module
			clone.understoreys = null;

			// ERROR fc+nd-24.2.2014 moved in RGStand.getInterventionBase ()
//			// fc-6.12.2013 restored the lines below for interventions (was missing)
//			if (understoreys != null) {
//				clone.understoreys = new ArrayList<RGUnderstorey> ();
//				for (RGUnderstorey u : understoreys) {
//					clone.understoreys.add ((RGUnderstorey) u.clone ());
//				}
//			}

			// fc+nd-26.2.2014
			clone.water = null;
			
			return clone;
		} catch (Exception e) {
			Log.println (Log.ERROR, "RGCell.clone ()", "Could not clone this object: " + this, e);
			return null;
		}
	}

	public void addCohort (RGSpecies s, RGCohort c) {
		if (cohorts == null) {
			cohorts = new SetMap<RGSpecies,RGCohort> ();
		}
		cohorts.addObject (s, c);
	}

	public void addUnderstorey (RGUnderstorey u) {
		if (understoreys == null) understoreys = new ArrayList<RGUnderstorey> ();
		understoreys.add (u);
	}

	public void addWater (double u) {
		if (water == null) water = new ArrayList ();
		water.add (u);
	}

	public void setWaterQuantity (double q) {
		waterQuantity = q;
	}

	public void setWaterFC (double q) {
		waterFC = q;
	}

	// public void setWQWRU(double q){
	// wQwRU = q;
	// }

	public void setWPoint (double q) {
		wiltingPoint = q;
	}

	public void setIndiceS (double q) {
		indiceS = q;
	}

	public void setIndiceSMoy (double q) {
		indiceSMoy = q;
	}

	/**
	 * Returns the energy in MJ intercepted by this cell at last radiative balance time. Updated in
	 * processLighting () or by an external light model.
	 */
	public abstract double getEnergy_MJ (); // MJ

}
