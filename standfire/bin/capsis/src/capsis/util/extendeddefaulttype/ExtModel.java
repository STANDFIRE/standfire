/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package capsis.util.extendeddefaulttype;

import java.util.Locale;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;

import repicea.app.AbstractGenericTask;
import repicea.gui.genericwindows.REpiceaProgressBarDialog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.extension.AbstractDataExtractor;
import capsis.extension.DESettings;
import capsis.extension.OverridableDataExtractorParameter;
import capsis.gui.MainFrame;
import capsis.kernel.EvolutionParameters;
import capsis.kernel.GModel;
import capsis.kernel.Project;
import capsis.kernel.Step;


public abstract class ExtModel<P extends ExtInitialParameters> extends GModel implements OverridableDataExtractorParameter {

	public static enum MessageID implements TextableEnum {
		Evolution("Evolution", "Evolution"),
		StartingEvolution("Starting evolution", "Amorce de l'\u00E9volution"),
		HarvestingStand("Harvesting scene", "R\u00E9colte"),
		EvolutionOf("Growing scene", "Evolution"),
		EvolutionIsOver("The evolution is done", "L'\u00E9volution est termin\u00E9e");

		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	
	public final class ExtEvolutionTask extends AbstractGenericTask {

		private Step stp;
		private EvolutionParameters e;
		private Step newStp = null;
		
		private ExtEvolutionTask(Step stp, EvolutionParameters e) {
			this.stp = stp;
			this.e = e;
		}
		
		
		@Override
		protected void doThisJob() throws Exception {
//			Step newStp = null;	// to be returned at the end
			ExtEvolutionParametersList<?> oVec = ((ExtEvolutionParametersList) e);
//			firePropertyChange(REpiceaProgressBarDialog.LABEL, "", MessageID.StartingEvolution.toString());
			int numberOfGrowthStepsSoFar = 0;
			for (ExtEvolutionParameters stepEvolution : oVec) {
				if (stepEvolution.isGoingToBeInterventionResult()) {
					displayThisMessage(MessageID.HarvestingStand.toString() + " " + stp.getCaption());
					newStp = processCutting(stp, stepEvolution);
					stp = newStp;
				} else {
					displayThisMessage(MessageID.EvolutionOf.toString() + " " + stp.getCaption());
					newStp = processEvolution(stp, stepEvolution, numberOfGrowthStepsSoFar, this);
					stp = newStp;
				}
				numberOfGrowthStepsSoFar += stepEvolution.getNbSteps();
			}
			displayThisMessage(MessageID.EvolutionIsOver.toString());
		}
		
		public void displayThisMessage(String message) {
			firePropertyChange(REpiceaProgressBarDialog.LABEL, "", message);
		}
		
	}
	
	
	
	public static RandomGenerator RANDOM = new JDKRandomGenerator();

	public static enum InfoType{EXPORT, EVOLUTION_DIALOG}

	protected ExtModel() {
		Locale locale = Locale.getDefault();
		String languageString = locale.getLanguage();
		if (languageString == "fr") {
			REpiceaTranslator.setCurrentLanguage(Language.French);
		} else {
			REpiceaTranslator.setCurrentLanguage(Language.English);
		}
	}

	/**
	 * This method returns the initial parameters of the simulation.
	 * @return a QuebecMRNFInitialParameters-derived instance
	 */
	@Override
	public P getSettings() {
		return (P) super.getSettings();
	}

	/**
	 * This method sets the initial parameters of the simulation.
	 * @param settings a QuebecMRNFInitialParameters derived instance
	 */
	public void setSettings(P settings) {
		this.settings = settings;
	}

	/*
	 * Makes sure that the "per hectare" option is always enabled for ExtendedModel instances
	 */
	@Override
	public void setDefaultProperties(DESettings settings) {
		if (settings.booleanProperties.containsKey(AbstractDataExtractor.HECTARE)
				|| settings.configProperties.contains(AbstractDataExtractor.HECTARE)) {
			settings.perHa = true;
		}
	}
	
	/**
	 * This method is the core of the evolution. 
	 * @param stp the Step instance from which the evolution is made
	 * @param evolutionElement a ExtendedEvolutionParameters instance that encompasses all the parameters for the evolution
	 * @param numberOfGrowthStepsSoFar an Integer that must be add to the ProgressDispatcher for consistent values on the progress bar
	 * @param task the task is sent in this method to allow for message display
	 * @return the last Step instance of the evolution
	 * @throws Exception
	 */
	protected abstract Step processEvolution(Step s, ExtEvolutionParameters evolutionElement, int numberOfGrowthStepsSoFar, ExtEvolutionTask task) throws Exception;	

	@Override
	public Step processEvolution(Step stp, EvolutionParameters e) throws Exception {

		ExtEvolutionTask evolutionTask = new ExtEvolutionTask(stp, e);
		
		if (getSettings().isGuiEnabled()) {
			new REpiceaProgressBarDialog(MainFrame.getInstance(), 
					MessageID.Evolution.toString(),
					MessageID.StartingEvolution.toString(),
					evolutionTask,
					true);
		} else {
			evolutionTask.run();
		}

		if (evolutionTask.isCorrectlyTerminated()) {
			return evolutionTask.newStp;
		} else {
			throw evolutionTask.getFailureReason();
		}
		
//		Step newStp = null;	// to be returned at the end
//		ExtEvolutionParametersList<?> oVec = ((ExtEvolutionParametersList) e);
//		if (getSettings().isGuiEnabled()) {
//			int numberOfGrowthIntervals = 0;
//			for (ExtEvolutionParameters stepEvolution : oVec) {
//				numberOfGrowthIntervals += stepEvolution.getNbSteps();
//			}
//			ProgressDispatcher.setMinMax (0, numberOfGrowthIntervals);
//		}
//		int numberOfGrowthStepsSoFar = 0;
//		for (ExtEvolutionParameters stepEvolution : oVec) {
//			if (stepEvolution.isGoingToBeInterventionResult()) {
//				newStp = processCutting(stp, stepEvolution);
//				stp = newStp;
//			} else {
//				newStp = processEvolution(stp, stepEvolution, numberOfGrowthStepsSoFar);
//				stp = newStp;
//			}
//			numberOfGrowthStepsSoFar += stepEvolution.getNbSteps();
//		}
//		
//		// 18.5.2001 - ProgressDispatcher
//		if (getSettings().isGuiEnabled()) {
//			ProgressDispatcher.stop();
//		}
//
//		return newStp;
	}

	protected abstract Step processCutting (Step stp, ExtEvolutionParameters stepEvolution) throws Exception;

	@Override
	public void setProject(Project p) {
		super.setProject(p);
		p.setName(getSettings().getProjectName());
	}
	
	@Override
	public void clear() {getSettings().clear();}
	
}
