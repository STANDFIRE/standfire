package capsis.lib.phenofit.function;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * The Phenofit FitlibPenmanFAO function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */

public class FitlibPenmanFAO extends FitlibStandardFunction {

	/**
	 * Constructor
	 */
	public FitlibPenmanFAO() {
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibPenmanFAO(String str) throws Exception {
		try {
			if (!str.startsWith("PenmanFAO("))
				throw new Exception();

		} catch (Exception e) {
			throw new Exception("FitlibPenmanFAO: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "PenmanFAO";
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

				// Calculation of pression, Gammafao and Deltafao
				double Deltafao = 4098 * (0.6108 * Math.exp((17.27 * dayClim.tmp) / (dayClim.tmp + 237.3)))
						/ (Math.pow(dayClim.tmp + 237.3, 2));
				;

				double Etmin = 0.6108 * Math.exp((17.27 * dayClim.tmn) / (dayClim.tmn + 237.3));
				double Etmax = 0.6108 * Math.exp((17.27 * dayClim.tmx) / (dayClim.tmx + 237.3));
				double Es = (Etmax + Etmin) / 2; // saturated vapour pressure
													// kPa
				double Ea = (dayClim.rh / 100) * Es; // vapour pressure in kPa

				double P = 101.3 * Math.pow(
						(((dayClim.tmp + 273.16) - (0.0065 * loc.getAltitude())) / (dayClim.tmp + 273.16)), 5.256); // atmospheric
																													// pressure
																													// //yassine
																													// suppose
																													// that
																													// elev
																													// =
																													// altitude
																													// !!?

				double Gammafao = 1615 * P / 2450000;

				// Calculation of Ra extraterrestrial radiation
				double Phi = (Math.PI / 180) * loc.getLatitude(); // latitude in
																	// radian
				double Dr = 1 + 0.033 * Math.cos(((2 * Math.PI) / 365) * (d));
				double Decli = 0.409 * Math.sin(((2 * Math.PI) / 365) * d - 1.39); // solar
																					// declination
																					// (rad)
				double AngSol = Math.acos(-Math.tan(Phi) * Math.tan(Decli)); // solar
																				// angle
																				// (rad)
				double Ra1 = 24 * 60 / Math.PI * 0.0820 * Dr;
				double Ra2 = AngSol * Math.sin(Phi) * Math.sin(Decli)
						+ (Math.cos(Phi) * Math.cos(Decli) * Math.sin(AngSol));
				double Ra = Ra1 * Ra2;

				// calculation of Rn
				double Rso = (0.75 + (2 / (Math.pow(10, 5))) * loc.getAltitude()) * Ra;
				double Rns = (1 - 0.23) * dayClim.glo; // Rns=net incoming solar
														// radiation

				double RsSURRso = dayClim.glo / Rso; // Rs=GLO=total incoming
														// solar radiation
				if (RsSURRso > 1)
					RsSURRso = 1;

				double steph = 4.903 / Math.pow(10, 9);

				double Rnl1 = steph
						* (((Math.pow((dayClim.tmx + 273.16), 4)) + (Math.pow((dayClim.tmn + 273.16), 4)) / 2));
				double Rnl2 = 0.34 - (0.14 * Math.sqrt(Ea));
				double Rnl3 = (1.35 * RsSURRso) - 0.35;
				double Rnl = Rnl1 * Rnl2 * Rnl3;

				// est-ce normal que �a donne des valeur n�gative? RSA
				double Rn = Math.max(0, Rns - Rnl);

				double petHigh = (0.408 * Deltafao * Rn)
						+ (Gammafao * (900 / (dayClim.tmp + 273.16)) * dayClim.wnd * (Es - Ea));
				double petLow = Deltafao + Gammafao * (1 + 0.34 * dayClim.wnd);

				double pet = petHigh / petLow;
				loc.pet[d] = pet;
				if (loc.pet[d] < 0)
					loc.pet[d] = 0;

			}

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibPenmanFAO.execute ()", "Error in FitlibPenmanFAO", e);
			throw new Exception("Error in FitlibPenmanFAO", e);
		}

	}

	public String toString() {
		return "PenmanFAO()";
	}

}
