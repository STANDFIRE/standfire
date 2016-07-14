package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibMemory;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.Chuine;
import capsis.lib.phenofit.function.util.DayLength;
import capsis.lib.phenofit.function.util.Gauzere;
import capsis.lib.phenofit.function.util.Sigmoid;
import capsis.lib.phenofit.function.util.ThresholdInferior;

/**
 * The Phenofit Gauzere function.
 * 
 * @author Isabelle Chuine, Julie Gauzère - August 2015
 */
public class FitlibGauzere extends FitlibFunction2Phases implements FitlibPhelibFunctions {

	private double t0;
	private double Vb;
	private double a1;
	private double a2;
	private double a3;
	private double d;
	private double e;
	private double Ccrit;
	private double Fcrit;

	/**
	 * Constructor
	 */
	public FitlibGauzere(double t0, double Vb, double a1, double a2, double a3, double d, double e, double Ccrit,
			double Fcrit) {

		this.t0 = t0;
		this.Vb = Vb;
		this.a1 = a1;
		this.a2 = a2;
		this.a3 = a3;
		this.d = d;
		this.e = e;
		this.Ccrit = Ccrit;
		this.Fcrit = Fcrit;

	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * Gauzere(-110;-3.347;-13.08;-0.138;17.301;194.02;8.21;8.21;8.21)
	 */
	public FitlibGauzere(String str) throws Exception {
		try {
			String s = str.replace("Gauzere(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			t0 = Double.parseDouble(st.nextToken().trim());
			Vb = Double.parseDouble(st.nextToken().trim());
			a1 = Double.parseDouble(st.nextToken().trim());
			a2 = Double.parseDouble(st.nextToken().trim());
			a3 = Double.parseDouble(st.nextToken().trim());
			d = Double.parseDouble(st.nextToken().trim());
			e = Double.parseDouble(st.nextToken().trim());
			Ccrit = Double.parseDouble(st.nextToken().trim());
			Fcrit = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibGauzere: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "Gauzere";
	}

	public String getExpectedParams() {
		return "t0,Vb,a1,a2,a3,d,e,Ccrit,Fcrit";
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

			
			ThresholdInferior thresholdInferior = new ThresholdInferior(Vb);
			Sigmoid sigmoid = new Sigmoid(d, e);
			Gauzere gauzere = new Gauzere(a1, a2, a3, Ccrit);
			
			if (prevPhase <= 1) {

				// Does phase 2 start ?
				if (prevPhase == 1 && prevState1 >= 1) {

					// For Gauzere, date1 is the phase 2 starting date
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

					// Stay in phase 1
					memory.phase = 1;
					
				}

			} 

			// Overlapping phases: phase 1 is always calculated
			FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
			double CU = prevState1 * Ccrit + thresholdInferior.execute(dayClim.tmp);
			memory.intermediateState = CU / Ccrit;
	
			double dayLength = new DayLength().execute(loc.getLatitude(), day);
			double FU = prevState2 * Fcrit + sigmoid.execute(dayClim.tmp) * gauzere.execute(CU, dayLength);
			memory.state = FU / Fcrit;
			
			if (memory.phase == 2 || prevPhase == 2) {

				// Process phase 2

				// Security: stop phase 2 at the end of 2nd year at max
				if (!pheno.isSetDate2 () && fit4Today >= nbDays) {
//				if (year == pheno.birthYear + 1 && day >= nbDays) {
					pheno.setDate2(year, nbDays);
					return 0;
				}

				memory.phase = 2;
				
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

//					// Date2 is set, reset the memory
//					memory.resetDevelopmentStates ();

				}

			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibGauzere.execute ()", "Error in FitlibGauzere", e);
			throw new Exception("Error in FitlibGauzere", e);
		}

	}

	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * the given year. Returns 0 if ok.
	 */
	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		// fc-31.8.2015 reviewed with IC and JG

		try {

			int nbDays = FitlibClimate.getNbDays(year);

			double FU = 0;
			double FU1 = 0;
			double CU = 0;
			double Y1 = 0;
			double Y2 = 0;

			int day = (int) Math.round(t0);
			
			// The loop below starts with a day++
			day--;
			
			ThresholdInferior f = new ThresholdInferior(Vb);
			Sigmoid f2 = new Sigmoid(d, e);
			Gauzere g = new Gauzere(a1, a2, a3, Ccrit);

			do {
				day++;
				
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				if (dayClim == null) {
					System.out.println("FitlibGauzere.execute (): dayClim == null: locId: " + loc.getId()
							+ " year: " + year + " day: " + day);
				}
				
				CU = CU + f.execute(dayClim.tmp);

				if (CU < Ccrit) {
					phases.setValue(day, 1);
					
				} else {
					
					if (!pheno.isSetDate1()) { // fc-10.11.2015
//					if (pheno.getDate1 () <= 0) { // means not set
						// Set the date 1
						pheno.setDate1(year, day); // leafDormancyBreakDate /
													// flowerDormancyBreakDate...
					}
					
					phases.setValue(day, 2);
				}

				double dayLength = new DayLength().execute(loc.getLatitude(), day);
				FU1 = FU;
				FU = FU + f2.execute(dayClim.tmp) * g.execute(CU, dayLength);
				
				states.setValue(day, FU / Fcrit);
				
			} while (FU < Fcrit && day < nbDays);			
				
			if (day >= nbDays) {
				pheno.setDate2(year, nbDays);
				return 0;
			}

			// Set the date 2
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
			Log.println(Log.ERROR, "FitlibGauzere.execute ()", "Error in FitlibGauzere", e);
			throw new Exception("Error in FitlibGauzere", e);
		}
	}

	public String toString() {
		return "Gauzere(" + t0 + ";" + Vb + ";" + a1 + ";" + a2 + ";" + a3 + ";" + d + ";" + e + ";" + Ccrit + ";"
				+ Fcrit + ")";
	}

	
	// COPY of EXECUTE fc-10.9.2015
	// BEFORE moving day++ at the beginning of the while loops
	/*
	  	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		// fc-31.8.2015 reviewed with IC and JG

		try {

			int nbDays = FitlibClimate.getNbDays(year);

			double FU = 0;
			double FU1 = 0;
			double CU = 0;
			double Y1 = 0;
			double Y2 = 0;

			int day = (int) Math.round(t0);

			ThresholdInferior f = new ThresholdInferior(Vb);

			do {
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				if (dayClim == null) {
					System.out.println("FitlibGauzere.execute (): dayClim == null: locId: " + loc.getId()
							+ " year: " + year + " day: " + day);
				}
				
				CU = CU + f.execute(dayClim.tmp);
				phases.setValue(day, 1);
				states.setValue(day, CU / Ccrit);

				day++;

			} while (CU < Ccrit && day < nbDays);

			// Set the date 1
			pheno.setDate1(year, day); // leafDormancyBreakDate /
										// flowerDormancyBreakDate...

			if (day >= nbDays) {
				pheno.setDate2(year, nbDays);
				return 0;
			}

			Sigmoid f2 = new Sigmoid(d, e);
			do {
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				CU = CU + f.execute(dayClim.tmp);

				Gauzere g = new Gauzere(a1, a2, a3, Ccrit);

				double dayLength = new DayLength().execute(loc.getLatitude(), day);

				FU1 = FU;
				FU = FU + f2.execute(dayClim.tmp) * g.execute(CU, dayLength);
				phases.setValue(day, 2);
				states.setValue(day, FU / Fcrit);

				day++;

			} while (FU < Fcrit && day < nbDays);

			if (day >= nbDays) {
				pheno.setDate2(year, nbDays);
				return 0;
			}

			// Set the date 2
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
			Log.println(Log.ERROR, "FitlibGauzere.execute ()", "Error in FitlibGauzere", e);
			throw new Exception("Error in FitlibGauzere", e);
		}
	}

	  

	 */
	
}
