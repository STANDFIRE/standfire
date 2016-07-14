package capsis.lib.crobas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


import jeeb.lib.util.Log;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Speciable;
import capsis.defaulttype.Species;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;
import capsis.util.TreeHeightComparator;
import flanagan.integration.Integration;

/**	Tree level characteristics used in Crobas (A. Makela, 1997. For. Sci.). 
*	@author R. Schneider - 20.5.2008
*   modified by Raymond Audet <raymond.audet@gmail.com> - 27.06.2008
*/
public class CTree extends Tree implements Numberable, Speciable {

	/**	This class contains immutable instance variables for a logical CTree.
	 *	@see Tree
	 */
	public static class Immutable extends Tree.Immutable {
		public CSpecies species;
	}

	private CStand stand;  // the scene now contains several stands fc-19.1.2011
	
	public double number;				// number of stems
	public String crobasLevel;	// CROBAS, PIPEQUAL_NODAL, PIPEQUAL_INTER_NODAL
	
	// Tree level variables
	
	// Heights
	public double height_double;		// replaces GTree.height
	public double Hc;					// crown length, m
	public double Hs;					// height to crown base, m
	public double Hb;					// average branch length, m
	public double Htrans;				// average transport root length, m

	// Diameter
	public double dbh_double;			// replaces GTree.dbh
	public double StemBasalArea;		// basal area of an individual stem, m2
	public double BasalArea;			// basal area of all the stems in the size class, m2 / ha

	// Volume
	public double Vtot;					// total stem volume, m3
	public double Vtot_ha;				// total stem volume of all the stems in the size class, m3 / ha
	public double Vmer;					// merchantable stem volume, m3
	public double Vmer_ha;				// merchantable stem volume of all the stems in the size class, m3 / ha

	// Interactive model parameters when whorl module used
	public double gammab;				// average pipe length in branches to pipe length in crown ratio 
	public double alphas;				// Pipemodel parameter: sapwood area in stem at crown base to foliage biomass ratio, m^2/kg
	public double alphab;				// Pipemodel parameter: cumulative branch sapwood area to foliage biomass ratio, m^2/kg
	public double phic;					// Form factor of sapwood in stem above crown
	public double phis;					// Form factor of sapwood in stem below crown
	public double psic;					// Form factor of senescent sapwwood in stem above crown
	public double psic1;				// Form factor of senescent sapwwood in stem above crown with age induced sapwood turnover
	public double psis;					// Form factor of senescent sapwwood in stem below crown
	public double psis1;				// Form factor of senescent sapwwood in stem below crown with age induced sapwood turnover
	public double ds0;					// specific sapwood in stem turnover rate per unit relative pruning
	public double db0;					// specific sapwood in branches turnover rate per unit relative pruning
	public double psib;					// Form factor
	public double psib1;				// Form factor at old crown base
	
	// Selfprunning parameters which can be set as a function of tree/stand variables
	public double aq;					// parameter related to self-prunning
	public double q;					// degree of control by crown coverage of self-prunning

	// foliage density - rs 25.02.09
	public double ksi;					// foliage biomass density
	public double z;					// fractal dimension of tree
	
	// Compartment weights
	public double Wf;					// foliage biomass of the tree, kg
	public double Ws;					// Sapwood biomass in the stem, kg
	public double Wds;					// Heartwood biomass in the stem, kg
	public double Wstem;				// stem biomass, kg
	public double Wt;					// Sapwood biomass in the transport roots, kg
	public double Wr;					// fine root biomass , kg
	public double Wb;					// Sapwood biomass in the branches, kg

	// Growth components
	public double LAI;					// effective leaf area index
	public double Rm;					// maintenance respiration, kg C / year
	public double Pg;					// photosynthesis, kg C / year
	public double G;					// net tree growth, kg DW / year
	public double crownCov;				// crown coverage
	public double crownCovCrownBase;	// crown coverage at crown base
	public double selfPrun;				// self pruning coefficient (0 <= selfPrun <= 1)

	// used and disused pipe areas
	public double As;					// sapwood area of stem at crown base, m^2
	public double Ab;					// total branch sapwood basal area, m^2
	public double At;					// total transport root sapwood basal area, m^2
	public double Ds;					// disused pipe area of stem at crown base, m^2

	// Growth rates
	public double iWf;
	public double iWr;
	public double iWs;
	public double iWds;
	public double iWb;
	public double iWt;

	public double iHc;
	public double iHs;
	public double iHb;
	public double iHtrans;

	public double iAs;
	public double iAb;
	public double iAt;
	
	public double iHeight;
	public double iStemBasalArea;
	public double dBApot;
	public double iDbh;
			
	public double iVtot;
	
	public double iN;					// stem mortality
	
	// Tree level variables used for tree and whorl module interactions
	
	public double rhos;					// Wood density of stem, kg/m^3
	public double rhosSap;				// Wood density of sapwood in stem, kg/m^3
	public double rhosHeart;			// Wood density of heartwood in stem, kg/m^3

	public double dbhWhorl;				// dbh calculated through whorl module
	public double gbl;					// used in calculating branch length of each whorl
	public int lowestLiveWhorl;			// index of the lowest live whorl
	public double AsOldH;				// sapwood area of stem at crown base at the previous year crown base, m^2
	public double DsOldH;				// disused pipe area of stem at crown base at the previous year crown base, m^2
	public double DsiCurrentAtOldBase;	// disused pipe area in the stem put on during the current year, at crown base of previous year, m^2
	public double AsCurrOldH;			// current sapwood area of stem at crown base at the previous year crown base, m^2
	public double DsCurrOldH;			// current disused pipe area of stem at crown base at the previous year crown base, m^2
	public double AsTurnOver;			// sapwood area turnover rate, m^2 / year
	public double DsTurnOver;			// disused pipe area turnover rate, m^2 / year
	
	public double Vtotd;				// disused pipe volume in stem, m^3
	public double VtotdWithAge;			// disused pipe volume in stem with aging, m^3
	public double VtotdCurrent;			// disused pipe volume of current year in stem, m^3
	public double VtotdCurrentWithAge;	// disused pipe volume of current year in stem with aging, m^3

	public double VtotdPrev;			// disused pipe volume in stem of previous year to crown base of this year, m^3
	public double VtotdCrownPrev;		// stem disused pipe volume in crown of previous year to crown base of this year, m^3
	public double VtotdBelowCrownPrev;  // stem disused pipe volume below crown of previous year to crown base of this year, m^3
	
	public double VtotdBeforeAge;				// disused pipe volume in stem before aging, m^3
	public double VtotdCrownBeforeAge;			// disused pipe volume in stem above crown base, before aging, m^3
	public double VtotdBelowCrownBeforeAge;		// disused pipe volume in stem below crown base, before aging, m^3
	public double VtotdCurrentBeforeAge;		// disused pipe volume in stem produced in current year, before aging, m^3
	public double VtotdCurrentCrownBeforeAge;	// disused pipe volume in stem above crown base produced in current year, before aging, m^3
	
	public double VtotdCrown;				// stem disused pipe volume in crown, m^3
	public double VtotdBelowCrown;			// stem disused pipe volume below crown, m^3
	public double VtotdCrownWithAge;		// stem disused pipe volume in crown with ageing, m^3
	public double VtotdCurrentCrown;		// stem current disused pipe volume in crown, m^3
	public double VtotdCurrentWithAgeCrown;	// disused pipe volume of current year in stem in crown with age turnover due to ageing, m^3

	public double VtotWhorl;			// total volume calculated through whorl module, m^3
	public double VtotCrown;			// stem volume in crown, m^3
	public double VtotBelowCrown;		// total stem volume below crown base, m^3
	public double Vtots;				// sapwood volume in stem, m^3
	public double VtotsCrown;			// stem sapwood volume in crown, m^3
	
	public double maxDsiCurrent;		// maximum current disused pipe area in stem, m^3
	public double maxHbi;				// maximum crown radius, m
	public double bsapwd;				// cumulative branch sapwood area by branch length
	public double bsapar;				// cumulative branch sapwood area
	public double hrtarea0;				// cumulative branch disused pipe area of previous year to crown base of previous year
	public double hrtarea;				// cumulative branch disused pipe area to crown base of previous year
	public double bhtwd;				// cumulative branch heartwood area by branch length
	public double bhtwd0; 				// cumulative branch heartwood area by branch length of previous year
	public double bhtwd1;				// cumulative branch heartwood area by branch length at old crown base
	public double Sbranch;				// change in branch heartwood volume
	public double Sbranch1;				// change in branch heartwood volume at old crown base
	public double dsd;					// relative change in disused pipes in the stem
	public double dbd;					// relative change in disused pipes in the branches
	public double AsWhorl;				// sapwood area at crown base calculated through whorl module
	public double AbWhorl;				// branch sapwood area calculated through Whorl module
	public double AbWhorlOld;			// branch sapwood area calculated through Whorl module of previous year
	public double deltaDbi;				// branch disused pipe area due to ageing
	public double deltaDsi;				// stem disused pipe area due to ageing
	public double Db;					// total branch disused pipe area
	public double DbWithAge;			// total branch disused pipe area with ageing to crown base
	public double DbToPrevBase;			// total branch disused pipe area before ageing to crown base of previous year
	public double DbWithAgeToBase; 		// total branch disused pipe area with ageing to crown base current year 
	public double prevAbWithAge;		// total branch disused pipe area after heartwood wood formation due to ageing of previous year
	public double prevAsWithAge;		// total stem disused pipe area after heartwood wood formation due to ageing of previous year
	public double DbOld;				// total branch disused pipe area of previous year

