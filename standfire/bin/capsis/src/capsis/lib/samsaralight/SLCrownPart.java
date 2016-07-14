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

/**
 * SLCrownPart - A description of a part of a crown. A crown may contain one or
 * several such objects, e.g. one for the top and one for the bottom.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public interface SLCrownPart extends SLTreePart {

	/**
	 * Evaluates if the given ray intercepts this crown part, previously
	 * relocated with the given shifts. Returns the interception path length and
	 * a distance to the origin (i.e. the target cell center) or null if no
	 * interception.
	 * 
	 * @param xShift
	 *            , yShift, zShift: m, to relocate the crown part for the
	 *            duration of this interception
	 * @param elevation
	 *            : angle, rad
	 * @param azimuth
	 *            : angle, rad, trigonometric 0 and counter clockwise
	 */
	public double[] intercept(double xShift, double yShift, double zShift,
			double elevation, double azimuth);

	public double getLeafAreaDensity();

	public double getExtinctionCoefficient();

	public void setLeafAreaDensity(double leafAreaDensity);

	public void setExtinctionCoefficient(double extinctionCoefficient);

	public void addDirectEnergy(double e); // MJ

	public void addDiffuseEnergy(double e); // MJ

	public void addPotentialEnergy(double e); // MJ

	public double getDirectEnergy(); // direct beam energy in MJ

	public double getDiffuseEnergy(); // diffuse beam energy in MJ

	public double getPotentialEnergy(); // in MJ, energy intercepted by this
										// part but without neighbours

	public double getVolume(); // m3

	/**
	 * Get a copy of the crown part given a SHIFT of the coordinates
	 * @author fc-23.11.2012
	 */
	public SLCrownPart getCopy (double xShift, double yShift, double zShift);

	public SLCrownPart clone () throws CloneNotSupportedException;
	
	/**
	 * get leaf area (in m�) contained within the crown part
	 * @author GL 17-05-2013
	 */
	public double getLeafArea(); //m2
	
	/**
	 * Method to test whether a point (x,y,z) is inside a crown part
	 * @param x
	 * @param y
	 * @param z
	 * @return boolean
	 * @author ligot.g, 11 feb 2014
	 */
	public boolean isIndsideCrownPart (double x, double y, double z);

}
