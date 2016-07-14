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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.Group;
import capsis.util.GroupableIntervener;

/**
 * FiTreePrunning : an intervener to simulate pruning.
 * 
 * @author F. Pimont - fev 2010
 */
public class FiTreePruning extends FiIntervenerWithRetention implements Intervener, GroupableIntervener, Automatable {

	public static final String NAME = "FiTreePruning";
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "F. Pimont";
	public static final String DESCRIPTION = "FiTreePruning.description";
	static public String SUBTYPE = "treepruning";
	private boolean constructionCompleted = false; // if cancel in interactive
													// mode, false	
	private double pruningHeight; // m
	
	static {
		Translator.addBundle("capsis.lib.fire.intervener.FiTreePruning");
	}

	public FiTreePruning() {
	}
	
	/**
	 * for script mode
	 */
	public FiTreePruning(double pruningHeight, boolean activityFuelRetention, double residualFuelHeight, double residualFuelCoverFraction, double residualFuelCharacteristicSize, double residualFuelMoisture) {
		super(activityFuelRetention, residualFuelHeight, residualFuelCoverFraction, residualFuelCharacteristicSize, residualFuelMoisture);
		this.pruningHeight = pruningHeight;
		constructionCompleted = true;
	}
	
	
	
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {

		stand = (FiStand) scene; // this is referentStand.getInterventionBase
		model = (FiModel) m; // fc-2.2.2015

		if (c == null) { // fc - 22.9.2004
			concernedTrees = ((TreeList) stand).getTrees();
		} else {
			concernedTrees = c;
		}
		constructionCompleted = true;
	}

