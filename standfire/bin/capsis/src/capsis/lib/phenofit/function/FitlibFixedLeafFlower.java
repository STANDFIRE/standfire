package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibConstants;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibMemory;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.Chuine;
import capsis.lib.phenofit.function.util.Sigmoid;

/**
 * The Phenofit FixedLeafFlower function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibFixedLeafFlower extends FitlibFunction2Phases implements FitlibPhelibFunctions {

	private double FixedDateQuiesc;
	private double FixedDate;

	/**
	 * Constructor
	 */
	public FitlibFixedLeafFlower(double FixedDateQuiesc, double FixedDate) {
		this.FixedDateQuiesc = FixedDateQuiesc;
		this.FixedDate = FixedDate;
	}

	/**
	 * Constructor 2, parses a String, e.g. FixedLeafFlower(7.2;145)
	 */
	public FitlibFixedLeafFlower(String str) throws Exception {
		try {
			String s = str.replace("FixedLeafFlower(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			FixedDateQuiesc = Double.parseDouble(st.nextToken().trim());
			FixedDate = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibFixedLeafFlower: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "FixedLeafFlower";
	}

	public String getExpectedParams() {
		return "FixedDateQuiesc,FixedDate";
	}

	/**
	 * For the given day, compute the current phase and state, and set the
	 * end-of-phase dates when they are reached. Returns 0 if ok.
	 */
	@Override
	public double executeDaily(FitlibLocation loc, FitlibLocationClimate locClim, int year, int day,
			Fit5Phenology pheno, FitlibMemory memory) throws Exception {

		// public double executeDaily(FitlibLocation loc, FitlibLocationClimate
		// locClim, int year, int day, Fit4Phenology pheno, FitlibPhases phases,
		// FitlibStates states) throws Exception {

		try {

			// When date 2 is set, nothing more to do
			if (pheno.isSetDate2()) // fc-10.11.2015
				return 0;

			int nbDays = FitlibClimate.getNbDays(year);
			int sept1 = FitlibClimate.get1September(year);
			int nbDays0 = FitlibClimate.getNbDays(year - 1);

			int prevPhase = memory.phase;
			double prevState1 = memory.intermediateState;
			double prevState2 = memory.state;

			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);

			if (prevPhase <= 1) {

				// Does phase 2 start ?
				if (fit4Today > FixedDateQuiesc) {
					
					pheno.setDate1(year, yesterday);

					// Start phase 2
					memory.phase = 2;

				} else {

					// Process phase 1

//					// Security
//					if (pheno.getApproxPhenoAge(year, day) > 300) {
//						pheno.setDate1(year, nbDays);
//						pheno.setDate2(year, nbDays);
//						return 0;
//					}

					memory.phase = 1;

					memory.intermediateState = (pheno.getPhenofit4Date (year, day) - FitlibConstants.PHENO_START_DAY)
							/ (FixedDateQuiesc - FitlibConstants.PHENO_START_DAY);

				}

			}

			if (memory.phase == 2 || prevPhase == 2) {

				// Process phase 2

				// Security: stop phase 2 at the end of 2nd year at max
				if (!pheno.isSetDate2 () && fit4Today >= nbDays) {
//				if (year == pheno.birthYear + 1 && day >= nbDays) {
					pheno.setDate2(year, nbDays);
					return 0;
				}

				memory.phase = 2;

				memory.state = (pheno.getPhenofit4Date (year, day) - pheno.getDate1()) / (FixedDate - pheno.getDate1());

				// Set date2
				if (fit4Today > FixedDate) {
					
					pheno.setDate2(year, FixedDate);
					
//					// Date2 is set, reset the memory
//					memory.resetDevelopmentStates();

				}

			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFixedLeafFlower.execute ()", "Error in FitlibFixedLeafFlower", e);
			throw new Exception("Error in FitlibFixedLeafFlower", e);
		}

	}

	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * all processed days. Returns 0 if ok.
	 */
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {

			Fit4Phenology pf = pheno;

			pheno.setDate1(year, FixedDateQuiesc);

			pheno.setDate2(year, FixedDate);

			for (int d = date0; d <= pheno.getDate1(); d++) {
				phases.setValue(d, 1);
				states.setValue(d, (d - date0) / (pheno.getDate1() - date0));
			}

			for (int d = (int) pheno.getDate1() + 1; d <= pheno.getDate2(); d++) {
				phases.setValue(d, 2);
				states.setValue(d, (d - pheno.getDate1()) / (pheno.getDate2() - pheno.getDate1()));
			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFixedLeafFlower.execute ()", "Error in FitlibFixedLeafFlower", e);
			throw new Exception("Error in FitlibFixedLeafFlower", e);
		}
	}

	public String toString() {
		return "FixedLeafFlower(" + FixedDateQuiesc + ";" + FixedDate + ")";
	}

}
