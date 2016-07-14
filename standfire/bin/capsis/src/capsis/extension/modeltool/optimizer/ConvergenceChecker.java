/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package capsis.extension.modeltool.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.util.MathUtils;


public class ConvergenceChecker implements RealConvergenceChecker {
	
	/** Default relative threshold. */
	private static final double DEFAULT_RELATIVE_THRESHOLD = 100 * MathUtils.EPSILON;

	/** Default absolute threshold. */
	private static final double DEFAULT_ABSOLUTE_THRESHOLD = 100 * MathUtils.SAFE_MIN;
   
	/** Relative tolerance threshold. */
	private final double relativeThreshold;
    
	/** Absolute tolerance threshold. */
	private final double absoluteThreshold;
	
	/** Absolute convergence criterion	*/
	private double absoluteConvergence ;
	
	/** Relative convergence criterion	*/
	private double relativeConvergence ;
	
	/** Has the convergence occured ? */
	private boolean convergence ;
    
	/** Historic of the convergence evaluation calls */
	private List<double[]> historic ;
	
	/** 
	 * Build an instance with default threshold.
	 */
	public ConvergenceChecker() {
		this.relativeThreshold = DEFAULT_RELATIVE_THRESHOLD;
		this.absoluteThreshold = DEFAULT_ABSOLUTE_THRESHOLD;
		this.absoluteConvergence = Double.NaN ;
		this.relativeConvergence = Double.NaN  ;
		this.convergence = false ;
		this.historic = new ArrayList<double[]>() ;
	}

	/** 
	 * Build an instance with a specified threshold.
	* <p>
	* In order to perform only relative checks, the absolute tolerance
	* must be set to a negative value. In order to perform only absolute
	* checks, the relative tolerance must be set to a negative value.
	* </p>
	* @param relativeThreshold relative tolerance threshold
	* @param absoluteThreshold absolute tolerance threshold
	*/
	public ConvergenceChecker(final double absoluteThreshold, final double relativeThreshold) {
		this.relativeThreshold = relativeThreshold;
		this.absoluteThreshold = absoluteThreshold;
		this.absoluteConvergence = Double.NaN  ;
		this.relativeConvergence = Double.NaN  ;
		this.convergence = false ;
		this.historic = new ArrayList<double[]>() ;
	}
 
	/** {@inheritDoc}
	*   Test the convergence criteria by comparing the current iteration to the previous one.
	*   The convergence is obtained as soon as one of the elementary convergence criteria 
	*   (absolute and relative convergence criterion) is verified.
	*/
	public boolean converged(final int iteration, final RealPointValuePair previous, final RealPointValuePair current) {
		final double p = previous.getValue();
		final double c = current.getValue();
		final double difference = Math.abs(p - c);
		final double size = Math.max(Math.abs(p), Math.abs(c));
		this.absoluteConvergence = difference;
		if (size > MathUtils.SAFE_MIN) {
			relativeConvergence = difference / size;
		} else {
			relativeConvergence = Double.POSITIVE_INFINITY;
		}
		this.convergence = difference <= (size * relativeThreshold) || (difference <= absoluteThreshold); 
		double[] resume = {iteration, this.absoluteConvergence, this.relativeConvergence } ;
		historic.add(resume) ;
		return convergence;
	}
  
	/** 
	 * Return the threshold for the absolute convergence criterion 
	 */
	public double getAbsoluteThreshold() {return absoluteThreshold;}

	/** 
	 * Return the threshold for the relative convergence criterion 
	 */
	public double getRelativeThreshold() {return relativeThreshold;}

	/** 
	 * Return the current value of the absolute convergence criterion 
	 */
	public double getAbsoluteConvergence() {return absoluteConvergence;}

	/** 
	 * Return the current value of the relative convergence criterion 
	 */
	public double getRelativeConvergence() {return relativeConvergence;}

	/** 
	 * Return the current convergence status 
	 */
	public boolean getConvergence() {return convergence;}

	/** 
	 * Get the historic of convergence criteria as a list 
	 */
	public List<double[]> getHistoric() {return historic;}
	
	/** Return the historic of convergence criteria as a String */
	public String historicToString() {
		String s = "Iteration\tAbsoluteConvergence\tRelativeConvergence\n" ;
		for (double[] resume : historic) {
			s+=(resume[0]+"\t"+resume[1]+"\t"+resume[2]+"\n") ;
		}
		return s;
	}

}