	@Override
	public boolean initGUI() throws Exception {
		/*
		 * constructionCompleted = false;
		 * 
		 * // This is always in starter for every intervener model = (FiModel)
		 * s.getModel (); stand = (FiStand) s.getStand (); // this is
		 * referentStand.getInterventionBase ();
		 * 
		 * if (s.getCollection () == null) { // fc - 22.9.2004 concernedTrees =
		 * ((TreeList) stand).getTrees (); } else { concernedTrees =
		 * s.getCollection (); }
		 */

		// Interactive dialog
		FiTreePruningDialog dlg = new FiTreePruningDialog();
		constructionCompleted = false;
		if (dlg.isValidDialog()) {
			// valid -> ok was hit and check were ok
			try {
				pruningHeight = dlg.getPruningHeight();
				this.setResidualFuelProperties(dlg);
				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception("FiTreePruning (): Could not get parameters in FiTreePruningDialog due to " + e);
			}
		}
		dlg.dispose();
		return constructionCompleted;
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof FiModel)) {
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiTreePruning.matchWith ()", "Error in matchWith () (returned false)", e);
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
			Log.println(Log.ERROR, "FiTreePruning.assertionsAreOk ()",
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
	 * From Intervener. Makes the action
	 */
	@Override
	public Object apply() throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply()) {
			throw new Exception("FiTreePruning.apply () - Wrong input parameters, see Log");
		}

		// There will be a "*" on the step carrying this stand
		// this stand is a copy of the initial stand
		stand.setInterventionResult(true);

		if (activityFuelRetention) {
			buildLayerSet();
		}
		
		Iterator i = concernedTrees.iterator();
		while (i.hasNext()) {
			FiPlant plant = (FiPlant) i.next();
			double[][] crownGeom = plant.getCrownGeometry();
			Map<Double, Double> crownGeomDim = new HashMap<Double, Double>();
			// this map contains couples (height, diameter) in m

			double cbh = plant.getCrownBaseHeight();
			double crownDiameter = plant.getCrownDiameter();
			double h = plant.getHeight();
			//System.out.println("cbh=" + cbh + " diam=" + crownDiameter + " h=" + h);
			if (cbh < pruningHeight) {
				// remove trees smaller than pruning height
				if (h <= pruningHeight) {
					if (this.activityFuelRetention) {
						for (FiParticle fp : plant.getParticles()) {
							if (plant.biomass.containsKey(fp.name)) {
							  double load = plant.getBiomass(fp.name)/layerSet.getPolygonArea();
							  layer.addFiLayerParticleFromLoad(load, fp);
							}
						}
					}
					i.remove();
					((FiStand) stand).removeTree(plant);
				} else {
					
					// tree is pruned
					// System.out.println("CPLENGHT" + crownProfile.length);
					double newCrownDiameter = 0d;
					double H1 = cbh + 0.01 * crownGeom[0][0] * (h - cbh);
					double D1 = 0.01 * crownGeom[0][1] * crownDiameter;

					if (H1 > pruningHeight) {
						crownGeomDim.put(pruningHeight, D1);
						crownGeomDim.put(H1, D1);
						newCrownDiameter = Math.max(newCrownDiameter, D1);
					}
					double H2 = H1;
					double D2 = D1;
					for (int it = 1; it < crownGeom.length; it++) {

						H2 = cbh + 0.01 * crownGeom[it][0] * (h - cbh);
						D2 = 0.01 * crownGeom[it][1] * crownDiameter;
						if (H2 > pruningHeight) {
							crownGeomDim.put(H2, D2);
							newCrownDiameter = Math.max(newCrownDiameter, D2);
							if (H1 <= pruningHeight) {
								double dAtPruneHeight = (D2 * (pruningHeight - H1) + D1 * (H2 - pruningHeight))
										/ (H2 - H1);
								crownGeomDim.put(pruningHeight, dAtPruneHeight);
								newCrownDiameter = Math.max(newCrownDiameter, dAtPruneHeight);
							}
						}
						H1 = H2;
						D1 = D2;
					}
					if (H2 < h) {
						crownGeomDim.put(h, 0d);
						// newCrownDiameter = Math.max(newCrownDiameter, D2);
					}
					plant.setCrownRadius(newCrownDiameter * 0.5);
					plant.setCrownBaseHeight(pruningHeight);

					// new crown geometry after pruning
					double[][] newCrownGeometry = new double[crownGeomDim.size()][2];

					Set<Double> sortedKeySet = new TreeSet<Double>();
					for (double key : crownGeomDim.keySet()) {
						sortedKeySet.add(key);
					}

					int ind = 0;
					for (double key : sortedKeySet) {
						newCrownGeometry[ind][0] = 100d * (key - pruningHeight) / (h - pruningHeight);
						newCrownGeometry[ind][1] = crownGeomDim.get(key) / (newCrownDiameter + 0.000001) * 100d;

						// System.out.println("H=" + newCrownProfile[ind][0]
						// + " D=" + newCrownProfile[ind][1]);
						ind++;
					}

					plant.setCrownGeometry(newCrownGeometry);
					plant.setCrownDiameterHeightFromCrownGeometry();
					plant.updateTotalThinMass();
					if (this.activityFuelRetention) {
						for (FiParticle fp : plant.getParticles()) {
							if (plant.biomass.containsKey(fp.name)) {
							Set<String> particleName = new HashSet<String>();
							particleName.add(fp.name);
							// NB the following formulae for removedMass works since getBiomass is the unprunned biomass
							double removedMass=plant.getBiomass(fp.name) - plant.computeThinMass(particleName);
							//System.out.println(" removed mass:"+particleName+":"+plant.getBiomass(fp.name)+","+plant.computeThinMass(particleName)+",");
							layer.addFiLayerParticleFromLoad(removedMass/layerSet.getPolygonArea(), fp);
							}
						}
					}
				}
			}

		}
		if (this.activityFuelRetention) {
			layerSet.addLayer(layer);
			((FiStand) stand).addLayerSet(layerSet);
		}

		return stand;
	}

	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	@Override
	public String toString() {
		return "class=" + getClass().getName() + " name=" + NAME + " constructionCompleted=" + constructionCompleted;
	}

}
