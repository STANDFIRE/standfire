package capsis.lib.fire.standviewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import jeeb.lib.sketch.gui.Panel3D;
import jeeb.lib.sketch.gui.SketcherManager;
import jeeb.lib.sketch.item.Grid;
import jeeb.lib.sketch.kernel.AddInfo;
import jeeb.lib.sketch.kernel.SimpleAddInfo;
import jeeb.lib.sketch.kernel.SketchController;
import jeeb.lib.sketch.kernel.SketchFacade;
import jeeb.lib.sketch.kernel.SketchLinkable;
import jeeb.lib.sketch.kernel.SketchLinker;
import jeeb.lib.sketch.scene.gui.TreeView;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.sketch.util.SketchTools;
import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.projectmanager.StepButton;
import capsis.extension.AbstractStandViewer;
import capsis.gui.DialogWithClose;
import capsis.kernel.GModel;
import capsis.kernel.PathManager;
import capsis.kernel.Relay;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.FiStatePanel;

/**
 * A 3D stand viewer with a lateral state panel for the FuelManager and
 * StandFire projects.
 * 
 * @author F. de Coligny - January 2015
 */
public class FiStandViewer3D extends AbstractStandViewer implements SketchController, ActionListener {

	static {
		Translator.addBundle("capsis.lib.fire.FiLabels");
	}

	static public String NAME = "FiStandViewer3D";
	static public String DESCRIPTION = "FiStandViewer3D.description";
	static public String AUTHOR = "F. de Coligny";
	static public String VERSION = "1.0";

	static {
		Translator.addBundle("capsis.extension.standviewer.FiStandViewer3D");
	}

	private FiModel fiModel;
	private FiStand fiStand;

	private FiStatePanel statePanel;

	private JTextField statusBar;
	private SketchFacade sketchFacade;
	private SceneModel sceneModel;
	private Panel3D panel3D;
	private SketcherManager sketcherManager;
	private JButton preferences;
	private JComponent preferencePanel;
	private AmapDialog preferenceDialog;

	/**
	 * Initialisation of the viewer on the given step.
	 */
	@Override
	public void init(GModel model, Step s, StepButton sb) throws Exception {
		super.init(model, s, sb);

		fiModel = (FiModel) model;
		fiStand = (FiStand) step.getScene();

		createUI();

		update(step);
	}

	/**
	 * Extension dynamic compatibility mechanism. This method checks if the
	 * extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			return referent instanceof FiModel;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiStandViewer3D.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

	}

	// Needed for GUI mode (user synchronization)
	public void update(StepButton sb) {
		super.update(sb);
		update(sb.getStep());
	}

	// Needed for script mode
	public void update(Step step) {

		this.step = step;
		fiStand = (FiStand) step.getScene();

		SketchLinker linker = null;

		// This technical section tries to find a reference to the sketchLinker
		// object of the current model. This SketchLinker knows how to draw the
		// model scenes in 3D (see the jeeb.lib.sketch 3D library)
		// fc-19.1.2015
		try {
			// Find the sketchLinker associated to this scene
			// we follow the Capsis guaranteed accessors
			GModel model = fiStand.getStep().getProject().getModel();
			Relay relay = model.getRelay();
			linker = ((SketchLinkable) relay).getSketchLinker(); // might fail
																	// in script
																	// mode

		} catch (Exception e) {
			try {
				// Try to create a linker relying on the Capsis conventions
				String linkerClassName = model.getIdCard().getModelPackageName() + ".sketch."
						+ model.getIdCard().getModelPrefix() + "SketchLinker";

				Class klass = Class.forName(linkerClassName);
				Class[] argTypes = { GModel.class };
				Constructor constructor = klass.getDeclaredConstructor(argTypes);
				Object[] arguments = { model };
				linker = (SketchLinker) constructor.newInstance(arguments);
			} catch (Exception e2) {
				Log.println(Log.ERROR, "FiStandViewer3D.update (Step)", "Could not get the sketchLinker", e2);
			}
		}

		// Update the 3D view
		try {

			// Ask the linker to 'draw the scene'
			linker.updateSketch(fiStand, sceneModel);

			// Reset selection / clear undo stack
			sceneModel.getUndoManager().undoableResetSelection(this);
			sceneModel.getUndoManager().clearMemory();
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiStandViewer3D.update (StepButton)", "Error while drawing the 3D scene", e);
		}

		// Update the state panel
		statePanel.update(fiStand);

		// revalidate();
		// repaint();

	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		if (evt.getSource().equals("Status")) {
			String message = (String) evt.getActionCommand();
			statusBar.setText(message);

		} else if (evt.getSource().equals(preferences)) {

			if (preferenceDialog == null) {
				boolean modal = false;
				boolean withControlPanel = false;
				preferenceDialog = new DialogWithClose(this, preferencePanel, Translator.swap("Shared.preferences"),
						modal, withControlPanel, true, true); // memoSize =
																// true,
																// memoLocation
																// = true
			} else {
				// Make it visible / invisible...
				preferenceDialog.setVisible(!preferenceDialog.isVisible());
			}

		}

	}

	/**
	 * Build a preference panel for the ObjectViewer
	 */
	private void createPreferencePanel(JToolBar toolBar) {
		if (preferencePanel != null) {
			return;
		}

		JTabbedPane tabs = new JTabbedPane();

		tabs.add(sketcherManager.getName(), sketcherManager);

		// Add a table
		TreeView treeView = sketchFacade.getTreeView();
		tabs.add(treeView.getName(), treeView);

		preferencePanel = tabs;

		ImageIcon icon = IconLoader.getIcon("option_24.png");
		preferences = new JButton(icon);
		preferences.setToolTipText(Translator.swap("Shared.preferences"));
		preferences.addActionListener(this);
		toolBar.add(preferences);

	}

	/**
	 * Creation of the user interface: a 3D panel and a lateral state panel.
	 */
	private void createUI() {

		// Left: a Panel3D
		JPanel left = new JPanel(new BorderLayout());
		try {

			statusBar = new JTextField();
			statusBar.setEditable(false);

			JDialog topDialog = null; // we are in a JPanel...

			sketchFacade = new SketchFacade(topDialog, PathManager.getDir("etc"));
			sceneModel = sketchFacade.getSceneModel();
			sceneModel.setEditable(this, true); // possible to add items in it
												// (by program)
			panel3D = sketchFacade.getPanel3D();
			sketchFacade.addStatusListener(this);

			JPanel p = panel3D.getPanel3DWithToolBar(BorderLayout.EAST, true, false, false, false, false);
			panel3D.setPovCenter(0, 0, 0);

			// fc-3.6.2014 change initial zoom factor
			panel3D.setZoomFactor(0.2f);

			// fc-19.1.2015 Needed to build the preference panel below
			sketcherManager = sketchFacade.getSketcherManager();

			left.add(p, BorderLayout.CENTER);
			left.add(statusBar, BorderLayout.SOUTH);

			// Add a preferences button in the toolbar of the Panel3D
			createPreferencePanel(panel3D.getToolBar());

			// Add a grid
			Grid grid = new Grid(); // Grid item
			AddInfo addInfo = new SimpleAddInfo(grid.getType(), SketchTools.inSet(grid));
			sceneModel.getUndoManager().undoableAddItems(this, addInfo);

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiStandViewer3D.createUI ()", "Could not build the Panel3D", e);
		}

		// Right: a state panel
		statePanel = new FiStatePanel(sceneModel, fiModel);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, statePanel);
		split.setResizeWeight(0.5);

		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);

	}

}
