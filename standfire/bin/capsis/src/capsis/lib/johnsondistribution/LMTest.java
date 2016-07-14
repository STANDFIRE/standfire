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

import org.apache.commons.math.estimation.EstimatedParameter;
import org.apache.commons.math.estimation.EstimationException;
import org.apache.commons.math.estimation.LevenbergMarquardtEstimator;
import org.apache.commons.math.estimation.SimpleEstimationProblem;
import org.apache.commons.math.estimation.WeightedMeasurement;

/**	Test the Levenberg-Marquardt implementation in Apache common-math
*	
*	@author F. de Coligny, Teresa Fonseca - june 2009
*/
public class LMTest {
	
		/** Function1
		*/
		private class Function1 extends WeightedMeasurement {
			
			public Function1 () {
				super (1.0, 0);		// rhs = 0
			}
			
			public double getTheoreticalValue () {
				return x.getEstimate () * x.getEstimate () - y.getEstimate () - 1;
			} 
			
			public double getPartial (EstimatedParameter p) {
				if (p == x) {
					return 2 * x.getEstimate ();
				} else {
					return -1;
				}
			}
			
		}
	
		/** Function2
		*/
		private class Function2 extends WeightedMeasurement {
			
			public Function2 () {
				super (1.0, 0);		// rhs = 0
			}
			
			public double getTheoreticalValue () {
				double a = x.getEstimate () - 2;
				double b = y.getEstimate () - 0.5;
				return a * a + b * b - 1;
			} 
			
			public double getPartial (EstimatedParameter p) {
				if (p == x) {
					return 2 * x.getEstimate () - 4;
				} else {
					return 2 * y.getEstimate () - 1;
				}
			}
			
		}
	
		
		private class MyProblem extends SimpleEstimationProblem {
			
			//~ public EstimatedParameter x; 
			//~ public EstimatedParameter y; 
			
			public MyProblem (EstimatedParameter x, EstimatedParameter y, WeightedMeasurement f1, WeightedMeasurement f2) {
				//~ this.x = x;
				//~ this.y = y;
				
				addParameter (x);
				addParameter (y);
				addMeasurement (f1);
				addMeasurement (f2);
			}
		}
			
	private EstimatedParameter x; 
	private EstimatedParameter y; 


	/**	
	*/
	public LMTest () {
		try { 
			System.out.println ("LMTest...");
			x = new EstimatedParameter("x", -1); 
			y = new EstimatedParameter("y", -1); 

			MyProblem pb = new MyProblem (x, y, new Function1 (), new Function2 ());
			
			LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator (); 
			estimator.estimate (pb); 
			
			System.out.println ("x estimate = "+x.getEstimate ());
			System.out.println ("y estimate = "+y.getEstimate ());
			
		} catch (EstimationException e) {
			System.err.println (e.getMessage());
		} 
	}
	
	public static void main (String[] args) {
		new LMTest ();
	}
}



