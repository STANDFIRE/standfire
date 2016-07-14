/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Philippe Dreyfus
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

package capsis.extension.dataextractor;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.ForestGalesFormat;

/**
 * Evolution of ForestGales Return period calculated values along time.
 *
 * @author C. Meredieu - december 2002
 */
public class DETimeForestGalesProbabilityValues extends DETimeG {
	protected Vector curves;
	protected Vector labels;
	protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeForestGalesProbabilityValues");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DETimeForestGalesProbabilityValues () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DETimeForestGalesProbabilityValues (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DETimeForestGalesProbabilityValues (Step stp) {
		super (s);
		try {
			curves = new Vector ();
			labels = new Vector ();

			// This is Configuration stuff, not memorized in settings
			// Reminder: only MultiConfiguration stuff is memorized in settings.

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeForestGalesProbabilityValues.c ()",
					"Exception occured while object construction : ", e);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal
	 * (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;

			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof ForestGalesFormat)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeForestGalesProbabilityValues.matchWith ()",
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
		// nonsense - addConfigProperty (DataExtractor.TREE_GROUP);
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
		methodProvider = step.getProject ().getModel ().getMethodProvider ();

		try {
			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector c3 = new Vector ();		// y coordinates

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				GScene stand = s.getScene ();
				//~ Collection trees = doFilter (stand);	// fc - no possible groups

				ForestGalesFormat std = (ForestGalesFormat) stand;

				int date = stand.getDate ();

				double breakageProbability = std.getBreakageProbability ();
				double overturningProbability = std.getOverturningProbability ();

				c1.add (new Integer (date));
				c2.add (new Double (breakageProbability));
				c3.add (new Double (overturningProbability));
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);
			curves.add (c3);

			labels.clear ();

			labels.add (new Vector ());		// no x labels

			Vector y1Labels = new Vector ();
			y1Labels.add (Translator.swap ("DETimeForestGalesProbabilityValues.y1Label"));
			labels.add (y1Labels);

			Vector y2Labels = new Vector ();
			y2Labels.add (Translator.swap ("DETimeForestGalesProbabilityValues.y2Label"));
			labels.add (y2Labels);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeForestGalesProbabilityValues.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DETimeForestGalesProbabilityValues");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeForestGalesProbabilityValues.xLabel"));
		v.add (Translator.swap ("DETimeForestGalesProbabilityValues.yLabel"));
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return 2;
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
	public List<List<String>> getLabels () {
		return labels;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "C. Meredieu";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeForestGalesProbabilityValues.description");}




}
