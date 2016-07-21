/*
 * This file is part of the LERFoB modules for Capsis4.
 *
 * Copyright (C) 2009-2014 UMR 1092 (AgroParisTech/INRA) 
 * Contributors Jean-Francois Dhote, Patrick Vallet,
 * Jean-Daniel Bontemps, Fleur Longuetaud, Frederic Mothe,
 * Laurent Saint-Andre, Ingrid Seynave, Mathieu Fortin.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package capsis.extension.intervener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.extension.intervener.RdiAutoThinner.RDIAutoThinnerSettings;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.lib.lerfobutil.DichotomicSolver;
import capsis.lib.lerfobutil.Trees;
import capsis.util.methodprovider.GCoppiceProvider;
import capsis.util.methodprovider.RdiProviderEnhanced.RdiTool;

/**
 * The RdiAutoThinnerScoredTreeCollection class sorts the trees and marks those to be harvested. 
 * The Tree instances are wrapped into RdiAutoThinnerScoredTree objects.
 * @author F. Mothe, G. LeMoguedec - May 2010
 * @author Mathieu Fortin (refactoring) - May 2014
 */
public class RdiAutoThinnerScoredTreeCollection {

	static class ScoreComparator implements Comparator<RdiAutoThinnerScoredTree> {
		private final boolean ascending;
		
		ScoreComparator(boolean ascending) {
			this.ascending = ascending;
		}
		
		public int compare(RdiAutoThinnerScoredTree et1, RdiAutoThinnerScoredTree et2) {
			int r = Double.compare(et1.getCuttingScore(), et2.getCuttingScore());
			if (r == 0) {
				r = Integer.signum(et1.t.getId() - et2.t.getId());
			}
			return ascending ? r : -r;
		}
	}
	
	protected final GModel model;
	private final GScene stand;
	private final double area_ha;
	private final RdiAutoThinnerStandData initialStandCharacteristics;
	private final List<RdiAutoThinnerScoredTree> scoredTrees;
	
	private double dMin;
	private double dMax;

	protected RdiAutoThinnerScoredTreeCollection(GModel model, GScene stand, Random randomGenerator) {

		this.model = model;
		this.stand = stand;
		this.area_ha = stand.getArea() * .0001;

		TreeCollection tc = (TreeCollection) stand;
		Tree[] trees = Trees.arraySortedByDbh(tc.getTrees(), true);	// ascending
		if (tc.getTrees().size() > 0) {
			dMin = trees[0].getDbh();
			dMax = trees[tc.getTrees().size() - 1].getDbh();
		} else {
			dMin = 0d;
			dMax = 0d;
		}

		//            this.N = 0.;
		scoredTrees = new ArrayList<RdiAutoThinnerScoredTree>(tc.getTrees().size());
		for (Tree t : trees) {
			RdiAutoThinnerScoredTree st = new RdiAutoThinnerScoredTree(t, randomGenerator.nextDouble());
			//                N += st.getNumber();
			scoredTrees.add(st);
		}
		initialStandCharacteristics = calcInitialStandData();
	}

