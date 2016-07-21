
package capsis.lib.castanea;

import java.io.Serializable;

import jeeb.lib.util.Translator;

/**	A Species property for DynaClim Model.
*	DynaClim may manage one or several species.
*
*	@author Hendrik Davi - march 2006
*/
public class FmSpecies implements Serializable, Comparable {

	static {
		Translator.addBundle("DynaClim.model.FmSpecies");
	}
	static private boolean ascending;	// technical, for sort

	public int castaneaCode;

	// fc + hd - 10.6.2008
	//~ public int rank;
	public double litterHeight;
	public double topHeight;


	// Respiration
	public double CRF;		// Leaf construction cost
	public double CRRG;		// ...
	public double CRRF;
	public double CRBV;
	public double tronviv;
	public double branviv;
	public double rgviv;
	public double Lignroots;
	public double LIGNrl;
	public double LIGNll;
	public double LIGNfb;
	public double LIGNcb;
	public double LIGNcr;

	// biochemistry

	public double TGSS;
	public double leafNitrogen;
	public double coarseRootsNitrogen;
	public double fineRootsNitrogen;
	public double branchesNitrogen;
	public double stemNitrogen;
	public double reservesNitrogen;

	// allocation
	public double potsoilToWood;
	public double GBVmin;
	public double TMRF;


	// allometry

	public double ratioBR;
	public double RS;
	public double coefrac;
	public double ratioG;
	public double TMBV;
	public double SF;
	public double LMA0;
	public double KLMA;
	public double alphal;
	public double alphab;
	public double CrownArea1;
	public double CrownArea2;
	public double CoefLAI1;
	public double CoefLAI2;
	public double CoefLAI3;
	public double aGF;
	public double bGF;
	public double Phi;
	public double ros;

	public double defaultClumping;

	// reflectance
	public double RauwPIR;
	public double RauwPAR;
	public double RaufPIR;
	public double TaufPIR;
	public double RaufPAR;
	public double TaufPAR;
	public double emsf;

	// water parameters
	public double Tleaf;
	public double Tbark;
	public double CIA;
	public double CIB;
	public double propec;
	public double g0;
	public double g1;
	public double RSoilToleaves; //450 pin; 23529 fagus Ventoux 33000 abies ventoux
	public double CapSoilToleaves;

	//photosynthesis parameter
	public double EaVJ;
	public double ETT;
	public double JMT;
	public double NC;
	public double teta;

	// phenology
	public double TBASEA;
	public double TBASEB;
	public double TBASEC;
	public double NSTART;
	public double NSTART3;
	public double TSUMBB;
	public double HSUMFL;
	public double HSUMLMA;
	public double TSUMLFAL;

	//FitlibUnichill_Threshold

	public double t0;
	public double Vb;
	public double d;
	public double e;
	public double Fcrit;
	public double Ccrit;

	// Gaüzere model
	public double a1;
	public double a2;
	public double a3;
	public double C50;


	public double decidu;
	public double cohortesOfLeaves;

	//public double woodStop=TSUMLFAL/3; //end of growth just after the beginning of leaf fall
	//Hendrik 26/04/12 -> to stop wood growth in fall, even if leaves are still there
	public double woodStop;
	public double TminEffect;

	public double Rec0;
	public double frec0;
	public double T1rec;
	public double T2rec;
	public double T3rec;
	public double T6rec;
	public double fd0;
	public double ffrost;
	public double fa0spg;
	public double fa0aut;
	public double powRec;


	// dynamics parameters
	public double reservesToReproduce;  //  indiv reproduce
	public double reservesToMortality; // individual die
	public double BSSminCrit;  //
	public double costOfOneSeed;  // Han et al 0.76*0.47gC  cupule+2*0.17*0.58GC seeds + 50% cost of respiratory/ 2 for one ssed
	public double rateOfSeedProduction;


	/**	Constructor.
	*/
	public FmSpecies (int castaneaCode) {
		super ();
		this.castaneaCode = castaneaCode;
	}

	public int compareTo (Object o) throws ClassCastException {
		if (!(o instanceof FmSpecies)) {
				throw new ClassCastException ("Object is not a FmSpecies : "+o);}
		FmSpecies s = (FmSpecies) o;
		ascending = true;
		if (castaneaCode < s.castaneaCode) {
			return ascending ? -1 : 1;		// asc : t1 < t2
		} else if  (castaneaCode > s.castaneaCode) {
			return ascending ? 1 : -1;		// asc : t1 > t2
		} else {
			return 0;		// t1 == t2
		}
	}

	public void setG1 (double v) {g1=v;}
	public void setTSUMBB (double v) {TSUMBB=v;}


}


