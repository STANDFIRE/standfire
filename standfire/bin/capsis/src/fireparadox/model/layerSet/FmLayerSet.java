package fireparadox.model.layerSet;

import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Log;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmStand;

/**
 * A layerSet in the FuelManager.
 * 
 * @author F. Pimont, F. de Coligny
 */
public class FmLayerSet extends FiLayerSet {

	// for evolution and treatment
	private int age; // age in year after clearing
	private double fertility; // from 0 to 3
	private int lastClearingType; // fire, prescribe burning, mechanical clearing
	private double treatmentEffect; // between 0 and 1 (0 is no treatment)

	
	/**
	 * Constructor.
	 */
	public FmLayerSet (int id) {
		super (id);
		this.setType ();
		age = -1;
	}

	/**
	 * Constructor 2.
	 */
	public FmLayerSet (int id, Collection<FiLayer> layers) {
		super (id, layers);
		age = -1;
	}

	/**
	 * Contructor 3 : to build a evolutive layerSet with age 0
	 * 
	 * @throws Exception
	 */
	public FmLayerSet (/* FmStand stand, */int id, Polygon poly, double fertility, double shrubCoverFraction,
			double shrubCharacteristicSize, double herbCoverFraction, double herbCharacteristicSize, double liveMC,
			double deadMC) throws Exception {
		this (id);
		this.setLayers (this.simplifiedLayersForEvolution ());
		// create an empty layerSet with SHRUB and HERB
		this.updateToMatch (poly);
		age = 0;
		this.fertility = fertility;
		this.lastClearingType = 1;
		this.treatmentEffect = 0d;
		Collection<FmLayer> lc = getFmLayers ();
		System.out.println ("local layers:" + lc.size ());
		for (FmLayer l : lc) {
			System.out.println ("local layer:" + l.getLayerType ());
			if (l.getLayerType ().equals (FmLayer.SHRUB)) {
				l.setCoverFraction (shrubCoverFraction);
				l.setCharacteristicSize (shrubCharacteristicSize);
			} else { // HERB
				l.setCoverFraction (herbCoverFraction);
				l.setCharacteristicSize (herbCharacteristicSize);
			}
		
			l.setMoistures (liveMC, FiParticle.LIVE);
			l.setMoistures (deadMC, FiParticle.DEAD);
		} 
	}
	
	/**
	 * Return a collection of FMLayers.
	 */
	public Collection<FmLayer> getFmLayers () {
		Collection<FmLayer> fmLayers = new ArrayList<FmLayer> ();
		for (FiLayer fiLayer : getLayers ()) {
			fmLayers.add ((FmLayer) fiLayer);
		}
		return fmLayers;
	}
	

	/**
	 * Clone method, does not clone the layers. Can be used for layerSet
	 * evolution.
	 */
	public FmLayerSet clone () {
		try {
			FmLayerSet c = (FmLayerSet) super.clone ();
			return c;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FmLayerSet.clone ()", "Exception", e);
			return null;
		}
	}

	/**
	 * Copy of FmLayerSet
	 * 
	 * @throws Exception
	 */
	public FmLayerSet copy () throws Exception {

		// Clone does not clone the layers
		FmLayerSet copy = (FmLayerSet) super.clone ();
		
		// Copy the layers
		for (FmLayer layer : getFmLayers ()) {
			copy.addLayer (layer.copy ());
		}

		copy.age = age;
		copy.fertility = fertility;
		copy.lastClearingType = lastClearingType;
		copy.treatmentEffect = treatmentEffect;

		return copy;
	}
	
	public boolean containsShrubAndHerbOnly () {
		for (FmLayer layer : this.getFmLayers ()) {
			if (!layer.getLayerType ().equals (FmLayer.HERB) && !layer.getLayerType ().equals (FmLayer.SHRUB)) { return false; }
		}
		return true;
	}
	
	public boolean isEvolutionPossible () {
		if (age < 0) {
			System.out.println("evolutionpossible, age: "+age);
			return false;
		}
		//TODO: a test of layers to see if they can be related to a dynamic model
		return true;
	}

