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

import jeeb.lib.util.Translator;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics2.EconomicScenario;

/**
 * A data extractor to show variation of net present value of one or several scenario
 * as a function of the discount rate
 * @author ligot.g
 */
public class DENetPresentValueVsRate extends DEEconomicIndicatorVsRate {
	//	private Vector labels;
	//
	//	static {
	//		Translator.addBundle("capsis.lib.economics2.gui.EconomicTranslator");
	//	}
	//
	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DENetPresentValueVsRate () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DENetPresentValueVsRate (GenericExtensionStarter s) {
		super (s);
	}
	//
	//	/**
	//	 * Extension dynamic compatibility mechanism.
	//	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	//	 */
	//	public boolean matchWith (Object referent) {
	//		try {
	//			if (!(referent instanceof EconomicModel)) {return false;}
	//		} catch (Exception e) {
	//			Log.println (Log.ERROR, "DENetPresentValue.matchWith ()", "Error in matchWith () (returned false)", e);
	//			return false;
	//		}
	//
	//		return true;
	//	}
	//
	//	/**
	//	 * This method is called by superclass DataExtractor.
	//	 */
	//	public void setConfigProperties () {
	//		// Choose configuration properties
	//		addDoubleProperty ("MINIMUM_RATE",0);
	//		addDoubleProperty ("MAXIMUM_RATE",0.15);
	//		addDoubleProperty ("INCREMENT_RATE",0.01);
	//	}
	//
	//	/**
	//	 * From DataExtractor SuperClass.
	//	 * Computes the data series. This is the real output building.
	//	 * It needs a particular step.
	//	 * Return false if trouble while extracting.
	//	 */
	//	public boolean doExtraction () {
	//		if (upToDate) {return true;}
	//		if (step == null) {return false;}
	//
	//		try {
	//			// Defining local variables for the computation
	//			double minimumRate = (double) settings.doubleProperties.get("MINIMUM_RATE");
	//			double maximumRate = (double) settings.doubleProperties.get("MAXIMUM_RATE");
	//			double incrementRate = (double) settings.doubleProperties.get("INCREMENT_RATE");
	//
	//			EconomicScenario currentEconomicScenario = ((EconomicModel) step.getProject().getModel()).getEconomicScenario();
	//
	//			// Defining the vectors for the curves
	//			Vector c1 = new Vector ();		// x coordinates
	//			Vector c2 = new Vector ();		// y coordinates
	//
	//			for(double r = minimumRate; r <= maximumRate; r =  r + incrementRate){
	//				c1.add(r);
	//				c2.add(currentEconomicScenario.calcNetPresentValue(r, currentEconomicScenario.getStandardizedEconomicOperations()));
	//			}
	//
	//			curves.clear ();
	//			curves.add (c1);
	//			curves.add (c2);
	//			labels.clear ();
	//			// fc - 16.10.2001			labels.add (l1);	// if you tell nothing, labels are calculated from x series
	//
	//		} catch (Exception exc) {
	//			Log.println (Log.ERROR, "DENetPresentValue.doExtraction ()", "Exception caught : ",exc);
	//			return false;
	//		}
	//
	//		upToDate = true;
	//		return true;
	//	}
	//
	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DENetPresentValueVsRate");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("discountRate"));
		v.add (Translator.swap ("DENetPresentValueVsRate.yLabel"));
		return v;
	}

	@Override
	public double getYValue(double r, EconomicScenario es) {
		return es.calcNetPresentValue(r, es.getStandardizedEconomicOperations(), es.getFirstDate());
	}

	//	/**
	//	 * From DFCurves interface.
	//	 */
	//	public List<List<String>> getLabels () {return labels;}
	//
	//	/**
	//	 * From Extension interface.
	//	 */
	//	public String getVersion () {return VERSION;}
	//	public static final String VERSION = "1.0";
	//
	//	/**
	//	 * From Extension interface.
	//	 */
	//	public String getAuthor () {return "G. Ligot";}
	//
	//	/**
	//	 * From Extension interface.
	//	 */
	//	public String getDescription () {return Translator.swap ("DENetPresentValue.description");}



}
