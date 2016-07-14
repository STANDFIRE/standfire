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
package capsis.util.extendeddefaulttype.disturbances;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import repicea.simulation.REpiceaLogisticPredictor;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;

public class ModelBasedThinningDisturbanceParameters extends ThinningDisturbanceParameters {

	private REpiceaLogisticPredictor standModel;
	private final REpiceaLogisticPredictor treeModel;

	/**
	 * Default constructor for DisturbanceMode set to None.
	 */
	protected ModelBasedThinningDisturbanceParameters() {
		this(null, null, DisturbanceMode.None, -1);
	}

	/**
	 * Constructor for partial implementation, i.e. with recurrence at the stand
	 * level, but model-based probabilities at tree level.
	 * 
	 * @param treeModel
	 *            a LogisticModelBasedSimulator instance
	 * @param recurrenceYrs
	 *            an integer
	 */
	protected ModelBasedThinningDisturbanceParameters(REpiceaLogisticPredictor treeModel, int recurrenceYrs) {
		this(treeModel, null, DisturbanceMode.Random, recurrenceYrs);
	}

	/**
	 * Constructor for partial implementation, i.e. with occurrence in the next
	 * step, but model-based probabilities at tree level.
	 * 
	 * @param treeModel
	 *            a LogisticModelBasedSimulator instance
	 */
	protected ModelBasedThinningDisturbanceParameters(REpiceaLogisticPredictor treeModel) {
		this(treeModel, null, DisturbanceMode.NextStep, -1);
	}

	/**
	 * Constructor for complete model implementation.
	 * 
	 * @param treeModel
	 *            a LogisticModelBasedSimulator instance
	 * @param standModel
	 *            a LogisticModelBasedSimulator instance
	 */
	protected ModelBasedThinningDisturbanceParameters(REpiceaLogisticPredictor treeModel,
			REpiceaLogisticPredictor standModel) {
		this(treeModel, standModel, DisturbanceMode.ModelBased, -1);
	}

	private ModelBasedThinningDisturbanceParameters(REpiceaLogisticPredictor treeModel,
			REpiceaLogisticPredictor standModel, DisturbanceMode mode, int recurrenceYrs) {
		super(mode, recurrenceYrs);
		this.treeModel = treeModel;
		this.standModel = standModel;
	}

	@Override
	public Object isThereADisturbance(int yrs, Object... parms) {
		if (getMode() == DisturbanceMode.ModelBased) {
			Object excludedGroup = null;
			if (parms.length > 1) {
				excludedGroup = parms[1];
			}
			Object thinningStandEvent = standModel.predictEvent(parms[0], null, excludedGroup); // tree
																								// =
																								// null
																								// //
																								// TODO
																								// FP
																								// add
																								// validation
																								// group
			return thinningStandEvent;
		} else { // full model implementation
			return super.isThereADisturbance(yrs);
		}
	}

	@Override
	public DisturbanceParameters mute() {
		DisturbanceMode mode = this.getMode();
		if (mode == DisturbanceMode.NextStep) {
			mode = DisturbanceMode.None;
		}
		DisturbanceParameters mutant = new ModelBasedThinningDisturbanceParameters(treeModel, standModel, mode,
				recurrenceYrs);
		return mutant;
	}

	@Override
	public Map<Tree, Object> markTrees(GScene stand, Collection<Tree> trees, int yrs, Object... parms) {
		Map<Tree, Object> resultMap = new HashMap<Tree, Object>();
		Object excludedGroup = null;
		if (parms.length >= 1) {
			excludedGroup = parms[0];
		}
		Object standEvent = isThereADisturbance(yrs, stand, excludedGroup);
		for (Tree tree : trees) {
			if (standEvent instanceof Boolean && !(Boolean) standEvent) {
				resultMap.put(tree, false);
			} else {
				Object result = treeModel.predictEvent(stand, tree, parms); // parms
																			// may
																			// contain
																			// the
																			// excluded
																			// group
																			// in
																			// case
																			// of
																			// validation
				if (standEvent instanceof Double && result instanceof Double) { // mean
																				// we
																				// are
																				// working
																				// in
																				// deterministic
																				// mode
					result = (Double) standEvent * (Double) result;
				}
				resultMap.put(tree, result);
			}
		}
		return resultMap;
	}

}
