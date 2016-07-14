package capsis.lib.forestgales;

import java.util.Date;

import capsis.kernel.PathManager;

/**
 * ForestGales' stand level roughness method.
 *
 * @author B. Gardiner, K. Kamimura , T. Labbe , C. Meredieu - August 2013
 *
 */
public class FGSimpleStandLevelRoughnessMethod extends FGMethod {
/*
	private double roughnessSimple_d;
	private double roughnessSimple_gammaSolved;
	private double roughnessSimple_z0;
	private static final int SECTIONS = 4;/////////////////////////WE CHOSE THIS VALUE FOR COMPUTATION, BARRY?????????????
    private double [] U_UH = new double [SECTIONS] ;
*/
	/**
	 * Constructor
	 */
	public FGSimpleStandLevelRoughnessMethod (FGStand stand, FGConfiguration configuration) {
		super (stand, configuration);

	}

	public String getName () {
		return "ForestGales Simple Stand Level Roughness Method";
	}



	public void run () throws Exception {

		// Use report () for progress messages, see superclass
		report ("\n"+getName ()+" "+new Date ()+"...\n");
		report ("--- Initialisation ---");
		report (stand.toString ());
		report (configuration.toString ());

		double nha = stand.getNha ();
		double spacing = 100. / Math.sqrt (nha);

		if (stand.getTrees ().size () != 1) { throw new Exception ("tree numbers different from one at stand level !"); }
		FGTree meanTree = (FGTree) stand.getTrees ().iterator ().next ();
		double height = meanTree.getHeight ();
		double crownWidth = meanTree.getCrownWidth ();
		double crownDepth = meanTree.getCrownDepth ();
		double stemWeight = meanTree.getStemWeight ();
		double dbh = meanTree.getDbh_m ();


		// Find the FGSpecies matching the given name (aborts if wrong species name)
		FGSpecies species = configuration.getSpecies (meanTree.getSpeciesName ());

		double overturningMomentMultiplier = species.getOverturningMomentMultipliers ()[0][0];
		double crownFactor = species.getCrownFactor ();
		double knotFactor = species.getKnotFactor ();
		double modulusOfRupture = species.getModulusOfRupture ();
		//double canopyStreamliningN = species.getCanopyStreamliningN ();
		//double canopyStreamliningC = species.getCanopyStreamliningC ();

		double windSpeed = 20; // NOT USED - BE CAREFFUL?????????????????????CHECK BARRY WE NEED A WINDSPEED HERE!!!!!!!


		// 1. calc roughness (FGSpecies species, double windSpeed, double crownWidth, double crownDepth, double height,
		// double nha, FGConfiguration configuration)
		double[] roughnessParameters = FGRoughnessSimple.roughnessSimple (species, windSpeed, crownWidth, crownDepth, height, spacing, configuration);
		//roughnessSimple (species, windSpeed, crownWidth, crownDepth, height, nha, configuration);

		double roughnessSimple_d = roughnessParameters  [0];
		double roughnessSimple_z0 = roughnessParameters  [2];


		// 2. calc gust factor (double nha, double treeHeightsNumberFromEdge, double height)
		double gustFactor = FGGustFactor.calculateGustFactorInForest (stand, spacing, height);

		// 3. calc overturningCriticalWindSpeed (FGConfiguration configuration, double nha, double
		// overturningMomentMultiplier, double stemWeight, double airDensity, double gustFactor,
		// double crownFactor, double height)
		double overturningCriticalWindSpeed = FGCriticalWindSpeed.calculateSimpleOverturningCriticalWindSpeed (configuration, spacing, overturningMomentMultiplier, stemWeight, gustFactor,
			crownFactor, height, roughnessSimple_d , roughnessSimple_z0);
		//double overturningCriticalWindSpeed = calculateSimpleOverturningCriticalWindSpeed (configuration, nha, overturningMomentMultiplier, stemWeight, gustFactor, crownFactor, height);

		// 4. calc breakageCriticalWindSpeed (FGConfiguration configuration, double nha, double
		// modulusOfRupture, double dbh, double airDensity, double gustFactor, double knotFactor,
		// double crownFactor, double height)
		double breakageCriticalWindSpeed = FGCriticalWindSpeed.calculateSimpleBreakageCriticalWindSpeed (configuration, spacing, modulusOfRupture, dbh, gustFactor, knotFactor, crownFactor,
			height, roughnessSimple_d , roughnessSimple_z0);
		//double breakageCriticalWindSpeed = calculateSimpleBreakageCriticalWindSpeed (configuration, nha, modulusOfRupture, dbh, gustFactor, knotFactor, crownFactor, height);



		//5. calc wind profile (double nha, double height= mean height for stand )
		double [] windProfile = FGCriticalWindSpeed.calculateWindProfile( spacing, height, roughnessSimple_d ,roughnessSimple_z0 );
		//calculateWindProfile( nha, height );

		// Results
		meanTree.setCwsForBreakage (breakageCriticalWindSpeed);
		meanTree.setCwsForOverturning (overturningCriticalWindSpeed);

		report ("--- Results ---");
		report (""+meanTree);
		report ("\n"+getName ()+" ended successfully\n");

	}



