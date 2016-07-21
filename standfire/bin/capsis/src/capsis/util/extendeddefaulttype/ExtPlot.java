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

import java.util.Collection;
import java.util.Map;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.allometrycalculator.AllometryCalculableTree;
import repicea.simulation.allometrycalculator.AllometryCalculator;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.simulation.covariateproviders.standlevel.TreeStatusCollectionsProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.util.EnumProperty;

/**
 * The AbstractExtendedTreeList class implements the MonteCarlo technique for a TreeList instance.
 * @author Mathieu Fortin - October 2013
 */
public abstract class ExtPlot extends TreeList implements MonteCarloSimulationCompliantObject, 
																			BasalAreaM2HaProvider,
																			AreaHaProvider,
																			TreeStatusCollectionsProvider,
																			GeographicalCoordinatesProvider {

	
	private static final long serialVersionUID = 20100804L;

	private static AllometryCalculator allometryCalculator;
	
	public static class Immutable extends TreeList.Immutable {
		private static final long serialVersionUID = 20100804L;
		public String ID;
		public double longitude;
		public double latitude;
		public double altitude;	
		public double weight = 1d;
	}

	private ExtCompositeStand stratum;
	private int monteCarloRealizationID;


	/**
	 * Constructor.
	 * @param ID a String
	 * @param monteCarloID a MonteCarlo realization
	 */
	protected ExtPlot(String ID, int monteCarloID) {
		super();
		getImmutable().ID = ID;
		this.monteCarloRealizationID = monteCarloID;
	}
	

	private synchronized void createAllometryCalculator() {
		if (allometryCalculator == null) {
			allometryCalculator = new AllometryCalculator();
		}
	}

	/**
	 * This method returns an AllometryCalculator which can be used to calculate
	 * simple allometry features.
	 * @return a allometry calculator (AllometryCalculator)
	 */
	public AllometryCalculator getAllometryCalculator() {
		if (allometryCalculator == null) {
			createAllometryCalculator();
		}
		return allometryCalculator;
	}

	
	@Override
	protected Immutable getImmutable() {return (Immutable) immutable;}
	
	@Override
	protected void createImmutable () { immutable = new Immutable();}

	/*
	 * Extended visibility (non-Javadoc)
	 * @see capsis.defaulttype.TreeList#getHeavyClone()
	 */
	@Override
	public ExtPlot getHeavyClone () {
		ExtPlot copy = (ExtPlot) super.getHeavyClone();
		return copy;
	}

	
	public String getId() {return getImmutable().ID;}

	public void setLongitudeDeg(double fValue) {getImmutable().longitude = fValue;}
	@Override
	public double getLongitudeDeg() {return getImmutable().longitude;}
	
	public void setLatitudeDeg(double fValue) {getImmutable().latitude = fValue;}
	@Override
	public double getLatitudeDeg() {return getImmutable().latitude;}

	public void setElevation(double fValue) {getImmutable().altitude = fValue;}
	@Override
	public double getElevationM() {return getImmutable().altitude;}

	
	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}


	@Override
	public String getSubjectId() {
		return getId();
	}
	
	/**
	 * This method returns the stratum this plot belongs to.
	 * @return a CompositeTreeList instance
	 */
	public ExtCompositeStand getStratum() {return stratum;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloRealizationID;}
	
	public void setMonteCarloRealizationId(int monteCarloRealizationID) {this.monteCarloRealizationID = monteCarloRealizationID;}

	@Override
	public Collection<Tree> getTrees(StatusClass statusClass) {
		return getTrees(statusClass.name());
	}
	
	protected void setStratumPointer(ExtCompositeStand stratum) {this.stratum = stratum;}
	
	/**
	 * This method updates the stand variables such as the basal area and so on.
	 */
	public abstract void updateStandVariables();
	
	/**
	 * This method sets the weight of this plot in the stratum.
	 * @param weight a double
	 */
	public void setWeight(double weight) {getImmutable().weight = weight;}

	/**
	 * This method returns the weight of this plot in the stratum.
	 * @return a double
	 */
	public double getWeight() {return getImmutable().weight;}

	
	/**
	 * This method returns a map whose keys are the species names and values are the collection of AllometryCalculableTree
	 * instances corresponding to the species
	 * @param trees a Collection of AllometryCalculableTree instances
	 * @return the above mentioned map
	 */
	public Map<String, Collection<AllometryCalculableTree>> getCollectionsBySpecies() {
		Collection<EnumProperty> index = getStratum().getSpeciesGroupTags().values();
		return getStratum().getCollectionsBySpecies(index, getTrees());
	}
	
	@Override
	public double getAreaHa() {return getArea() * .0001;}
	
}
