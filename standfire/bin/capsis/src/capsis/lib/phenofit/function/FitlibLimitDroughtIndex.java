package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * The Phenofit LimitDroughtIndex function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibLimitDroughtIndex extends FitlibStandardFunction {

	private double MildDrought;
	private double MildLength;
	private double ModerateDrought;
	private double ModerateLength;
	private double SevereDrought;
	private double SevereLength;
	private double ExtremeDrought;
	private double ExtremeLength;

	/**
	 * Constructor
	 */
	public FitlibLimitDroughtIndex(double MildDrought, double MildLength, double ModerateDrought, double ModerateLength,
			double SevereDrought, double SevereLength, double ExtremeDrought, double ExtremeLength) {
		this.MildDrought = MildDrought;
		this.MildLength = MildLength;
		this.ModerateDrought = ModerateDrought;
		this.ModerateLength = ModerateLength;
		this.SevereDrought = SevereDrought;
		this.SevereLength = SevereLength;
		this.ExtremeDrought = ExtremeDrought;
		this.ExtremeLength = ExtremeLength;
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibLimitDroughtIndex(String str) throws Exception {
		try {
			String s = str.replace("LimitDroughtIndex(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			MildDrought = Double.parseDouble(st.nextToken().trim());
			MildLength = Double.parseDouble(st.nextToken().trim());
			ModerateDrought = Double.parseDouble(st.nextToken().trim());
			ModerateLength = Double.parseDouble(st.nextToken().trim());
			SevereDrought = Double.parseDouble(st.nextToken().trim());
			SevereLength = Double.parseDouble(st.nextToken().trim());
			ExtremeDrought = Double.parseDouble(st.nextToken().trim());
			ExtremeLength = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibLimitDroughtIndex: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "LimitDroughtIndex";
	}

	public String getExpectedParams() {
		return "MildDrought,MildLength,ModerateDrought,ModerateLength,SevereDrought,SevereLength,ExtremeDrought,ExtremeLength";
	}

	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, FitlibFitness fitness, int year) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			double s4 = 0;
			double s4Max = 0; // not modified then used ????
			double[] Ia = null;
			for (int d = 1; d <= nbDays; d++) {

				Ia[d] = loc.aet[d] / loc.pet[d]; // reference to pheno-Delphi
													// evapotranspiration class
				if (Ia[d] < loc.droughtIndex[d])
					s4++; // classe[4].drought = loc.droughtIndex[d] 
				else
					s4 = 0;
				s4Max = Math.max(s4, s4Max);

			}

			if (s4Max > loc.droughtIndex.length) { // S4Max>classe[4].length
				fitness.setDroughtSurvival(0.1);
			}else { //checked with isabelle
				fitness.setDroughtSurvival(1);
			}

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibLimitDroughtIndex.execute ()", "Error in FitlibLimitDroughtIndex", e);
			throw new Exception("Error in FitlibLimitDroughtIndex", e);
		}

	}

	public String toString() {
		return "LimitDroughtIndex(" + MildDrought + ";" + MildLength + ";" + ModerateDrought + ";" + ModerateLength
				+ ";" + SevereDrought + ";" + SevereLength + ";" + ExtremeDrought + ";" + ExtremeLength + ")";
	}

}
