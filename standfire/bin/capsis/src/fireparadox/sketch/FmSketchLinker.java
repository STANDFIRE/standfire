package fireparadox.sketch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.defaulttype.TreeWithCrownProfile;
import jeeb.lib.defaulttype.Type;
import jeeb.lib.sketch.kernel.AddInfo;
import jeeb.lib.sketch.kernel.BuiltinType;
import jeeb.lib.sketch.kernel.SimpleAddInfo;
import jeeb.lib.sketch.kernel.SketchEvent;
import jeeb.lib.sketch.kernel.SketchFacade;
import jeeb.lib.sketch.kernel.SketchLinker;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.sketch.scene.item.SceneItem;
import jeeb.lib.sketch.scene.item.TreeWithCrownProfileItem;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.sketch.scene.terrain.Plane;
import jeeb.lib.util.Vertex3d;
import capsis.kernel.Cell;
import capsis.kernel.GModel;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;

/**	Makes the link with a Sketch SceneModel.
*	Contains a method to update the SketchModel from a FiStand.
*	Listens to the SketchModel to update the FiStand (if editable) 
*	when items are added / removed in the SketchModel.
*	@author F. de Coligny - september 2009
*/
public class FmSketchLinker implements SketchLinker<FmStand,SceneModel> {
	
	private boolean initialized;
	private boolean ignoreSketchHappening;
	
	private Set<Type> availableTypes;

	private FmModel userModel;
	private FmStand userScene;
	private SceneModel sceneModel;
	
	private Polygon pendingPolygon;
	
	private Collection updatedUserObjects;
	private Collection updatedItems;
	
	
	/**	Constructor.
	*	It is possible to add extensions to the sketch extension manager here.
	*/
	public FmSketchLinker (GModel userModel) {
		this.userModel = (FmModel)userModel;
		updatedUserObjects = new ArrayList ();
		updatedItems = new ArrayList ();
	}

	/**	The list of item types the linked module knows and handles.
	 */
	public Set<Type> getAvailableTypes () {
		return availableTypes;
	}

	/**	This must be called when updateSketch is called for the
	*	first time on a SceneModel.
	*/
	private void init (SketchFacade facade) {
		ClassLoader classLoader = this.getClass ().getClassLoader ();
		//~ facade.addExtensionFile (classLoader, "jeeb/simeo/work/amapscene/amapscene.extensions");
		//~ facade.addType (new AMAPLineTreeType ());
		
		availableTypes = new HashSet<Type> ();
		availableTypes.add(new FmPlantType (userModel));
		availableTypes.add(new FmLayerSetType ());

		initialized = true;
	}
	
	/**	Adds items for ALL the userObjects of the given userScene into the given sceneModel.
	*	At first time, registers to the SketchModel to know about sketchHappenings.
	*	To 'copy' the user scene into the given sketchModel:
	*	1. sceneModel.setSketchLinker (null);	// temporary, to avoid loops
	*	2. set the SketchModel empty (leave the grid...) and create / add items 
	*	for ALL the user objects in the user scene (terrain, trees...)
	*	4. sceneModel.setSketchLinker (this);	// restore
	*/
	public void updateSketch (FmStand fiScene, SceneModel sceneModel) throws Exception {
		this.userScene = fiScene;
		this.sceneModel = sceneModel;
		
		// Add all the trees of the scene into the sceneModel
		updateSketch (fiScene, fiScene.getTrees (), sceneModel);
		
	}

