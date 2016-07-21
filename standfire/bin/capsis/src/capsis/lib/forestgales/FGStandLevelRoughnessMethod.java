package capsis.lib.forestgales;

import java.util.Date;

import capsis.kernel.PathManager;
import jeeb.lib.util.Check;
import java.util.Collection;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.FileOutputStream;

/**
 * ForestGales' stand level roughness method (loop)
 *
 * @author B. Gardiner, T. Labbe , C. Meredieu - March 2014
 *
 */
public class FGStandLevelRoughnessMethod extends FGMethod {

	/**
	 * Constructor
	 */
	public FGStandLevelRoughnessMethod (FGStand stand, FGConfiguration configuration) {
		super (stand, configuration);

	}

	public String getName () {
		return "ForestGales Stand Level Roughness Method";
	}


	private double [] outputs  = new double [10] ;

	public void run () throws Exception {

		// Use report () for progress messages, see superclass
		report ("\n"+getName ()+" "+new Date ()+"...\n");
		report ("--- Initialisation ---");
		report (stand.toString ());
		report (configuration.toString ());

		double nha = stand.getNha ();
		if (nha < 0 ) { throw new Exception ("The stand is not monospecific or empty !"); }
		double spacing = 100. / Math.sqrt (nha);

		if (stand.getTrees ().size () != 1) { throw new Exception ("tree numbers different from one at stand level !"); }
		FGTree meanTree = (FGTree) stand.getTrees ().iterator ().next ();

        FGTreeCharacteristics.treeCharacteristics( stand, meanTree, configuration) ;

		outputs = FGWindProcess.calculateCriticalWind(  meanTree , stand, configuration );


		report ("--- Results ---\n");
		report ("nha : "+ nha);
		report ("\n spacing : " + spacing);
		report ("\n"+meanTree);
		report ("\n"+getName ()+" ended successfully\n");

	}



