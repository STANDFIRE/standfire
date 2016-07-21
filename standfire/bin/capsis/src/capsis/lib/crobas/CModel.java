package capsis.lib.crobas;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import jeeb.lib.util.DefaultNumberFormat;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import uqar.jackpine.model.JpTree;
import capsis.util.TreeHeightComparator;

/**
 * Model script for stand growth using Crobas (A. Makela 1997 For. Sci.) as the
 * growth engine. Homogeneous, monospecific stands can be simulated with
 * homogeneousStandGrowth () method. Some methods rely on Fortran code given by
 * A. Makela, mai 2006.
 * 
 * @author R. Schneider - 20.5.2008
 */

public class CModel implements Serializable {

	static {
		Translator.addBundle("capsis.lib.crobas.CModel");
	}

	/**
	 * Default constructor.
	 */
	public CModel() {
	}

	/**
	 * Must be called after creation of the initial Stand, creates the whorls.
	 * If no PipeQualSpecies in CSpecies, no whorls will be initiated.
	 */
	public void initialize(CStand initStand, CSettings sets) throws Exception {
		// CTree t1 = (CTree) initStand.getTrees ().iterator ().next ();
		// CSpecies sp = t1.getCSpecies ();

		for (Iterator i = initStand.getTrees().iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();

			// CSpecies sp = tree.getCSpecies ();
			// if (sp.getPipeQualSpecies () != null) {

			if (tree.isPipeQualLevel()) { // fc-18.2.2011

				tree.initWhorls(sets);
				tree.whorlInteractions();
			}
		}

		initStand.calculateStandVariables();
	}

	/**
	 * Tests the given stand to check that all trees have the same species. If
	 * not, throws an exception. Accepts single species and several
	 * crobasLevels.
	 */
	private void checkStandIsHomogeneous(CStand stand) throws Exception {
		// fc-13.1.2011
		CTree t1 = (CTree) stand.getTrees().iterator().next();
		CSpecies sp = t1.getCSpecies();
		for (Iterator i = stand.getTrees().iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();
			if (tree.getCSpecies().getValue() != sp.getValue()) {
				throw new Exception(
						"Stand is not homogeneous, found at least 2 species: tree "
								+ t1.getId() + " is " + sp.getName()
								+ " and tree " + tree.getId() + " is "
								+ tree.getCSpecies().getName());
			}
		}

	}

	/**
	 * Crobas model, with optional PipeQual model. Stand growth main method.
	 * Checks if one or several species in newStand, triggers
	 * homogeneousStandGrowth () or mixedStandGrowth () accordingly.
	 */
	public void standGrowth(CStand prevStand, CStand newStand,
			int numberOfStepsInYear, CSettings sets) throws Exception {

		// Is there only one single species in the stand ?
		boolean singleSpecies = true;
		try {
			// Throws an exception if not a single species
			// Note: we may have one single species and several crobasLevels
			checkStandIsHomogeneous(newStand); // fc-13.1.2011
		} catch (Exception e) {
			singleSpecies = false;
		}

		if (singleSpecies) {
			// One single species
			homogeneousStandGrowth(prevStand, newStand, numberOfStepsInYear,
					sets);
		} else {
			// Several species
			mixedStandGrowth(prevStand, newStand, numberOfStepsInYear, sets);
		}

	}

	/**
	 * Crobas model, with optional PipeQual model for plurispecific,
	 * heterogeneous stands
	 */
	private void mixedStandGrowth(CStand prevStand, CStand newStand,
			int numberOfStepsInYear, CSettings sets) throws Exception {

		// At this time, mixed stands are processed with the same method than
		// monospecific stands
		// May change in the future (R. Schneider said, fc-18.2.2011)
		homogeneousStandGrowth(prevStand, newStand, numberOfStepsInYear, sets);

	}

