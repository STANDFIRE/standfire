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
package capsis.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jeeb.lib.util.Disposable;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.StringUtil;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.CompatibilityManager;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.projectmanager.ButtonColorer;
import capsis.commongui.projectmanager.Current;
import capsis.commongui.projectmanager.ProjectManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.extension.AbstractDiagram;
import capsis.extensiontype.DataBlock;
import capsis.extensiontype.ExtractorGroup;
import capsis.extensiontype.StandViewer;
import capsis.gui.selectordiagramlist.ToolList;
import capsis.kernel.GModel;
import capsis.kernel.Step;
import capsis.util.SwingWorker3;

/**
 * A Selector panel with tabs inside for each type of linked extensions. Click
 * on the name of an extension opens the extension on current step button.
 * 
 * @author F. de Coligny - march 2003 - reviewed on 9.1.2004
 */
public class Selector extends JPanel implements /* SMListener, */ChangeListener, Disposable, Repositionable, Listener {

	private static final long serialVersionUID = 1L;

	private SelectorModel selectorModel;
	private ProjectManager projectManager;

	private JTabbedPane tabs;
	private Map<String, ImageIcon> icons;
	private Map<String, Map<String, JComponent>> memory; // Map: moduleName ->
															// Map of panels
															// (one for
															// each type of
															// extension)

	// save profil : model -> weakmap<component, classname>
	Map<String, Map<Object, String>> viewByModel = new HashMap<String, Map<Object, String>>();

	private String lastSelection;

	/**
	 * Constructor for Selector.
	 */
	public Selector(ProjectManager projectManager) {
		super();

		this.projectManager = projectManager;
		// projectManager.addListener (this); // listening to ScenarioManager
		selectorModel = new SelectorModel();

		tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // fc - 2.4.2003
		tabs.addChangeListener(this);

		memory = new HashMap<String, Map<String, JComponent>>(); // will
																	// memorize
																	// panels to
																	// save time

		// Icons preparation
		ImageIcon viewerIcon = IconLoader.getIcon("zoom_16.png");
		ImageIcon extractorIcon = IconLoader.getIcon("charts_16.png");
		ImageIcon extractorGroupIcon = IconLoader.getIcon("viewers_16.png");
		icons = new HashMap<String, ImageIcon>();
		icons.put(CapsisExtensionManager.STAND_VIEWER, viewerIcon);
		icons.put(CapsisExtensionManager.DATA_EXTRACTOR, extractorIcon);
		icons.put(CapsisExtensionManager.EXTRACTOR_GROUP, extractorGroupIcon);

		// fc-25.8.2011 - trying to fine tune the main frame (few pixels
		// missing)
		// this.setOpaque (true);
		// this.setBackground (Color.RED);
		tabs.setBorder(null);

		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);

		Current.getInstance().addListener(this);

		reset();

