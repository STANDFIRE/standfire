package capsis.lib.forestgales;
import capsis.kernel.PathManager;
import capsis.lib.forestgales.function.Function;

/**
 * ForestGales Tree Characteristics
 *
 * @author B. Gardiner, T. Labbe, C. Meredieu March 2014
 *
 * This part of the programme needs Mensurational information
 * This procedure is used to calculate Canopy Breath, Canopy Depth, Stem weight
 * and Canopy weight.  It takes Species as parameters.
 * If the growth model is able to give values for these 4 variables, FG don't use this computation. Check values ne to -1
 */


public class FGTreeCharacteristics  {
	private static final int MAX_NO_OF_TREE_SECTIONS = 50;


		public static void treeCharacteristics( FGStand stand, FGTree tree,  FGConfiguration configuration) throws Exception {

		FGSpecies species = configuration.getSpecies (tree.getSpeciesName ());
		double snowDepth = 0 ; //No snow : We must add a text fiedl in the dialog box to open this option;

		 //int i;//  : Word;        // counter used in the calculation of Branch Volume at each section
		 double DCanopyBase;//  : Single;      // Diameter at the bottom of the canopy
		 double C1         ;//  : Single;      // C1 is a constant to estimate diameter above Canopy Base
		 double C2         ;//  : Single;      // C2 is a constant calculated to measure diameters below MidCanopy
		 double DBH_Height ;//  : Single;      // Height at which DBH is meassured
		 double [] Z = new double[  MAX_NO_OF_TREE_SECTIONS ];
		 double [] Diam = new double[  MAX_NO_OF_TREE_SECTIONS ];
		 double [] branchWidth = new double[  MAX_NO_OF_TREE_SECTIONS ];
		 double [] MSnow = new double[  MAX_NO_OF_TREE_SECTIONS ];
		 double [] mass = new double[  MAX_NO_OF_TREE_SECTIONS ];


	  	//XXX count the number of times that routine is called
	  	//treeCharacteristicsCounter++;


		//calculate crown width
		double canopyBdth = tree.getCrownWidth() ;
		if (canopyBdth < 0) { //value -1 in from the model
			canopyBdth = calculateCanopyBreadth( stand, tree, configuration);
			tree.setCrownWidth(canopyBdth);
		}


	 	// Calculate Canopy Depth
	 	double canopyDepth = tree.getCrownDepth();
	 	if (canopyDepth < 0) {//value -1 in from the model
			canopyDepth = calculateCanopyDepth( stand, tree, configuration);
			tree.setCrownDepth(canopyDepth);
		}


		// Calculate Canopy Height
		double height = tree.getHeight();
		double canopyHeight = height - canopyDepth;

		// Calculate Mid Canopy height
		//{ This is an emprirical model where above MidCanopy the diameter is
		//  constant, while below it the diameter = cubicroot(aZ)}
		double midCanopy = height - (canopyDepth / 2.0);

		// Trap to get rid of small trees. If MidCanopy goes below 1.3 m you cannot measure DBH
		if( midCanopy > 1.3 ) DBH_Height = 1.3;
		else DBH_Height=0;


	  	// Find Green Wood Density
		double stemDensity= species.getGreenWoodDensity();

		//case fSpecies of
		//For MARITIME PINE stemDensity = 903.4 kg/m3 =/- 58.3 //inital parameters given by Cucchi et al, 2004 based on tree pulling trees
		//spSP..spDF: StemDensity := 850;  {Data from Finland}
		//spNS,spSS : StemDensity := 850;  {Average for Sitka spruce in British tree pulling data base}
		//spNF..spWH: StemDensity := 850;  {Data from Finland}


		// Calculate Canopy Density
		double canopyDensity = species.getCanopyDensity();
		//For MARITIME PINE canopyDensity = 2.73 kg/m3 +/- 0.86 //inital parameters given by Cucchi et al, 2004 based on tree pulling trees
		//canopyDensity = 2.5; //  {This bit needs analysis of Tree Pulling Database}



		// Calculate Snow Weight
		//{ At present this is a relatively simple model and it's barely used by the model}
		double snowWeight = snowDepth * configuration.getSnowDensity() * Math.PI * Math.pow((canopyBdth / 2.0),2);


		// C2 is a constant calculated to measure diameters below MidCanopy
		// Therefore d(Z) = C2*Pow(Z, 0.33)
		double dbh = tree.getDbh_m();
		C2 = dbh / Math.pow ((midCanopy - DBH_Height), 0.333);
		DCanopyBase = C2 * Math.pow ((canopyDepth / 2),0.333);

		// C1 is a constant to estimate diameter above Canopy Base
		// Therefore d(z') = C1* z'  (z'= tree height above Canopy Base
		C1 = DCanopyBase / canopyDepth;


		//calculate the number of section for one tree depending of the total height, section >=1m;
		int noOfSections = (int) height;
		System.out.println ("noOfSections =" + noOfSections);

		//Calculate the stem taper Diam[i] and branchWidth[i] Be careful model can give these values, CHECK THIS !
		for (int i = 0 ; i < noOfSections; i++){
			Z[i] = i * height / noOfSections;
			if ( Z[i] < canopyHeight ) Diam[i] = C2 * Math.pow((midCanopy - Z[i]), 0.333);
			//assumes constant stress for stem below crown and loading at mid canopy
			else Diam[i] = C1 * (height - Z[i]);
			// assumes linear change of diameter in crown after Mattheck
			if( Z[i] < canopyHeight ) branchWidth[i] = 0;
				else if (Z[i] < midCanopy) branchWidth[i] = canopyBdth * (Z[i] - canopyHeight) / (midCanopy - canopyHeight);
				else branchWidth[i] = canopyBdth * (1 - (Z[i] - midCanopy)/(height - midCanopy));
		}


		// Calculate branch volume AND Branch Weight or crown weight
		double branchWeight = tree.getCrownWeight();
		double branchVolume = tree.getCrownVolume ();
		if (tree.getCrownVolume () < 0 ){//value -1 in from the model
			branchVolume = 0;
			for (int i = 0 ; i < noOfSections; i++){
				branchVolume = branchVolume + Math.PI * (height / noOfSections) * Math.pow (branchWidth[i] / 2.0, 2.0);
			}
			if( branchVolume < 0.1 ) branchVolume = 0.1;
			tree.setCrownVolume(branchVolume);
		}

		if (tree.getCrownWeight () < 0 ) {//value -1 in from the model
			branchWeight = branchVolume * canopyDensity;//slight difference here
			tree.setCrownWeight(branchWeight);
		}



		// Calculate Stem Weight
		double stemWeight=0;
		for(int i= 0; i < noOfSections; i++ ){
			MSnow[i] = (Math.PI * (height / noOfSections) * Math.pow(branchWidth[i] / 2.0, 2.0))* snowWeight / branchVolume;
			mass[i] = ( stemDensity * Math.PI * (Diam[i] / 2) * (Diam[i] / 2)
				 + canopyDensity * Math.PI * (branchWidth[i] / 2) * (branchWidth[i] / 2)) * height / noOfSections + MSnow[i];
			stemWeight = stemWeight + stemDensity * Math.PI * (Diam[i] / 2) * (Diam[i] / 2) * height / noOfSections;
		}
		for ( int i = 0 ; i < noOfSections; i ++ ){
			mass[i] = mass[i] * noOfSections / height;
			System.out.println ("mass["+ i+"] =" + mass[i]);
			System.out.println ("Z["+ i+"] =" + Z[i]);
			System.out.println ("Diam["+ i+"] =" + Diam[i]);
		}

		if ((tree.getStemWeight() < 0) && (tree.getStemVolume() < 0)) {//value -1 in from the model
				tree.setStemWeight(stemWeight);
		} else if ((tree.getStemWeight() < 0) && (tree.getStemVolume() > 0)) {//value -1 in from the model
			stemWeight=tree.getStemVolume() * stemDensity;
			tree.setStemWeight(stemWeight);
		} else if (tree.getStemWeight() >0 ){
			stemWeight=tree.getStemWeight();
		}

	tree.setMass(mass);
	tree.setH(Z);
	tree.setDiam(Diam);


	}




