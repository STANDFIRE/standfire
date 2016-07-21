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
 * The Phenofit FitlibSenWhite function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibSenWhite extends FitlibFunction1Phase implements FitlibPhelibFunctions {

	private double Pmax;
	private double Tmax1;
	private double Tmax2;
	private double timelag;

	/**
	 * Constructor
	 */
	public FitlibSenWhite(double Pmax, double Tmax1, double Tmax2, double timelag) {
		this.Pmax = Pmax;
		this.Tmax1 = Tmax1;
		this.Tmax2 = Tmax2;
		this.timelag = timelag;
	}

	/**
	 * Constructor 2, parses a String, e.g. SenWhite(13.38;29.95;1.0;2.0)
	 */
	public FitlibSenWhite(String str) throws Exception {
		try {
			String s = str.replace("SenWhite(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			Pmax = Double.parseDouble(st.nextToken().trim());
			Tmax1 = Double.parseDouble(st.nextToken().trim());
			Tmax2 = Double.parseDouble(st.nextToken().trim());
			timelag = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibSenWhite: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "SenWhite";
	}

	public String getExpectedParams() {
		return "Pmax,Tmax1,Tmax2,timelag";
	}

	/**
	 * For the given day, compute the current phase and state, and set the
	 * end-of-phase dates when they are reached. Returns 0 if ok.
	 */
	@Override
	public double executeDaily(FitlibLocation loc, FitlibLocationClimate locClim, int year, int day, Fit5Phenology pheno, FitlibMemory memory)
			throws Exception {

		// public double executeDaily(FitlibLocation loc, FitlibLocationClimate
		// locClim, int year, int day, Fit4Phenology pheno, FitlibPhases phases,
		// FitlibStates states) throws Exception {

		try {

//			int nbDays = FitlibClimate.getNbDays(year);
//			int sept1 = FitlibClimate.get1September(year);
//			int nbDays0 = FitlibClimate.getNbDays(year - 1);
//			// day is UnfoldingDate
//			if (year == pheno.birthYear && day == 0)
//				day = Math.max(day, sept1);
//			day = Math.max(day, 201);
//			
//			int prevPhase = pheno.phase;
//			double prevState = pheno.state1;
///			int prevPhase = phases.getPrevValue(year, day);
////			double prevState = states.getPrevValue(year, day);
//			
//			int yesterday = FitlibClimate.getYesterday(year, day);
//
//			Fit4Phenology pf = pheno;
//
//			if (prevPhase == 3 || prevPhase == 2) {
//
//				// Is phase 3 over
//				if (prevPhase == 3 && prevState >= 1) {
//
//					pheno.setDate1(year, yesterday);
//				} else {
//
//					double dayLength = new DayLength().execute(loc.getLatitude(), day);
//					FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
//
//					if ((dayLength <= Pmax && dayClim.tmp <= Tmax2) || dayClim.tmp <= Tmax1 || day >= nbDays)
//
//						// {
//						pheno.setDate1(year, yesterday);
//					// stay in phase 3
//					// for(int
//					// d=Math.max(day,201);d<pheno.getDate1();d++){//Math.max(date0,201)
//					phases.setValue(day, 3);
//					// states.setValue(d,day/(pheno.getDate1()-day));
//					states.setValue(day, (day - Math.max(day, 201)) / (pheno.getDate1() - Math.max(day, 201)));
//					// }}
//				}
//
//			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenWhite.execute ()", "Error in FitlibSenWhite", e);
			throw new Exception("Error in FitlibSenWhite", e);
		}
	}

	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * the given year. Returns 0 if ok.
	 */
	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);

			// int nbDays = 365;
			// if (locClim.isBissextile(year))
			// nbDays++;

			Fit4Phenology pf = pheno;
			
			// fc+ic+jg-27.1.2016
			if (date0 >= nbDays) {
				pheno.setDate1(year, nbDays);
				return 0;
			}

			int day = Math.max(date0, 201);
			double dayLength = 0;
			FitlibClimateDay dayClim;

			do {
				day++;

				dayClim = locClim.getClimateDay(year, day);

				dayLength = new DayLength().execute(loc.getLatitude(), day);

				System.out.println(dayLength + " " + Pmax + " " + dayClim.tmp + " " + Tmax2 + " " + Tmax1 + " " + day);
				// what's gonna be the value of the
				// state in this case

			} while ((dayLength > Pmax || dayClim.tmp > Tmax2) && dayClim.tmp > Tmax1 && day < nbDays);

			pheno.setDate1(year, day);

			for (int d = Math.max(date0, 201); d < pheno.getDate1(); d++) {
				phases.setValue(d, 3);
				states.setValue(d, (d - Math.max(date0, 201)) / (pheno.getDate1() - Math.max(date0, 201)));
			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenWhite.execute ()", "Error in FitlibSenWhite", e);
			throw new Exception("Error in FitlibSenWhite", e);
		}
	}

	public String toString() {
		return "SenWhite(" + Pmax + ";" + Tmax1 + ";" + Tmax2 + ";" + timelag + ")";
	}

}
