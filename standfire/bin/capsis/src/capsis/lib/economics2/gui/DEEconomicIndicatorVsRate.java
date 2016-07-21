/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
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

package capsis.lib.economics2.gui;

import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.dataextractor.DETimeG;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics2.EconomicModel;
import capsis.lib.economics2.EconomicScenario;
import capsis.lib.economics2.EconomicStandardizedOperation;

/**
 * A data extractor to show variation of net present value of one or several scenario
 * as a function of the discount rate
 * @author ligot.g
 */
public abstract class DEEconomicIndicatorVsRate extends DETimeG {
	private Vector labels;
	protected double MINIMUM_RATE_DEFAULT = 0.01;
	protected double MAXIMUM_RATE_DEFAULT = 0.15;
	protected double INCREMENT_RATE_DEFAULT = 0.005;

	static {
		Translator.addBundle("capsis.lib.economics2.gui.EconomicTranslator");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DEEconomicIndicatorVsRate () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DEEconomicIndicatorVsRate (GenericExtensionStarter s) {
		super (s);
		settings.icNumberOfSimulations=0;
		labels = new Vector ();
		settings.icNumberOfSimulations=0;
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof EconomicModel)) {return false;}
		} catch (Exception e) {
			Log.println (Log.ERROR, "DEEconomicIndicatorVsRate.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		modifyDefaultValue();
		addDoubleProperty ("MINIMUM_RATE",MINIMUM_RATE_DEFAULT);
		addDoubleProperty ("MAXIMUM_RATE",MAXIMUM_RATE_DEFAULT);
		addDoubleProperty ("INCREMENT_RATE",INCREMENT_RATE_DEFAULT);
	}
	
	/**
	 * An optional method to modify default values
	 */
	protected void modifyDefaultValue(){}
	

	/**
	 * From DataExtractor SuperClass.
	 * Computes the data series. This is the real output building.
	 * It needs a particular step.
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		try {
			// Defining local variables for the computation
			double minimumRate = (double) settings.doubleProperties.get("MINIMUM_RATE");
			double maximumRate = (double) settings.doubleProperties.get("MAXIMUM_RATE");
			double incrementRate = (double) settings.doubleProperties.get("INCREMENT_RATE");

			EconomicScenario currentEconomicScenario = ((EconomicModel) step.getProject().getModel()).getEconomicScenario();

			// Defining the vectors for the curves
			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates

			for(double r = minimumRate; r <= maximumRate; r =  r + incrementRate){
				c1.add(r);
				c2.add(getYValue(r, currentEconomicScenario));
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);
			labels.clear ();
			// fc - 16.10.2001			labels.add (l1);	// if you tell nothing, labels are calculated from x series

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DENetPresentValue.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}
	
	/**
	 * Get the value to plot on Y axis
	 * eg. currentEconomicScenario.calcNetPresentValue(r, es.getStandardizedEconomicOperations())
	 */
	public abstract double getYValue(double r, EconomicScenario es);
	
	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DENetPresentValue");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("discountRate"));
		v.add (Translator.swap ("DENetPresentValue.yLabel"));
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<String>> getLabels () {return labels;}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "G. Ligot";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DENetPresentValue.description");}



}
