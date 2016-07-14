/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2012 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 */
package capsis.extensiontype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.projectmanager.ButtonColorer;
import capsis.commongui.projectmanager.ButtonColorerListener;
import capsis.commongui.projectmanager.StepButton;
import capsis.extension.AbstractDataExtractor;
import capsis.extension.AbstractDiagram;
import capsis.extension.DECalibration;
import capsis.gui.Pilot;
import capsis.kernel.GModel;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.CancelException;
import capsis.util.SwingWorker3;

/**
 * A data block contains one or more data extractors. These extractors are all
 * instances of the same class (e.g.: Basal area / time). A data renderer is
 * associated to the data block. It's originally the default renderer of first
 * data extractor. Then, it can be swapped by user action on the renderer. The
 * renderer must be able to show the extracted data.
 * 
 * This data block listens to the ButtonColorer to manage color move and removal
 * in the ProjectManager by synchronizing / closing the related extractors.
 * 
 * @author F. de Coligny - december 1999 / march 2003 / april 2010.
 */
public class DataBlock implements ButtonColorerListener {

	// Extractor type is a data extractor className, e.g.
	// "capsis.extension.dataextractor.DETimeDbh"
	protected String extractorType;
	protected Set<DataExtractor> extractors;
	protected DataRenderer renderer;
	protected ExtensionManager extMan;
	// protected Step step;

	protected boolean captionRequired;
	private Collection<DataExtractor> specialExtractors; // never null, possibly
															// empty, e.g.
															// DECalibration
															// instances

	/**
	 * Default constructor, needed for subclasses, does nothing.
	 */
	public DataBlock() {
	}

	/**
	 * Preferred constructor.
	 */
	public DataBlock(String type, Step step) {
		this(type, step, null);
	}

	/**
	 * Builds a dataBlock with the given extractor (type, i.e. graph) opened on
	 * the given step, renderered by the suggested or default renderer,
	 * evrything is done in a thread to keep the gui reactive.
	 * 
	 * The suggestedRendererClassName field has been added for diagramLists, it
	 * is optional and may be passed null.
	 */
	public DataBlock(String type, Step step, String suggestedRendererClassName) {

		extractorType = type; // Must be a complete className (with package
								// name)

		extractors = new TreeSet<DataExtractor>(); // sorted on step date
		specialExtractors = new ArrayList<DataExtractor>();
		renderer = null;
		// this.step = step;
		captionRequired = true; // Change if needed with setCaptionRequired
								// (false)

		extMan = CapsisExtensionManager.getInstance();

		// Final variables for the worker thread below
		final String finalExtractorType = extractorType;
		final Step finalStep = step;
		final ExtensionManager finalExtMan = extMan;
		final Set<DataExtractor> finalExtractors = extractors;
		final String finalSuggestedRendererClassName = suggestedRendererClassName;

		// Data block construction: in a thread
		SwingWorker3 worker = new SwingWorker3() {

			// Runs in new Thread
			public Object construct() {
				try {
					// 1. Load an extractor of given type for given step
					DataExtractor extractor = (DataExtractor) finalExtMan.loadInitData(finalExtractorType,
							new GenericExtensionStarter("model", finalStep.getProject().getModel(), "step", finalStep));

					extractor.init(finalStep.getProject().getModel(), finalStep);

					finalExtractors.add(extractor);

					extractor.doExtraction(); // initialize extractor

					return extractor; // correct return of construct

				} catch (Exception e) {
					Log.println(Log.ERROR, "DataBlock (type, step)",
							"Constructor: unable to load data extractor of type " + finalExtractorType, e);
					return e;
				}
			}

			// Runs in dispatch event thread when construct is over
			public void finished() {

				DataExtractor extractor = null;
				try {
					Object result = get(); // result is either the correct
											// return of construct or an
											// exception
					if (result instanceof Exception)
						throw (Exception) result;

					// 2. Load the default renderer for dataBlock
					extractor = (DataExtractor) result;

					String rendererClassName = extractor.getDefaultDataRendererClassName();

					// fc-10.12.2015 for diagramLists, must reopen with same
					// renderer
					if (finalSuggestedRendererClassName != null)
						rendererClassName = finalSuggestedRendererClassName;

					renderer = (DataRenderer) finalExtMan.instantiate(rendererClassName);

					renderer.init(DataBlock.this);

					renderer.update(); // update renderer

					// Restore the calibration extractor if the option was set
					// at last extractor opening time
					if (extractor.hasConfigProperty("activateCalibration")) {

						String modelName = extractor.getComboProperty("activateCalibration");

						// At opening time, check if the calibration is
						// activated and if the selected model is not ours,
						// desactivate it (the user may reactivate it if needed)
						// fc-26.9.2012
						String ourModelName = finalStep.getProject().getModel().getIdCard().getModelPackageName();

						if (modelName.equals(Translator.swap("Shared.noneFeminine"))) {
							// Calibration is not activated, do nothing

						} else if (modelName.equals(ourModelName)) {
							// Add the calibration extractor
							addCalibrationExtractor(modelName);

						} else {
							// Let the user select the calibration extractor if
							// he likes (the memorised option is not our model,
							// deselect it)
							String toBeSelected = Translator.swap("Shared.noneFeminine");
							((AbstractDataExtractor) extractor).selectOptionInComboProperty("activateCalibration",
									toBeSelected);

						}

					}

					// Register to the ButtonColorer
					ButtonColorer.getInstance().addListener((ButtonColorerListener) DataBlock.this);

					String extensionName = ExtensionManager.getName(extractorType);
					StatusDispatcher.print("" + extensionName);

				} catch (Exception e) {
					if (e instanceof CancelException) {
						StatusDispatcher.print(Translator.swap("Shared.operationCancelled")); // nothing
																								// more
					} else {
						Log.println(
								Log.ERROR,
								"DataBlock (type, step)",
								"finished (): cannot instanciate default DataRenderer <"
										+ extractor.getDefaultDataRendererClassName() + ">", e);
						StatusDispatcher.print(Translator.swap("Shared.errorSeeLog"));
					}
				}
			}
		};

		String extensionName = ExtensionManager.getName(extractorType);
		StatusDispatcher.print(Translator.swap("Shared.opening") + " " + extensionName + "...");

		worker.start(); // launch the thread

	}

