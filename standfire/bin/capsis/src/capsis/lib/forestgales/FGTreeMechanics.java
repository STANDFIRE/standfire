package capsis.lib.forestgales;
import capsis.kernel.PathManager;

/**
 * ForestGales Tree Mechanics
 *
 * @author B. Gardiner, T. Labbe, C. Meredieu March 2014
 */


public class FGTreeMechanics  {


    public static double calculateMaxOverturningMoment( FGStand stand, FGTree tree,  FGConfiguration configuration) throws Exception {

		boolean noDataFlag = false ;
		boolean beyondRangeFlag = false ;

        //int cultIdx = getCultivationScore();
       int soilDepthIdx = stand.getRootingDepth().getId();

        //int soilIdx = getSoilScore();
        int soilIdx = stand.getSoilType().getId();

        //int specIdx = tree.getSpeciesIndex();
        FGSpecies species = configuration.getSpecies (tree.getSpeciesName ());
        //int specIdx = species.getId(); in CAPSIS JAVA ; NOT USE HERE;

        //double notch   = getSpeciesFreeNotch();

  		double stemWeight = tree.getStemWeight();

        //double maxWt   = maxWTArray [ specIdx- 1][soilIdx-1][cultIdx-1 ];
        double overturningMomentMaximumStemWeights = species.getOverturningMomentMaximumStemWeights()[soilIdx][soilDepthIdx];
        //????????????????????????????????????????????????????????????????????
		// IS IT IMPORTANT TO CHECK, WHERE CAN WE WRITE THIS ANSWER ?
        if( overturningMomentMaximumStemWeights == 0.0 ) noDataFlag = true;
        else if( stemWeight > overturningMomentMaximumStemWeights ) beyondRangeFlag = true;


        //getOverturningMomentMultipliers ()[SOIL TYPE A, B, C, D][soilDepth 0,1,2];
        //double RM = notch * multiplierArray[ specIdx-1][soilIdx-1][cultIdx-1 ] / 100;
        //double overturningMomentMultiplier = species.getOverturningMomentMultipliers ()[soilIdx][soilDepthIdx] /100;
        double overturningMomentMultiplier = species.getOverturningMomentMultipliers ()[soilIdx][soilDepthIdx] ;

        double overturnMoment = overturningMomentMultiplier * stemWeight;

		System.out.println ("results of calculateMaxOverturningMoment : " + overturnMoment);


        /*
        * DON'T USE DRAINAGE
        if( drainage.equalsIgnoreCase( "average") ) return overturnMoment;
        else if( drainage.equalsIgnoreCase( "poor") ) return overturnMoment * 0.8;
        else return overturnMoment * 1.2;
        */

        return overturnMoment;
    }

    public static double calculateMOR ( FGStand stand, FGTree tree,  FGConfiguration configuration) throws Exception {

        FGSpecies species = configuration.getSpecies (tree.getSpeciesName ());

		double MOR = species.getModulusOfRupture();
		double knotFactor = species.getKnotFactor();

		System.out.println ("results of calculateMOR : " + (MOR * knotFactor));

		return MOR * knotFactor ;
	}

	//Gives bending moment at tree base required to break tree.
  	//Diam[0] is the base diameter. See Wood in 'Wind and Trees' for further details}

    public static double calculateBreakingBM( FGStand stand, FGTree tree,  FGConfiguration configuration) throws Exception {


		//double diameter = tree.getDbh_m();
		double diameter = tree.getDiam()[0];

		System.out.println ("results of calculateBreakingBM : " + (calculateMOR(stand, tree, configuration) * Math.PI * Math.pow( diameter, 3.0)/32.0));

        return calculateMOR(stand, tree, configuration) * Math.PI * Math.pow( diameter, 3.0)/32.0;
    }








	public static void main (String [] args) throws Exception {


		// Create a stand object and set its properties
		FGStand stand = new FGStand ();
		stand.setSoilType(FGSoilType.SOIL_TYPE_B);
		stand.setRootingDepth(FGRootingDepth.MEDIUM);

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
		double [] h = null;
		double [] mass = null;

		FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
				crownWeight, diam, h, mass,"Maritime pine");
		stand.addTree (meanTree);

		// Create a configuration object and load the species file
		FGConfiguration configuration = new FGConfiguration ();
		configuration.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");


		System.out.println ("results of calculateMaxOverturningMoment : " + calculateMaxOverturningMoment(stand, meanTree, configuration));
		System.out.println ("results of calculateMOR : " + calculateMOR(stand, meanTree, configuration));
		System.out.println ("results of calculateBreakingBM : " + calculateBreakingBM(stand, meanTree, configuration));



// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGTreeMechanics


/*
FOR
stand.setSoilType(FGSoilType.SOIL_TYPE_A);
stand.setRootingDepth(FGRootingDepth.MEDIUM);
results of calculateMaxOverturningMoment : 439.7135

stand.setSoilType(FGSoilType.SOIL_TYPE_B);
stand.setRootingDepth(FGRootingDepth.MEDIUM);
double dbh_m = 0.2; // m
results of calculateMaxOverturningMoment : 385.54810000000003
results of calculateMOR : 3.06E7
results of calculateBreakingBM : 24033.183799961924

*/




	}
}