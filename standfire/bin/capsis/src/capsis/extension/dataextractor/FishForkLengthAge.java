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
 * Fishes fork length vs age scatterplot.
 *
 * @author J. Labonne - september 2004
 */
public class FishForkLengthAge extends PaleoDataExtractor implements DFCurves {
	private Vector labels;
	protected Vector curves;

	static {
		Translator.addBundle("capsis.extension.dataextractor.FishForkLengthAge");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public FishForkLengthAge () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public FishForkLengthAge (GenericExtensionStarter s) {
		//~ this (s.getStep ());
		//~ curves = new Vector ();
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected FishForkLengthAge (Step stp) {
		super (s);
		curves = new Vector ();
		labels = new Vector ();
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FishModel)) {return false;}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FishForkLengthAge.matchWith ()",
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
		addGroupProperty (Group.FISH, PaleoDataExtractor.COMMON);		// fc - 15.9.2004
		addGroupProperty (Group.FISH, PaleoDataExtractor.INDIVIDUAL);	// fc - 15.9.2004
	}

	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 * This extractor computes ForkLength distribution and Age distribution.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		//System.out.println ("FishForkLengthAge : extraction being made");

		try {
			GScene stand = (GScene) step.getScene ();
			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates

			// Restriction to a group if needed
			Collection aux = doFilter (step.getScene (), Group.FISH);		// fc - 15.9.2004
			Iterator j = aux.iterator();

			// Create output data
			while (j.hasNext ()) {
				GFish f = (GFish) j.next ();

				double fl = f.getForkLength ();		// fl : cm
				double a = f.getAge ();	// age in month

				c1.add (new Double (a));
				c2.add (new Double (fl));
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "FishForkLengthAge.doExtraction ()", "Exception caught : ",exc);
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
		return getNamePrefix ()+Translator.swap ("FishForkLengthAge");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("FishForkLengthAge.xLabel"));
		v.add (Translator.swap ("FishForkLengthAge.yLabel"));
		return v;
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
	public String getDescription () {return Translator.swap ("FishForkLengthAge.description");}



}





