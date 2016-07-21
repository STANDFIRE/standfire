package capsis.lib.forestgales;
import capsis.kernel.PathManager;

/**
 * ForestGales RoughnessSimple
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu February 2014
 */


public class FGRoughnessSimple  {




	public static double [] roughnessSimple (FGSpecies species, double windSpeed, double crownWidth, double crownDepth, double height, double currentSpacing,
				FGConfiguration configuration) {


			double [] roughnessParameters = {0d,0d,0d};
			double CD1 = 7.5;
			double CW = configuration.getRoughnessConstant ();
			double CS = configuration.getSurfaceDragCoefficient ();
			double CR = configuration.getElementDragCoefficient ();
			double K = configuration.getVonKarmanConstant ();

			//double currentSpacing = 100. / Math.sqrt (nha);


			double roughnessSimple_d;
			double roughnessSimple_gammaSolved;
			double roughnessSimple_z0;


			// Find the FGSpecies matching the given name (aborts if wrong species name)
			//FGSpecies species = configuration.getSpecies (meanTree.getSpeciesName ());
			double canopyStreamliningN = species.getCanopyStreamliningN ();
			double canopyStreamliningC = species.getCanopyStreamliningC ();

			double porosity = FGStreamlining.calcStreamlining(windSpeed, canopyStreamliningN, canopyStreamliningC);


			double canopyBreadth = crownWidth * porosity / 2.0;
			double lambda = (canopyBreadth * crownDepth) / (currentSpacing * currentSpacing);
			double lambdaCapital = 2 * lambda;
			double psih = Math.log (CW) - 1 + 1 / CW;

			roughnessSimple_d = (1 - ((1 - Math.exp (-Math.sqrt (CD1 * lambdaCapital))) / Math.sqrt (CD1 * lambdaCapital)))
					* height;
			roughnessParameters [0]= roughnessSimple_d ;
			System.out.println 	("results of roughnessSimple_d : " + roughnessSimple_d );

			if (lambdaCapital > 0.6) {
				roughnessSimple_gammaSolved = 1 / Math.sqrt (CS + CR * 0.3);
			} else {
				roughnessSimple_gammaSolved = 1.0 / Math.sqrt (CS + CR * lambdaCapital / 2.0);
			}
			roughnessParameters [1]= roughnessSimple_gammaSolved;
			System.out.println 	("results of roughnessSimple_gammaSolved : " + roughnessSimple_gammaSolved );


			roughnessSimple_z0 = (height - roughnessSimple_d) * Math.exp ((-K * roughnessSimple_gammaSolved) + psih);
			roughnessParameters [2]= roughnessSimple_z0;
			System.out.println 	("results of roughnessSimple_z0 : " + roughnessSimple_z0 );


			return roughnessParameters ;
		}



    public static double calculateForce( double windSpeed , double currentSpacing , double roughnessSimple_d, double roughnessSimple_gammaSolved, FGConfiguration configuration){

        double heightOfForce = roughnessSimple_d ;		/// NOT USED BARRY??????????????????????????????????????
        double airDensity = configuration.getAirDensity  ();

        System.out.println 	("calculateForce :" + (airDensity  *  (currentSpacing * windSpeed / roughnessSimple_gammaSolved) * (currentSpacing * windSpeed / roughnessSimple_gammaSolved)));

        return  airDensity  *  (currentSpacing * windSpeed / roughnessSimple_gammaSolved) * (currentSpacing * windSpeed / roughnessSimple_gammaSolved)  ;
    }





}