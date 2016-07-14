package capsis.lib.forestgales;
import capsis.kernel.PathManager;




/**
 * ForestGales WIND SPEED
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu October 2013
 */


public class FGWindSpeed{


	//Wind Speed at 10m Above zero plane displacement
    public static double elevate( double  UH_Speed, double AZ0value, double ADValue, double meanHeight ){

        return UH_Speed * Math.log ( 10/ AZ0value) / Math.log( (meanHeight - ADValue)/AZ0value );
    }


	//Wind Speed at Forest Edge
    public static double edgeSpeed( double UH_Speed, double AZ0value, double ADValue, double meanHeight, double calcHT, double surroundingLandRoughness){

		//*double CALC_HT = configuration.getHeightOfCalculation();
		//*double FIELDZ0 =configuration.getFieldZ0(); = surroundingLandRoughness in Configuration


        return ( UH_Speed/ Math.log( calcHT/AZ0value)) * Math.log((1000-ADValue)/AZ0value )
               * Math.log( 1/surroundingLandRoughness)/Math.log( 1000/(surroundingLandRoughness * meanHeight) );
	}


	//Wind Speed Methods
	// Need three methods

	public static double calcU ( double UH_Speed, double x, double Th, double z1, double z2, double dforest, double surroundingLandRoughness, FGConfiguration configuration){


		double U;
		double delta = calcDepthOfIBL (x,z1,z2,dforest);
		// more complicated calculation is available - bg FGWindSpeed BG.docx 2011213
		//       alpha:=-2.4*ln(fcurrentSpacing/Th)+1.621;
		//        SetLength(Result, Length(deltanext));

		 if (Th<dforest) Th = dforest;

		 if (Th>delta) {
			 U= UH_Speed * calcUUpWind (10., surroundingLandRoughness, configuration)/calcUUpWind (Th , surroundingLandRoughness, configuration);
		 } else {
			 U =UH_Speed * calcUUpWind (10., surroundingLandRoughness, configuration)/((calcUUpWind (delta, surroundingLandRoughness, configuration)/Math.log((delta - dforest)/z2))* Math.log((Th - dforest)/z2));
		 }

		 // don't used - bg FGWindSpeed BG.docx 2011213
		 //double U10 = calcUUpWind(10.);
		 //double Udelta = calcUUpWind (delta);
		 return U ;
	 }



	public static double calcDepthOfIBL (double x, double z1, double z2, double d) {
		double M = Math.log (z1 /z2);
		double A1 = 0.75 + 0.03 * M ;

		return A1 * z2 * Math.pow ((x/z2), 0.8) + d;
	}

	public static double calcUUpWind (double z, double surroundingLandRoughness, FGConfiguration configuration) {

		//*double CALC_HT = configuration.getHeightOfCalculation();
		//*double FIELDZ0 =configuration.getFieldZ0(); surroundingLandRoughness in Configuration

		double k = configuration.getVonKarmanConstant () ;

		return (1/k)* Math.log (z/surroundingLandRoughness);


	}

	public static void main (String [] args) throws Exception {

		FGConfiguration configuration = new FGConfiguration ();
		///configuration.loadSpeciesMap (PathManager.getDir ("Data") + "/forestGales/forestGalesSpecies.txt");

		//FGStand stand = new FGStand ();
		//stand

		double UH_Speed=10;
		double AZ0value=1;
		double ADValue=15;
		double meanHeight=20;

		System.out.println ("results of elevate : " + elevate ( UH_Speed, AZ0value, ADValue, meanHeight ));

		// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGWindSpeed

		double calcHT=10;
		double surroundingLandRoughness=0.3;

		System.out.println 	("results of edgeSpeed : " + edgeSpeed( UH_Speed, AZ0value,  ADValue,  meanHeight, calcHT, surroundingLandRoughness));


		double x=100;
		double Th=20;
		double z1=0.3;
		double z2=1;
		double dforest=15;


		System.out.println 	("results of calcU : " + calcU ( UH_Speed,  x, Th,  z1,  z2, dforest,  surroundingLandRoughness, configuration));


		System.out.println 	("results of calcDepthOfIBL  : " + calcDepthOfIBL (x,  z1, z2, dforest));


		System.out.println 	("results of calcUUpWind with 10 : " + calcUUpWind (10.,  surroundingLandRoughness, configuration));
		System.out.println 	("results of calcUUpWind with Th : " + calcUUpWind (Th,  surroundingLandRoughness, configuration));

		double delta = calcDepthOfIBL (x,z1,z2,dforest);

		System.out.println 	("results of calcUUpWind with delta : " + calcUUpWind (delta,  surroundingLandRoughness, configuration));

/*
// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGWindSpeed
//Test input variables
//UH_Speed=10
//AZ0value=1
//ADValue=15
//meanHeight=20
//calcHT=10
//surroundingLandRoughness=0.3
//UH_Speed=10
//x=100
//Th=20
//z1=0.3
//z2=1
//dforest=15


First Result with VonKarman = 1 FALSE FALSE FLASE
results of elevate : 14.306765580733934
results of edgeSpeed : 7.044602746022005
results of calcU : 14.658553422617574
results of calcDepthOfIBL  : 43.420107171851626
results of calcUUpWind with 10 : 3.506557897319982
results of calcUUpWind with Th : 4.199705077879927
results of calcUUpWind with delta : 4.974895437033092


New results with VonKarman in Configuration file
results of elevate : 14.306765580733934
results of edgeSpeed : 7.044602746022005
results of calcU : 14.658553422617574
results of calcDepthOfIBL  : 43.420107171851626
results of calcUUpWind with 10 : 8.766394743299955
results of calcUUpWind with Th : 10.499262694699818
results of calcUUpWind with delta : 12.437238592582728

*/



	}




}

