/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2001  Francois de Coligny
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 */
package capsis.extension.intervener;

import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.economics.EconModel;
import capsis.lib.economics.EconStand;

/**
 * Create an EconomicIntervention. Does not affect stand, defined by an intervention name.
 * Can be used for economic calculations.
 * 
 * @author C. Orazio - january 2003
 */
public class EconomicIntervention implements Intervener {

	private GScene stand;			// Reference stand: will be altered by apply ()
	private String name;	// intervention is defined by a name (ex: clearing, pruning)
	private boolean constructionCompleted = false;
	
	public static final String NAME = "EconomicIntervention";
	public static final String VERSION = "1.1";
	public static final String AUTHOR =  "C. Orazio";
	public static final String DESCRIPTION = "EconomicIntervention.description";
	static public String SUBTYPE = "MiscellaneousIntervener";

	static {
		Translator.addBundle ("capsis.extension.intervener.EconomicIntervention");
	}


	/**
	 * Phantom constructor. 
	 * Only to ask for extension properties (authorName, version...).
	 */
	public EconomicIntervention () {}

	/**
	 * This constructor must be used to create a usable instance of EconomicIntervention.
	 * If the given ExtensionStarter is an instance of EconomicInterventionStarter, every needed
	 * construction parameters are inside (ex: script mode).
	 * If not (ex: interactive mode),  we need to open a dialog box for the user to choose 
	 * the missing parameters.
	 * Then, apply () must be called.
	 */
	public EconomicIntervention (String name) throws Exception {
		
		this.name = name;
		constructionCompleted = true;		
		
	}
	
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {
		stand = scene;
		
	}

	@Override
	public boolean initGUI() throws Exception {
		// 3. Interactive start
		EconomicInterventionDialog dlg = new EconomicInterventionDialog ();
		constructionCompleted = false;
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and check were ok
			try {
				name = dlg.getName ();
				constructionCompleted = true;		
			} catch (Exception e) {
				throw new Exception ("EconomicIntervention (): Could not get parameters in EconomicInterventionDialog due to "+e);
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
			if (! (referent instanceof GModel)) {return false;}
			if (! (referent instanceof EconModel)) {return false;}
			
			GModel m = (GModel) referent;
			Step root = (Step) m.getProject ().getRoot ();
			GScene s = root.getScene ();
			if (! (s instanceof EconStand)) {return false;}
					
		} catch (Exception e) {
			Log.println (Log.ERROR, "EconomicIntervention.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		
		return true;
	}

	//
	// These assertions are checked at the beginning of apply ().
	//
	private boolean assertionsAreOk () {
		if (name == null || name.length () == 0) {
			Log.println (Log.ERROR, "EconomicIntervention.assertionsAreOk ()", "Wrong name : "+name
					+". EconomicIntervention is not appliable.");
			return false;
		}
		
		return true;
	}

	/**
	 * From Intervener.
	 * Control input parameters.
	 */
	public boolean isReadyToApply () {
		// Cancel on dialog in interactive mode -> constuctionCompleted = false
		if (constructionCompleted && assertionsAreOk ()) {return true;}	
		return false;
	}

	/**
	 * From Intervener.
	 * Makes the action : thinning.
	 */
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {	
			throw new Exception ("EconomicIntervention.apply () - Wrong input parameters, see Log");
		}
		
		stand.setInterventionResult (true);
		
		// 1. No action, return a perfect clone of the reference stand
		
		return stand;
	}

	
	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString () {
		return "class="+getClass().getName ()
				+" name=\""+NAME+"\""
				+" className=\"EconomicIntervention\"";	// className="EconomicIntervention" is mandatory (see EconomicBalance)
	}

	

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}
	
}

