/* 
* The Regeneration library for Capsis4
* 
* Copyright (C) 2008  N. Donès, Ph. Balandier, N. Gaudio
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

import capsis.defaulttype.RectangularPlot;
import capsis.kernel.GScene;


/**	RGPlot is a plot in the regeneration library.
*	@author N. Donès, Ph. Balandier, N. Gaudio - october 2008
*/
public class RGPlot extends RectangularPlot {
	
	// Above canopy solar energy
	private double incidentEnergy_MJ; // MJ
	
	public RGPlot (GScene stand, double cellWidth) {
		super (stand, cellWidth);
	}

	public double getIncidentEnergy_MJ() {
		return incidentEnergy_MJ;
	}

	public double getIncidentEnergy_MJm2() {
		return incidentEnergy_MJ / getArea ();
	}

	public void setIncidentEnergy_MJ(double incidentEnergy_MJ) {
		this.incidentEnergy_MJ = incidentEnergy_MJ;
	}

	
	
}
