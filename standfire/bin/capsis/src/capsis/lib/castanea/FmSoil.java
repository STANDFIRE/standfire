package capsis.lib.castanea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;

/**
 * FmSoil : Soil of a FLCell of Dynaclim model.
 *
 * @author Hendrik Davi - march 2006
 */
public class FmSoil implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone ()
	// for template)

	// fc+hd-28.2.2013
	private double litterHeight;
	private double topHeight;

	private double height; // m
	private double usefulReserve; // mm
	private double alit;
	private double asoil;

	private double rlit;
	private double rlitmin;
	private double rlitfc;
	private double rsol;
	private double rsolfc;
	private double rsolwilt;
	private double rtop;
	private double rtopfc;
	private double rtopwilt;
	private double rtopmin;
	private double rsolmin;

	private double stoneContent;

	private double [] drainage;

	private double REW;
	private double stomatalControl;
	private double stressCompteur;
	private double stressLevel;
	private double potsoil;

	private double C1;
	private double C2top;
	private double C2sol;
	private double C3top;
	private double C3sol;
	private double C4;
	private double C5;
	private double C6top;
	private double C6sol;
	private double C7top;
	private double C7sol;
	private double C8top;
	private double C8sol;
	private double C9;
	private double C10;
	private double C11top;
	private double C11sol;

	private double Ctop;
	private double Csol;
	private double CTOT;
	private double prac; // to be improved => rate of fines roots in top level

	private double Captop;
	private double Capsol;
	private double Caltop;
	private double Calsol;
	private double Csptop;
	private double Cspsol;


