package capsis.lib.phenofit.function;

import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * A superclass for functions working on locations in Phenofit.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public abstract class FitlibStandardFunction extends FitlibFunction {

	/**
	 * The general form of a function: y = f(x), must be provided by all
	 * subclasses.
	 * @param pheno
	 *            TODO FP
	 * @param fitness TODO FP
	 */
	abstract public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno,
			FitlibFitness fitness, int year) throws Exception;

}
