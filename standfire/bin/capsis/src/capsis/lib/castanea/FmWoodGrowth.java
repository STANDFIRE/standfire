package capsis.lib.castanea;

import java.io.Serializable;
import java.util.Collection;
import java.util.Arrays;

import java.util.Iterator;

import jeeb.lib.util.Log;

/**
 * FmWood : wood in a FLCell of Dynaclim model.
 *
 * @author Hendrik Davi - march 2006
 */
public class FmWoodGrowth implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone ()
	// for template)

	public double[] dailyWoodGrowth; // g m-2
	public double[] dailyCoarseRootsGrowth;
	public double[] dailyFineRootsGrowth; // g m-2
	public double[] dailyReservesGrowth; // g m-2

	// other properties here...

	/**
	 * Constructor for new logical FmWood.
	 */
	public FmWoodGrowth(FmCell cell, FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];


		dailyWoodGrowth = new double[nSpecies]; // g m-2
		dailyCoarseRootsGrowth = new double[nSpecies];
		dailyFineRootsGrowth = new double[nSpecies]; // g m-2
		dailyReservesGrowth = new double[nSpecies];

//		double[] dailyWoodGrowth = new double[nSpecies]; // g m-2
//		double[] dailyCoarseRootsGrowth = new double[nSpecies];
//		double[] dailyFineRootsGrowth = new double[nSpecies]; // g m-2
//		double[] dailyReservesGrowth = new double[nSpecies];

		// UNUSED fc+hd-15.10.2014
//		double tronvivAge = 0;

//		int sp = 0;
//		for (FmSpecies species : fmSpeciesList) {
//			dailyWoodGrowth[sp] = 0;
//			dailyCoarseRootsGrowth[sp] = 0;
//			dailyFineRootsGrowth[sp] = 0;
//			dailyReservesGrowth[sp] = 0;
//
//			sp = sp + 1;
//		}

