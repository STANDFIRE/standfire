package capsis.lib.forestgales;
import capsis.kernel.PathManager;

/**
 * ForestGales Wind PROCESS
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu February 2014
 */


public class FGWindProcess  {


    public static double[] calculateCriticalWind( FGTree tree , FGStand stand, FGConfiguration configuration )throws Exception {

		FGSpecies species = configuration.getSpecies (tree.getSpeciesName ());
		double stopLimit = configuration.getResolutionOfCalculation();
		double surroundingLandRoughness = configuration.getSurroundingLandRoughness();
		double heightOfCalculation = configuration.getHeightOfCalculation();

		double nha = stand.getNha ();
		double spacing = 100. / Math.sqrt (nha);

		double height = tree.getHeight ();
		//calculate the number of section for one tree depending of the total height, section >=1m;
		int noOfSections = (int) height;

		double crownWidth = tree.getCrownWidth ();
		double crownDepth = tree.getCrownDepth ();
		double stemWeight = tree.getStemWeight ();
		double dbh = tree.getDbh_m ();
		double [] diam = tree.getDiam();
		double [] Z = tree.getH();
		double [] mass = tree.getMass();

		double uCritical = 0d;

		double [] outputs = new double[11] ; // 11 ouput variables from 0 to 10 ;


		// Procedure to calculate Critical Wind Speed
		//checkEdgeAndGap(); // check Values of Edge and GapWidth
		//gustiness  = calculateGustiness(currentSpacing, meanHeight, edge , getGapWidth() ) ;//5);//edge, gapWidth);
		//edgeFactor = calculateEdgeFactor(currentSpacing, meanHeight, edge/*edge , getGapWidth() ) ;//);// edge, gapWidth);
		// Calculate GustFactor
		//gustFactor = gustiness * edgeFactor;

		//double gustFactor = FGGustFactor.calculateGustFactorInForest (stand, spacing, height);
		double gustFactor = FGGustFactor.calculateNEWGustFactor(stand, spacing, height);
		outputs[0] = gustFactor ;

		// Calculate Maximum Overturning Moment as a function of Species, Soil, Cultivation, Drainage and Stem Weight.
		//  public static double calculateMaxOverturningMoment( FGStand stand, FGTree tree,  FGConfiguration configuration)

		double maxOverturningMoment = FGTreeMechanics.calculateMaxOverturningMoment(stand,  tree,  configuration);
		outputs[1] = maxOverturningMoment ;

		// calculate Overturning Moment as a ratio between Maximum Overturning Moment and Gust Factor
		double overturningMoment = maxOverturningMoment / gustFactor;
		outputs[2] = overturningMoment ;

		// Calculate Maximum Breaking Moment
		//public static double calculateBreakingBM( FGStand stand, FGTree tree,  FGConfiguration configuration)
		double maxBreakingMoment = FGTreeMechanics.calculateBreakingBM(stand,  tree,  configuration);
		outputs[3] = maxBreakingMoment ;


		// Calculate Breaking Moment as a ratio between Maximum Breaking Moment and the Gust Factor
		double breakingMoment = maxBreakingMoment / gustFactor;
		outputs[4] = breakingMoment ;


		// Get MOE
		double modulusOfElasticity = species.getModulusOfElasticity ();


		// Calculate Critical Wind Speed for Overturning at tree height
		double UH_Overturn = calculateWindProcess(overturningMoment, stopLimit, crownWidth, crownDepth, spacing, diam, Z, mass, dbh, height,noOfSections, species, configuration );
		outputs[5] = UH_Overturn ;

		//calc roughness
		double[] roughnessParameters = FGRoughnessSimple.roughnessSimple (species, UH_Overturn, crownWidth, crownDepth, height, spacing, configuration);

		double roughnessSimple_d = roughnessParameters  [0];
		double roughnessSimple_z0 = roughnessParameters  [2];
		double roughnessSimple_gammaSolved = roughnessParameters [1] ;



		// Calculate the wind speed at 10 metres above zero plane displacement
		double U_Overturn_10 = FGWindSpeed.elevate(UH_Overturn, roughnessSimple_z0, roughnessSimple_d, height);
		outputs[6] = U_Overturn_10 ;


		// Calculates wind speed at tree top height at the edge of the forest
		double UH_OverturnEdge =  FGWindSpeed.edgeSpeed(U_Overturn_10, roughnessSimple_z0, roughnessSimple_d, height, heightOfCalculation, surroundingLandRoughness);
		outputs[7] = UH_OverturnEdge ;



		// Calculate Critical Wind Speed for Breaking at tree height
		 double UH_Break = calculateWindProcess(breakingMoment, stopLimit, crownWidth, crownDepth, spacing, diam, Z, mass, dbh, height,noOfSections, species, configuration );
		outputs[8] = UH_Break ;

		//calc roughness
		roughnessParameters = FGRoughnessSimple.roughnessSimple (species, UH_Break, crownWidth, crownDepth, height, spacing, configuration);

		roughnessSimple_d = roughnessParameters  [0];
		roughnessSimple_z0 = roughnessParameters  [2];
		roughnessSimple_gammaSolved = roughnessParameters [1] ;


		// Calculate the wind speed at 10 metres above zero plane displacement
		double U_Break_10    = FGWindSpeed.elevate(UH_Break, roughnessSimple_z0, roughnessSimple_d, height);
		outputs[9] = U_Break_10 ;

		// Calculates wind speed at tree top height at the edge of the forest
		double UH_BreakEdge =  FGWindSpeed.edgeSpeed(U_Break_10, roughnessSimple_z0, roughnessSimple_d, height, heightOfCalculation, surroundingLandRoughness);
		outputs[10] = UH_BreakEdge ;



		if( U_Overturn_10 < U_Break_10 ) {
			uCritical = U_Overturn_10;
		} else {
			uCritical = U_Break_10;
		}

		//Results AT TREE HEIGHT (< 15/12/2014)
//		tree.setCwsForBreakage (UH_Break);
//		tree.setCwsForOverturning (UH_Overturn);
		//Results 10 metres above zero (15/12/2014)
		tree.setCwsForBreakage (U_Break_10);
		tree.setCwsForOverturning (U_Overturn_10);
		return outputs ;
    }



