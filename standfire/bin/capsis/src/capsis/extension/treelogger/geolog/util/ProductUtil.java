/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.treelogger.geolog.util;

import java.util.Vector;

/**	ProductUtil : utility methods for GeoLogProduct
*	All the methods are static
*
*	@author F. Mothe - april 2006
*/
@Deprecated
class ProductUtil {

	// Max number of iterations for log length
	// (we need around 10 * Math.log10 (precisionLength_m) / 3 iterations) :





	// Gilles Le Moguédec logging algo for using the max stem length
	// Returns the cutting heights, starting from minHeight
	// The number of logs is size - 1 of the returned vector
	// (negative minLength / maxLength means no test)
	//
	// "La règle de découpe est la suivante : on cherche à faire autant de billons
	//  de grande longueur que possible dans la mesure ou c'est compatible avec
	//  l'exploitation de la totalité de la bille. Les billons les plus longs sont
	//  découpés dans le bas de la grume, là où le diamètre est le plus important."
	//
	// (modified management of nbMaxLogs + modif (*) below)
	@Deprecated
	public static Vector <Double> getCuttingHeights_GLM (
			double minLength, double maxLength,
			double minHeight, double maxHeight,
			int nbMaxLogs
	) {

		Vector <Double> heights = new Vector <Double> ();
		double totalLength = maxHeight - minHeight;
		if (nbMaxLogs<0) {
			nbMaxLogs = Integer.MAX_VALUE;
		}

		// First height = minHeight :
		heights.add (minHeight);

		if (totalLength <= 0 || nbMaxLogs <= 0) {
			// Should not occur
			System.out.println ("getCuttingHeights_GLM : totalLength <= 0");
		} else if (maxLength <= 0) {
			// maxLength unspecified
			if (totalLength>=minLength) {
				// minLength unspecified or enough available length
				heights.add (maxHeight);
			}
		} else if (minLength <=0) {
			// maxLength specified, minLength unspecified
			int nbLenMax = Math.min (nbMaxLogs, (int) (totalLength / maxLength));
			double topHeight =
					cutLog_GLM (heights, nbLenMax, minHeight, maxLength);
			if (topHeight<maxHeight && nbMaxLogs>=heights.size ()) {
				heights.add (maxHeight);
			}
		} else {
			// General case
			int nbLenMax = (int) (totalLength / maxLength);
			int nbLenMin = (int) (totalLength / minLength);

			// Modif (*) by F.Mothe :
			// Going further when (nbLenMax * maxLength == totalLength)
			// may lead to cut small logs in place of maxLength logs
			// e.g. : getCuttingHeights_GLM (1.0, 1.5, 0.0, 3.0, -1);	// -:> 3x1m
			if (nbLenMin <= nbLenMax || nbLenMax >= nbMaxLogs
					|| nbLenMax * maxLength >= totalLength
			) {
				nbLenMax = Math.min (nbMaxLogs, nbLenMax);
				cutLog_GLM (heights, nbLenMax, minHeight, maxLength);
			} else {
				// nbLenMin > nbLenMax && nbLenMax < nbMaxLogs
				int nbMaxN = Math.min ( nbLenMax, (int) (
						(nbLenMin - nbLenMax) * minLength /
						(maxLength - minLength)
				) );
				// nbMaxN < nbMaxLogs

				double lenN = totalLength - nbMaxN * maxLength;
				int nbLenMaxN = nbLenMax - nbMaxN ;
				int nbLenMinN = (int) (lenN / minLength);
				while (nbLenMaxN >= nbLenMinN ) {
					nbMaxN--;
					lenN = totalLength - nbMaxN * maxLength;
					nbLenMaxN = nbLenMax - nbMaxN ;
					nbLenMinN = (int) (lenN / minLength);
				}

				double topHeight =
						cutLog_GLM (heights, nbMaxN, minHeight, maxLength);

				// nbMaxN < nbMaxLog =:> one more log is always possible
				topHeight += (lenN - (nbLenMinN-1) * minLength);
				heights.add (topHeight);

				// Total nb of logs = nbMaxN + 1 + nbMinN <= nbMaxLogs
				// =:> nbMinN <= nbMaxLogs - nbMaxN - 1 (may be <0)
				int nbMinN = Math.min (nbMaxLogs - nbMaxN - 1, nbLenMinN - 1);
				cutLog_GLM (heights, nbMinN, topHeight, minLength);
			}
		}

		/*
		// TODO : move elsewhere :
		if (heights.size () > 0 && (maxHeight - heights.lastElement () < 0.001)) {
			heights.set (heights.size () - 1, maxHeight);
		}
		*/
		return heights;
	}

	// Cut 'number' logs of length 'length' starting from baseHeight
	// Returns the final top height
	// (number may be <=0)
	@Deprecated
	private static double cutLog_GLM (Vector <Double> heights, int number,
			double baseHeight, double length) {
		double topHeight = baseHeight;
		for (int i = 0; i<number; i++) {
			// topHeight = baseHeight + (i+1) * length;
			topHeight += length;
			heights.add (topHeight);
		}
		return topHeight;
	}

