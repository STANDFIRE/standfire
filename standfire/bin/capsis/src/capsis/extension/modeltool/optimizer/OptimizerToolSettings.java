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

import java.awt.Container;
import java.awt.Window;
import java.io.Serializable;

import org.apache.commons.math.optimization.direct.NelderMead;

import repicea.gui.REpiceaShowableUIWithParent;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;


public class OptimizerToolSettings implements REpiceaShowableUIWithParent, Memorizable, Serializable {
	
	protected int maxNumberEvaluations;

	/** Maximum number of iterations */
	protected int maxNumberIterations;
	
	/** Threshold value for the absolute convergence criterion */
	protected double absoluteConvergence;

	/** Threshold value for the relative convergence criterion */
	protected double relativeConvergence;

	
	protected int numberOfRealizationsForGridSearch;
	protected int numberOfRealizationsForOptimization;
	
	protected String logPath = System.getProperty ("java.io.tmpdir");
	protected int gridDivision = 3;
	
	protected transient OptimizerTool optimizer;
	protected boolean enableGridSearch = true;
	protected boolean enableRandomStart = false;
	protected boolean verbose = true;
	
	protected boolean isStochastic = false;
	protected boolean isStochasticAllowed = false;

	private transient OptimizerToolSettingsDialog guiInterface;


	protected OptimizerToolSettings(OptimizerTool optimizer) {
		super();
		this.optimizer = optimizer;
		
		maxNumberEvaluations = optimizer.getSettingMemory().getProperty("maxNumberEvaluations", 50000);
		maxNumberIterations = optimizer.getSettingMemory().getProperty("maxNumberIterations", 500);
		if (maxNumberIterations < 100) {
			maxNumberIterations = 500;
		}
		absoluteConvergence = optimizer.getSettingMemory().getProperty("absoluteConvergence", 1E-3);
		relativeConvergence = optimizer.getSettingMemory().getProperty("relativeConvergence", 1E-5);
		numberOfRealizationsForGridSearch = optimizer.getSettingMemory().getProperty("numberOfRealizationsForGridSearch", 10);
		numberOfRealizationsForOptimization = optimizer.getSettingMemory().getProperty("numberOfRealizationsForOptimization", 400);
		logPath = optimizer.getSettingMemory().getProperty("logPath", System.getProperty ("java.io.tmpdir"));
		gridDivision = optimizer.getSettingMemory().getProperty("gridDivision", 3);
		enableGridSearch = optimizer.getSettingMemory().getProperty("enableGridSearch", true);
		enableRandomStart = optimizer.getSettingMemory().getProperty("enableRandomStart", false);
		verbose = optimizer.getSettingMemory().getProperty("verbose", true);
		isStochastic = optimizer.getSettingMemory().getProperty("isStochastic", false);
	}

	protected NelderMead getNelderMeadOptimizer() {
		NelderMead nm = new NelderMead() ;
		
		// 2-Set values for technical parameters of the algorithm
		nm.setMaxEvaluations(maxNumberEvaluations) ;
		nm.setMaxIterations(maxNumberIterations) ;
		
		// Convergence criterion : either absolute or relative threshold have to be encountered
		ConvergenceChecker cc = new ConvergenceChecker(absoluteConvergence, relativeConvergence);
		nm.setConvergenceChecker(cc);
		return nm;
	}

	@Override
	public OptimizerToolSettingsDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new OptimizerToolSettingsDialog(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override 
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(maxNumberEvaluations);
		mp.add(maxNumberIterations);
		mp.add(absoluteConvergence);
		mp.add(relativeConvergence);
		mp.add(logPath);
		mp.add(gridDivision);
		mp.add(numberOfRealizationsForGridSearch);
		mp.add(numberOfRealizationsForOptimization);
		mp.add(isStochastic);
		return mp;
	}

	@Override
	public void unpackMemorizerPackage (MemorizerPackage wasMemorized) {
		maxNumberEvaluations = (Integer) wasMemorized.get(0);
		maxNumberIterations = (Integer) wasMemorized.get(1);
		if (maxNumberIterations < 100) {
			maxNumberIterations = 100;
		} else if (maxNumberIterations > 500) {
			maxNumberIterations = 500;
		}
		absoluteConvergence = (Double) wasMemorized.get(2);
		relativeConvergence = (Double) wasMemorized.get(3);
		logPath = wasMemorized.get(4).toString();
		gridDivision = (Integer) wasMemorized.get(5);
		numberOfRealizationsForGridSearch = (Integer) wasMemorized.get(6);
		numberOfRealizationsForOptimization = (Integer) wasMemorized.get(7); 
		isStochastic = (Boolean) wasMemorized.get(8);
	}
	
	protected int getNumberOfControlVariables() {
		int numberControlVariables = 0;
		if (optimizer != null) {
			numberControlVariables = optimizer.controlVariableManager.getList().size();
		}
		return numberControlVariables;
	}
	
	protected void updateProperties() {
		optimizer.getSettingMemory().setProperty("maxNumberEvaluations", maxNumberEvaluations);
		optimizer.getSettingMemory().setProperty("maxNumberIterations", maxNumberIterations);
		optimizer.getSettingMemory().setProperty("absoluteConvergence", absoluteConvergence);
		optimizer.getSettingMemory().setProperty("relativeConvergence", relativeConvergence);
		optimizer.getSettingMemory().setProperty("logPath", logPath);
		optimizer.getSettingMemory().setProperty("gridDivision", gridDivision);
		optimizer.getSettingMemory().setProperty("enableGridSearch", enableGridSearch);
		optimizer.getSettingMemory().setProperty("enableRandomStart", enableRandomStart);
		optimizer.getSettingMemory().setProperty("verbose", verbose);
		optimizer.getSettingMemory().setProperty("numberOfRealizationsForGridSearch", numberOfRealizationsForGridSearch);
		optimizer.getSettingMemory().setProperty("numberOfRealizationsForOptimization", numberOfRealizationsForOptimization);
		optimizer.getSettingMemory().setProperty("isStochastic", isStochastic);
	}

}
