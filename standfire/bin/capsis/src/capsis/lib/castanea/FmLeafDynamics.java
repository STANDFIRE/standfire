package capsis.lib.castanea;
//package capsis.lib.phenofit.function;
//package capsis.lib.phenofit.function.util;

import java.io.Serializable;
import jeeb.lib.util.Log;
import java.util.Collection;
import java.util.StringTokenizer;
import capsis.lib.phenofit.Fit5Phenology;
import capsis.lib.phenofit.FitlibMemory;
import capsis.lib.phenofit.FitlibPhases;
import capsis.lib.phenofit.FitlibStates;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.function.util.Chuine;
import capsis.lib.phenofit.function.util.Sigmoid;
import capsis.lib.phenofit.function.util.ThresholdInferior;



/**
 * FmLeaf : leaves of Dynaclim model.
 *
 * @author Hendrik Davi - october 2009
 */
public class FmLeafDynamics implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone ()
	// for template)

	// common phenological variable


	public int[] BBDAY; // budburst date
	public int[] BBDAY2; // budburst date of second flush
	public int[] LMAXN; // date when LAI is maximum
	public int[] LMAXN2;
	public int[] LFALL;
	public double[] LFALLH;
	public int[] LEND;
	public int[] DAYLMA;
	public double[] TSUM;
	public double[] TSUM2;
	public double[] TLMA;
	public double[] TLMA2;
	public double[] HSUMLA;
	public double[] HSUMLA2;
	public double[] HSUMLFAL;
	public double[] TSUMFALD;
	public double[] dayOfWoodStop;
	public double[] dayOfEndFall;
	public double[] dayOfBeginFall;
	public double[]  sumLstress;
	public double[] dayOfLateFrost;
	public double[] LMAXsecondFlush;

	//

	public double[] Rch;
	public double[] Sch;
	public double[] Rfr;
	public double[] Sfr;
	public double[] diffLAIFrost;




	/**
	 * Constructor for new logical FmCanopyLayer.
	 */
	public FmLeafDynamics (FmCell cell, FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];

		int nSpecies = fmSpeciesList.length;

		 BBDAY= new int[nSpecies];
		 BBDAY2= new int[nSpecies];
		 LMAXN= new int[nSpecies];
		 LMAXN2= new int[nSpecies];
		 LFALL= new int[nSpecies];
		 LFALLH= new double[nSpecies];
		 LEND= new int [nSpecies];
		 DAYLMA= new int[nSpecies];
		 TSUM= new double[nSpecies];
		 TSUM2= new double[nSpecies];
		 TLMA= new double[nSpecies];
		 TLMA2= new double[nSpecies];
		 HSUMLA= new double[nSpecies];
		 HSUMLA2= new double[nSpecies];
		 HSUMLFAL= new double[nSpecies];
		 TSUMFALD= new double[nSpecies];
		 dayOfWoodStop= new double[nSpecies];
		 dayOfEndFall= new double[nSpecies];
		 dayOfBeginFall= new double[nSpecies];
		 dayOfLateFrost= new double[nSpecies];
		 LMAXsecondFlush= new double[nSpecies];
		 diffLAIFrost = new double[nSpecies];

		 Rch= new double[nSpecies];
		 Sch= new double[nSpecies];
		 Rfr= new double[nSpecies];
		 Sfr= new double[nSpecies];

		 sumLstress= new double[nSpecies];

		// initialization species dependant !!!! need to be changes if
		// evergreen? to be improved
		int sp = 0;

		BBDAY[sp] = 0;
		BBDAY2[sp] = 0;
		LMAXN[sp] = 366;
		LMAXN2[sp] = 366;
		DAYLMA[sp] = 0;
		LFALL[sp] = 366;
		LEND[sp] = 366;
		TSUM[sp] = 0;
		HSUMLA[sp] = 0;
		HSUMLA2[sp] = 0;
		HSUMLFAL[sp] = 0;
		TLMA[sp] = 0;
		TLMA2[sp] = 0;
		HSUMLFAL[sp] = 0;
		LFALLH[sp] = 0;
		dayOfWoodStop[sp] = 366;
		dayOfEndFall[sp] = 0;
		dayOfBeginFall[sp] = 0;
		Rch[sp] = 0;
		Sch[sp] = 0;
		Rfr[sp] = 0;
		Sfr[sp] = 0;
		sumLstress[sp]=0;
		dayOfLateFrost[sp]=0;
		LMAXsecondFlush[sp]=0;
		diffLAIFrost[sp]=0;





	}

	public int[] getBBDAY () {
		return BBDAY;
	}

	public int[] getLMAXN () {
		return LMAXN;
	}

	public int[] getLFALL () {
		return LFALL;
	}

	public double[] getLFALLH () {
		return LFALLH;
	}

	public int[] getLEND () {
		return LEND;
	}

	public int[] getDAYLMA () {
		return DAYLMA;
	}

	public double[] getTSUM () {
		return TSUM;
	}

	public double[] getTLMA () {
		return TLMA;
	}

	public double[] getHSUMLA () {
		return HSUMLA;
	}

	public double[] getHSUMLFAL () {
		return HSUMLFAL;
	}

	public double[] getTSUMFALD () {
		return TSUMFALD;
	}
	public double[] getDayOfLateFrost () {
			return dayOfLateFrost;
	}

	public void setBBDAY (int[] v) {
		BBDAY = v;
	}

	public void setLMAXN (int[] v) {
		LMAXN = v;
	}

	public void setLFALL (int[] v) {
		LFALL = v;
	}

	public void setLFALLH (double[] v) {
		LFALLH = v;
	}

	public void setLEND (int[] v) {
		LEND = v;
	}

	public void setDAYLMA (int[] v) {
		DAYLMA = v;
	}

	public void setTSUM (double[] v) {
		TSUM = v;
	}

	public void setTLMA (double[] v) {
		TLMA = v;
	}

	public void setHSUMLA (double[] v) {
		HSUMLA = v;
	}

	public void setHSUMLFAL (double[] v) {
		HSUMLFAL = v;
	}

	public void setTSUMFALD (double[] v) {
		TSUMFALD = v;
	}

	public void setDayOfWoodStop (double[] v) {
			dayOfWoodStop = v;
	}
	public void setDayOfEndFall(double[] v) {
			dayOfEndFall = v;
	}
	public void setDayOfBeginFall (double[] v) {
			dayOfBeginFall = v;
	}
	public void setDayOfLateFrost (double[] v) {
				dayOfLateFrost = v;
	}


	// phenology module
	// *********************************************************************************************

	public void currentLAIdeciduous (FmCell cell, FmYearlyResults yearlyResults, FmClimateDay climateDay,
			double LHMIN, int j, FmSettings settings, FmSpecies species, int sp, int nbSpecies) throws Exception{

		// phenology procedure that calculate budburst date and leaf fall and
		// return current LAI

		double[] LOLD = cell.getCanopy ().getLAI ();
		double[] LMAX = cell.getCanopy ().getLAImax ();
		double[] L = LOLD;
		double[] LwithoutFrost=  new double[nbSpecies];
		double Lnew;
		double BSS= cell.getWood().getBiomassOfReserves()[sp];
		double LMAXwithoutFrost=cell.getCanopy ().getLAImaxBeforeFrost();
		int day=j+1; //julian day
		double coefSecondFlush=0.8;

		double Tmin = climateDay.getDailyMinTemperature();
		double Tmoy = climateDay.getDailyAverageTemperature();
		double PRI = climateDay.getDailyPrecipitation();
		double H = climateDay.getDailyLength(settings.latitude, day);
		int year= (int)cell.currentYear;

		FmCanopy canopy= cell.getCanopy();
		if (settings.phenoMode=="Unichill_Threshold") { // unichill from phenofit
		       boolean deciduous= true;
		       Fit5Phenology pheno=canopy.getPheno();
		       int sept1 = FitlibClimate.get1September(cell.currentYear);


		        if (day == sept1) {
			    pheno = new Fit5Phenology(cell.currentYear, deciduous);;
			    canopy.setPheno(pheno);
		        }


		        FitlibUnichill_Threshold(cell,climateDay, canopy, day);

		        if (BBDAY[sp]==0 && cell.currentYear> settings.initialDate) { // before budburst
		        	    if ((int)pheno.getLeafUnfoldingDate() <= 0) { // first year we use thermal time see below
				BBDAY[sp] = 0;
			    } else {
				int temp= (int)pheno.getLeafUnfoldingDate();
				BBDAY[sp] = temp;
			    }
		        }

		        if (settings.output > 1) {
		        	    Log.println("unichill1", day+";"+sept1+";"+pheno.birthYear+";"+cell.currentYear+";"+BBDAY[sp]);
		        }
		}

		if (settings.iFROST >= 1) {
			//if (BBDAY[sp] == 0) { // BEFORE budburst
			//	LMAX = cell.getCanopy ().getLAImaxBeforeFrost();
			//}
			if (Tmin < species.TminEffect && BBDAY[sp] > 0 && DAYLMA[sp] == 0) { // after budburst and before leaf complete maturation
				LMAX[sp] = LMAX[sp] * (1 + (Tmin-species.TminEffect) * settings.frostEffectCoef);
				LMAX[sp] = Math.max (0.1, LMAX[sp]); // residual LAI of 0.1
				double lateFrostNumber = yearlyResults.getLateFrostNumber (sp);
				yearlyResults.setLateFrostNumber (sp, lateFrostNumber + 1);


				// Log.println(settings.logPrefix+"DynaclimTest",
				// "effet gel newLAI= "+LMAX[sp]+" day= "+day+" Tmin= "+Tmin+" BBDAY= "+BBDAY[sp]);
				diffLAIFrost[sp]= LwithoutFrost[sp]-L[sp];
				dayOfLateFrost[sp]=day; // the last late forst of the year
			}
			if (settings.iFROST > 1 && BBDAY2[sp]>0) {
				LMAXsecondFlush[sp]= coefSecondFlush*LMAXwithoutFrost-LMAX[sp];
			}
		}

		// TSUM temperture sum before budburst
		if (BBDAY[sp] <= 0) { // BEFORE budburst

			// calculation of the budburst date
			if (cell.getDateDeb()[sp]==0) { // no budburst dates in the inventory file => CASTANEA calculate a budburst date
			        if (settings.phenoMode=="thermal time CASTANEA" || cell.currentYear== settings.initialDate) { // thermal timec cell.currentYear== cell.birthYear
						if (day >= species.NSTART && Tmoy > species.TBASEA) {
							TSUM[sp] = TSUM[sp] + Tmoy;
						}

						if (TSUM[sp] > cell.getTSUMBBcell()[sp]) {
							BBDAY[sp] = day;
						}
						//Log.println("unichill2", day+";"+BBDAY[sp]+";"+cell.currentYear+";"+settings.initialDate);

					}
			} else { // budburst date read in the inventory file
				BBDAY[sp]=cell.getDateDeb()[sp];
			}

		} else { // AFTER budburst
			// HSUMLA and TLMA temperture sum after budburst the fir for
			// leaf expansion the second for Leaf mass
			if (day > BBDAY[sp]) {
				HSUMLA[sp] = HSUMLA[sp] + Tmoy;
				TLMA[sp] = TLMA[sp] + Tmoy;
				if (TLMA[sp] >= species.HSUMLMA && DAYLMA[sp] == 0) { // day when leaf is mature
					DAYLMA[sp] = day;
				}
				// LMAXN : date where maximum LAI
											// is obtained
				if (day < LMAXN[sp] && (Math.abs (LMAX[sp] - L[sp]) < settings.eps || L[sp] > LMAX[sp])) {
					LMAXN[sp] = day;
					//LMAX[sp] = L[sp];
				}
				if (settings.iFROST >1 && day > BBDAY2[sp]) {
					if (day < LMAXN2[sp] && (Math.abs (LMAX[sp]+LMAXsecondFlush[sp] - L[sp]) < settings.eps || L[sp] > LMAX[sp]+LMAXsecondFlush[sp])) {
							LMAXN2[sp] = day;
					}
				}
				// HSUMLFAL temperature sum for leaf fall
				if (day > species.NSTART3 && Tmoy < species.TBASEC) {
					HSUMLFAL[sp] = HSUMLFAL[sp] + (species.TBASEC - Tmoy);
				}

				// LFALL: date of beginning of the leaf fall
				if (HSUMLFAL[sp] > species.TSUMLFAL && day < LFALL[sp]) {
					LFALL[sp] = day;
					LFALLH[sp] = H;
				}

				if (HSUMLFAL[sp] > cell.getWoodStopcell()[sp] && day < dayOfWoodStop[sp]) {
					dayOfWoodStop[sp] = day;
				}
				if (day > LFALL[sp] && Tmoy < 25) {
					TSUMFALD[sp] = TSUMFALD[sp] + (25 - Tmoy);
				}

				// LEND : date of end of leaf fall
				if (day > LFALL[sp] && L[sp] < 0.01 && day < LEND[sp]) LEND[sp] = day;
			}

			if (settings.iFROST >1) { // modelling the date of the second flush
				//if (dayOfLateFrost[sp]>0) {
				if (dayOfLateFrost[sp]>0 && day >= LMAXN[sp]) {
					TSUM2[sp] = TSUM2[sp] + Tmoy;
				}
				if (TSUM2[sp] > cell.getTSUMBBcell()[sp] && BBDAY2[sp]==0) {
					BBDAY2[sp] = day;
				}
				if (BBDAY2[sp]>0){
					HSUMLA2[sp] = HSUMLA2[sp] + Tmoy;
				}
			}
		}


		// CALCULATION of Current Leaf Area Index
		if (species.decidu == 1) {
			if (BBDAY[sp] == 0) {
				L[sp] = 0;
			} else {
				if (day < LFALL[sp]) {
					L[sp] = Math.min (1., HSUMLA[sp] / species.HSUMFL) * LMAX[sp];
					if (settings.iFROST >=1) {
						LwithoutFrost[sp] = Math.min (1., HSUMLA[sp] / species.HSUMFL) *LMAXwithoutFrost;
					}
					//if (settings.iFROST == 2 && day <= LMAXN2[sp] && BBDAY2[sp]>0) {
					if (settings.iFROST > 1  && BBDAY2[sp]>0) {
						L[sp] = Math.min (1., HSUMLA2[sp] / species.HSUMFL) * LMAXsecondFlush[sp]+LMAX[sp];
					}
				} else {
					if (day < LEND[sp]) {
						L[sp] = L[sp] * (Math.pow ((H - LHMIN) / (LFALLH[sp] - LHMIN), 0.4));
						if (settings.iFROST >=1) {
							LwithoutFrost[sp]= LwithoutFrost[sp] *(Math.pow ((H - LHMIN) / (LFALLH[sp] - LHMIN), 0.4));
						}
					} else {
						if (settings.iFROST > 1) {
							LMAX[sp]= LMAX[sp]+LMAXsecondFlush[sp];
						}
						L[sp] = 0;
						LwithoutFrost[sp]= 0;
					}
				}
			} // loop budburst
		} //  loop  decidu

		yearlyResults.setBudburstDate (sp, BBDAY[sp]);
		yearlyResults.setEndLeafGrowth (sp, DAYLMA[sp]);
		yearlyResults.setDayOfBeginFall (sp, LFALL[sp]);
		yearlyResults.setDayOfEndFall (sp, LEND[sp]);
		yearlyResults.setDayOfWoodStop (sp, dayOfWoodStop[sp]);

		if (settings.output>1) {
			Log.println(settings.logPrefix+"leafDynamics", day +";"+L[sp]+";"+LHMIN+";"+LFALLH[sp]+";"+H+";"+BBDAY[sp]+";"+LMAX[sp]+";"+yearlyResults.getLateFrostNumber (sp)+";"+Tmin+";"+species.TminEffect+";"+diffLAIFrost[sp]+";"+BBDAY2[sp]+";"+LMAXsecondFlush[sp]+";"+LwithoutFrost[sp]+";"+LMAXwithoutFrost+";"+LMAXN2[sp]+";"+HSUMLA2[sp]+";"+HSUMLA[sp]+";"+LEND[sp]+";"+LFALL[sp]+";"+cell.getTSUMBBcell()[sp]);
		}
		yearlyResults.setDailyLAI (sp, j, L[sp]);




		cell.getCanopy ().setLAImax (LMAX);
		L[0]=Math.max(0.1,L[0]);
		cell.getCanopy ().setLAI (L);

	}// end of METHOD


	//**********************************************************************************************

	public void currentLAIevergreen (FmCell cell, FmYearlyResults yearlyResults, FmClimateDay climateDay,
				double LHMIN, int j, FmSettings settings, FmSpecies species, int sp, int nbSpecies) throws Exception{



		int day=j+1;
		double Tmin = climateDay.getDailyMinTemperature();
		double Tmoy = climateDay.getDailyAverageTemperature();
		double PRI = climateDay.getDailyPrecipitation();
		double H = climateDay.getDailyLength(settings.latitude, day);
		double stressToLeafFall= 0.02;
		FmCanopy canopy= cell.getCanopy();
		FmWood wood= cell.getWood();


		//double [] params_LAI= settings.params_LAI;
		double Lnew=0;

		double[] LOLD = canopy.getLAI ();
		double[] LMAX = canopy.getLAImax ();
		double[] L = LOLD;
		double LMAXwithoutFrost=cell.getCanopy ().getLAImaxBeforeFrost();

		double[] LwithoutFrost=  new double[nbSpecies];

		double BSS= wood.getBiomassOfReserves ()[sp];
		//double RVM= yearlyResults.getMaintenanceRespiration(sp,day-2);

		double ConcBSS= BSS/cell.getWood ().getBiomassOfAliveWood()[sp];


		double newLAI= 0;
		//double newLMA= 0;
		//double newP= 0;
		//double newN= 0;
		double lstress_temp=0;


		FmSpecies[] fmSpeciesList = cell.getUsedFmSpecies();
		int nSpecies = fmSpeciesList.length;

		double [] Kch = new double[nSpecies];

		Kch[sp]= 0;

		int cohortesOfLeaves= (int)(species.cohortesOfLeaves);


		FmCanopyEvergreen canopyEvergreen = canopy.getCanopyEvergreen ();

		double [] lyold= new double[cohortesOfLeaves];
		double [] lmyold=  new double[cohortesOfLeaves];

		double lyp=0; // Leaf area of new shoot
		double lateFrostNumber;


		for (int k = 0; k < cohortesOfLeaves; k++) {
			lyold[k]= canopyEvergreen.getLy(k);
			lmyold[k]= canopyEvergreen.getLmy(k);
		}

		if (settings.phenoMode=="Unichill_Threshold") { // unichill from phenofit
		       boolean deciduous= false;
		       Fit5Phenology pheno=canopy.getPheno();
		       int sept1 = FitlibClimate.get1September(cell.currentYear);


		        if (day == sept1) {
			    pheno = new Fit5Phenology(cell.currentYear, deciduous);;
			    canopy.setPheno(pheno);
		        }


		        FitlibUnichill_Threshold(cell,climateDay, canopy, day);

		        if (BBDAY[sp]==0 && cell.currentYear> settings.initialDate) { // before budburst
		        	    if ((int)pheno.getLeafUnfoldingDate() <= 0) { // first year we use thermal time see below
				BBDAY[sp] = 0;
			    } else {
				int temp= (int)pheno.getLeafUnfoldingDate();
				BBDAY[sp] = temp;
			    }
		        }

		        if (settings.output > 1) {
		        	    Log.println("unichill1", day+";"+sept1+";"+pheno.birthYear+";"+cell.currentYear+";"+BBDAY[sp]);
		        }
		}

		if (settings.iFROST >= 1) {
			//if (BBDAY[sp] == 0) { // BEFORE budburst
			//	LMAX = cell.getCanopy ().getLAImaxBeforeFrost();
			//}
			if (Tmin < species.TminEffect && BBDAY[sp] > 0 && DAYLMA[sp] == 0) { // after budburst and before leaf complete maturation
				lateFrostNumber = yearlyResults.getLateFrostNumber (sp);
				yearlyResults.setLateFrostNumber (sp, lateFrostNumber + 1);


				// Log.println(settings.logPrefix+"DynaclimTest",
				// "effet gel newLAI= "+LMAX[sp]+" day= "+day+" Tmin= "+Tmin+" BBDAY= "+BBDAY[sp]);
				//diffLAIFrost[sp]= LwithoutFrost[sp]-L[sp];
				dayOfLateFrost[sp]=day; // the last late forst of the year
			}
		}



		// Calcul de TSUM : somme de T avant d?bourrement (Kramer 1994, sequential model)

		if (settings.phenoMode=="thermal time CASTANEA") {

			if(day>=species.NSTART && Tmoy>species.TBASEA) {
				TSUM[sp]= TSUM[sp]+ Tmoy;
			}
			if(TSUM[sp]>cell.getTSUMBBcell()[sp] && BBDAY[sp]==0) {
				BBDAY[sp]= day;
			}
		}


		//if (BBDAY[sp] != 0 && day>BBDAY[sp] && BSS>20) { /// 20 to be change

		if (BBDAY[sp] != 0 && day>BBDAY[sp] ) { /// 20 to be change

			HSUMLA[sp]=HSUMLA[sp]+Tmoy;

			// calcul du LAI
			if (yearlyResults.getLateFrostNumber (sp)>0) {
			        lyp=0;
			} else {
			        if (settings.fixedLAI==0) {
				        lyp= canopyEvergreen.getRatioPerCohorts(0)*LMAX[sp]*Math.min(1. , HSUMLA[sp]/ species.HSUMFL);
			        } else {
				        double newLAImax=FmCanopy.getLaiCalc (cell, species, settings,sp,false);
				        lyp= newLAImax*Math.min(1. , HSUMLA[sp]/ species.HSUMFL)*canopyEvergreen.getRatioPerCohorts(0);
			        }
			}
			canopyEvergreen.setLy(0,lyp);

		}

		// LMAXN : date o? on atteint le LAI max

		if (HSUMLA[sp]>= species.HSUMFL && LMAXN[sp]!=day+1) {
			LMAXN[sp]= day;
		}


		// somme des temperature pour fin de saison de croissance
		if (day > species.NSTART3 && Tmoy < species.TBASEC) {
			HSUMLFAL[sp]= HSUMLFAL[sp] + (species.TBASEC- Tmoy);
		}

		if (HSUMLFAL[sp] > cell.getWoodStopcell()[sp] && day < dayOfWoodStop[sp]) {
			dayOfWoodStop[sp] = day;
		}

		if ((HSUMLFAL[sp] > species.TSUMLFAL &&  day < LFALL[sp])) {
				LFALL[sp]= day;
				LFALLH[sp]= H;
		}

		// Calcul du LAI de l'annee
		if (BBDAY[sp]==day) { // changement de cohortes au debourrement


		    for (int k = cohortesOfLeaves-1; k > 0; k--) {
				    //canopyEvergreen.setLhivy(k+1, canopyEvergreen.getLy(k));
				    //canopyEvergreen.setLy(k+1, canopyEvergreen.getLhivy(k+1));
				    //lyold[k+1]= canopyEvergreen.getLhivy(k+1);

				    lyold[k]= canopyEvergreen.getLy(k-1);
				    canopyEvergreen.setLhivy(k, canopyEvergreen.getLhivy(k-1));
				    canopyEvergreen.setLy(k, canopyEvergreen.getLy(k-1));

		    }

		}



           // chute des feuilles
		for (int k = 0; k < cohortesOfLeaves; k++) {
			//double lyTemp= Math.max(0.,canopyEvergreen.getLy(k)-canopyEvergreen.getLhivy(k)*canopyEvergreen.getCoefOfLeafFall(k)*(0.00274-7.85*1e-4*Math.cos(2*Math.PI*day/365)-13.52*1e-4*Math.sin(2*Math.PI*day/365)));
			//double lyTemp= Math.max(0.,canopyEvergreen.getLy(k)-canopyEvergreen.getRatioPerCohorts(k)*LMAX[sp]*canopyEvergreen.getCoefOfLeafFall(k)*(0.00274-7.85*1e-4*Math.cos(2*Math.PI*day/365)-13.52*1e-4*Math.sin(2*Math.PI*day/365)));
			//Log.println(settings.logPrefix+"understanding", k+";"+day+";"+lyp+";"+lyTemp+";"+canopyEvergreen.getLy(k)+";"+canopyEvergreen.getRatioPerCohorts(0)*LMAX[sp]+";"+canopyEvergreen.getCoefOfLeafFall(k));

			double lyTemp= Math.max(0.,canopyEvergreen.getLy(k)-canopyEvergreen.getRatioPerCohorts(0)*LMAX[sp]*canopyEvergreen.getCoefOfLeafFall(k)*(0.00274-7.85*1e-4*Math.cos(2*Math.PI*day/365)-13.52*1e-4*Math.sin(2*Math.PI*day/365)));
			canopyEvergreen.setLy(k,lyTemp);

		}
		// new LAI after for evergreen before leaf fall
		for (int k = 0; k < cohortesOfLeaves; k++) {
				Lnew= Lnew+ canopyEvergreen.getLy(k);

		}

		L[sp]= Lnew;


		// stress effect
		double BSSth = species.TGSS * wood.getBiomassOfAliveWood()[sp];
		double RVM=0;

		if (day>1) {
			RVM=yearlyResults.getMaintenanceRespiration(sp,day-2);
		}
		if (settings.fixedLAI==3) {
			sumLstress[sp]=0;
			if (BSS-RVM<settings.thresholdLeafFall & RVM>0) {
				lstress_temp= Math.pow((RVM-BSS+settings.thresholdLeafFall)/RVM*L[sp],stressToLeafFall)/365;  // needle portion to be removed to gap filled the lack of reserves
				sumLstress[sp]= lstress_temp;
				Log.println(settings.logPrefix+"sumLstress", settings.thresholdLeafFall+";"+BSS+";"+stressToLeafFall+";"+sumLstress[sp]+";"+day+";"+lstress_temp+";"+canopyEvergreen.getLy(2)+";"+RVM+";"+L[sp]);

				if (lstress_temp<L[sp]) {
					for (int k = cohortesOfLeaves-1; k > 1; k--) {
						newLAI= Math.max(0,canopyEvergreen.getLy(k)-sumLstress[sp]);
						//Log.println(settings.logPrefix+"sumLstress", newLAI+";"+canopyEvergreen.getLy(k)+";"+thresholdLeafFall+";"+BSS+";"+stressToLeafFall+";"+sumLstress[sp]+";"+BSSth+";"+lstress_temp+";"+canopyEvergreen.getLy(2)+";"+RVM+";"+L[sp]);

						canopyEvergreen.setLy(k,newLAI);
						sumLstress[sp]= Math.max(sumLstress[sp]-newLAI,0);


					}


				} else {
					for (int k = cohortesOfLeaves-1; k > 1; k--) {
						canopyEvergreen.setLy(k,0);
					}
				}

				Lnew=0;
				// new LAI after leaf fall
				for (int k = 0; k < cohortesOfLeaves; k++) {
						Lnew= Lnew+ canopyEvergreen.getLy(k);

				}


				L[sp]= Lnew;
				//Log.println(settings.logPrefix+"sumLstress", thresholdLeafFall+";"+BSS+";"+stressToLeafFall+";"+sumLstress[sp]+";"+day+";"+lstress_temp+";"+canopyEvergreen.getLy(2)+";"+RVM+";"+L[sp]);

			}


		}



		double LMAmoy=canopy.getAverageLMA(cell, settings, species,sp);


		if (settings.output>1) {
			//Log.println(settings.logPrefix+"leafDynamics", day +";"+L[sp]+";"+LHMIN+";"+LFALLH[sp]+";"+H+";"+BBDAY[sp]+";"+LMAX[sp]);
			//Log.println(settings.logPrefix+"understanding2", cohortesOfLeaves+";"+lyp+";"+day+";"+HSUMLA[sp]+";"+species.HSUMFL+";"+canopyEvergreen.getLy(0)+";"+canopyEvergreen.getLy(1)+";"+canopyEvergreen.getLy(8)+";"+canopyEvergreen.getLy(9)+";"+canopyEvergreen.getLy(10)+";"+canopyEvergreen.getLhivy(9)+";"+canopyEvergreen.getLhivy(10));
			Log.println(settings.logPrefix+"leafDynamics", day +";"+L[sp]+";"+LHMIN+";"+LFALLH[sp]+";"+H+";"+BBDAY[sp]+";"+LMAX[sp]+";"+yearlyResults.getLateFrostNumber (sp)+";"+Tmin+";"+species.TminEffect+";"+diffLAIFrost[sp]+";"+BBDAY2[sp]+";"+LMAXsecondFlush[sp]+";"+LwithoutFrost[sp]+";"+LMAXwithoutFrost+";"+LMAXN2[sp]+";"+HSUMLA2[sp]+";"+HSUMLA[sp]+";"+LEND[sp]+";"+LFALL[sp]+";"+cell.getTSUMBBcell()[sp]);


		}

		yearlyResults.setBudburstDate (sp, BBDAY[sp]);
		yearlyResults.setEndLeafGrowth (sp, DAYLMA[sp]);
		yearlyResults.setDayOfBeginFall (sp, LFALL[sp]);
		yearlyResults.setDayOfEndFall (sp, LEND[sp]);
		yearlyResults.setDayOfWoodStop (sp, dayOfWoodStop[sp]);
		yearlyResults.setDailyLAI (sp, day-1, L[sp]);


		cell.getCanopy ().setLAI (L);

	}// end of METHOD

	//////////////////////////execute daily of FitlibUnichill_Threshold from PHENOFIT model/////////////////////////

	public void FitlibUnichill_Threshold (FmCell cell, FmClimateDay climateDay, FmCanopy canopy, int day)
		throws Exception {


	        //if (pheno.isSetDate2()) {
	         //   return 0;}
	        int year=cell.currentYear;
	        Fit5Phenology pheno= canopy.getPheno();
	        pheno.setMode(Fit5Phenology.LEAF_MODE);

	        FitlibMemory memory= pheno.leafMemory;


	        FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
	        FmSpecies species=fmSpeciesList[0];
	        double t0=species.t0;
	        double Vb= species.Vb;
	        double d= species.d;
	        double e= species.e;
	        double Ccrit= species.Ccrit;
	        double Fcrit= species.Fcrit;


	        int nbDays = FitlibClimate.getNbDays(year);
	        int sept1 = FitlibClimate.get1September(year);
	        int nbDays0 = FitlibClimate.getNbDays(year - 1);

	        int prevPhase = memory.phase;
	        double prevState1 = memory.intermediateState;
	        double prevState2 = memory.state;

	        double Tmoy= climateDay.getDailyAverageTemperature();
	        int yesterday= FitlibClimate.getYesterday(year, day);

	        int birthYear=pheno.birthYear;

	        int fit4Today = pheno.getPhenofit4Date(year, day);


	        double t0Day = (int) Math.round(t0);

	        //if (fit4Today < t0Day) {
	    //		return 0;
	      //  }


	        Sigmoid sigmoid = new Sigmoid(d, e);
	        ThresholdInferior thresholdInferior = new ThresholdInferior(Vb);

	        if (prevPhase <= 1) {

		        // Does phase 2 start ?
		        if (prevPhase == 1 && prevState1 >= 1) {

			        pheno.setDate1(year, yesterday);

			        // Start phase 2
			        memory.phase = 2;

		        } else {

			        // Process phase 1

//					// Security
//					if (pheno.getApproxPhenoAge(year, day) > 300) {
//						pheno.setDate1(year, nbDays);
//						pheno.setDate2(year, nbDays);
//						return 0;
//					}

			        memory.phase = 1;

			        double CU = thresholdInferior.execute(Tmoy) + prevState1 * Ccrit;
			        memory.intermediateState = CU / Ccrit;

		        }

	        }

	        if (memory.phase == 2 || prevPhase == 2) {

		        memory.phase = 2;

		        double FU = prevState2 * Fcrit + sigmoid.execute(Tmoy);
		        memory.state = FU /Fcrit;

		        // Set date2
		        if (memory.state >= 1) {
			        double FU1 = prevState2 * Fcrit;
			        double Y1 = FU - FU1; // statesVal = prevState
			        double Y2 = Fcrit - FU1; // statesVal = prevState
			        if (Y2 < 0) {
				        pheno.setDate2(year, yesterday);
			        } else if (Y2 != Y1 && Y1 != 0) {
				        pheno.setDate2(year, yesterday + (Y2 / Y1));
			        } else {
				        pheno.setDate2(year, day);
			        }

//					// Date2 is set, reset the memory
//					memory.resetDevelopmentStates ();

		        }
	        }

	        Log.println("unichill", Tmoy+";"+t0+";"+Vb+";"+d+";"+e+";"+Ccrit+";"+Fcrit+";"+year+";"+day+";"+yesterday+";"+fit4Today+";"+t0Day+";"+memory.state+";"+memory.intermediateState+";"+pheno.getDate1()+";"+pheno.getDate2()+";"+memory.phase+";"+prevState1+";"+prevState2+";"+pheno.birthYear);

	    if (pheno.getDate2()>0) {
	    	pheno.setLeafUnfoldingDate(pheno.getDate2());
	    }else{
	    	pheno.setLeafUnfoldingDate(0);
	    }

	} // end of FitlibUnichill_Threshold


} // end of class
