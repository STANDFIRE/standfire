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

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import fireparadox.model.FmModel;

/**
 * Create an layerSetThinner : a tool that clear or burn layerSet.
 * 
 * @author F. Pimont - jan 2010
 */
public class LayerSetThinner extends FiIntervenerWithRetention implements Intervener, GroupableIntervener {

	public static final String NAME = "LayerSetThinner";
	public static final String VERSION = "1.1";
	public static final String AUTHOR = "F. Pimont";
	public static final String DESCRIPTION = "LayerSetThinner.description";

	static public String SUBTYPE = "understoreyTreatment";
	private boolean constructionCompleted = false; // if cancel in interactive
													// mode, false

	private int mode; // fire, prescribed burning, clearing
	final public static int FIRE = 0;
	final public static int PRESCRIBED_BURNING = 1;
	final public static int MECHANICAL_CLEARING = 2;

	private double remainingFractionAfterBurning = 0d;
	
	private Collection<FiLayerSet> layerSets; // all layerSets
	
	static {
		Translator.addBundle("capsis.lib.fire.intervener.LayerSetThinner");
	}

	public LayerSetThinner() {
	}

	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {
		stand = scene;
		model = m;
		layerSets = ((FiStand) stand).getLayerSets();

		constructionCompleted = true;
	}

	@Override
	public boolean initGUI() throws Exception {
		LayerSetThinnerDialog dlg = new LayerSetThinnerDialog(layerSets);

		constructionCompleted = false;
		if (dlg.isValidDialog()) {
			// valid -> ok was hit and check were ok
			try {
				layerSet = dlg.getLayerSetToBeThinned();
				mode = dlg.getClearingType();
				if (mode == this.PRESCRIBED_BURNING) {
					this.remainingFractionAfterBurning = dlg.getRemainingFractionAfterBurning();
				}
				this.setResidualFuelProperties(dlg);
				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception("LayerSetThinner (): Could not get parameters in LayerSetThinnerDialog due to " + e);
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
			Log.println(Log.ERROR, "LayerSetThinner.matchWith ()", "Error in matchWith () (returned false)", e);
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
		if (mode != FIRE && mode != PRESCRIBED_BURNING && mode != MECHANICAL_CLEARING) {
			Log.println(Log.ERROR, "LayerSetThinner.assertionsAreOk ()", "Wrong mode" + mode + ", should be " + FIRE
					+ "(FIRE), or" + PRESCRIBED_BURNING + " (PRESCRIBED BURNING) or " + MECHANICAL_CLEARING
					+ "(MECHANICAL CLEARING). LayerSetThinner is not appliable.");
			return false;
		}
		if (mode == PRESCRIBED_BURNING && (remainingFractionAfterBurning <0d || remainingFractionAfterBurning >1d)) {
			
		}
		if (stand == null) {
			Log.println(Log.ERROR, "LayerSetThinner.assertionsAreOk ()",
					"stand is null. LayerSetThinner is not appliable.");
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
			throw new Exception("LayerSetThinner.apply () - Wrong input parameters, see Log");
		}

		stand.setInterventionResult(true);
		if (activityFuelRetention) {
			buildLayerSet();
		}
		// 1. Iterate and clear
//		FiLayerSet ls = layerSet;
//		if (layerSet instanceof FmLayerSet) {
//			double treatmentEffect = ls.getTreatmentEffect();
//			// no previous treatment effect after 10 years
//			if (ls.getAge() >= 10) {
//				treatmentEffect = 1d;
//			}
//			if (treatmentEffect >= 0.9d) {
//				// almost no previous treatment effect
//				treatmentEffect *= 0.9;
//			} else {
//				treatmentEffect *= 0.7;
//			}
//
//			ls.setTreatmentEffect(treatmentEffect);
//
//			ls.setLastClearingType(mode);
//			ls.setAge(0);

			for (FiLayer ll : layerSet.getLayers()) {
				if (mode == PRESCRIBED_BURNING) {
					for (FiParticle particle : ll.getParticles()) {
						ll.getBulkDensityMap().put(particle, this.remainingFractionAfterBurning * ll.getBulkDensityMap().get(particle));
					}
				} else {
				  if (this.activityFuelRetention) {
				    for (FiParticle particle : ll.getParticles()) {
				  	  double load = ll.getCoverFraction() * ll.getBulkDensity(particle) * (ll.getHeight() - ll.getBaseHeight()) ;
					  layer.addFiLayerParticleFromLoad(load, particle);
				    }
				  }
				  ll.setHeight(0.05d);
				  ll.setBaseHeight(0d);
				  ll.setHeight(0.01d);
				}
				// TODO FP layerSetThinner: evolution of characteristicSize,
				// percentage...
				// ll.setCharacteristicSize();
				// ll.setPercentage;
				// ll.setSpatialGroup;
				// ll.setLiveMoisture;
				// ll.setDeadMoisture;

			}
			layerSet.update();
			if (this.activityFuelRetention) {
				layerSet.addLayer(layer);
				((FiStand) stand).addLayerSet(layerSet);
			}
			// }
		return stand;
	}

	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	public String toString() {
		return "class=" + getClass().getName() + " name=" + NAME + " constructionCompleted=" + constructionCompleted
				+ " mode=" + mode + " stand=" + stand;
	}

}
