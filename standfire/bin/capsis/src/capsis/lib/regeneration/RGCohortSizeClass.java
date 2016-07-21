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

import jeeb.lib.util.Log;

import java.util.Iterator;
import java.util.Vector;	//mod
import java.util.List;

import rreshar.model.RRSCell;
import rreshar.model.RRSUnderstorey;
import rreshar.model.RRSCohortSizeClass;


/**
 * RGCohortSizeClass is a class in a RGCohort.
 * 
 * @author N. Don�s, Ph. Balandier - April 2012
 */
public abstract class RGCohortSizeClass implements RGHeightComparable,
		RGLightable, Cloneable {

	private int id; // fc-10.10.2013
	
	// borne sup de la classe de hauteur (m)
//	protected double heightUp;
	// hauteur moyenne dans la classe (m)
	protected double heightAvg;
	// diametre moyen de la classe (cm)
	protected double diameterAvg;
	// effectif
	protected int number;
//	protected double height_m;
	public double transmittance; //[0,1] ratio of received light compare to incident light above saplings
	// le terme de transmittance est abusif, il correspond plutot a : l'energie disponible au dessus
	// de l'objet (en %) ~ transmis

	protected double growthHeight;


	// Updated in processLighting () or by an external light model
	private double energy_MJ; // MJ
	private double energy_Above_MJ;
	private double energy_Below_MJ;

	/**
	 * Constructor
	 */
	public RGCohortSizeClass(int id, double heightAvg,
			double diameterAvg, int number) { // fc-10.10.2013 added id
		super();
		this.id = id; // fc-10.10.2013
		this.heightAvg = heightAvg;
		this.diameterAvg = diameterAvg;
		this.number = number;
	}

	/**
	 * Clone a RGCohortSizeClass: first calls super.clone (), then clone the
	 * RGCohortSizeClass instance variables.
	 */
	public Object clone() {
		try {
			RGCohortSizeClass clone = (RGCohortSizeClass) super.clone();
			return clone;
		} catch (Exception e) {
			Log.println(Log.ERROR, "RGCohortSizeClass.clone ()",
					"Could not clone this object: " + this, e);
			return null;
		}
	}

	
	public int getId () {
		return id;
	}

	public double getHeight() {
		return heightAvg;
	}

	public void setHeight(double heightAvg) {
		this.heightAvg = heightAvg;
	}

	public double getDiameterAvg() {
		return diameterAvg;
	}

	public void setDiameterAvg(double diameterAvg) {
		this.diameterAvg = diameterAvg;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Returns the energy in MJ intercepted by this size class at last radiative
	 * balance time.
	 */
	public double getEnergy_MJ() { // MJ
		return energy_MJ;
	}

	public void setEnergy_MJ(double energy_MJ) {
		this.energy_MJ = energy_MJ;
	}

	public double getHeight_m() {
		return heightAvg;
	}

	public void setHeight_m(double heightAvg) {
		this.heightAvg = heightAvg;
	}
	
	public double getTransmittance(){
		return transmittance;
	}
	
	public double getEnergy_Available_MJ() { // MJ
		return energy_Above_MJ;
	}

	public void setEnergy_Available_MJ(double energy_ab_MJ) {
		this.energy_Above_MJ = energy_ab_MJ;
	}
	
	public double getEnergy_Below_MJ() { // MJ
		return energy_Below_MJ;
	}

	public void setEnergy_Below_MJ(double energy_be_MJ) {
		this.energy_Below_MJ = energy_be_MJ;
	}
	
	public double getGrowthHeight(){
		return growthHeight;
	}
	
	public void setGrowthHeight(double groH){
		this.growthHeight=groH;
	}	
	
	public String toString () {
		return getClass ().getSimpleName ()+" id: "+id+" heightAvg: "+heightAvg+" number: "+number;
	}

}