	/**
	 * Test of FGSimpleStandLevelRoughnessMethod.
	 *
	 * Type the following command from capsis4/ (Capsis install directory)
	 *
	 * Run (Linux): java -cp class:ext/* capsis.lib.forestgales.FGSimpleStandLevelRoughnessMethod
	 *
	 * Run (windows): java -cp class;ext\* capsis.lib.forestgales.FGSimpleStandLevelRoughnessMethod
	 *
	 */
	public static void main (String[] args) throws Exception {



		// Create a stand object and set its properties
		FGStand stand = new FGStand ();
		double nha=2000;
		stand.setNha (nha);
		stand.setDominantHeight (20);

		// Create a single tree in the stand: the mean tree
		//Test input variables
		double dbh_m = 0.2; // m
		double height=20; //m
		double crownWidth = 5; // m, optional (-1)
		double crownDepth = 5; // m, optional (-1)
		double stemVolume = -1; // m3, optional (-1)
		double stemWeight = 304.3; // kg, optional (-1)
		double crownVolume = -1; // m3, optional (-1)
		double crownWeight = 53.8; // kg, optional (-1)
		double [] diam = null;
		double [] z = null;
		double [] mass = null;


		FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
				crownWeight, diam,  z, mass, "Maritime pine");
		stand.addTree (meanTree);

		// Create a configuration object and load the species file
		FGConfiguration configuration = new FGConfiguration ();
		configuration.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");


		// Run the stand level roughness method
		FGSimpleStandLevelRoughnessMethod m = new FGSimpleStandLevelRoughnessMethod (stand, configuration);


		m.setWriteInTerminal (true); // writes details in the terminal
		m.run ();


		//Test input variables
		//double windSpeed=20;

		//Test input variables
		//FGSpecies species = configuration.getSpecies ("Maritime pine");


		//double overturningMomentMultiplier = species.getOverturningMomentMultipliers ()[0][0];
		//double crownFactor = species.getCrownFactor ();
		//double knotFactor = species.getKnotFactor ();
		//double modulusOfRupture = species.getModulusOfRupture ();


		//m.roughnessSimple (species, windSpeed, crownWidth, crownDepth, height, nha, configuration);

		//System.out.println 	("results of roughnessSimple : " + roughnessSimple (species, windSpeed, crownWidth, crownDepth, height, nha, configuration)); BAD METHOD
		//System.out.println 	("results of roughnessSimple_d : " + m.roughnessSimple_d );
		//System.out.println 	("results of roughnessSimple_gammaSolved : " + m.roughnessSimple_gammaSolved );
		//System.out.println 	("results of roughnessSimple_z0 : " + m.roughnessSimple_z0 );



		//System.out.println 	("results of NEWGustFactor  : " + m.calculateNEWGustFactor (configuration, nha, height));

		//double gustFactor = m.calculateGustFactorInForest(configuration, nha, height);
		//System.out.println 	("results of GustFactorInForest  : " + m.calculateGustFactorInForest (configuration, nha, height));



		//System.out.println 	("results of overturningCriticalWindSpeed : " +
		//	m.calculateSimpleOverturningCriticalWindSpeed (configuration, nha, overturningMomentMultiplier, stemWeight, gustFactor, crownFactor, height));


		//System.out.println 	("results of breakageCriticalWindSpeed : " +
		//	m.calculateSimpleBreakageCriticalWindSpeed (configuration, nha, modulusOfRupture, dbh_m, gustFactor, knotFactor, crownFactor, height));

/*
		m.calculateWindProfile( nha, height );
		System.out.println 	("results of WindProfile : " );
		for (int i = 0; i < SECTIONS; i++) {
				System.out.println("" + m.U_UH[i]);
		}
*/


	}

}




/* to launch the command in C:/capsis windows
//java -cp class;ext\* capsis.lib.forestgales.FGSimpleStandLevelRoughnessMethod


		//Test input variables 31 01 2014
		double windSpeed=20;
		FGSpecies species = configuration.getSpecies ("Maritime pine");
		double crownWidth=5;
		double crownDepth=5;
		double height=20;
		double nha=2000;
		double stemWeight = 304.3; // kg, optional (-1)
		double dbh_m = 0.2; // m

results of roughnessSimple_d : 15.62767569577639
results of roughnessSimple_gammaSolved : 3.2791291789197645
results of roughnessSimple_z0 : 1.428736166870482
results of NEWGustFactor  : 7.9520315302364715
results of GustFactorInForest  : 6.47607684719374
results of overturningCriticalWindSpeed : 22.993310207449554
results of breakageCriticalWindSpeed : 16.42590394437762








results of roughnessSimple_d : 14.44268631123483
results of roughnessSimple_gammaSolved : 3.2791291789197645
results of roughnessSimple_z0 : 1.8159529132167371
stemWeight :304.3
gustFactor:6.47607684719374
crownFactor:1.0
height:20.0
results of roughnessSimple_d dans OCWS : 14.44268631123483
results of roughnessSimple_gammaSolved dans OCWS : 3.2791291789197645
results of roughnessSimple_z0 dans OCWS : 1.8159529132167371
results of overturningCriticalWindSpeed : 26.503776109225083
results of roughnessSimple_d dans BCWS : 14.44268631123483
results of roughnessSimple_gammaSolved dans BCWS : 3.2791291789197645
results of roughnessSimple_z0 dans BCWS : 1.8159529132167371
results of breakageCriticalWindSpeed : 19.004573558483596
results of NEWGustFactor  : 7.9520315302364715
results of GustFactorInForest  : 6.47607684719374
results of WindProfile :
0.0010287282363700238
0.03207379360739892
1.0
1.9203503678402776

*/

