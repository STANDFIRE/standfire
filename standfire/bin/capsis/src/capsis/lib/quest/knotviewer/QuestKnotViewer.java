package capsis.lib.quest.knotviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.defaulttype.Type;
import jeeb.lib.sketch.gui.Panel3D;
import jeeb.lib.sketch.gui.SelectionManager;
import jeeb.lib.sketch.gui.SketcherManager;
import jeeb.lib.sketch.item.Grid;
import jeeb.lib.sketch.kernel.AddInfo;
import jeeb.lib.sketch.kernel.BuiltinType;
import jeeb.lib.sketch.kernel.SimpleAddInfo;
import jeeb.lib.sketch.kernel.SketchController;
import jeeb.lib.sketch.kernel.SketchFacade;
import jeeb.lib.sketch.scene.gui.TreeView;
import jeeb.lib.sketch.scene.item.MeshItem;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.sketch.scene.terrain.Plane;
import jeeb.lib.sketch.scene.toolbar.CenterOnSelectionAction;
import jeeb.lib.sketch.util.SketchTools;
import jeeb.lib.structure.geometry.mesh.SimpleMesh;
import jeeb.lib.structure.geometry.mesh.SimpleMeshFactory;
import jeeb.lib.util.Alert;
import jeeb.lib.util.AmapDialog;
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
import jeeb.lib.util.Vertex3d;
import capsis.app.C4Script;
import capsis.commongui.util.Helper;
import capsis.defaulttype.Tree;
import capsis.extension.AbstractObjectViewer;
import capsis.gui.DialogWithClose;
import capsis.kernel.PathManager;
import capsis.lib.quest.commons.QuestCompatible;
import capsis.lib.quest.commons.QuestSpecies;
import capsis.lib.quest.commons.QuestTreeFileLoader;
import capsis.lib.quest.commons.QuestTreeWriter;
import capsis.lib.quest.ringviewer.QuestRingViewer;

