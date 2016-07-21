/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
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

package capsis.kernel;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.NTree;
import jeeb.lib.util.Node;
import jeeb.lib.util.Settings;
import jeeb.lib.util.TicketDispenser;
import capsis.kernel.automation.Automation;
import capsis.kernel.extensiontype.Intervener;
import capsis.kernel.extensiontype.Memorizer;
import capsis.kernel.extensiontype.OFormat;

import com.thoughtworks.xstream.XStream;

/**
 * A Project in the Capsis kernel. A project is connected to a model (GModel)
 * and contains at least a root step carrying the initial scene (GScene) of the
 * simulation. There may be several Projects in the Session.
 * 
 * @author F. de Coligny - may 1999, september 2010
 */
public class Project extends NTree implements ListenedTo {

	private static final long serialVersionUID = 3L;
	/** Project name */
	private String name;
	/** Associated model */
	private GModel model;
	/** Tells if the project is saved / was once saved under this name */
	private boolean saved;
	/** Set true on open and save: means 'already saved under this fileName' */
	private boolean wasSaved;
	/** Associated memorizer, decides what steps will be memorized when added */
	private Memorizer memorizer;
	/** Visibility frequency, some steps may be temporarily hidden */
	private int visiFreq;
	/** File name of the project */
	private String fileName = "";
	/** The Steps in a Project have unique ids */
	private TicketDispenser stepIdDispenser;
	/** The project automation */
	transient private Automation automation;
	/** The automations are now optional (see GModel to activate them) */
	private boolean automationEnabled; // fc-28.11.2012
	/** A map stepId -> Step */
	transient HashMap<Integer, WeakReference<Step>> stepMap;
	/** The last Step created in the Project */
	transient private Step lastStep;
	/** Some objects may listen to the Project */
	transient private List<Listener> listeners; // ListenedTo interface

	/**
	 * Constructor
	 */
	public Project(String name, GModel model) {

		// fc-9.11.2015 Project name must be unique in the session
		setName(name);
		// this.name = name;

		this.model = model;
		setRoot(null);

		saved = false;
		wasSaved = false;

		stepIdDispenser = new TicketDispenser();

		// fc-28.11.2012 automations are now optional, see GModel for decision
		automationEnabled = model.enablesAutomation();

		String modelName = model.getIdCard().getModelPackageName();
		String memo = Settings.getProperty(modelName + ".last.memorizer", (String) null);
		memorizer = MemorizerFactory.createMemorizer(memo);

		if (automationEnabled) {
			automation = new Automation(name, modelName);
			automation.memorizer = memorizer.toString();
		}

		setVisibilityFrequency(model.getDefaultStepVisibilityFreq());
	}

	public boolean isAutomationEnabled() { // fc-28.11.2012
		return automationEnabled;
	}

	/**
	 * Convenient accessor for the project's root: casted from Node to Step.
	 */
	public Step getRoot() {
		return (Step) super.getRoot();
	}

	// Project serialization uses these specific methods

	/**
	 * Specific serialization writeObject () method.
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {

		out.defaultWriteObject();

		if (automationEnabled) {

			try {
				XStream xstream = new XStream();
				xstream.setMode(XStream.ID_REFERENCES);
				out.writeObject(xstream.toXML(automation));

			} catch (OutOfMemoryError e) {
				Log.println(Log.ERROR, "Project.writeObject ()",
						"Out of memory: the Project automation was not correctly saved", e);
			}

		}

	}

	/**
	 * Specific serialization readObject () method.
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();

		if (automationEnabled) {

			try {
				XStream xstream = new XStream();
				xstream.setMode(XStream.ID_REFERENCES);
				automation = (Automation) xstream.fromXML((String) in.readObject());

			} catch (OutOfMemoryError e) {
				Log.println(Log.ERROR, "Project.readObject ()",
						"Out of memory: the Project automation was not correctly loaded", e);
			}

		}

	}

	// Main properties

	/**
	 * Project name.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		
		// fc-9.11.2015 Make sure the project name is unique in the session
		// Having duplicate project names results in strange graphs
		Session s = Engine.getInstance().getSession();

		String memoName = name;
		int k = 1;
		boolean check = true;

		while (check) {
			check = false;
			for (Project p : s.getProjects()) {
				if (p.getName().equals(name)) {
					name = memoName + "(" + k + ")";
					check = true;
					k++;
				}
			}
		}
		// fc-9.11.2015

		this.name = name;
	}

	/**
	 * Automation of the project.
	 */
	public Automation getAutomation() {
		return automation; // fc-28.11.2012 Automations are now optional, may
							// return null
	}