	/**	Adds items for ONLY the given userObjects into the given sceneModel.
	*	The given userObjects are part of the given userScene.
	*	This can be used when we want to see only a selection in 3D.
	*	To add the entire scene in the sceneModel, see updateSketch (S userScene, M sceneModel).
	*	Note: a userScene should be composed of XYZBBox elements, the Collection 
	*	should contain such elements.
	*/
	public void updateSketch (FmStand fiScene, Collection userObjects, SceneModel sceneModel) throws Exception {
		System.out.println ("-- entering FmSketchLinker updateSketch ()...");
		
		// ENHANCEMENT NEEDED: init if this sceneModel is 'new' (first time we update in it)
		if (!initialized) {init (sceneModel.getFacade ());}
		
		// Unconnect sketchModel
		//~ sceneModel.setSketchLinker (this, null);	// controller, linker
		ignoreSketchHappening = true;
		
		Collection plants = extractTrees (userObjects);

		// TEMPORARY: fiScene may be null when this method is called by an ObjectViewer
		// -> get the fiScene from the first plant in userObjects
		if (fiScene == null) {
			try {
				FiPlant plant = (FiPlant) plants.iterator ().next ();	// 1st tree
				fiScene = (FmStand) plant.getScene ();
			} catch (Exception e) {
				// Maybe no trees, try with the cells
				Collection cells = extractCells (userObjects);
				Cell cell = (Cell) cells.iterator ().next ();	// 1st cell
				fiScene = (FmStand) cell.getPlot ().getScene ();
			}
		}

		this.userScene = fiScene;
		this.sceneModel = sceneModel;
		
//		System.out.println ();
//		System.out.println ("Fi Linker - update Sketch");
//		System.out.println ("FiStand...   "+userScene);
//		System.out.println ("SceneModel... "+sceneModel);
//		System.out.println ("plants... n="+(plants == null ? 0 : plants.size ()));
		
		// Clear the scene
//		boolean includingTechnicalItems = false;
//		sceneModel.getUndoManager ().undoableRemoveAllItems (this, includingTechnicalItems);
		sceneModel.clearModel(this);		
		
		
		updatedUserObjects.clear ();
		updatedItems.clear ();
		
		System.out.println ("Cleared the sceneModel");
		System.out.println (sceneModel.toString2 ());
		
		// Terrain
		// add a plane terrain
		FmPlot plot = fiScene.getPlot ();
		Plane plane = new Plane (plot.getAltitude (), new ArrayList<Vertex3d> (plot.getVertices ()));
		(sceneModel).setTerrain (this, plane);
		
		// Trees
			Type treeType = new FmPlantType (userModel);
			
			updatedUserObjects.addAll (plants);
			
			Collection<Item> items = new ArrayList<Item> ();
			for (Object o : plants) {
				FiPlant p = (FiPlant) o;
				items.add (new TreeWithCrownProfileItem (p, treeType));
			}
			updatedItems.addAll (items);
			
			AddInfo addInfo = new SimpleAddInfo (treeType, new ArrayList<Item> (items));
			sceneModel.getUndoManager ().undoableAddItems (this, addInfo);
		
		// LayerSets
		if (fiScene.getLayerSets () != null && !fiScene.getLayerSets ().isEmpty ()) {
			
System.out.println ("FmSketchLinker ------------");
System.out.println ("fiScene "+fiScene);
	for (FiLayerSet ls : fiScene.getLayerSets ()) {
		System.out.println ("   LayerSet: "+ls);
		System.out.println ("     vertices "+ls.getVertices ());
		
	}
System.out.println ("FmSketchLinker ------------");
			
			
			Type layerSetType = new FmLayerSetType ();
			addInfo = new SimpleAddInfo (layerSetType, getItems (fiScene.getLayerSets ()));
			sceneModel.getUndoManager ().undoableAddItems (this, addInfo);
				updatedUserObjects.add (fiScene.getLayerSets ());
		}
		
		// Polygons
		Type polygonType = new BuiltinType ("BuiltinType.POLYGON", 
				jeeb.lib.sketch.scene.extension.sketcher.ContourSketcher.class);
		
		addInfo = new SimpleAddInfo (polygonType, getItems (plot.getPolygons ()));
		sceneModel.getUndoManager ().undoableAddItems (this, addInfo);
			updatedUserObjects.add (plot.getPolygons ());

//		updatedItems = updatedUserObjects;

		System.out.println ("Added the terrain, trees, layerSets and polygons");
		System.out.println (sceneModel.toString2 ());
		
		// Reset selection / clear undo stack
		sceneModel.getUndoManager ().undoableResetSelection (this);
		sceneModel.getUndoManager ().clearMemory ();	// fc - 4.3.2008 - clear undo/redo stacks
	
		System.out.println ("Reseted selection and memory ");
		System.out.println ("-- end of FmSketchLinker updateSketch () ");
		System.out.println ();
		
		// Cconnect sketchModel
		//~ sceneModel.setSketchLinker (this, this);	// controller, linker
		ignoreSketchHappening = false;
		
	}
	
	/**	Returns the userObjects turned into items during the 
	*	last updateSketch ().
	*/
	public Collection getUpdatedUserObjects () {return updatedUserObjects;}
	
	/**	Returns the items added into the sceneModel during the 
	*	last updateSketch ().
	*/
	public Collection getUpdatedItems () {return updatedItems;}
	
	/** SketchListener interface
	*	Called by the SketchModel when items are added, removed, 
	*	updated or when selection changed. Also service messages.
	*	See SketchEvent for details.
	*/
	public void sketchHappening (SketchEvent evt) {
		if (ignoreSketchHappening) {return;}
		
		this.sceneModel = (SceneModel) evt.getModel ();
		
		if (evt.getType ().equals (SketchEvent.ITEMS_ADDED)) {
			Set<SceneItem> items = (Set<SceneItem>) evt.getParameter ();
			itemsAdded (items);

		} else if (evt.getType ().equals (SketchEvent.ITEMS_REMOVED)) {
			Set<SceneItem> items = (Set<SceneItem>) evt.getParameter ();
			itemsRemoved (items);

		} else if (evt.getType ().equals (SketchEvent.ITEMS_UPDATED)) {
			
			if (evt.isAdjusting ()) return; // fc-5.9.2011
			
			Set<SceneItem> items = (Set<SceneItem>) evt.getParameter ();
			itemsUpdated (items);

		} else if (evt.getType ().equals (SketchEvent.SELECTION_CHANGED)) {
			Set[] t = (Set[]) evt.getParameter ();
			// selectionChanged (t[0], t[1]);	// selectedItems, deselectedItems
			// nothing at present time
		}

	}

