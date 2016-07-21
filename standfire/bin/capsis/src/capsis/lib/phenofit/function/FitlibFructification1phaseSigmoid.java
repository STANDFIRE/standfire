package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibConstants;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.Sigmoid;

/**
 * The Phenofit FitlibFructification1phaseSigmoid function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibFructification1phaseSigmoid extends FitlibFunction2Phases {

	private double aa;
	private double bb;
	private double Fcrit;
	private double sigma;
	private double pfe50;

	/**
	 * Constructor
	 */
	public FitlibFructification1phaseSigmoid(double aa, double bb, double Fcrit, double sigma, double pfe50) {
		this.aa = aa;
		this.bb = bb;
		this.Fcrit = Fcrit;
		this.sigma = sigma;
		this.pfe50 = pfe50;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * Fructification1phaseSigmoid(-0.2;14.7297;9.0;5.002;0.6)
	 */
	public FitlibFructification1phaseSigmoid(String str) throws Exception {
		try {
			String s = str.replace("Fructification1phaseSigmoid(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			aa = Double.parseDouble(st.nextToken().trim());
			bb = Double.parseDouble(st.nextToken().trim());
			Fcrit = Double.parseDouble(st.nextToken().trim());
			sigma = Double.parseDouble(st.nextToken().trim());
			pfe50 = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibFructification1phaseSigmoid: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "Fructification1phaseSigmoid";
	}

	public String getExpectedParams() {
		return "aa,bb,Fcrit,sigma,pfe50";
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
			
			// fc+ic+jg-27.1.2016
			if (date0 >= nbDays) {
				pheno.setDate1(year, 0); // 1 phase model so there is no fruitGrowthInitDate
				pheno.setDate2(year, nbDays);
				return 0;
			}

			int day = date0;

			double sum1 = 0;
			Sigmoid f = new Sigmoid(aa, bb);

			do {
				day++;

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				sum1 += f.execute(dayClim.tmp) * 1d / (1 + Math.exp(-10 * (pf.leafFrostStates.getValue(day) - pfe50)))
						* loc.droughtIndex[day];

				phases.setValue(day, 3);
				states.setValue(day, sum1 / Fcrit);

			} while (sum1 < Fcrit - 3 * sigma && day < nbDays);

			// Set the date 1
			pheno.setDate1(year, 0); // 1 phase model so there is no fruitGrowthInitDate

			if (day >= nbDays) {
				pf.setMaturationIndex(FitlibConstants.ALMOST_ZERO);
				pheno.setDate2(year, nbDays);
				return 0;
			}

			double maturationIndex = 0;

			do {
				day++;

				FitlibClimateDay dayClim = locClim.getClimateDay(year, day);

				sum1 += f.execute(dayClim.tmp);


				phases.setValue(day, 3);
				states.setValue(day, sum1 / Fcrit);

				if (sum1 >= Fcrit) {
					pheno.setDate2(year, day);
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
			Log.println(Log.ERROR, "FitlibFructification1phaseSigmoid.execute ()",
					"Error in FitlibFructification1phaseSigmoid", e);
			throw new Exception("Error in FitlibFructification1phaseSigmoid", e);
		}
	}

	public String toString() {
		return "Fructification1phaseSigmoid(" + aa + ";" + bb + ";" + Fcrit + ";" + sigma + ";" + pfe50 + ")";
	}

}