	public Collection<FmLayer> simplifiedLayersForEvolution() throws Exception {
		
		if (this.containsShrubAndHerbOnly ()) {
			return getFmLayers ();
		}
		double shrubHeight = 0d;
		double[] shrubLiveLoad = new double[3];
		double[] shrubDeadLoad = new double[3];
		double[] shrubLiveBulkDensity = new double[3];
		double[] shrubDeadBulkDensity = new double[3];
		double[] shrubVolume = new double[3];
		double[] shrubSurface = new double[3];
		double[] shrubMVR = new double[3];
		double[] shrubSVR = new double[3];
		int shrubSpatialGroup = 0;
		double shrubCharacteristicSize = 0d;
		double shrubCoverFraction = 0d;
		// double[] shrubLiveWaterMass = new double[3];
		// double[] shrubDeadWaterMass = new double[3];
		double shrubLiveWaterMass = 0d;
		double shrubDeadWaterMass = 0d;
		int nshrubLayer=0;


		double herbHeight = 0d;
		double herbLiveLoad = 0d;
		double herbDeadLoad = 0d;
		double herbVolume = 0d;
		double herbSurface = 0d;
		int herbSpatialGroup = 1;
		double herbCharacteristicSize = 0d;
		double herbCoverFraction = 0d;
		double herbLiveWaterMass = 0d;;
		double herbDeadWaterMass = 0d;
		double[] herbLiveBulkDensity = new double[1];
		double[] herbDeadBulkDensity = new double[1];
		double[] herbMVR = new double[1];
		double[] herbSVR = new double[1];
		int nherbLayer=0;
		
		//1. Computation of synthetic properties (SHRUB and HERB)
		FiSpecies defaultSpecies = null; 
		int nfam = FmLayer.nfam;
		for (FmLayer layer: getFmLayers ()) {
			FiSpecies species = layer.species;
			defaultSpecies = species.getDefaultSpecies ();
			if (species.isDefaultSpecies ()) {
				throw new Exception("FmLayerSet.simplifyForEvolution: the simplication of layerSet because layer "
							+layer.getLayerType ()+" is a defaultSpecies (no known type)");
			}
			String speciesTrait = species.getTrait();
			if (speciesTrait.equals (FiSpecies.TRAIT_BROADLEAVES)||speciesTrait.equals (FiSpecies.TRAIT_RESINEOUS)) {
				shrubHeight = Math.max(shrubHeight, layer.getHeight()); 	
				for (int i = 0; i < nfam; i++) {
					FiParticle fp = layer.getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), FiParticle.LIVE));
					if (!(fp==null)) {
						double liveLoad = layer.getBulkDensity (fp) * fp.mvr * layer.getHeight ()* layer.getCoverFraction ();
						shrubLiveLoad[i] += liveLoad;
						shrubVolume[i] += liveLoad / fp.mvr;	
						shrubSurface[i] += liveLoad / fp.mvr * fp.svr;
						shrubLiveWaterMass += fp.moisture * 0.01 * liveLoad;
					}
					fp = layer.getParticle(FiParticle.makeKey (FiParticle.getTypeName (i), FiParticle.DEAD));
					if (!(fp==null)) {
						double deadLoad = layer.getBulkDensity (fp) * fp.mvr * layer.getHeight ()* layer.getCoverFraction ();
						shrubDeadLoad[i] += deadLoad;
						shrubVolume[i] += deadLoad / fp.mvr;	
						shrubSurface[i] += deadLoad / fp.mvr * fp.svr;
						shrubDeadWaterMass += fp.moisture * 0.01 * deadLoad;
					}
				}
				shrubCoverFraction += layer.getCoverFraction ();
				shrubCharacteristicSize += layer.getCharacteristicSize()*layer.getCoverFraction ();
			} else if (speciesTrait.equals (FiSpecies.TRAIT_HERBACEOUS)) {
				herbHeight = Math.max(herbHeight, layer.getHeight());
				FiParticle fp = layer.getParticle(FiParticle.makeKey (FiParticle.getTypeName (0), FiParticle.LIVE));
				if (!(fp==null)) {
					double liveLoad = layer.getBulkDensity (fp) * fp.mvr * layer.getHeight ()* layer.getCoverFraction ();
					herbLiveLoad += liveLoad;
					herbVolume += liveLoad / fp.mvr;	
					herbSurface += liveLoad / fp.mvr * fp.svr;
					herbLiveWaterMass += fp.moisture * 0.01 * liveLoad;
				}
				fp = layer.getParticle(FiParticle.makeKey (FiParticle.getTypeName (0), FiParticle.DEAD));
				if (!(fp==null)) {
					double deadLoad = layer.getBulkDensity (fp) * fp.mvr * layer.getHeight ()* layer.getCoverFraction ();
					herbDeadLoad += deadLoad;
					herbVolume += deadLoad / fp.mvr;	
					herbSurface += deadLoad / fp.mvr * fp.svr;
					herbDeadWaterMass += fp.moisture * 0.01 * deadLoad;
				}
				herbCoverFraction += layer.getCoverFraction ();
				herbCharacteristicSize += layer.getCharacteristicSize()*layer.getCoverFraction ();
			} else {
				throw new Exception(
						"FmLayerSet.simplifyForEvolution: the simplication of layerSet is not possible due to speciesTrait of species :"
						+ layer.getLayerType()); 
			}

		}
						
		
		// 2. RETURN collection

		Collection<FmLayer> res = new ArrayList<FmLayer>();
		double totalShrubLiveLoad = 0d;
		double totalShrubDeadLoad = 0d;
		for (int i = 0; i < nfam; i++) {
			totalShrubLiveLoad += shrubLiveLoad[i];
			totalShrubDeadLoad += shrubDeadLoad[i];
			shrubMVR[i] = (shrubLiveLoad[i] + shrubDeadLoad[i])
			/ shrubVolume[i];
			shrubSVR[i] = shrubSurface[i] / shrubVolume[i];
			shrubLiveBulkDensity[i] = shrubLiveLoad[i]
			                                        / (shrubMVR[i] * shrubHeight * shrubCoverFraction);
			shrubDeadBulkDensity[i] = shrubDeadLoad[i]
			                                        / (shrubMVR[i] * shrubHeight * shrubCoverFraction);
		}
		double shrubLiveMoisture = shrubLiveWaterMass * 100d
		/ (totalShrubLiveLoad + 1e-10);
		double shrubDeadMoisture = shrubDeadWaterMass * 100d
		/ (totalShrubDeadLoad + 1e-10);
		if (shrubCoverFraction > 1d) {
			throw new Exception(
					"FmLayerSet.simplifyForEvolution: the simplication of layerSet is not possible because shrubCover > 1");
		}
		shrubCharacteristicSize /= shrubCoverFraction; // ponderation with
		// percentage
		
		FmLayer shrub = new FmLayer(FmLayer.SHRUB, shrubHeight, 0d,
				shrubCoverFraction, shrubCharacteristicSize,
				shrubSpatialGroup,
				shrubLiveMoisture,
				shrubDeadMoisture,shrubLiveMoisture, shrubLiveBulkDensity, shrubDeadBulkDensity, // shrubLiveMoisture for twigs
				shrubMVR, shrubSVR, defaultSpecies);
		System.out.println("adding shrub: "+ shrub.getLayerType());
		res.add(shrub);

		double herbLiveMoisture =0d;
		double herbDeadMoisture =0d;
		if (herbVolume > 0d) {
			herbMVR[0] = (herbLiveLoad + herbDeadLoad) / herbVolume;
			herbSVR[0] = herbSurface / herbVolume;
			herbLiveBulkDensity[0] = herbLiveLoad
			/ (herbMVR[0] * herbHeight * herbCoverFraction);
			herbDeadBulkDensity[0] = herbDeadLoad
			/ (herbMVR[0] * herbHeight * herbCoverFraction);
			herbLiveMoisture = herbLiveWaterMass * 100d
			/ (herbLiveLoad + 1e-10);
			herbDeadMoisture = herbDeadWaterMass * 100d
			/ (herbDeadLoad + 1e-10);
			if (herbCoverFraction > 1d) {
				throw new Exception(
						"FmLayerSet.simplifyForEvolution: the simplication of layerSet is not possible because herbCover > 1");
			}
			herbCharacteristicSize /= herbCoverFraction; // ponderation with
			// percentage
			
		}
		FmLayer herb = new FmLayer(FmLayer.HERB, herbHeight, 0d,
				herbCoverFraction,
				herbCharacteristicSize, herbSpatialGroup, herbLiveMoisture,
				herbDeadMoisture, herbLiveMoisture, herbLiveBulkDensity, herbDeadBulkDensity, // herbLiveMoisture for twigs...
				herbMVR, herbSVR, defaultSpecies);
		res.add(herb);
		return res;

	}

	/**
	 * get the SHRUB/HERB layer derived from simplifiedLayer
	 * 
	 * @throws Exception
	 * 
	 */
	public FmLayer getLayer(String Type) throws Exception {
		Collection<FmLayer> layers = this.simplifiedLayersForEvolution();
		FmLayer result = null;
		for (FmLayer ll : layers) {
			if (ll.getLayerType().equals(Type)) {
				result = ll;
			}
		}
		// System.out.println("getlayer of type= " + Type + "is "
		// + result.getSpeciesName());
		return result;
	}

	/**
	 * the routine to make the evolution of FmLayerSet : require a simplification of layers (herb, shrub) and some laws for evolution
	 * @param refStand
	 * @return
	 * @throws Exception
	 */
	public FmLayerSet processGrowth(FmStand refStand) throws Exception {
		if (!this.isEvolutionPossible()) {
			throw new Exception(
					"FmLayerSet.growthFrom: impossible to compute evolution "
					+ this.id);
		}
		FmLayerSet newLayerSet = this.clone ();
		// no layers for the moment...

		Collection<FmLayer> simplifiedLayers = this.simplifiedLayersForEvolution();
		newLayerSet.setAge(this.getAge() + 1);

		// fc-2.2.2015 particleNames
		FiModel m = (FiModel) refStand.getStep().getProject().getModel();

		double [] param = FmStand.calcMultiCov(refStand.getTrees(), refStand.getLayerSets(),
				refStand.getPlot(), this
				.getHeight() + 0.01, this.getPolygon2(), 
				m.particleNames); // fc-2.2.2015 particlaNames
		
		double treeCoverPerc = param[2];

		// current cover
		double shrubCover = 0d;
		double herbCover = 0d;
		for (FiLayer layer : simplifiedLayers) {
			if (layer.getLayerType().equals(FmLayer.SHRUB)) {
				shrubCover = layer.getCoverFraction();
			}
			if (layer.getLayerType().equals(FmLayer.HERB)) {
				herbCover = layer.getCoverFraction();
			}
		}

		for (FmLayer layer : simplifiedLayers) {
			if (!(layer.getLayerType().equals(FmLayer.SHRUB) || layer
					.getLayerType().equals(FmLayer.HERB))) {
				throw new Exception(
				"FmLayerSet.growthFrom: impossible to compute evolution because layer species name is not of type SHRUB or HERB (should probably be simplified first");
			}
			FmLayer grownLayer = layer.processGrowth(age, fertility,
					lastClearingType, treatmentEffect, herbCover, shrubCover,
					treeCoverPerc, layer.getLayerType());
			//newLayerSet.addLayer(layer);
		    newLayerSet.addLayer(grownLayer);
		}
		return newLayerSet;
	}
	
	public FmLayerSet processGrowth(FmStand refStand, int age) throws Exception {
		FmLayerSet ls = this.processGrowth(refStand);
		System.out.println("it=" + 0 + " age=" + ls.getAge()+ " load=" + ls.getInternalLoad());
		for (int i=1; i<age; i++) {
			ls = ls.processGrowth(refStand);
			System.out.println("it=" + i + " age=" + ls.getAge()+ " load=" + ls.getInternalLoad());
		}
		return ls;
	}

	// to get and set properties for evolution
	public int getAge() {
		return age;
	}
	
	public double getFertility() {
		return fertility;
	}
	public int getLastClearingType() {
		return lastClearingType;
	}
	public double getTreatmentEffect() {
		return treatmentEffect;
	}
	public void setAge(int value) {
		this.age = value;
	}
	public void setFertility(double value) {
		this.fertility = value;
	}
	
	public void setLastClearingType(int value) {
		this.lastClearingType = value;
	}
	public void setTreatmentEffect(double value) {
		this.treatmentEffect = value;
	}

}
