package capsis.lib.organon;

import java.util.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.*;

/**
 * Organon: a link to the Orgrun.dll dynamic link library.
 *
 * @author Nathaniel Osborne, Doug Maguire, David W. Hann, F. de Coligny - August 2014
 */
public class OrgRun extends OrgTools {

	static public Map<Integer,String> SERROR_MAP;
	static public Map<Integer,String> TERROR_MAP;
	static public Map<Integer,String> SWARNING_MAP;
	static public Map<Integer,String> TWARNING_MAP;

 	static {
 		// Set jna.library.path
 		System.setProperty ("jna.library.path", System.getProperty ("java.library.path"));
 		//System.out.println ("jna.library.path: "+System.getProperty ("jna.library.path"));
 		createSERROR_MAP ();
 		createTERROR_MAP ();
 		createSWARNING_MAP ();
 		createTWARNING_MAP ();
 	}

	// Mapping ORGRUN.dll
	private interface OrgRunDll extends Library {
		OrgRunDll INSTANCE = (OrgRunDll) Native.loadLibrary("ORGRUN", OrgRunDll.class);

		void get_orgrun_edition_ (FloatByReference EDITION);

		void execute_ (IntByReference CYCLG, IntByReference VERSION, IntByReference NPTS,
				IntByReference NTREES1, IntByReference STAGE, IntByReference BHAGE,
				int[] TREENO, int[] PTNO, int[] SPECIES, int[] USER,
				int[] INDS, float[] DBH1, float[] HT1, float[] CR1, float[] SCR1, float[] EXPAN1,
				float[] MGEXP, float[] RVARS, float[] ACALIB, float[] PN,
				float[] YSF, FloatByReference BABT, float[] BART, float[] YST,
				int[] NPR, int[] PRAGE, float[] PRLH, float[] PRDBH, float[] PRHT,
				float[] PRCR, float[] PREXP, int[] BRCNT, int[] BRHT, int[] BRDIA,
				int[] JCORE,
				int[] SERROR, int[] TERROR, int[] SWARNING, int[] TWARNING,
				IntByReference IERROR,
				float[] DGRO, float[] HGRO, float[] CRCHNG, float[] SCRCHNG,
				float[] MORTEXP, IntByReference NTREES2, float[] DBH2, float[] HT2,
				float[] CR2, float[] SCR2, float[] EXPAN2, float[] STOR);
	}

	// Parameters for the get_orgrun_edition_ () function
	public FloatByReference EDITION;