	// Calculate crown diameter or canopy breadth or canopy width;
    public static double calculateCanopyBreadth ( FGStand stand, FGTree tree,  FGConfiguration configuration) throws Exception {
        double result = 0.0;

        double dbh_m = tree.getDbh_m();
        double height = tree.getHeight();

        double nha = stand.getNha ();
		double spacing = 100. / Math.sqrt (nha);

        FGSpecies species = configuration.getSpecies (tree.getSpeciesName ());
        Function canopyWidthFunction = species.getCanopyWidthFunction();


        //For MARITIME PINE result = 0.15674 *  DBH; //inital parameters given by Cucchi 2004 for dbh in cm
        result = canopyWidthFunction.f(dbh_m*100);

       /*
        if( species.equalsIgnoreCase("SP") ) result = -0.097 + 0.146 * meanDBH * 100;
        else if( species.equalsIgnoreCase("CP") ) result = -0.097 + 0.146 * meanDBH * 100;
        else if( species.equalsIgnoreCase("LP") ) result = 0.1022 + 0.2918 * meanDBH * 100;
        else if( species.equalsIgnoreCase("EL") ) result = 0.191 * meanDBH * 100 - 0.22;
        else if( species.equalsIgnoreCase("HL") ) result = 0.1754 * meanDBH * 100 - 0.4519;
        else if( species.equalsIgnoreCase("JL") ) result = 0.1754 * meanDBH * 100 - 0.4519;
        else if( species.equalsIgnoreCase("DF") ) result = 6.4-40 * Math.pow (meanDBH * 100, -0.9);
        else if( species.equalsIgnoreCase("NS") ) result = 0.126 * meanDBH * 100 + 0.3145;
        else if( species.equalsIgnoreCase("SS") ) result = 0.064 + 0.1549 * meanDBH * 100;
        else if( species.equalsIgnoreCase("NF") ) result = 0.1255 * meanDBH * 100 + 0.3306;
        else if( species.equalsIgnoreCase("GF") ) result = 0.1255 * meanDBH * 100 + 0.3306;
        else if( species.equalsIgnoreCase("WH") ) result = 4.5-40 * Math.pow (meanDBH * 100, -1.1);
		*/
        if( result > 2.0 * spacing ) {
			result = 2.0 * spacing;
		}
        if( result > height ) result = height;

        return result;
    }


