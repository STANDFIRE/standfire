/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.dataextractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.extension.treelogger.geolog.util.TreeHistory;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.AgeDomProvider;
import capsis.util.methodprovider.AgeGProvider;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgProvider;

/**
 * Ring width versus time:
 *	- obj : ring width of objective trees (the set of trees at the chosen step) at time t (average, min and max)
 *	- Ddom : ring width of dominant tree (= Ddom / AgeDom / 2) (needs AgeDomProvider)
 *	- Dg : ring width of mean tree (= Dg / AgeG / 2) (needs AgeGProvider)
 * Ring width is estimated by (dbh2 - dbh1) / (age2 - age1) / 2 (i.e. bark included !!)
 * If meanPB is selected, ring width is the average from pith to time t (dbh / age)
 * Should work with numberable trees.
 *
 * @author F. Mothe - july 2008
 */
public class DETimeRW extends PaleoDataExtractor implements DFCurves {
	protected List<List<? extends Number>> curves;
	protected List<List<String>> labels;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeRW");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DETimeRW () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DETimeRW (GenericExtensionStarter s) {
		super (s);
		try {
			curves = new ArrayList<List<? extends Number>> ();
			labels = new ArrayList<List<String>> ();

			if (step != null) {
				MethodProvider mp = step.getProject ().getModel ().getMethodProvider ();
				boolean withDdom = (mp instanceof DdomProvider)
						&& (mp instanceof AgeDomProvider);
				boolean withDg = (mp instanceof DgProvider)
						&& (mp instanceof AgeGProvider);
				setPropertyEnabled ("DETimeRW.n01Ddom", withDdom);
				setPropertyEnabled ("DETimeRW.n02Dg", withDg);
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeRW.c ()", "Exception occured while object construction : ", e);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (! (referent instanceof GModel)) {return false;}

			Step step = (Step) ((GModel) referent).getProject ().getRoot ();
			if (! (step.getScene () instanceof TreeList)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeRW.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {

		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.TREE_IDS);
		// TODO : group selection in two tabs ?
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration

		// Properties in the form boxTitle_componentTitle :
		addBooleanProperty ("DETimeRW.n03obj_avg", true);
		addBooleanProperty ("DETimeRW.n03obj_minmax", false);
		addBooleanProperty ("DETimeRW.n01Ddom", true);
		addBooleanProperty ("DETimeRW.n02Dg", true);
		addBooleanProperty ("DETimeRW.n04selected", true);
		addBooleanProperty ("DETimeRW.n09meanPB", false);
	}

	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 * This extractor computes Number of trees in the stand versus Date.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		// Retrieve method provider
		MethodProvider mp = step.getProject ().getModel ().getMethodProvider ();

		try {

			boolean withObj_avg = isSet ("DETimeRW.n03obj_avg");
			boolean withObj_minmax = isSet ("DETimeRW.n03obj_minmax");
			boolean withObj = withObj_avg || withObj_minmax;

			// TODO : use stand.getDate () in place of mp.getAgeXX for withDdom & withDg
			boolean withDdom = mp instanceof DdomProvider && mp instanceof AgeDomProvider
					&& isSet ("DETimeRW.n01Ddom");
			boolean withDg = mp instanceof DgProvider && mp instanceof AgeGProvider
					&& isSet ("DETimeRW.n02Dg");

			boolean withSelected = isSet ("DETimeRW.n04selected");

			boolean withMeanPB = isSet ("DETimeRW.n09meanPB");

			// Retrieve Steps from root to this step (without intervention steps) :
			Vector <Step> steps = TreeHistory.getStepsFromRoot (step, false);
			int nbSteps = steps.size ();

			// Date initialisation:
			Vector <Integer> cDate = new Vector <Integer> (nbSteps);	// x coordinates
			for (Step step : steps) {
				cDate.add (step.getScene ().getDate ());
			}

			// RW of objective trees :
			Vector <Double> cObj_avg = withObj_avg ? new Vector <Double> (nbSteps) : null;
			Vector <Double> cObj_min = withObj_minmax ? new Vector <Double> (nbSteps) : null;
			Vector <Double> cObj_max = withObj_minmax ? new Vector <Double> (nbSteps) : null;

			// RW DDom and Dg :
			Vector <Double> cDdom = withDdom ? new Vector <Double> (nbSteps) : null;
			Vector <Double> cDg = withDg ? new Vector <Double> (nbSteps) : null;

			// RW of selected trees :
			// (treeIds (from DataExtractor) is a List <String> containing the ids
			// of selected trees)
			// TODO : does not work with groups
			int treeSelectedNumber = treeIds == null ? 0 : treeIds.size ();
			if (treeSelectedNumber == 0) {withSelected = false;}
			Vector <Vector <Double>> cSelected = null;
			if (withSelected) {
				cSelected = new Vector <Vector <Double>> (treeSelectedNumber);
				for (int i = 0; i < treeSelectedNumber; ++ i) {
					cSelected.add (new Vector <Double> (nbSteps));
				}
			}

			// RW DDom and Dg:
			if (withDdom || withDg) {
				double previousDdom = 0.;
				double previousAgeDom = 0.;
				double previousDg = 0.;
				double previousAgeG = 0.;
				Vector <Step> stepsWithInterv = TreeHistory.getStepsFromRoot (step, true);

				for (Step step : stepsWithInterv) {
					TreeList stand = (TreeList) step.getScene ();
					Collection trees = doFilter (stand);
					if (withDdom) {
						double age = ((AgeDomProvider) mp).getAgeDom (stand, trees);
						double d_cm = ((DdomProvider) mp).getDdom (stand, trees);
						if (! stand.isInterventionResult ()) {
							boolean valid = age >= 0. && d_cm >= 0.;
							double rw;
							if (withMeanPB) {
								rw = valid ? getRingWidth_mm (d_cm, age) : Double.NaN;
							} else {
								valid &= previousAgeDom >= 0. && previousDdom >= 0.;
								rw = valid
									? getRingWidth_mm (d_cm - previousDdom, age - previousAgeDom)
									: Double.NaN;
							}
							cDdom.add (rw);
						}
						previousDdom = d_cm;
						previousAgeDom = age;
					}
					if (withDg) {
						double age = ((AgeGProvider) mp).getAgeG (stand, trees);
						double d_cm = ((DgProvider) mp).getDg (stand, trees);
						boolean valid = age >= 0. && d_cm >= 0.;
						if (! stand.isInterventionResult ()) {
							double rw;
							if (withMeanPB) {
								rw = valid ? getRingWidth_mm (d_cm, age) : Double.NaN;
							} else {
								valid &= previousAgeG >= 0. && previousDg >= 0.;
								rw = valid
									? getRingWidth_mm (d_cm - previousDg, age - previousAgeG)
									: Double.NaN;
							}
							cDg.add (rw);
						}
						previousDg = d_cm;
						previousAgeG = age;
					}
				}
			}

			// RW of objective trees:
			if (withObj) {

				// Cumulated number of rings of the objective trees (for Numberable trees) :
				// (may change with time if objective trees were recruited)
				Vector <Double> nbRings = new Vector <Double> (nbSteps);

				// Initialisation:
				for (Step step : steps) {
					if (withObj_avg) {
						cObj_avg.add (0.);
					}
					if (withObj_minmax) {
						cObj_min.add (Double.NaN);
						cObj_max.add (Double.NaN);
					}
					nbRings.add (0.);
				}

				Collection lastTrees = doFilter (step.getScene ());
				for (Object o : lastTrees) {
					Tree tree = (Tree) o;

					// Number (at the final step) for numberable trees, 1 either:
					double nbThisTree = 1d;
					if (tree instanceof Numberable) {
						nbThisTree = ((Numberable) tree).getNumber();
					}
					if (nbThisTree > 0.) {

//						Vector <Tree> treeHistory =
//								TreeUtil.getVectorTreeHistory (tree, steps, true);
						Tree[] treeHistory = new TreeHistory(tree, steps, true).getHistory();

						double previousDbh = 0.;
						int previousAge = 0;
						int nStep = 0;

						// Sum of rw from pith (without considering nbThisTree):
						for (Tree t : treeHistory) {
							// t may be null at the beginning if recruited later :
							if (t != null && t.getAge () > previousAge) {
								double nb = nbRings.get (nStep);
								double rw = getRingWidth_mm (
										t, previousDbh, previousAge, withMeanPB);;
								if (withObj_avg) {
									cObj_avg.set (nStep, cObj_avg.get (nStep)
										+ nbThisTree * rw);
								}
								if (withObj_minmax) {
									if (nb == 0.) {
										// first valid tree at this step
										cObj_min.set (nStep, rw);
										cObj_max.set (nStep, rw);
									} else if (rw < cObj_min.get (nStep)) {
										cObj_min.set (nStep, rw);
									} else if (rw > cObj_max.get (nStep)) {
										cObj_max.set (nStep, rw);
									}
								}

								nbRings.set (nStep, nb + nbThisTree);
								previousAge = t.getAge ();
								previousDbh = t.getDbh ();
							}
							++ nStep;
						}
					}
				}

				// Average obj :
				if (withObj_avg) {
					for (int nStep = 0; nStep < nbSteps; ++ nStep) {
						double nb = nbRings.get (nStep);
						double sum = cObj_avg.get (nStep);
						cObj_avg.set (nStep, sum / nb);
					}
				}
			}

			// RW of selected trees :
			if (withSelected) {
				// Initialisation:
				for (Step step : steps) {
					for (int i = 0; i < treeSelectedNumber; ++ i) {
						cSelected.get (i).add (Double.NaN);
					}
				}

				int numId = 0;
				for (String id : treeIds) {
					double previousDbh = 0.;
					int previousAge = 0;
					int nStep = 0;
					for (Step step : steps) {
						TreeList stand = (TreeList) step.getScene ();
						Tree t = stand.getTree (Integer.parseInt (id));
						if (t != null && t.getAge () > previousAge) {
							double rw = getRingWidth_mm (
									t, previousDbh, previousAge, withMeanPB);;
							cSelected.get (numId).set (nStep, rw);
							previousAge = t.getAge ();
							previousDbh = t.getDbh ();
						}
						++ nStep;
					}
					++ numId;
				}
			}

			// Add the curves :
			labels.clear ();
			curves.clear ();

			addSerie (cDate, null);	// x data (without labels)
			if (withObj_avg) {
				addSerie (cObj_avg, Translator.swap ("DETimeRW.cObj_avg"));
			}
			if (withObj_minmax) {
				addSerie (cObj_min, Translator.swap ("DETimeRW.cObj_min"));
				addSerie (cObj_max, Translator.swap ("DETimeRW.cObj_max"));
			}
			if (withDdom) {addSerie (cDdom, Translator.swap ("DETimeRW.cDdom"));}
			if (withDg) {addSerie (cDg, Translator.swap ("DETimeRW.cDg"));}

			if (withSelected) {
				String suffixe = Translator.swap ("DETimeRW.cSelected");
				for (int i = 0; i < treeSelectedNumber; ++ i) {
					addSerie (cSelected.get (i), treeIds.get (i) + suffixe);
				}
			}

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeRW.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	*	Ring width for a given tree considering previous dbh at previous age.
	*	Returns the radial increment or the mean from pith.
	*/
	private static double getRingWidth_mm (Tree t, double previousDbh, int previousAge,
			boolean withMeanPB)
	{
		double rw;
		if (withMeanPB) {
			rw = getRingWidth_mm (t.getDbh (), t.getAge ());
		} else {
			int nbYears = t.getAge () - previousAge;
			double incDbh = Math.max (0., t.getDbh () - previousDbh);
			rw = getRingWidth_mm (incDbh, nbYears);
		}
		return rw;
	}

	/**
	*	Ring width for a given increment in diameter during a given period.
	*/
	private static double getRingWidth_mm (double incDiam_cm, double incAge) {
		return incAge > 0. ? incDiam_cm / (.2 * incAge) : 0.;	// mm
	}

	/**
	*	Add a data serie to curves and labels.
	*	label may be null for x coordinates.
	*/
	private void addSerie (Vector <? extends Number> coordinates, String label) {
		curves.add (coordinates);
		Vector <String> yLabels = new Vector <String> ();
		if (label != null) {
			yLabels.add (label);
		}
		labels.add (yLabels);
	}

	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix () + Translator.swap ("DETimeRW");
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector <String> v = new Vector <String> ();
		v.add (Translator.swap ("DETimeRW.xLabel"));
		v.add (Translator.swap ("DETimeRW.yLabel"));
		return v;
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {return curves.size () - 1;}

	/**
	 * From DFCurves interface.
	 */
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<List<String>> getLabels () {
		return labels;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "F. Mothe";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DETimeRW.description");}




}
