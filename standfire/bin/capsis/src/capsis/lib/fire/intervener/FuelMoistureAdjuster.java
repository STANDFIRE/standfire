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

package capsis.lib.fire.intervener;

import java.util.Collection;
import java.util.Set;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.layerSet.FmLayerSet;
import fireparadox.model.plant.FmPlant;

/**
 * Create an FuelMoistureAdjuster : a tool to set the fuel moisture in trees and
 * layersets.
 * 
 * @author F. Pimont - sept 2011
 */
public class FuelMoistureAdjuster implements Intervener, GroupableIntervener /*
																			 * ,
																			 * Automatable
																			 */{ // removing
																					// Automatable:
																					// trying
																					// to
																					// get
																					// rid
																					// of
																					// a
																					// "SaveAs Project"
																					// bug
																					// fc-5.10.2011

	public static final String NAME = "FuelMoistureAdjuster";
	public static final String VERSION = "1.1";
	public static final String AUTHOR = "F. Pimont";
	public static final String DESCRIPTION = "FuelMoistureAdjuster.description";

	static public String SUBTYPE = "meteorologicalData";
	private boolean constructionCompleted = false; // if cancel in interactive
													// mode, false

	private FmStand stand; // Reference stand: will be altered by apply ()

	private Collection<FiPlant> plants;
	private double treeLiveMoisture;
	private double treeDeadMoisture;
	private double treeLiveTwigMoisture;
	private Collection<FmLayerSet> layerSets; // all layerSets

	/*
	 * private Map<FiLayerSet, Double> shrubLiveMoisture = new
	 * HashMap<FiLayerSet, Double>(); private Map<FiLayerSet, Double>
	 * shrubDeadMoisture = new HashMap<FiLayerSet, Double>(); private
	 * Map<FiLayerSet, Double> herbLiveMoisture = new HashMap<FiLayerSet,
	 * Double>(); private Map<FiLayerSet, Double> herbDeadMoisture = new
	 * HashMap<FiLayerSet, Double>();
	 */

	static {
		Translator.addBundle("capsis.lib.fire.intervener.FuelMoistureAdjuster");
	}

	public FuelMoistureAdjuster() {
	}

	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {
		stand = (FmStand) scene;
		plants = (Collection<FiPlant>) stand.getTrees();
		// calcul des teneurs en eau actuelles
		double load = 0d;

		for (FiPlant p : plants) {
			treeLiveMoisture = p.getMoisture(FmModel.LEAVE_LIVE);
			treeDeadMoisture = p.getMoisture(FmModel.LEAVE_DEAD);
			treeLiveTwigMoisture = p.getMoisture(FmModel.TWIG1_LIVE);
			;
		}

		layerSets = stand.getFmLayerSets();

		// simplification of layers and definition of the moisture maps with old
		// values
		constructionCompleted = true;
		for (FmLayerSet ls : layerSets) {
			// simplify if required
			try {
				ls.setLayers(ls.simplifiedLayersForEvolution());
				ls.update();
				/*
				 * shrubLiveMoisture.put(ls, ls.getLayer(FiModel.SHRUB)
				 * .getLiveMoisture()); shrubDeadMoisture.put(ls,
				 * ls.getLayer(FiModel.SHRUB) .getDeadMoisture());
				 * herbLiveMoisture.put(ls, ls.getLayer(FiModel.HERB)
				 * .getLiveMoisture()); herbDeadMoisture.put(ls,
				 * ls.getLayer(FiModel.HERB) .getDeadMoisture());
				 */

			} catch (Exception e) {
				constructionCompleted = false;
			}
		}
	}

	@Override
	public boolean initGUI() throws Exception {
		FuelMoistureAdjusterDialog dlg = new FuelMoistureAdjusterDialog(treeLiveMoisture, treeDeadMoisture,
				treeLiveTwigMoisture, layerSets); // , shrubLiveMoisture,
													// shrubDeadMoisture,;
		// herbLiveMoisture, herbDeadMoisture);

		constructionCompleted = false;
		if (dlg.isValidDialog()) {
			// valid -> ok was hit and check were ok
			try {
				treeLiveMoisture = dlg.getTreeLiveMoisture();
				treeDeadMoisture = dlg.getTreeDeadMoisture();
				treeLiveTwigMoisture = dlg.getTreeLiveTwigMoisture();
				/*
				 * shrubLiveMoisture = dlg.getShrubLiveMoisture();
				 * shrubDeadMoisture = dlg.getShrubDeadMoisture();
				 * herbLiveMoisture = dlg.getHerbLiveMoisture();
				 * herbDeadMoisture = dlg.getHerbDeadMoisture();
				 */
				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception(
						"FuelMoistureAdjuster (): Could not get parameters in FuelMoistureAdjusterDialog due to " + e);
			}
		}
		dlg.dispose();
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
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof FmModel)) {
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FuelMoistureAdjuster.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on layerSets,
	 * 
	 */
	public String getGrouperType() {
		return Group.UNKNOWN;
	} // .TREE;} //FP

	// These assertions are checked at the beginning of apply ().
	//
	private boolean assertionsAreOk() {
		/*
		 * if (mode != FIRE && mode != PRESCRIBED_BURNING && mode !=
		 * MECHANICAL_CLEARING) { Log.println (Log.ERROR,
		 * "LayerSetThinner.assertionsAreOk ()", "Wrong mode"+mode
		 * +", should be "
		 * +FIRE+"(FIRE), or"+PRESCRIBED_BURNING+" (PRESCRIBED BURNING) or "
		 * +MECHANICAL_CLEARING
		 * +"(MECHANICAL CLEARING). LayerSetThinner is not appliable."); return
		 * false; }
		 */
		if (stand == null) {
			Log.println(Log.ERROR, "FuelMoistureAdjuster.assertionsAreOk ()",
					"stand is null. FuelMoistureAdjuster is not appliable.");
			return false;
		}

		return true;
	}

	/**
	 * From Intervener. Control input parameters.
	 */
	public boolean isReadyToApply() {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk()) {
			return true;
		}
		return false;
	}

	/**
	 * From Intervener. Makes the action : clearing.
	 */
	public Object apply() throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply()) {
			throw new Exception("FuelMoistureAdjuster.apply () - Wrong input parameters, see Log");
		}

		Set<String> particleNames = stand.getModel().particleNames;

		// 1. Iterate set new moisture on species
		for (FiPlant plant : plants) {
			for (String ptName : particleNames) {
				FiParticle fp = ((FmPlant) plant).getParticle(ptName);
				if (ptName.endsWith(FiParticle.DEAD)) {
					fp.moisture = treeDeadMoisture;
				} else if (ptName.startsWith(FiParticle.LEAVE)) {
					fp.moisture = treeLiveMoisture;
				} else {
					fp.moisture = treeLiveTwigMoisture;
				}
			}
		}

		// 2. Iterate set new moisture on layerset : NOT REQUIRED BECAUSE
		// ALREADY DONE IN THE GUI WITH THE FUELMOISTUREFROMTABLEMODEL
		/*
		 * for (FiLayerSet ls : layerSets) { for (FiLayer ll : ls.getLayers()) {
		 * if (ll.getSpeciesName().equals(FiModel.SHRUB)) {
		 * ll.setLiveMoisture(shrubLiveMoisture.get(ls));
		 * ll.setDeadMoisture(shrubDeadMoisture.get(ls));
		 * 
		 * } else if (ll.getSpeciesName().equals(FiModel.HERB)) { // HERBS
		 * ll.setLiveMoisture(herbLiveMoisture.get(ls));
		 * ll.setDeadMoisture(herbDeadMoisture.get(ls)); } else { throw new
		 * Exception(
		 * "FuelMoistureAdjuster.apply () - LayerSet still contains other things than SHRUB and HERB"
		 * ); } } ls.update(); }
		 */

		stand.setInterventionResult(true);

		return stand;
	}

	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString() {
		return "class=" + getClass().getName() + " name=" + NAME + " constructionCompleted=" + constructionCompleted
				+ " stand=" + stand;
	}

}
