/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin 
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lerfob.carbonbalancetool.CATCompatibleStand;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.TreeStatusCollectionsProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;

/**
 * The PlotSample class contains a sample of plots from a stand or a larger
 * population.
 * 
 * @author Mathieu Fortin - July 2015
 */
public class ExtPlotSample extends TreeList implements TreeStatusCollectionsProvider, AreaHaProvider,
		MonteCarloSimulationCompliantObject, CATCompatibleStand {

	public class Immutable extends TreeList.Immutable {
		private static final long serialVersionUID = 20131022L;
		private int monteCarloRealizationID;
		private double area;
	}

	private final Map<String, ExtPlot> internalPlotList;
	protected final ExtCompositeStand compositeTreeList;

	protected ExtPlotSample(ExtCompositeStand compositeTreeList, int i) {
		super();
		this.compositeTreeList = compositeTreeList;
		internalPlotList = new TreeMap<String, ExtPlot>();
		createImmutable();
		getImmutable().monteCarloRealizationID = i;
	}

	@Override
	protected void createImmutable() {
		immutable = new Immutable();
	}

	@Override
	protected Immutable getImmutable() {
		return (Immutable) immutable;
	}

	public Map<String, ExtPlot> getPlotMap() {
		return internalPlotList;
	}

	@Override
	public Collection<? extends Tree> getTrees() {
		Collection coll = new ArrayList();
		for (ExtPlot treeList : internalPlotList.values()) {
			coll.addAll(treeList.getTrees());
		}
		return coll;
	}

	/**
	 * This method returns the trees that match a particular StatusClass. To be
	 * preferred over getTrees(String status).
	 * 
	 * @param statusClass
	 *            a StatusClass enum variable
	 * @return a Collection of Tree instances
	 */
	@Override
	public Collection<Tree> getTrees(StatusClass statusClass) {
		return getTrees(statusClass.name());
	}

	@Override
	public Collection<Tree> getTrees(String status) {
		Collection<Tree> coll = new ArrayList<Tree>();

		if (status == null) {
			return coll;
		}

		for (ExtPlot treeList : internalPlotList.values()) {
			coll.addAll(treeList.getTrees(status));
		}
		return coll;
	}

	@Override
	public Set<String> getStatusKeys() {
		Set<String> oStatusCollection = new HashSet<String>();

		for (ExtPlot treeList : internalPlotList.values()) {
			oStatusCollection.addAll(treeList.getStatusKeys());
		}
		return oStatusCollection;
	}

	@Override
	public double getAreaHa() {
		return getArea() * .0001;
	}

	@Override
	public double getArea() {
		if (getImmutable().area == 0d) {
			double area = 0d;
			for (ExtPlot treeList : internalPlotList.values()) {
				area += treeList.getArea();
			}
			getImmutable().area = area;
		}
		return getImmutable().area;
	}

	@Override
	public void setInterventionResult(boolean b) {
		super.setInterventionResult(b);

		if (getPlotMap() == null) {
			return;
		}

		for (TreeList stand : getPlotMap().values()) {
			stand.setInterventionResult(b);
		}
	}

	@Override
	public void clearTrees() {
		for (TreeCollection stand : getPlotMap().values()) {
			stand.clearTrees();
		}
	}

	@Override
	public int size() {
		if (getPlotMap() != null) {
			return getPlotMap().size();
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "PlotSample_" + getImmutable().monteCarloRealizationID;
	}

	@Override
	public String getSubjectId() {
		return "0";
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() { // Should be called only
														// if the scale of
														// application is set to
														// Stand
		return HierarchicalLevel.PLOT; // TODO FP this part is unclear and
										// should obey to a spatial model
	}

	@Override
	public int getMonteCarloRealizationId() {
		return getImmutable().monteCarloRealizationID;
	}

	@Override
	public String getStandIdentification() {
		return compositeTreeList.getStandIdentification();
	}

	@Override
	public int getDateYr() {
		return this.compositeTreeList.getDateYr();
	}

}
