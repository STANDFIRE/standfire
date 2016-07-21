/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.model;

import capsis.kernel.EvolutionParameters;
import capsis.kernel.automation.Automatable;


/**	SFEvolutionParameters is a description of the parameters needed to run  
 * 	an evolution stage in the Standfire. 
 * 	It can be at for example a target date, or a target age for a plantation, or 
 * 	a number of time steps.
 * 
 * 	@author F. Pimont - September 2013
 */
public class SFEvolutionParameters implements EvolutionParameters, Automatable {
		
	private int numberOfYears;  // number of years for an evolution process
	
	
	/**	Constructor
	 */
	public SFEvolutionParameters (int numberOfYears) {
		this.numberOfYears = numberOfYears;
		
	}
	
	public int getNumberOfYears () {return numberOfYears;}

	
}