	/**
	 * Returns the fileName of the project.
	 */
	public String getFileName() {
		if (!fileName.equals("")) {
			return fileName;
		} else {
			return Settings.getProperty("capsis.project.path", (String) null) + File.separator + name;
		}
	}

	/**
	 * Sets the file name for this project. Optional, if not set, getFileName ()
	 * will return "capsis.project.path / name".
	 */
	public void setFileName(String v) {
		this.fileName = v;
	}

	/**
	 * Returns the model linked to this Project. This model has created the
	 * initial Step and contains the method to create the new Steps.
	 */
	public GModel getModel() {
		return model;
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean b) {
		saved = b;
	}

	public boolean wasSaved() {
		return wasSaved;
	}

	public void setWasSaved(boolean b) {
		wasSaved = b;
	}

	// The memorizer will decide which new Steps must be kept in memory

	/**
	 * Sets the Project Memorizer.
	 */
	public void setMemorizer(String m) {
		setMemorizer(MemorizerFactory.createMemorizer(m));
	}

	/**
	 * Sets the Project Memorizer.
	 */
	public void setMemorizer(Memorizer m) {
		memorizer = m;

		String modelName = model.getIdCard().getModelPackageName();

		String memostr = memorizer.toString();
		Settings.setProperty(modelName + ".last.memorizer", memostr);

		if (automationEnabled) {
			automation.memorizer = memostr;
		}
	}

	/**
	 * Returns the current Memorizer of the Project.
	 */
	public Memorizer getMemorizer() {
		return memorizer;
	}

	/**
	 * Redefines visibility for a given frequency
	 */
	public void setVisibilityFrequency(int f) {
		visiFreq = f;
		updateVisibility(true);
	}

	/**
	 * Recalculates Steps visibility. If force is false, recalculates only for
	 * Steps which visibility is undefined .
	 */
	public void updateVisibility(boolean force) {
		setVisibility(visiFreq, 0, getRoot(), force);
	}

	/**
	 * Makes sure the final Steps ('leaves': no son) are set visible. May be
	 * used after a project evolution cancellation, when everything is not in
	 * order.
	 */
	public void ensureAllTheFinalStepsAreVisible() {
		updateVisibility(true);
	}

	// Steps management

	/**
	 * Returns the last step that was created.
	 */
	public Step getLastStep() {
		return lastStep;
	}

	/**
	 * Returns the step with the given id. This method may return null.
	 */
	public Step getStep(Integer id) {

		if (stepMap != null && stepMap.containsKey(id)) {
			return stepMap.get(id).get();
		}

		return null;
	}

	/**
	 * Returns the number of steps in the whole project
	 */
	public int getSize() {
		int n = 0;
		for (Iterator<Node> i = this.preorderIterator(); i.hasNext();) {
			i.next();
			n++;
		}
		return n;
	}

	/**
	 * Returns a list containing all the steps from the root Step to the given
	 * Step.
	 */
	public Vector<Step> getStepsFromRoot(Step step) {
		Vector<Step> v = new Vector<Step>();
		// Vector<Step> steps = new Vector<Step> ();

		// From step to root (backwards)
		while (step != null) {
			v.add(step);
			step = (Step) step.getFather();
		}

		// // 2. From root to step: reverse
		// for (int i = v.size ()-1; i >= 0; i--) {
		// steps.add (v.get (i));
		// }

		// Reverse the list
		Collections.reverse(v);

		return v;
	}

	/**
	 * Ties a Step in a project. Used by the Memorizer.
	 */
	public void tieStep(Step fatherStep, Step newStep) {
		// No fatherStep: new step is the project root
		if (fatherStep == null) {
			this.setRoot(newStep);

			// fatherStep != null: insert new step as a new son for it
		} else {
			fatherStep.insertSon(newStep);
		}
		newStep.setTied(true);

	}

	/**
	 * Replaces a Step in the project. Used by the Memorizer.
	 */
	public void replaceStep(Step fatherStep, Step newStep) {
		// No fatherStep: new step is the project root
		if (fatherStep == null || fatherStep.isRoot()) {
			this.setRoot(newStep);
		} else {
			fatherStep.replace(newStep);
		}
		newStep.setTied(true);
		fatherStep.setTied(false);
	}

