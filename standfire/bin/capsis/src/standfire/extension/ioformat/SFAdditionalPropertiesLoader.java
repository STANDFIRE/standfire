/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package standfire.extension.ioformat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import standfire.model.SFInitialParameters;
import standfire.model.SFModel;
import standfire.model.SFScene;
import standfire.model.SFTree;
import capsis.kernel.GModel;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiSpecies;

/**
 * SFAdditionalPropertiesLoader loads additional data to SVS files required to build a fuel scene
 * (tree moisture and layer additional data). This StandRecordSet has a method "add" to avoid
 * overwriting of the scene
 * 
 * @author F. Pimont, R. Parsons - June 2013
 */
public class SFAdditionalPropertiesLoader extends RecordSet {

	static {
		Translator.addBundle ("standfire.model.SFAdditionalPropertiesLoader");
	}

	@Import
	// this string record is here for option selection (such as understoreyFuelOption)
	static public class StringRecord extends Record {
		public StringRecord () {
			super ();
		}
		public StringRecord (String line) throws Exception {
			super (line);
		}
		public String getSeparator () {
			return "=";
		} // to change default "\t" separator
		public String name;
		public String value;
	}

	@Import
	static public class TreeMoistureRecord extends Record {
		public TreeMoistureRecord () {
			super ();
		}
		public TreeMoistureRecord (String line) throws Exception {
			super (line);
		}
		public String speciesName; // speciesName
		public String particleName;// (Leave / Twig1 / Twig2 / Twig3)_(LIVE / DEAD) or surface_fuel
		public double massFraction; 
		public double moisture;
	}		

	// Particle property record for SVS shrub and herb (understoreyFuelOption==0)
	@Import
	static public class SVSParticlePropertyRecord extends Record {
		public SVSParticlePropertyRecord () {
			super ();
		}
		public SVSParticlePropertyRecord (String line) throws Exception {
			super (line);
		}
		public String layerType; // SHRUB, HERB
		public String particleName;// Leave_Live...
		public double loadFraction;
		public double mvr;
		public double svr;
		public double moisture;
	}
	
	@Import
	// Layer geometry for SVS shrub and herb (understoreyFuelOption==0)
	static public class SVSLayerGeomRecord extends Record {
		public SVSLayerGeomRecord () {
			super ();
		}
		public SVSLayerGeomRecord (String line) throws Exception {
			super (line);
		}
		public String layerType; // SHRUB, HERB
		public double height; // height of the layer in m
		public double baseHeight; // base of the layer in m
		public double coverFraction; // cover fraction (%)
		public double characteristicSize; // clump size (m)
		public int spatialGroup; // group of species that are in exclusion
	}
	
	// Surface fuel record (litter or grass) for SVS (understoreyFuelOption==0)
	@Import
	static public class SVSSurfaceFuelRecord extends Record {
		public SVSSurfaceFuelRecord () {
			super ();
		}
		public SVSSurfaceFuelRecord (String line) throws Exception {
			super (line);
		}
		//public String type; // generally litter or grass
		public double height; // height of the layer in m
		public double mvr;
		public double svr;
		public double moisture;
	}

	// LayerSetRecord record is described here, used for LayerSet spatialization  (understoreyFuelOption==1)
	@Import
	static public class LayerSetRecord extends Record {
		public LayerSetRecord () {
			super ();
		}
		public LayerSetRecord (String line) throws Exception {
			super (line);
		}
		public Collection vertices;
		public Collection layerSetNumbers; //int
		public Collection surfaceFuels; // boolean
	}
		
	// Individual layers (add individual layers one by one to define the
	// layerSet)
	/**
	 * Record for the layers of LayerSets
	 */
	@Import
	static public class LayerRecord extends Record {
		public LayerRecord () {super ();}
		public LayerRecord (String line) throws Exception {super (line);}
		public int layerSetNumber;
		public String type;	// shrub, herb, quercus coccifera
		public double height; // height of the layer in polygon
		public double baseHeight; // cbh (m)
		public double coverFraction; // internal cover fraction (%) in the layerSet
		public double characteristicSize; //p atch size  (m)
		public int spatialGroup; // group of species that are in exclusion
	}
	/**
	 * Record for the particles of each layer
	 */
	@Import
	static public class ParticleRecord extends Record {
		public ParticleRecord () {super ();}
		public ParticleRecord (String line) throws Exception {super (line);}
		public String name;	// TYPE_STATUS (LEAVE, TWIG1, TWIG2, TWIG3)_(LIVE,DEAD) or SURFACE_FUEL
		public double load; // mass per surface kg/m2
		public double mvr; // mass to volume ratio kg/m3
		public double svr; // surface to volume ratio m2/m3
		public double moisture; // mc 
	}

	
	private int understoreyFuelOption = -1; // 0 for SVS data spread on the whole scene, 1 for multiple detailed layerSet definition
	private Map<String,FiSpecies> speciesMap;
	private Map<String,Double> layerLoads;

