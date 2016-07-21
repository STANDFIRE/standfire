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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jeeb.lib.util.Log;
import repicea.math.Matrix;
import repicea.simulation.allometrycalculator.AllometryCalculator;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.StochasticInformationProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.LawOfTotalVarianceMonteCarloEstimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.stats.estimates.SampleMeanEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.util.extendeddefaulttype.methodprovider.AverageRingWidthCmProvider;
import capsis.util.extendeddefaulttype.methodprovider.DDomEstimateProvider;
import capsis.util.extendeddefaulttype.methodprovider.GEstimateProvider;
import capsis.util.extendeddefaulttype.methodprovider.HDomEstimateProvider;
import capsis.util.extendeddefaulttype.methodprovider.NEstimateProvider;
import capsis.util.extendeddefaulttype.methodprovider.PeriodicAnnualIncrementComponentsProvider;
import capsis.util.extendeddefaulttype.methodprovider.VEstimateProviderWithName;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.HdomProvider;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.TotalAboveGroundVolumeProvider;
import capsis.util.methodprovider.VMerchantProviderWithName;
import capsis.util.methodprovider.VProviderWithName;


public class ExtMethodProvider implements GProvider, 
												NProvider, 
												VProviderWithName,
												VMerchantProviderWithName,
												HdomProvider,
												DgProvider,
												DdomProvider,
												TotalAboveGroundVolumeProvider,
												VEstimateProviderWithName, 
												NEstimateProvider,
												GEstimateProvider,
												PeriodicAnnualIncrementComponentsProvider,
												AverageRingWidthCmProvider,
												HDomEstimateProvider,
												DDomEstimateProvider {

	private static final long serialVersionUID = 20100804L;

	private static final Class<?>[] EmptyClassArray = new Class<?>[0];
	
	private static enum MessageID implements TextableEnum {
		CommercialVolume("Merchantable", "Marchand");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString (this, englishText, frenchText);
		}

		@Override
		public String toString() {
			return REpiceaTranslator.getString(this); 
		}

	}


	private static AllometryCalculator ac;

	@SuppressWarnings("rawtypes")
	public double getDg(GScene compositeStand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

			double G = getG(compositeStand, trees);
			double N = getN(compositeStand, trees);
			double Dg = Math.sqrt(G *40000 / (N*Math.PI));
			return Dg;
		} catch (Exception e) {
			Log.println (Log.ERROR, "QuebecMRNFMethodProvider.getDg ()", 
					"Error while computing Dg", e);
			return -1d;
		}
	}


	/*	
	 * G: basal area (m2). Dbh must be in cm. 
	 * Square of dbh are weighted by the number of stems(non-Javadoc)
	 * @see capsis.util.methodprovider.DgProvider#getDg(capsis.kernel.GScene, java.util.Collection)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public double getG(GScene compositeStand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

			int numberIterationMC = 1;
			if (compositeStand instanceof StochasticInformationProvider) {
				numberIterationMC = ((StochasticInformationProvider) compositeStand).getNumberRealizations();
			}
			ExtTree t;

			double basalArea = 0;
			for (Object tree : trees) {
				t = (ExtTree) tree;
				if (t.getNumber() > 0) {
					basalArea += t.getStemBasalAreaM2() * t.getNumber() * ((ExtPlot) t.getScene()).getWeight();			// plot weight added mf2009-08-18
				}
			}
			return basalArea / numberIterationMC;
		} catch (Exception e) {
			Log.println(Log.ERROR, "ExtendedMethodProvider.getG ()", "Error while computing basal area", e);
			return -1d;
		}
	}

	/*	
	 * N: number of trees  
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double getN(GScene compositeStand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

			int numberIterationMC = 1;
			if (compositeStand instanceof StochasticInformationProvider) {
				numberIterationMC = ((StochasticInformationProvider) compositeStand).getNumberRealizations();
			}
			ExtTree t;

			double NumberofStems = 0;
			for (Object tree : trees) {
				t = (ExtTree) tree;
				NumberofStems += t.getNumber() * ((ExtPlot) t.getScene()).getWeight();				// plot weight added mf2009-08-18
			}
			return NumberofStems / numberIterationMC;
		} catch (Exception e) {
			Log.println (Log.ERROR, "ExtendedMethodProvider.getN ()", "Error while computing number of stems", e);
			return -1d;
		}
	}

	/*	
	 * V: Volume (m3).  
	 * Merchantable volume are weighted by the number of stems (non-Javadoc)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double getV(GScene compositeStand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

			int numberIterationMC = 1;
			if (compositeStand instanceof StochasticInformationProvider) {
				numberIterationMC = ((StochasticInformationProvider) compositeStand).getNumberRealizations();
			}
			ExtTree t;

			double volume = 0;
			for (Object tree : trees) {
				t = (ExtTree) tree;
				if (t.getNumber() > 0) {
					volume += t.getCommercialVolumeM3() * t.getNumber() * ((ExtPlot) t.getScene()).getWeight();				
				}
			}
			return volume / numberIterationMC;
		} catch (Exception e) {
			Log.println (Log.ERROR, "QuebecMRNFMethodProvider.getV ()", 
					"Error while computing plot volume", e);
			return -1d;
		}
	}

	
	@Override
	public Estimate<?> getVolumePerHaEstimate(GScene compositeStand, Collection trees) {
		try {
			Method method = ExtTree.class.getDeclaredMethod("getCommercialVolumeM3", EmptyClassArray);
			Estimate<?> estimate = getEstimatePerHaForThisMethod(compositeStand, trees, method);
			return estimate;
		} catch (Exception e) {
			Log.println (Log.ERROR, "ExtendedMethodProvider.getVolumePerHaEstimate()", "Error while computing plot sample volume", e);
			return null;
		}
	}

	
	private Estimate<?> getEstimatePerHaForThisMethod(GScene compositeStand, Collection trees, Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		int numberRealizations = ((ExtCompositeStand) compositeStand).getNumberRealizations();
		ExtPlotSample plotSample = ((ExtCompositeStand) compositeStand).getRealization(0);
		Set<String> plotIDs = plotSample.getPlotMap().keySet();

		Map<String, Double>[] results = new TreeMap[numberRealizations];
		for (int i = 0; i < numberRealizations; i++) {
			Map<String, Double> valueMap = new TreeMap<String, Double>();		// TODO check if the order persists
			for (String plotID : plotIDs) {
				valueMap.put(plotID, 0d);
			}
			results[i] = valueMap;
		}
		ExtTree t;
		double expansionFactor;
		
		for (Object tree : trees) {
			t = (ExtTree) tree;
			int monteCarloRealizationID = t.getMonteCarloRealizationId();
			ExtPlot treeList = (ExtPlot) t.getScene();
			expansionFactor = 1d / treeList.getAreaHa();
			if (t.getNumber() > 0) {
				double formerValue = results[monteCarloRealizationID].get(treeList.getId());
				Double value;
				if (method == null) {
					value = 1d;
				} else {
					value = (Double) method.invoke(t, (Object[]) null);
				}
				Double newValue = value * t.getNumber() * treeList.getWeight() * expansionFactor;
//				if (newValue.isInfinite() || newValue.isNaN()) {
//					int u = 0;
//				}
				results[monteCarloRealizationID].put(treeList.getId(), formerValue + newValue);				
			}
		}
					
		Estimate<?> estimate; 
		if (plotIDs.size() > 1) {
			estimate = new LawOfTotalVarianceMonteCarloEstimate();
		} else {
			estimate = new MonteCarloEstimate();
		}
		for (int i = 0; i < numberRealizations; i ++) {
			SampleMeanEstimate sampleEstimate = new SampleMeanEstimate();
			Matrix mat;
			for (Double obs : results[i].values()) {
				mat = new Matrix(1,1);
				mat.m_afData[0][0] = obs;
				sampleEstimate.addObservation(mat);
			}
			if (estimate instanceof LawOfTotalVarianceMonteCarloEstimate) {
				((LawOfTotalVarianceMonteCarloEstimate) estimate).addRealization(sampleEstimate);
			} else {
				((MonteCarloEstimate) estimate).addRealization(sampleEstimate.getMean());
			}
		}
		
		return estimate;
	}
	
//	private Matrix getMean(Collection<Double> values) {
//		double sum = 0;
//		for (Double value : values) {
//			sum += value;
//		}
//		Matrix output = new Matrix(1,1);
//		output.m_afData[0][0] = sum / values.size();
//		return output;
//	}
//	
//	private Matrix getVarianceOfTheMean(Collection<Double> values, double mean) {
//		double sse = 0;
//		for (Double value : values) {
//			double diff = value - mean;
//			sse += diff * diff;
//		}
//		Matrix output = new Matrix(1,1);
//		output.m_afData[0][0] = sse / (values.size() - 1) / values.size();
//		return output;
//	}
	
	@Override
	public Estimate<?> getNPerHaEstimate(GScene compositeStand, Collection trees) {
		try {
			Estimate<?> estimate = getEstimatePerHaForThisMethod(compositeStand, trees, null);	// null for frequencies
			return estimate;
		} catch (Exception e) {
			Log.println (Log.ERROR, "ExtendedMethodProvider.getV ()", "Error while computing plot volume", e);
			return null;
		}
	}

	@Override
	public Estimate<?> getGPerHaEstimate(GScene compositeStand, Collection trees) {
		try {
			Method method = ExtTree.class.getDeclaredMethod("getStemBasalAreaM2", EmptyClassArray);
			Estimate<?> estimate = getEstimatePerHaForThisMethod(compositeStand, trees, method);
			return estimate;
		} catch (Exception e) {
			Log.println (Log.ERROR, "QuebecMRNFMethodProvider.getV ()", "Error while computing plot volume", e);
			return null;
		}
	}
	
	@Override
	public Estimate<?> getHdomEstimate(GScene compositeStand, Collection trees) {
		try {
			int numberRealizations = ((ExtCompositeStand) compositeStand).getNumberRealizations();
			Collection<ExtTree>[] collections = getTreeCollectionsByRealization((ExtCompositeStand) compositeStand, trees);

			MonteCarloEstimate estimate = new MonteCarloEstimate();
			Matrix realization;
			for (int i = 0; i < numberRealizations; i++) {
				realization = new Matrix(1,1);
				realization.m_afData[0][0] = getHdom(compositeStand, collections[i]); 
				estimate.addRealization(realization);
			}
			
			return estimate;
		} catch (Exception e) {
			Log.println (Log.ERROR, "ExtendedMethodProvider.getHDomEstimate()", "Error while computing dominant height", e);
			return null;
		}
	}

	@Override
	public Estimate<?> getDdomEstimate(GScene compositeStand, Collection trees) {
		try {
			int numberRealizations = ((ExtCompositeStand) compositeStand).getNumberRealizations();
			Collection<ExtTree>[] collections = getTreeCollectionsByRealization((ExtCompositeStand) compositeStand, trees);

			MonteCarloEstimate estimate = new MonteCarloEstimate();
			Matrix realization;
			for (int i = 0; i < numberRealizations; i++) {
				realization = new Matrix(1,1);
				realization.m_afData[0][0] = getDdom(compositeStand, collections[i]); 
				estimate.addRealization(realization);
			}
			
			return estimate;
		} catch (Exception e) {
			Log.println (Log.ERROR, "ExtendedMethodProvider.getHDomEstimate()", "Error while computing dominant height", e);
			return null;
		}
	}

	private Collection<ExtTree>[] getTreeCollectionsByRealization(ExtCompositeStand compositeStand, Collection<ExtTree> trees) {
		int numberRealizations = ((ExtCompositeStand) compositeStand).getNumberRealizations();
		Collection<ExtTree>[] collections = new ArrayList[numberRealizations];
		for (int i = 0; i < numberRealizations; i++) {
			collections[i] = new ArrayList<ExtTree>();
		}
		ExtTree t;
		for (Object tree : trees) {
			t = (ExtTree) tree;
			int monteCarloRealizationID = t.getMonteCarloRealizationId();
			collections[monteCarloRealizationID].add(t);
		}
		return collections;
	}
	
	
	
	/**
	 * VProviderWithName Returns the name of volume.
	 */
	@Override
	public String getVolumeName() {
		return MessageID.CommercialVolume.toString();
	}

	/*
	 * getHdom provides the average height for the dominant trees which are by definition the
	 * x tallest trees. x value is set in ArtUtility
	 */
	@SuppressWarnings("rawtypes")
	public double getHdom(GScene stand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

			double plotAreaHa = ((AreaHaProvider) stand).getAreaHa();
			double domHeight = getAllometryCalculator().getDominantHeightM(trees, plotAreaHa, true);
			return domHeight;	
		} catch (Exception e) {
			Log.println (Log.ERROR, "QuebecMRNFMethodProvider.getHdom ()", 
					"Error while computing dominant height", e);
			return -1d;
		}
	}

	/*
	 * getDdom provides the average diameter for the dominant trees which are by definition the
	 * x tallest trees in terms of DBH. x value is set in ArtUtility
	 */
	@SuppressWarnings("rawtypes")
	public double getDdom (GScene stand, Collection trees) {
		try {
			if (trees == null) {return -1d;}
			if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

			double plotAreaHa = ((AreaHaProvider) stand).getAreaHa();
			double domDiameter = getAllometryCalculator().getDominantDiameterCM(trees, plotAreaHa, true);
			return domDiameter;	
		} catch (Exception e) {
			Log.println (Log.ERROR, "ArtMethodProvider.getDdom ()", 
					"Error while computing dominant diameter", e);
			return -1d;
		}
	}

	protected AllometryCalculator getAllometryCalculator() {
		if (ac == null) {
			ac = new AllometryCalculator();
		}
		return ac;
	}


	@Override
	public double getTotalAboveGroundVolume(GScene stand, Collection trees) {
		if (trees == null) {return -1d;}
		if (trees.isEmpty ()) {return 0d;}		// if no trees, return 0

		int numberIterationMC = ((ExtCompositeStand) stand).getNumberRealizations();
		ExtTree t;

		double volume = 0;
		for (Object tree : trees) {
			t = (ExtTree) tree;
			if (t.getNumber() > 0) {
				volume += t.getTotalVolumeDm3() * .001 * t.getNumber() * ((ExtPlot) t.getScene()).getWeight();		
			}
		}
		return volume / numberIterationMC;
	}


	@Override
	public double getVMerchant(GScene stand, Collection trees) {
		return getV(stand, trees);
	}


	@Override
	public String getVMerchantName () {
		return getVolumeName();
	}

	private double getVariableCalculated(GScene compositeStand, Collection trees, Variable var) {
		switch (var) {
		case N:
			return getN(compositeStand, trees);
		case G:
			return getG(compositeStand, trees);
		case V:
			return getV(compositeStand, trees);
		default:
			return -1;
		}
	}
	
	private ExtTree getFormerTree(ExtTree currentTree) {
		ExtPlot currentStand = (ExtPlot) currentTree.getScene();
		ExtPlot formerStand = (ExtPlot) ((ExtCompositeStand) getPreviousScene(currentStand.getStratum())).getTreeList(currentStand.getMonteCarloRealizationId(), currentStand.getId());
		ExtTree formerTree = (ExtTree) formerStand.getTree(currentTree.getId());
		return formerTree;
	}
	
	private GScene getPreviousScene(ExtCompositeStand currentCompositeTreeList) {
		return ((Step) currentCompositeTreeList.getStep().getFather()).getScene();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double getPAIComponents(GScene compositeStand, GrowthComponent component, Variable variable) {
		double output = 0d;
		
		if (!compositeStand.getStep().isRoot() && !compositeStand.isInterventionResult()) {
//			GScene fatherStand = ((Step) compositeStand.getStep().getFather()).getScene();
			int timeInterval = compositeStand.getDate() - getPreviousScene((ExtCompositeStand) compositeStand).getDate();
			double intervalFactor = (double) 1 / timeInterval;
				
			Collection<Tree> trees;
			
			switch (component) {

			case Mortality:
				trees = ((ExtCompositeStand) compositeStand).getTrees(StatusClass.dead);
				output = - getVariableCalculated(compositeStand, trees, variable) * intervalFactor;
				break;
				
			case Recruitment:
				trees = new ArrayList<Tree>();
				for (Tree t : ((TreeList) compositeStand).getTrees()) {
					if (t.getAge() == 0) {
						trees.add(t);
					}
				}
				output = getVariableCalculated(compositeStand, trees, variable) * intervalFactor;
				break;
				
			case SurvivorGrowth:
				trees = new ArrayList<Tree>();
				for (Tree t : ((TreeList) compositeStand).getTrees()) {
					if (t.getAge() > 0) {
						trees.add(t);
					}
				}
	
				double monteCarloFactor = 1d;
				if (((ExtCompositeStand) compositeStand).getNumberRealizations() > 1) {
					monteCarloFactor = (double) 1 / ((ExtCompositeStand) compositeStand).getNumberRealizations();
				}
				
				output = 0d;
				
				double currentValue;
				double formerValue;
				
				for (Tree tree : trees) {
					ExtTree currentTree = (ExtTree) tree;
					ExtPlot currentStand = (ExtPlot) currentTree.getScene();
//					AbstractExtendedTreeList formerStand = (AbstractExtendedTreeList) ((CompositeTreeList) fatherStand).getTreeList(currentStand.getMonteCarloRealizationId(), currentStand.getId());
//					ExtendedTree formerTree = (ExtendedTree) formerStand.getTree(currentTree.getId());
					ExtTree formerTree = getFormerTree(currentTree);
					if (variable == Variable.N) {
						currentValue = currentTree.getNumber();
						formerValue = formerTree.getNumber();
					} else if (variable == Variable.G) {
						currentValue = currentTree.getStemBasalAreaM2() * currentTree.getNumber();
						formerValue = formerTree.getStemBasalAreaM2() * formerTree.getNumber();
					} else {
						currentValue = currentTree.getCommercialVolumeM3() * currentTree.getNumber();
						formerValue = formerTree.getCommercialVolumeM3() * formerTree.getNumber();
					}
					output += (currentValue - formerValue) * monteCarloFactor * intervalFactor * currentStand.getWeight();
				}
				
				if (((ExtCompositeStand) compositeStand).getNumberRealizations() == 1) {	// deterministic : mortality must be subtracted
					output -= getPAIComponents(compositeStand, GrowthComponent.Mortality, variable); 
				}
				break;

			}
		} 
		return output;
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Estimate<?> getAverageRingWidthCm(GScene compositeStand, Collection trees) {
		int nbRealizations = ((ExtCompositeStand) compositeStand).getNumberRealizations();
		double[] output = new double[nbRealizations];
		double[] n = new double[nbRealizations];
		if (!compositeStand.getStep().isRoot() && !compositeStand.isInterventionResult()) {
			int timeInterval = compositeStand.getDate() - getPreviousScene((ExtCompositeStand) compositeStand).getDate();
			double intervalFactor = (double) 1 / timeInterval;
			
			for (Object tree : trees) {
				ExtTree currentTree = (ExtTree) tree;
				ExtPlot currentStand = (ExtPlot) currentTree.getScene();
				ExtTree formerTree = getFormerTree(currentTree);
				if (formerTree != null) {
					output[currentStand.getMonteCarloRealizationId()] += (currentTree.getDbhCm() - formerTree.getDbhCm()) * currentTree.getNumber() * intervalFactor * currentStand.getWeight();
					n[currentStand.getMonteCarloRealizationId()] += currentTree.getNumber();
				}
			}
		}
		
		MonteCarloEstimate outputEstimate = new MonteCarloEstimate();
		Matrix realization;
		for (int i = 0; i < nbRealizations; i++) {
			realization = new Matrix(1,1);
			realization.m_afData[0][0] = output[i] / n[i]; // we divide the sum of the weighted ring width by the number of trees to get the average ring width
			outputEstimate.addRealization(realization);
		}
		
		return outputEstimate;
	}

}