//	private double yearlyDrainage;

	// private double [] fineRootRespiration;
	// private double [] coarseRootRespiration;

	private Collection soilLayers;

	// other properties here...

	/**
	 * Constructor.
	 */
	public FmSoil () {

	}

	/**
	 * Inits the soil.
	 */
	public void init (FmCell cell, FmSettings settings, int sp) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];


		//usefulReserve= cell.getSoilUsefulReserve();
		//height= cell.getSoilHeight();
		alit = settings.Alit;
		asoil = settings.Asoil;

		//litterHeight=0.5;

		double wmin = 0.13;
		double wilt = 0.25; // fixed to be improved
		double wfc = 0.39;
		double bulk= 1.07;
		rlit = wfc * litterHeight;
		rlitmin = 0;  /// before 29/11/2012 wmin * 5 change to zero to decrease of gsol during summer
		rlitfc = wfc * litterHeight; // hlit fixed to 5mm to be improved
		rsol = wfc * height;
		usefulReserve=  (wfc-wilt)*(1-stoneContent)*height*bulk;

		//Log.println(settings.logPrefix+"initSoil", litterHeight+";"+ rlitfc);

		//System.out.println ("test"+ height+" "+usefulReserve+" "+rsol+" "+topHeight+" "+litterHeight);

		rsolfc = wfc * height*(1-stoneContent);
		rsolwilt = wilt * height*(1-stoneContent);
		rtopfc = wfc * topHeight*(1-stoneContent);
		rtopwilt = wilt * topHeight*(1-stoneContent);
		rtopmin = wmin * topHeight*(1-stoneContent);
		rtop = rtopfc;
		rsolmin = wmin * height*(1-stoneContent);
		REW = 1;
		stomatalControl = 1;
		stressCompteur = 0;
		stressLevel = 0;
		potsoil= settings.potbase;

		int Ndays=366;

		drainage = new double[Ndays];


		for (int i=0;i<Ndays;i++) {
			drainage[i]=0;

		}

		soilEq (cell, settings);


		Ctop=0;
		Csol=0;
	 	CTOT=0;
	 	prac=0.8;

		// Fraction du C provenant du réservoir 3 (microbes du sol) entrant dans le 8 (C passif)
		Captop=0.003+0.032*settings.SOLCLAYtop;
		Capsol=0.003+0.032*settings.SOLCLAYsol;

		// Fraction du C provenant du réservoir 3 (microbes du sol) perdu par lessivage
		Caltop=0.01+0.04*settings.SOLSANDtop;
		Calsol=0.01+0.04*settings.SOLSANDsol;

		//Fraction du carbone provenant du reservoir 7 (slow SOM) entrant dans le reservoir passif (8)
		Csptop = 0.003 - 0.009 * settings.SOLCLAYtop;
		Cspsol = 0.003 - 0.009 * settings.SOLCLAYsol;

	}



	public void setLitterHeight (double litterHeight) {
		this.litterHeight = litterHeight;
	}


	public double getTopHeight () {
		return topHeight;
	}




	public void setTopHeight (double topHeight) {
		this.topHeight = topHeight;
	}

	// double getSoilEvaporation() {return soilEvaporation;}
	double getRsol () {
		return rsol;
	}

	double getRsolfc () {
		return rsolfc;
	}

	double getRsolwilt () {
		return rsolwilt;
	}

	double getRtop () {
		return rtop;
	}

	double getRtopfc () {
		return rtopfc;
	}

	public double getRtopwilt () {
		return rtopwilt;
	}

	double getRtopmin () {
		return rtopmin;
	}

	double getRlit () {
		return rlit;
	}

	double getRlitmin () {
		return rlitmin;
	}

	double getRlitfc () {
		return rlitfc;
	}

	double getRsolmin () {
		return rsolmin;
	}

	double getREW () {
		return REW;
	}
	double getPotsoil () {
		return potsoil;
	}
	public double getCsol () {
		return Csol;
	}
	public double getCtop () {
		return Ctop;
	}
	public double getStomatalControl () {
		return stomatalControl;
	}

	public double getAsoil () {
		return asoil;
	}

	public double getAlit () {
		return alit;
	}

	public double getDrainage (int i) {
		return drainage[i];
	}


	double getStressCompteur () {
		return stressCompteur;
	}

	double getStressLevel () {
		return stressLevel;
	}

	// TO BE IMPROVED fitted in specific a soil
	double getTsol (double ta) {
		double tsol = 0.6516 * ta + 3.2698;
		return tsol;
	}

	// TO BE IMPROVED fitted in a specific soil
	double getTs30 (double tsol) {
		double ts30 = 0.8674 * tsol + 1.19;
		return ts30;
	}


	public void setRsol (double v) {
		rsol = v;
	}

	public void setRlit (double v) {
		rlit = v;
	}

	public void setRtop (double v) {
		rtop = v;
	}

	public void setREW (double v) {
		REW = v;
	}

	public void setDrainage (int i, double v) {
		drainage[i] = v;
	}
	public void setPotsoil (double v) {
		potsoil = v;
	}

	public void setStomatalControl (double v) {
		stomatalControl = v;
	}


	public void setStressCompteur (double v) {
		stressCompteur = v;
	}

	public void setStressLevel (double v) {
		stressLevel = v;
	}

	public void setUsefulReserve (double v) {
				usefulReserve = v;
	}
	public void setStoneContent (double v) {
		stoneContent = v;
	}
	public void setHeight (double v) {
				height = v;
	}


	public double getHeight () {
				return height;
	}


	public double getUsefulReserve () {
				return usefulReserve;
	}


	/**
	 * Clone method.
	 */
	public Object clone () {
		try {
			FmSoil s = (FmSoil) super.clone (); // calls protected Object
												// Object.clone () {}

			// s.fineRootRespiration = AmapTools.getCopy(fineRootRespiration);
			// s.coarseRootRespiration =
			// AmapTools.getCopy(coarseRootRespiration);

			s.soilLayers = null;
			if (soilLayers != null) {
				for (Iterator i = soilLayers.iterator (); i.hasNext ();) {
					FmSoilLayer layer = (FmSoilLayer) i.next ();
					s.addLayer ((FmSoilLayer) layer.clone ());
				}
			}

			return s;

		} catch (CloneNotSupportedException e) {
			Log.println (Log.ERROR, "FmSoil.clone ()", "Error while cloning", e);
			return null;
		}
	}

	public void addLayer (FmSoilLayer l) {
		if (soilLayers == null) {
			soilLayers = new ArrayList ();
		}
		soilLayers.add (l);
	}

	public Collection getLayers () {
		return soilLayers;
	}



	// ****************************************************************************************
	// processSoilevaporation: calculate half hourly Soil evaporation(Dufene et
	// al., 2005).

	public double getSoilEvaporation (FmSettings s, double Tsoil, double Rnhsol, double Ras, double Deltae,
			double ETRhcan) {

		double gsolmax = s.gsolmax;
		double gsolmin = s.gsolmin;
		double ETRhsol = 0;
//		double rtop = this.getRtop ();
//		double rsol = this.getRsol ();
//		double rmin = this.getRsolmin ();
//		double rtopfc = this.getRtopfc ();
		double gsol = 0;

		double es = 6.1078 * Math.exp (17.269 * Tsoil / (Tsoil + 237.3)); // (hPa)
																			// (Monteith,
																			// 1990)

		// temperature effect on micrometeorologic variables (R= 8.31) ! g

		double Rauh = -4.111 * Tsoil + 1289.764; // g/m3
		double Landah = -2.37273 * Tsoil + 2501; // J.g-1
		double Psyh = 0.01 * s.Cph * (Rauh / 18) * s.R * (Tsoil + 273.15) / Landah;// in
																					// Monteith
																					// p.181
		double Deltah = Landah * 18 * es / (s.R * Math.pow (Tsoil + 273.15, 2)); // mb.K-1
																					// Monteith
																					// p.
																					// 10
		double ETRhrsol = 0;
		double ETRhcsol = 0;

		// Calcul de la r?sistance du sol ? l'?vaporation

		if (rlit > 0) {
			gsol = gsolmax;
		} else {
			gsol = Math.max ((gsolmax - gsolmin) * (rtop - rtopwilt) / (rtopfc - rtopwilt) + gsolmin, gsolmin);
		}
		// if frost no evaporation

		if (Tsoil <= 0) {
			gsol = 0;
		}
		if (s.output>2) {
			Log.println(s.logPrefix+"soilEvapoHourly", gsol+";"+Tsoil+";"+Rnhsol+";"+Ras+";"+Deltae+";"+gsolmax+";"+gsolmin+";"+rtop+";"+rtopfc+";"+rtopwilt);
		}
		if (gsol > 0) {
			ETRhrsol = 3.6 * (Deltah * Rnhsol) / (Landah * (Deltah + Psyh * (1 + 1 / (gsol * Ras))));
			ETRhcsol = 3.6 * (Rauh * s.Cph * Deltae / Ras) / (Landah * (Deltah + Psyh * (1 + 1 / (gsol * Ras))));

		}

		if (Rnhsol < 0) {
			ETRhrsol = 0;
		}
		if (Deltae < 0) {
			ETRhcsol = 0;
		}

		ETRhsol = ETRhcsol + ETRhrsol;

		// Log.println(s.logPrefix+"DynaclimTest", "rlit=" +rlit+" ras= "+
		// Ras+"ETRhsol "+ETRhsol);

		if (rlit < s.eps & Math.abs (rsol - rsolmin) < s.eps) {
			ETRhsol = 0;
		}

		// Log.println(s.logPrefix+"DynaclimTest", "rlit=" +rlit+" ETRhsol= "+ETRhsol);
		return ETRhsol;

	} // end of processSoilevaporation

	// *************************************************************************************************
	// PROCESS waterDynamics Davi july 2010 (from CASTANEA 4.3)
	// **************************************************************************************************

	public void waterDynamics (FmCell cell, FmSettings settings, FmYearlyResults yearlyResults, int j, FmCanopyWaterReserves waterReserves) {

		// sum on all species of the canopy
		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];

		int sp=0;
		double psd = 0;
		double ec = 0;
		double egt = 0;
		double transpiration = 0;
		double dailyETRsoil = yearlyResults.getSoilEvaporation (j);
		double drmacro;

		psd = psd + waterReserves.getPsd (sp);
		egt = egt + waterReserves.getEgt (sp);
		ec = ec + waterReserves.getEc (sp);
		transpiration = transpiration + yearlyResults.getCanopyTranspiration (sp, j);


		double elit = 0; // litter evaporation
		double etop = 0;

		double dlit = 0; // litter drainage
		double drtop = 0;
		double dr = 0;

		double transpirationTop = 0;

		double C3 = 1;
		double propmacro = 0; // to be improved => rate of macroporosity in the
								// soil differs from coarse element
		double Nstress = 0.4; // to be improved to a constant value

		// water reserve in litter
		rlit = rlit + psd + egt;

		if (rlit - rlitmin < dailyETRsoil) {
			elit = rlit;

		} else {
			elit = dailyETRsoil;
		}

		rlit = rlit - elit;

		dlit = Math.max (rlit - rlitfc, 0.);
		dlit = Math.min (rlit - rlitmin, dlit);

		rlit = rlit - dlit;

		// water reserve in the total soil (without litter but with Rtop !!!!)

		rsol = rsol + ec + dlit;

		dr = C3 * Math.max (rsol - rsolfc, 0.) + propmacro * (ec + dlit);
		dr = Math.min (dr, rsol - rsolmin);
		drmacro= propmacro * (ec + dlit);

		etop = (dailyETRsoil - elit);



		if (rtop <= rtopmin) {
			etop = 0; // evaporation blocked
		}

		rsol = rsol - dr - etop - transpiration;



		// R?serve en eau du sol superficiel: Rtop
		rtop = rtop + ec + dlit;

		// drainage de la partie sup?rieure (le drainage a lieu avant
		// l'evaporation)
		drtop = propmacro * (ec + dlit) + C3 * Math.max (rtop - rtopfc, 0.);
		drtop = Math.min (drtop, rtop - rtopmin);

		rtop = rtop - drtop;

		// potential transpiration for Rtop
		if (transpiration > 0) {
			transpirationTop = prac * transpiration; // prac : %racines
		} else {
			transpirationTop = 0;
		}

		// IF (Rtop <= Rtopwilt) TRtop=0

		rtop = rtop - etop - transpirationTop;

		// Effet de L'EAU DISPONIBLE dans le SOL sur les Echanges Gazeux
		// Foliaires
		// Pour la prochaine it?ration

		REW = (rsol - rsolwilt) / (rsolfc - rsolwilt);

		if (settings.stomataStress =="Granier") {
			if (rsol - rsolwilt >= Nstress * usefulReserve) {
				stomatalControl = 1;
			} else {
				stomatalControl = (rsol - rsolwilt) / (Nstress * usefulReserve);
			}
		}

		potsoil= settings.potbase*Math.pow(rsol/rsolfc,settings.parameterPot);  // -5.05 intialement change to -8 to fit Ventoux measurements

		if (settings.stomataStress =="Rambal") {
				stomatalControl = 1+cell.getSlopePotGsCell()[sp]*potsoil;
		}

		// fermer les stomates si Rsol <= rsolwilt et Rtop <= Rtopwilt
		if (stomatalControl<0) {
			stomatalControl = 0;
			REW = 0;
		}


		/*
		 * other formulations
		 *
		 * IF (istress==1) then //pas de stress si r?servoir 0 - 30 cm > Rlim IF
		 * (Rtop > Rlim) Reduc= 1
		 *
		 * IF (site==1) THEN
		 *
		 * Pot= -0.04*(Rsol/rsolfc)**(-5.05) IF (pot<-3.5) pot=-3.5 IF(espece==1)
		 * g1= min(g1max + 4.2*Pot,g1max) IF(espece==2) g1= g1max + 5.3*Pot IF
		 * (g1<g1min) g1= g1min
		 *
		 *
		 * ELSE g1= (g1max - g1min) * Reduc + g1min
		 *
		 *
		 * ENDIF ENDIF
		 *
		 * IF (istress==2) THEN Potsoil= -0.4*(Rsol/rsolfc)**(-5.05)
		 * g1=g1max*(ea/10
		 * -Potsoilmax+trpmhmax2*Rhydth)/(ea/10-potsoil+trpmhmax2*Rhyd) IF (Rtop
		 * > Rlim) g1=g1max ENDIF
		 *
		 *
		 * IF (espece==7 .and. icohort==1) THEN g1= g1min+
		 * (g1max-g1min)*Reduc*(gsf1
		 * *ly+gsf1*ly1+gsf2*ly2+gsf3*ly3+gsf4*ly4+gsf5*ly5)/L ENDIF
		 *
		 * IF (espece==8 .and. icohort==1) THEN g1= g1min+
		 * (g1max-g1min)*Reduc*(gsf1
		 * *ly+gsf1*ly1+gsf2*ly2+gsf3*ly3+gsf4*ly4+gsf5*
		 * ly5*gsf6*ly6+gsf7*ly7+gsf8*ly8+gsf9*ly9+gsf10*ly10)/L ENDIF
		 */

		// calculation of stress index

		if (REW < 0.4) {
			stressCompteur = stressCompteur + 1;
			//stressLevel = stressLevel + (1 - stomatalControl);

		}

		stressLevel = stressLevel + potsoil;

		if (settings.output>1) {
			Log.println(settings.logPrefix+"soilWater", REW+";"  + stomatalControl+ ";"+ stressLevel+";"+rsol+";"+rtop+";"+psd+";"+egt+";"+transpiration+";"+dr+";"+drmacro+";"+dailyETRsoil+";"+rlit+";"+potsoil+";"+this.getUsefulReserve());
		}
		yearlyResults.setRsol (j, rsol);
		yearlyResults.setREW (j, REW);
		yearlyResults.setPotsoil (j, potsoil);

		yearlyResults.setStomatalControl (j, stomatalControl);
		yearlyResults.setStressLevel (j, stressLevel);
		this.setDrainage(j,dr);


	}// end of soilDynamics

 public void calculateFineRootsMortality(FmCell cell, FmYearlyResults yearlyResults, int j, FmSettings settings)  {

	 FmWood wood= cell.getWood();
	 double [] biomassOfFineRoot = wood.getBiomassOfFineRoot();
	 FmSpecies[] fmSpeciesList = cell.getUsedFmSpecies ();
	 int nSpecies = fmSpeciesList.length;
	 FmSpecies species=fmSpeciesList[0];


	 double [] BRFtemp= new double[nSpecies];
	 double LossRoots= 0;
	 int sp=0;
	 double RootsVul= 0.93;


	if (REW >= 0.4) {
		BRFtemp[sp]= biomassOfFineRoot[sp];

	}else{
		LossRoots= 1/(1+Math.exp(RootsVul*(potsoil-settings.rootsPLC50)));
		BRFtemp[sp]= biomassOfFineRoot[sp]*(1-LossRoots);

	}


	wood.setBiomassOfFineRoot(BRFtemp);

	 /*if (reduc==1)  then
		BRFDepthold=BRFDepth
		BRFTopold= BRFtop

		else

			LossRootsTop= 1/(1+Math.exp(RootsVul*(potsoiltop-rootsPLC50)))
			LossRootsDepth= 1/(1+Math.exp(RootsVul*(potsoildepth-rootsPLC50)))

			if (LossRootsDepth>LossRootsDepthold) then
				BRFDepth= (1-LossRootsDepth)*BRFDepthold
				else
				LossRootsDepth= LossRootsDepthold
			endif

			if (LossRootsTop>LossRootsTopold) then
				BRFTop= (1-LossRootsTop)*BRFTopold
				else
				LossRootsTop=LossRootsTopold
			endif

			LossRootsDepthold= LossRootsDepth
			LossRootsTopold= LossRootsTop

	endif*/
	}





