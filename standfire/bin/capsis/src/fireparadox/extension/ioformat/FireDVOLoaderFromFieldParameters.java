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

package fireparadox.extension.ioformat;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiPlantPopulationGenerator;
import capsis.lib.fire.fuelitem.FiSeverity;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.SpeciesDependentCVSDBH;
import capsis.util.StandRecordSet;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSet;
import fireparadox.model.layerSet.FmLocalLayerSetModels;
import fireparadox.model.plant.FmPlant;
import fireparadox.model.plant.FmPlantPopulationGenerator;

/**
 * FireDVOLoaderFromFieldParameters contains records description for all plots in ICFME
 * experiment But can probably be used for a lot of field data!
 * 
 * -Tree collection of a given species in a given polygon, in a given diameter
 * class, with a stemdensity in polygon -LayerSet and layers in polygon can be
 * added with their properties
 * 
 * - and also individual trees
 * 
 * @author F. Pimont - october 2009
 */
public class FireDVOLoaderFromFieldParameters extends StandRecordSet {


	// this map contains the different layerSet and are filled during the record
	// procedure
	// the same kind of thing should be done for the trees so that there
	// position is set when all the stand is defined
	Map<Integer, FmLayerSet> layerSetMap; // <layerSetId,FmLayerSet>
	Map<Polygon,List<FmPlant>> treePolyMap; // map that make the link
	// between a polygon and the
	// trees attached to it
	//int maxId = 0; // fc4.0
	FmPlot plot = null;

	static {
		Translator.addBundle ("capsis.extension.ioformat.FireDVOLoaderICFME");
	}

	// Generic keyword record is described in superclass: key = value

	// Fire Paradox Terrain record is described here - a Rectangle with an altitude
	@Import
	static public class TerrainRecord extends Record {
		public TerrainRecord () {super ();}
		public TerrainRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public String name;
		public double cellWidth;
		public double altitude;
		public double xMin; //m
		public double yMin;
		public double xMax;
		public double yMax;
	}

	// Fire Paradox PolygonRecord record is described here
	// fc - 8.4.2008
	@Import
	static public class PolygonRecord extends Record {
		public PolygonRecord () {super ();}
		public PolygonRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public int id;
		public Collection vertices;
	}


	// Tree record is described here
	@Import
	static public class TreeRecord extends Record {
		public TreeRecord () {super ();}
		public TreeRecord (String line) throws Exception {super (line);}
		//public String getSeparator () {return ";";}	// to change default "\t" separator
		public int fileId;
		public String speciesName;	// ex: Pinus halepensis
		public double x;
		public double y;
		public double z;
		//~ public String dbFuelId;
		public double dbh;
		public double height;
		public double crownBaseHeight;
		public double crownDiameter;
		//public double crownDiameterHeight;
		// public double meanBulkDensity;

		public double liveNeedleMoistureContent; // %
		public double liveTwigMoistureContent; // %
		public double deadTwigMoistureContent; // %

		//public boolean openess; //unused
	}

	// Tree with fire damage record is described here (cf trou du ras data)
	@Import
	static public class TreeWithDamageRecord extends Record {
		public TreeWithDamageRecord() {
			super();
		}

		public TreeWithDamageRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public int fileId;
		public String speciesName; // ex: Pinus halepensis
		public double x;
		public double y;
		public double z;
		// ~ public String dbFuelId;
		public double dbh;
		public double height;
		public double crownBaseHeight;
		public double crownDiameter;
		// public double crownDiameterHeight;

		public double minBoleLengthCharred; // m
		public double maxBoleLengthCharred; // m
		public double meanBoleLengthCharred; // m

		// Bark Damage : BCN +cambiumIsKilled, etc.
		// BCN
		// 0 : no damage
		// 1 :light, when bark is not completely blackened
		// 2 :moderate, when bark is uniformly black
		// 3 :deep, when bark is deeply charred, with the surface
		// characteristics of the bark no more discernible.mortality.

		// public double meanBarkCharNote = 0d; // 0-3
		// private double minBarkCharNote = -1; // 0-3
		// public double maxBarkCharNote = 0d; // 0-3
		// public boolean cambiumIsKilled = false;

		// canopy damage
		public double crownScorchHeight; // m
		public double crownScorchVolume; // %
		public int status; //1: alive 0: dead -1 unknown

	}


	// ICFME treeGroup
	@Import
	static public class TreeGroupRecord extends Record {
		public TreeGroupRecord () {super ();}
		public TreeGroupRecord (String line) throws Exception {super (line);}
		public int fileId;
		public String speciesName;	// ex: Pinus halepensis
		public int polygonId; // polygon Id where they will be planted
		public double lowerBoundDBH; // lowed bound of the dbh class
		public double upperBoundDBH; // upper bound of the dbh class
		public double groupAge; // used only to build a few species like pinus pinaster for the moment
		public double stemDensity; // stem/ha
		public double liveNeedleMoistureContent; // %
		public double liveTwigMoistureContent; // %
		public double deadTwigMoistureContent; // %
	}