/**
 * QuestKnotViewer: a viewer showing the 3D distribution of knots along the
 * stem.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestKnotViewer extends AbstractObjectViewer implements ActionListener, Disposable, SketchController {

	static {
		Translator.addBundle("capsis.lib.quest.QuestLabels");
	}

	static public final String NAME = Translator.swap("QuestKnotViewer");
	static public final String DESCRIPTION = Translator.swap("QuestKnotViewer.description");
	static public final String AUTHOR = "Emmanuel Duchateau, F. de Coligny";
	static public final String VERSION = "1.0";

	private QuestCompatible questCompatible; // 1st ref on the tree
	private Tree tree; // second ref on the same tree

	private QuestSpecies selectedSpecies;

	private List<Double> dbhs;
	private List<Double> heights;

	// The knots calculated by the knot builder are in these growth units
	private List<QuestGU> gus;

	private SketchFacade sketchFacade;
	private SceneModel sceneModel;
	private Panel3D panel3D;
	private SketcherManager sketcherManager;
	private SelectionManager selectionManager;
	private JComponent preferencePanel;
	private AmapDialog preferenceDialog;
	private JButton preferences;

	private MemoPanel statusBar;
	private MemoPanel display;

	private JButton loadTreeFile;
	private JButton loadMobFile;
	private JButton writeFiles;
	private JButton help;

	/**
	 * Default constructor.
	 */
	public QuestKnotViewer() {
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

				// If we find a QuestCompatible tree with a non null
				// knotsBuilder, we tell we are compatible
				if (o instanceof QuestCompatible && o instanceof Tree) {
					// QuestCompatible t = (QuestCompatible) o;
					// QuestSpecies species = t.getQuestSpecies();
					// if (species.getKnotsBuilder() != null) {
					return true;
					// }
				}

			}
			return false;

		} catch (Exception e) {
			Log.println(Log.WARNING, "QuestKnotViewer.matchWith ()", "Error in matchWith (), returned false", e);
			return false;
		}

	}

	/**
	 * The given collection contains trees, we draw the first one if the species
	 * is known by QuEST.
	 */
	public void init(Collection s) throws Exception {

		try {

			createUI();

			show(s);

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotViewer ()", "Trouble in QuestKnotViewer constructor", e);
			throw e;
		}
	}

	/**
	 * The selection comes from Capsis, a single tree instanceof QuestCompatible
	 * is expected.
	 * 
	 * Show the tree in the selection. If trouble, send a message and stop
	 * drawing.
	 */
	public Collection show(Collection candidateSelection) {
		try {

			statusBar.setText(Translator.swap("QuestKnotViewer.QuestKnotViewerRefreshing") + "...");
			display.setText("<html></html>");

			// No selection: please load a file
			if (candidateSelection.size() == 0) {
				statusBar.setText(Translator.swap("QuestKnotViewer.pleaseLoadAnInputFile") + "...");

				clear3DView();

				return Collections.EMPTY_LIST;

			}

			// Several selections: please one only
			if (candidateSelection.size() != 1) {
				statusBar.setText(Translator.swap("QuestKnotViewer.QuestKnotViewerDoesNotAcceptMultipleSelections"));

				clear3DView();

				return Collections.EMPTY_LIST;
			}

			questCompatible = (QuestCompatible) candidateSelection.iterator().next();

			tree = (Tree) questCompatible;

			// Wrong species: please select another
			if (QuestSpecies.OTHER.equals(questCompatible.getQuestSpecies())) {
				statusBar.setText(Translator
						.swap("QuestKnotViewer.QuestKnotViewerOnlyAcceptsTreesOfTheFollowingSpecies")
						+ " : "
						+ QuestSpecies.getSupportedSpeciesSentence());

				clear3DView();

				return Collections.EMPTY_LIST;
			}

			// A correct selection was done, process it
			selectedSpecies = questCompatible.getQuestSpecies();

			// Extraction of dbhs and heights from the tree (with yearly
			// interpolation if needed)
			dbhs = new ArrayList<>();
			heights = new ArrayList<>();

			QuestRingViewer.extractDbhsAndHeights(tree, dbhs, heights);

			// Build the knots for the tree, species, dbhs and heights
			buildGUsAndKnots();

			// Refresh the whole drawing
			refresh();

			// We are sure the tree has a correct QuestSpecies
			realSelection = candidateSelection;

			return realSelection;

		} catch (Exception e) {

			clear3DView();

			Log.println(Log.ERROR, "QuestKnotViewer.show ()",
					"An exception interrupted QuestKnotViewer.show () method (aborted)", e);
			MessageDialog.print(this,
					Translator.swap("QuestKnotViewer.anErrorOccurredInQuestKnotViewerPleaseSeeTheLog"));
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Build the growth units and knots for the tree, species, dbhs and heights.
	 */
	private void buildGUsAndKnots() throws Exception {
		try {

			statusBar.setText(Translator.swap("QuestSpecies.species") + " : "
					+ Translator.swap(selectedSpecies.getName()) + "\n" + Translator.swap("QuestSpecies.tree") + " : "
					+ tree.getId());

			QuestKnotsBuilder builder = selectedSpecies.getKnotsBuilder();
			builder.execute(selectedSpecies.getTaper(), dbhs, heights);
			gus = builder.getGUs();

			display.setText(Translator.swap("QuestKnotsViewer.userInformation"));

		} catch (Exception e) {

			Log.println(Log.ERROR, "QuestKnotViewer.buildGUsAndKnots ()", "Could not build the GUs and knots", e);
			statusBar.setText(Translator.swap("QuestKnotViewer.couldNotBuildTheGUsAndKnotsSeeLog") + "\n-> "
					+ e.getMessage());

			clear3DView();

		}
	}

	/**
	 * Refreshes the whole drawing for the current Growth units.
	 */
	private void refresh() throws Exception {

		// try {
		// statusBar.setText(Translator.swap("QuestSpecies.species") + " : " +
		// Translator.swap(selectedSpecies.getName())
		// + "\n" + Translator.swap("QuestSpecies.tree") + " : " +
		// tree.getId());
		// } catch (Exception e) {
		// statusBar.setText(".mob file");
		// }

		try {

			// MOVED to buildGUsAndKnots ()
			// QuestKnotsBuilder builder = selectedSpecies.getKnotsBuilder();
			// builder.execute(selectedSpecies.getTaper(), dbhs, heights);
			// gus = builder.getGUs();

			update3DView();

			// MOVED to buildGUsAndKnots()
			// Do not print this models related message in case a .mob file is
			// loaded
			// display.setText(Translator.swap("QuestKnotsViewer.userInformation"));

		} catch (Exception e) {

			Log.println(Log.ERROR, "QuestKnotViewer.refresh ()", "Could not refresh the QuestKnotViewer", e);
			statusBar.setText(Translator.swap("QuestKnotViewer.couldNotRefreshTheQuestKnotViewerSeeLog") + "\n-> "
					+ e.getMessage());

			clear3DView();

		}

	}

	/**
	 * Clear the 3D view.
	 */
	private void clear3DView() {
		try {

			sceneModel.clearModel(this);

			// Add a grid
			Grid grid = new Grid();

			AddInfo addInfo = new SimpleAddInfo(grid.getType(), SketchTools.inSet(grid));
			sceneModel.getUndoManager().undoableAddItems(this, addInfo);

			// Add a terrain
			double size = 5;
			double half = size / 2d;
			double x = -half;
			double y = -half;
			double z = 0;

			List<Vertex3d> vertices = new ArrayList<Vertex3d>();
			vertices.add(new Vertex3d(x, y, z));
			vertices.add(new Vertex3d(x, y + size, z));
			vertices.add(new Vertex3d(x + size, y + size, z));
			vertices.add(new Vertex3d(x + size, y, z));
			double altitude = z;

			Plane plane = new Plane(altitude, vertices);
			sceneModel.setTerrain(this, plane);

			// Reset selection / clear undo stack
			sceneModel.getUndoManager().undoableResetSelection(this);
			sceneModel.getUndoManager().clearMemory();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsViewer.clear3DView ()", "Could not update the 3D view", e);
			statusBar.setText(Translator.swap("QuestKnotsViewer.couldNot§UpdateThe3DviewSeeLog"));
		}

	}

	/**
	 * Draws the knots in 3D.
	 */
	private void update3DView() {
		try {

			clear3DView();

			// Create knot and pith meshes and items
			Type knotType = new BuiltinType("Knots", jeeb.lib.sketch.extension.sketcher.MeshItemSketcher.class);
			List<Item> knotItems = new ArrayList<>();

			Type pithType = new BuiltinType("Pith", jeeb.lib.sketch.extension.sketcher.MeshItemSketcher.class);
			List<Item> pithItems = new ArrayList<>();

			// NOTES fc+ed-26.3.2015 createItems_2 is not ready
			// Still TODO: disks are not oriented about their previous vector
			// Warning: check their diameter (too slim ?)
			// Draw better the inner faces (add config on MeshItemSketcher)
			// There is a selection problem on tubes AND pith segments (transformed meshes)
			// Draws tubes
			// createItems_2(knotType, knotItems, pithType, pithItems); // to be finished
			
			// Draws spheres
			createItems_1(knotType, knotItems, pithType, pithItems);

//			System.out.println("QuestKnotsViewer: created knotItems: " + knotItems.size());

			// Add the knots meshItems in the scene, sketcher MeshItemSketcher
			AddInfo addInfo2 = new SimpleAddInfo(knotType, knotItems);
			sceneModel.getUndoManager().undoableAddItems(this, addInfo2);

			// Center on the knots
			CenterOnSelectionAction.smartCenter(panel3D);

			// Add the pith meshItems in the scene, sketcher MeshItemSketcher
			AddInfo addInfo3 = new SimpleAddInfo(pithType, pithItems);
			sceneModel.getUndoManager().undoableAddItems(this, addInfo3);

			// Reset selection / clear undo stack
			sceneModel.getUndoManager().undoableResetSelection(this);
			sceneModel.getUndoManager().clearMemory();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsViewer.update3DView ()", "Could not update the 3D view", e);
			statusBar.setText(Translator.swap("QuestKnotsViewer.couldNot§UpdateThe3DviewSeeLog"));
		}

	}

	/**
	 * Read the Growth units (gus), create the meshes and MeshItems for the
	 * knots an the pith, return them in the given lists.
	 */
	// ALTERNATIVE METHOD, should build extrusions like Bil3D, standby
	private void createItems_2(Type knotType, List<Item> knotItems, Type pithType, List<Item> pithItems)
			throws Exception {

		try {

			double zMin = 0;
			double zMax = -Double.MAX_VALUE;

			List<Point3f> points = new ArrayList<>();

			// Create the knots
			for (QuestGU gu : gus) {
				for (QuestKnot k : gu.getKnots()) {

					List<SimpleMesh> meshList = new ArrayList<SimpleMesh>();
					Vertex3d anchor = null;

					SimpleMesh prevDisk = null;

					for (QuestKnotDiameter d : k.getDiameters()) {

						double x = d.x / 1000d; // m
						double y = d.y / 1000d; // m
						double z = d.z / 1000d; // m

						zMax = Math.max(zMax, z);

						// First diameter's xyz
						if (anchor == null) {
							anchor = new Vertex3d(x, y, z);
							points.add(new Point3f((float) x, (float) y, (float) z));
//							System.out.println("QuestKnotViewer createItems_1() creating anchor for knot: " + k.id
//									+ " anchor: " + anchor);
						}

						int nPoints = 10;
						double r_m = d.diameter / 2d / 1000d;
						if (r_m <= 0)
							r_m = 0.0005; // default r = 1mm

						SimpleMesh m = SimpleMeshFactory.simpleDisk(nPoints);

						// Disk rotation
						Matrix4f mat4 = new Matrix4f();
						mat4.setIdentity();

						float xEuler = 0;
						float yEuler = (float) Math.PI / 2;

						MatrixUtils mu = new MatrixUtils();
						mu.setEuler(-(xEuler - (float) Math.PI / 2), -(yEuler - (float) Math.PI / 2), 0, mat4);
						Matrix3f mat3 = new Matrix3f();
						mat3.setIdentity();
						mat4.get(mat3);
						m.multiply(mat3);

						// Scale the disk
						m.scale((float) (r_m));

						// Translate the mesh
						Point3f p1 = new Point3f((float) x, (float) y, (float) z);
						m.translate(p1);

						SimpleMesh contour = null;
						if (prevDisk != null) {
							contour = addContour(prevDisk, m);
							contour.setColor(d.getColor());
							meshList.add(contour);
						} else {
							m.setColor(d.getColor());
							meshList.add(m);
						}

						prevDisk = m;
					}

					// System.out.println("QuestKnotViewer createItems_1() item for knot "+k.id+" anchor: "+anchor);

					int itemId = -1;
					MeshItem knotItem = new MeshItem(knotType, itemId, meshList, anchor);
					// Convenient for selection / inspectors
					knotItem.setExternalRef(k);
					knotItems.add(knotItem);
				}
			}

			// Create cylinders for the pith

			if (points.size() >= 2) {

				Point3f p0 = points.get(0);

				// If missing part at the bottom, add a pith cylinder from the
				// ground to p0
				if (p0.z > 0) {
					Point3f additionalPoint = new Point3f(p0.x, p0.y, 0);
					points.add(0, additionalPoint);
					p0 = additionalPoint;
				}

				for (int i = 1; i < points.size(); i++) {
					Point3f p1 = points.get(i);

					List<SimpleMesh> meshList = new ArrayList<SimpleMesh>();
					float d = p0.distance(p1);

					// A cylinder
					double x = 0;
					double y = 0;
					// Size is set directly, no scale needed
					double radius = 0.5 / 1000d; // d = 1mm
					double length = d;
					int nSectors = 8;
					int nSlices = 3;
					boolean closed = true;
					SimpleMesh m = SimpleMeshFactory.createCylinder(x, y, zMin, radius, length, nSectors, nSlices,
							closed);

					// Make the cylinder match the 2 points
					transformCylinder(m, p0, p1, length);

					m.setColor(Color.BLACK);
					meshList.add(m);

					// Create an item with this pith cylinder
					int itemId = -1;
					Vertex3d anchor = new Vertex3d(p0.x, p0.y, p0.z);
					MeshItem pithItem = new MeshItem(pithType, itemId, meshList, anchor);
					// Convenient for selection / inspectors
					pithItem.setExternalRef(null);
					pithItems.add(pithItem);

					p0 = p1;
				}

			}

			// // Create a cylinder for a centered pith
			// double x = 0;
			// double y = 0;
			// double radius = 0.5 / 1000d; // d = 1mm
			// double length = zMax - zMin;
			// int nSectors = 8;
			// int nSlices = 50;
			// boolean closed = true;
			// SimpleMesh m = SimpleMeshFactory.createCylinder(x, y, zMin,
			// radius, length, nSectors, nSlices, closed);
			// m.setColor(Color.BLACK);
			// List<SimpleMesh> meshList = new ArrayList<SimpleMesh>();
			// meshList.add(m);
			//
			// int itemId = -1;
			// Vertex3d anchor = new Vertex3d(x, y, zMin);
			// MeshItem pithItem = new MeshItem(pithType, itemId, meshList,
			// anchor);
			// // Convenient for selection / inspectors
			// pithItem.setExternalRef(null);
			// pithItems.add(pithItem);

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotViewer.createItems_1 ()", "Could not create the knot / pith meshes", e);
			throw e;
		}
	}

	/**
	 * Adds contours in disk d1 to obtain a kind of cylinder from d0 to d1. Do
	 * and d1 have the same number of points. Returns d2, the augmented mesh d1
	 * including contour.
	 */
	private SimpleMesh addContour(SimpleMesh d0, SimpleMesh d1) {

		int n1 = d1.getPoints().length;
		int n2 = 2 * n1;
		Point3f[] points2 = new Point3f[n2];

		// n2 points -> n2 triangles
		int[][] paths2 = new int[n2][3];

		int ipo = 0;
		int ipa = 0;

		Point3f p2 = null;
		Point3f p3 = null;

		int i2 = -1;
		int i3 = -1;

		for (int i = 0; i < d1.getPoints().length; i++) {
			Point3f p0 = d0.getPoints()[i];
			Point3f p1 = d1.getPoints()[i];

			points2[ipo] = p0;
			int i0 = ipo++;
			points2[ipo] = p1;
			int i1 = ipo++;

			if (p2 != null) {

				// i2 = i0 - 2;
				// i3 = i1 - 2;

				// Add 2 triangles
				paths2[ipa][0] = i2;
				paths2[ipa][1] = i3;
				paths2[ipa][2] = i1;
				ipa++;
				paths2[ipa][0] = i2;
				paths2[ipa][1] = i1;
				paths2[ipa][2] = i0;
				ipa++;
			}

			p2 = p0;
			p3 = p1;

			i2 = i0;
			i3 = i1;

		}

		// Add the 2 last faces
		int i0 = 0;
		int i1 = 1;
		Point3f p0 = d0.getPoints()[i0];
		Point3f p1 = d1.getPoints()[i1];

		if (p2 != null) {

			// int i2 = i0 - 2;
			// int i3 = i1 - 2;

			// Add 2 triangles
			paths2[ipa][0] = i2;
			paths2[ipa][1] = i3;
			paths2[ipa][2] = i1;
			ipa++;
			paths2[ipa][0] = i2;
			paths2[ipa][1] = i1;
			paths2[ipa][2] = i0;
			ipa++;
		}

//		System.out.println("QuestKnotViewer ipa: " + ipa);

		SimpleMesh d2 = new SimpleMesh();
		d2.setPoints(points2);
		d2.setPaths(paths2);

		return d2;
	}

	/**
	 * Transforms the given cylinder with origin in (0, 0) with the given length
	 * to make it start in p0 and end in p1.
	 */
	private void transformCylinder(SimpleMesh m, Point3f p0, Point3f p1, double length) {
		// Normalize mesh
		m.scale((float) (1 / length));

		// The main 3D vector
		Vector3f v = new Vector3f(p1.x - p0.x, -(p1.y - p0.y), p1.z - p0.z);
		v.normalize();

//		System.out.println("Main 3D vector: " + v);

		// Get the euler angles
		Vector3f v_yz = new Vector3f(0, v.y, v.z);
		float xEuler = v_yz.angle(new Vector3f(0, 1, 0));

//		System.out.println("QuestKnotViewer xEuler_deg: " + Math.toDegrees(xEuler));

		Vector3f v_xz = new Vector3f(v.x, 0, v.z);
		float yEuler = v_xz.angle(new Vector3f(1, 0, 0));

//		System.out.println("QuestKnotViewer yEuler_deg: " + Math.toDegrees(yEuler));

		Matrix4f mat4 = new Matrix4f();
		mat4.setIdentity();

		MatrixUtils mu = new MatrixUtils();
		mu.setEuler(-(xEuler - (float) Math.PI / 2), -(yEuler - (float) Math.PI / 2), 0, mat4);
		Matrix3f mat3 = new Matrix3f();
		mat3.setIdentity();
		mat4.get(mat3);
		m.multiply(mat3);

		// scale back
		m.scale((float) length);

		// translate the mesh
		m.translate(p0);

		m.computeBBox();
	}

	/**
	 * Read the Growth units (gus), create the meshes and MeshItems for the
	 * knots an the pith, return them in the given lists.
	 */
	private void createItems_1(Type knotType, List<Item> knotItems, Type pithType, List<Item> pithItems)
			throws Exception {

		// WORKS: pith segments are correctly oriented and transformed
		// still a little selection problem fc+ed-26.3.2015

		try {

			double zMin = 0;
			double zMax = -Double.MAX_VALUE;

			List<Point3f> points = new ArrayList<>();

			// Create the knots
			for (QuestGU gu : gus) {
				for (QuestKnot k : gu.getKnots()) {

					List<SimpleMesh> meshList = new ArrayList<SimpleMesh>();
					Vertex3d anchor = null;

					for (QuestKnotDiameter d : k.getDiameters()) {

						double x = d.x / 1000d; // m
						double y = d.y / 1000d; // m
						double z = d.z / 1000d; // m

						zMax = Math.max(zMax, z);

						// First diameter's xyz
						if (anchor == null) {
							anchor = new Vertex3d(x, y, z);
							points.add(new Point3f((float) x, (float) y, (float) z));
//							System.out.println("QuestKnotViewer createItems_1() creating anchor for knot: " + k.id
//									+ " anchor: " + anchor);
						}

						int nSectors = 3;
						int nSlices = 3;
						double r_m = d.diameter / 2d / 1000d;
						if (r_m <= 0)
							r_m = 0.0005; // default r = 1mm
						double a = r_m; // m
						double b = r_m; // m
						double c = r_m; // m

						SimpleMesh m = SimpleMeshFactory.createEllipsoid(x, y, z, a, b, c, nSectors, nSlices);

						m.setColor(d.getColor());
						// if (d.alive)
						// m.setColor(Color.GREEN);
						// else
						// m.setColor(Color.RED);

						meshList.add(m);
					}

					// System.out.println("QuestKnotViewer createItems_1() item for knot "+k.id+" anchor: "+anchor);

					int itemId = -1;
					MeshItem knotItem = new MeshItem(knotType, itemId, meshList, anchor);
					// Convenient for selection / inspectors
					knotItem.setExternalRef(k);
					knotItems.add(knotItem);
				}
			}

			// Create cylinders for the pith

			if (points.size() >= 2) {

				Point3f p0 = points.get(0);

				// If missing part at the bottom, add a pith cylinder from the
				// ground to p0
				if (p0.z > 0) {
					Point3f additionalPoint = new Point3f(p0.x, p0.y, 0);
					points.add(0, additionalPoint);
					p0 = additionalPoint;
				}

				for (int i = 1; i < points.size(); i++) {
					Point3f p1 = points.get(i);

					List<SimpleMesh> meshList = new ArrayList<SimpleMesh>();
					float d = p0.distance(p1);

					// A cylinder
					double x = 0;
					double y = 0;
					// Size is set directly, no scale needed
					double radius = 0.5 / 1000d; // d = 1mm
					double length = d;
					int nSectors = 8;
					int nSlices = 3;
					boolean closed = true;
					SimpleMesh m = SimpleMeshFactory.createCylinder(x, y, zMin, radius, length, nSectors, nSlices,
							closed);

					// Make the cylinder match the 2 points
					transformCylinder(m, p0, p1, length);

					m.setColor(Color.BLACK);
					meshList.add(m);

					// Create an item with this pith cylinder
					int itemId = -1;
					Vertex3d anchor = new Vertex3d(p0.x, p0.y, p0.z);
					MeshItem pithItem = new MeshItem(pithType, itemId, meshList, anchor);
					// Convenient for selection / inspectors
					pithItem.setExternalRef(null);
					pithItems.add(pithItem);

					p0 = p1;
				}

			}

			// // Create a cylinder for a centered pith
			// double x = 0;
			// double y = 0;
			// double radius = 0.5 / 1000d; // d = 1mm
			// double length = zMax - zMin;
			// int nSectors = 8;
			// int nSlices = 50;
			// boolean closed = true;
			// SimpleMesh m = SimpleMeshFactory.createCylinder(x, y, zMin,
			// radius, length, nSectors, nSlices, closed);
			// m.setColor(Color.BLACK);
			// List<SimpleMesh> meshList = new ArrayList<SimpleMesh>();
			// meshList.add(m);
			//
			// int itemId = -1;
			// Vertex3d anchor = new Vertex3d(x, y, zMin);
			// MeshItem pithItem = new MeshItem(pithType, itemId, meshList,
			// anchor);
			// // Convenient for selection / inspectors
			// pithItem.setExternalRef(null);
			// pithItems.add(pithItem);

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotViewer.createItems_1 ()", "Could not create the knot / pith meshes", e);
			throw e;
		}
	}

	/**
	 * Disposable interface
	 */
	@Override
	public void dispose() {
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

		} else if (evt.getSource().equals(loadMobFile)) {
			loadMobFileAction();

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

		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Action on browse, returns a file name or null if user cancelled.
	 */
	private String getExternalFileName() {
		JFileChooser chooser = new JFileChooser(Settings.getProperty("quest.knots.viewer.path",
				PathManager.getDir("data")));

		int returnVal = chooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = chooser.getSelectedFile().toString();
			Settings.setProperty("quest.knots.viewer.path", fileName);
			return fileName;
		}

		return null; // user cancelled
	}

	/**
	 * Loads a .mob file
	 */
	private void loadMobFileAction() {

		String fileName = "";

		try {
			// Ask the fileName to user
			fileName = getExternalFileName();
			if (fileName == null)
				return;

			// Load the .mob file
			QuestMobFileLoader loader = new QuestMobFileLoader();
			String loaderReport = loader.load(fileName);
			// In case of trouble, tell user
			if (!loader.succeeded()) {
				throw new Exception(loaderReport);
			}

			gus = loader.getGus();

			statusBar.setText(Translator.swap("QuestKnotViewer.viewMobFile") + " : " + fileName);
			display.setText(Translator.swap("QuestKnotViewer.mobExplanation"));

			refresh();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotViewer.loadMobFileAction ()", "Could not load a .mob file", e);
			statusBar.setText(Translator.swap("QuestKnotViewer.couldNotLoadMobFilePleaseCheckItsFormat"));
			MessageDialog.print(this, Translator.swap("QuestKnotViewer.couldNotLoadMobFilePleaseCheckItsFormat") + "\n"
					+ fileName, e);
		}
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

			for (QuestTreeFileLoader.DbhHeightRecord r : loader.dbhHeightRecords) {
				dbhs.add(r.dbh_cm);
				heights.add(r.height_m);
			}

			// Build the knots for the tree, species, dbhs and heights
			buildGUsAndKnots();

			// Refresh the drawing
			refresh();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotViewer.loadTreeFileAction ()", "Could not load a tree file", e);
			statusBar.setText(Translator.swap("QuestKnotViewer.couldNotLoadTreeFilePleaseCheckItsFormat"));
			MessageDialog.print(this, Translator.swap("QuestKnotViewer.couldNotLoadTreeFilePleaseCheckItsFormat")
					+ "\n" + fileName, e);
		}
	}

	/**
	 * Write the files to disk.
	 */
	private void writeFilesAction() {

		String treeFileName = PathManager.getInstallDir() + "/tmp/quest-tree.txt";
		String mobFileName = PathManager.getInstallDir() + "/tmp/quest-knots.mob";
		String knotsControlFileName = PathManager.getInstallDir() + "/tmp/quest-knots-control.txt";

		String userMessage = "";

		try {
			new QuestTreeWriter(tree, dbhs, heights).save(treeFileName);
			String m1 = Translator.swap("QuestKnotViewer.wroteTheTreeFile") + " : " + treeFileName;
			System.out.println(m1);
			userMessage += m1 + "\n";

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsViewer.writeFilesAction ()", "Could not write Tree file", e);
			userMessage += Translator.swap("QuestKnotViewer.couldNotWriteTreeFile") + "\n";
		}

		try {
			new QuestMobWriter(tree, gus).save(mobFileName);
			String m2 = Translator.swap("QuestKnotViewer.wroteTheMobFile") + " : " + mobFileName;
			System.out.println(m2);
			userMessage += m2 + "\n";

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsViewer.writeFilesAction ()", "Could not write .mob file", e);
			userMessage += Translator.swap("QuestKnotViewer.couldNotWriteMobFile") + "\n";
		}

		try {
			new QuestKnotsControlWriter(tree, gus).save(knotsControlFileName);
			String m2 = Translator.swap("QuestKnotViewer.wroteTheKnotsControlFile") + " : " + knotsControlFileName;
			System.out.println(m2);
			userMessage += m2 + "\n";

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsViewer.writeFilesAction ()", "Could not write Knots control file", e);
			userMessage += Translator.swap("QuestKnotViewer.couldNotWriteKnotsControlFile") + "\n";
		}

		statusBar.setText(userMessage);

	}

	/**
	 * User interface definition.
	 */
	private void createUI() {

		JPanel main = new JPanel(new BorderLayout());

		// Top: statusBar + legend + available models
		ColumnPanel top = new ColumnPanel();
		main.add(top, BorderLayout.NORTH);

		LinePanel line0 = new LinePanel();

		LinePanel l0 = new LinePanel();
		statusBar = new MemoPanel("");
		statusBar.setBackground(Color.WHITE);
		l0.add(statusBar);
		l0.addStrut0();
		line0.add(l0);

		top.add(line0);

		JPanel view3D = new JPanel();
		try {
			sketchFacade = new SketchFacade(AmapTools.getWindow(this), PathManager.getDir("etc"));
			sceneModel = sketchFacade.getSceneModel();
			sceneModel.setEditable(this, true); // possible to add items in it
												// (by program)
			panel3D = sketchFacade.getPanel3D();
			sketchFacade.addStatusListener(this);

			view3D = panel3D.getPanel3DWithToolBar(BorderLayout.EAST, true, true, false, false, false);
			main.add(view3D, BorderLayout.CENTER);

			panel3D.setPovCenter(0, 0, 0);

			// fc-3.6.2014 change initial zoom factor
			panel3D.setZoomFactor(0.005f);

			sketcherManager = sketchFacade.getSketcherManager();

			// The selection Manager will respond on selections
			// Note: passing a null or empty candidateObjects collection -> all
			// ObjectViewers
			Collection candidateObjects = null;
			selectionManager = sketchFacade.createSelectionManager(candidateObjects, "");

			// Add a preferences button in the toolbar of the Panel3D
			createPreferencePanel(panel3D.getToolBar());

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsViewer.createUI ()", "Could not create the 3D view", e);
			statusBar.setText(Translator.swap("QuestKnotViewer.couldNotCreateThe3DViewSeeLog"));
		}

		// User memo
		display = new MemoPanel("<html></html>");
		main.add(display, BorderLayout.SOUTH);

		// Control panel
		LinePanel controlPanel = new LinePanel();

		loadTreeFile = new JButton(Translator.swap("QuestKnotViewer.loadTreeFile"));
		loadTreeFile.addActionListener(this);
		controlPanel.add(loadTreeFile);

		loadMobFile = new JButton(Translator.swap("QuestKnotViewer.loadMobFile"));
		loadMobFile.addActionListener(this);
		controlPanel.add(loadMobFile);

		controlPanel.addGlue();

		writeFiles = new JButton(Translator.swap("QuestKnotViewer.writeFiles"));
		writeFiles.addActionListener(this);
		controlPanel.add(writeFiles);

		help = new JButton(Translator.swap("Shared.help"));
		help.addActionListener(this);
		controlPanel.add(help);

		controlPanel.addStrut0();

		// Layout parts
		this.setLayout(new BorderLayout());
		this.add(main, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);

	}

	/**
	 * Build a preference panel for the ObjectViewer
	 */
	private void createPreferencePanel(JToolBar toolBar) {

		if (preferencePanel != null)
			return;

		JTabbedPane tabs = new JTabbedPane();

		// Sketcher manager
		tabs.add(sketcherManager.getName(), sketcherManager);

		// Tree view
		TreeView treeView = sketchFacade.getTreeView();
		tabs.add(treeView.getName(), treeView);

		// Selection manager
		tabs.add(selectionManager.getName(), selectionManager);

		preferencePanel = tabs;

		ImageIcon icon = IconLoader.getIcon("option_24.png");
		preferences = new JButton(icon);
		preferences.setToolTipText(Translator.swap("Shared.preferences"));
		preferences.addActionListener(this);
		toolBar.add(preferences);

	}

	/**
	 * An entry point to launch the QuestKnotViewer as a standalone, with input
	 * files. fc-23.3.2015
	 * 
	 * <pre>
	 * # To launch it from the capsis4/ dir with the new run pilot:
	 * capsis -p run QuestKnotViewer
	 * 
	 * # To launch it from the capsis4/ dir in a terminal:
	 * java -cp class:ext/* capsis.lib.quest.knotviewer.QuestKnotViewer
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

		QuestKnotViewer v = new QuestKnotViewer();
		try {
			v.init(Collections.EMPTY_LIST);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle(QuestKnotViewer.NAME);
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

	// /////////////////////////////////////////:

	/*****************************************************************************
	 * J3D.org Copyright (c) 2000 Java Source
	 * 
	 * This source is licensed under the GNU LGPL v2.1 Please read
	 * http://www.gnu.org/copyleft/lgpl.html for more information
	 * 
	 * This software comes with the standard NO WARRANTY disclaimer for any
	 * purpose. Use it at your own risk. If there's a problem you get to fix it.
	 * 
	 ****************************************************************************/

	// External imports
	// import javax.vecmath.*;

	// Local imports
	// none

	/**
	 * A utility class that performs various matrix operations on the
	 * {@link javax.vecmath} package.
	 * <p>
	 * 
	 * @author Justin Couch
	 * @version $Revision: 1.9 $
	 */
	public class MatrixUtils {
		/** Work variable for the fallback lookat calculations. */
		private AxisAngle4f orient;

		/** Work variable for the fallback lookat calculations. */
		private AxisAngle4d orientd;

		/** A temp 3x3 matrix used during the invert() routines */
		private float[] tempMat3;

		/** A temp 4x4 matrix used during the invert() routines */
		private float[] tempMat4;

		/** A temp 4x4 matrix used during the invert() routines */
		private float[] resMat4;

		/**
		 * A temp 3x3 double matrix used during the invert() routines when
		 * double precision is required
		 */
		private double[] tempMat3d;

		/**
		 * A temp 4x4 double matrix used during the invert() routines when
		 * double precision is required
		 */
		private double[] tempMat4d;

		/**
		 * Scratch matrix object used in double precision invert() calculation
		 */
		private Matrix4d matrix4d;

		/**
		 * Construct a default instance of this class.
		 */
		public MatrixUtils() {
			tempMat3 = new float[9];
			tempMat4 = new float[16];
			resMat4 = new float[16];

			tempMat3d = new double[9];
			tempMat4d = new double[16];
			matrix4d = new Matrix4d();
		}

		/**
		 * Perform a LookAt camera calculation and place it in the given matrix.
		 * If using this for a viewing transformation, you should invert() the
		 * matrix after the call.
		 * 
		 * @param eye
		 *            The eye location
		 * @param center
		 *            The place to look at
		 * @param up
		 *            The up vector
		 * @param res
		 *            The result to put the calculation into
		 */
		public void lookAt(Point3f eye, Point3f center, Vector3f up, Matrix4f res) {
			double d = eye.x - center.x;
			double d1 = eye.y - center.y;
			double d2 = eye.z - center.z;

			double det = d * d + d1 * d1 + d2 * d2;
			if (det != 1) {
				if (det == 0) {
					res.setIdentity();
					res.m03 = eye.x;
					res.m13 = eye.y;
					res.m23 = eye.z;
					return;
				}

				det = 1 / Math.sqrt(det);
				d *= det;
				d1 *= det;
				d2 *= det;
			}

			double d4 = up.x;
			double d5 = up.y;
			double d6 = up.z;

			det = (up.x * up.x + up.y * up.y + up.z * up.z);
			if (det != 1) {
				if (det == 0)
					throw new IllegalArgumentException("Up vector is all zeroes");

				det = 1 / Math.sqrt(det);
				d4 *= det;
				d5 *= det;
				d6 *= det;
			}

			double d7 = d5 * d2 - d1 * d6;
			double d8 = d6 * d - d4 * d2;
			double d9 = d4 * d1 - d5 * d;

			det = d7 * d7 + d8 * d8 + d9 * d9;

			if (det != 1) {
				// If this value is zero then we have a case where the up vector
				// is
				// parallel to the eye-center vector. In this case, set the
				// translation to the normal location and recalculate everything
				// again using the slow way using a reference of the normal view
				// orientation pointing along the -Z axis.
				if (det == 0) {
					lookAtFallback(eye, (float) d, (float) d1, (float) d2, res);
					return;
				}

				det = 1 / Math.sqrt(det);
				d7 *= det;
				d8 *= det;
				d9 *= det;
			}

			d4 = d1 * d9 - d8 * d2;
			d5 = d2 * d7 - d * d9;
			d6 = d * d8 - d1 * d7;

			res.m00 = (float) d7;
			res.m01 = (float) d8;
			res.m02 = (float) d9;
			res.m03 = (float) (-eye.x * res.m00 - eye.y * res.m01 - eye.z * res.m02);

			res.m10 = (float) d4;
			res.m11 = (float) d5;
			res.m12 = (float) d6;
			res.m13 = (float) (-eye.x * res.m10 - eye.y * res.m11 - eye.z * res.m12);

			res.m20 = (float) d;
			res.m21 = (float) d1;
			res.m22 = (float) d2;
			res.m23 = (float) (-eye.x * res.m20 - eye.y * res.m21 - eye.z * res.m22);

			res.m30 = 0;
			res.m31 = 0;
			res.m32 = 0;
			res.m33 = 1;
		}

		/**
		 * Perform a LookAt camera calculation and place it in the given matrix.
		 * If using this for a viewing transformation, you should invert() the
		 * matrix after the call.
		 * 
		 * @param eye
		 *            The eye location
		 * @param center
		 *            The place to look at
		 * @param up
		 *            The up vector
		 * @param res
		 *            The result to put the calculation into
		 */
		public void lookAt(Point3d eye, Point3d center, Vector3d up, Matrix4d res) {
			double d = eye.x - center.x;
			double d1 = eye.y - center.y;
			double d2 = eye.z - center.z;

			double det = d * d + d1 * d1 + d2 * d2;
			if (det != 1) {
				if (det == 0) {
					res.setIdentity();
					res.m03 = eye.x;
					res.m13 = eye.y;
					res.m23 = eye.z;
					return;
				}

				det = 1 / Math.sqrt(det);
				d *= det;
				d1 *= det;
				d2 *= det;
			}

			double d4 = up.x;
			double d5 = up.y;
			double d6 = up.z;

			det = (up.x * up.x + up.y * up.y + up.z * up.z);
			if (det != 1) {
				if (det == 0)
					throw new IllegalArgumentException("Up vector is all zeroes");

				det = 1 / Math.sqrt(det);
				d4 *= det;
				d5 *= det;
				d6 *= det;
			}

			double d7 = d5 * d2 - d1 * d6;
			double d8 = d6 * d - d4 * d2;
			double d9 = d4 * d1 - d5 * d;

			det = d7 * d7 + d8 * d8 + d9 * d9;

			if (det != 1) {
				// If this value is zero then we have a case where the up vector
				// is
				// parallel to the eye-center vector. In this case, set the
				// translation to the normal location and recalculate everything
				// again using the slow way using a reference of the normal view
				// orientation pointing along the -Z axis.
				if (det == 0) {
					lookAtFallback(eye, d, d1, d2, res);
					return;
				}

				det = 1 / Math.sqrt(det);
				d7 *= det;
				d8 *= det;
				d9 *= det;
			}

			d4 = d1 * d9 - d8 * d2;
			d5 = d2 * d7 - d * d9;
			d6 = d * d8 - d1 * d7;

			res.m00 = d7;
			res.m01 = d8;
			res.m02 = d9;
			res.m03 = (-eye.x * res.m00 - eye.y * res.m01 - eye.z * res.m02);

			res.m10 = d4;
			res.m11 = d5;
			res.m12 = d6;
			res.m13 = (-eye.x * res.m10 - eye.y * res.m11 - eye.z * res.m12);

			res.m20 = d;
			res.m21 = d1;
			res.m22 = d2;
			res.m23 = (-eye.x * res.m20 - eye.y * res.m21 - eye.z * res.m22);

			res.m30 = 0;
			res.m31 = 0;
			res.m32 = 0;
			res.m33 = 1;
		}

		/**
		 * Set the upper 3x3 matrix based on the given the euler angles.
		 * 
		 * @param angles
		 *            The set of angles to use, one per axis
		 * @param mat
		 *            The matrix to set the values in
		 */
		public void setEuler(Vector3f angles, Matrix4f mat) {
			setEuler(angles.x, angles.y, angles.z, mat);
		}

		/**
		 * Set the upper 3x3 matrix based on the given the euler angles.
		 * 
		 * @param x
		 *            The amount to rotate around the X axis
		 * @param y
		 *            The amount to rotate around the Y axis
		 * @param z
		 *            The amount to rotate around the Z axis
		 * @param mat
		 *            The matrix to set the values in
		 */
		public void setEuler(float x, float y, float z, Matrix4f mat) {
			float a = (float) Math.cos(x);
			float b = (float) Math.sin(x);
			float c = (float) Math.cos(y);
			float d = (float) Math.sin(y);
			float e = (float) Math.cos(z);
			float f = (float) Math.sin(z);
			float a_d = a * d;
			float b_d = b * d;

			mat.m00 = c * e;
			mat.m01 = -c * f;
			mat.m02 = d;
			mat.m03 = 0;

			mat.m10 = b_d * e + a * f;
			mat.m11 = -b_d * f + a * e;
			mat.m12 = -b * c;
			mat.m13 = 0;

			mat.m20 = -a_d * e + b * f;
			mat.m21 = a_d * f + b * e;
			mat.m22 = a * c;
			mat.m23 = 0;

			mat.m30 = 0;
			mat.m31 = 0;
			mat.m33 = 1;
			mat.m32 = 0;
		}

		/**
		 * Set the matrix to the rotation about the X axis by the given angle.
		 * 
		 * @param angle
		 *            The angle to rotate in radians
		 * @param mat
		 *            The matrix to set the values in
		 */
		public void rotateX(float angle, Matrix4f mat) {
			float a = (float) Math.cos(angle);
			float b = (float) Math.sin(angle);

			mat.m00 = 1;
			mat.m01 = 0;
			mat.m02 = 0;
			mat.m03 = 0;

			mat.m10 = 0;
			mat.m11 = a;
			mat.m12 = -b;
			mat.m13 = 0;

			mat.m20 = 0;
			mat.m21 = b;
			mat.m22 = a;
			mat.m23 = 0;

			mat.m30 = 0;
			mat.m31 = 0;
			mat.m33 = 1;
			mat.m32 = 0;
		}

		/**
		 * Set the matrix to the rotation about the Y axis by the given angle.
		 * 
		 * @param angle
		 *            The angle to rotate in radians
		 * @param mat
		 *            The matrix to set the values in
		 */
		public void rotateY(float angle, Matrix4f mat) {
			float a = (float) Math.cos(angle);
			float b = (float) Math.sin(angle);

			mat.m00 = a;
			mat.m01 = 0;
			mat.m02 = b;
			mat.m03 = 0;

			mat.m10 = 0;
			mat.m11 = 1;
			mat.m12 = 0;
			mat.m13 = 0;

			mat.m20 = -b;
			mat.m21 = 0;
			mat.m22 = a;
			mat.m23 = 0;

			mat.m30 = 0;
			mat.m31 = 0;
			mat.m33 = 1;
			mat.m32 = 0;
		}

		/**
		 * Calculate the inverse of a 4x4 matrix and place it in the output. The
		 * implementation uses the algorithm from
		 * http://www.j3d.org/matrix_faq/matrfaq_latest.html#Q24
		 * 
		 * @param src
		 *            The source matrix to read the values from
		 * @param dest
		 *            The place to put the inverted matrix
		 * @return true if the inversion was successful
		 */
		public boolean inverse(Matrix4f src, Matrix4f dest) {
			float mdet = src.determinant();

			if (Math.abs(mdet) < 0.0000005f) {
				dest.setIdentity();
				return false;
			}
			if ((mdet > Float.MAX_VALUE) || (mdet < Float.MIN_VALUE)) {
				inversed(src);
			} else {
				mdet = 1 / mdet;

				// copy the matrix into an array for faster calcs
				tempMat4[0] = src.m00;
				tempMat4[1] = src.m01;
				tempMat4[2] = src.m02;
				tempMat4[3] = src.m03;

				tempMat4[4] = src.m10;
				tempMat4[5] = src.m11;
				tempMat4[6] = src.m12;
				tempMat4[7] = src.m13;

				tempMat4[8] = src.m20;
				tempMat4[9] = src.m21;
				tempMat4[10] = src.m22;
				tempMat4[11] = src.m23;

				tempMat4[12] = src.m30;
				tempMat4[13] = src.m31;
				tempMat4[14] = src.m32;
				tempMat4[15] = src.m33;

				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 4; j++) {
						int sign = 1 - ((i + j) % 2) * 2;
						submatrix(i, j);
						resMat4[i + j * 4] = determinant3x3() * sign * mdet;
					}
				}
			}
			// Now copy it back to the destination
			dest.m00 = resMat4[0];
			dest.m01 = resMat4[1];
			dest.m02 = resMat4[2];
			dest.m03 = resMat4[3];

			dest.m10 = resMat4[4];
			dest.m11 = resMat4[5];
			dest.m12 = resMat4[6];
			dest.m13 = resMat4[7];

			dest.m20 = resMat4[8];
			dest.m21 = resMat4[9];
			dest.m22 = resMat4[10];
			dest.m23 = resMat4[11];

			dest.m30 = resMat4[12];
			dest.m31 = resMat4[13];
			dest.m32 = resMat4[14];
			dest.m33 = resMat4[15];

			return true;
		}

		/**
		 * Perform the inverse calculation of the argument 4x4 matrix in double
		 * precision, placing the result in the class-level float temp result
		 * matrix.
		 * 
		 * @param src
		 *            The source matrix to read the values from
		 */
		private void inversed(Matrix4f src) {

			matrix4d.set(src);

			double mdet = matrix4d.determinant();

			mdet = 1 / mdet;

			// copy the matrix into an array for faster calcs
			tempMat4d[0] = matrix4d.m00;
			tempMat4d[1] = matrix4d.m01;
			tempMat4d[2] = matrix4d.m02;
			tempMat4d[3] = matrix4d.m03;

			tempMat4d[4] = matrix4d.m10;
			tempMat4d[5] = matrix4d.m11;
			tempMat4d[6] = matrix4d.m12;
			tempMat4d[7] = matrix4d.m13;

			tempMat4d[8] = matrix4d.m20;
			tempMat4d[9] = matrix4d.m21;
			tempMat4d[10] = matrix4d.m22;
			tempMat4d[11] = matrix4d.m23;

			tempMat4d[12] = matrix4d.m30;
			tempMat4d[13] = matrix4d.m31;
			tempMat4d[14] = matrix4d.m32;
			tempMat4d[15] = matrix4d.m33;

			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					int sign = 1 - ((i + j) % 2) * 2;
					submatrixd(i, j);
					resMat4[i + j * 4] = (float) (determinant3dx3d() * sign * mdet);
				}
			}
		}

		/**
		 * Find the 3x3 submatrix for the 4x4 matrix given the intial start and
		 * end positions. This uses the class-level temp matrices for input.
		 */
		private void submatrix(int i, int j) {
			// loop through 3x3 submatrix
			for (int di = 0; di < 3; di++) {
				for (int dj = 0; dj < 3; dj++) {
					// map 3x3 element (destination) to 4x4 element (source)
					int si = di + ((di >= i) ? 1 : 0);
					int sj = dj + ((dj >= j) ? 1 : 0);

					// copy element
					tempMat3[di * 3 + dj] = tempMat4[si * 4 + sj];
				}
			}
		}

		/**
		 * Find the 3x3 submatrix for the 4x4 matrix given the intial start and
		 * end positions. This uses the class-level double temp matrices for
		 * input.
		 */
		private void submatrixd(int i, int j) {
			// loop through 3x3 submatrix
			for (int di = 0; di < 3; di++) {
				for (int dj = 0; dj < 3; dj++) {
					// map 3x3 element (destination) to 4x4 element (source)
					int si = di + ((di >= i) ? 1 : 0);
					int sj = dj + ((dj >= j) ? 1 : 0);

					// copy element
					tempMat3d[di * 3 + dj] = tempMat4d[si * 4 + sj];
				}
			}
		}

		/**
		 * Calculate the determinant of the 3x3 temp matrix.
		 * 
		 * @return the determinant value
		 */
		private float determinant3x3() {
			return tempMat3[0] * (tempMat3[4] * tempMat3[8] - tempMat3[7] * tempMat3[5]) - tempMat3[1]
					* (tempMat3[3] * tempMat3[8] - tempMat3[6] * tempMat3[5]) + tempMat3[2]
					* (tempMat3[3] * tempMat3[7] - tempMat3[6] * tempMat3[4]);
		}

		/**
		 * Calculate the determinant of the 3x3 double temp matrix.
		 * 
		 * @return the determinant value
		 */
		private double determinant3dx3d() {
			return tempMat3d[0] * (tempMat3d[4] * tempMat3d[8] - tempMat3d[7] * tempMat3d[5]) - tempMat3d[1]
					* (tempMat3d[3] * tempMat3d[8] - tempMat3d[6] * tempMat3d[5]) + tempMat3d[2]
					* (tempMat3d[3] * tempMat3d[7] - tempMat3d[6] * tempMat3d[4]);
		}

		/**
		 * Perform a LookAt camera calculation and place it in the given matrix.
		 * If using this for a viewing transformation, you should invert() the
		 * matrix after the call.
		 * 
		 * @param eye
		 *            The eye location
		 * @param center
		 *            The place to look at
		 * @param up
		 *            The up vector
		 * @param res
		 *            The result to put the calculation into
		 */
		private void lookAtFallback(Point3f eye, float evX, float evY, float evZ, Matrix4f res) {
			// cross product of the -Z axis and the direction vector (ev?
			// params).
			// This gives the rotation axis to put into the matrix. Since we
			// know
			// the original -Z axis is (0, 0, -1) then we can skip a lot of the
			// calculations to be done.
			float rot_x = evY; // 0 * evZ - -1 * evY;
			float rot_y = -evX; // -1 * evX - 0 * evZ;
			float rot_z = 0; // 0 * evY - 0 * evX;

			// Angle is cos(theta) = (A / |A|) . (B / |B|)
			// A is the -Z vector. B is ev? values that need to be normalised
			// first
			float n = evX * evX + evY * evY + evZ * evZ;
			if (n != 0 || n != 1) {
				n = 1 / (float) Math.sqrt(n);
				evX *= n;
				evY *= n;
				evZ *= n;
			}

			float dot = -evZ; // 0 * evX + 0 * evY + -1 * evZ;
			float angle = (float) Math.acos(dot);

			if (orient == null)
				orient = new AxisAngle4f(rot_x, rot_y, rot_z, angle);
			else
				orient.set(rot_x, rot_y, rot_z, angle);

			res.set(orient);
			res.m03 = eye.x;
			res.m13 = eye.y;
			res.m23 = eye.z;
		}

		/**
		 * Perform a LookAt camera calculation and place it in the given matrix.
		 * If using this for a viewing transformation, you should invert() the
		 * matrix after the call.
		 * 
		 * @param eye
		 *            The eye location
		 * @param center
		 *            The place to look at
		 * @param up
		 *            The up vector
		 * @param res
		 *            The result to put the calculation into
		 */
		private void lookAtFallback(Point3d eye, double evX, double evY, double evZ, Matrix4d res) {
			// cross product of the -Z axis and the direction vector (ev?
			// params).
			// This gives the rotation axis to put into the matrix. Since we
			// know
			// the original -Z axis is (0, 0, -1) then we can skip a lot of the
			// calculations to be done.
			double rot_x = evY; // 0 * evZ - -1 * evY;
			double rot_y = -evX; // -1 * evX - 0 * evZ;
			double rot_z = 0; // 0 * evY - 0 * evX;

			// Angle is cos(theta) = (A / |A|) . (B / |B|)
			// A is the -Z vector. B is ev? values that need to be normalised
			// first
			double n = evX * evX + evY * evY + evZ * evZ;
			if (n != 0 || n != 1) {
				n = 1 / (double) Math.sqrt(n);
				evX *= n;
				evY *= n;
				evZ *= n;
			}

			double dot = -evZ; // 0 * evX + 0 * evY + -1 * evZ;
			double angle = (double) Math.acos(dot);

			if (orientd == null)
				orientd = new AxisAngle4d(rot_x, rot_y, rot_z, angle);
			else
				orientd.set(rot_x, rot_y, rot_z, angle);

			res.set(orientd);
			res.m03 = eye.x;
			res.m13 = eye.y;
			res.m23 = eye.z;
		}

	}

}
