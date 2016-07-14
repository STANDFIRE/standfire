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

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import repicea.app.AbstractGenericEngine;
import repicea.app.GenericTask;
import repicea.gui.REpiceaShowableUI;
import repicea.gui.Resettable;
import repicea.io.IOUserInterfaceableObject;
import repicea.math.Matrix;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
import repicea.simulation.covariateproviders.standlevel.StochasticInformationProvider;
import repicea.util.ExtendedFileFilter;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.commongui.projectmanager.Current;
import capsis.extension.modeltool.optimizer.ObjectiveFunction.InvalidDialogParametersException;
import capsis.extension.modeltool.optimizer.ObjectiveFunction.ObjectiveFunctionEvaluation;
import capsis.extension.modeltool.optimizer.ObjectiveFunction.OptimizerCancelException;
import capsis.extension.modeltool.optimizer.OptimizerEvent.EventType;
import capsis.extension.modeltool.optimizer.OptimizerTask.TaskID;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder;
import capsis.extensiontype.ModelTool;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.lib.emerge.EmergeDB;
import capsis.lib.emerge.UseEmergeDB;


@SuppressWarnings("deprecation")
public class OptimizerTool extends AbstractGenericEngine implements REpiceaShowableUI, ModelTool, IOUserInterfaceableObject, Resettable, Memorizable {

	
	protected static enum MessageID implements TextableEnum {
		Name("AMAP-LERFoB Optimization Tool", "Outil d'optimisation AMAP-LERFoB"),
		Description("Optimization Tool", "Outil d'optmisation"),
		CapsisOptimizerFileFilter("CAPSIS optimization file (*.opt)", "Fichier d'optimisation CAPSIS (*.opt)");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	public static final String AUTHOR="G. LeMogu\u00E9dec, M. Fortin";
	public static final String VERSION="1.0";
	public static final String NAME = MessageID.Name.toString ();
	public static final String DESCRIPTION = MessageID.Description.toString();

	private static class CapsisOptimizerFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".opt";
		
		@Override
		public boolean accept(File f) {
			if (f.getAbsolutePath().toLowerCase().trim().endsWith(extension)) {
				return true;
			} else if (f.isDirectory()) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getDescription() {
			return MessageID.CapsisOptimizerFileFilter.toString();
		}

		@Override
		public String getExtension() {return extension;}
	}

	private static final FileFilter MyFileFilter = new CapsisOptimizerFileFilter();

	protected final ObjectiveFunction of;
	protected final ControlVariableManager controlVariableManager;
	protected final OptimizerToolSettings currentSettings;

	
	private transient boolean guiMode;
	private transient final List<ObjectiveFunctionEvaluation> bestGridSearchSolutions;
	private transient OptimizerToolDialog guiInterface;
	private transient final List<OptimizerListener> listeners;
	protected transient final OptimizerLogFileWriter logFileWriter;
	private transient GModel model;
	protected transient Step originalStep;
	private transient Project internalProject;
	private transient Project originalProject;
	private transient Step secondStep;
	private transient String filename;

	public OptimizerTool() {
		super();
		filename = "";
		setSettingMemory(new OptimizerToolDefaultSettings());
		listeners = new CopyOnWriteArrayList<OptimizerListener>();
		of = new ObjectiveFunction(this);
		controlVariableManager = new ControlVariableManager(this);
		currentSettings = new OptimizerToolSettings(this);
		logFileWriter = new OptimizerLogFileWriter(this);
		bestGridSearchSolutions = new ArrayList<ObjectiveFunctionEvaluation>();
	}
	

	/**
	 * Constructor in script mode.
	 * @param mod a GModel instance
	 * @param step a Step instance
	 */
	public OptimizerTool(GModel mod, Step step) {
		this();
		initInScriptMode(mod, step);
	}
	
	
	@Override
	public OptimizerToolDialog getUI() {
		if (guiInterface == null) {
			Window parent = null;
			try {
				parent = MainFrame.getInstance();
			} catch (Error e) {}
			guiInterface = new OptimizerToolDialog(this, parent);
		}
		return guiInterface;
	}