	// phDreyfus treeGroup record
	@Import
	static public class TreeGroupRecord2 extends Record {
		public TreeGroupRecord2() {
			super();
		}

		public TreeGroupRecord2(String line) throws Exception {
			super(line);
		}

		public int fileId;
		public String speciesName; // ex: Pinus halepensis
		public int polygonId; // polygon Id where they will be planted
		public double hDom50; // dominant height at 50 yo
		public int ageTot; // age of the stand
		public double stemDensity; // stem/ha
		public double gHaETFactor; // number of gHaET factor (for basal area
		public double gibbs; // parameter for none random distribution
		// computation from tree space index
		//public int reduceCompetN;// reduce competition by swapping N trees
		public double liveNeedleMoistureContent; // %
		public double liveTwigMoistureContent; // %
		public double deadTwigMoistureContent; // %
	}

	// layerSet properties (required for evolution and intervention)
	@Import
	static public class LayerSetPropertyRecord extends Record {
		public LayerSetPropertyRecord () {super();}
		public LayerSetPropertyRecord (String line) throws Exception {super (line);}
		public int layerSetId;
		public int age; // year after clearing
		public double fertility; // between 0 and 3
		public int lastClearingType; // fire, precribed burning, clearing
		public double treatmentEffect; // between 0 and 1
	}


	// added by fp june 2010 :
	// The model uses the relationship FiLocalLayerEvolution from and parameters
	// for LayerSetPropertyRecord
	// to build the layerSet (with a layer of SHRUB
	// and one of HERB)
	@Import
	static public class LayerSetGrowthRecord extends Record {
		public LayerSetGrowthRecord() {
			super();
		}

		public LayerSetGrowthRecord(String line) throws Exception {
			super(line);
		}

		public int layerSetId; 
		public double shrubCover; // %
		public double shrubCharacteristicSize; // p atch size (m)
		public double shrubLiveFraction; // between 0 and 1

		public double herbCover; // %
		public double herbCharacteristicSize; // p atch size (m)
		// public double herbLiveFraction; // set to 0 by default


		public double aliveMoistureContent; // %
		public double deadMoistureContent; // %


	}


	// LayerSet Model
	// this record use a FiLocalLayerSetModels defined in a text file (see class
	// FiLocalLayerSetModels)
	// the model name is layerSetName and should be present in the
	// FiLocalLayerSetModels
	@Import
	static public class LayerSetModelRecord extends Record {
		public LayerSetModelRecord() {
			super();
		}

		public LayerSetModelRecord(String line) throws Exception {
			super(line);
		}
		public int fileId;
		public int polygonId; // polygon number (layerSet)
		public String layerSetName; // ex: Q coccifera Garrigue

	}
	/**
	 * Record to make the link between a layerSet and a polygon
	 * @author pimont
	 *
	 */
	@Import
	static public class LayerSetRecord extends Record {
		public LayerSetRecord() {
			super();
		}

		public LayerSetRecord(String line) throws Exception {
			super(line);
		}
		public int layerSetId;
		public int polygonId; // polygon number (layerSet)
		public String colorRGB; // color of the layerSet (if -1,-1,-1 would be compute from waterLoad)
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
		public int layerSetId;
		public String speciesName;	// ex: Pinus halepensis
		public double height; // height of the layer in polygon
		public double baseHeight; // cbh (m)
		public double coverFraction; // internal cover fraction (%) in the layerSet
		public double characteristicSize; //p atch size  (m)
		public int spatialGroup; // group of species that are in exclusion
		public double aliveMoistureContent; // %
		public double deadMoistureContent; // %
		public double aliveBulkDensity; //kg/m3
		public double deadBulkDensity; //kg/m3
		public double mvr; // mass to volume ratio kg/m3
		public double svr; // surface to volume ratio m2/m3
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public FireDVOLoaderFromFieldParameters () {}


	/**
	 * Direct constructor
	 */
	public FireDVOLoaderFromFieldParameters(String fileName) throws Exception {
		layerSetMap = new HashMap<Integer, FmLayerSet>();
		treePolyMap = new HashMap<Polygon, List<FmPlant>>();
		createRecordSet(fileName);
	} // for direct use for Import

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FmModel)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FireDVOLoader.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	@Override
	public void createRecordSet (String fileName) throws Exception {super.createRecordSet (fileName);}


