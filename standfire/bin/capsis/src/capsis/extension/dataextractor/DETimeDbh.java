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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.amapsim.AMAPsimRequestableTree;
import capsis.util.methodprovider.DbhStandardDeviationProvider;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgAmapsimProvider;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.MaxDbhProvider;
import capsis.util.methodprovider.MeanDbhProvider;
import capsis.util.methodprovider.MedianDbhProvider;
import capsis.util.methodprovider.MinDbhProvider;

/**	Individual Diameter At Breast Height versus Year (for one or several individuals).
*
*	@author F. de Coligny - november 2000 - review january 2004 - enhanced august 2005
*	modified by L. Saint-Andr� and Y. Caraglio to introduce DgAmapsim
*	modified by S. Turbis to introduce Dmin et Dmax
*	modified by fc to comply with models without trees (Lemoine), 12.10.2006
*/
public class DETimeDbh extends PaleoDataExtractor implements DFCurves {
	protected Vector curves;
	protected Vector labels;
	//protected MethodProvider methodProvider;

	private boolean availableDg;		// maybe module does not calculate this property
	private boolean availableDdom;
	private boolean availableDamapsim;
	private boolean availableDgamapsim; // LSA and YC 18.3.2004
	private boolean availableDm;				// fc - 25.8.2005
	private boolean availableDmedian;				// fc + tf - june 2009
	private boolean availableDbhStandardDeviation;	// fc - 25.8.2005
	private boolean availableDmin;			// st - 29.8.2005
	private boolean availableDmax;			// st - 29.8.2005

