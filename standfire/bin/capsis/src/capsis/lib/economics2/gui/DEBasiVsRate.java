package capsis.lib.economics2.gui;

import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Translator;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics2.EconomicScenario;
import capsis.lib.economics2.EconomicStandardizedOperation;
import capsis.lib.economics2.EconomicScenario.EconomicCase;

public class DEBasiVsRate extends DEEconomicIndicatorVsRate {

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DEBasiVsRate () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DEBasiVsRate (GenericExtensionStarter s) {
		super (s);
	}
	
	@Override
	public double getYValue(double r, EconomicScenario es) {
		return es.calcBASI(r, es.getStandardizedEconomicOperations(), es.getFirstDate(), es.getFirstDateInfiniteCycle(), es.getLastDate(), es.getEconomicCase());
	}
	
	@Override
	protected void modifyDefaultValue(){
		MINIMUM_RATE_DEFAULT = 0.01; //if r=0, BASI=INFINITY!
	}
	
	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DEBasiVsRate");
	}
	
	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("discountRate"));
		v.add (Translator.swap ("DEBasiVsRate.yLabel"));
		return v;
	}

}
