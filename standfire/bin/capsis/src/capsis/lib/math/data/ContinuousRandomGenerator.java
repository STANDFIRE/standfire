/**
 * 
 */
package capsis.lib.math.data;

import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math3.random.AbstractRandomGenerator;

/**
 * Random generator build on an AbstractContinuousDistribution (see
 * org.apache.commons.maths.distribution) example : x = new RandomGenerator(new
 * ChiSquaredDistributionImpl(...)); x.nextDouble()
 * 
 * @author S. Dufour Kowalski - 2009-2010
 */
public class ContinuousRandomGenerator extends AbstractRandomGenerator {

	private ContinuousDistribution distribution;

	// fc+ed-23.3.2015 removed final, see setSeed ()
	private Random randomSource;

	// final Random randomSource;

	/**
	 * Constructor.
	 * 
	 * @param distribution
	 *            : the underlying distribution.
	 */
	public ContinuousRandomGenerator(ContinuousDistribution distribution) {
		super();
		this.distribution = distribution;
		this.randomSource = new Random();
	}

	/**
	 * @return a double taken from the distribution
	 */
	public double nextDouble() {

		try {
			return distribution.inverseCumulativeProbability(randomSource.nextDouble());
		} catch (MathException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param n
	 *            : number of values to generate
	 * @return a array of n double
	 */
	public double[] getValues(int n) {

		double[] values = new double[n];

		for (int i = 0; i < n; i++) {
			double v = this.nextDouble();
			values[i] = v;
		}
		return values;
	}

	/**
	 * Added this method to comply with the AbstractRandomGenerator superclass
	 * in apache commons math 3, for use with StableRandomGenerator / skewness.
	 * fc+ed-23.3.2015
	 */
	@Override
	public void setSeed(long seed) {
		clear();
		this.randomSource = new Random(seed);

	}

}