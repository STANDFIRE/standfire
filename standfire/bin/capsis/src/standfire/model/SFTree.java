/*
 * The Standfire model.
 * 
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software. It is the property of its
 * authors and must not be copied without their permission. It can be shared by the modellers of the
 * Capsis co-development community in agreement with the Capsis charter
 * (http://capsis.cirad.fr/capsis/charter). See the license.txt file in the Capsis installation
 * directory for further information about licenses in Capsis.
 */

package standfire.model;

import jeeb.lib.defaulttype.CrownProfileUtil;
import jeeb.lib.util.Log;
import capsis.defaulttype.SpatializedTree;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSpecies;

/**
 * SFTree is the description of a tree in Standfire. The SFScene contains a list
 * of SFTree objects. Each tree has a unique id in the scene. When creating a
 * scene for a new time step, the trees are 'copied' with the same id and new
 * dimensions are calculated (see SFModel.processEvolution ()).
 * 
 * @author F. Pimont - September 2013
 */
public class SFTree extends FiPlant {

	/**
	 * The properties that do not change in time for a tree are located in an
	 * Immutable inner class to save space (e.g. id, x, y, z, species...).
	 * 
	 * @see capsis.defaulttype.SpatializedTree.Immutable
	 * @see capsis.defaulttype.Tree.Immutable
	 */
	public static class Immutable extends SpatializedTree.Immutable {

		private double[][] crownProfile; // crownRadius relatively to tree
											// height

	}

	// private double crownBaseHeight; // m
	// private double crownRadius; // m

	/**
	 * Constructor for a new SFTree (first time). The Immutable object is
	 * created in the superclasses and completed for this level with
	 * crownProfile.
	 * 
	 * @see SpatializedTree, Tree
	 */
	public SFTree(int id, SFScene scene, SFModel model, double x, double y, double z, double dbh, double height,
			double crownBaseHeight, double crownDiameter, FiSpecies species, double crownwt0_kg, double crownwt1_kg,
			double crownwt2_kg, double crownwt3_kg

	) throws Exception {

		super(id, scene, model, 0, x, y, z, dbh, height, crownBaseHeight, crownDiameter, species); // 0
		// is
		// age
		// (unknow
		// in
		// standfire)
		// setCrownBaseHeight (crownBaseHeight);
		// setCrownRadius (crownRadius);
		// initially the fuel is alive, be can be modified withth additionnal
		// parameter
		
		biomass.put(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.LIVE), crownwt0_kg);
		biomass.put(FiParticle.makeKey(FiParticle.TWIG1, FiParticle.LIVE), crownwt1_kg);
		biomass.put(FiParticle.makeKey(FiParticle.TWIG2, FiParticle.LIVE), crownwt2_kg);
		biomass.put(FiParticle.makeKey(FiParticle.TWIG3, FiParticle.LIVE), crownwt3_kg);
		biomass.put(FiParticle.makeKey(FiParticle.LEAVE, FiParticle.DEAD), 0d);
		biomass.put(FiParticle.makeKey(FiParticle.TWIG1, FiParticle.DEAD), 0d);
		biomass.put(FiParticle.makeKey(FiParticle.TWIG2, FiParticle.DEAD), 0d);
		biomass.put(FiParticle.makeKey(FiParticle.TWIG3, FiParticle.DEAD), 0d);

		this.updateTotalThinMass(); 

		// System.out.println ("	SFTREE " + id + ":biomass=" +
		// this.totalThinMass);
		// System.out.println ("		" + crownwt0_kg + "," + crownwt1_kg + "," +
		// crownwt2_kg + "," +
		// crownwt3_kg);

