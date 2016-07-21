/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2001-2011 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package capsis.extension.standviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jeeb.lib.maps.geom.Point2;
import jeeb.lib.maps.geom.Polygon2;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListMap;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.OVSelector;
import jeeb.lib.util.OVSelectorSource;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.defaulttype.MultipartScene;
import capsis.defaulttype.ScenePart;
import capsis.extension.AbstractStandViewer;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.util.Drawer;
import capsis.util.Panel2D;
import capsis.util.Polygon2D;

/**
 * MultipartSceneViewer is a cartography viewer MultipartScenes.
 * 
 * @author F. de Coligny - january 2011
 */
public class MultipartSceneViewer extends AbstractStandViewer implements ActionListener {

	static {
		Translator.addBundle("capsis.extension.standviewer.MultipartSceneViewer");
	}

	public static final String NAME = "MultipartSceneViewer";
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "F. de Coligny";
	public static final String DESCRIPTION = "MultipartSceneViewer.description";

	private MultipartScene scene;

	private ListMap<String, ScenePart> partMap;
	private List<InnerPanel> panels;

	private JCheckBox separate;
	private int selectedTab;
	private JLabel statusBar;

	private Map<String, Rectangle.Double> memoPanelSelectionRectangles;
	private Map<String, OVSelector> memoPanelOvSelector;

	// If true, the parts are shown in separate panels
	// fc-26.6.2015
	protected boolean separateViews;

	/**
	 * Init method. Initialises the extension on the given model and step.
	 */
	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init(model, s, but);

		memoPanelSelectionRectangles = new HashMap<>();
		memoPanelOvSelector = new HashMap<>();

		separateViews = Settings.getProperty("MultipartSceneViewer.separateViews", false);

