package fireparadox.model.plant;

import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;

/**
 * This class contains several static methods to compute tree's extenstion:
 * treeHeight, crown radius and crownBaseHeight
 * @author pimont
 *
 */
public class FmLocalPlantDimension {

	/**
	 * Allometric relashionship to compute the height of a tree
	 * @param species TODO FP
	 * @param dbh
	 *            (cm)
	 * 
	 * @return height (m)
	 * @throws Exception
	 */
	public static double computeTreeHeight(FiSpecies species, double dbh,
			double age)
			throws Exception {
		String speciesName = species.getName();
		if (species.hEq==null) 
		throw new Exception(
				"FiLocalPlantDimension.computeTreeHeight: no model available for species "
						+ speciesName);
		return species.hEq.f(dbh);
//		if (speciesName.equals(FmModel.PICEA_MARIANA)||speciesName.equals(FmModel.PICEA_MARIANA_DEAD)) {
//			return 0.9477 * dbh + 0.7108;
//		}
//		if (speciesName.equals(FmModel.PINUS_BANKSIANA)) {
//			return Math
//					.min(3.2678 * Math.pow(dbh, 0.5703), 2.24 * dbh - 1.1925);
//		}
//		if (speciesName.equals(FmModel.PINUS_BANKSIANA_DEAD)) {
//			return Math.min(3.2819 * Math.pow(dbh, 0.5355),
//					1.3927 * dbh + 0.4788);
//		}
//		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
//			// Lopez Serrano 2005
//			return 2.55 * Math.pow(dbh, 0.45);  
//		}	
//		if (speciesName.equals(FmModel.PINUS_PINASTER)) {
//			// TODO To improve
//			// fni data from cinto rotondo area in corsica
//			return 27.4 * Math.pow(0.01 * dbh, 0.568);
//		}
//		if (speciesName.equals(FmModel.PINUS_PINASTER_NAVAS)
//				|| speciesName.equals(FmModel.PINUS_PINASTER_TELENO)) {
//			// chema fernandez
//			return 1.3 + Math.exp(-5.914 + 5.717 * Math.pow(dbh, 0.117));
//		}
//		if (speciesName.equals(FmModel.PINUS_SYLVESTRIS)) {
//			// TODO BIDON!!!!!!!!!!
//			return 2.0 * Math.pow(dbh, 0.5);
//		}
//		if (speciesName.equals(FmModel.PINUS_PONDEROSA_USFS1)) {
//			// temporary
//			return 2.55 * Math.pow(dbh, 0.45);
//		}	
//		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
//			 // Quercus ilex L ecosystems book (Romane & Terradas ed)
//			// + Canadell 1988
//			return 1.3025 * Math.pow(dbh, 0.5519);
//		}
//
//		if (speciesName.equals(FmModel.QUERCUS_PUBESCENS)) {
//			// Tognetti 2003
//			return 10.5 * (1.0 - Math.exp(-0.125 * dbh)) - 1.3;
//		}
//		
	}

	/**
	 * Allometric relashionship to compute the height of a tree given the
	 * dominant height and diameter for Aleppo pine only
	 * 
	 * @param speciesName
	 * @param dbh
	 *            (cm)
	 *@param dDom
	 *            dominant diameter (cm)
	 *@param hDom
	 *            dominant height (cm)
	 * @return height (m)
	 * @throws Exception
	 */
	public static double computeAleppoPineHeight(double dbh,
			double hDom, double dDom) throws Exception {
		
			double temp = 1.01 * (1d / dbh - 1d / dDom)
					+ Math.pow(hDom - 1.3, -1d / 3d);
			return 1.3 + Math.pow(temp, -3d);
	}

