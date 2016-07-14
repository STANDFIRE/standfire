package capsis.lib.phenofit.function;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * The Phenofit PriestleyTaylor function.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibPriestleyTaylor extends FitlibStandardFunction {

	/**
	 * Constructor
	 */
	public FitlibPriestleyTaylor() {
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibPriestleyTaylor(String str) throws Exception {
		try {
			if (!str.startsWith("PriestleyTaylor("))
				throw new Exception();

			// PriestleyTaylor has no parameters

			// String s = str.replace ("PriestleyTaylor(", "");
			// s = s.replace (")", "");
			// s = s.trim ();
			// StringTokenizer st = new StringTokenizer (s, ";");
			// a = Double.parseDouble (st.nextToken ().trim ());
			// b = Double.parseDouble (st.nextToken ().trim ());

		} catch (Exception e) {
			throw new Exception("FitlibPriestleyTaylor: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "PriestleyTaylor";
	}

	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, FitlibFitness fitness, int year) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			// Index 1 is 1st January, index 0 is unused
			loc.pet = new double[nbDays + 1];

			for (int d = 1; d <= nbDays; d++) {

				FitlibClimateDay dayClim = locClim.getClimateDay(year, d);

				double Emax = 0.6108 * Math.exp((17.27 * dayClim.tmx) / (237.3 + dayClim.tmx));
				double Emin = 0.6108 * Math.exp((17.27 * dayClim.tmn) / (237.3 + dayClim.tmn));
				double Es = (Emax + Emin) / 2; // saturated vapor pressure, kPa
				double Ea = (dayClim.rh / 100) * Es; // vapor pressure, kPa

				double delta = 4098 * Es / Math.pow((dayClim.tmp + 237.3),2); // kPa
																			// /
																			// degree
																			// celsius
				//double P = 101.3 * Math.pow(((dayClim.tmp + 273.16) - (0.0065 * loc.getAltitude()))
				//		/ (dayClim.tmp + 273.16), 5.256);                                          Modification Yassine MOTIE 10/03/2015
				double P = 101.3 * Math.pow((((dayClim.tmp+273.16) - (0.0065*loc.getAltitude()))/(dayClim.tmp+273.16)),5.256) ;
				
				double gamma = 1615 * P / 2450000;
				double alpha = 0.8;

				// Calculation of Ra, extraterrestrial radiation
				double Phi = Math.PI / 180 * loc.getLatitude(); // radians
				double Dr = 1 + 0.033 * Math.cos(((2 * Math.PI) / 365) * d);
				double Decli = 0.409 * Math.sin(((2 * Math.PI) / 365) * d - 1.39); // solar
																					// declination,
																					// radians
				double AngSol = Math.acos(-Math.tan(Phi) * Math.tan(Decli)); // solar
																				// angle,
																				// radians

				double Ra1 = 24 * 60 / Math.PI * 0.0820 * Dr;
				double Ra2 = AngSol * Math.sin(Phi) * Math.sin(Decli) + Math.cos(Phi) * Math.cos(Decli)
						* Math.sin(AngSol);
				double Ra = Ra1 * Ra2;

				double Rso = (0.75 + (2 / Math.pow(10, 5)) * loc.getAltitude()) * Ra;
				double Rns = (1 - 0.23) * dayClim.glo; // net incoming solar
														// radiation

				double RsSURRso = dayClim.glo / Rso;
				if (RsSURRso > 1) {
					RsSURRso = 1;
				}

				double steph = 4.903 / Math.pow(10, 9);

				//double Rnl1 = steph * (Math.pow(dayClim.tmx + 273.16, 4) + Math.pow(dayClim.tmn + 273.16, 4)) / 2;                09/03/2015 Yassine MOTIE Modification
				double Rnl1 = steph*(((Math.pow((dayClim.tmx+273.16),4))+(Math.pow((dayClim.tmn+273.16),4))/2));
				double Rnl2 = 0.34 - 0.14 * Math.sqrt(Ea);
				double Rnl3 = (1.35 * RsSURRso) - 0.35;
				double Rnl = Rnl1 * Rnl2 * Rnl3;

				double Rn = Math.max(0, Rns - Rnl);

				double pet = alpha * Rn * (delta / (delta + gamma));
				loc.pet[d] = pet;

			}

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibPriestleyTaylor.execute ()", "Error in FitlibPriestleyTaylor", e);
			throw new Exception("Error in FitlibPriestleyTaylor", e);
		}

	}

	public String toString() {
		return "PriestleyTaylor()";
	}

}