	public static double calculateWindProcess( double criticalMoment, double stopLimit, double crownWidth, double crownDepth, double spacing,
		double [] Diam,  double [] Z , double [] Mass, double dbh , double height, int noOfSections, FGSpecies species, FGConfiguration configuration ){

        double guessWindSpeed = 64;  // Guess 64 m/s
        double delta = guessWindSpeed / 2.0;
        double forceOnTree;
        double heightOfForce;

        //calculateWindProfile( currentSpacing );//loop invariant
        do
        {
            /*
             * suspected loop invariant method call
             */
            //porosity = calculateStreamLining(guessWindSpeed);//
            // Calculate Roughness (Z0), Zero Plane Displacement (D) and GammaSolved

			// 1. calc roughness
			double[] roughnessParameters = FGRoughnessSimple.roughnessSimple (species, guessWindSpeed, crownWidth, crownDepth, height, spacing, configuration);

			double roughnessSimple_d = roughnessParameters  [0];
			double roughnessSimple_z0 = roughnessParameters  [2];
			double roughnessSimple_gammaSolved = roughnessParameters [1] ;


            /*
             * suspected loop invariant method call
             */
            //calculateWindProfile( currentSpacing );
			//5. calc wind profile
			double [] windProfile = FGCriticalWindSpeed.calculateWindProfile( spacing, height, roughnessSimple_d ,roughnessSimple_z0 );



            // Calculate Force on tree and Height of Force.  These parameters will be passed to
            // the function that calculates the Bending Moment

            // Calculate Force on tree
            forceOnTree = FGRoughnessSimple.calculateForce( guessWindSpeed , spacing , roughnessSimple_d, roughnessSimple_gammaSolved, configuration);



            // Calculate Bending Moment.
            //BendingMoment := CalculateBendingMoment(ForceOnTree, HeightOfForce, AMeanHeight, bugger,
                           //  NoOfSections, Z, Diam, Mass);
            //bendingMoment = calculateBendingMoment(forceOnTree, HeightOfForce, Diam, Z, Mass, meanDBH );


			heightOfForce = roughnessSimple_d;
            double bendingMoment = FGBendingMoment.calculateBendingMoment(forceOnTree, heightOfForce, Diam,  Z, Mass,  dbh, height,  noOfSections, species, configuration);
			System.out.println ("bendingMoment : " + bendingMoment);
			System.out.println ("forceOnTree : " + forceOnTree);

			System.out.println ("heightOfForce : " + heightOfForce);


            //is this some sort of binary search condition???
            if ( bendingMoment > criticalMoment ){
				guessWindSpeed = guessWindSpeed - delta;
			} else {
				guessWindSpeed = guessWindSpeed + delta;
			}
            delta = delta  /2.0;
        }
        while (delta < stopLimit == false );
        return guessWindSpeed;
    }



	public static void main (String [] args) throws Exception {
/*
			FGStand stand = new FGStand ();
			//configuration.loadSpeciesMap (PathManager.getDir ("Data") + "/forestGales/forestGalesSpecies.txt");

			//Test input variables
			double treeHeight=20;
			double gapSize = 20d;
			double nha=2000;
			double spacing = 100. / Math.sqrt (nha);
			double distanceToEdge = 10d;


			System.out.println ("results of calculateEdgeFactor : " + calculateEdgeFactor
				(spacing,treeHeight, distanceToEdge , gapSize, stand));
*/


// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGWindProcess


	}
}