// ****************************************************************************************
	// processSoilRespiration: calculate half hourly Soil heterotrophic respiration(Dufene et
	// al., 2005).

	 public void soilHeterotrophicRespiration (FmCell cell, FmSettings settings, FmYearlyResults yearlyResults, FmClimateDay climateDay, int day){

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];
		FmCanopy canopy = cell.getCanopy();
		int nSpecies = fmSpeciesList.length;
		int sp=0;
		double Tmoy = climateDay.getDailyAverageTemperature();


		double MBF=0;
		double litfall=0;
		double ATStop= 0;
		double ATair= 0;
		double ATsol= 0;

		double Nroot=0.7; // to be improved
		double lignroot= 25.4; 	// to be improved

		double Nlit= 0.5*species.leafNitrogen;
		double lignlit= 0.97*species.LIGNll;					// lignine litière normally LIGNll is LGNF to be checked
		double LNlit= lignlit/Nlit;				// rapport lignine/azote litière
		double LNroot= lignroot/species.fineRootsNitrogen;
		double Tsol= getTsol(Tmoy);
		double Ts30= getTs30(Tmoy);
		double LMAmoysol= canopy.getAverageLMA(cell, settings, species, sp);
		double LMAlit= 0.8*LMAmoysol; // to be improved

		double finebranchfall=yearlyResults.getMBBV(sp,day)*0.72;       // d'après Duvigneaud
		double coarsebranchfall=yearlyResults.getMBBV(sp,day)*(1-0.72);
		double rootfall=yearlyResults.getFineRootsMortality(sp,day);
		double crootfall=yearlyResults.getMBRG(sp,day);

		double FMll=0.85-0.028*LNlit;  //metabolic fraction from the leaf litter
		double FSll=1-FMll;			 //structural fraction from the leaf litter
		double FMrl=0.85-0.014*LNroot; //metabolic fraction from root litter
		double FSrl=1-FMrl;			//Structural fraction from root litter

		if (day>0) {
		         MBF=yearlyResults.getMBF(sp,day-1);
		         litfall= yearlyResults.getMBF(sp,day-1)*(LMAlit/LMAmoysol);
		}

		// Effect of temperature on carbon fluxes between pools

		if (Tsol<45 && Tmoy<45 && Ts30<45) {

			ATStop=Math.pow((45-Tsol)/(45-35),0.2)*Math.exp(0.076*(1-Math.pow((45-Tsol)/(45-35),2.63)));
			ATair=Math.pow((45-Tmoy)/(45-25),0.2)*Math.exp(0.076*(1-Math.pow((45-Tmoy)/(45-25),4.9)));
			ATsol=Math.pow((45-Ts30)/(45-35),0.2)*Math.exp(0.076*(1-Math.pow((45-Ts30)/(45-35),2.63)));

		} else {

			ATStop= 0;
			ATair= 0;
			ATsol= 0;

		}


