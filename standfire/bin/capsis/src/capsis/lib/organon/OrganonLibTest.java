package capsis.lib.organon;

import java.io.*;

/**
 * Organon: a link to the Orgedit.dll dynamic link library.
 *
 * <code>
 * # To run it under Windows:
 * # Note: Organon dlls need Windows 32 bits + jdk 32 bits
 * cd .../capsis4
 * # Calling capsis adds the path to the 32 bits dlls in the system path
 * capsis
 * java -cp class;ext\* capsis.lib.organon.OrganonLibTest
 * </code>
 *
 * @author Nathaniel Osborne, Doug Maguire, David W. Hann, F. de Coligny - August 2014
 */
public class OrganonLibTest extends OrgTools {

	static private BufferedWriter out; // a file to check outputs

	/**
	 * A test method to check the Organon dll link.
	 */
	public static void main (String[] args) throws Exception {

		System.out.println ("entering main...");

		// Opening the output file
		out = new BufferedWriter (new FileWriter ("out.txt"));

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
		// Checking: we forget height values in HT, prepare will calculate them
		float[] ht = new float[]{93.0f,92.0f,87.0f,97.0f};
		// the complete ht line: new float[]{93.0f,92.0f,87.0f,97.0f,81.0f,98.0f,93.0f,78.0f,92.0f,95.0f,81.0f}; // Total height for each tree (ft)
		float[] cr = new float[]{0.387f,0.379f,0.381f,0.333f,0.387f,0.395f,0.550f,0.394f,0.415f,0.486f,0.396f};
		float[] expan = new float[]{20f,20f,20f,20f,20f,20f,20f,20f,20f,20f,20f};
		float[] radgro = new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
		float[] rvars = new float[]{125f,0f,0f,0f,0f,0f};
		int irad = 0;

		System.out.println ("HT: "+orgedit.head(ht));
		System.out.println ("calling OrgEdit.prepare ()...");

		orgedit.prepare (version, npts, ntrees, stage, bhage,
			species, user, ieven, dbh, ht, cr, expan,
			radgro, rvars);

		System.out.println (orgedit.errorReport ());

		System.out.println ("HT: "+orgedit.head (orgedit.get_ht (), 11));

		// write a line in the output file
		write ("# dbh (6 trees) ht (6 trees) expan (6 trees)");
		write (get6Cols (orgedit.get_dbh ())+"\t"+get6Cols (orgedit.get_ht ())+"\t"+get6Cols (orgedit.get_expan ()));

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
		// Tuning TERROR(I,J) message: sending a wrong dbh for tree 2
//		dbh1[1] = 0f; // TEST, will be removed

		float[] ht1 = orgedit.get_ht ();
		// Tuning TERROR(I,J) message: sending a wrong ht for tree 5
//		ht1[4] = 2f; // TEST, will be removed

		float[] cr1 = orgedit.get_cr ();
		float[] scr1 = new float[2000];

		float[] expan1 = orgedit.get_expan ();
		// Tuning TERROR(I,J) message: sending a wrong expan for tree 5
//		expan1[4] = -1f; // TEST, will be removed

		float[] mgexp = new float[2000];
		// value in first pos to prevent having an SERROR[4] "Both SITE_1 and SITE_2 are set to 0"
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


		for (int k = 0; k <= 3; k++) {

			int cyclg = k;
			//int cyclg = 0;

			System.out.println ("calling OrgRun.execute () with cyclg: "+cyclg+"...");

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

			System.out.println (orgrun.toString ());
			write (get6Cols (orgrun.get_dbh2 ())+"\t"+get6Cols (orgrun.get_ht2 ())+"\t"+get6Cols (orgrun.get_expan2 ()));

			int fatal = orgrun.get_ierror ();
			if (fatal > 0) {
				System.out.println ("breaking the loop...");
				break;
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


		}
		// Close the output file
		out.close ();

		System.out.println ("end-of-main");

	}

	static String get6Cols (float[] array) {
		return head (array, 6, "\t", false);
	}

	static private void write (String line) throws Exception {
		out.write (line);
		out.newLine ();
	}

}

