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
 * The Phenofit Unified function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibUnified extends FitlibFunction2Phases implements FitlibPhelibFunctions {

	private double t0; // fc+ic+jg-27.1.2016 added t0
	private double a;
	private double b;
	private double c;
	private double d;
	private double e;
	private double w;
	private double z;
	private double Ccrit;
	private double tc;

	/**
	 * Constructor
	 */
	public FitlibUnified(double t0, double a, double b, double c, double d, double e, double w, double z, double Ccrit, double tc) {
		this.t0 = t0;
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.w = w;
		this.z = z;
		this.Ccrit = Ccrit;
		this.tc = tc;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * Unified(-110;0.8;-29.99;-8.75;-0.577;12.05;2138.4;-0.05011;89.83;-18.11)
	 */
	public FitlibUnified(String str) throws Exception {
		try {
			String s = str.replace("Unified(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			t0 = Double.parseDouble(st.nextToken().trim());
			a = Double.parseDouble(st.nextToken().trim());
			b = Double.parseDouble(st.nextToken().trim());
			c = Double.parseDouble(st.nextToken().trim());
			d = Double.parseDouble(st.nextToken().trim());
			e = Double.parseDouble(st.nextToken().trim());
			w = Double.parseDouble(st.nextToken().trim());
			z = Double.parseDouble(st.nextToken().trim());
			Ccrit = Double.parseDouble(st.nextToken().trim());
			tc = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibUnified: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "Unified";
	}

	public String getExpectedParams() {
		return "t0,a,b,c,d,e,w,z,Ccrit,tc";
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

			
//			// Default case: if tc is positive, it refers to pheno.birthYear + 1
//			int tcYear = pheno.birthYear + 1;
//			double tcDay = (int) Math.round(tc);
//			// If tc is negative, it refers to pheno.birthYear
//			if (tc < 0) {
//				int nbDaysBirthDate = FitlibClimate.getNbDays(pheno.birthYear);
//				tcYear = pheno.birthYear;
//				tcDay = nbDaysBirthDate + tcDay;
//			}
		
			
			Sigmoid sigmoid = new Sigmoid(d, e);
			Chuine chuine = new Chuine(a, b, c);

			if (prevPhase <= 1) {

				// Does phase 2 start ?
				if (prevPhase == 1 && prevState1 >= 1) {

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

					FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
					double CU = chuine.execute(dayClim.tmp) + prevState1 * Ccrit;
					memory.intermediateState = CU / Ccrit;

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

				// We need the prev value of Fcrit_unified fc+ic+jg-12.11.2015
				double prevCU = memory.intermediateState * Ccrit;
				double prevFcrit_unified = 0;
				if (z * prevCU > 600)
					prevFcrit_unified = 100000000;
				else
					prevFcrit_unified = w * Math.exp(z * prevCU);
				// We need the prev value of Fcrit_unified fc+ic+jg-12.11.2015

				// MUST BE CHECKED fc-27.1.2016
//				double CU = chuine.execute(dayClim.tmp) + prevState1 * Ccrit;				
//				memory.intermediateState = CU / Ccrit;
				
				double CU = prevCU;
				
				// If tc not reached yet
				if (fit4Today < tc) {
//				if (!(year >= tcYear && day >= tcDay)) {
					
					CU = chuine.execute(dayClim.tmp) + prevState1 * Ccrit;				
					memory.intermediateState = CU / Ccrit;

				}
				// MUST BE CHECKED fc-27.1.2016
			
				
				
				double Fcrit_unified = 0;
				if (z * CU > 600)
					Fcrit_unified = 100000000;
				else
					Fcrit_unified = w * Math.exp(z * CU);

				double FU = prevState2 * prevFcrit_unified + sigmoid.execute(dayClim.tmp);

				memory.state = FU / Fcrit_unified;

				
				
				
				// TO BE CONTINUED, see below execute (...): a section must be
				// added with the following test
				// fc+ic+jg-12.11.2015
				// } while (day < tc && FU < Fcrit_unified);
				// if (day >= tc || FU >= Fcrit_unified);

				
				
				
				// Set date2
				if (memory.state >= 1) {
					double FU1 = prevState2 * Fcrit_unified;
					double Y1 = FU - FU1; // statesVal = prevState
					double Y2 = Fcrit_unified - FU1; // statesVal = prevState
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
			Log.println(Log.ERROR, "FitlibUnified.execute ()", "Error in FitlibUnified", e);
			throw new Exception("Error in FitlibUnified", e);
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

			double FU = 0;
			double FU1 = 0;
			double CU = 0;
			double Y1 = 0;
			double Y2 = 0;
			double Fcrit_unified = 0;

//			int day = date0;
//			assert ((tc > date0) && (tc <= nbDays));

			int day = (int) Math.round(t0);
			
			// The loop below starts with a day++
			day--;

			Chuine f = new Chuine(a, b, c);
			do {
				day++;
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				
				if (day < tc) 
					CU = CU + f.execute(dayClim.tmp);
				
				phases.setValue(day, 1);
				states.setValue(day, CU / Ccrit);

			} while (CU < Ccrit && day < nbDays);

			// Set the date 1
			pheno.setDate1(year, day); // leafDormancyBreakDate /
										// flowerDormancyBreakDate...

			if (tc < pheno.getDate1() || day >= FitlibConstants.FIRST_SEPTEMBER) {
				pheno.setDate2(year, nbDays);
				return 0;
			}

			Sigmoid f2 = new Sigmoid(d, e);
			do {
				day++;
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				
				if (day < tc) 
					CU = CU + f.execute(dayClim.tmp);

				if (z * CU > 600)
					Fcrit_unified = 100000000;
				else
					Fcrit_unified = w * Math.exp(z * CU);

				FU1 = FU;
				// double debug = dayClim.tmp;
				FU = FU + f2.execute(dayClim.tmp);

				phases.setValue(day, 2);
				states.setValue(day, FU / Fcrit_unified);

			} while (FU < Fcrit_unified && day < nbDays);

//			if (FU < Fcrit_unified) {
//				do {
//					day++;
//					FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
//
//					if (z * CU > 600)
//						Fcrit_unified = 100000000;
//					else
//						Fcrit_unified = w * Math.exp(z * CU);
//
//					FU1 = FU;
//					// double debug = dayClim.tmp;
//					FU = FU + f2.execute(dayClim.tmp);
//					phases.setValue(day, 2);
//					states.setValue(day, FU / Fcrit_unified);
//
//				} while (FU < Fcrit_unified && day < nbDays);
//			}

			if (day >= nbDays) {
				pheno.setDate2(year, nbDays);
				return 0;
			}

			Y1 = FU - FU1;
			Y2 = Fcrit_unified - FU1;
			if (Y2 < 0) {
				pheno.setDate2(year, day - 1);
			} else if (Y2 != Y1 && Y1 != 0) {
				pheno.setDate2(year, day - 1 + (Y2 / Y1));
			} else {
				pheno.setDate2(year, day);
			}

			// if (getFcrit_unified() < FitlibInitialParameters.ALMOST_ZERO)
			// setFcrit_unified(FitlibInitialParameters.ALMOST_ZERO);

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibUnified.execute ()", "Error in FitlibUniChill", e);
			throw new Exception("Error in FitlibUnified", e);
		}

	}

	public String toString() {
		return "Unified(" + t0 + ";" + a + ";" + b + ";" + c + ";" + d + ";" + e + ";" + w + ";" + z + ";" + Ccrit + ";" + tc
				+ ")";
	}

	// public static void main(String[] args) {
	// System.out.println("Correct answer: " + "a,b,c,d,e,w,z,Ccrit,tc");
	// test("");
	// test("a");
	// test("a,b,c,d,e,w,z,Ccrit");
	// test("b,c,d,e,w,z,Ccrit,tc");
	// test("a,b,c,d,w,z,Ccrit,tc");
	// test("i,a,b,c,d,e,w,z,Ccrit,tc");
	// test("a,b,c,d,e,w,z,Ccrit,tc,j");
	// test("a, b,c,d,e, w,z,Ccrit,tc");
	// test("a;b,c,d,e,w,z,Ccrit,tc");
	// test("a,b,c,d,e,w,z,Ccrit,tc");
	// }
	//
	// private static void test(String a) {
	// try {
	// check(a);
	// System.out.println("FitlibUnified " + a + " OK");
	// } catch (Exception e) {
	// System.out.println("FitlibUnified " + a + " error: " + e);
	// }
	// }

}