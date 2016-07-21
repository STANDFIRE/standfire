package capsis.lib.phenofit.function;

import java.util.StringTokenizer;

import jeeb.lib.util.Log;
import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibConstants;
import capsis.lib.phenofit.FitlibLocation;
import capsis.lib.phenofit.FitlibLocationClimate;
import capsis.lib.phenofit.FitlibMemory;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.Fit4Phenology;
import capsis.lib.phenofit.FitlibStates;

/**
 * The Phenofit FitlibFixedSenescence function.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FitlibFixedSenescence extends FitlibFunction1Phase implements FitlibPhelibFunctions {

	private double FixedDateSen;

	/**
	 * Constructor
	 */
	public FitlibFixedSenescence(double FixedDateSen) {
		this.FixedDateSen = FixedDateSen;
	}

	/**
	 * Constructor 2, parses a String, e.g. FixedSenescence(13.38)
	 */
	public FitlibFixedSenescence(String str) throws Exception {
		try {
			String s = str.replace("FixedSenescence(", "");
			s = s.replace(")", "");
			s = s.trim();
			StringTokenizer st = new StringTokenizer(s, ";");
			FixedDateSen = Double.parseDouble(st.nextToken().trim());
		} catch (Exception e) {
			throw new Exception("FitlibFixedSenescence: could not parse this function: " + str, e);
		}
	}

	public String getName() {
		return "FixedSenescence";
	}

	public String getExpectedParams() {
		return "FixedDateSen";
	}

	/**
	 * For the given day, compute the current phase and state, and set the
	 * end-of-phase dates when they are reached. Returns 0 if ok.
	 */
	@Override
	public double executeDaily(FitlibLocation loc, FitlibLocationClimate locClim, int year, int day,
			Fit5Phenology pheno, FitlibMemory memory) throws Exception {

		// public double executeDaily(FitlibLocation loc, FitlibLocationClimate
		// locClim, int year, int day, Fit4Phenology pheno, FitlibPhases phases,
		// FitlibStates states) throws Exception {

		try {

			// When date 1 is set, nothing more to do
			if (pheno.isSetDate1()) // fc-10.11.2015
				return 0;

			int nbDays = FitlibClimate.getNbDays(year);
			int sept1 = FitlibClimate.get1September(year);
			int nbDays0 = FitlibClimate.getNbDays(year - 1);

			int prevPhase = memory.phase;
			double prevState3 = memory.state;

			int yesterday = FitlibClimate.getYesterday(year, day);
			int fit4Today = pheno.getPhenofit4Date(year, day);

			if (prevPhase <= 3) {

				// Is phase 3 over ?
				if (fit4Today > FixedDateSen) {
					
					pheno.setDate1(year, yesterday);

				} else {

					// Process phase 3
					memory.phase = 3;
					
					memory.state = (pheno.getPhenofit4Date (year, day) - pheno.getLeafUnfoldingDate())
							/ (FixedDateSen - pheno.getLeafUnfoldingDate());

				}

			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFixedSenescence.execute ()", "Error in FitlibFixedSenescence", e);
			throw new Exception("Error in FitlibFixedSenescence", e);
		}

	}

	@Override
	public double execute(FitlibLocation loc, FitlibLocationClimate locClim, Fit4Phenology pheno, int year, int date0,
			FitlibPhases phases, FitlibStates states) throws Exception {

		try {

			Fit4Phenology pf = pheno;

			int day = (int) FixedDateSen;

			pheno.setDate1(year, day);

			for (int d = date0 + 1; d <= pheno.getDate1(); d++) {
				phases.setValue(d, 3);
				states.setValue(d, (d - date0) / (pheno.getDate1() - date0));
			}

			return 0; // ok

		} catch (Exception e) {
			Log.println(Log.ERROR, "FitlibFixedSenescence.execute ()", "Error in FitlibFixedSenescence", e);
			throw new Exception("Error in FitlibFixedSenescence", e);
		}
	}

	public String toString() {
		return "FixedSenescence(" + FixedDateSen + ")";
	}

}