	// Parameters for the execute_ () function
	public IntByReference CYCLG; // Organon cycle
	public IntByReference VERSION; // Version of Organon to run (1, 2, 3)
	public IntByReference NPTS; // Number of sample in the samples
	public IntByReference NTREES1; // Number of trees prior to growing
	public IntByReference STAGE; // Stand age
	public IntByReference BHAGE; // Breast height age
	public int[] TREENO = new int[2000]; // Tree identification number
	public int[] PTNO = new int[2000]; // Sample number corresponding to each tree
	public int[] SPECIES = new int[2000]; // Species for each tree
	public int[] USER = new int[2000]; // Code for user thinning
	public int[] INDS = new int[30]; // A set of indicator variables for the simulation
	public float[] DBH1 = new float[2000]; // Diameter breast height for each tree (in), prior to growing
	public float[] HT1 = new float[2000]; //  Total tree height for each tree (ft), prior to growing
	public float[] CR1 = new float[2000]; // Crown ratio for each tree, prior to growing
	public float[] SCR1 = new float[2000]; // Shadow crown ratio for the ith sample tree at the start of the growth period
	public float[] EXPAN1 = new float[2000]; // Expansion factor for each tree (tpa), prior to growing
	public float[] MGEXP = new float[2000]; // The plot/point expansion factor for the ith sample tree at the start of the growth period...
	public float[] RVARS = new float[30]; // Set of 9 additional indicator values
	public float[] ACALIB = new float[3*18]; // Prepared calibration values
	public float[] PN = new float[5]; // Number of pounds of nitrogen per acre at the ith application
	public float[] YSF = new float[5]; // Number of years since start of the run that the ith application of nitrogen fertilizer was applied...
	public FloatByReference BABT; // Basal area per acre of the stand just before the most recent removal of trees
	public float[] BART = new float[5]; // Basal area pre acre cut at the ith removal of trees
	public float[] YST = new float[5]; // Number of years since the start of the run that the ith removal of trees occurred
	public int[] NPR = new int[2000]; // Number of prunings conducted on the ith trees
	public int[] PRAGE = new int[2000*3]; // Age of the ith tree when the jth pruning was conducted on the tree
	public float[] PRLH = new float[2000*3]; // Lift height, in feet, for the ith tree when the jth pruning was conducted on the tree
	public float[] PRDBH = new float[2000*3]; // Dbh of the ith tree when the jth pruning was conducted on the tree
	public float[] PRHT = new float[2000*3]; // Total height of the ith tree when the jth pruning was conducted on the tree
	public float[] PRCR = new float[2000*3]; // Crown ratio of the ith tree when the jth pruning was conducted on the tree
	public float[] PREXP = new float[2000*3]; // Plot/point level expansion factor of the ith tree when the jth pruning was conducted on the tree
	public int[] BRCNT = new int[2000*3]; // Wood quality branch count of type j for the ith tree
	public int[] BRHT = new int[2000*40]; // Height to the jth branch on the ith tree
	public int[] BRDIA = new int[2000*40]; // Branch diameter of the jth branch on the ith tree
	public int[] JCORE = new int[2000*40]; // Diameter of the juvenile wood core at the jth branch on the ith tree
	public int[] SERROR = new int[35];
	public int[] TERROR = new int[2000*6];
	public int[] SWARNING = new int[9];
	public int[] TWARNING = new int[2000];
	public IntByReference IERROR;
	public float[] DGRO = new float[2000]; // The 5-year diameter growth rate for the ith sample tree
	public float[] HGRO = new float[2000]; // The 5-year height growth rate for the ith sample tree
	public float[] CRCHNG = new float[2000]; // The 5-year change in crown ratio for the ith sample tree
	public float[] SCRCHNG = new float[2000]; // The 5-year change in the shadow crown ratio for the ith sample tree
	public float[] MORTEXP = new float[2000]; // The plot/point level expansion factor for the 5-year mortality on the ith sample tree
	public IntByReference NTREES2; // Total number of sample trees measured in the stand at the end of the growth period
	public float[] DBH2 = new float[2000]; // Dbh for the ith sampled tree at the end of the growth period
	public float[] HT2 = new float[2000]; //  Total height of the ith sample tree at the end of the growth period
	public float[] CR2 = new float[2000]; // Crown ratio for the ith sample tree at the end of the growth period
	public float[] SCR2 = new float[2000]; // Shadow crown ratio for the ith sample tree at the end of the growth period
	public float[] EXPAN2 = new float[2000]; // The plot/point expansion factor for the ith sample tree at the end of the growth period
	public float[] STOR; // An array of 30 internal variables used by Organon which must not change over multiple calls of the Organon dll


	/**
	 * A get_orgrun_edition () function turning the Java typed parameters into JNA types before calling
	 * the get_orgrun_edition_ () function in the Orgedit dll.
	 */
	public float get_orgrun_edition () {
		EDITION = new FloatByReference (0);
		OrgRunDll.INSTANCE.get_orgrun_edition_ (EDITION);
		return EDITION.getValue ();
	}

