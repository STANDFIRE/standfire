/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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
package capsis.extension.modeltool.optimizer;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;

import repicea.gui.REpiceaUIObject;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import capsis.extension.modeltool.optimizer.OptimizerEvent.EventType;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;


public class ObjectiveFunction implements MultivariateRealFunction, REpiceaUIObject, Serializable {

	protected static class OptimizerCancelException extends IllegalArgumentException {}
	protected static class InvalidDialogParametersException extends IllegalArgumentException {}

	protected static DecimalFormat Formatter = (DecimalFormat) NumberFormat.getInstance(Locale.CANADA);
	static {
		Formatter.setMaximumFractionDigits(4);
		Formatter.setMinimumFractionDigits(4);
	}

	protected GoalType goal = GoalType.MAXIMIZE;

	protected final IndicatorList indicators;
	protected final CostBenefitCalculator cbc;
	protected ScenarioRecorder scenRecorder;
	
	protected transient final OptimizerTool tool;
	protected transient int iteration;
	protected transient boolean cancelRequested = false;
	protected transient boolean interrupted = false;
	protected transient boolean isGridSearch;
	protected transient MonteCarloEstimate objectiveFunctionValue;

	private transient ObjectiveFunctionPanel guiInterface;
	protected transient MethodProvider methodProvider;
	
	protected ObjectiveFunction(OptimizerTool tool) {
		this.tool = tool;
		scenRecorder = ScenarioRecorder.getInstance();
		indicators = new IndicatorList(tool);
		cbc = new CostBenefitCalculator(tool);
		goal = GoalType.valueOf(tool.getSettingMemory().getProperty("goal", "MAXIMIZE"));
	}
	
	protected static class ObjectiveFunctionEvaluation implements Comparable<ObjectiveFunctionEvaluation> {

		private final int index;
		private final MonteCarloEstimate result;
		private final double[] parms;
		private final double[] realParms;
		
		protected ObjectiveFunctionEvaluation(int index, MonteCarloEstimate result, double realParms[], double parms[]) {
			this.index = index;
			this.result = result;
			this.parms = parms;
			this.realParms = realParms;
		}
		
		protected int getIndex() {return index;}
		
		protected MonteCarloEstimate getResult() {return result;}
	
		protected double[] getRealParms() {return realParms;}
				
		@Override
		public int compareTo(ObjectiveFunctionEvaluation arg0) {
			double thisResult = result.getMean().m_afData[0][0];
			double thatResult = arg0.result.getMean().m_afData[0][0];
			if (thisResult < thatResult) {
				return -1;
			} else if (thisResult == thatResult) {
				return 0;
			} else {
				return 1;
			}
		}
		
		@Override
		public String toString() {
			String paramSubstring = "";
			for (int j = 0; j < parms.length; j++) {
				paramSubstring += "p" + (j+1) + "= " + Formatter.format(parms[j]) + " ";
			}
			String output = "Point "+ index + " : " + paramSubstring + "Evaluation : " + Formatter.format(result.getMean().m_afData[0][0]);
			return output;
		}
		
	}
	
	@Override
	public double value(double[] values) throws FunctionEvaluationException, IllegalArgumentException {
		return getValue(values).getResult().getMean().m_afData[0][0];		// the mean is returned
	}

	protected ObjectiveFunctionEvaluation getValue(double[] values) throws FunctionEvaluationException, IllegalArgumentException {
		interrupted = false;
		iteration++;
//		int nbMaxWorkers = Runtime.getRuntime().availableProcessors() - 1;
		int nbMaxWorkers = 1;
		tool.controlVariableManager.setControlVariables(values);
		double[] params = tool.controlVariableManager.getControlVariablesValues();

		objectiveFunctionValue = new MonteCarloEstimate();
		
		int numberOfRealizations;
		if (tool.currentSettings.isStochastic && tool.currentSettings.isStochasticAllowed) {
			if (isGridSearch) {
				numberOfRealizations = tool.currentSettings.numberOfRealizationsForGridSearch;
			} else {
				numberOfRealizations = tool.currentSettings.numberOfRealizationsForOptimization;
			}
		} else {
			numberOfRealizations = 1;
		}
		
		Queue queue = new LinkedBlockingQueue();
		for (int iter = 0; iter < numberOfRealizations; iter++) {
			queue.add(iter);
		}

		List<OptimizerWorker> workers = new ArrayList<OptimizerWorker>();
		for (int nbWorkers = 0; nbWorkers < nbMaxWorkers; nbWorkers++) {
			workers.add(new OptimizerWorker(this, queue, nbWorkers));
		}
		
		for (OptimizerWorker worker : workers) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (cancelRequested || interrupted) {
			IllegalArgumentException e;
			if (cancelRequested) {
				e = new OptimizerCancelException();
			} else {
				e = new InvalidDialogParametersException();
			}
			cancelRequested = false;
			interrupted = false;
			tool.originalStep.setLeftSon(null);		// the list of steps is left to the garbage collector to avoid memory leakage
			throw e;
		}
		
		ObjectiveFunctionEvaluation eval = new ObjectiveFunctionEvaluation(iteration, objectiveFunctionValue, values, params);
		tool.fireOptimizerEvent(EventType.EVALUATING, eval.toString());
		return eval;
	}

	
	protected synchronized void computeIndicators(Vector<Step> steps, MonteCarloEstimate objectiveFunctionValue) {
		indicators.resetValues();
		indicators.steps = steps;
		Matrix realization = new Matrix(1,1);
		realization.m_afData[0][0] = indicators.getValue();
		objectiveFunctionValue.addRealization(realization);		
		tool.fireOptimizerEvent(EventType.SUBITERATING, "Subiterating...");
	}
	
	protected Vector<Step> findStepVectorFromOriginalStep(Vector<Step> steps) {
		Vector<Step> stepsToOriginalSteps = new Vector<Step>();
		stepsToOriginalSteps.addAll(steps);
		int firstStepIndex = stepsToOriginalSteps.indexOf(tool.originalStep);
		for (int i = 0; i < firstStepIndex; i++) {
			steps.remove(0);
		}
		return steps;
	}

	@Override
	public ObjectiveFunctionPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new ObjectiveFunctionPanel(this);
		}
		return guiInterface;
	}

	
	protected void saveParametersIntoSettings() {
		tool.getSettingMemory().setProperty("goal", goal.toString());
		for (Indicator indicator : indicators) {
			indicator.saveParametersIntoSettings(tool);
		}
	}

	protected void loadFrom(ObjectiveFunction of) {
		indicators.clear();
		indicators.addAll(of.indicators);
		cbc.unpackMemorizerPackage(of.cbc.getMemorizerPackage());
		goal = of.goal;
		scenRecorder.loadFrom(of.scenRecorder);
	}

	
}
