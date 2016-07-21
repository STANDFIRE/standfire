package capsis.lib.economics;

import java.io.Serializable;

/**	BillBookSpecies : an interface for species in the BillBook framework
*
*	@author O. Pain - november 2007
*/
public interface BillBookSpecies extends Serializable {

	public String getLatinName ();

	public double getTED7volume (BillBookCompatible stand);
	public double getTED7biomass (BillBookCompatible stand);
	public double getTED0biomass (BillBookCompatible stand);
	public BillBookCompatible processEvolution (BillBookCompatible stand,
			int targetType, int targetValue);	// ex: type = diameter, value = 12cm
	public double getDryWoodContent ();	// (dry wood weight / gross mass) x 100)
	public double getFreshWoodDensity ();	// kg/m3 (gross mass / volume over bark)
	public double getM3ToMapCoefficient ();	 // volume m3 -> Map (apparent m3)
	public double getDryTonToMWHCoefficient ();	// dry biomass (ton) -> MWH

	public String getName ();

	public double convert (double value, String sourceUnit, String targetUnit);

}
