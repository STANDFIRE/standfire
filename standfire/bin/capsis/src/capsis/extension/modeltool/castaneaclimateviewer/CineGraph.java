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

import java.util.ArrayList;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import capsis.lib.castanea.FmClimate;
import capsis.lib.castanea.FmClimateDay;


/**	A graph for variables average cinetic
*	@author H. Davi, F. de Coligny - march 2009
*/
public class CineGraph extends Graph {
	static {
		Translator.addBundle ("capsis.extension.modeltool.castaneaclimateviewer.CineGraph");
	}
	// In Graph superclass
	//~ protected FmClimate climate;

	//~ protected String name;
	//~ protected String xName;
	//~ protected String yName;

	//~ protected String step;
	//~ protected int yyyyMin;
	//~ protected int dddMin;
	//~ protected int yyyyMax;
	//~ protected int dddMax;
	//~ protected double latitude;		// north degrees
	//~ protected double longitude;		// west degrees


	public CineGraph (FmClimate climate) {
		super (climate);
		timeSteps = new ArrayList<String> ();
		timeSteps.add (Graph.MONTHLY);
		timeSteps.add (Graph.DAILY);
		timeSteps.add (Graph.HOURLY);
	}

	public XYSeriesCollection getData () throws Exception {

		if (!climate.contains (yyyyMin, dddMin)) {
			throw new Exception ("CineGraph: climate file does not contain dateMin: "
					+yyyyMin+" "+dddMin+" file="+climate.getCompleteFileName ());
		}
		if (!climate.contains (yyyyMax, dddMax)) {
			throw new Exception ("CineGraph: climate file does not contain dateMax: "
					+yyyyMax+" "+dddMax+" file="+climate.getCompleteFileName ());
		}

		if (step.equals (Graph.MONTHLY)) {
			return null;

		} else if (step.equals (Graph.DAILY)) {
			return getDailyData ();

		} else if (step.equals (Graph.HOURLY)) {
			return getHourlyData ();

		} else {
			throw new Exception ("CineGraph.getData (), wrong timeStep: "
					+step+" should be Graph.YEARLY, MONTHLY, DAILY or HOURLY");
		}

	}

	public String getName () {
		if (variableName != null) {
			return Translator.swap ("CineGraph.averageCineticOf")+" "+variableName;
		} else {
			return Translator.swap ("CineGraph.averageCinetic");
		}
	}
	public String getXName () {return Translator.swap ("CineGraph.time");}
	public String getYName () {return variableName;}

	private XYSeriesCollection getDailyData () throws Exception {
		// create 1 series
		XYSeries series = new XYSeries (getName ());
		series.setNotify (false);
		double[] dailyValue = new double[366];	// 0 -> 365

		climate.init (yyyyMin, dddMin);
		boolean finished = false;
		int nbYears = 1;
		int k = 0;

		while (!finished) {
			FmClimateDay day = climate.next ();
			if (climate.yearChanged ()) {
				nbYears++;
				k = 0;
			}

			dailyValue[k] += day.getDaily (variableName);

			if (day.getYear () >= yyyyMax && day.getDay () >= dddMax) {
				finished = true;
			}
			k++;
		}

		for (k = 0; k < 366; k++) {
			dailyValue[k] /= nbYears;
			series.add (k + 1, dailyValue[k]);
		}

		// create the dataset
		XYSeriesCollection dataSet = new XYSeriesCollection ();
		//add serials to dataset
		dataSet.addSeries (series);
		return dataSet;
	}

	private XYSeriesCollection getHourlyData () throws Exception {
		// create 1 series
		XYSeries series = new XYSeries (getName ());
		series.setNotify (false);

		double[] hourlyValue = new double[24];	// 0 -> 23

		climate.init (yyyyMin, dddMin);
		boolean finished = false;
		int i = dddMin;
		int nbDays = 0;

		while (!finished) {
			FmClimateDay day = climate.next ();
			nbDays++;

			for (int h = 0; h < 24; h++) {
				double v = day.getHourly (variableName, latitude, longitude, h, i);
				//Log.println("DynaclimTest", "h=" +variableName+latitude+longitude+h+i);

				hourlyValue[h] += v;
			}

			if (day.getYear () >= yyyyMax && day.getDay () >= dddMax) {
				finished = true;
			}
		}

		for (int k = 0; k < 24; k++) {
			hourlyValue[k] /= nbDays;
			series.add (k + 1, hourlyValue[k]);
		}

		// create the dataset
		XYSeriesCollection dataSet = new XYSeriesCollection ();
		//add serials to dataset
		dataSet.addSeries (series);
		return dataSet;
	}

}


