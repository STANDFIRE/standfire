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

import simmem.model.SimModel;
import simmem.model.SimScene;
import simmem.model.SimSceneWrapper;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;


/**
 * Relative dominant diameter versus relative density - Parameters used to drive Simmem simulations
 * Still does not group by scene parts!
 * @author C. Orazio  relative diameter by relative density (SIMMEM drivers) 2014
 */
public class DEDdom01Nha01 extends DETimeG {

	static {
		Translator.addBundle("capsis.extension.dataextractor.DEDdom01Nha01");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DEDdom01Nha01 () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DEDdom01Nha01 (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DEDdom01Nha01 (Step stp) {
		super (s);
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof SimModel)) {return false;}
			//SimModel m = (SimModel) referent;
			//MethodProvider mp = m.getMethodProvider ();
			//if (!(m instanceof SimModel)) {return false;}
			//if (!(mp instanceof DdomProvider)) {return false;}

			// Remove this matchWith with TC - tl cm 28 08 2012
			//GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			//if (!(s instanceof TreeCollection)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DEDdom01Nha01.matchWith ()", "Error in matchWith () (returned false)", e);
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
//		addConfigProperty (DataExtractor.STATUS);	// fc - 22.4.2004
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration
// tl 29.1.2007 from DEGirthClassN
		//addBooleanProperty ("roundN");	// fc - 22.8.2006 - n may be double (NZ1) -> round becomes an option
	}

	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 * This extractor computes Number of trees in the stand versus Date.
	 *
	 * Return false if trouble while extracting.
	 * @param <SimScene>
	 */
	public  boolean doExtraction () {
		//System.out.print ("DEDdom01Nha01 : extraction requested...");

		if (upToDate) {
			//System.out.println (" upToDate -> NO EXTRACTION");
			return true;
		}
		if (step == null) {
			//System.out.println (" null Step -> NO EXTRACTION");
			return false;
		}

		// Retrieve method provider
		//methodProvider = MethodProviderFactory.getMethodProvider (step.getScenario ().getModel ());
		//methodProvider = step.getProject ().getModel ().getMethodProvider ();

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

			// data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				SimScene scene = (SimScene) s.getScene ();
				Collection<SimSceneWrapper> wrappers = scene.getWrappers();
				for (SimSceneWrapper w:wrappers){
					c1.add(w.getDdom_0x());
					c2.add(w.getDensity_01());
				}
				//c1.add(null);
				//c2.add(null);
				//Collection trees = doFilter (stand);		// fc - 5.4.2004

				//int date = stand.getDate ();

				// Rounded under
				//c1.add (new Double (Cdom));
				//~ c2.add (new Integer (N));
				// c2.add (new Double (N)); // fc - 22.8.2006 - Numberable is double
				// tl 29.1.2007 from DEGirthClassN
				// New option: if (roundN), N is rounded to the nearest int
				/*double numbers = 0;
				if (isSet ("roundN")) {
					numbers = (int) (N + 0.5);	// fc - 29.9.2004 : +0.5 (sp)
				} else {
					numbers = N;	// fc - 22.8.2006 - Numberable is now double
				}
				c2.add (new Double (numbers));*/
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DEDdom01Nha01.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		//System.out.println (" MADE");
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DEDdom01Nha01");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DEDdom01Nha01.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DEDdom01Nha01.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DEDdom01Nha01.yLabel"));
		}
		return v;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "C. Orazio";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DEDdom01Nha01.description");}

}
