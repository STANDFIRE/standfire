/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.sketch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.defaulttype.Type;
import jeeb.lib.sketch.kernel.AddInfo;
import jeeb.lib.sketch.kernel.BuiltinType;
//import jeeb.lib.sketch.kernel.ItemManager;
import jeeb.lib.sketch.kernel.SimpleAddInfo;
import jeeb.lib.sketch.kernel.SketchEvent;
import jeeb.lib.sketch.kernel.SketchFacade;
import jeeb.lib.sketch.kernel.SketchLinker;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.sketch.scene.item.TreeWithCrownProfileItem;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.sketch.scene.terrain.Plane;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import standfire.model.SFModel;
import standfire.model.SFScene;
import standfire.model.SFTree;
import capsis.kernel.GModel;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.sketcher.FiLayerSetSketcher;

/**
 * Makes the link with a Sketch SceneModel (Sketchy 3D editors / viewers).
 * Contains a method to update the SketchModel from a Scene object of Standfire.
 * Listens to the SketchModel to update the Scene (if editable) when items are
 * added / removed in the SketchModel.
 * 
 * @author F. Pimont - September 2013
 */
public class SFSketchLinker implements SketchLinker<SFScene, SceneModel> {

	private boolean initialized;

	private Set<Type> availableTypes;

	private SFModel userModel;
	private SFScene userScene;
	private SceneModel sceneModel;

	private Collection updatedUserObjects;
	private Collection updatedItems;

	/**
	 * Constructor. It is possible to add extensions to the sketch extension
	 * manager here.
	 */
	public SFSketchLinker(GModel userModel) {
		this.userModel = (SFModel) userModel;
	}

	/**
	 * The list of item types the linked module knows and handles.
	 */
	public Set<Type> getAvailableTypes() {
		return availableTypes;
	}

	/**
	 * This must be called when updateSketch is called for the first time on a
	 * SceneModel.
	 */
	private void init(SketchFacade facade) {
		ClassLoader classLoader = this.getClass().getClassLoader();

		availableTypes = new HashSet<Type>();

		initialized = true;
	}

	/**
	 * Adds items for ALL the userObjects of the given userScene into the given
	 * sketchModel. At first time, registers to the SketchModel to know about
	 * sketchHappenings. To 'copy' the user scene into the given sketchModel: 1.
	 * sketchModel.setSketchLinker (null); // temporary, to avoid loops 2. set
	 * the SketchModel empty (leave the grid...) and create / add items for ALL
	 * the user objects in the user scene (terrain, trees...) 4.
	 * sketchModel.setSketchLinker (this); // restore
	 */
	public void updateSketch(SFScene maddScene, SceneModel sceneModel) throws Exception {
		this.userScene = maddScene;
		this.sceneModel = sceneModel;

		// Add all the trees of the scene into the sceneModel
		updateSketch(maddScene, maddScene.getTrees(), sceneModel);

	}

	/**
	 * Adds items for ONLY the given userObjects into the given sketchModel. The
	 * given userObjects are part of the given userScene. This can be used when
	 * we want to see only a selection in 3D. To add the entire scene in the
	 * sketchModel, see updateSketch (S userScene, M sketchModel). Note: a
	 * userScene should be composed of XYZBBox elements, the Collection should
	 * contain such elements.
	 */
	public void updateSketch(SFScene maddScene, Collection userObjects, SceneModel sceneModel) throws Exception {

		// ENHANCEMENT NEEDED: init if this sceneModel is 'new' (first time we
		// update in it)
		if (!initialized) {
			init(sceneModel.getFacade());
		}

		// Unconnect sketchModel
		sceneModel.setSketchLinker(this, null); // controller, linker

		this.userScene = maddScene;
		this.sceneModel = sceneModel;

		System.out.println();
		System.out.println("SF Linker - update Sketch");
		System.out.println("SFStand...   " + userScene);
		System.out.println("trees...      " + userObjects.size());
		System.out.println("SceneModel... " + sceneModel);

		// clear the scene
		// boolean includingTechnicalItems = false;
		// sceneModel.getUndoManager ().undoableRemoveAllItems (this,
		// includingTechnicalItems);
		sceneModel.clearModel(this);

		System.out.println("Cleared the sceneModel");
		System.out.println(sceneModel.toString2());

		// Terrain
		// add a plane terrain
		SFTree tree = (SFTree) userObjects.iterator().next(); // 1st tree
		SFScene stand = (SFScene) tree.getScene();
		double x = stand.getOrigin().x;
		double y = stand.getOrigin().y;
		double z = stand.getOrigin().z;
		double w = stand.getXSize();
		double h = stand.getYSize();
		List<Vertex3d> vertices = new ArrayList<Vertex3d>();
		vertices.add(new Vertex3d(x, y, z));
		vertices.add(new Vertex3d(x, y + h, z));
		vertices.add(new Vertex3d(x + w, y + h, z));
		vertices.add(new Vertex3d(x + w, y, z));
		double altitude = z;

		Plane plane = new Plane(altitude, vertices);
		sceneModel.setTerrain(this, plane);

		// We consider all the elements in userObjects are SFTree instances
		// Trees
		Type type = new BuiltinType("BuiltinType.TREE_WITH_CROWN_PROFILE",
				jeeb.lib.sketch.scene.extension.sketcher.TreeWithCrownProfileSketcher.class); // preferred
																								// sketcher

		Collection<SFTree> trees = new ArrayList<SFTree>();
		for (Object o : userObjects) {
			if (o instanceof SFTree) {
				trees.add((SFTree) o);
			}
		}

		updatedUserObjects = trees;

		System.out.println("...#trees " + trees.size());

		Collection<Item> items = new ArrayList<Item>();
		for (SFTree t : trees) {
			// ~ items.add (new TreeItem (t, type));
			items.add(new TreeWithCrownProfileItem(t, type));
		}
		updatedItems = items;

		AddInfo addInfo = new SimpleAddInfo(type, items);
		sceneModel.addItems(this, addInfo);

		System.out.println("Added the terrain and trees");
		System.out.println(sceneModel.toString2());

		// Adding layer sets // fc-7.11.2014
		Collection<FiLayerSet> layerSets = stand.getLayerSets();
		if (layerSets != null && !layerSets.isEmpty()) {
			Collection<Polygon> polygons = new ArrayList<Polygon>();
//			for (FiLayerSet ls : layerSets) {
//				// FiLayerSet extends Sketch Polygon
//				Polygon p = ls;
//				polygons.add(p);
//
//			}
			// Vegetation layers
			Type polygonType = new BuiltinType(Translator.swap ("SFSketchLinker.vegetationLayers"),
					FiLayerSetSketcher.class);

			addInfo = new SimpleAddInfo(polygonType, getItems(layerSets));
			sceneModel.getUndoManager().undoableAddItems(this, addInfo);
			updatedUserObjects.add(polygons);

			// updatedItems = updatedUserObjects;

			System.out.println("Added the layerSets");
		} else {
			System.out.println("Found no layerSets");
		}
		//

		// fc - 21.10.2008 - reset selection / clear undo stack
		sceneModel.getUndoManager().undoableResetSelection(this);
		sceneModel.getUndoManager().clearMemory(); // fc - 4.3.2008 - clear
													// undo/redo stacks

		System.out.println("Reset selection and memory ");
		System.out.println();

		// Cconnect sketchModel
		sceneModel.setSketchLinker(this, this); // controller, linker

	}

