package fireparadox.model.layerSet;

import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiSpecies;

/**
 * A layer in the FuelManager, with evolution. It also has specific constructors because forms and
 * loaders
 * 
 * @author F. Pimont, F. de Coligny
 */
public class FmLayer extends FiLayer {

	static public final String HERB = "Herb";
	static public final String SHRUB = "Shrub";
	static public final int nfam = 3;

	public FmLayer () {
		super ();
	}

	
	
	/**
	 * Constructor 
	 * NB:mvr and svr are known for LIVE or DEAD, not LIVE AND
	 * @throws Exception
	 */
	public FmLayer (String layerType, double height, double baseHeight, double coverFraction,
			double characteristicSize, int spatialGroup, double liveMoisture, double deadMoisture, double liveTwigMoisture,
			double[] liveBulkDensity, double[] deadBulkDensity, double[] mvr, double[] svr,
			FiSpecies defaultSpecies) throws Exception {
		super (layerType, height, baseHeight, coverFraction, characteristicSize, spatialGroup, defaultSpecies);

		for (int i = 0; i < liveBulkDensity.length; i++) {
			// System.out.println ("i=" + i + ", bd=" + liveBulkDensity[i]);
			double moisture = liveMoisture;
			if (i>0) {
				moisture = liveTwigMoisture;
			}
			if (liveBulkDensity[i] > 0d) {
				FiParticle pt = new FiParticle (FiParticle.getTypeName (i), FiParticle.LIVE, mvr[i], svr[i], moisture, species.getName ());
				this.addFiLayerParticle (liveBulkDensity[i], pt);
			}
		}
		for (int i = 0; i < deadBulkDensity.length; i++) {
			if (deadBulkDensity[i] > 0d) {
				FiParticle pt = new FiParticle (FiParticle.getTypeName (i), FiParticle.DEAD, mvr[i], svr[i], deadMoisture, species.getName ());
				this.addFiLayerParticle (deadBulkDensity[i], pt);
			}
		}
	}
	/**
	 * Constructor for every properties, but the particles and bulkDensities (set with a function
	 * addFiLayerParticle...), based on super constructor (used for copy)
	 */

	public FmLayer (String layerType, double height, double baseHeight, double coverFraction,
			double characteristicSize, int spatialGroup, FiSpecies defaultSpecies) {
			super(layerType, height, baseHeight,coverFraction,characteristicSize,  spatialGroup, defaultSpecies);
			
			
	}
	/**
	 * Returns a copy of the FiLayer.
	 * 
	 * @throws Exception
	 */
	public FmLayer copy () throws Exception {
		//return (FmLayer) super.copy ();
		FmLayer cp = new FmLayer (layerType, height, baseHeight, coverFraction, characteristicSize, spatialGroup,
				species.getDefaultSpecies ());
		for (FiParticle fp : getParticles()) {
			FiParticle newfp = fp.copy ();
			cp.bulkDensityMap.put (newfp, getBulkDensity(fp));
		}
		return cp;
		
	}

