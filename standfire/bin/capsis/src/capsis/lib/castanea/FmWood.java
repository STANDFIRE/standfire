package capsis.lib.castanea;

import java.io.Serializable;
import java.util.Collection;
import java.util.Arrays;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;

/**
 * FmWood : wood in a FLCell of Dynaclim model.
 *
 * @author Hendrik Davi - march 2006
 */
public class FmWood implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	private double[] biomassOfTrunk; // g m-2
	private double[] biomassOfAliveWood;
	private double[] biomassOfBranch; // g m-2
	private double[] biomassOfCoarseRoot; // g m-2
	private double[] biomassOfFineRoot; // g m-2
	private double[] biomassOfReserves; // g m-2
	private double[] biomassOfReservesMinimal; // g m-2
	private double GRF;
	private double[] treeVolume;
	private double[] rootShoot;


	/**
	 * Constructor for new logical FmWood.
	 */
	public FmWood (int nbSpecies) {

		biomassOfTrunk = new double[nbSpecies];
		biomassOfAliveWood = new double[nbSpecies];
		biomassOfBranch = new double[nbSpecies];
		biomassOfCoarseRoot = new double[nbSpecies];
		biomassOfFineRoot = new double[nbSpecies];
		biomassOfReserves = new double[nbSpecies];
		biomassOfReservesMinimal = new double[nbSpecies];
		rootShoot=  new double[nbSpecies];
		treeVolume= new double[nbSpecies];
	}

	/**
	 * Inits the wood.
	 */
	public void init (FmCell cell, FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];

		FmCanopy canopy= cell.getCanopy();

		double[] meanHeight = new double[nSpecies];

		// CAUTION: biomassOfTrunk, biomassOfBranch must have been set before

		int sp = 0;

		meanHeight[sp]=species.aGF*Math.pow(cell.getMeanDbh()[sp],species.bGF);

		double projectedAreaOfCanopy= cell.getProjectedAreaOfCanopy(cell.getMeanDbh()[sp],species.CrownArea1,species.CrownArea2);
		treeVolume[sp]= Math.pow(cell.getMeanDbh()[sp]/2/100,2)*species.Phi*Math.PI*meanHeight[sp];
		double tronvivAge= 0;

		biomassOfTrunk[sp]= treeVolume[sp]/projectedAreaOfCanopy*settings.tc*species.ros*1000;
		double age = cell.getMeanAge ()[sp];
		int ageOfTrees = (int) age;
		biomassOfBranch[sp]=  species.ratioBR/(1-species.ratioBR)*biomassOfTrunk[sp];
		rootShoot[sp]= this.getAgeEffectRS(settings, cell, species, sp, ageOfTrees);

		biomassOfCoarseRoot[sp]= rootShoot[sp]*(biomassOfBranch[sp]+biomassOfTrunk[sp]);

		if (settings.fixedTronviv) {
			biomassOfAliveWood[sp]= species.tronviv*(this.biomassOfTrunk[sp])+species.branviv*this.biomassOfBranch[sp];
		} else {
			tronvivAge= this.getTronvivAge(cell,species,settings,cell.getMeanDbh()[sp]);
			//branvivAge= this.getBranvivAge(cell,species,settings,ageOfTrees);
			biomassOfAliveWood[sp] = tronvivAge * (this.biomassOfTrunk[sp]) + species.branviv * this.biomassOfBranch[sp];

		}

		biomassOfReserves[sp] = species.TGSS * biomassOfAliveWood[sp];
		biomassOfReservesMinimal[sp] = 1000;
		//Theoretical Biomass of reserve (to compute LAI)
		double BSSth = species.TGSS * biomassOfAliveWood[sp];
		cell.getG()[sp] = Math.pow ((cell.getMeanDbh()[sp] / 2 / 100), 2) * Math.PI / projectedAreaOfCanopy * 10000; // average
		//cell.setG(G);

		if (settings.fixedLAI>0){
			//~ Gsp[sp] = G;
			if (BSSth > 0 & biomassOfReserves[sp]>0) {
					canopy.getLAImax ()[sp] = FmCanopy.getLaiCalc (cell, species, settings, sp, true);
			} else {
				canopy.getLAImax ()[sp] = 0;
			}
			//System.out.println (cell.getMeanDbh()[sp]);
			//System.out.println (canopy.getLAImax ()[sp]);
		}

		double LMAmoy = cell.getLMAcell()[sp]* (1 - Math.exp (-species.KLMA * canopy.getLAImax ()[sp]))
				/ (species.KLMA * canopy.getLAImax ()[sp]);
		double BF = LMAmoy * settings.tc * canopy.getLAImax ()[sp];
		//biomassOfFineRoot[sp]= BF*cell.getCoefraccell()[sp];
		biomassOfFineRoot[sp]= BF*species.coefrac;


		GRF = settings.GRFinit; // initial value



		if (settings.output>0) {
			Log.println(settings.logPrefix+"init", "ageOfTrees; DBH; Height;biomassOfTrunk;biomassOfBranch;biomassOfCoarseRoot;biomassOfFineRoot;biomassOfAliveWood;biomassOfReserves;LMAX;G;LMAmy;Tronviv;Branviv;potsoil");

			Log.println(settings.logPrefix+"init", ageOfTrees+";"+cell.getMeanDbh()[sp]+";"+meanHeight[sp]+ ";"+biomassOfTrunk[sp]+";"+biomassOfBranch[sp]+";"+biomassOfCoarseRoot[sp]+
			";"+biomassOfFineRoot[sp]+";"+biomassOfAliveWood[sp]+";"+biomassOfReserves[sp]+";"+canopy.getLAImax ()[sp]
			+";"+cell.getG()[sp]+";"+LMAmoy+";"+tronvivAge+";"+species.branviv+";"+cell.getPotsoiltowoodcell()[sp]);
		}

	//	if (settings.fixedLAI>0) {
	//			canopy.setLAImaxBeforeFrost (canopy.getLAImax ());
	//	}

		canopy.setWAI (cell.getWAIfromLAI (canopy.getLAImax (), fmSpeciesList));
		cell.setMeanHeight (meanHeight);

		// double[] woodRespiration = new double [nSpecies];
		// Arrays.fill(woodRespiration,0);
		// this.woodRespiration =woodRespiration;

	}

	/**
	 * Clone method.
	 */
	public Object clone () {
		try {
			FmWood w = (FmWood) super.clone (); // calls protected Object Object.clone () {}

			w.biomassOfTrunk = AmapTools.getCopy (biomassOfTrunk);
			w.biomassOfAliveWood = AmapTools.getCopy (biomassOfAliveWood);
			w.biomassOfBranch = AmapTools.getCopy (biomassOfBranch);
			w.biomassOfCoarseRoot = AmapTools.getCopy (biomassOfCoarseRoot);
			w.biomassOfFineRoot = AmapTools.getCopy (biomassOfFineRoot);
			w.biomassOfReserves = AmapTools.getCopy (biomassOfReserves);
			w.biomassOfReservesMinimal = AmapTools.getCopy (biomassOfReservesMinimal);
			return w;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FmWood.clone ()", "Error while cloning", e);
			return null;
		}
	}

	public double[] getBiomassOfTrunk () {
		return biomassOfTrunk;
	} // g m-2

	public void setBiomassOfAliveWood (double[] biomassOfAliveWood) {
		this.biomassOfAliveWood = biomassOfAliveWood;
	}

	public double[] getBiomassOfBranch () {
		return biomassOfBranch;
	}

	public double[] getBiomassOfAliveWood () {
		return biomassOfAliveWood;
	}// g m-2

	public double[] getBiomassOfCoarseRoot () {
		return biomassOfCoarseRoot;
	} // g m-2

	public double[] getBiomassOfFineRoot () {
		return biomassOfFineRoot;
	} // g m-2

	public double[] getBiomassOfReserves () {
		return biomassOfReserves;
	}

	public double[] getBiomassOfReservesMinimal () {
		return biomassOfReservesMinimal;
	} // g m-2
	// public double[] getWoodRespiration(){return woodRespiration;} //�mol co2 m-2 s-1

	public double getGRF () {
		return GRF;
	}
	public double[] getRootShoot () {
		return rootShoot;
	}

	public void setBiomassOfTrunk (double[] v) {
		biomassOfTrunk = v;
	}

	public void setBiomassOfBranch (double[] v) {
		biomassOfBranch = v;
	}

	public void setBiomassOfCoarseRoot (double[] v) {
		biomassOfCoarseRoot = v;
	}

	public void setBiomassOfFineRoot (double[] v) {
		biomassOfFineRoot = v;
	}

	public void setBiomassOfReserves (double[] v) {
		biomassOfReserves = v;
	}

	public void setBiomassOfReservesMinimal (int sp, double v) {
		biomassOfReservesMinimal[sp] = v;
	}

	public void setGRF (double v) {
		GRF = v;
	}



	// public void setWoodRespiration (double[] v) {woodRespiration=v;}

	public void hourlyWoodRespiration (FmCell cell, int j, double Tveg, double Tsolh, FmClimateDay climateDay,
			FmYearlyResults yearlyResults, FmDailyResults dailyResults, FmHourlyResults hourlyResults, FmSettings settings, int age) {

		int day= j+1;
		double conversion= 3.6 * 12/ 1000.; // conversion to gc

		double[] canopyRespiration= hourlyResults.getCanopyRespiration ();

		double[] maintenanceRespiration = dailyResults.getMaintenanceRespiration ();
		double[] growthRespiration = dailyResults.getGrowthRespiration ();

		double[] leafGrowthRespiration = dailyResults.getLeafGrowthRespiration ();
		double[] coarseRootsRespiration = dailyResults.getCoarseRootsRespiration ();
		double[] fineRootsRespiration = dailyResults.getFineRootsRespiration ();
		double[] woodRespiration = dailyResults.getWoodRespiration ();

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;

		double QDIX = settings.QDIX;
		double TBASE = settings.Tbase;
		double MRN = settings.MRN;

		double[] biomassOfTrunk = this.getBiomassOfTrunk ();
		double[] biomassOfBranch = this.getBiomassOfBranch ();
		double[] biomassOfCoarseRoot = this.getBiomassOfCoarseRoot ();
		double[] biomassOfFineRoot = this.getBiomassOfFineRoot ();

		double[] RVMh = new double[nSpecies];
		double[] RVCh = new double[nSpecies];
		double[] Tah = new double[24];

		double DBF = 0;
		double DBBV = 0;
		double DBRG = 0;
		double DBRF = 0;

		double BTV = 0;
		double BBRV =0;
		double BRGV =0;

		double tronvivAge= 0;

		double TEMPj = 0;
		for (int h = 0; h < 24; h++) {
			Tah[h] = climateDay.getHourlyTemperature (h);
			double TEMPh = Math.pow (QDIX, ((Tah[h] - TBASE) / 10));
			TEMPj = TEMPj + TEMPh;
		}

		int sp = 0;
		for (FmSpecies species : fmSpeciesList) {
			double dbh= cell.getMeanDbh()[sp];
			// need to add proportion of sapwood
			BBRV = species.branviv * biomassOfBranch[sp];

			if (settings.fixedTronviv) {
				BTV = species.tronviv * biomassOfTrunk[sp];
				BRGV = species.tronviv * biomassOfCoarseRoot[sp];
			} else {

				tronvivAge= this.getTronvivAge(cell,species,settings,dbh);
				BTV = tronvivAge * biomassOfTrunk[sp];
				BRGV = tronvivAge* biomassOfCoarseRoot[sp];

			}

			this.biomassOfAliveWood[sp]= BTV+BBRV;


			double BRFV = biomassOfFineRoot[sp];


			if (day > 1) { // lack a day to be to improved

				DBF = yearlyResults.getLeafGrowth (sp, j-1);
				DBBV = yearlyResults.getWoodGrowth (sp, j-1);
				DBRG = yearlyResults.getCoarseRootsGrowth (sp,j-1);
				DBRF = yearlyResults.getFineRootsGrowth (sp, j-1);
			} else {
				DBF = 0;
				DBBV = 0;
				DBRG = 0;
				DBRF = 0;
			}

			double NRG = species.coarseRootsNitrogen;
			double NRF = species.fineRootsNitrogen;
			double NTV = species.stemNitrogen;
			double NBR = species.branchesNitrogen;
			double NSS = species.reservesNitrogen;

			double MRRGO = MRN * NRG;
			double MRRFO = MRN * NRF;
			double MRTVO = MRN * NTV;
			double MRBRO = MRN * NBR;
			double MRSSO = MRN * NSS;

			// RESPIRATION D'ENTRETIEN SEMI-HORAIRE DES ORGANES gC/ m2/ demi-heure
			// Calculation of respiratory coefficients for mainteanace respiration i gC / gC / hour
			double MRTV = 12 * MRTVO * Math.pow (QDIX, ((Tveg - TBASE) / 10)); // alive stems
			double MRBR = 12 * MRBRO * Math.pow (QDIX, ((Tveg - TBASE) / 10)); // branches
			double MRRG = 12 * MRRGO * Math.pow (QDIX, ((Tsolh - TBASE) / 10)); // coarse roots
			double MRRF = 12 * MRRFO * Math.pow (QDIX, ((Tsolh - TBASE) / 10)); // fine roots
			double MRSS = 12 * MRSSO * Math.pow (QDIX, ((Tveg - TBASE) / 10)); // Reserves

			// !hourly respiration for maintenance in gC/ m2/ hour

			double RMTVh = MRTV * BTV;
			double RMBRh = MRBR * BBRV;
			double RMRGh = MRRG * BRGV;
			double RMRFh = MRRF * BRFV;
			double RMSSh = 0;

			RVMh[sp] = RMTVh + RMBRh + RMRGh + RMRFh + RMSSh;

			double RCF = (species.CRF - 1) * DBF;
			double RCBV = (cell.getCRBVcell()[sp] - 1) * DBBV;
			double RCRG = (species.CRRG - 1) * DBRG;
			double RCRF = (species.CRRF - 1) * DBRF;

			// ! calcul des coefficients respiratoires
			double RCFO = RCF / TEMPj;
			double RCBVO = RCBV / TEMPj;
			double RCRGO = RCRG / TEMPj;
			double RCRFO = RCRF / TEMPj;
			double RCSSO = 0;

			// hourly respiration for growth in gC/ m2/ hour
			// calculated from previous days growth
			// Sert uniquement en sortie diagnostique (respiration autotrophe)

			double RCFh = RCFO * Math.pow (QDIX, ((Tveg - TBASE) / 10));
			double RCBVh = RCBVO * Math.pow (QDIX, ((Tveg - TBASE) / 10));
			double RCRGh = RCRGO * Math.pow (QDIX, ((Tsolh - TBASE) / 10));
			double RCRFh = RCRFO * Math.pow (QDIX, ((Tsolh - TBASE) / 10));
			double RCSSh = RCSSO * Math.pow (QDIX, ((Tveg - TBASE) / 10));

			RVCh[sp] = RCFh + RCRFh + RCRGh + RCBVh + RCSSh;

	//
	if (settings.output>2) {
		Log.println(settings.logPrefix+"hourlyWoodRespiration", RCFh+";"+
				 RCRFh+";"+RCRGh+";"+RCBVh+";"+RMTVh+";"+RMBRh+";"+RMRGh+";"+RMRFh+";"+tronvivAge+";"+biomassOfTrunk[sp]+";"+Tveg+";"+Tsolh+";"+QDIX+";"+canopyRespiration[sp]*conversion+";"+RCFO+";"+TEMPj+";"+DBF);
	}

			maintenanceRespiration[sp] = maintenanceRespiration[sp] + RVMh[sp] + canopyRespiration[sp]*conversion;
			growthRespiration[sp] = growthRespiration[sp] + RVCh[sp];
			leafGrowthRespiration[sp] = leafGrowthRespiration[sp] + RCFh;

			fineRootsRespiration[sp] = fineRootsRespiration[sp] + RCRFh + RMRFh;
			coarseRootsRespiration[sp] = coarseRootsRespiration[sp] + RCRGh + RMRGh;
			woodRespiration[sp] = woodRespiration[sp] + RCBVh + RMBRh + RMTVh;

			sp = sp + 1;

		}
		dailyResults.setMaintenanceRespiration (maintenanceRespiration);
		dailyResults.setGrowthRespiration (growthRespiration);
		dailyResults.setLeafGrowthRespiration (leafGrowthRespiration);

		dailyResults.setFineRootsRespiration (fineRootsRespiration);
		dailyResults.setCoarseRootsRespiration (coarseRootsRespiration);
		dailyResults.setWoodRespiration (woodRespiration);

	}


		// public void getTronviv ()

	public double getTronvivAge (FmCell cell, FmSpecies species, FmSettings settings, double dbh) {
   // default value
		//double coef1=24.5;
		double coef=-0.0033; // dbh in mm in Ceshia et al. 2002 erreur dans le papier de Ceshia 0.033^pas 0.066

		// Ceshia et al.2002


		if (species.castaneaCode==4) {
			coef= -0.0033;
		}

		if (species.castaneaCode==7) {// to improve
			coef= -0.0028;
		}

		if (species.castaneaCode==1) {// to improve

		}


		double tronviv= Math.max(species.tronviv+ coef*dbh,0.1);
		return tronviv;
	}



	public double getAgeEffectRS (FmSettings settings, FmCell cell, FmSpecies species, int sp, int age) {
	 // default value
			double coef1=1.6259;
			double coef2=-0.3174;

				if (species.castaneaCode==4) {
					coef1=1.6259;
					coef2=-0.3174;
				}
				if (species.castaneaCode==3) {// to improve
					coef1=0.8637;
					coef2=-0.3174;
				}

				if (species.castaneaCode==7) {// to improve
							coef1=0.8295;
							coef2=-0.0496;
				}

				double ageEffectRs= cell.getRootshootcell()[sp] + coef1*Math.exp(coef2*age);
				return ageEffectRs;
		}



}
