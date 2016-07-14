/* 
 * The Regeneration library for Capsis4
 * 
 * Copyright (C) 2008  N. Don�s, Ph. Balandier, N. Gaudio
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
package capsis.lib.regeneration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Identifiable;
import jeeb.lib.util.Log;

/**
 * RGCohort is a cohort of regeneration for a given species on a given ground
 * cell.
 * 
 * @author N. Don�s, Ph. Balandier, N. Gaudio - october 2008
 * 
 */
// public class RGCohort implements Identifiable, Numberable {
public class RGCohort implements Cloneable {

//	protected int id; // est ce utile?

	// annee d'apparitin de la cohorte
	protected int year;

	protected List<RGCohortSizeClass> sizeClasses;
	

	/**
	 * Constructor.
	 */
	public RGCohort(int year) {
		this.year = year;

	}

	/**
	 * Clone a RGCohort: first calls super.clone (), then clone the RGCohort
	 * instance variables.
	 */
	public Object clone() {
		try {
			RGCohort clone = (RGCohort) super.clone();
			clone.sizeClasses = new ArrayList<RGCohortSizeClass> ();
			for (RGCohortSizeClass c : sizeClasses) {
				clone.sizeClasses.add((RGCohortSizeClass) c.clone ());
			}

			return clone;
		} catch (Exception e) {
			Log.println(Log.ERROR, "RGCohort.clone ()",
					"Could not clone this object: " + this, e);
			return null;
		}
	}


	public int getYear() {
		return year;
	}

	public void setYear(int newYear){
		this.year = newYear;
	}
	
	public int getNbClass() {
		return sizeClasses == null ? 0 : sizeClasses.size();
	}

	public void addSizeClass(RGCohortSizeClass sizeClass) {
		if (sizeClasses == null) sizeClasses = new ArrayList<RGCohortSizeClass> ();
		sizeClasses.add(sizeClass);
	}

	public List<RGCohortSizeClass> getSizeClasses() {
		return sizeClasses;
	}

	
	// LATER
//	abstract public void processGrowth();

}
