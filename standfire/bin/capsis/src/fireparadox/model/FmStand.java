package fireparadox.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.defaulttype.TreeList;
import capsis.kernel.GScene;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.layerSet.FmLayerSet;
import fireparadox.model.plant.FmPlant;
import fireparadox.model.plant.FmPlantPopulationGenerator;

/**
 * FiStand : Fire Paradox stand, a collection of trees.
 * 
 * @author O. Vigy, E. Rigaud - september 2006
 */
public class FmStand extends FiStand {
	// fc - 7.9.2009 - refactored to comply with the SketchLinker framework

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	/**	This class contains immutable instance variables for a logical GMaddTree.
	 *	@see Gtree
	 */
	public static class Immutable extends TreeList.Immutable {
		//FirePatternSketcher needs this model to retrieve the pattern map
		public FmModel model;
	}

	private float canCov;
//	private Map<Integer,FiLayerSet> layerSets;	// key = layerSet id
	private boolean beetleAttacked;
	private double maxSpreadDistance; // m
	private double beetleAttack_a;
	private double beetleAttack_b;
	private double beetleAttack_c;

	/**
	 * Constructor.
	 */
	public FmStand (FmModel model) {
		super ();
		((Immutable)immutable).model = model;
		beetleAttacked = false;
	}
	
	/**
	 * Get the fmLayerSet collection.
	 */
	public Collection<FmLayerSet> getFmLayerSets () {
		Collection<FmLayerSet> c = new ArrayList<FmLayerSet> ();
		for (FiLayerSet ls : getLayerSets ()) {
			c.add ((FmLayerSet) ls);
		}
		return c;
	}


	/**
	 * Create an Immutable object whose class is declared at one level of the
	 * hierarchy. This is called only in constructor for new logical object in
	 * superclass. If an Immutable is declared in subclass, subclass must
	 * redefine this method (same body) to create an Immutable defined in
	 * subclass.
	 */
	@Override
	protected void createImmutable () {immutable = new Immutable ();}

	/**
	 * Redefines superclass default
	 */
	@Override
	public String getToolTip () {
		return "Date "+getDate ();
	}

	public FmModel getModel () {
		return ((Immutable)immutable).model;
	}

	/**
	 * From GScene Interface.
	 * 
	 * Used by evolution processes in modules. General concept is to use a base
	 * for evolution (without trees in it), which will be the new stand. Then,
	 * consider every tree in old stand, make it evolve (gives a new instance)
	 * and add it in this new stand.
	 */
	@Override
	public GScene getEvolutionBase () {

		// Trees are NOW copied (new Collection)
		FmStand copy = (FmStand) super.getHeavyClone();
		
		
		copy.canCov = 0f;

		// LayerSets are not copied (new HashMap)
		copy.layerSets = new HashMap<Integer,FiLayerSet> ();

		return copy;
	}

