/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
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

package capsis.defaulttype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jeeb.lib.util.Alert;
import jeeb.lib.util.Log;
import jeeb.lib.util.SetMap;
import jeeb.lib.util.Spatialized;
import capsis.kernel.DateCorrectable;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Plot;
import capsis.util.methodprovider.NProvider;

/**	TreeList is a collection of trees, associated or not to a plot with cells.
 *	It is a GScene implementation.
 *
 *	WARNING : if subclassed and subclass holds any object instance variables
 *	(not primitive types), subclass must redefine "public Object clone ()"
 *	to provides clonage for these objects (see RectangularPlot.clone () for template).
 *
 *	@author F. de Coligny - june 1999
 */
public abstract class TreeList extends SimpleScene implements DateCorrectable, TreeCollection {

	private static final long serialVersionUID = 1L;

	/**	This class contains immutable variables for a TreeList.
	 * 	Both getEvolutionBase () and getInterventionBase () return
	 * 	instances sharing this Immutable part with the original TreeList.
	 *	@see Tree
	 */
	public static class Immutable extends SimpleScene.Immutable {
		private static final long serialVersionUID = 1L;

		public boolean dateCorrectionEnabled;	// fc - 15.5.2003
		public boolean dateCorrected;
		public int dateCorrection;
		public int stepCorrection;
		public int rootDate;	// not corrected

	}

	// The alive trees
	protected Set<Tree> alive; 

	// A map id -> Tree for the alive trees
	protected Map<Integer, Tree> idmap;

	/**	This map contains references to trees according to their status.
	 *	Ex of status: alive, new, cut, dead, windfall...
	 *	Structure in the SetMap: status -> set of trees.
	 *	Note that "alive" always refers to getTrees (). As getTrees () may contain 
	 *	marked trees (i.e. dead trees), you should pay attention not to consider 
	 *	them as alive. 
	 *	Interveners store "cut" trees in status.
	 * 	F. de Coligny - 19.3.2004
	 */
	protected SetMap<String, Tree> statusMap;		// fc - 19.3.2004

	//	protected Collection<TCListener> tcListeners;  // unsused
	

	/**	Constructor for a new logical TreeList. 
	 *	Immutable object is created.
	 */
	public TreeList () {

		super();

		// At beginning, date correction is enabled and not active
		getImmutable().dateCorrected = false;
		getImmutable().dateCorrection = 0;
		getImmutable().stepCorrection = 1;

		init ();

		initialScene = true;
		interventionResult = false;

	}

	/** Initialisation 
	 */
	public void init () {

		statusMap = new SetMap<String, Tree> (); 
		statusMap.addObjects("alive", new HashSet<Tree>());
		alive = statusMap.get("alive");
		idmap = new HashMap<Integer, Tree>();

	}

	/**	Create an Immutable object whose class is declared at one level of the hierarchy.
	 *	This is called only in constructor for new logical object in superclass. 
	 *	If an Immutable is declared in subclass, subclass must redefine this method
	 *	(same body) to create an Immutable defined in subclass.
	 */
	protected void createImmutable () {immutable = new Immutable ();}
	
	protected Immutable getImmutable() { return (Immutable) immutable;}	

	/**	Clone a TreeList: first calls super.clone (), then clone the TreeList instance variables.
	 *	This is a very specific cloning : see getLightClone () and getHeavyClone ().
	 */
	public Object clone () {
		try {
			TreeList s = (TreeList) super.clone ();

			s.immutable = immutable;	// same immutable (it was made for that)
			s.step = null;				// no step, cloned scene may be added to a new step
			s.init ();

			
			// fc-9.12.2011 Set the PLOT REF TO null
			// Warning in ModisPinaster on integrated interventions: newScene.getPlot ().getScene () == refScene
			s.plot = null;
			
			
			// Don't change other cloned value (date, initialScene, interventionResult)
			return s;

		} catch (Exception exc) {
			Log.println (Log.ERROR, "TreeList.clone ()", "Exception caught, source scene="+this, exc);
			return null;
		}
	}