		reposition();
	}

	/**
	 * Update for the given model : remove and reselect.
	 */
	public void update(String modelPackageName) {

		memory.remove(modelPackageName);
		selectorModel.remove(modelPackageName);

		GModel currentModel = Current.getInstance().getProject().getModel();
		if (currentModel.getIdCard().getModelPackageName().equals(modelPackageName)) {
			lastSelection = null; // force re-selection
			select(currentModel);
		}
	}

	/**
	 * General update : forget memories and reselect.
	 */
	public void update() {

		memory.clear();
		selectorModel.clearMemory();

		GModel currentModel = Current.getInstance().getProject().getModel();
		lastSelection = null; // force re-selection
		select(currentModel);

	}

	/**
	 * Selection for the given model Support select (null) -> last project was
	 * closed, set selector "empty".
	 */
	private void select(GModel model) {
		if (model == null) {
			reset();
			return;
		}

		// We want the matchWith methods to be called again, delete the cache
		// fc-14.10.2013
		CompatibilityManager cm = CapsisExtensionManager.getInstance().getCompatibilityManager();
		cm.clearMatchMap();

		// Not empty
		final String fModuleName = model.getIdCard().getModelPackageName();

		if (fModuleName.equals(lastSelection)) {
			return;
		}

		lastSelection = fModuleName;

		StatusDispatcher.print(Translator.swap("Selector.updatingSelectorForModule") + " " + fModuleName + "...");
		tabs.setEnabled(false);

		final int fSelectedIndex = Math.max(0, tabs.getSelectedIndex()); // first
																			// time,
																			// index
																			// =
																			// -1

		final Selector fSelector = this;
		final GModel fModel = model;
		final Map<String, Map<String, JComponent>> fMemory = memory;
		final SelectorModel fSelectorModel = selectorModel;

		SwingWorker3 worker = new SwingWorker3() {

			// Runs in new Thread
			public Object construct() {

				// Build and memorize panels for module (if not yet built)
				if (!fMemory.containsKey(fModuleName)) {

					for (String type : fSelectorModel.getConsideredExtensionTypes()) {

						Map<String, String> tools = fSelectorModel.getTools(fModel, type);

						JComponent panel = new ToolList(fSelector, fModel, tools, type);
						// Type is for memo

						Map<String, JComponent> panels = (Map) fMemory.get(fModuleName);
						if (panels == null) {
							panels = new HashMap<String, JComponent>();
							fMemory.put(fModuleName, panels);
						}
						panels.put(type, panel);
					}
				}
				return null;
			}

			// Runs in dispatch event thread when construct is over
			public void finished() {

				// Update tabs with panels in memory
				tabs.removeAll(); // remove old tabs
				for (String extensionType : selectorModel.getConsideredExtensionTypes()) {

					Map<String, JComponent> panels = memory.get(fModuleName);
					JComponent panel = (JComponent) panels.get(extensionType);

					Icon icon = (Icon) icons.get(extensionType);
					if (icon != null) {
						tabs.addTab("", icon, panel, Translator.swap(extensionType));
					} else {
						tabs.addTab(Translator.swap(extensionType), null, panel);
					}
				}
				tabs.setSelectedIndex(fSelectedIndex);
				tabs.setEnabled(true);

				// Re-validate user interface
				revalidate();

				// fc - 16.1.2004
				// this process may have been long, maybe current scenario has
				// changed (during
				// session
				// opening, when many scenarios are opened, scenario selection
				// changes several
				// times)
				// check and reselect the good one if needed
				try {
					GModel currentModel = Current.getInstance().getProject().getModel();
					if (!currentModel.getClass().equals(fModel.getClass())) {
						// ~ System.out.println
						// ("--> Selector.reselect... "+currentModel);
						select(currentModel);
					}
				} catch (Exception e) {
				}

			}

		};
		worker.start(); // start thread and continue
	}

	// No module is selected (project manager is empty)
	// Selector is "empty"
	//
	public void reset() {
		// Empty representation
		int selectedIndex = 0;
		try {
			selectedIndex = new Integer(Settings.getProperty("capsis.selector.selected.tab", 0)).intValue();
		} catch (Exception e) {
		}

		tabs.removeAll(); // remove old tabs
		for (String extensionType : selectorModel.getConsideredExtensionTypes()) {

			JPanel panel = new JPanel();
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);

			Icon icon = (Icon) icons.get(extensionType);
			if (icon != null) {
				tabs.addTab("", icon, panel, Translator.swap(extensionType));
			} else {
				tabs.addTab(Translator.swap(extensionType), null, panel);
			}
		}
		tabs.setSelectedIndex(selectedIndex);

		lastSelection = "";

		// selectorModel.clearMemory (); // This memory may disturb when
		// compatibility options vary
		// at runtime (GraphicalExtensionManager)

		revalidate();
	}

	/**
	 * Open the given extension on current step button.
	 */
	public void createTool(String toolClassName, String type) {
		createTool(toolClassName, type, null);
	}

	/**
	 * Open the given extension on current step button. rendererClassName is
	 * optional, can be null. If not null, this renderer will be opened instead
	 * of the default renderer for the extractor.
	 */
	public void createTool(String toolClassName, String type, String rendererClassName) {

//		System.out.println("Selector.createTool (), toolClassName: " + toolClassName + " type: " + type + "...");

		Step step = Current.getInstance().getStep();
		StepButton sb = projectManager.getStepButton(step);
		GModel model = step.getProject().getModel();

		if (type.equals(CapsisExtensionManager.STAND_VIEWER)) {

			try {

				if (!sb.isColored())
					ButtonColorer.getInstance().newColor(sb);

				StandViewer viewer = (StandViewer) CapsisExtensionManager.getInstance().instantiate(toolClassName);

				viewer.init(model, step, sb);

				viewer.activate(); // init processing (Extension)
				recordView(model, toolClassName, viewer);

			} catch (Exception e) {
				Log.println(Log.ERROR, "SelectorPanel.addViewer ()", "Exception caught:", e);
				MessageDialog.print(this, Translator.swap("SelectorPanel.couldNotCreateViewerSeeLogForDetails"));

			}

		} else if (type.equals(CapsisExtensionManager.DATA_EXTRACTOR)) {

			if (!sb.isColored())
				ButtonColorer.getInstance().newColor(sb);

			// rendererClassName is optional, used for diagramLists
			// fc-10.12.2015
			DataBlock c = new DataBlock(toolClassName, sb.getStep(), rendererClassName);
			recordView(model, toolClassName, c);

		} else if (type.equals(CapsisExtensionManager.EXTRACTOR_GROUP)) {

			if (!sb.isColored())
				ButtonColorer.getInstance().newColor(sb);

			ExtractorGroup g = (ExtractorGroup) CapsisExtensionManager.getInstance().instantiate(toolClassName);
			g.init(step);

			recordView(model, toolClassName, g);

		}

	}

	/**
	 * Some tab was clicked, memo which one (ChangedListener interface).
	 */
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(tabs)) {
			int selectedIndex = Math.max(0, tabs.getSelectedIndex()); // index
																		// may
																		// be -1
			Settings.setProperty("capsis.selector.selected.tab", "" + selectedIndex);
		}
	}

	/**
	 * Layout the selector (Repositionable interface).
	 */
	public void reposition() {
		Pilot.getPositioner().layOut(this);
	}

	/**
	 * Title for embedder title bar (...)
	 */
	public String getTitle() {
		return Translator.swap("Shared.selector");
	}

	/**
	 * Dispose : manage embedder dispose (Embedded interface).
	 */
	@Override
	public void dispose() {

		Pilot.getPositioner().remove(this);
	}

	@Override
	public void setLayout(Positioner p) {
		p.layoutComponent(this);
	}

	@Override
	public void somethingHappened(ListenedTo l, Object param) {
		if (param.equals(Current.FORCE_UPDATE)) {

			update();

		} else if (param.equals(Current.STEP_CHANGED)) {

			try {
				Step s = Current.getInstance().getStep();
				GModel m = s.getProject().getModel();
				select(m); // trigger selection for this model
			} catch (Exception e) {
				select(null); // no project
			}

		}
	}

	protected void recordView(GModel m, String cn, Object obj) {
		String className = m.getClass().getName();

		if (!viewByModel.containsKey(className)) {
			// weak map : entry is removed if component is destroyed
			Map<Object, String> s = new WeakHashMap<Object, String>();
			viewByModel.put(className, s);
		}

		viewByModel.get(className).put(obj, cn);
	}

	/** Save view in settings */
	public void saveView(GModel m) {

		System.gc(); // remove weak references
		String className = m.getClass().getName();
		if (viewByModel.containsKey(className)) {

			Map<Object, String> s = viewByModel.get(className);

			String out = StringUtil.join(s.values(), ":");
			Settings.setProperty(className + ".view", out);

		}
	}

	/** load view from settings */
	public void restoreView(GModel m) {

		String className = m.getClass().getName();
		String in = Settings.getProperty(className + ".view", "");
		String[] vals = in.split(":");

		ExtensionManager em = CapsisExtensionManager.getInstance();

		for (String s : vals) {

			String t;
			try {
				t = em.getType(Class.forName(s));
				createTool(s, t, null);

			} catch (ClassNotFoundException e) {
				Log.println(Log.ERROR, "Selector.restoreView", "Cannot restore view", e);

			}
		}

	}

}