		try {
			// See CrownProfileUtil.createRelativeCrownProfile ()
			double[] profile = { 0, 0, 25, 80, 60, 100, 90, 40, 100, 0 };
			getImmutable().crownProfile = CrownProfileUtil.createRelativeCrownProfile(profile);
		} catch (Exception e) {
			Log.println(Log.ERROR, "SFTree.c ()", "Could not create the crown profile", e);
		}

	}

	/**
	 * method to get a put a copy of a SFTree in scene at position 0,0,0
	 * 
	 * @return
	 * @throws Exception
	 */
	protected SFTree putCopyIn(SFScene scene) throws Exception {
		SFTree cp = new SFTree(scene.maxId + 1, scene, (SFModel) this.model, 0d, 0d, 0d, this.dbh, this.height, this.crownBaseHeight,
				this.crownDiameter, this.species, 0d, 0d, 0d, 0d);
		// System.out.println(" treeCBD="+cp.getCrownBaseHeight()+","+this.crownBaseHeightBeforePruning);
		for (String key : biomass.keySet()) {
			cp.biomass.put(key, biomass.get(key));
		}
		this.updateTotalThinMass(); // fc-2.2.2015
		return cp;
	}

	// /** Copy constructor
	// */
	// public SFTree (SFTree modelTree) {
	// super ((SpatializedTree) modelTree,
	// modelTree.scene,
	// modelTree.age,
	// modelTree.height,
	// modelTree.dbh,
	// false); // marked = false (unused)
	//
	// setCrownBaseHeight (modelTree.getCrownBaseHeight ());
	// setCrownRadius (modelTree.getCrownRadius ());
	//
	// // This tree and the modelTree have the same Immutable object
	// // done in superclass - this.immutable = modelTree.immutable;
	//
	// }

	/**
	 * Create an Immutable object whose class is declared at one level of the
	 * hierarchy. This is called only in constructor for new logical object in
	 * superclass. If an Immutable is declared in subclass, subclass must
	 * redefine this method (same body) to create an Immutable defined in
	 * subclass.
	 */
	@Override
	protected void createImmutable() {
		immutable = new Immutable();
	}

	/**
	 * Convenient method.
	 */
	protected Immutable getImmutable() {
		return (Immutable) immutable;
	}

	/**
	 * Clones a SFTree: first calls super.clone () (primitive types like
	 * boolean, int, double... and references are copied). Then clones the
	 * SFTree 'object type' instance variables (i.e. not primitive).
	 */
	@Override
	public Object clone() {
		try {
			SFTree t = (SFTree) super.clone();

			// Clone the object type instance variables if any
			// -> nothing to clone here: crownBaseHeight and crownRadius are
			// primitive types

			return t;
		} catch (Exception e) {
			Log.println(Log.ERROR, "SFTree.clone ()", "Error while cloning tree: " + this, e);
			return null;
		}
	}

	/**
	 * Creates a new SFTree, one time step older.
	 */
	public SFTree processGrowth(double p1, double p2, double p3, double p4) {

		// // H in m. and dH computed in cm./year
		// double dH = p1 * Math.pow (getHeight (), 2) * Math.exp (-p2 *
		// getHeight ());
		//
		// // D in cm. and dD computed in mm./year
		// double dD = p3 * Math.pow (getDbh (), 2) * Math.exp (-p4 * getDbh
		// ());
		//
		// // We use 'this' as modelTree to create a new instance
		// SFTree newTree = new SFTree (this);
		//
		// newTree.setAge (getAge () + 1);
		// newTree.setHeight (getHeight () + dH / 100d); // m
		// newTree.setDbh (getDbh () + dD / 10d); // cm
		//
		// double cr = crownRadius + ((newTree.getDbh () - getDbh ()) / 100d *
		// 5); // m
		// newTree.setCrownRadius (cr);
		//
		// return newTree;
		return null;
	}

	/**
	 * A string representation of this object.
	 */
	@Override
	public String toString() {
		return "SFTree_" + getId();
	}

	// public void setCrownBaseHeight (double v) {
	// crownBaseHeight = v;
	// }

	// public void setCrownRadius (double v) {
	// crownRadius = v;
	// }

	// TreeWithCrownProfile interface

	public String getName() {
		return "SFTree " + getId();
	}
}
