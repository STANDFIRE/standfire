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
import java.util.List;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import capsis.lib.castanea.FmClimate;
import capsis.lib.castanea.FmClimateDay;


/**	A graph for all the variables in FmClimateDay
*	@author H. Davi, F. de Coligny - march 2009
*/
public class EvoGraph extends Graph {
	static {
		Translator.addBundle ("capsis.extension.modeltool.castaneaclimateviewer.EvoGraph");
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


	public EvoGraph (FmClimate climate) {
		super (climate);
		timeSteps = new ArrayList<String> ();
		timeSteps.add (Graph.YEARLY);
		timeSteps.add (Graph.MONTHLY);
		timeSteps.add (Graph.DAILY);
		timeSteps.add (Graph.HOURLY);
	}

	public XYSeriesCollection getData () throws Exception {

		if (!climate.contains (yyyyMin, dddMin)) {
			throw new Exception ("EvoGraph: climate file does not contain dateMin: "
					+yyyyMin+" "+dddMin+" file="+climate.getCompleteFileName ());
		}
		if (!climate.contains (yyyyMax, dddMax)) {
			throw new Exception ("EvoGraph: climate file does not contain dateMax: "
					+yyyyMax+" "+dddMax+" file="+climate.getCompleteFileName ());
		}

		if (step.equals (Graph.YEARLY)) {
			return getYearlyData ();

		} else if (step.equals (Graph.MONTHLY)) {
			return null;

		} else if (step.equals (Graph.DAILY)) {
			return getDailyData ();

		} else if (step.equals (Graph.HOURLY)) {
			return getHourlyData ();

		} else {
			throw new Exception ("EvoGraph.getData (), wrong timeStep: "
					+step+" should be Graph.YEARLY, MONTHLY, DAILY or HOURLY");
		}

	}

	public String getName () {
		if (variableName != null) {
			return Translator.swap ("EvoGraph.evolutionOf")+" "+variableName;
		} else {
			return Translator.swap ("EvoGraph.evolution");
		}
	}
	public String getXName () {return Translator.swap ("EvoGraph.time");}
	public String getYName () {return variableName;}

	private XYSeriesCollection getYearlyData () throws Exception {
		// create 1 series
		XYSeries series = new XYSeries (getName ());
		series.setNotify (false);

		// getYearlyMeans () returns a collection of days
		// each day contains the mean values for each column
		// to retrieve the values, we use getDaily (variableName)
		List<FmClimateDay> means = climate.getYearlyMeans (yyyyMin, yyyyMax);
		for (FmClimateDay mean : means) {
			series.add (mean.getYear (), mean.getDaily (variableName));
		}

		// create the dataset
		XYSeriesCollection dataSet = new XYSeriesCollection ();
		//add serials to dataset
		dataSet.addSeries (series);
		return dataSet;
	}

	private XYSeriesCollection getDailyData () throws Exception {
		// create 1 series
		XYSeries series = new XYSeries (getName ());
		series.setNotify (false);

		climate.init (yyyyMin, dddMin);
		boolean finished = false;
		int i = dddMin;

		while (!finished) {
			FmClimateDay day = climate.next ();

			//~ series.add (i, day.getRelativeHumidity ());
			series.add (i, day.getDaily (variableName));

			if (day.getYear () >= yyyyMax && day.getDay () >= dddMax) {
				finished = true;
			}
			i++;
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

		climate.init (yyyyMin, dddMin);
		boolean finished = false;
		int i = dddMin;
		//int i=0;

		while (!finished) {
			FmClimateDay day = climate.next ();

			for (int h = 0; h < 24; h++) {
				Log.println("DynaclimTest", " h="+h+" day="+day+" lat="+latitude);

				double v = day.getHourly (variableName, latitude, longitude, h, i);
				series.add (i++, v);
			}

			//j++;
			//series.add (i++, v);
			if (day.getYear () >= yyyyMax && day.getDay () >= dddMax) {
				finished = true;
			}
		}

		// create the dataset
		XYSeriesCollection dataSet = new XYSeriesCollection ();
		//add serials to dataset
		dataSet.addSeries (series);
		return dataSet;
	}

}


