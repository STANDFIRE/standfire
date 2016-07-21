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
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.TreeDiameterClassProvider;


/**
 * Numbers of trees per large diameter classes,
 * based on DESpeciesClassN.
 *
 * @author B. Courbaud - 27/06/01
 */
public class DELargeDbhClassN extends DETimeG {
	private Vector labels;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DELargeDbhClassN");
	}


	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DELargeDbhClassN () {}


	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DELargeDbhClassN (GenericExtensionStarter s) {
		super (s);
		labels = new Vector ();
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
			if (!(mp instanceof TreeDiameterClassProvider)) {return false;}

			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeCollection)) {return false;}
			TreeCollection tc = (TreeCollection) s;
			if (tc.getTrees ().isEmpty ()) {return true;} // bare soil problem

		} catch (Exception e) {
			Log.println (Log.ERROR, "DELargeDbhClassN.matchWith ()", "Error in matchWith () (returned false)", e);
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
		addConfigProperty (PaleoDataExtractor.STATUS);		// fc - 22.4.2004
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration
	}


	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 * This extractor computes Numbers of trees per diameter classes.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}
		// Retrieve method provider
		methodProvider = step.getProject ().getModel ().getMethodProvider ();

		//System.out.println ("DELargeDbhClassN : extraction being made");

		try {
			// per Ha computation
			double coefHa = 1;
			if (settings.perHa) {
				coefHa = 10000 / step.getScene ().getArea ();
			}

			int n1 = 0;
			int n2 = 0;
			int n3 = 0;
			int n4 = 0;
			int n5 = 0;
			int n6 = 0;

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector l1 = new Vector ();		// labels for x axis (ex: Pin, Hetre...)

			// Consider restriction to one particular group if needed
			GScene stand = step.getScene ();
			Collection aux = doFilter (step.getScene ());
			Iterator trees = aux.iterator ();

			// Limited in size! (java initializes each value to 0)
			int tab[] = new int[200];
			int maxCat = 0;
			int minCat = 200;

			// Create output data
			while (trees.hasNext ()) {
				Tree t = (Tree) trees.next ();
				int d = ((TreeDiameterClassProvider) methodProvider).getTreeDiameterClass (t);

				if ((d < 1) || (d > 6)) {	// fc - 17.10.2001 - warn and bypass
					Log.println (Log.WARNING, "DELargeDbhClassN.doExtraction ()"
							, "diameter class="+d+" should be in [1, 6]. Nearest limit was taken.");
				}

				if (d <= 1) {	// fc - 17.10.2001 - changed == by <=
					n1++;
				} else if (d == 2) {
					n2++;
				} else if (d == 3) {
					n3++;
				} else if (d == 4) {
					n4++;}
				else if (d == 5) {	// bc 14/09/04
					n5++;
				}
				else if (d >= 6) {	// fc - 17.10.2001 - changed == by >=
					n6++;
				}
			}

			c1.add (new Integer (1));
			c1.add (new Integer (2));
			c1.add (new Integer (3));
			c1.add (new Integer (4));
			c1.add (new Integer (5));
			c1.add (new Integer (6));

			c2.add (new Integer ((int) (n1*coefHa)));
			c2.add (new Integer ((int) (n2*coefHa)));
			c2.add (new Integer ((int) (n3*coefHa)));
			c2.add (new Integer ((int) (n4*coefHa)));
			c2.add (new Integer ((int) (n5*coefHa)));
			c2.add (new Integer ((int) (n6*coefHa)));

			l1.add (Translator.swap ("DELargeDbhClassN.largeDbhClass1"));
			l1.add (Translator.swap ("DELargeDbhClassN.largeDbhClass2"));
			l1.add (Translator.swap ("DELargeDbhClassN.largeDbhClass3"));
			l1.add (Translator.swap ("DELargeDbhClassN.largeDbhClass4"));
			l1.add (Translator.swap ("DELargeDbhClassN.largeDbhClass5"));
			l1.add (Translator.swap ("DELargeDbhClassN.largeDbhClass6"));

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

			labels.clear ();
			labels.add (l1);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DELargeDbhClassN.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}


	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DELargeDbhClassN");
	}


	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DELargeDbhClassN.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DELargeDbhClassN.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DELargeDbhClassN.yLabel"));
		}
		return v;
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

	public static final String VERSION = "1.0";


	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "B. Courbaud";}


	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DELargeDbhClassN.description");}



}