	public double sumAsi;				// sum of stem sapwood area of all whorls where heartwood is formed due to ageing, m^3
	public double sumAbi;				// sum of branch sapwood area of all whorls where heartwood is formed due to ageing, m^3
	
	// fc + rs - 29.5.2008
	public double SumWfi;				// sum of foliage biomass in whorls, shoud equal Wf, kg
	public double SumWfiNodal;			// sum of foliage biomass in nodal whorls, kg
	public double SumWfiInterNodal;		// sum of foliage biomass in internodal whorls, kg

	protected Map<Integer,PipeQualWhorl> whorlMap;	// optional, if PipeQual is used, Map key is the Whorl index


	/**	Constructor for a new CTree.
	 *	@see Tree
	 */
	public CTree (	int			id,
					GScene		scene,
					CStand		stand, 
					int			age,
					double		height,
					double		dbh,
					CSpecies	species,
					double		number,
					double		Hc,
					String		crobasLevel
					) {
		super (id, scene, age, height, dbh, false);

		this.stand = stand;
		this.crobasLevel = crobasLevel;
		
		// GTree height and dbh are float, set to = -1 (unused),
		// accessors are redefined to use height_double and dbh_double
		this.height = -1;
		this.dbh = -1;
		setHeight (height);
		setDbh (dbh);

		((Immutable) immutable).species = species;
		this.number = number;
		this.Hc = Hc;

		CSpecies sp = getCSpecies ();

		// crown foliage density and fractal dimensions
		ksi = sp.ksiMin;
		z = sp.zMin;
		
		// set tree - whorl module interaction parameters to default parameters
		alphas = sp.alphas;
		alphab = sp.alphab;
		gammab = sp.cb;
		psic = sp.psic;
		psic1 = sp.psic;
		psis = sp.psis;
		psis1 = sp.psis;
		phic = sp.phic;
		phis = sp.phis;
//		ds0 = sp.ds0;
//		db0 = sp.db0;
		ds0 = sp.ds0*2;
		db0 = sp.db0*2;
		dsd = sp.ds1;
		dbd = sp.db1;
		psib = sp.psib;
		psib1 = sp.psib;
		gbl = sp.gbl;
		rhos = sp.rhos;
		
		// calculate self prunning parameters
		calculateSelfPrunningParameters ();
			
		// calculate average pipe length in branches
		Hb = sp.br1 * Math.pow(Hc, sp.br2);

		// calculate foliage biomass
		Wf = calculateWf (this);

		//----------- Others ---------------

		// Caution: height and dbh==-1 (float, UNUSED),
		// use getHeight () and getDbh () to get height_double and dbh_double

		// calculate tree state variables through allometric relationships
		Hs = getHeight ()-Hc;
		Htrans = sp.gammat * getHeight ();
		Ws = sp.rhos * alphas * (phis * Hs + phic * Hc) * Wf;
		Wt = sp.rhot * sp.alphat * sp.phit * Htrans * Wf;
		Wr = sp.alphar * Wf;
		Wb = sp.rhob * sp.alphab * sp.phib * Hb * Wf;

		Ab = sp.alphab * Wf;
		At = sp.alphat * Wf;
		As = alphas * Wf;

		StemBasalArea = Math.PI * getDbh () * getDbh () / 40000;
		
		// Vtot = (sp.vol1 + sp.vol2 * getDbh () + sp.vol3 * getHeight () * getDbh () * getDbh ()) / 1000;
		Vtot = calculateVtot (sp, getDbh (), getHeight ());
		
		iN = 0;
		
		// calculate tree state variables which aren't computed by tree growth (e.g. BasalArea, Vtot_ha)
		calculateStateVariables ();
	}
	
	public CStand getStand () {return stand;}
	
	/**	Calculate tree total volume 
	* NEW - EB - 2010-12-08
	*/
	public double calculateVtot (CSpecies sp, double Dbh, double Height) {
		double Vtot = (sp.vol1 + sp.vol2 * Dbh + sp.vol3 * Height * Dbh * Dbh) / 1000;
		return Vtot;
	}
	
	/**	Calculate tree leaf biomass
	*/
	public double calculateWf (CTree tree) {
		CSpecies sp = getCSpecies ();
		double Wf = sp.ksiMin * Math.pow (tree.Hc, 2*sp.zMin);
		return Wf;
	}
	//Vency to track WF
//	{System.out.println ("value of Wf in CTree= " +" " +Wf);};
	/**	Calculate crown length from leaf biomass
	*/
	public double calculateHc (CTree tree) {
		CSpecies sp = getCSpecies ();
		double Hc = Math.pow (tree.Wf / sp.ksiMin, 0.5 / sp.zMin);
		return Hc;
	}
	
		
	/**	Calculate crown length from DBH and tree height for initialisation
	*/
	public void calculateHcinitialisation (CTree tree) {
	}
	
	
	
	/** Calculate self prunning parameters used in Cmodel
	*/
	public void calculateSelfPrunningParameters () {
		CSpecies sp = getCSpecies ();
		q = sp.q;
		aq = sp.aq;
	}

	/**	Calculate new state variables from increments
	*/
	public void growth (double iWf1, double iWr1, double iWs1, double iWb1, double iWt1,
			double iHc1, double iHs1, double iHb1, double iHeight1,
			double iStemBasalArea1, double iVtot1, double iAs1, double iAb1, double iAt1,
			double iHtrans1, double iWds1) {

		CSpecies sp = getCSpecies ();

		if (iWf1 > 0) iWf = iWf1;
		if (iWr1 > 0) iWr = iWr1;
		if (iWs1 > 0) iWs = iWs1;
		if (iWb1 > 0) iWb = iWb1;
		if (iWt1 > 0) iWt = iWt1;
		if (iWds1 > 0) iWds = iWds1;

		if (iHc1 > 0) iHc = iHc1;
		if (iHs1 > 0) iHs = iHs1;
		if (iHb1 > 0) iHb = iHb1;
		if (iHtrans1 > 0) iHtrans = iHtrans1;
		if (iAs1 > 0) iAs = iAs1;
		if (iAb1 > 0) iAb = iAb1;
		if (iAt1 > 0) iAt = iAt1;
		
		// TEMPORARY, Robert must review this fc+rs-3.7.2012
		if (iHc <= 0) iHc = 0.01; // Epsilon growth

		iHeight = iHc + iHs;
		if (iStemBasalArea1 > 0) iStemBasalArea = iStemBasalArea1;
		iDbh = (200 * Math.pow ((StemBasalArea+iStemBasalArea)/Math.PI, 0.5)) - (200 * Math.pow (StemBasalArea/Math.PI, 0.5));
				
		if (iVtot1 > 0) iVtot = iVtot1;
		
		Wf += iWf;
		Wr += iWr;
		Ws += iWs;
		Wds += iWds;
		Wstem = Ws + Wds;
		Wb += iWb;
		Wt += iWt;

		Hc += iHc;
		Hs += iHs;
		Hb += iHb;
		Htrans += iHtrans;

		As += iAs;
		Ab += iAb;
		At += iAt;
				
		setHeight (getHeight () + iHeight);
		StemBasalArea += iStemBasalArea;
		setDbh (200 * Math.pow (StemBasalArea/Math.PI, 0.5));
		Vtot += iVtot;
		
		gammab = Hb / Hc;

	}

	/**	Calculate new state variables from increments.
	 * Alternate methode, however doesn't yield same results as 
	 * previous methode.
	*/
	public void growth (CTree tree) {

		CSpecies sp = getCSpecies ();

		if (iWf < 0) iWf = 0;
		if (iWr < 0) iWr = 0;
		if (iWs < 0) iWs = 0;
		if (iWb < 0) iWb = 0;
		if (iWt < 0) iWt = 0;
		if (iWds < 0) iWds = 0;

		if (iHc < 0) iHc = 0;
		if (iHs < 0) iHs = 0;
		if (iHb < 0) iHb = 0;
		if (iHtrans < 0) iHtrans = 0;
		if (iAs < 0) iAs = 0;
		if (iAb < 0) iAb = 0;
		if (iAt < 0) iAt = 0;
		
		iHeight = iHc + iHs;
		if (iStemBasalArea < 0) iStemBasalArea = 0;
		iDbh = (200 * Math.pow ((StemBasalArea+iStemBasalArea)/Math.PI, 0.5)) - (200 * Math.pow (StemBasalArea/Math.PI, 0.5));
				
		if (iVtot < 0) iVtot = 0;
		
		Wf += iWf;
		Wr += iWr;
		Ws += iWs;
		Wds += iWds;
		Wstem = Ws + Wds;
		Wb += iWb;
		Wt += iWt;

		Hc += iHc;
		Hs += iHs;
		Hb += iHb;
		Htrans += iHtrans;

		As += iAs;
		Ab += iAb;
		At += iAt;
				
		setHeight (getHeight () + iHeight);
		StemBasalArea += iStemBasalArea;
		setDbh (200 * Math.pow (StemBasalArea/Math.PI, 0.5));
		Vtot += iVtot;
		
		gammab = Hb / Hc;
	}