	/**	Items were added in the SketchModel.
	*	HOMOGENEOUS item set: all items are of the same class
	*/
	private void itemsAdded (Set<SceneItem> items) {
		if (items == null || items.isEmpty ()) {return;}
//		System.out.println ("FiStand.addItems...");

		for (SceneItem item : items) {
			if (item instanceof TreeWithCrownProfileItem) {
				TreeWithCrownProfileItem i = (TreeWithCrownProfileItem) item;
				FiPlant t = (FiPlant) i.getTree ();
				if (t.getId () <= 0) {	// not set yet, set it now
					t.setId (userModel.getTreeIdDispenser ().getNext ());
				}
				userScene.addTree (t);
				t.setScene (userScene);
				
			} else if (item instanceof FiLayerSet) {	// fc - 17.11.2008
				FiLayerSet ls = (FiLayerSet) item;
				if (ls.getId () <= 0) {	// not set yet, set it now
					ls.setId (userModel.getLayerSetIdDispenser ().getNext ());
				}
				userScene.addLayerSet (ls);
			} else if (item instanceof Polygon) {

				pendingPolygon = (Polygon) item;

			}
		}

//		System.out.println ("end-of-FiStand.addItems");
	}

	/**	Items were removed from the SketchModel.
	*	All items are of the same class
	*/
	private void itemsRemoved (Set<SceneItem> items) {
		if (items == null || items.isEmpty ()) {return;}
//		System.out.println ("FiStand.removeItems...");
		FmPlot plot = userScene.getPlot ();
		
		for (SceneItem item : items) {
			if (item instanceof TreeWithCrownProfileItem) {
				TreeWithCrownProfileItem i = (TreeWithCrownProfileItem) item;
				FiPlant t = (FiPlant) i.getTree ();
//			if (item instanceof FiPlant) {
//				FiPlant t = (FiPlant) item;
				userScene.removeTree (t);

			} else if (item instanceof FiLayerSet) {	// fc - 17.11.2008
				FiLayerSet l = (FiLayerSet) item;
				userScene.removeLayer (l);

			} else if (item instanceof Polygon) {
				Polygon p = (Polygon) item;

				for (Iterator i = plot.getPolygons ().iterator (); i.hasNext ();) {
					Polygon polygon = (Polygon) i.next ();

					if (polygon.equals (p)) {	// reviewed - fc - 23.9.2008
						System.out.println ("equal");
						i.remove ();
					}
				}
			}
		}

//		System.out.println ("end-of-FiStand.removeItems");
	}

	/**	Items were updated in the SketchModel.
	*	All items are of the same class
	*/
	private void itemsUpdated (Set<SceneItem> items) {
		if (items == null || items.isEmpty ()) {return;}
		System.out.println ("FmSketchLinker.itemsUpdated...");
		FmPlot plot = userScene.getPlot ();

		for (SceneItem item : items) {
			if (item instanceof Polygon) {
				Polygon polygon = (Polygon) item;

				if (pendingPolygon != null
						&& polygon.isClosed ()
						&& polygon.getItemId () == pendingPolygon.getItemId ()
						) {

//						System.out.println ("adding a FirePolygon to FiPlot...");
					plot.add (pendingPolygon);

					pendingPolygon = null;
				}

			}
		}

//		System.out.println ("end-of-FiStand.itemsUpdated");
	}

	/**	Returns the userModel, subclass of GModel.
	*	Was given to the constructor.
	*/
	public FmModel getUserModel () {return userModel;}

	/**	Returns the current userScene, subclass of GStand.
	*	This is the last userScene given to one of the updateSketch () methods.
	*/
	public FmStand getUserScene () {return userScene;}

	/**	Returns the SketchModel this linker talks to, i.e. SceneModel or ArchiModel.
	*	This is the last sketchModel given to one of the updateSketch () methods.
	*/
	public SceneModel getSketchModel () {return sceneModel;}
	
	/**	Ensures deregistration to the SketchModel
	*/
	public void destroy () {
		// maybe not needed, see setSketchLinker () in SceneModel (named setSketchLinker () temporarily)
	}

	/**	Problem with java generics.
	*	This is because we cannot cast getTree () into a Collection<AbstractItem>.
	*/
	private Collection<Item> getItems (Collection c) {
		Collection<Item> items = new ArrayList<Item> ();
		for (Object o : c) {
			items.add ((Item) o);
		}
		return items;
	}

	/**	Extracts the trees instance of TreeWithCrownProfile of the 
	*	given collection and return them in another Collection (may be empty).
	*/
	private Collection extractTrees (Collection userObjects) {
		Collection trees = new ArrayList ();
		for (Object o : userObjects) {
			if (o instanceof TreeWithCrownProfile) {trees.add (o);}
		}
		return trees;
	}

	/**	Extracts the cells from the 
	*	given collection and return them in another Collection (may be empty).
	*/
	private Collection extractCells (Collection userObjects) {
		Collection cells = new ArrayList ();
		for (Object o : userObjects) {
			if (o instanceof Cell) {cells.add (o);}
		}
		return cells;
	}

//	/**	Returns true if the given scene is editable.
//	 */
//	@Override
//	public boolean isEditable(FiStand userScene) {
//		return true;
//	}
	



}