	public void execute (int cyclg, int version, int npts,
				int ntrees1, int stage, int bhage,
				int[] treeno, int[] ptno, int[] species, int[] user,
				int[] inds, float[] dbh1, float[] ht1, float[] cr1, float[] scr1, float[] expan1,
				float[] mgexp, float[] rvars, float[] acalib, float[] pn,
				float[] ysf, float babt, float[] bart, float[] yst,
				int[] npr, int[] prage, float[] prlh, float[] prdbh, float[] prht,
				float[] prcr, float[] prexp, int[] brcnt, int[] brht, int[] brdia,
				int[] jcore
				/* int[] SERROR, int[] TERROR, int[] SWARNING, int[] TWARNING,
				IntByReference IERROR,
				float[] DGRO, float[] HGRO, float[] CRCHNG, float[] SCRCHNG,
				float[] MORTEXP, IntByReference NTREES2, float[] DBH2, float[] HT2,
				float[] CR2, float[] SCR2, float[] EXPAN2, float[] STOR */ ) {

		System.out.println ("[OrgRun.execute ()] entering method, cyclg: "+cyclg+"...");

		CYCLG = new IntByReference (cyclg);
		VERSION = new IntByReference (version);
		NPTS = new IntByReference (npts);
		NTREES1 = new IntByReference (ntrees1);
		STAGE = new IntByReference (stage);
		BHAGE = new IntByReference (bhage);
		fillArray (TREENO, treeno);
		fillArray (PTNO, ptno);
		fillArray (SPECIES, species);
		fillArray (USER, user);
		fillArray (INDS, inds);
		fillArray (DBH1, dbh1);
		fillArray (HT1, ht1);
		fillArray (CR1, cr1);
		fillArray (SCR1, scr1);
		fillArray (EXPAN1, expan1);
		fillArray (MGEXP, mgexp);
		fillArray (RVARS, rvars);
		fillArray (ACALIB, acalib);
		fillArray (PN, pn);
		fillArray (YSF, ysf);
		BABT = new FloatByReference (babt);
		fillArray (BART, bart);
		fillArray (YST, yst);
		fillArray (NPR, npr);
		fillArray (PRAGE, prage);
		fillArray (PRLH, prlh);
		fillArray (PRDBH, prdbh);
		fillArray (PRHT, prht);
		fillArray (PRCR, prcr);
		fillArray (PREXP, prexp);
		fillArray (BRCNT, brcnt);
		fillArray (BRHT, brht);
		fillArray (BRDIA, brdia);
		fillArray (JCORE, jcore);

		// Output variables
		SERROR = new int[35];
		TERROR = new int[2000*6];
		SWARNING = new int[9];
		TWARNING = new int[2000];
		IERROR = new IntByReference (0);
		DGRO = new float[2000];
		HGRO = new float[2000];
		CRCHNG = new float[2000];
		SCRCHNG = new float[2000];
		MORTEXP = new float[2000];
		NTREES2 = new IntByReference (0);
		DBH2 = new float[2000];
		HT2 = new float[2000];
		CR2 = new float[2000];
		SCR2 = new float[2000];
		EXPAN2 = new float[2000];

		// Organon memory variables, init at first call only
		if (STOR == null) STOR = new float[30];

		OrgRunDll.INSTANCE.execute_ (CYCLG, VERSION, NPTS,
				NTREES1, STAGE, BHAGE,
				TREENO, PTNO, SPECIES, USER,
				INDS, DBH1, HT1, CR1, SCR1, EXPAN1,
				MGEXP, RVARS, ACALIB, PN,
				YSF, BABT, BART, YST,
				NPR, PRAGE, PRLH, PRDBH, PRHT,
				PRCR, PREXP, BRCNT, BRHT, BRDIA,
				JCORE,
				SERROR, TERROR, SWARNING, TWARNING,
				IERROR,
				DGRO, HGRO, CRCHNG, SCRCHNG,
				MORTEXP, NTREES2, DBH2, HT2,
				CR2, SCR2, EXPAN2, STOR);

		System.out.println ("[OrgRun.execute ()] end-of-method, ierror: "+get_ierror ());

	}

	// Accessors returning Java types
	public int get_cyclg () {return CYCLG.getValue ();}
	public int get_version () {return VERSION.getValue ();}
	public int get_npts () {return NPTS.getValue ();}
	public int get_ntrees1 () {return NTREES1.getValue ();}
	public int get_stage () {return STAGE.getValue ();}
	public int get_bhage () {return BHAGE.getValue ();}
	public int[] get_treeno () {return TREENO;}
	public int[] get_ptno () {return PTNO;}
	public int[] get_species () {return SPECIES;}
	public int[] get_user () {return USER;}
	public int[] get_inds () {return INDS;}
	public float[] get_dbh1 () {return DBH1;}
	public float[] get_ht1 () {return HT1;}
	public float[] get_cr1 () {return CR1;}
	public float[] get_scr1 () {return SCR1;}
	public float[] get_expan1 () {return EXPAN1;}
	public float[] get_mgexp () {return MGEXP;}
	public float[] get_rvars () {return RVARS;}
	public float[] get_acalib () {return ACALIB;}
	public float[] get_pn () {return PN;}
	public float[] get_ysf () {return YSF;}
	public float get_babt () {return BABT.getValue ();}
	public float[] get_bart () {return BART;}
	public float[] get_yst () {return YST;}
	public int[] get_npr () {return NPR;}
	public int[] get_prage () {return PRAGE;}
	public float[] get_prlh () {return PRLH;}
	public float[] get_prdbh () {return PRDBH;}
	public float[] get_prht () {return PRHT;}
	public float[] get_prcr () {return PRCR;}
	public float[] get_prexp () {return PREXP;}
	public int[] get_brcnt () {return BRCNT;}
	public int[] get_brht () {return BRHT;}
	public int[] get_brdia () {return BRDIA;}
	public int[] get_jcore () {return JCORE;}
	public int[] get_serror () {return SERROR;}
	public int[] get_terror () {return TERROR;}
	public int[] get_swarning () {return SWARNING;}
	public int[] get_twarning () {return TWARNING;}
	public int get_ierror () {return IERROR.getValue ();}
	public float[] get_dgro () {return DGRO;}
	public float[] get_hgro () {return HGRO;}
	public float[] get_crchng () {return CRCHNG;}
	public float[] get_scrchng () {return SCRCHNG;}
	public float[] get_mortexp () {return MORTEXP;}
	public int get_ntrees2 () {return NTREES2.getValue ();}
	public float[] get_dbh2 () {return DBH2;}
	public float[] get_ht2 () {return HT2;}
	public float[] get_cr2 () {return CR2;}
	public float[] get_scr2 () {return SCR2;}
	public float[] get_expan2 () {return EXPAN2;}
	public float[] get_stor () {return STOR;}
	// Accessors returning Java types

