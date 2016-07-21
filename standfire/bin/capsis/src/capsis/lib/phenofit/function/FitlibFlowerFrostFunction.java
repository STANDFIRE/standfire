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
 * The Phenofit Frost function for flowers.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibFlowerFrostFunction extends FitlibStatesFunction implements FitlibPhelibFunctions {

	private double FHfrmax1;
	private double FHfrmax2;
	// private double FHminfe;
	private double FHminfl;
	private double Te1;
	private double Te2;
	// private double FHtfemax;
	private double FHtflmax;
	// private double FHpfemax;
	private double FHpflmax;
	private double NL1;
	private double NL2;

	/**
	 * Constructor
	 */
	public FitlibFlowerFrostFunction(double FHfrmax1, double FHfrmax2, double FHminfl, double Te1, double Te2,
			double FHtflmax, double FHpflmax, double NL1, double NL2) {
		this.FHfrmax1 = FHfrmax1;
		this.FHfrmax2 = FHfrmax2;
		this.FHminfl = FHminfl;
		this.Te1 = Te1;
		this.Te2 = Te2;
		this.FHtflmax = FHtflmax;
		this.FHpflmax = FHpflmax;
		this.NL1 = NL1;
		this.NL2 = NL2;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * Frost(-6.0;-20.0;-5.0;-5.0;10.0;-16.0;-8.0;-8.0;-12.0;-12.0;10.0;16.0)
	 */
	public FitlibFlowerFrostFunction(String str) throws Exception {
		try {
			String s = str.replace("Frost(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			FHfrmax1 = Double.parseDouble(st.nextToken().trim());
			FHfrmax2 = Double.parseDouble(st.nextToken().trim());
			double FHminfe = Double.parseDouble(st.nextToken().trim());
			FHminfl = Double.parseDouble(st.nextToken().trim());
			Te1 = Double.parseDouble(st.nextToken().trim());
			Te2 = Double.parseDouble(st.nextToken().trim());
			double FHtfemax = Double.parseDouble(st.nextToken().trim());
			FHtflmax = Double.parseDouble(st.nextToken().trim());
			double FHpfemax = Double.parseDouble(st.nextToken().trim());
			FHpflmax = Double.parseDouble(st.nextToken().trim());
			NL1 = Double.parseDouble(st.nextToken().trim());
			NL2 = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibFlowerFrostFunction: could not parse this function: " + str, e);
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

			if (pheno.getFloweringDate() >= nbDays) {
				pheno.setFruitIndex(FitlibConstants.ALMOST_ZERO);
				return 0; // ok
			}

			double NL = 0; // night length, hour fractions
			// double FD = 0; // frost damage, unitless, [0,1]
			// double FH = FHminfl; // frost hardiness, celsius degrees
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
				prevFrostHardiness = FHminfl; // init

			// int flowerDevelopmentPhase = phases.getValue(day);
			// double flowerDevelopmentState = states.getValue(day);

			// Fruit die at fruit maturation date
			if (pheno.isSetFruitMaturationDate () && fit4Today > pheno.getFruitMaturationDate()) {
//			if (pheno.getFruitMaturationDate() != 0 && day > pheno.getFruitMaturationDate()) {
				pheno.setFruitIndex(1 - prevFrostDamage); //
				// From now on, the function should exit directly at its
				// beginning for the next days
				return 0;
			}

			// ic-5.2.2015 Math.abs SHOULD BE REMOVED in the next version of
			// Phenofit
			NL = 24 - Math.abs(new DayLength().execute(loc.getLatitude(), day));

			if (developmentPhase == 1) {
				CR = 1;
			} else if (developmentPhase == 2) {
				CR = Math.max(0, 1 - developmentState);
			} else {
				CR = 1;// Math.max(0, 1 - flowerDevelopmentState); 11/03/2015
						// Modificatin Yassine MOTIE 1;
			}

			FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

			if (dayClim.tmn > Te1) {
				dFHti = 0;
			} else if (dayClim.tmn < Te2) {
				dFHti = FHtflmax;
			} else {
				dFHti = FitlibLeafFrostFunction.dFHt(dayClim.tmn, Te1, Te2, FHtflmax);
			}

			if (CR == 0) {
				dFHpi = 0;
			} else if (fit4Today > FitlibConstants.WINTER_SOLSTICE && fit4Today < FitlibConstants.SUMMER_SOLSTICE) {
//			} else if (day > FitlibConstants.WINTER_SOLSTICE && day < FitlibConstants.SUMMER_SOLSTICE) {
				dFHpi = FHpflmax;
			} else if (NL > NL2) {
				dFHpi = FHpflmax;
			} else if (NL < NL1) {
				dFHpi = 0;
			} else {
				dFHpi = FitlibLeafFrostFunction.dFHp(NL, NL1, NL2, FHpflmax);
			}

			double frostHardiness = 0;
			if (developmentPhase == 1 || developmentPhase == 2) {
				frostHardiness = (4d / 5d) * prevFrostHardiness + (1d / 5d) * (FHminfl + CR * (dFHti + dFHpi));
			} else if (developmentPhase == 3) {
				frostHardiness = FHminfl + (FHfrmax1 - FHminfl) * developmentStateIntermediate;
			} else {
				// 11/03/2015 developmentState - 0.5: because (flowerDevelopmentState = GDD/matmoy)
				frostHardiness = FHfrmax1 + (FHfrmax2 - FHfrmax1)
						/ (1 + Math.exp(-10 * (developmentState - 0.5)));  // fc+ic+jg-28.1.2016 removed matmoy, change to -10
			}

			BF = -0.3 - 1.5 * Math.exp(0.1 * frostHardiness);
			double frostDamage = Math.min(1d, prevFrostDamage + 1d / (1d + Math.exp(BF * (frostHardiness - dayClim.tmn))));
			
			memory.frostDamage = frostDamage;
			memory.frostHardiness = frostHardiness;
			
			
			if (frostDamage < 1) {
				//pheno.setFruitIndex(frostDamage); // ic 16/03/16 error! FruitIndex = FD and not 1-FD! 
				pheno.setFruitIndex(1-frostDamage);
			} else {
				pheno.setFruitIndex(FitlibConstants.ALMOST_ZERO);
			}

			// more than 1 year maturation
//			pf.setFruitIndex(pf.flowerFrostStates.getValue(day));
			
			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFlowerFrostFunction.execute ()", "Error in FitlibFlowerFrostFunction", e);
			throw new Exception("Error in FitlibFlowerFrostFunction", e);
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

			// this method writes in loc.flowerFrostStates

			if (pf.getFloweringDate() >= nbDays) {
				pf.setFruitIndex(FitlibConstants.ALMOST_ZERO);
				// pf.flowerFrostStates keeps zero values for all days
				return 0; // ok
			}

			double NL = 0; // night length, hour fractions
			double FD = 0; // frost damage, unitless, [0,1]
			double FH = FHminfl; // frost hardiness, celsius degrees
			double dFHti = 0; // temperature component of frost hardiness,
								// celsius degrees
			double dFHpi = 0; // photoperiod component of frost hardiness,
								// celsius degrees
			double BF = 0; //
			double CR = 0; // hardening competence

			int day = date0;

			do {
				day++;

				int flowerDevelopmentPhase = phases.getValue(day);
				double flowerDevelopmentState = states.getValue(day);

				// Fruit die at fruit maturation date
				//if (flowerDevelopmentPhase == 0) { // ic 08/02/16
					if ( day > (int)Math.round(pf.floweringDate) && (flowerDevelopmentPhase == 0)) {	
				pf.setFruitIndex(pf.flowerFrostStates.getValue(day - 1));
					return 0;
				}

				// ic-5.2.2015 Math.abs SHOULD BE REMOVED in the next version of
				// Phenofit
				NL = 24 - Math.abs(new DayLength().execute(loc.getLatitude(), day));

				if (flowerDevelopmentPhase == 1) {
					CR = 1;
				} else if (flowerDevelopmentPhase == 2) {
					CR = Math.max(0, 1 - flowerDevelopmentState);
				} else {
					CR = 1;// Math.max(0, 1 - flowerDevelopmentState);
							// 11/03/2015 Modificatin Yassine MOTIE 1;
				}

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				if (dayClim.tmn > Te1) {
					dFHti = 0;
				} else if (dayClim.tmn < Te2) {
					dFHti = FHtflmax;
				} else {
					dFHti = FitlibLeafFrostFunction.dFHt(dayClim.tmn, Te1, Te2, FHtflmax);
				}

				if (CR == 0) {
					dFHpi = 0;
				} else if (day > FitlibConstants.WINTER_SOLSTICE && day < FitlibConstants.SUMMER_SOLSTICE) {
					dFHpi = FHpflmax;
				} else if (NL > NL2) {
					dFHpi = FHpflmax;
				} else if (NL < NL1) {
					dFHpi = 0;
				} else {
					dFHpi = FitlibLeafFrostFunction.dFHp(NL, NL1, NL2, FHpflmax);
				}

				if (flowerDevelopmentPhase == 1 || flowerDevelopmentPhase == 2) {
					FH = (4d / 5d) * FH + (1d / 5d) * (FHminfl + CR * (dFHti + dFHpi));
				} else if (flowerDevelopmentPhase == 3) {
					FH = FHminfl + (FHfrmax1 - FHminfl) * flowerDevelopmentState;
				} else {
					FH = FHfrmax1
							+ (FHfrmax2 - FHfrmax1)
							/ (1 + Math
									.exp(-10 * (flowerDevelopmentState - 0.5))); // fc+ic+jg-28.1.2016 removed matmoy, change to -10
				}

				BF = -0.3 - 1.5 * Math.exp(0.1 * FH);
				FD = Math.min(1d, FD + 1d / (1d + Math.exp(BF * (FH - dayClim.tmn))));
				if (FD < 1) {
					pf.flowerFrostStates.setValue(day, 1 - FD);
				} else {
					pf.flowerFrostStates.setValue(day, FitlibConstants.ALMOST_ZERO);
				}

			} while (day < nbDays);

			// } while (day < nbDays && FD < 1);

			// if (loc.getFruitMaturationDate() == nbDays) { // 02/03/2015
			// conditions error ; valable for maturationIndex but not for
			// FruitIndex (checked with Isabelle)
			// loc.setFruitIndex(FitlibInitialParameters.ALMOST_ZERO);
			// } else {
			// more than 1 year maturation
			pf.setFruitIndex(pf.flowerFrostStates.getValue(day));
			// }

			return 0; // ok
		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFlowerFrostFunction.execute ()", "Error in FitlibFlowerFrostFunction", e);
			throw new Exception("Error in FitlibFlowerFrostFunction", e);
		}

	}

	public String toString() {
		return "FlowerFrostFunction(" + FHfrmax1 + ";" + FHfrmax2 + ";" + FHminfl + ";" + Te1 + ";" + Te2 + ";"
				+ FHtflmax + ";" + FHpflmax + ";" + NL1 + ";" + NL2 + ")";
	}

}