	/**	Store a tree ref relatively to its status (ex: new, cut, dead...). 
	 *	This method applies for individuals trees.
	 *	This optional feature can be used explicitly in modules when evolution is processed
	 *	to store dead trees, new trees (...) and to be able to retrieve them later.
	 *	This does not replace addTree (t) in case of new tree : you must also do it.
	 *	Interveners use it to store "cut" trees.
	 */
	public void storeStatus (Tree tree, String status) {		// fc - 19.3.2004
		if (tree == null) {return;}
		if (tree instanceof Numberable) {
			Alert.print ("The method storeStatus (GTree, String) was called for a Numberable tree (id="
					+tree.getId ()
					+"). Please check model code. Status was not stored");
			return;
		}
		if (status == null) {
			Alert.print ("The method storeStatus (GTree, String) was called with a null status. "
					+"Please check model code. Status was not stored");
			return;
		}
		if (statusMap == null) {statusMap = new SetMap<String, Tree> ();}

		if (status.equals ("alive")) {
			addTree (tree);
		} else {
			statusMap.addObject (status, tree);
		}

	}

	/**	Store a number of trees relatively to a status (ex: new, cut, dead...). 
	 *	See storeStatus (Tree tree, String status) for individual trees. 
	 *	Interveners use it to store "cut" trees.
	 *	This method applies for Numberable trees only. A simple clone of the tree id built 
	 *	and its number is changed to numberUnderThisStatus. 
	 *	Later, when reading the tree, all the properties of the original tree (dbh...) will 
	 *	be accessible. The cloned tree MUST NOT BE ALTERED because all its references 
	 *	are the same than the original (ex: changing the date of its scene will change the date 
	 *	of the original scene: to be avoided)
	 */
	public void storeStatus (Numberable tree, String status, double numberUnderThisStatus) {
		if (!(tree instanceof Tree)) {
			Alert.print ("The method storeStatus (Numberable, String, double) was called for an object which is not a GTree (id="
					+tree
					+"). Please check model code. Status was not stored");
			return;
		}
		Numberable t = (Numberable) ((Tree) tree).identicalClone ();	// IMPORTANT: we store a simple clone : do not alter it !

		t.setNumber (numberUnderThisStatus);
		if (statusMap == null) {statusMap = new SetMap<String, Tree> ();}
		
		if (status.equals ("alive")) {
			addTree ((Tree) tree);
		} else {
			statusMap.addObject (status, (Tree) t);
		}
	}

	//public SetMap<String, GTree> getStatusMap () {return statusMap;}		// fc - 19.3.2004
	
	public Set<String> getStatusKeys () {return statusMap.keySet();}		// fc - 19.3.2004

	/**	Get the collection of trees with the given status.
	 *	If null or unknown status, return an empty collection (fc - 23.4.2004).
	 *	Note that getTrees ("alive") return same collection than getTrees ().
	 *	Never return null, may return an empty collection.
	 */
	public Collection<Tree> getTrees (String status) {	

		if (status == null) {
			Log.println ("TreeList.getTrees (status): status = null, returned an empty collection ***");
			return new ArrayList<Tree> ();
		}		

		return statusMap.getObjects (status);		
	}

	/**	Get the union collection of trees with a status in the given status list.
	 *	If one status is wrong (null or not existing), ignore it (fc - 23.4.2004);
	 *	Never return null, may return an empty collection.
	 */
	public Collection<Tree> getTrees (String[] status) {	// fc - 22.3.2004
		// No status -> return an empty collection
		if (status == null) {
			Log.println ("TreeList.getTrees (status[]): status = null, returned an empty collection *** ***");
			return new ArrayList<Tree> ();
		}		


		// Single known status -> use method with single parameter
		if (status.length == 1) {
			return getTrees (status[0]);
		}	// fast and economic

		// Several status -> concatenate collections
		// Caution: If some status is unknown in this scene, ignore it

		Collection<Tree> result = new ArrayList<Tree> ();
		for (int i = 0; i < status.length; i++) {
			String key = status[i];
			if (statusMap.keySet ().contains (key)) {
				Collection<Tree> r = getTrees (key);
				result.addAll (r);
			}
		}

		return result;
	}

