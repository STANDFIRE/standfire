package fireparadox.model.plant;

import java.util.Random;

import fireparadox.model.FmModel;
/**
 * THIS CLASS ONLY CONTAINS A METHOD TO GENERATE DBH FROM HEIGHT PREVIOUSLY USED IN PANEL AND BARK THICKNESS MODELS
 * 
 * @author pimont
 *
 */
public class FmDendromTreeProperties {
	
	
//	/**
//	 * this method compute the volume of crown,
//	 * assuming a cylindric shape of diameter "crown diameter" and or height
//	 * ="height-cbh" as a function of
//	 * height, cbh and diameter
//	 * @param height (m)
//	 * @param crownBaseHeight (m)
//	 * @param crownDiameter (m)
//	 * @param crownPerpendicularDiameter (m)
//	 * @author pimont
//	 */
//	public static double computeCylindricCrownVolume(double height,
//			double crownBaseHeight, double crownDiameter,
//			double crownPerpendicularDiameter) { // FP 23/09/2009
//		double surface = FiPlant.computeCrownProjectedArea(crownDiameter,
//				crownPerpendicularDiameter);
//		double crownheight = height - crownBaseHeight;
//		return surface * crownheight;
//	}
	/**
	 * this method compute the elliptic projected area of the crown
	 * @param crownDiameter (m)
	 * @param crownPerpendicularDiameter(m)
	 * @author pimont
	 */
//	public static double computeCrownProjectedArea(double crownDiameter,
//			double crownPerpendicularDiameter) { // FP 23/09/2009
//		return 3.14 * crownDiameter * crownPerpendicularDiameter / 4.0;
//	}

	/**
	 * relashionship for barkThickness (cm), used for cambium and tree mortality
	 * models
	 * 
	 * @param speciesName
	 * @param dbh (cm)
	 */
	
	public static double computeBarkThickness(String speciesName, double dbh) {
		if (dbh < 0.0) {
			return -1;
		}
		double dbhm = dbh * 0.01; // dbh (m)
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)
				|| speciesName.equals(FmModel.PINUS_BRUTIA)) {
			return 100. * (0.000424 + 0.076 * dbhm);
			// Ryan et al 1994 , r=0.99, 3454 trees, dbh=8-84 cm
		}

		if (speciesName.equals(FmModel.PINUS_PINEA)) {
			return 100. * (0.00559 + 0.067 * dbhm);
			// Ryan et al 1994 , r=0.75, 241 trees, dbh=8-60 cm
		}

		if (speciesName.equals(FmModel.PINUS_PINASTER)) {
			return 100. * (0.0697 * Math.pow(dbhm, 0.657));
			// IFN 06 dbh=8-80 cm
		}
		if (speciesName.equals(FmModel.PINUS_NIGRA)) {
			return 100. * (0.0621 * Math.pow(dbhm, 0.838));
			// IFN 04 dbh=8-60 cm
		}
		if (speciesName.equals(FmModel.PINUS_NIGRA_LARICIO)) {
			return 100. * (0.0117 + 0.0440 * dbhm);
			// Pimont et al 2006 , r=0.99, 1221 trees
		}
		if (speciesName.equals(FmModel.PINUS_SYLVESTRIS)) {
			double val = 100. * (-0.00589 + 0.0595 * Math.pow(dbhm, 0.620));
			if (val < 0)
				return 0.;
			return val;
			// IFN 05 dbh=8-80 cm
		}
		if (speciesName.equals(FmModel.QUERCUS_PUBESCENS)) {
			return 100. * (0.0381 * Math.pow(dbhm, 0.623));
			// IFN 04 dbh=8-70 cm
		}
		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
			return 100. * (0.0232 * Math.pow(dbhm, 0.614));
			// IFN 06 dbh=8-35 cm
		}
		return (-1);
	}

	/**
	 * contact : Ph.Dreyfus INRA URFM Generate a tree diameter (cm) as a
	 * function of stand characteristics
	 * 
	 * @param speciesName
	 * @param Hdom
	 *            : dominant height in m
	 * @param Agedom
	 *            : dominant age (year)
	 * @param Nha
	 *            : number of stems/ha) Until now (2009-07-09), only for Pinus
	 *            halepensis
	 * @param stochastic
	 *            : boolean true= variability, false=meandbh
	 * @throws Exception
	 **/
	// NB THIS METHOD IS USED TO ADD TREE WITH THE PANEL
	public static double computeTreeDbh(String speciesName, double Hdom,
			int Agedom, double Nha, boolean stochastic) throws Exception {
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			double etDiam_cm = -6.077484847 + Hdom * 1.164754741 + Hdom * Hdom
					* -0.027422757 + Agedom * 0.025768608 + Nha * 0.004361325
					+ Nha * Nha * -0.000002706;

			double Dg_cm = 0.8329 * Hdom + 0.0904 * Agedom + -33.9580
					* (-0.0743 - Math.exp(-0.00512 * Nha));

			Random R = new Random();
			double treeDiam_cm = Dg_cm;
			if (stochastic) {
				treeDiam_cm += R.nextGaussian() * etDiam_cm;
			}

			return treeDiam_cm;
		}
		if (speciesName.equals(FmModel.PICEA_MARIANA)
				|| speciesName.equals(FmModel.PICEA_MARIANA_DEAD)) {
			return (Hdom - 0.7108) / 0.9477;
		}
		if (speciesName.equals(FmModel.PINUS_BANKSIANA)) {
			return Math.min(Math.pow(Hdom / 3.2678, 1d / 0.5703),
					(Hdom + 1.1925) / 2.24);
		}
		if (speciesName.equals(FmModel.PINUS_BANKSIANA_DEAD)) {
			return Math.min(Math.pow(Hdom / 3.2819, 1d / 0.5355),
					(Hdom - 0.4788) / 1.3927);
		}

		if (speciesName.equals(FmModel.PINUS_PINASTER)) {
			// TODO To improve
			// fni data from cinto rotondo area in corsica
			return 100d * Math.pow(Hdom / 27.4, 1 / 0.568);
		}
		if (speciesName.equals(FmModel.PINUS_SYLVESTRIS)) {
			// TODO BIDON!!!!!!!!!!
			return Math.pow(Hdom / 2.0, 1 / 0.5);
		}
		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
			// Quercus ilex L ecosystems book (Romane & Terradas ed)
			// + Canadell 1988
			return Math.pow(Hdom / 1.3025, 1 / 0.5519);
		}

		if (speciesName.equals(FmModel.QUERCUS_PUBESCENS)) {
			// Tognetti 2003
			// return 10.5 * (1.0 - Math.exp(-0.125 * dbh)) - 1.3;
			return Math.log(1d - (Hdom + 1.3) / 10.5) / (-0.125);
		}
		throw new Exception(
				"FiDendromTreeProperties.computeTreeDbh: no model available for species "
						+ speciesName);
	}
	

}
