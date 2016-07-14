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

import gymnos.model.GymnoModel;
import gymnos.model.GymnoStand;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.VProvider;

/**
 * Volume (living, fresh standing deadwood, decayed deadwood) versus Date.
 * @author G. Ligot - October 2011
 */
public class DETimeVDeadwood extends PaleoDataExtractor implements DFCurves {

	protected List<List<? extends Number>> curves;
	protected Vector labels;
	protected MethodProvider methodProvider;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeVDeadwood");
	}

	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public DETimeVDeadwood () {}

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public DETimeVDeadwood (GenericExtensionStarter s) {
		super (s);
		try {
            curves = new Vector ();
			labels = new Vector<String> ();

		} catch (Exception e) {
            Log.println (Log.ERROR, "DETimeVDeadwood.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GymnoModel)) {return false;}
			// No more tests needed: GymnoModel is enough
//			GModel m = (GModel) referent;
//			MethodProvider mp = m.getMethodProvider ();
//			if (!(mp instanceof VProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeVDeadwood.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**	This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);
		//addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		//addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration

		addBooleanProperty ("livingVolume", true); 		
		addBooleanProperty ("totalVolume"); 		
		addBooleanProperty ("freshDeadwoodVolume"); 		
		addBooleanProperty ("decayedDeadwoodVolume"); 	
		addBooleanProperty ("totalDeadwoodVolume"); 	
	}

	/**
	 * From DataExtractor SuperClass.
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
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

//			System.out.println ("DETimeVDeadwood doExtraction () ...");
//			
//			System.out.println ("livingVolume:          "+isSet ("livingVolume"));
//			System.out.println ("totalVolume:           "+isSet ("totalVolume"));
//			System.out.println ("freshDeadwoodVolume:   "+isSet ("freshDeadwoodVolume"));
//			System.out.println ("decayedDeadwoodVolume: "+isSet ("decayedDeadwoodVolume"));
//			System.out.println ("totalDeadwoodVolume:   "+isSet ("totalDeadwoodVolume"));
//			System.out.println ();
			
			
			
			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);

			Vector c1 = new Vector ();		// x coordinates (time)
			Vector c2 = new Vector ();		// y coordinates (living volume)
			Vector c3 = new Vector ();		// y coordinates (total volume - alive and dead trees)
			Vector c4 = new Vector ();		// y coordinates (fresh deadwood volume)
			Vector c5 = new Vector ();		// y coordinates (decayed deadwood volume)
			Vector c6 = new Vector ();		// y coordinates (total deadwood volume)

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				// Consider restrictions to particular groups
				GymnoStand ds = (GymnoStand) s.getScene ();
				Collection livingTrees = ds.getTrees();
				Collection freshDeadTrees = ds.getFreshDeadTrees();
				Collection decayedDeadTrees = ds.getDecayedDeadTrees();	
				
				int date = ds.getDate ();
				double v2 = ((VProvider) methodProvider).getV (ds, livingTrees);
				double v4;
				double v5;
				
				if ( freshDeadTrees == null ){
					v4 = 0;
				} else {
					v4 = ((VProvider) methodProvider).getV (ds, freshDeadTrees);
				}
				
				if ( decayedDeadTrees == null ){
					v5 = 0;
				} else {
					v5 = ((VProvider) methodProvider).getV (ds, decayedDeadTrees);
				}
				
				double v3 = v2 + v4 + v5;
				double v6 = v4 + v5;
				
				c1.add (new Integer (date));
				//System.out.println(isSet ("showLivingVolume"));
				if (isSet ("livingVolume")){c2.add (new Double (v2*coefHa));}
				if (isSet ("totalVolume")) {c3.add (new Double (v3*coefHa));}
				if (isSet ("freshDeadwoodVolume")) {c4.add (new Double (v4*coefHa));}
				if (isSet ("decayedDeadwoodVolume")) {c5.add (new Double (v5*coefHa));}
				if (isSet ("totalDeadwoodVolume")) {c6.add (new Double (v6*coefHa));}

			}

			// Data series
			curves.clear ();
			curves.add (c1);	
			
			if (isSet ("livingVolume")) curves.add (c2);
			if (isSet ("totalVolume")) curves.add (c3);
			if (isSet ("freshDeadwoodVolume")) curves.add (c4);
			if (isSet ("decayedDeadwoodVolume")) curves.add (c5);
			if (isSet ("totalDeadwoodVolume")) curves.add (c6);
			
			
			// Labels at the end of the lines
			labels.clear ();
			
			labels.add (new Vector ());		// no x labels
			
			if (isSet ("livingVolume")) {
				Vector l1 = new Vector ();
				l1.add (Translator.swap ("livingVolume"));
				labels.add (l1);
			}
			
			if (isSet ("totalVolume")) {
				Vector  l2 = new Vector ();
				l2.add (Translator.swap ("totalVolume"));
				labels.add (l2);
			}
			
			if (isSet ("freshDeadwoodVolume")) {
				Vector l3 = new Vector ();
				l3.add (Translator.swap ("freshDeadwoodVolume"));
				labels.add (l3);
			}
			
			if (isSet ("decayedDeadwoodVolume")) {
				Vector l4 = new Vector ();
				l4.add (Translator.swap ("decayedDeadwoodVolume"));
				labels.add (l4);
			}
			
			if (isSet ("totalDeadwoodVolume")) {
				Vector l5 = new Vector ();
				l5.add (Translator.swap ("totalDeadwoodVolume"));
				labels.add (l5);
			}
			
					
		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeVDeadwood.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DETimeVDeadwood");
	}

	/**
	 * From DFCurves interface.
	 */
	@Override
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeVDeadwood.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DETimeVDeadwood.yLabel")+" (perHa)");
		} else {
			v.add (Translator.swap ("DETimeVDeadwood.yLabel"));
		}
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	@Override
	public int getNY () {
		return curves.size () - 1;
	}

	@Override
	public List<List<? extends Number>> getCurves () {
		return curves;
	}
	
	/**
	 * From DFCurves interface.
	 */
	@Override
	public List<List<String>> getLabels () {
		return labels;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "G. Ligot";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeVDeadwood.description");}

}
