package capsis.lib.organon;

import java.util.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.*;

/**
 * Organon: a link to the Orgedit.dll dynamic link library.
 *
 * @author Nathaniel Osborne, Doug Maguire, David W. Hann, F. de Coligny - August 2014
 */
 public class OrgEdit extends OrgTools {

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

	// Mapping ORGEDIT.dll
	private interface OrgEditDll extends Library {
		OrgEditDll INSTANCE = (OrgEditDll) Native.loadLibrary("ORGEDIT", OrgEditDll.class);

		void get_orgedit_edition_ (FloatByReference EDITION);

		void prepare_ (IntByReference VERSION, IntByReference NPTS, IntByReference NTREES,
				IntByReference STAGE, IntByReference BHAGE, int[] SPECIES, int[] USER,
				IntByReference IEVEN, float[] DBH, float[] HT, float[] CR, float[] EXPAN,
				float[] RADGRO, float[] RVARS, int[] SERROR, int[] TERROR, int[] SWARNING,
				int[] TWARNING, IntByReference /* FloatByReference */ IERROR, IntByReference IRAD, float[] GROWTH,
				float[] ACALIB);
	}

	// Parameters for the get_orgedit_edition_ () function
	public FloatByReference EDITION;

	// Parameters for the prepare_ () function
	public IntByReference VERSION; // Version of Organon to run (1, 2, 3)
	public IntByReference NPTS; // Number of sample points fixed at one
	public IntByReference NTREES; // Number of trees in the sample
	public IntByReference STAGE; // Stand age
	public IntByReference BHAGE; // Breast height age
	public int[] SPECIES = new int[2000]; // Species for each tree
	public int[] USER = new int[2000]; // Code for user thinning
	public IntByReference IEVEN; // If the stand is even aged or not
	public float[] DBH = new float[2000]; // Diameter at breast height for each tree (in)
	public float[] HT = new float[2000]; // Total height for each tree (ft)
	public float[] CR = new float[2000]; // Crown ratio for each tree
	public float[] EXPAN = new float[2000]; // Expansion factor for each tree (tpa)
	public float[] RADGRO = new float[2000]; // User supplied estimates of radial growth (in)
	public float[] RVARS = new float[30]; // Set of six indicator variables
	public int[] SERROR = new int[13];
	public int[] TERROR = new int[2000*6];
	public int[] SWARNING = new int[8];
	public int[] TWARNING = new int[2000];
	public IntByReference IERROR;
	public IntByReference IRAD; // Indicator if radial growth measurements were entered
	public float[] GROWTH = new float[2000];
	public float[] ACALIB = new float[3*18];


	/**
	 * A get_orgedit_edition () function turning the Java typed parameters into JNA types before calling
	 * the get_orgedit_edition_ () function in the Orgedit dll.
	 */
	public float get_orgedit_edition () {
		EDITION = new FloatByReference (0);
		OrgEditDll.INSTANCE.get_orgedit_edition_ (EDITION);
		return EDITION.getValue ();
	}

	/**
	 * A prepare () function turning the Java typed parameters into JNA types before calling
	 * the prepare_ () function in the Orgedit dll.
	 */
	public void prepare (int version, int npts, int ntrees, int stage, int bhage,
			int[] species, int[] user, int ieven, float[] dbh, float[] ht, float[] cr, float[] expan,
			float[] rdagro, float[] rvars /* , int[] serror, int[] terror, int[] swarning, int[] twarning,
			float ierror, int irad, float[] growth, float[] acalib */ ) {

		System.out.println ("[OrgEdit.prepare ()] entering method...");

		VERSION = new IntByReference (version);
		NPTS = new IntByReference (npts);
		NTREES = new IntByReference (ntrees);
		STAGE = new IntByReference (stage);
		BHAGE = new IntByReference (bhage);
		fillArray (SPECIES, species);
		fillArray (USER, user);
		IEVEN = new IntByReference (ieven);
		fillArray (DBH, dbh);
		fillArray (HT, ht);
		fillArray (CR, cr);
		fillArray (EXPAN, expan);
		fillArray (RADGRO, rdagro);
		fillArray (RVARS, rvars);

		// Output variables
		SERROR = new int[13];
		TERROR = new int[2000*6];
		SWARNING = new int[8];
		TWARNING = new int[2000];
		IERROR = new IntByReference (0);
		IRAD = new IntByReference (0);
		GROWTH = new float[2000];
		ACALIB = new float[3*18];

		OrgEditDll.INSTANCE.prepare_ (VERSION, NPTS, NTREES,
				STAGE, BHAGE, SPECIES, USER,
				IEVEN, DBH, HT, CR, EXPAN,
				RADGRO, RVARS, SERROR, TERROR, SWARNING,
				TWARNING, IERROR, IRAD, GROWTH,
				ACALIB);

		System.out.println ("[OrgEdit.prepare ()] end-of-method, ierror: "+get_ierror ());

	}

	// Accessors returning Java types
	public float get_edition () {return EDITION.getValue ();}

	public int get_version () {return VERSION.getValue ();}
	public int get_npts () {return NPTS.getValue ();}
	public int get_ntrees () {return NTREES.getValue ();}
	public int get_stage () {return STAGE.getValue ();}
	public int get_bhage () {return BHAGE.getValue ();}
	public int[] get_species () {return SPECIES;}
	public int[] get_user () {return USER;}
	public int get_ieven () {return IEVEN.getValue ();}
	public float[] get_dbh () {return DBH;}
	public float[] get_ht () {return HT;}
	public float[] get_cr () {return CR;}
	public float[] get_expan () {return EXPAN;}
	public float[] get_radgro () {return RADGRO;}
	public float[] get_rvars () {return RVARS;}
	public int[] get_serror () {return SERROR;}
	public int[] get_terror () {return TERROR;}
	public int[] get_swarning () {return SWARNING;}
	public int[] get_twarning () {return TWARNING;}
	public int get_ierror () {return IERROR.getValue ();}
	public int get_irad () {return IRAD.getValue ();}
	public float[] get_growth () {return GROWTH;}
	public float[] get_acalib () {return ACALIB;}
	// Accessors returning Java types

	/**
	 * Builds a report by exploring the error and warning arrays
	 */
	public String errorReport () {
		StringBuffer b = new StringBuffer ("-- OrgEdit errorReport...");

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
		SERROR_MAP.put (9, "MSDI_1, MSDI_2, and/or MSDI_3 > 1000");
		SERROR_MAP.put (10, "SITE_1 is set to 0 for RAP-ORGANON");
		SERROR_MAP.put (11, "PDEN is set to zero for RAP-ORGANON");
		SERROR_MAP.put (12, "Stand must be even-aged for RAP-ORGANON");
		SERROR_MAP.put (13, "Stand must have at least 90% of basal area in red alder for RAP-ORGANON");
	}

	static private void createTERROR_MAP () {
		TERROR_MAP = new LinkedHashMap<> ();
		TERROR_MAP.put (1, "Illegal species code for the VERSION");
		TERROR_MAP.put (2, "DBH <= 0.0");
		TERROR_MAP.put (3, "HT > 0.0 and HT <= 4.5");
		TERROR_MAP.put (4, "CR > 1.0");
		TERROR_MAP.put (5, "EXPAN < 0.0");
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
	}

	static private void createTWARNING_MAP () {
		TWARNING_MAP = new LinkedHashMap<> ();
		TWARNING_MAP.put (1, "HT to DBH ratio is too large for the species");
	}


 }