		updateViewer();
	}

	/**
	 * Updates the whole viewer on the current Step / StepButton
	 */
	private void updateViewer() throws Exception {

		scene = (MultipartScene) step.getScene();

		MultipartScene mps = (MultipartScene) step.getScene();

		partMap = new ListMap<>();

		for (ScenePart part : mps.getParts()) {
			if (!separateViews)
				// all part in same list
				partMap.addObject(Translator.swap("MultipartSceneViewer.wholeScene"), part);
			else
				partMap.addObject(part.getName(), part); // split parts
		}

		panels = new ArrayList<>();
		for (String name : partMap.getKeys()) {
			List<ScenePart> parts = partMap.getObjects(name);

			// If no selection, this is null
			Rectangle.Double r = memoPanelSelectionRectangles.get(name);
			OVSelector ovSel = memoPanelOvSelector.get(name);

			InnerPanel panel = new InnerPanel(name, parts, mps, r, ovSel);
			panel.reselect();

			panels.add(panel);
		}

		Collections.sort(panels);

		createUI();

	}

	/**
	 * A stand viewer may be updated to synchronize itself with a given step
	 * button. In subclasses, redefine this method (beginning by super.update
	 * (sb);) to update your representation of the step.
	 */
	public void update(StepButton sb) {
		try {
			super.update(sb);

			// Manage selection refreshing
			memoPanelSelectionRectangles.clear();
			for (InnerPanel p : panels) {
				Rectangle.Double r = p.getMemoSelectionRectangle();
				if (r != null) {
					memoPanelSelectionRectangles.put(p.getName(), r);
					memoPanelOvSelector.put(p.getName(), p.getOvSelector());
				}
			}

			updateViewer();

		} catch (Exception e) {
			Log.println(Log.ERROR, "MultiPartSceneViewer.update (StepButton)", "Could not update on sb: " + sb, e);
			MessageDialog
					.print(this, Translator.swap("MultipartSceneViewer.errorWhileUpdatingMultipartSceneViewer"), e);
		}

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchWith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof GModel)) {
				return false;
			}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject().getRoot()).getScene();

			if (!(s instanceof MultipartScene)) {
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "MultipartSceneViewer.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(separate)) {
			separateViews = !separateViews;
			Settings.setProperty("MultipartSceneViewer.separateViews", separateViews);

			try {
				updateViewer();
			} catch (Exception ex) {
				Log.println(Log.ERROR, "MultipartSceneViewer.actionPerformed ()", "Could not update viewer", ex);
				MessageDialog.print(this, Translator.swap("MultipartSceneViewer.couldNotUpdateTheViewerSeeLog"), ex);
			}
		}

	}

	/**
	 * Create the user interface.
	 */
	private void createUI() {

		// In case we change steps fc-26.6.2015
		getContentPane().removeAll();

		getContentPane().setLayout(new BorderLayout());

		separate = new JCheckBox(Translator.swap("MultipartSceneViewer.separateViews"), separateViews);
		separate.addActionListener(this);
		LinePanel l1 = new LinePanel();
		l1.add(separate);
		l1.addGlue();

		statusBar = new JLabel();
		statusBar.setText(Translator.swap("Shared.ready"));

		final JTabbedPane tabs = new JTabbedPane();
		for (InnerPanel p : panels) {
			tabs.addTab(p.getName(), p);
		}
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				selectedTab = tabs.getSelectedIndex();
			}
		});
		try {
			tabs.setSelectedIndex(selectedTab);
		} catch (Exception e) {
			tabs.setSelectedIndex(0);
		}

		getContentPane().add(separate, BorderLayout.NORTH);
		getContentPane().add(tabs, BorderLayout.CENTER);
		// getContentPane().add(new JScrollPane(panel2D), BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		// fc-23.6.2016
		scene = (MultipartScene) step.getScene();
		JPanel legend = scene.getLegend();
		if (legend != null) 
			getContentPane().add(legend, BorderLayout.EAST);
	}

	// ////////////////////////////////////////////////////////
	/**
	 * A panel to draw a list of scene parts.
	 */
	private static class InnerPanel extends JPanel implements Drawer, OVSelectorSource, Comparable {

		private String name;
		private List<ScenePart> parts;
		private MultipartScene mps;

		private Panel2D panel2D;
		private Rectangle.Double userBounds;
		protected boolean thisIsAReselection;
		protected Rectangle.Double memoSelectionRectangle;
		protected Collection<Object> memoSelection;
		protected Collection<Object> effectiveSelection;

		protected OVSelector ovSelector;

		/**
		 * Constructor
		 */
		public InnerPanel(String name, List<ScenePart> parts, MultipartScene mps,
				Rectangle.Double memoSelectionRectangle, OVSelector ovSelector) {

			this.name = name;
			this.parts = parts;
			this.mps = mps;
			this.memoSelectionRectangle = memoSelectionRectangle; // may be null
			this.ovSelector = ovSelector; // may be null

			userBounds = createUserBounds();

			panel2D = new Panel2D(this, userBounds, Panel2D.X_MARGIN_IN_PIXELS, Panel2D.Y_MARGIN_IN_PIXELS);

			if (ovSelector == null)
				createOVSelector(parts);

			createUI();
		}

		/**
		 * Defines what objects are candidate for selection by OVSelector
		 */
		protected void createOVSelector(List<ScenePart> parts) {
			try {
				// Default: the sceneParts
				Collection candidateObjects = new ArrayList(parts);

				// fc-25.6.2015 Added the trees
				for (ScenePart part : parts) {
					candidateObjects.addAll(part.getObjectsInPart());
				}

				JScrollPane targetScrollPane = null;
				GModel modelForVetoes = null;
				ExtensionManager extMan = CapsisExtensionManager.getInstance();
				ovSelector = new OVSelector(extMan, this, candidateObjects, targetScrollPane, false, false,
						modelForVetoes);

			} catch (Exception e) {
				Log.println(Log.ERROR, "MultipartSceneViewer.InnerPanel.createOVSelector ()",
						"Exception during OVSelector construction, wrote this error and passed", e);
			}
		}

		/**
		 * Inits the userBounds: the rectangle in which the drawing will be
		 * drawn.
		 */
		private Rectangle.Double createUserBounds() {

			double xMin = Double.MAX_VALUE;
			double xMax = -Double.MAX_VALUE;
			double yMin = Double.MAX_VALUE;
			double yMax = -Double.MAX_VALUE;

			for (ScenePart part : parts) {
				List<Polygon2> polygons = part.getPolygons();
				for (Polygon2 p : polygons) {
					xMin = Math.min(xMin, p.getXmin());
					xMax = Math.max(xMax, p.getXmax());
					yMin = Math.min(yMin, p.getYmin());
					yMax = Math.max(yMax, p.getYmax());
				}
			}

			userBounds = new Rectangle.Double(xMin, yMin, xMax - xMin, yMax - yMin);

			return userBounds;

		}

		/**
		 * Drawer interface
		 */
		@Override
		public void draw(Graphics g, Rectangle2D.Double r) {

			Graphics2D g2 = (Graphics2D) g;
			// MultipartScene mps = (MultipartScene) step.getScene();

			// Color edgeColor = new Color (148, 148, 148);
			// Color fillColor = edgeColor.brighter ();
			Color textColor = Color.GRAY;

			// For each part
			for (ScenePart part : parts) {
				List<Polygon2> polygons = part.getPolygons();

				double xMin = Double.MAX_VALUE;
				double xMax = -Double.MAX_VALUE;
				double yMin = Double.MAX_VALUE;
				double yMax = -Double.MAX_VALUE;

				// For each polygon of the part
				Shape sh = null;
				for (Polygon2 polygon : polygons) {

					xMin = Math.min(xMin, polygon.getXmin());
					xMax = Math.max(xMax, polygon.getXmax());
					yMin = Math.min(yMin, polygon.getYmin());
					yMax = Math.max(xMax, polygon.getYmax());

					sh = getShape(polygon);

					// Color - fc-22.5.2012
					int[] rgb = part.getRGB();
					Color fillColor = new Color(rgb[0], rgb[1], rgb[2]);
					Color edgeColor = fillColor.darker();

					g2.setColor(fillColor);
					g2.fill(sh);
					g2.setColor(edgeColor);
					g2.draw(sh);

				}

				// fc-25.6.2015 Optional further drawing
				mps.draw(g2, part);

				// Draw the lThe label of this part
				g2.setColor(textColor);

				Rectangle2D bbox = sh.getBounds2D();

				// g2.drawLine ((int) x0, (int) y0, (int) x1, (int) y1);
				g2.drawString(part.getName(), (int) bbox.getCenterX(), (int) bbox.getCenterY());

			}

			// // Debug
			// g2.setColor (Color.ORANGE);
			// g2.draw (userBounds);

		}

		/**
		 * Turns a polygon2 into a Shape
		 */
		public Shape getShape(Polygon2 polygon) {
			List<Point2> points = polygon.getPoints();

			int n = points.size();

			double[] xs = new double[n];
			double[] ys = new double[n];

			int k = 0;
			for (Point2 p : points) {
				xs[k] = p.getX();
				ys[k] = p.getY();
				k++;
			}
			GeneralPath path = Polygon2D.getGeneralPath(xs, ys, n);

			return path;
		}

		public Rectangle.Double getMemoSelectionRectangle() {
			return memoSelectionRectangle;
		}

		// public void setMemoSelectionRectangle(Rectangle.Double
		// memoSelectionRectangle) {
		// this.memoSelectionRectangle = memoSelectionRectangle;
		// }

		public OVSelector getOvSelector() {
			return ovSelector;
		}

		/**
		 * Drawer interface
		 */
		@Override
		public JPanel select(Rectangle2D.Double r, boolean more) {

			memoSelectionRectangle = r; // for reselect

			if (r == null) { // fc - 6.12.2007 - select () may be called early,
								// on
								// update ()
				thisIsAReselection = false;
				// If there was some selection, remove it
				if (effectiveSelection != null && !effectiveSelection.isEmpty()) {
					effectiveSelection.clear();
					panel2D.reset(); // Force panel repainting for selection
										// removal
					panel2D.repaint();
				}
				return null;
			} // fc - 6.12.2007 - select () may be called early, on update ()

			memoSelection = searchInRectangle(r); // fc - 23.11.2007

			effectiveSelection = ovSelector.select(memoSelection, thisIsAReselection);

			// rearm selection for next time (detail: in reselection mode,
			// if the OVDialog is unvisible, it is not set visible)
			thisIsAReselection = false;

			// Force panel repainting for effective selection enlighting
			panel2D.reset();
			panel2D.repaint();

			return null; // OVSelector framework: the OVSelector takes care of
							// showing the ov, return
							// null to panel2D
		}

		/**
		 * This methods returns the object selected by the given rectangle.
		 */
		protected Collection<Object> searchInRectangle(Rectangle.Double r) {
			Collection<Object> inRectangle = new ArrayList<Object>();
			if (r == null) {
				return inRectangle;
			} // null rectangle: nothing found

			// What parts intersect the user selection rectangle ?
			for (ScenePart part : parts) {

				for (Polygon2 p : part.getPolygons()) {
					Shape sh = getShape(p);

					if (sh.intersects(r)) {
						inRectangle.add(part);
						// fc-25.6.2015 trees may be added...
						inRectangle.addAll(part.getObjectsInPart());
					}
				}
			}

			return inRectangle;
		}

		public String getName() {
			return name;
		}

		/**
		 * Create the user interface.
		 */
		private void createUI() {
			setLayout(new BorderLayout());

			JToolBar toolbar = new JToolBar();
			toolbar.add(ovSelector);
			add(toolbar, BorderLayout.NORTH);
			add(new JScrollPane(panel2D), BorderLayout.CENTER);

		}

		/**
		 * OVSelectorSource interface
		 */
		@Override
		public void reselect() {
			boolean ctrlIsDown = false;
			thisIsAReselection = true;
			select(memoSelectionRectangle, ctrlIsDown);

		}

		/**
		 * OVSelectorSource interface
		 */
		@Override
		public void cancelSelection() {
			select(null, false);

		}

		@Override
		public int compareTo(Object o) {
			InnerPanel p1 = this;
			InnerPanel p2 = (InnerPanel) o;
			return p1.name.compareTo(p2.name);
		}
	}

}