	/**
	 * Instantiates a step with the correct id. The Step is not tied yet in the
	 * Project.
	 */
	public Step createStep(GScene scene) {

		if (stepMap == null) {
			stepMap = new HashMap<Integer, WeakReference<Step>>();
		}

		Step s = new Step(this, scene, stepIdDispenser.next());
		stepMap.put(s.getId(), new WeakReference<Step>(s));
		return s;

	}

	/**
	 * Creates and adds a new Step in the Project to carry the given scene.
	 */
	public Step processNewStep(Step father, GScene scene, String reason) throws InterruptedException {
		Step s = processNewStep(father, scene);
		s.reason = reason;
		return s;
	}

	/**
	 * Creates and adds a new Step in the Project to carry the given scene.
	 */
	synchronized public Step processNewStep(Step father, GScene scene) throws InterruptedException {

		// Task cancellation hook
		// if (Thread.currentThread ().isInterrupted ()) { return father; } //
		// NO
		Thread.sleep(1L); // YES fc-28.5.2014 (throws the InterruptedException)

		// Step is not tied yet in project
		Step stp = createStep(scene);

		// The Memorizer will decide to tie the step or not
		Memorizer m = getMemorizer();
		m.memorize(this, father, stp);

		this.setSaved(false);

		// Notify listeners of the step addition
		// fc-29.9.2010: Use jeeb.lib.util.Listener
		Object[] param = new Object[] { "stepAdded", stp.getProject(), stp };
		tellSomethingHappened(param);

		// This is important (waits for 1 millisecond)
		// Calling this method will throw an InterruptedException
		// if the thread was interrupted - helps canceling the long tasks
		Thread.sleep(1L);

		return stp;
	}

	/**
	 * Deletes a visible Step.
	 */
	public Step processDeleteStep(Step stp) {
		Project p = this;
		p.setSaved(false);
		Step currentStep = (Step) stp.removeVisible();

		// Notify listeners of the steps deletion
		// fc-29.9.2010: Use jeeb.lib.util.Listener
		Object[] param = new Object[] { "stepsDeleted", p, stp, currentStep }; // project,
																				// main
																				// deleted
																				// step,
																				// new
																				// current
																				// step
																				// after
																				// deletion
		tellSomethingHappened(param);

		return currentStep;
	}

	// Project initialization

	/**
	 * Initialize calls the initialize () method of the linked model. The
	 * InitialParameters object contains the initialScene to be linked to the
	 * root Step of the Project.
	 */
	public Step initialize(InitialParameters ip) throws Exception {
		try {

			GScene initScene = ip.getInitScene();

			// Legacy case: one single initScene
			if (initScene.getStep() == null) {

				// The initial scene is linked to the root Step before calling
				// initializeModel ()
				lastStep = processNewStep(null, initScene, "initial Step");
				lastStep.setVisible(true);

				// fc-24.1.2011 - jeeb.lib.format.OPSLoader
				// More recent case: initScene may be connected to a Step with
				// other Steps linked
				// -> replace these Steps Project instance by 'this' project
			} else {

				Step rootStep = initScene.getStep();
				Project dummyProject = rootStep.getProject();

				// Update the Steps' project reference
				for (Iterator i = dummyProject.preorderIterator(); i.hasNext();) {
					Step s = (Step) i.next();
					s.setProject(this);
					s.setVisible(true); // all steps visible
				}
				// Set the project root step
				this.setRoot(rootStep);

				// In this case, we consider the rootStep
				// Maybe the loaded project contains several scenarios -> no
				// clear 'last step'
				lastStep = rootStep;
			}

			lastStep = model.getRelay().initializeModel(ip);

			// Set the initialParameters object as the model settings
			if (ip instanceof AbstractSettings) {
				model.setSettings((AbstractSettings) ip);
			}

			// Update the automation
			if (automationEnabled) {
				automation.addEvent(-1, ip, "Initial Parameters");
			}

			return lastStep;

		} catch (Exception e) {

			Log.println(Log.ERROR, "Project.initialize ()", "Exception in initialize ()", e);
			// Try to remove the Project from the Session
			try {
				Engine.getInstance().getSession().removeProject(this);
			} catch (Exception e2) {
			}
			throw new Exception("Exception in initialize ()", e);
		}
	}