	// TODO : remove
	// Original algo for testing
	@Deprecated
	public static Vector <Double> getCuttingHeights_GLM_Ori (
			double minLength, double maxLength,
			double minHeight, double maxHeight,
			int nbMaxLogs
	) {

		Vector <Double> heights = new Vector <Double> ();
		double totalLength = maxHeight - minHeight;

		// First height = minHeight :
		heights.add (minHeight);

		if (totalLength <= 0) {
			// Should not occur
			System.out.println ("getCuttingHeights_GLM_Ori : totalLength <= 0");
		} else if (maxLength <= 0) {
			// maxLength unspecified
			if (totalLength>=minLength) {
				// minLength unspecified or enough available length
				heights.add (maxHeight);
			}
		} else if (minLength <=0) {
			// maxLength specified, minLength unspecified
			int nbLenMax = (int) (totalLength / maxLength);
			double topHeight =
					cutLog_GLM (heights, nbLenMax, minHeight, maxLength);
			if (topHeight<maxHeight) {
				heights.add (maxHeight);
			}
		} else {
			// General case
			int nbLenMax = (int) (totalLength / maxLength);
			int nbLenMin = (int) (totalLength / minLength);
//System.out.println ("nbLenMin = " + nbLenMin + " nbLenMax = " + nbLenMax);

			if (nbLenMin <= nbLenMax) {
				cutLog_GLM (heights, nbLenMax, minHeight, maxLength);
			} else {
				// nbLenMin > nbLenMax
				int nbMaxN = Math.min ( nbLenMax, (int) (
						(nbLenMin - nbLenMax) * minLength /
						(maxLength - minLength)
				) );
//System.out.println ("nbMaxN0 = " + nbMaxN);
//System.out.println ("nbMaxN0 = E " + (nbLenMin - nbLenMax) * minLength / (maxLength - minLength));

				double lenN = totalLength - nbMaxN * maxLength;
				int nbLenMaxN = nbLenMax - nbMaxN ;
				int nbLenMinN = (int) (lenN / minLength);
//System.out.println ("nbLenMinN = " + nbLenMinN + " nbLenMaxN = " + nbLenMaxN);
				while (nbLenMaxN >= nbLenMinN ) {
					nbMaxN--;
					lenN = totalLength - nbMaxN * maxLength;
					nbLenMaxN = nbLenMax - nbMaxN ;
					nbLenMinN = (int) (lenN / minLength);
//System.out.println ("nbLenMinN = " + nbLenMinN + " nbLenMaxN = " + nbLenMaxN);
				}
//System.out.println ("nbMaxN = " + nbMaxN);

				double topHeight =
						cutLog_GLM (heights, nbMaxN, minHeight, maxLength);

				topHeight += (lenN - (nbLenMinN-1) * minLength);
				heights.add (topHeight);

				cutLog_GLM (heights, nbLenMinN-1, topHeight, minLength);
			}
		}

		// Original management of nbMaxLogs :
		int NbBillons = heights.size () - 1;
		if (nbMaxLogs>0 && NbBillons > nbMaxLogs) {
			heights = new Vector <Double> ();
			// First height = minHeight :
			heights.add (minHeight);

			int nbLenMax = (int) (totalLength / maxLength);
			if (nbMaxLogs<=nbLenMax) {
				cutLog_GLM (heights, nbMaxLogs, minHeight, maxLength);
			} else {
				if (minLength>0) {
					nbLenMax= (int) ( (totalLength-nbMaxLogs*minLength)/(maxLength-minLength) ) ;
				}
				int nbLenMin=nbMaxLogs-nbLenMax-1;
				double topHeight = cutLog_GLM (heights, nbLenMax, minHeight, maxLength);
				if (minLength>0) {
					topHeight += (totalLength-nbLenMax*maxLength - nbLenMin*minLength ) ;
				} else {
					topHeight += (totalLength-nbLenMax*maxLength ) ;
				}
				heights.add (topHeight);

				if (nbLenMin>0 && minLength>0) {
					cutLog_GLM (heights, nbLenMin, topHeight, minLength);
				}
			}
		}

		return heights;
	}


	// Returns the stem volume between botHeight_m and topHeight_m
	// If topHeight_m <0, topHeight_m is set to the height where
	// diameter = topDiameter_cm
//	public static double getVolume_m3 (GeoLogTreeData td,
//			double botHeight_m, double topHeight_m, double topDiameter_cm,
//			boolean topDiamOverBark, boolean volumeOverBark,
//			double precisionLength_m, double precisionThickness_m) {
//		if (topHeight_m < 0.0) {
//			topHeight_m = td.getMaxHeight_m(topDiameter_cm, topDiamOverBark, precisionLength_m);
//		}
//		return (topHeight_m <= botHeight_m)
//			? 0.0
//			: td.getVolume_m3 (botHeight_m, topHeight_m, volumeOverBark, precisionThickness_m)
//		;
//	}

//	// TODO remove ref to GeoLogTreeData and put into TreeUtil
//	// Returns the stem volume between botHeight_m and topHeight_m
//	// If topHeight_m <0, topHeight_m is set to the height where
//	// diameter = topDiameter_cm
//	public static double getVolume_m3 (Tree t, TreeRadius_cmProvider radMp,
//			double botHeight_m, double topHeight_m, double topDiameter_cm,
//			boolean topDiamOverBark, boolean volumeOverBark,
//			double precisionLength_m, double precisionThickness_m)
//	{
//		// TODO : remove this stuff !!
//		// It works because we only need TreeData.getTreeRadius_cm ()
//		GeoLogTreeData fakeTd = GeoLogTreeData.makeFakeTreeData (t, radMp);
//		return getVolume_m3 (fakeTd, botHeight_m, topHeight_m,
//				topDiameter_cm, topDiamOverBark, volumeOverBark,
//				precisionLength_m, precisionThickness_m);
//	}


}