	/**
	* Create an Immutable object whose class is declared at one level of the hierarchy.
	* This is called only in constructor for new logical object in superclass.
	* If an Immutable is declared in subclass, subclass must redefine this method
	* (same body) to create an Immutable defined in subclass.
	*/
	protected void createImmutable () {immutable = new Immutable ();}

	/* accessors and setters
	*/
	public double getNumber () {return number;}

	public void setNumber (double v) {number = v;}
	
	public Map<Integer,PipeQualWhorl> getWhorls () {return whorlMap;}

	public Collection<PipeQualWhorl> getSortedWhorls () {
		if (whorlMap == null) {return null;}
		// Whorls in ws are sorted in decreasing order on the index property
		Collection<PipeQualWhorl> ws = new TreeSet<PipeQualWhorl> (whorlMap.values ());
		return ws;
	}

	/** If whorl module selected, method to add whorl
	*/
	public void addWhorl (PipeQualWhorl v) {
		if (whorlMap == null) {whorlMap = new HashMap<Integer,PipeQualWhorl> ();}
		whorlMap.put (v.index, v);
	}

	/**	Initialize whorls by interpolating whorls heights from total stem height
	*/
	public void initWhorls (CSettings sets) throws Exception {

		if (!isPipeQualLevel ()) return;  // fc-18.2.2011

		//calculate crown length for initialisation purposes to make sure
		//that crown length is in balance for the model -vg-22.10.11
		calculateHcinitialisation(this);
		
		CSpecies sp = getCSpecies ();
		PipeQualSpecies pqs = sp.getPipeQualSpecies ();
		if (pqs == null) {return;}

		int index0 = 1;
		int n = updateWhorlCollection (index0, 0, 0, 0, 0, sets);
		
		if (age == 1) {									// if age == 1, only 1 nodal whorl at the top of the stem

			index0 += 1;
			double shootLength = this.getHeight ();
			double shootBaseHeight = 0;
			double treeHeight = this.getHeight ();

			n = updateWhorlCollection (index0, shootLength, shootBaseHeight, age, treeHeight, sets);

		} else {										// if age > 1, calculate shoot lengths and nodal whorl locations as presented in A. Makela (2002) Functional ecoloagy

			calculatePreviousGrowth(sets, pqs);

		}


		calculateFoliageBiomassInNodalWhorls ();		// estimate foliage biomass which is in the nodal whorls

		whorls (null);									// go through the whorl module

		// called only at initialisation
		calculateTaperBelowCrown ();					// estimate stem taper between crown base and dbh, if Hs > dbh
		if (getHeight() > 2){
		verifyTaper ();									// verify if taper predicted by whorl is logical by comparing with 
		}												// taper equation
	}

	/**	Subclasses may distribute foliage biomass into nodal and inter nodal whorls
	*/
	public void calculateFoliageBiomassInNodalWhorls () {
		}
	
	/**	Add one whorl every year.
	*/
	public int updateWhorlCollection (int index, double shootLength, double shootBaseHeight,
			int age, double treeHeight, CSettings sets) {

		PipeQualWhorl w = new PipeQualWhorl (this, index);	// index = age
		w.height = treeHeight;
		w.age = age;
		w.nodal = true;
		addWhorl (w);
		return 1;
	}

	/**	Calculate taper below crown during initialisation.
	*/
	public void calculateTaperBelowCrown () throws Exception {

		if (!isPipeQualLevel ()) return;  // fc-18.2.2011
		if (whorlMap == null) {return;}

		if (Hs < 1.3) {return;}

		CSpecies sp = getCSpecies ();
		PipeQualSpecies pqs = sp.getPipeQualSpecies ();

		// Whorls in ws are sorted in decreasing order on the index property
		Collection<PipeQualWhorl> ws = new TreeSet<PipeQualWhorl> (whorlMap.values ());
		
		double DiaiCrownBase = 0;
		
		for (PipeQualWhorl w : ws) {	// from top to bottom
			if (w.x == 1) {	// whorl at crown base
				DiaiCrownBase = w.Diai;
 
			} else {
				if (w.x == 0 && w.height < Hs) {	// below crown base
					double a1 = Math.pow (getDbh () - DiaiCrownBase, 2);
					double a2 = (Hs - w.height) / (Hs - 1.3);
					double a3 = pqs.b2 * (Hs - w.height) + pqs.b3 * DiaiCrownBase;
					double a4 = Math.pow (a2, a3);
					w.Diai = Math.pow (a1 * a4, 0.5) + DiaiCrownBase;

					w.Atoti = Math.PI * w.Diai * w.Diai / 40000;
					w.Dsi = w.Atoti - w.Asi;
										
				}
			}
		}

	}
	
	/**	Compare predicted stem taper during initialisation with stem taper equation. 
	*/
	
	public void verifyTaper () throws Exception {
	}
	

