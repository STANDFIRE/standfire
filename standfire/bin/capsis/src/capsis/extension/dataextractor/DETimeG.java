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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.GProvider;

/**	Basal area ("Grundfl�che" (G), "surface terri�re" en allemand) over Time.
*	@author F. de Coligny - november 2000
*/
public class DETimeG extends PaleoDataExtractor implements DFCurves {
	protected List<List<? extends Number>> curves;
	protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeG");
	}

	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public DETimeG () {}

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public DETimeG (GenericExtensionStarter s) {
		super (s);
		try {
			curves = new ArrayList<List<? extends Number>> ();
			setPropertyEnabled ("showIncrement", true); // PhD 2008-06-25
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeG.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			MethodProvider mp = m.getMethodProvider ();
			if (!(mp instanceof GProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeG.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**	This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);		// group multiconfiguration
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration
		addBooleanProperty ("showIncrement"); // PhD 2008-06-25
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
	public boolean doExtraction () throws Exception {

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
			// modified in order to show increment instead of direct value, depending on "showIncrement" button (to be changed in Configuration (Common)) - PhD 2008-06-25
			double read = 0, value, previous = 0;
			int date;
			Iterator i = steps.iterator ();

			if (i.hasNext ()) { // if a least one step (... !)
				Step s = (Step) i.next ();
				// Consider restriction to one particular group if needed
				GScene stand = s.getScene ();
				Collection trees = doFilter (stand);
				date = stand.getDate ();
				read = ((GProvider) methodProvider).getG (stand, trees) * coefHa;	// fc - 24.3.2004

				if (!i.hasNext()) { // if only 1 step (no second step), the 1st value or a null increment is added, and extraction is finished
					c1.add (new Integer (date));
					if (isSet ("showIncrement")) { // PhD 2008-06-25
						c2.add (new Double (0));
					} else {
						c2.add (new Double (read));
					}
				} else { // there is a 2nd step (and possibly more steps)
					if (isSet ("showIncrement")) { // PhD 2008-06-25
						previous = read;
						// ... what was read is assigned to "previous" - nothing is added at now
					} else { // value is added
						c1.add (new Integer (date));
						c2.add (new Double (read));
					}
				}

				while(i.hasNext ()) { // ... beginning at the second date, if any
					s = (Step) i.next ();
					// Consider restriction to one particular group if needed
					stand = s.getScene ();
					trees = doFilter (stand);
					date = stand.getDate ();
					read = ((GProvider) methodProvider).getG (stand, trees) * coefHa;	// fc - 24.3.2004
					c1.add (new Integer (date));
					if (isSet ("showIncrement")) { // PhD 2008-06-25
						c2.add (new Double (read - previous));
						previous = read;
					} else {
						c2.add (new Double (read));
					}
				}
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeG.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**	From DataFormat interface.
	*	From Extension interface.
	*/
	public String getName () {
		return getNamePrefix ()+Translator.swap ("DETimeG");
	}

	/**	From DFCurves interface.
	*/
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**	From DFCurves interface.
	*/
	public List<List<String>> getLabels () {
		return null;	// optional : unused
	}

	/**	From DFCurves interface.
	*/
	public List<String> getAxesNames () {
			Vector v = new Vector ();
			v.add (Translator.swap ("DETimeG.xLabel"));
			/*if (settings.perHa) {
				v.add (Translator.swap ("DETimeG.yLabel")+" (ha)");
			} else {
				v.add (Translator.swap ("DETimeG.yLabel"));
			}*/
			String yLab;
			yLab = Translator.swap ("DETimeG.yLabel");
			if (settings.perHa) {
				yLab = yLab +" (ha)";
			}
			if (isSet ("showIncrement")) { // PhD 2008-06-25
				yLab = "d " + yLab;
			}
			v.add (yLab);
			return v;
	}

	/**	From DFCurves interface.
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
	public String getAuthor () {return "F. de Coligny";}

	/**	From Extension interface.
	*/
	public String getDescription () {return Translator.swap ("DETimeG.description");}



}


