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

package capsis.extension.dataextractor;


import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.FishModel;
import capsis.util.FishStand;
import capsis.util.Group;
import capsis.util.NeighboursCounter;


/**
 * Numbers of reaches per order classes.
 *
 * @author J. Labonne - august 2004 (fully suggested by the design of B.Courbaud DEVolumeClassN extractor).
 */
public class ReachConnectivity extends PaleoDataExtractor implements DFCurves {
	public static final int MAX_FRACTION_DIGITS = 2;
	protected Vector curves;
	protected Vector labels;
	protected NumberFormat formater;

	static {
		Translator.addBundle("capsis.extension.dataextractor.ReachConnectivity");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public ReachConnectivity () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public ReachConnectivity (GenericExtensionStarter s) {
		//~ super (s.getStep ());
		super (s);
		curves = new Vector ();
		labels = new Vector ();

		// Used to format decimal part with 2 digits only
		formater = NumberFormat.getInstance ();
		formater.setMaximumFractionDigits (MAX_FRACTION_DIGITS);
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FishModel) ) {return false;}

			} catch (Exception e) {
				Log.println (Log.ERROR, "ReachConnectivity.matchWith ()",
						"Error in matchWith () (returned false)", e);
			return false;}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {  //si commence par un i_   alors dans individuel
		// Choose configuration properties
		addDoubleProperty ("classWidth", 1d);
		addDoubleProperty ("minThreshold", 0d);  //equivalent to ForkLengthMin in result but not in mechanism.
		addIntProperty ("connectivityRadius", 5);		// used to call the method computeLocalConnectivity in DynModel.
		addBooleanProperty ("centerClasses");
		addBooleanProperty ("frequency");		//j.l. 28.09.2004
		addGroupProperty (Group.REACH, PaleoDataExtractor.COMMON);		// fc - 16.9.2004
		addGroupProperty (Group.REACH, PaleoDataExtractor.INDIVIDUAL);	// fc - 16.9.2004
	}

	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		// Retrieve method provider
		//methodProvider = step.getScenario ().getModel ().getMethodProvider ();

		try {

			// Retrieve Steps from root to this step
			double minThreshold = getDoubleProperty ("minThreshold");
			double classWidth = getDoubleProperty ("classWidth");
			int radius = getIntProperty("connectivityRadius");

			// Security
			if (classWidth <= 0d) {classWidth = 1d;}

			double shift = 0;
			if (isSet ("centerClasses")) {shift = classWidth/2;}

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector l1 = new Vector ();		// labels for x axis (ex: 0-10, 10-20...)

			// Restriction to a group if needed
			//Step s = step.getLastStep();


			// Limited in size! (java initializes each value to 0)
			int tab[] = new int[100];
			int maxCat = 0;
			int minCat = 100;
			NeighboursCounter NC= new NeighboursCounter ();

			// Create output data
			FishStand stand =  (FishStand)step.getScene ();
			FishModel model = (FishModel) stand.getStep ().getProject ().getModel ();
			double [] data = NC.computeLocalConnectivity(stand.getReachMap().values(),radius);
			for (int i = 0; i<data.length; i++) {
				double c = data[i];
				if (c<minThreshold) {continue;}
				int category = (int) ((c-shift) / classWidth);
				tab [category] += 1;
				if (category > maxCat) {maxCat = category;}
				if (category < minCat) {minCat = category;}
			}


			int anchor = 1;
			for (int i = minCat; i <= maxCat; i++) {
				//~ c1.add (new Integer (anchor++));
				c1.add (new Integer (i));

				if (isSet ("frequency")) {					// enable the frequency mode. Useful for comparing different trajectories in distribution extractors. j.l. 28.09.2004.
					double numbers = (double) (((double)(tab[i]))/data.length);
					c2.add (new Double (numbers));
				} else {
					int numbers = (int) (tab[i]);
					c2.add (new Integer (numbers));
				}

				double classBase = shift + i*classWidth;
				l1.add (""+formater.format (classBase));

			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

			labels.clear ();
			labels.add (l1);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "ReachConnectivity.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {

		return getNamePrefix ()+Translator.swap ("ReachConnectivity");	// fc - 16.9.2004
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<? extends Number>> getCurves () {  // has to be defined here, is no more inherited from DETimeG.
		return curves;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {  // has to be defined here, is no more inherited from DETimeG.
		Vector v = new Vector ();
		v.add (Translator.swap ("ReachConnectivity.xLabel"));
		if (isSet ("frequency")) {v.add (Translator.swap ("ReachConnectivity.yLabel2"));
		} else {v.add (Translator.swap ("ReachConnectivity.yLabel"));}
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<String>> getLabels () {  // has to be defined here, is no more inherited from DETimeG.
		return labels;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {   // has to be defined here, is no more inherited from DETimeG.
		return 1;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "J. Labonne";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("ReachConnectivity.description");}



}