	/**	Whorl module, activated for PipeQual (A. Makela 2002 Function Ecology, Makela and Makinen 2003 For Eco and Manag).
	*	The method calculates the whorl level parameters as listed in PipeQualWhorl.
	*	The prevTree parameter is the same tree one year earlier.
	*/
	public void whorls (CTree prevTree) throws Exception {

		if (!isPipeQualLevel ()) return;  // fc-18.2.2011
		if (whorlMap == null) {return;}

		CSpecies sp = getCSpecies ();
		PipeQualSpecies pqs = sp.getPipeQualSpecies ();

		// Whorls in ws are sorted in decreasing order on the index property
		Collection<PipeQualWhorl> ws = new TreeSet<PipeQualWhorl> (whorlMap.values ());

		PipeQualWhorl prevW = null;
		double[] hi = new double[ws.size ()];
		double[] Dsi = new double[ws.size ()];
		double[] Atoti = new double[ws.size ()];
		double[] DsiCurrenti = new double[ws.size ()];
		double[] Asi = new double[ws.size ()];
		double[] preDsi = new double[ws.size ()];
		double dsB = 0;
		double dsA0 = 0;
				
		// set whorl variables to 0 - NEW rs 20.03.09
		setWhorlToZero();
		
		int i = ws.size () - 1;
		
		// get lowest live whorl of previous year
		int lowestLiveWhorlPrev = 1;
		if (prevTree != null) {
			lowestLiveWhorlPrev = prevTree.lowestLiveWhorl;
		}
		
		// establish lowest live whorl of current year
		lowestLiveWhorl = calculateLowestLiveWhorl (ws, Hs);	
			
		// calculate relative whorl depth (x) and foliage biomass on the whorl (Wfi)
		calculateWfi (ws, lowestLiveWhorl);								
		
		for (PipeQualWhorl w : ws) {
			hi[i] = w.height;
			
			// verify lowest live whorl height
			if ((w.Wfi > 0) && (getWhorl(lowestLiveWhorl).height > w.height)) {
				lowestLiveWhorl = w.index;
			}

			// calculate whorl depth
			w.depth = getHeight () - w.height;						

			// calculate foliage biomass to stem sapwood area of the whorl, kg / m^2
			calculateAsx (w, prevW, sp.amin, sp.amax, sp.bs, sp.alphas);	

			// calculate foliage biomass to branch sapwood area of the whorl, kg / m^2
			calculateAbx (w, prevW, pqs.amin2, pqs.amax2, sp.bs, sp.alphab);		

			// calculate branch sapwood area of the whorl, m^2
			w.Abi = 0;
			if (w.abx != 0) {
				w.Abi = w.Wfi / w.abx;							
			}

			// calculate stem sapwood area of the whorl, m^2
			w.Asi = 0;
			if (prevW != null) {
				if (w.index >= lowestLiveWhorl) {
					w.Asi = prevW.Asi + w.Wfi / w.asx;				
				} else {
					w.Asi = prevW.Asi;
				}
			}
			
			// calculate stem sapwood area at crown base, m^2
			AsWhorl = Math.max (AsWhorl, w.Asi);							
			Asi[i] = w.Asi;

			// calculate disused pipe area put on this year in the branches of the whorl, 
			// by comparing with the previous year, m^2 and total disused pipe area in the branches
			w.Dbi = 0;
			if (prevTree != null) {
				PipeQualWhorl prevTreeW = prevTree.getWhorl (w.index);
				if (prevTreeW != null) {	// verify if we are at the top whorl
					w.DbiCurrent = Math.max (0, prevTreeW.Abi - w.Abi);		
					w.Dbi = prevTreeW.Dbi + w.DbiCurrent;			 		
					Db += w.Dbi;
					if (w.index >= lowestLiveWhorlPrev) {
						DbToPrevBase += w.Dbi;
					}
				}
			}
			
			// calculate previous year and current year disused pipe area in the branches
			if (w.index >= lowestLiveWhorlPrev) {
				hrtarea += w.Dbi;
				if (prevTree != null) {
					Collection<PipeQualWhorl> prevTreeWs = new TreeSet<PipeQualWhorl> (prevTree.whorlMap.values ());
					if (w.index < prevTreeWs.size()) {
						hrtarea0 += prevTree.getWhorl(w.index).Dbi;
					}
				}
			}

			// transfer the disused pipe area of the branches into the stem
			// to get the disused pipe area in the stem of this year, m^2
			w.DsiCurrent = 0;
			if (w.asx != 0) {
				double prevDsiCurrent = 0;
				if (prevW != null) {
					prevDsiCurrent = prevW.DsiCurrent;
				}
				w.DsiCurrent = w.abx * w.DbiCurrent / w.asx + prevDsiCurrent;
				dsB += w.abx * w.DbiCurrent / w.asx;
//				w.DsiCurrent = 0.08 * w.abx * w.DbiCurrent / w.asx + prevDsiCurrent;
//				dsB += 0.08 * w.abx * w.DbiCurrent / w.asx;
			}
			maxDsiCurrent = Math.max (maxDsiCurrent, w.DsiCurrent);		
			DsiCurrenti[i] = w.DsiCurrent;

			// disused pipe area in the stem put on in current year at previous year's crown base
			if (w.index == lowestLiveWhorlPrev) {
				dsA0 = dsB;
			}
			
			// calculate the total disused pipe area of the stem at each whorl, m^2
			w.Dsi = 0;
			if (prevTree != null) {
				PipeQualWhorl prevTreeW = prevTree.getWhorl (w.index);
				if (prevTreeW != null) {	// verify if we are at the top whorl
					w.Dsi = prevTreeW.Dsi + w.DsiCurrent;			
					preDsi[i] = prevTreeW.Dsi;
				}
			}
			Dsi[i] = w.Dsi;

			// get average pipe length of each whorl and maximum pipe length
			w.Hbi = calculateHbi (w, pqs.gamma, pqs.zbr);
			maxHbi = Math.max (maxHbi, w.Hbi);
			
			// sum sapwood area (bsapar) and cylindric volume of sapwood (bsapwd) in the branches
			bsapwd += w.Hbi * w.Abi;
			bsapar += w.Abi;
			
			// sum cylindric volume of disused pipes in the stem of current (bhtwd) and previous year (bhtwd0)
			if ((prevTree != null) && (w.index >= lowestLiveWhorlPrev)) {
				bhtwd += w.Dbi * calculateHbiFunction(w.depth, sp.br1, sp.br2, gbl);
				if (prevTree != null) {
					Collection<PipeQualWhorl> prevTreeWs = new TreeSet<PipeQualWhorl> (prevTree.whorlMap.values ());
					if (w.index < prevTreeWs.size()) {
						bhtwd0 += prevTree.getWhorl(w.index).Dbi * calculateHbiFunction(prevTree.getWhorl(w.index).depth, sp.br1, sp.br2, gbl);
					}
				}
			}
			
			prevW = w;
			i--;
		}
		
		// Calculate tree level variables used in whorlInteraction
		
		// Disused pipe area in the stem at crown base of previous year
		DsiCurrentAtOldBase = dsA0;
		
		// current disused pipe volume in all of the stem and above actual crown base
		VtotdCurrent = calculateSmalianVolume (ws.size (), hi, DsiCurrenti);
		VtotdCurrentCrown = calculateCrownVolume (ws.size (), hi, DsiCurrenti, lowestLiveWhorl);
		
		// Stem sapwood and disused pipe area at crown base
		AsCurrOldH = As;
		DsCurrOldH = Ds;
		
		// Stem sapwood and disused pipe area at crown base and cumulative branch disused pipe area at previous crown base 
		if (hi.length > 1 && prevTree != null) { 	// more than 1 whorl and prevTree exists
			AsOldH = prevTree.AsWhorl;
			DsOldH = prevTree.Ds;
			DbOld = prevTree.DbWithAge;

			try {
				LinearInterpolator l = new LinearInterpolator (hi, Dsi);
				this.DsCurrOldH = l.interpolate (prevTree.Hs);
			} catch (Exception e) {
				Log.println (Log.ERROR, "CTree.whorls ()",
						"Exception while calculating tree.DsCurrOldH (LinearInterpolator) Hs="+prevTree.Hs, e);
				throw e;
			}

		}
		
		// sapwood and disused pipe area increment
		DsTurnOver = DsCurrOldH - DsOldH;
		AsTurnOver = AsCurrOldH - AsOldH;
		
		// calculate Disused pipe volume in stem below and above crown base before turnover due to ageing
		VtotdBeforeAge = calculateSmalianVolume (ws.size (), hi, Dsi);
		VtotdCrownBeforeAge = calculateCrownVolume (ws.size (), hi, Dsi, lowestLiveWhorl);
		VtotdBelowCrownBeforeAge = calculateBelowCrownVolume (ws.size (), hi, Dsi, lowestLiveWhorl);
		
		if (prevTree != null) {
			VtotdCurrentBeforeAge = Vtotd - prevTree.VtotdWithAge;
			VtotdCurrentCrownBeforeAge = VtotdCrown - prevTree.VtotdCrown;
		}
		
		i = ws.size () - 1;
		
		// sapwood turnover due to ageing
		for (PipeQualWhorl w : ws) {
			
			if ((age - w.age) > sp.turnOverAge) {
				
				if (prevTree != null) {
					PipeQualWhorl prevTreeW = prevTree.getWhorl (w.index);
					double ds11 = 0;
					double ds12 = 0;
					if (prevTreeW != null) {	// verify if we are at the top whorl
						if (prevTreeW.Asi > 0) {
							ds11 = (w.Dsi - prevTreeW.Dsi)/prevTreeW.Asi;
						}
						if (prevTreeW.Abi > 0) {
							ds12 = (w.Dbi - prevTreeW.Dbi)/prevTreeW.Abi;
						}
					}
					if ((sp.db1 - ds11) > 0) {
						deltaDsi += (sp.db1 - ds11)*prevTreeW.Asi;
						w.DsiCurrent += (sp.db1 - ds11)*prevTreeW.Asi;
						w.Dsi += (sp.db1 - ds11)*prevTreeW.Asi;

					}
					prevAsWithAge += prevTreeW.Asi;
					if ((sp.db1 - ds12) > 0) {
						deltaDbi += (sp.db1 - ds12)*prevTreeW.Abi;
						w.Dbi += (sp.db1 - ds12)*prevTreeW.Abi;
					}
					
					prevAbWithAge += prevTreeW.Abi;
					sumAsi += w.Asi;
					sumAbi += w.Abi;
				}
				
				if ((prevTree != null) && (w.index >= lowestLiveWhorlPrev)) {
					bhtwd1 += w.Dbi * calculateHbiFunction(w.depth, sp.br1, sp.br2, gbl);
				}

				if (w.index >= lowestLiveWhorl) {
					DbWithAge += w.Dbi;
					DbWithAgeToBase += w.Dbi;
				}
			}
			
			maxDsiCurrent = Math.max (maxDsiCurrent, w.DsiCurrent);		
			DsiCurrenti[i] = w.DsiCurrent;
			Dsi[i] = w.Dsi;

			w.AbiTot = w.Abi + w.Dbi;
			AbWhorl += w.Abi;
			if (prevTree != null) {
				PipeQualWhorl prevTreeW = prevTree.getWhorl (w.index);
				if (prevTreeW != null) {	// verify if we are at the top whorl
					w.AbiTot = w.Abi + w.Dbi + prevTreeW.AbiTot;	// total branch basal area of the whorl (sapwood + disused pipe area + previous branch basa area)
				}
			}

			// total stem basal area of the whorl
			w.Atoti = w.Dsi + w.Asi;
			Atoti[i] = w.Atoti;

			// stem diameter of the whorl
			w.Diai = 200 * Math.pow (w.Atoti / Math.PI, 0.5);

			// go through branch module
            w.updateBranches(); // added by Raymond Audet <raymond.audet@gmail.com

			prevW = w;
			i--;
		}
		
		// Calculate tree level variables used in whorlInteraction - continued
		this.Ds = 0;
		if (hi.length > 1) {
			try {
				LinearInterpolator l = new LinearInterpolator (hi, Dsi);
				this.Ds = l.interpolate (this.Hs);
			} catch (Exception e) {
				Log.println (Log.ERROR, "CTree.whorls ()",
						"Exception while calculating tree.Ds (LinearInterpolator), Hs="+Hs, e);
				throw e;
			}
		}
		
		if (prevTree != null) {
			Sbranch = Math.max(0, sp.phib*(bhtwd - bhtwd0));
			Sbranch1 = Math.max(0, sp.phib*(bhtwd1 - bhtwd));
			AbWhorlOld = prevTree.AbWhorl;
		}
		
		VtotWhorl = calculateSmalianVolume (ws.size (), hi, Atoti);
		Vtotd = calculateSmalianVolume (ws.size (), hi, Dsi);
		
		VtotdPrev = calculateSmalianVolume (ws.size (), hi, preDsi);
		VtotdCrownPrev = calculateCrownVolume (ws.size (), hi, preDsi, lowestLiveWhorl);
		VtotdBelowCrownPrev = calculateBelowCrownVolume (ws.size (), hi, preDsi, lowestLiveWhorl);
		
		Vtots = VtotWhorl - Vtotd;

		VtotCrown = calculateCrownVolume (ws.size (), hi, Atoti, lowestLiveWhorl);
		VtotdCrown = calculateCrownVolume (ws.size (), hi, Dsi, lowestLiveWhorl);
		VtotBelowCrown = calculateBelowCrownVolume (ws.size (), hi, Atoti, lowestLiveWhorl);
		VtotdBelowCrown = calculateBelowCrownVolume (ws.size (), hi, Dsi, lowestLiveWhorl);
		VtotsCrown = VtotCrown - VtotdCrown;

		VtotdCurrentWithAgeCrown = VtotdCrown - VtotdCrownBeforeAge;
		VtotdCurrentWithAge = Vtotd - VtotdBeforeAge;

		// calculate total and merchantable volume as well as dbh
		double VmerWhorl = calculateMarchantableVolume(9.1, hi, Atoti);
		Vtot = VtotWhorl;
		Vtot_ha = Vtot * getNumber();
		Vmer = Vtot * VmerWhorl / VtotWhorl;
		Vmer_ha = Vmer * getNumber();
		
		dbhWhorl = calculateDbhFromWhorl (1.3, hi, Atoti);
//		setDbh(dbhWhorl);
//		StemBasalArea = Math.PI * Math.pow (getDbh()/200, 2);
//		BasalArea = StemBasalArea * getNumber ();
	}

