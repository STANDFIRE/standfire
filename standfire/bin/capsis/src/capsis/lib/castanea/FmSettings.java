package capsis.lib.castanea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import capsis.kernel.AbstractSettings;

/**	FmSettings - List of model settings.
*	May have defaults or not. May be modified by user action during model
*	initialization.
*
*	@author Hendrik Davi - march 2006
*/
public class FmSettings extends AbstractSettings{

	public static final int NB_ITER= 5;	// others parameters
	public static final int NB_CANOPY_LAYERS= 5;	// others parameters
	public static final int NB_SOIL_LAYERS= 3;	// others parameters
	public static final int iVERTICAL_HETEROGENEITY=0; //0 ->No vertical heterogenity between species: equal repartition of LAI
														// 1 -> vertical heterogenity depending on canopy height and resp�ctives Leaf Area Density profiles

	public double frach; // hourly: 1, halfHourly: 0.5

	public String castaneaFile;
	public String basicMeteoFile; // useful for elevation relationships if continuous simulations accross elevation
	public int numberOfCastaneaSpecies;	// used in this simulation, ex: 2
	public Collection<Integer> usedCastaneaSpeciesCodes;
	public boolean castaneaOnly; // If true, only the Castanea model is run
	public int fixedLAI; // 0 LAI fixed between years, 1 LAI growth depends on reserves no needle or leaf fall caused by stresses, 2:LAI growth depends on reserves and DBH, needle or leaf fall caused by stresses
	public boolean fixedTronviv; // If true, Tronviv fixed between years
	public boolean simulationReproduction; // If true, simulation of reproduction
	public boolean simulationMortality; // If true, simulation of mortality
	public String phenoMode;  // choice of phenological model thermal time CASTANEA; Unichill_Threshold..
	public String stomataStress;
	public boolean vcmaxStress;


	//public double potsoilToWood;// to be determinate species by species

	public double Ca; // can be simulated according IPCC scenario
	public boolean variationOfCa;
	public boolean fixedAgeOftrees;
	public double thresholdLeafFall;

	// All the castanea species ex: 10
	public Map<Integer,FmSpecies> castaneaSpeciesMap;

	// List of the castanea species really used in the simulation
	// LinkedHashSet Keeps insertion order, no duplicates
	public LinkedHashSet<FmSpecies> usedFmSpecies; // fc-28.2.2013 changed Set to List to keep insertion order

	// fc + hd - 10.6.2008 - try to remove FmSpeciesProperties and move its properties into FmSpecies
	// CastaneaCode -> FmSpeciesProperties (including getRank ()) - fc + hd - 7.2.2008
	//~ public Map<Integer,FmSpeciesProperties> fmSpeciesProps;
	// fc + hd - 10.6.2008 - try to remove FmSpeciesProperties and move its properties into FmSpecies

	//~ public Map<Integer,Integer> rank2CastaneaCode;	// fc - 7.2.2008

	public Map climates;

	public String historyTimeStep;	// HALF_HOURLY, DAILY, MONTHLY, YEARLY

	public double latitude;
	public double longitude;
	public int initialDate;


	public double MRN;
	public double QDIX;
	public double tc;
	public double Tbase;
	public double Oi0;
	public double Ko0;
	public double Kc0;
	public double cVc;
	public double cVo;
	public double cKc;
	public double cKo;
	public double cgama;
	public double EaKc;
	public double EaKo;
	public double EaVc;
	public double EaVo;
	public double Eagama;
	public double rdtq;
	public double coefbeta;
	public double KAR;
	public double Cd;
	public double Zosol;
	public double K1;
	public double K2;
	public double K3;
	public double K4;
	public double K5;
	public double K6;
	public double K7;
	public double K8;
	public double Cph;
	public double R;
	public double IO;

	// to be improved to be added in stand file

	public double gsolmax;
	public double gsolmin;
	public double emsg;
	public double Asoil;
	public double Alit;
	public double SOLCLAYtop= 0.5045;	   // proportion d'argile dans le sol (0 - 30 cm)
	public double SOLCLAYsol= 0.4354;	    // proportion d'argile dans le sol (30 - 100 cm)
	public double SOLFINtop=  0.91;    // proportion de particules fines (argiles + limons) (0 - 30 cm)
	public double SOLFINsol= 0.8164;	   // proportion de particules fines (argiles + limons) (30 - 100 cm)
	public double SOLSANDtop= 0.0895;    // proportion de sables (0 - 30 cm)
    public double SOLSANDsol= 0.18;    // proportion de sables (30 - 100 cm)


	// SAIL initialization
	public int thetav;			// visual angle
	public int phi;				// azimuthal angle between sun and visual angle
	public int kdeb;				// ctype='multi'

	public double ratio;		//conversion ratio PAR/global


	// INSTANCIATION IN SUBCLASS

			// others parameters
	public int niter;			//
	public int nbCanopyLayers;
	public int nbSoilLayers;

	public double eps;

	public double T_oldinit; //first temperature previous day at the end of diurnal period
	public double frostEffectCoef;
	public int iFROST;

	public boolean rootsCavitation;
	public String temperatureEffectOnPhotosynthesis;  //Bernacchi Arrhenius
	public double output; //0 no outputs,1 yearly, 2 yearly and daily, 3 yearly, daily and hourly, 4 yearly, daily, hourly at leaf level
	public String allocRemain;
	public String allocSchema;

	public double GRFinit;
	public double rootsPLC50;

	//public double GBV;

	//public double reservesToReproduce;  // less indiv reproduce
	//public double reservesToMortality; //more individual die
	//public double BSSminCrit;  //-160 new SOM oct 2012
	//public double costOfOneSeed;  // Han et al 0.76*0.47gC  cupule+2*0.17*0.58GC seeds + 50% cost of respiratory/ 2 for one ssed
	//public double rateOfSeedProduction;
		//   	public double [] params_LAI = new double[4];

		   	//to be add in species parameter

	public double potbase;
	public double parameterPot;
	//public double RSoilToleaves; //450 pin; 23529 fagus Ventoux 33000 abies ventoux
	//public double CapSoilToleaves;


   //	params_LAI= null;

	// This prefix is added at the beginning of the log names, can be changed
	// in a script if several runs are launched in parallel
	// fc-11.6.2014 for Katalin Csillery
	public String logPrefix;

   	public FmSettings () {

		// default value: empty, no effect
		logPrefix = ""; // fc-11.6.2014 for Katalin Csillery

		castaneaSpeciesMap = new HashMap<Integer,FmSpecies> ();
		// SAIL initialization
	  		thetav=0;			// visual angle
			phi=90;				// azimuthal angle between sun and visual angle
			kdeb=2;				// ctype='multi'

			ratio=0.47;		//conversion ratio PAR/global

		// Keeps insertion order, no duplicates
		usedFmSpecies = new LinkedHashSet<FmSpecies> ();
	}



	public void addClimate (String key, FmClimate climate) {
		if (climates == null) {climates = new HashMap ();}
		climates.put (key, climate);
	}

	public Map getClimates () {return climates;}
	//~ public int getRank (int castaneaCode) {return (Integer) fmSpeciesProps.get (castaneaCode).getRank ();}




}



