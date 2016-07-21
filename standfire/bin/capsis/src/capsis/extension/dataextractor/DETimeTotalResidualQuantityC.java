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
import capsis.util.Group;
import capsis.util.methodprovider.AgeTotalAccumulationQuantityNProvider;
import capsis.util.methodprovider.AgeTotalResidualQuantityCProvider;

/**	Total Residual Quantity of Carbon of Litter Fall over Time (according to the model of R. D'annunzio, Phd thesis, 2007)
*	@author Zhun MAO - november 2008
*/
public class DETimeTotalResidualQuantityC extends PaleoDataExtractor implements DFCurves {
	protected Vector curves;
	protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeTotalResidualQuantityC");
	}

	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/

	public DETimeTotalResidualQuantityC () {}

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public DETimeTotalResidualQuantityC (GenericExtensionStarter s) {
		super (s);
		try {
			curves = new Vector ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeTotalResidualQuantityC.c ()", "Exception occured while object construction : ", e);
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
			if (!(mp instanceof AgeTotalAccumulationQuantityNProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeTotalResidualQuantityC.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**	This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);
		addConfigProperty (PaleoDataExtractor.CELL_GROUP);		// group multiconfiguration
		addConfigProperty (PaleoDataExtractor.I_CELL_GROUP);		// group individual configuration
	}

	/**	From DataExtractor SuperClass.
	*
	*	Computes the data series. This is the real output building.
	*	It needs a particular Step.
	*	This output computes Annual Quantity of Litter Fall of the stand versus date
	*	from the root Step to this one.
	*
	*	Return false if trouble while extracting.
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

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				GScene stand = s.getScene ();

				Collection cells = doFilter (stand,Group.CELL);

				int date = stand.getDate ();
				double quantityTotalResidualQuantityC = 0d;

					quantityTotalResidualQuantityC = ((AgeTotalResidualQuantityCProvider) methodProvider).getTotalResidualQuantityC (stand);

				c1.add (new Integer (date));
				c2.add (new Double (quantityTotalResidualQuantityC));
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeTotalResidualQuantityC.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**	From DataFormat interface.
	*	From Extension interface.
	*/
	public String getName () {
		return getNamePrefix ()+Translator.swap ("DETimeTotalResidualQuantityC");
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
		v.add (Translator.swap ("DETimeTotalResidualQuantityC.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DETimeTotalResidualQuantityC.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DETimeTotalResidualQuantityC.yLabel"));
		}
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
	public static final String VERSION = "1.0";

	/**	From Extension interface.
	*/
	public String getAuthor () {return "Zhun MAO";}

	/**	From Extension interface.
	*/
	public String getDescription () {return Translator.swap ("DETimeTotalResidualQuantityC.description");}



}


