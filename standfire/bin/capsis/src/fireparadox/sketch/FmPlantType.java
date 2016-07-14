package fireparadox.sketch;

import java.awt.Color;
import java.util.Random;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.sketch.extension.ItemChooser;
import jeeb.lib.sketch.scene.item.TreeWithCrownProfileItem;
import jeeb.lib.sketch.scene.kernel.CustomType;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.database.FmPlantFromDB;
import fireparadox.model.database.FmPlantSyntheticData;
import fireparadox.model.plant.FmDendromTreeProperties;
import fireparadox.model.plant.FmLocalPlantDimension;
import fireparadox.model.plant.FmLocalTreeExternalRef;
import fireparadox.model.plant.FmPlant;

/**	FiPlantType is the type of a FiPlant Item.
*	@author F. de Coligny - december 2008
*/
public class FmPlantType extends CustomType {
	
	private FmModel model;
	private Random random;

	
	/**	Constructor.
	*/
	public FmPlantType (FmModel model) {
		// super (Translator.swap ("FiPlant.itemType"), // name
		// "fireparadox.extension.sketcher.FirePatternSketcher"); //
		// preferredSketcher
		super (Translator.swap ("FiPlant.itemType"), 			// name
				fireparadox.extension.sketcher.FirePatternSketcher.class); // preferredSketcher
//				// For tests, should return to fireparadox.extension.sketcher.FirePatternSketcher.class
//				jeeb.lib.sketch.scene.extension.sketcher.TreeWithCrownProfileSketcher.class); // preferredSketcher
		
		this.model = model;
		random = new Random ();
	}

	/**	Return a Sketch Item for the given external reference
	*/
	@Override
	public Item getItem (Object externalRef) throws Exception {
		if (externalRef instanceof fireparadox.gui.database.FmPlantFormLine) {
			return createTree ((fireparadox.gui.database.FmPlantFormLine) externalRef);

		} else if (externalRef instanceof FmLocalTreeExternalRef) {
			return createTree ((FmLocalTreeExternalRef) externalRef);

		} else {
			Log.println (Log.ERROR, "FiPlantType.getItem (Object externalRef)",
					"Unknown externalRef, wrong type: "+externalRef);
			return null;
		}
	}

	/**	Creates a tree with data coming from the data base
	*/
	private Item createTree (fireparadox.gui.database.FmPlantFormLine formLine) throws Exception {
		try {
			Color crownColor = Color.YELLOW;
			FmPlantSyntheticData data = formLine.getPlantSyntheticData ();
			FiSpecies species = model.getSpecies (data.getSpeciesName ());
			FmPlantFromDB plant = new FmPlantFromDB(data, crownColor, species, model);
			
			// Additional property: moisture
			//TODO
//			plant.setLiveMoisture(formLine.getLiveMoisture());
//			plant.setDeadMoisture(formLine.getDeadMoisture());
			
			Item item = new TreeWithCrownProfileItem (plant, this);
			return item;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantType.createTree (fireparadox.gui.FiPlantFormLine externalRef)", "Exception", e);
			throw e;
		}

	}

	/**	Creates a tree with data from outside the data base
	*/
	private Item createTree (FmLocalTreeExternalRef externalRef) throws Exception {
		try {
			int id = 0;				// id will be set in FiStand.sketchHappening ()
			FmStand stand = null;	// stand should be set in FiStand.addTree ()
			double x = 0;			// x, y, z will be set by an ItemPattern
			double y = 0;
			double z = 0;
			String fileId = "";

			// Properties from interface, needed for phD trees
			String speciesName = externalRef.getSpeciesName();
			double dominantHeight = externalRef.getDominantHeight(); // m
			int meanAge = (int) externalRef.getMeanAge(); // years
			double ageStandardDeviation = externalRef.getAgeStandardDeviation(); // years
			double stemDensity = externalRef.getStemDensity(); // stems per ha

			double liveMoisture = externalRef.getLiveMoisture();
			double deadMoisture = externalRef.getDeadMoisture();
			double liveTwigMoisture = externalRef.getLiveTwigMoisture();
			FmModel model = externalRef.getModel();
			
			// This will create the FiSpecies if not found (ok for local species)
			FiSpecies species = model.getSpecies(speciesName);
			
			//~ if (species == null)
				//~ species = new FiSpecies(1000, "", "Resineous", "Pinus",
						//~ FiInitialParameters.PINUS_PONDEROSA, 1);

			// Macroscopique Properties of the individual derived from phd's
			// models
			// to be
			// Some of these variables could be moved into the externalRef: different values for popcov and fpimont trees
			double height;
			double crownDiameter;
			double crownBaseHeight;
			// 1. Random distribution of age in the stand: other distribution than gaussian? 
			Random R = new Random();
			int age = Math.min(5,meanAge+ (int) (Math.round(R.nextGaussian()*ageStandardDeviation)));
			// 2. Distribution of diameter
			// this distribution could be improved using parameter age and ageStandardDeviation
			// with new regressions (see Ph Dreyfus)
			double dbh = FmDendromTreeProperties.computeTreeDbh(speciesName,
					dominantHeight, meanAge, stemDensity, true); // PhD
			
			// 3. Tree dimensions:
			if (dbh > 0.0) {
				// basal area and plot mean height
				double meanDbh = FmDendromTreeProperties.computeTreeDbh(
						speciesName, dominantHeight, meanAge, stemDensity,
						false);
				double bA = stemDensity * (meanDbh * 0.01 / 2.0)
						* (meanDbh * 0.01 / 2.0) * 3.14;
				double plotMeanHeight = FmLocalPlantDimension
						.computeTreeHeight(species, meanDbh, meanAge);

				// treeHeight
				height = FmLocalPlantDimension.computeTreeHeight(species,
						dbh, meanAge);

				// tree diameter
				crownDiameter = FmLocalPlantDimension
						.computeCrownDiameter(species, dbh, height);

				// crown base height
				crownBaseHeight = FmLocalPlantDimension
						.computeCrownBaseHeight(species, dbh, height,
						        age,
								plotMeanHeight, bA);
				
			} else {
				throw new Exception(
						"FiPlantType.createTree: no model available for species "
								+ speciesName);
				// DEFAULT MODEL
				// height = 0.3 * meanAge * random.nextDouble();
				// dbh = height * 3.0;
				// crownBaseHeight = height / 3d;
				// crownDiameter = crownBaseHeight;
			}
			System.out.println("FiPlant2:" + speciesName + " dbh=" + dbh
					+ " h=" + height + " cbh=" + crownBaseHeight
					+ " cDiameter=" + crownDiameter);
			
			
			
			// boolean closedEnvironment = false;
			int pop = 0; // PhD 2008-09-18

			
			
			// Create the tree
			FmPlant plant = new FmPlant (
					id,
					stand,
					model, // fc-2.2.2015
					age,
					x,
					y,
					z,
					fileId,
					dbh,
					height,
					crownBaseHeight,
					crownDiameter,
					species,
					pop,
					liveMoisture, deadMoisture, liveTwigMoisture,false);
			
			Item item = new TreeWithCrownProfileItem (plant, this);
			return item;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantType.createTree (FiLocalTreeExternalRef externalRef)", "Exception", e);
			throw e;
		}
	}

	/**	Returns true if items of this type can be selected by the given chooser.
	*/
	@Override
	public boolean accepts (ItemChooser chooser) {
		return chooser instanceof fireparadox.extension.itemchooser.FiPlantChooser
				|| chooser instanceof fireparadox.extension.itemchooser.FiLocalTreeChooser;
	}


}