//		this.dailyWoodGrowth = dailyWoodGrowth;
//		this.dailyCoarseRootsGrowth = dailyCoarseRootsGrowth;
//		this.dailyFineRootsGrowth = dailyFineRootsGrowth;
//		this.dailyReservesGrowth = dailyReservesGrowth;

	}

	public double[] getDailyWoodGrowth() {
		return dailyWoodGrowth;
	}

	public double[] getDailyCoarseRootsGrowth() {
		return dailyCoarseRootsGrowth;
	}

	public double[] getDailyFineRootsGrowth() {
		return dailyFineRootsGrowth;
	}

	public double[] getDailyReservesGrowth() {
		return dailyReservesGrowth;
	}

	public void setDailyWoodGrowth(double[] v) {
		dailyWoodGrowth = v;
	}

	public void setDailyCoarseRootsGrowth(double[] v) {
		dailyCoarseRootsGrowth = v;
	}

	public void setDailyFineRootsGrowth(double[] v) {
		dailyFineRootsGrowth = v;
	}

	public void setDailyReservesGrowth(double[] v) {
		dailyReservesGrowth = v;
	}

	public void carbonAllocation(FmCell cell, FmYearlyResults yearlyResults, FmDailyResults dailyResults, int j,
			FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmCanopy canopy = cell.getCanopy();
		FmWood wood = cell.getWood();
		FmSoil soil = cell.getSoil();
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];

		Collection<FmCanopyLayer> canopylayers = canopy.getLayers();

		double aboveLAI = 0;
		int nbstrat = settings.NB_CANOPY_LAYERS;

		double[] L = canopy.getLAI();
		double strat = 0;
		double[][] strat_sp = canopy.getStrat_sp();
		double LMAlayer = 0;

		// double coefrac= 1; // fixed to be improved (hydraulic model or data
		// with age effect)

		double GBV = 0;
		double GSS = 0;
		double GRG = 0;
		double GRF = 0;

		double BRFth = 0;
		double[] biomassOfFineRoot = wood.getBiomassOfFineRoot();
		double[] biomassOfTrunk = wood.getBiomassOfTrunk();
		double[] biomassOfBranch = wood.getBiomassOfBranch();
		double[] biomassOfReserves = wood.getBiomassOfReserves();
		double[] biomassOfReservesMinimal = wood.getBiomassOfReservesMinimal();
		double[] biomassOfCoarseRoot = wood.getBiomassOfCoarseRoot();
		double[] biomassOfAliveWood = wood.getBiomassOfAliveWood();
		double[] reserveToGrowth = new double[nSpecies];

		// double [] GRF= cell.getWood ().getGRF();

		// double [] GRFsp= new double[nSpecies]; ;

		double DBF = 0;
		double DBSS = 0;
		double DBBV = 0;
		double DBRF = 0;
		double DBRG = 0;
		double MBSS = 0;
		double BFold = 0;
		double MBRF = 0;
		double MBRG = 0;
		double MBBV = 0;

		double[] biomassOfAerialWood = new double[nSpecies];

		double CRSS = 1;

		// double BBV= cell.getWood ().getBiomassOfAliveWood();
		int sp = 0;
		int cohortesOfLeaves = (int) species.cohortesOfLeaves;

		BFold = canopy.getBiomassOfLeaves(sp);

		double BF = 0;
		double ConcBSS = biomassOfReserves[sp] / wood.getBiomassOfAliveWood()[sp];
		double PBjC = yearlyResults.getCanopyPhotosynthesis(sp, j);
		double RVM = yearlyResults.getMaintenanceRespiration(sp, j);
		double RCF = yearlyResults.getLeafGrowthRespiration(sp, j);
		// Log.println(settings.logPrefix+"understanding",
		// L[sp]+";"+PBjC+";"+RVM+";"+RCF+";"+biomassOfFineRoot[sp]+";"+BFold+";"+BF);

		if (L[sp] > 0) {
			int l = 0;

			double LMAmoy = canopy.getAverageLMA(cell, settings, species, sp);
			BF = LMAmoy * L[sp] * settings.tc;

			//BRFth = BF * cell.getCoefraccell()[sp];
			BRFth = BF * species.coefrac;
			DBF = Math.max(BF - BFold, 0);

			// Log.println(settings.logPrefix+"GBV",
			// "biomassOfFineRoot[sp]=" +species.RS+";"+
			// GRFsp[sp]+";"+ConcBSS+";"+species.TGSS);


			if (yearlyResults.getDayOfWoodStop(sp) == 366 && yearlyResults.getBudburstDate(sp) > 0) {
				// calculation of new allocation coefficients
				if (settings.allocRemain == "wood") {
					GRF = Math.min(Math.max(settings.GRFinit * (BRFth / biomassOfFineRoot[sp]), 0.), 0.5);
					if (cell.getSoil().getPotsoil() > cell.getPotsoiltowoodcell()[sp]) {
						GBV = Math.max((1 - GRF) / (wood.getRootShoot()[sp] + species.TGSS / ConcBSS + 1), 0.);
					} else {
						GBV = 0;
					}
					GRG = GBV * wood.getRootShoot()[sp];
					GSS = GBV * species.TGSS / ConcBSS;
				}

				if (settings.allocRemain == "reserves") {
					GRF = Math.min(Math.max(settings.GRFinit * (BRFth / biomassOfFineRoot[sp]), 0.), 1- 1.2*species.GBVmin);
					if (cell.getSoil().getPotsoil() > cell.getPotsoiltowoodcell()[sp]) {
						//GBV = Math.min(Math.max(cell.getGBVmincell()[sp] * (ConcBSS / species.TGSS), 0.), 0.5);
						GBV = cell.getGBVmincell()[sp];
					} else {
						GBV = 0;
					}
					GRG = GBV * wood.getRootShoot()[sp];
					GSS = 1 - GBV - GSS - GRF - GRG;
				}
			} else {
				GBV = 0;
				GRG = 0;
				GRF = Math.min(Math.max(settings.GRFinit * (BRFth / biomassOfFineRoot[sp])* (ConcBSS / species.TGSS), 0.), 0.9);
				// Math.min(species.TGSS/ConcBSS,1);
				GSS = 1 - GRF; // pb for evergreen to be improved, where
								// the carbon goes during winter
			}


			// calculation of Growth increment

			double coefToBSS = 0.05;
			if (settings.allocSchema == "Davi2014") {

				if (ConcBSS > species.TGSS) {
					reserveToGrowth[sp] = (biomassOfReserves[sp] - species.TGSS * biomassOfAliveWood[sp])
							* coefToBSS;
				} else {
					reserveToGrowth[sp] = 0;
				}
			} else {
				reserveToGrowth[sp] = 0;
			}
			// DBSS= Math.max(0.,(GSS/ CRSS)* (PBjC- RVM-DBF-RCF ));
			DBRF = Math.max(0., (GRF / species.CRRF) * (PBjC - RVM - DBF - RCF + reserveToGrowth[sp]));
			DBRG = Math.max(0., (GRG / species.CRRG) * (PBjC - RVM - DBF - RCF + reserveToGrowth[sp]));
			DBBV = Math.max(0., (GBV / species.CRBV) * (PBjC - RVM - DBF - RCF + reserveToGrowth[sp]));
			DBSS = Math.max(0., (GSS / CRSS) * (PBjC - RVM - DBF - RCF));

			MBSS = Math.max(0., RVM - PBjC + DBF + RCF - reserveToGrowth[sp]);

		} else { // case where LAI is nul
			DBSS = 0;
			MBSS = Math.max(0., RVM - PBjC);
			DBRF = 0;
			DBRG = 0;
			DBBV = 0;
		}

		MBRF = species.TMRF / 365 * biomassOfFineRoot[sp]; // to be improve and
															// modulated by
														// roots
		double TMRG	=species.TMBV; //to be improved									// cavitation

		MBRG= TMRG * biomassOfCoarseRoot[sp];			 	//to be improve and modulated by cavitation and NSC
		MBBV= species.TMBV * biomassOfBranch[sp]; 				//to be improve and modulated by cavitation and wind

		biomassOfAerialWood[sp] = biomassOfTrunk[sp] + biomassOfBranch[sp] + DBBV-MBBV; // normalyy MBBV only for branches to be improved
		biomassOfBranch[sp] = species.ratioBR * biomassOfAerialWood[sp];
		biomassOfTrunk[sp] = (1 - species.ratioBR) * biomassOfAerialWood[sp];

		biomassOfCoarseRoot[sp] = biomassOfCoarseRoot[sp] + DBRG-MBRG;
		biomassOfFineRoot[sp] = biomassOfFineRoot[sp] + DBRF - MBRF;
		biomassOfReserves[sp] = biomassOfReserves[sp] + DBSS - MBSS;

		if (biomassOfReserves[sp] < biomassOfReservesMinimal[sp]) {
			biomassOfReservesMinimal[sp] = biomassOfReserves[sp];
		}

		/*
		 * if (settings.allocSchema=="Davi2014") { // Davi2009 Francois
		 * Martin2013....///EN CHANTIER/// double Tmoy=
		 * yearlyResults.getTmoy(day-1); FmSoil soil= cell.getSoil ();
		 * double potsoil= soil.getPotsoil(); double QDIX = settings.QDIX;
		 * double TBASE = settings.Tbase; double labilePool=
		 * PBjC-RVM-DBF-RCF; double woodSink= ConcBSS/species.TGSS; double
		 * fineRootsSink= BRFth* Math.pow (QDIX, ((Tmoy - TBASE) / 10));
		 * double coarseRootsSink=woodSink *wood.getRootShoot()[sp];
		 *
		 *
		 * // All daily available carbon is sent to reserves
		 * //BSS=BSS+PBjC-RVM-DBF-RCF;
		 *
		 *
		 * // sink for fine roots to sustain hydraulic pathway //GRFsp[sp]=
		 * Math
		 * .min(Math.max(settings.GRF*(BRFth/biomassOfFineRoot[sp]),0.),0.5
		 * )* Math.pow (QDIX, ((Tmoy - TBASE) / 10)); GRF=
		 * Math.min(Math.max(
		 * settings.GRFinit*(BRFth/biomassOfFineRoot[sp]),0.),0.5); if
		 * (yearlyResults.getDayOfWoodStop(sp)==366 &&
		 * yearlyResults.getBudburstDate(sp)>0) { GBV=
		 * Math.min(Math.max(cell
		 * .getGBVmin()[sp]*(ConcBSS/species.TGSS),0.),0.5); }
		 *
		 *
		 * }
		 */

		canopy.setBiomassOfLeaves(sp, BF);
		yearlyResults.setBiomassOfReserves(sp, j, biomassOfReserves[sp]);
		yearlyResults.setLeafGrowth(sp, j, DBF);
		yearlyResults.setWoodGrowth(sp, j, DBBV);
		yearlyResults.setCoarseRootsGrowth(sp, j, DBRG);
		yearlyResults.setFineRootsGrowth(sp, j, DBRF);
		yearlyResults.setReservesGrowth(sp, j, DBSS);
		yearlyResults.setFineRootsMortality(sp, j, MBRF);
		yearlyResults.setMBBV(sp, j, MBBV);
		yearlyResults.setMBRG(sp, j, MBRG);


		if (settings.output > 1) {
			Log.println(settings.logPrefix + "woodGrowth",
					"  " + BF + ";" + biomassOfFineRoot[sp] + ";" + biomassOfReserves[sp] + ";"
							+ biomassOfTrunk[sp] + ";" + biomassOfBranch[sp] + ";" + biomassOfCoarseRoot[sp] + ";"
							+ DBBV + ";" + DBSS + ";" + DBRF + ";" + MBSS + ";" + GBV + ";" + GSS + ";" + GRF + ";"
							+ DBF + ";" + RVM + ";" + species.coefrac + ";" + yearlyResults.getDayOfWoodStop(sp)
							+ ";" + RCF + ";" + cell.getCanopy().getLAImax()[sp] + ";" + reserveToGrowth[sp]+";"+MBBV+";"+MBRF+";"+MBRG);
		}
		wood.setBiomassOfReservesMinimal(sp, biomassOfReservesMinimal[sp]);


		wood.setBiomassOfBranch(biomassOfBranch);
		wood.setGRF(GRF);
		wood.setBiomassOfTrunk(biomassOfTrunk);
		wood.setBiomassOfFineRoot(biomassOfFineRoot);
		wood.setBiomassOfCoarseRoot(biomassOfCoarseRoot);
		wood.setBiomassOfReserves(biomassOfReserves);

		if (settings.rootsCavitation) {
			soil.calculateFineRootsMortality(cell, yearlyResults, j, settings);
		}

	}

}
