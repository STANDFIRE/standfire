package capsis.lib.forestgales;
import capsis.kernel.PathManager;

/**
 * ForestGales AnnualExceedenceProbability
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu February 2014
 */


public class FGAnnualExceedenceProbability  {


	public static double getAnnualExceedenceProbability( double windSpeed, FGStand  stand ){

		double k_weibull = stand.getWindClimateWeibullK ();
		double a_weibull = stand.getWindClimateWeibullA ();

		double  Ua = 5;
		double U_C = -0.5903 * Math.pow(k_weibull, 3) + 4.4345 * Math.pow(k_weibull, 2) - 11.8633 * k_weibull + 13.569;
		double  U  = (a_weibull * U_C) * (a_weibull * U_C);
		double  Aaa = U / Ua;
		double  Aep = 1 - Math.exp( -1 * Math.exp( -1 * ((windSpeed * windSpeed) - U) / Aaa) );
		return Aep;
	}




	public static void main (String [] args) throws Exception {

			FGStand  stand = new FGStand ();
			//configuration.loadSpeciesMap (PathManager.getDir ("Data") + "/forestGales/forestGalesSpecies.txt");


			//Test input variables
			double windSpeed=10;

			System.out.println ("results of getAnnualExceedenceProbability: " + getAnnualExceedenceProbability
				(windSpeed, stand));


// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGAnnualExceedenceProbability

/*
FOR treeHeight = 20

for windSpeed=10;

results of getAnnualExceedenceProbability: 0.0
*/

	}
}