	/**
	 * Problem with java generics. This is because we cannot cast getTree ()
	 * into a Collection<AbstractItem>.
	 */
	private Collection<Item> getItems(Collection c) {
		Collection<Item> items = new ArrayList<Item>();
		for (Object o : c) {
			items.add((Item) o);
		}
		return items;
	}

	/**
	 * Returns the userObjects turned into items during the last updateSketch
	 * ().
	 */
	public Collection getUpdatedUserObjects() {
		return updatedUserObjects;
	}

	/**
	 * Returns the items added into the sketchModel during the last updateSketch
	 * ().
	 */
	public Collection getUpdatedItems() {
		return updatedItems;
	}

	/**
	 * SketchListener interface Called by the SketchModel when items are added,
	 * removed, updated or when selection changed. Also service messages. See
	 * SketchEvent for details.
	 */
	public void sketchHappening(SketchEvent e) {
		this.sceneModel = (SceneModel) e.getModel();

		// ~ if (e.getType ().equals (SketchEvent.ITEMS_ADDED)) {
		// ~ Set<AbstractItem> items = (Set<AbstractItem>) e.getParameter ();
		// ~ itemsAdded (sceneModel, items);

		// ~ } else if (e.getType ().equals (SketchEvent.ITEMS_REMOVED)) {
		// ~ Set<AbstractItem> items = (Set<AbstractItem>) e.getParameter ();
		// ~ itemsRemoved (sceneModel, items);

		// ~ } else if (e.getType ().equals (SketchEvent.ITEMS_UPDATED)) {
		// ~ Set<AbstractItem> items = (Set<AbstractItem>) e.getParameter ();
		// ~ itemsUpdated (sceneModel, items);

		// ~ } else if (e.getType ().equals (SketchEvent.SELECTION_CHANGED)) {
		// ~ Set[] t = (Set[]) e.getParameter ();
		// ~ selectionChanged (sceneModel, t[0], t[1]); // selectedItems,
		// deselectedItems
		// ~ }
		// ~ System.out.println (userScene.getState ());
	}

	/**
	 * Returns the userModel, subclass of GModel. Was given to the constructor.
	 */
	public SFModel getUserModel() {
		return userModel;
	}

	/**
	 * Returns the current userScene, subclass of GStand. This is the last
	 * userScene given to one of the updateSketch () methods.
	 */
	public SFScene getUserScene() {
		return userScene;
	}

	/**
	 * Returns the SketchModel this linker talks to, i.e. SceneModel or
	 * ArchiModel. This is the last sketchModel given to one of the updateSketch
	 * () methods.
	 */
	public SceneModel getSketchModel() {
		return sceneModel;
	}

	/**
	 * Ensures deregistration to the SketchModel
	 */
	public void destroy() {
		// maybe not needed, see setSketchLinker () in SceneModel (named
		// setSketchLinker () temporarily)
	}

	// /** Returns true if the given scene is editable.
	// */
	// @Override
	// public boolean isEditable(SFScene userScene) {
	// return false;
	// }

}
