package capsis.lib.optimisation ;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.util.MathUtils;

/** 
* Copy of the original class SimpleScalarValueChecker from Apache 2.0
* org.apache.commons.math.optimization.SimpleScalarValueChecker 
* modified in order to allow the user to get the threshold values
* and to get the actual numeric values of convergence criteria. 
* @author G. Le Moguedec - jan 2010
*/
public class SimpleScalarValueChecker implements RealConvergenceChecker {

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
	
	/** Build an instance with default threshold.
	*/
	public SimpleScalarValueChecker() {
		this.relativeThreshold = DEFAULT_RELATIVE_THRESHOLD;
		this.absoluteThreshold = DEFAULT_ABSOLUTE_THRESHOLD;
		this.absoluteConvergence = Double.NaN ;
		this.relativeConvergence = Double.NaN  ;
		this.convergence = false ;
		this.historic = new ArrayList<double[]>() ;
	}

	/** Build an instance with a specified threshold.
	* <p>
	* In order to perform only relative checks, the absolute tolerance
	* must be set to a negative value. In order to perform only absolute
	* checks, the relative tolerance must be set to a negative value.
	* </p>
	* @param relativeThreshold relative tolerance threshold
	* @param absoluteThreshold absolute tolerance threshold
	*/
	public SimpleScalarValueChecker(final double absoluteThreshold,
										final double relativeThreshold) {
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
	public boolean converged(final int iteration,
							final RealPointValuePair previous,
							final RealPointValuePair current) {
		final double p          = previous.getValue();
		final double c          = current.getValue();
		final double difference = Math.abs(p - c);
		final double size       = Math.max(Math.abs(p), Math.abs(c));
		this.absoluteConvergence = difference ;
		this.relativeConvergence = (size>MathUtils.SAFE_MIN) ? difference/size : Double.POSITIVE_INFINITY;
		this.convergence = (difference <= (size * relativeThreshold)) || (difference <= absoluteThreshold) ; 
		double[] resume = {iteration, this.absoluteConvergence, this.relativeConvergence } ;
		historic.add(resume) ;
		return (this.convergence);
	}
  
	/** Return the threshold for the absolute convergence criterion */
	public double getAbsoluteThreshold(){
		return this.absoluteThreshold ;
	}

	/** Return the threshold for the relative convergence criterion */
	public double getRelativeThreshold(){
		return this.relativeThreshold ;
	}

	/** Return the current value of the absolute convergence criterion */
	public double getAbsoluteConvergence(){
		return this.absoluteConvergence ;
	}

	/** Return the current value of the relative convergence criterion */
	public double getRelativeConvergence(){
		return this.relativeConvergence ;
	}

	/** Return the current convergence status */
	public boolean getConvergence(){
		return this.convergence ;
	}

	/** Get the historic of convergence criteria as a list */
	public List<double[]> getHistoric(){
		return(historic) ;
	}
	
	/** Return the historic of convergence criteria as a String */
	public String historicToString() {
		String s = "Iteration\tAbsoluteConvergence\tRelativeConvergence\n" ;
		for (double[] resume : historic){
			s+=(resume[0]+"\t"+resume[1]+"\t"+resume[2]+"\n") ;
		}
		return(s) ;
	}

}