	/**	Returns the status of the given tree in the statusMap of this TreeList.
	 * 	If not found or trouble, returns "".	
	 */
	public String getStatus (Tree t) {
		try {
			for (String status : statusMap.keySet()) {  // there are few keys in the statusMap: fast
				Set<Tree> set = statusMap.getObjects (status);
				if (set.contains (t)) {return status;}  // contains () is fast in HashSets: fast
			}
		} catch (Exception e) {}
		return "";
	}

	/**	GScene Interface.
	 * 
	 *	Used by evolution processes in modules. General concept is to use a base for
	 *	evolution (without trees in it), which will be the new scene. Then, consider
	 *	every tree in old scene, make it evolve (gives a new instance)  and add it in 
	 *	this new scene.
	 */
	public GScene getEvolutionBase () {
		return getLightClone ();
	}

	/**	GScene Interface.
	 * 
	 *	Interventions can occur on the object returned.
	 *	Return a kind of clone of this object : a complete clone, containing
	 *	clones of every trees.
	 *	Can be redefined to return other types of clones.
	 */
	public GScene getInterventionBase () {
		return getHeavyClone ();
	}

	/**	Result -> a scene with no step, false initialScene, false interventionResult,
	 *	same date, plot and cells if original have some, no trees, 
	 *	same bottomLeft origin, same width and height.
	 */
	protected GScene getLightClone () {
		TreeList lightScene = null;
		try {
			lightScene = (TreeList) this.clone ();		
			lightScene.initialScene = false;
			lightScene.interventionResult = false;

			// Deal with plot and cells (no trees yet)	// fc MOVED FROM clone () on 5.12.2000
			if (this.hasPlot ()) {
				lightScene.plot = (Plot) plot.clone ();
				lightScene.plot.setScene (lightScene);
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "TreeList.getLightClone ()", "Exception caught, source scene="+this, e);
		}

		return lightScene;
	}

	/**	Result -> getLightClone () plus tree clones registered in cells if some exist.
	 */
	protected GScene getHeavyClone () {
		TreeList heavyScene = (TreeList) getLightClone ();

		try {
			// Copy the trees: deal with treeId_Tree
			for (Iterator<? extends Tree> i = getTrees ().iterator (); i.hasNext ();) {
				Tree t = (Tree) i.next ();
				heavyScene.addTree ((Tree) t.clone ());	// deals with plot registration if necessary
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "TreeList.getHeavyClone ()", "Exception caught, source scene="+this, e); 
		}

		return heavyScene;
	}

	/**	Creates a plot which creates its cells.
	 *	Called explicitly for first scene.
	 *	This method can be redefined in subclasses to create another type of plot.
	 */
	public void createPlot (GModel model, double cellWidth){
		plot = new RectangularPlot (this, cellWidth);
		((RectangularPlot) plot).createCells ();

		// At first time, trees have been added without registration 
		// because cells did not exist yet.
		makeTreesPlotRegister ();	
	}

	/**	Tells all the trees of the scene to go and register in the plot
	 *	to obtain their cell allocation. 
	 *	Works only with spatialized trees. 
	 */
	public void makeTreesPlotRegister (){
		for (Tree t : getTrees()) {
			t.registerInPlot (plot);
		}
	}