	// Calculate crown height or crown depth or canopy depth;
    public static double calculateCanopyDepth( FGStand stand, FGTree tree,  FGConfiguration configuration) throws Exception {
        double result = 0.0;

        double height = tree.getHeight();


        FGSpecies species = configuration.getSpecies (tree.getSpeciesName ());
        Function canopyDepthFunction = species.getCanopyDepthFunction();

         //For MARITIME PINE result = 0.3156 *  height + 1.3424;	//inital parameters given by Barry Gardiner in text file
        result = canopyDepthFunction.f(height);


        /*
        if( species.equalsIgnoreCase("SP") ) result = 0.557 *  meanHeight - 1.83;
        else if( species.equalsIgnoreCase("CP") ) result = 0.4118 * meanHeight + 0.6658;
        else if( species.equalsIgnoreCase("LP") ) result = 0.4605 * meanHeight + 1.0356;
        else if( species.equalsIgnoreCase("EL") ) result =  3.341 * Math.exp(0.0496 * meanHeight);
        else if( species.equalsIgnoreCase("HL") ) result = 0.3423 * meanHeight + 1.9933;
        else if( species.equalsIgnoreCase("JL") ) result = 0.3423 * meanHeight + 1.9933;
        else if( species.equalsIgnoreCase("DF") ) result = 0.5863 * meanHeight - 1.023;
        else if( species.equalsIgnoreCase("NS") ) result = 0.683 *  meanHeight - 1.66;
        else if( species.equalsIgnoreCase("SS") ) result = 0.3489 * meanHeight + 1.7828;
        else if( species.equalsIgnoreCase("NF") ) result = 0.5228 * meanHeight + 0.2032;
        else if( species.equalsIgnoreCase("GF") ) result = 0.5205 * meanHeight + 0.6119;
        else if( species.equalsIgnoreCase("WH") ) result = 0.1636 * meanHeight + 4.7828;
        */
        if( result > height ) return height;
        if( result < 0 ) return 0;	// error in the initial Java code
        return result;
    }














/*
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

		FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
				crownWeight, "Maritime pine");
		stand.addTree (meanTree);

		// Create a configuration object and load the species file
		FGConfiguration configuration = new FGConfiguration ();
		configuration.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");


		System.out.println ("results of calculateMaxOverturningMoment : " + calculateMaxOverturningMoment(stand, meanTree, configuration));
		System.out.println ("results of calculateMOR : " + calculateMOR(stand, meanTree, configuration));
		System.out.println ("results of calculateBreakingBM : " + calculateBreakingBM(stand, meanTree, configuration));



// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGTreeMechanics

*/
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






	}
	*/
}