	// Evolution

	/**
	 * Runs an Evolution stage from the given step to the limit found in the
	 * EvolutionParameters object. Relies on the processEvolution () method of
	 * the connected Model.
	 */
	public Step evolve(Step stp, EvolutionParameters ep) throws Exception {
		try {
			memorizer.reinit();

			// Triggers the evolution method in the connected model
			// Note: this will call Project.processNewStep () maybe several
			// times
			lastStep = model.getRelay().processEvolution(stp, ep);

			if (lastStep == null) {
				throw new Exception("processEvolution () returned null: " + model);
			}

			// Take care of Steps visibility
			updateVisibility(false);

			// Update the automation
			if (automationEnabled) {
				automation.addEvent(stp.getId(), ep, "Evolution " + stp.getName());
			}

			return lastStep;

		} catch (InterruptedException e) {
			// Do nothing here: tasks may be interrupted
			throw e;

		} catch (Exception e) {
			Log.println(Log.ERROR, "Project.evolve ()", "Exception in evolve ()", e);
			throw new Exception("Exception in evolve ()", e);
		}
	}

	// Intervention

	/**
	 * Runs an already set intervener on the given step and creates a new Step
	 * to carry the resulting Scene. Note: the intervener must have been built
	 * on step.getInterventionBase () i.e. a copy of the (unchanged) Scene under
	 * the original step.
	 */
	public Step intervention(Step stp, Intervener intervener, boolean recAutomation) throws Exception {

		memorizer.reinit();

		// Apply the intervener, get the resulting Scene
		GScene scene = (GScene) intervener.apply();

		// Launch the optional post processing described in the linked module
		model.getRelay().processPostIntervention(scene, stp.getScene());

		// Attach new Scene in the project after the given step
		lastStep = processNewStep(stp, scene, intervener.toString());
		lastStep.setVisible(true);

		memorizer.reinit();

		// Update the automation
		if (automationEnabled && recAutomation) {
			automation.addEvent(stp.getId(), intervener, "Intervention " + stp.getName());
		}

		return lastStep;
	}

	// Export

	/**
	 * Exports the Scene under the given Step with the given output format into
	 * a file with the given fileName.
	 */
	public Step export(Step stp, OFormat export, String fileOut) throws Exception {

		export.save(fileOut);

		if (automationEnabled) {

			// Update the automation
			Automation.Event e = automation.addEvent(stp.getId(), export, "Export " + stp.getName());
			if (e != null) {
				e.parameters.put("filename", fileOut);
			}

		}

		return stp;
	}

	// Other methods

	/**
	 * String representation for a Project
	 */
	public String toString() {
		return "Project_" + getName();
	}

	/**
	 * Adds a listener to this object. ListenedTo interface.
	 */
	public void addListener(Listener l) {
		if (listeners == null) {
			listeners = new ArrayList<Listener>();
		}
		listeners.add(l);
	}

	/**
	 * Removes a listener to this object. ListenedTo interface.
	 */
	public void removeListener(Listener l) {
		if (listeners == null) {
			return;
		}
		listeners.remove(l);
	}

	/**
	 * Notifies all the listeners by calling their somethingHappened
	 * (listenedTo, param) method. ListenedTo interface.
	 */
	public void tellSomethingHappened(Object param) {
		if (listeners == null) {
			return;
		}
		for (Listener l : listeners) {
			l.somethingHappened(this, param);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		model.clear();
	}
	
	
	/**
	 * Defines visibility depending on a frequency. If force is false, set only
	 * undefined step.
	 */
	public void setVisibility(int freq, int current_index, Node nod, boolean force) {

		if (nod == null)
			return;
		boolean v = (nod.getLeftSon() == null || // force visibility for last
													// class
		(freq > 0 && (current_index % freq) == 0)); // if freq <=0 visi is false

		// fc-12.12.2013 option: intervention steps are always visible
		boolean starsAreVisible = Settings.getProperty("Project.interventionStepsAreAlwaysVisible", true);
		boolean star = ((Step) nod).getScene().isInterventionResult();
		if (star && starsAreVisible)
			v = true;

		if (freq >= 0 && (force || nod.isVisibleUndefined())) {
			nod.setVisible(v);
		}

		setVisibility(freq, current_index + 1, nod.getLeftSon(), force);
		setVisibility(freq, current_index, nod.getRightBrother(), force);
	}

}