	/**
	 * RecordSet -> FiStand
	 * Implementation here.
	 * Was initialy described in FiModel.loadInitStand ()
	 * To load a stand for another model, recognize real type of model :
	 * if (model instanceof FiModel) -> this code
	 * if (model instanceof MobyDickModel) -> other code...
	 */
	@Override
	public GScene load (GModel model) throws Exception {
		System.out.println ("beginning load in ICFME loader...");

		FmModel m = (FmModel) model;
		FmInitialParameters settings = m.getSettings ();
		FmStand initStand = new FmStand (m);

		int speciesId = 1;


		int treeGroupNumber=0;
		int layerGroupNumber=0;

		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			
			Record record = (Record) i.next ();

			// fc - 14.5.2007
			if (record instanceof FireDVOLoaderFromFieldParameters.TerrainRecord) {
				FireDVOLoaderFromFieldParameters.TerrainRecord r = (FireDVOLoaderFromFieldParameters.TerrainRecord) record;	// cast to precise type
				double xdim = r.xMax - r.xMin;
				double ydim = r.yMax - r.yMin;
				initStand = (FmStand) m.loadFromScratch(r.xMin, r.yMin,
						r.altitude, xdim, ydim, r.cellWidth, source);
				
				plot = initStand.getPlot();

			} else if (record instanceof FireDVOLoaderFromFieldParameters.PolygonRecord) {
				if (plot == null) {
					throw new Exception ("Can not process a Polygon record before a Terrain record");}
				FireDVOLoaderFromFieldParameters.PolygonRecord r = (FireDVOLoaderFromFieldParameters.PolygonRecord) record;
				initStand = (FmStand) m.addPolygonTo(r.id, r.vertices,
						initStand);

			} else if (record instanceof FireDVOLoaderFromFieldParameters.TreeRecord) {
				FireDVOLoaderFromFieldParameters.TreeRecord r = (FireDVOLoaderFromFieldParameters.TreeRecord) record;	// cast to precise type
				if (r.fileId > initStand.maxId) {initStand.maxId = r.fileId;}

				FiSpecies s = m.getSpecies (r.speciesName);	// il 16/09/09
				int age = -1;
				FmPlant tree = new FmPlant (r.fileId,
						initStand, 
						m, // fc-2.2.2015
						age,
						r.x,
						r.y,
						r.z,
						""+r.fileId,
						r.dbh, // dbh
						r.height,
						r.crownBaseHeight,
						r.crownDiameter,
						s,			// species
						0, 			// pop = 0 PhD 2008-09-25
						r.liveNeedleMoistureContent, 
						r.deadTwigMoistureContent, 
						r.liveTwigMoistureContent,false); 

				// adds tree in stand
				initStand.addTree (tree);


			} else if (record instanceof FireDVOLoaderFromFieldParameters.TreeWithDamageRecord) {
				FireDVOLoaderFromFieldParameters.TreeWithDamageRecord r = (FireDVOLoaderFromFieldParameters.TreeWithDamageRecord) record; // cast
				// to
				// precise
				// type
				if (r.fileId > initStand.maxId) {
					initStand.maxId = r.fileId;
				}
				// maxId++;
				FiSpecies s = m.getSpecies(r.speciesName); // il 16/09/09

				int age = -1;
				//double crownDiameterHeight = 0d; // unknown
				FmPlant tree = new FmPlant(r.fileId, initStand, 
						m, // fc-2.2.2015
						age, r.x, r.y,
						r.z, "" + r.fileId,
						r.dbh, // dbh
						r.height, r.crownBaseHeight, r.crownDiameter,
						 s, // species
						0, // pop = 0 PhD 2008-09-25
						0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d,false); // liveTwigMoisture=0

				FiSeverity sev = tree.getSeverity();
				sev.setMeanBoleLengthCharred(100d * r.meanBoleLengthCharred
						/ r.height);
				sev.setMinBoleLengthCharred(100d * r.minBoleLengthCharred
						/ r.height);
				sev.setMaxBoleLengthCharred(100d * r.maxBoleLengthCharred
						/ r.height);
				// canopy damage
				sev.setCrownScorchHeight(r.crownScorchHeight);
				sev.setCrownVolumeScorched(r.crownScorchVolume);
				sev.computeCrownLengthScorched(r.height, r.crownBaseHeight);
				sev.setCrownKillHeight(r.crownScorchHeight);
				sev.setCrownVolumeKilled(r.crownScorchHeight);
				sev.computeCrownLengthKilled(r.height, r.crownBaseHeight);
                
				SpeciesDependentCVSDBH mortality = new SpeciesDependentCVSDBH();
				// GenericCVSDBH mortality = new GenericCVSDBH();
				double mortaProba = mortality.getMortalityProbability(sev,
						// FiModel.PINUS_PINEA, r.dbh);
						r.speciesName, r.dbh);
				sev.setMortalityProbability(mortaProba);
				if (r.status < -1) {
					sev.setIsKilled(mortaProba >= 0.5);
				} else {
					sev.setIsKilled(r.status == 0);
				}

				// /

				initStand.addTree(tree);

			} else if (record instanceof FireDVOLoaderFromFieldParameters.TreeGroupRecord) {
				treeGroupNumber++;
				StatusDispatcher.print (Translator.swap ("FireDVOLoaderFromFieldParameters.buildingTreeGroupNumber")
						+" "+treeGroupNumber);
				FireDVOLoaderFromFieldParameters.TreeGroupRecord r = (FireDVOLoaderFromFieldParameters.TreeGroupRecord) record;
				System.out.println ("building tree group number" + " " + treeGroupNumber + ". Species is "
						+ r.speciesName);

				FiSpecies s = m.getSpecies(r.speciesName);
				// / CONVERTING SMALL TREES IN LAYERSET IN ICFME TO AVOID TO
				// MANY PICEA MARIANA
				boolean convertToLayer = false;
				String speciesName = r.speciesName;
				convertToLayer = (speciesName.equals(FmModel.PICEA_MARIANA) || r.speciesName
						.equals(FmModel.PICEA_MARIANA_DEAD))
						&& (r.upperBoundDBH <= 0d) && (r.stemDensity > 0d);
				if (convertToLayer) {
					System.out.println("	TreeGroupConvertedToLayer");
					double height = 0.5 * 1.3d;
					double bottomHeight = 0d;
					double characteristicSize = 0.5;
					double surface = Math.PI * 0.25d * characteristicSize
					* characteristicSize;
					double coverFraction = 1. * Math.min (1.0, 
							r.stemDensity
							* 0.0001 * surface);
					// biomass computation
					double DGLOB = 1.4d; // diameter at ground level
					// ICFME <1.3m
					double a = 0.01534;
					double b = 2.3069;
					double liveLeafBiomass = a * Math.pow(DGLOB, b);
					// live twigs
					a = 0.01202;
					b = 2.09296;
					double liveTwigBiomass = a * Math.pow(DGLOB, b);
					a = 0.00673;
					b = 1.96155;
					double liveTwig2Biomass = a * Math.pow(DGLOB, b);
					// dead twigs
					a = 0.00588;
					b = 1.99293;
					double deadTwigBiomass = a * Math.pow(DGLOB, b);
					a = 4.39E-7;
					b = 5.67014;
					double deadTwig2Biomass = a * Math.pow(DGLOB, b);
					double[] liveBulkDensity = new double[3];
					if (speciesName.equals(FmModel.PICEA_MARIANA)) {
						liveBulkDensity[0] = liveLeafBiomass / (surface * height);
						liveBulkDensity[1] = liveTwigBiomass / (surface * height);
						liveBulkDensity[2] = liveTwig2Biomass / (surface * height);
					} else { //DEAD
						liveBulkDensity[0] = 0d;
						liveBulkDensity[1] = 0d;
						liveBulkDensity[2] = 0d;
					}
					double[] deadBulkDensity = new double[3];
					if (speciesName.equals(FmModel.PICEA_MARIANA)) {
						deadBulkDensity[0] = 0d;
						deadBulkDensity[1] = deadTwigBiomass / (surface * height);
						deadBulkDensity[2] = deadTwig2Biomass / (surface * height);
					} else { // DEAD: no model for dead, so using live twig+dead twig of live
						deadBulkDensity[0] = 0d;
						deadBulkDensity[1] = (liveTwigBiomass+deadTwigBiomass) / (surface * height);
						deadBulkDensity[2] = (liveTwig2Biomass+deadTwig2Biomass) / (surface * height);
					}
					double[] mvr = new double[3];
					double[] svr = new double[3];
					
					if (speciesName.equals(FmModel.PICEA_MARIANA)) {
						mvr[0] = s.getMVR (FmModel.LEAVE_LIVE);
						mvr[1] = s.getMVR (FmModel.TWIG1_LIVE);
						mvr[2] = s.getMVR (FmModel.TWIG2_LIVE);
						svr[0] = s.getSVR (FmModel.LEAVE_LIVE);
						svr[1] = s.getSVR (FmModel.TWIG1_LIVE);
						svr[2] = s.getSVR (FmModel.TWIG2_LIVE);
					} else { //dead picea mariana
						mvr[0] = s.getMVR (FmModel.LEAVE_DEAD);
						mvr[1] = s.getMVR (FmModel.TWIG1_DEAD);
						mvr[2] = s.getMVR (FmModel.TWIG2_DEAD);
						svr[0] = s.getSVR (FmModel.LEAVE_DEAD);
						svr[1] = s.getSVR (FmModel.TWIG1_DEAD);
						svr[2] = s.getSVR (FmModel.TWIG2_DEAD);
					}

					int spatialGroup = 0;
					buildLayer(r.polygonId, speciesName, height, bottomHeight,
							liveBulkDensity, deadBulkDensity, mvr,
							svr,
							characteristicSize, coverFraction, spatialGroup,
							r.liveNeedleMoistureContent,
							r.deadTwigMoistureContent,
							r.liveTwigMoistureContent, initStand);

				} else {
					//NORMAL TREE GROUP
					double crownInPolygon = 0d;
					Polygon currentPoly = getPolygon(r.polygonId);
					// at this stage, the tree group is just build and added to the treePolyMap. Spatialization and add to the stand is done later
					List <FmPlant> treeGroup=initStand.buildTreeGroup( s,
							currentPoly, r.lowerBoundDBH, r.upperBoundDBH,
							r.stemDensity, r.groupAge,
							r.liveNeedleMoistureContent,
							r.deadTwigMoistureContent,
							r.liveTwigMoistureContent, crownInPolygon);
					if (treePolyMap.containsKey(currentPoly)) {
						List <FmPlant> tempTreeList = treePolyMap.get(currentPoly);
						tempTreeList.addAll(treeGroup);
					} else {
						//System.out.println ("The Polygon contains " + treeGroup.size ());
						treePolyMap.put(currentPoly, treeGroup);
					}

				}
			} else if (record instanceof FireDVOLoaderFromFieldParameters.TreeGroupRecord2) {
				FireDVOLoaderFromFieldParameters.TreeGroupRecord2 r = (FireDVOLoaderFromFieldParameters.TreeGroupRecord2) record; // cast
				Polygon currentPoly = this.getPolygon(r.polygonId);
				initStand.addTreeGroup2(r.speciesName,
						currentPoly, r.hDom50, r.ageTot, r.stemDensity,
						r.gHaETFactor, FiPlantPopulationGenerator.HARDCORE, r.gibbs,
						r.liveNeedleMoistureContent,
						r.deadTwigMoistureContent, r.liveTwigMoistureContent);
			} else if (record instanceof FireDVOLoaderFromFieldParameters.LayerSetRecord) {
				FireDVOLoaderFromFieldParameters.LayerSetRecord r = (FireDVOLoaderFromFieldParameters.LayerSetRecord) record; 
				if (layerSetMap.containsKey(r.layerSetId)) {
					throw new Exception ("LayerSetId "+r.layerSetId+" has been defined twice (in LayerSetRecords)");
				} else {
					initStand.maxId++;
					Color lsColor=null;
					boolean computeColor = false;
					try {
						lsColor = decodeColor(r.colorRGB);
					} catch (Exception e) {
						//lsColor = Color.BLACK; // does not matter because will be computed
						computeColor = true;
					}
					FmLayerSet layerSet = new FmLayerSet(initStand.maxId);
					if (!computeColor) layerSet.setColor(lsColor);
					Collection<Polygon> polys = plot.getPolygons();
					boolean findPoly = false;
					for (Polygon poly : polys) {
						// System.out.println("polygon : "+poly.getItemId());
						if (r.polygonId == poly.getItemId()) {
							layerSet.updateToMatch(poly);
							findPoly = true;
						}
					}
					if (!findPoly) {
						throw new Exception ("LayerSetId "+r.layerSetId+" can not be attached to polygon "+r.polygonId+" (unknown polygon)");
					}
					layerSetMap.put(r.layerSetId,layerSet);
				}
			} else if (record instanceof FireDVOLoaderFromFieldParameters.LayerSetPropertyRecord) {
				FireDVOLoaderFromFieldParameters.LayerSetPropertyRecord r = (FireDVOLoaderFromFieldParameters.LayerSetPropertyRecord) record;	// cast to precise type 
				if(!(layerSetMap.containsKey(r.layerSetId))) {
					throw new Exception ("LayerSetId "+r.layerSetId+" used in LayerSetPropertyRecord is not defined");
				}
				FmLayerSet layerSet = layerSetMap.get(r.layerSetId);
				layerSet.setAge(r.age);
				layerSet.setFertility(r.fertility);
				layerSet.setLastClearingType(r.lastClearingType);
				layerSet.setTreatmentEffect(r.treatmentEffect);

			} else if (record instanceof FireDVOLoaderFromFieldParameters.LayerSetGrowthRecord) {
				FireDVOLoaderFromFieldParameters.LayerSetGrowthRecord r = (FireDVOLoaderFromFieldParameters.LayerSetGrowthRecord) record;
				if (!(layerSetMap.containsKey(r.layerSetId))) {
					throw new Exception ("LayerSetId "+r.layerSetId+" used in LayerSetGrowthRecord is not defined");
				}
				FmLayerSet layerSet = layerSetMap.get(r.layerSetId);
				int age = layerSet.getAge();
				double fertility = layerSet.getFertility();
				int lastClearingType = layerSet.getLastClearingType();
				double treatmentEffect = layerSet.getTreatmentEffect();
				// threshold is computed from crown base height of trees with a min of 2m
				double threshold = 2d;
				for (Iterator it = initStand.getTrees().iterator(); it.hasNext();) {
					FiPlant tt = (FiPlant) it.next();
					threshold = Math.min(threshold, tt.getCrownBaseHeight());
				}
				double[] param = FmStand.calcMultiCov(
						initStand.getTrees(), null, initStand.getPlot(),
						threshold, layerSet.getPolygon2(), m.particleNames); // fc-2.2.2015 particleNames
				
				double treeCoverPerc = param[2];
				//System.out.println("TREE COVER=" + treeCover);
				double bottomHeight = 0d;
				// SHRUB
				String speciesName = FmLayer.SHRUB;
				double coverFraction = r.shrubCover;
				double liveFraction = r.shrubLiveFraction;
				double characteristicSize = r.shrubCharacteristicSize;
				double mvr[] = new double[3];
				double svr[] = new double[3];
				mvr[0] = 500d;
				mvr[1] = 500d;
				mvr[2] = 500d;
				svr[0] = 5000d;
				svr[1] = 2000d;
				svr[2] = 500d;
				int spatialGroup = 0;

				double height = FmLayer.height(age, fertility,
						lastClearingType, treatmentEffect, treeCoverPerc,
						speciesName);
				double[] thinBiomass = FmLayer.thinBiomassLoad(
						age,
						fertility, lastClearingType, treatmentEffect,
						r.herbCover, r.shrubCover, treeCoverPerc, speciesName);
				// System.out.println(age + " " + fertility + " "
				// + lastClearingType + " " + treatmentEffect + " "
				// + r.herbCover + " " + r.shrubCover + " " + treeCover
				// + " " + speciesName);
				System.out.println("SHRUB BIOMASS="
						+ (thinBiomass[0] + thinBiomass[1] + thinBiomass[2]));
				double[] liveBulkDensity = new double[3];
				double[] deadBulkDensity = new double[3];

				for (int j = 0; j < 3; j++) {
					double bulkDensity = thinBiomass[j]
					                                 / (height * coverFraction);

					liveBulkDensity[j] = bulkDensity * liveFraction;
					deadBulkDensity[j] = bulkDensity * (1d - liveFraction);
				}

				buildLayer(r.layerSetId, speciesName, height, bottomHeight,
						liveBulkDensity, deadBulkDensity, mvr, svr,
						characteristicSize, coverFraction, spatialGroup,
						r.aliveMoistureContent, r.deadMoistureContent,
						r.aliveMoistureContent, initStand);


				// HERBS
				speciesName = FmLayer.HERB;
				coverFraction = r.herbCover;
				liveFraction = 0d;
				characteristicSize = r.herbCharacteristicSize;
				double[] liveBulkDensityh = new double[1];
				double[] deadBulkDensityh = new double[1];

				double mvrh[] = new double[1];
				double svrh[] = new double[1];
				mvrh[0] = 500;
				svrh[0] = 6000d;
				spatialGroup = 1;

				height = FmLayer.height(age, fertility,
						lastClearingType, treatmentEffect, treeCoverPerc,
						speciesName);
				thinBiomass = FmLayer.thinBiomassLoad(age,
						fertility, lastClearingType, treatmentEffect,
						r.herbCover, r.shrubCover, treeCoverPerc, speciesName);
				double bulkDensity = thinBiomass[0]
				                                 / (height * coverFraction);
				System.out.println("HERBS BIOMASS=" + thinBiomass[0]+";COVERFRACTION="+coverFraction);
				liveBulkDensityh[0] = bulkDensity * liveFraction;
				deadBulkDensityh[0] = bulkDensity * (1d - liveFraction);

				buildLayer(r.layerSetId, speciesName, height, bottomHeight,
						liveBulkDensityh, deadBulkDensityh, mvrh, svrh,
						characteristicSize, coverFraction, spatialGroup,
						r.aliveMoistureContent, r.deadMoistureContent,
						r.aliveMoistureContent, initStand);


			} else if (record instanceof FireDVOLoaderFromFieldParameters.LayerSetModelRecord) {
				FireDVOLoaderFromFieldParameters.LayerSetModelRecord r = (FireDVOLoaderFromFieldParameters.LayerSetModelRecord) record; 
				FmLocalLayerSetModels propertyLayerSetMap = m.localLayerSetModels;
				String layerSetName = r.layerSetName;
				int n = propertyLayerSetMap.getLayerNumber(layerSetName);

				for (int il = 0; il < n; il++) {
					String speciesName = propertyLayerSetMap.getLayerName(
							layerSetName, il);
					int spatialGroup = propertyLayerSetMap.getSpatialGroup(
							layerSetName, il);
					double height = propertyLayerSetMap.getLayerHeight(
							layerSetName, il);
					double bottomHeight = 0d;
					double coverFraction = propertyLayerSetMap.getCoverFraction (
							layerSetName, il);
					double characteristicSize = propertyLayerSetMap
					.getCharacteristicSize(layerSetName, il);
					double aliveMoisture = propertyLayerSetMap
					.getAliveMoisture(layerSetName, il);
					double deadMoisture = propertyLayerSetMap.getDeadMoisture(
							layerSetName, il);
					double liveBulkDensity[] = new double[1];
					liveBulkDensity[0] = propertyLayerSetMap
					.getLiveBulkDensity(layerSetName, il);
					double[] deadBulkDensity = new double[1];
					deadBulkDensity[0] = propertyLayerSetMap
					.getDeadBulkDensity(layerSetName, il);
					double[] svr = new double[1];
					svr[0] = propertyLayerSetMap.getSvr(layerSetName, il);
					double[] mvr = new double[1];
					mvr[0] = propertyLayerSetMap.getMvr(layerSetName, il);
					buildLayer(r.polygonId, speciesName, height, bottomHeight,
							liveBulkDensity, deadBulkDensity, mvr,
							svr, characteristicSize, coverFraction, spatialGroup,
							aliveMoisture, deadMoisture, aliveMoisture,
							initStand);
				}

			} else if (record instanceof FireDVOLoaderFromFieldParameters.LayerRecord) {
				FireDVOLoaderFromFieldParameters.LayerRecord r = (FireDVOLoaderFromFieldParameters.LayerRecord) record;	// cast to precise type
				if (!layerSetMap.containsKey (r.layerSetId)) {
					throw new Exception ("LayerSetId "+r.layerSetId+" has not been attached to a polygon");
				} 
				FmLayerSet layerSet = layerSetMap.get (r.layerSetId);
				layerGroupNumber++;
				System.out.println ("building layer group number"+" "+layerGroupNumber);
				StatusDispatcher.print (Translator.swap ("FireDVOLoaderFromFieldParameters.buildingLayerGroupNumber")
						+" "+layerGroupNumber);
				String speciesName = r.speciesName;
				double height = r.height;
				double baseHeight = r.baseHeight;
				double[] aliveBulkDensity = new double[1];
				aliveBulkDensity[0] = r.aliveBulkDensity;
				double[] deadBulkDensity = new double[1];
				deadBulkDensity[0] = r.deadBulkDensity;
				double[] mvr = new double[1];
				mvr[0] = r.mvr;
				double[] svr = new double[1];
				svr[0] = r.svr;
				
				FiSpecies defaultSpecies = initStand.getModel ().getSpecies (FiSpecies.DEFAULT);
				// System.out.println("default speciesName:"+defaultSpecies.getName ());
				FmLayer layer = new FmLayer(speciesName, height,
						baseHeight, r.coverFraction, 
						r.characteristicSize,
						r.spatialGroup, 
						r.aliveMoistureContent, 
						r.deadMoistureContent,r.aliveMoistureContent, //aliveMoisture is used for twigs too
						aliveBulkDensity,
						deadBulkDensity, mvr, svr, defaultSpecies);
				layerSet.addLayer(layer);
			} else if (record instanceof FireDVOLoader.KeyRecord) {
				FireDVOLoader.KeyRecord r = (FireDVOLoader.KeyRecord) record;	// cast to precise type

				// DEPRECATED, replaced by Terrain record (see upper) - fc - 14.5.2007
				//~ if (r.key.equals ("maxX")) {
				//~ standWidth = r.getIntValue ();
				//~ } else if (r.key.equals ("maxY")) {
				//~ standHeight = r.getIntValue ();
				//~ } else {
				//~ throw new Exception ("Unrecognized key: "+record);
				//~ }

				//System.out.println ("record="+r);		// Automatic toString ()

			} else {
				throw new Exception ("Unrecognized record: "+record);	// automatic toString () (or null)
			}
			//System.out.println("	record "+record.toString ()+" exported zone:"+initStand.getOrigin().x+","+initStand.getOrigin().y+","+initStand.getXSize ()+","+initStand.getYSize ());

		}

