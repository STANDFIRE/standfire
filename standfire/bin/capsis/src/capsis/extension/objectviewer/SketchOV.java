/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
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
package capsis.extension.objectviewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
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
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.gui.TreeView;
import jeeb.lib.sketch.util.SketchTools;
import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Disposable;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.Log;
import jeeb.lib.util.Namable;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeAvatar;
import capsis.extension.AbstractObjectViewer;
import capsis.gui.DialogWithClose;
import capsis.kernel.Cell;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Relay;
import capsis.kernel.Step;

/**
 * A 3D viewer for trees based on the sketch library. Based on the SketchLinker
 * of the module. Compatible only if a SketchLinker is found.
 * 
 * @author F. de Coligny - september 2009
 */
public class SketchOV extends AbstractObjectViewer implements Disposable, ActionListener, SketchController, Namable {

	static {
		Translator.addBundle("capsis.extension.objectviewer.SketchOV");
	}
	static public final String NAME = Translator.swap("SketchOV");
	static public final String DESCRIPTION = Translator.swap("SketchOV.description");
	static public final String AUTHOR = "F. de Coligny";
	static public final String VERSION = "1.0";

	// Main components of the 3D scene editor
	private SketchFacade sketchFacade;
	private SketchModel sceneModel;
	private Panel3D panel3D;
	private SketcherManager sketcherManager;

	private JTextField statusBar;
	private JButton preferences;
	private JComponent preferencePanel;
	// private JScrollPane controlPane; // fc - 20.9.2008
	private AmapDialog preferenceDialog;

	/**
	 * Default constructor.
	 */
	public SketchOV() {
	}

