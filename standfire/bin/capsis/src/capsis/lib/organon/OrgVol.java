package capsis.lib.organon;

import java.util.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.*;

/**
 * Organon: a link to the Orgvol.dll dynamic link library.
 *
 * @author Nathaniel Osborne, Doug Maguire, David W. Hann, F. de Coligny - August 2014
 */
 public class OrgVol extends OrgTools {

	static public Map<Integer,String> VERROR_MAP;
	static public Map<Integer,String> TERROR_MAP;
	static public Map<Integer,String> VWARNING_MAP;
	static public Map<Integer,String> TWARNING_MAP;

 	static {
 		// Set jna.library.path
 		System.setProperty ("jna.library.path", System.getProperty ("java.library.path"));
 		//System.out.println ("jna.library.path: "+System.getProperty ("jna.library.path"));
 		createVERROR_MAP ();
 		createTERROR_MAP ();
 		createVWARNING_MAP ();
 		createTWARNING_MAP ();
 	}

	// Mapping ORGEDIT.dll
	private interface OrgVolDll extends Library {
		OrgVolDll INSTANCE = (OrgVolDll) Native.loadLibrary("ORGVOL", OrgVolDll.class);

		void get_orgvol_edition_ (FloatByReference EDITION);

		void volcal_ (IntByReference VERSION, IntByReference SPECIES, FloatByReference CFTD,
				FloatByReference CFSH, FloatByReference LOGLL, FloatByReference LOGML, FloatByReference LOGTD,
				FloatByReference LOGSH, FloatByReference LOGTA,
				FloatByReference DBH, FloatByReference HT, FloatByReference CR,
				int[] VERROR, int[] TERROR, int[] VWARNING, IntByReference TWARNING, IntByReference IERROR,
				FloatByReference CFVOL, FloatByReference BFVOL);
	}

	// Parameters for the get_orgvol_edition_ () function
	public FloatByReference EDITION;

	// Parameters for the volcal_ () function
	public IntByReference VERSION; // Version of Organon to be used: 1 = Southwest Oregon, 2 = Northwest Oregon, 3 = Stand Management Cooperative, 4 = Red Alder Plantation
	public IntByReference SPECIES; // Species code for the sample tree
	public FloatByReference CFTD; // Top diameter inside bark (in inches) for cubic foot volume
	public FloatByReference CFSH; // Stump height (in feet) for cubic foot volume
	public FloatByReference LOGLL; // Log length (in whole feet) for Scribner board foot volume
	public FloatByReference LOGML; // Minimum log length (in whole feet) for Scribner board foot volume
	public FloatByReference LOGTD; // Top diameter inside bark (in inches) for Scribner board foot volume
	public FloatByReference LOGSH; // Stump height (in feet) for Scribner board foot volume
	public FloatByReference LOGTA; // Trim allowance (in inches) for Scribner board foot volume
	public FloatByReference DBH; // DBH for the sample tree
	public FloatByReference HT; // Total height for the sample tree
	public FloatByReference CR; // Crown ratio for the sample tree

	public int[] VERROR = new int[5]; // If VERROR(I)=1 (1 <= I <= 5), then a volume specification error of type "I" has occurred (a value of 0 indicated no error)
	public int[] TERROR = new int[4]; // If TERROR(I)=1 (1 <= I <= 4), then a tree level error of type "I" has occurred for the sample tree (a value of 0 indicated no error)
	public int[] VWARNING = new int[5]; // If SWARNING(I)=1 (1 <= I <= 5), then a volume specification warning of type "I" has occurred (a value of 0 indicated no warning)
	public IntByReference TWARNING; // If TWARNING=1, then a tree warning has occurred for the sample tree (a value of 0 indicated no warning)
	public IntByReference IERROR; // If IERROR=1, then a stand or tree level error has occurred and the error must be corrected before proceeding.

	public FloatByReference CFVOL; // Calculated cubic foot volume for the sample tree
	public FloatByReference BFVOL; // Calculated Scribner board foot volume for the sample tree


	/**
	 * A get_orgvol_edition () function turning the Java typed parameters into JNA types before calling
	 * the get_orgvol_edition_ () function in the Orgvol dll.
	 */
	public float get_orgvol_edition () {
		EDITION = new FloatByReference (0);
		OrgVolDll.INSTANCE.get_orgvol_edition_ (EDITION);
		return EDITION.getValue ();
	}

	/**
	 * A volcal () function turning the Java typed parameters into JNA types before calling
	 * the volcal_ () function in the Orgvol dll.
	 */
	public void volcal (int version, int species, float cftd, float cfsh,
			float logll, float logml, float logtd, float logsh, float logta,
			float dbh, float ht, float cr) {

//		System.out.println ("[OrgVol.volcal ()] entering method...");

		VERSION = new IntByReference (version);
		SPECIES = new IntByReference (species);
		CFTD = new FloatByReference (cftd);
		CFSH = new FloatByReference (cfsh);
		LOGLL = new FloatByReference (logll);
		LOGML = new FloatByReference (logml);
		LOGTD = new FloatByReference (logtd);
		LOGSH = new FloatByReference (logsh);
		LOGTA = new FloatByReference (logta);
		DBH = new FloatByReference (dbh);
		HT = new FloatByReference (ht);
		CR = new FloatByReference (cr);

		// Output variables
		VERROR = new int[5];
		TERROR = new int[4];
		VWARNING = new int[5];
		TWARNING = new IntByReference (0);
		IERROR = new IntByReference (0);

		CFVOL = new FloatByReference (0);
		BFVOL = new FloatByReference (0);

		OrgVolDll.INSTANCE.volcal_ (VERSION, SPECIES, CFTD, CFSH,
				LOGLL, LOGML, LOGTD, LOGSH, LOGTA,
				DBH, HT, CR,
				VERROR, TERROR, VWARNING,
				TWARNING, IERROR,
				CFVOL, BFVOL);

//		System.out.println ("[OrgVol.volcal ()] end-of-method (ierror: "+get_ierror ()+")");

	}

	// Accessors returning Java types
	public float get_edition () {return EDITION.getValue ();}

	public int get_version () {return VERSION.getValue ();}
	public int get_species () {return SPECIES.getValue ();}
	public float get_cftd () {return CFTD.getValue ();}
	public float get_cfsh () {return CFSH.getValue ();}
	public float get_logll () {return LOGLL.getValue ();}
	public float get_logml () {return LOGML.getValue ();}
	public float get_logtd () {return LOGTD.getValue ();}
	public float get_logsh () {return LOGSH.getValue ();}
	public float get_logta () {return LOGTA.getValue ();}
	public float get_dbh () {return DBH.getValue ();}
	public float get_ht () {return HT.getValue ();}
	public float get_cr () {return CR.getValue ();}

	public int[] get_verror () {return VERROR;}
	public int[] get_terror () {return TERROR;}
	public int[] get_vwarning () {return VWARNING;}
	public int get_twarning () {return TWARNING.getValue ();}
	public int get_ierror () {return IERROR.getValue ();}

	public float get_cfvol () {return CFVOL.getValue ();}
	public float get_bfvol () {return BFVOL.getValue ();}
	// Accessors returning Java types

	public String toString () {
		StringBuffer b = new StringBuffer ("OrgVol trace...");

		b.append ("\n  version: "+get_version ());
		b.append ("\n  species: "+get_species ());
		b.append ("\n  cftd: "+get_cftd ());
		b.append ("\n  cfsh: "+get_cfsh ());
		b.append ("\n  logll: "+get_logll ());
		b.append ("\n  logml: "+get_logml ());
		b.append ("\n  logtd: "+get_logtd ());
		b.append ("\n  logsh: "+get_logsh ());
		b.append ("\n  logta: "+get_logta ());
		b.append ("\n  dbh: "+get_dbh ());
		b.append ("\n  ht: "+get_ht ());
		b.append ("\n  cr: "+get_cr ());
		b.append ("\n  ierror: "+get_ierror ());
		b.append ("\n  cfvol: "+get_cfvol ());
		b.append ("\n  bfvol: "+get_bfvol ());

		return b.toString ();
	}

	/**
	 * Builds a report by exploring the error and warning arrays
	 */
	public String errorReport () {
		StringBuffer b = new StringBuffer ("-- OrgVol errorReport...");

		b.append ("\nIERROR: "+get_ierror ());

		searchErrorsI ("VERROR", VERROR, VERROR_MAP, b);
		searchErrorsI ("TERROR", TERROR, TERROR_MAP, b);
		searchErrorsI ("VWARNING", VWARNING, VWARNING_MAP, b);
		searchErrors ("TWARNING", get_twarning (), TWARNING_MAP, b);

		b.append ("\n-- end-of-report");

		return b.toString ();
	}

	static private void createVERROR_MAP () {
		VERROR_MAP = new LinkedHashMap<> ();
		VERROR_MAP.put (1, "CFSH > 4.5");
		VERROR_MAP.put (2, "LOGML > LOGLL");
		VERROR_MAP.put (3, "LOGTD > 0.0 but < 1.0");
		VERROR_MAP.put (4, "LOGSH > 4.5");
		VERROR_MAP.put (5, "LOGTA > 0.0 but < 1.0");
	}

	static private void createTERROR_MAP () {
		TERROR_MAP = new LinkedHashMap<> ();
		TERROR_MAP.put (1, "Illegal species code");
		TERROR_MAP.put (2, "DBH <= 0.0");
		TERROR_MAP.put (3, "HT <= 4.5");
		TERROR_MAP.put (4, "CR > 1.0 or CR < 0.0");
	}

	static private void createVWARNING_MAP () {
		VWARNING_MAP = new LinkedHashMap<> ();
		VWARNING_MAP.put (1, "CFSH > 12.0");
		VWARNING_MAP.put (2, "LOGLL < 8.0 or LOGLL > 40.0");
		VWARNING_MAP.put (3, "LOGML < 8.0 or LOGML > 40.0");
		VWARNING_MAP.put (4, "LOGTD > 12.0");
		VWARNING_MAP.put (5, "LOGTA > 12.0");
	}

	static private void createTWARNING_MAP () {
		TWARNING_MAP = new LinkedHashMap<> ();
		TWARNING_MAP.put (1, "HT to DBH ratio is too large for the species");
	}


 }