		if (initStand.getPlot () == null) {
			throw new Exception ("missing Terrain in file, could not create plot; aborted");}

		// Init treeIdDispenser (to get new ids for regeneration)
		m.getTreeIdDispenser ().setCurrentValue (initStand.maxId);

		// All trees added in stand : plot creation (not for all models)
		//~ initStand.createPlot (m, 10);
		for (FmLayerSet layerSet : layerSetMap.values()) {
			initStand.addLayerSet(layerSet);
		}
		// allocate positions of trees and add trees to initStand
		System.out.println("beginning respatialization");
		for (Polygon currentPoly:initStand.getPlot().getPolygons()) {
			List<FmPlant> theseTrees = treePolyMap.get (currentPoly);
			if (theseTrees != null) {
				System.out.println ("polygon : " + currentPoly.getItemId () + "; ntrees=" + theseTrees.size ());
				FmPlantPopulationGenerator pg = new FmPlantPopulationGenerator (theseTrees, currentPoly,m.rnd);
				// gibbsparam=1 is enougth
				double gibbsParam = 1d; 
				if (theseTrees.size () < 30000) {
					pg.setSpatialDistribution (pg.HARDCORE, gibbsParam);
				} else {
					//pg.setPositionDistribution (pg.RANDOM, gibbsParam);
					pg.setSpatialDistribution (pg.GIBBS, gibbsParam);	
				}
				initStand.addTrees (theseTrees);
			}
		}
		//System.out.println("	exported zone:"+initStand.getOrigin().x+","+initStand.getOrigin().y+","+initStand.getXSize ()+","+initStand.getYSize ());
		
