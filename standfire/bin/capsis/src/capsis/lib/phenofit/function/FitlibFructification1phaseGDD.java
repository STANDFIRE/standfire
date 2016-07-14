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
import capsis.lib.phenofit.function.util.GDD;
import capsis.lib.phenofit.function.util.Sigmoid;
import capsis.lib.phenofit.function.util.Wang;

/**
 * The Phenofit FitlibFructification1phaseGDD function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibFructification1phaseGDD extends FitlibFunction2Phases implements FitlibPhelibFunctions {

	private double Tb;
	private double Fcrit;
	private double sigma;
	private double pfe50;

	/**
	 * Constructor
	 */
	public FitlibFructification1phaseGDD (double Tb, double Fcrit, double sigma, double pfe50) {
		this.Tb = Tb;
		this.Fcrit = Fcrit;
		this.sigma = sigma;
		this.pfe50 = pfe50;
	}

	/**
	 * Constructor 2, parses a String, e.g. FitlibFructification1phaseGDD(14.7297;9.0;5.002;0.6)
	 */
	public FitlibFructification1phaseGDD (String str) throws Exception {
		try {
			String s = str.replace ("Fructification1phaseGDD(", "");
			s = s.replace (")", "");
			s = s.trim ();
			StringTokenizer st = new StringTokenizer (s, ";");
			Tb = Double.parseDouble (st.nextToken ().trim ());
			Fcrit = Double.parseDouble (st.nextToken ().trim ());
			sigma = Double.parseDouble (st.nextToken ().trim ());
			pfe50 = Double.parseDouble (st.nextToken ().trim ());
		} catch (Exception e) {
			throw new Exception ("FitlibFructification1phaseGDD: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "Fructification1phaseGDD";
	}

	public String getExpectedParams() {
		return "Tb,Fcrit,sigma,pfe50";
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
			
//			GDD f = new GDD(Tb);
//			double sum1 =0,maturationIndex=0;
//			// When date 2 is set, nothing more to do
//			if (pheno.isSetDate2 ())
//				return 0;
//			
//			int nbDays = FitlibClimate.getNbDays(year);
//			int sept1 = FitlibClimate.get1September(year);
//			int nbDays0 = FitlibClimate.getNbDays(year - 1);
//            // day is floweringDate 
//			if(year == pheno.birthYear && day == 0 || year == pheno.birthYear+1 && day == 0) day = Math.max(day,sept1);
//			
//			int prevPhase = phases.getPrevValue (year, day);
//			double prevState = states.getPrevValue (year, day);
//			int yesterday = FitlibClimate.getYesterday (year, day);
//
//			Fit4Phenology pf = pheno;
//
//		   if (prevPhase <= 3) {
//			   
//				// Is phase 3 over ? -> phase 4
//				if (prevPhase == 3 && prevState >= 1) {	
//					pheno.setDate1(year, yesterday);
//				}		
//				
//				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
//		    		sum1 = f.execute (dayClim.tmp) * 1d / (1 + Math.exp(-10 * (pf.leafFrostStates.getValue(day) - pfe50)))
//						       * loc.droughtIndex[day]+ prevState * Fcrit;
//					
//						phases.setValue(day, 3);
//						states.setValue(day, sum1 / Fcrit);
//					
//					
//					 if(sum1 >= Fcrit - 3 * sigma && sum1 <= Fcrit + 3 * sigma){
//						 pheno.setDate1(year, 0);
//					// Security
//					if (pheno.getApproxPhenoAge(year, day) > 300) {
//						pheno.setDate1(year, nbDays);
//						pheno.setDate2(year, nbDays);
//						pf.setMaturationIndex(FitlibConstants.ALMOST_ZERO);
//						return 0;
//					}
//					
//	                 sum1 = f.execute (dayClim.tmp) + prevState * Fcrit;
//				
//					
//					phases.setValue(day, 3);
//					states.setValue(day, sum1 / Fcrit);
//	
//					if (sum1 >= Fcrit) {
//						pheno.setDate2 (year, day);
//					}	
//				}	
//					
//		   }	
//			
//		   double x1 = Fcrit -3*sigma;
//	       int stop = (int) Math.round(sum1);
//			 for(int i = (int) Math.round(x1);i<=stop;i++)
//				 maturationIndex += Math.exp( -Math.pow(i - Fcrit, 2) / (2 * sigma * sigma)) / (2.5059 * sigma); 
//				 
//	
//			 pf.setMaturationIndex(maturationIndex);
//				return 0; // ok
			
			return -1;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFructification1phaseGDD.execute ()", "Error in FitlibFructification1phaseGDD", e);
			throw new Exception("Error in FitlibFructification1phaseGDD", e);
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
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;
			
			// fc+ic+jg-27.1.2016
			if (date0 >= nbDays) {
				pheno.setDate1(year, 0); // 1 phase model so there is no fruitGrowthInitDate
				pheno.setDate2(year, nbDays);
				return 0;
			}

			int day = date0;

			double sum1 = 0;
			GDD f = new GDD(Tb);
			
			do {
				day++;
				
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				
				sum1 += f.execute (dayClim.tmp) * 1d / (1 + Math.exp(-10 * (pf.leafFrostStates.getValue(day) - pfe50)))
				       * loc.droughtIndex[day];
			
				phases.setValue(day, 3);
				states.setValue(day, sum1 / Fcrit);

			} while (sum1 < Fcrit - 3 * sigma && day < nbDays);

			// Set the date 1
			pheno.setDate1(year, 0); // 1 phase model so there is no fruitGrowthInitDate
			
			if (day >= nbDays) {
				pf.setMaturationIndex(FitlibConstants.ALMOST_ZERO);
				pheno.setDate2 (year, nbDays);
				return 0;
			}
				
			double maturationIndex = 0;
			
			do {
				day++;
				
				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
				
				sum1 += f.execute (dayClim.tmp);
			
				
				phases.setValue(day, 3);
				states.setValue(day, sum1 / Fcrit);

				if (sum1 >= Fcrit) {
					pheno.setDate2 (year, day);
				}
			} while (sum1 < Fcrit + 3 * sigma && day < nbDays);

// 26/03/2015 checked results are the same
			double x1 = Fcrit -3*sigma;
            int stop = (int) Math.round(sum1);
			 for(int i = (int) Math.round(x1);i<=stop;i++)
				 maturationIndex += Math.exp( -Math.pow(i - Fcrit, 2) / (2 * sigma * sigma)) / (2.5059 * sigma); 
				 

			 pf.setMaturationIndex(maturationIndex);
			
			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFructification1phaseGDD.execute ()", "Error in FitlibFructification1phaseGDD", e);
			throw new Exception ("Error in FitlibFructification1phaseGDD", e);
		}
	}

	public String toString () {
		return "Fructification1phaseGDD(" + Tb + ";" + Fcrit + ";" + sigma + ";" + pfe50 + ")";
	}

}