	/**
	 * Crobas model, with optional PipeQual model for monospecific, homogeneous
	 * stands
	 */
	private void homogeneousStandGrowth(CStand prevStand, CStand newStand,
			int numberOfStepsInYear, CSettings sets) throws Exception {

		// CTree t1 = (CTree) newStand.getTrees ().iterator ().next ();
		// CSpecies sp = t1.getCSpecies ();

		GrowthEngine e = new GrowthEngine(); // Initialise growth engine

		double cpt = 0;
		for (int k = 0; k < numberOfStepsInYear; k++) {

			cpt += 1d / numberOfStepsInYear;

			// System.out.println
			// ("CModel, homogeneousStandGrowth - before calculateNetGrowth (newStand)");
			for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
				CTree tree = (CTree) i.next();
				// System.out.println ("Tree id"+tree.getId
				// ()+" age "+tree.getAge()+" ksi "+tree.ksi+" z "+tree.z);
			}

			calculateNetGrowth(newStand); // calculate effective leaf area
											// index, maintenance respiration,
											// photosynthesis and net growth

			// System.out.println
			// ("CModel, homogeneousStandGrowth - after calculateNetGrowth (newStand) + before calculateCrownCoverage (newStand);");
			for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
				CTree tree = (CTree) i.next();
				// System.out.println ("Tree id"+tree.getId
				// ()+" age "+tree.getAge()+" ksi "+tree.ksi+" z "+tree.z);
			}

			calculateCrownCoverage(newStand); // calculate crown coverage of the
												// tree