		return initStand;
	}



	////////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName () {return Translator.swap ("FireDVOLoaderFromFieldParameters");}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";
	private static final String LANL_GRASS = "Lanl_grass";
	private static final String LANL_LITTER = "Lanl_litter";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "F. Pimont";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("FireDVOLoaderFromFieldParameters.description");}


	////////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {return true;}
	public boolean isExport () {return true;}


	private void buildLayer(Integer polyId, String speciesName, double height,
			double baseHeight, double[] aliveBulkDensity,
			double[] deadBulkDensity, double[] mvr, double[] svr,
			double characteristicSize, double percentage, int spatialGroup,
			double liveMoisture,
			double deadMoisture, double liveTwigMoisture,
			FmStand stand) throws Exception {
		FmLayerSet layerSet;
		if (layerSetMap.containsKey(polyId)) {
			layerSet = layerSetMap.get(polyId);
		} else {
			stand.maxId++;
			layerSet = new FmLayerSet(stand.maxId);
			Collection<Polygon> polys = plot.getPolygons();
			for (Polygon poly : polys) {
				// System.out.println("polygon : "+poly.getItemId());
				if (polyId == poly.getItemId()) {
					layerSet.updateToMatch(poly);
				}
			}
			layerSetMap.put(polyId, layerSet);
		}
		double coverFraction = percentage;
		if (speciesName.equals(LANL_GRASS)
				|| speciesName.equals(LANL_LITTER)) {
			
			// fc-2.2.2015 particleNames
			FiModel m = (FiModel) stand.getStep().getProject().getModel();
			Set<String> particleNames = m.particleNames;
			
			double[] properties = FmStand.calcMultiCov(stand
					.getTrees(), stand.getLayerSets(), plot, height + 0.001,
					layerSetMap.get(polyId).getPolygon2(), particleNames);
			
			double canopyCover = properties[2] * 100d;
			coverFraction = canopyCover * (1d - Math.exp(-5d / 3d * 0.3)); // here
			// 0.3
			// should
			// be
			// CrownLenght/Hmean
			if (speciesName.equals(LANL_GRASS)) {
				coverFraction = 100d - coverFraction;
			}
		}

		// System.out.println("new layer:"+speciesName+":"+height);
		FiSpecies defaultSpecies = stand.getModel ().getSpecies (FiSpecies.DEFAULT);
		// System.out.println("default speciesName:"+defaultSpecies.getName ());
		FmLayer layer = new FmLayer(speciesName, height,
				baseHeight, coverFraction, 
				characteristicSize,
				spatialGroup, 
				liveMoisture, 
				deadMoisture, liveTwigMoisture, aliveBulkDensity,
				deadBulkDensity, mvr, svr, defaultSpecies);

		// if (!(liveTwigMoisture == -1)) {
		// layer.setLiveMoisture(0.5 * (liveTwigMoisture + liveMoisture));
		// }
		layerSet.addLayer(layer);
	}

	/**
	 * get the �lygon of Id=id (if id=-1 generate a polygon of the whole scene
	 * whole scene)
	 * 
	 * @throws Exception
	 */
	private Polygon getPolygon(int id) throws Exception {
		Vertex3d standOrigin = plot.getOrigin();
		double standWidth = plot.getXSize();
		double standHeight = plot.getYSize();


		if (id == -1) { // everywhere in the scene
			Polygon currentPoly = new Polygon();
			double minX = standOrigin.x;
			double maxX = standOrigin.x + standWidth;
			double minY = standOrigin.y;
			double maxY = standOrigin.y + standHeight;
			currentPoly.add(new Vertex3d(minX, minY, standOrigin.z));
			currentPoly.add(new Vertex3d(maxX, minY, standOrigin.z));
			currentPoly.add(new Vertex3d(maxX, maxY, standOrigin.z));
			currentPoly.add(new Vertex3d(minX, maxY, standOrigin.z));
			currentPoly.ensureTrigonometricOrder();
			currentPoly.setClosed(true);
			return currentPoly;
		} else {
			Collection<Polygon> polys = plot.getPolygons();
			boolean polyFind = false;
			for (Polygon poly : polys) {
				// System.out.println("polygon : "+poly.getItemId());
				if (id == poly.getItemId()) {
					return poly;
				}
			}

		}
		throw new Exception("wrong polygon number :" + id);
	}
	private Color decodeColor(String encodedColor) throws Exception {
		//try {
			encodedColor = encodedColor.trim();
			StringTokenizer st = new StringTokenizer(encodedColor, ",");
			int r = Integer.parseInt(st.nextToken().trim());
			int g = Integer.parseInt(st.nextToken().trim());
			int b = Integer.parseInt(st.nextToken().trim());
			return new Color(r, g, b);
		//} catch (Exception e) {
		//	throw new Exception("FireDVOLoaderFromFieldParameter: could not decode this color: " + encodedColor, e);
		//}
	}
}