// Effect of water on the carbon flux between pools

		double AWlit=1/(1+30*Math.exp(-8.5*((rlit-rlitmin)/(rlitfc-rlitmin))));
		double AWtop=1/(1+30*Math.exp(-8.5*((rtop-rtopmin)/(rtopfc-rtopmin))));
		double AWsol=1/(1+30*Math.exp(-8.5*((rsol-rtop-rsolmin+rtopmin)/(rsolfc-rtopfc-rsolmin+rtopmin))));

// Respiration of differents pools
		double R1= (0.6 * (1 - species.LIGNll) + 0.3 * species.LIGNll) * settings.K1/365 * Math.exp(-3 * species.LIGNll) * ATair * AWlit * C1; // resp en proportion de la lignine
		double R2top= (0.55 * (1 - species.LIGNrl) + 0.3 * species.LIGNrl) * settings.K2/365 * Math.exp(-3 * species.LIGNrl) * ATStop * AWtop * C2top;
		double R2sol= (0.55 * (1 - species.LIGNrl) + 0.3 * species.LIGNrl) * settings.K2/365 * Math.exp(-3 * species.LIGNrl) * ATsol * AWsol * C2sol;
		double R3top= (0.85 - 0.68 * settings.SOLFINtop) * settings.K3/365 * (1 - 0.75 * settings.SOLFINtop) * ATStop * AWtop * C3top;
		double R3sol= (0.85 - 0.68 * settings.SOLFINsol) * settings.K3/365 * (1 - 0.75 * settings.SOLFINsol) * ATsol * AWsol * C3sol;
		double R4= 0.6 * settings.K4/365 * ATair * AWlit * C4;
		double R5= 0.6 * settings.K5/365 * ATair * AWlit * C5;
		double R6top= 0.55 * settings.K6/365 * ATStop * AWtop * C6top;
		double R6sol= 0.55 * settings.K6/365 * ATsol * AWsol * C6sol;
		double R7top= 0.55 * settings.K7/365 * ATStop * AWtop * C7top;
		double R7sol= 0.55 * settings.K7/365 * ATsol * AWsol * C7sol;
		double R8top= 0.55 * settings.K8/365 * ATStop * AWtop * C8top;
		double R8sol= 0.55 * settings.K8/365 * ATsol * AWsol * C8sol;
		double R9= (0.6 * (1 - species.LIGNfb) + .3 * species.LIGNfb) * Math.exp(-3 * species.LIGNfb) * (settings.K1/365)/2 * ATair * AWlit * C9;
		double R10= (0.6 * (1 - species.LIGNcb) + .3 * species.LIGNcb) * Math.exp(-3 * species.LIGNcb) * (settings.K1/365)/10 * ATair * AWlit * C10;
		double R11top= (0.55 * (1 - species.LIGNcr) + .3 * species.LIGNcr) * Math.exp(-3 * species.LIGNcr) * (settings.K2/365)/10 * ATStop * AWtop * C11top;
		double R11sol= (0.55 * (1 - species.LIGNcr) + .3 * species.LIGNcr) * Math.exp(-3 * species.LIGNcr) * (settings.K2/365)/10 * ATsol * AWsol * C11sol;

		double RTOT= R1 + R2top + R2sol + R3top + R3sol + R4 + R5 + R6top + R6sol +  R7top + R7sol + R8top + R8sol + R9 + R10 + R11top + R11sol;

