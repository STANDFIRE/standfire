package capsis.lib.castanea;

import java.io.Serializable;
import java.util.Collection;
import jeeb.lib.util.Log;
import java.util.Collection;

/**
 * FmCanopyLayer : a layer of leaves of Dynaclim model.
 *
 * @author Hendrik Davi - July 2010
 */
// class for stogking the daily results from increment hourly data

public class FmYearlyResults implements Serializable, Cloneable {

	// Daily variables
	private double[][] canopyPhotosynthesis;
	private double[][] canopyDelta13C;
	private double[][] canopyRespiration;
	private double[][] canopyConductance;
	private double[][] canopyTranspiration;
	private double[][] maxHourlyTranspiration;

	private double[][] canopyEvapoTranspiration;
	private double[][] canopyPotentialEvaporation;
	private double[][] woodRespiration;
	private double[][] fineRootsRespiration;
	private double[][] coarseRootsRespiration;
	private double[][] growthRespiration;
	private double[][] leafGrowthRespiration;
	private double[][] maintenanceRespiration;
	private double[][] respiration;
	private double[][] biomassOfReserves;

	private double[][] leafGrowth;
	private double[][] dailyLAI;
	private double[][] woodGrowth;
	private double[][] coarseRootsGrowth;
	private double[][] fineRootsGrowth;
	private double[][] reservesGrowth;
	private double[][] fineRootsMortality;
	private double[][] reservesMortality;
	private double[][] potleafmin;
	private double[][] BFold;
	private double[][] MBF;
	private double[][] MBBV;
	private double[][] MBRG;


	private double[] soilEvaporation;
	private double[] heterotrophicRespiration;
	private double[] drainage;
	private double[] REW;
	private double[] potsoil;
	private double[] stomatalControl;
	private double[] rsol;
	private double[] stressLevel;

	private double[] tmoy;
	private double[] tmin;
	private double[] tmax;
	private double[] globalRadiation;
	private double[] rain;
	// end-of-Daily variables

	// private double [][] biomassOfLeaves;
	private double[] lateFrostNumber;
	private double[] budburstDate;
	private double[] dayOfWoodStop;
	private double[] endLeafGrowth;
	private double[] dayOfEndFall;
	private double[] dayOfBeginFall;

	private double[] yearlyCanopyPhotosynthesis;
	private double[] yearlyCanopyDelta13C;
	private double[] yearlyCanopyRespiration;
	private double[] yearlyCanopyConductance;
	private double[] yearlyCanopyTranspiration;
	private double[] yearlyCanopyEvapoTranspiration;
	private double[] yearlyCanopyPotentialEvaporation;
	private double[] yearlyWoodRespiration;
	private double[] yearlyFineRootsRespiration;
	private double[] yearlyCoarseRootsRespiration;
	private double[] yearlyHeterotrophicRespiration;

	private double[] yearlyGrowthRespiration;
	private double[] yearlyMaintenanceRespiration;
	private double[] yearlyRespiration;

	private double[] yearlyLeafGrowth;
	private double[] yearlyWoodGrowth;
	private double[] yearlyCoarseRootsGrowth;
	private double[] yearlyFineRootsGrowth;
	private double[] yearlyReservesGrowth;

	private double[] ringWidth;
	// private double [] dbh;
	// private double [] height;
	private double[] seedProduction;
	private double[] ksmax;
	private double[] leafBiomass;

	private double[] yearlyPotLeafMin;

	private double yearlySoilEvaporation;
	private double yearlyDrainage;
	private double yearlyStressLevel;
	private double yearlyTmoy;
	private double yearlyTmin;
	private double yearlyTmax;
	private double yearlyRg;
	private double yearlyPRI;

	private int dayWithPhoto;

	// int annee;
	// int IDtree;
	private int yclimate = 0;
	private int Ndays = 366;
	private int Nyears=0;

	/**
	 * Constructor for new logical FmYearlyResults.
	 */

	public FmYearlyResults(FmCell cell, FmSettings settings, int nunberOfpecies) {

		// FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		// int nSpecies = fmSpeciesList.length;

		int IDtree = cell.getID();

		int nSpecies = nunberOfpecies;

		int dayNumber = this.Ndays;
		int annee = this.yclimate;

		// fc+hd-15.10.2014
		createDailyVariables (nSpecies, 366);

		yearlyCanopyPhotosynthesis = new double[nSpecies];
		yearlyCanopyDelta13C = new double[nSpecies];
		yearlyCanopyRespiration = new double[nSpecies];
		yearlyCanopyConductance = new double[nSpecies];
		yearlyCanopyTranspiration = new double[nSpecies];
		yearlyCanopyEvapoTranspiration = new double[nSpecies];
		yearlyCanopyPotentialEvaporation = new double[nSpecies];
		yearlyWoodRespiration = new double[nSpecies];
		yearlyFineRootsRespiration = new double[nSpecies];
		yearlyCoarseRootsRespiration = new double[nSpecies];
		yearlyHeterotrophicRespiration = new double[nSpecies];


		yearlyMaintenanceRespiration = new double[nSpecies];
		yearlyGrowthRespiration = new double[nSpecies];
		yearlyRespiration = new double[nSpecies];

		yearlyLeafGrowth = new double[nSpecies];
		yearlyWoodGrowth = new double[nSpecies];
		yearlyCoarseRootsGrowth = new double[nSpecies];
		yearlyFineRootsGrowth = new double[nSpecies];
		yearlyReservesGrowth = new double[nSpecies];
		yearlyPotLeafMin = new double[nSpecies];

		lateFrostNumber = new double[nSpecies];
		budburstDate = new double[nSpecies];
		dayOfWoodStop = new double[nSpecies];
		endLeafGrowth = new double[nSpecies];
		dayOfEndFall = new double[nSpecies];
		dayOfBeginFall = new double[nSpecies];
		ringWidth = new double[nSpecies];
		// dbh= new double [nSpecies];
		// height= new double [nSpecies];
		seedProduction = new double[nSpecies];
		leafBiomass= new double[nSpecies];

		ksmax = new double[nSpecies];

	}