	/**
	 * Direct constructor
	 */

	public SFAdditionalPropertiesLoader (String fileName, Map<String,FiSpecies> speciesMap,
			Map<String,Double> layerLoads)
	throws Exception {
		super (); // fc - sep 2010
		this.speciesMap = speciesMap;
		this.layerLoads = layerLoads;
		createRecordSet (fileName);
	} 

	/**
	 * add the contains of the loader to the scene
	 * 
	 * @param model
	 * @param initScene
	 * @return
	 * @throws Exception
	 */
	public SFScene addLayersAndUpdateTreeMoisture (GModel model, SFScene initScene)
	throws Exception {

		SFModel m = (SFModel) model;
		SFInitialParameters settings = m.getSettings ();
		FiSpecies defaultSpecies = speciesMap.get (FiSpecies.DEFAULT);
		double sceneOriginX = settings.sceneOriginX;
		double sceneOriginY = settings.sceneOriginY;
		double sceneSizeX = settings.sceneSizeX;
		double sceneSizeY = settings.sceneSizeY;
		System.out.println ("SFAdditionalPropertiesLoader.load () : # of records : " + size ());

		// variables below  SVS understorey (underStoreyFuel==0)
		FiLayerSet svsLayerSet=null;
		FiLayerSet svsSurfaceLayerSet=null;
		boolean svsSurfaceFuelAlreadyDefined = false;
		Map<String,FiLayer> layerMap = new HashMap<String,FiLayer> (); // map to build all the layer with their properties before layerSet.setLayers()
		Map <String, Double> massFractionControl = new HashMap<String, Double>(); // key is speciesName_particuleType
		Map <String, Double> loadFractionControl = new HashMap<String, Double>(); // key is layerType

		// map of LayerSets  The index is the Polygon for understoreyFuel==1
		Map<Integer, FiLayerSet> layerSetMap = null;
		FiLayer currentLayer = null;
		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Record record = (Record) i.next ();
			//Object record = i.next ();
// UNDERSTOREY FUEL OPTION==0 (ONE LAYERSET WITH LOAD COMMING FROM SVS)
			System.out.println(record);
			if (record instanceof StringRecord) {
				System.out.println("STRINGRECORD");
				StringRecord r = (StringRecord) record;
				if (r.name.equals ("understoreyFuelOption")) {
					understoreyFuelOption = Integer.parseInt (r.value);
					if (understoreyFuelOption<0 ||understoreyFuelOption>2) {
						throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 0 or 1");
					}
				} else {
					//throw new Exception ("SFAdditionalPropertiesLoader, unknown keyword "+r.name+" line " + record);
					throw new Exception ("SFAdditionalPropertiesLoader, "+r.name+" should be surfaceFuelOption");
				}
				if (understoreyFuelOption==0) {
					// polygon for the whole scene:
					List<Vertex3d> vertices = new ArrayList<Vertex3d> ();
					Vertex3d v1 = new Vertex3d (sceneOriginX, sceneOriginY, 0d);
					Vertex3d v2 = new Vertex3d (sceneOriginX + sceneSizeX, sceneOriginY, 0d);
					Vertex3d v3 = new Vertex3d (sceneOriginX + sceneSizeX, sceneOriginY + sceneSizeY, 0d);
					Vertex3d v4 = new Vertex3d (sceneOriginX, sceneOriginY + sceneSizeY, 0d);
					vertices.add (v1);
					vertices.add (v2);
					vertices.add (v3);
					vertices.add (v4);
					Polygon p = new Polygon (vertices);
					// unique layerSet for understoreyFuelOption==0 (SVS data for understorey);
					svsLayerSet = new FiLayerSet (initScene.maxId);
					svsLayerSet.updateToMatch (p);
					initScene.maxId++;
					svsSurfaceLayerSet = new FiLayerSet (initScene.maxId);
					svsSurfaceLayerSet.updateToMatch (p);
					initScene.maxId++;
				} else if (understoreyFuelOption ==1) {
					layerSetMap = new HashMap<Integer, FiLayerSet>();
				}
			} else if (record instanceof SVSLayerGeomRecord) {
				System.out.println("SVSLAYERGEOMRECORD");
				if (understoreyFuelOption!=0) {
					throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 0 to define a SVSLayerGeom");
				}
				SVSLayerGeomRecord r = (SVSLayerGeomRecord) record;
				if (r.characteristicSize<=0 || r.coverFraction == 1d) {
					r.characteristicSize = 0d;
					r.coverFraction = 1d;
				}
				FiLayer layer = new FiLayer (r.layerType, r.height, r.baseHeight, r.coverFraction, r.characteristicSize, r.spatialGroup, defaultSpecies);
				layerMap.put (r.layerType, layer);

			} else if (record instanceof SFAdditionalPropertiesLoader.SVSSurfaceFuelRecord) {
				System.out.println("SVSSURFACEFUELRECORD");
				if (understoreyFuelOption!=0) {
					throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 0 to define a SVSSurfaceFuel");
				}
				if (svsSurfaceFuelAlreadyDefined) { throw new Exception ("SFAdditionalProperties.addLayersAndUpdateTreeMoisture: surfaceFuel can be defined only once ");
				}
				SFAdditionalPropertiesLoader.SVSSurfaceFuelRecord r = (SFAdditionalPropertiesLoader.SVSSurfaceFuelRecord) record;
				FiLayer layer = new FiLayer (FiLayer.LITTER, r.height, 0d, 1d, -1d, 0, defaultSpecies);
				FiParticle particle = new FiParticle (FiParticle.LITTER, r.mvr,r.svr,r.moisture,FiLayer.LITTER);
				double load = this.layerLoads.get (FiLayer.LITTER);
				layer.addFiLayerParticleFromLoad (load, particle);
				svsSurfaceLayerSet.addLayer (layer);
				svsSurfaceLayerSet.setSurfaceFuel (true);
				initScene.addLayerSet (svsSurfaceLayerSet);
				svsSurfaceFuelAlreadyDefined = true;
			} else if (record instanceof SFAdditionalPropertiesLoader.SVSParticlePropertyRecord) {
				System.out.println("SVSPARTICLEPROPERTYRECORD");
				if (understoreyFuelOption != 0) {
					throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 0 to define a SVSParticleProperty");
				}	SFAdditionalPropertiesLoader.SVSParticlePropertyRecord r = (SFAdditionalPropertiesLoader.SVSParticlePropertyRecord) record;
				if (!layerMap.keySet ().contains (r.layerType)) { throw new Exception (
						"SFAdditionalProperties.addLayersAndUpdateTreeMoisture: layer " + r.layerType
						+ "should be define before its particles");
				}
				FiLayer layer = layerMap.get (r.layerType);
				FiParticle particle = new FiParticle (r.particleName, r.mvr, r.svr, r.moisture,r.layerType);
				double load = this.layerLoads.get (r.layerType) * r.loadFraction;
				// control
				String controlKey = r.layerType;
				double currentLoadFractionSum = 0d;
				if (loadFractionControl.containsKey (controlKey)) {
					currentLoadFractionSum = loadFractionControl.get (controlKey);
				}
				loadFractionControl.put(controlKey,currentLoadFractionSum+r.loadFraction);
				// end control
				layer.addFiLayerParticleFromLoad (load, particle);

// UNDERSTOREY FUEL OPTION==1 (LAYERSETS WITH BULK DENSITY DEFINED HERE)
			} else if (record instanceof SFAdditionalPropertiesLoader.LayerSetRecord) {
				System.out.println("LAYERSETRECORD");
				if (understoreyFuelOption!=1) { // only for understoreyFuelOption==1
					throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 1 to define a Polygon");
				}	
				SFAdditionalPropertiesLoader.LayerSetRecord r = (SFAdditionalPropertiesLoader.LayerSetRecord) record;
				Polygon p = new Polygon(new ArrayList<Vertex3d>(AmapTools
						.toVertex3dCollection(r.vertices)));
				if (r.layerSetNumbers.size ()!=r.surfaceFuels.size ()) { 
					throw new Exception ("SFAdditionalPropertiesLoader, for Polygon "+p+" the number of layerSet is not the same as the number of surfaceFuels");
				}
				int ils = 0;
				List surfaceFuels = new ArrayList (r.surfaceFuels);
				for (Object nls:r.layerSetNumbers) {
					FiLayerSet ls = new FiLayerSet (initScene.maxId);
					ls.setSurfaceFuel (surfaceFuels.get (ils).equals("true"));
					ils += 1;
					ls.updateToMatch (p);
					initScene.maxId++;
					double inls = (Double) nls;
					layerSetMap.put ((int) inls, ls);
				}
				
			} else if (record instanceof SFAdditionalPropertiesLoader.LayerRecord) {
				System.out.println("LAYERRECORD");
				if (understoreyFuelOption!=1) { // only for understoreyFuelOption==1
					throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 1 to define a Layer");
				}
				SFAdditionalPropertiesLoader.LayerRecord r = (SFAdditionalPropertiesLoader.LayerRecord) record;
				if (!layerSetMap.containsKey (r.layerSetNumber)) {
					throw new Exception ("SFAdditionalPropertiesLoader, no polygon is defined for layerSet "+r.layerSetNumber);
				} 
				FiLayerSet ls = layerSetMap.get (r.layerSetNumber);
				currentLayer = new FiLayer (r.type, r.height, r.baseHeight, r.coverFraction, r.characteristicSize, r.spatialGroup, defaultSpecies);
				ls.addLayer (currentLayer);
				
			} else if (record instanceof SFAdditionalPropertiesLoader.ParticleRecord) {
				System.out.println("PARTICLERECORD");
				
				if (understoreyFuelOption!=1) { // only for understoreyFuelOption==1
					throw new Exception ("SFAdditionalPropertiesLoader, understoreyFuelOption should be 1 to define a Particle");
				}
				if (currentLayer==null) { // only for understoreyFuelOption==1
					throw new Exception ("SFAdditionalPropertiesLoader, a layer should be defined to define its Particles");
				}
				SFAdditionalPropertiesLoader.ParticleRecord r = (SFAdditionalPropertiesLoader.ParticleRecord) record;
				currentLayer.addFiLayerParticleFromLoad (r.load, new FiParticle (r.name,  r.mvr, r.svr, r.moisture, currentLayer.getLayerType()));
				
// TREES ADDITIONAL PROPERTIES (MOISTURE)
			} else if (record instanceof SFAdditionalPropertiesLoader.TreeMoistureRecord) {
				System.out.println("TREEMOISTURERECORD");
				SFAdditionalPropertiesLoader.TreeMoistureRecord r = (SFAdditionalPropertiesLoader.TreeMoistureRecord) record;
				if (!FiParticle.checkName (r.particleName)) {
					throw new Exception ("SFAdditionalPropertiesLoader, wrong particle name "+r.particleName+" for tree moisture record "+r);
				}
				if (!speciesMap.containsKey (r.speciesName)) {
					throw new Exception ("SFAdditionalProperties.addLayersAndUpdateTreeMoisture: " +r.speciesName +" is unknown (record "+r);
				}
			 	// additional speciesList moisture...)
				FiSpecies sp = speciesMap.get (r.speciesName);
				FiParticle particle = sp.getParticle (r.particleName);	
				particle.moisture = r.moisture;
				// NB: the tree biomass for each particle type is stored in status=LIVE by default : here we eventually update the value of each fuel class if r.massfraction of dead is > 0
				if (FiParticle.getType (r.particleName).equals (FiParticle.DEAD)) {
					if (r.massFraction < 0 || r.massFraction > 1d) {
						throw new Exception ("SFAdditionalProperties.addLayersAndUpdateTreeMoisture: massFraction should between 0 and 1 for " +r.speciesName +" and particle "+r.particleName);
					}
					String pttype_LIVE = FiParticle.makeKey (FiParticle.getType (r.particleName), FiParticle.LIVE);
					String pttype_DEAD = FiParticle.makeKey (FiParticle.getType (r.particleName), FiParticle.DEAD);
					for (SFTree tree: initScene.getTrees (sp)) {
						double massForType = tree.getBiomass(pttype_LIVE);
						tree.setBiomass (pttype_LIVE, (1d - r.massFraction) * massForType);//LIVE
						tree.setBiomass (pttype_DEAD, r.massFraction * massForType);//DEAD
					}
				}
						
				// control to check that sum of massFraction for a species and a particle status is really 1
				double currentMassFractionSum = 0d;
				String controlKey = FiParticle.makeKey (r.speciesName, FiParticle.getType (r.particleName));
				if (massFractionControl.containsKey (controlKey)) {
					currentMassFractionSum = massFractionControl.get (controlKey);
				}
				massFractionControl.put(controlKey,currentMassFractionSum+r.massFraction);
				// end control
					
			} else {
				throw new Exception ("SFAdditionalProperties, unknown line: " + record);
			}
		}
		if (understoreyFuelOption==0) { //
			// controls only for understoreyFuelOption==0

			for (String key: massFractionControl.keySet ()) {
				if (Math.abs (massFractionControl.get (key) - 1d) > 1e-5) {
					throw new Exception (
							"SFAdditionalProperties.addLayersAndUpdateParticleProperties: the sum of massFraction for "
							+ key+ " is not one, but "+massFractionControl.get (key));
				}
			}// controls
			for (String key: loadFractionControl.keySet ()) {
				if (Math.abs (loadFractionControl.get (key) - 1d) > 1e-5) {
					throw new Exception (
							"SFAdditionalProperties.addLayersAndUpdateParticleProperties: the sum of loadFraction for layer "
							+ key+ " is not one, but"+loadFractionControl.get (key));
				}
			}
			//end controls
			svsLayerSet.setLayers (layerMap.values ());
			initScene.addLayerSet (svsLayerSet);
			// NB: the svsSurfaceLayerSet has been added above
		} else {
			for (FiLayerSet layerSet : layerSetMap.values()) {
				layerSet.update ();
				initScene.addLayerSet(layerSet);
			}
		}

		return initScene;
	}
}
