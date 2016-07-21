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
 * The Phenofit LimitETP function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibLimitETP extends FitlibStandardFunction {

	private double ETpomax;
	private double ETpomin;
	private double Immin;
	private double Immax;

	/**
	 * Constructor
	 */
	public FitlibLimitETP(double ETpomax, double ETpomin, double Immin, double Immax) {
		this.ETpomax = ETpomax;
		this.ETpomin = ETpomin;
		this.Immin = Immin;
		this.Immax = Immax;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibLimitETP(String str) throws Exception {
		try {
			String s = str.replace("LimitETP(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			ETpomax = Double.parseDouble(st.nextToken().trim());
			ETpomin = Double.parseDouble(st.nextToken().trim());
			Immin = Double.parseDouble(st.nextToken().trim());
			Immax = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibLimitETP: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "LimitETP";
	}

	public String getExpectedParams() {
		return "ETpomax,ETpomin,Immin,Immax";
	}

	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, FitlibFitness fitness, int year) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			double sumETP = 0;
			double sumPre = 0;
			double Imannual = 0;

			for (int d = 1; d <= nbDays; d++) {

				sumETP = sumETP + loc.pet[d];
				FitlibClimateDay dayClim = locClim.getClimateDay(year, d);
				sumPre += dayClim.pre;

				if (sumETP != 0)
					Imannual = 100 * (sumPre - sumETP) / sumETP;

			}

			if ((sumETP > ETpomax || sumETP < ETpomin) && (Imannual < Immin || Imannual > Immax)) {
				fitness.setDroughtSurvival(0.1);
			}else { //checked with isabelle
				fitness.setDroughtSurvival(1);
			}

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLimitETP.execute ()", "Error in FitlibLimitETP", e);
			throw new Exception("Error in FitlibLimitETP", e);
		}

	}

	public String toString() {
		return "LimitETP(" + ETpomax + ";" + ETpomin + ";" + Immin + ";" + Immax + ")";
	}

}
