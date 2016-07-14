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

import org.apache.commons.math.estimation.EstimatedParameter;
import org.apache.commons.math.estimation.EstimationException;
import org.apache.commons.math.estimation.LevenbergMarquardtEstimator;
import org.apache.commons.math.estimation.SimpleEstimationProblem;
import org.apache.commons.math.estimation.WeightedMeasurement;

import flanagan.integration.Integration;

/**	Use the Levenberg-Marquardt implementation in Apache common-math
*	to find delta and lambda parameters in the SB distribution recovery method.
*	In this implementation, we consider constraints on delta and lambda : 
*	delta > 0 and lambda_low < lambda < lambda_up. For this, we introduce
*	p_delta and p_lambda and try to estimate them instead of directly 
*	delta and lambda.
*	
*	@author F. de Coligny, Teresa Fonseca - june 2009
*/
public class LMEstimator {
	
		/** Fcn1
		*/
		private class Fcn1 extends WeightedMeasurement {
			private double dmed;
			private double csi;
			private double davg;
			
			public Fcn1 (double dmed, double csi, double davg) {
				super (1.0, 0);		// rhs (right hand side of the equation) = 0
				this.dmed = dmed;
				this.csi = csi;
				this.davg = davg;
			}
			
			public double getTheoreticalValue () {
				double d = getDelta (p_delta.getEstimate ());
				double l = getLambda (p_lambda.getEstimate ());

				// Moments of Ipsilon
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);

				return l * mom1 + csi - davg;
			} 
			
			public double getPartial (EstimatedParameter p) {
				double d = getDelta (p_delta.getEstimate ());
				double l = getLambda (p_lambda.getEstimate ());
		
				// Moments of Ipsilon
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);
				double mom2 = getMoment (dmed, csi, l, d, 2, -5, 5, 15);
				
				double pse1 = getPseudoMoment (dmed, csi, l, d, 1, -6, 6, 15);
				double pse2 = getPseudoMoment (dmed, csi, l, d, 2, -6, 6, 15);
				
