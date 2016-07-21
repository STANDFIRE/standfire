/* 
* The Johnson distribution library for Capsis4
* 
* Copyright (C) 2009 Bernard Parresol, Teresa Fonseca
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public
* License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package capsis.lib.johnsondistribution;


/**	SBRecoverySystem3params - This procedure recovers the 3 parameters 
*	lambda delta and gamma of the Johnson's SB distribution.
*	It was initially programmed by Bernard Parresol, USDA Forest service, 
*	Southern Research Station.
*	Parresol, B.R. 2003. Recovering parameters of Johnson's SB distribution.
*	USDA Forest Service Research Paper SRS-31. 9 p.
*	Use in sequence getDeltaAndLambda () and getGamma ().

*	@author Teresa Fonseca - june 2009
*/
public class SBRecoverySystem3params {

	/**	Calculates Delta, Lambda and then Gamma
	*	Returns a double array containing delta (at position 0), lambda (at position 1) 
	*	and gamma (at position 2).
	*/
	public static double[] getDeltaLambdaAndGamma (double dmed, double csi, 
			double davg, double G, int numberOfTrees, 
			double initialDelta, double initialLambda) throws Exception {
		
		LMEstimator estimator = new LMEstimator (dmed, csi, davg, G, numberOfTrees, 
				initialDelta, initialLambda); 
		double[] r = estimator.estimate ();
		
		double delta = r[0];
		double lambda = r[1];
		
		double gamma = delta * Math.log (lambda / (dmed - csi) - 1);
		
		double[] result = new double[3];
		result[0] = delta;
		result[1] = lambda;
		result[2] = gamma;
		return result;
	}
	
	/**	Calculates Gamma
	*/
	//~ public static double getGamma (delta, lambda) {
		
	//~ }
	
	
	
}



