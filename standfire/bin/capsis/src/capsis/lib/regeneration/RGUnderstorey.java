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

import java.io.Serializable;

import jeeb.lib.util.Log;



/**	RGUnderstorey.
*	@author N. Don�s, Ph. Balandier, N. Gaudio - october 2008
*/
public abstract class RGUnderstorey implements Cloneable, RGHeightComparable, RGLightable, Serializable {
	
	private String speciesName;
	private double height; //m
	private double cover; //%

	// Updated in processLighting () or by an external light model
	private double energy_MJ;
	private double height_m;
	private double energy_Above_MJ;
	private double energy_Below_MJ;
	private double growthHeight;

	public double transmittance; //[0,1] ratio of received light compare to incident light above saplings
	// le terme de transmittance est abusif, il correspond plutot a : l'energie disponible au dessus
	// de l'objet (en %) ~ transmis

	public RGUnderstorey (String speciesName, double height, double cover) {
		this.speciesName = speciesName;
		this.height = height;
		this.cover = cover;
		this.height_m = height;
	}
	
	/**
	 * Clone a RGUnderstorey: first calls super.clone (), then clone the RGUnderstorey
	 * instance variables.
	 */
	public Object clone() {
		try {
			RGUnderstorey clone = (RGUnderstorey) super.clone();
			return clone;
		} catch (Exception e) {
			Log.println(Log.ERROR, "RGUnderstorey.clone ()",
					"Could not clone this object: " + this, e);
			return null;
		}
	}

	public String getSpeciesName () {return speciesName;}
	public double getHeight () {return height;}
	public double getHeightAvg(){return height;}
	public double getCover () {return cover;}
	
	public void setSpeciesName (String v) {speciesName = v;}
	public void setHeight (double v) {height = v;}
	public void setCover (double v) {cover = v;}
	
	/**
	 * Returns the energy in MJ intercepted by this understorey at last radiative
	 * balance time.
	 */
	public double getEnergy_MJ() {
		return energy_MJ;
	}

	public void setEnergy_MJ(double energy_MJ) {
		this.energy_MJ = energy_MJ;
	}

	public double getHeight_m () {
		return height_m;
	}
	
	public void setHeight_m(double v){
		height_m = v;
	}
	
	public void  setEnergy_Available_MJ(double energy_av_MJ) {
		this.energy_Above_MJ = energy_av_MJ;
	}
	
	public double getEnergy_Available_MJ() {
		return energy_Above_MJ;
	}
	
	public void  setEnergy_Below_MJ(double energy_be_MJ) {
		this.energy_Below_MJ = energy_be_MJ;
	}
	
	public double getEnergy_Below_MJ() {
		return energy_Below_MJ;
	}
	
	public void setGrowthHeight(double growthHeight){
		this.growthHeight = growthHeight;
	}
	
	public double getGrowthHeight(){
		return growthHeight;
	}
}
