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
import capsis.lib.phenofit.function.util.Sigmoid;
import capsis.lib.phenofit.function.util.ThresholdInferior;

/**
 * The Phenofit UniForc function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibUniForc extends FitlibFunction2Phases implements FitlibPhelibFunctions {

	private double t0;
	private double d;
	private double e;
	private double Fcrit;

	/**
	 * Constructor
	 */
	public FitlibUniForc(double t0, double d, double e, double Fcrit) {
		this.t0 = t0;
		this.d = d;
		this.e = e;
		this.Fcrit = Fcrit;
	}

	/**
	 * Constructor 2, parses a String, e.g. UniForc(76;-0.138;17.301;8.21)
	 */
	public FitlibUniForc(String str) throws Exception {
		try {
			String s = str.replace("UniForc(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			t0 = Double.parseDouble(st.nextToken().trim());
			d = Double.parseDouble(st.nextToken().trim());
			e = Double.parseDouble(st.nextToken().trim());
			Fcrit = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibUniForc: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "UniForc";
	}

	public String getExpectedParams() {
		return "t0,d,e,Fcrit";
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
			if (pheno.isSetDate2())
				return 0;

			int nbDays = FitlibClimate.getNbDays(year);
			int sept1 = FitlibClimate.get1September(year);
			int nbDays0 = FitlibClimate.getNbDays(year - 1);

			int prevPhase = memory.phase;
			double prevState1 = memory.intermediateState;
			double prevState2 = memory.state;

			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);


			// If t0 is not reached, exit
			double t0Day = (int) Math.round(t0);
			if (fit4Today < t0Day) {
				return 0;
			}

			Sigmoid sigmoid = new Sigmoid(d, e);

			if (prevPhase <= 1) {

				// Does phase 2 start ?
				if (pheno.getPhenofit4Date(year, day) > t0) {

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

					memory.intermediateState = (day - FitlibConstants.PHENO_START_DAY)
							/ (t0 - FitlibConstants.PHENO_START_DAY);

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

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				double FU = prevState2 * Fcrit + sigmoid.execute(dayClim.tmp);
				memory.state = FU / Fcrit;

				// Set date2
				if (memory.state >= 1) {
					double FU1 = prevState2 * Fcrit;
					double Y1 = FU - FU1; // statesVal = prevState
					double Y2 = Fcrit - FU1; // statesVal = prevState
					if (Y2 < 0) {
						pheno.setDate2(year, yesterday);
					} else if (Y2 != Y1 && Y1 != 0) {
						pheno.setDate2(year, yesterday + (Y2 / Y1));
					} else {
						pheno.setDate2(year, day);
					}

					// // Date2 is set, reset the memory
					// memory.resetDevelopmentStates ();

				}

			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibUniForc.execute ()", "Error in FitlibUniForc", e);
			throw new Exception("Error in FitlibUniForc", e);
		}

	}

	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * all processed days. Returns 0 if ok.
	 */
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);

			// int nbDays = 365;
			// if (locClim.isBissextile(year))
			// nbDays++;

			Fit4Phenology pf = pheno;

			assert ((t0 > date0) && (t0 <= nbDays));

			double FU = 0;
			double FU1 = 0;
			double Y1 = 0;
			double Y2 = 0;

			// Set the date 1
			pheno.setDate1(year, t0);

			for (int d = date0; d < t0; d++) {  // ic, jg 31/03/2016 replaced <= by < to be coherent with PMP and 2-phase models, phase 2 starts at t0 and not t0+1
				phases.setValue(d, 1);
				states.setValue(d, (d - date0) / (pheno.getDate1() - date0));
			}

			int day = (int) t0;
						
			Sigmoid f2 = new Sigmoid(d, e);

			do {
				day++;
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				FU1 = FU;
				// double debug = dayClim.tmp;
				FU = FU + f2.execute(dayClim.tmp);
				phases.setValue(day, 2);
				states.setValue(day, FU / Fcrit);

			} while (FU < Fcrit && day < nbDays);

			if (day >= nbDays) {
				pheno.setDate2(year, nbDays);
				return 0;
			}

			Y1 = FU - FU1;
			Y2 = Fcrit - FU1;
			if (Y2 < 0) {
				pheno.setDate2(year, day - 1);
			} else if (Y2 != Y1 && Y1 != 0) {
				pheno.setDate2(year, day - 1 + (Y2 / Y1));
			} else {
				pheno.setDate2(year, day);
			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibUniForc.execute ()", "Error in FitlibUniForc", e);
			throw new Exception("Error in FitlibUniForc", e);
		}
	}

	public String toString() {
		return "UniForc(" + t0 + ";" + d + ";" + e + ";" + Fcrit + ")";
	}

}