	@Override
	public void init(GModel m, Step s) {
		initializeTool(m, s, true);
	}

	
	public void initInScriptMode(GModel m, Step s) {
		initializeTool(m, s, false);
	}
	
	protected void initializeTool(GModel m, Step s, boolean guiMode) {
		this.guiMode = guiMode;
		originalStep = s;
		model = m;
		
		if (originalStep != null) {
			GScene scene = originalStep.getScene();
			if (scene instanceof StochasticInformationProvider) {
				currentSettings.isStochasticAllowed = ((StochasticInformationProvider) scene).isStochastic();		
			}
		}

		originalProject = model.getProject();
		
		setEmergeDataBaseEnabled(model, originalProject, false);		// disconnect
		
		of.methodProvider = originalProject.getModel().getMethodProvider();
		internalProject = new Project(originalProject.getName(), model);
		
		ScenarioRecorder.getInstance().initialize(model, originalStep);

		Runnable toBeRun = new Runnable () {
			@Override
			public void run () {
				try {
					startApplication();
				} catch (Exception e) {
					throw new RuntimeException("The Optimizer engine has failed!");
				} 
			}
		};
		
		new Thread(toBeRun, "OptimizerTool").start();
	}
	
	
	
	private void setEmergeDataBaseEnabled(GModel model, Project project, boolean enabled) {
		if (model instanceof UseEmergeDB) {		// make sure to disconnect the database in order to avoid memory leak
			EmergeDB dataBase = ((UseEmergeDB) model).getEmergeDB();
			if (dataBase != null) {
				if (enabled) {
					project.addListener(dataBase); 	
				} else {
					project.removeListener(dataBase); 	
				}
			}
		}
		
	}
	
	/*	
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		if (referent != null && referent instanceof GModel) {
			return true;
		} else {
			return false;
		}
	}

	
	/*
	 * Useless (non-Javadoc)
	 * @see jeeb.lib.defaulttype.Extension#activate()
	 */
	@Override
	public void activate () {}

	@Override
	public void showUI() {
		getUI().setVisible(true);
	}

	@Override
	protected void decideWhatToDoInCaseOfFailure(GenericTask task) {queue.clear();}

	@Override
	protected void firstTasksToDo () {
		if (guiMode) {
			addTask(new OptimizerTask(TaskID.ShowInterface, this));
		}
	}
	
	
	private void detachStepFromOriginalProject() {
		internalProject.setRoot(originalStep);
		model.setProject(internalProject);
		setEmergeDataBaseEnabled(model, internalProject, false);		// disconnect
		originalStep.setProject(internalProject);
		secondStep = (Step) originalStep.getLeftSon();
		originalStep.setLeftSon(null);
	}
	
	
	protected void gridSearch() throws Exception {
		fireOptimizerEvent(EventType.GRID_START, "Grid search started.");
		
		long timeStart = System.currentTimeMillis();		
		detachStepFromOriginalProject();

		Matrix quantiles = OptimizerTool.getQuantilesForGrid(currentSettings.gridDivision, 
				controlVariableManager.getList().size(), 
				true);
		
		List<ObjectiveFunctionEvaluation> evaluations = new ArrayList<ObjectiveFunctionEvaluation>();
		long elapsedTime;
		
		of.isGridSearch = true;
		of.iteration = 0;
		try {
			for (int i = 0; i < quantiles.m_iRows; i++) {
				double[] quantilesForThisEvaluation = quantiles.m_afData[i];
				double[] realValues = controlVariableManager.convertQuantileToReal(quantilesForThisEvaluation);
				ObjectiveFunctionEvaluation eval = of.getValue(realValues);
				evaluations.add(eval);
			}
			
			if (of.goal == GoalType.MAXIMIZE) {
				Collections.sort(evaluations, Collections.reverseOrder()); // sorting in descending order if the goal is maximizing
			} else {
				Collections.sort(evaluations);	// sorting in ascending order if the goal is minimizing
			}

			bestGridSearchSolutions.clear();	 
			bestGridSearchSolutions.add(evaluations.get(0));
			bestGridSearchSolutions.add(evaluations.get(1));
			String message = "Grid search terminated. The best solutions were: " + "\n";
			for (ObjectiveFunctionEvaluation eval : bestGridSearchSolutions) {
				message = message + eval.toString() + "\n";
			}

			elapsedTime = System.currentTimeMillis () - timeStart;
			fireOptimizerEvent(EventType.GRID_END, message + "Elapsed Time : " + formatTime(elapsedTime));
		} catch (Exception e) {
			// TODO restore original parameters
			elapsedTime = System.currentTimeMillis () - timeStart;
			if (e instanceof OptimizerCancelException) {
				fireOptimizerEvent(EventType.GRID_CANCELLED, "Grid search cancelled." +"\n" + "Elapsed Time : " + formatTime(elapsedTime));
			} else if (e instanceof OptimizationException) {
				fireOptimizerEvent(EventType.OPTIMIZATION_ABORTED, "Optimization aborted : " + e.getMessage() + System.getProperty("line.separator")  + "Elapsed time : " + formatTime(elapsedTime));
			} else if (e instanceof InvalidDialogParametersException) {
				fireOptimizerEvent(EventType.OPTIMIZATION_INVALIDPARAMETERS, "Optimization aborted : " + e.getMessage() + System.getProperty("line.separator")  + "Elapsed time : " + formatTime(elapsedTime));
			}
			throw e;
		} finally {
			of.isGridSearch = false;
			reconnectOnOriginalProject();
		}
	}
	

