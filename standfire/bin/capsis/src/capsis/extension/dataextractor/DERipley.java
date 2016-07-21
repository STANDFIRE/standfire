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

import javax.swing.JOptionPane;

import jeeb.lib.util.Check;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.defaulttype.SpatializedTree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.DEMultiConfPanel;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.spatial.Ripley;

/**
 * Ripley K(r) and Besag L(r) functions.
 * This is the Dataextractor that makes the curves,
 * using the Ripley.java class to compute L(r) function
 * for a specified group of trees
 *
 * @author F. Goreaud - 29/3/2001 - 23/05/06
 */
public class DERipley extends DETimeG {
	private Vector labels;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DERipley");
		//System.out.println ("DERipley *** loaded");	// Please don't ;-)
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DERipley () {}

	/**
	* Official constructor. It uses the standard Extension starter.
	*/
	public DERipley (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	//~ * Functional constructor.
	//~ */
	//~ protected DERipley (Step stp) {
		super (s);
		settings.icNumberOfSimulations=0;
		labels = new Vector ();
		settings.icNumberOfSimulations=0;
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			
			Step root = (Step) m.getProject ().getRoot ();
			GScene stand = root.getScene ();
			if (!(stand instanceof TreeCollection)) {return false;}	// Must be a TreeCollection
			TreeCollection tc = (TreeCollection) stand;
			
			// fc - 20.11.2003 - If stand is empty, return true until we know more 
			// Some simulations (ex: Mountain) may begin with empty stand to 
			// test regeneration and this tool may still be compatible later
			//
			if (tc.getTrees ().isEmpty ()) {return true;}
			
			// fc - 20.11.2003 - If stand is not empty, all trees must be GMadTrees
			// Do not limit test to first tree (some modules mix GMaddTrees and 
			// GMaidTrees -> must not be compatible)
			//
			Collection reps = Tools.getRepresentatives (tc.getTrees ());	// one instance of each class
			//~ if (reps.size () == 1 
					//~ && reps.iterator ().next () instanceof GMaddTree) {return true;}
			// Possibly several classes of GMaddTree
			// A. Piboule - 29.3.2004
			//
			for (Iterator i = reps.iterator (); i.hasNext ();) {
				if (!(i.next () instanceof SpatializedTree)) {return false;}
			}
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "DERipley.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	* This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties

// 1) which group is concerned ? 
// FG : normally, only in individual properties ?

		addConfigProperty (PaleoDataExtractor.STATUS);	// fc - 22.4.2004
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);	// checked - fc
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration

//2) Commun properties 
		// new settings (see DataExtractor superclass) - fc
		addConfigProperty (PaleoDataExtractor.INTERVAL_NUMBER);
		addConfigProperty (PaleoDataExtractor.INTERVAL_SIZE);
		addConfigProperty (PaleoDataExtractor.IC_NUMBER_OF_SIMULATIONS);
		addConfigProperty (PaleoDataExtractor.IC_RISK);
		addConfigProperty (PaleoDataExtractor.IC_PRECISION);

	}

	/**
	* From DataExtractor SuperClass.
	*
	* Computes the data series. This is the real output building.
	* It needs a particular Step.
	* This extractor computes L(r) function.
	*
	* Return false if trouble while extracting.
	*/
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		//System.out.println ("DERipley : extraction being made");

		try {

			// parameter values defined in DE settings

			int intervalNumber = settings.intervalNumber;
			double intervalSize = settings.intervalSize;
			int icNumberOfSimulations = settings.icNumberOfSimulations;
			double icRisk = settings.icRisk;
			double icPrecision = settings.icPrecision;

			// Trace, comment it in normal conditions
			//~ System.out.println ("intervalNumber="+intervalNumber+" intervalSize="+intervalSize
					//~ +" icNumberOfSimulations="+icNumberOfSimulations+" icRisk="+icRisk+" icPrecision="+icPrecision);

			// Restriction to a group if needed
			Collection aux = doFilter (step.getScene ());
			Iterator trees = aux.iterator ();
			Log.println ("trees n="+aux.size());
			
			// Defining the vectors for the curves

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector c3 = new Vector ();		// y coordinates IC
			Vector c4 = new Vector ();		// y coordinates IC
			Vector l1 = new Vector ();		// labels for x axis (ex: 0-10, 10-20...)

			// Defining local variables for the computation

			// Limited in size! (java initializes each value to 0)
			// WARNING : to call Ripley function, trees are to be in x[1..N]. No point in x[0] will be considered
			double x[] = new double[aux.size()+1];
			double y[] = new double[aux.size()+1];
			int pointnb = 0;

			double xmi=step.getScene().getOrigin().x;
			double xma=xmi+step.getScene().getXSize();
			double ymi=step.getScene().getOrigin().y;
			double yma=ymi+step.getScene().getYSize();

			//double max=(xma-xmi);
			//if ((yma-ymi)<max)
			//	max = (yma-ymi);
			//int maxCat = 25;	// FG
			//int minCat = 1;		// FG
			//double classWidth = (max/100.0);

			double lic1[] = new double[intervalNumber+2];
			double lic2[] = new double[intervalNumber+2];
			double tabl[] = new double[intervalNumber+2];

			// Create output data
			// WARNING : index for x and y MUST be from 1 to N inclusive (no point in 0) FG060522
			while (trees.hasNext ()) {
				pointnb=pointnb+1;
				SpatializedTree t = (SpatializedTree) trees.next ();
				x[pointnb]=t.getX();		// x : m	// FG
				y[pointnb]=t.getY();		// y : m	// FG
			}


// Computation itself !
// WARNING : here we make the asumption that the ploit is rectangular
// too be improved !
			
			int erreur=0;
			erreur=Ripley.computeLRect(tabl, x,y,pointnb,xmi,xma,ymi,yma,intervalNumber,intervalSize);
			int erreurIC=0;
			erreurIC=Ripley.computeLICRect(2,lic1, lic2,pointnb,xmi,xma,ymi,yma,intervalNumber,intervalSize, icNumberOfSimulations,icRisk, icPrecision);

			if (erreur==0) {
// create the curve :
				for (int i = 1; i <= intervalNumber; i++) {
					double classBase = (i * intervalSize);
					//double a = (classBase + intervalSize / 2);
					c1.add (new Double (classBase));
					double numbers = (double) (tabl[i]);
					c2.add (new Double (numbers));
					if (erreurIC==0)					
					{	double numbers3 = (double) (lic1[i]);
						c3.add (new Double (numbers3));
						double numbers4 = (double) (lic2[i]);
						c4.add (new Double (numbers4));	
					}
					else			// we dont want to see the confidence interval curves if not computed
					{	c3.add (new Double (numbers));
						c4.add (new Double (numbers));
					}										
					l1.add (""+(classBase));
				}
			
				curves.clear ();
				curves.add (c1);
				curves.add (c2);
				curves.add (c3);	// new curve - fc - 18.10.2001
				curves.add (c4);	// new curve - fc - 18.10.2001

				labels.clear ();
				// fc - 16.10.2001			labels.add (l1);	// if you tell nothing, labels are calculated from x series
	
				// fc - 18.10.2001 -2  labels to tag confidence interval curves
				labels.add (new Vector ());		// no x labels

				Vector v0 = new Vector ();		// no y labels for c1 (no vector or empty vector -> no detection)
				v0.add (" ");
				labels.add (v0);

				if (erreurIC==0) {
					Vector v1 = new Vector ();		// y labels for c2
					v1.add ("ci-");
					labels.add (v1);
					Vector v2 = new Vector ();		// y labels for c3
					v2.add ("ci+");
					labels.add (v2);
				}
				else	// we dont want to see the confidence interval curves if not computed
				{	Vector v1 = new Vector ();		// y labels for c2
					v1.add (" ");
					labels.add (v1);
					Vector v2 = new Vector ();		// y labels for c3
					v2.add (" ");
					labels.add (v2);
				}

			}
			else
			{
				Log.println (Log.ERROR, "DERipley.doExtraction ()", "Error in Ripley computation");
				return false;
			}
		} catch (Exception exc) {
			Log.println (Log.ERROR, "DERipley.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	* From DataFormat interface.
	*/
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DERipley");
	}

	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return 3;	// redefines DEtimeG (3 curves instead of 1) - fc
	}

	/**
	* From DFCurves interface.
	*/
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DERipley.xLabel"));
		v.add (Translator.swap ("DERipley.yLabel"));
		return v;
	}

	/**
	* From DFCurves interface.
	*/
	public List<List<String>> getLabels () {return labels;}

	/**
	* From Extension interface.
	*/
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.1";

	/**
	* From Extension interface.
	*/
	public String getAuthor () {return "F. Goreaud";}

	/**
	* From Extension interface.
	*/
	public String getDescription () {return Translator.swap ("DERipley.description");}

	/**
	 * Redefinition of the method in DataExtractor.
	 * This method is called after multi configuration dialog to allow
	 * the extractor (this one) to perform functional tests on the input
	 * settings.
	 * Technical tests have already been performed (empty, type check...).
	 * See DataExtractor.
	 */
	protected boolean functionalTestsAreOk (DEMultiConfPanel panel) {

		if (hasConfigProperty (PaleoDataExtractor.INTERVAL_NUMBER)
				&& hasConfigProperty (PaleoDataExtractor.INTERVAL_SIZE)) {

			// Remind technical tests have already been performed (empty, type check...).
			int intervalNumber = Check.intValue (panel.intervalNumber.getText ());
			double intervalSize = Check.doubleValue (panel.intervalSize.getText ());

			// FG : You check here (fc - 18.10.2001) :

			double xmi=step.getScene().getOrigin().x;
			double xma=xmi+step.getScene().getXSize();
			double ymi=step.getScene().getOrigin().y;
			double yma=ymi+step.getScene().getYSize();

			if ((intervalNumber * intervalSize) > (Math.max ((xma - xmi), (yma - ymi)) / 2)) {
				JOptionPane.showMessageDialog (null, Translator.swap ("DERipley.outOfRange"),
						Translator.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE );	// Shared.error -> title bar
				return false;
			}

			// to be improved
			int icNumberOfSimulations = Check.intValue (panel.icNumberOfSimulations.getText ());
			double icRisk = Check.doubleValue (panel.icRisk.getText ());

			//if ((icNumberOfSimulations > 0)||(icRisk != 1)) {
			//	JOptionPane.showMessageDialog (null, Translator.swap ("DERipley.notYet"),
			//			Translator.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE );	// Shared.error -> title bar
			//	return false;
			//}

		}

		return true;	// ok
	}

}




