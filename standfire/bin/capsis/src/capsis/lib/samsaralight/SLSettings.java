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

import java.util.ArrayList;
import java.util.List;

import capsis.defaulttype.SquareCell;
import capsis.kernel.AbstractSettings;
import capsis.util.ListMap;

/**
 * SLSettings - List of samsaralight settings.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public class SLSettings extends AbstractSettings {

	// Main parameter file name
	public String fileName;

	// turbidMedium: if false, porous envelop
	public boolean turbidMedium = true;

	// trunkInterception: if true, the algorithm computes trunk interceptions
	public boolean trunkInterception = false;

	public double directAngleStep = 5; // deg

	public double heightAngleMin = 15; // deg

	public double diffuseAngleStep = 15; // deg

	// Standard Overcast Sky, if false: Uniform Overcast Sky
	public boolean soc = true;

	public int leafOnDoy = 40; // day of year (see the calendar, fc)

	public int leafOffDoy = 200;

	private List<SLMonthlyRecord> monthlyRecords;

	private ListMap hourlyRecords;
	
	public boolean writeStatusDispatcher = true; //GL 5-6-2013. option to speed up the computations in script mode

	// A list of cells that will be targeted by rays. If the list is null all
	// plot cells will be targeted.
	// if the list is empty, all plot cells will be targeted
	// This works only if the list is updated after each evolution and intervention (not very good)
	// => moved to lightableScene (18/05/2015)
//	private List<SquareCell> cellstoEnlight;

	// GMT Time lag between the local time and the local solar time meridian (GL, 06/09/2012)
	// In Occidental Europe : + 1 during the winter; + 2 during the summer. (But
	// default is 0, if no information in the settings file.
	private int GMT = 0;

	//----- Plot location / orientation properties
	private double plotLatitude_deg = 45; // deg: default is near Grenoble, France
	
	private double plotLongitude_deg = 5; // deg
	
	private double plotSlope_deg = 0; // deg

	// Angle of slope bottom on the compass from the North, clockwise rotation
	// northern aspect : 0, eastern aspect : 90, southern aspect : 180, western
	// aspect : 270
	private double plotAspect_deg = 0; // deg
	
	// Angle from North to x axis clockwise. Default correspond to a Y axis
	// oriented toward the North.
	private double northToXAngle_cw_deg = 90; // deg
	//----- Plot location / orientation properties
	
	// Enlight only the virtual sensor?
	private boolean sensorLightOnly = false;

	// Accessors
	public void addMontlyRecord(SLMonthlyRecord r) {
		if (monthlyRecords == null)
			monthlyRecords = new ArrayList<SLMonthlyRecord>();
		monthlyRecords.add(r);
	}

	public List<SLMonthlyRecord> getMontlyRecords() {
		return monthlyRecords;
	}

	public void addHourlyRecord(SLHourlyRecord r) {
		if (hourlyRecords == null)
			hourlyRecords = new ListMap<int[], SLHourlyRecord>();
		int month = r.month;
		hourlyRecords.addItem(month, r);
	}

	public ListMap<int[], SLHourlyRecord> getHourlyRecord() {
		return hourlyRecords;
	}

//	public void addCelltoEnlight(SquareCell c) {
//		if (cellstoEnlight == null)
//			cellstoEnlight = new ArrayList<SquareCell>();
//		cellstoEnlight.add(c);
//	}

	public double getPlotLatitude_deg() {
		return plotLatitude_deg;
	}

	public void setPlotLatitude_deg(double plotLatitude) {
		this.plotLatitude_deg = plotLatitude;
	}

	public double getPlotLongitude_deg() {
		return plotLongitude_deg;
	}

	public void setPlotLongitude_deg(double plotLongitude) {
		this.plotLongitude_deg = plotLongitude;
	}

	public double getPlotSlope_deg() {
		return plotSlope_deg;
	}

	public void setPlotSlope_deg(double plotSlope) {
		this.plotSlope_deg = plotSlope;
	}

	public double getPlotAspect_deg() {
		return plotAspect_deg;
	}

	public void setPlotAspect_deg(double plotAspect) {
		this.plotAspect_deg = plotAspect;
	}

	public double getNorthToXAngle_cw_deg() {
		return northToXAngle_cw_deg;
	}

	public void setNorthToXAngle_cw_deg(double northToXAngle_cw) {
		this.northToXAngle_cw_deg = northToXAngle_cw;
	}

//	public List<SquareCell> getCellstoEnlight() {
//		return cellstoEnlight;
//	}
//	
//	public void clearListOfCellsToEnlight(){
//		cellstoEnlight = new ArrayList<SquareCell>();
//	}

	public int getGMT() {
		return GMT;
	}

	public void setGMT(int gmt) {
		this.GMT = gmt;
	}
	
	public boolean isSensorLightOnly () {
		return sensorLightOnly;
	}
	
	public void setSensorLightOnly (boolean sensorLightOnly) {
		this.sensorLightOnly = sensorLightOnly;
	}
}