				if (p == p_delta) {	// derivative for delta
					return -l * (pse1 - pse2) / (d*d);
				} else {			// derivative for lambda
					return mom1 - l * (mom1 - mom2) / (l - dmed + csi);
				}
			}
			
		}
	
		/** Fcn2
		*/
		private class Fcn2 extends WeightedMeasurement {
			private double dmed;
			private double csi;
			private double davg;
			private double G;
			private double numberOfTrees;
			
			public Fcn2 (double dmed, double csi, double davg, double G, int numberOfTrees) {
				super (1.0, 0);		// rhs = 0
				this.dmed = dmed;
				this.csi = csi;
				this.davg = davg;
				this.G = G;
				this.numberOfTrees = numberOfTrees;
			}
			
			public double getTheoreticalValue () {
				double d = getDelta (p_delta.getEstimate ());
				double l = getLambda (p_lambda.getEstimate ());

				// Moments of Ipsilon
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);
				double mom2 = getMoment (dmed, csi, l, d, 2, -5, 5, 15);

				return Math.PI / 40000d * numberOfTrees * (l*l * mom2 + 2 * csi * l * mom1 + csi*csi) - G;
			} 
			
			public double getPartial (EstimatedParameter p) {
				double d = getDelta (p_delta.getEstimate ());
				double l = getLambda (p_lambda.getEstimate ());
				
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);
				double mom2 = getMoment (dmed, csi, l, d, 2, -5, 5, 15);
				double mom3 = getMoment (dmed, csi, l, d, 3, -5, 5, 15);
				double pse1 = getPseudoMoment (dmed, csi, l, d, 1, -6, 6, 15);
				double pse2 = getPseudoMoment (dmed, csi, l, d, 2, -6, 6, 15);
				double pse3 = getPseudoMoment (dmed, csi, l, d, 3, -6, 6, 15);
				
				if (p == p_delta) {	// derivative for delta
					double term1 = -2 * l * (l * (pse2 - pse3) + csi * (pse1 - pse2));
					return Math.PI / 40000d * numberOfTrees / (d*d) * term1;
				} else {			// derivative for lambda
					double term1 = -2 * l * (l * (mom2 - mom3) + csi * (mom1 - mom2));
					return Math.PI / 40000d * (term1 / (l - dmed + csi) + 2 * (l * mom2 + csi * mom1));
				}
			}
			
		}
	
		
		private class MyProblem extends SimpleEstimationProblem {
			
			public MyProblem (EstimatedParameter p_delta, EstimatedParameter p_lambda, 
					WeightedMeasurement fcn1, WeightedMeasurement fcn2) {
				addParameter (p_delta);
				addParameter (p_lambda);
				addMeasurement (fcn1);
				addMeasurement (fcn2);
			}
		}
			
	private EstimatedParameter p_delta; 
	private EstimatedParameter p_lambda; 
	private double lambda_low; 
	private double lambda_up; 
	private MyProblem pb;

	/**	
	*/
	public LMEstimator (double dmed, double csi, 
			double davg, double G, int numberOfTrees, 
			double initialDelta, double initialLambda) {
				
		//~ System.out.println ("LMEstimator...");
		
		double initial_p_Delta = Math.sqrt (initialDelta);
		double initial_p_Lambda = initialLambda;	// temporary
		
		lambda_low = dmed - csi;
		lambda_up = 2 * initialLambda;
		
		p_delta = new EstimatedParameter ("p_delta", initial_p_Delta); 
		p_lambda = new EstimatedParameter ("p_lambda", initial_p_Lambda); 

		pb = new MyProblem (p_delta, p_lambda, 
				new Fcn1 (dmed, csi, davg), 
				new Fcn2 (dmed, csi, davg, G, numberOfTrees));
			
	}
	
	/**	Returns a double[] result
	*	result[0] = delta estimation
	*	result[1] = lambda estimation
	*/
	public double[] estimate () throws Exception {	
		try { 
			LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator (); 
			estimator.estimate (pb); 
			
			//~ System.out.println ("delta estimate = "+delta.getEstimate ());
			//~ System.out.println ("lambda estimate = "+lambda.getEstimate ());
			
			double[] result = new double[2];
			result[0] = getDelta (p_delta.getEstimate ());
			result[1] = getLambda (p_lambda.getEstimate ());
			return result;
			
		} catch (EstimationException e) {
			Log.println (Log.ERROR, "LMEstimator.estimate ()", "Could not estimate delta and lambda", e);
			throw e;
		} 
	}
	
	/**	Get delta from p_delta
	*/
	private double getDelta (double p_delta) {
		return p_delta * p_delta;
	}

	/**	Get lambda from p_lambda
	*/
	private double getLambda (double p_lambda) {
		return 0.5 * (lambda_low + lambda_up) 
				+ Math.atan (p_lambda) * (lambda_up - lambda_low) / p_lambda;
	}
	
	private double getMoment (double dmed, double csi, double l, double d, int order, int low, int up, int n) {
		MomentFunctionZ f = new MomentFunctionZ (dmed, csi, l, d, order);
		Integration intg = new Integration (f);
		intg.setLimits (low, up);
		double integration = intg.gaussQuad (n);
		double mom = 1 / Math.sqrt (2 * Math.PI) * integration;
		return mom;
	}
		
	private double getPseudoMoment (double dmed, double csi, double l, double d, int order, int low, int up, int n) {
		PseudoMomentFunctionZ g = new PseudoMomentFunctionZ (dmed, csi, l, d, order);
		Integration intg = new Integration (g);
		intg.setLimits (low, up);
		double integration = intg.gaussQuad (n);
		double pseudomom = 1 / Math.sqrt (2 * Math.PI) * integration;
		return pseudomom;
	}

	
	//~ public static void main (String[] args) {
		//~ new LMEstimator ();
	//~ }
}