	public void init(Collection s) throws Exception {
		try {
			createUI();
			show(new ArrayList(s));

		} catch (Exception exc) {
			Log.println(Log.ERROR, "SketchOV.c ()", exc.toString(), exc);
			throw exc; // fc - 4.11.2003 - object viewers may throw exception
		}
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			// referent is a Collection, a candidate selection
			// Warning: there may be trees and cells in the referent collection
			// Do not match only if all the elements are trees of a given type
			// (cells also)

			// Look if the relay is SketchLinkable to get a SketchLinker
			SketchLinker linker = getSketchLinker((Collection) referent);
			return linker != null;

		} catch (Exception e) {
			Log.println(Log.ERROR, "SketchOV.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	/**
	 * Ask the SketchLinker to the relay of the module. We expect at least one
	 * instance of GTree in the candidateSelection. If not found (relay not
	 * instanceof SeketchLinkable...) returns null.
	 */
	static private SketchLinker getSketchLinker(Collection candidateSelection) {
		try {
			// 1. Get a tree
			Tree tree = null;
			for (Object o : candidateSelection) {
				if (o instanceof Tree) {
					tree = (Tree) o;
					break;
				}
			}

			// 1.1. Try with a tree avatar
			if (tree == null) {
				for (Object o : candidateSelection) {
					if (o instanceof TreeAvatar) {
						TreeAvatar a = (TreeAvatar) o;
						tree = a.getRealTree();
						break;
					}
				}

			}

			if (tree != null) {
				GScene stand = tree.getScene();
				if (stand != null) { // Jackpine has a special infrastructure
					Step step = stand.getStep();
					if (step != null) { // Jackpine has a special infrastructure
						GModel model = step.getProject().getModel();

						// fc-13.11.2014 added this hint to make the StandFire
						// interactive script with its 3D viewer, see SFModel
						if (model instanceof SketchLinkable) {
							SketchLinker linker = ((SketchLinkable) model).getSketchLinker();
							return linker;
						}

						Relay relay = model.getRelay();
						if (relay instanceof SketchLinkable) { // fc-13.11.2014 added this if (...) line
							SketchLinker linker = ((SketchLinkable) relay).getSketchLinker();
							return linker;
						}
					}
				}
			}

			// 2. Try with a cell
			Cell cell = null;
			for (Object o : candidateSelection) {
				if (o instanceof Cell) {
					cell = (Cell) o;
					break;
				}
			}
			if (cell != null) {
				GScene stand = cell.getPlot().getScene();
				Step step = stand.getStep();
				GModel model = step.getProject().getModel();
				Relay relay = model.getRelay();
				SketchLinker linker = ((SketchLinkable) relay).getSketchLinker();
				return linker;
			}

			// Could not find the relay
			return null;

		} catch (Exception e) {

			System.out.println("SketchOV.getSketchLinker(): could not get sketchLinker from: "
					+ AmapTools.toString(candidateSelection));
			e.printStackTrace(System.out);

			return null;
		}
	}

	/**
	 * Disposable.
	 */
	public void dispose() {
	}

	/**
	 * Some button was hit...
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals("Status")) { // fc - 1.6.2007
			String message = (String) evt.getActionCommand();
			statusBar.setText(message);

		} else if (evt.getSource().equals(preferences)) {

			// update controlPanel each time
			// if (controlPane != null) {
			// controlPane.getViewport ().setView (new JScrollPane
			// (AmapTools.createInspectorPanel (sceneModel)));
			// }

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
	 * OVSelector framework.
	 */
	public Collection show(Collection candidateSelection) {

		realSelection = updateUI(candidateSelection);

		return realSelection;
	}

	/**
	 * User interface definition.
	 */
	protected void createUI() {
		setLayout(new BorderLayout());

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

			sketcherManager = sketchFacade.getSketcherManager();

			add(p, BorderLayout.CENTER);
			add(statusBar, BorderLayout.SOUTH);

			// Add a preferences button in the toolbar of the Panel3D
			createPreferencePanel(panel3D.getToolBar());

			// Add a grid
			Grid grid = new Grid(); // Grid item
			AddInfo addInfo = new SimpleAddInfo(grid.getType(), SketchTools.inSet(grid));
			sceneModel.getUndoManager().undoableAddItems(this, addInfo);

		} catch (Exception e) {
			Log.println(Log.ERROR, "SketchOV.createUI ()", "Exception", e);
		}
	}

	/**
	 * Shows subject, returns what it effectively shown.
	 */
	protected Collection updateUI(Collection subject) {
		try {

			if (subject == null) {
				subject = new ArrayList();
			} // fc - 7.12.2007 - ovs should accept null subject

			// Ask the linker to 'draw the scene'
			SketchLinker linker = getSketchLinker(subject);
			linker.updateSketch(null, subject, sceneModel); // scene is null:
															// we do not
															// have the ref
															// here

			// Reset selection / clear undo stack
			sceneModel.getUndoManager().undoableResetSelection(this);
			sceneModel.getUndoManager().clearMemory();

			// Add a preferences button in the toolbar of the Panel3D
			// Moved here - fc - 22.5.2009
			// createPreferencePanel (panel3D.getToolBar ());

			// AutoCentering is now managed in Panel3D and occurs only once to
			// respect manual center changes // fc-7.8.2013
			// // Center correctly
			// Collection updatedItems = linker.getUpdatedItems();
			// boolean atTheGroundLevel = true;
			// Vertex3d sceneCenter = SketchTools.calculateCenter(updatedItems,
			// atTheGroundLevel);
			// panel3D.setPovCenter(sceneCenter);

			Collection accurateSelection = linker.getUpdatedUserObjects();
			return accurateSelection; // shown trees

		} catch (Exception e) {
			Log.println(Log.ERROR, "SketchOV.updateUI ()", "Caught an exception", e);
			statusBar.setText(Translator.swap("SketchOV.couldNotOpenSketcherSeeLog"));
		}
		return Collections.EMPTY_LIST; // showed nothing
	}

	/**
	 * Build a preference panel for the ObjectViewer
	 */
	private void createPreferencePanel(JToolBar toolBar) {
		if (preferencePanel != null) {
			return;
		}

		// try {
		JTabbedPane tabs = new JTabbedPane();

		tabs.add(sketcherManager.getName(), sketcherManager);

		// Add a table
		TreeView treeView = sketchFacade.getTreeView();
		tabs.add(treeView.getName(), treeView);

		preferencePanel = tabs;

		// } catch (Exception e) {
		// Log.println (Log.WARNING, "SketchOV.createPreferencePanel ()",
		// "Could not create the SketcherManager, used simple tabs instead", e);
		//
		// // If trouble, build simple tabs
		// JTabbedPane tabs = new JTabbedPane ();
		// boolean foundAtLeastOne = false;
		// Collection<Drawer3D> drawer3Ds = panel3D.getDrawer3Ds ();
		// for (Drawer3D d : drawer3Ds) {
		// if (d instanceof InstantConfigurable) {
		// InstantConfigurable ic = (InstantConfigurable) d;
		// tabs.add (ic.getName (), new JScrollPane (ic.getInstantPanel ()));
		// foundAtLeastOne = true;
		// }
		// }
		//
		// // Testing the SketchModel - trace
		// controlPane = new JScrollPane ();
		// tabs.add (Translator.swap ("StretchOV.SketchModel"), controlPane);
		//
		// if (!foundAtLeastOne) {return;}
		//
		// preferencePanel = tabs;
		// }

		ImageIcon icon = IconLoader.getIcon("option_24.png");
		preferences = new JButton(icon);
		preferences.setToolTipText(Translator.swap("Shared.preferences"));
		preferences.addActionListener(this);
		toolBar.add(preferences);

	}

}
