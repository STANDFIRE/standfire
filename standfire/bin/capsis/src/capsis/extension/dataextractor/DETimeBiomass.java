package capsis.extension.dataextractor;

import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.AbstractDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.util.methodprovider.BiomassProvider;

/**
 * Biomass versus Time
 * Calls BiomassProvider
 *
 * 
 */
public class DETimeBiomass extends DETimeY {

	public static final String AUTHOR = "p. Vallet";
	public static final String VERSION = "3.0";

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeBiomass");
	}


	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			MethodProvider mp = m.getMethodProvider ();
			if (!(mp instanceof BiomassProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeBiomass.matchWith ()", "Error in matchWith () (returned false)", e);
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
		addConfigProperty (AbstractDataExtractor.HECTARE);
		addConfigProperty (AbstractDataExtractor.TREE_GROUP);
	}


	@Override
	protected Number getValue(GModel m, GScene stand, int date) {
		MethodProvider methodProvider = m.getMethodProvider ();
		
		Collection trees = doFilter (stand);  
		double biomass = ((BiomassProvider) methodProvider).getBiomass (stand, trees);	
		return biomass;
	}

}