/* BACKUP: without constraints
package capsis.lib.johnsondistribution;

import flanagan.integration.*;
import org.apache.commons.math.estimation.*;
import jeeb.lib.util.*;
import capsis.kernel.*;
import capsis.util.*;
import org.apache.commons.math.distribution .*;

/**	Use the Levenberg-Marquardt implementation in Apache common-math
*	to find delta and lambda parameters in the SB distribution recovery method.
*	
*	@author F. de Coligny, Teresa Fonseca - june 2009
*
public class LMEstimator {
	
		/** Fcn1
		*
		private class Fcn1 extends WeightedMeasurement {
			private double dmed;
			private double csi;
			private double davg;
			
			public Fcn1 (double dmed, double csi, double davg) {
				super (1.0, 0);		// rhs = 0
				this.dmed = dmed;
				this.csi = csi;
				this.davg = davg;
			}
			
			public double getTheoreticalValue () {
				double d = delta.getEstimate ();
				double l = lambda.getEstimate ();

				// Moments of Ipsilon
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);

				return l * mom1 + csi - davg;
			} 
			
			public double getPartial (EstimatedParameter p) {
				double d = delta.getEstimate ();
				double l = lambda.getEstimate ();
		
				// Moments of Ipsilon
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);
				double mom2 = getMoment (dmed, csi, l, d, 2, -5, 5, 15);
				
				double pse1 = getPseudoMoment (dmed, csi, l, d, 1, -6, 6, 15);
				double pse2 = getPseudoMoment (dmed, csi, l, d, 2, -6, 6, 15);
				
				if (p == delta) {	// derivative for delta
					return -l * (pse1 - pse2) / (d*d);
				} else {			// derivative for lambda
					return mom1 - l * (mom1 - mom2) / (l - dmed + csi);
				}
			}
			
		}
	
		/** Fcn2
		*
		private class Fcn2 extends WeightedMeasurement {
			private double dmed;
			private double csi;
			private double davg;
			private double G;
			private double numberOfTrees;
			
			public Fcn2 (double dmed, double csi, double davg, double G, int numberOfTrees) {
				super (1.0, 0);		// rhs = 0
				this.dmed = dmed;
				this.csi = csi;
				this.davg = davg;
				this.G = G;
				this.numberOfTrees = numberOfTrees;
			}
			
			public double getTheoreticalValue () {
				double d = delta.getEstimate ();
				double l = lambda.getEstimate ();

				// Moments of Ipsilon
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);
				double mom2 = getMoment (dmed, csi, l, d, 2, -5, 5, 15);

				return Math.PI / 40000d * numberOfTrees * (l*l * mom2 + 2 * csi * l * mom1 + csi*csi) - G;
			} 
			
			public double getPartial (EstimatedParameter p) {
				double d = delta.getEstimate ();
				double l = lambda.getEstimate ();
				
				double mom1 = getMoment (dmed, csi, l, d, 1, -5, 5, 15);
				double mom2 = getMoment (dmed, csi, l, d, 2, -5, 5, 15);
				double mom3 = getMoment (dmed, csi, l, d, 3, -5, 5, 15);
				double pse1 = getPseudoMoment (dmed, csi, l, d, 1, -6, 6, 15);
				double pse2 = getPseudoMoment (dmed, csi, l, d, 2, -6, 6, 15);
				double pse3 = getPseudoMoment (dmed, csi, l, d, 3, -6, 6, 15);
				
				if (p == delta) {	// derivative for delta
					double term1 = -2 * l * (l * (pse2 - pse3) + csi * (pse1 - pse2));
					return Math.PI / 40000d * numberOfTrees / (d*d) * term1;
				} else {			// derivative for lambda
					double term1 = -2 * l * (l * (mom2 - mom3) + csi * (mom1 - mom2));
					return Math.PI / 40000d * (term1 / (l - dmed + csi) + 2 * (l * mom2 + csi * mom1));
				}
			}
			
		}
	
		
		private class MyProblem extends SimpleEstimationProblem {
			
			public MyProblem (EstimatedParameter delta, EstimatedParameter lambda, WeightedMeasurement fcn1, WeightedMeasurement fcn2) {
				addParameter (delta);
				addParameter (lambda);
				addMeasurement (fcn1);
				addMeasurement (fcn2);
			}
		}
			
	private EstimatedParameter delta; 
	private EstimatedParameter lambda; 
	private MyProblem pb;

	/**	
	*
	public LMEstimator (double dmed, double csi, 
			double davg, double G, int numberOfTrees, 
			double initialDelta, double initialLambda) {
				
		//~ System.out.println ("LMEstimator...");
				
		delta = new EstimatedParameter ("delta", initialDelta); 
		lambda = new EstimatedParameter ("lambda", initialLambda); 

		pb = new MyProblem (delta, lambda, 
				new Fcn1 (dmed, csi, davg), 
				new Fcn2 (dmed, csi, davg, G, numberOfTrees));
			
	}
	
	/**	Returns a double[] result
	*	result[0] = delta estimation
	*	result[1] = lambda estimation
	*
	public double[] estimate () throws Exception {	
		try { 
			LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator (); 
			estimator.estimate (pb); 
			
			//~ System.out.println ("delta estimate = "+delta.getEstimate ());
			//~ System.out.println ("lambda estimate = "+lambda.getEstimate ());
			
			double[] result = new double[2];
			result[0] = delta.getEstimate ();
			result[1] = lambda.getEstimate ();
			return result;
			
		} catch (EstimationException e) {
			Log.println (Log.ERROR, "LMEstimator.estimate ()", "Could not estimate delta and lambda", e);
			throw e;
		} 
	}
	
		
	private double getMoment (double dmed, double csi, double l, double d, int order, int low, int up, int n) {
		MomentFunctionZ f = new MomentFunctionZ (dmed, csi, l, d, order);
		Integration intg = new Integration (f);
		intg.setLimits (low, up);
		double integration = intg.gaussQuad (n);
		double mom = 1 / Math.sqrt (2 * Math.PI) * integration;
		return mom;
	}
		
	private double getPseudoMoment (double dmed, double csi, double l, double d, int order, int low, int up, int n) {
		PseudoMomentFunctionZ g = new PseudoMomentFunctionZ (dmed, csi, l, d, order);
		Integration intg = new Integration (g);
		intg.setLimits (low, up);
		double integration = intg.gaussQuad (n);
		double pseudomom = 1 / Math.sqrt (2 * Math.PI) * integration;
		return pseudomom;
	}
}

*/

