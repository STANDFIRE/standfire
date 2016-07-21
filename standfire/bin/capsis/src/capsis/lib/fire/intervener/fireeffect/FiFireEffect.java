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

package capsis.lib.fire.intervener.fireeffect;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Intervener;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSeverity;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.CambiumDamageModel;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.CrownDamageModel;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.MichaletzAndJohnsonCrown;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.MortalityModel;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.PetersonAndRyanMortality;
import capsis.util.Group;
import capsis.util.GroupableIntervener;
import fireparadox.model.FmModel;

/**
 * FiPhysicalFireEffect : an intervener to simulate physical fire effects from firetec simulations 
 * 
 * @author F. de Coligny, F. Pimont - september 2009
 */
public abstract class FiFireEffect  implements Intervener, GroupableIntervener {

	
	
	
	
	protected boolean constructionCompleted = false;		// if cancel in interactive mode, false
	protected FiStand stand;			// Reference stand: will be altered by apply ()
	protected FiModel model;
	// fc - 22.9.2004
	protected Collection concernedTrees;	// Intervener will be ran on this trees only (maybe all, maybe a group)

	// models used to assess damage
	protected CrownDamageModel crownDamageModel; //crown damage
	protected CambiumDamageModel cambiumDamageModel; //cambium damage
	protected MortalityModel mortalityModel; // mortality


	// height threshold to explicitely compute mortality : lower than
	// thresholdHeight dead and damage automatically set
	protected double heightThreshold = 2.;

	static {
		Translator
		.addBundle("capsis.lib.fire.intervener.fireeffect.FiFireEffect");
	}

	
	
	@Override
	public void init(GModel m, Step s, GScene scene, Collection c) {

		model = (FiModel) m;
		stand = (FiStand) scene;	// this is referentStand.getInterventionBase ();

		if (c == null) {		// fc - 22.9.2004
			concernedTrees = ((TreeList) stand).getTrees ();
		} else {
			concernedTrees = c;
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof FiModel)) {return false;}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiFireEffect.matchWith ()",
					"Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on trees, tree groups
	 * can be processed.
	 */
	public String getGrouperType () {return Group.TREE;}		// fc - 22.9.2004

	//	These assertions are checked at the beginning of apply ().
	//
	private boolean assertionsAreOk () {
		if (stand == null) {
			Log.println(Log.ERROR, "FiFireEffect.assertionsAreOk ()",
			"stand is null. FiFireEffect is not appliable.");
			return false;
		}

		return true;
	}

	/**
	 * From Intervener. Control input parameters.
	 */
	@Override
	public boolean isReadyToApply () {
		// Cancel on dialog in interactive mode -> constructionCompleted = false
		if (constructionCompleted && assertionsAreOk ()) {return true;}
		return false;
	}

	/**
	 * Compute the temperature threshold for kill (bud lethal temperature), according to litterature 
	 */
	protected double computeKillTemperature(String speciesName) {
		/*
		 * Here should be done according to the species, see Eric and a flore
		 * for that // according to peterson & Ryan 1986 if small, in growth,
		 * not protected buds scorchTemperature = 60; // pin d'alep , pin
		 * sylvestre if one condition is not scorchTemperature = 65; // if big
		 * or pin pignon protected or scale scorchTemperature = 70; pin
		 * maritime, pin noir
		 */
		if (speciesName.equals(FmModel.PINUS_HALEPENSIS)) {
			return 60.0;
		}
		if (speciesName.equals(FmModel.PINUS_SYLVESTRIS)) {
			return 60.0;
		}
		if (speciesName.equals(FmModel.PINUS_PINEA)) {
			return 65.0;
		}
		if (speciesName.equals(FmModel.PINUS_PINASTER)) {
			return 70.0;
		}
		if (speciesName.equals(FmModel.PINUS_NIGRA)) {
			return 70.0;
		}
		if (speciesName.equals(FmModel.PINUS_NIGRA_LARICIO)) {
			return 70.0;
		}
		return 60.;
	}

