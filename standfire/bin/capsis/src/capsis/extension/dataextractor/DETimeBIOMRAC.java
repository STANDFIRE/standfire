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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.CoarseRootBiomProvider;
import capsis.util.methodprovider.LitterFineRootBiomProvider;
import capsis.util.methodprovider.MediumRootBiomProvider;
import capsis.util.methodprovider.SoilFineRootBiomProvider;
import capsis.util.methodprovider.StumpBiomProvider;

/**
 * Current Basal area increment and Mean Basal area increment ("Grundflache" (G), "surface terri�re") versus Date.
 *
 * @author L. Saint-André - august 2002
 */
public class DETimeBIOMRAC extends DETimeG {
	protected Vector curves;
	protected Vector labels;
	protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeBIOMRAC");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DETimeBIOMRAC () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DETimeBIOMRAC (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DETimeBIOMRAC (Step stp) {
		super (s);
		try {
			curves = new Vector ();
			labels = new Vector ();

			// This is Configuration stuff, not memorized in settings
			// Reminder: only MultiConfiguration stuff is memorized in settings.

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeBIOMRAC.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			MethodProvider mp = m.getMethodProvider ();

			if (!(mp instanceof StumpBiomProvider)) {return false;}
			if (!(mp instanceof SoilFineRootBiomProvider)) {return false;}
			if (!(mp instanceof LitterFineRootBiomProvider)) {return false;}
			if (!(mp instanceof MediumRootBiomProvider)) {return false;}
			if (!(mp instanceof CoarseRootBiomProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeBIOMRAC.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
	}

	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 * This extractor computes Number of trees in the stand versus Date.
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
			Vector c4 = new Vector ();		// y coordinates
			Vector c5 = new Vector ();		// y coordinates
			Vector c6 = new Vector ();		// y coordinates

			// data extraction : points with (Integer, Double) coordinates
			// Data extraction : points with (Integer, Double) coordinates
			Iterator i = steps.iterator ();
			do {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				GScene stand = s.getScene ();
				Collection trees = doFilter (stand);

				int date = stand.getDate ();

				double stumpBiom = ((StumpBiomProvider) methodProvider).
						getStumpBiom (stand, trees);
				double sFineRBBiom = ((SoilFineRootBiomProvider) methodProvider).
						getSoilFineRootBiom (stand, trees);
				double lFineRBiom = ((LitterFineRootBiomProvider) methodProvider).
						getLitterFineRootBiom (stand, trees);
				double medRBiom = ((MediumRootBiomProvider) methodProvider).
						getMediumRootBiom (stand, trees);
				double coarseRBiom = ((CoarseRootBiomProvider) methodProvider).
						getCoarseRootBiom (stand, trees);

				c1.add (new Integer (date));
				c2.add (new Double (stumpBiom));
				c3.add (new Double (sFineRBBiom));
				c4.add (new Double (lFineRBiom));
				c5.add (new Double (medRBiom));
				c6.add (new Double (coarseRBiom));

			} while (i.hasNext());

			curves.clear ();
			curves.add (c1);
			curves.add (c2);
			curves.add (c3);
			curves.add (c4);
			curves.add (c5);
			curves.add (c6);

			labels.clear ();
			labels.add (new Vector ());		// no x labels
			Vector y1Labels = new Vector ();
			y1Labels.add ("Stump");
			labels.add (y1Labels);
			Vector y2Labels = new Vector ();
			y2Labels.add ("SFineR");
			labels.add (y2Labels);
			Vector y3Labels = new Vector ();
			y3Labels.add ("LFineR");
			labels.add (y3Labels);
			Vector y4Labels = new Vector ();
			y4Labels.add ("MedR");
			labels.add (y4Labels);
			Vector y5Labels = new Vector ();
			y5Labels.add ("CoarseR");
			labels.add (y5Labels);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeBIOMRAC.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DETimeBIOMRAC");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeBIOMRAC.xLabel"));
			v.add (Translator.swap ("DETimeBIOMRAC.yLabel"));
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return 5;
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
	public String getAuthor () {return "L. Saint-André";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeBIOMRAC.description");}




}