	public int size () { return alive.size (); }

	
	/**	Adds a tree directly with a status	
	 */
	// NOT SO EASY: should be addTree (t), removeTree (t), storeStatus (t, status) -> wait for status refactorization
	// NOT SO EASY: should be addTree (t), removeTree (t), storeStatus (t, status) -> wait for status refactorization
	// NOT SO EASY: should be addTree (t), removeTree (t), storeStatus (t, status) -> wait for status refactorization
//	public boolean addTree (Tree t, String status) {
//		boolean yep = addTree (t);
//		storeStatus (t, status);
//		return yep;
//	}
	
	
	/**	TreeCollection interface.
	 * 
	 *	Add a tree in the scene.
	 *	If the tree is spatialized, try to register it in its plot cell.
	 */
	public boolean addTree (Tree tree){
		// fc - october 2001

		tree.setScene (this);	// [fc] to correct bug: added trees have wrong GScene reference
		alive.add (tree);
		idmap.put(tree.getId(), tree);

		if (tree instanceof Spatialized) {
			
			plot.adaptSize((Spatialized) tree);
			

			// associate cell to tree
			if(plot.hasCells()) {

				TreeListCell c = tree.getCell ();
				if (c != null) { return true; }		// already knows its cell - fc - 16.1.2002 // fc - 17.12.2003 - true: was added in scene

				c = plot.matchingCell((Spatialized)tree);

				// fc - 17.12.2003 - if tree belongs to no cell, remove it from scene (SOM bug in Alisier)
				if (c == null) {
					tree.setScene (null);
					removeTree(tree);
					Spatialized t = (Spatialized) tree;
					Log.println (Log.ERROR, "TreeList.addTree ()", "tree " + tree.getId ()
							+" ("+t.getX ()+","+t.getY ()+") in no cell -> REMOVED from scene, please correct input");
					return false;	// fc - 17.12.2003 - false: was not added in scene
				}
				else {
					c.registerTree (tree);
				}

			}
		}
		return true;	// fc - 17.12.2003 - true: was added in scene
	}

	/**	TreeCollection interface.
	 */
	public void removeTree (Tree tree){

		if (tree.getCell () != null) {
			tree.unregisterFromPlot ();
		}

		idmap.remove(tree.getId ());
		alive.remove(tree);
	}

	/**	TreeCollection interface.
	 */
	public void clearTrees () {
		alive.clear();
		idmap.clear();
	}

	/**	TreeCollection interface.
	 */
	public Collection<? extends Tree> getTrees () { return alive; }
	
	/**	TreeCollection interface.
	 */
	public Tree getTree (int treeId) {return idmap.get (treeId);}
	
	/**	TreeCollection interface.
	 */
	public Collection<Integer> getTreeIds () {return idmap.keySet(); }

	
	
	/**	Return the "new" trees which were not present in prevScene
	 * 	and are present in this scene.
	 * 	WARNING: these trees are part of this.getTrees ().
	 */
	public Collection<Tree> getNewTrees (TreeList prevScene) {

		// If prevScene is null, all trees are new
		if (prevScene == null) {return new ArrayList<Tree> (this.getTrees ());}

//		trees.removeAll (prevScene.getTrees());  // NOT ENOUGH (found by GV, ML - 6.12.2010)
		
		// Get the ids of the trees in prevScene
		Set<Integer> prevIds = new HashSet<Integer> ();
		for (Tree t : prevScene.getTrees ()) {prevIds.add (t.getId ());}

		// Retain the trees not in prevIds: the new trees
		Collection<Tree> newTrees = new ArrayList<Tree> ();
		for (Tree t : this.getTrees ()) {
			if (!prevIds.contains (t.getId ())) {newTrees.add (t);}
		}
		
		return newTrees;
	}

	/**	Return the "missing" trees which were present in prevScene
	 * 	and are not present in this scene.
	 * 	WARNING: these trees are part of prevScene.getTrees ().
	 */
	public Collection<Tree> getMissingTrees (TreeList prevScene) {
		
		// If prevScene is null, no tree is missing
		if (prevScene == null) {return new ArrayList<Tree> ();}

		// Get the ids of the trees in this scene
		Set<Integer> ids = new HashSet<Integer> ();
		for (Tree t : this.getTrees ()) {ids.add (t.getId ());}

//		trees.removeAll (alive);  // NOT ENOUGH (found by GV, ML - 6.12.2010)

		// Retain the trees in prevScene which ids are not in the ids list
		Collection<Tree> missingTrees = new ArrayList<Tree> ();
		for (Tree t : prevScene.getTrees ()) {
			if (!ids.contains (t.getId ())) {missingTrees.add (t);}
		}
		
		return missingTrees;
	}

//	public void addTCListener (TCListener l) {
//		if (tcListeners == null) {tcListeners = new ArrayList<TCListener> ();}
//		tcListeners.add (l);
//	}
//	public void removeTCListener (TCListener l)  { tcListeners.remove (l);}
//	public Collection<TCListener> getTCListeners () {return tcListeners; }