	protected void optimize() throws Exception {
		fireOptimizerEvent(EventType.OPTIMIZATION_START, "Optimization started.");

		long timeStart = System.currentTimeMillis();		
		detachStepFromOriginalProject();
		
		double[] startParms;
		
		NelderMead nm = currentSettings.getNelderMeadOptimizer();
		of.iteration = 0;

		if (currentSettings.enableRandomStart) {
			startParms = controlVariableManager.getRandomRealValues();
			double[][] simplex = controlVariableManager.getRandomSimplex();
			nm.setStartConfiguration(simplex) ;
		} else {	// means we have used the grid search method
			startParms = bestGridSearchSolutions.get(0).getRealParms();
			double[] secondBest = bestGridSearchSolutions.get(1).getRealParms();
			double[] deltaParms = new Matrix(secondBest).subtract(new Matrix(startParms)).transpose().m_afData[0];
			nm.setStartConfiguration(deltaParms);
		}
		
		long elapsedTime;
		try {
			RealPointValuePair result = nm.optimize(of, of.goal, startParms);
			elapsedTime = System.currentTimeMillis () - timeStart;
			fireOptimizerEvent(EventType.OPTIMIZATION_END, "Optimization successful." + System.getProperty("line.separator") + "Elapsed time : " + formatTime(elapsedTime));
			controlVariableManager.setControlVariables(result.getPoint());
			reconnectOnOriginalProject();
			Step finalStep = of.scenRecorder.playScenario();
			finalStep.setVisible(true);
			Current.getInstance().setStep(finalStep);
			fireOptimizerEvent(EventType.BEST_SCENARIO_PLAYED, "Best scenario just played!");
		} catch (Exception e) {
			// TODO FP restore original parameters
			elapsedTime = System.currentTimeMillis () - timeStart;
			if (e instanceof OptimizerCancelException) {
				fireOptimizerEvent(EventType.OPTIMIZATION_CANCELLED, "Optimization cancelled." + System.getProperty("line.separator")  + "Elapsed time : " + formatTime(elapsedTime));
			} else if (e instanceof OptimizationException) {
				fireOptimizerEvent(EventType.OPTIMIZATION_ABORTED, "Optimization aborted : " + e.getMessage() + System.getProperty("line.separator")  + "Elapsed time : " + formatTime(elapsedTime));
			} else if (e instanceof InvalidDialogParametersException) {
				fireOptimizerEvent(EventType.OPTIMIZATION_INVALIDPARAMETERS, "Optimization aborted : " + e.getMessage() + System.getProperty("line.separator")  + "Elapsed time : " + formatTime(elapsedTime));
			}
			throw e;
		} finally {
			reconnectOnOriginalProject();
		}
	}

