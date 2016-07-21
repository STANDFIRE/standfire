/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.extension.modeltool.castaneaclimateviewer;

//import nz1.model.*;

import java.util.Collection;

import jeeb.lib.util.Translator;

import org.jfree.data.xy.XYSeriesCollection;

import capsis.lib.castanea.FmClimate;


/**	A graph for relative humidity
*	@author H. Davi, F. de Coligny - march 2009
*/
public abstract class Graph {
	
	public static final String YEARLY = Translator.swap ("Graph.YEARLY");
	public static final String MONTHLY = Translator.swap ("Graph.MONTHLY");
	public static final String DAILY = Translator.swap ("Graph.DAILY");
	public static final String HOURLY = Translator.swap ("Graph.HOURLY");
	
	// Set in the constructor
	protected FmClimate climate;
	protected Collection<String> timeSteps;
	
	protected String name;
	protected String xName;
	protected String yName;
	
	// Set later by the set () method, before calling getData ()
	protected String variableName;
	protected String step;
	protected int yyyyMin;
	protected int dddMin;
	protected int yyyyMax;
	protected int dddMax;
	protected double latitude;		// north degrees
	protected double longitude;		// west degrees
	
	
	public Graph (FmClimate climate) {
		this.climate = climate;
	}
	
	/**	Set the graph parameters. yyyyMin, dddMin, yyyyMax and dddMax are 
	*	all required or all equal to -1. Step is in YEARLY, MONTHLY, DAILY, HOURLY.
	*/
	public void set (String variableName, String step, int yyyyMin, int dddMin, int yyyyMax, int dddMax, 
			double latitude, double longitude) {
		this.variableName = variableName;
		this.step = step;
		this.yyyyMin = yyyyMin;
		this.dddMin = dddMin;
		this.yyyyMax = yyyyMax;
		this.dddMax = dddMax;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public abstract XYSeriesCollection getData () throws Exception;
	
	public Collection<String> getTimeSteps () {return timeSteps;}
	
	public String getName () {return name;}
	public String getXName () {return xName;}
	public String getYName () {return yName;}
	
}


