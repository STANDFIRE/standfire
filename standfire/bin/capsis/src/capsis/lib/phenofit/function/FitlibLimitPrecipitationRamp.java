package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibConstants;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.function.util.Chuine;
import capsis.lib.phenofit.function.util.Sigmoid;

/**
 * The Phenofit LimitPrecipitationRamp function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibLimitPrecipitationRamp extends FitlibStandardFunction /* implements FitlibPhelibFunctions */ { // FitlibFunction1Phase
																		// ?

	private double ppextremelow;
	private double pplow;
	private double pphigh;
	private double ppextremehigh;

	/**
	 * Constructor
	 */
	public FitlibLimitPrecipitationRamp(double ppextremelow, double pplow, double pphigh, double ppextremehight) {
		this.ppextremelow = ppextremelow;
		this.pplow = pplow;
		this.pphigh = pphigh;
		this.ppextremehigh = ppextremehight;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibLimitPrecipitationRamp(String str) throws Exception {
		try {
			String s = str.replace("LimitPrecipitationRamp(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			ppextremelow = Double.parseDouble(st.nextToken().trim());
			pplow = Double.parseDouble(st.nextToken().trim());
			pphigh = Double.parseDouble(st.nextToken().trim());
			ppextremehigh = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibLimitPrecipitationRamp: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "LimitPrecipitationRamp";
	}

	public String getExpectedParams() {
		return "ppextremelow,pplow,pphigh,ppextremehigh";
	}
	
//	//ym 5/6/2015
//	public double executeDaily (FitlibLocation loc, FitlibLocationClimate locClim, int year, int day, Fit4Phenology pheno,FitlibPhases phases, FitlibStates states) throws Exception{
//		
//		try {
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
//
//			Fit4Phenology pf = pheno;
//			double sumPre = 0;
//
//			FitlibClimateDay dayClim = locClim.getClimateDay(year, day);
//			sumPre += dayClim.pre;
//
//			if (sumPre <= ppextremelow || sumPre >= ppextremehigh) {
//				fitness.setDroughtSurvival(0.1);
//			} else if (sumPre > ppextremelow && sumPre <= pplow) {
//				double res = ((0.9 * sumPre) / (pplow - ppextremelow)) + ((ppextremelow - 0.1 * pplow)
//						/ (ppextremelow - pplow));
//				fitness.setDroughtSurvival(res);
//			} else if (sumPre >= pphigh && sumPre < ppextremehigh) {
//				double res = ((0.9 * sumPre) / (pphigh - ppextremehigh)) + ((ppextremehigh - 0.1 * pphigh)
//						/ (ppextremehigh - pphigh));
//				fitness.setDroughtSurvival(res);
//			} else { // sumPre > pplow && sumPre < pphigh
//				fitness.setDroughtSurvival(1);
//			}				
//			return 0; // ok
//
//		} catch (Exception e) {
//			Log.println(Log.ERROR, "FitlibUniChill.execute ()", "Error in FitlibUniChill", e);
//			throw new Exception("Error in FitlibUniChill", e);
//		}
//	
//	}
//	////////////////////////////

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

			if (sumPre <= ppextremelow || sumPre >= ppextremehigh) {
				fitness.setDroughtSurvival(0.1);
			} else if (sumPre > ppextremelow && sumPre <= pplow) {
				double res = ((0.9 * sumPre) / (pplow - ppextremelow)) + ((ppextremelow - 0.1 * pplow)
						/ (ppextremelow - pplow));
				fitness.setDroughtSurvival(res);
			} else if (sumPre >= pphigh && sumPre < ppextremehigh) {
				double res = ((0.9 * sumPre) / (pphigh - ppextremehigh)) + ((ppextremehigh - 0.1 * pphigh)
						/ (ppextremehigh - pphigh));
				fitness.setDroughtSurvival(res);
			} else { // sumPre > pplow && sumPre < pphigh
				fitness.setDroughtSurvival(1);
			}

			return 0;
			
		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLimitPrecipitationRamp.execute ()", "Error in FitlibLimitPrecipitationRamp", e);
			throw new Exception("Error in FitlibLimitPrecipitationRamp", e);
		}

	}

	public String toString() {
		return "LimitPrecipitationRamp(" + ppextremelow + ";" + pplow + ";" + pphigh + ";" + ppextremehigh + ")";
	}

}
