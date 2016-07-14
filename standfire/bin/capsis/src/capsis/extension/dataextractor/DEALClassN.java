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
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.ALProvider;

/**
 * Numbers of trees per level of available light .
 * 
 * @author S. Turbis - august 2005
 */
public class DEALClassN extends DETimeG {
	private Vector labels;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DEALClassN");
	}

	/**
	 * Phantom constructor. 
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DEALClassN () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DEALClassN (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DESpeciesClassN (Step stp) {
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
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();

			if (!(s instanceof TreeCollection)) {return false;}
			TreeCollection tc = (TreeCollection) s;			
			Tree t = tc.getTrees ().iterator ().next ();
			if (t instanceof Numberable) {return false;}
			if (!(t instanceof ALProvider)) {return false;}
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "DEALClassN.matchWith ()", 
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
		addConfigProperty (PaleoDataExtractor.HECTARE);
		addConfigProperty (PaleoDataExtractor.STATUS);	// fc - 22.4.2004
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration
	}

	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 * This extractor computes Numbers of trees per level classes.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		//System.out.println ("DEALClassN : extraction being made");

		try {
			// per Ha computation
			double coefHa = 1;
			if (settings.perHa) {
				coefHa = 10000 / step.getScene ().getArea ();
			}

			int n1 = 0; // Very Sunny
			int n2 = 0; // Moderatly Sunny
			int n3 = 0; // Little Sunny			
			int n4 = 0; // Not Sunny 
			

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector l1 = new Vector ();		// labels for x axis (ex: Dominant, ...)

			// Consider restriction to one particular group if needed
			GScene stand = step.getScene ();
			Collection trees = doFilter (stand);

			for (Iterator i = trees.iterator (); i.hasNext ();) {
				Tree t = (Tree) i.next ();
				
        ALProvider t2 = (ALProvider) t;
				// Available level classes
				double AL = t2.getAL();
				if (AL >= 0.75) {
					n1++;
				} else if ((AL >= 0.5) && (AL < 0.75) ) {
					n2++;
				} else if ((AL >= 0.25) && (AL < 0.5)) {
					n3++;
				} else if (AL < 0.25) {
					n4++;
				}
			}

			c1.add (new Integer (1));
			c1.add (new Integer (2));
			c1.add (new Integer (3));
			c1.add (new Integer (4));

			c2.add (new Integer ((int) (n1*coefHa)));
			c2.add (new Integer ((int) (n2*coefHa)));
			c2.add (new Integer ((int) (n3*coefHa)));
			c2.add (new Integer ((int) (n4*coefHa)));

			l1.add (Translator.swap ("DEALClassN.VerySunny")); 
			l1.add (Translator.swap ("DEALClassN.ModeratlySunny"));
			l1.add (Translator.swap ("DEALClassN.LittleSunny"));
			l1.add (Translator.swap ("DEALClassN.NotSunny"));			

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

			labels.clear ();
			labels.add (l1);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DEALClassN.doExtraction ()", 
          "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DEALClassN");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DEALClassN.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DEALClassN.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DEALClassN.yLabel"));
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
	public String getAuthor () {return "S. Turbis";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DEALClassN.description");}



}



