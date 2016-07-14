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

import java.text.NumberFormat;

import capsis.defaulttype.NeighbourhoodMask;

/**
 * A light beam.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public class SLBeam extends NeighbourhoodMask {

	private double azimut_rad;
	private double heightAngle_rad;
	private double initialEnergy;
	private double currentEnergy;
	private boolean direct; // true if the beam is direct false if it is diffuse

	/**
	 * Create a beam.
	 */
	public SLBeam(double azimut_rad, double heightAngle_rad,
			double initialEnergy, boolean direct) {
		this.azimut_rad = azimut_rad; // azimut is with anticlockwise (trigonometric) rotation from X axis
		this.heightAngle_rad = heightAngle_rad;
		this.initialEnergy = initialEnergy;
		this.direct = direct;
	}

	public double getHeightAngle_rad() {
		return heightAngle_rad;
	}

	public double getAzimut_rad() {
		return azimut_rad;
	}

	public double getCurrentEnergy() {
		return currentEnergy;
	}

	public double getInitialEnergy() {
		return initialEnergy;
	}

	public void resetEnergy() {
		currentEnergy = initialEnergy;
	}

	public void reduceCurrentEnergy(double f) {
		currentEnergy -= f;
	}

	public void setInitialEnergy(double e) {
		initialEnergy = e;
	}

	public boolean isDirect() {
		return direct;
	}

	/**
	 * Return a String representation of this object.
	 */
	public String toString() {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		StringBuffer b = new StringBuffer(super.toString());
		b.append("SLBeam");
		b.append(" azimut: ");
		b.append(nf.format(Math.toDegrees(this.getAzimut_rad())));
		b.append(" heightAngle: ");
		b.append(nf.format(Math.toDegrees(this.getHeightAngle_rad())));
		b.append(" initialEnergy: ");
		b.append(nf.format(initialEnergy));
		b.append(" currentEnergy: ");
		b.append(nf.format(currentEnergy));
		b.append(" direct: ");
		b.append(direct);

		return b.toString();
	}

	/**
	 * Return a String representation of this object.
	 * adapted by GL (30/04/2013) to produce a nice csv file in the console
	 */
	public String toCSVString() {
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		
		StringBuffer b = new StringBuffer();
//		StringBuffer b = new StringBuffer(super.toString());
		//b.append("SLBeam");
		//b.append(" azimut: ");
		b.append(nf.format(Math.toDegrees(this.getAzimut_rad())));
		b.append (";");
		//b.append(" heightAngle: ");
		b.append(nf.format(Math.toDegrees(this.getHeightAngle_rad())));
		b.append (";");
		//b.append(" initialEnergy: ");
		b.append(nf.format(initialEnergy));
		b.append (";");
		//b.append(" currentEnergy: ");
		//b.append(nf.format(currentEnergy));
		//b.append(" direct: ");
		b.append(direct);

		return b.toString();
	}
}