	/** set calculated whorl level variables to 0
	 * rs 20.03.09
	 */
	
	public void setWhorlToZero () {
		
		Collection<PipeQualWhorl> ws = new TreeSet<PipeQualWhorl> (whorlMap.values ());

		AsWhorl = 0;
		AbWhorl = 0;
		Db = 0;
		DbWithAge = 0;
		DbWithAgeToBase = 0;
		DbToPrevBase = 0;
		maxDsiCurrent = 0;
		maxHbi = 0;
		bsapwd = 0;
		bsapar = 0;
		hrtarea0 = 0;
		hrtarea = 0;
		bhtwd0 = 0;
		bhtwd = 0;
		bhtwd1 = 0;
		prevAbWithAge = 0;
		prevAsWithAge = 0;
		sumAsi = 0;
		sumAbi = 0;
		
		deltaDsi = 0;
		deltaDbi = 0;
		
		DsiCurrentAtOldBase = 0;
		VtotdCurrent = 0;
		VtotdCurrentCrown = 0;
		AsOldH = 0;
		DsOldH = 0;
		AsCurrOldH = 0;
		DsCurrOldH = 0;
		AsOldH = 0;
		DsOldH = 0;
		DsTurnOver = 0;
		AsTurnOver = 0;
		VtotdBeforeAge = 0;
		VtotdCrownBeforeAge = 0;
		VtotdCurrentBeforeAge = 0;
		VtotdCurrentCrownBeforeAge = 0;
		
		rhos = 0;
		
		for (PipeQualWhorl w : ws) {
			w.x = 0;
			w.depth = 0;
			w.Wfi = 0;
			w.asx = 0;
			w.abx = 0;
			w.Abi = 0;
			w.Asi = 0;
			w.Dbi = 0;
			w.DbiCurrent = 0;
			w.AbiTot = 0;
			w.Dsi = 0;
			w.DsiCurrent = 0;
			w.Atoti = 0;
			w.Diai = 0;
			w.Hbi = 0;
		}
	}
	
	/** whorl interactions used during initialisation process
	 * rs 24.02.09
	*/;
	
	public void whorlInteractions () {
		if (whorlMap == null) {return;}

//		Collection<PipeQualWhorl> ws = getSortedWhorls ();
		PipeQualWhorl w0 = getWhorl (1);
		
		CSpecies sp = getCSpecies ();

		// Interaction related to average pipe length in the branches
		Hb = bsapwd / bsapar;
		gammab = Hb / Hc;
		
		// Pipe model parameters
		As = AsWhorl;
		alphas = sp.alphas;
		if (Wf > 0) {
			alphas = As / Wf;
		}

		alphab = sp.alphab;
		if (Wf > 0) {
			alphab = bsapar / Wf;
		}
		
		// form factors
		phic = sp.phic;
		if ((getHeight() - getWhorl(lowestLiveWhorl).height) > 0 && As > 0 && w0.Wfi == 0) {
			phic = VtotsCrown / ((getHeight() - getWhorl(lowestLiveWhorl).height) * AsWhorl);
		}

		phis = sp.phis;
		if (getWhorl(lowestLiveWhorl).height > 0 && As > 0  && w0.Wfi == 0) {
			phis = (VtotBelowCrown - VtotdBelowCrown) / (getWhorl(lowestLiveWhorl).height * AsWhorl);
		}
		
		// interaction parameters which cannot be calculuated upon initilization
		dsd = 0;
		dbd = 0;
		psic = sp.psic;
		psic1 = 0;
		psis = sp.psis;
		psis1 = 0;
		psib = sp.psib;
		psib1 = sp.psib;
		ds0 = sp.ds0;
		db0 = sp.db0;
		rhos = sp.rhos;
		
		// Calculate state variables using new tree parameters
		As = Wf * alphas;
		Ab = Wf * alphab;
		Ws = sp.rhos * Vtots;
		Wds = sp.rhos * Vtotd;
//		Wb = sp.raub * alphab * sp.phib * Hb * Wf;

	}

	/**	Calculate interaction between whorl and tree levels.
	*/
	
	public void whorlInteractions (CTree prevTree) {
		if (whorlMap == null) {return;}

		CSpecies sp = getCSpecies ();

		// Interaction related to average pipe length in the branches
		Hb = bsapwd / bsapar;
		gammab = Hb / Hc;

		// Pipe model parameters
		alphas = sp.alphas;
		if (Wf > 0) {
			alphas = AsWhorl / Wf;
		}
		
		alphab = sp.alphab;
		if (Wf > 0) {
			alphab = bsapar / Wf;
		}
		
		// declare temporary variables
		double AsPrev = 0;
		double bsapwd0 = 0;
		double h3 = 0;
		double DbWithAgeToBase0 = 0;

		// set interactive parameters to default values
		dsd = 0;
		dbd = 0;
		psic1 = 0;
		psis1 = 0;
		psib = sp.psib;
		psib1 = sp.psib;
		phic = sp.phic;
		phis = sp.phis;
		ds0 = sp.ds0;
		db0 = sp.db0;
		psis = sp.psis;
		psic = sp.psic;
		rhos = sp.rhos;
		rhosSap = sp.rhos;
		rhosHeart = sp.rhos;
		
		if (prevTree != null) {
			if (prevAsWithAge > 0) {
				dsd = deltaDsi/prevAsWithAge;
			}
			if (prevAbWithAge > 0) {
				dbd = deltaDbi/prevAbWithAge;
			}
			int baseIndex = prevTree.lowestLiveWhorl;
			AsPrev = prevTree.getWhorl(baseIndex).Asi;
			DbWithAgeToBase0 = prevTree.DbWithAgeToBase;
			
			// carry over interactive parameter values from previous year
			psic1 = prevTree.psic1;
			psis1 = prevTree.psis1;
			psib = prevTree.psib;
			psib1 = prevTree.psib1;
			phic = prevTree.phic;
			phis = prevTree.phis;
			psis = prevTree.psis;
			psic = prevTree.psic;
		}
		
		// wood density in stem
		if (VtotWhorl > 0) {
			rhos = Wstem / VtotWhorl;
		}
		if (Vtotd > 0) {
			rhosHeart = Wds / Vtotd;
		}
		if (Vtots > 0) {
			rhosSap = Ws / Vtots;
		}
		
		// form factors and turnover parameters
		if ((AsPrev * (getHeight() - getWhorl(lowestLiveWhorl).height) * dsd) > 0.0001) {
			psic1 = (VtotdCrown - VtotdCrownBeforeAge) / (AsPrev * (getHeight() - getWhorl(lowestLiveWhorl).height) * dsd);
		}
		
		if ((AsPrev * getWhorl(lowestLiveWhorl).height * dsd) > 0.0001) {
			psis1 = (VtotdBelowCrown - VtotdBelowCrownBeforeAge) / (AsPrev * getWhorl(lowestLiveWhorl).height * dsd);
		}
		
		double ds = 0;
		if (AsOldH > 0) {
			ds = DsiCurrentAtOldBase / AsOldH;
		}
		
		double db = 0;
		if (AbWhorlOld > 0) {
			db = (hrtarea - hrtarea0) / AbWhorlOld;
		}
	
		if ((getHeight() - getWhorl(lowestLiveWhorl).height) > 0 && AsWhorl > 0) {
			phic = VtotsCrown / ((getHeight() - getWhorl(lowestLiveWhorl).height) * AsWhorl);
		}

		if (getWhorl(lowestLiveWhorl).height > 0 && AsWhorl > 0) {
			phis = (VtotBelowCrown - VtotdBelowCrown) / (getWhorl(lowestLiveWhorl).height * AsWhorl);
		}
		
		if (prevTree != null) {
			
			double us = 0;
			if ((Hs - prevTree.Hs) > 0.000001) {
				us = Hs - prevTree.Hs;
			}
	
			double uc = 0;
			if ((Hc - prevTree.Hc) > 0.000001) {
				uc = Hc - prevTree.Hc;
			}
			
			int baseIndex = prevTree.lowestLiveWhorl;
			Collection<PipeQualWhorl> prevWs = new TreeSet<PipeQualWhorl> (prevTree.whorlMap.values ());
			
			for (PipeQualWhorl prevW : prevWs){
				bsapwd0 += prevW.Abi * calculateHbiFunction(prevW.depth, sp.br1, sp.br2, gbl);
			}

			if (prevTree.bsapar > 0) {
				h3 = prevTree.Hb;
			}
			
			if ((prevTree.bsapar*h3*db)>0) {
				psib = Sbranch / (prevTree.bsapar*h3*db);
			}
			
			if ((dbd*prevTree.bsapar*h3) > 0) {
				psib1 = Sbranch1 / (dbd*prevTree.bsapar*h3);
			}
			
			int prevNWhorls = prevWs.size ();
			double prevHs = prevTree.getWhorl(baseIndex).height;
			double h10 = prevTree.getWhorl(prevNWhorls).height - prevTree.getWhorl(baseIndex).height;
			double iAsRel = 0;
			if (AsPrev > 0) {
				iAsRel = (AsWhorl - AsPrev) / AsPrev;
			};
			double h1 = h10 + uc/2 + iAsRel * (h10/2 + uc/3);
			double h2 = prevHs + us/2 + iAsRel * (prevHs/2 + us/3); 
			
			if ((AsPrev * h2 * ds) > 0.0001) {
				psis = (VtotdBelowCrownBeforeAge - VtotdBelowCrownPrev) / (AsPrev * h2 * ds);
			}
			
			if ((AsPrev * h1 * ds) > 0.0001) {
				psic = (VtotdCrownBeforeAge - VtotdCrownPrev) / (AsPrev * h1 * ds);
			}
			
			if (us > 0.000001 && db > 0) {
				ds0 = Hc * ds / us;
				db0 = Hc * db / us;
			}
			
		}
		

	}

	
	/**	Calculate relative depth of an individual whorl.
	*	NEW: called from calculateWfi () - 30.5.2008
	*/
	public void calculateX (PipeQualWhorl w, PipeQualWhorl prevW) {
		if (w.height < Hs) {
			w.x = 0;
			// whorl just below the crown base : x = 1
			if (prevW != null) {
				if (prevW.x > 0 && prevW.x < 1) {
					w.x = 1;
				}
			}
		} else {
			if (Hc > 0) {
				w.x = (getHeight () - w.height) / Hc;
			}
		}
	}