// Changes in carbon stock in the different compartments of the soil

		double dC1    = FSll * litfall - settings.K1/365 * Math.exp(-3 * species.LIGNll) * ATair  * AWlit * C1;
		double dC2top = FSrl * rootfall * prac - settings.K2/365 * Math.exp(-3 * species.LIGNrl) * ATStop * AWtop * C2top;
		double dC2sol = FSrl * rootfall * (1 - prac) - settings.K2/365 * Math.exp(-3 * species.LIGNrl) * ATsol  * AWsol * C2sol;
		double dC3top = ATStop * AWtop * (0.45 * (1 - species.LIGNrl) * settings.K2/365 * Math.exp(-3 * species.LIGNrl) * C2top + 0.45 * settings.K6/365 * C6top + 0.45 * settings.K8/365 * C8top + (1 - Csptop - 0.55) * settings.K7/365 * C7top - settings.K3/365 * (1 - 0.75 * settings.SOLFINtop) * C3top + 0.45 * (1 - species.LIGNcr) * settings.K2/365/10 * Math.exp(-3 * species.LIGNrl) * C11top);
		double dC3sol = ATsol * AWsol * (0.45 * (1 - species.LIGNrl) * settings.K2/365 * Math.exp(-3 * species.LIGNrl) * C2sol + 0.45 * settings.K6/365 * C6sol + 0.45 * settings.K8/365 * C8sol + (1 - Cspsol - 0.55) * settings.K7/365 * C7sol - settings.K3/365 * (1 - 0.75 * settings.SOLFINsol) * C3sol + 0.45 * (1 - species.LIGNcr) * settings.K2/365/10 * Math.exp(-3 * species.LIGNrl) * C11sol);
		double dC4 = ATair * AWlit * (0.4 *(1 - species.LIGNll) * settings.K1/365 * Math.exp(-3 * species.LIGNll) * C1 + 0.4 *settings.K5/365 * C5- settings.K4/365 * C4 + 0.4 *(1 - species.LIGNfb) * settings.K1/365/2 * Math.exp(-3 * species.LIGNfb) * C9 + 0.4 *(1 - species.LIGNcb) * settings.K1/365/10 * Math.exp(-3 * species.LIGNcb) * C10);
		double dC5 = FMll * litfall - settings.K5/365 * AWlit * ATair * C5;
		double dC6top = FMrl * rootfall * prac - settings.K6/365 * ATStop * AWtop * C6top;
		double dC6sol = FMrl * rootfall * (1 - prac) - settings.K6/365 * ATsol * AWsol * C6sol;
		double dC7top = ATStop * AWtop * (0.7 * species.LIGNll * settings.K1/365 * Math.exp(-3 * species.LIGNll) * C1 + 0.7 *species.LIGNrl * settings.K2/365 * Math.exp(-3 * species.LIGNrl) * C2top + 0.4 * settings.K4/365 * C4 + (1 - Captop - Caltop - (0.85 - 0.68 * settings.SOLFINtop)) * settings.K3/365 * (1 - 0.75 * settings.SOLFINtop) * C3top - settings.K7/365 * C7top + 0.7 * species.LIGNfb * settings.K1/365/2 * Math.exp(-3 * species.LIGNfb) * C9 + 0.7 * species.LIGNcb * settings.K1/365/10 * Math.exp(-3 * species.LIGNcb) * C10 + 0.7 * species.LIGNcr * settings.K2/365/10 * Math.exp(-3 * species.LIGNcb) * C11top);
		double dC7sol = ATsol * AWsol * (0.7 * species.LIGNrl * settings.K2/365 * Math.exp(-3 * species.LIGNrl) * C2sol + (1 - Capsol - Calsol - (0.85 - 0.68 * settings.SOLFINsol)) * settings.K3/365 * (1 - 0.75 * settings.SOLFINsol) * C3sol - settings.K7/365 * C7sol + 0.7 * species.LIGNcr * settings.K2/365/10 * Math.exp(-3 * species.LIGNcr) * C11sol);
		double dC8top = ATStop * AWtop * (Csptop * settings.K7/365 * C7top + Captop * settings.K3/365 * (1 - 0.75 * settings.SOLFINtop) * C3top - settings.K8/365 * C8top);
		double dC8sol = ATsol * AWsol * (Cspsol * settings.K7/365 * C7sol + Capsol * settings.K3/365 * (1 - 0.75 * settings.SOLFINsol) * C3sol - settings.K8/365 * C8sol);
		double dC9 = finebranchfall - settings.K1/365/2 * ATair  * AWlit * C9;
		double dC10 = coarsebranchfall - settings.K1/365/10 * ATair  * AWlit * C10;
		double dC11top = crootfall * prac - settings.K2/365/10 * ATStop * AWtop * C11top;
		double dC11sol = crootfall * (1-prac) - settings.K2/365/10 * ATsol * AWsol * C11sol;


		double dCtop = dC1 + dC2top + dC3top + dC4 + dC5 + dC6top + dC7top + dC8top + dC9 + dC10 + dC11top;
		double dCsol = dC2sol + dC3sol + dC6sol + dC7sol + dC8sol + dC11sol;
		double DCTOT = dCtop + dCsol;

		C1= C1 + dC1;
		C2top = C2top + dC2top;
		C2sol = C2sol + dC2sol;
		C3top = C3top + dC3top;
		C3sol = C3sol + dC3sol;
		C4 = C4 + dC4;
		C5 = C5 + dC5;
		C6top = C6top + dC6top;
		C6sol = C6sol + dC6sol;
		C7top = C7top + dC7top;
		C7sol = C7sol + dC7sol;
		C8top = C8top + dC8top;
		C8sol = C8sol + dC8sol;
		C9 = C9 + dC9;
		C10 = C10 + dC10;
		C11top = C11top + dC11top;
		C11sol = C11sol + dC11sol;

		Ctop = C1 + C2top + C3top + C4 + C5+ C6top + C7top + C8top + C9 + C10 + C11top;
		Csol = C2sol + C3sol + C6sol + C7sol + C8sol + C11sol;
		CTOT = Ctop + Csol;

		yearlyResults.setHeterotrophicRespiration(day,RTOT);


		if (settings.output>1) {
			Log.println(settings.logPrefix+"soilCarbon", RTOT+";"+Ctop+";"+Csol+";"+R1+";"+ R2top
			        +";"+R2sol+";"+ R3top+";"+ R3sol+";"+R4+";"+ R5+";" + R6top+";" + R6sol+";" +  R7top+";"
			        + R7sol+";" + R8top+";" + R8sol+";" + R9+";" + R10+";" + R11top+";"
			        + R11sol+";"+ATStop+";"+ATair+";"+ATsol+";"+AWlit+";"+AWtop+";"+AWsol+";"
			        + dC1+";"+ dC2top+";" + dC3top+";"+dC4+";"+dC5+";"+dC6top+";"+dC7top+";"
			        + dC8top+";" + dC9+";" + dC10+";" + dC11top+";"+ litfall+";"+LMAmoysol);
		}




		}

