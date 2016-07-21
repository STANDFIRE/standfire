package capsis.lib.castanea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import capsis.lib.phenofit.Fit5Phenology;


/**
 * FmCanopy : Leaf of a FLCell of Dynaclim model.
 *
 * @author Hendrik Davi - march 2006
 */
public class FmCanopy implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone ()
	// for template)

	private double[] LAImax;
	private double LAImaxBeforeFrost;

	private double[] LAI;
	private double[] WAI;
	private double[] clumping;
	private double[] nitrogen;
	private double[] potleaf;
	private double[] strat;
	private double [] biomassOfLeaves;

	private double[][] strat_sp;
	private double[][] WAI_sp;

	private Collection canopylayers;

	private FmCanopyWaterReserves canopyWaterReserves;
	private FmCanopyEvergreen canopyEvergreen;

	//private FmSettings settings; // fc-11.6.2014 for logPrefix
	private boolean deciduous; 

	private Fit5Phenology pheno;
	private FmSettings settings;

	/**
	 * Constructor for new logical FmCanopy.
	 */
	public FmCanopy (int nSpecies, FmCell cell, FmSettings settings) {

		int nbstrat = settings.NB_CANOPY_LAYERS;
	        
		LAImax = new double[nSpecies];
		LAI = new double[nSpecies];
		biomassOfLeaves= new double[nSpecies];
		strat= new double[nSpecies];
		WAI = new double[nSpecies];
		clumping = new double[nSpecies];
		nitrogen = new double[nSpecies];
		potleaf= new double[nSpecies];
		strat_sp = new double[nSpecies][nbstrat];
		WAI_sp = new double[nSpecies][nbstrat];
		this.settings = settings;
		
		
		pheno= new Fit5Phenology(settings.initialDate,true); // WARNING intialized to true = decidous because fmcspecies do not created 

		
	}

	/**
	 * Inits the canopy.
	 */
	public void init (FmCell cell, FmSettings settings) {


		double[][] speciesProportion = cell.getSpeciesProportion ();

		FmSpecies[] fmSpeciesList = cell.getUsedFmSpecies ();
		FmSpecies species=fmSpeciesList[0];
		
		
		//pheno= new Fit5Phenology (cell.birthYear,deciduous);
		
		int nSpecies = fmSpeciesList.length;
		int nbstrat = settings.NB_CANOPY_LAYERS;

		double[] rleaf = new double[nSpecies];
		double[] rbark = new double[nSpecies];
		double[] rcanop = new double[nSpecies];
		double[] rleafmax = new double[nSpecies];
		double[] rbarkmax = new double[nSpecies];
		double[] rcanmax = new double[nSpecies];
		double[] egt = new double[nSpecies];
		double[] ps = new double[nSpecies];
		double[] psd = new double[nSpecies];
		double[] ec = new double[nSpecies];
		double[] biomassOfLeaves= new double[nSpecies];
		double[] LMAX = cell.getCanopy ().getLAImax ();


		double[] thickness = new double[nSpecies];


//		WAI = cell.getWAI();

		double WAItot = cell.sumArray (WAI);
		double LAItot = cell.sumArray (LAImax);



		int sp = 0;
		if (species.decidu == 1) {
			LAI[sp] = 0;
		} else {
			LAI[sp] = this.LAImax[sp];
		}
		this.setLAImaxBeforeFrost(LAImax[sp]);

		strat[sp]= LAItot / nbstrat;
		rleaf[sp] = 0;
		rbark[sp] = 0;
		rcanop[sp] = 0;
		rleafmax[sp] = LAI[sp] * species.Tleaf;
		rbarkmax[sp] = 2 * WAI[sp] * species.Tbark;
		rcanmax[sp] = rbarkmax[sp] + rleafmax[sp];
		egt[sp] = 0;
		ps[sp] = 0;
		psd[sp] = 0;
		ec[sp] = 0;
		potleaf[sp] =0;
		//cell.getCanopy ().setLAImaxBeforeFrost (LMAX);


		int cohortesOfLeaves= (int)species.cohortesOfLeaves;

		if (species.decidu==2) {
			FmCanopyEvergreen canopyEvergreen = new FmCanopyEvergreen (cohortesOfLeaves, settings); // fc-11.6.2014 added settings
			canopyEvergreen.initCanopyEvergreen(species,cohortesOfLeaves,cell,sp);
			this.canopyEvergreen= canopyEvergreen;
		}

		for (int l = 0; l < nbstrat; l++) {
			strat_sp[sp][l] = strat[sp] * speciesProportion[sp][l];
			thickness[sp] = strat[sp] * speciesProportion[sp][l];
			WAI_sp[sp][l] = WAItot / nbstrat * speciesProportion[sp][l];
		}

		for (int l = 0; l < nbstrat; l++) {
			FmCanopyLayer layer = new FmCanopyLayer (l, thickness);
			addLayer (layer);
		}

		// initialisation des biomass
		sp=0;
		double BF=0;
		if (species.decidu==1) {
			BF=0;
		} else {
			cohortesOfLeaves= (int)species.cohortesOfLeaves;

			double LMAmoy=getAverageLMA(cell, settings, species, sp);

			BF= LMAmoy*LAI[sp]*settings.tc;

		}

		this.setBiomassOfLeaves(sp,BF);
		FmCanopyWaterReserves canopyWaterReserves = new FmCanopyWaterReserves (rleaf, rbark, rcanop, rleafmax,
				rbarkmax, rcanmax, egt, ps, psd, ec);
		this.canopyWaterReserves = canopyWaterReserves;


	}

	/**
	 * Clone method.
	 */
	public Object clone () {
		try {
			FmCanopy l = (FmCanopy) super.clone (); // calls protected Object

			l.LAImax = AmapTools.getCopy (LAImax);
			l.LAI = AmapTools.getCopy (LAI);
			//l.LAImaxBeforeFrost = AmapTools.getCopy (LAImaxBeforeFrost);
			l.WAI = AmapTools.getCopy (WAI);
			l.clumping = AmapTools.getCopy (clumping);
			l.nitrogen = AmapTools.getCopy (nitrogen);
			l.strat_sp = AmapTools.getCopy (strat_sp);
			l.WAI_sp = AmapTools.getCopy (WAI_sp);

			l.canopylayers = null;
			for (Iterator i = canopylayers.iterator (); i.hasNext ();) {
				FmCanopyLayer layer = (FmCanopyLayer) i.next ();
				l.addLayer ((FmCanopyLayer) layer.clone ());
			}
			l.canopyWaterReserves = (FmCanopyWaterReserves) canopyWaterReserves.clone ();

			// FmSettings settings not cloned

			return l;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FmCanopy.clone ()", "Error while cloning", e);
			return null;
		}
	}

	
	public Fit5Phenology getPheno (){
	        return pheno;
	}
	public void  setPheno (Fit5Phenology pheno){
	        this.pheno= pheno;
	}
	public double[] getLAImax () {
		return LAImax;
	}

	public void setLAImax (double[] LAImax) {
		this.LAImax = LAImax;
	}
	public double getLAImaxBeforeFrost () {
		return LAImaxBeforeFrost;
	}

	public void setLAImaxBeforeFrost (double LAImaxBeforeFrost) {
		this.LAImaxBeforeFrost = LAImaxBeforeFrost;
	}

	public void setLAImaxsp (int sp, double LAImax) {
		this.LAImax[sp] = LAImax;
	}

	public double[] getLAI () {
		return LAI;
	}

	public void setLAI(double[] LAI) {
			this.LAI = LAI;
	}

	public double getBiomassOfLeaves (int sp) {
		return biomassOfLeaves[sp];
	}

	public void setBiomassOfLeaves(int sp, double v) {
		this.biomassOfLeaves[sp] = v;
	}

	public double getPotleaf (int sp) {
		return potleaf[sp];
	}

	public void setPotleaf(int sp, double v) {
		this.potleaf[sp] = v;
	}

	public double getStrat (int sp) {
		return strat[sp];
	}

	public void setStrat (int sp, double strat) {
		this.strat[sp] = strat;
	}

	public double[] getWAI () {
		return WAI;
	}

	public void setWAI (double[] wAI) {
		this.WAI = wAI;
	}

	public double[] getClumping () {
		return clumping;
	}

	public void setClumping (double[] clumping) {
		this.clumping = clumping;
	}

	public double[] getNitrogen () {
		return nitrogen;
	}

	public void setNitrogen (double[] nitrogen) {
		this.nitrogen = nitrogen;
	}

	public double[][] getStrat_sp () {
		return strat_sp;
	}

	public void setStrat_sp (double[][] stratSp) {
		this.strat_sp = stratSp;
	}

	public double[][] getWAI_sp () {
		return WAI_sp;
	}

	public void setWAI_sp (double[][] WAISp) {
		this.WAI_sp = WAISp;
	}

	public FmCanopyWaterReserves getCanopyWaterReserves () {
		return canopyWaterReserves;
	}

	public void setCanopyWaterReserves (FmCanopyWaterReserves canopyWaterReserves) {
		this.canopyWaterReserves = canopyWaterReserves;
	}

	public FmCanopyEvergreen getCanopyEvergreen () {
		return canopyEvergreen;
	}

	public void setCanopyEvergreen (FmCanopyEvergreen canopyEvergreen) {
		this.canopyEvergreen = canopyEvergreen;
	}

	public void addLayer (FmCanopyLayer l) {
		if (canopylayers == null) {
			canopylayers = new ArrayList ();
		}
		canopylayers.add (l);
	}

	public Collection getLayers () {
		return canopylayers;
	}