	private boolean modelWithTrees;	// fc - 12.10.2006 (Lemoine...)

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeDbh");
	}


	/**	Phantom constructor.
	*	Only to ask for extension properties (authorName, version...).
	*/
	public DETimeDbh () {}

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public DETimeDbh (GenericExtensionStarter s) {
		super (s);
		try {
			curves = new Vector ();
			labels = new Vector ();

			// MOVED, see below
//			checkMethodProvider (step.getProject ().getModel ().getMethodProvider ());
//
//			// According to module capabiblities, disable some configProperties
//			setPropertyEnabled ("showDg", availableDg);
//			setPropertyEnabled ("showDdom", availableDdom);
//			setPropertyEnabled ("showDamapsim", availableDamapsim);
//			setPropertyEnabled ("showDgamapsim", availableDgamapsim);
//			setPropertyEnabled ("showDm", availableDm);
//			setPropertyEnabled ("showDmedian", availableDmedian);
//			setPropertyEnabled ("showDbhStandardDeviation", availableDbhStandardDeviation);
//			setPropertyEnabled ("showDmin", availableDmin);
//			setPropertyEnabled ("showDmax", availableDmax);

		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeDbh.c ()",
         "Exception occured during object construction : ", e);
		}
	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;

			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();

			// fc - 12.10.2006
			if (s instanceof TreeList) {return true;}
			checkMethodProvider (m.getMethodProvider ());
			if (modelWithTrees) {return true;}
			if (availableDg
					|| availableDdom
					|| availableDamapsim
					|| availableDgamapsim
					|| availableDm
					|| availableDmedian
					|| availableDbhStandardDeviation
					|| availableDmin
					|| availableDmax) {return true;}
			// fc - 12.10.2006
					
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeDbh.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}

		return false;	// fc - 8.1.2007 - bug correction [J. Labonne] always returned true
	}

	/**	This method is called by superclass DataExtractor constructor.
	*	If previous config was saved in a file, this method may not be called.
	*	See etc/extensions.settings file
	*/
	public void setConfigProperties () {

		checkMethodProvider (step.getProject ().getModel ().getMethodProvider ());

		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.TREE_IDS);

		addBooleanProperty ("showDg");
		addBooleanProperty ("showDdom");
		addBooleanProperty ("showDamapsim");
		addBooleanProperty ("showDgamapsim");
		addBooleanProperty ("showDm");
		addBooleanProperty ("showDmedian");
		addBooleanProperty ("showDbhStandardDeviation");
		addBooleanProperty ("showDmin");
		addBooleanProperty ("showDmax");
		
		// According to module capabiblities, disable some configProperties
		setPropertyEnabled ("showDg", availableDg);
		setPropertyEnabled ("showDdom", availableDdom);
		setPropertyEnabled ("showDamapsim", availableDamapsim);
		setPropertyEnabled ("showDgamapsim", availableDgamapsim);
		setPropertyEnabled ("showDm", availableDm);
		setPropertyEnabled ("showDmedian", availableDmedian);
		setPropertyEnabled ("showDbhStandardDeviation", availableDbhStandardDeviation);
		setPropertyEnabled ("showDmin", availableDmin);
		setPropertyEnabled ("showDmax", availableDmax);
	}

	//	Evaluate MethodProvider's capabilities to propose more or less options
	//	fc - 6.2.2004 - step variable is available
	//
	private void checkMethodProvider (MethodProvider methodProvider) {
		// Retrieve method provider
		// fc - 12.10.2006 - methodProvider = step.getScenario ().getModel ().getMethodProvider ();

		if (methodProvider instanceof DgProvider) {availableDg = true;}
		if (methodProvider instanceof DdomProvider) {availableDdom = true;}
		if (methodProvider instanceof MeanDbhProvider) {availableDm = true;}
		if (methodProvider instanceof MedianDbhProvider) {availableDmedian = true;}
		if (methodProvider instanceof DbhStandardDeviationProvider) {availableDbhStandardDeviation = true;}
		if (methodProvider instanceof MinDbhProvider) {availableDmin = true;}
		if (methodProvider instanceof MaxDbhProvider) {availableDmax = true;}

		availableDamapsim = false;
		try {	// fc - 12.10.2006
			TreeCollection tc = (TreeCollection) step.getScene ();

			modelWithTrees = true;	// fc - 12.10.2006

			for (Iterator i = tc.getTrees ().iterator (); i.hasNext ();) {
				Tree t = (Tree) i.next ();
				if (t instanceof AMAPsimRequestableTree
						&& ((AMAPsimRequestableTree) t).getAMAPsimTreeData () != null) {
					availableDamapsim = true;
					break;
				}
			}
		} catch (Exception e) {
			// stand may not be instance of TreeCollection (ex: Lemoine)
		}

		availableDgamapsim = false;
		if (availableDamapsim){
			if (methodProvider instanceof DgAmapsimProvider) {availableDgamapsim = true;}
		}

//~ System.out.println ("availableDg "+availableDg);
//~ System.out.println ("availableDdom "+availableDdom);
//~ System.out.println ("availableDamapsim "+availableDamapsim);
//~ System.out.println ("availableDgamapsim "+availableDgamapsim);
	}

	/**	From DataExtractor SuperClass.
	*
	*	Computes the data series. This is the real output building.
	*	It needs a particular Step.
	*
	*	Return false if trouble while extracting.
	*/
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		// Retrieve method provider
		MethodProvider methodProvider = step.getProject ().getModel ().getMethodProvider ();

		try {
			// If no curve at all, choose one tree
			// fc - 6.2.2004
			if (modelWithTrees	// fc - 12.10.2006
					&& !isSet ("showDg")
					&& !isSet ("showDdom")
					&& !isSet ("showDm")
					&& !isSet ("showDmedian")
					&& !isSet ("showDbhStandardDeviation")
					&& !isSet ("showDmin")
					&& !isSet ("showDmax")
					&& (treeIds == null || treeIds.isEmpty ())) {

				TreeCollection tc = (TreeCollection) step.getScene ();
				if (treeIds == null) {treeIds = new Vector ();}
				int minId = Integer.MAX_VALUE;
				for (Iterator i = tc.getTrees ().iterator (); i.hasNext ();) {
					Tree t = (Tree) i.next ();
					minId = Math.min (minId, t.getId ());	// fc - 27.9.2006
				}
				treeIds.add (""+minId);

				// this block replaced by above block - fc - 12.10.2006
				/*if (treeIds == null) {treeIds = new Vector ();}
				// Consider restriction to one particular group if needed
				//TreeCollection tc = (TreeCollection) step.getStand ();
				GStand stand = step.getStand ();
				Collection trees = doFilter (stand);
				if (!trees.isEmpty ()) {
					treeIds.add (""+((GTree) trees.iterator ().next ()).getId ());
				}*/
			}


			int treeNumber = treeIds.size ();

			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);

			Vector c1 = new Vector ();					// x coordinates (years)
			Vector c2 = new Vector ();					// optional: y coordinates (Dg)
			Vector c3 = new Vector ();					// optional: y coordinates (Ddom)
			Vector c4 = new Vector ();					// optional: y coordinates (Dgamapsim)
			Vector c5 = new Vector ();					// optional: y coordinates (Dm)
			Vector c5a = new Vector ();					// optional: y coordinates (Dmedian)
			Vector c6 = new Vector ();					// optional: y coordinates (DbhStandardDeviation)
			Vector c7 = new Vector ();					// optional: y coordinates (Dmin)
			Vector c8 = new Vector ();					// optional: y coordinates (Dmax)

			int treeCurvesNumber = treeNumber;
			if (isSet ("showDamapsim")) {
				treeCurvesNumber*=2;		// y coordinates (tree dbhs + optionaly tree amapsim dbhs)
			}
			Vector cy[] = new Vector[treeCurvesNumber];		// y coordinates (dbhs)

			for (int i = 0; i < treeCurvesNumber; i++) {
				Vector v = new Vector ();
				cy[i] = v;
			}

			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();

				GScene stand = s.getScene ();
				Collection trees = null;
				if (modelWithTrees) {	// fc - 12.10.2006
					// better ? no group in DETimeH...
					//trees = ((TreeCollection) stand).getTrees ();	// fc - 9.4.2004
					trees = doFilter (stand);
				}
				//Collection trees = doFilter (stand);	// ok, groups are considered

				int year = stand.getDate ();

				c1.add (new Integer (year));
				if (isSet ("showDg")) {
					c2.add (new Double (((DgProvider) methodProvider).getDg (stand, trees)));
				}
				if (isSet ("showDdom")) {
					c3.add (new Double (((DdomProvider) methodProvider).getDdom (stand, trees)));
				}
				if (isSet ("showDgamapsim")) {
					c4.add (new Double (((DgAmapsimProvider) methodProvider).getDgAmapsim (stand, trees)));
				}
				if (isSet ("showDm")) {
					c5.add (new Double (((MeanDbhProvider) methodProvider).getMeanDbh (stand, trees)));
				}
				if (isSet ("showDmedian")) {
					c5a.add (new Double (((MedianDbhProvider) methodProvider).getMedianDbh (stand, trees)));
				}
				if (isSet ("showDbhStandardDeviation")) {
					c6.add (new Double (((DbhStandardDeviationProvider) methodProvider).getDbhStandardDeviation (stand, trees)));
				}
				if (isSet ("showDmin")) {
					c7.add (new Double (((MinDbhProvider) methodProvider).getMinDbh (stand, trees)));
				}
				if (isSet ("showDmax")) {
					c8.add (new Double (((MaxDbhProvider) methodProvider).getMaxDbh (stand, trees)));
				}

				// Get dbh for each tree
				if (modelWithTrees) {
					int n = 0;		// ith tree (O to n-1)
					for (Iterator ids = treeIds.iterator (); ids.hasNext ();) {
						int id = new Integer ((String) ids.next ()).intValue ();
						Tree t = ((TreeCollection) stand).getTree (id);

	// bug correction : some maid trees may have number == 0 -> ignore them - tl 12/09/2005
						double number = 1;	// fc - 22.8.2006 - Numberable returns double
						if (t instanceof Numberable) number=((Numberable) t).getNumber ();
						if (t == null || t.isMarked () || number ==0) {	// fc & phd - 5.1.2003
							cy[n++].add (new Double (Double.NaN));	// dbh
							if (isSet ("showDamapsim")) {
								cy[n++].add (new Double (Double.NaN));	// amapsim dbh
							}
						} else {
							cy[n++].add (new Double (t.getDbh ()));

							if (isSet ("showDamapsim")) {
								try {
									AMAPsimRequestableTree at = (AMAPsimRequestableTree) t;
									double amapsimD = at.getAMAPsimTreeData ().treeStep.dbh;
									cy[n++].add (new Double (amapsimD));	// amapsim dbh
								} catch (Exception e) {
									cy[n++].add (new Double (Double.NaN));	// amapsim dbh
								}
							}

						}
					}
				}
			}

			// Curves
			//
			curves.clear ();
			curves.add (c1);
			if (isSet ("showDg")) {curves.add (c2);}
			if (isSet ("showDdom")) {curves.add (c3);}
			if (isSet ("showDgamapsim")) {curves.add (c4);}
			if (isSet ("showDm")) {curves.add (c5);}
			if (isSet ("showDmedian")) {curves.add (c5a);}
			if (isSet ("showDbhStandardDeviation")) {curves.add (c6);}
			if (isSet ("showDmin")) {curves.add (c7);}
			if (isSet ("showDmax")) {curves.add (c8);}

			// Labels
			//
			labels.clear ();
			labels.add (new Vector ());		// no x labels
			if (isSet ("showDg")) {
				Vector y1Labels = new Vector ();
				y1Labels.add ("Dg");
				labels.add (y1Labels);			// y1 : label "Dg"
			}
			if (isSet ("showDdom")) {
				Vector y2Labels = new Vector ();
				y2Labels.add ("Ddom");
				labels.add (y2Labels);			// y2 : label "Ddom"
			}
			if (isSet ("showDgamapsim")) {
				Vector y3Labels = new Vector ();
				y3Labels.add ("DgAmapsim");
				labels.add (y3Labels);			// y3 : label "DgAmapsim"
			}
			if (isSet ("showDm")) {
				Vector y4Labels = new Vector ();
				y4Labels.add ("Dm");
				labels.add (y4Labels);			//y4 : label "Dm"
			}
			if (isSet ("showDmedian")) {
				Vector y4aLabels = new Vector ();
				y4aLabels.add ("D0.50");
				labels.add (y4aLabels);			//y4a : label "Dmedian"
			}
			if (isSet ("showDbhStandardDeviation")) {
				Vector y5Labels = new Vector ();
				//y5Labels.add ("DbhStandardDeviation");
				// st - 29.08.2005 a bit too long for graph
				y5Labels.add ("Sigma");	// fc - 30.8.2005 - standard deviation (not error)
				labels.add (y5Labels);			//y5 : label "DbhStandardDeviation"
			}

			if (isSet ("showDmin")) {
				Vector y6Labels = new Vector ();
				y6Labels.add ("Dmin");
				labels.add (y6Labels);			//y6 : label "Dmin"
			}

			if (isSet ("showDmax")) {
				Vector y7Labels = new Vector ();
				y7Labels.add ("Dmax");
				labels.add (y7Labels);			//y7 : label "Dmax"
			}


			if (isSet ("showDamapsim")) {	// n curves = 2 * n trees
				int i = 0;		// fc - 6.2.2004
				int treeId = 0;
				while (i < treeCurvesNumber) {
					curves.add (cy[i]);
					Vector v = new Vector ();
					v.add ((String) treeIds.get (treeId));
					labels.add (v);		// y curve name = matching treeId
					i++;

					curves.add (cy[i]);
					v = new Vector ();
					v.add ((String) treeIds.get (treeId)+" AMAPsim");
					labels.add (v);		// y curve name = matching treeId
					i++;
					treeId++;

				}

			} else {		// n urves = n trees
				for (int i = 0; i < treeCurvesNumber; i++) {
					curves.add (cy[i]);
					Vector v = new Vector ();
					v.add ((String) treeIds.get (i));
					labels.add (v);		// y curve name = matching treeId
				}
			}

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeDbh.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**	From DataFormat interface.
	*/
	public String getName() {return getNamePrefix ()+Translator.swap ("DETimeDbh");}

	/**	From DFCurves interface.
	*/
	public List<List<? extends Number>> getCurves () {return curves;}

	/**	From DFCurves interface.
	*/
	public List<List<String>> getLabels () {return labels;}

	/**	From DFCurves interface.
	*/
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeDbh.xLabel"));
		v.add (Translator.swap ("DETimeDbh.yLabel"));
		return v;
	}

	/**	From DFCurves interface.
	*/
	public int getNY () {return curves.size () - 1;}

	/**	From Extension interface.
	*/
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.5";

	/**	From Extension interface.
	*/
	public String getAuthor () {return "F. de Coligny";}

	/**	From Extension interface.
	*/
	public String getDescription () {return Translator.swap ("DETimeDbh.description");}



}
