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

import jeeb.lib.util.Log;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import capsis.commongui.util.Tools;

/**	TreeDistribution - creation of the distribution of diameters.
*
*	@author Teresa Fonseca - june 2009
*/
public class TreeDistribution {

	// Admitted error between stand level N and the sum of the N in the distribution 
	public static final double MAX_ADMITTED_ERROR = 5;
	
	
	/**	Distribution of trees. 
	*	Returns two arrays: 
	*	result[0] is an array of class diameters, 
	*	result[1] is an array of number of trees for each class.
	*	if createAClassBelowDminIfNecessary is true and minBound != 0, create a class0
	*	and add its number of trees in first class. 
	*/
	public static double[][] calculate_distribution (double G, int N, double dmedian, double davg, 
			double csi, double initialDelta, double initialLambda, 
			double minBound, double maxBound, double classWidth, boolean createAClassBelowDminIfNecessary) throws Exception {
		try {		
			double[][] result = new double[2][];
			
			// 1. Calculate Lambda, Delta and Gamma
			// Temporary values
			//~ double delta = 0.81879;
			//~ double lambda = 13.790582;
			//~ double gamma = -0.038421;
			
			double[] r = SBRecoverySystem3params.getDeltaLambdaAndGamma (dmedian, csi, davg, G, N, 
					initialDelta, initialLambda);
			double delta = r[0];
			double lambda = r[1];
			double gamma = r[2];
			
			Log.println ("ModisPinaster", "TreeDistribution.calculate_distribution ()...");
			Log.println ("ModisPinaster", "csi  = "+csi);
			Log.println ("ModisPinaster", "delta estimate  = "+delta);
			Log.println ("ModisPinaster", "lambda estimate = "+lambda);
			Log.println ("ModisPinaster", "gamma estimate  = "+gamma);
			
			
			// 2. Obtain the normal distribution
			int classNumber = (int) Math.ceil ((maxBound - minBound) / classWidth);
			double halfClass = classWidth / 2;
			
			double[] bounds = new double[classNumber + 1];
			double[] z = new double[classNumber + 1];
			double[] Fz = new double[classNumber + 1];
			
			double[] classCenter = new double[classNumber];
			double[] Nj = new double[classNumber];
			
			NormalDistribution nd = new NormalDistributionImpl ();	// mean = 0, sd = 1
			
			for (int k = 0; k < classNumber + 1; k++) {
				double i = minBound + k * classWidth;

				bounds[k] = i;
				
				if (i < csi) {
					z[k] = 0;
					Fz[k] = 0;
				} else if (i > csi + lambda) {
					z[k] = 0;
					Fz[k] = 1;
				} else {				
					z[k] = zFunction (gamma, delta, csi, lambda, i);
					Fz[k] = nd.cumulativeProbability (z[k]);
				}
			}
			
			Log.println ("ModisPinaster", "bounds ="+Tools.toString (bounds));
			Log.println ("ModisPinaster", "z=      "+Tools.toString (z));
			Log.println ("ModisPinaster", "Fz=     "+Tools.toString (Fz));
			
			for (int i = 0; i < classNumber; i++) {
				classCenter[i] = minBound + classWidth * i + halfClass;
				Nj[i] = (Fz[i+1] - Fz[i]) * N;
			}
			
			// If csi is lower than the minBound, check if we must add some trees in Nj[0]
			if (createAClassBelowDminIfNecessary && csi < minBound) {
				double z_csi = zFunction (gamma, delta, csi, lambda, csi);
				double Fz_csi = nd.cumulativeProbability (z_csi);
				double n_csi = (Fz[0] - Fz_csi) * N;
				Nj[0] += n_csi;
			}
			
			Log.println ("ModisPinaster", "cCenter="+Tools.toString (classCenter));
			Log.println ("ModisPinaster", "Nj     ="+Tools.toString (Nj));
			
			double Nsum = 0;
			for (int i = 0; i < Nj.length; i++) {
				Nsum += Nj[i];
			}
			if (Math.abs (N - Nsum) > MAX_ADMITTED_ERROR) {
				throw new Exception ("TreeDistribution.calculate_distribution () failed, N="+N
						+", Nsum="+Nsum+", difference > MAX_ADMITTED_ERROR ("+MAX_ADMITTED_ERROR+")");
			}
			
			result[0] = classCenter;
			result[1] = Nj;
			return result;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "TreeDistribution.calculate_distribution ()", 
					"Could not calculate the distribution", e);
			throw e;
		}
	}
	
	/**	Returns the value of z using the parameters of the Johnson's SB distribution.
	*/
	private static double zFunction (double gamma, double delta, double csi, double lambda, double diameterBound) {
		return gamma + delta * Math.log ((diameterBound - csi) / (csi + lambda - diameterBound));
	}
	
	
}