	public String toString () {
		StringBuffer b = new StringBuffer ("OrgRun trace (extracts)...");
		b.append ("\n  cyclg: "+get_cyclg ());
		b.append ("\n  version: "+get_version ());
		b.append ("\n  npts: "+get_npts ());
		b.append ("\n  ntrees1: "+get_ntrees1 ());
		b.append ("\n  stage: "+get_stage ());
		b.append ("\n  bhage: "+get_bhage ());
		b.append ("\n  treeno: "+head (get_treeno ()));
		b.append ("\n  ptno: "+head (get_ptno ()));
		b.append ("\n  species: "+head (get_species ()));
		b.append ("\n  user: "+head (get_user ()));
		b.append ("\n  inds: "+head (get_inds ()));
		b.append ("\n  dbh1: "+head (get_dbh1 ()));
		b.append ("\n  ht1: "+head (get_ht1 ()));
		b.append ("\n  cr1: "+head (get_cr1 ()));
		b.append ("\n  expan1: "+head (get_expan1 ()));
		b.append ("\n  rvars: "+head (get_rvars ()));
		b.append ("\n  acalib: "+head (get_acalib ()));
		// ...
		b.append ("\n  ntrees2: "+get_ntrees2 ());
		b.append ("\n  dbh2: "+head (get_dbh2 ()));
		b.append ("\n  ht2: "+head (get_ht2 ()));
		b.append ("\n  cr2: "+head (get_cr2 ()));
		b.append ("\n  expan2: "+head (get_expan2 ()));

		return b.toString ();
	}

	/**
	 * Builds a report by exploring the error and warning arrays
	 */
	public String errorReport () {
		StringBuffer b = new StringBuffer ("-- OrgRun errorReport...");

		b.append ("\nIERROR: "+get_ierror ());

		searchErrorsI ("SERROR", SERROR, SERROR_MAP, b);
		searchErrorsIJ ("TERROR", TERROR, TERROR_MAP, b);
		searchErrorsI ("SWARNING", SWARNING, SWARNING_MAP, b);
		searchErrorsI ("TWARNING", TWARNING, TWARNING_MAP, b);

		b.append ("\n-- end-of-report");

		return b.toString ();
	}