	private String formatTime(long elapsedTime) {
		long hr = TimeUnit.MILLISECONDS.toHours(elapsedTime);
		long min = TimeUnit.MILLISECONDS.toMinutes (elapsedTime - TimeUnit.HOURS.toMillis(hr));
		long sec = TimeUnit.MILLISECONDS.toSeconds (elapsedTime - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		return ((Long) hr).toString() + "h " + ((Long) min).toString() + "m " + ((Long) sec).toString() + "s";
	}
	
	private void reconnectOnOriginalProject() {
		model.setProject (originalProject);
		originalStep.setProject(originalProject);
		originalStep.setLeftSon(secondStep);
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void addOptimizerListener(OptimizerListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void removeOptimizerListener(OptimizerListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireOptimizerEvent(OptimizerEvent.EventType type, String message) {
		for (OptimizerListener listener : listeners) {
			listener.optimizerJustDidThis(new OptimizerEvent(type, message));
		}
	}
	
	protected static Matrix getQuantilesForGrid(int gridDivider, int dimensions, boolean randomNoise) {
		double factor = 1d / (gridDivider);
		int totNumber = (int) Math.pow(gridDivider, dimensions);
		int[] thresholds = new int[dimensions];
		for (int i = 0; i < thresholds.length; i++) {
			if (i == 0) {
				thresholds[i] = 1; 
			} else {
				thresholds[i] = thresholds[i-1] * gridDivider;
			}
		}
		
		Matrix mat = new Matrix(totNumber, dimensions);
		Matrix noiseMat = new Matrix(totNumber, dimensions);
		for (int i = 0; i < mat.m_iRows; i++) {
			int remainder = i;
			for (int j = mat.m_iCols - 1; j >= 0; j--) {
				mat.m_afData[i][j] = Math.floor(remainder / thresholds[j]);
				remainder -= mat.m_afData[i][j] * thresholds[j];
				if (randomNoise) {
					noiseMat.m_afData[i][j] = Math.random();
				}
			}
		}
		
		mat = mat.scalarMultiply(factor).scalarAdd (.5 * factor);
		
		if (randomNoise) {
			noiseMat = noiseMat.scalarMultiply(factor).scalarAdd(-.5 * factor);
			mat = mat.add(noiseMat);
		}

		return mat;
	}

	@Override
	protected void shutdown(int i) {
		System.out.println("Shutting down optimizer...");
		setEmergeDataBaseEnabled(model, originalProject, true);		// reconnect
	}
	
	@Override
	public void requestShutdown() {
		currentSettings.updateProperties();
		super.requestShutdown();
	}

	@Override
	public void save(String filename) throws IOException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(getMemorizerPackage());
		this.filename = filename;
	}

	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		MemorizerPackage mp = (MemorizerPackage) deserializer.readObject();
		unpackMemorizerPackage(mp);
		fireOptimizerEvent(EventType.JUST_LOADED, null);
		this.filename = filename;
	}

	@Override
	public FileFilter getFileFilter() {return MyFileFilter;}

	@Override
	public String getFilename() {return filename;}

	@Override
	public void reset() {
		controlVariableManager.getList().clear();
		of.indicators.reset();
		filename = "";
	}


	public static void main(String[] args) {
		OptimizerTool ot = new OptimizerTool();
		ot.showUI();
//		System.exit(0);
	}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(of);
		mp.add(controlVariableManager);
		mp.add(currentSettings);
		return mp;
	}


	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		ObjectiveFunction of = (ObjectiveFunction) wasMemorized.get(0);
		this.of.loadFrom(of);
		ControlVariableManager controlVariableManager = (ControlVariableManager) wasMemorized.get(1);
		this.controlVariableManager.loadFrom(controlVariableManager);
		OptimizerToolSettings settings = (OptimizerToolSettings) wasMemorized.get(2);
		currentSettings.unpackMemorizerPackage(settings.getMemorizerPackage());
	}


}
