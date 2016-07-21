package capsis.lib.forestgales;
import capsis.kernel.PathManager;


/**
 * ForestGales STREAMLINING
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu January 2014
 */
public class FGStreamlining  {

	public static double calcStreamlining(double windSpeed, double canopyStreamliningN, double canopyStreamliningC) {

		 if( windSpeed < 10) windSpeed = 10;
		 if( windSpeed > 25) windSpeed = 25;
		 return canopyStreamliningC * Math.pow( windSpeed, -1 * canopyStreamliningN );

		//return 0.5;
	}

/*
**** NOt different with calcPorosity ***********************************
    public double calculateStreamLining( double windSpeed ){
        double C = 2.35;
        double N = 0.51;
        if( species.equalsIgnoreCase("SP") ) { C=3.07; N=0.75;}
        if( species.equalsIgnoreCase("CP") ) { C=2.57; N=0.61;}
        if( species.equalsIgnoreCase("LP") ) { C=2.48; N=0.63;}
        if( species.equalsIgnoreCase("EL") ) { C=3.07; N=0.75;}
        if( species.equalsIgnoreCase("HL") ) { C=3.07; N=0.75;}
        if( species.equalsIgnoreCase("JL") ) { C=3.07; N=0.75;}
        if( species.equalsIgnoreCase("DF") ) { C=2.4; N=0.7;}
        if( species.equalsIgnoreCase("NS") ) { C=2.35; N=0.51;}
        if( species.equalsIgnoreCase("SS") ) { C=2.35; N=0.51;}
        if( species.equalsIgnoreCase("NF") ) { C=4.7; N=0.74;}
        if( species.equalsIgnoreCase("GF") ) { C=4.7; N=0.74;}
        if( species.equalsIgnoreCase("WH") ) { C=1.51; N=0.68;}
        if( windSpeed < 10) windSpeed = 10;
        if( windSpeed > 25) windSpeed = 25;
        return C * Math.pow( windSpeed, -1 * N );
    }
*/



	public static void main (String [] args) throws Exception {

			FGConfiguration configuration = new FGConfiguration ();
			configuration.loadSpeciesMap (PathManager.getDir ("Data") + "/forestGales/forestGalesSpecies.txt");

		//Test input variables
		FGSpecies species = configuration.getSpecies ("Maritime pine");


		//Test input variables
		double windSpeed=10;
		double canopyStreamliningC = species.getCanopyStreamliningC() ;
		double canopyStreamliningN=species.getCanopyStreamliningN();

		double streamlining= calcStreamlining(windSpeed, canopyStreamliningN, canopyStreamliningC);
		System.out.println 	("results of calcStreamlining: " + streamlining );






// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGStreamlining

/*
FOR windSpeed=10;
species ="Maritime pine"
results of streamlining= 0.5459317788819492;
*/

	}
}