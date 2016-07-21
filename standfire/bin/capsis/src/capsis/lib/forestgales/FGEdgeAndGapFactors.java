package capsis.lib.forestgales;
import capsis.kernel.PathManager;

/**
 * ForestGales EDGE and GAP FACTORS
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu February 2014
 */


public class FGEdgeAndGapFactors  {


	public static double calculateEdgeFactor ( double spacing, double treeHeight, double distanceToEdge, double gapSize, FGStand stand){

	/*{ This function calculates how the mean loading on a tree changes as a function of
	  tree height, spacing, distance from edge and size of any upwind gap.  An upwind
	  gap greater than 10 tree heights is assumed to be an infinite gap.
	  The function calculates the mean and maximum Bending Moment.  The gustiness
	  is derived from the ratio of the maximum to the mean loading. The output is used
	  to modify the calculated wind loading on the tree which assumes a steady wind and
	  a position well inside the forest.}*/
		double TREE_HEIGHTS_FROM_EDGE = stand.getTreeHeightsNumberFromEdge ();

		double Mean_BM;
		double Mean_BM_MidForest;
		double S_H;   // Ratio Spacing and Tree Height


		if( distanceToEdge >  TREE_HEIGHTS_FROM_EDGE * treeHeight ){
			distanceToEdge = TREE_HEIGHTS_FROM_EDGE * treeHeight;
		}
		// calculate Ratio Spacing and Tree Height
		S_H = spacing / treeHeight;
		// S_H must be within 0.075 and 0.55
		if (S_H < 0.075) S_H=0.075;
		if (S_H > 0.55) S_H = 0.55;
		// calculate Mean Bending Moment
		Mean_BM = (0.68 * S_H - 0.0385) + (-0.68 * S_H + 0.4785) * Math.pow ((1.7239 * S_H + 0.0316), (distanceToEdge / treeHeight))* getMeanGapFactor (gapSize, treeHeight, stand);
		// calculate Mean Bending Moment at the centre of the forest
		Mean_BM_MidForest = (0.68 * S_H - 0.0385) + (-0.68 * S_H + 0.4785) * Math.pow ((1.7239 * S_H + 0.0316), TREE_HEIGHTS_FROM_EDGE) * getMeanGapFactor (gapSize, treeHeight, stand);
		//Accounts for increase in wind loading at forest edge and gap size
		return Mean_BM / Mean_BM_MidForest;
	}




	public static double getGapFactor(  double gapSize, double treeHeight , double param0, double param1, double param2, FGStand stand ){

		double BIGGAP = stand.getSizeOfUpwindGap();

		if( gapSize > BIGGAP * treeHeight ) gapSize = BIGGAP * treeHeight;
		double X_H = gapSize / treeHeight ;

		double gapTen = param0 + param1 * Math.pow( BIGGAP , param2 );
		return (param0 +  param1 * Math.pow( X_H , param2 ))/gapTen;
	}


	public static double getMeanGapFactor (double gapSize, double treeHeight, FGStand stand){
		return getGapFactor(gapSize, treeHeight, 0d, 0.001, 0.562, stand );
	}

	public static double getMaxGapFactor(  double gapSize, double treeHeight, FGStand stand ){
		return getGapFactor( gapSize, treeHeight, 0d, 0.0064, 0.3467, stand );
	}

/*
	public static void checkEdgeAndGap (double spacing, double gapSize, double gapWidth, double height, double distanceToEdge, FGStand stand){

		double TREE_HEIGHTS_FROM_EDGE = stand.getTreeHeightsNumberFromEdge ();
		double BIGGAP = stand.getSizeOfUpwindGap();

		if (edge > TREE_HEIGHTS_FROM_EDGE  * height){
			calculateEdgeFactor (spacing, height, distanceToEdge, gapSize, stand);
		}
		if (gapWidth > BIGGAP * height){
			gapWidth = BIGGAP * height;
		}
	}
*/




	public static void main (String [] args) throws Exception {

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


			System.out.println ("results of getMeanGapFactor  : " + getMeanGapFactor
				(gapSize, treeHeight, stand));


			System.out.println ("results of getMaxGapFactor  : " + getMaxGapFactor
				(gapSize, treeHeight, stand));


			double param0=0d;
			double param1=0.0064;
			double param2=0.3467;

			System.out.println ("results of getGapFactor with max parameters : " + getGapFactor
				(gapSize, treeHeight, param0, param1, param2, stand));

// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGEdgeAndGapFactors




// to launch the command in C:/capsis windows
		//java -cp class;ext\* capsis.lib.forestgales.FGEdgeFactor

/*
FOR treeHeight = 20
FOR gapSize  = 20
For nha=2000;
For distanceToEdge = 10d;
results of calculateEdgeFactor : 2.39267495488067
*/


/*
FOR treeHeight = 20
FOR gapSize  = 0
results of getMeanGapFactor  : 0.0
results of getMaxGapFactor  : 0.0
results of getGapFactor with max parameters : 0.0

FOR treeHeight = 20
FOR gapSize  = 10
results of getMeanGapFactor  : 0.18570395041697332
results of getMaxGapFactor  : 0.3539426519046518
results of getGapFactor with max parameters : 0.3539426519046518

FOR treeHeight = 20
FOR gapSize  = 20
results of getMeanGapFactor  : 0.27415741719278824
results of getMaxGapFactor  : 0.45009065914561663
results of getGapFactor with max parameters : 0.45009065914561663
*/




	}
}