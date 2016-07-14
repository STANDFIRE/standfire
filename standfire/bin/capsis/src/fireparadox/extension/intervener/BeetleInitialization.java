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

package fireparadox.extension.intervener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.plant.FmPlant;

/**
 * 
 * @author R. Parsons - nov 2010
 */
public class BeetleInitialization implements Intervener, GroupableIntervener,
		Automatable {
	
	
	public Collection concernedTrees; // Intervener will be ran on this trees
										// only (maybe all, maybe a group)
	public static final String NAME = "BeettleInitialization";
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "R. Parsons";
	public static final String DESCRIPTION = "BeetleInitialization.description";
	static public String SUBTYPE = "perturbation";
	private boolean constructionCompleted = false; // if cancel in interactive
													// mode, false

	// private GScene stand; // Reference stand: will be altered by apply ()
	private FmStand stand; // Reference stand: will be altered by apply ()

	private Random random;
	
	public double initialProportionAttacked = 0.04; // between 0 and 1
	public double maxSpreadDistance = 6.1; // m
	public double param_a = 0.959;
	public double param_b = 2271;
	public double param_c = 0.509;
	
	static {
		Translator
				.addBundle("fireparadox.extension.intervener.BeetleInitialization");
	}


	public BeetleInitialization() {
	}
	
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {

		
		stand = (FmStand) scene; // this is referentStand.getInterventionBase
		// ();
		random = new Random();

		if (c == null) { 
			concernedTrees = ((TreeList) stand).getTrees();
		} else {
			concernedTrees = c;
		}
		constructionCompleted = true;
	}
	
	
	@Override
	public boolean initGUI() throws Exception {
		
		// Interactive dialog
		BeetleInitializationDialog dlg = new BeetleInitializationDialog(this);
		constructionCompleted = false;
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and check were ok
			try {
				initialProportionAttacked = dlg
						.getInitialProportionAttacked();
				maxSpreadDistance = dlg.getMaxSpreadDistance();
				param_a = dlg.getParam_a();
				param_b = dlg.getParam_b();
				param_c = dlg.getParam_c();
				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception(
						"BeetleInitialization (): Could not get parameters in BeetleInitializationDialog due to "
								+ e);
			}
		}
		dlg.dispose ();
		return constructionCompleted;
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FmModel)) {
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "BeetleInitialization.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}
	
	/**
	 * GroupableIntervener interface. This intervener acts on layerSets,
	 * 
	 */
	public String getGrouperType() {
		return Group.TREE;
	} // FP

	// These assertions are checked at the beginning of apply ().
	//
	private boolean assertionsAreOk() {
		if (stand == null) {
			Log.println(Log.ERROR, "BeetleInitialization.assertionsAreOk ()",
					"stand is null. BeetleInitialization is not appliable.");
			return false;
		}

		return true;
	}

	/**
	 * From Intervener. Control input parameters.
	 */
	@Override
	public boolean isReadyToApply() {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk()) {
			return true;
		}
		return false;
	}

	/**
	 * From Intervener. Makes the action
	 */
	@Override
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {
			throw new Exception(
					"BeetleInitialization.apply () - Wrong input parameters, see Log");
		}

		// There will be a "*" on the step carrying this stand
		// this stand is a copy of the initial stand
		stand.setInterventionResult (true);
		stand.setMaxSpreadDistance(maxSpreadDistance);
		stand.setBeetleAttack_a(param_a);
		stand.setBeetleAttack_b(param_b);
		stand.setBeetleAttack_c(param_c);
		
		stand.setBeetleAttacked(true);
		
		Iterator i = concernedTrees.iterator();
		while (i.hasNext()) {
			FmPlant plant = (FmPlant) i.next ();
			double r = random.nextDouble();
			if (r < this.initialProportionAttacked) {
				plant.setBeetleStatus((byte) 1);
			}
			// String speciesName = plant.getSpeciesName ();
			// double dbh = plant.getDbh();
			// double p_attack = param_a / (1d + param_b * Math.exp( - param_c *
			// dbh));
			
		
		}

		return stand;
	}
	



	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	@Override
	public String toString () {
		return "class="+getClass().getName ()
		+ " name=" + NAME
				+ " constructionCompleted=" + constructionCompleted;
	}

	

}