	/**	Calculate relative depth of an individual whorl using lowest live whorl index.
	 * NEW rs - 18.03.09
	*/
	public void calculateX (PipeQualWhorl w, int lowestIndex) {
		if (w.height < getWhorl(lowestIndex).height) {
			w.x = 0;
		} else {
			if ((getHeight () - getWhorl(lowestIndex).height) > 0) {
				w.x = (getHeight () - w.height) / (getHeight () - getWhorl(lowestIndex).height);
			}
		}
	}
	
	/**	Calculate index of lowest live whorl.
	*/
	public int calculateLowestLiveWhorl (Collection<PipeQualWhorl> ws, double Hs) {
		
		int lowestIndex = ws.size ();		// set lowest live whorl to the highest one
		for (PipeQualWhorl w:ws) {
			if ((w.height >= Hs) && (getWhorl(lowestIndex).height > w.height)) {
				lowestIndex = w.index;
			}
		}
		return lowestIndex;
	}

	/**	Calculate x, then foliage biomass of an individual whorl.
	*	ws must be sorted from top to bottom whorl.
	*/
	public void calculateWfi (Collection<PipeQualWhorl> ws) throws Exception {

		if (!isPipeQualLevel ()) return;  // fc-18.2.2011
		
		PipeQualWhorl prevW = null;
		double sumWfi = 0;
		double prevHeight = Double.MAX_VALUE;	// check whorls order: from top to bottom whorl needed
		
		for (PipeQualWhorl w : ws) {
			
			if (w.height >= prevHeight) {
					throw new Exception ("CTree.calculateWfi needs an ordered whorl Collection (top to bottom whorls)");}
			
			// 1. calculate x
			calculateX (w, prevW);
			
			if (prevW == null) {	// top whorl
				w.Wfi = 0;
			} else {

				w.Wfi = 0;
				if (w.x != 0) {		// whorl in crown

					CSpecies sp = getCSpecies ();
					PipeQualSpecies pqs = sp.getPipeQualSpecies ();

					BetaFunction f = new BetaFunction();
					f.setP (pqs.pBeta);
					f.setQ (pqs.qBeta);

					Integration intgn = new Integration (f);
					intgn.setLimits (0d, 1d);
					int numberOfIntervals = 500;
					//~ double denom = intgn.trapezium (numberOfIntervals);
					double denom = intgn.gaussQuad (numberOfIntervals);

					intgn.setLimits (prevW.x, w.x);
					//~ double num = intgn.trapezium (numberOfIntervals);
					double num = intgn.gaussQuad (numberOfIntervals);

					if (denom > 0) {
						w.Wfi = this.Wf * num / denom;
					}

					if (w.x == 1) {		// whorl at crown base
						w.Wfi = this.Wf - sumWfi;
						w.Wfi = Math.max (w.Wfi, 0);	// to avoid slight negative values
					}
				}

			}
			//~ w.Wfi = Math.max (w.Wfi, 0);
			sumWfi += w.Wfi;
			
			prevW = w;
			prevHeight = w.height;
		}
	}

	/**	Calculate x, then foliage biomass of an individual whorl using lowest live whorl index.
	*	ws must be sorted from top to bottom whorl.
	*	NEW rs 18.03.09
	*/
	public void calculateWfi (Collection<PipeQualWhorl> ws, int lowestIndex) throws Exception {

		if (!isPipeQualLevel ()) return;  // fc-18.2.2011
		
		PipeQualWhorl prevW = null;
		double sumWfi = 0;
		double prevHeight = Double.MAX_VALUE;	// check whorls order: from top to bottom whorl needed
		
		for (PipeQualWhorl w : ws) {
			
			if (w.height >= prevHeight) {
				String w_msg = "w.height >= prevHeight, whorl index "+w.index+" height "+w.height+" prevHeight "+prevHeight; 
				throw new Exception ("CTree.calculateWfi needs an ordered whorl Collection (top to bottom whorls)"
						+"\n"+w_msg);
			}
			
			// 1. calculate x
			calculateX (w, lowestIndex);
			
			// 2. calculate whorl foliage biomass
			if (prevW == null) {	// top whorl
				w.Wfi = 0;
			} else {

				w.Wfi = 0;
				if (w.x != 0) {		// whorl in crown

					CSpecies sp = getCSpecies ();
					PipeQualSpecies pqs = sp.getPipeQualSpecies ();

					BetaFunction f = new BetaFunction();
					f.setP (pqs.pBeta);
					f.setQ (pqs.qBeta);

					Integration intgn = new Integration (f);
					intgn.setLimits (0d, 1d);
					int numberOfIntervals = 500;
					//~ double denom = intgn.trapezium (numberOfIntervals);
					double denom = intgn.gaussQuad (numberOfIntervals);

					intgn.setLimits (prevW.x, w.x);
					//~ double num = intgn.trapezium (numberOfIntervals);
					double num = intgn.gaussQuad (numberOfIntervals);

					if (denom > 0) {
						w.Wfi = this.Wf * num / denom;
					}

					if (w.x == 1) {		// whorl at crown base
						w.Wfi = this.Wf - sumWfi;
						w.Wfi = Math.max (w.Wfi, 0);	// to avoid slight negative values
					}
				}

			}
			//~ w.Wfi = Math.max (w.Wfi, 0);
			sumWfi += w.Wfi;
			
			prevW = w;
			prevHeight = w.height;
		}
	}

	/**	Calculate x, then foliage biomass of an individual whorl using A. Makela's fortran code.
	*	ws must be sorted from top to bottom whorl.
	*/
	
	public void calculateWfi (Collection<PipeQualWhorl> ws, int lowestIndex, double tester) throws Exception {

		if (!isPipeQualLevel ()) return;  // fc-18.2.2011
		
		PipeQualWhorl prevW = null;
		double sumWfi = 0;
		double prevHeight = Double.MAX_VALUE;	// check whorls order: from top to bottom whorl needed
		
		for (PipeQualWhorl w : ws) {
			
			if (w.height >= prevHeight) {
					throw new Exception ("CTree.calculateWfi needs an ordered whorl Collection (top to bottom whorls)");}
			
			// 1. calculate x
			calculateX (w, lowestIndex);
			
			if (prevW == null) {	// top whorl
				w.Wfi = 0;
			} else {

				w.Wfi = 0;
				if (w.x != 0) {		// whorl in crown

					CSpecies sp = getCSpecies ();
					PipeQualSpecies pqs = sp.getPipeQualSpecies ();

					double denom = caculateBetaFunction (pqs.pBeta,pqs.qBeta, 0, 1);
					double x1 = (getHeight () - prevW.height) / Hc;
					double x2 = (getHeight () - w.height) / Hc;
					double num = 0;
					if (x2 <= 1) {
						num = caculateBetaFunction (pqs.pBeta,pqs.qBeta, x1, x2);
					}
					
					double w0 = 0;
					if (denom > 0) {
						w0 =  this.Wf / denom;
					}
					
					w.Wfi = w0 * num;

				}

			}

			sumWfi += w.Wfi;
			
			prevW = w;
			prevHeight = w.height;
		}
	}
	
