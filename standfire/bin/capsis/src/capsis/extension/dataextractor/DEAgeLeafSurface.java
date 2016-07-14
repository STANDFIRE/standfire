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
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.amapsim.AMAPsimRequestableTree;
import capsis.lib.amapsim.AMAPsimTreeData;
import capsis.lib.amapsim.AMAPsimTreeStep;

/**
 * Leaf Surface Age Scatterplot
 * (From DEDbhLeafSurface). Specific to AMAPSimRequestableTree.
 * @author Y. Caraglio - March 2004
 */
public class DEAgeLeafSurface extends DETimeG {

	static {
		Translator.addBundle("capsis.extension.dataextractor.DEAgeLeafSurface");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DEAgeLeafSurface () {}

	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DEAgeLeafSurface (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }

	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DEAgeLeafSurface (Step stp) {
		super (s);
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This static matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeCollection)) {return false;}
			TreeCollection tc = (TreeCollection) s;
			if (tc.getTrees ().isEmpty ()) {return true;} // bare soil problem
			Tree t = tc.getTrees ().iterator ().next ();
			if (!(t instanceof AMAPsimRequestableTree)) {return false;}
			AMAPsimRequestableTree tree = (AMAPsimRequestableTree) t;
			AMAPsimTreeData data = tree.getAMAPsimTreeData ();
			if (data==null) {return false;}


		} catch (Exception e) {
			Log.println (Log.ERROR, "DEAgeLeafSurface.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.TREE_GROUP);
		addConfigProperty (PaleoDataExtractor.I_TREE_GROUP);		// group individual configuration
	}


	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		try {
			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates

			// Restriction to a group if needed
			//~ Filtrable fil = doFilter ((Filtrable) step.getStand ());
			Collection aux = doFilter (step.getScene ());		// fc - 23.4.2004

			// Retrieve trees for this step
			Iterator trees = aux.iterator ();

			// Create output data
			while (trees.hasNext ()) {

				AMAPsimRequestableTree t = (AMAPsimRequestableTree) trees.next ();
				AMAPsimTreeData data = t.getAMAPsimTreeData ();
				AMAPsimTreeStep treeStep = data.treeStep;

				double age = treeStep.age;		// age
				double leafSurface = treeStep.leafSurface; // leaf surface

				c1.add (new Double (age));
				c2.add (new Double (leafSurface));
			}

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DEAgeLeafSurface.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}


	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DEAgeLeafSurface");
	}


	/**
	 * From DFCurves interface.
	 */
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DEAgeLeafSurface.xLabel"));
		v.add (Translator.swap ("DEAgeLeafSurface.yLabel"));
		return v;
	}


	/**
	 * From DFCurves interface.
	 */
	public int getNY () {
		return curves.size () - 1;
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}

	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "Y. Caraglio";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DEAgeLeafSurface.description");}


}









