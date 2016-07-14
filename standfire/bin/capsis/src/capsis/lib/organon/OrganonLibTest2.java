package capsis.lib.organon;

import java.io.*;

/**
 * Organon: testing the Organon dynamic link libraries from Java.
 *
 * <code>
 * # To run it under Windows:
 * # Note: Organon dlls need Windows 32 bits + jdk 32 bits
 * cd .../capsis4
 * # Calling capsis adds the path to the 32 bits dlls in the system path
 * capsis
 * java -cp class;ext\* capsis.lib.organon.OrganonLibTest2
 * </code>
 *
 * @author Nathaniel Osborne, Doug Maguire, David W. Hann, F. de Coligny - August 2014
 */
public class OrganonLibTest2 extends OrgTools {

	static private BufferedWriter out; // a file to check outputs

	/**
	 * A test method to check the Organon dlls link.
	 */
	public static void main (String[] args) throws Exception {

		System.out.println ("entering main...");

		// Opening the output file
		String fileName = "organon.txt";
		out = new BufferedWriter (new FileWriter (fileName));
		writeHeader ();

		// OrgEdit
		OrgEdit orgedit = new OrgEdit ();
		System.out.println ("OrgEdit edition: "+orgedit.get_orgedit_edition ());

		int version = 3;
		int npts = 1;
		int ntrees = 11;
		int stage = 24;
		int bhage = 20;
		int[] species = new int[]{202,202,202,202,202,202,202,202,202,202,202};
		int[] user = new int[]{0,0,0,0,0,0,0,0,0,0,0};
		int ieven = 1;
		float[] dbh = new float[]{12.2f,10.0f,10.3f,13.6f,9.0f,16.0f,12.1f,8.5f,12.7f,13.3f,8.0f};
		// Checking: we may forget height values in HT, prepare would calculate them: float[] ht = new float[]{93.0f,92.0f,87.0f,97.0f};
		float[] ht = new float[]{93.0f,92.0f,87.0f,97.0f,81.0f,98.0f,93.0f,78.0f,92.0f,95.0f,81.0f}; // Total height for each tree (ft)
		float[] cr = new float[]{0.387f,0.379f,0.381f,0.333f,0.387f,0.395f,0.550f,0.394f,0.415f,0.486f,0.396f};
		float[] expan = new float[]{20f,20f,20f,20f,20f,20f,20f,20f,20f,20f,20f};
		float[] radgro = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
		float[] rvars = new float[]{125f,0f,0f,0f,0f,0f};
		int irad = 0;

		orgedit.prepare (version, npts, ntrees, stage, bhage,
			species, user, ieven, dbh, ht, cr, expan,
			radgro, rvars);

		System.out.println (orgedit.errorReport ());

		int ierror = orgedit.get_ierror ();
		if (ierror > 0) {
			System.out.println (orgedit.toString ());
			System.out.println ("OrgEdit returned a wrong ierror: "+ierror+", aborted");
			System.exit (ierror);
		}

		// Preparing OrgVol
		OrgVol orgvol = new OrgVol ();
		System.out.println ("OrgVol edition: "+orgvol.get_orgvol_edition ());



		// OrgRun
		OrgRun orgrun = new OrgRun ();
		System.out.println ("OrgRun edition: "+orgrun.get_orgrun_edition ());

		//int version = orgedit.get_version ();
		//int npts = orgedit.get_npts ();
		int ntrees1 = orgedit.get_ntrees ();
		//int stage = orgedit.get_stage ();
		//int bhage = orgedit.get_bhage ();
		int[] treeno = new int[]{1,2,3,4,5,6,7,8,9,10,11};
		int[] ptno = new int[]{1,1,1,1,1,1,1,1,1,1,1};
		//int[] species = orgedit.get_species ();
		//int[] user = orgedit.get_user ();
		int[] inds = new int[]{1,1,1,1,0,0,0,0,1,0,0,0,0,0,0}; // 15
		float[] dbh1 = orgedit.get_dbh ();
		float[] ht1 = orgedit.get_ht ();
		float[] cr1 = orgedit.get_cr ();
		float[] scr1 = new float[2000];
		float[] expan1 = orgedit.get_expan ();
		float[] mgexp = new float[2000];
		float[] rvars9 = new float[]{125,0,0,0,0,0,0,0,0}; // 9
		float[] acalib = orgedit.get_acalib ();
		float[] pn = new float[5];
		float[] ysf = new float[5];
		float babt = 0;
		float[] bart = new float[5];
		float[] yst = new float[5];
		int[] npr = new int[2000];
		int[] prage = new int[2000*3];
		float[] prlh = new float[2000*3];
		float[] prdbh = new float[2000*3];
		float[] prht = new float[2000*3];
		float[] prcr = new float[2000*3];
		float[] prexp = new float[2000*3];
		int[] brcnt = new int[2000*3];
		int[] brht = new int[2000*40];
		int[] brdia = new int[2000*40];
		int[] jcore = new int[2000*40];

		// Write initial tree lines in the output file, including volumes
		int date = 0;
		write (orgvol, orgedit.get_version (), date, ntrees1,
				orgedit.get_stage () /* stage */ , orgedit.get_user (), treeno, orgedit.get_species (),
				orgedit.get_dbh (), orgedit.get_ht (), orgedit.get_cr (),
				orgedit.get_expan (), mgexp);

		for (int k = 0; k <= 3; k++) {
			int cyclg = k;

			orgrun.execute (cyclg,  version,  npts,
							ntrees1,  stage,  bhage,
							treeno, ptno, species, user,
							inds, dbh1, ht1, cr1, scr1, expan1,
							mgexp, rvars9, acalib, pn,
							ysf, babt, bart, yst,
							npr, prage, prlh, prdbh, prht,
							prcr, prexp, brcnt, brht, brdia,
							jcore);

			System.out.println (orgrun.errorReport ());

			ierror = orgrun.get_ierror ();
			if (ierror > 0) {
				System.out.println (orgrun.toString ());
				System.out.println ("OrgRun returned a wrong ierror: "+ierror+", aborted");
				System.exit (ierror);
			}

			// Prepare next run
			treeno = orgrun.get_treeno ();
			ptno = orgrun.get_ptno ();
			species = orgrun.get_species ();
			user = orgrun.get_user ();
			ntrees1 = orgrun.get_ntrees2 ();
			stage = orgrun.get_stage ();
			bhage = orgrun.get_bhage ();
			dbh1 = orgrun.get_dbh2 ();
			ht1 = orgrun.get_ht2 ();
			cr1 = orgrun.get_cr2 ();
			scr1 = orgrun.get_scr2 ();
			expan1 = orgrun.get_expan2 ();
//			crchng = orgrun.get_crchng ();
//			scrchng = orgrun.get_scrchng ();
//			dgro = orgrun.get_dgro ();
//			hgro = orgrun.get_hgro ();

			date = k + 1;

			// Write tree lines in the file, including volumes
			write (orgvol, orgedit.get_version (), date, ntrees1,
					orgrun.get_stage (), orgrun.get_user (), orgrun.get_treeno (), species,
					orgrun.get_dbh2 (), orgrun.get_ht2 (), orgrun.get_cr2 (),
					orgrun.get_expan2 (), mgexp);

		}

		// Close the output file
		out.close ();

		System.out.println ("end-of-main, wrote results in file: "+fileName);

	}

