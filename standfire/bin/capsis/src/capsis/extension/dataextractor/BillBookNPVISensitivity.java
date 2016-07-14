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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JCheckBox;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.extension.DEMultiConfPanel;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFColoredCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.BBCashFlow;
import capsis.lib.economics.BillBookCompatible;
import capsis.lib.economics.FinancialTools;
import capsis.util.ConfigurationPanel;

/**	BillBook extension : sensitivity of NPVI : Net Present Value NPVI = "BAo"
*	@author F. de Coligny, O. Pain - may 2008
*/
public class BillBookNPVISensitivity extends PaleoDataExtractor implements DFColoredCurves {
	protected Vector curves;
	protected Vector labels;
	protected Vector colors;
	//~ protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.BillBookNPVISensitivity");
	}

	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public BillBookNPVISensitivity () {}

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public BillBookNPVISensitivity (GenericExtensionStarter s) {
		super (s);
		try {
			curves = new Vector ();
			labels = new Vector ();
			colors = new Vector ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "BillBookNPVISensitivity.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			//~ MethodProvider mp = m.getMethodProvider ();
			//~ if (!(mp instanceof GProvider)) {return false;}

			Project sc = m.getProject ();
			Step root = (Step) sc.getRoot ();
			GScene std = root.getScene ();
			return std instanceof BillBookCompatible;

		} catch (Exception e) {
			Log.println (Log.ERROR, "BillBookNPVISensitivity.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	/**	This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		//~ addConfigProperty (DataExtractor.HECTARE);
		//~ addConfigProperty (DataExtractor.TREE_GROUP);		// group multiconfiguration
		//~ addConfigProperty (DataExtractor.I_TREE_GROUP);		// group individual configuration
	}

	private void updateConfigProperties (Collection<String> soldProductNames, Collection<String> wpNames)	 {
		settings.booleanProperties.clear ();
		for (String v : soldProductNames) {
			String propName = "sdn_"+v;
			//~ String propName = v;
			addBooleanProperty (propName, Settings.getProperty (propName, false));
		}

		for (String v : wpNames) {
			String propName = "wpn_"+v;
			//~ String propName = v;
			addBooleanProperty (propName, Settings.getProperty (propName, false));
		}

		addBooleanProperty ("includeAnnualAndVariableCosts",  
				Settings.getProperty ("includeAnnualAndVariableCosts", false));
		addDoubleProperty ("discountRate", 4d);
	}

	/**
	 * MultiConfigurable interface. Redefinition of DataExtractor.multiConfigure ().
	 *
	 */
	public void sharedConfigure (ConfigurationPanel panel) {
		// 1. do the job in the super class method
		super.sharedConfigure (panel);

		// 2. memorize the checkboxes selections in environment variables
		DEMultiConfPanel p = (DEMultiConfPanel) panel;

		// Set booleanProperties
		//
		Iterator keys = p.booleanPropertiesCheckBoxes.keySet ().iterator ();
		Iterator values = p.booleanPropertiesCheckBoxes.values ().iterator ();
		while (keys.hasNext () && values.hasNext ()) {
			String name = (String) keys.next ();
			JCheckBox cb = (JCheckBox) values.next ();
			if (isIndividualProperty (name)) {continue;}

			Settings.setProperty (name, ""+cb.isSelected ());

			//~ settings.booleanProperties.remove (name);
			//~ settings.booleanProperties.put (name, new Boolean (cb.isSelected ()));
		}


	}

	/**	From DataExtractor SuperClass.
	*
	*	Computes the data series. This is the real output building.
	*	It needs a particular Step.
	*	This output computes the basal area of the stand versus date
	*	from the root Step to this one.
	*
	*	Return false if trouble while extracting.
	*/
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		// Retrieve method provider
		//~ methodProvider = step.getScenario ().getModel ().getMethodProvider ();

		try {
			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);

			BBCashFlow cf = new BBCashFlow (steps, isSet ("includeAnnualAndVariableCosts"));

			updateConfigProperties (cf.getSoldProductNames (), cf.getWpNames ());

			//
			Collection<String> selectedNames = new ArrayList<String> ();
			Iterator keys = settings.booleanProperties.keySet ().iterator ();
			Iterator values = settings.booleanProperties.values ().iterator ();
			while (keys.hasNext () && values.hasNext ()) {
				String name = (String) keys.next ();
				Boolean yep = (Boolean) values.next ();
				if (name.equals ("includeAnnualAndVariableCosts")) {continue;}
				if (isIndividualProperty (name)) {continue;}
				if (yep) {
					// remove prefix
					name = name.substring (name.indexOf ('_')+1);
					selectedNames.add (name);
				}
			}

			Vector c1 = new Vector ();		// x coordinates
			//~ Vector c2 = new Vector ();		// y coordinates
			Vector[] cy = new Vector[selectedNames.size ()];


			int variationBound = 20;
			int variationStep = 5;
			Collection<Double> variations = new ArrayList<Double> ();
			Collection<Integer> xAnchors = new ArrayList<Integer> ();	// the curves renderer needs int x anchors
			for (int i = -variationBound; i<= variationBound; i+= variationStep) {
				double v = 1 + (((double) i)/100);
				variations.add (v);
				xAnchors.add (i);
			}

			double rate = getDoubleProperty ("discountRate");
			// Security
			if (rate <= 0d) {rate = 4d;}
			int i = 0;
			for (String selectedName : selectedNames) {
				cy[i] = new Vector ();
				for (double variation : variations) {

					BBCashFlow result = cf.applyVariation (selectedName, variation);

					double x = variation;
					double NPVI = FinancialTools.getNetPresentValueI (rate/100, result.getYears (), result.getCashFlows ());

					if (i == 0) {c1.add (x);}
					cy[i].add (NPVI);
				}
				i++;
			}


			// Data extraction : points with (Integer, Double) coordinates
			//~ for (Iterator i = steps.iterator (); i.hasNext ();) {
				//~ Step s = (Step) i.next ();

				//~ // Consider restriction to one particular group if needed
				//~ GStand stand = s.getStand ();
				//~ Collection trees = doFilter (stand);		// fc - 5.4.2004

				//~ int date = stand.getDate ();
				//~ double G = ((GProvider) methodProvider).getG (stand, trees) * coefHa;	// fc - 24.3.2004

				//~ c1.add (new Integer (date));
				//~ c2.add (new Double (G));
			//~ }

			curves.clear ();
			colors.clear ();
			curves.add (new Vector (xAnchors));
			//~ curves.add (c2);
			for (int k = 0; k < cy.length; k++) {
				curves.add (cy[k]);
				colors.add (getSomeColor ());
			}

			labels.clear ();
			Vector xLabels = new Vector ();
			for (double v : variations) {
				xLabels.add (""+v);
			}
			labels.add (xLabels);		// x labels

			Iterator it = selectedNames.iterator ();
			Vector[] ly = new Vector[selectedNames.size ()];
			for (int k = 0; k < ly.length; k++) {
				ly[k] = new Vector ();
				ly[k].add ((String) it.next ());
				labels.add (ly[k]);
			}

			//~ Vector y1Labels = new Vector ();
			//~ y1Labels.add ("Bois fort");
			//~ labels.add (y1Labels);
			//~ if (totalVolumeMode) {
				//~ Vector y2Labels = new Vector ();
				//~ y2Labels.add ("Total aérien");
				//~ labels.add (y2Labels);
			//~ }
		} catch (Exception exc) {
			Log.println (Log.ERROR, "BillBookNPVISensitivity.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	private Color getSomeColor () {
		Random random = new Random ();
		//~ int r = random.nextInt (256);
		//~ int g = random.nextInt (256);
		//~ int b = random.nextInt (256);
		float h = random.nextFloat ();
		float s = 0.8f;		// dark enough
		float b = 0.8f;		// dark enough

		return Color.getHSBColor (h, s, b);
	}

	/**	From DataFormat interface.
	*	From Extension interface.
	*/
	public String getName () {
		String name = getNamePrefix ()+Translator.swap ("BillBookNPVISensitivity");
		if (isSet ("includeAnnualAndVariableCosts")) {
			name+= " - ";
			name+=Translator.swap ("includeAnnualAndVariableCosts");
		}
		return name;
	}

	/**	From DFCurves interface.
	*/
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**	From DFCurves interface.
	*/
	public List<List<String>> getLabels () {
		return labels;
	}

	/**	DFColoredCurves.
	*	Returns a color per curve: getCurves ().size () - 1.
	*/
	public Vector getColors () {
		return colors;
	}

	/**	From DFCurves interface.
	*/
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("BillBookNPVISensitivity.xLabel"));
		//~ if (settings.perHa) {
			//~ v.add (Translator.swap ("BillBookNPVISensitivity.yLabel")+" (ha)");
		//~ } else {
			v.add (Translator.swap ("BillBookNPVISensitivity.yLabel"));
		//~ }
		return v;
	}

	/**	From DFCurves interface.
	*/
	public int getNY () {
		return curves.size () - 1;
	}

	/**	From Extension interface.
	*/
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**	From Extension interface.
	*/
	public String getAuthor () {return "F. de Coligny, O. Pain";}

	/**	From Extension interface.
	*/
	public String getDescription () {return Translator.swap ("BillBookNPVISensitivity.description");}



}


