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

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lerfob.carbonbalancetool.CATCompatibleStand;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.allometrycalculator.AllometryCalculableTree;
import repicea.simulation.covariateproviders.standlevel.StochasticInformationProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.util.MemoryWatchDog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GScene;
import capsis.util.EnumProperty;

/**
 * The CompositeTreeList class handles several TreeList instance in the same
 * simulation. It is also designed for stochastic simulation.
 * 
 * @author Mathieu Fortin - October 2013
 */
public abstract class ExtCompositeStand extends TreeList implements StochasticInformationProvider<ExtPlotSample>,
		CATCompatibleStand {

	private static final long serialVersionUID = 20131022L;

	public static class PredictorID implements Serializable {

		public static final PredictorID DIAMETER_GROWTH = new PredictorID("DIAMETER_GROWTH");
		public static final PredictorID MORTALITY = new PredictorID("MORTALITY");
		public static final PredictorID HD_RELATIONSHIP = new PredictorID("HD_RELATIONSHIP");
		public static final PredictorID COMMERCIAL_VOLUME = new PredictorID("COMMERCIAL_VOLUME");
		public static final PredictorID TOTAL_VOLUME = new PredictorID("TOTAL_VOLUME");
		public static final PredictorID TREE_HARVESTING = new PredictorID("TREEHARVESTING");
		public static final PredictorID STAND_HARVESTING = new PredictorID("STAND_HARVESTING");
		public static final PredictorID CLIMATE = new PredictorID("CLIMATE");

		private final String predictorName;

		protected PredictorID(String name) {
			predictorName = name;
		}

		@Override
		public int hashCode() {
			return predictorName.hashCode();
		}

		@Override
		public String toString() {
			return "PredictorID - " + predictorName;
		}
	}

	protected static enum MessageID implements TextableEnum {
		StemDensity("stems/ha", "tiges/ha"), BasalArea("m2/ha", "m2/ha"), SampleSize("Sample size",
				"Taille d'\u00E9chantillon"), StochasticMode("Stochastic mode", "Mode stochastique"), DeterministicMode(
				"Deterministic mode", "Mode d\u00E9terministe"), Realization("realization", "r\u00E9alisation");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	public class Immutable extends TreeList.Immutable {
		private static final long serialVersionUID = 20131022L;
		private String stratumName = null;
		private boolean isStochastic;
		private int numberIterationMC;
		private Map<Integer, EnumProperty> speciesGroupTags;
		private Map<String, REpiceaPredictor> predictors;
		private double area;
	}

	private List<ExtEvolutionParameters> evolutionVector;
	private ExtPlotSample[] plotSampleArray;

	/**
	 * Constructor. In case of deterministic simulations the number of Monte
	 * Carlo iteration is automatically set to 1.
	 * 
	 * @param isStochastic
	 *            true to enable stochastic simulation (if implemented)
	 * @param numberIterationMC
	 *            the number of Monte Carlo iterations
	 */
	protected ExtCompositeStand(boolean isStochastic, int numberIterationMC) {
		super();
		if (numberIterationMC < 1) {
			throw new InvalidParameterException(
					"The number of Monte Carlo iteration must be larger than or equal to 1!");
		}
		evolutionVector = new ArrayList<ExtEvolutionParameters>();
		getImmutable().isStochastic = isStochastic;
		getImmutable().numberIterationMC = numberIterationMC;
		getImmutable().predictors = new HashMap<String, REpiceaPredictor>();
		init();
	}

	/**
	 * Constructor for deterministic simulations.
	 */
	protected ExtCompositeStand() {
		this(false, 1);
	}

	/**
	 * This method returns a ModelBasedSimulator instance.
	 * 
	 * @param predictorID
	 *            the enum that designates the ModelBasedSimulator instance
	 * @return a ModelBasedSimulator instance
	 */
	public REpiceaPredictor getPredictor(PredictorID predictorID) {
		return getImmutable().predictors.get(predictorID.predictorName);
	}

	/**
	 * This method adds a predictor to a map of predictors.
	 * 
	 * @param predictorID
	 *            a PredictorID enum variable
	 * @param predictor
	 *            a ModelBasedSimulator instance
	 */
	public void addPredictor(PredictorID predictorID, REpiceaPredictor predictor) {
		getImmutable().predictors.put(predictorID.predictorName, predictor);
	}

	@Override
	protected Immutable getImmutable() {
		return (Immutable) immutable;
	}

	@Override
	protected void createImmutable() {
		immutable = new Immutable();
	}

	@Override
	public void init() {
		super.init();
		createMapArray();
	}

	@SuppressWarnings("unchecked")
	private void createMapArray() {
		plotSampleArray = new ExtPlotSample[getNumberRealizations()];
		for (int i = 0; i < getNumberRealizations(); i++) {
			plotSampleArray[i] = createPlotSample(i);
		}
	}

	/**
	 * This method can be overriden in derived class if the PlotSample instance
	 * has to be extended.
	 * 
	 * @param i
	 *            the Monte Carlo realization id
	 * @return a PlotSample instance
	 */
	protected ExtPlotSample createPlotSample(int i) {
		return new ExtPlotSample(this, i);
	}

	@Override
	public void setInterventionResult(boolean b) {
		super.setInterventionResult(b);

		if (getTreeListMapArray() != null) {
			for (ExtPlotSample plotList : plotSampleArray) {
				plotList.setInterventionResult(b);
			}
		}
	}

	@Override
	public void clearTrees() {
		for (ExtPlotSample plotSample : plotSampleArray) {
			plotSample.clearTrees();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection<? extends Tree> getTrees() {
		Collection trees = new ArrayList();
		for (ExtPlotSample plotList : plotSampleArray) {
			trees.addAll(plotList.getTrees());
		}
		return trees;
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

		for (ExtPlotSample plotList : plotSampleArray) {
			coll.addAll(plotList.getTrees(status));
		}
		return coll;
	}

	@Override
	public Set<String> getStatusKeys() {
		Set<String> oStatusCollection = new HashSet<String>();

		for (ExtPlotSample plotList : plotSampleArray) {
			oStatusCollection.addAll(plotList.getStatusKeys());
		}
		return oStatusCollection;
	}

	/*
	 * Needed redefinition for getTree (id). Chose the option to return always
	 * the tree with the given id in the standList(0) (could be changed).
	 */
	@Override
	// fc - 27.4.2009 mf2009-10-13
	public Tree getTree(int treeId) {
		Tree t = null;
		for (TreeList stand : getPlotSample().getPlotMap().values()) {
			if (stand.getTree(treeId) != null) {
				t = stand.getTree(treeId);
			}
		}
		return t;
	}

	@Override
	// mf2009-10-13
	public void removeTree(Tree tree) {
		for (int i = 0; i < plotSampleArray.length; i++) {
			for (TreeList stand : getRealization(i).getPlotMap().values()) {
				if (stand.getTrees().contains(tree)) {
					stand.removeTree(tree);
				}
			}
		}
	}

	/**
	 * This method add a stand in the stand map
	 * 
	 * @param i
	 *            the Monte Carlo iteration
	 * @param strID
	 *            the stand id
	 * @param stand
	 *            the AbstractExtendedTreeList object
	 */
	public void addTreeList(int i, String strID, ExtPlot stand) {
		getRealization(i).getPlotMap().put(strID, stand);
		stand.setStratumPointer(this);
	}

	// @Deprecated
	// protected void setTreeListMap(int i, Map<String,
	// AbstractExtendedTreeList> treeListMap) {
	// standMapArray[i] = treeListMap;
	// }

	/**
	 * This method overrides the super method to make sure the evolution
	 * tracking is independent from step to step
	 */
	@Override
	public GScene getLightClone() {

		MemoryWatchDog.checkAvailableMemory();

		ExtCompositeStand lightClone = (ExtCompositeStand) super.getLightClone();

		// A copy of the evolution tracking is made
		List<ExtEvolutionParameters> oVec = new ArrayList<ExtEvolutionParameters>();
		for (ExtEvolutionParameters param : evolutionVector) {
			oVec.add(param.clone());
		}
		// this copy is put into the light clone
		lightClone.evolutionVector = oVec;

		return lightClone;
	}

	/**
	 * getHeavyClose method is redefined because the ArtCompositeStand object
	 * may contain many ArtStand objects
	 * 
	 * @see capsis.defaulttype.GTCStand#getHeavyClone()
	 */
	@Override
	public GScene getHeavyClone() {

		ExtCompositeStand heavyCompositeStand = (ExtCompositeStand) getLightClone();

		for (int i = 0; i < plotSampleArray.length; i++) {
			for (ExtPlot stand : getRealization(i).getPlotMap().values()) {
				ExtPlot heavyStand = stand.getHeavyClone();
				heavyCompositeStand.addTreeList(i, heavyStand.getSourceName(), heavyStand);
			}
		}
		return heavyCompositeStand;
	}

	@Override
	public String toString() {
		return "CompositeTreeList_" + getCaption();
	}

	/**
	 * This method sets a map of EnumProperty instances which represent the
	 * species.
	 * 
	 * @param speciesGroupTags
	 *            a Map of EnumProperty instances
	 */
	public void setSpeciesGroupTag(Map<Integer, EnumProperty> speciesGroupTags) {
		getImmutable().speciesGroupTags = speciesGroupTags;
	}

	/**
	 * This method returns the map of group tags for the species.
	 * 
	 * @return a Map of integers and EnumProperty instances
	 */
	public Map<Integer, EnumProperty> getSpeciesGroupTags() {
		return getImmutable().speciesGroupTags;
	}

	/**
	 * This method returns the TreeList that matches the following parameters
	 * 
	 * @param iter
	 *            the Monte Carlo realization
	 * @param strID
	 *            the TreeList id
	 * @return a TreeList instance
	 */
	public TreeList getTreeList(int iter, String strID) {
		return plotSampleArray[iter].getPlotMap().get(strID);
	}

	/**
	 * This method returns the map of TreeList instance.
	 * 
	 * @return a TreeMap of Strings and TreeList instances
	 */
	public ExtPlotSample[] getTreeListMapArray() {
		return plotSampleArray;
	}

	/**
	 * This method returns all the TreeList instances of a particular Monte
	 * Carlo realization.
	 * 
	 * @param iter
	 *            the MonteCarlo realization
	 * @return a Map of Strings and TreeList instances
	 */
	@Override
	public ExtPlotSample getRealization(int iter) {
		return plotSampleArray[iter];
	}

	/**
	 * This method returns the TreeList instance of the first realization, which
	 * is the default in case of deterministic simulation.
	 * 
	 * @return a Map of Strings and TreeList instances
	 */
	public ExtPlotSample getPlotSample() {
		return getRealization(0);
	}

	@Override
	public int getNumberRealizations() {
		return getImmutable().numberIterationMC;
	}

	@Override
	public boolean isStochastic() {
		return getImmutable().isStochastic;
	}

	/**
	 * This method sets the stratum name.
	 * 
	 * @param stratumName
	 *            a String
	 */
	public void setStratumName(String stratumName) {
		getImmutable().stratumName = stratumName;
	}

	/**
	 * This method returns the stratum name.
	 * 
	 * @return a String
	 */
	public String getStratumName() {
		return getImmutable().stratumName;
	}

	@Override
	public double getArea() {
		if (getImmutable().area == 0d) {
			getImmutable().area = getPlotSample().getArea();
		}
		return getImmutable().area;
	}

	/**
	 * This method returns the evolution tracking of the composite tree list.
	 * 
	 * @return a List of ExtendedEvolutionParameters
	 */
	public List<ExtEvolutionParameters> getEvolutionTracking() {
		return evolutionVector;
	}

	// /**
	// * This method serves to dump all the stand in the stand map from one
	// AbstractQuebecMRNFCompositeStand
	// * object to another. Used in the Monte Carlo implementation
	// * @param compositeStand
	// * @param iStartIndex
	// */
	// @Deprecated
	// public void add(CompositeTreeList compositeStand, int iStartIndex) {
	// Map<String, AbstractExtendedTreeList> oStandMap;
	// for (int i = iStartIndex; i <
	// compositeStand.getTreeListMapArray().length; i++) {
	// oStandMap = compositeStand.getTreeListMap(i);
	// if (!oStandMap.isEmpty()) {
	// setTreeListMap(i, oStandMap);
	// compositeStand.setTreeListMap(i, null);
	// }
	// }
	// }

	@SuppressWarnings("rawtypes")
	@Override
	public String getToolTip() {
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(1);
		ExtMethodProvider mp = new ExtMethodProvider();
		double areaFactor = 10000d / getArea();
		String mode = MessageID.DeterministicMode.toString() + ", ";
		boolean plural = getNumberRealizations() > 1;
		if (isStochastic()) {
			mode = MessageID.StochasticMode.toString() + " (" + getNumberRealizations() + " "
					+ MessageID.Realization.toString();
			if (plural) {
				mode = mode.concat("s");
			}
			mode = mode.concat(")");
		}
		String sampleSize = MessageID.SampleSize.toString() + ": " + getPlotSample().size();
		return "<html>" + mode + "<br>" + sampleSize + "<br>" + "N = "
				+ formatter.format(mp.getN(this, getTrees()) * areaFactor) + " " + MessageID.StemDensity.toString()
				+ "<br>" + "G = " + formatter.format(mp.getG(this, getTrees()) * areaFactor) + " "
				+ MessageID.BasalArea.toString();
	}

	/**
	 * This method returns a map whose keys are the species names and values are
	 * the collection of QuebecMRNFTree instances corresponding to the species
	 * 
	 * @param trees
	 *            a Collection of AllometryCalculableTree instances
	 * @return the above mentioned map
	 */
	public Map<String, Collection<AllometryCalculableTree>> getCollectionsBySpecies() {
		Collection<EnumProperty> index = getSpeciesGroupTags().values();
		return getCollectionsBySpecies(index, getTrees());
	}

	/**
	 * This method sorts the Tree instance and produce a series of
	 * species-specific collections.
	 * 
	 * @param index
	 *            a Vector of integers that represent the species groups
	 * @param trees
	 *            a Collection of trees
	 * @return a Map instance with at least one empty collection for the
	 *         all-species category
	 */
	public Map<String, Collection<AllometryCalculableTree>> getCollectionsBySpecies(Collection<EnumProperty> index,
			Collection trees) {
		Map<String, Collection<AllometryCalculableTree>> speciesCollection = new TreeMap<String, Collection<AllometryCalculableTree>>();
		for (EnumProperty speciesGroupTag : index) {
			Collection<AllometryCalculableTree> oMap = new ArrayList<AllometryCalculableTree>();
			speciesCollection.put(speciesGroupTag.getName(), oMap);
		}

		for (Object tree : trees) {
			speciesCollection.get(((ExtTree) tree).getSpeciesName()).add((AllometryCalculableTree) tree);
		}

		speciesCollection.put(ExtSimulationSettings.ALL_SPECIES, (Collection<AllometryCalculableTree>) trees); // add
																												// the
																												// whole
																												// collection
																												// of
																												// living
																												// trees
																												// in
																												// the
																												// all-species
																												// collection
		return speciesCollection;
	}

	@Override
	public double getAreaHa() {
		return getArea() * .0001;
	}

	@Override
	public String getStandIdentification() {
		return getStep().getProject().getName() + " - " + getStep().getName();
	}

	@Override
	public int getDateYr() {
		return getDate();
	}

}
