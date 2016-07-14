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
import capsis.lib.phenofit.function.util.DayLength;
import capsis.lib.phenofit.function.util.Sigmoid;

/**
 * The Phenofit SenDelpierre function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibSenDelpierre extends FitlibFunction1Phase implements FitlibPhelibFunctions {

	private double Pb;
	private double Tb;
	private double alpha;
	private double beta;
	private double Scrit;
	private double timelag;

	/**
	 * Constructor
	 */
	public FitlibSenDelpierre(double Pb, double Tb, double alpha, double beta, double Scrit, double timelag) {
		this.Pb = Pb;
		this.Tb = Tb;
		this.alpha = alpha;
		this.beta = beta;
		this.Scrit = Scrit;
		this.timelag = timelag;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * SenDelpierre(13.38;29.95;1.0;2.0;570.6;0.0)
	 */
	public FitlibSenDelpierre(String str) throws Exception {
		try {
			String s = str.replace("SenDelpierre(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			Pb = Double.parseDouble(st.nextToken().trim());
			Tb = Double.parseDouble(st.nextToken().trim());
			alpha = Double.parseDouble(st.nextToken().trim());
			beta = Double.parseDouble(st.nextToken().trim());
			Scrit = Double.parseDouble(st.nextToken().trim());
			timelag = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibSenDelpierre: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "SenDelpierre";
	}

	public String getExpectedParams() {
		return "Pb,Tb,alpha,beta,Scrit,timelag";
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
			// When date 1 is set, nothing more to do
			if (pheno.isSetDate1()) // fc-10.11.2015
				return 0;

			int nbDays = FitlibClimate.getNbDays(year);
			int sept1 = FitlibClimate.get1September(year);
			int nbDays0 = FitlibClimate.getNbDays(year - 1);

			int prevPhase = memory.phase;
			double prevState3 = memory.state;

			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);

			
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
					if (fit4Today > FitlibConstants.SUMMER_SOLSTICE && dayLength < Pb && dayClim.tmp < Tb)
//					if (day > FitlibConstants.SUMMER_SOLSTICE && dayLength < Pb && dayClim.tmp < Tb)
						Sen = Math.pow(Tb - dayClim.tmp, alpha) * Math.pow(dayLength / Pb, beta) + prevState3 * Scrit;

					memory.state = Sen / Scrit;
//					states.setValue(day, Sen / Scrit);
				}

			}

			return 0; // ok
			
		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenDelpierre.execute ()", "Error in FitlibSenDelpierre", e);
			throw new Exception("Error in FitlibSenDelpierre", e);
		}

	}

	// int nbDays = FitlibClimate.getNbDays(year);
	//
	// // int nbDays = 365;
	// // if (locClim.isBissextile(year))
	// // nbDays++;
	//
	// Fit4Phenology pf = pheno;
	// double Sen = 0;
	//
	//
	// if (phases.getValue(day) == 0 && phases.getValue(day) <=
	// FitlibConstants.SUMMER_SOLSTICE) {// on devrait commencer au jour qui
	// suit la date de debourement mais on la conait pas
	// phases.setValue(day, 3);
	// }
	// else {
	//
	//
	// FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
	//
	// double dayLength = new DayLength().execute(loc.getLatitude(), day);
	//
	//
	// if (day > FitlibConstants.SUMMER_SOLSTICE && dayLength < Pb &&
	// dayClim.tmp < Tb) {
	//
	// Sen = Math.pow(Tb - dayClim.tmp, alpha) * Math.pow(dayLength / Pb, beta);
	// }
	// states.setValue(day, (Sen+ states.getValue(day - 1)) / Scrit);
	//
	// if (states.getValue(day) < 1)
	// phases.setValue(day, 3);
	//
	// } setDate1(day);
	//
	// return 0; // ok
	// //////////////////////////
	
	
	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * the given year. Returns 0 if ok.
	 */
	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);

			Fit4Phenology pf = pheno;

			double Sen = 0;

			// int day = Math.max(date0,201); // we can replace 201 by summer
			// solstice

			/*
			 * if(date0<201)
			 * 
			 * for (int d=date0;d<201;d++) {//09/03/2015 filling values between
			 * leafUnfoldingDate and 201 to make the LeafSenescenceDate the same
			 * between capsis and delphi versions
			 * 
			 * phases.setValue(d, 3); }
			 * 
			 * else
			 */
			
			// fc+ic+jg-27.1.2016
			if (date0 >= nbDays) {
				pheno.setDate1(year, nbDays);
				return 0;
			}
			
			int day = date0; // 06/03/2015 checked with IC to make LeafIndex
								// values the same between capsis and delphi
								// versions

			// fc+ym-5.5.2015 TO BE CHECKED with I. Chuine
			// During our tests, we swapped the param values of Unichill and it
			// was not yet detected by the species loader
			// We then arrived here with date0 = nbDays = 366 and the
			// function crashed below in line 118 because dayClim == null
			// We added these 4 lines
			// if (date0 >= nbDays) {
			// pheno.setDate1(date0);
			// return 0; // ok
			// }
			// Should we run further checks somewhere ?
			// fc+ym-5.5.2015 TO BE CHECKED with I. Chuine

			do {

				day++;

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				double dayLength = new DayLength().execute(loc.getLatitude(), day);

				if (dayClim == null) {
					Log.println(Log.ERROR, "FitlibSenDelpierre.execute ()", "Could not find dayClim for year: " + year
							+ " and day: " + day + ", date0: " + date0 + ", nbDays: " + nbDays + ", dayClim = null");
				}

				if (day > FitlibConstants.SUMMER_SOLSTICE && dayLength < Pb && dayClim.tmp < Tb) { // declared
																									// as
																									// max
																									// of
																									// 201
																									// and
																									// date0
					Sen = Sen + Math.pow(Tb - dayClim.tmp, alpha) * Math.pow(dayLength / Pb, beta);

				}

				phases.setValue(day, 3);
				states.setValue(day, Sen / Scrit);

			} while (Sen < Scrit && day < nbDays);

			pheno.setDate1(year, day);

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenDelpierre.execute ()", "Error in FitlibSenDelpierre", e);
			throw new Exception("Error in FitlibSenDelpierre", e);
		}
	}

	public String toString() {
		return "SenDelpierre(" + Pb + ";" + Tb + ";" + alpha + ";" + beta + ";" + Scrit + ";" + timelag + ")";
	}

}