	public boolean isDateCorrectionEnabled () {return getImmutable().dateCorrectionEnabled;}
	public boolean isDateCorrected () {return getImmutable().dateCorrected;}
	public int getDateCorrection () {return getImmutable().dateCorrection;}
	public int getStepCorrection () {return getImmutable().stepCorrection;}

	public void setDateCorrectionEnabled (boolean v) {getImmutable().dateCorrectionEnabled = v;}
	public void setDateCorrected (boolean v) {getImmutable().dateCorrected = v;}
	public void setDateCorrection (int v) {getImmutable().dateCorrection = v;}
	public void setStepCorrection (int v) {getImmutable().stepCorrection = v;}

	/**	Return scene date (maybe corrected by correction mechanism).
	 */
	public int getDate () {

		if (!isDateCorrectionEnabled () || !isDateCorrected ()) {
			return date;

		} else  {	// date -> corrDate
			int corrDate = correctDate (date);
			return corrDate;
		}

	}

	public void setDate (int d) {

		if (!isDateCorrectionEnabled () || !isDateCorrected ()) {
			date = d;
			if (initialScene) {	getImmutable().rootDate = d; }

		} else  {	// corrDate -> date
			date = unCorrectDate (d);

			if (initialScene) {
				getImmutable().rootDate = date;
			}
		}
	}

	/**	Date of root scene, not corrected (original value).
	 */
	public int getRootDate () {return getImmutable().rootDate;}

	private int correctDate (int d) {			
		//~ return getDateCorrection () + (d * getStepCorrection ());
		return (getDateCorrection () + d) * getStepCorrection ();
	}
	private int unCorrectDate (int d) {			
		return (d - correctDate (getImmutable().rootDate)) / getStepCorrection () + getImmutable().rootDate;
	}

	/**	Scene caption is of type "*17" (with * if scene is intervention result and 17 is the scene date).
	 *	Note: date may be corrected (see getDate ()).
	 */
	public String getCaption () {
		if (isInterventionResult ()) {
			return "*"+String.valueOf (getDate ());
		} else {
			return String.valueOf (getDate ());
		}
	}

	/**	Default: redefine in subClasses as you like
	 */
	public String getToolTip () {
		MethodProvider mp = getStep ().getProject ().getModel ().getMethodProvider ();
		try {
			return "N = "+((NProvider) mp).getN (this, getTrees ());
		} catch (Exception e) {
			return this.toString ();
		} catch (Error err) {
			// do nothing (TreeList may be used outside Capsis, NProvider may not exist, think Simeo)
			return "";
		}
	}

	public String toString () {
		return "TreeList_"+getCaption ();
	}

	public String bigString () {
		StringBuffer sb = new StringBuffer (toString ());
		sb.append (" date=");
		sb.append (date);
		sb.append (" bottomLeft=");
		sb.append (getOrigin());
		sb.append (" width=");
		sb.append (getXSize());
		sb.append (" height=");
		sb.append (getYSize());
		sb.append (" area=");
		sb.append (getArea ());
		sb.append (" initialScene=");
		sb.append (initialScene);
		sb.append (" interventionResult=");
		sb.append (interventionResult);
		sb.append (" sourceName=");
		sb.append (getImmutable().sourceName);

		if (plot != null) {
			sb.append (" plot=");
			sb.append (plot);	// will trigger toString (), else reference
		} else {
			sb.append (" *** NO PLOT");
		}

		sb.append (" ");
		sb.append ("Scene contains ");
		sb.append (getTrees ().size ());
		sb.append (" tree(s)\n");

		sb.append (completeString ());
		return sb.toString ();
	}

	protected String completeString () {
		StringBuffer sb = new StringBuffer ();
		sb.append ("Scene trees {");
		if (getTrees () != null) {
			sb.append ("\n");
			for (Tree t : getTrees ()) {
				sb.append (t.bigString ());
				sb.append ("\n");
			}
			sb.append ("}\n");
		} else {
			sb.append (" ***NO TREES\n");
		}
		return sb.toString ();
	}


}


