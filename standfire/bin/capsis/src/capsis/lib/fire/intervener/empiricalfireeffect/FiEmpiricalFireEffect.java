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

package capsis.lib.fire.intervener.empiricalfireeffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jeeb.lib.maps.geom.Polygon2;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiFireParameters;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSeverity;
import capsis.lib.fire.intervener.fireeffect.FiFireEffect;
import capsis.util.GroupableIntervener;
import fireparadox.model.FmStand;

/**
 * FiEmpiricalFireEffect : an intervener to simulate empirical fire effects.
 * 
 * @author F. de Coligny, F. Pimont - september 2009
 */
public class FiEmpiricalFireEffect extends FiFireEffect implements GroupableIntervener {

	public static final String NAME = "FiEmpiricalFireEffect";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "F. Pimont";
	public static final String DESCRIPTION = "FiEmpiricalFireEffect.description";
	static public String SUBTYPE = "fireeffect";
	
	private boolean intensityFromRos; // Byram computation of intensity using
										// local biomass
	private double rateOfSpread; // M/s
	private double fireIntensity; //kW/m
	private double residenceTime; //s
	private double ambiantTemperature; // �C
	private double windVelocity; //m/s
	
	static {
		Translator.addBundle ("capsis.lib.fire.intervener.empiricalfireeffect.FiEmpiricalFireEffect");
	}


	public FiEmpiricalFireEffect () {}
	

	@Override
	public boolean initGUI() throws Exception {
		constructionCompleted = false;
		/*constructionCompleted = false;

		// This is always in starter for every intervener
		model = (FiModel) s.getModel ();
		stand = (FiStand) s.getStand ();	// this is referentStand.getInterventionBase ();

		if (s.getCollection () == null) {		// fc - 22.9.2004
			concernedTrees = ((TreeList) stand).getTrees ();
		} else {
			concernedTrees = s.getCollection ();
		}*/

		// Interactive dialog
		FiEmpiricalFireEffectDialog dlg = new FiEmpiricalFireEffectDialog ();
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and check were ok
			try {
				// Fire parameters
				intensityFromRos = dlg.fromRosIsSelected();
				rateOfSpread = dlg.getRateOfSpread();
				fireIntensity = dlg.getFireIntensity ();
				residenceTime=dlg.getResidenceTime ();
				ambiantTemperature=dlg.getAmbiantTemperature();
				windVelocity=dlg.getWindVelocity();

				// Models
				crownDamageModel = dlg.getCrownDamageModel ();
				cambiumDamageModel = dlg.getCambiumDamageModel();
				mortalityModel = dlg.getMortalityModel();

				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception ("FiEmpiricalFireEffect (): Could not get parameters in FiEmpiricalFireEffectDialog due to "+e);
			}
		}
		dlg.dispose ();
		return constructionCompleted;
		
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}
	
	static public boolean matchWith (Object referent) {
		return FiFireEffect.matchWith(referent);
	}

	
	/**
	 * From Intervener. Makes the action : compute fire effects. Call private methods :
	 * - computeCrownDamage
	 * - computeCambiumDamage
	 * - computeMortality
	 */
	@Override
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {
			throw new Exception ("FiEmpiricalFireEffect.apply () - Wrong input parameters, see Log");
		}

		// There will be a "*" on the step carrying this stand
		// this stand is a copy of the initial stand
		stand.setInterventionResult (true);

		// process each plant
		System.out.println("Fire Effects");

		// fc-2.2.2015 particleNames
		//FiModel m = (FiModel) stand.getStep().getProject().getModel();
		
		for (Iterator i = concernedTrees.iterator (); i.hasNext ();) {
			FiPlant plant = (FiPlant) i.next ();
			String speciesName = plant.getSpeciesName ();
			System.out.println(speciesName + " ID " + plant.getId()
					+ " height " + " CBH " + plant.getCrownBaseHeight()
					+ plant.getHeight() + " dbh :" + plant.getDbh());
			double scorchTemperature = computeScorchTemperature(speciesName);
			double killTemperature = computeKillTemperature(speciesName);
			if (intensityFromRos) {
				
				
				
				
				List<Double> xs = new ArrayList<Double>();
				List<Double> ys = new ArrayList<Double>();
				double footprint = 10d; // in m
				xs.add(plant.getX() - footprint);
				ys.add(plant.getY() - footprint);
				xs.add(plant.getX() + footprint);
				ys.add(plant.getY() - footprint);
				xs.add(plant.getX() + footprint);
				ys.add(plant.getY() + footprint);
				xs.add(plant.getX() - footprint);
				ys.add(plant.getY() + footprint);

				Polygon2 poly = new Polygon2(xs, ys);
				Collection trees = new ArrayList();
				
				double[] prop = FmStand.calcMultiCov(trees,
						stand.getLayerSets(), stand.getPlot(),
						plant.getCrownBaseHeight() - 1e-5, poly, model.particleNames); // fc-2.2.2015 particleNames
				
				// prop[3] is load before threshold, do not include trees...
				fireIntensity = 18000 * rateOfSpread * prop[3];
			}
			FiFireParameters fire = new FiFireParameters(fireIntensity,
					residenceTime, 0d, ambiantTemperature, windVelocity);
			plant.setFire(fire);
			
			FiSeverity severity = new FiSeverity ();
			computeCrownDamage(severity, plant, scorchTemperature,
					killTemperature);
			computeCambiumDamage(severity, plant);
			computeMortality(severity, plant);
			
			
			// in case of multiple fire event
			FiSeverity previousSeverity = plant.getSeverity();
			if (!previousSeverity.isAlreadyBurn()) {
				plant.setSeverity(severity);
			} else { // multiple fire event
				computeWorseSeverity(severity, previousSeverity);
				plant.setSeverity(severity);
			}
			severity.alreadyBurn(true);
			System.out.println("cambium is killed :"
					+ severity.getCambiumIsKilled());
			System.out.println("CSHeight " + severity.getCrownScorchHeight()
					+ " CVolS " + severity.getCrownVolumeScorched()
					+ " CLengthS " + severity.getCrownLengthScorched()
					+ " BLengthC " + severity.getMeanBoleLengthCharred());
			System.out.println("CKillHeight " + severity.getCrownKillHeight());
			
			System.out.println("tree is killed "
					+ severity.getCambiumIsKilled() + " tree mortability p "
					+ severity.getMortalityProbability());
			System.out.println("");
			/*
			boolean dead = false;		// option: remove the dead trees
			if (dead) {
				stand.removeTree (plant);
				i.remove ();
			}*/


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

