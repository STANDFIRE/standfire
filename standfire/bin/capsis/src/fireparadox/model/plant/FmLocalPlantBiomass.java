package fireparadox.model.plant;

import java.util.Set;

import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.layerSet.FmLayer;

/**
 * This class contain several static method to compute for localPlant:
 * - the biomass of a tree (based on branching or dbh) : branchProperties, crownProperties
 * - the bulk density in the crown as a function of distance to trunc and height : bulkDensity 
 * - the biomassInVoxel...
 * - These last two methods are based on horizontal and vertical distribution defined in this class too
 *  
 * @author pimont
 *
 */
public class FmLocalPlantBiomass {

	/**
	 * FP 11-06-2009: relashionship for leaf area in m2 [0] (total, not single
	 * sided) leaves biomass [1] and twigs [2] for "crowns" for different
	 * species, as a function of dbh (cm) and age (years) NB age is required
	 * just for Pinus pinaster
	 * 
	 * @throws Exception
	 * 
	 * @throws Exception
	 */
	public static void setBiomassWithoutPruning(FmPlant plant,Set<String> particleNames) throws Exception /* throws Exception */{
		double a, b, c, sla;
		String speciesName = plant.getSpeciesName();
		double dbh = plant.getDbh();
		double h = plant.getHeight();
		// HARD CODED first
		if (speciesName.equals(FmModel.PINUS_PINEA)) {
			// Correia et al 2010 Forest Systems 19(3) 418-433
			double k = 22.27;
			a =1.76;
			b = -0.50;
			double lbm= k * Math.pow(Math.PI * dbh / 100, a) * Math.pow (plant.getHeight ()/dbh,b); 
			plant.biomass.put(FiParticle.makeKey (FiParticle.LEAVE, FiParticle.LIVE), lbm);

		} else if (speciesName.equals(FmModel.PINUS_PINASTER)) {
			// PORTE ???
			int age = plant.getAge();
			if (age < 0) {
				throw new Exception(
						"FiLocalPlantBiomass.setBiomassWithoutPruning: age is required for "
								+ speciesName);
			}
			sla = plant.getSLA();
			// year1
			a = 0.546;
			b = 2.508;
			c = 1.18;
			double leafArea = a * Math.pow(dbh, b) / Math.pow(age, c);
			// year2
			a = 0.234;
			b = 2.708;
			c = 1.16;
			leafArea += a * Math.pow(dbh, b) / Math.pow(age, c);
			// year3
			a = 7.854;
			b = 2.308;
			c = 2.308;
			leafArea += a * Math.pow(dbh, b) / Math.pow(age, c);
			plant.biomass.put(FiParticle.makeKey (FiParticle.LEAVE, FiParticle.LIVE), leafArea / sla);
		} else if (speciesName.equals(FmModel.PINUS_PONDEROSA_LANL)) {
			double crownLength = plant.getCrownLengthBeforePruning();
			double R = plant.getCrownRadius();
			double rhomean = 0.4;
			double rhomax = rhomean * 3.0 / 2.0; // kg/m3
			plant.biomass.put(FiParticle.makeKey (FiParticle.LEAVE, FiParticle.LIVE), rhomax * Math.PI * crownLength * R * R / 3.0);
		} else if (speciesName.equals(FmModel.JUNIPER_TREE)) {
			//sla = slaArray[0];
			double crownLength = plant.getCrownLengthBeforePruning();
			double R = plant.getCrownRadius();
			double rhomean = 0.7;
			double rhomax = rhomean * 3.0 / 2.0; // kg/m3
			plant.biomass.put(FiParticle.makeKey (FiParticle.LEAVE, FiParticle.LIVE), rhomax * Math.PI * crownLength * R * R / 3.0);
		} else if (speciesName.equals(FmModel.PINON_PINE)) {
			double crownLength = plant.getCrownLengthBeforePruning();
			double R = plant.getCrownRadius();
			double rhomean = 0.7;
			double rhomax = rhomean * 3.0 / 2.0; // kg/m3
			plant.biomass.put(FiParticle.makeKey (FiParticle.LEAVE, FiParticle.LIVE), rhomax * Math.PI * crownLength * R * R / 3.0);
		} else if (speciesName.equals(FmModel.PINON_PINE_DEAD)) {
			double crownLength = plant.getCrownLengthBeforePruning();
			double R = plant.getCrownRadius();
			double rhomean = 0.7*0.4;
			double rhomax = rhomean * 3.0 / 2.0; // kg/m3
			plant.biomass.put(FiParticle.makeKey (FiParticle.TWIG1, FiParticle.DEAD), rhomax * Math.PI * crownLength * R * R / 3.0);
		} else if (speciesName.equals(FmModel.QUERCUS_COCCIFERA)) {
			// PIMONT 2011
			double fertility = 1.5d;
			int lastClearingType = 0;
			double treatmentEffect = 0d;
			double herbCover = 0d;
			double shrubCover = 0.80d;
			double treeCoverPerc = 75d;
			String familyType = FmLayer.SHRUB;
			// TODO : here height should be taken into account...
			double[] load = FmLayer.thinBiomassLoad(plant
					.getAge(), fertility, lastClearingType, treatmentEffect,
					herbCover, shrubCover, treeCoverPerc, familyType);

			double liveLeaveBiomass = load[0] * plant.getCrownRadius()
					* plant.getCrownRadius() * Math.PI
					/ (shrubCover);
			double liveTwigBiomass = (load[1] + load[2])
					* plant.getCrownRadius() * plant.getCrownRadius()
					* Math.PI / (shrubCover);
			plant.biomass.put(FiParticle.makeKey (FiParticle.LEAVE, FiParticle.LIVE), liveLeaveBiomass);
			plant.biomass.put(FiParticle.makeKey (FiParticle.TWIG1, FiParticle.LIVE), liveTwigBiomass);
		} 
		// LOOK IN SPECIES FILE
		FiSpecies s= plant.getSpecies();
		if (s.massEqs.size()==0) 
			throw new Exception(
					"FiLocalPlantBiomass.setBiomassWithoutPruning: no model available for species "
							+ speciesName);

		for (String pt:s.massEqs.keySet()) {
			plant.biomass.put(pt, s.massEqs.get(pt).f(dbh, h));

		}
	}

