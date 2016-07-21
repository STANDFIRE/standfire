package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * The Phenofit LimitETA function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibLimitETA extends FitlibStandardFunction {

	private double ETacmax;
	private double ETacmin;
	private double deficitmax;

	/**
	 * Constructor
	 */
	public FitlibLimitETA(double ETacmax, double ETacmin, double deficitmax) {
		this.ETacmax = ETacmax;
		this.ETacmin = ETacmin;
		this.deficitmax = deficitmax;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibLimitETA(String str) throws Exception {
		try {
			String s = str.replace("LimitETA(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			ETacmax = Double.parseDouble(st.nextToken().trim());
			ETacmin = Double.parseDouble(st.nextToken().trim());
			deficitmax = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibLimitETA: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "LimitETA";
	}

	public String getExpectedParams() {
		return "ETacmax,ETacmin,deficitmax";
	}

	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, FitlibFitness fitness, int year) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			double sumETA = 0;
			//double sumDeficit = 0; // not modified then used ????

			for (int d = 1; d <= nbDays; d++) {

				sumETA = sumETA + loc.aet[d];

			}

			if ((sumETA > ETacmax || sumETA < ETacmin)) {    // && (sumDeficit > deficitmax)) {   we don't use it in this version
				fitness.setDroughtSurvival(0.1);
			}else { //checked with isabelle
				fitness.setDroughtSurvival(1);
			}

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLimitETA.execute ()", "Error in FitlibLimitETA", e);
			throw new Exception("Error in FitlibLimitETA", e);
		}

	}

	public String toString() {
		return "LimitETA(" + ETacmax + ";" + ETacmin + ";" + deficitmax + ")";
	}

}