	public void init(int nunberOfpecies, FmCell cell) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];


		int nSpecies = nunberOfpecies;

		int dayNumber = this.Ndays;
		int annee = this.yclimate;

		// fc+hd-15.10.2014 check if the first daily variable is null, recreate them
		if (canopyPhotosynthesis == null) createDailyVariables (nSpecies, 366);

		yearlySoilEvaporation = 0;
		yearlyDrainage = 0;
		yearlyStressLevel = 0;
		yearlyTmoy = 0;
		yearlyTmin = 0;
		yearlyTmax = 0;
		yearlyRg = 0;
		yearlyPRI = 0;
		dayWithPhoto= 0;

		int sp = 0;

		yearlyCanopyPhotosynthesis[sp] = 0;
		yearlyCanopyDelta13C[sp] = 0;
		yearlyCanopyRespiration[sp] = 0;
		yearlyCanopyConductance[sp] = 0;
		yearlyCanopyTranspiration[sp] = 0;
		yearlyCanopyEvapoTranspiration[sp] = 0;
		yearlyCanopyPotentialEvaporation[sp] = 0;
		yearlyWoodRespiration[sp] = 0;
		yearlyFineRootsRespiration[sp] = 0;
		yearlyCoarseRootsRespiration[sp] = 0;
		yearlyHeterotrophicRespiration[sp] = 0;

		yearlyMaintenanceRespiration[sp] = 0;
		yearlyGrowthRespiration[sp] = 0;
		yearlyRespiration[sp] = 0;

		yearlyLeafGrowth[sp] = 0;
		yearlyWoodGrowth[sp] = 0;
		yearlyCoarseRootsGrowth[sp] = 0;
		yearlyFineRootsGrowth[sp] = 0;
		yearlyReservesGrowth[sp] = 0;
		yearlyPotLeafMin[sp] = 0;

		lateFrostNumber[sp] = 0;
		budburstDate[sp] = 0;
		dayOfWoodStop[sp] = 0;
		dayOfEndFall[sp] = 0;
		dayOfBeginFall[sp] = 0;
		endLeafGrowth[sp] = 0;
		leafBiomass[sp] = 0;
		ksmax[sp] = 0;

		for (int i = 0; i < dayNumber; i++) {
			canopyPhotosynthesis[sp][i] = 0;
			canopyDelta13C[sp][i] = 0;
			canopyRespiration[sp][i] = 0;
			canopyConductance[sp][i] = 0;
			canopyTranspiration[sp][i] = 0;
			maxHourlyTranspiration[sp][i] = 0;

			canopyEvapoTranspiration[sp][i] = 0;
			canopyPotentialEvaporation[sp][i] = 0;
			woodRespiration[sp][i] = 0;
			fineRootsRespiration[sp][i] = 0;
			coarseRootsRespiration[sp][i] = 0;
			potleafmin[sp][i] = 0;
			soilEvaporation[i] = 0;

			maintenanceRespiration[sp][i] = 0;
			growthRespiration[sp][i] = 0;
			respiration[sp][i] = 0;
			leafGrowthRespiration[sp][i] = 0;

			leafGrowth[sp][i] = 0;
			woodGrowth[sp][i] = 0;
			coarseRootsGrowth[sp][i] = 0;
			fineRootsGrowth[sp][i] = 0;
			reservesGrowth[sp][i] = 0;
			fineRootsMortality[sp][i] = 0;
			reservesMortality[sp][i] = 0;
			biomassOfReserves[sp][i] = 0;
			BFold[sp][i] = 0;
			MBF[sp][i] = 0;
			MBBV[sp][i] = 0;

			if (species.decidu == 1) { // all decidous species without dormancy
				dailyLAI[sp][i] = 0;
			}
		}


		for (int i = 0; i < dayNumber; i++) {
			soilEvaporation[i] = 0;
			heterotrophicRespiration[i] = 0;
			REW[i] = 0;
			potsoil[i] = 0;
			stomatalControl[i] = 0;
			rsol[i] = 0;
			stressLevel[i] = 0;
			tmoy[i] = 0;
			tmax[i] = 0;
			tmin[i] = 0;
			globalRadiation[i] = 0;
			rain[i] = 0;
		}

	} // end of init

	/**
	 * Related to forgetDailyVariables.
	 */
	private void createDailyVariables (int nSpecies, int dayNumber) { // fc+hd-15.10.2014

		canopyPhotosynthesis = new double[nSpecies][dayNumber]; // here in gC
		canopyDelta13C = new double[nSpecies][dayNumber];
		canopyRespiration = new double[nSpecies][dayNumber]; // here in gC
		canopyConductance = new double[nSpecies][dayNumber]; // here in gC
		canopyTranspiration = new double[nSpecies][dayNumber];
		maxHourlyTranspiration = new double[nSpecies][dayNumber];

		canopyEvapoTranspiration = new double[nSpecies][dayNumber];
		canopyPotentialEvaporation = new double[nSpecies][dayNumber];
		woodRespiration = new double[nSpecies][dayNumber];
		fineRootsRespiration = new double[nSpecies][dayNumber];
		coarseRootsRespiration = new double[nSpecies][dayNumber];

		growthRespiration = new double[nSpecies][dayNumber];
		leafGrowthRespiration = new double[nSpecies][dayNumber];
		maintenanceRespiration = new double[nSpecies][dayNumber];
		respiration = new double[nSpecies][dayNumber];
		biomassOfReserves = new double[nSpecies][dayNumber];

		leafGrowth = new double[nSpecies][dayNumber];
		dailyLAI = new double[nSpecies][dayNumber];
		woodGrowth = new double[nSpecies][dayNumber];
		coarseRootsGrowth = new double[nSpecies][dayNumber];
		fineRootsGrowth = new double[nSpecies][dayNumber];
		reservesGrowth = new double[nSpecies][dayNumber];
		fineRootsMortality = new double[nSpecies][dayNumber];
		reservesMortality = new double[nSpecies][dayNumber];
		potleafmin = new double[nSpecies][dayNumber];
		BFold= new double[nSpecies][dayNumber];
		MBF= new double[nSpecies][dayNumber];
		MBBV= new double[nSpecies][dayNumber];
		MBRG= new double[nSpecies][dayNumber];

		soilEvaporation = new double[dayNumber];
		heterotrophicRespiration= new double[dayNumber];
		drainage = new double[dayNumber];
		REW = new double[dayNumber];
		potsoil = new double[dayNumber];

		stomatalControl = new double[dayNumber];
		rsol = new double[dayNumber];
		stressLevel = new double[dayNumber];

		tmoy = new double[dayNumber];
		tmin = new double[dayNumber];
		tmax = new double[dayNumber];
		globalRadiation = new double[dayNumber];
		rain = new double[dayNumber];

	}

	/**
	 * In order to save memory, possible to optionnally set all daily arrays to
	 * null.
	 */
	public void forgetDailyVariables() { // fc+hd-15.10.2014

		canopyPhotosynthesis = null;
		canopyDelta13C = null;
		canopyRespiration = null;
		canopyConductance = null;
		canopyTranspiration = null;
		maxHourlyTranspiration = null;
		canopyEvapoTranspiration = null;
		canopyPotentialEvaporation = null;
		woodRespiration = null;
		fineRootsRespiration = null;
		coarseRootsRespiration = null;

		growthRespiration = null;
		leafGrowthRespiration = null;
		maintenanceRespiration = null;
		respiration = null;
		biomassOfReserves = null;

		leafGrowth = null;
		dailyLAI = null;
		woodGrowth = null;
		coarseRootsGrowth = null;
		fineRootsGrowth = null;
		reservesGrowth = null;
		fineRootsMortality = null;
		reservesMortality = null;
		potleafmin = null;

		soilEvaporation = null;
		heterotrophicRespiration = null;

		drainage = null;
		REW = null;
		potsoil = null;

		stomatalControl = null;
		rsol = null;
		stressLevel = null;

		tmoy = null;
		tmax = null;
		tmin = null;
		globalRadiation = null;
		rain = null;

	}

	public void setCanopyPhotosynthesis(int sp, int i, double v) {
		canopyPhotosynthesis[sp][i] = v;
	}

	public void setCanopyDelta13C(int sp, int i, double v) {
		canopyDelta13C[sp][i] = v;
	}

	public void setCanopyRespiration(int sp, int i, double v) {
		canopyRespiration[sp][i] = v;
	}

	public void setCanopyConductance(int sp, int i, double v) {
		canopyConductance[sp][i] = v;
	}

	public void setCanopyTranspiration(int sp, int i, double v) {
		canopyTranspiration[sp][i] = v;
	}

	public void setMaxHourlyTranspiration(int sp, int i, double v) {
		maxHourlyTranspiration[sp][i] = v;
	}
	public void setCanopyEvapoTranspiration(int sp, int i, double v) {
		canopyEvapoTranspiration[sp][i] = v;
	}

	public void setCanopyPotentialEvaporation(int sp, int i, double v) {
		canopyPotentialEvaporation[sp][i] = v;
	}

	public void setFineRootsRespiration(int sp, int i, double v) {
		fineRootsRespiration[sp][i] = v;
	}

	public void setWoodRespiration(int sp, int i, double v) {
		woodRespiration[sp][i] = v;
	}

	public void setCoarseRootsRespiration(int sp, int i, double v) {
		coarseRootsRespiration[sp][i] = v;
	}

	public void setMaintenanceRespiration(int sp, int i, double v) {
		maintenanceRespiration[sp][i] = v;
	}

	public void setGrowthRespiration(int sp, int i, double v) {
		growthRespiration[sp][i] = v;
	}

	public void setRespiration(int sp, int i, double v) {
		respiration[sp][i] = v;
	}

	public void setLeafGrowthRespiration(int sp, int i, double v) {
		leafGrowthRespiration[sp][i] = v;
	}

	public void setDailyLAI(int sp, int i, double v) {
		dailyLAI[sp][i] = v;
	}

	public void setLeafGrowth(int sp, int i, double v) {
		leafGrowth[sp][i] = v;
	}

	public void setWoodGrowth(int sp, int i, double v) {
		woodGrowth[sp][i] = v;
	}

	public void setFineRootsGrowth(int sp, int i, double v) {
		fineRootsGrowth[sp][i] = v;
	}

	public void setReservesGrowth(int sp, int i, double v) {
		reservesGrowth[sp][i] = v;
	}

	public void setCoarseRootsGrowth(int sp, int i, double v) {
		coarseRootsGrowth[sp][i] = v;
	}

	public void setReservesMortality(int sp, int i, double v) {
		reservesMortality[sp][i] = v;
	}

	public void setFineRootsMortality(int sp, int i, double v) {
		fineRootsMortality[sp][i] = v;
	}

	public void setPotleafmin(int sp, int i, double v) {
		potleafmin[sp][i] = v;
	};

	public void setBiomassOfReserves(int sp, int i, double v) {
		biomassOfReserves[sp][i] = v;
	}
	public void setBFold(int sp, int i, double v) {
			BFold[sp][i] = v;
		}
	public void setMBF(int sp, int i, double v) {
		MBF[sp][i] = v;
	}
	public void setMBBV(int sp, int i, double v) {
		MBBV[sp][i] = v;
	}
	public void setMBRG(int sp, int i, double v) {
		MBRG[sp][i] = v;
	}

	public void setSoilEvaporation(int i, double v) {
		soilEvaporation[i] = v;
	}
	public void setHeterotrophicRespiration(int i, double v) {
		heterotrophicRespiration[i] = v;
	}
	public void setDrainage(int i, double v) {
		drainage[i] = v;
	}

	public void setREW(int i, double v) {
		REW[i] = v;
	}
	public void setPotsoil(int i, double v) {
		potsoil[i] = v;
	}

	public void setRsol(int i, double v) {
		rsol[i] = v;
	}

	public void setStomatalControl(int i, double v) {
		stomatalControl[i] = v;
	}

	public void setStressLevel(int i, double v) {
		stressLevel[i] = v;
	}

	public void setTmoy(int i, double v) {
		tmoy[i] = v;
	}

	public void setTmin(int i, double v) {
		tmin[i] = v;
	}

	public void setTmax(int i, double v) {
		tmax[i] = v;
	}

	public void setGlobalRadiation(int i, double v) {
		globalRadiation[i] = v;
	}

	public void setRain(int i, double v) {
		rain[i] = v;
	}

	public void setYearlyCanopyPhotosynthesis(int sp, double v) {
		yearlyCanopyPhotosynthesis[sp] = v;
	};

	public void setYearlyCanopyDelta13C(int sp, double v) {
		yearlyCanopyDelta13C[sp] = v;
	};

	public void setYearlyCanopyRespiration(int sp, double v) {
		yearlyCanopyRespiration[sp] = v;
	};

	public void setYearlyCanopyConductance(int sp, double v) {
		yearlyCanopyConductance[sp] = v;
	};

	public void setYearlyCanopyTranspiration(int sp, double v) {
		yearlyCanopyTranspiration[sp] = v;
	};

	public void setYearlyCanopyEvapoTranspiration(int sp, double v) {
		yearlyCanopyEvapoTranspiration[sp] = v;
	};

	public void setYearlyCanopyPotentialEvaporation(int sp, double v) {
		yearlyCanopyPotentialEvaporation[sp] = v;
	};

	public void setYearlyWoodRespiration(int sp, double v) {
		yearlyWoodRespiration[sp] = v;
	};

	public void setYearlyFineRootsRespiration(int sp, double v) {
		yearlyFineRootsRespiration[sp] = v;
	};

	public void setYearlyCoarseRootsRespiration(int sp, double v) {
		yearlyCoarseRootsRespiration[sp] = v;
	};
	public void setYearlyHeterotrophicRespiration(int sp, double v) {
		yearlyHeterotrophicRespiration[sp] = v;
	};
	public void setYearlyMaintenanceRespiration(int sp, double v) {
		yearlyMaintenanceRespiration[sp] = v;
	};

	public void setYearlyGrowthRespiration(int sp, double v) {
		yearlyGrowthRespiration[sp] = v;
	};

	public void setYearlyRespiration(int sp, double v) {
		yearlyRespiration[sp] = v;
	};

	public void setKsmax(int sp, double v) {
		ksmax[sp] = v;
	};
	public void setLeafBiomass(int sp, double v) {
		leafBiomass[sp] = v;
	};

	public void setYearlyPotLeafMin(int sp, double v) {
		yearlyPotLeafMin[sp] = v;
	};

	public void setYearlyLeafGrowth(int sp, double v) {
		yearlyLeafGrowth[sp] = v;
	};

	public void setYearlyWoodGrowth(int sp, double v) {
		yearlyWoodGrowth[sp] = v;
	};

	public void setYearlyCoarseRootsGrowth(int sp, double v) {
		yearlyCoarseRootsGrowth[sp] = v;
	};

	public void setYearlyFineRootsGrowth(int sp, double v) {
		yearlyFineRootsGrowth[sp] = v;
	};

	public void setYearlyReservesGrowth(int sp, double v) {
		yearlyReservesGrowth[sp] = v;
	};

	public void setYearlySoilEvaporation(double v) {
		yearlySoilEvaporation = v;
	};

	public void setYearlyDrainage(double v) {
		yearlyDrainage = v;
	};

	public void setYearlyStressLevel(double v) {
		yearlyStressLevel = v;
	};

	public void setYearlyTmoy(double v) {
		yearlyTmoy = v;
	};

	public void setYearlyTmin(double v) {
		yearlyTmin = v;
	};

	public void setYearlyTmax(double v) {
		yearlyTmax = v;
	};

	public void setYearlyRg(double v) {
		yearlyRg = v;
	};

	public void setYearlyPRI(double v) {
		yearlyPRI = v;
	};

	public void setLateFrostNumber(int sp, double v) {
		lateFrostNumber[sp] = v;
	}

	public void setBudburstDate(int sp, double v) {
		budburstDate[sp] = v;
	}

	public void setDayOfWoodStop(int sp, double v) {
		dayOfWoodStop[sp] = v;
	}

	public void setDayOfEndFall(int sp, double v) {
		dayOfEndFall[sp] = v;
	}

	public void setDayOfBeginFall(int sp, double v) {
		dayOfBeginFall[sp] = v;
	}

	public void setEndLeafGrowth(int sp, double v) {
		endLeafGrowth[sp] = v;
	}

	public void setRingWidth(int sp, double v) {
		ringWidth[sp] = v;
	}

	public void setNyears(int v) {
			Nyears = v;
	}

	// public void setDbh(int sp, double v) {dbh[sp]=v;}
	// public void setHeight(int sp, double v) {height[sp]=v;}

	public void setSeedProduction(int sp, double v) {
		seedProduction[sp] = v;
	}

	public void setYclimate(int v) {
		yclimate = v;
	}

	public void setNdays(int v) {
		Ndays = v;
	}
	public void setDayWithPhoto(int v) {
		Ndays = v;
	}
	public double getCanopyPhotosynthesis(int sp, int i) {
		return canopyPhotosynthesis[sp][i];
	}

	public double getCanopyDelta13C(int sp, int i) {
		return canopyDelta13C[sp][i];
	}

	public double getCanopyRespiration(int sp, int i) {
		return canopyRespiration[sp][i];
	}

	public double getCanopyConductance(int sp, int i) {
		return canopyConductance[sp][i];
	}

	public double getCanopyTranspiration(int sp, int i) {
		return canopyTranspiration[sp][i];
	}
	public double getMaxHourlyTranspiration(int sp, int i) {
		return maxHourlyTranspiration[sp][i];
	}
	public double getCanopyEvapoTranspiration(int sp, int i) {
		return canopyEvapoTranspiration[sp][i];
	}

	public double getCanopyPotentialEvaporation(int sp, int i) {
		return canopyPotentialEvaporation[sp][i];
	}

	public double getWoodRespiration(int sp, int i) {
		return woodRespiration[sp][i];
	}

	public double getFineRootsRespiration(int sp, int i) {
		return fineRootsRespiration[sp][i];
	}

	public double getCoarseRootsRespiration(int sp, int i) {
		return coarseRootsRespiration[sp][i];
	}

	public double getMaintenanceRespiration(int sp, int i) {
		return maintenanceRespiration[sp][i];
	}

	public double getGrowthRespiration(int sp, int i) {
		return growthRespiration[sp][i];
	}

	public double getRespiration(int sp, int i) {
		return respiration[sp][i];
	}

	public double getLeafGrowthRespiration(int sp, int i) {
		return leafGrowthRespiration[sp][i];
	}

	public double getBiomassOfReserves(int sp, int i) {
		return biomassOfReserves[sp][i];
	}

	public double getDailyLAI(int sp, int i) {
		return dailyLAI[sp][i];
	}

	public double getLeafGrowth(int sp, int i) {
		return leafGrowth[sp][i];
	}

	public double getWoodGrowth(int sp, int i) {
		return woodGrowth[sp][i];
	}

	public double getFineRootsGrowth(int sp, int i) {
		return fineRootsGrowth[sp][i];
	}

	public double getCoarseRootsGrowth(int sp, int i) {
		return coarseRootsGrowth[sp][i];
	}

	public double getReservesGrowth(int sp, int i) {
		return reservesGrowth[sp][i];
	}
	public double getBFold(int sp, int i) {
		return BFold[sp][i];
	}
	public double getMBF(int sp, int i) {
		return MBF[sp][i];
	}
	public double getMBRG(int sp, int i) {
		return MBRG[sp][i];
	}
	public double getMBBV(int sp, int i) {
		return MBBV[sp][i];
	}
	public double getReservesMortality(int sp, int i) {
		return reservesMortality[sp][i];
	}

	public double getFineRootsMortality(int sp, int i) {
		return fineRootsMortality[sp][i];
	}

	public double getPotleafmin(int sp, int i) {
		return potleafmin[sp][i];
	}

	public double getSoilEvaporation(int i) {
		return soilEvaporation[i];
	}
	public double getHeterotrophicRespiration(int i) {
		return heterotrophicRespiration[i];
	}

	public double getREW(int i) {
		return REW[i];
	}
	public double getPotsoil(int i) {
		return potsoil[i];
	}

	public double getRsol(int i) {
		return rsol[i];
	}

	public double getStomatalControl(int i) {
		return stomatalControl[i];
	}

	public double getStressLevel(int i) {
		return stressLevel[i];
	}

	public double getTmoy(int i) {
		return tmoy[i];
	}

	public double getTmin(int i) {
		return tmin[i];
	}

	public double getTmax(int i) {
		return tmax[i];
	}

	public double getGlobalRadiation(int i) {
		return globalRadiation[i];
	}

	public double getRain(int i) {
		return rain[i];
	}

	public double getYearlyCanopyPhotosynthesis(int sp) {
		return yearlyCanopyPhotosynthesis[sp];
	}

	public double getYearlyCanopyDelta13C(int sp) {
		return yearlyCanopyDelta13C[sp];
	}

	public double getYearlyCanopyRespiration(int sp) {
		return yearlyCanopyRespiration[sp];
	}

	public double getYearlyCanopyConductance(int sp) {
		return yearlyCanopyConductance[sp];
	}

	public double getYearlyCanopyTranspiration(int sp) {
		return yearlyCanopyTranspiration[sp];
	}

	public double getYearlyCanopyEvapoTranspiration(int sp) {
		return yearlyCanopyEvapoTranspiration[sp];
	}

	public double getYearlyCanopyPotentialEvaporation(int sp) {
		return yearlyCanopyPotentialEvaporation[sp];
	}

	public double getYearlyWoodRespiration(int sp) {
		return yearlyWoodRespiration[sp];
	}

	public double getYearlyFineRootsRespiration(int sp) {
		return yearlyFineRootsRespiration[sp];
	}

	public double getYearlyCoarseRootsRespiration(int sp) {
		return yearlyCoarseRootsRespiration[sp];
	}
	public double getYearlyHeterotrophicRespiration(int sp) {
		return yearlyHeterotrophicRespiration[sp];
	}

	public double getYearlyMaintenanceRespiration(int sp) {
		return yearlyMaintenanceRespiration[sp];
	}

	public double getYearlyGrowthRespiration(int sp) {
		return yearlyGrowthRespiration[sp];
	}

	public double getYearlyRespiration(int sp) {
		return yearlyRespiration[sp];
	}

	public double getYearlyLeafGrowth(int sp) {
		return yearlyLeafGrowth[sp];
	}

	public double getYearlyWoodGrowth(int sp) {
		return yearlyWoodGrowth[sp];
	}

	public double getYearlyFineRootsGrowth(int sp) {
		return yearlyFineRootsGrowth[sp];
	}

	public double getYearlyCoarseRootsGrowth(int sp) {
		return yearlyCoarseRootsGrowth[sp];
	}

	public double getKsmax(int sp) {
		return ksmax[sp];
	}

	public double getYearlyPotLeafMin(int sp) {
		return yearlyPotLeafMin[sp];
	}

	public double getYearlySoilEvaporation() {
		return yearlySoilEvaporation;
	}

	public double getYearlyDrainage() {
		return yearlyDrainage;
	}

	public double getYearlyStressLevel() {
		return yearlyStressLevel;
	}

	public double getYearlyTmoy() {
		return yearlyTmoy;
	}

	public double getYearlyTmax() {
		return yearlyTmax;
	}

	public double getYearlyTmin() {
		return yearlyTmin;
	}

	public double getYearlyRg() {
		return yearlyRg;
	}

	public double getYearlyPRI() {
		return yearlyPRI;
	}

	public int getNdays() {
		return Ndays;
	}

	public int getYclimate() {
		return yclimate;
	}

	public double getLateFrostNumber(int sp) {
		return lateFrostNumber[sp];
	}

	public double getBudburstDate(int sp) {
		return budburstDate[sp];
	}

	public double getEndLeafGrowth(int sp) {
		return endLeafGrowth[sp];
	}

	public double getDayOfWoodStop(int sp) {
		return dayOfWoodStop[sp];
	}

	public double getDayOfEndFall(int sp) {
		return dayOfEndFall[sp];
	}

	public double getDayOfBeginFall(int sp) {
		return dayOfBeginFall[sp];
	}

	public double getRingWidth(int sp) {
		return ringWidth[sp];
	}
	public double getLeafBiomass(int sp) {
		return leafBiomass[sp];
	}
	public double getNyears(int Nyears) {
			return Nyears;
	}
	public double getDayWithPhoto(int Nyears) {
			return dayWithPhoto;
	}

	// public double getDbh(int sp) {return dbh[sp];}
	// public double getHeight(int sp) {return height[sp];}
	public double getSeedProduction(int sp) {
		return seedProduction[sp];
	}

	public void fill(FmCell cell, FmDailyResults dailyResults, int j, FmSettings settings, FmClimateDay climateDay) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];


		double conversion = 3.6 * 12 / 1000.; // conversion to gc

		// double [][] canopyPhotosynthesis= this.canopyPhotosynthesis;
		// double [][] canopyRespiration= this.canopyRespiration;
		// double [][] canopyConductance= this.canopyConductance;
		// double [][] canopyTranspiration= this.canopyTranspiration;
		// double [][] canopyEvapoTranspiration= this.canopyEvapoTranspiration;
		// double [][] canopyPotentialEvaporation=
		// this.canopyPotentialEvaporation;
		// double [] soilEvaporation= this.soilEvaporation;
		// double [][] woodRespiration= this.woodRespiration;
		// double [][] fineRootsRespiration= this.fineRootsRespiration;
		// double [][] coarseRootsRespiration= this.coarseRootsRespiration;
		//
		// FmWood fmWood= cell.getWood();
		// FmSoil soil= cell.getSoil();

		int sp = 0;
		this.setCanopyPhotosynthesis(sp, j, dailyResults.getCanopyPhotosynthesisSp(sp) * conversion);

		double canopyDelta13Cint = 0;

		this.setSoilEvaporation(j, dailyResults.getSoilEvaporation());
		// this.setDrainage(day,soil.getDrainage());
		this.setTmoy(j, climateDay.getDailyAverageTemperature());
		this.setTmin(j, climateDay.getDailyMinTemperature());
		this.setTmax(j, climateDay.getDailyMaxTemperature());
		this.setGlobalRadiation(j, climateDay.getDailyGlobalRadiation());
		this.setRain(j, climateDay.getDailyPrecipitation());

		if (getCanopyPhotosynthesis(sp, j) > 0) {
			canopyDelta13Cint = dailyResults.getDeltasum13CSp(sp) / getCanopyPhotosynthesis(sp, j);
		}

		this.setCanopyDelta13C(sp, j, canopyDelta13Cint);
		this.setCanopyRespiration(sp, j, dailyResults.getCanopyRespirationSp(sp) * conversion);
		this.setCanopyConductance(sp, j, dailyResults.getCanopyConductanceSp(sp) * conversion);
		this.setCanopyTranspiration(sp, j, dailyResults.getCanopyTranspirationSp(sp));
		this.setMaxHourlyTranspiration(sp, j, dailyResults.getMaxHourlyTranspirationSp(sp));

		this.setCanopyEvapoTranspiration(sp, j, dailyResults.getCanopyEvapoTranspirationSp(sp));
		this.setCanopyPotentialEvaporation(sp, j, dailyResults.getCanopyPotentialEvaporationSp(sp));
		this.setWoodRespiration(sp, j, dailyResults.getWoodRespirationSp(sp));
		this.setFineRootsRespiration(sp, j, dailyResults.getFineRootsRespirationSp(sp));
		this.setCoarseRootsRespiration(sp, j, dailyResults.getCoarseRootsRespirationSp(sp));
		this.setMaintenanceRespiration(sp, j, dailyResults.getMaintenanceRespirationSp(sp));
		this.setRespiration(sp, j,dailyResults.getMaintenanceRespirationSp(sp) + dailyResults.getGrowthRespirationSp(sp));
		this.setGrowthRespiration(sp, j, dailyResults.getGrowthRespirationSp(sp));
		this.setLeafGrowthRespiration(sp, j, dailyResults.getLeafGrowthRespirationSp(sp));
		// this.setPotleafmin(sp, j, v)

		// this.setBiomassOfreserves(sp,day,fmWood.getBiomassOfReserves(sp));

	}

	public void sumVariables(FmCell cell, FmClimateDay climateDay, int dayNumber, FmSettings settings, int i) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;

		int sp = 0;
		int day = i+1;
		int month=climateDay.getMonth();
		int thresholdPhoto = 8; // to calculate delat13C
		FmSoil soil = cell.getSoil();
		sp=0;
		FmSpecies species=fmSpeciesList[0];

		yearlyCanopyPhotosynthesis[sp] = yearlyCanopyPhotosynthesis[sp] + getCanopyPhotosynthesis(sp, i);

		// Log.println(settings.logPrefix+"dailyResults", "endleaf= " +
		// getEndLeafGrowth(sp));

		if (getStomatalControl(i) > 0.8 && getCanopyPhotosynthesis(sp, i) > 0 && i < getEndLeafGrowth(sp) + 1) {
			yearlyCanopyDelta13C[sp] = yearlyCanopyDelta13C[sp] + getCanopyDelta13C(sp, i);
			this.dayWithPhoto=this.dayWithPhoto +1;
		} // to improved
		yearlyCanopyRespiration[sp] = yearlyCanopyRespiration[sp] + getCanopyRespiration(sp, i);
		yearlyCanopyConductance[sp] = yearlyCanopyConductance[sp] + getCanopyConductance(sp, i);
		yearlyCanopyTranspiration[sp] = yearlyCanopyTranspiration[sp] + getCanopyTranspiration(sp, i);
		yearlyCanopyEvapoTranspiration[sp] = yearlyCanopyEvapoTranspiration[sp]
				+ getCanopyEvapoTranspiration(sp, i);
		yearlyCanopyPotentialEvaporation[sp] = yearlyCanopyPotentialEvaporation[sp]
				+ getCanopyPotentialEvaporation(sp, i);
		yearlyWoodRespiration[sp] = yearlyWoodRespiration[sp] + getWoodRespiration(sp, i);
		yearlyFineRootsRespiration[sp] = yearlyFineRootsRespiration[sp] + getFineRootsRespiration(sp, i);
		yearlyCoarseRootsRespiration[sp] = yearlyCoarseRootsRespiration[sp] + getCoarseRootsRespiration(sp, i);
		yearlyHeterotrophicRespiration[sp] = yearlyHeterotrophicRespiration[sp] + getHeterotrophicRespiration(i);

		yearlyMaintenanceRespiration[sp] = yearlyMaintenanceRespiration[sp] + getMaintenanceRespiration(sp, i);
		yearlyGrowthRespiration[sp] = yearlyGrowthRespiration[sp] + getGrowthRespiration(sp, i);
		yearlyRespiration[sp] = yearlyRespiration[sp] + getRespiration(sp, i);

		yearlyLeafGrowth[sp] = yearlyLeafGrowth[sp] + getLeafGrowth(sp, i);
		yearlyWoodGrowth[sp] = yearlyWoodGrowth[sp] + getWoodGrowth(sp, i);
		yearlyCoarseRootsGrowth[sp] = yearlyCoarseRootsGrowth[sp] + getCoarseRootsGrowth(sp, i);
		yearlyFineRootsGrowth[sp] = yearlyFineRootsGrowth[sp] + getFineRootsGrowth(sp, i);
		yearlyReservesGrowth[sp] = yearlyReservesGrowth[sp] + getReservesGrowth(sp, i);

		if (getPotleafmin(sp, i) < yearlyPotLeafMin[sp]) {
			yearlyPotLeafMin[sp] = getPotleafmin(sp, i);
		}
		yearlySoilEvaporation = yearlySoilEvaporation + getSoilEvaporation(i);
		yearlyDrainage = yearlyDrainage + soil.getDrainage(i);
		yearlyTmoy = yearlyTmoy + getTmoy(i) / dayNumber;
		yearlyTmax = yearlyTmax + getTmax(i) / dayNumber;
		yearlyTmin = yearlyTmin + getTmin(i) / dayNumber;
		yearlyRg = yearlyRg + getGlobalRadiation(i) / dayNumber;
		yearlyPRI = yearlyPRI + getRain(i);
		yearlyCanopyDelta13C[sp] = yearlyCanopyDelta13C[sp] / dayWithPhoto;

		leafBiomass[sp]=Math.max(leafBiomass[sp],cell.getCanopy().getBiomassOfLeaves(sp));

		if (day==1) {
			setBFold(sp, i, cell.getCanopy().getBiomassOfLeaves(sp));
			setMBF(sp, i, 0);
		} else{
			double MBFtemp= Math.max(this.getBFold(sp,i-1)-cell.getCanopy().getBiomassOfLeaves(sp),0);
			setMBF(sp, i,MBFtemp);
			setBFold(sp, i, cell.getCanopy().getBiomassOfLeaves(sp));

		}

		if (settings.output > 1) {
			Log.println(settings.logPrefix + "dailyResults", species.castaneaCode +";"+ cell.getID() + ";"
					+ yclimate + ";" + day +  ";" +month+";" + getCanopyPhotosynthesis(sp,i) + ";"
					+ getCanopyTranspiration(sp,i) + ";" + getCanopyEvapoTranspiration(sp,i) + ";"+getSoilEvaporation(i)+";"+ getCanopyPotentialEvaporation(sp, i)+";"
					+ getREW(i) + ";" + getDailyLAI(sp,i) + ";" + getWoodGrowth(sp,i) + ";"
					+ getFineRootsGrowth(sp,i) + ";" + getBiomassOfReserves(sp,i) + ";"+cell.getCanopy().getBiomassOfLeaves(sp)+";"+getMBF(sp, i)
					+";"+ getTmax(i)
					+ ";" + getTmin(i) + ";" + getGlobalRadiation(i) + ";"+ getRain(i)+";" + getLateFrostNumber(sp) + ";"
					+ cell.getCanopy().getLAImax()[sp] + ";" + getMaintenanceRespiration(sp,i) + ";"
					+ getGrowthRespiration(sp,i) + ";" + getPotleafmin(sp, i)+";"+getPotsoil(i)+";"+getMaxHourlyTranspiration(sp,i)+";"+cell.getTSUMBBcell()[sp]+";"
					+ getHeterotrophicRespiration(i)+";"+ cell.getSoil().getCtop()+";"+cell.getSoil().getCsol());
		}

		yearlyStressLevel = getStressLevel(dayNumber - 1);

		this.setYearlySoilEvaporation(yearlySoilEvaporation);
		this.setYearlyDrainage(yearlySoilEvaporation);

	}

}