//	public void updateFmCanopy (double[] LAIint, double[] WAIint, double[] clumpingint, double[] nitrogenint
//			/*double[][] speciesProportionint*/) {
//		setLAI (LAIint);
//		setWAI (WAIint);
//		setClumping (clumpingint);
//		setNitrogen (nitrogenint);
//	}

	// ****************************************************************************************
	// aerodynamic values: calculate half hourly aerodynamics values (Dufr?ne et
	// al., 2005).

	public double[] getAerodynamicResistances (double V, double Ta, double H, double LAI, double WAI,
			double CrownRatioMean) {

		// aim calculation of aerodynamics values
		// parameterisation : 2 sources, Huntingford et al. (1994)

		// Log.println(settings.logPrefix+"DynaclimTest", "V="
		// +V+" CrownRatioMean= "+CrownRatioMean+" H= "+H);
		double[] aerodynamicResistances = new double[2];
		double rac;
		double ras;
		double Za;
		double dep;
		double Z0;
		double ra_air_ref;
		double ra_ref_sol;
		double Z0sol = 0.1; // soil rugosity
		double Kar = 0.4; // Karman constant
		double PAI = WAI + LAI;
		// double [] Bt= new double [nveg];
		//double hsource;
		double Vsoil;
		double na;

		// formulation fromp Lindroth 1993
		Za = 2 + H; // measured height kept equal to tree heightdep= H*(0.408*(1-Math.exp(-0.328*PAI))+0.45);
		dep= H*(0.408*(1-Math.exp(-0.328*PAI))+0.45);
		//Z0= H*(0.096*Math.exp(-0.611*PAI)+0.051);
		Z0=0.1;
		rac= Math.pow(Math.log ((Za - dep) / Z0),2)/(Math.pow(Kar,2)*V);
		na = 2.6 * Math.pow (LAI, 0.36);
		Vsoil=Math.exp (-na * (1 - Z0sol / H))*V;
		ras= Math.pow(Math.log ((Za - dep) / Z0sol),2)/(Math.pow(Kar,2)*Vsoil);




		// wind must be different from 0
		/*if (LAI > 0) {
			Za = 2 + H; // measured height kept equal to tree height
			dep = 2. / 3. * H;
			Z0 = 0.1 * H;
			na = 2.6 * Math.pow (LAI, 0.36);

		} else {
			dep = 0;
			Z0 = Z0sol;
			na = 1;
		}

		na = Math.min (na, 3.62); // na is limited
		double Vnew = Math.max (V, 0.2);
		//double Vnew = V;
		double ustar = Vnew * Kar / Math.log ((Za - dep) / Z0);

		double Z0h = Z0 * Math.exp (-6.27 * Kar * (Math.pow (ustar, 1. / 3)));
		// crown height
		if (CrownRatioMean > 0) {
			hsource = CrownRatioMean * H;
		} else {
			hsource = dep + Z0h;
		}

		double vk_ustar = Math.log ((Za - dep) / Z0) / (Vnew * Math.pow (Kar, 2));

		// wind speed at canopy - atmospher interface (m/s)
		double Vh = Math.log ((H - dep) / Z0h) / Math.log ((Za - dep) / Z0h) * Vnew;

		// wind speed at source height(m/s)
		double Vsource = ustar / Kar * Math.log ((H - dep) / Z0) * Math.exp (-na * (1 - hsource / H));

		// diffusivity coef. in Za (p.ex. Mohan & Srivastava)
		double K0 = ((Math.pow (Kar, 2)) * (Za - dep) / (Math.log ((Za - dep) / Z0h)));

		// diffusivity coef. in source point (0.75*hauteur)
		double Kd = Kar * ustar * (H - dep);

		// level source in canopy : ref= hsource
		// wind profile : logarithmic above canopy then negative exponetial
		// inside
		// ordre de grandeurs pour les for?ts 8 ? 15 s.m-1 (???)
		// Lindroth 1993 BLM : LAI= 7 : Rac=200 s/m (v=0.5 m/s), Rac=25 s/m (v=4
		// m/s)
		// Lindroth 1993 BLM : LAI= 0 : Rac=50 s/m (v=0.5 m/s), Rac=10 s/m (v=4
		// m/s)

		//rac = vk_ustar
		//		* (Math.log ((Za - dep) / (H - dep)) + (H / (na * (H - dep)))
		//				* (Math.exp (na * (1 - (hsource) / H)) - 1));



		double ra_air_hauteur = vk_ustar * (Math.log ((Za - dep) / (H - dep)));
		double ra_hauteur_ref = vk_ustar * (H / (na * (H - dep)) * (Math.exp (na * (1 - (hsource) / H)) - 1));
		ra_air_ref = ra_air_hauteur + ra_hauteur_ref;

		// soil aeorynamic resistance
		ras = vk_ustar
				* (Math.log ((Za - dep) / (H - dep)) + (H / (na * (H - dep))) * (Math.exp (na * (1 - (Z0sol) / H)) - 1));

		// ustarsol= Vsource*Kar/(log(Hsource/Z0sol)); //m/s
		// Vsubcanop= ustarsol/Kar*Math.log(3./Z0sol); //m/s ? 3 m de hauteur
		// (Barbeau)

		ra_ref_sol = vk_ustar
				* ((H / (na * (H - dep))) * (Math.exp (na * (1 - (Z0sol) / H)) - Math.exp (na * (1 - (hsource) / H))));

		// boundary layer resistance Aphalo & Jarvis 1993 added by Davi 2009

		// temperature effect Amthor 94

		double Dw = 24.2 * 1e-6 * Math.pow (Ta / 293.15, 1.75); // for water
		double Dh = 21.5 * 1e-6 * Math.pow (Ta / 293.15, 1.75); // for heat

		// for (int j= 0; j < nveg; j++){
		// Bt[j]= 0.004*Math.pow(ls[j]/Vnew,0.5);
		// gbw[j]= Dw/Bt[j]; // for water m.s-1
		// gbh[j]= Dh/Bt[j]; // for heat m.s-1
		// gbc[j]= gbw[j]/1.37; // for Co2 m.s-1
		// }
		*/

		aerodynamicResistances[0] = rac; // canopy
		aerodynamicResistances[1] = ras; // soil


//	Log.println(settings.logPrefix+"aerodynamic", rac+";"+ ras+";"+H+";"+dep+";"+PAI+";"+V+";"+Vsoil+";"+Z0+";"+na);


		return aerodynamicResistances;

	} // end of method

	// ********************************************************************************
	// Campbell.f
	//
	// Computation of the leaf angle distribution function value (freq)
	// Ellipsoidal distribution function caracterised by the average leaf
	// inclination angle in degree (ala)
	// Campbell 1986
	//
	// ********************************************************************************

	public double[] getFreq (double ala) {

		// ala is in degrees

		int n = 18; // leaf class number
		double[] tx2 = { 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85 };
		double[] tx1 = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90 };
		double[] x = new double[n];
		double[] tl1 = new double[n];
		double[] tl2 = new double[n];
		double[] freq = new double[n];

		for (int i = 0; i < n; i++) {
			x[i] = (tx2[i] + tx1[i]) / 2;
		}

		// conversion in radian
		for (int i = 0; i < n; i++) {
			tl1[i] = tx1[i] * Math.atan (1.) / 45;
			tl2[i] = tx2[i] * Math.atan (1.) / 45;
		}

		double excent = Math.exp (-1.6184e-5 * Math.pow (ala, 3) + 2.1145e-3 * ala * ala - 1.2390e-1 * ala + 3.2491);
		double sum = 0;
		for (int i = 0; i < n; i++) {
			double x1 = excent / Math.pow (1. + Math.pow (excent, 2) * Math.pow (Math.tan (tl1[i]), 2), 0.5);
			double x2 = excent / Math.pow (1. + Math.pow (excent, 2) * Math.pow (Math.tan (tl2[i]), 2), 0.5);

			if (excent == 1) {
				freq[i] = Math.abs (Math.cos (tl1[i]) - Math.cos (tl2[i]));
			} else {
				double alpha = excent / Math.pow (Math.abs (1. - Math.pow (excent, 2)), 0.5);
				double alpha2 = Math.pow (alpha, 2);
				double x12 = Math.pow (x1, 2);
				double x22 = Math.pow (x2, 2);

				if (excent > 1) {
					double alpx1 = Math.pow (alpha2 + x12, 0.5);
					double alpx2 = Math.pow (alpha2 + x22, 0.5);
					double dumpx = x1 * alpx1 + alpha2 * Math.log (x1 + alpx1);
					freq[i] = Math.abs (dumpx - (x2 * alpx2 + alpha2 * Math.log (x2 + alpx2)));
				} else {
					double almx1 = Math.pow (alpha2 - x12, 0.5);
					double almx2 = Math.pow (alpha2 - x22, 0.5);
					double dummx = x1 * almx1 + alpha2 * Math.asin (x1 / alpha);
					freq[i] = Math.abs (dummx - (x2 * almx2 + alpha2 * Math.asin (x2 / alpha)));
				}
			}
			sum = sum + freq[i];
		}

		for (int i = 0; i < n; i++) {
			freq[i] = freq[i] / sum;
			// Log.println(settings.logPrefix+"DynaclimTest", "freq=" +freq[i]+" ala= " +ala);
		}

		return freq;

	}

	// ****************************************************************************************
	// processTranspiration: calculate half hourly transpiration and evaporation
	// (Dufr?ne et al., 2005).

	/**
	 * Returns Rleaf, Rbark, Rcanop, TRpmh, ETRhcan.
	 */
	public double[] getTranspiration (FmSettings s, FmSpecies species, int sp, double T, double Tveg, double Rnhveg,
			double ea, double es, double Deltae, double Rac, double gc, double L) {

		double rleaf;
		double rbark;
		double rcanop;
		double rleafmax;
		double rbarkmax;
		double rcanmax;

		rleaf = this.canopyWaterReserves.getRleaf (sp);
		rbark = this.canopyWaterReserves.getRbark (sp);
		rcanop = rleaf+rbark;
		// rleafmax= this.canopyWaterReserves.getRleafmax(sp);
		rbarkmax = this.canopyWaterReserves.getRbarkmax (sp);
		// rcanmax= this.canopyWaterReserves.getRcanmax(sp);

		rleafmax = this.canopyWaterReserves.getChangeRleafmax (L, species);
		rcanmax = rleafmax + rbarkmax;

		double TRpmh;
		double[] transpirationResults = new double[3];

		// temperature effect on micrometeorologic variables (R= 8.31)

		double Rauh = -4.111 * T + 1289.764; // g/m3
		double Landah = -2.37273 * Tveg + 2501; // J.g-1
		double Psyh = 0.01 * s.Cph * (Rauh / 18) * s.R * (Tveg + 273.15) / Landah; // in
																					// Hpa
																					// mb.K-1
																					// Monteith
																					// p.181
		double Deltah = Landah * 18 * es / (s.R * Math.pow (Tveg + 273.15, 2)); // mb.K-1
																				// Monteith
																				// p.
																				// 10

		double TRpmhr = 0;
		double TRpmhc = 0;
		double ETPrh = 0;
		double ETPch = 0;
		double EVleafPot = 0;
		double EVbarkPot = 0;
		double EVleafh = 0;
		double EVbarkh = 0;
		double ETRhcan = 0;



		// Log.println(settings.logPrefix+"DynaclimTest", "Rnhveg=" +Rnhveg+" Deltae= "+Deltae+
		// "gc= "+gc);

		// calculaition of half hourly transpiration in mm
		// conversion factor from g/m2/s to mm/demi-heure : 3.6

		if (gc > 0) {
			TRpmhr = 3.6 * (Deltah * Rnhveg) / (Landah * (Deltah + Psyh * (1 + 1 / (gc * Rac))));
			TRpmhc = 3.6 * (Rauh * s.Cph * Deltae / Rac) / (Landah * (Deltah + Psyh * (1 + 1 / (gc * Rac))));
		}

		// half hourly potential evaporation (Penman-Monteith) in mm
		if (Rnhveg > 0) {
			ETPrh = 3.6 * (Deltah * Rnhveg) / (Landah * (Deltah + Psyh));
		} else {
			TRpmhr = 0;
		}

		if (Deltae > 0) {
			ETPch = 3.6 * (Rauh * s.Cph * Deltae / Rac) / (Landah * (Deltah + Psyh));
		} else {
			ETPch = 0;
			TRpmhc = 0;
		}

		double den = (Landah * (Deltah + Psyh));
		double numr = (Deltah * Rnhveg);
		double numc = (Rauh * s.Cph * Deltae / Rac);

		double TRpm = TRpmhr + TRpmhc;

		// calculation of omega jarvis coefficient of couplage
		double omega = (Deltah / Psyh + 1) / (Deltah / Psyh + 1 + 1 / (gc * Rac));

		double ETPh = ETPrh + ETPch;


		// Calculation of real evapotranspiration ETRhcan

		// split of ETP between leaves and trunc (ETPch is false when rainning)

		if (rcanop > 0) {
			EVleafPot = (rleaf / rcanop) * ETPh;
			EVbarkPot = (rbark / rcanop) * ETPh;
		} else {
			EVleafPot = 0;
			EVbarkPot = 0;
		}

		// taking into account of evaporation resistance and limitation by Rleaf
		// and Rbark
		if (L > 0 & rleafmax>0) {
			EVleafh = Math.min (rleaf, (rleaf / rleafmax) * EVleafPot);

		} else {
			EVleafh = 0;
		}

		if (rbarkmax>0) {
			EVbarkh = Math.min (rbark, (rbark / rbarkmax) * EVbarkPot);

		} else {
			EVbarkh = 0;
		}

		// water budget Bilan hydrique des r?servoirs feuilles et bois

		rleaf = rleaf- EVleafh;
		rbark = rbark- EVbarkh;
		rcanop = rleaf + rbark;

		this.canopyWaterReserves.setRleaf (sp, rleaf);
		this.canopyWaterReserves.setRbark (sp, rbark);
		this.canopyWaterReserves.setRcanop (sp, rcanop);
		this.canopyWaterReserves.setRleafmax (sp, rleafmax);

		TRpmh = (1 - rcanop / rcanmax) * TRpm;


		ETRhcan = TRpmh + EVleafh + EVbarkh;

		transpirationResults[0] = TRpmh;
		transpirationResults[1] = ETRhcan;
		transpirationResults[2] = ETPh;


		//Log.println(settings.logPrefix+"hourlyWaterReserve", ETPh+";"+ ETPch+";"+ETPrh+";"+Rnhveg+";"+Rac+";"+TRpmhr+";"+TRpmhc+";"+Deltah+";"+rleaf+";"+rcanop+";"+gc+";"+rbarkmax+";"+rleafmax+";"+EVleafh+";"+TRpmh+";"+ETRhcan);


		if (settings.output > 2) {
			Log.println(settings.logPrefix+"hourlyWater", TRpmh+";"+ ETPh+";"+rleaf+";"+rbark+";"+TRpm+";"+ETRhcan+";"+Deltae+";"+Rac+";"+Rauh+";"+Rnhveg);

		}
		return transpirationResults;

	} // end of processTranspiration



	public double getPotleafmin (int h, FmSettings settings, FmCell cell, FmSpecies species, double TRpmh, double potleafmin, int sp, double Ksmax) {
		// TRpmh in mm/h convertir en Kg/m2/S

		// capacitance cf Lousteau 0.040
		FmCanopy canopy= cell.getCanopy();
		double potleaf= canopy.getPotleaf(sp);
		double potsoil= cell.getSoil().getPotsoil();
		double RSoilToLeaves= species.RSoilToleaves; // resistance 10^3 MPa.m-2.s-1.Kg-1 cf Loustau et al., 1988
		double TR= TRpmh/3600*settings.frach;
				 // to Kg/m2/s
		//double RSoilToLeaves=1/Ksmax*3600;
		// Lousteau et al. 1998
		double deltaT=3600;
		double newPotleaf= potsoil-TR*RSoilToLeaves+(potleaf-potsoil+TR*RSoilToLeaves)*Math.exp(-deltaT/(RSoilToLeaves*species.CapSoilToleaves));
		//double newPotleaf= potsoil-TR*RSoilToLeaves;

		double newPotleafmin= Math.min(newPotleaf, potleafmin);
		canopy.setPotleaf(sp,newPotleaf);
		//Log.println (settings.logPrefix+"potmin",h+";"+potsoil+";"+newPotleaf+";"+newPotleafmin+";"+TR+";"+RSoilToLeaves
		//			+";"+settings.CapSoilToleaves);
		return newPotleafmin;

	}

	// */*********************************************************************************************************

	// method computing LAD distribution with canopy height
	// YOKOZAWA et al., Annals of Botany 78: 437?447, 1996

	static public double[] getLADz (double[] params, int h) {
		double u = params[0];
		double beta = params[1];
		double gama = params[2];
		double teta = params[3];

		double[] LADz = new double[h];

		for (int k = 0; k < h; k++) {

			double a1 = Math.pow (1 / beta / gama * Math.exp (gama * h) - 1, 2);
			double a2 = (1 - Math.pow (k / h, u)) * Math.pow (k, u - 1) / Math.pow (h, u);
			LADz[k] = 2 * teta * u * a1 * a2;
		}

		return LADz;
	}

	static public double[] getLADparams (FmSpecies species, FmSettings settings) {

		// fitted value need to be changed according to species and density

		double[] params = new double[4];

		params[0] = 3.2998; // conversion factor 0.65 in YOKOZAWA et al., Annals
							// of Botany 78: 437?447, 1996
		params[1] = 1.6549; // allocation parameters 0.6 m cm-1 in YOKOZAWA et
							// al., Annals of Botany 78: 437?447, 1996
		params[2] = 0.0555; // 0.1 in YOKOZAWA et al., Annals of Botany 78:
							// 437?447, 1996
		params[3] = 0.0089; // parameter for leaf area distribution 0.04 0.1 in
							// YOKOZAWA et al., Annals of Botany 78: 437?447,
							// 1996

		return params;
	}

	// calculation of layer Thickness
	public double[] getThickness (FmCell cell, double[][] speciesProportion, double[] LAI, FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmCanopy canopy= cell.getCanopy();
		int fmSpeciesNumber = fmSpeciesList.length;
		int nbstrat = FmSettings.NB_CANOPY_LAYERS;

		double[] thickness = new double[fmSpeciesNumber];
		double totalLAI = cell.getTotalLAI (LAI, settings);
		for (int l = 0; l < nbstrat; l++) {
			for (int sp = 0; sp < fmSpeciesNumber; sp++) {
				thickness[sp] = canopy.getStrat(sp) * speciesProportion[sp][l];
			}
		}
		return thickness;

	}


	public double getAverageLMA (FmCell cell, FmSettings settings, FmSpecies species, int sp) {
		int cohortesOfLeaves= (int)species.cohortesOfLeaves;
		double LMA0=0;
		double LAIsp=0;
		double Lnew=0;
		double LMAmoy=0;


		if (species.decidu==1) {
			LMA0=cell.getLMAcell()[sp];
			LAIsp= this.LAI[sp];

		}else { 	// ageing of LMA for evergreen
			//double sumMass= 0;
	        double LMA0average=0;
			double [] LMA0evergreen= canopyEvergreen.getLMA0evergreen(cell, settings, cohortesOfLeaves, sp);

	        for (int k = 0; k < cohortesOfLeaves; k++) {
				Lnew= Lnew+ canopyEvergreen.getLy(k);
				LMA0average= LMA0average+LMA0evergreen[k]*canopyEvergreen.getLy(k);
				//sumMass=sumMass+canopyEvergreen.getLy(k)*canopyEvergreen.getCoefOfLeafMass(k);
			}

	        LMA0average=LMA0average/Lnew;
			// case where k=0 for sun leaves
			canopyEvergreen.setLMAy(1,cell.getLMAcell()[sp]*canopyEvergreen.getCoefOfLeafMass(0)/(species.KLMA*Lnew)*(1-Math.exp(-species.KLMA*Lnew)) );


			for (int k = 1; k < cohortesOfLeaves; k++) {
				canopyEvergreen.setLMAy(k,LMA0evergreen[k]/(species.KLMA*Lnew)*(1-Math.exp(-species.KLMA*Lnew)));
			}

			LMA0=LMA0average;
			LAIsp= Lnew;
		}


		LMAmoy= LMA0*(1-Math.exp(-species.KLMA*LAIsp))/(species.KLMA*LAIsp);

	return LMAmoy;

	}

	// method to compute LAI in each layer when vertical distribution of species
	// is asymetric

	static public double getLaiCalc (FmCell cell, FmSpecies species, FmSettings settings, int sp, boolean initialize) {
		double LAI = 0;
		double constante=1;
		FmCanopy canopy=cell.getCanopy();
		FmWood wood= cell.getWood();
		double oldLAI=canopy.getLAImax ()[sp];
		double dbh=cell.getMeanDbh()[sp];
		double biomassOfAliveWood= wood.getBiomassOfAliveWood()[sp];
		double BSSth= species.TGSS * biomassOfAliveWood;
		double BSS= wood.getBiomassOfReserves()[sp];
		FmCanopyEvergreen canopyEver= canopy.getCanopyEvergreen();



		if (settings.fixedLAI==1) {
			if (species.decidu==1) {
				LAI = oldLAI* Math.pow (BSS / BSSth, species.CoefLAI3);
			} else {
				if (initialize) {
					LAI = oldLAI* Math.pow (BSS / BSSth, species.CoefLAI3);
				} else {
					LAI = oldLAI* Math.pow (BSS / BSSth, species.CoefLAI3)*canopyEver.getRatioPerCohorts(0);
				}
			}
		}
		if (settings.fixedLAI>1) {
			if (species.decidu==1) {
				LAI = constante + species.CoefLAI1 * Math.pow (dbh, species.CoefLAI2) * Math.pow (BSS / BSSth, species.CoefLAI3);
			} else {
				if (initialize) {
					LAI = constante + species.CoefLAI1 * Math.pow (dbh, species.CoefLAI2) * Math.pow (BSS / BSSth, species.CoefLAI3);
				} else {
					LAI = constante + species.CoefLAI1 * Math.pow (dbh, species.CoefLAI2) * Math.pow (BSS / BSSth, species.CoefLAI3);
				}
			}
		}

		//Log.println (settings.logPrefix+"LAI",LAI+";"+species.CoefLAI1+";"+dbh+";"+species.CoefLAI2+";"+BSS+";"+BSSth+";"+species.CoefLAI3);
		return LAI;
	}

