package capsis.lib.quest.ringviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import jeeb.lib.util.Alert;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Disposable;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MemoPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import repicea.simulation.treelogger.LoggableTree;
import capsis.app.C4Script;
import capsis.commongui.util.Helper;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.AbstractObjectViewer;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.GPieceRing;
import capsis.kernel.PathManager;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.lib.quest.commons.QuestCompatible;
import capsis.lib.quest.commons.QuestSpecies;
import capsis.lib.quest.commons.QuestTaper;
import capsis.lib.quest.commons.QuestTreeFileLoader;
import capsis.lib.quest.commons.QuestTreeWriter;
import capsis.lib.quest.ringviewer.model.QuestModel;
import capsis.util.Drawer;
import capsis.util.Panel2D;

/**
 * QuestRingViewer : a 2D GPiece viewer for Black spruce.
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public class QuestRingViewer extends AbstractObjectViewer implements Drawer, ActionListener, ItemListener, Disposable {

	static {
		Translator.addBundle("capsis.lib.quest.QuestLabels");
	}
	static public final String NAME = Translator.swap("QuestRingViewer");
	static public final String DESCRIPTION = Translator.swap("QuestRingViewer.description");
	static public final String AUTHOR = "Alexis Achim, F. de Coligny";
	static public final String VERSION = "1.0";

	public static final int X_MARGIN_IN_PIXELS = 20;
	public static final int Y_MARGIN_IN_PIXELS = 20;

	private double userWidth;
	private double userHeight;
	private double pixelWidth;
	private double pixelHeight;

	private QuestCompatible questTree; // 1st ref on the tree
	private Tree tree; // second ref on the same tree

	private List<Double> dbhs;
	private List<Double> heights;

	private GPiece piece; // the tree as a single piece of wood (discs,
							// rings...)

	private QuestSpecies selectedSpecies;
	private QuestModel selectedModel;

	private QuestSpecies previousSpecies;

	private boolean drawingPossible;

	private MemoPanel statusBar;
	private JPanel legendPanel;
	private JComboBox<QuestModel> availableModels;
	private JScrollPane scroll;
	private Panel2D panel2D;
	private MemoPanel display;

	private Map<GeneralPath, QuestRing> selectionMap;

	private JButton loadTreeFile;
	private JButton writeFiles;
	private JButton help;

	/**
	 * Default constructor.
	 */
	public QuestRingViewer() {
		selectionMap = new HashMap<GeneralPath, QuestRing>();
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchWith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {

		try {
			// for ObjectViewers, referent is always a Collection // fc-8.2.2008
			Collection c = (Collection) referent;
			if (c.isEmpty()) {
				return false;
			}

			for (Object o : c) {

				// If we find a QuestCompatible tree, we tell we are compatible
				if (o instanceof QuestCompatible && o instanceof Tree)
					return true;

				// if (o instanceof QuestCompatible) {
				// QuestCompatible qc = (QuestCompatible) o;
				//
				// boolean compatible = o instanceof Tree &&
				// !qc.getQuestSpecies().equals(QuestSpecies.OTHER);
				//
				// if (compatible)
				// return true;
				// // return compatible && ((Tree) o).getScene() instanceof
				// // TreeList;
				//
				// }
			}
			return false;

		} catch (Exception e) {
			Log.println(Log.WARNING, "QuestRingViewer.matchWith ()", "Error in matchWith (), returned false", e);
			return false;
		}

	}

	/**
	 * The given collection contains trees, we draw the first one if the species
	 * is known by QuEST.
	 */
	public void init(Collection s) throws Exception {

		try {
			pixelWidth = 0;
			pixelHeight = 0;

			drawingPossible = true;

			createUI();

			show(s);

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRingViewer ()", "Trouble in QuestRingViewer constructor", e);
			throw e;
		}
	}

	/**
	 * Show the tree in the selection. If several trees, draw only the first. If
	 * trouble, send a message and stop drawing.
	 */
	public Collection show(Collection candidateSelection) {
		try {
			drawingPossible = true;

			// No selection: please load a file
			if (candidateSelection.size() == 0) {
				statusBar.setText(Translator.swap("QuestRingViewer.pleaseLoadAnInputFile") + "...");

				drawingPossible = false;
				availableModels.setEnabled(false);
				resetPanel2D();

				return Collections.EMPTY_LIST;

			}

			if (candidateSelection.size() != 1) {
				statusBar.setText(Translator.swap("QuestRingViewer.QuestRingViewerDoesNotAcceptMultipleSelections"));

				drawingPossible = false;
				availableModels.setEnabled(false);
				resetPanel2D();

				return Collections.EMPTY_LIST;
			}

			questTree = (QuestCompatible) candidateSelection.iterator().next();

			tree = (Tree) questTree;

			if (QuestSpecies.OTHER.equals(questTree.getQuestSpecies())) {
				statusBar.setText(Translator
						.swap("QuestRingViewer.QuestRingViewerOnlyAcceptsTreesOfTheFollowingSpecies")
						+ " : "
						+ QuestSpecies.getSupportedSpeciesSentence());

				drawingPossible = false;
				availableModels.setEnabled(false);
				resetPanel2D();

				return Collections.EMPTY_LIST;
			}

			selectedSpecies = questTree.getQuestSpecies();

			if (!selectedSpecies.equals(previousSpecies)) {

				// Update the combo with the models for the new species
				availableModels.setModel(new DefaultComboBoxModel(getAvailableModels(selectedSpecies).toArray()));

			}
			availableModels.setEnabled(true);

			previousSpecies = selectedSpecies;

			// Create dbhs and heights list
			dbhs = new ArrayList<>();
			heights = new ArrayList<>();

			// Get the successive dbhs and heights of the tree in the Capsis
			// project
			QuestRingViewer.extractDbhsAndHeights(tree, dbhs, heights);

			// Refresh the whole drawing
			refresh();

			// We are sure the tree has a correct QuestSpecies
			realSelection = candidateSelection;

			return realSelection;

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRingViewer.show ()",
					"An exception disturbed QuestRingViewer.show () method (aborted)", e);
			MessageDialog.print(this,
					Translator.swap("QuestRingViewer.anErrorOccurredInQuestRingViewerPleaseSeeTheLog"));
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Refreshes the whole drawing for the current tree/questTree, its
	 * selectedSpecies and selectedModel.
	 */
	private void refresh() throws Exception {

//		System.out.println("QuestRingViewer refresh()...");

		statusBar.setText(Translator.swap("QuestSpecies.species") + " : " + Translator.swap(selectedSpecies.getName())
				+ "\n" + Translator.swap("QuestSpecies.tree") + " : " + tree.getId());

		// Select the selected model in the combo box
		selectedModel = (QuestModel) availableModels.getSelectedItem();

//		System.out.println("QuestRingViewer refresh() selectedModel: " + selectedModel);

		try {

			// MOVED upper, now can come from a file
			// dbhs = new ArrayList<>();
			// heights = new ArrayList<>();
			//
			// QuestRingViewer.extractDbhsAndHeights(tree, dbhs, heights);

			// Build the fake wood piece with its discs and rings
			piece = extractPiece(dbhs, heights);

			// // TMP, empty Panel2D when loading a tree file...
			// tree.setHeight(50d);

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRingViewer.refresh ()",
					"Could not build a piece, please check that the scenario cntains several steps", e);
			statusBar.setText(Translator
					.swap("QuestRingViewer.couldNotBuildAWoodPiecePleaseCheckTheScenarioContainsSeveralSteps")
					+ "\n-> " + e.getMessage());
			drawingPossible = false;
			resetPanel2D();

		}

		// Refresh the 2D view
		initialiseScale();
		sizePanel2D();
		resetPanel2D();

	}

	/**
	 * Get user width and height : the drawing size in user coordinates system.
	 */
	private void initialiseScale() {

		GPieceDisc disc0 = piece.getBottomDisc();

		GPieceDisc disc1 = piece.getTopDisc();

		// public double getLengthM() {
		// public double getHeightOfBottomSectionM() {
		// private double getHeightOfTopSectionM() {

		userHeight = disc1.getHeight_m() * 1.05;
		// userHeight = (piece.getHeightOfBottomSectionM() + piece.getLengthM())
		// * 1.05;
		// userHeight = tree.getHeight() * 1.05;

		double radiusMax_mm = QuestRing.getDiscRadius_mm(disc0);

		// Search for the largest radius (may be not the lowest one)
		for (GPieceDisc disc : piece.getDiscs()) {

			if (QuestRing.getDiscRadius_mm(disc) > radiusMax_mm) {
				radiusMax_mm = QuestRing.getDiscRadius_mm(disc);
			}
		}

		userWidth = radiusMax_mm * 1.2;

	}

	/**
	 * Calculate the size extension of the panel2D to view the complete selected
	 * scene.
	 */
	private void sizePanel2D() {

		Rectangle.Double r2 = new Rectangle.Double(0, 0, userWidth, userHeight);
		panel2D = new Panel2D(this, r2, X_MARGIN_IN_PIXELS, Y_MARGIN_IN_PIXELS, true);
		scroll.getViewport().setView(panel2D);

	}

	private void resetPanel2D() {
		if (panel2D != null) {
			panel2D.reset();
			panel2D.repaint();
		}
	}

	/**
	 * Turn the selectedTree into a piece of wood with discs and rings.
	 */
	static public void extractDbhsAndHeights(Tree tree, List<Double> dbhs, List<Double> heights) throws Exception {

		Step refStep = tree.getScene().getStep();
		Project project = refStep.getProject();
		Collection<Step> stepsFromRoot = project.getStepsFromRoot(refStep);

		if (stepsFromRoot == null || stepsFromRoot.size() == 1) {
			throw new Exception("QuestRingViewer needs at least two steps in the project");
		}

		// Get dbhs and heights of this tree along the simulation
		dbhs.clear();
		heights.clear();
		// dbhs = new ArrayList<Double>();
		// heights = new ArrayList<Double>();
		int treeId = ((Tree) tree).getId();

		int date0 = -1;
		int date1 = -1;

		for (Step step : stepsFromRoot) {
			if (date0 < 0) {
				date0 = step.getScene().getDate();
			} else if (date1 < 0) {
				date1 = step.getScene().getDate();
			}

			TreeList tl = (TreeList) step.getScene();
			Tree t = tl.getTree(treeId);
			if (t != null) {
				dbhs.add(t.getDbh());
				heights.add(t.getHeight());
			}
		}

//		System.out.println("QuestRingViewer ");
//		System.out.println("#heights: " + heights.size());

		int timeStep = date1 - date0;
		// System.out.println("QuestRingViewer timeStep: "+timeStep);

		if (timeStep > 1) {
			interpolate(dbhs, heights, date0, timeStep);
		}
//		System.out.println("after interpolation");
//		System.out.println("#heights: " + heights.size());

	}

	/**
	 * In case the time step is greater than 1 year, interpolate to find the
	 * tree dimensions each year. This static method may also be used by
	 * QuestKnotViewer.
	 */
	static public void interpolate(List<Double> dbhs, List<Double> heights, int date0, int timeStep) {
//		System.out.println("Quest interpolate...");

		int n = dbhs.size();

//		System.out.println("dates...");
		double[] tx = new double[n];
		for (int i = 0; i < n; i++) {
			tx[i] = date0 + i * timeStep;
//			System.out.println("" + tx[i]);
		}

		int dateMax = (int) Math.round(tx[n - 1]);

//		System.out.println("n: " + n + " dateMax: " + dateMax);

//		System.out.println("dbhs...");
		double[] tdbh = new double[n];
		for (int i = 0; i < n; i++) {
			tdbh[i] = dbhs.get(i);
//			System.out.print(" " + tdbh[i]);
		}
//		System.out.println();

//		System.out.println("heights...");
		double[] theight = new double[n];
		for (int i = 0; i < n; i++) {
			theight[i] = heights.get(i);
//			System.out.print(" " + theight[i]);
		}
//		System.out.println();

		SplineInterpolator dbhInterpolator = new SplineInterpolator();
		PolynomialSplineFunction fDbh = dbhInterpolator.interpolate(tx, tdbh);

//		System.out.println("interpolated dbhs...");
		dbhs.clear();
		for (int i = date0; i <= dateMax; i++) { // fc+ed-17.3.2015
			// for (int i = date0; i < dateMax; i++) {
			double dbh = fDbh.value(i);
			dbhs.add(dbh);
//			System.out.print(" " + dbh);
		}
//		System.out.println();

		SplineInterpolator heightInterpolator = new SplineInterpolator();
		PolynomialSplineFunction fHeight = heightInterpolator.interpolate(tx, theight);

//		System.out.println("interpolated heights...");
		heights.clear();
		for (int i = date0; i <= dateMax; i++) { // fc+ed-17.3.2015
			// for (int i = date0; i < dateMax; i++) {
			double h = fHeight.value(i);
			heights.add(h);
//			System.out.print(" " + h);
		}
//		System.out.println();

	}

	private GPiece extractPiece(List<Double> dbhs, List<Double> heights) {

		QuestTaper taper = selectedSpecies.getTaper();

		GPiece piece = QuestLogger.makeFakePiece((LoggableTree) tree, taper, dbhs, heights);

//		System.out.println("QuestRingViewer piece:\n" + piece);

		return piece;
	}

	/**
	 * Drawer interface. This method draws in the Panel2D each time this one
	 * must be repainted. The given Rectangle is the sub-part of the object to
	 * draw (zoom) in user coordinates (i.e. meters...). It can be used in
	 * pre-processes to avoid drawing invisible parts.
	 */
	public void draw(Graphics g, Rectangle.Double r) {

//		System.out.println("QuestRingViewer. draw ()...");

		Graphics2D g2 = (Graphics2D) g;

		// Clear legend and caption
		legendPanel.removeAll();
		display.setText("<html></html>");

		if (drawingPossible) {

			// Update drawing
			pixelWidth = panel2D.getUserWidth(1);
			pixelHeight = panel2D.getUserHeight(1);

			drawPiece(g2, piece);

			// Update legend
			legendPanel.add(selectedModel.getLegend(), BorderLayout.CENTER);

			// Update caption
			display.setText(selectedModel.getCaption());
		}

		legendPanel.revalidate();
		legendPanel.repaint();

		// Axes are always drawn
		drawAxes(g2);

	}

	/**
	 * Draw the whole piece, i.e. the whole tree.
	 */
	private void drawPiece(Graphics2D g2, GPiece piece) {

		boolean skipDisc0 = skipDisc0(piece.getDiscs());

		int i = 0;

		selectionMap.clear();

		for (GPieceDisc disc : piece.getDiscs()) {

			// Sometimes the first disc is missing
			if (i++ == 0 && skipDisc0)
				continue;

			for (GPieceRing ring : disc.getRings()) {

				QuestRing r = (QuestRing) ring;

				drawRing(g2, disc, r);

			}
		}

//		System.out.println("QuestRingViewer minV: " + minV + " maxV: " + maxV);

	}

	/**
	 * Evaluate if the disc0 should be drawn. In case of missing data, the disc0
	 * is very thick and we do not want to draw it.
	 */
	private boolean skipDisc0(Vector<GPieceDisc> discs) {
		try {
			Vector<GPieceDisc> discCopy = new Vector<GPieceDisc>(discs);

			GPieceDisc disc0 = discCopy.remove(0);
			double disc0Alt = disc0.getHeight_m();

			List<Double> thicks = new ArrayList<Double>();

			double prevAlt = -1;
			double disc1Alt = -1;

			for (GPieceDisc disc : discCopy) {
				double alt = disc.getHeight_m();

				if (disc1Alt <= -1)
					disc1Alt = alt;

				if (prevAlt > -1) {
					double thick = alt - prevAlt;
					thicks.add(thick);
				}

				prevAlt = alt;

			}

			int n = thicks.size();

			if (thicks.isEmpty())
				return false; // do not skip it

			// Do we skip disc0 ?
			double thick0 = disc1Alt - disc0Alt;

			double sum = 0d;
			for (double thick : thicks) {
				sum += thick;
			}
			double mean = sum / n;

			if (thick0 > 2 * mean) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// If trouble, do not skip, the problem may be visible
			return false;
		}

	}

	private double minV = Double.MAX_VALUE;
	private double maxV = -Double.MAX_VALUE;

	/**
	 * Draw the given ring.
	 */
	private void drawRing(Graphics2D g2, GPieceDisc disc, QuestRing ring) {

		double v = 0;
		try {

			v = selectedModel.getValue(ring);

//			if (v <= 0)
//				System.out.println("QuestRingViewer drawRing() found a ring with a wrong value: " + v + " in disc: "
//						+ disc + " ring: " + ring);

			if (v < minV)
				minV = v;
			if (v > maxV)
				maxV = v;

		} catch (Exception e) {
		}

		g2.setColor(selectedModel.colorGradient.getColor((float) v));

		GeneralPath p = new GeneralPath();
		p.moveTo(ring.v1.x, ring.v1.y);
		p.lineTo(ring.v2.x, ring.v2.y);
		p.lineTo(ring.v3.x, ring.v3.y);
		p.lineTo(ring.v4.x, ring.v4.y);
		p.closePath();

		g2.fill(p);

		selectionMap.put(p, ring);

	}

	/**
	 * Draw the graph axes.
	 */
	private void drawAxes(Graphics2D g2) {

		g2.setColor(Color.BLACK);

		// 1 - Axis
		g2.draw(new Line2D.Double(0, 0, userWidth, 0));
		g2.draw(new Line2D.Double(0, 0, 0, userHeight));

		// 2 - Dash and Labels
		int yAxisStep = (int) Math.ceil(userHeight / 10);
		if (yAxisStep < 1)
			yAxisStep = 1; // fc+ed-18.3.2015
		// int xAxisStep = (int) Math.ceil (userWidth/7);

		int xDashesNumber = 8; // approximative
		double userWidth_cm = userWidth / 10.0;
		int xAxisStep = 10 * (int) Math.ceil(userWidth_cm / xDashesNumber); // mm
		if (xAxisStep < 1)
			xAxisStep = 1; // fc+ed-18.3.2015

		assert (xAxisStep > 0 && yAxisStep > 0);

		float yDashLength = (float) pixelWidth * 5; // yDashLength is the length
													// of the dash on the y axis
													// (so in the x direction)
		float xDashLength = (float) pixelHeight * 5; // xDashLength is the
														// length of the dash on
														// the x axis (so in the
														// y direction)

		int yDashHeight = 0;
		int xDashWidth = 0;

		// Main Dash
		while (yDashHeight < userHeight) {
			g2.draw(new Line2D.Double(0, yDashHeight, -yDashLength, yDashHeight));
			g2.drawString("" + yDashHeight, -yDashLength * 4, yDashHeight - xDashLength);
			yDashHeight += yAxisStep;
		}

		while (xDashWidth < userWidth) {
			g2.draw(new Line2D.Double(xDashWidth, 0, xDashWidth, -xDashLength));
			int radius_cm = (int) xDashWidth / 10;
			g2.drawString("" + radius_cm, xDashWidth - yDashLength, -xDashLength * 4);
			xDashWidth += xAxisStep;
		}

		// Small dash
		yDashHeight = 0;
		int yDashStep = yAxisStep / 2;
		if (yDashStep < 1)
			yDashStep = 1; // fc+ed-18.3.2015

		xDashWidth = 0;
		int xDashStep = xAxisStep / 2;
		if (xDashStep < 1)
			xDashStep = 1; // fc+ed-18.3.2015

		while (yDashHeight < userHeight) {
			g2.draw(new Line2D.Double(0, yDashHeight, -yDashLength / 2, yDashHeight));
			yDashHeight += yDashStep;
			// yDashHeight += yAxisStep;
		}
		while (xDashWidth < userWidth) {
			g2.draw(new Line2D.Double(xDashWidth, 0, xDashWidth, -xDashLength / 2));
			xDashWidth += xDashStep;
			// xDashWidth += xAxisStep;
		}

		// Axes labels
		g2.drawString(Translator.swap("QuestRingViewer.height"), 0, (float) userHeight + xDashLength);
		g2.drawString(Translator.swap("QuestRingViewer.radius"), (float) userWidth - yDashLength * 10, -xDashLength * 7);

	}

	/**
	 * Drawer interface. We may receive (from Panel2D) a selection rectangle (in
	 * user space i.e. meters) and return a JPanel containing information about
	 * the objects (trees) inside the rectangle. If no objects are found in the
	 * rectangle, return null.
	 */
	public JPanel select(Rectangle.Double r, boolean ctrlIsDown) {

		List selection = new ArrayList();

		for (GeneralPath p : selectionMap.keySet()) {
			if (p.intersects(r)) {
				QuestRing ring = selectionMap.get(p);
				selection.add(ring);
			}
		}

		if (!selection.isEmpty())
			return AmapTools.getIntrospectionPanel(selection);

		// Not found
		return null;
	}

	/**
	 * Disposable interface
	 */
	@Override
	public void dispose() {
	}

	/**
	 * ItemListener interface
	 */
	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getStateChange() == ItemEvent.SELECTED) {
			// User changed his choice in the model combo box, refresh
			try {

				// // Get the successive dbhs and heights of the tree in the
				// Capsis
				// // project
				// QuestRingViewer.extractDbhsAndHeights(tree, dbhs, heights);

				refresh();

			} catch (Exception e) {
				Log.println(Log.ERROR, "QuestRingViewer.itemStateChanged ()",
						"Could not refresh the 2D view when a model changed", e);
				MessageDialog.print(this,
						Translator.swap("QuestRingViewer.anErrorOccurredInQuestRingViewerPleaseSeeTheLog"));
			}

		}

	}

	/**
	 * ActionListener interface.
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {

		if (evt.getSource().equals(writeFiles)) {
			writeFilesAction();

		} else if (evt.getSource().equals(loadTreeFile)) {
			loadTreeFileAction();

		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Action on browse, returns a file name or null if user cancelled.
	 */
	private String getExternalFileName() {
		JFileChooser chooser = new JFileChooser(Settings.getProperty("quest.ring.viewer.path",
				PathManager.getDir("data")));

		int returnVal = chooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = chooser.getSelectedFile().toString();
			Settings.setProperty("quest.ring.viewer.path", fileName);
			return fileName;
		}

		return null; // user cancelled
	}

	/**
	 * Loads a tree file
	 */
	private void loadTreeFileAction() {

		String fileName = "";

		try {
			// Ask the fileName to user
			fileName = getExternalFileName();
			if (fileName == null)
				return;

			// Load the tree file
			QuestTreeFileLoader loader = new QuestTreeFileLoader();
			String loaderReport = loader.load(fileName);
			// In case of trouble, tell user
			if (!loader.succeeded()) {
				throw new Exception(loaderReport);
			}

			// Get the QuestSpecies from the codeName in the file
			QuestSpecies species = QuestSpecies.findSpecies(loader.species);
			int treeId = loader.treeId;

			// Make a fake tree
			QuestTree t = new QuestTree(treeId, species);
			tree = t;
			selectedSpecies = t.getQuestSpecies();

			// Read dbhs and heights, store them in the expected lists
			dbhs = new ArrayList<>();
			heights = new ArrayList<>();

			double hMax = -Double.MAX_VALUE;

			for (QuestTreeFileLoader.DbhHeightRecord r : loader.dbhHeightRecords) {
				dbhs.add(r.dbh_cm);
				heights.add(r.height_m);

				hMax = Math.max(hMax, r.height_m);
			}

			tree.setHeight(hMax);

//			System.out.println("QuestRingViewer loadTreeFileAction() ...");
//			System.out.println("tree: " + tree);
//			System.out.println("species: " + selectedSpecies);
//			System.out.println("dbhs: " + AmapTools.toString(dbhs));
//			System.out.println("heights: " + AmapTools.toString(heights));

			// Refresh the available models for the loaded species
			DefaultComboBoxModel model = (DefaultComboBoxModel) availableModels.getModel();
			model.removeAllElements();

			for (QuestModel m : getAvailableModels(QuestSpecies.BLACK_SPRUCE)) {
				model.addElement(m);
			}

			availableModels.setEnabled(true);

			drawingPossible = true;

			// Refresh the drawing
			refresh();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRingViewer.loadTreeFileAction ()", "Could not load a tree file", e);
			statusBar.setText(Translator.swap("QuestRingViewer.couldNotLoadTreeFilePleaseCheckItsFormat"));
			MessageDialog.print(this, Translator.swap("QuestRingViewer.couldNotLoadTreeFilePleaseCheckItsFormat")
					+ "\n" + fileName, e);
		}
	}

	/**
	 * Write the files to disk.
	 */
	private void writeFilesAction() {

		String treeFileName = PathManager.getInstallDir() + "/tmp/quest-tree.txt";
		String controlFileName = PathManager.getInstallDir() + "/tmp/quest-ring-control.txt";

		String userMessage = "";

		try {
			new QuestTreeWriter(tree, dbhs, heights).save(treeFileName);
			String m1 = Translator.swap("QuestRingViewer.wroteTheTreeFile") + " : " + treeFileName;
			System.out.println(m1);
			userMessage += m1 + "\n";

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRingViewer.writeFilesAction ()", "Could not write tree file", e);
			userMessage += Translator.swap("QuestRingViewer.couldNotWriteTreeFile") + "\n";
		}

		try {
			new QuestRingControlWriter(tree, selectedSpecies, piece).save(controlFileName);
			String m2 = Translator.swap("QuestRingViewer.wroteTheRingControlFile") + " : " + controlFileName;
			System.out.println(m2);
			userMessage += m2 + "\n";

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestRingViewer.writeFilesAction ()", "Could not write ring control file", e);
			userMessage += Translator.swap("QuestRingViewer.couldNotWriteRingControlFile") + "\n";
		}

		statusBar.setText(userMessage);

	}

	/**
	 * Returns a vector (JComboBox prefers vectors) with the available models
	 * for the given species.
	 */
	private Vector<QuestModel> getAvailableModels(QuestSpecies species) {
		Vector v = new Vector(QuestSpecies.getSupportedModels(species));
		return v;
	}

	/**
	 * User interface definition.
	 */
	private void createUI() {

		JPanel main = new JPanel(new BorderLayout());

		// Top: statusBar + legend + available models
		ColumnPanel top = new ColumnPanel();

		LinePanel line0 = new LinePanel();

		LinePanel l0 = new LinePanel();
		statusBar = new MemoPanel("");
		statusBar.setBackground(Color.WHITE);
		l0.add(statusBar);
		l0.addStrut0();
		line0.add(l0);

		legendPanel = new JPanel(new BorderLayout());

		line0.add(legendPanel);
		top.add(line0);

		availableModels = new JComboBox(getAvailableModels(QuestSpecies.BLACK_SPRUCE));
		availableModels.addItemListener(this);

		top.add(availableModels);
		top.addStrut0();

		main.add(top, BorderLayout.NORTH);

		// Drawing
		JPanel part1 = new JPanel(new BorderLayout());
		scroll = new JScrollPane();
		part1.add(scroll, BorderLayout.CENTER);
		main.add(part1, BorderLayout.CENTER);

		// User memo
		display = new MemoPanel("<html></html>");
		main.add(display, BorderLayout.SOUTH);

		// Control panel
		LinePanel controlPanel = new LinePanel();

		loadTreeFile = new JButton(Translator.swap("QuestRingViewer.loadTreeFile"));
		loadTreeFile.addActionListener(this);
		controlPanel.add(loadTreeFile);

		controlPanel.addGlue();

		writeFiles = new JButton(Translator.swap("QuestRingViewer.writeFiles"));
		writeFiles.addActionListener(this);
		controlPanel.add(writeFiles);

		help = new JButton(Translator.swap("QuestRingViewer.help"));
		help.addActionListener(this);
		controlPanel.add(help);

		controlPanel.addStrut0();

		// Layout parts
		this.setLayout(new BorderLayout());
		this.add(main, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);

	}

	/**
	 * An entry point to launch the QuestRingViewer as a standalone, with input
	 * files. fc-25.3.2015
	 * 
	 * <pre>
	 * # To launch it from the capsis4/ dir with the new run pilot:
	 * capsis -p run QuestRingViewer
	 * 
	 * # To launch it from the capsis4/ dir in a terminal:
	 * java -cp class:ext/* capsis.lib.quest.ringviewer.QuestRingViewer
	 * 
	 * </pre>
	 */
	public static void main(String[] args) {

		// Start and init an underground capsis
		C4Script s = new C4Script();

		// This app has a gui, alerts will appear in dialogs (else in terminal)
		Alert.setInteractive(true);

		// Setup gui related things
		IconLoader.addPath("capsis/images");
		Translator.addSystemBundle("capsis.Labels");

		// Set system look & feel
		try {
			// This one is always available
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) { // ...should always be available
			System.out.println("Could not set System Look & Feel:" + e + ", passed");
		}

		// App information
		System.out.println();
		System.out.println(NAME + " (version " + VERSION + ") by " + AUTHOR);
		System.out.println(DESCRIPTION);
		System.out.println();

		JFrame f = new JFrame();

		QuestRingViewer v = new QuestRingViewer();
		try {
			v.init(Collections.EMPTY_LIST);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle(QuestRingViewer.NAME);
		f.setContentPane(v);
		f.setSize(500, 700);
		f.setLocationByPlatform(true);
		f.setVisible(true);

	}

	/**
	 * We need this fake tree in the case we load an input file. It mainly
	 * carries the QuestSpecies.
	 * 
	 * @author F. de Coligny - March 2015
	 */
	private static class QuestTree extends Tree implements QuestCompatible {
		private QuestSpecies species;

		/**
		 * Constructor
		 */
		public QuestTree(int id, QuestSpecies species) {
			// scene, age, height, dbh, marked are all null and zero
			super(id, null, 0, 0d, 0d, false);
			this.species = species;
		}

		@Override
		public double getNumber() {
			return 1;
		}

		@Override
		public double getCommercialVolumeM3() {
			return 0;
		}

		@Override
		public String getSpeciesName() {
			return null;
		}

		@Override
		public QuestSpecies getQuestSpecies() {
			return species;
		}

	}

}
