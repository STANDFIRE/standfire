package capsis.lib.crobas;

import java.util.Random;

import jeeb.lib.util.Translator;
import capsis.kernel.AbstractSettings;

/**	Settings to be used in Crobas (A. Makela 1997 For. Sci.) and PipeQual (Makela 2002 Tree Phys, Makela and Makinen 2003 For Eco and Manag)
*	simulations. 
*	@author R. Schneider - may 2008
*/
public class CSettings extends AbstractSettings {
	static {
		Translator.addBundle("capsis.lib.crobas.CModel");
	} 

	public static final String CROBAS = Translator.swap ("CSettings.CROBAS");							// Tree level
	public static final String PIPEQUAL_NODAL = Translator.swap ("CSettings.PIPEQUAL_NODAL");				// With nodal whorls only
	public static final String PIPEQUAL_INTER_NODAL = Translator.swap ("CSettings.PIPEQUAL_INTER_NODAL");	// With nodal and internodal whorls

	private Random random;		// a random number generator
	
	public CSpecies species;	// chosen species, ex: JackPineSpecies
	
	// MOVED to CTree (fc-14.1.2011, uqar.jackpine becomes multi-species)
//	public String crobasLevel;	// CROBAS, PIPEQUAL_NODAL, PIPEQUAL_INTER_NODAL
	
	public CSettings () {
		random = new Random ();
	}
	
	public Random getRandom () {return random;}
}
