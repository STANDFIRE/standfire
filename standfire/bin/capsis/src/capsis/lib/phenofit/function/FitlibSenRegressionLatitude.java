package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;

/**
 * The Phenofit FitlibSenRegressionLatitude function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibSenRegressionLatitude extends FitlibFunction1Phase {

	private double DateNorth;
	private double DateSouth;
	private double LatNorth;
	private double LatSouth;

	/**
	 * Constructor
	 */
	public FitlibSenRegressionLatitude(double DateNorth, double DateSouth, double LatNorth, double LatSouth) {
		this.DateNorth = DateNorth;
		this.DateSouth = DateSouth;
		this.LatNorth = LatNorth;
		this.LatSouth = LatSouth;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 * SenRegressionLatitude(13.38;29.95;1.0;2.0)
	 */
	public FitlibSenRegressionLatitude(String str) throws Exception {
		try {
			String s = str.replace("SenRegressionLatitude(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			DateNorth = Double.parseDouble(st.nextToken().trim());
			DateSouth = Double.parseDouble(st.nextToken().trim());
			LatNorth = Double.parseDouble(st.nextToken().trim());
			LatSouth = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibSenRegressionLatitude: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "SenRegressionLatitude";
	}

	public String getExpectedParams() {
		return "DateNorth,DateSouth,LatNorth,LatSouth";
	}

	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {
			
			Fit4Phenology pf = pheno;

			double day = Math.round((loc.getLatitude() - LatSouth) * ((DateSouth - DateNorth) / (LatSouth - LatNorth))
					+ DateSouth);

			phases.setValue((int) day, 3);
			states.setValue((int) day, 0); // what's gonna be the value of the
											// state in this case

			pheno.setDate1(year, day);

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibSenRegressionLatitude.execute ()", "Error in FitlibSenRegressionLatitude", e);
			throw new Exception("Error in FitlibSenRegressionLatitude", e);
		}
	}

	public String toString() {
		return "SenRegressionLatitude(" + DateNorth + ";" + DateSouth + ";" + LatNorth + ";" + LatSouth + ")";
	}

}