	/**
	 * Compute the temperature threshold for scorch (foliage lethal temperature), according to litterature 
	 */
	protected double computeScorchTemperature(String speciesName) {
		/*
		 * Here should be done according to the species, see Eric and a flore
		 * for that // according to peterson & Ryan 1986 if small, in growth,
		 * not protected buds scorchTemperature = 60; // pin d'alep , pin
		 * sylvestre if one condition is not scorchTemperature = 65; // if big
		 * or pin pignon protected or scale scorchTemperature = 70; pin
		 * maritime, pin noir
		 */
		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
			return 55.0;
		}
		if (speciesName.equals(FmModel.QUERCUS_PUBESCENS)) {
			return 55.0;
		}
		return 60.;
	}

	/**
	 * Crown damage Updates the given FiSeverity object relatively to Crown
	 * damage.
	 */
	protected void computeCrownDamage(FiSeverity severity, FiPlant plant,
			double scorchTemperature, double killTemperature) throws Exception {

				
		double fireIntensity = plant.getFire().getFireIntensity();
		double residenceTime = plant.getFire().getResidenceTime();
		double ambiantTemperature = plant.getFire().getAmbiantTemperature();
		double windVelocity = plant.getFire().getWindVelocity();
		double crownBaseHeight = plant.getCrownBaseHeight();
		double treeHeight = plant.getHeight();

		double scorchHeight = treeHeight;
		double killHeight = treeHeight;


		// TODO Michaletz and Johnson model is not operationnal
		if (crownDamageModel instanceof MichaletzAndJohnsonCrown) {
			double MVR = 1.0;
			double SVR = 1.0;
			double moistureContent = 1.0;
			((MichaletzAndJohnsonCrown) crownDamageModel).set(MVR, SVR,
					moistureContent);
		}
		scorchHeight = crownDamageModel.getDamageHeight(scorchTemperature,
				fireIntensity, residenceTime, ambiantTemperature,
				windVelocity,FmModel.CROWN_SCORCHED);


		// KILL HEIGHT
		// TODO Michaletz and Johnson model is not operationnal
		if (crownDamageModel instanceof MichaletzAndJohnsonCrown) {
			double MVR = 1.0;
			double SVR = 1.0;
			double moistureContent = 1.0;
			((MichaletzAndJohnsonCrown) crownDamageModel).set(MVR, SVR,
					moistureContent);
		}
		killHeight = crownDamageModel.getDamageHeight(killTemperature,
				fireIntensity, residenceTime, ambiantTemperature,
				windVelocity, FmModel.BUD_KILLED);


		// SET SEVERITY VARIABLES
		// Crown damage
		severity.setCrownScorchHeight(Math.max(Math.min(scorchHeight, treeHeight),crownBaseHeight));
		severity.setCrownKillHeight(Math.max(Math.min(killHeight, treeHeight),crownBaseHeight));
		// BoleLengthCharred
		// TODO : this should be improved
		severity.setMeanBoleLengthCharred(100. * Math.min(0.5 * scorchHeight
				/ treeHeight, 1.0));
		severity.setMinBoleLengthCharred(100. * Math.min(0.5 * scorchHeight
				/ treeHeight, 1.0));
		severity.setMaxBoleLengthCharred(100. * Math.min(0.5 * scorchHeight
				/ treeHeight, 1.0));

		// VOLUME AND LENGTH (SCORCHED AND KILLED)
		double cvs = severity.computeCrownVolumeUnderH2(severity
				.getCrownScorchHeight(), plant);

		severity.setCrownVolumeScorched(cvs);
		severity.computeCrownLengthScorched(treeHeight, crownBaseHeight);
		double cvk = severity.computeCrownVolumeUnderH2(severity
				.getCrownKillHeight(), plant);

		severity.setCrownVolumeKilled(cvk);
		severity.computeCrownLengthKilled(treeHeight, crownBaseHeight);
	}


	/**
	 * Cambium damage Updates the given FiSeverity object relatively to Cambium
	 * damage.
	 */
	protected void computeCambiumDamage(FiSeverity severity, FiPlant plant)
	throws Exception {
		 double fireIntensity = plant.getFire().getFireIntensity();
		double residenceTime = plant.getFire().getResidenceTime();
		 
		double treeHeight=plant.getHeight();
		boolean cambiumIsKilled=true;
		if (treeHeight >= heightThreshold) { // big trees
			double dbh = plant.getDbh();
			String speciesName = plant.getSpeciesName();
			cambiumIsKilled = cambiumDamageModel.isCambiumKilled(speciesName,
					dbh, fireIntensity, residenceTime);
		}
		severity.setCambiumIsKilled(cambiumIsKilled);
	}
	/**
	 * Mortality Updates the given FiSeverity object relatively to mortality.
	 */
	protected void computeMortality(FiSeverity severity, FiPlant plant)
	throws Exception {
		double residenceTime = plant.getFire().getResidenceTime();
		double treeHeight = plant.getHeight();
		double mortalityProbability = 1.0;
		if (treeHeight >= heightThreshold) { // big trees
			if (mortalityModel instanceof PetersonAndRyanMortality) {
				((PetersonAndRyanMortality) mortalityModel).set(residenceTime);
			}
			double dbh = plant.getDbh();
			String speciesName = plant.getSpeciesName();
			mortalityProbability = mortalityModel.getMortalityProbability(
					severity, speciesName, dbh);
			if (severity.getCambiumIsKilled()) {
				mortalityProbability = 1.0;
			} else if (severity.getCrownVolumeScorched() <= 20.) {
				// case of trees with cvs<20 and cambiumIsAlive)
				mortalityProbability *= (1.0 + severity
						.getCrownVolumeScorched()) / 21.0;
			}	
		}
		severity.setMortalityProbability(mortalityProbability);
		double rand =plant.model.rnd.nextDouble();//Math.random();// stochastic
		// double rand= 0.5; //determinist
		if (mortalityProbability >= rand) {
			severity.setIsKilled(true);
		}	
	}
	/**
	 * compute the worse severity of successive fires
	 */
	protected void computeWorseSeverity(FiSeverity s, FiSeverity ps) {
		s.setIsKilled(s.getIsKilled() || ps.getIsKilled());
		s.setMortalityProbability(Math.max(s.getMortalityProbability(), ps
				.getMortalityProbability()));
		s.setCambiumIsKilled(s.getCambiumIsKilled() || ps.getCambiumIsKilled());
		s.setMaxBarkCharNote(Math.max(s.getMaxBarkCharNote(), ps
				.getMaxBarkCharNote()));
		s.setMeanBarkCharNote(Math.max(s.getMeanBarkCharNote(), ps
				.getMeanBarkCharNote()));
		s.setCrownVolumeScorched(Math.max(s.getCrownVolumeScorched(), ps
				.getCrownVolumeScorched()));
		s.setCrownVolumeKilled(Math.max(s.getCrownVolumeKilled(), ps
				.getCrownVolumeKilled()));
		s.setCrownScorchHeight(Math.max(s.getCrownScorchHeight(), ps
				.getCrownScorchHeight()));
		s.setCrownKillHeight(Math.max(s.getCrownKillHeight(), ps
				.getCrownKillHeight()));
		s.setCrownLengthScorched(Math.max(s.getCrownLengthScorched(), ps
				.getCrownLengthScorched()));
		s.setCrownLengthKilled(Math.max(s.getCrownLengthKilled(), ps
				.getCrownLengthKilled()));
		s.setMeanBoleLengthCharred(Math.max(s.getMeanBoleLengthCharred(), ps
				.getMeanBoleLengthCharred()));
		s.setMinBoleLengthCharred(Math.max(s.getMinBoleLengthCharred(), ps
				.getMinBoleLengthCharred()));
		s.setMaxBoleLengthCharred(Math.max(s.getMaxBoleLengthCharred(), ps
				.getMaxBoleLengthCharred()));
	}

	private void addPointNoDoublon(ArrayList<Point> ar, Point np) {
		boolean shouldBeAdded = true;
		for (Point p : ar) {
			if (p.x == np.x && p.y == np.y)
				shouldBeAdded = false;
		}
		if (shouldBeAdded)
			ar.add(np);
	}

	

}
