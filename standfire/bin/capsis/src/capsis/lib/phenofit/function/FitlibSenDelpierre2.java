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
import capsis.lib.phenofit.function.util.DayLength;

/**
 * The Phenofit SenDelpierre2 function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibSenDelpierre2 extends FitlibFunction1Phase implements FitlibPhelibFunctions {

	private double Pb;
	private double Tb;
	private double alpha;
	private double beta;
	private double Scrit;
	private double timelag;

	/**
	 * Constructor
	 */
	public FitlibSenDelpierre2(double Pb, double Tb, double alpha, double beta, double Scrit, double timelag) {
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
	public FitlibSenDelpierre2(String str) throws Exception {
		try {
			String s = str.replace("SenDelpierre2(", "");
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
			throw new Exception("FitlibSenDelpierre2: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "SenDelpierre2";
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
						Sen = Math.pow(Tb - dayClim.tmp, alpha) * Math.pow(1 - dayLength / Pb, beta) + prevState3 * Scrit;

					memory.state = Sen / Scrit;
//					states.setValue(day, Sen / Scrit);
				}

			}

			return 0; // ok
			
		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenDelpierre2.execute ()", "Error in FitlibSenDelpierre2", e);
			throw new Exception("Error in FitlibSenDelpierre2", e);
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

			double Sen = 0;
			
			// fc+ic+jg-27.1.2016
			if (date0 >= nbDays) {
				pheno.setDate1(year, nbDays);
				return 0;
			}

			int day = Math.max(date0,201);

			do {
				day++;

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				double dayLength = new DayLength().execute(loc.getLatitude(), day);

				if (dayLength < Pb && dayClim.tmp < Tb) {
					Sen = Sen + Math.pow(Tb - dayClim.tmp, alpha) * Math.pow(1 - dayLength / Pb, beta);
				}

				phases.setValue(day, 3);
				states.setValue(day, Sen / Scrit);

			} while (Sen < Scrit && day < nbDays);

			pheno.setDate1(year, day);

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenDelpierre2.execute ()", "Error in FitlibSenDelpierre2", e);
			throw new Exception("Error in FitlibSenDelpierre2", e);
		}
	}

	public String toString() {
		return "SenDelpierre2(" + Pb + ";" + Tb + ";" + alpha + ";" + beta + ";" + Scrit + ";" + timelag + ")";
	}

}
