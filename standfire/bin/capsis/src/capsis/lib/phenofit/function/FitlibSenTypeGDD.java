package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibMemory;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.DayLength;
import capsis.lib.phenofit.function.util.GDD;
import capsis.lib.phenofit.function.util.Sigmoid;

/**
 * The Phenofit SenTypeGDD function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibSenTypeGDD extends FitlibFunction1Phase implements FitlibPhelibFunctions {

	private double Fcrit;
	private double Tb;

	/**
	 * Constructor
	 */
	public FitlibSenTypeGDD(double Fcrit, double Tb) {
		this.Fcrit = Fcrit;
		this.Tb = Tb;
	}

	/**
	 * Constructor 2, parses a String, e.g. SenTypeGDD(13.38;29.95)
	 */
	public FitlibSenTypeGDD(String str) throws Exception {
		try {
			String s = str.replace("SenTypeGDD(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			Fcrit = Double.parseDouble(st.nextToken().trim());
			Tb = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibSenTypeGDD: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "SenTypeGDD";
	}

	public String getExpectedParams() {
		return "Fcrit,Tb";
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
			double prevState3 = memory.state;

			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);

			// // If t0 is not reached, exit
			// // Default case: if t0 is positive, it refers to pheno.birthYear
			// + 1
			// int t0Year = pheno.birthYear + 1;
			// double t0Day = (int) Math.round(t0);
			// // If t0 is negative, it refers to pheno.birthYear
			// if (t0 < 0) {
			// int nbDaysBirthDate = FitlibClimate.getNbDays(pheno.birthYear);
			// t0Year = pheno.birthYear;
			// t0Day = nbDaysBirthDate + t0Day;
			// }
			//
			// if (!(year >= t0Year && day >= t0Day))
			// return 0; // t0 is not reached

			GDD f = new GDD(Tb);

			if (prevPhase <= 3) {

				// Is phase 3 over ?
				if (prevPhase == 3 && prevState3 >= 1) {

					pheno.setDate1(year, yesterday);

				} else {

					// Process phase 3
					memory.phase = 3;

					double dayLength = new DayLength().execute(loc.getLatitude(), day);
					FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

					double Sen = 0;
					Sen = f.execute(dayClim.tmp) + prevState3 * Fcrit;

					memory.state = Sen / Fcrit;

				}

			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenTypeGDD.execute ()", "Error in FitlibSenTypeGDD", e);
			throw new Exception("Error in FitlibSenTypeGDD", e);
		}

	}

	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			double GDD = 0;
			
			// fc+ic+jg-27.1.2016
			if (date0 >= nbDays) {
				pheno.setDate1(year, nbDays);
				return 0;
			}

			int day = date0;
			GDD f = new GDD(Tb);
			do {

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				GDD = f.execute(dayClim.tmp);
				day++;

				phases.setValue(day, 3);
				states.setValue(day, GDD / Fcrit);

			} while (GDD < Fcrit && day < nbDays);

			pheno.setDate1(year, day);

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenTypeGDD.execute ()", "Error in FitlibSenTypeGDD", e);
			throw new Exception("Error in FitlibSenTypeGDD", e);
		}
	}

	public String toString() {
		return "SenTypeGDD(" + Fcrit + ";" + Tb + ")";
	}

}
