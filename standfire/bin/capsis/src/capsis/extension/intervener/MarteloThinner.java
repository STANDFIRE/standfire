/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2010  Francois de Coligny
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

package capsis.extension.intervener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jeeb.lib.util.Check;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.Group;
import capsis.util.GroupableIntervener;

/**
 * MarteloThinner: a tool to cut trees which ids are in lists made by different
 * teams.
 * 
 * @author F. de Coligny - sep 2010
 */
public class MarteloThinner implements Intervener, GroupableIntervener, Automatable {

	static {
		Translator.addBundle("capsis.extension.intervener.MarteloThinner");
	}

	public static final String NAME = "MarteloThinner";
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "F. de Coligny";
	public static final String DESCRIPTION = "MarteloThinner.description";
	static public String SUBTYPE = "SelectiveThinner";

	@Ignore
	private boolean constructionCompleted = false; // if cancel in interactive
													// mode, false
	@Ignore
	private GScene stand; // Reference stand: will be altered by apply ()
	@Ignore
	private GModel model;
	@Ignore
	protected Collection<Tree> concernedTrees;
	@Ignore
	protected List<Integer> treeIdsToBeCut; // the trees with these ids will be
											// cut

	/**
	 * Default constructor.
	 */
	public MarteloThinner() {
	}

	/**
	 * Script constructor.
	 */
	public MarteloThinner(List<Integer> treeIdsToBeCut) {

		this.treeIdsToBeCut = treeIdsToBeCut;

		constructionCompleted = true;
	}

	/**
	 * A constructor for script mode. See ScriptMinna2014. init () will be
	 * called just after, see example below.
	 * 
	 * Here, we process only files with one single column. A text header is
	 * expected, it will be ignored. Blank lines will also be ignored. All
	 * numbers will be considered to be ids of trees to be cut.
	 * 
	 * <pre>
	 * Intervener intervener = new MarteloThinner(removalFileName);
	 * step = script.runIntervener(intervener, step);
	 * </pre>
	 * 
	 * fc-16.12.2014
	 */
	public MarteloThinner(String removalFileName) throws Exception {
		MarteloFileReader r = new MarteloFileReader(removalFileName);
		r.interpret();
		Object[][] lines = r.getLines();

		this.treeIdsToBeCut = new ArrayList<Integer>();

		// Each line is supposed to contain a treeId (int) except the header
		// line (if other columns, only the first column is processed).
		for (int i = 0; i < lines.length; i++) {
			Object[] line = lines[i];

			// We expect only *one* treeId in each line
			Object word = line[0];

			try {
				int treeId = (Integer) word;
				treeIdsToBeCut.add(treeId);
			} catch (Exception e) {
			} // header is not an int, ignore

		}

	}

	/**
	 * Init the thinner on a given scene.
	 */
	@Override
	public void init(GModel model, Step s, GScene scene, Collection c) {
		// This is always in starter for every intervener
		this.stand = scene; // this is referentStand.getInterventionBase ();
		this.model = model;

		if (c == null) {
			concernedTrees = (Collection<Tree>) ((TreeList) stand).getTrees(); // all
																				// trees
		} else {
			concernedTrees = c; // restrict to the given collection
		}

		constructionCompleted = true;
	}

	/**
	 * Open a dialog to tune the thinner.
	 */
	@Override
	public boolean initGUI() throws Exception {
		// Interactive start
		MarteloThinnerDialog dlg = new MarteloThinnerDialog(this);
		constructionCompleted = false;
		if (dlg.isValidDialog()) {
			// Valid -> ok was hit and all checks were ok
			try {
				treeIdsToBeCut = dlg.getTreeIdsToBeCut();

				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception("MarteloThinner (): Could not get parameters in MarteloThinnerDialog due to " + e);
			}
		}
		dlg.dispose();
		return constructionCompleted;

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof GModel)) {
				return false;
			}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject().getRoot()).getScene();
			if (!(s instanceof TreeList)) {
				return false;
			}
			TreeList tl = (TreeList) s;
			if (tl.getTrees().isEmpty()) {
				return true;
			} // compatible with empty stands

		} catch (Exception e) {
			Log.println(Log.ERROR, "MarteloThinner.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on trees, tree groups
	 * can be processed.
	 */
	public String getGrouperType() {
		return Group.TREE;
	}

	/**
	 * These assertions are checked at the beginning of apply ().
	 */
	private boolean assertionsAreOk() {
		if (stand == null) {
			Log.println(Log.ERROR, "MarteloThinner.assertionsAreOk ()",
					"stand is null. MarteloThinner is not appliable.");
			return false;
		}

		if (treeIdsToBeCut == null || treeIdsToBeCut.size() == 0) {
			Log.println(Log.ERROR, "MarteloThinner.assertionsAreOk ()",
					"The list of tree ids to be cut is null or empty. MarteloThinner is not appliable.");
			return false;
		}

		return true;
	}

	/**
	 * Intervener. Control input parameters.
	 */
	public boolean isReadyToApply() {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk()) {
			return true;
		}
		return false;
	}

	/**
	 * Intervener. Makes the action: thinning.
	 */
	public Object apply() throws Exception {
		// Check if apply is possible (should have been done before: security)
		if (!isReadyToApply()) {
			throw new Exception("MarteloThinner.apply () - Wrong input parameters, see Log");
		}

		stand.setInterventionResult(true);

		// 1. Iterate and cut
		Set<Integer> ids = new HashSet<Integer>(treeIdsToBeCut);

		for (Iterator i = concernedTrees.iterator(); i.hasNext();) {
			Tree t = (Tree) i.next();

			// Do we cut this tree ?
			if (ids.contains(t.getId())) { // yes
				// Cut or mark
				if (!model.isMarkModel()) {
					i.remove();
					((TreeList) stand).removeTree(t);
				} else {
					t.setMarked(true);
				}

				// Individual or numberable
				if (!(t instanceof Numberable)) {
					((TreeList) stand).storeStatus(t, "cut");
				} else {
					Numberable n = (Numberable) t;
					((TreeList) stand).storeStatus(n, "cut", n.getNumber()); // "cut all this tree"
				}
			}

		}

		return stand;
	}

	/**
	 * String description.
	 */
	public String toString() {
		return "class=" + getClass().getName() + " name=" + NAME + " constructionCompleted=" + constructionCompleted;
	}

	/**
	 * Intervener
	 */
	@Override
	public void activate() {
		// not used

	}

}