	/**
	 * Allometric relashionship to compute the crown diameter of a tree
	 * 
	 * @param speciesName
	 * @param dbh
	 *            (cm)
	 * @param height
	 *            (m)
	 * @return diameter (m)
	 * @throws Exception
	 */
	public static double computeCrownDiameter(FiSpecies species, double dbh,
			double height) throws Exception {
		//HARDCODED MODEL FIRST
		String speciesName = species.getName();
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			// Option1 :Lopez Serrano 2005
			// return 2*Math.sqrt(0.067 / 3.14 * Math.pow(dbh, 1.661));

			// Option2 : contact : Ph.Dreyfus INRA URFM
			//double h_d_ = (height - 1.3) / (0.01 * dbh);
			//double emax_ht = 0.4823 - 0.3888
			//		* Math.exp(-10685.5 * Math.pow(h_d_, -2.5)) - 0.000925
			//		* (dbh * Math.PI);
			//return 2* emax_ht * height;

			// Option3 : contact : Ph.Dreyfus INRA URFM
			// 			D:\aphd__S1\ESP\Alep\Houppier\emax_ALEP_log.sas      avec 3eme branche     2010-07-18
			double a = -0.6682;
			double b =  1.7336;
			double c =  15820.9;
			double p = -0.3504;
			double h_d_ = (height - 1.3) / (0.01 * dbh);
			double c130 = Math.PI*dbh;
	        double logemax_ht = a-b*Math.exp(-c*Math.pow(h_d_, -2.5)) + p * c130/100;
	        return 2*height * Math.exp(logemax_ht) * 0.97; // FP result from
															// branch averaging
															// bias
	    }
		// LOOK IN SPECIES FILE
		if (species.cdEq==null) 
		throw new Exception(
				"FiLocalPlantDimension.computeCrownDiameter: no model available for species "
						+ speciesName);
		return species.cdEq.f(dbh,height);
//		if (speciesName.equals(FmModel.PICEA_MARIANA)||speciesName.equals(FmModel.PICEA_MARIANA_DEAD)) {
//			// Kerry Andersen relationships
//			if (height<=1.3) {
//				return  height * 0.5 / 0.8;
//			} else {
//				return  height * 1.2 / 4.4;
//			}
//		}
//		if (speciesName.equals(FmModel.PINUS_BANKSIANA)||speciesName.equals(FmModel.PINUS_BANKSIANA_DEAD)) {
//			// Kerry Andersen relationships
//			return  height * 1.6 / 12.1;
//		}
//		}
//		if (speciesName.equals(FmModel.PINUS_PINASTER)) {
//			// Porte 2000
//			return 2*0.106 * Math.pow(dbh, 0.861);
//		}
//		if (speciesName.equals(FmModel.PINUS_PINASTER_NAVAS)) {
//			// chema fernandez
//			return  0.01 * (11.495 * dbh + 35.89);
//		}
//		if (speciesName.equals(FmModel.PINUS_PINASTER_TELENO)) {
//			// chema fernandez
//			return  0.01 * (5.61 * dbh + 59.903);
//		}
//		if (speciesName.equals(FmModel.PINUS_PONDEROSA_USFS1)) {
//			// TEMPORARY
//			return  height * 1.6 / 12.1;
//
//		}
//        if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
//			// Canadell 1988
//			return 2*0.3358 * Math.pow(dbh, 0.7028);
//		} 
//	
//
//		if (speciesName.equals(FmModel.QUERCUS_PUBESCENS)) {
//			// Tognetti 2003
//			return 0.88 + 2*0.0067 * Math.pow(dbh, 1.82);
//		}
//		
//		
//		throw new Exception(
//				"FiLocalPlantDimension.computeCrownRadius: no model available for species "
//						+ speciesName);
	}

	/**
	 * Allometric relashionship to compute the crown base height of a tree
	 * 
	 * @param speciesName
	 * @param dbh
	 *            (cm)
	 * @param h
	 *            (m)
	 * @param plotMeanHeight
	 *            : dominant height in m
	 * @param bA
	 *            basal area (m2/ha)
	 * @return cbh (m)
	 * @throws Exception 
	 */
	public static double computeCrownBaseHeight(FiSpecies species, double dbh,
			double h, int age, double plotMeanHeight, double bA) throws Exception {
		// HARDCODED MODEL FIRST
		String speciesName = species.getName();
		if (speciesName.equals(FmModel.PINUS_SYLVESTRIS)) {
			// Tahvanainen & Forss 2008 : for pinus sylvestris
			double temp = 1.937 + 0.105 * h - 0.0016 * h * h - 0.341 * h / dbh
					+ 0.0475 * (h - plotMeanHeight) - 0.009 * bA;
			return h - temp * temp;
	    }
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			// contact : Ph.Dreyfus INRA URFM
			double d_h = (0.01*dbh) / h;   // h = total height;
			return h * Math.exp(-(2.1523 + 1896.2 / (double) age) * d_h);
		}

		
		if (species.cbhEq==null) 
		throw new Exception(
				"FiLocalPlantDimension.computeBaseHeight: no model available for species "
						+ speciesName);
		return species.cbhEq.f(dbh,h);

		
		
//		if (speciesName.equals(FmModel.PICEA_MARIANA)||speciesName.equals(FmModel.PICEA_MARIANA_DEAD)) {
//			// Francois PIMONT, from ICFME paper
//			// return h*0.25;
//			return h * 0.05;
//		}
//		if (speciesName.equals(FmModel.PINUS_BANKSIANA)||speciesName.equals(FmModel.PINUS_BANKSIANA_DEAD)) {
//			// Francois PIMONT, from ICFME paper
//			// return h*0.66;
//			return h * 0.20;
//		}
//		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
//			// contact : Ph.Dreyfus INRA URFM
//			double d_h = (0.01*dbh) / h;   // h = total height;
//			return h * Math.exp(-(2.1523 + 1896.2 / (double) age) * d_h);
//		}
//		if (speciesName.equals(FmModel.PINUS_PINASTER_NAVAS)
//				|| speciesName.equals(FmModel.PINUS_PINASTER_TELENO)) {
//			// contact : chema fernandez
//			return h / (1.0 + Math.exp(-0.216 + 0.016 * h));
//		}
//		if (speciesName.equals(FmModel.PINUS_PONDEROSA_USFS1)) {
//			// Temporary
//			return h * 0.20;
//		}
//		if (speciesName.equals(FmModel.QUERCUS_COCCIFERA)) {
//			
//			return 0.05d;
//		}
		

	}
	

}
