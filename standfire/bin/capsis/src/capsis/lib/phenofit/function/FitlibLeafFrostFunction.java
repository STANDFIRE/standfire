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
 * The Phenofit Frost function for leaves.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibLeafFrostFunction extends FitlibStatesFunction implements FitlibPhelibFunctions {

	// private double FHfrmax1;
	// private double FHfrmax2;
	private double FHminfe;
	// private double FHminfl;
	private double Te1;
	private double Te2;
	private double FHtfemax;
	// private double FHtflmax;
	private double FHpfemax;
	// private double FHpflmax;
	private double NL1;
	private double NL2;

	/**
	 * Constructor
	 */
	public FitlibLeafFrostFunction(double FHminfe, double Te1, double Te2, double FHtfemax, double FHpfemax,
			double NL1, double NL2) {
		this.FHminfe = FHminfe;
		this.Te1 = Te1;
		this.Te2 = Te2;
		this.FHtfemax = FHtfemax;
		this.FHpfemax = FHpfemax;
		this.NL1 = NL1;
		this.NL2 = NL2;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * Frost(-6.0;-20.0;-5.0;-5.0;10.0;-16.0;-8.0;-8.0;-12.0;-12.0;10.0;16.0)
	 */
	public FitlibLeafFrostFunction(String str) throws Exception {
		try {
			String s = str.replace("Frost(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			double FHfrmax1 = Double.parseDouble(st.nextToken().trim());
			double FHfrmax2 = Double.parseDouble(st.nextToken().trim());
			FHminfe = Double.parseDouble(st.nextToken().trim());
			double FHminfl = Double.parseDouble(st.nextToken().trim());
			Te1 = Double.parseDouble(st.nextToken().trim());
			Te2 = Double.parseDouble(st.nextToken().trim());
			FHtfemax = Double.parseDouble(st.nextToken().trim());
			double FHtflmax = Double.parseDouble(st.nextToken().trim());
			FHpfemax = Double.parseDouble(st.nextToken().trim());
			double FHpflmax = Double.parseDouble(st.nextToken().trim());
			NL1 = Double.parseDouble(st.nextToken().trim());
			NL2 = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibLeafFrostFunction: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "Frost";
	}

	public String getExpectedParams() {
		return "FHfrmax1,FHfrmax2,FHminfe,FHminfl,Te1,Te2,FHtfemax,FHtflmax,FHpfemax,FHpflmax,NL1,NL2";
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
			
			int nbDays = FitlibClimate.getNbDays(year);
			int sept1 = FitlibClimate.get1September(year);
			int nbDays0 = FitlibClimate.getNbDays(year - 1);

			int prevPhase = memory.phase;
			double prevState1 = memory.intermediateState;
			double prevState2 = memory.state;
			
			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);

			if (pheno.getLeafUnfoldingDate() >= nbDays) {
				pheno.setLeafIndex(FitlibConstants.ALMOST_ZERO);
				// loc.leafFrostStates keeps zero values for all days
				return 0; // ok
			}

			double NL = 0; // night length, hour fractions
			// double FD = 0; // frost damage, unitless, [0,1]
			// double FH = FHminfe; // frost hardiness, celsius degrees
			double dFHti = 0; // temperature component of frost hardiness,
								// celsius degrees
			double dFHpi = 0; // photoperiod component of frost hardiness,
								// celsius degrees
			double BF = 0; //
			double CR = 0; // hardening competence

			int developmentPhase = memory.phase;
			double developmentStateIntermediate = memory.intermediateState;
			double developmentState = memory.state;
			double prevFrostDamage = memory.frostDamage; // frost damage,
															// unitless, [0,1]
			double prevFrostHardiness = memory.frostHardiness; // frost
																// hardiness,
																// celsius
																// degrees
			if (prevFrostHardiness >= 100)
				prevFrostHardiness = FHminfe; // init

			// int leafDevelopmentPhase = phases.getValue(day);
			// double leafDevelopmentState = states.getValue(day);

			// For deciduous trees, leaves die at leaves senescence date (leafIndex becomes null)
			//if (pheno.isDeciduous() && pheno.isSetLeafSenescenceDate () && fit4Today > pheno.getLeafSenescenceDate()) {
			//	pheno.setLeafIndex(1 - prevFrostDamage);
				// From now on, the function should exit directly at its beginning for the next days
			//	return 0;
			//}
			// ic 04/07/16 la condition était à l'envers
			//// For deciduous trees, leaves die at leaves senescence date (leafIndex becomes null)
				if (pheno.isDeciduous() && pheno.isSetLeafSenescenceDate () && fit4Today > pheno.getLeafSenescenceDate()) {
					pheno.setLeafIndex(0);
					}
					else pheno.setLeafIndex(1 - prevFrostDamage);
					// From now on, the function should exit directly at its beginning for the next days
					
			// ic-5.2.2015 Math.abs SHOULD BE REMOVED in the next version of
			// Phenofit
			NL = 24 - Math.abs(new DayLength().execute(loc.getLatitude(), day));

			if (developmentPhase == 1) {
				CR = 1;
			} else if (developmentPhase == 2) {
				CR = Math.max(0, 1 - developmentState);
			} else if (developmentPhase == 3 && !pheno.isSetLeafSenescenceDate ()) {
				CR = 0;
			} else {
				CR = 1; // sempervirens only
			}

			FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

			if (dayClim.tmn > Te1) {
				dFHti = 0;
			} else if (dayClim.tmn < Te2) {
				dFHti = FHtfemax;
			} else {
				dFHti = dFHt(dayClim.tmn, Te1, Te2, FHtfemax);
			}

			if (CR == 0) {
				dFHpi = 0;
			} else if (fit4Today > FitlibConstants.WINTER_SOLSTICE && fit4Today < FitlibConstants.SUMMER_SOLSTICE) {
//			} else if (day > FitlibConstants.WINTER_SOLSTICE && day < FitlibConstants.SUMMER_SOLSTICE) {
				dFHpi = FHpfemax;
			} else if (NL > NL2) {
				dFHpi = FHpfemax;
			} else if (NL < NL1) {
				dFHpi = 0;
			} else {
				dFHpi = dFHp(NL, NL1, NL2, FHpfemax);
			}

			double frostHardiness = (4d / 5d) * prevFrostHardiness + (1d / 5d) * (FHminfe + CR * (dFHti + dFHpi));
			BF = -0.3 - 1.5 * Math.exp(0.1 * frostHardiness);
			double frostDamage = Math.min(1d,
					prevFrostDamage + 1d / (1d + Math.exp(BF * (frostHardiness - dayClim.tmn))));

//			System.out.println("dayClim.tmn: "+dayClim.tmn); // rounded ???
			
			memory.frostDamage = frostDamage;
			memory.frostHardiness = frostHardiness;

			// // Trace tmp to check 1955
			// if (year == 1955 && loc.getId() == 38) {
			// System.out.println("loc: "+loc.getId
			// ()+" year: "+year+" day: "+day+" NL: "+NL+" CR: "+CR+" dFHti: "+dFHti+" dFHpi: "+dFHpi+" FH: "+FH+" BF: "+BF+" FD: "+FD+" 1-FD: "+(1-FD));
			// }

			if (frostDamage < 1){
				pheno.setLeafIndex(1 - frostDamage);
			} else {
				pheno.setLeafIndex(FitlibConstants.ALMOST_ZERO);
			}

			// if (FD >= 1);

			// pheno.setLeafIndex(memory.frostState);
			// pheno.setLeafIndex(pheno.leafFrostStates.getValue(day)); //
			// sempervirens

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLeafFrostFunction.execute ()", "Error in FitlibLeafFrostFunction", e);
			throw new Exception("Error in FitlibLeafFrostFunction", e);
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

			// this method writes in loc.leafFrostStates

			if (pf.getLeafUnfoldingDate() >= nbDays) {
				pf.setLeafIndex(FitlibConstants.ALMOST_ZERO);
				// pf.leafFrostStates keeps zero values for all days
				return 0; // ok
			}

			double NL = 0; // night length, hour fractions
			double FD = 0; // frost damage, unitless, [0,1]
			double FH = FHminfe; // frost hardiness, celsius degrees
			double dFHti = 0; // temperature component of frost hardiness,
								// celsius degrees
			double dFHpi = 0; // photoperiod component of frost hardiness,
								// celsius degrees
			double BF = 0; //
			double CR = 0; // hardening competence

			int day = date0;

			do {
				day++;

				int leafDevelopmentPhase = phases.getValue(day);
				double leafDevelopmentState = states.getValue(day);

				// For deciduous trees, leaves die at leaves senescence date

				//if (leafDevelopmentPhase == 0 && pf.isDeciduous()) { // 23/03/2015 modification and add condition within leafHabit value either deciduous or evergreen
				if (day > (int)Math.round(pf.leafSenescenceDate) && pf.isDeciduous()) {  // fc+ic+jg-28.1.2016  
//				if (leafDevelopmentPhase == 0 && day > (int)Math.round(pf.leafSenescenceDate) && pf.isDeciduous()) {  //15/01/16  essai correction Isabelle														
//				if (leafDevelopmentPhase == 0 && day == (int)Math.round(pf.leafSenescenceDate)+1 && pf.isDeciduous()) {  //12/01/16  essai correction Isabelle														
					pf.setLeafIndex(pf.leafFrostStates.getValue(day - 1)); // deciduous
					return 0;
				}

				// ic-5.2.2015 Math.abs SHOULD BE REMOVED in the next version of
				// Phenofit
				NL = 24 - Math.abs(new DayLength().execute(loc.getLatitude(), day));

				if (leafDevelopmentPhase == 1) {
					CR = 1;
				} else if (leafDevelopmentPhase == 2) {
					CR = Math.max(0, 1 - leafDevelopmentState);
				} else if (leafDevelopmentPhase == 3) {
					CR = 0;
				} else {
					CR = 1; // sempervirens only
				}

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				if (dayClim.tmn > Te1) {
					dFHti = 0;
				} else if (dayClim.tmn < Te2) {
					dFHti = FHtfemax;
				} else {
					dFHti = dFHt(dayClim.tmn, Te1, Te2, FHtfemax);
				}

				if (CR == 0) {
					dFHpi = 0;
				} else if (day > FitlibConstants.WINTER_SOLSTICE && day < FitlibConstants.SUMMER_SOLSTICE) {
					dFHpi = FHpfemax;
				} else if (NL > NL2) {
					dFHpi = FHpfemax;
				} else if (NL < NL1) {
					dFHpi = 0;
				} else {
					dFHpi = dFHp(NL, NL1, NL2, FHpfemax);
				}

				FH = (4d / 5d) * FH + (1d / 5d) * (FHminfe + CR * (dFHti + dFHpi));
				BF = -0.3 - 1.5 * Math.exp(0.1 * FH);
				FD = Math.min(1d, FD + 1d / (1d + Math.exp(BF * (FH - dayClim.tmn))));

				// // Trace tmp to check 1955
				// if (year == 1955 && loc.getId() == 38) {
				// System.out.println("loc: "+loc.getId
				// ()+" year: "+year+" day: "+day+" NL: "+NL+" CR: "+CR+" dFHti: "+dFHti+" dFHpi: "+dFHpi+" FH: "+FH+" BF: "+BF+" FD: "+FD+" 1-FD: "+(1-FD));
				// }

//				if (FD < 1) { ISA 14/01/16 leafFrostState must be 0 after senescence date for the calculation of fruit maturation
				if (FD < 1 && day <= pf.leafSenescenceDate ) {
					pf.leafFrostStates.setValue(day, 1 - FD);
				} else {
					pf.leafFrostStates.setValue(day, FitlibConstants.ALMOST_ZERO);
				}

			} while (day < nbDays && FD < 1);

			pf.setLeafIndex(pf.leafFrostStates.getValue(day)); // sempervirens

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLeafFrostFunction.execute ()", "Error in FitlibLeafFrostFunction", e);
			throw new Exception("Error in FitlibLeafFrostFunction", e);
		}

	}

	static protected double dFHt(double t, double temp1, double temp2, double max) {
		if (t > temp1) {
			return 0;
		} else if (t < temp2) {
			return max;
		} else {
			return max - (max / (temp1 - temp2)) * (t - temp2);
		}
	}

	static protected double dFHp(double nl, double nl1, double nl2, double max) {
		if (nl < nl1) {
			return 0;
		} else if (nl > nl2) {
			return max;
		} else {
			return max / (nl2 - nl1) * (nl - nl1);
		}

	}

	public String toString() {
		return "LeafFrostFunction(" + FHminfe + ";" + Te1 + ";" + Te2 + ";" + FHtfemax + ";" + FHpfemax + ";" + NL1
				+ ";" + NL2 + ")";
	}

}
