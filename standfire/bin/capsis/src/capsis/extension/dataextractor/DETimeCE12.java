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
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.defaulttype.SpatializedTree;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.spatial.ClarkEvans;
import capsis.util.Grouper;
import capsis.util.GrouperManager;


/**
 * Clark & Evans intertype  versus Date.
 *
 * @author F. Goreaud - october 2004
 */
public class DETimeCE12 extends DETimeG {
	
	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeCE12");
	}
	
	/**
	* Phantom constructor.
	* Only to ask for extension properties (authorName, version...).
	*/
	public DETimeCE12 () {}
   
	/**
	* Official constructor. It uses the standard Extension starter.
	*/
	public DETimeCE12 (GenericExtensionStarter s) {
		super (s);
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
			if (tc.getTrees ().isEmpty ()) {
					return true;}
			
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
				if (!(i.next () instanceof SpatializedTree)) {
						return false;}
			}
         
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeCE12.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		
		return true;
	}
   
	/**
	* This method is called by superclass DataExtractor.
	*/
	public void setConfigProperties () {
		// Choose configuration properties
		// addConfigProperty (DataExtractor.HECTARE);
		//		addConfigProperty (DataExtractor.TREE_GROUP);
		//		addConfigProperty (DataExtractor.I_TREE_GROUP);		// group individual configuration
		
		// 1) : Defining the two groups for the intertype function :
		// FG : normally, this should be individual properties
		// fc - 10.6.2004 - combo property test
		// 
		GrouperManager gm = GrouperManager.getInstance();
		Collection trees = ((TreeCollection)(step.getScene())).getTrees();
		Collection grouperNames = gm.getGrouperNames (trees);
		
		LinkedList c = new LinkedList(grouperNames);
		addComboProperty("Type 1",c);
		LinkedList c2 = new LinkedList(grouperNames);
		addComboProperty("Type 2",c2);
		// FG : here we need to update the combolist
		
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
		if (upToDate) {
				return true;}
		if (step == null) {
				return false;}
		
		// Retrieve method provider
		methodProvider = step.getProject ().getModel ().getMethodProvider ();
		
		try {
			// Defining two groups
			System.out.println("Type 1="+getComboProperty("Type 1"));
			System.out.println("Type 2="+getComboProperty("Type 2"));
			// FG comment r�cup�rer les valeurs par d�faut pour les groupes ?
			// FDG : comment faire figurer ces noms dans le nom de la fen�tre ?
			
			// Retrieve Steps from root to this step
			Vector steps = step.getProject ().getStepsFromRoot (step);
			
			// Defining the vectors for the curves
			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			
			// Data extraction : points with (Integer, Double) coordinates
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step s = (Step) i.next ();
				GScene stand = s.getScene ();
				
				// Consider restriction to one particular group if needed
				//Collection aux = doFilter (stand);
				
				// Retrieve trees for this step
				
				GrouperManager gm = GrouperManager.getInstance ();
				
				Collection trees1 = ((TreeCollection) stand).getTrees ();
				Grouper g1 = gm.getGrouper (getComboProperty ("Type 1"));
				trees1 = g1.apply (trees1);
				System.out.println ("trees1 n="+trees1.size ());
				Iterator t1 = trees1.iterator ();
				
				Collection trees2 = ((TreeCollection) (step.getScene ())).getTrees ();
				Grouper g2 = gm.getGrouper (getComboProperty ("Type 2"));
				trees2 = g2.apply (trees2);
				System.out.println ("trees2 n="+trees2.size ());
				Iterator t2 = trees2.iterator ();
				
				
				
				
				int date = stand.getDate ();
				
				
				// Limited in size! (java initializes each value to 0)
				double x1[] = new double[trees1.size ()+1];
				double y1[] = new double[trees1.size ()+1];
				double x2[] = new double[trees2.size ()+1];
				double y2[] = new double[trees2.size ()+1];
				int pointnb1=0;
				int pointnb2=0;
				double xmi=step.getScene().getOrigin().x;
				double xma=xmi+step.getScene().getXSize();
				double ymi=step.getScene().getOrigin().y;
				double yma=ymi+step.getScene().getYSize();
				
				// Create output data
				while (t1.hasNext ()) {
					SpatializedTree t = (SpatializedTree) t1.next ();
					x1[pointnb1]=t.getX();		// x : m	// FG
					y1[pointnb1]=t.getY();		// y : m	// FG
					pointnb1+=1;
				}
				//System.out.println("D n1="+pointnb1);
				
				while (t2.hasNext ()) {
					SpatializedTree t = (SpatializedTree) t2.next ();
					//{	fc - 20.10.2004 - unused
					x2[pointnb2]=t.getX();		// x : m	// FG
					y2[pointnb2]=t.getY();		// y : m	// FG
					pointnb2+=1;
					//}	fc - 20.10.2004 - unused
				}
				//System.out.println("E n2="+pointnb2);
				
				
				Log.println("DETimeCE12 -> pointNumber1 : "+pointnb1+" xmi : "+xmi+"xma : "+xma+"ymi : "+ymi+" yma : "+yma);
				
				
				c1.add (new Integer (date));
				c2.add (new Double (ClarkEvans.computeCE12(x1,y1,pointnb1,x2,y2,pointnb2,xmi,xma,ymi,yma)));
			}
			
			curves.clear ();
			curves.add (c1);
			curves.add (c2);
		
		} catch (Exception exc) {
			Log.println (Log.ERROR, "DETimeCE12.doExtraction ()", "Exception caught : ",exc);
			return false;
		}
		
		upToDate = true;
		return true;
	}
	
	/**
	* From DataFormat interface.
	*/
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DETimeCE12");
	}
	
	/**
	* From DFCurves interface.
	*/
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DETimeCE12.xLabel"));
		if (settings.perHa) {
			v.add (Translator.swap ("DETimeCE12.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DETimeCE12.yLabel"));
		}
		return v;
	}
	
	/**
	* From Extension interface.
	*/
	public String getVersion () {
			return VERSION;}
	public static final String VERSION = "1.1";
	
	/**
	* From Extension interface.
	*/
	public String getAuthor () {
			return "F. Goreaud";}
	
	/**
	* From Extension interface.
	*/
	public String getDescription () {
			return Translator.swap ("DETimeCE12.description");}
	
}