//	static public void frecCalculate (FmSpecies species, double frec, double reci, double fdorm, double fa0,
//			double ifrost, int ijour, double Tmin, double Tmoyp, double Tmoy, double H) {
//
//		// forst temmperature effect on photosynthesis of coniferous (Nicolas
//		// Depierres)
//		// from subroutine photo_recovery_DDd in CASTANEA
//		// *** Recovery from Winter low temperatures ***
//		// Base Bergh 1998 FEM
//		// modifs:
//		// - diff?renciations cin?tiques acclim hivernal / printanier (Nippert
//		// 2004 FE)
//		// - processus recovery de type ODE1 (e.g. Makela 2004 AFM, GCB, base
//		// Pelkonen & Hari 1980)
//		// - pas de d?pendance Tsol (Suni 2003 GCB)
//		// (discutable : ok sur sites Hyy et Tha, mais sol gel? superficiel,
//		// quid de sites tundra ???)
//
//		int fthaw = 1;
//		double fb, dreci;
//		double frecold = frec;
//		double fa = 0;
//		double dfdorm = 0;
//
//		if (reci >= species.Rec0 && fdorm < 0.1) {
//			reci = 0.; // initialisation Reci ? l'entr?e en dormance
//
//		}
//
//		if (Tmin >= species.T2rec) {
//			fa = 0.;
//		} else if (Tmin > species.T3rec && Tmin < species.T2rec) {
//			fa = fa0 * (species.T2rec - Tmin) / (species.T2rec - species.T3rec);
//		} else if (Tmin <= species.T3rec) {
//			fa = fa0;
//		}
//
//		if (Tmin >= species.T1rec && Tmoyp <= species.T1rec) {
//			fb = 1.;
//		} else if (Tmin <= species.T2rec || Tmoyp < species.T2rec) {
//			fb = 0.;
//		} else {
//			fb = Math.min ((Tmin - species.T2rec) / (species.T1rec - species.T2rec), (Tmoyp - species.T2rec)
//					/ (species.T1rec - species.T2rec));
//		}
//
//		if (Tmin > species.T2rec) {
//			dreci = (Math.pow ((1. - reci / species.Rec0), species.powRec)) * (Tmoy - species.T2rec) * fthaw * fb;
//		} else {
//			dreci = -reci * fa;
//		}
//
//		// Sensibilite de la reprise ? l'endurcissement
//		// Endurcissement partiel => cinetique de reprise ralentie
//		if (species.castaneaCode == 7 || species.castaneaCode == 8) {
//			if (H < 9. && fa0 == species.fa0aut && dreci > 0) {
//				dreci = Math.min (dreci, 0.0025 * species.Rec0);
//			}
//		}
//
//		reci = reci + dreci;
//		if (reci >= 0.99 * species.Rec0) {
//			reci = species.Rec0;
//		}
//
//		if (reci < 0) {
//			reci = 0;
//		}
//
//		// R?ponse lin?aire Bergh
//		if (reci <= species.Rec0) {
//			frec = Math.max (0., species.frec0 + (1 - species.frec0) * reci / species.Rec0);
//		} else {
//			frec = 1.;
//		}
//
//		if (Math.abs (frec - 1.) < 1e-3) {
//			fa0 = species.fa0aut;
//		}
//
//		// Decline of assimilation due to low autumn temperatures
//		if (ijour == 1) {
//			fdorm = 1;
//			ifrost = 0;
//		}
//
//		if (Tmoy < 0 && ifrost == 0 && ijour > .227) {
//			ifrost = 1;
//		}
//
//		if (ifrost == 1) {
//			if (Tmin >= 0) {
//				dfdorm = species.fd0;
//			} else if (Tmin > species.T6rec && Tmin < 0) {
//				dfdorm = species.ffrost * Tmin / species.T6rec;
//			} else {
//				dfdorm = species.ffrost;
//			}
//		}
//		fdorm = Math.max (fdorm - dfdorm, species.frec0);
//
//		if (Math.abs (fdorm - species.frec0) < 1e-4) {
//			frec = species.frec0;
//			fa0 = species.fa0spg;
//		}
//	}// end of METHOD

} // end of class