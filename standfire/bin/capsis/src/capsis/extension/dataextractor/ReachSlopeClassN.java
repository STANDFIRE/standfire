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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GScene;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.FishModel;
import capsis.util.GReach;
import capsis.util.Group;


/**
 * Number od reaches per slope classes.
 *
 * @author J. Labonne - august 2004 (fully suggested by the design of B.Courbaud DEVolumeClassN extractor).
 */
public class ReachSlopeClassN extends PaleoDataExtractor implements DFCurves {
	public static final int MAX_FRACTION_DIGITS = 2;
	protected Vector curves;  // Integer for x  and Double for y
	protected Vector labels;
	protected NumberFormat formater;

	static {
		Translator.addBundle("capsis.extension.dataextractor.ReachSlopeClassN");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public ReachSlopeClassN () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public ReachSlopeClassN (GenericExtensionStarter s) {
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
			if (!(referent instanceof FishModel)) {return false;}

			} catch (Exception e) {
				Log.println (Log.ERROR, "ReachSlopeClassN.matchWith ()",
						"Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		addDoubleProperty ("classWidthIn%", 1d);
		addDoubleProperty ("minThresholdIn%", 0d);  //equivalent to ForkLengthMin in result but not in mechanism.
		addBooleanProperty ("centerClasses");
		addBooleanProperty ("frequency");		//j.l. 28.09.2004
		addGroupProperty (Group.REACH, PaleoDataExtractor.COMMON);		// fc - 17.9.2004
		addGroupProperty (Group.REACH, PaleoDataExtractor.INDIVIDUAL);	// fc - 17.9.2004
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
			double minThreshold = getDoubleProperty ("minThresholdIn%");
			double classWidth = getDoubleProperty ("classWidthIn%");

			// Security
			if (classWidth <= 0d) {classWidth = 1d;}

			double shift = 0;
			if (isSet ("centerClasses")) {shift = classWidth/2;}

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector l1 = new Vector ();		// labels for x axis (ex: 0-10, 10-20...)

			// Restriction to a group if needed
			//Step s = step.getLastStep();
			GScene stand = (GScene) step.getScene ();
			Collection reaches = doFilter (stand, Group.REACH);	// fc - 17.9.2004

			// Limited in size! (java initializes each value to 0)
			int tab[] = new int[200];
			int maxCat = 0;
			int minCat = 200;

			// Create output data

			//~ Collection refReaches = stand.getReachMap ().values ();
			for (Iterator k = reaches.iterator (); k.hasNext ();) {	// fc - 17.9.2004
				GReach refReach = (GReach) k.next ();

				double rs = (double) refReach.getSlope();		//

				if (rs < minThreshold) {continue;}	// fc - 10.4.2003

				int category = (int) ((rs-shift) / classWidth);

				tab [category] += 1;

				if (category > maxCat) {maxCat = category;}
				if (category < minCat) {minCat = category;}
			}

			int anchor = 1;
			for (int i = minCat; i <= maxCat; i++) {
				//~ c1.add (new Integer (anchor++));
				c1.add (new Integer (i));

				if (isSet ("frequency")) {					// enable the frequency mode. Useful for comparing different trajectories in distribution extractors. j.l. 28.09.2004.
					double numbers = (double) (((double)(tab[i]))/reaches.size());
					c2.add (new Double (numbers));
				} else {
					int numbers = (int) (tab[i]);
					c2.add (new Integer (numbers));
				}

				double classBase = shift + i*classWidth;
				l1.add (""+formater.format (classBase)+"-"+formater.format ((classBase+classWidth)));

			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

			labels.clear ();
			labels.add (l1);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "ReachSlopeClassN.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {

		return getNamePrefix ()+Translator.swap ("ReachSlopeClassN");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("ReachSlopeClassN.xLabel"));
		if (isSet ("frequency")) {v.add (Translator.swap ("ReachSlopeClassN.yLabel2"));
		} else {v.add (Translator.swap ("ReachSlopeClassN.yLabel"));}
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<String>> getLabels () {
		return labels;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return 1;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "J. Labonne";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("ReachSlopeClassN.description");}



}




