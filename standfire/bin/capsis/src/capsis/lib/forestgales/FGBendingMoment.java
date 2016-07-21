package capsis.lib.forestgales;
import capsis.kernel.PathManager;


/**
 * ForestGales BENDING MOMENT
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu January 2014
 */
public class FGBendingMoment{

	private static final int MAX_NO_OF_TREE_SECTIONS = 50;


	public static double calculateBendingMoment( double forceOnTree, double heightOfForce, double [] Diam, double [] Z, double [] Mass, double dbh, double height,
		int noOfSections, FGSpecies species, FGConfiguration configuration ){


		double K = species.getRootBendingK () ;  //  {root bending term. See paper by Neild and Wood}
		double MOE = species.getModulusOfElasticity ();
		double G = configuration.getGravityAcceleration ();


		//if d too close to the top of the tree use a default height for the action of the wind
		if ( heightOfForce > 0.8 * height )heightOfForce = 0.8 * height;



		// initialise ii
		double Slope = 0.0;
		double R     = 0.0;
		double fac1   = 0.0;
		double fac2    = 0.0;
		double fac1Max = 0.0;
		double fac2Max = 0.0;
		double eq      = 0.0;
		double maxEq   = 0.0;
		boolean Finish = false;
		double [] Y = new double[  MAX_NO_OF_TREE_SECTIONS ];  //{displacement with height}
		double [] I = new double[  MAX_NO_OF_TREE_SECTIONS ];//  {second area moment of inertia}
		double [] Z_H = new double[  MAX_NO_OF_TREE_SECTIONS ];//  {ratio of height from top of tree to tree height}
		double [] YOld = new double[  MAX_NO_OF_TREE_SECTIONS ];
		double [] MX   = new double[  MAX_NO_OF_TREE_SECTIONS ];
		double [] M_EI = new double[  MAX_NO_OF_TREE_SECTIONS ];
		double [] Mz_EI= new double[  MAX_NO_OF_TREE_SECTIONS ];
		double [] MG   = new double [51];
		double [] MP   = new double [51];
		double [] MTotal = new double[51];


		for ( int i = 0 ; i < noOfSections; i++) Y[i] = 0.0;

		// Assign values to R, Fac1Max, Fac2Max and MaxEq
		R = (height-heightOfForce)/height;
		fac1Max = Math.pow(R, 0.6);
		fac2Max = Math.pow(R, -0.4);
		maxEq= (2.5 * R - 4.17 * fac1Max - 0.71 * R * R - 1.79 * R * fac2Max + 1.67 + 2.5 * R);



		for (int ii = 0; ii < noOfSections ; ii++ ){

		  /*if( ii == 29 )
			  System.out.println( "in bad sector" );*/
		  Z_H[ii] = (height - Z[ii]) / height;
		  I[ii] = Math.PI * Math.pow (Diam[ii], 4) / 64.0;
		  fac1 = Math.pow(Z_H[ii], 0.6);
		  fac2 = Math.pow(Z_H[ii], -0.4);
		  eq = (2.5 * Z_H[ii] - 4.17 * fac1 - 0.71 * Z_H[ii] * R - 1.79 * R * fac2 + 1.67 + 2.5 * R);
		  if ( Z[ii] < heightOfForce )
			Y[ii] = forceOnTree * height * height * height * eq / (MOE * I[0]);
		  else
		  {
			if( (int)Math.rint(heightOfForce * noOfSections / height) > 0 ){
			Slope = (Y[(int)Math.rint(heightOfForce * noOfSections / height)] -
					 Y[(int)Math.rint(heightOfForce * noOfSections / height) - 1]) /
					 (Z[(int)Math.rint(heightOfForce * noOfSections / height)] -
					 Z[(int)Math.rint(heightOfForce * noOfSections / height) - 1]);
			Y[ii] = (forceOnTree * height * height * height * maxEq /
					 (MOE * I[0])) + Slope * (Z[ii] - heightOfForce);
			}
		  }
		// first guess at tree bending based on Gardiner 1989
		 if( Y[ii] > (Z[ii] * Math.sin(60.0 * Math.PI / 180.0)) )
			 Y[ii] = (Z[ii] * Math.sin(60 * Math.PI / 180)); //stops tree bending more than 30 degs
		}
		// end for ii
		//ok to here
		do{//while ( Finish == false){
		Finish = true;


		for( int ii= 0 ; ii < (noOfSections); ii++ ){
			MG[ii] = 0;
			YOld[ii] = Y[ii];
			MX[ii] = Mass[ii] * YOld[ii];
			Y[ii] = 0;
		}
		// double loop



		for( int ii = 0; ii < (noOfSections); ii++ )
			for( int iii = ii ; iii< (noOfSections); iii++ )
				MG[ii] = MG[ii] + (MX[iii] - YOld[ii] * Mass[iii]) * G * (height / noOfSections);
		//ok to here


		for( int ii= 0 ; ii < (noOfSections) ; ii++ ){
			if( Z[ii] < heightOfForce )  MP[ii] = forceOnTree * (heightOfForce - Z[ii]);
			else MP[ii]=0;

			MTotal[ii] = MG[ii] + MP[ii];

			if ( Diam[ii] > 0  ) M_EI[ii] =  MTotal[ii] * 64.0 / (MOE * Math.PI * Math.pow(Diam[ii], 4.0));
			else M_EI[ii] = 0;
			Mz_EI[ii] = M_EI[ii] * Z[ii];
		}



		for( int ii = 0; ii <(noOfSections); ii++ )
		  for (int iii =0 ; iii <= ii; iii++ )
			Y[ii] = Y[ii] + (Z[ii] * M_EI[iii] - Mz_EI[iii]) * (height / noOfSections);

		for( int ii = 0; ii <(noOfSections); ii++ )
		  Y[ii] = Y[ii] + Z[ii] * MTotal[0] * K * height * 64.0 / (MOE * Math.PI * Math.pow (dbh, 4.0));




		 for( int ii = 0; ii <(noOfSections); ii++ )
		 {
		   if( Y[ii] > (Z[ii] * Math.sin(60 * Math.PI / 180.0)) )
			   Y[ii] = (Z[ii] * Math.sin(60 * Math.PI / 180.0)); //{stops tree bending more than 30 degs}
		   //System.out.println( "Y = " + Y[ii] + " : YOld = " + YOld[ii] + " : Diff = "+ Math.abs(Y[ii] - YOld[ii]) );
		   if( Math.abs(Y[ii] - YOld[ii]) > Y[ii] / 100.0 )
			 Finish = false;//false;
		 }



		}while( Finish == false );
		return MTotal[0];

		}

	public static void main (String [] args) throws Exception {

			FGConfiguration configuration = new FGConfiguration ();
			configuration.loadSpeciesMap (PathManager.getDir ("Data") + "/forestGales/forestGalesSpecies.txt");

		//Test input variables
		FGSpecies species = configuration.getSpecies ("Maritime pine");


		//Test input variables

		double forceOnTree = 467;
		double heightOfForce= 4.19;
		double [] Diam = {0.15,0.10,0.05};
		double [] Z = {0.15,0.10,0.05};
		double [] Mass = {0.15,0.10,0.05};
		int noOfSections = 3;

		double dbh = 0.2; // m
		double height=20; //m



		double bendingMoment =calculateBendingMoment( forceOnTree, heightOfForce, Diam, Z, Mass, dbh, height, noOfSections, species, configuration);
		System.out.println 	("results of calculateBendingMoment: " + bendingMoment );






	// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGBendingMoment

	/*

	*/

	}










}