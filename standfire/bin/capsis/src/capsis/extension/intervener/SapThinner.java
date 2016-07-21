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

package capsis.extension.intervener;

import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;


/**
 * Create an SapThinner.
 * For interactive mode, use constructor with ExtensionStarter (containing stand to
 * thin and mode CUT/MARK trees). A dialog box is showed to get user choices.
 * For console mode, use the other constructor with specific paramater 
 * SapThinnerStarter.
 * 
 * @author F. de Coligny - april 2001
 */
public class SapThinner implements Intervener {

	public static final String NAME = "SapThinner";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "F. de Coligny";
	public static final String DESCRIPTION = "SapThinner.description";
	static public String SUBTYPE = "FormationThinner";
	

	private boolean constructionCompleted;		// if cancel in interactive mode, false
	private int mode;				// CUT or MARK
	private GScene stand;			// Reference stand: will be altered by apply ()

	private int frequency;			// Cut (or mark) trees according to this freq.
	private boolean ok = false;

	static {
		Translator.addBundle ("capsis.extension.intervener.SapThinner");
	}


	public SapThinner () {	}

	
	/**
	 * Build an individual thinner in console mode from an SapThinnerStarter.
	 * It can then be executed by apply ().
	 */
	public SapThinner (int freq) {
		frequency = freq;
		ok = true;
		
	}

	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {

		stand = scene;
		//frequency = s.frequency;

		// Define mode : ask model
		if (m.isMarkModel ()) {
			mode = MARK;
		} else {
			mode = CUT;
		}
	}

	@Override
	public boolean initGUI() throws Exception {
	
		SapThinnerDialog dlg = new SapThinnerDialog ();
		constructionCompleted = false;
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and check were ok
			try {
				frequency = dlg.getFrequency ();
				ok = true;
					
			} catch (Exception e) {
				
			}
		}
		dlg.dispose ();
		
		return constructionCompleted;
	}
	
	
	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeList)) {return false;}

			// Check we have no Numberable Gtree instances in collection
			// fc - 18.3.2004
			//
			Collection reps = Tools.getRepresentatives (((TreeList) s).getTrees ());	// one object for each class in collection
			if (reps != null) {
				for (Iterator i = reps.iterator (); i.hasNext ();) {
					if (i.next () instanceof Numberable) {
						return false;
					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "SapThinner.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	//
	// These assertions must be checked before apply.
	//
	private boolean assertionsAreOk () {
		if (mode != CUT && mode != MARK) {
			Log.println (Log.ERROR, "SapThinner.assertionsAreOk ()", "Wrong mode="+mode
					+", should be "+CUT+" (CUT) or "+MARK+" (MARK). SapThinner is not appliable.");
			return false;
		}

		if (stand == null) {
			Log.println (Log.ERROR, "SapThinner.assertionsAreOk ()", 
			"stand is null. SapThinner is not appliable.");
			return false;
		}
		if (frequency < 1) {
			Log.println (Log.ERROR, "SapThinner.assertionsAreOk ()", 
			"frequency < 1. SapThinner is not appliable.");
			return false;
		}


		return true;
	}

	/**
	 * From Intervener.
	 * Control input parameters.
	 */
	public boolean isReadyToApply () {
		
		return assertionsAreOk () && ok;
	}

	/**
	 * From Intervener.
	 * Makes the action : thinning.
	 */
	@Override
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {	
			throw new Exception ("SapThinner.apply () - Wrong input parameters, see Log");
		}

		stand.setInterventionResult (true);

		// 1. Iterate and cut
		int i = 0;
		for (Iterator ite = ((TreeList) stand).getTrees ().iterator (); ite.hasNext ();) {
			Tree t = (Tree) ite.next ();


			if (++i % frequency == 0) {
				if (mode == CUT) {
					ite.remove ();
					((TreeCollection) stand).removeTree (t);
				} else if (mode == MARK) {
					t.setMarked (true);
				}

				// fc - 18.3.2004
				if (!(t instanceof Numberable)) {((TreeList) stand).storeStatus (t, "cut");}

			}

		}

		return stand;
	}

	
	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString () {
		return "class="+getClass().getName ()
		+" name=\""+ NAME +"\""
		+" frequency="+frequency;
	}



	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}


}






