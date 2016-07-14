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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.simulation.REpiceaLogisticPredictor;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.util.extendeddefaulttype.disturbances.ThinningDisturbanceParametersPanel.BoundaryVariable;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.VProvider;


public class RuleBasedThinningDisturbanceParameters extends ThinningDisturbanceParameters {

	private final double minimumValue;
	private final double maximumValue;
	
	private final BoundaryVariable boundaryVariable;
	private final MethodProvider mp;
	
	private REpiceaLogisticPredictor treeThinningScorer;
	
	
	private static class TreeWrapper implements Comparable<TreeWrapper> {

		private final Tree tree;
		private final double harvestProbability;

		private TreeWrapper(Tree tree, double harvestProbability) {
			this.tree = tree;
			this.harvestProbability = harvestProbability;
		}
		
		@Override
		public int compareTo(TreeWrapper arg0) {
			if (harvestProbability < arg0.harvestProbability) {
				return -1;
			} else if (harvestProbability == arg0.harvestProbability) {
				return 0;
			} else {
				return 1;
			}
		}
		
	}
	
	
	public RuleBasedThinningDisturbanceParameters(BoundaryVariable boundaryVariable, 
			double minimumValue, 
			double maximumValue,
			MethodProvider mp) {
		super(DisturbanceMode.RuleBased, -1);
		this.boundaryVariable = boundaryVariable;
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
		this.mp = mp;
	}

	
	@Override
	public Boolean isThereADisturbance(int yrs, Object... parms) {
		GScene stand = (GScene) parms[0];
		Collection trees = ((TreeCollection) stand).getTrees();
		double areaFactor = 1d / ((AreaHaProvider) stand).getAreaHa();
		double value = getValue(stand, trees, areaFactor);
		return value > maximumValue;
	}


	@Override
	public Map<Tree, Object> markTrees(GScene stand, Collection<Tree> trees, int yrs, Object... parms) {
		Object standEvent = isThereADisturbance(yrs, stand);
		double areaFactor = 1d / ((AreaHaProvider) stand).getAreaHa();
		Map<Tree, Object> resultMap = new HashMap<Tree, Object>();
		List<TreeWrapper> copyList = new ArrayList<TreeWrapper>();
		double prediction;
		for (Tree tree : trees) {
			if (treeThinningScorer != null) {
				prediction = treeThinningScorer.predictEventProbability(stand, tree, standEvent);
			} else {
				prediction = 0d;
			}
			copyList.add(new TreeWrapper(tree, prediction));
		}
		Collections.sort(copyList, Collections.reverseOrder());
		List<Tree> orderedCopyList = new ArrayList<Tree>();
		for (TreeWrapper wrapper : copyList) {
			orderedCopyList.add(wrapper.tree);
		}
		double currentValue;
		while (!orderedCopyList.isEmpty()) {
			currentValue = getValue(stand, orderedCopyList, areaFactor);
			if (currentValue <= minimumValue || (standEvent instanceof Boolean && !(Boolean) standEvent)) {
				resultMap.put(orderedCopyList.remove(0), false);
			} else {
				resultMap.put(orderedCopyList.remove(0), true);
			}
		}
		return resultMap;
	}

	
	private double getValue(GScene stand, Collection trees, double areaFactor) {
		switch(boundaryVariable) {
//		case N:
//			return ((NProvider) mp).getN(stand, trees) * areaFactor;
		case G:
			return ((GProvider) mp).getG(stand, trees) * areaFactor;
		case V:
			return ((VProvider) mp).getV(stand, trees) * areaFactor;
		}
		return -1d;
	}
	
	@Override
	public DisturbanceParameters mute() {
		return this;
	}

	/**
	 * This method sets a model that makes it possible to rank the trees according to their probability of being harvested. This
	 * way, the selection is not made at random.
	 * @param logisticModelBasedSimulator a LogisticModelBasedSimulator instance
	 */
	public void setScorer(REpiceaLogisticPredictor logisticModelBasedSimulator) {
		this.treeThinningScorer = logisticModelBasedSimulator;
	}
	
}
