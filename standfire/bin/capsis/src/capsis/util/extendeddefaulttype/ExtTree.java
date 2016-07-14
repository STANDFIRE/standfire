/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package capsis.util.extendeddefaulttype;

import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.util.Log;
import lerfob.carbonbalancetool.CATCompatibleTree;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.allometrycalculator.AllometryCalculableTree;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider;
import capsis.defaulttype.NumberableTree;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;
import capsis.util.methodprovider.TreeBasicFeatures;
import capsis.util.methodprovider.TreeStatus;

public abstract class ExtTree extends NumberableTree implements TreeBasicFeatures, Cloneable, TreeStatusProvider,
		AllometryCalculableTree, MonteCarloSimulationCompliantObject, CATCompatibleTree {

	/**
	 * This class contains immutable instance variables for a logical ArtTree.
	 * 
	 * @see Tree
	 */
	public static class Immutable extends NumberableTree.Immutable {
		private static final long serialVersionUID = 20100804L;
		private String initialSpeciesName; // name of the species in the input
											// file
		public ExtSpeciesGroup species;
	}

	private StatusClass statusClass;

	/**
	 * General constructor.
	 * 
	 * @param id
	 * @param stand
	 * @param age
	 * @param height
	 * @param dbh
	 * @param number
	 */
	public ExtTree(int id, GScene stand, int age, double height, double dbh, double number, ExtSpeciesGroup species) {
		super(id, stand, age, height, dbh, false, number, -1);
		this.statusClass = StatusClass.alive;
		getImmutable().species = species;
	}

	/**
	 * Constructor for survivor tree.
	 * 
	 * @param modelTree
	 *            a QuebecMRNFTree instance
	 * @param stand
	 *            the QuebecMRNFStand instance whose tree is
	 */
	protected ExtTree(ExtTree modelTree, GScene stand) {
		super(modelTree, stand, modelTree.age + 1, -1.0, modelTree.getDbh(), false, modelTree.getNumber(), -1); // age
																												// =
																												// 0,
																												// marked
																												// =
																												// false,
																												// numberOfDead
																												// =
																												// -1,
																												// height
																												// =
																												// -1.0
		this.statusClass = StatusClass.alive;
		getImmutable().species = modelTree.getSpecies();
	}

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

	protected Immutable getImmutable() {
		return (Immutable) immutable;
	}

	/**
	 * This method provides a wrapper for the status class in the different data
	 * extractor.
	 * 
	 * @return a TreeStatus which extends EnumProperty
	 */
	public TreeStatus getTreeStatus() {
		return TreeStatus.getStatus(getStatusClass());
	}

	@Override
	public StatusClass getStatusClass() {
		return statusClass;
	}

	@Override
	public void setStatusClass(StatusClass statusClass) {
		this.statusClass = statusClass;
	}

	@Override
	public double getDbhCm() {
		return getDbh();
	}

	@Override
	public double getSquaredDbhCm() {
		return getDbh2();
	}

	@Override
	public double getStemBasalAreaM2() {
		double dbh2 = getDbh2();
		return Math.PI * dbh2 * 0.000025;
	}

	@Override
	public double getPlotWeight() {
		return ((ExtPlot) getScene()).getWeight();
	}

	@Override
	public double getHeightM() {
		return getHeight();
	}

	@Override
	public abstract double getCommercialVolumeM3();

	@Override
	public abstract double getTotalVolumeDm3();

	/**
	 * Return the square dbh.
	 */
	protected double getDbh2() {
		return (double) dbh * dbh;
	}

	/**
	 * This method returns the name of the species group to which belong that
	 * tree.
	 * 
	 * @return a String
	 */
	public String getSpeciesName() {
		return getSpecies().getName();
	}

	/**
	 * This method returns the species features of this tree.
	 * 
	 * @return a QuebecMRNFSpecies instance
	 */
	public ExtSpeciesGroup getSpecies() {
		return getImmutable().species;
	}

	@Override
	public String getSubjectId() {
		return ((Integer) getId()).toString();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return ((ExtPlot) getScene()).getMonteCarloRealizationId();
	}

	/**
	 * This method returns the initial species name or an empty string if this
	 * field has not been set
	 * 
	 * @return a String
	 */
	public String getInitialSpeciesName() {
		if (getImmutable().initialSpeciesName != null) {
			return getImmutable().initialSpeciesName;
		} else {
			return "";
		}
	}

	protected void setInitialSpeciesName(String speciesName) {
		getImmutable().initialSpeciesName = speciesName;
	}
	
	/**
	 * This method is called in the processMortality from the model.
	 * 
	 * @param scene
	 *            an AbstractExtendedTreeList instance
	 * @return an ExtendedTree instance
	 */
	protected abstract ExtTree createSurvivorTree(ExtPlot scene);

	/**
	 * This method handles a change in the status such as in the mortality or
	 * the thinning process.
	 * 
	 * @param event
	 *            an Object resulting from a LogisticModelBasedSimulator
	 *            instance
	 * @param hostStand
	 *            the scene that retrieve the tree
	 * @param newStatus
	 *            a StatusClass instance
	 */
	public void processStatusChange(Object event, ExtPlot hostStand, StatusClass newStatus) {
		Collection<ExtTree> newTrees = new ArrayList<ExtTree>();

		boolean isAlreadyPartOfThisScene = hostStand.getTrees().contains(this);

		ExtTree survivorTree;

		if (isAlreadyPartOfThisScene) {
			survivorTree = this;
		} else {
			survivorTree = createSurvivorTree(hostStand);
		}

		ExtTree treeWithNewStatus;
		if (event instanceof Boolean) {
			boolean changingStatus = (Boolean) event;
			if (changingStatus) {
				treeWithNewStatus = clone();
				treeWithNewStatus.setScene(hostStand);
				treeWithNewStatus.setStatusClass(newStatus);
				newTrees.add(treeWithNewStatus);
				if (isAlreadyPartOfThisScene) {
					hostStand.removeTree(this);
				}
			} else if (!isAlreadyPartOfThisScene) {
				newTrees.add(survivorTree);
			}
		} else if (event instanceof Double) {
			double eventProbability = (Double) event;
			double numberBeforeProcessing = survivorTree.number;
			survivorTree.number *= (1.0 - eventProbability);
			if (survivorTree.number > ExtSimulationSettings.VERY_SMALL) {
				if (!isAlreadyPartOfThisScene) {
					newTrees.add(survivorTree);
				}
			} else {
				if (isAlreadyPartOfThisScene) {
					hostStand.removeTree(this);
				}
			}
			if ((numberBeforeProcessing - survivorTree.number) > ExtSimulationSettings.VERY_SMALL) { // number
																										// of
																										// dead
																										// not
																										// negligible
				treeWithNewStatus = clone();
				treeWithNewStatus.setScene(hostStand);
				treeWithNewStatus.number = numberBeforeProcessing - survivorTree.number;
				treeWithNewStatus.setStatusClass(newStatus);
				newTrees.add(treeWithNewStatus);
			}
		}

		if (!newTrees.isEmpty()) {
			for (ExtTree tree : newTrees) {
				hostStand.storeStatus(tree, tree.getStatusClass().name(), tree.getNumber());
			}
		}

	}

	// /**
	// * This method handles the thinning at the tree level. The method takes in
	// charge the
	// * update of the tree list in the new stand.
	// * @param dStandThinningProbability a double between 0 and 1 that
	// represents the probability of the stand being thinned
	// * @param dTreeThinningProbability a double between 0 and 1 that
	// represents the probability of the tree being thinned
	// * @param newStand a QuebecMRNFStand instance that contains the tree
	// * @param bMonteCarlo a boolean (true if the model operates in stochastic
	// mode or false if it is deterministic)
	// */
	// public void processThinning (double dStandThinningProbability, double
	// dTreeThinningProbability, AbstractExtendedTreeList newStand, boolean
	// bMonteCarlo) {
	//
	// // fc+rm-11.6.2015 Created this thinning method based on the mortality
	// method below
	//
	// Collection<ExtendedTree> newTrees = new ArrayList<ExtendedTree>();
	//
	// ExtendedTree survivorTree = createSurvivorTree(newStand);
	// // ExtendedTree survivorTree = clone();
	// // survivorTree.setScene(newStand);
	// ExtendedTree cutTree;
	//
	// if (bMonteCarlo) { // stochastic
	// double standResidualError = ExtendedModel.RANDOM.nextDouble();
	// double treeResidualError = ExtendedModel.RANDOM.nextDouble();
	// if (standResidualError < dStandThinningProbability && treeResidualError <
	// dTreeThinningProbability) {
	// cutTree = clone();
	// cutTree.setScene(newStand);
	// cutTree.setStatusClass(StatusClass.cut);
	// newTrees.add(cutTree);
	// } else {
	// newTrees.add(survivorTree);
	// }
	// } else { // deterministic
	// double thinProba = dStandThinningProbability * dTreeThinningProbability;
	// survivorTree.number *= (1.0 - thinProba);
	// if (survivorTree.number > AbstractSimulationSettings.VERY_SMALL) {
	// newTrees.add(survivorTree);
	// }
	// if ((this.number - survivorTree.number) >
	// AbstractSimulationSettings.VERY_SMALL) { // number of dead not negligible
	// cutTree = clone();
	// cutTree.setScene(newStand);
	// cutTree.number *= thinProba;
	// cutTree.setStatusClass(StatusClass.cut);
	// newTrees.add(cutTree);
	// }
	// }
	//
	// if (!newTrees.isEmpty()) {
	// for (ExtendedTree tree : newTrees) {
	// newStand.storeStatus(tree, tree.getStatusClass().name(),
	// tree.getNumber());
	// }
	// }
	// }

	/**
	 * This method handles the mortality at the tree level. The method takes in
	 * charge the update of the tree list in the new stand.
	 * 
	 * @param dDeathProbability
	 *            a double between 0 and 1 that represents the probability of
	 *            mortality
	 * @param newStand
	 *            a QuebecMRNFStand instance that contains the tree
	 * @param bMonteCarlo
	 *            a boolean (true if the model operates in stochastic mode or
	 *            false if it is deterministic)
	 */
	@Deprecated
	public void processMortality(double dDeathProbability, ExtPlot newStand, boolean bMonteCarlo) {
		Collection<ExtTree> newTrees = new ArrayList<ExtTree>();

		ExtTree survivorTree = createSurvivorTree(newStand);
		ExtTree deadTree;

		if (bMonteCarlo) { // stochastic
			double residualError = ExtModel.RANDOM.nextDouble();
			if (residualError < dDeathProbability) {
				deadTree = clone();
				deadTree.setScene(newStand);
				deadTree.setStatusClass(StatusClass.dead);
				newTrees.add(deadTree);
			} else {
				newTrees.add(survivorTree);
			}
		} else { // deterministic
			survivorTree.number *= (1.0 - dDeathProbability);
			if (survivorTree.number > ExtSimulationSettings.VERY_SMALL) {
				newTrees.add(survivorTree);
			}
			if ((this.number - survivorTree.number) > ExtSimulationSettings.VERY_SMALL) { // number
																							// of
																							// dead
																							// not
																							// negligible
				deadTree = clone();
				deadTree.setScene(newStand);
				deadTree.number *= dDeathProbability;
				deadTree.setStatusClass(StatusClass.dead);
				newTrees.add(deadTree);
			}
		}

		if (!newTrees.isEmpty()) {
			for (ExtTree tree : newTrees) {
				newStand.storeStatus(tree, tree.getStatusClass().name(), tree.getNumber());
			}
		}
	}

	@Override
	public ExtTree clone() {
		try {
			ExtTree t = (ExtTree) super.clone();
			return t;
		} catch (Exception exc) {
			Log.println(Log.ERROR, "ExtendedTree.clone ()", "Error while cloning tree." + " Source tree=" + toString()
					+ " " + exc.toString(), exc);
			return null;
		}
	}

}
