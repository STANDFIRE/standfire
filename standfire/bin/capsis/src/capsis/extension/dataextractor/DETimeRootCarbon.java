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
import capsis.util.methodprovider.RootCarbonProvider;

/**
 * Total volume versus Date.
 *
 * @author P. Vallet - winter 2003
 */
public class DETimeRootCarbon extends DETimeG {
        protected Vector labels;
        private MethodProvider mp;
	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeRootCarbon");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DETimeRootCarbon () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DETimeRootCarbon (GenericExtensionStarter s) {

		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DETimeRootCarbon (Step stp) {
		//~ super (stp);
		// super (s.getStep ());
                super(s);
		labels = new Vector ();

	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This static matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
                        mp = m.getMethodProvider ();
			if (!(mp instanceof RootCarbonProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeRootCarbon.matchWith ()", "Error in matchWith () (returned false)", e);
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
                
                //Retreive MethodProvider
                MethodProvider mp = step.getProject().getModel().getMethodProvider();

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
                                
                                GScene stand = s.getScene ();
				Collection trees = doFilter (stand);

				double rootCarbon = ((RootCarbonProvider) mp).getRootCarbonContent (stand, trees);

				int date = stand.getDate ();

				c1.add (new Integer (date));
				c2.add (new Double (rootCarbon*coefHa));

			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

			labels.clear ();
			labels.add (new Vector ());		// no x labels
			Vector y1Labels = new Vector ();
			y1Labels.add (Translator.swap ("DETimeRootCarbon.rootCarbon"));
			labels.add (y1Labels);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeRootCarbon.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DETimeRootCarbon");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeRootCarbon.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DETimeRootCarbon.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DETimeRootCarbon.yLabel"));
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
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "P. Vallet";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeRootCarbon.description");}

}