	//	/**
	//	 * This function return the distribution of biomass for a small "dh" at height Hrel (inside crown) or 
	//	 * HrelFromGround for a some models
	//	 * 
	//	 * @param speciesName
	//	 * @param Hrel
	//	 * @param HrelFromGround
	//	 * @param status
	//	 * @param particles
	//	 * @return
	//	 */
	//	
	//	private static double massCrownProfile(String speciesName, double Hrel,
	//			double crownLength, double HrelFromGround, double treeHeight,
	//			boolean status, boolean particles) {
	//		double dx = 0.05;
	//		double a;
	//		double b;
	//		double c;
	//		if (Hrel > 1.0 || HrelFromGround < 0.0) {
	//			return 0.0;
	//		}
	//		if (speciesName.equals(FiModel.PINUS_PINASTER))  {
	//			// reference is from crown base height
	//			if (Hrel < 0.0)
	//				return 0.0;
	//			
	//			if (status==FiPlant.ALIVEb && particles==FiPlant.LEAVESb) {
	//				/* FP 09-2009: Annabel porte relashionship for biomass distribution return
	//				 * the normalized value of LA for needles of year 1, 2 and 3 as a function
	//				 * of relative diameter in crown */
	//				// FP RENORMALISATION
	//				double coeff = 0.82;
	//				Hrel = Hrel * (1.0 - 0.18) + 0.18;
	//
	//				double year1 = coeff * 35.5 * Math.pow(Hrel, 4.02)
	//				* Math.pow(Math.max(0, 0.9999 - Hrel), 1.11);
	//				double year2 = coeff * 107.0 * Math.pow(Hrel, 4.19)
	//				* Math.pow(Math.max(0, 0.9999 - Hrel), 1.93);
	//				double year3 = coeff * 1254.0 * Math.pow(Hrel, 4.86)
	//				* Math.pow(Math.max(0, 0.89 - Hrel), 3.07) / 0.89;
	//				return (0.5 * year1 + 0.4 * year2 + 0.1 * year3) / crownLength;
	//			}
	//			return 0.0;
	//		}
	//		// FP 09-2009: ICFME
	//		// be careful here, the equations provided by alexander are valid from the apex and not from the ground!
	//		if (speciesName.equals(FiModel.PINUS_BANKSIANA)) {
	//			if (status==FiPlant.ALIVEb) {
	//				if (particles==FiPlant.LEAVESb) {
	//					a=0.996;
	//					b=2.403;
	//					c=13.086;
	//					return 1.1
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c)
	//							/ treeHeight;
	//				} else { //ALIVE TWIGS 
	//					a=0.996;
	//					b=2.936;
	//					c=14.112;
	//					return 1.06
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c)
	//							/ treeHeight;
	//				}
	//			} else { //DEAD NEEDLES
	//				if (particles==FiPlant.LEAVESb) {
	//					return 0.0;
	//				} else { // DEAD TWIGS
	//					a=1.025;
	//					b=3.84;
	//					c=6.945;
	//					return 1.04
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c)
	//							/ treeHeight;
	//				}
	//			}
	//		}
	//		if (speciesName.equals(FiModel.PINUS_BANKSIANA_DEAD))  {
	//			if (status == FiPlant.DEADb && particles ==FiPlant.TWIGSb) {
	//				a=1.053;
	//				b=2.625;
	//				c=5.358;
	//				return 1.09
	//						* verticalFromCumulative(1d - HrelFromGround, a, b, c)
	//						/ treeHeight;
	//			}
	//			return 0.0;
	//		}
	//		if (speciesName.equals(FiModel.PICEA_MARIANA))  {
	//			if (status==FiPlant.ALIVEb) {
	//				if (particles==FiPlant.LEAVESb) {
	//					a=1.015;
	//					b = 2.755;
	//					c=6.923;
	//					return 1.07
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c)
	//							/ treeHeight;
	//				} else { //ALIVE TWIGS 
	//					a=1.032;
	//					b=2.654;
	//					c=6.113;
	//					return 1.07
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c)
	//							/ treeHeight;
	//				}
	//			} else { //DEAD NEEDLES
	//				if (particles==FiPlant.LEAVESb) {
	//					return 0.0;
	//				} else { // DEAD TWIGS
	//					a=1.00;
	//					b=9.79;
	//					c=12.254;
	//					return 1.09
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c)
	//							/ treeHeight;
	//				}
	//			}
	//		}
	//		// FP 09-2009: Mistopoulos distribution for Aleppo pine
	//		if (speciesName.equals(FiModel.PINUS_HALEPENSIS))  {
	//			// if (status==FiModel.ALIVEb && particles==FiModel.LEAVESb) {
	//			a=1.045;
	//			b=4.925; 
	//			c= 8.055;
	//			return verticalFromCumulative(HrelFromGround, a, b, c)
	//						/ treeHeight;
	//			// }
	//			// return 0.0;
	//		}
	//		// chema
	//		if (speciesName.equals(FiModel.PINUS_PINASTER_NAVAS)
	//				|| speciesName.equals(FiModel.PINUS_PINASTER_TELENO)) {
	//			if (status == FiPlant.ALIVEb && particles == FiPlant.LEAVESb) {
	//				a = 1.16;
	//				b = 13.83;
	//				c = 15.71;
	//				return verticalFromCumulative(HrelFromGround, a, b, c)
	//						/ treeHeight;
	//			}
	//			if (status == FiPlant.ALIVEb && particles == FiPlant.TWIGSb) {
	//				a = 1.06;
	//				b = 11.60;
	//				c = 14.55;
	//				return verticalFromCumulative(HrelFromGround, a, b, c)
	//						/ treeHeight;
	//			}
	//			if (status == FiPlant.DEADb && particles == FiPlant.TWIGSb) {
	//				a = 1.03;
	//				b = 4.74;
	//				c = 9.24;
	//				return verticalFromCumulative(HrelFromGround, a, b, c)
	//						/ treeHeight;
	//			}
	//		}
	//		if (speciesName.equals(FiModel.PINUS_PONDEROSA_USFS1)) {
	//			// TEMPORARY BANKSIANA
	//			if (status == FiPlant.ALIVEb) {
	//				if (particles == FiPlant.LEAVESb) {
	//					a = 0.996;
	//					b = 2.403;
	//					c = 13.086;
	//					return 1.1
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c) / treeHeight;
	//				} else { // ALIVE TWIGS
	//					a = 0.996;
	//					b = 2.936;
	//					c = 14.112;
	//					return 1.06
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c) / treeHeight;
	//				}
	//			} else { // DEAD NEEDLES
	//				if (particles == FiPlant.LEAVESb) {
	//					return 0.0;
	//				} else { // DEAD TWIGS
	//					a = 1.025;
	//					b = 3.84;
	//					c = 6.945;
	//					return 1.04
	//							* verticalFromCumulative(1d - HrelFromGround, a, b,
	//									c) / treeHeight;
	//				}
	//			}
	//		}
	//		if (speciesName.equals(FiModel.PINUS_SYLVESTRIS))  {
	//			// / reference is in crown (need to be checked)
	//			if (Hrel < 0.0)
	//				return 0.0;
	//
	//			if (status==FiPlant.ALIVEb && particles==FiPlant.LEAVESb) {
	//			// FP 09-2009: Tahvanainen distribution for Scott pine
	//			double dPlus = 0.0428 + 1.14 * (1.0 - Math.exp(-3.43
	//					* (Hrel + 0.5 * dx))) * 0.993;
	//			double dMoins = 0.0428 + 1.14 * (1.0 - Math.exp(-3.43
	//					* (Hrel - 0.5 * dx))) * 0.993;
	//			return (dPlus - dMoins) / (dx * crownLength);
	//			} 
	//			return 0.0;
	//		}
	//		
	//		return 1;
	//	}
	//

	/**
	 * FP 11-06-2009: Annabel porte relashionship for leaf area in m2 and
	 * biomass in kg for "branch" for different species, as a function of
	 * relative height relH, and Branch diameter branchDiameter (mm NB relH is
	 * not used for Quercus ilex
	 * 
	 * @author pimont
	 */
	public static double[] branchProperties(String speciesName, double relH,
			double branchDiameter) throws Exception {
		double[] result = new double[2];

		//TODO		
		//		double a, b, c, sla;
		//		double[] slaArray = FiSpecies
		//				.slaOfSeveralYears(speciesName);
		//		result[0] = -1;
		//		result[1] = -1;
		//		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
		//			// TO DO correct units
		//			// PORTE ???
		//			// year1
		//			a = 1.721;
		//			b = 2.476;
		//			c = 0.7423;
		//			sla = slaArray[1];
		//			double year1lf = a * Math.pow(branchDiameter, b)
		//					* Math.pow(relH, c);
		//			double year1bm = year1lf / sla;
		//			// year2
		//			a = 0.9783;
		//			b = 2.681;
		//			c = 0.9463;
		//			sla = slaArray[2];
		//			double year2lf = a * Math.pow(branchDiameter, b)
		//					* Math.pow(relH, c);
		//			double year2bm = year2lf / sla;
		//			// CORRECTION: FP SUPPOSED bm in g instead of kg in the publication
		//			// result[0] = (year1lf + year2lf);
		//			// result[1] = (year1bm + year2bm);
		//
		//			result[0] = 0.001 * (year1lf + year2lf);
		//			result[1] = 0.001 * (year1bm + year2bm);
		//			return result;
		//		}
		//		if (speciesName.equals(FmModel.PINUS_PINASTER)) {
		//			// PORTE 2000 in the model, the branchDiameter are in cm
		//			branchDiameter *= 0.1; // branchDiameter in cm
		//			// year1
		//			a = 0.348;
		//			b = 0.030;
		//			c = 0.881;
		//			sla = slaArray[1];
		//			double year1lf = Math.pow(branchDiameter * branchDiameter
		//					* (a * relH + b), c);
		//			double year1bm = year1lf / sla;
		//			// year2
		//			a = 0.348;
		//			b = 0.030;
		//			c = 0.881;
		//			sla = slaArray[2];
		//			double year2lf = Math.pow(branchDiameter * branchDiameter
		//					* (a * relH + b), c);
		//			double year2bm = year2lf / sla;
		//			// year3
		//			// in annabel study year 3 varied between 1 % and 30 % of
		//			// year1+year2
		//			// here we choose 15 %
		//			sla = slaArray[2];
		//			double year3lf = 0.15 * (year1lf + year2lf);
		//			double year3bm = year3lf / sla;
		//			result[0] = year1lf + year2lf + year3lf;
		//			result[1] = year1bm + year2bm + year3bm;
		//			return result;
		//		}
		//		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
		//			a = 0.6452;
		//			b = 0.2726;
		//			sla = slaArray[0];
		//			// CORRECTION: FP SUPPOSED bm in g instead of kg in the publication
		//			// result[0] = Math.exp(branchDiameter * b);
		//			// result[1] = result[0] / sla;
		//			result[0] = 0.001 * Math.exp(branchDiameter * b);
		//			result[1] = result[0] / sla;
		//			return result;
		//		}
		return result;
	}

	/**
	 * FP 11-06-2009: relashionship for leaf area in m2 [0] (total, not single
	 * sided) leaves biomass [1] and twigs [2] for "crowns" for different
	 * species, as a function of dbh (cm) and age (years) NB age is required
	 * just for Pinus pinaster
	 * 
	 * NB THIS IS JUST AN ESTIMATE FOR PLANT PROPERTIES COMPUTATION
	 * 
	 * @throws Exception
	 * 
//	 * @throws Exception
//	 */
	//	public static void crownPropertiesIncludePruning(FmPlant plant)
	//			throws Exception {
	//
	//		String speciesName = plant.getSpeciesName();
	//		double pruneLenght = plant.getCrownBaseHeight()
	//				- plant.getCrownBaseHeightBeforePruning();
	//		double crownLength = plant.getCrownLengthBeforePruning();
	////		double[] tempLive = crownPropertiesWithoutPruning(localPlant,
	////				FmModel.ALIVE);
	////		double[] tempDead = crownPropertiesWithoutPruning(localPlant,
	////				FmModel.DEAD);
	//		double treeHeight = plant.getHeight();
	//		// hrel inside crown:
	//		double Hrel = pruneLenght / plant.getCrownLengthBeforePruning();
	//		double dHrel = 0.1 * Hrel;
	//		Hrel = 0.5 * dHrel;
	//		if (pruneLenght > 0d) {
	//			for (String particleName : plant.biomass.keySet ()) {
	//				double biomassBelowPruneHeightFraction = 0d;
	//				for (int i = 1; i <= 10; i++) {
	//					double HrelFromGround = (Hrel * crownLength + plant
	//						.getCrownBaseHeightBeforePruning())
	//						/ plant.getHeight();
	//
	//					biomassBelowPruneHeightFraction += dHrel
	//						* plant.getSpecies ().getVerticalProfile (particleName).f (HrelFromGround, plant);
	//					Hrel += dHrel;
	//				}
	//				double newBiomass = plant.biomass.get(particleName)* (1d - biomassBelowPruneHeightFraction);
	//			}
	//		}
	//	}

	//	/**
	//	 * FP 09-2009: This method use different allometry to assess crown
	//	 * distribution, depending on the species
	//	 * 
	//	 * @param distanceToTrunc
	//	 *            (m)
	//	 * @param relativeHeight
	//	 *            in unpruned tree (in crown)
	//	 * @return local bulk density (kg/m3)
	//	 * @throws Exception
	//	 * 
	//	 */
	//	public static double bulkDensity(FiPlant plant,
	//			// String speciesName, double dbh,int age,
	//			double distanceToTrunc, double z, boolean status,
	//			boolean particles) throws Exception {
	//
	//		String speciesName = plant.getSpeciesName();
	//		// before pruning
	//		double crownLength = plant.getHeight()
	//				- plant.getCrownBaseHeightBeforePruning();
	//
	//		// pruning:
	//		// if (relativeHeight < (plant.getCrownBaseHeight() - plant
	//		// .getCrownBaseHeightBeforePruning())
	//		// / crownLength) {
	//		// return 0d;
	//		// }// pruning:
	//		if ((plant.getCrownBaseHeight() > plant
	//				.getCrownBaseHeightBeforePruning())
	//				&& z < plant.getCrownBaseHeight()) {
	//			System.out.println("prunned tree:" + plant.getId());
	//			return 0d;
	//		}
	//
	//		double crownRadius = plant.getCrownRadius();
	//		double cbh = plant.getCrownBaseHeightBeforePruning();
	//		double[] properties = crownPropertiesWithoutPruning(plant, (status) ? 1
	//				: 0);
	//		double biomass = -1;
	//		// cylindric shape for tests:
	//		// double crownRayZ = crownRadius;
	//		// double relR = distanceToTrunc/crownRayZ;
	//		// double horizDistrib=horizontalDistribution(speciesName,
	//		// relR)/(crownRayZ*crownRayZ);
	//		// double vertiDistrib=1./crownLength;
	//
	//		// real distribution;
	//		double relativeHeight = (z - cbh) / (crownLength);
	//		// double crownRayZ = FiLocalPlantShape
	//		// .relativeRadiusPorte(relativeHeight)
	//		// * crownRadius + 0.00001;
	//		double crownRayZ = FmPlant
	//				.relativeRadiusFromCrownProfileIncludingDead(relativeHeight,
	//						speciesName)
	//				* crownRadius + 0.000001;
	//		double relR = distanceToTrunc / crownRayZ;
	//		double horizDistrib = massHorizontalDistribution(speciesName, relR, status,
	//				particles)
	//				/ (crownRayZ * crownRayZ);
	//
	//		// this is required for some models to compute vertical distribution...
	//		double relativeHeightFromGround = z / plant.getHeight();
	//		double vertiDistrib = massCrownProfile(speciesName, relativeHeight,
	//				crownLength, relativeHeightFromGround, plant.getHeight(),
	//				status, particles);
	//		// / crownLength;
	//
	//		// ALIVE or DEAD LEAVES
	//		if (particles == FiPlant.LEAVESb) {
	//			biomass = properties[1];
	//		} else {
	//			// ALIVE or DEAD TWIGS
	//			biomass = properties[2];
	//		}
	//
	//		if (biomass < 0.0) {
	//			throw new Exception(
	//					"FiLocalPlantBiomass.propertiesInVoxel: no model available for species "
	//							+ speciesName);
	//		}
	//		// System.out.println("bulkDensity="+result+" relH="+relativeHeight+" relR="+relR+" vertic[0]="+vertiDistrib[0]+" horiz[0]="+horizDistrib[0]+" biomass[0]="+biomass[0]);
	//		// here max was added by FP 05/2012 to avoid cases with vertiDistrib or
	//		// horizDistrib with negative values
	//		return Math.max(0d, vertiDistrib * horizDistrib * biomass);
	//	}
	//
	//	private static double verticalFromCumulative(double Hrel, double a,
	//			double b, double c) {
	//		// double dx=0.05;
	//		// double dPlus = a / (1.0 + Math
	//		// .exp(b - c * (Hrel + 0.5 * dx)));
	//		// double dMoins = a / (1.0 + Math
	//		// .exp(b - c * (Hrel - 0.5 * dx)));
	//		// return (dPlus - dMoins) / dx;
	//		
	//		
	//		// Be careful: the reference for models of the icfme are from the top of the tree
	//		return a * c * Math.exp(b - c * Hrel)
	//				/ Math.pow(1.0 + Math.exp(b - c * Hrel), 2d);
	//		
	//	}


	//	/**
	//	 * FP 29-09-2009: leaf in m2 (total, not single sided) and biomass in a
	//	 * voxel for different species
	//	 * @throws Exception
	//	 */
	//	public static double biomassInVoxel(double x, double y, double z,
	//			double voxelVolume, FiPlant plant, boolean status, boolean particles)
	//	throws Exception {
	//
	//		String speciesName = plant.getSpeciesName();
	//		// crown lenght including pruned zone
	//		
	//		// special model for ponderosa pine
	//		if (speciesName.equals(FiModel.PINUS_PONDEROSA_LANL)) {
	//			double crownLength = plant.getHeight()
	//					- plant.getCrownBaseHeightBeforePruning();
	//	
	//			// pruning:
	//			if ((plant.getCrownBaseHeight() > plant
	//					.getCrownBaseHeightBeforePruning())
	//					&& z < plant.getCrownBaseHeight()) {
	//				System.out.println("prunned tree:" + plant.getId());
	//				return 0d;
	//			}
	//			// model for alive needles 
	//			if (status == FiPlant.ALIVEb && particles == FiPlant.LEAVESb) {
	//				// Rod Linn's model
	//				double rhomax = 0.4 * 6.0 / 5.0; // kg/m3
	//				double h = crownLength * 0.2;
	//				double d = crownLength * 0.8;
	//				double R = plant.getCrownRadius();
	//				// relative ray squarred in crown
	//				double relRayToCrownRadius2 = (x * x + y * y) / (R * R);
	//				double relRay2 = Double.MAX_VALUE;
	//				if (z > 0. && z <= h) {// lower part
	//					relRay2 = relRayToCrownRadius2 * h / z;
	//				} else if (z > h && z <= crownLength) { // upper part
	//					relRay2 = relRayToCrownRadius2 * d / (crownLength - z);
	//				}
	//				if (relRay2 <= 1.0) { // in crown
	//					double bulkDensity = (z + d * relRayToCrownRadius2) / crownLength * rhomax;
	//					return bulkDensity * voxelVolume;
	//				}
	//			}
	//			return 0.0;
	//		}
	//		double distanceToTrunc = Math.sqrt(x * x + y * y);
	//		// TODO for clarity bulkDensity should use z instead of z/crownLenght
	//		// nevertheless hrelfromground is computed in bulk density so it should
	//		// be OK
	//		double bulkDensity = bulkDensity(plant,
	//				distanceToTrunc, z, status,
	//				particles);
	//		// distanceToTrunc, z / crownLength, status, particles);modified by FP
	//		//System.out.println("x="+x+" y="+y+" z="+z+" distanceToTrunc="+distanceToTrunc+" crownLength="+crownLength);
	//		return bulkDensity * voxelVolume;
	//	}
}