	static private void createSERROR_MAP () {
		SERROR_MAP = new LinkedHashMap<> ();
		SERROR_MAP.put (1, "NTREES < 1 or NTREES > 2000");
		SERROR_MAP.put (2, "VERSION < 1 or VERSION > 4");
		SERROR_MAP.put (3, "NPTS < 1");
		SERROR_MAP.put (4, "Both SITE_1 and SITE_2 are set to 0");
		SERROR_MAP.put (5, "There are no major tree species for the VERSION");
		SERROR_MAP.put (6, "BHAGE has been set to 0 for an uneven-aged stand");
		SERROR_MAP.put (7, "BHAGE > 0 for an uneven-aged stand");
		SERROR_MAP.put (8, "STAGE is too small for the BHAGE");
		SERROR_MAP.put (9, "An uneven-aged stand cannot be fertilized");
		SERROR_MAP.put (10, "YSF and/or PN variables are not zero for an unfertilized stand");
		SERROR_MAP.put (11, "The implied stand age of fertilization (based on YSF) must be less than or equal to current stand age or less than or equal to 70 years.");
		SERROR_MAP.put (12, "PN < 0 or PN > 400 lbs per acre.");
		SERROR_MAP.put (13, "BART(1) >= BABT");
		SERROR_MAP.put (14, "YST and/or BART variables are not zero for an uncut stand");
		SERROR_MAP.put (15, "For an even-aged stand, the implied stand age of cutting (based on YST) must be less than or equal to current stand age.");
		SERROR_MAP.put (16, "For multiple cuttings in which YST != 0, BART <= 0");
		SERROR_MAP.put (17, "BABT < 0 for a stand with cuttings");
		SERROR_MAP.put (18, "Some MGEXP values must be > 0 in a stand that has been cut at the start of the growth period");
		SERROR_MAP.put (19, "CYCLG < 0");
		SERROR_MAP.put (20, "ACALIB < 0.5 or ACALIB > 2.0");
		SERROR_MAP.put (21, "MSDI_1, MSDI_2, and/or MSDI_3 > 1000");
		SERROR_MAP.put (22, "Stand not even-aged so genetic gain cannot be applied");
		SERROR_MAP.put (23, "A genetic worth value cannot be < 0%");
		SERROR_MAP.put (24, "A genetic worth value must be <= 20%");
		SERROR_MAP.put (25, "A genetic worth value is > 0% when no genetic gain is indicated");
		SERROR_MAP.put (26, "Swiss needle cast cannot be applied to this version of ORGANON");
		SERROR_MAP.put (27, "Swiss needle cast cannot be applied to an unevenaged stand");
		SERROR_MAP.put (28, "Foliage retention cannot be < 0.85");
		SERROR_MAP.put (29, "Foliage retention cannot be > 7.0");
		SERROR_MAP.put (30, "Fertilization cannot be applied to a stand with foliage retention < 3.0");
		SERROR_MAP.put (31, "Foliage retention is >= 0.85 when no Swiss needle cast impact is indicated");
		SERROR_MAP.put (32, "SITE_1 is set to 0 for RAP-ORGANON");
		SERROR_MAP.put (33, "PDEN is set to zero for RAP-ORGANON");
		SERROR_MAP.put (34, "Stand must be even-aged for RAP-ORGANON");
		SERROR_MAP.put (35, "Stand must have at least 90% of basal area in red alder for RAP-ORGANON");
	}

	static private void createTERROR_MAP () {
		TERROR_MAP = new LinkedHashMap<> ();
		TERROR_MAP.put (1, "Illegal species code for the VERSION");
		TERROR_MAP.put (2, "DBH <= 0.0");
		TERROR_MAP.put (3, "HT <= 4.5");
		TERROR_MAP.put (4, "CR <= 0.0 or CR > 1.0");
		TERROR_MAP.put (5, "EXPAN < 0.0");
		TERROR_MAP.put (6, "SCR < 0.0 or SCR > 1.0");
	}

	static private void createSWARNING_MAP () {
		SWARNING_MAP = new LinkedHashMap<> ();
		SWARNING_MAP.put (1, "SITE_1 is out of range for the VERSION");
		SWARNING_MAP.put (2, "SITE_2 is out of range for the VERSION");
		SWARNING_MAP.put (3, "Tree heights are too large for the site index value");
		SWARNING_MAP.put (4, "BHAGE is too young for the VERSION");
		SWARNING_MAP.put (5, "Amount of minor species is higher than recommended for the VERSION");
		SWARNING_MAP.put (6, "Number of sample trees is below recommended minimum");
		SWARNING_MAP.put (7, "Majority of the input stand is over the upper age recommended for the VERSION");
		SWARNING_MAP.put (8, "Majority of the projected stand is now over the upper age recommended for the VERSION");
		SWARNING_MAP.put (9, "Number of cycles to be projected will make the resulting stand older than that recommended for the VERSION.");
	}

	static private void createTWARNING_MAP () {
		TWARNING_MAP = new LinkedHashMap<> ();
		TWARNING_MAP.put (1, "HT to DBH ratio is too large for the species");
	}

}