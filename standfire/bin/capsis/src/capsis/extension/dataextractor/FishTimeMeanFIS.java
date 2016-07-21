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


import java.util.List;
import java.util.Map;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.FishModel;
import capsis.util.FishRecorder;
import capsis.util.FishStand;

/**
 * Number of fishes over date.
 *
 * @author F. de Coligny - july 2004
 */
public class FishTimeMeanFIS extends PaleoDataExtractor implements DFCurves { // DataFormat subinterface
	protected Vector curves;  // Integer for x and Double pour y
	private Vector labels;
	//~ protected MethodProvider methodProvider;


	static {
		Translator.addBundle("capsis.extension.dataextractor.FishTimeMeanFIS");  //axis translation
	}

	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public FishTimeMeanFIS () {}    // habitual for extensions, not operational

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public FishTimeMeanFIS (GenericExtensionStarter s) {
		//~ super (s.getStep ());
		super (s);
		curves = new Vector ();
		labels = new Vector ();
		//setSettings (bidSettings);	// to be standard
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FishModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FishTimeMeanFIS.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**	This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		//addGroupProperty (Group.FISH, DataExtractor.COMMON);	// fc - 13.9.2004
		//addGroupProperty (Group.FISH, DataExtractor.INDIVIDUAL);	// fc - 13.9.2004
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
			FishStand stand =  (FishStand)step.getScene ();
			Map records = stand.getRecords();
	System.out.println (" records size is "+ records.size());
			// return the x values vector, getstepfromroots
			Vector vgdate 	= new Vector ();	// x coordinates
			Vector vg6      = new Vector ();		// y locus  6  FST


			// Data extraction : points with (Integer, Double) coordinates

			for (int i = 1; i< records.size(); i++ ) {
				FishRecorder record = (FishRecorder) records.get((Integer)(i)) ;;
				int date = record.getCreationDate();
System.out.println (" date in DE is "+ date);
				double a = (double)record.getMeanFISRecord ();



				vgdate.add( date);
				vg6.add(a );

			}
System.out.println (" tout est loopé ");
			curves.clear ();
			curves.add (vgdate);
			curves.add (vg6);

System.out.println (" curves ajoutées ");

			labels.clear ();
			// fc - 16.10.2001			labels.add (l1);	// if you tell nothing, labels are calculated from x series

			// fc - 18.10.2001 -2  labels to tag confidence interval curves
			labels.add (new Vector ());		// no x labels

			Vector lg6 = new Vector ();		// no y labels for c1 (no vector or empty vector -> no detection)
			lg6.add ("mean value");
			labels.add (lg6);



System.out.println (" labels ajoutées ");

		} catch (Exception exc) {
			Log.println (Log.ERROR, "FishTimeMeanFIS.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**	From DataFormat interface.
	*/
	public String getName() {

		// fc - 13.9.2004
		return getNamePrefix ()+Translator.swap ("FishTimeMeanFIS");
	}

	/**	From DFCurves interface.
	*/
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("FishTimeMeanFIS.xLabel"));
		v.add (Translator.swap ("FishTimeMeanFIS.yLabel"));
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
		return labels;	// optional : unused
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
	public String getDescription () {return Translator.swap ("FishTimeMeanFIS.description");}




}
