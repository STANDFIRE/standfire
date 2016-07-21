/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2014 Mathieu Fortin (AgroParisTech/INRA - UMR LERFoB)
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
package capsis.extension.modeltool.scenariorecorder;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.filechooser.FileFilter;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import repicea.gui.REpiceaDialog;
import repicea.gui.REpiceaShowableUI;
import repicea.gui.Resettable;
import repicea.gui.UIToolKit;
import repicea.gui.UIToolKit.WindowTrackerListener;
import repicea.io.IOUserInterfaceableObject;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;
import repicea.util.ExtendedFileFilter;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.commongui.EvolutionDialog;
import capsis.commongui.projectmanager.Current;
import capsis.extension.modeltool.scenariorecorder.ParameterDialogWrapper.ParameterCategory;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderEvent.EventType;
import capsis.gui.MainFrame;
import capsis.kernel.EvolutionParameters;
import capsis.kernel.GModel;
import capsis.kernel.Step;

public class ScenarioRecorder implements IOUserInterfaceableObject, REpiceaShowableUI, WindowTrackerListener, Listener,
		Resettable {

	public static class InvalidDialogException extends Exception {
	}

	protected static enum MessageID implements TextableEnum {
		Name("LERFoB Scenario recorder", "Enregistreur de sc\u00E9narios LERFoB"), Description("Scenario recorder",
				"Enregistreur de sc\u00E9narios"), CapsisScenarioFileFilter("CAPSIS scenario file (*.sce)",
				"Fichier de sc\u00E9nario CAPSIS (*.sce)");

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

	private static class CapsisScenarioFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".sce";

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
			return MessageID.CapsisScenarioFileFilter.toString();
		}

		@Override
		public String getExtension() {
			return extension;
		}
	}

	private static final FileFilter MyFileFilter = new CapsisScenarioFileFilter();

	private static ScenarioRecorder Instance;

	private transient String filename;
	protected List<ParameterDialogWrapper> parameterWrappers;

	private transient ParameterDialogWrapper currentWrapper;
	private transient ScenarioRecorderDialog guiInterface;
	private transient GModel model;
	protected transient Step initialStep;
	protected transient Step lastStepInScenario;
	private transient boolean isWaitingForApproval;
	private final transient List<ScenarioRecorderListener> listeners;

	private boolean isPlaying;
	private boolean interrupted;

	private ScenarioRecorder() {
		parameterWrappers = new ArrayList<ParameterDialogWrapper>();
		listeners = new CopyOnWriteArrayList<ScenarioRecorderListener>();
		Current.getInstance().addListener(this);
	}

	/**
	 * This method initializes the ScenarioRecorder singleton to a particular
	 * model and step.
	 * 
	 * @param m
	 *            a GModel instance
	 * @param s
	 *            a Step instance
	 */
	public void initialize(GModel m, Step s) {
		model = m;
		initialStep = s;
		lastStepInScenario = s;
		reset();
	}

	public static ScenarioRecorder getInstance() {
		if (Instance == null) {
			Instance = new ScenarioRecorder();
		}
		return Instance;
	}

	@Override
	public REpiceaDialog getUI() {
		if (guiInterface == null) {
			guiInterface = new ScenarioRecorderDialog(this, MainFrame.getInstance());
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	@Override
	public void showUI() {
		getUI().setVisible(true);
	}

	protected void startRecording() {
		UIToolKit.addWindowTrackerListener(this);
	}

	protected void stopRecording() {
		UIToolKit.removeWindowTrackerListener(this);
	}

	protected void registerWrapper(ParameterDialogWrapper wrapper) {
		wrapper.addToWrappersAndSetName();
		fireScenarioRecorderEvent(EventType.WRAPPER_ADDED, parameterWrappers);
	}

	@Override
	public void receiveThisWindow(Window retrievedWindow) {
		if (isPlaying) {
			if (!(retrievedWindow instanceof EvolutionDialog) && !(retrievedWindow instanceof MainFrame)) {
				interrupted = true;
			}
		} else {
			if (retrievedWindow instanceof AmapDialog) {
				if (retrievedWindow instanceof EvolutionDialog) { // ||
																	// retrievedWindow
																	// instanceof
																	// InterventionDialog)
																	// {
					if (Current.getInstance().getStep().equals(lastStepInScenario)) {
						if (!doYouKnowThisWindow(retrievedWindow)) {
							if (currentWrapper == null || !currentWrapper.guiInterface.equals(retrievedWindow)) {
								ParameterCategory category;
								if (retrievedWindow instanceof EvolutionDialog) {
									category = ParameterCategory.Evolution;
								} else {
									category = ParameterCategory.Intervention;
								}
								currentWrapper = new ParameterDialogWrapper(category, (AmapDialog) retrievedWindow,
										this);
								isWaitingForApproval = true;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method returns true if the window is part of the scenario of false
	 * otherwise.
	 * 
	 * @param window
	 *            a given Window instance
	 * @return a boolean
	 */
	private boolean doYouKnowThisWindow(Window window) {
		for (ParameterDialogWrapper wrapper : parameterWrappers) {
			if (window.equals(wrapper.guiInterface)) {
				return true;
			}
		}
		return false;
	}

	public void addScenarioRecorderListener(ScenarioRecorderListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeScenarioRecorderListener(ScenarioRecorderListener listener) {
		listeners.remove(listener);
	}

	private void fireScenarioRecorderEvent(ScenarioRecorderEvent.EventType type, List<ParameterDialogWrapper> wrappers) {
		for (ScenarioRecorderListener listener : listeners) {
			listener.scenarioRecorderJustDidThis(new ScenarioRecorderEvent(type, wrappers));
		}
	}

	/**
	 * This method plays the current scenario.
	 */
	public Step playScenario() throws InvalidDialogException {
		isPlaying = true;
		interrupted = false;
		UIToolKit.addWindowTrackerListener(this);
		fireScenarioRecorderEvent(EventType.PLAY_CALLED, parameterWrappers);
		Step currentStep = initialStep;
		// Project prj = model.getProject();
		ParameterDialogWrapper wrapper = null;
		EvolutionParameters parameters = null;
		int i = 0;
		for (i = 0; i < parameterWrappers.size(); i++) {
			wrapper = parameterWrappers.get(i);
			if (wrapper.getCategory() == ParameterCategory.Evolution) {
				parameters = getEvolutionParameters(wrapper, currentStep);
				if (!interrupted) {
					try {
						// currentStep = prj.evolve(currentStep, parameters); //
						// NOT THREAD SAFE
						currentStep = model.getRelay().processEvolution(currentStep, parameters);
					} catch (Exception e) {
						e.printStackTrace();
						interrupted = true;
						break;
					}
				} else {
					break;
				}
			}
		}
		if (interrupted) {
			throw new InvalidDialogException();
		} else {
			if (lastStepInScenario.equals(initialStep)) {
				lastStepInScenario = currentStep;
			}
			fireScenarioRecorderEvent(EventType.PLAY_TERMINATED, parameterWrappers);
			UIToolKit.removeWindowTrackerListener(this);
			isPlaying = false;
			return currentStep;
		}
	}

	private synchronized EvolutionParameters getEvolutionParameters(ParameterDialogWrapper wrapper, Step currentStep) {
		wrapper.setInstantiationStep(currentStep);
		return (EvolutionParameters) wrapper.getParameters();
	}

	@Override
	public void save(String filename) throws FileNotFoundException, XmlMarshallException {
		XmlSerializer serializer = new XmlSerializer(filename);
		serializer.writeObject(this);
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public void load(String filename) throws FileNotFoundException, XmlMarshallException {
		fireScenarioRecorderEvent(EventType.ABOUT_TO_LOAD, parameterWrappers);
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		ScenarioRecorder scenario = (ScenarioRecorder) deserializer.readObject();
		loadFrom(scenario);
		this.filename = filename;
	}

	public void loadFrom(ScenarioRecorder scenario) {
		parameterWrappers.clear();
		parameterWrappers.addAll(scenario.parameterWrappers);
		fireScenarioRecorderEvent(EventType.JUST_LOADED, null);
	}

	@Override
	public FileFilter getFileFilter() {
		return MyFileFilter;
	}

	@Override
	public void somethingHappened(ListenedTo l, Object param) {
		if (isWaitingForApproval) {
			if (param.toString().equals(Current.STEP_CHANGED)) {
				this.lastStepInScenario = Current.getInstance().getStep();
				fireScenarioRecorderEvent(EventType.WRAPPER_APPROVED, parameterWrappers);
				isWaitingForApproval = false;
			}
		}
	}

	@Override
	public void reset() {
		filename = "";
		parameterWrappers.clear();
		lastStepInScenario = initialStep;
	}

}
