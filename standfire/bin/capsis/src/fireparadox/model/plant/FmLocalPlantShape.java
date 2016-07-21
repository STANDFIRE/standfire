package fireparadox.model.plant;


/**
 * EMPTY
 * 
 *
 */
public class FmLocalPlantShape {
//	/**
//	 * FP 11-09-2009: Annabel porte relashionship for crownlenght and rad (in
//	 * m), as a function of species and dbh
//	 *
//	 * @throws Exception
//	 */
//	//TODO this method should be moved in FiLocalPlantDimension or removed
//	// public static double[] crownDimensions(String speciesName, double dbh)
//	// throws Exception {
//	// double[] result = new double[2];
//	// double a, b;
//	// result[0]=-1;
//	// result[1]=-1;
//	// if (speciesName.equals(FiModel.PINUS_PINASTER)) {
//	// a = 0.853;
//	// b = 0.629;
//	// double crownLenght = a * Math.pow(dbh, b);
//	// a = 0.106;
//	// b = 0.861;
//	// double crownRad = a * Math.pow(dbh, b);
//	// result[0] = crownLenght;
//	// result[1] = crownRad;
//	// return result;
//	// }
//	// return result;
//	// }
//
//	/**
//	 * Compute the relative radius of the crown, given the relative Height in
//	 * crown
//	 * 
//	 * @param relHeight
//	 * @return
//	 */
//	static double relativeRadiusPorte(double relHeight) {
//		if (relHeight >= 1.0 || relHeight <= 0.0)
//			return 0.0;
//		return 8.3 * relHeight - 23.4 * relHeight * relHeight + 27.0
//		* Math.pow(relHeight, 3.0) - 11.9 * Math.pow(relHeight, 4.0);
//	}
//
//	/**
//	 * Compute the relative radius of the crown, given the relative Height in
//	 * crown from the crownProfile of the species
//	 * 
//	 * @param relHeight
//	 * @return
//	 * @throws Exception
//	 */
//	static double relativeRadiusFromCrownProfileIncludingDead(double relHeight,
//			String speciesName) throws Exception {
//		if (relHeight >= 1.0)
//			return 0.0;
//		double[][] crownProfile = crownProfile(speciesName);
//		// computation of relMaxHeight
//		double maxR = 0d;
//		double maxRHeight = 0d;
//		int nDiamMaxR = 1;
//		
//		for (int ndiam = 1; ndiam < crownProfile.length; ndiam++) {
//			if (crownProfile[ndiam][1] >= maxR) {
//				maxR = crownProfile[ndiam][1];
//				nDiamMaxR = ndiam;
//				maxRHeight = crownProfile[ndiam][0];
//			}
//		}
//		
//		// computation of relBelowMaxHeight;=> reference below maxHeight
//		double rBelowMaxHeight = crownProfile[nDiamMaxR - 1][1];
//		double hBelowMaxHeight = crownProfile[nDiamMaxR - 1][0];
//
//		// below hBelowMaxHeight:
//		if (relHeight <= hBelowMaxHeight)
//			return 0.01 * rBelowMaxHeight;
//		// above
//		double result = 0d;
//		for (int ndiam = nDiamMaxR; ndiam < crownProfile.length - 1; ndiam++) {
//			if (crownProfile[ndiam][0] <= relHeight) {
//				result = 0.01
//						* (crownProfile[ndiam + 1][1]
//								* (relHeight - crownProfile[ndiam][0]) + crownProfile[ndiam][1]
//								* (crownProfile[ndiam + 1][0] - relHeight))
//						/ (crownProfile[ndiam + 1][0] - crownProfile[ndiam][0]);
//			}
//		}
//		return result;
//	}
//	
//	
//	public static double[][] crownProfile(String speciesName) throws Exception {
//		double[][] crownProfile;
//
//		// Create a crown profile for the tree (spheric crown)
//		// crownProfile = CrownProfileUtil.createRelativeCrownProfile (
//		// new double[] {0, 0, 50, 100, 100, 0}); // '50' could be
//		// recalculated with maxDiameterHeight
//		// FP put a crown profile from porte 2000
//		// detailed
//		// crownProfile = CrownProfileUtil
//		// .createRelativeCrownProfile(new double[] { 0, 0, 10, 62,
//		// 20, 92, 30, 100, 40, 99, 50, 93, 60, 85, 70, 75,
//		// 80, 61, 90, 39, 100, 0 });
//		// coarse
//		if (speciesName.equals(FmModel.PICEA_MARIANA)
//				|| speciesName.equals(FmModel.PICEA_MARIANA_DEAD)
//				|| speciesName.equals(FmModel.PINUS_BANKSIANA)
//				|| speciesName.equals(FmModel.PICEA_MARIANA_DEAD)) {
//			crownProfile = CrownProfileUtil
//					.createRelativeCrownProfile(new double[] { 0, 80, 25, 99,
//							50, 66, 75, 33, 100, 0 });
//		} else if (speciesName.equals(FmModel.PINUS_PONDEROSA_LANL)
//				|| speciesName.equals(FmModel.JUNIPER_TREE)
//				|| speciesName.equals(FmModel.PINON_PINE)
//				|| speciesName.equals(FmModel.PINON_PINE_DEAD)
//				|| speciesName.equals(FmModel.PINUS_PONDEROSA_USFS1)) {
//			crownProfile = CrownProfileUtil
//					.createRelativeCrownProfile(new double[] { 0, 0, 5, 50, 10,
//							71, 20, 100, 40, 87, 60, 71, 80, 50, 100, 0 });
//		} else if (speciesName.equals(FmModel.QUERCUS_COCCIFERA)) {
//			crownProfile = CrownProfileUtil
//					.createRelativeCrownProfile(new double[] { 0, 80, 50, 100,
//							 100, 90 });
//
//		} else if (speciesName.equals(FmModel.PINUS_PINASTER_NAVAS)
//				|| speciesName.equals(FmModel.PINUS_PINASTER_TELENO)) {
//			crownProfile = CrownProfileUtil
//					.createRelativeCrownProfile(new double[] { 0, 8, 10, 50,
//							25, 75, 45, 100, 75, 67, 100, 0 });
//		} else {
//			crownProfile = CrownProfileUtil
//					.createRelativeCrownProfile(new double[] { 0, 5, 10, 60,
//							25, 99, 50, 93, 75, 69, 100, 0 });
//		}
//		return crownProfile;
//
//	}
}
