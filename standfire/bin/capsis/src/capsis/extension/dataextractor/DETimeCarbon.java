package capsis.extension.dataextractor;

import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.util.methodprovider.CarbonProvider;

/**
 * This data extractor computes the aboveground carbon (branches + boles) in time.
 * Requires the CarbonProvider interface.
 * @author P. Vallet - winter 2003
 * 	modified M. Fortin - August 2010 (extended from DETimeY)
 */
public class DETimeCarbon extends DETimeY {
	
	public static final String AUTHOR = "P. Vallet";
	public static final String VERSION = "2.0";

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeCarbon");
	}

	/**
	 * Extension dynamic compatibility mechanism. 
	 * This static matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			MethodProvider mp = m.getMethodProvider ();
			if (!(mp instanceof CarbonProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeCarbon.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	@Override
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected Number getValue(GModel m, GScene stand, int date) {
		double areaFactor = 1d;
		if (settings.perHa) {
			areaFactor = 10000d / stand.getArea();
		}
		Collection trees = doFilter(stand);
		CarbonProvider carbonProvider = (CarbonProvider) m.getMethodProvider();
		return carbonProvider.getCarbonContent(stand, trees) * areaFactor;
	}

}
