package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * The Phenofit LimitPrecipitationAnnual function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibLimitPrecipitationAnnual extends FitlibStandardFunction {

	private double pp1;
	private double pp2;

	/**
	 * Constructor
	 */
	public FitlibLimitPrecipitationAnnual(double pp1, double pp2) {
		this.pp1 = pp1;
		this.pp2 = pp2;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibLimitPrecipitationAnnual(String str) throws Exception {
		try {
			String s = str.replace("LimitPrecipitationAnnual(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			pp1 = Double.parseDouble(st.nextToken().trim());
			pp2 = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibLimitPrecipitationAnnual: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "LimitPrecipitationAnnual";
	}

	public String getExpectedParams() {
		return "pp1,pp2";
	}

	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, FitlibFitness fitness, int year) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			double sumPre = 0;

			for (int d = 1; d <= nbDays; d++) {

				FitlibClimateDay dayClim = locClim.getClimateDay(year, d);
				sumPre += dayClim.pre;

			}

			if (sumPre < pp1 || sumPre > pp2) {
				fitness.setDroughtSurvival(0.1);
			}else { //checked with isabelle
				fitness.setDroughtSurvival(1);
			}
			

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLimitPrecipitationAnnual.execute ()", "Error in FitlibLimitPrecipitationAnnual", e);
			throw new Exception("Error in FitlibLimitPrecipitationAnnual", e);
		}

	}

	public String toString() {
		return "LimitPrecipitationAnnual(" + pp1 + ";" + pp2 + ")";
	}

}
