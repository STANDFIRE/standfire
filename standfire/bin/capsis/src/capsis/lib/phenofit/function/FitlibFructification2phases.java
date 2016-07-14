package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import phenofit5.model.Fit5Location;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibConstants;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibMemory;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.Sigmoid;
import capsis.lib.phenofit.function.util.Wang;

/**
 * The Phenofit FitlibFructification2phases function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibFructification2phases extends FitlibFunction2Phases implements FitlibPhelibFunctions {

	private double aa;
	private double bb;
	private double Fcrit;
	private double Top;
	private double matmoy;
	private double sigma;
	private double pfe50;
	private double photosType;

	/**
	 * Constructor
	 */
	public FitlibFructification2phases(double aa, double bb, double Fcrit, double Top, double matmoy, double sigma,
			double pfe50, double photosType) {
		this.aa = aa;
		this.bb = bb;
		this.Fcrit = Fcrit;
		this.Top = Top;
		this.matmoy = matmoy;
		this.sigma = sigma;
		this.pfe50 = pfe50;
		this.photosType = photosType;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * Fructification2phases(-0.2;14.7297;9.0;5.002;100.0;3.7;0.6;2.0)
	 */

	public FitlibFructification2phases(String str) throws Exception {
		try {
			String s = str.replace("Fructification2phases(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			aa = Double.parseDouble(st.nextToken().trim());
			bb = Double.parseDouble(st.nextToken().trim());
			Fcrit = Double.parseDouble(st.nextToken().trim());
			Top = Double.parseDouble(st.nextToken().trim());
			matmoy = Double.parseDouble(st.nextToken().trim());
			sigma = Double.parseDouble(st.nextToken().trim());
			pfe50 = Double.parseDouble(st.nextToken().trim());
			photosType = Double.parseDouble(st.nextToken().trim());
			
			if (photosType != 2 && photosType != 3)
				throw new Exception ("wrong value for photosType: "+photosType+", should be 2 or 3");
			
		} catch (Exception e) {
			throw new Exception("FitlibFructification2phases: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "Fructification2phases";
	}

	public String getExpectedParams() {
		return "aa,bb,Fcrit,Top,matmoy,sigma,pfe50,photosType";
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

			// Final maturationIndex value is known after date2 is set
//			if (pheno.isSetDate2 ())
//				return 0;

			int nbDays = FitlibClimate.getNbDays(year);
			int sept1 = FitlibClimate.get1September(year);
			int nbDays0 = FitlibClimate.getNbDays(year - 1);

			int prevPhase = memory.phase;
			double prevState3 = memory.intermediateState;
			double prevState4 = memory.state;

			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);

//			boolean date2IsSet = (pheno.getDate2() > 0);

			Fit5Location loc5 = (Fit5Location) loc;
			
			Wang wang = null;
			if (photosType == 2) {
				wang = new Wang(Top, -5, 40);
			} else if (photosType == 3) {
				wang = new Wang(Top, 0, 40);
			}

			Sigmoid sigmoid = new Sigmoid(aa, bb);

			if (prevPhase <= 3) {

				// Does phase 4 start ?
				if (prevPhase == 3 && prevState3 >= 1) {

					pheno.setDate1(year, yesterday);

					// Start phase 4
					memory.phase = 4;
					
//					FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
//					double sum2 = wang.execute(dayClim.tmp)
//							* (1d / (1 + Math.exp(-10 * (pheno.leafIndex - pfe50))))
//							* loc.droughtIndex[day] + prevState4 * matmoy;
//
//					memory.state = sum2 / matmoy;
					
				} else {

					// Process phase 3
					
//					// Security
//					if (pheno.getApproxPhenoAge(year, day) > 300) {
//						pheno.setDate1(year, nbDays);
//						pheno.setDate2(year, nbDays);
//						pheno.setMaturationIndex(FitlibConstants.ALMOST_ZERO);
//						return 0;
//					}

					memory.phase = 3;

					FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
					double sum1 = sigmoid.execute(dayClim.tmp) + prevState3 * Fcrit;
					memory.intermediateState = sum1 / Fcrit;

				}

			}

			if (prevPhase == 4) {

				// Process phase 4
				
				// Security: stop phase 4 at the end of 2nd year at max
				if (!pheno.isSetDate2 () && fit4Today >= nbDays) {
//				if (!pheno.isSetDate2 () && year == pheno.birthYear + 1 && day >= nbDays) {
					pheno.setDate2(year, nbDays);
					return 0;
				}

				double prevSum2 = prevState4 * matmoy;
				
				memory.phase = 4;
				
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				double sum2 = 0;
				if (prevSum2 < matmoy - 3 * sigma) {
					// fc+ic+jg-28.1.2016
					sum2 = wang.execute(dayClim.tmp)
							* (1d / (1 + Math.exp(-10 * (pheno.leafIndex - pfe50))))
							//* loc.droughtIndex[day] + prevSum2;
							* loc5.rew[day]+ prevSum2; // ic 13.4.2016 we use a water stress index calculated in the Water Budget in FitModel L 534
														
				} else {
					sum2 = wang.execute(dayClim.tmp) + prevSum2;
					
				}
				memory.state = sum2 / matmoy;

				// Set date2
				if (!pheno.isSetDate2 () && sum2 >= matmoy) {
//				if (!date2IsSet && sum2 >= matmoy) {
					
					pheno.setDate2(year, day); // IC 09/07/15
					
					// 24/03/2015 on calcule le dommage jusqu'à 8j avant senescence feuilles
					// pheno.setDate2(year, day - 8);

					// Date2 is set, BUT we do not reset the memory, we still need it 
					// to calculate maturationIndex (see below)
					//memory.reset ();

				}

				// Set maturationIndex
				if (sum2 >= matmoy - 3 * sigma && sum2 <= matmoy + 3 * sigma) { // calculate
																				// maturationIndex
//					double x1 = matmoy - 3 * sigma;
//					int stop = (int) Math.max(Math.round(sum2), Math.round(x1) + 1);
//					for (int i = (int) Math.round(x1); i <= stop; i++) {
//						// should we get the last maturityIndex
					
					double maturationIndex = pheno.getMaturationIndex()
							+ Math.exp(-Math.pow(sum2 - matmoy, 2) / (2 * sigma * sigma)) / (2.5059 * sigma);
					pheno.setMaturationIndex(maturationIndex);
					
//					}
						
				}

			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFructification2phases.execute ()", "Error in FitlibFructification2phases", e);
			throw new Exception("Error in FitlibFructification2phases", e);
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
				pheno.setDate2(year, nbDays);
				return 0;
			}

			int day = date0; // 06/03/2015 checked with IC //we deleted -1 in
								// order to begin at 115 and not at 114

			double sum1 = 0;
			Sigmoid f = new Sigmoid(aa, bb);

			do {
				day++;

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				sum1 = sum1 + f.execute(dayClim.tmp);

				phases.setValue(day, 3);
				states.setValue(day, sum1 / Fcrit);

			} while (sum1 < Fcrit && day < nbDays);
			// Set the date 1
			pheno.setDate1(year, day); // fruitGrowthInitDate

			if (day >= nbDays) {
				pf.setMaturationIndex(FitlibConstants.ALMOST_ZERO);
				pheno.setDate2(year, nbDays);
				return 0;
			}

			double sum2 = 0;
			Wang w = null;

			if (photosType == 2) {
				w = new Wang(Top, -5, 40);
			} else if (photosType == 3) {
				w = new Wang(Top, 0, 40);
			}
			do {
				day++;

				/*
					double sum2 = wang.execute(dayClim.tmp)
				 		* (1d / (1 + Math.exp(-10 * (pheno.leafIndex - pfe50))))
						* loc.droughtIndex[day] + prevState4 * matmoy;

					memory.state = sum2 / matmoy;
 				 
				 */
				
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				sum2 += w.execute(dayClim.tmp)
						* (1d / (1 + Math.exp(-10 * (pf.leafFrostStates.getValue(day) - pfe50))))
						* loc.droughtIndex[day]; // helps us to modify the Delphi's version
						

				phases.setValue(day, 4);
				states.setValue(day, sum2 / matmoy);

			} while (sum2 < matmoy - 3 * sigma && day < nbDays);

			if (day >= nbDays) {
				pf.setMaturationIndex(FitlibConstants.ALMOST_ZERO);
				pheno.setDate2(year, nbDays);
				return 0;
			}

			double maturationIndex = 0;

			do {
				day++;

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				sum2 += w.execute(dayClim.tmp); // 24/03/2015Modified with
												// Isabelle erase * 1d / (1 +
												// Math.exp(-10
												// *(loc.leafFrostStates.getValue(day)
												// - pfe50)));
				// Q2: 2.506628 ??? // sqrt (2*PI) = 2.5059

				/*
				 * 24/03/20115 comment the line bellow and write it as in delphi
				 * 
				 * maturationIndex += Math.exp( -Math.pow(sum2 - matmoy, 2) / (2
				 * * sigma * sigma)) / (2.5059 * sigma);
				 */

				phases.setValue(day, 4);
				states.setValue(day, sum2 / matmoy);

				// Set date2 // ic 17/03/16
				if (!pheno.isSetDate2 () && sum2 >= matmoy){
								pheno.setDate2(year, day);}
					
				//if (sum2 >= matmoy) {// (pheno.getDate2 () == nbDays && sum2 >= matmoy) 
				//				{ //24/03/2015 change condition pheno.getDate2==0 To pheno.getDate2==nbDays
					
				//	pheno.setDate2(year, day - 8);// 24/03/2015 on calcule le dommage jusqu'a 8j avant senescence feuilles
				//}

			} while (sum2 < matmoy + 3 * sigma && day < nbDays);

			// 24/03/2015 comment the line bellow and write it as in delphi
			double x1 = matmoy - 3 * sigma;
			int stop = (int) Math.max(Math.round(sum2), Math.round(x1) + 1);
			for (int i = (int) Math.round(x1); i <= stop; i++)
				maturationIndex += Math.exp(-Math.pow(i - matmoy, 2) / (2 * sigma * sigma)) / (2.5059 * sigma);

			// ///////////////////////////////////////////////////////////////
			pf.setMaturationIndex(maturationIndex);

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFructification2phases.execute ()", "Error in FitlibFructification2phases", e);
			throw new Exception("Error in FitlibFructification2phases", e);
		}
	}

	public String toString() {
		return "Fructification2phases(" + aa + ";" + bb + ";" + Fcrit + ";" + Top + ";" + matmoy + ";" + sigma + ";"
				+ pfe50 + ";" + photosType + ")";
	}

}
