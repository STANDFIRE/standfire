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
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.StemBiomProvider;

/**
 * Stem Biomass versus Date.
 *
 * @author L. Saint-André - august 2002
 */
public class DETimeSTEMB extends PaleoDataExtractor implements DFCurves {	// MultiConfigurable goes up to DataExtractor
	protected Vector curves;
	protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeSTEMB");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DETimeSTEMB () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DETimeSTEMB (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DETimeSTEMB (Step stp) {
		super (s);
		try {
			curves = new Vector ();
			// MOVED TO SUPERCLASS retrieveSettings ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeSTEMB.c ()", "Exception occured while object construction : ", e);
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
			if (!(mp instanceof StemBiomProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeSTEMB.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);
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
			// per Ha computation
			double coefHa = 1;
			if (settings.perHa) {
				coefHa = 10000 / step.getScene ().getArea ();
			}

			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				GScene stand = s.getScene ();
				Collection trees = doFilter (stand);

				int date = stand.getDate ();
				double stemBiomass = ((StemBiomProvider) methodProvider).
						getStemWoodBiom (stand, trees) * coefHa;

				c1.add (new Integer (date));
				c2.add (new Double (stemBiomass));
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeSTEMB.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * This prefix is built depending on current settings.
	 * ex: "+ 25 years /ha"
	 */
	// fc - 9.4.2004 - inherited.
	//~ protected String getNamePrefix () {
		//~ String prefix = "";
		//~ try {
			//~ if (isCommonGrouper ()
					//~ && isGrouperMode () 
					//~ && GrouperManager.getInstance ().getGrouperNames ().contains (getGrouperName ())) {
				//~ prefix += getGrouperName ()+" - ";
			//~ }
			//~ if (settings.perHa) {prefix += "/ha - ";}
		//~ } catch (Exception e) {}	// if trouble, prefix is empty
		//~ return prefix;
	//~ }

	/**
	 * From DataFormat interface.
	 * From Extension interface.
	 */
	public String getName () {
		return getNamePrefix ()+Translator.swap ("DETimeSTEMB");
	}

	/**
	 * From DataFormat interface.
	 */
	// fc - 21.4.2004 - DataExtractor.getCaption () is better
	//~ public String getCaption () {
		//~ return getStep ().getCaption ();
	//~ }

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
		return null;	// optional : unused
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeSTEMB.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DETimeSTEMB.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DETimeSTEMB.yLabel"));
		}
		return v;
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
	public String getAuthor () {return "L. Saint-André";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeSTEMB.description");}



}