public void soilEq (FmCell cell, FmSettings settings){

        // true for ventoux+++++++to be improved
        double ATairy= 0.310110;
        double ATStopy= 0.15650;
        double ATsoly= 0.008410;

        double Awsoly= 0.9816;
        double Awlity=0.70909;
        double Awtopy= 0.80194;
        double Nroot=0.7; // to be improved
        double lignroot= 25.4; 	// to be improved

        FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
        FmSpecies species=fmSpeciesList[0];
        FmCanopy canopy = cell.getCanopy();
        int nSpecies = fmSpeciesList.length;
        int sp=0;

        double Nlit= 0.5*species.leafNitrogen;
        double lignlit= 0.97*species.LIGNll;					// lignine litière normally species.LIGNll is LGNF to be checked
        double LNlit= lignlit/Nlit;				// rapport lignine/azote litière
        double LNroot= lignroot/species.fineRootsNitrogen;
        double LMAmoysol= canopy.getAverageLMA(cell, settings, species, sp);
        double LMAlit= 0.8*LMAmoysol; // to be improved
        double MBBV= cell.getWood().getBiomassOfBranch()[sp]*species.TMBV;
        double finebranchfall=MBBV*0.72;       // d'après Duvigneaud
        double coarsebranchfall=MBBV*(1-0.72);
        double rootfall=cell.getWood().getBiomassOfFineRoot()[sp]*species.TMRF;
        double crootfall=cell.getWood().getBiomassOfCoarseRoot()[sp]*species.TMBV;
        double LMAX= cell.getCanopy ().getLAImax ()[sp];
        double LMAmoy = canopy.getAverageLMA(cell, settings, species, sp);
        double litfall = LMAmoy * LMAX * settings.tc;



        double FMll=0.85-0.028*LNlit;  //metabolic fraction from the leaf litter
        double FSll=1-FMll;			 //structural fraction from the leaf litter
        double FMrl=0.85-0.014*LNroot; //metabolic fraction from root litter
        double FSrl=1-FMrl;			//Structural fraction from root litter

        double C1eq= (FSll * litfall)/(settings.K1 * Math.exp(-3 * species.LIGNll) * ATairy  * Awlity);
        double C2topeq= (FSrl * rootfall * prac)/(settings.K2 * Math.exp(-3 * species.LIGNrl) * ATStopy * Awtopy);
        double C2soleq= (FSrl * rootfall * (1 - prac))/(settings.K2 * Math.exp(-3 * species.LIGNrl) * ATsoly  * Awsoly);
        double C5eq=(FMll * litfall)/(settings.K5 * Awlity * ATairy);
        double C6topeq=(FMrl * rootfall * prac)/(settings.K6 * ATStopy * Awtopy);
        double C6soleq=(FMrl * rootfall * (1 - prac))/(settings.K6 * ATsoly * Awsoly);
        double C9eq=(finebranchfall)/(settings.K1/2 * ATairy  * Awlity);
        double C11topeq=(crootfall * prac)/(settings.K2/10 * ATStopy * Awtopy);
        double C11soleq=(crootfall * (1-prac))/(settings.K2/10 * ATsoly * Awsoly);
        double C10eq=(coarsebranchfall)/(settings.K1/10 * ATairy  * Awlity);
        double C4eq=(0.4 * ((1 - species.LIGNll) * settings.K1 * Math.exp(-3 * species.LIGNll) * C1eq + settings.K5 * C5eq) + .4 * ((1 - species.LIGNfb) * (settings.K1/2) * Math.exp(-3 * species.LIGNfb) * C9eq + (1 - species.LIGNcb) * (settings.K1/10) * Math.exp(-3 * species.LIGNcb) * C10eq))/settings.K4;

        double Aeq=0.45 * settings.K8/(settings.K3 * (1 - .75 * settings.SOLFINtop));
        double Beq=(1 - Csptop - .55) * settings.K7/(settings.K3 * (1 - .75 * settings.SOLFINtop));
        double Ceq=(0.45 * (1 - species.LIGNrl) * settings.K2 * Math.exp(-3 * species.LIGNrl) * C2topeq + 0.45 * settings.K6 * C6topeq + .45 * (1 - species.LIGNcr) * (settings.K2/10) * Math.exp(-3 * species.LIGNrl) * C11topeq)/(settings.K3 * (1 - .75 * settings.SOLFINtop));
        double Deq=((1 - Captop - Caltop - (.85 - .68 * settings.SOLFINtop)) * settings.K3 * (1 - .75 * settings.SOLFINtop))/settings.K7;
        double Eeq=(0.7 * (species.LIGNll * settings.K1 * Math.exp(-3 * species.LIGNll) * C1eq + species.LIGNrl * settings.K2 * Math.exp(-3 * species.LIGNrl) * C2topeq) + .4 * settings.K4 * C4eq + .7 * (species.LIGNfb * settings.K1/2 * Math.exp(-3 * species.LIGNfb) * C9eq + species.LIGNcb * settings.K1/10 * Math.exp(-3 * species.LIGNcb) * C10eq + species.LIGNcr * settings.K2/10 * Math.exp(-3 * species.LIGNcb) * C11topeq))/settings.K7;
        double Feq=(Csptop * settings.K7)/settings.K8;
        double Geq=(Captop * settings.K3 * (1 - 0.75 * settings.SOLFINtop))/settings.K8;

        double C3topeq=(Beq*Eeq+Ceq+Aeq*Feq*Eeq)/(1 - Deq*Beq - Feq*Deq*Aeq - Geq*Aeq);
        double C7topeq=Deq*C3topeq+Eeq;
        double C8topeq=Feq*C7topeq+Geq*C3topeq;

         Aeq=0.45 * settings.K8/(settings.K3 * (1 - .75 * settings.SOLFINsol));
         Beq=(1 - Cspsol - .55) * settings.K7/(settings.K3 * (1 - .75 * settings.SOLFINsol));
         Ceq=(.45 * (1 - species.LIGNrl) * settings.K2 * Math.exp(-3 * species.LIGNrl) * C2soleq + 0.45 * settings.K6 * C6soleq + .45 * (1 - species.LIGNcr) * settings.K2/10 * Math.exp(-3 * species.LIGNrl) * C11soleq)/(settings.K3 * (1 - .75 * settings.SOLFINsol));
         Deq=((1 - Capsol - Calsol - (.85 - .68 * settings.SOLFINsol)) * settings.K3 * (1 - .75 * settings.SOLFINsol))/settings.K7;
         Eeq=(.7 * (species.LIGNrl * settings.K2 * Math.exp(-3 * species.LIGNrl) * C2soleq)+ 0.7 * (species.LIGNcr * settings.K2/10 * Math.exp(-3 * species.LIGNcr) * C11soleq))/settings.K7;
         Feq=(Cspsol * settings.K7)/settings.K8 ;
         Geq=(Capsol * settings.K3 * (1 - .75 * settings.SOLFINsol))/settings.K8 ;

        double C3soleq=(Beq*Eeq+Ceq+Aeq*Feq*Eeq)/(1 - Deq*Beq - Feq*Deq*Aeq - Geq*Aeq);
        double C7soleq=Deq*C3soleq+Eeq;
        double C8soleq=Feq*C7soleq+Geq*C3soleq;

        C1=C1eq;
        C2top=C2topeq;
        C2sol=C2soleq;
        C3top=C3topeq;
        C3sol=C3soleq;
        C4=C4eq;
        C5=C5eq;
        C6top=C6topeq;
        C6top=C6topeq;
        C6top=C6topeq;
        C7sol=C7soleq;
        C8top=C8topeq;
        C8sol=C8soleq;
        C9=C9eq;
        C10=C10eq;
        C11top=C11topeq;
        C11sol=C11soleq;
} //end of method
} // end of class