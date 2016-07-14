package capsis.lib.phenofit.function;

import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibMemory;

/**
 * A superclass for phenology functions in Phenofit.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public interface FitlibPhelibFunctions {

	/**
	 * The general form of a function: y = f(x), must be provided by all
	 * subclasses. The Phelib function is called for one specific day.
	 * @param memory TODO FP
	 */
	abstract public double executeDaily(FitlibLocation loc, FitlibLocationClimate locClim, int year, int day,
			Fit5Phenology pheno, FitlibMemory memory) throws Exception;
	
	// REPLACED the following line, see upper fc-31.8.2015
//	abstract public double executeDaily(FitlibLocation loc, FitlibLocationClimate locClim, int year, int day,
//			Fit4Phenology pheno, FitlibPhases phases, FitlibStates states) throws Exception;

}