	/**
	 * Write the current stand state in a file: one line per tree.
	 */
	static private void writeHeader () throws Exception {
		StringBuffer b = new StringBuffer ();

		b.append ("#date");
		b.append ("\tstage");
		b.append ("\tuser");
		b.append ("\ttreeno");
		b.append ("\tspecies");
		b.append ("\tdbh");
		b.append ("\tht");
		b.append ("\tcr");
		b.append ("\texpan");
		b.append ("\tmgexp");
		b.append ("\tcfvol");
		b.append ("\tbfvol");
		out.write (b.toString ());
		out.newLine ();

	}

	/**
	 * Write the current stand state in a file: one line per tree.
	 */
	static private void write (OrgVol orgvol, int version, int date, int ntrees,
			int stage, int[] user, int[] treeno, int[] species,
			float[] dbh, float[] ht, float[] cr, float[] expan, float[] mgexp) throws Exception {

		for (int i = 0; i < ntrees; i++) {
			StringBuffer b = new StringBuffer ();

			b.append (date);
			b.append ("\t"+stage);
			b.append ("\t"+user[i]);
			b.append ("\t"+treeno[i]);
			b.append ("\t"+species[i]);
			b.append ("\t"+dbh[i]);
			b.append ("\t"+ht[i]);
			b.append ("\t"+cr[i]);
			b.append ("\t"+expan[i]);
			b.append ("\t"+mgexp[i]);

			float[] volumes = getVolumes (orgvol, version,
					species[i], dbh[i], ht[i], cr[i]);

			float cfvol = volumes[0];
			float bfvol = volumes[1];

			b.append ("\t"+cfvol);
			b.append ("\t"+bfvol);

			out.write (b.toString ());
			out.newLine ();
		}
	}

	/**
	 * Calculates the OrgVol volumes for the given tree.
	 * Returns cfvol and bfvol.
	 */
	static private float[] getVolumes (OrgVol orgvol, int version,
		int tree_species, float tree_dbh, float tree_ht, float tree_cr) {

		float cftd = 6f; // Top diameter inside bark (in inches) for cubic foot volume
		float cfsh = 1.2f; // Stump height (in feet) for cubic foot volume
		float logll = 32.0f; // Log length (in whole feet) for Scribner board foot volume
		float logml = 8.0f; // Minimum log length (in whole feet) for Scribner board foot volume
		float logtd = 6.0f; // Top diameter inside bark (in inches) for Scribner board foot volume
		float logsh = 0.5f; // Stump height (in feet) for Scribner board foot volume
		float logta = 8.0f; // Trim allowance (in inches) for Scribner board foot volume

		orgvol.volcal (version, tree_species, cftd, cfsh,
				logll, logml, logtd, logsh, logta,
				tree_dbh, tree_ht, tree_cr);

		int ierror = orgvol.get_ierror ();
		if (ierror > 0) {
			System.out.println (orgvol.errorReport ());
			System.out.println (orgvol.toString ());
			try {out.close ();} catch (Exception e) {} // no matter
			System.out.println ("OrgVol returned a wrong ierror: "+ierror+", aborted");
			System.exit (ierror);
		}

		return new float[]{orgvol.get_cfvol (), orgvol.get_bfvol ()};
	}

}