	/**
	 * From GScene Interface.
	 * 
	 * Interventions can occur on the object returned. Return a complete clone, containing clones of
	 * every trees and layerSets.
	 * 
	 * @throws Exception
	 */
	public GScene getInterventionBase () {
		FmStand standCopy = (FmStand) super.getInterventionBase ();

		// Take care of the layerSets
		standCopy.layerSets = null;
		for (FiLayerSet layerSet : getLayerSets ()) {
//		for (FmLayerSet layerSet : getLayerSets ()) { // fc-7.11.2014
			FmLayerSet layerSetCopy;
			try {
				layerSetCopy = (FmLayerSet) layerSet.copy (); // fc-7.11.2014 added the cast
				standCopy.addLayerSet (layerSetCopy);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
			
		}
		return standCopy;
	}

	@Override
	public FmPlot getPlot () {return (FmPlot) plot;}

	public double getCanCov () {return canCov;	}
	public void setCanCov (double x) {canCov = (float) x;}

	/**
	 * add a collection of trees to the FmStand
	 * @param theseTrees
	 */
	public void addTrees(Collection<FmPlant> theseTrees) {
		double xmin = this.getOrigin().x;
		double ymin = this.getOrigin().y;
		double xmax = xmin + this.getXSize ();
		double ymax = ymin +this.getYSize ();
		for (FmPlant tree:theseTrees) {
			double xt=tree.getX ();
			double yt=tree.getY ();
			if (xmin<=xt & xt<=xmax & ymin<=yt & yt<=ymax) {
				this.addTree(tree);
			}
		}
	}
	
	/**
	 * Build a tree group for a given class of dbh (used for icfme ...) (aleppo pine available only)
	 * @throws Exception FP added theseTrees to have the possibility to spatialize trees later
	 * 
	 */
	public List<FmPlant> buildTreeGroup(FiSpecies sp,
			Polygon polygon, double lowerBoundDBH, double upperBoundDBH,
			double stemDensity, double groupAge,
			double liveNeedleMoistureContent, double deadTwigMoistureContent,
			double liveTwigMoistureContent,
			double crownInPolygon) throws Exception {
		FmPlantPopulationGenerator pg = new FmPlantPopulationGenerator (this, maxId, sp, polygon,
				lowerBoundDBH, upperBoundDBH, stemDensity, groupAge, liveNeedleMoistureContent,
				deadTwigMoistureContent, liveTwigMoistureContent, crownInPolygon);
		return (pg.getFmPlants ());
	}
	
	
	
	/**
	 * Add a tree group with stand parameter derived from ph Dreyfus models
	 * (aleppo pine available only)
	 * 
	 * @throws Exception
	 * 
	 */
	public void addTreeGroup2(String speciesName, Polygon polygon,
			double hDom50, int ageTot, double stemDensity, double gHaETFactor,
			String spatial, double gibbs, double liveNeedleMoistureContent,
			double deadTwigMoistureContent, double liveTwigMoistureContent)
	throws Exception {
		System.out.println("building tree group of species "
				+ speciesName);
		StatusDispatcher.print(Translator
				.swap("FireDVOLoaderFromFieldParameters.buildingTreeGroupOfSpecies"
						+ speciesName));
		// generation of a FmPlant distribution of trees
		FmPlantPopulationGenerator pg = new FmPlantPopulationGenerator (this, speciesName, hDom50, ageTot, stemDensity,
				gHaETFactor, polygon, maxId, liveNeedleMoistureContent, deadTwigMoistureContent,
				liveTwigMoistureContent);
		pg.setSpatialDistribution (spatial, gibbs);
		this.addTrees (pg.getFmPlants ());
		this.maxId = pg.maxId;
	}

	/**
	 * Add a tree group with stand parameter derived from FNI data (aleppo pine
	 * available only)
	 * 
	 * @throws Exception
	 * 
	 */
	public void addTreeGroup3( String speciesName, Polygon polygon,
			double hDom, int ageTot, double stemDensity, double gHa,
			String spatial, double gibbs, double liveNeedleMoistureContent,
			double deadTwigMoistureContent, double liveTwigMoistureContent)
			throws Exception {
		System.out.println("building tree group of species " + speciesName);
		StatusDispatcher
				.print(Translator
						.swap("FireDVOLoaderFromFieldParameters.buildingTreeGroupOfSpecies"
								+ speciesName));
		// generation of a FmPlant distribution of trees
		FmPlantPopulationGenerator pg = new FmPlantPopulationGenerator (this, hDom, speciesName, ageTot, stemDensity,
				gHa, polygon, maxId, liveNeedleMoistureContent, deadTwigMoistureContent, liveTwigMoistureContent);
		pg.setSpatialDistribution (spatial, gibbs);
		this.maxId = pg.maxId;
	}
//
//	/**
//	 * Add a layerSet.
//	 */
//	public void addLayerSet (FiLayerSet layerSet) {
//		if (layerSet == null) {return;}
//		if (layerSet.getId () <= 0) {
//			Log.println (Log.ERROR, "FiStand.addLayerSet ()", "Can not add a LayerSet without id, passed");
//			return;
//		}
//		if (layerSets == null) {layerSets = new HashMap<Integer,FiLayerSet> ();}
//		layerSets.put (layerSet.getId (), layerSet);
//	}
//
//	/**
//	 * Remove a layerSet.
//	 */
//	public void removeLayer (FiLayerSet layerSet) {
//		if (layerSet == null) {return;}
//		if (layerSets == null || layerSets.isEmpty ()) {return;}
//		if (layerSet.getId () <= 0) {
//			Log.println (Log.ERROR, "FiStand.removeLayer ()", "Can not remove a LayerSet without id, passed");
//			return;
//		}
//		layerSets.remove (layerSet.getId ());
//	}
//
//	/**
//	 * Get the layerSet with the given id.
//	 */
//	public FiLayerSet getLayerSet (int id) {return layerSets.get (id);}
//
//	/**
//	 * Get the layerSet collection.
//	 */
//	public Collection<FiLayerSet> getLayerSets () {
//		return layerSets != null ? layerSets.values() : Collections.EMPTY_LIST;
//	}
//
	/**
	 * Overides the trees registration in cells to cancel this registration. We
	 * load big files and do not need this registration at this time.
	 */
	@Override
	public void makeTreesPlotRegister (){
		// nothing
	}


	/**	Returns the list of species of the trees in the given stand	
	 */
	static public Collection<FiSpecies> getStandSpeciesList (FmStand stand) {
		// We need the list of the species accurately in the stand
		// asking the first species for the known species list does not work
		// because it will return the species of the removed trees also
		Collection<FiSpecies> standSpecies = new HashSet<FiSpecies> ();
		
		if (stand == null || stand.getTrees ().isEmpty ()) {return standSpecies;}
		
		for (Iterator i = stand.getTrees ().iterator (); i.hasNext ();) {
			FiPlant t = (FiPlant) i.next ();
			standSpecies.add (t.getSpecies ());  // set : duplicates are not kept
		}
		return standSpecies;
		
	}

	public boolean isBeetleAttacked() {
		return beetleAttacked;
	}

	public void setBeetleAttacked(boolean beetleAttacked) {
		this.beetleAttacked = beetleAttacked;
	}

	public double getBeetleAttack_a() {
		return beetleAttack_a;
	}

	public void setBeetleAttack_a(double beetleAttack_a) {
		this.beetleAttack_a = beetleAttack_a;
	}

	public double getBeetleAttack_b() {
		return beetleAttack_b;
	}

	public void setBeetleAttack_b(double beetleAttack_b) {
		this.beetleAttack_b = beetleAttack_b;
	}

	public double getBeetleAttack_c() {
		return beetleAttack_c;
	}

	public void setBeetleAttack_c(double beetleAttack_c) {
		this.beetleAttack_c = beetleAttack_c;
	}

	public double getMaxSpreadDistance() {
		return maxSpreadDistance;
	}

	public void setMaxSpreadDistance(double maxSpreadDistance) {
		this.maxSpreadDistance = maxSpreadDistance;
	}

	// fc-19.1.2015 MOVED to FiStand (moving state panel to capsis.lib.fire)
//	public static double[] calcMultiCov(Collection trees, Collection layerSets,
//			Plot plot,
//			double heightThreshold, Polygon2 poly) {
	
	//100 bigger trees
	public static double computeDDom(List<Double> dbhList) {
		int tn=dbhList.size();
		Collections.sort(dbhList);
		double quadMean100bigger = 0d;
		for (int i = 0; i < 100; i++) {
			double dbh = dbhList.get(tn - 1 - i);
			quadMean100bigger += dbh * dbh;
		}
		return Math.sqrt(0.01 * quadMean100bigger);

	}
	
	//Basal area m2/ha
		public static double computeBasalArea(List<Double> dbhList) {
			double ba=0;
			for(double dbh:dbhList) {
				ba+=0.25*Math.PI*dbh*dbh*0.0001;
			}
			return ba;
		}
		
	
	
	//100 bigger trees
		public static double computeHDom(List<Double> hList) {
			int tn=hList.size();
			Collections.sort(hList);
			double h100bigger = 0d;
			for (int i = 0; i < 100; i++) {
				double h = hList.get(tn - 1 - i);
				h100bigger += h;
			}
			return 0.01 * h100bigger;

		}

}