	/** Calculate beta function integral using trapeze method according to
	 * A. Makela's fortran code.
	*/
	/*Vency changement du private en protected*/
	protected double caculateBetaFunction (double p, double q, double x1, double x2) {
		
		int N = (int)Math.max (1,(x2-x1)*100 + 0.5);
	    double dx = (x2-x1)/N;

	    double w = 0;
	    double x = x1;

	    for ( int i = 0; i < N; i++ ) {

		    if ((x+dx) > 1) {
		    	x = 1 - dx;
		    }
			double A = Math.pow(x, p) * Math.pow(1.-x, q);
			double B = Math.pow(x+dx/2, p) * Math.pow(1.-x-dx/2, q);
			double C = B;
			double apu = Math.max(0,1-x-dx);
			double D = Math.pow(x+dx, p) * Math.pow(apu, q);
			w = w + (A + 2*B + 2*C + D)/6;
			x = x+dx;
	    }
	
	      double wwx = w*dx;
	      return wwx;
	}

	/**	Calculate foliage biomass to stem sapwood area ratio of a whorl.
	*/
	public void calculateAsx (PipeQualWhorl w, PipeQualWhorl prevW,
			double amin, double amax, double bs, double alphas) {
		if (prevW == null) {
			w.asx = 0;
		} else {
			if (w.x != 0) {
				// Modified according A. Makela's fortran code - rs - 12.11.2008
				w.asx = ((1/alphas) * w.depth / (bs+w.depth) + amin*bs / (bs+w.depth));

			} else {
				w.asx = prevW.asx;
			}
		}

	}

	/**	Calculate foliage biomass to branch sapwood area ratio of a whorl.
	*/
	public void calculateAbx (PipeQualWhorl w, PipeQualWhorl prevW,
			double amin2, double amax2, double bs, double alphab) {
		if (prevW == null) {
			w.abx = 0;
		} else {
			if (w.x != 0) {
				// Modified according A. Makela's fortran code - rs - 12.11.2008
				w.abx = ((1/alphab)* w.depth / (bs+w.depth) + amin2*bs / (bs+w.depth));
				
			} else {
				w.abx = prevW.abx;
			}
		}

	}

	/**	Calculate branch length of whorl.
	*/
	public double calculateHbi (PipeQualWhorl w, double gamma, double zbr) {
		// Modified according to A. Makela's fortran code - rs - 12.11.2008
		// Modified to calculate gbl - rs - 08.04.2009

		CStand stand = getStand();
		
		CSpecies sp = getCSpecies ();	// moved before call of calculateGbl to add  
										// Cspecies sp to list of arguments of calculateGbl  
		gbl = calculateGbl(stand, sp);
				
		// for debugging only
		if (stand.age == 0) {
			gbl = sp.gbl; // replaced 1.2 by sp.gbl
		}
		
//		CTree tree = (CTree) w.getTree ();
//		if (tree.age == 10) {
//			gbl = 1.0;
//		}
		// end debugging only
		
		double Hbi=0;
		if (w.Wfi > 0) {
			Hbi = calculateHbiFunction(w.depth, sp.br1, sp.br2, gbl);
		}

		return Hbi;
	}

	/**	Calculate branch length function per sae
	*/
	public double calculateHbiFunction (double depth, double br1, double br2, double gbl) {

		double Hbi = br1*Math.pow(depth,br2*gbl);
		return Hbi;
		
	}

	/** Calculate gbl
	 */
	
	public double calculateGbl (CStand stand, CSpecies sp) { // added Cspecies
		Collection trees = stand.getTrees ();
		CTree t1 = (CTree )stand.getTrees ().iterator ().next();
		CSpecies spt1 = t1.getCSpecies ();
		
		double maxHc = 0;
		double Ntot = 0;
		for (Iterator z = trees.iterator (); z.hasNext ();) {
			CTree t = (CTree) z.next ();
			if (t.Hc > maxHc) {
				maxHc = t.Hc - t.iHc;
			}
			Ntot += t.getNumber() - t.iN;;
		}
		
		double avoin = 0;
		
		if ((Ntot > 0) && (maxHc > 0)) {
			avoin = Math.sqrt(10000 / Ntot) / maxHc;
		}
		
		double gblTemp = spt1.gbl;
		
		if (avoin > sp.avoinMax) { // replaced 2 by sp.avoinMax
			gblTemp = spt1.gbl;
		} else {
			if (avoin < sp.avoinMin) { // replaced 0.6 by sp.avoinMin
				gblTemp = 1;
			} else {
				gblTemp = 1 + (avoin - sp.avoinMin) * (spt1.gbl - 1); // replaced 0.6 by sp.avoinMin
			}
		}
		gbl = gblTemp;
		
		return gbl;
	}
	
	/**	Calculate volume using Smalian's equation.
	*/
	public double calculateSmalianVolume (int numberOfWhorls, double[] hi, double[] area) {
		double vol = 0;
		for (int i = 0; i < numberOfWhorls - 1; i++) {
			vol += (area[i] + area[i+1] + Math.pow (area[i] * area[i+1], 0.5)) * (hi[i+1] - hi[i]) / 3;
		}
		return vol;
	}

	/**	Calculate stem volume in crown using Smalian's equation.
	*/
	public double calculateCrownVolume (int numberOfWhorls, double[] hi, double[] area, int index) {
		double[] area2 = new double[numberOfWhorls - index + 1];
		double[] hi2 = new double[numberOfWhorls - index + 1];
		int i2 = 0;
		for (int i = 0; i < area.length; i++) {
			if (i >= (index-1)) {
				hi2[i2] = hi[i];
				area2[i2] = area[i];
				i2++;
			} 
		}
		return calculateSmalianVolume (area2.length, hi2, area2);
	}

	/**	Calculate below crown volume using Smalian's equation.
	*/
	public double calculateBelowCrownVolume (int numberOfWhorls, double[] hi, double[] area, int index) {
		double[] area2 = new double[index];
		double[] hi2 = new double[index];
		for (int i = 0; i < index; i++) {
			hi2[i] = hi[i];
			area2[i] = area[i]; 
		}
		return calculateSmalianVolume (area2.length, hi2, area2);
	}
	
	/**	Calculate merchantable volume using Smalian's equation.
	*/
	public double calculateMarchantableVolume (double merchDia, double[] hi, double[] area) {
		int index = hi.length;
		double[] area2 = new double[index];
		double[] hi2 = new double[index];
		double merchArea = Math.PI * Math.pow (merchDia/200, 2);
		for (int i = 0; i < (index-1); i++) {
			if ((area[i] > merchArea) && (area[i+1] > merchArea)) {
				hi2[i] = hi[i];
				area2[i] = area[i];
			} else if ((area[i] > merchArea) && (area[i+1] < merchArea)) {
				hi2[i] = hi[i];
				area2[i] = area[i];
				hi2[i+1] = hi[i] + (hi[i+1]-hi[i]) * (area[i] - merchArea) / (area[i] - area[i+1]);
				area2[i+1] = merchArea;
				break;
			}
		}
		return calculateSmalianVolume (area2.length, hi2, area2);
	}

	/**	Calculate merchantable volume using Smalian's equation.
	*/
	public double calculateDbhFromWhorl (double dbhHeight, double[] hi, double[] area) {
		int index = hi.length;
		double G = 0;
		if (hi[index-1] < 1.3) {
			G = 0;
		} else {
			for (int i = 0; i < (index-1); i++) {
				if (hi[i] == dbhHeight) {
					G = area[i];
					break;
				} else if ((hi[i] < dbhHeight) && (hi[i+1] > dbhHeight)) {
					G = area[i] - (1.3 - hi[i]) * (area[i] - area[i+1]) / (hi[i+1] - hi[i]);
					break;
				} 
			}
		}
		double dbh = 200 * Math.pow (G / Math.PI, 0.5);
		return dbh;
	}
	
	/**	Calculate new values of state variables which are not calculated through increment (BasalArea, Vtot_ha),
	*	or set to 0 all state variables if no stems are present in the class
	*/
	public void calculateStateVariables () {
		if (getNumber () > 0) {
		
			BasalArea = StemBasalArea * getNumber ();
			Vtot_ha = Vtot * getNumber ();

			// controls
			if (whorlMap != null) {
				SumWfi = 0;
				SumWfiNodal = 0;
				SumWfiInterNodal = 0;
				for (PipeQualWhorl w : whorlMap.values ()) {
					SumWfi += w.Wfi;
					if (w.nodal) {
						SumWfiNodal += w.Wfi;
					} else {
						SumWfiInterNodal += w.Wfi;
					}
				}
				
			}
			
		} else {

			setNumber (0);			// number of stems
			setAge (0);
			
			// Crobas level variables
			Hc = 0;
			height_double = 0;
			dbh_double = 0;
			StemBasalArea = 0;
			BasalArea = 0;
			//~ age = 0;		
			Vtot = 0;		
			Vtot_ha = 0;	
			Vmer = 0;		
			Vmer_ha = 0;	
			Wf = 0;

			alphas = 0;
			psic = 0;
			psis = 0;
			phic = 0;
			phis = 0;
			ds0 = 0;
			db0 = 0;
			Hb = 0;

			Hs = 0;
			Ws = 0;
			Wt = 0;
			Wr = 0;
			Wb = 0;

			LAI = 0;
			Rm = 0;
			Pg = 0;
			G = 0;	
			crownCov = 0;
			selfPrun = 0;

			As = 0;
			Ab = 0;
			At = 0;
			Ds = 0;

			// PipeQual variables
			AsOldH = 0;
			DsOldH = 0;
			AsCurrOldH = 0;
			DsCurrOldH = 0;
			AsTurnOver = 0;
			DsTurnOver = 0;
			Vtotd = 0;
			VtotdCurrent = 0;
			Vtots = 0;
			VtotCrown = 0;
			VtotdCrown = 0;
			VtotdCurrentCrown = 0;
			VtotsCrown = 0;
			maxDsiCurrent = 0;
			maxHbi = 0;

			whorlMap = null;
		}			
	}

