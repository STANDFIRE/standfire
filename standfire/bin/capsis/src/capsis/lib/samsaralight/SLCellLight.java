/* 
 * Samsaralight library for Capsis4.
 * 
 * Copyright (C) 2008 / 2012 Benoit Courbaud.
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
package capsis.lib.samsaralight;

import java.io.Serializable;

import capsis.defaulttype.SquareCell;

/**
 * SLCellLight - light related properties of a cell.
 * 
 * @author B. Courbaud, N. Dones, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public class SLCellLight implements Serializable {

	// The cell which we are the light properties
	private SquareCell connectedCell;

	private float aboveCanopyHorizontalEnergy;// in MJ/m2 along the horizontal before interception
	
	private float totalHorizontalEnergy; // in MJ/m2 along the horizontal
	private float directHorizontalEnergy; // in MJ/m2 along the horizontal
	private float diffuseHorizontalEnergy; // in MJ/m2 along the horizontal
	
	private float directSlopeEnergy; // in MJ/m2 along the slope
	private float diffuseSlopeEnergy; // in MJ/m2 along the slope
	private float totalSlopeEnergy; // in MJ/m2 along the slopeT
	
	private float relativeSlopeEnergy; // in %/m2 along the slope 
	private float relativeDiffuseSlopeEnergy;
	private float relativeDirectSlopeEnergy;
	
	private float relativeHorizontalEnergy; // in %/m2 along the slope 
	private float relativeDiffuseHorizontalEnergy;
	private float relativeDirectHorizontalEnergy;

	// private double transmitance; //between 0 and 1 (I/I0) --ND 04-2010 - GL
	// 06/04/2012 (redundant information with relativeHorizontalEnergy/100)

	/**
	 * Constructor.
	 */
	public SLCellLight(SquareCell connectedCell) {
		this.connectedCell = connectedCell;
	}

	/**
	 * Sometimes, the light is not calculated each year to save time and copies
	 * are used instead.
	 */
	public SLCellLight getCopy(SquareCell connectedCell) {
		SLCellLight sl = new SLCellLight(connectedCell);
		sl.directSlopeEnergy = directSlopeEnergy;
		sl.diffuseSlopeEnergy = diffuseSlopeEnergy;
		sl.totalSlopeEnergy = totalSlopeEnergy;
		sl.relativeSlopeEnergy = relativeSlopeEnergy;
		sl.relativeDiffuseSlopeEnergy = relativeDiffuseSlopeEnergy;
		sl.relativeDirectSlopeEnergy = relativeDirectSlopeEnergy;
		sl.relativeHorizontalEnergy = relativeHorizontalEnergy;
		sl.relativeDiffuseHorizontalEnergy = relativeDiffuseHorizontalEnergy;
		sl.relativeDirectHorizontalEnergy = relativeDirectHorizontalEnergy;
		// sl.transmitance = transmitance;
		return sl;
	}

	// public double getTransmitance() {return transmitance;} // --ND 04-2010
	// public void setTransmitance (double e) {transmitance = e;} // --ND
	// 04-2010

	public void addDirectEnergy(float e) {
		setDirectSlopeEnergy(getDirectSlopeEnergy() + e);
	}

	public void addDiffuseEnergy(float e) {
		setDiffuseSlopeEnergy(getDiffuseSlopeEnergy() + e);
	}

	public void addTotalEnergy(float e) {
		setTotalSlopeEnergy(getTotalSlopeEnergy() + e);
	}

	public void resetEnergy() {
		
		setTotalSlopeEnergy(0);
		setDiffuseSlopeEnergy(0);
		setDirectSlopeEnergy(0);
		
		// Added the 6 lines below fc-28.6.2012
		setRelativeSlopeEnergy(0);
		setRelativeDiffuseSlopeEnergy(0);
		setRelativeDirectSlopeEnergy(0);
		
		setRelativeHorizontalEnergy(0);
		setRelativeDiffuseHorizontalEnergy(0);
		setRelativeDirectHorizontalEnergy(0);
		// setTransmitance (0);// --ND 04-2010
	}

	public SquareCell getConnectedCell() {return connectedCell;}
	public float getDirectSlopeEnergy() {return directSlopeEnergy;}
	public float getDiffuseSlopeEnergy() {return diffuseSlopeEnergy;}
	public float getTotalSlopeEnergy() {return totalSlopeEnergy;}
	public float getRelativeSlopeEnergy() {return relativeSlopeEnergy;}
	public float getRelativeDiffuseSlopeEnergy() {return relativeDiffuseSlopeEnergy;}
	public float getRelativeDirectSlopeEnergy() {return relativeDirectSlopeEnergy;}
	public float getRelativeHorizontalEnergy() {return relativeHorizontalEnergy;}
	public float getRelativeDiffuseHorizontalEnergy() {return relativeDiffuseHorizontalEnergy;}
	public float getRelativeDirectHorizontalEnergy() {return relativeDirectHorizontalEnergy;}
	public float getAboveCanopyHorizontalEnergy() {return aboveCanopyHorizontalEnergy;}
	public float getTotalHorizontalEnergy() {return totalHorizontalEnergy;}
	public float getDirectHorizontalEnergy() {return directHorizontalEnergy;}
	public float getDiffuseHorizontalEnergy() {return diffuseHorizontalEnergy;}
	public void setDirectSlopeEnergy(float e) {directSlopeEnergy = e;}
	public void setDiffuseSlopeEnergy(float e) {diffuseSlopeEnergy = e;}
	public void setTotalSlopeEnergy(float e) {totalSlopeEnergy = e;}
	public void setRelativeSlopeEnergy(float r) {relativeSlopeEnergy = r;}
	public void setRelativeDiffuseSlopeEnergy(float relativeDiffuseSlopeEnergy) {this.relativeDiffuseSlopeEnergy = relativeDiffuseSlopeEnergy;}
	public void setRelativeDirectSlopeEnergy(float relativeDirectSlopeEnergy) {this.relativeDirectSlopeEnergy = relativeDirectSlopeEnergy;}
	public void setRelativeHorizontalEnergy(float r) {relativeHorizontalEnergy = r;}
	public void setRelativeDiffuseHorizontalEnergy(	float relativeDiffuseHorizontalEnergy) {this.relativeDiffuseHorizontalEnergy = relativeDiffuseHorizontalEnergy;}
	public void setRelativeDirectHorizontalEnergy(float relativeDirectHorizontalEnergy) {this.relativeDirectHorizontalEnergy = relativeDirectHorizontalEnergy;}
	public void setAboveCanopyHorizontalEnergy(float aboveCanopyHorizontalEnergy) {this.aboveCanopyHorizontalEnergy = aboveCanopyHorizontalEnergy;}
	public void setTotalHorizontalEnergy(float totalHorizontalEnergy) {this.totalHorizontalEnergy = totalHorizontalEnergy;}
	public void setDirectHorizontalEnergy(float directHorizontalEnergy) {this.directHorizontalEnergy = directHorizontalEnergy;}
	public void setDiffuseHorizontalEnergy(float diffuseHorizontalEnergy) {this.diffuseHorizontalEnergy = diffuseHorizontalEnergy;}
}
