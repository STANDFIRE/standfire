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
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.TotalAboveGroundVolumeProvider;
import capsis.util.methodprovider.VMerchantProviderWithName;
import capsis.util.methodprovider.VProvider;
import capsis.util.methodprovider.VProviderWithName;

/**
 * Total volume versus Date.
 *
 * @author N. Robert - July 2008; adapted from DETimeProducedV by P. Vallet - autumn 2002
 */
public class DETimeAverageProducedV extends DETimeG {
// checked for capsis4.1.2 - fc - 16.6.2003

	protected boolean totalVolumeMode ;
	protected Vector labels;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeAverageProducedV");
	}


	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DETimeAverageProducedV () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DETimeAverageProducedV (GenericExtensionStarter s) {
		//~ super (s.getStep ());
		super (s);
		labels = new Vector ();

		documentationKeys.add ("VProvider");	// tl cm - 29.1.2007
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
			if (!(mp instanceof VProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeAverageProducedV.matchWith ()",
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
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);	// fc - 9.4.2004

		// Properties in the form boxTitle_componentTitle :
		addBooleanProperty ("DETimeAverageProducedV.avprod_normal", true);
		addBooleanProperty ("DETimeAverageProducedV.avprod_merchant", false);
		addBooleanProperty ("DETimeAverageProducedV.avprod_totag", true);
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

		totalVolumeMode = false;
		if (methodProvider instanceof TotalAboveGroundVolumeProvider) {totalVolumeMode = true;}

		boolean normalVolumeMode = isSet ("DETimeAverageProducedV.avprod_normal");
		boolean merchantVolumeMode = isSet ("DETimeAverageProducedV.avprod_merchant")
				&& methodProvider instanceof VMerchantProviderWithName;
		boolean totalVolumeMode = isSet ("DETimeAverageProducedV.avprod_totag")
				&& methodProvider instanceof TotalAboveGroundVolumeProvider;

		try {
			// per Ha computation
			double coefHa = 1;
			if (settings.perHa) {
				coefHa = 10000 / step.getScene ().getArea ();
			}

			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);

			Vector c0 = new Vector ();		// x coordinates
			Vector c1 = new Vector ();		// y1 coordinates (normal)
			Vector c2 = new Vector ();		// y2 coordinates (merchant)
			Vector c3 = new Vector ();		// y3 coordinates (total above ground)

			double producedBFVolume = 0;
			double previousBFVolume = 0;
			double producedMerchantVolume = 0;
			double previousMerchantVolume = 0;
			double producedTAGVolume = 0;
			double previousTAGVolume = 0;

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restriction to one particular group if needed
				GScene stand = s.getScene ();
				Collection trees = doFilter (stand);

				int date = stand.getDate ();
				c0.add (new Integer (date));

				// fc-15.10.2010
				// date is at the denominator below -> never zero ! (bug found by BC)
				if (date == 0) {date = 1;}
				
				if (normalVolumeMode) {
					double bfVolume = ((VProvider) methodProvider).getV (stand, trees);

					if (stand.isInterventionResult()) {
						producedBFVolume += Math.max (0., previousBFVolume - bfVolume);
					}
					previousBFVolume = bfVolume;

					c1.add (new Double ((producedBFVolume+bfVolume) * coefHa/date));
				}

				if (merchantVolumeMode) {
					double merchantVolume = ((VMerchantProviderWithName) methodProvider).
							getVMerchant (stand, trees);
					if (stand.isInterventionResult()) {
						producedMerchantVolume += Math.max (
								0., previousMerchantVolume - merchantVolume);
					}
					previousMerchantVolume = merchantVolume;

					c2.add (new Double ((producedMerchantVolume + merchantVolume)
							* coefHa/date));
				}

				if (totalVolumeMode) {
					double totalAGVolume = ((TotalAboveGroundVolumeProvider) methodProvider).
							getTotalAboveGroundVolume (stand, trees);
					if (stand.isInterventionResult()) {
						producedTAGVolume += Math.max (
								0., previousTAGVolume - totalAGVolume);
					}
					previousTAGVolume = totalAGVolume;

					c3.add (new Double ((producedTAGVolume + totalAGVolume) * coefHa/date));
				}
			}

			curves.clear ();
			labels.clear ();
			addSeries (null, c0);		// no x labels

			if (normalVolumeMode) {
				String n1;
				if (methodProvider instanceof VProviderWithName) {
					n1 = ((VProviderWithName) methodProvider).getVolumeName ();
				} else {
					n1 = Translator.swap ("DETimeAverageProducedV.y1Label");
				}
				addSeries (n1, c1);
			}

			if (merchantVolumeMode) {
				String n2 = ((VMerchantProviderWithName) methodProvider).
							getVMerchantName ();
				addSeries (n2, c2);
			}
			if (totalVolumeMode) {
				addSeries (Translator.swap ("DETimeAverageProducedV.y3Label"), c3);
			}

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeAverageProducedV.doExtraction ()",
					"Exception caught : ",exc);
			return false;
		}

		// Debugging trouble - fc-15.10.2010
		trace ();
		
		
		upToDate = true;
		return true;
	}

	private void trace () {
		System.out.println("DETimeAverageProducedV debugging trace");
		System.out.println("getName () "+getName ());
		System.out.println("getCurves () "+getCurves ());
		for (Object o : getCurves ()) {
			Vector v = (Vector) o;
			System.out.println("* "+v);
			for (Object a : v) {
				System.out.println(""+a);
			}
		}
		System.out.println("getLabels () "+getLabels ());
		for (List<String> l : getLabels ()) {
			System.out.println("* "+l);
			for (String b : l) {
				System.out.println(""+b);
			}
		}
		
		System.out.println("end-of-trace");
		
	}
	
	
	/**
	*	Add a data series to curves and labels.
	*	label may be null for x coordinates.
	*/
	private void addSeries (String label, Vector coordinates) {
		curves.add (coordinates);
		Vector yLabels = new Vector ();
		if (label != null) {
			yLabels.add (label);
		}
		labels.add (yLabels);
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DETimeAverageProducedV");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeAverageProducedV.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DETimeAverageProducedV.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DETimeAverageProducedV.yLabel"));
		}
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return curves.size()-1;
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
	public static final String VERSION = "0.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "N. Robert";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeAverageProducedV.description");}

}
