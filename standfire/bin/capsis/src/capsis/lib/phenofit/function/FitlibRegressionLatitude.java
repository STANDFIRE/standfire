package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibConstants;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;

/**
 * The Phenofit RegressionLatitude function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibRegressionLatitude extends FitlibFunction2Phases {

	private double DateNorth;
	private double DateSouth;
	private double LatNorth;
	private double LatSouth;

	/**
	 * Constructor
	 */
	public FitlibRegressionLatitude(double LatSouth, double LatNorth, double DateSouth, double DateNorth) {
		this.DateNorth = DateNorth;
		this.DateSouth = DateSouth;
		this.LatNorth = LatNorth;
		this.LatSouth = LatSouth;
	}

	/**
	 * Constructor 2, parses a String, e.g. RegressionLatitude(7.2;14;a;b)
	 */
	public FitlibRegressionLatitude(String str) throws Exception {
		try {
			String s = str.replace("RegressionLatitude(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			LatSouth = Double.parseDouble(st.nextToken().trim());
			LatNorth = Double.parseDouble(st.nextToken().trim());
			DateSouth = Double.parseDouble(st.nextToken().trim());
			DateNorth = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibRegressionLatitude: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "RegressionLatitude";
	}

	public String getExpectedParams() {
		return "LatSouth,LatNorth,DateSouth,DateNorth";
	}

	/**
	 * Computes 2 phenology dates and updates the given phases and states for
	 * all processed days. Returns 0 if ok.
	 */
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			double Fcrit_unified = FitlibConstants.ALMOST_ZERO;

			pheno.setDate1(year, FitlibConstants.ALMOST_ZERO);

			pheno.setDate2(year, Math.round((loc.getLatitude() - LatSouth) * ((DateSouth - DateNorth) / (LatSouth - LatNorth))
					+ DateSouth));

			// phases and states ???

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibRegressionLatitude.execute ()", "Error in FitlibRegressionLatitude", e);
			throw new Exception("Error in FitlibRegressionLatitude", e);
		}
	}

	public String toString() {
		return "RegressionLatitude(" + LatSouth + ";" + LatNorth + ";" + DateSouth + ";" + DateNorth + ")";
	}

}
