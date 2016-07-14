package capsis.lib.phenofit.function;

import phenofit5.model.Fit5Plant;
import phenofit5.model.Fit5VegetationLayer;
import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.FixedDateProcess;

/**
 * The Phenofit FlowerLocFixed function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibFlowerLocFixed extends FitlibFunction2Phases {

	/**
	 * Constructor
	 */
	public FitlibFlowerLocFixed() {
	
	}

	/**
	 * Constructor 2, parses a String, e.g. FlowerLocFixed()
	 */
	public FitlibFlowerLocFixed(String str) throws Exception {
		try {
			if (!str.startsWith("FlowerLocFixed("))
				throw new Exception();

		} catch (Exception e) {
			throw new Exception("FitlibFlowerLocFixed: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "FlowerLocFixed";
	}

	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * all processed days. Returns 0 if ok.
	 */
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			
			Fit4Phenology pf = pheno;
			
			FixedDateProcess f1 = new FixedDateProcess("FlowerDormancyBreakDateMean");
			double day1 = f1.methodToCompare(loc.getLatitude(),loc.getLongitude()); // date which match with the correspandante location

			FixedDateProcess f2 = new FixedDateProcess("FloweringDateMean");
			double day2 = f2.methodToCompare(loc.getLatitude(),loc.getLongitude()); // date which match with the correspandante location
			
			pheno.setDate1(year, day1);
			pheno.setDate2(year, day2);
			
			for (int d = date0; d <= pheno.getDate1(); d++) {
				phases.setValue(d, 1);
				states.setValue(d, (d-date0)/(pheno.getDate1()-date0));
		      }

			for (int d = (int) pheno.getDate1(); d <= pheno.getDate2(); d++) {
				phases.setValue(d, 2);
				states.setValue(d, (d-pheno.getDate1())/(pheno.getDate2()-pheno.getDate1()));
		      }
			
			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFlowerLocFixed.execute ()", "Error in FitlibFlowerLocFixed", e);
			throw new Exception("Error in FitlibFlowerLocFixed", e);
		}
	}

	public String toString() {
		return "FlowerLocFixed()";
	}


}
