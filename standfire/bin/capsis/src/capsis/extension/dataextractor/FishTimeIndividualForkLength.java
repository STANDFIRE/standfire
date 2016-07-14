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
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.FishModel;
import capsis.util.GFish;
import capsis.util.Group;

/**
 * Individual fork length over date.
 *
 * @author J. Labonne - august 2004
 */
public class FishTimeIndividualForkLength extends PaleoDataExtractor implements DFCurves { // DataFormat subinterface
	protected Vector curves;  	// Integer for x and Double pour y
	//~ protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.FishTimeIndividualForkLength");  //axis translation
	}

	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public FishTimeIndividualForkLength () {}    // usual for extensions, not operational

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public FishTimeIndividualForkLength (GenericExtensionStarter s) {
		//~ super (s.getStep ());
		super (s);
		curves = new Vector ();
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FishModel)) {return false;}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FishTimeIndividualForkLength.matchWith ()",
						"Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**	This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		addGroupProperty (Group.FISH, PaleoDataExtractor.COMMON);		// fc - 15.9.2004
		addGroupProperty (Group.FISH, PaleoDataExtractor.INDIVIDUAL);	// fc - 15.9.2004
	}

	/**	From DataExtractor SuperClass.
	*
	*	Computes the data series. This is the real output building.
	*	It needs a particular Step.
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
			Vector steps = step.getProject ().getStepsFromRoot (step); // protected VI from dataextractor , reference step from root
			// return the x values vector, getstepfromroots
			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				GScene stand = (GScene) s.getScene ();
				Collection fishes = doFilter (stand, Group.FISH);

				int date = stand.getDate ();

				// Rounded under
				for (Iterator j = fishes.iterator (); j.hasNext ();) {
					GFish f = (GFish) j.next ();

					double fl = (double) f.getForkLength ();	// fl : cm
					c1.add (new Integer (date));
					c2.add (new Double (fl));
				}

			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "FishTimeIndividualForkLength.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**	From DataFormat interface.
	*/
	public String getName() {

		// fc - 15.9.2004
		return getNamePrefix ()+Translator.swap ("FishTimeIndividualForkLength");
	}

	/**	From DFCurves interface.
	*/
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("FishTimeIndividualForkLength.xLabel"));
		v.add (Translator.swap ("FishTimeIndividualForkLength.yLabel"));
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
	public List<List<String>> getLabels () {
		return null;	// optional : unused
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return 1;
	}

	/**	From Extension interface.
	*/
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**	From Extension interface.
	*/
	public String getAuthor () {return "J. Labonne";}

	/**	From Extension interface.
	*/
	public String getDescription () {return Translator.swap ("FishTimeIndividualForkLength.description");}




}