	/**
	 * Add a calibration extractor.
	 */
	public void addCalibrationExtractor(String modelName) {

		if (specialExtractors.contains(modelName))
			return; // Already there

		String extractorName = AmapTools.getClassSimpleName(extractorType);
		DECalibration c = new DECalibration(this, modelName, extractorName);
		c.doExtraction();

		// Calibration extractors are managed aside
		specialExtractors.clear(); // only one calibration extractor at the same
									// time
		specialExtractors.add(c);

		renderer.update();
	}

	/**
	 * Remove a calibration extractor.
	 */
	public void removeCalibrationExtractor() {
		specialExtractors.clear();
	}

	/**
	 * Accessor for special extractors, ex: DECalibration.
	 */
	public Collection<DataExtractor> getSpecialExtractors() {
		return specialExtractors;
	}

	/**
	 * Add an extractor to the data block.
	 */
	public void addExtractor(Step step) {
		if (step == null) {
			return;
		}

		// For security
		if (!extMan.isCompatible(extractorType, step.getProject().getModel())) {
			return;
		}

		// Calculate rank : how many extractors of this type already
		// synchronized on same step ?
		int rank = 0;
		for (DataExtractor e : extractors) {
			if (e.getStep().equals(step)) {
				rank++;
			}
		}

		final String finalExtractorType = extractorType;
		final Step finalStep = step;
		final ExtensionManager finalExtMan = CapsisExtensionManager.getInstance();
		final int finalRank = rank;
		final Set<DataExtractor> finalExtractors = extractors;

		// fc - 20.9.2005 - try to thread data block construction
		SwingWorker3 worker = new SwingWorker3() {

			// Runs in new Thread
			public Object construct() {
				try {

					DataExtractor extractor = (DataExtractor) finalExtMan.loadInitData(finalExtractorType,
							new GenericExtensionStarter("model", finalStep.getProject().getModel(), "step", finalStep));
					extractor.init(finalStep.getProject().getModel(), finalStep);

					if (finalRank != 0)
						extractor.setRank(finalRank); // fc - 5.5.2003
					finalExtractors.add(extractor);

					// System.out.println ("DataBlock addExtractor ()...");

					extractor.doExtraction(); // initialize new extractor

					// System.out.println
					// ("DataBlock addExtractor () extractor added");

					return null; // correct return of construct
				} catch (Exception e) {

					// System.out.println ("DataBlock addExtractor () error");

					Log.println(Log.ERROR, "DataBlock.addExtractor ()",
							"construct (): exception during loading / doExtraction for <" + finalExtractorType + ">.",
							e);
					return e;
				}
			}

			// Runs in dispatch event thread when construct is over
			public void finished() {
				try {
					Object result = get(); // result is either the correct
											// return of construct or an
											// exception
					if (result instanceof Exception) {
						throw (Exception) result;
					}

					renderer.update(); // update renderer

				} catch (Exception e) {
					if (e instanceof CancelException) {
						StatusDispatcher.print(Translator.swap("Shared.operationCancelled")); // nothing
																								// more
					} else {

						Log.println(Log.ERROR, "DataBlock.addExtractor ()",
								"finished (): exception while updating renderer", e);
						StatusDispatcher.print(Translator.swap("Shared.errorSeeLog"));
					}
				}
			}
		};

		renderer.setUpdating();
		worker.start(); // launch the thread

	}

