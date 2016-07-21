package fireparadox.model.layerSet;
/**
 * This class contain the field properties required for evolution and treatment of layerSets
 * @author pimont
 *
 */

public class FmLayerSetFieldProperties {
	
	private int age; // age in year after clearing
	private double fertility; // from 0 to 3
	private int lastClearingType ; //fire, prescribe burning, mechanical clearing
	private double treatmentEffect; // between 0 and 1 (0 is no treatment)
	
	public FmLayerSetFieldProperties(int age, double fertility, int lastClearingType, double treatmentEffect) {
		this.age = age;
		this.fertility = fertility;
		this.lastClearingType = lastClearingType;
		this.treatmentEffect = treatmentEffect;
	}
	
	
	
}
