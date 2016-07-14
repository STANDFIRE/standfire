package fireparadox.model;

import capsis.kernel.EvolutionParameters;

/**	FiEvolutionParameters - parameters for the evolution process
 *
 *	@author F. de Coligny - june 2010
 */
public class FmEvolutionParameters implements EvolutionParameters {

	public int numberOfSteps;

	
	/**	Default constructor
	 */
	public FmEvolutionParameters() {}
		
	/**	Constructor 2
	 */
	public FmEvolutionParameters(int i) {
		numberOfSteps = i;
	}
	
}