	/**	Returns the Whorl with the given index.
	*	If not found, returns null.
	*/
	public PipeQualWhorl getWhorl (int index) {
		return whorlMap.get (index);
	}

	/* Accessors and settors
	*/
	public double getHeight () {
		if (height != -1) {Log.println (Log.WARNING,
				"CTree.getHeight ()",
				"CAUTION: only height_double should be used in Crobas, GTree.height should always be -1, height="+height);}
		return height_double;
	}
	public double getDbh () {
		if (dbh != -1) {Log.println (Log.WARNING,
				"CTree.getDbh ()",
				"CAUTION: only dbh_double should be used in Crobas, GTree.dbh should always be -1, dbh="+dbh);}
		return dbh_double;
	}
	public void setHeight (double h) {height_double = h;}
	public void setDbh (double d) {dbh_double = d;}

	/**	Speciable, i.e. enable methods to access species specific parameters
	*/
	public Species getSpecies () {return ((Immutable) immutable).species;}
	public CSpecies getCSpecies () {return ((Immutable) immutable).species;}

	
	
	/**	Returns true if the tree if PIPEQUAL_NODAL or PIPEQUAL_INTER_NODAL.
	 * 	Returns false if the tree is only CROBAS.
	 * 	fc-18.2.2011 trying to simplify the species
	 */
	public boolean isPipeQualLevel () {
		return crobasLevel.equals (CSettings.PIPEQUAL_NODAL) 
				|| crobasLevel.equals (CSettings.PIPEQUAL_INTER_NODAL);
	}
	
	
	
	public String toString () {
		return "CTree_"+getId ()
				+", n = "+number
				+", "+getCSpecies ().getName()
				+", "+crobasLevel
				+", whorls "+(getWhorls() == null ? "null" : getWhorls().size())
				;
	}
	
	/**	
	 * A method to help debug trees height / whorls problems.
	 */
	public String traceWhorls () {
		String m = "";
		if (getWhorls() != null) {
			m += " whorls "+getWhorls().size();
			m += " tree height "+getHeight ();
			double topHeight = 0;
			for (PipeQualWhorl w : getWhorls().values ()) {
				if (w.height > topHeight) {
					topHeight = w.height;
				}
			}
			m += " top whorl height "+topHeight;
		}
		
		return "CTree_"+getId ()
				+", n = "+number
				+", "+getCSpecies ().getName()
				+", "+crobasLevel
				+m
				;
	}

	/**	Clone a CTree: first calls super.clone (), then clone the CTree
	*	instance variables.
	*/
	public Object clone () {
		try {
			CTree t = (CTree) super.clone ();

			if (whorlMap != null) {
				t.whorlMap = null;		// reinitialize map (needed)
				for (PipeQualWhorl w : whorlMap.values ()) {
					PipeQualWhorl w2 = (PipeQualWhorl) w.clone ();
					w2.setTree (t);
					t.addWhorl (w2);
				}
			}

			return t;
		} catch (Exception e) {
			Log.println (Log.ERROR, "CTree.clone ()",
					"Error while cloning tree, source tree="+toString ()
					+" "+e.toString (), e);
			return null;
		}
	}

	public String traceTree () {
		StringBuffer b = new StringBuffer (toString ());
		if (whorlMap == null) {
			b.append (" whorlMap=null");
		} else {
			double sum = 0;
			for (PipeQualWhorl w : getSortedWhorls ()) {
				sum += w.Wfi;
				b.append ("\n  w.x="+w.x+" w.Wfi="+w.Wfi+" sumWfi="+sum+" sumWfi/Wf="+sum/Wf);
			}
			b.append ("\n");
		}
		return b.toString ();
	}


	protected void calculatePreviousGrowth(CSettings sets, PipeQualSpecies pqs) {

		double[] ih = new double[age];
		double sumih = 0;
		for (int t = 0; t < age; t++) {
			ih[t] = 1 - Math.exp (pqs.p1 * (t+1));
			if (t == 0) {
				sumih = ih[t];
			} else {
				sumih = ih[t] + sumih;
			}
		}

		int cpt = 2;
		double shootBaseHeight = 0;
		for (int t = 0; t < age; t++) {
			ih[t] = getHeight () * ih[t] / sumih;


			int index0 = cpt;
			double shootLength = ih[t];
			int _age = t + 1;
			double treeHeight = shootBaseHeight + shootLength;
			if (t >= age - 1) {
				treeHeight = getHeight ();
			}

			int n = updateWhorlCollection (index0, shootLength, shootBaseHeight, _age, treeHeight, sets);
			cpt += n;
			shootBaseHeight += shootLength;
		}
	}

	/* Calculate crown density
		 * rs - 25.02.09
		 */
	public void calculateCrownDensity (CTree tree){
		
		CStand stand = (CStand) tree.getStand ();
		CSpecies sp = tree.getCSpecies ();
		
		// Sort trees on ascending heights
		Collection trees = new TreeSet (new TreeHeightComparator ());
		trees.addAll (stand.getTrees ());
		
		// calculate longest crown length
		double maxHc = 0;
		for (Iterator z = trees.iterator (); z.hasNext ();) {
			CTree t = (CTree) z.next ();
			if (t.Hc > maxHc) {
				maxHc = t.Hc;
			}
		}
		
		double Ntot = stand.N;
		double avoin = 0;
		
		if ((Ntot > 0) && (stand.maxHt > 0)) {
			// calculate mean available crown volume (m3) per meter of crown per tree
			// on the basis of stand density ??? (added Eric Beaulieu 2010-12-03)
			avoin = Math.sqrt(10000 / Ntot) / maxHc;
		}
		
		double ksiTemp = 0;
		double zTemp = sp.zMin;
		double gblTemp = sp.gbl;
		
		// validate ksi, z and gbl are inside empirical avoin range
		if (avoin > sp.avoinMax) {
			ksiTemp = sp.ksiMax;
			zTemp = sp.zMax;
			gblTemp = sp.gbl;
		} else {
			if (avoin < sp.avoinMin) {
				ksiTemp = sp.ksiMin;
				zTemp = sp.zMin;
				gblTemp = sp.gbl;

			} else {
//				ksiTemp = sp.zeta + (avoin - sp.avoinMin) * (sp.ksim - sp.zeta);
//				zTemp = sp.z + (avoin - sp.avoinMin) * (sp.zm - sp.z);
//				gblTemp = 1 + (avoin - sp.avoinMin) * (sp.gbl - 1);
				
				// calculate ksi as to represent empirical avoin range
				ksiTemp = sp.ksiMin + (avoin - sp.avoinMin) * (sp.ksiMax - sp.ksiMin)/(sp.avoinMax - sp.avoinMin);
				// calculate z as to represent empirical avoin range
				zTemp = sp.zMin + (avoin - sp.avoinMin) * (sp.zMax - sp.zMin)/(sp.avoinMax - sp.avoinMin);
				gblTemp = 1 + (avoin - sp.avoinMin) * (sp.gbl - 1)/(sp.avoinMax - sp.avoinMin);
			}
		}
		tree.ksi = Math.pow(ksiTemp, zTemp);
		tree.z = zTemp;
		tree.gbl = gblTemp;
	}

	
	public void calculateStemMortality (int numberOfStepsInYear) {
		CStand stand = getStand ();
				
		// calculate mortality
		CSpecies sp = getCSpecies ();

		double Gtemp = StemBasalArea - iStemBasalArea;						// get stem basal area
		
		double Diam = Math.pow(Gtemp*1.2732,0.5);										// get stem diameter 
		double dDiam = Math.pow(dBApot * numberOfStepsInYear, 0.5);					// get potential stem diameter increment
		if (Diam > 0){
			dDiam = 0.6366 * dBApot*numberOfStepsInYear / Diam;
		}
		double rNs = 0;
		double rN0 = 0;
		double term1 = 1 + sp.delta1*dDiam + sp.delta2*Diam*Diam;
		
		if ((getHeight () - iHeight) > 2) {
			if ((term1 > 0) && (1-1/term1 > 0)) {
				rNs = -1 * Math.log(1 - 1/term1);
			}
		} else {
			rN0 = sp.rNs0 * stand.N;
		}
		
		double stemMort = 0;
		
		if (stand.maxCrownCoverage > sp.rNs1) {
			stemMort = -1 * Math.pow(stand.maxCrownCoverage/sp.rNs1, sp.p) * rNs * getNumber() - rN0 * getNumber();
//			stemMort = -1 * Math.pow(stand.maxCrownCoverage/.8, sp.p)*rNs*tree.getNumber() - rN0 * tree.getNumber();
		} else {
			stemMort = -1 * rN0 * getNumber();
		}
		
		iN = stemMort;
		setNumber (getNumber () + stemMort/numberOfStepsInYear);
	}

	public String getPlotName() {
		return null;
	}

	public double getSi() {
		return 0;
	}

}
