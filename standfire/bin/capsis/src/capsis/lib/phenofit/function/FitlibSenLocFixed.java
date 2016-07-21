package capsis.lib.phenofit.function;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.FixedDateProcess;

/**
 * A function reading a date in a date file for each location.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibSenLocFixed extends FitlibFunction1Phase {

       
	/**
	 * Constructor
	 */
	public FitlibSenLocFixed() {
	
	}

	/**
	 * Constructor 2, parses a String, e.g. SenLocFixed()
	 */
	public FitlibSenLocFixed(String str) throws Exception {
		try {
			if (!str.startsWith("SenLocFixed("))
				throw new Exception();

		} catch (Exception e) {
			throw new Exception("FitlibSenLocFixed: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "SenLocFixed";
	}

	/**
	 * Computes 1 phenology date and updates the given phases and states for
	 * all processed days. Returns 0 if ok.
	 */
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			
			Fit4Phenology pf = pheno;

			FixedDateProcess f = new FixedDateProcess("LeafSenescenceDateMean");
			double day = f.methodToCompare(loc.getLatitude(),loc.getLongitude()); // date which match with the correspandante location

			pheno.setDate1(year, day);

			for (int d = date0; d <= pheno.getDate1(); d++) {
				phases.setValue(d, 3);
				states.setValue(d, (d-date0)/(pheno.getDate1()-date0));
		      }
			
			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenLocFixed.execute ()", "Error in FitlibSenLocFixed", e);
			throw new Exception("Error in FitlibSenLocFixed", e);
		}
	}

	public String toString() {
		return "SenLocFixed()";
	}

}