	private RdiAutoThinnerStandData calcInitialStandData() {
		double sum = 0.;
		double numberOfStems = 0d;
		for (RdiAutoThinnerScoredTree st : scoredTrees) {
			numberOfStems += st.getNumber();
			sum += st.getDbhCm() * st.getDbhCm() * st.getNumber();
		}
		double basalAreaM2 = sum * Math.PI / 40000.;	// m2
		double mdqCm = Math.sqrt(sum / numberOfStems);
		double rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, model, numberOfStems / area_ha, mdqCm);
		MethodProvider mp = model.getMethodProvider();
		RdiAutoThinnerStandData data = new RdiAutoThinnerStandData(numberOfStems, basalAreaM2, mdqCm, rdi, area_ha);	
		if (mp instanceof GCoppiceProvider) {
			double gCoppice = ((GCoppiceProvider) mp).getCoppiceG(stand, null);		
			data.setCoppiceBasalAreaM2(gCoppice);
		}
		return data;
	}

	
	protected double[] retrieveHistogram(boolean before, int histoNbClasses) {
		double[] histo = new double[histoNbClasses];
		if (histoNbClasses > 0) {
			for (int n = 0; n < histoNbClasses; ++n) {
				histo[n] = 0;
			}
			double histoCoef = dMax > dMin ? (histoNbClasses - 1) / (dMax - dMin) : 0.;
			for (RdiAutoThinnerScoredTree st : scoredTrees) {
				if (!st.toCut || before) {		// if pre harvesting characteristics are needed then before ensure this condition is true
					int n = (int) ((st.getDbhCm() - dMin) * histoCoef);
					n = Math.min(Math.max(n, 0), histoNbClasses - 1);
					histo[n] += st.getNumber();
				}
			}
		}
		return histo;
	}

	private void removeTree(Tree t) {
		if (model.isMarkModel()) {
			t.setMarked(true);
		} else {
			((TreeCollection) stand).removeTree(t);
			if (t instanceof Numberable) {
				((TreeList) stand).storeStatus((Numberable) t, StatusClass.cut.name (), ((Numberable) t).getNumber());
			} else {
				((TreeList) stand).storeStatus(t, StatusClass.cut.name());
			}
		}
	}

	private double getExpectedRdi(double slope, double intercept) {
		double sum = 0.;
		double expectedN = 0.;
		for (RdiAutoThinnerScoredTree st : scoredTrees) {
			double n = st.getExpectedRemainingNumber(slope, intercept);
			sum += n * st.getDbhCm() * st.getDbhCm();
			expectedN += n;
		}

		double rdi;
		if (expectedN > 0.) {
			double Dg_cm = Math.sqrt(sum / expectedN);
			rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand,
					model,
					expectedN / area_ha,
					Dg_cm);
		} else {
			rdi = 0.;
		}
		return rdi;
	}

	private double solveInterceptForRdi(double objectiveRdi, final double slope, double iMin, double iMax) {
		class Solver extends DichotomicSolver {
			@Override
			// Monotone decreasing function:
			public double function(double intercept) {
				return getExpectedRdi(slope, intercept);
			}
		}
		return new Solver().solveInRange(objectiveRdi, iMax, iMin);
	}

	/**
	 * Returns a slope in the range [-inf, +inf] from a coef in the range [-1, +1]
	 */
	private double getSlope(double thinningCoef) {
		final double powCoef = 2.;	// to increase the accuracy near 0
		double t = Math.signum(thinningCoef) * Math.pow(Math.abs(thinningCoef), powCoef);
		double x = Math.min(Math.max(t, -1.), 1.);
		return Math.log((1. + x) / (1. - x));
	}

	/**
	 * Returns Dg of the full population from Dg and N of two subpopulations (n1 or n2 may be negative to remove trees)
	 */
	private double getTotalDg(double N1, double Dg1, double N2, double Dg2) {
		double totalN = N1 + N2;
		return totalN > 0. ? Math.sqrt((N1 * Dg1 * Dg1 + N2 * Dg2 * Dg2) / totalN) : 0.;
	}

	/**
	 * This method returns the future stand characteristic for a given target rdi and a thinning coefficient.
	 * The method first sets the scores of the tree, then simulate the harvesting from the highest to the lowest
	 * score. This is the main method for this class.
	 * @param settings a RDIAutoThinnerSettings instance
	 * @return a StandData instance
	 */
	private RdiAutoThinnerStandData getFutureStandCharacteristics(RDIAutoThinnerSettings settings) {
		double objectiveRdi = settings.getObjectiveRdi();
		double thinningCoef = settings.getThinningCoefficient();
		RdiAutoThinnerStandData after;
		if (objectiveRdi < getInitialStandCharacteristics().getRDI()) {
			if (Math.abs(thinningCoef) >= 1.) {		// the tree score follows the dbh in ascending or descending order
				// infinite slope
				double sign = thinningCoef >= 1. ? 1. : -1.;
				for (RdiAutoThinnerScoredTree st : scoredTrees) {
					st.setCuttingScore(sign * st.getDbhCm());
				}
			} else if (thinningCoef == 0.) {		// the tree score is random
				// null slope
				for (RdiAutoThinnerScoredTree st : scoredTrees) {		
					st.setCuttingScore(st.getUniform());
				}
			} else {
				double slope = getSlope(thinningCoef);
				// System.out.println ("slope=" + slope);
				double iMin, iMax;
				if (slope > 0.) {
					iMin = -slope * dMax;
					iMax = 1. - slope * dMin;
				} else {
					iMin = -slope * dMin;
					iMax = 1. - slope * dMax;
				}
				double intercept = solveInterceptForRdi(objectiveRdi, slope, iMin, iMax);
				// System.out.println ("intercept=" + intercept);
				for (RdiAutoThinnerScoredTree st : scoredTrees) {
					st.setCuttingScore(slope, intercept);
				}
			}
		} else {
			for (RdiAutoThinnerScoredTree st : scoredTrees) {
				st.setCuttingScore(0d);
			}
		}

		// Trees with higher scores will be removed first:
		Collections.sort(scoredTrees, new ScoreComparator(false));
		after = new RdiAutoThinnerStandData(initialStandCharacteristics.getN(), 
				initialStandCharacteristics.getBasalAreaM2(), 
				initialStandCharacteristics.getMeanQuadraticDiameterCm(), 
				initialStandCharacteristics.getRDI(), 
				area_ha);
		for (RdiAutoThinnerScoredTree st : scoredTrees) {
			st.toCut = after.getRDI() > objectiveRdi;
			if (st.toCut) {
				after.setMeanQuadraticDiameterCm(getTotalDg(after.getN(), after.getMeanQuadraticDiameterCm(), -st.getNumber(), st.getDbhCm()));
				after.setN(after.getN() - st.getNumber());
				after.setRDI(RdiTool.getRdiDependingOnMethodProviderInstance(stand, model, after.getN() / area_ha, after.getMeanQuadraticDiameterCm()));
			}
			after.setBasalAreaM2(after.getMeanQuadraticDiameterCm() * after.getMeanQuadraticDiameterCm() * Math.PI * after.getN() / 40000.);
		}
		MethodProvider mp = model.getMethodProvider();
		if (mp instanceof GCoppiceProvider) {
			after.setCoppiceBasalAreaM2(settings.getCoppiceBasalAreaM2());
		}
		return after;
	}


	/**
	 * This method really performs the thinning. The getFutureStandCharacteristics method
	 * needs to be called first in order to set the scores. 
	 * @param objectiveRdi a double between 0 and 1
	 * @param thinningCoef a double between -1 and 1
	 */
	protected boolean thinForRdi() {
		boolean thinned = false;
		for (RdiAutoThinnerScoredTree st : scoredTrees) {
			if (st.toCut) {
				removeTree(st.t);
				thinned = true;
			}
		}
		return thinned;
	}

	/**
	 * This method returns the initial stand characteristics, ie before any harvesting.
	 * @return a StandData instance
	 */
	private RdiAutoThinnerStandData getInitialStand() {return initialStandCharacteristics;}
	
	public RdiAutoThinnerStandData getStandCharacteristics(RDIAutoThinnerSettings settings) {
		if (settings == null) {
			return getInitialStand();
		} else {
			return getFutureStandCharacteristics(settings);
		}
	}

	public RdiAutoThinnerStandData getInitialStandCharacteristics() {
		return getStandCharacteristics(null);
	}

	
}