	/**
	 * Remove an extractor from the data block, then update renderer. If last
	 * extractor was removed, dispose the renderer (which also disposes this
	 * dataBlock).
	 */
	public void removeExtractor(DataExtractor target) {
		int initialSize = extractors.size();

		for (Iterator<DataExtractor> i = extractors.iterator(); i.hasNext();) {
			DataExtractor extractor = i.next();
			if (extractor.equals(target)) {
				i.remove();
			}
		}

		// Update or close renderer
		if (extractors.size() == 0) {
			renderer.close();
		} else if (extractors.size() != initialSize) {
			renderer.update();
		}
	}

	/**
	 * Set the renderer unvisible, then dispose this data block.
	 */
	public void dispose() {
		// Deregister from the ButtonColorer
		ButtonColorer.getInstance().removeListener(this);
	}

	/**
	 * Return the collection of the data extractors connected to this block.
	 */
	public Collection<DataExtractor> getDataExtractors() {
		return extractors;
	}

	/**
	 * Force every extractor update (after configuration changed for example).
	 */
	public void updateExtractors() {

		final Collection<DataExtractor> finalExtractors = new ArrayList<DataExtractor>(extractors);

		// fc - 19.9.2005 - try to thread extractors update
		// Launch a thread extractors update
		SwingWorker3 worker = new SwingWorker3() {

			// Runs in new Thread
			public Object construct() {
				try {
					for (DataExtractor extractor : finalExtractors) {
						extractor.doExtraction();
					}
					return null; // correct return of construct
				} catch (final Throwable e) {
					Log.println(Log.ERROR, "DataBlock.updateExtractors ()", "Exception/Error in construct ()", e);
					return e;
				}
			}

			// Runs in dispatch event thread when construct is over
			public void finished() {
				try {
					Object result = get(); // result is either the correct
											// return of construct or an
											// exception
					if (result instanceof Exception) {
						throw (Exception) result;
					}

					renderer.update();
				} catch (Exception e) {
					Log.println(Log.ERROR, "DataBlock ().updateExtractors ()", "Exception/Error in finished ()", e);
					StatusDispatcher.print(Translator.swap("Shared.errorSeeLog"));
				}
			}
		};
		// no message in StatusDispatcher here (occurs very often)
		renderer.setUpdating();
		worker.start(); // launch the thread
	}

	/**
	 * Tell the extractors to update from source to target. Only the extractors
	 * synchronized on source will change.
	 */
	private void updateExtractors(Step source, Step target) {

		final Step finalSource = source;
		final Step finalTarget = target;
		final Collection<DataExtractor> finalExtractors = new ArrayList<DataExtractor>(extractors);

		// fc - 19.9.2005 - try to thread extractors update
		// Launch a thread extractors update
		SwingWorker3 worker = new SwingWorker3() {

			// Runs in new Thread
			public Object construct() {
				try {
					boolean someChange = false;
					for (DataExtractor extractor : finalExtractors) {
						boolean moved = extractor.update(finalSource, finalTarget); // if
																					// extractor
																					// is
																					// concerned,
																					// update
																					// returns
																					// true
						if (moved) {
							someChange = true;
						}
					}
					return new Boolean(someChange); // correct return of
													// construct
				} catch (final Throwable e) {
					Log.println(Log.ERROR, "DataBlock.updateExtractors (source, target)",
							"Exception/Error in construct ()", e);
					return e;
				}
			}

			// Runs in dispatch event thread when construct is over
			public void finished() {
				try {
					Object result = get(); // result is either the correct
											// return of construct or an
											// exception
					if (result instanceof Exception) {
						throw (Exception) result;
					}

					Boolean b = (Boolean) result;
					boolean someChange = b.booleanValue();

					if (someChange) {

						// Redo the extractors sorting
						extractors = new TreeSet<DataExtractor>(new Vector(extractors));

						renderer.update();
					} else {
						renderer.setUpdated(); // not updating... any more
					}
				} catch (Exception e) {
					Log.println(Log.ERROR, "DataBlock ().updateExtractors (source, target)",
							"Exception/Error in finished ()", e);
					StatusDispatcher.print(Translator.swap("Shared.errorSeeLog"));
				}
			}
		};
		// no message in StatusDispatcher here (occurs very often)
		renderer.setUpdating();
		worker.start(); // launch the thread
	}