	/**
	 * Test of FGStandLevelRoughnessMethod.
	 *
	 * Type the following command from capsis4/ (Capsis install directory)
	 *
	 * Run (Linux): java -cp class:ext/* capsis.lib.forestgales.FGStandLevelRoughnessMethod
	 *
	 * Run (windows): java -cp class;ext\* capsis.lib.forestgales.FGStandLevelRoughnessMethod
	 *
	 */
	public static void main (String[] args) throws Exception {

		String errorMessage;

		System.out.println ("length =" + args.length);

		String s = "";
		for (int i = 0; i < args.length; i++) {
			s += args[i];
			System.out.println ("args "+i+" = "+args[i]);
			System.out.println (PathManager.getDir ("data") + "/forestGales/" +args[0]);

		}
		System.out.println ("Script01 - args=" + s);

		if (args.length == 0) {
			// Create a stand object and set its properties
			FGStand stand = new FGStand ();
			double nha=560;
			stand.setNha (nha);
			stand.setDominantHeight (13.01);

			stand.setSoilType(FGSoilType.SOIL_TYPE_A);
			stand.setRootingDepth(FGRootingDepth.MEDIUM);

			// Create a single tree in the stand: the mean tree
			//Test input variables
			double dbh_m = 0.206; // m
			double height=12.5; //m
			double crownWidth = -1; // m, optional (-1)
			double crownDepth = -1; // m, optional (-1)
			double stemVolume = -1; // m3, optional (-1)
			double stemWeight = -1; // kg, optional (-1)
			double crownVolume = -1; // m3, optional (-1)
			double crownWeight = -1; // kg, optional (-1)
			double [] diam = null;
			double [] z = null;
			double [] mass = null;



			FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
					crownWeight, diam, z, mass,"Maritime pine");
			stand.addTree (meanTree);

			// Create a configuration object and load the species file
			FGConfiguration configuration = new FGConfiguration ();
			configuration.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");


			// Run the stand level roughness method
			FGStandLevelRoughnessMethod m = new FGStandLevelRoughnessMethod (stand, configuration);


			m.setWriteInTerminal (true); // writes details in the terminal
			m.run ();

		} else if (args.length == 1 && Check.isFile (PathManager.getDir ("data") + "/forestGales/"+args[0])) {
			String standFileName = PathManager.getDir ("data") + "/forestGales/" +args[0];
			Collection standRecords;
			Collection unknownStandLines;
			String line;

			PrintWriter standOut = new PrintWriter (new FileOutputStream(PathManager.getInstallDir()+"/tmp/"+args[0]+".csv",true));
//			line = "# Forest gales stand output file";
//			standOut.println (line);
//			line = "";
//			standOut.println (line);


			line = "id;cwsForBreakage;cwsForOverturning;probabilityOfBreakage;probabilityOfOverturning;dbh;ht;CrownWidth;CrownDepth;" +
			"StemWeight;CrownVolume;CrownWeight;SpeciesName;"+
			"gustFactor;maxOverturningMoment;overturningMoment;maxBreakingMoment;breakingMoment;"+
			"UH_Overturn;U_Overturn_10;UH_OverturnEdge;UH_Break;U_Break_10;UH_BreakEdge";
			standOut.println (line);


			// load stand records. If trouble, exception
			FGStandLoader standLoader = new FGStandLoader (standFileName);
			standLoader.load ();
			standRecords = standLoader.getRecords ();
			unknownStandLines = standLoader.getUnknownLines ();
			if (standRecords.size () == 0) {
				throw new Exception ("Could not find any stand records in IFN Stand level file, aborted");
			}
			for (Iterator i = standRecords.iterator (); i.hasNext ();) {
				FGStandLoader.StandRecord r = (FGStandLoader.StandRecord) i.next ();

				FGStand stand = new FGStand ();
				double nha = r.nha;
				stand.setNha (nha);
				stand.setDominantHeight (r.hdom);

				//FGSoilType SOIL_TYPE_A = new FGSoilType (0, "A - Free-draining mineral soils");
				//FGSoilType SOIL_TYPE_B = new FGSoilType (1, "B - Gleyed mineral soils");
				//FGSoilType SOIL_TYPE_C = new FGSoilType (2, "C - Peaty mineral soils");
				//FGSoilType SOIL_TYPE_D = new FGSoilType (3, "D - Deep peats");
				switch(r.soilType) {
					case 0:
					stand.setSoilType(FGSoilType.SOIL_TYPE_A);
					break;
					case 1:
					stand.setSoilType(FGSoilType.SOIL_TYPE_B);
					break;
					case 2:
					stand.setSoilType(FGSoilType.SOIL_TYPE_C);
					break;
					case 3:
					stand.setSoilType(FGSoilType.SOIL_TYPE_D);
					break;
					default:
					stand.setSoilType(FGSoilType.SOIL_TYPE_A); //Default value in FGStand.java
				}

				//FGRootingDepth SHALLOW = new FGRootingDepth (0, "Shallow < 40cm");
				//FGRootingDepth MEDIUM = new FGRootingDepth (1, "Medium 40-80cm");
				//FGRootingDepth DEEP = new FGRootingDepth (2, "Deep > 80cm");
				switch(r.rootingDepth) {
					case 0:
					stand.setRootingDepth(FGRootingDepth.SHALLOW);
					break;
					case 1:
					stand.setRootingDepth(FGRootingDepth.MEDIUM);
					break;
					case 2:
					stand.setRootingDepth(FGRootingDepth.DEEP);
					break;
					default:
					stand.setRootingDepth(FGRootingDepth.MEDIUM); //Default value in FGStand.java
				}

				// Create a single tree in the stand: the mean tree
				double dbh_m = r.meanDbh_m; // m
				double height = r.meanHeight; //m
				double crownWidth = r.meanCrownWidth; // m, optional (-1)
				double crownDepth = r.meanCrownDepth; // m, optional (-1)
				double stemVolume = r.meanStemVolume; // m3, optional (-1)
				double stemWeight = r.meanStemWeight; // kg, optional (-1)
				double crownVolume = r.meanCrownVolume; // m3, optional (-1)
				double crownWeight = r.meanCrownWeight; // kg, optional (-1)
				double [] diam = null;
				double [] z = null;
				double [] mass = null;
				String species = r.species;
				FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
						crownWeight, diam, z, mass, species);
				stand.addTree (meanTree);

				// Create a configuration object and load the species file
				FGConfiguration configuration = new FGConfiguration ();
				configuration.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");


				// Run the stand level roughness method
				FGStandLevelRoughnessMethod m = new FGStandLevelRoughnessMethod (stand, configuration);


				m.setWriteInTerminal (true); // writes details in the terminal
				m.run ();

				//System.out.println ("CwsForBreakage" + meanTree.getCwsForBreakage ());
				//System.out.println ("CwsForOverturning" + meanTree.getCwsForOverturning ());
				line = ""+r.id+";"+meanTree.getCwsForBreakage ()+";"+meanTree.getCwsForOverturning ()+";"+meanTree.getProbabilityOfBreakage ()+";"+meanTree.getProbabilityOfOverturning ()+
				";"+meanTree.getDbh_m()+";"+meanTree.getHeight()+";"+meanTree.getCrownWidth()+";"+meanTree.getCrownDepth()+";"+meanTree.getStemWeight()+";"+
				meanTree.getCrownVolume()+";"+meanTree.getCrownWeight()+";"+meanTree.getSpeciesName()
				+";"+ m.outputs[0] // gustFactor
				+";"+ m.outputs[1] // maxOverturningMoment
				+";"+ m.outputs[2] // overturningMoment
				+";"+ m.outputs[3] // maxBreakingMoment
				+";"+ m.outputs[4] // breakingMoment
				+";"+ m.outputs[5] // UH_Overturn
				+";"+ m.outputs[6] // U_Overturn_10
				+";"+ m.outputs[7] // UH_OverturnEdge
				+";"+ m.outputs[8] // UH_Break
				+";"+ m.outputs[9] // U_Break_10
				+";"+ m.outputs[10] ; // UH_BreakEdge
				standOut.println (line);
			}
			standOut.close ();


		} else {
			errorMessage = "Too many arguments or the argument is not a file name";
			System.out.println (errorMessage);
		}



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
//java -cp class;ext\* capsis.lib.forestgales.FGStandLevelRoughnessMethod


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

