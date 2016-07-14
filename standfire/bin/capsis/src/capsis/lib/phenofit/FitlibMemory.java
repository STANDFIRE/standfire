package capsis.lib.phenofit;

/**
 * A one day memory for the phenology object for Phenofit5.
 * 
 * @author F. de Coligny - August 2015
 */
public class FitlibMemory {

	public int phase;
	public double intermediateState;
	public double state;
	// frostHardiness: celsius, 100: special value to detect first time this
	// variable is used, to manage initialisation in frost functions
	public double frostHardiness = 100;
	public double frostDamage;

	/**
	 * Constructor.
	 */
	public FitlibMemory() {
	}

	/**
	 * Constructor 2.
	 */
	public FitlibMemory(FitlibMemory memory0) {
		this.phase = memory0.phase;
		this.intermediateState = memory0.intermediateState;
		this.state = memory0.state;
		this.frostDamage = memory0.frostDamage;
		this.frostHardiness = memory0.frostHardiness;
	}

	public void resetDevelopmentStates() {
		intermediateState = 0;
		state = 0;

	}

	@Override
	public String toString() {
		return "FitlibMemory"
				+" phase: "+phase
				+" intermediateState: "+intermediateState
				+" state: "+state
				+" frostHardiness: "+frostHardiness
				+" frostDamage: "+frostDamage;
	}
	
}