	/**
	 * Can be used to swap compatible data renderers.
	 */
	public void setRenderer(String rendererFullClassName) {
		DataRenderer memory = renderer;

		// New renderer instanciation

		try {
			renderer = (DataRenderer) extMan.instantiate(rendererFullClassName);

			// Avoid to recreate a new window
			Pilot.getInstance().getPositioner().replaceDiagram((AbstractDiagram) memory, (AbstractDiagram) renderer);
			renderer.init(this);
			renderer.update();

		} catch (Exception e) {
			Log.println(Log.ERROR, "DataBlock.setRenderer ()", "Unable to load DataRenderer <" + rendererFullClassName
					+ ">", e);
			renderer = memory;
			return;
		}
		// memory.destroyRendererOnly (); // this destroys the renderer without
		// disposing the dataBlock
	}

	/**
	 * Returns the current data renderer.
	 */
	public DataRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Return the connected extractors' type (i.e. complete extractor
	 * className).
	 */
	public String getExtractorType() {
		return extractorType;
	}

	/**
	 * Return the class names of DataRenderers that can render the block.
	 */
	public Collection<String> getCompatibleDataRendererClassNames() {
		DataExtractor extractor = (DataExtractor) extractors.iterator().next();
		if (extractor != null) {
			return extMan.getExtensionClassNames(CapsisExtensionManager.DATA_RENDERER, extractor);
		} else {
			return null;
		}
	}

	/**
	 * Name of the block is the name of one of its extractors.
	 */
	public String getName() {
		Iterator<DataExtractor> i = extractors.iterator();
		if (i.hasNext()) {
			return i.next().getName(); // may change depending on configuration
										// (ex: "GroupName - /ha - name")
		} else {
			return "";
		}
	}

	/**
	 * Renderers may not draw the caption if not required. Normal case :
	 * required.
	 */
	public boolean isCaptionRequired() {
		return captionRequired;
	}

	public void setCaptionRequired(boolean v) {
		captionRequired = v;
	}

	/**
	 * Returns a String representation of this data block.
	 */
	public String toString() {
		return "DataBlock [" + getName() + "]";
	}

	@Override
	public void colorMoved(StepButton previousButton, StepButton newButton) {
		try {
			updateExtractors(previousButton.getStep(), newButton.getStep());

		} catch (Exception e) { // fc-20.1.2014
			renderer.security();
		}

	}

	@Override
	public void colorRemoved(StepButton stepButton) {
		int initialSize = extractors.size();
		for (Iterator<DataExtractor> i = extractors.iterator(); i.hasNext();) {
			DataExtractor extractor = i.next();
			if (extractor.getStep().equals(stepButton.getStep())) {
				i.remove();
			}
		}

		// Update or close renderer
		if (extractors.size() == 0) {
			renderer.close();
		} else if (extractors.size() != initialSize) {
			renderer.update();
		}

	}

	/**
	 * Check if calibration data is available If so, add a comboProperty to
	 * propose to select calibration data for some model Return a collection
	 * with the model names of the data block which propose calibration data
	 */
	public Collection<String> detectCalibrationData() { // fc - 12.10.2004
		Set<String> modelNames = new HashSet<String>(); // no duplicates
		for (Iterator<DataExtractor> i = getDataExtractors().iterator(); i.hasNext();) {
			DataExtractor e = i.next();
			if (e instanceof DECalibration)
				continue;

			GModel m = e.getStep().getProject().getModel();
			modelNames.add(m.getIdCard().getModelPackageName());
		}

		String extractorName = AmapTools.getClassSimpleName(extractorType);

		for (Iterator<String> i = modelNames.iterator(); i.hasNext();) {
			String modelName = i.next();

			Object[] modelAndExtractorNames = new String[2];
			modelAndExtractorNames[0] = modelName;
			modelAndExtractorNames[1] = extractorName;
			DECalibration c = new DECalibration(this, modelName, extractorName);
			if (!c.matchWith(modelAndExtractorNames)) {
				i.remove(); // no calibration data found for this model name
			}
		}

		if (modelNames.isEmpty())
			return modelNames; // no calibration data found

		// Log.println (Log.INFO, "DataExtractor.detectCalibrationData ()",
		// "DataExtractor found calibration data for: "
		// + AmapTools.toString (modelNames));

		return modelNames;
	}

	// UNUSED: all extractors in this data block may be synchronised on
	// different steps
	/**
	 * Step accessor.
	 */
	// public Step getStep () {
	// return step;
	// }

}
