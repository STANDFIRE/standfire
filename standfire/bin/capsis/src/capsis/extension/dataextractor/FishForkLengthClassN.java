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
import capsis.util.GFish;
import capsis.util.Group;


/**
 * Number of fish per fork length class.
 *
 * @author J. Labonne - august 2004 (fully suggested by the design of B.Courbaud DEVolumeClassN extractor).
 */
public class FishForkLengthClassN extends PaleoDataExtractor implements DFCurves {
	public static final int MAX_FRACTION_DIGITS = 2;
	protected Vector curves;  // Integer for x  and Double for y
	protected Vector labels;
	protected NumberFormat formater;

	static {
		Translator.addBundle("capsis.extension.dataextractor.FishForkLengthClassN");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public FishForkLengthClassN () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public FishForkLengthClassN (GenericExtensionStarter s) {
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
				Log.println (Log.ERROR, "FishForkLengthClassN.matchWith ()",
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
		addDoubleProperty ("classWidthInCM", 1d);
		addDoubleProperty ("minThresholdInCM", 0d);
		addBooleanProperty ("centerClasses");
		addBooleanProperty ("frequency");		//j.l. 28.09.2004
		addGroupProperty (Group.FISH, PaleoDataExtractor.COMMON);	// fc - 13.9.2004
		addGroupProperty (Group.FISH, PaleoDataExtractor.INDIVIDUAL);	// fc - 13.9.2004
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
			double minThreshold = getDoubleProperty ("minThresholdInCM");
			double classWidth = getDoubleProperty ("classWidthInCM");

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
			Collection fishes = doFilter (stand, Group.FISH);		// fc - 15.9.2004

			// Limited in size! (java initializes each value to 0)
			int tab[] = new int[200];
			int maxCat = 0;
			int minCat = 200;

			// Create output data


			Iterator j = fishes.iterator();
			while (j.hasNext ()) {
				GFish f = (GFish) j.next ();

				double fl = (double) f.getForkLength();
				if (fl < minThreshold) {continue;}

				int category = (int) ((fl-shift) / classWidth);


				tab [category] += 1;

				if (category > maxCat) {maxCat = category;}
				if (category < minCat) {minCat = category;}
			}



			int anchor = 1;
			for (int i = minCat; i <= maxCat; i++) {
				//~ c1.add (new Integer (anchor++));
				c1.add (new Integer (i));

				if (isSet ("frequency")) {					// enable the frequency mode. Useful for comparing different trajectories in distribution extractors. j.l. 28.09.2004.
					double numbers = (double) (((double)(tab[i]))/fishes.size());
					System.out.println("taille de fishes : " + fishes.size());
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
			Log.println (Log.ERROR, "GForkLengthClassN.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {

		// fc - 15.9.2004
		return getNamePrefix ()+Translator.swap ("FishForkLengthClassN");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<? extends Number>> getCurves () {  //has to be called here, as no inheritance from DETimeG.
		return curves;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {  //has to be called here, as no inheritance from DETimeG.
		Vector v = new Vector ();
		v.add (Translator.swap ("FishForkLengthClassN.xLabel"));
		if (isSet ("frequency")) {v.add (Translator.swap ("FishForkLengthClassN.yLabel2"));
		} else {v.add (Translator.swap ("FishForkLengthClassN.yLabel"));}
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<String>> getLabels () {  //has to be called here, as no inheritance from DETimeG.
		return labels;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {    //has to be called here, as no inheritance from DETimeG.
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
	public String getDescription () {return Translator.swap ("FishForkLengthClassN.description");}



}




