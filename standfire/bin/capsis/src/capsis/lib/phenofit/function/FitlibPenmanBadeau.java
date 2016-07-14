package capsis.lib.phenofit.function;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibFitness;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.Fit4Phenology;

/**
 * The Phenofit PenmanBadeau function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */

public class FitlibPenmanBadeau extends FitlibStandardFunction {

	/**
	 * Constructor
	 */
	public FitlibPenmanBadeau() {
	}

	/**
	 * Constructor 2, parses a String, e.g.
	 */
	public FitlibPenmanBadeau(String str) throws Exception {
		try {
			if (!str.startsWith("PenmanBadeau("))
				throw new Exception();

		} catch (Exception e) {
			throw new Exception("FitlibPenmanBadeau: could not parse this function: " + str, e);
		}
	}
	
	public String getName() {
		return "PenmanBadeau";
	}

	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, FitlibFitness fitness, int year) throws Exception {

		try {
			int nbDays = FitlibClimate.getNbDays(year);
			
//			int nbDays = 365;
//			if (locClim.isBissextile(year))
//				nbDays++;
			
			Fit4Phenology pf = pheno;

			// var
			double ALB = 0.2; // herbacees 0.2 foret 0.11
			double Gamma = 0.65;
			double SB = 0.0000000049;
			// Index 1 is 1st January, index 0 is unused
			loc.pet = new double[nbDays + 1];

			for (int d = 1; d <= nbDays; d++) {

				FitlibClimateDay dayClim = locClim.getClimateDay(year, d);

				double LAT_r = (Math.PI / 180) * loc.getLatitude();

				// Calcul de la tension de vapeur saturante
				double Ew = 6.1070 * Math.pow((1 + Math.sqrt(2) * Math.sin((dayClim.tmp * Math.PI / 180) / 3)), 8.827);
				// Calcul de la pente de la courbe Ew � Tw
				double Ew1 = 6.1070 * Math.pow(
						(1 + Math.sqrt(2) * Math.sin(((dayClim.tmp - 0.5) * Math.PI / 180) / 3)), 8.827);
				double Ew2 = 6.1070 * Math.pow(
						(1 + Math.sqrt(2) * Math.sin(((dayClim.tmp + 0.5) * Math.PI / 180) / 3)), 8.827);

				double Delta = Ew2 - Ew1;

				// Calcul du d�ficit de sturation de l'air

				double Dsat = Ew * (1 - (dayClim.rh / 100));

				// Calcul de la tension de vapeur

				double TV = Ew - Dsat;

				// calcul rayonnement extraterrestre routine FAO
				// GSC=0.0820;
				double DECL_r = 1 + 0.033 * Math.cos(((2 * Math.PI) / 365) * (d));
				double Decli = 0.409 * Math.sin(((2 * Math.PI) / 365) * (d) - 1.39); // solar
																						// declination
																						// (rad)
				double AngSol = Math.acos(-Math.tan(LAT_r) * Math.tan(Decli)); // solar
																				// angle
																				// (rad)
				// So:=AngSol*24/PI;
				double Go_MJ = (24 * 60 / Math.PI * 0.0820 * DECL_r)
						* (AngSol * Math.sin(LAT_r) * Math.sin(Decli) + Math.cos(LAT_r) * Math.cos(Decli)
								* Math.sin(AngSol));

				double L = (2500840 - 2358.6 * dayClim.tmp) * 0.000001;
				double TMa = dayClim.tmp + 273.16;

				double fv = 1 + 0.54 * dayClim.wnd; //
				double Fracinsol = (((dayClim.glo / 100) / Go_MJ) - 0.18) / 0.62; // glo^[jour]/100
																					// J/cm2
																					// -
																					// MJ/m2
				double Rayboltz = SB * Math.pow(TMa, 4) * (0.56 - 0.08 * Math.sqrt(TV)) * (0.1 + 0.9 * Fracinsol);
				double E = (1 - ALB) * ((dayClim.glo / 100)) - Rayboltz; // glo^[jour]/100
																			// J/cm2
																			// -
																			// MJ/m2
				double Eair = 0.26 * Dsat * fv;

				double pet = ((Delta * E / L) + (Gamma * Eair)) / (Delta + Gamma);
				loc.pet[d] = pet;
				if (loc.pet[d] < 0)
					loc.pet[d] = 0;

			}

			return 0;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibPenmanBadeau.execute ()", "Error in FitlibPenmanBadeau", e);
			throw new Exception("Error in FitlibPenmanBadeau", e);
		}

	}

	public String toString() {
		return "PenmanBadeau()";
	}

}