	/**
	 * get the mean bulkDensity for i=0 to 3 (leave, twig1,...)
	 * use the superclass getBulkDensity;
	 * 
	 * @param i
	 * @param status
	 * @return
	 */
	public double getBulkDensity (int i, String status) {
		return getBulkDensity(getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), status)));
	}
	/**
	 * set the bulk density
	 * @param v value
	 * @param i number (0-3)
	 * @param status live or dead
	 * @throws Exception
	 */
	public void setBulkDensity (double v, int i, String status) throws Exception {
		bulkDensityMap.put (getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), status)), v);
	}
	/**
	 * 
	 * @param v arrays of 4 value
	 * @param status
	 * @throws Exception
	 */
	public void setBulkDensities (double[] v, String status) throws Exception {
		for (int i = 0; i < v.length; i++) {
			if (v[i] > 0d) {
				setBulkDensity (v[i], i, status);
			}
		}
	}
	
	/**
	 * get the mean MVR for i=0 to 3 (leave, twig1,...)
	 * 
	 * @param i
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public double getMVR (int i, String status) throws Exception {
		return getMVR(FiParticle.makeKey (FiParticle.getTypeName (i), status));
	}
	/**
	 * set the value of MVR if exist
	 * @param v value
	 * @param i (0-3)
	 * @param status
	 * @throws Exception
	 */
	public void setMVR (double v, int i, String status) throws Exception {
		FiParticle pt = getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), status));
		pt.mvr = v;
	}
	/**
	 * 
	 * @param v
	 * @param status
	 * @throws Exception
	 */
	public void setMVR (double[] v, String status) throws Exception {
		for (int i = 0; i < v.length; i++) {
			setMVR (v[i], i, status);
		}
	}

	/**
	 * get the mean SVR for i=0 to 3 (leave, twig1,...)
	 * 
	 * @param i
	 * @param status
	 * @return
	 * @throws Exception
	 */

	public double getSVR (int i, String status) throws Exception {
		return getSVR(FiParticle.makeKey (FiParticle.getTypeName (i), status));
	}
	
	/**
	 * set the value of SVR if exist
	 * @param v value
	 * @param i (0-3)
	 * @param status
	 * @throws Exception
	 */
	public void setSVR (double v, int i, String status) throws Exception {
		FiParticle pt = getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), status));
		pt.svr = v;
	}
	
	/**
	 * 
	 * @param v
	 * @param status
	 * @throws Exception
	 */
	public void setSVR (double[] v, String status) throws Exception {
		for (int i = 0; i < v.length; i++) {
			setSVR (v[i], i, status);
		}
	}

	/**
	 * get the mean Moisture for i=0 to 3 (leave, twig1,...)
	 * 
	 * @param i
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public double getMoisture (int i, String status) throws Exception {
		return getMoisture(FiParticle.makeKey (FiParticle.getTypeName (i), status));
	}
	/**
	 * set the value of SVR if exist
	 * @param v value
	 * @param i (0-3)
	 * @param status
	 * @throws Exception
	 */
	public void setMoisture (double v, int i, String status) throws Exception {
		FiParticle pt = getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), status));
		pt.moisture = v;
	}
	/**
	 * 
	 * @param v
	 * @param status
	 * @throws Exception
	 */
	public void setMoistures (double value, String status) {
		for (FiParticle particle : getParticles()) {
			if (particle.name.endsWith (status)) {
				particle.moisture = value;
			}
		}
	}

	/**
	 * Evolution of the FmLayer with time
	 * 
	 * @param age
	 * @param fertility
	 * @param lastClearingType
	 * @param treatmentEffect
	 * @param herbCover
	 * @param shrubCover
	 * @param treeCover
	 * @param familyType
	 * @return
	 * @throws Exception
	 */
	public FmLayer processGrowth (int age, double fertility, int lastClearingType, double treatmentEffect,
			double herbCover, double shrubCover, double treeCoverPerc, String familyType) throws Exception {
		double coverFractionDelta = 0d;// TODO: cover fraction evolution to be done
		double liveFraction = 0d;// TODO: livefraction evolution to be done

		if (familyType.equals (FmLayer.SHRUB)) {
			liveFraction = 0.8d;
		}

		double lastShrubTheoreticalHeight = height (age, fertility, lastClearingType, treatmentEffect, treeCoverPerc, familyType);
		double newShrubTheoreticalHeight = height (age + 1, fertility, lastClearingType, treatmentEffect, treeCoverPerc, familyType);
		double[] lastShrubTheoreticalThinLoad = thinBiomassLoad (age, fertility, lastClearingType, treatmentEffect, herbCover, shrubCover, treeCoverPerc, familyType);
		double[] newShrubTheoreticalThinLoad = thinBiomassLoad (age + 1, fertility, lastClearingType, treatmentEffect, herbCover, shrubCover, treeCoverPerc, familyType);
		// if (familyType.equals(FiModel.SHRUB)) {
		// System.out.println("age="+age+" fertility="+fertility+" treeC="+treeCover+" famType="+familyType);
		// System.out.println("newtheoricalBiomass="+newShrubTheoreticalThinLoad[0]);
		// System.out.println("newtheoricalHeight="+newShrubTheoreticalHeight);
		// }
		double heightDelta = newShrubTheoreticalHeight - lastShrubTheoreticalHeight;
		// double thinLoadDelta = newShrubTheoreticalThinLoad
		// - lastShrubTheoreticalThinLoad;

		FmLayer newLayer = this.copy ();
		double newHeight = this.height + heightDelta;

		double newCoverFraction = coverFraction + coverFractionDelta;
		double[] newLiveBulkDensity = new double[nfam];
		double[] newDeadBulkDensity = new double[nfam];

		for (int i = 0; i < nfam; i++) {
			double bdlivedead =  getBulkDensity (i, FiParticle.LIVE)+ getBulkDensity (i, FiParticle.DEAD); 
			double previousThinLoadI = height * bdlivedead * coverFraction;
			//double newThinLoadI = previousThinLoadI +Math.abs(newShrubTheoreticalThinLoad[i] - lastShrubTheoreticalThinLoad[i]) ;
			double newThinLoadI = previousThinLoadI +newShrubTheoreticalThinLoad[i] - lastShrubTheoreticalThinLoad[i] ;
				
			double newBulkDensityI = newThinLoadI / (newHeight * newCoverFraction);
			newLiveBulkDensity[i] = newBulkDensityI * liveFraction;
			newDeadBulkDensity[i] = newBulkDensityI * (1d - liveFraction);
		}

		// TODO FP: layerSetProcessGrowth liveFraction should depend on species and age and height!
		newLayer.setHeight (newHeight);
		newLayer.setCoverFraction (newCoverFraction);
		newLayer.setBulkDensities (newLiveBulkDensity, FiParticle.LIVE);
		newLayer.setBulkDensities (newDeadBulkDensity, FiParticle.DEAD);
		return newLayer;

	}

	/**
	 * This method compute the thin biomass load of the layerSet, assuming a given effect of the
	 * canopy above
	 * 
	 * @param age
	 * @param fertility
	 * @param lastClearingType
	 * @param treatmentEffect
	 * @param treeCoverPerc
	 * @return
	 * @throws Exception
	 */
	static public double[] thinBiomassLoad (int age, double fertility, int lastClearingType, double treatmentEffect,
			double herbCover, double shrubCover, double treeCoverPerc, String familyType) throws Exception {
		double btot = totalLoad (age, fertility, lastClearingType, treatmentEffect, herbCover, shrubCover, treeCoverPerc, familyType);

		double[] load = new double[3];
		//System.out.println("BTOT for famility type "+familyType+"=" + btot);
		if (familyType.equals (SHRUB)) {
			// double bleaves = btot * 0.58d / Math.pow(1d + (double) age,
			// 0.25);
			load[0] = btot * 0.65d / Math.pow (1d + (double) age, 0.4);
			load[1] = btot * 0.3d / Math.pow (1d + (double) age, 0.2);
			load[2] = btot * 0.32d * Math.pow ((double) age / (1d + (double) age), 0.4);
			return load;
		}
		if (familyType.equals (HERB)) {
			load[0] = btot;
			load[1] = 0d;
			load[2] = 0d;
			return load;
		}
		throw new Exception ("FiLocalLayerEvolution.totalLoad: no model for type=" + familyType);

	}

	static public double height (int age, double fertility, int lastClearingType, double treatmentEffect,
			double treeCoverPerc, String type) throws Exception {
		if (age == 0) { return 0d; }
		if (type.equals (SHRUB)) {
			double hpot = (0.228 * Math.log (age) + 0.112) * (1d + 0.2 * (fertility - 1.5));
			// TODO effet traitement...
			return hpot * (1d + 0.00673 * (Math.min (treeCoverPerc, 75d) - 25d));
		}
		if (type.equals (HERB)) { return 0.25; }
		throw new Exception ("FiLocalLayerEvolution.height: no model for type=" + type);
	}

	/**
	 * This method compute the total load of the layerSet, assuming a given effect of the canopy
	 * above
	 * 
	 * @param age
	 * @param fertility
	 * @param lastClearingType
	 * @param treatmentEffect
	 * @param treeCover
	 * @return
	 * @throws Exception
	 */
	public static double totalLoad (int age, double fertility, int lastClearingType, double treatmentEffect,
			double herbCover, double shrubCover, double treeCoverPerc, String type) throws Exception {
		if (age == 0) { return 0d; }
		//System.out.println("BTOT computation: age="+age+"fertility="+fertility+"lastClearingType="+lastClearingType+"treatmentEffect="+treatmentEffect);
		//System.out.println("	herbCover="+herbCover+"shrubCover="+shrubCover+"treeCover="+treeCover);
		double bpot = (0.700 * (1d - 0.5 * 0.01*treeCoverPerc) * Math.log (age) + 0.247) // potentiel
				// including
				// light
				* (1d + 0.2 * (fertility - 1.5)) // fertility
				* shrubCover / 0.80d; // cover

		if (type.equals (SHRUB)) {
			// TODO effet traitement...
			return bpot * (1d - 0.3 * Math.pow ((double) age, -0.75));
		}
		if (type.equals (HERB)) {
			// return bpot * 0.3 * Math.pow((double) age, -0.75) * 2d *
			// herbCover
			// / shrubCover + 0.2 
			// * Math.max(herbCover - 0.5 * shrubCover, 0d);
			return bpot * 0.3 * Math.pow ((double) age, -0.75) + 0.2 
					* Math.max (herbCover - 0.5 * shrubCover, 0d);
		}
		throw new Exception ("FiLocalLayerEvolution.totalLoad: no model for type=" + type);

	}
}
