package capsis.lib.forestgales;
import capsis.kernel.PathManager;

/**
 * ForestGales GUST FACTOR
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu February 2014
 */


public class FGGustFactor  {


	public static double calculateGustFactorInForest (FGStand stand, double currentSpacing, double height) {

		double treeHeightsNumberFromEdge = stand.getTreeHeightsNumberFromEdge ();
		//double currentSpacing = 100. / Math.sqrt (nha);
		double ratioSpacingToHeight = currentSpacing / height;

		// The range of ratioSpacingToHeight must be between 0.075 and 0.55
		if (ratioSpacingToHeight < 0.075) {
			ratioSpacingToHeight = 0.075;
		} else if (ratioSpacingToHeight > 0.55) {
			ratioSpacingToHeight = 0.55;
		}

		double meanBMin = (0.68 * ratioSpacingToHeight - 0.0385) + (-0.68 * ratioSpacingToHeight + 0.4785)
				* Math.pow (1.7239 * ratioSpacingToHeight + 0.0316, treeHeightsNumberFromEdge);
		double maxBMin = (2.7193 * ratioSpacingToHeight - 0.061) + (-1.273 * ratioSpacingToHeight + 0.9701)
				* Math.pow (1.1127 * ratioSpacingToHeight + 0.0311, treeHeightsNumberFromEdge);

		System.out.println 	("results of GustFactorInForest  : " + maxBMin / meanBMin);
		return maxBMin / meanBMin;






	}

	 public static double calculateNEWGustFactor (FGStand stand, double currentSpacing, double height) {

		double treeHeightsNumberFromEdge = stand.getTreeHeightsNumberFromEdge ();
		//double currentSpacing = 100./Math.sqrt(nha);
		double ratioSpacingToHeight = (currentSpacing / height);

		// The range of ratioSpacingToHeight must be between 0.075 and 0.55
		if (ratioSpacingToHeight < 0.075) {
			ratioSpacingToHeight = 0.075;
		} else if (ratioSpacingToHeight > 0.55) {
			ratioSpacingToHeight = 0.55;
		}

		double A = -2.1 * ratioSpacingToHeight + 0.91;
		//Added next line to stop A going negative. Barry Gardiner 18 June 2014
		if( A < 0) A = 0;

		double B = 1.0611 * Math.log( ratioSpacingToHeight ) + 4.2;

		System.out.println 	("results of NEWGustFactor  : " +  (( A * treeHeightsNumberFromEdge ) + B) );
		return  ( A * treeHeightsNumberFromEdge ) + B ;

	}



	/*
	 * public double calculateGustFactor (double nha, double treeHeightsNumberFromEdge, double height) {
	 *
	 * double currentSpacing = 100./Math.sqrt(nha); double ratioSpacingToHeight = currentSpacing /
	 * height);
	 *
	 * // The range of ratioSpacingToHeight must be between 0.075 and 0.55 if (ratioSpacingToHeight
	 * < 0.075) { ratioSpacingToHeight = 0.075; } else if (ratioSpacingToHeight > 0.55) {
	 * ratioSpacingToHeight = 0.55; }
	 *
	 * double A = -2.1 * ratioSpacingToHeight + 0.91; if( A < 0) A = 0;
	 *
	 * double B = 1.0611 * Math.log( ratioSpacingToHeight ) + 4.2;
	 *
	 * double meanBMin = (0.68 * ratioSpacingToHeight - 0.0385) + ( -0.68 * ratioSpacingToHeight +
	 * 0.4785 ) * Math.pow( 1.7239 * ratioSpacingToHeight + 0.0316 , TREE_HEIGHTS_FROM_EDGE); double
	 * maxBMin = (2.7193 * ratioSpacingToHeight - 0.061) + ( -1.273 * ratioSpacingToHeight + 0.9701
	 * ) * Math.pow( 1.1127 * ratioSpacingToHeight + 0.0311 , TREE_HEIGHTS_FROM_EDGE);
	 *
	 * double meanBMEdge = (0.68 * ratioSpacingToHeight - 0.0385) + ( -0.68 * ratioSpacingToHeight +
	 * 0.4785 ) * Math.pow( 1.7239 * ratioSpacingToHeight + 0.0316 , 0 ); double maxBMEdge = (2.7193
	 * * ratioSpacingToHeight - 0.061) + ( -1.273 * ratioSpacingToHeight + 0.9701 ) * Math.pow(
	 * 1.1127 * ratioSpacingToHeight + 0.0311 , 0);
	 *
	 * double meanBMEdgeGap = (0.68 * ratioSpacingToHeight - 0.0385) + ( -0.68 *
	 * ratioSpacingToHeight + 0.4785 ) * Math.pow( 1.7239 * ratioSpacingToHeight + 0.0316 , 0 ) *
	 * getMeanGapFactor( gap, treeHeight ); double maxBMEdgeGap = (2.7193 * ratioSpacingToHeight -
	 * 0.061) + ( -1.273 * ratioSpacingToHeight + 0.9701 ) * Math.pow( 1.1127 * ratioSpacingToHeight
	 * + 0.0311 , 0 ) * getMaxGapFactor( gap, treeHeight );
	 *
	 * double oldGustIn = maxBMin / meanBMin; double oldGustEdge = maxBMEdge / meanBMEdge ; double
	 * oldGustEdgeGap = maxBMEdgeGap / meanBMEdgeGap;
	 *
	 * double newGustIn = ( A * TREE_HEIGHTS_FROM_EDGE ) + B ; double newGustEdge = ( A * 0 ) + B;
	 *
	 * double newGustEdgeGap = 0.0; if( oldGustIn - oldGustEdge > 0 ) newGustEdgeGap = ((
	 * oldGustEdgeGap - oldGustEdge) * (newGustIn - newGustEdge)) / (oldGustIn - oldGustEdge ) +
	 * newGustEdge; else newGustEdgeGap = newGustEdge;
	 *
	 * return (((distanceToEdge/ ( treeHeight * TREE_HEIGHTS_FROM_EDGE )) * ( newGustIn -
	 * newGustEdgeGap )) + newGustEdgeGap) * 1.5;*
 	 *}
	 */





	public static void main (String [] args) throws Exception {

			FGStand stand = new FGStand ();
			//configuration.loadSpeciesMap (PathManager.getDir ("Data") + "/forestGales/forestGalesSpecies.txt");

			//Test input variables
			double treeHeight=20;
			double nha=2000;


		System.out.println 	("results of NEWGustFactor  : " + calculateNEWGustFactor (stand, nha, treeHeight));

		System.out.println 	("results of GustFactorInForest  : " + calculateGustFactorInForest (stand, nha, treeHeight));



// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGGustFactor

/*
FOR treeHeight = 20

For nha=2000;

results of NEWGustFactor  : 7.9520315302364715
results of GustFactorInForest  : 6.47607684719374
*/

	}
}