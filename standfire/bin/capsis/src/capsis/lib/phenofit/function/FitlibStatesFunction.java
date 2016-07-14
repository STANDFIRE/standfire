package capsis.lib.phenofit.function;

import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;

/**
 * A superclass for phenology functions in Phenofit.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public abstract class FitlibStatesFunction extends FitlibFunction {

	/**
	 * The general form of a function: y = f(x), must be provided by all
	 * subclasses.
	 * @param pheno TODO FP
	 */
	abstract public double execute(FitlibLocation loc, FitlibLocationClimate locClim,
			Fit4Phenology pheno, int year, int date0, FitlibPhases phases, FitlibStates states) throws Exception;

}
