/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package capsis.extension.intervener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.Group;
import capsis.util.GroupableIntervener;

/**
 * DHAThinner: a tool that cuts trees according to their Dbh, Height or Age.
 * 
 * @author F. de Coligny - march 2002
 */
public class DHAThinner implements Intervener, GroupableIntervener, Automatable {

	public static final String NAME = "DHAThinner";
	public static final String VERSION = "1.2";
	public static final String AUTHOR = "F. de Coligny";
	public static final String DESCRIPTION = "DHAThinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	static {
		Translator.addBundle ("capsis.extension.intervener.DHAThinner");
	}

	public static final int DBH = 2;
	public static final int HEIGHT = 3;
	public static final int AGE = 4;

	@Ignore
	private boolean constructionCompleted = false; // if cancel in interactive mode, false
	@Ignore
	private int mode; // CUT or MARK
	@Ignore
	private GScene stand; // Reference stand: will be altered by apply ()
	@Ignore
	protected Collection<Tree> concernedTrees; // if intervention is restricted to a group

	private Collection<Integer> treeIds;
	private int context;
	private float min;
	private float max;

	
	
	/**
	 * Default constructor.
	 */
	public DHAThinner () {}

	/**
	 * Script constructor.
	 */
	public DHAThinner (int context, float min, float max) {

		this.context = context;
		this.min = min;
		this.max = max;
		constructionCompleted = true;
	}

	@Override
	public void init (GModel m, Step s, GScene scene, Collection c) {
		stand = scene;  // this is referentStand.getInterventionBase ();

		// The trees that can be cut
		if (c == null) {
			concernedTrees = (Collection<Tree>) ((TreeList) stand).getTrees ();
		} else {
			concernedTrees = c;
		}

		// Save ids for future use
		treeIds = new HashSet<Integer> ();
		for (Object o : concernedTrees) {
			Tree t = (Tree) o;
			treeIds.add (t.getId ());
		}
		
		// Define cutting mode: ask model
		mode = (m.isMarkModel ()) ? MARK : CUT;
		constructionCompleted = true;
	}

	@Override
	public boolean initGUI () throws Exception {
		// Interactive start
		DHAThinnerDialog dlg = new DHAThinnerDialog (this);
		
		constructionCompleted = false;
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and all checks were ok
			try {
				context = dlg.getContext ();
				min = dlg.getMin ();
				max = dlg.getMax ();
				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception ("DHAThinner (): Could not get parameters in DHAThinnerDialog", e);
			}
		}
		dlg.dispose ();
		return constructionCompleted;

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchWith method checks if the extension can
	 * deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) { return false; }
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeList)) { return false; }
			TreeList treeList = (TreeList) s;
			if (treeList.getTrees ().isEmpty ()) { return true; }  // compatible with empty stands

		} catch (Exception e) {
			Log.println (Log.ERROR, "DHAThinner.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on trees, tree groups can be processed.
	 */
	public String getGrouperType () {
		return Group.TREE;
	}

	/**
	 * These assertions are checked at the beginning of apply () in 
	 * script AND interactive mode.
	 */
	private boolean assertionsAreOk () {
		if (mode != CUT && mode != MARK) {
			Log.println (Log.ERROR, "DHAThinner.assertionsAreOk ()", "Wrong mode=" + mode + ", should be " + CUT + " (CUT) or " + MARK
					+ " (MARK). DHAThinner is not appliable.");
			return false;
		}
		if (stand == null) {
			Log.println (Log.ERROR, "DHAThinner.assertionsAreOk ()", "stand is null. DHAThinner is not appliable.");
			return false;
		}
		if (min < 0) {
			Log.println (Log.ERROR, "DHAThinner.assertionsAreOk ()", "min < 0. DHAThinner is not appliable.");
			return false;
		}
		if (max < min) {
			Log.println (Log.ERROR, "DHAThinner.assertionsAreOk ()", "max < min. DHAThinner is not appliable.");
			return false;
		}

		return true;
	}

	/**
	 * Intervener interface. Controls input parameters.
	 */
	public boolean isReadyToApply () {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk ()) { return true; }
		return false;
	}

	/**
	 * Intervener interface. Makes the action: cuts trees.
	 */
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) { throw new Exception ("DHAThinner.apply () - Wrong input parameters, see Log"); }

		stand.setInterventionResult (true);

		// 1. Iterate and cut
		int i = 0;
		Iterator<Integer> it = treeIds.iterator ();
		while (it.hasNext ()) {

			Integer id = it.next ();
			Tree t = ((TreeList) stand).getTree (id);

			if ((context == DBH && t.getDbh () >= min && t.getDbh () <= max) 
					|| (context == HEIGHT && t.getHeight () >= min && t.getHeight () <= max)
					|| (context == AGE && t.getAge () >= min && t.getAge () <= max)) {
				if (mode == CUT) {
					it.remove ();
					((TreeCollection) stand).removeTree (t);

				} else if (mode == MARK) {
					t.setMarked (true);
				}

				// All interveners working on TreeList instances are supposed to store the cut trees 
				// on the result stand under the "cut" status
				if (!(t instanceof Numberable)) {
					((TreeList) stand).storeStatus (t, "cut");
				} else {
					Numberable n = (Numberable) t;
					((TreeList) stand).storeStatus (n, "cut", n.getNumber ()); // fc + tl - 7.3.2006: "cut all this tree"
				}

			}

		}

		return stand;
	}

	/**
	 * toString () method.
	 */
	public String toString () {
		return "class=" + getClass ().getName () + " name=" + NAME + " constructionCompleted=" + constructionCompleted + " mode=" + mode + " stand=" + stand + " context="
				+ context + " min=" + min + " max=" + max;
	}

	@Override
	public void activate () {
		// TODO Auto-generated method stub

	}

}