			// System.out.println
			// ("CModel, homogeneousStandGrowth - after calculateCrownCoverage (newStand) + before growth engine;");
			for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
				CTree tree = (CTree) i.next();
				// System.out.println ("Tree id"+tree.getId
				// ()+" age "+tree.getAge()+" ksi "+tree.ksi+" z "+tree.z);
			}

			for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
				CTree tree = (CTree) i.next();

				// double previous_h = tree.getHeight ();

				// fc-21.4.2011 - re-init the growEngine (maybe needed here...)
				e = new GrowthEngine();

				// System.out.println ("CModel, growth for tree "+tree.getId
				// ()+"...");
				// System.out.println
				// ("CModel, before: tree height "+tree.getHeight ());

				e.treeGrowth(tree, numberOfStepsInYear); // calculate growth
															// compartment of a
															// given tree

				// System.out.println
				// ("CModel, after  : tree height "+tree.getHeight ());

				// // Added this test to detect a bug early (caused a bug lower
				// in whorls management)
				// if (tree.getHeight () <= previous_h) {
				// throw new Exception ("CModel.homogeneousStandGrowth ():"
				// +" Tree did not grow (will cause errors when creating the new whorl)"
				// +" tree id: "+tree.getId ()
				// +" previousHeight: "+previous_h+" new height: "+tree.getHeight
				// ()
				// +" species: "+tree.getSpecies ());
				// }

				tree.calculateStemMortality(numberOfStepsInYear); // calculate
																	// mortality
			}

			setStandDensity(newStand); // calculate stand density
		}

		// Update age: + 1 year
		newStand.age += 1;
		for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();
			tree.setAge(tree.getAge() + 1);
		}

		// Optional PipeQual model: calculate whorl level information

		// Refactored the following to allow trees of same species and different
		// crobasLevels - fc-13.1.2011
		for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();

			if (tree.isPipeQualLevel()) { // fc-18.2.2011

				CTree prevTree = (CTree) prevStand.getTree(tree.getId());

				try {
					if (tree.getNumber() != 0) {
						int index0 = prevTree.getWhorls().size() + 1;
						double shootLength = tree.getHeight()
								- prevTree.getHeight();
						double shootBaseHeight = prevTree.getHeight();
						int age = tree.getAge();
						double treeHeight = tree.getHeight();

						// System.out.println
						// ("CModel, updating whorls collection for tree "+tree.getId
						// ()+"...");
						// System.out.println
						// ("CModel before: "+tree.traceWhorls ());

						// fc + rs - 29.5.2008
						tree.calculateFoliageBiomassInNodalWhorls(); // estimate
																		// foliage
																		// biomass
																		// which
																		// are
																		// in
																		// the
																		// nodal
																		// and
																		// internodal
																		// whorls

						tree.updateWhorlCollection(index0, shootLength,
								shootBaseHeight, age, treeHeight, sets); // insert
																			// new
																			// whorls:
																			// 1
																			// if
																			// only
																			// nodal,
																			// >=
																			// 1
																			// if
																			// internodal
																			// whorls
																			// also

						// System.out.println
						// ("CModel after  : "+tree.traceWhorls ());

						tree.whorls(prevTree); // whorl module (Makela 2002 Tree
												// Phys)
						tree.whorlInteractions(prevTree); // tree and whorl
															// module
															// interaction
															// (Makela 2002 Tree
															// Phys)
					}
				} catch (Exception e2) {

					// Searching a null pointer exception - fc
					Object _scene = tree.getScene();
					int _id = tree.getId();
					Object _species = tree.getCSpecies();
					Object _pipeQualSpecies = tree.getCSpecies()
							.getPipeQualSpecies();

					Log.println(Log.ERROR,
							"CModel.homogeneousStandGrowth ()",
							// "date "+newStand.getDate ()
							"target stand age " + newStand.age + "\ntree "
									+ tree.getId() + "\nspecies "
									+ tree.getCSpecies() + "\nPipeQualSpecies "
									+ tree.getCSpecies().getPipeQualSpecies()
									+ "\nprevTree " + prevTree, e2);
					throw e2; // was missing - fc-22.3.2011
				}
			}
		}

		newStand.inGrowth(sets);

		for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();

			tree.calculateStateVariables(); // calculate certain state variables
											// which are not computed by the
											// tree growth
		}

		newStand.calculateStandVariables(); // calculate stand level variables

	}

	/* VENCY start: to compare one target tree with one competitor */// ////////////////////
	// //////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////Modifications start
	// here////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////
	protected double[] compareTrees(CTree targetTree, CTree competTree) {
		// crown height target tree
		double targetht = targetTree.getHeight();
		// height to crown base target tree
		double targeths = targetTree.Hs;
		// crown height competitor tree
		double compht = competTree.getHeight();
		// height to crown base competitor tree
		double comphs = competTree.Hs;
		// crown length target tree
		double targetTreeHc = targetTree.getHeight() - targetTree.Hs;
		// crown length competitor tree
		double competTreeHc = competTree.getHeight() - competTree.Hs;

		// Shading by competitor on target tree

		// leaf area at the crown base
		double c1 = 0; // Competitor crown length above target tree top, raised
						// to square
		double c2 = 0; // Competitor crown length above target tree base, raised
						// to square
		double vrel1 = 0; // relative volume of crown shading at the top of the
							// tree
		double vrel2 = 0; // relative volume of crown shading at the base of the
							// tree
		double lpt1 = 0; // Leaf area of competitor tree above target tree top
		double lpt2 = 0; // Leaf area of competitor tree above target tree base
		double lt = 0; // leaf area above the top of the tree to be incremented
						// for the stand
		double bt = 0; // crown shading the base of the tree to be incremented
						// for the stand

		if (targetht >= compht) {
			// All the target tree crown is above all the crown of the
			// competitor then no shading
			if (targeths >= compht) {
				vrel1 = 0;
				vrel2 = 0;
				lpt1 = 0;
				lpt2 = 0;
			} else {
				if (targeths > comphs) {
					// Competitor crown length above target tree, raised to
					// square
					c1 = 0;
					// comments: cr�er une variable "compht - targeths" pour ne
					// pas la r�p�ter
					c2 = Math.pow((compht - targeths), 2);
					// relative volume of crown shading at the top of the tree
					vrel1 = 0;
					// relative volume of crown shading at the base of the tree
					vrel2 = (compht - targeths) * c2
							/ Math.pow(competTreeHc, 3);
					// Leaf area of competitor tree above target tree top
					lpt1 = 0;
					// Leaf area of competitor tree above target tree base
					lpt2 = competTree.LAI * vrel2;
				} else {
					c2 = Math.pow((compht - comphs), 2);
					vrel2 = (compht - comphs) * c2 / Math.pow(competTreeHc, 3);
					lpt1 = 0;
					lpt2 = competTree.LAI * vrel2;
				}
			}
		} else {
			if (targeths >= comphs) {
				c1 = Math.pow(compht - targetht, 2);
				c2 = Math.pow(compht - targeths, 2);
				vrel1 = (compht - targetht) * (c1) / Math.pow(competTreeHc, 3);
				vrel2 = (compht - targeths) * (c2) / Math.pow(competTreeHc, 3);
				lpt1 = competTree.LAI * vrel1;
				lpt2 = competTree.LAI * vrel2;
			} else {
				c1 = Math.pow(compht - comphs, 2);
				c2 = Math.pow(compht - comphs, 2);
				vrel1 = (compht - comphs) * c1 / Math.pow(competTreeHc, 3);
				vrel2 = (compht - comphs) * c2 / Math.pow(competTreeHc, 3);
				lpt1 = competTree.LAI * vrel1;
				lpt2 = competTree.LAI * vrel2;
			}

		}

		// System.out.println ("CModel-compareTrees, for tree "+targetTree.getId
		// ());
		// System.out.println ("CModel, lpt1 "+lpt1+" lpt2 "+lpt2);

		double[] outputArray = new double[2];
		outputArray[0] = lpt1;
		outputArray[1] = lpt2;
		return outputArray;

	}

	// Compare target tree with trees in the collection
	private double[] compareTargetreeAndList(CTree targetTree,
			Map competitorMap) {

		double[] shading = compareTrees(targetTree, targetTree);

		// Vency ajout� lundi 20 fev 2012
		int ii = 0;
		for (Iterator i = competitorMap.keySet ().iterator(); i.hasNext();) {

			CTree compTree = (CTree) i.next();
			double[] tmpShading = compareTrees(targetTree, compTree);
			shading[0] = shading[0] + tmpShading[0];// represents the bt at tree
													// top
			shading[1] = shading[1] + tmpShading[1];// represents the bt at
													// crown base
		}
		// Vency ajout� lundi 20 fev 2012
		ii++;
		return shading;
	}

	/*
	 * Calculates net growth for the trees in a stand by estimating (1)
	 * effective leaf area of a tree (LAI), (2) maintenance respiration (Rm),
	 * (3) photosynthesis (Pg) and (4) net growth (G = (Pg - Rm) / Y Translation
	 * of Fortran code given by A. Makela, mai 2006
	 */

	private void calculateNetGrowth(CStand newStand) {

		// /verifier ces deux ligne, peut etre un probl�me de cast??????

		Collection trees = new TreeSet(new TreeHeightComparator());
		trees.addAll(newStand.getTrees());

		// double totalLAI = 0;
		// Vency ajout� Lundi 20fev 2012
		int ii = 0;
		for (Iterator i = trees.iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();
			calculateLAI(tree); // Calculate effective Leaf Area Index
			calculateRm(tree);
			// totalLAI = totalLAI + tree.LAI;
			ii++;
		}
		// Vency ajout� Lundi 20fev 2012
		// ii++;
		for (Iterator i = trees.iterator(); i.hasNext();) {
			CTree targetTree = (CTree) i.next();

			CSpecies sp = targetTree.getCSpecies();

			Map competitorMap = getCompetitors(newStand, targetTree);

			// new ArrayList (trees); // a copy of the list
			// competitors.remove (targetTree); // remove the target tree from
			// copy

			double[] totalShading = compareTargetreeAndList(targetTree,
					competitorMap);

			// calculate light extinction ;
			// v�rifier si la sortie de compareTargetreeAndList() correspond �
			// bt[i1] et bt[i2]
			double e1 = Math.exp(-sp.k * totalShading[0])
					- Math.exp(-sp.k * totalShading[1]);
			// double dc = 1 ; // a calculer !!!!!!
			// double dc []= shading[0] ;

			// System.out.println
			// ("CModel-new method, growth for tree "+targetTree.getId
			// ()+" tree age "+targetTree.getAge()+" Hs "+targetTree.Hs);
			// System.out.println
			// ("CModel, bt[i1] "+totalShading[0]+" bt[i2] "+totalShading[1]);
			// System.out.println ("CModel, dc "+(totalShading[1] -
			// totalShading[0]));

			double qc = 0;
			// if (totalLAI != 0) {
			// qc = targetTree.LAI * e1 / totalLAI;
			// }
			if ((totalShading[1] - totalShading[0]) != 0) {
				qc = targetTree.LAI * e1 / (totalShading[1] - totalShading[0]);
			}

			// calculate photosynthesis
			double sigmac = 0;
			if (targetTree.Wf > 0 && targetTree.getNumber() > 0) {
				double P00 = Math.min(0.25 * targetTree.getHeight() + 0.75, 1)
						* sp.P0;
				sigmac = P00 * qc / (targetTree.getNumber() * targetTree.Wf)
						* 10000;
			}

			targetTree.Pg = sigmac * (1 - sp.asig * targetTree.Hb)
					* targetTree.Wf; // Photosynthesis of the tree

			calculateG(targetTree); // Calculate net tree growth
			// Vency ajout� Lundi 20fev 2012
			ii++;
		}

	}

	/**
	 * Returns the competitors for the given tree. The keys in the map are the
	 * neighbour trees and the matching value is a weight for this neighbour: if
	 * not spatialized, weight = 1, if spatialized, weight = distance of the
	 * neighbour.
	 */
	protected Map getCompetitors(CStand stand, CTree targetTree) {
		Collection competitors = new ArrayList(stand.getTrees()); // a copy of
																	// the list
		competitors.remove(targetTree); // remove the target tree from copy
		
		// Transform the collection into a map
		Map m = new HashMap ();
		for (Object o : competitors) {
			JpTree jt = (JpTree) o;
			m.put(jt, 1);
		}
		
		return m;
	}

	/* VENCY fin */// //////////////////////////////////////////////////////////////////////////
	/* VENCY start: to compare one target tree with one competitor */// ////////////////////
	// //////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////Modifications end
	// here////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////

	/*
	 * Calculates net growth for the trees in a stand by estimating (1)
	 * effective leaf area of a tree (LAI), (2) maintenance respiration (Rm),
	 * (3) photosynthesis (Pg) and (4) net growth (G = (Pg - Rm) / Y Translation
	 * of Fortran code given by A. Makela, mai 2006
	 */

	private void calculateNetGrowth_v2(CStand newStand) {

		// CTree t1 = (CTree) newStand.getTrees ().iterator ().next ();
		// CSpecies sp = t1.getCSpecies ();

		// Sort trees on ascending heights
		Collection trees = new TreeSet(new TreeHeightComparator());
		trees.addAll(newStand.getTrees());
		int NClasses = trees.size();

		double[] ht = new double[NClasses];
		double[] hs = new double[NClasses];

		int k = 0;
		for (Iterator i = trees.iterator(); i.hasNext();) {
			CTree tree = (CTree) i.next();
			calculateLAI(tree); // Calculate effective Leaf Area Index
			calculateRm(tree); // Calculate maintenance respiration

			ht[k] = tree.getHeight();
			hs[k] = tree.Hs;

			k++;
		}

		double[] l = new double[2 * NClasses];
		System.arraycopy(ht, 0, l, 0, NClasses);
		System.arraycopy(hs, 0, l, NClasses, NClasses);

		// 1. sort ascending
		Arrays.sort(l);

		// 2. reverse : descending
		int nv = l.length;
		int hnv = nv / 2;
		double tmp;
		for (int i = 0; i < hnv; i++) {
			tmp = l[i];
			l[i] = l[nv - 1 - i];
			l[nv - 1 - i] = tmp;
		}

		// calculate relative crown volume, effective leaf area and crown
		// shading
		// bt: crown shading
		// lt: leaf area above i

		double[] lt = new double[nv];
		double[] bt = new double[nv];

		for (int i = 0; i < nv - 1; i++) {

			int ii = 0;
			for (Iterator z = trees.iterator(); z.hasNext();) {
				CTree tree = (CTree) z.next();

				double vrel = 0;
				if ((l[i] > hs[ii]) && (l[i + 1] < ht[ii])) {
					double c1 = (ht[ii] - l[i]) * (ht[ii] - l[i]);
					double c2 = (ht[ii] - l[i]) * (ht[ii] - l[i + 1]);
					double c3 = (ht[ii] - l[i + 1]) * (ht[ii] - l[i + 1]);
					if (tree.Hc != 0) {
						vrel = (l[i] - l[i + 1]) * (c1 + c2 + c3)
								/ ((tree.Hc) * (tree.Hc) * (tree.Hc));
					}
				}
				double lpt = tree.LAI * vrel;
				lt[i + 1] = lt[i + 1] + lpt;

				ii++;
			}

			bt[i + 1] = bt[i] + lt[i + 1];
		}

		// calculate photosynthesis:
		// e1: light extinction using Beer-lambert law
		// qc: photosynthesis

		double[] qc = new double[NClasses];
		int ii = 0;
		for (Iterator z = trees.iterator(); z.hasNext();) {
			CTree tree = (CTree) z.next();

			// fc-13.1.2011
			CSpecies sp = tree.getCSpecies();

			double dc = 0;

			int i1 = 0;
			int i2 = 0;
			for (int i = 0; i < nv; i++) {
				if (ht[ii] == l[i]) {
					i1 = i;
				}
				if (hs[ii] == l[i]) {
					i2 = i;
				}
				if ((ht[ii] > l[i]) && (hs[ii] <= l[i])) {
					dc = dc + lt[i];
				}
			}
			// System.out.println
			// ("CModel-original, growth for tree "+tree.getId
			// ()+" tree age "+tree.getAge()+" Hs "+tree.Hs);
			// System.out.println
			// ("CModel, i1 "+i1+" i2 "+i2+" bt[i1] "+bt[i1]+" i2 "+i2+" bt[i2] "+bt[i2]);
			// System.out.println ("CModel, dc "+dc);
			// System.out.println ("CModel, l array "+Arrays.toString(l));
			// System.out.println ("CModel, lt array "+Arrays.toString(lt));

			double e1 = Math.exp(-sp.k * bt[i1]) - Math.exp(-sp.k * bt[i2]);
			if (dc != 0) {
				qc[ii] = tree.LAI * e1 / dc;
			}

			// Update Pg in the trees of refStand
			double sigmac = 0;
			if (tree.Wf > 0 && tree.getNumber() > 0) {
				double P00 = Math.min(0.25 * tree.getHeight() + 0.75, 1)
						* sp.P0;
				sigmac = P00 * qc[ii] / (tree.getNumber() * tree.Wf) * 10000;
			}

			tree.Pg = sigmac * (1 - sp.asig * tree.Hb) * tree.Wf; // Photosynthesis
																	// of the
																	// tree

			calculateG(tree); // Calculate net tree growth

			ii++;
		}

	}

	/*
	 * Calculate maintenance respiration of the tree with wood and foliage/fine
	 * roots separately
	 */
	private void calculateRm(CTree tree) {
		CSpecies sp = tree.getCSpecies();
		tree.Rm = sp.r1 * (tree.Wf + tree.Wr) + sp.r2
				* (tree.Ws + tree.Wb + tree.Wt);
	}

	/*
	 * Calculate effective leaf area of the tree
	 */
	private void calculateLAI(CTree tree) {
		CSpecies sp = tree.getCSpecies();

		double space = tree.getNumber() / 10000;
		double sar = Math.PI * Math.pow(tree.Hb, 2)
				* (1 + Math.sqrt(1 + 1 / (tree.gammab * tree.gammab)));
		double L = 0;
		if (sar > 0.001) {
			L = sp.sa * tree.Wf / sar;
		}
		double PIO = sar * (1 - Math.exp(-sp.k * L));
		tree.LAI = (space * PIO) / sp.k;
	}

	/*
	 * Calculate net tree growth
	 */
	private void calculateG(CTree tree) {
		CSpecies sp = tree.getCSpecies();
		tree.G = (tree.Pg - tree.Rm) / (sp.Y);
		// fc and rg were temporarily unavailable for black spruce,
		// so fc + rg is replaced by Y
		// tree.G = (tree.Pg - tree.Rm) / (sp.fc + sp.rg);

		// fc - 29.5.2008
		if (tree.G <= 0) {
			tree.setNumber(0);
		}

	}

	/*
	 * Calculate crown coverage and self pruning for each size class
	 */

	private void calculateCrownCoverage(CStand newStand) {
		// CTree t1 = (CTree) newStand.getTrees ().iterator ().next ();
		// CSpecies sp = t1.getCSpecies ();

		// Sort trees on ascending heights
		Collection trees = new TreeSet(new TreeHeightComparator());
		trees.addAll(newStand.getTrees());
		int NClasses = trees.size();

		newStand.maxCrownCoverage = 0;
		newStand.crownCovCrownBase = 0;

		double Ntot = 0;
		double Atot = 0;
		double dr = 0;
		double[] AA1 = new double[NClasses];
		double[] AA2 = new double[NClasses];
		double[] selfPrun = new double[NClasses];

		// Minimum self prunning - rs 12.11.2008
		int i = 0;
		double fol1 = 0;
		double fol2 = 0;

		// Iterate over all tree classes to calculate
		// AA: crown coverage at crown base
		// BB: crown coverage at stem apex
		// CC: total stand crown coverage

		for (Iterator z = trees.iterator(); z.hasNext();) {
			CTree tree = (CTree) z.next();

			// fc-13.1.2011
			CSpecies sp = tree.getCSpecies();

			double AA = 0;
			double CC = 0;
			double BB = 0;
			Ntot = 0;
			Atot = 0;
			dr = 0;

			if ((tree.Wf <= 0) || (tree.getNumber() <= 0)) {
				AA1[i] = 0;
				AA2[i] = 0;
				selfPrun[i] = 0;
			}

			if ((tree.Wf > 0) && (tree.getNumber() > 0)) {
				for (Iterator z2 = trees.iterator(); z2.hasNext();) {
					CTree tree2 = (CTree) z2.next();
					Atot = Atot + Math.PI
							* Math.pow(tree2.gammab * tree2.Hc, 2)
							* tree2.getNumber() / 10000;
					CC = CC + tree2.getNumber() * Math.PI
							* Math.pow(sp.gammab * tree2.Hc, 2);
					Ntot = Ntot + tree2.getNumber();
					if (tree2.Hs >= (tree.Hs - 0.01)) {
						dr = sp.gammab * tree2.Hc;
						AA = AA + Math.PI * dr * dr * tree2.getNumber();
					} else {
						if (tree2.getHeight() > tree.Hs) {
							dr = (tree2.getHeight() - tree.Hs) * sp.gammab;
							AA = AA + Math.PI * dr * dr * tree2.getNumber();
						}
					}

					if (tree2.getHeight() > tree.getHeight()) {
						if (tree2.Hs <= tree.getHeight()) {
							dr = (tree2.getHeight() - tree.getHeight())
									* sp.gammab;
						} else {
							dr = tree2.Hc * sp.gammab;
						}
						BB = BB + Math.PI * dr * dr * tree2.getNumber();
					}
				}

				fol1 = fol1 + tree.getNumber() * sp.ksiMin
						* Math.pow(tree.Hc, 2 * sp.zMin);
				fol2 = fol2 + tree.getNumber() * tree.Wf;

				AA1[i] = (fol2 / fol1) * (AA + BB) / 10000;
				if (AA1[i] < 1) {
					selfPrun[i] = Math.max(sp.minSelfPrun,
							1 - Math.pow(sp.aq * AA1[i], tree.q));
				} else {
					selfPrun[i] = 0;
				}
				AA2[i] = CC / 10000;
			}

			tree.crownCov = AA1[i];
			tree.crownCovCrownBase = AA / 10000;
			tree.selfPrun = selfPrun[i];

			tree.calculateCrownDensity(tree);

			if (newStand.crownCovCrownBase < tree.crownCovCrownBase) {
				newStand.crownCovCrownBase = tree.crownCovCrownBase;
			}

			newStand.maxCrownCoverage = Math.max(Atot, AA2[i]);

			i++;
		}

	}

	/*
	 * Calculate stem mortality Translation of Fortran code given by A. Makela,
	 * mai 2006
	 */

	/*
	 * Adjust stand density to new values after calculating mortality
	 */
	private void setStandDensity(CStand stand) {

		stand.N = 0;
		Collection trees = stand.getTrees();
		for (Iterator z = trees.iterator(); z.hasNext();) {
			CTree tree = (CTree) z.next();
			stand.N += tree.getNumber();
		}

	}

	private String trace(double[] t) {
		NumberFormat nf = DefaultNumberFormat.getInstance(); // 3 decimals
		StringBuffer r = new StringBuffer("length=" + t.length + " ");
		for (int i = 0; i < t.length; i++) {
			r.append(nf.format(t[i]));
			r.append(" ");
		}
		r.append("\n");
		return r.toString();
	}

}
