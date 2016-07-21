package fireparadox.model;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.TicketDispenser;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import jeeb.lib.util.task.TaskManager;
import capsis.commongui.util.Tools;
import capsis.defaulttype.RoundMask;
import capsis.defaulttype.Tree;
import capsis.kernel.EvolutionParameters;
import capsis.kernel.GScene;
import capsis.kernel.InitialParameters;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.extension.ioformat.FiLocalLayerModelLoader;
import fireparadox.extension.ioformat.FiLocalLayerSetModelLoader;
import fireparadox.extension.ioformat.FireDBFileLoader;
import fireparadox.extension.ioformat.FireDVOLoader;
import fireparadox.extension.ioformat.FireDVOLoader2;
import fireparadox.extension.ioformat.FireDVOLoaderFromFieldParameters;
import fireparadox.extension.ioformat.FireSVSLoader;
import fireparadox.model.database.FmBufferingTask;
import fireparadox.model.database.FmDBCheck;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBCountry;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmDBUpdator;
import fireparadox.model.database.FmLayerSyntheticDataBaseData;
import fireparadox.model.database.FmPlantSyntheticData;
import fireparadox.model.layerSet.FmLayerSet;
import fireparadox.model.layerSet.FmLocalLayerModels;
import fireparadox.model.layerSet.FmLocalLayerSetModels;
import fireparadox.model.plant.FmPlant;
import fireparadox.model.plant.fmgeom.FmGeomList;
import fireparadox.model.plant.fmgeom.FmGeomMap;

/**
 * FiModel is the main class for module FireParadox
 * 
 * @author O. Vigy, E. Rigaud - september 2006
 */
public class FmModel extends FiModel implements Listener, ListenedTo {

	public static final String LEAVE_LIVE = "Leave_Live";
	public static final String LEAVE_DEAD = "Leave_Dead";
	public static final String TWIG1_LIVE = "Twig1_Live";
	public static final String TWIG1_DEAD = "Twig1_Dead";
	public static final String TWIG2_LIVE = "Twig2_Live";
	public static final String TWIG2_DEAD = "Twig2_Dead";

	// Local plant species, see dispatchSpeciesList ()
	public static final String JUNIPER_TREE = "Juniper tree";
	public static final String PICEA_MARIANA = "Picea mariana";
	public static final String PICEA_MARIANA_DEAD = "Picea mariana dead";
	public static final String PINUS_BANKSIANA = "Pinus banksiana";
	public static final String PINUS_BANKSIANA_DEAD = "Pinus banksiana dead";
	public static final String PINUS_BRUTIA = "Pinus brutia";
	public static final String PINUS_HALEPENSIS = "Pinus halepensis";
	public static final String PINUS_NIGRA_LARICIO = "Pinus nigra subsp laricio";
	public static final String PINUS_NIGRA = "Pinus nigra";
	public static final String PINUS_PINASTER = "Pinus pinaster";
	public static final String PINUS_PINASTER_NAVAS = "Pinus pinaster navas";
	public static final String PINUS_PINASTER_TELENO = "Pinus pinaster teleno";
	public static final String PINUS_PINEA = "Pinus pinea";
	public static final String PINUS_PONDEROSA_LANL = "Pinus ponderosa lanl";
	public static final String PINUS_PONDEROSA_USFS1 = "Pinus ponderosa usfs1";
	public static final String PINON_PINE_DEAD = "Pinon pine dead";
	public static final String PINON_PINE = "Pinon pine";
	public static final String PINUS_SYLVESTRIS = "Pinus sylvestris";
	public static final String QUERCUS_ILEX = "Quercus ilex";
	public static final String QUERCUS_PUBESCENS = "Quercus pubescens";

	// Local layer species, see dispatchSpeciesList ()
	public static final String NEW_SAMPLE = "New sample";
	public static final String AUSTRALIAN_GRASSLAND = "Australian grassland";
	public static final String HERBACEOUS = "Herbaceous";
	public static final String BRACHYPODIUM_RAMOSUM = "Brachypodium ramosum";
	public static final String QUERCUS_COCCIFERA = "Quercus coccifera";
	public static final String ROSMARINUS_OFFICINALIS = "Rosmarinus officinalis";
	// public static final String LANL_LITTER = "Lanl litter (below trees)";
	// public static final String LANL_GRASS = "Lanl grass (avoid trees)";

	transient public FmLocalLayerModels localLayerModels;
	transient public FmLocalLayerSetModels localLayerSetModels;

	// Constant for damage
	public static final String CROWN_SCORCHED = "crown scorched";
	public static final String BUD_KILLED = "bud killed";

	// Tested only once at project starting time and for all the simulation time
	private boolean networkAvailable;

	private TicketDispenser layerSetIdDispenser;
	private TicketDispenser speciesIdDispenser; // fc - 28.9.2009
	private FiSpecies speciesSpecimen; // needed for FiSpecies instances
	// interconnection
	//private Random random; // a random number generator

	private FmGeomMap patternMap;
	private FmGeomList patternList;

	// fc - 5.5.2008 - added transient : during Serialization, the instance
	// variable is not saved, null at reloading
	transient private FmDBCommunicator bdCommunicator;
	transient private FmDBUpdator bdUpdator;

	transient private FmBufferingTask bufferingTask; // this task will load
	// buffers
	private transient Collection<Listener> listeners; // the listeners will be
	// told when buffers are
	// loaded

	transient private Map<String, FiSpecies> speciesMap; // FP: this is the main
	// speciesMap
	// A speciesMap for species in the database + local
	// ex: local trees with species not in the database
	// ex: DVOLoader species when network not avaialble
	// fc - 28.9.2009

	// list and map of species (db: database, local: local, and all)
	transient private Collection<FiSpecies> dbSpeciesList;
	transient private Collection<FiSpecies> localTreeSpeciesList;
	transient private Collection<FiSpecies> localLayerSpeciesList;
	// all= db+local:
	transient private Collection<FiSpecies> speciesList;

	transient private Collection<String> traitNames;
	transient private Collection<String> genusNames;
	transient private Collection<String> speciesNames;
	transient private LinkedHashMap<Long, FmDBTeam> teamMap;
	transient private Map<Long, FmPlantSyntheticData> plantSyntheticMap;
	transient private Map<Long, FmLayerSyntheticDataBaseData> layerSyntheticMap;
	transient private ArrayList<FmDBCheck> checkList;

	transient private LinkedHashMap<Long, FmDBCountry> countryMap;

	private FmDBTeam teamLogged; // team logged is owner of new data
	private int rightLevel; // 0=visitor, 2=team, 9=admin
	private String teamPassword; // password of the team logged

	// private FiStand initStand; // REMOVED (was tricky)

	// ~ transient private CallBack listener; // the initial dialog could be
	// there, transient needed for good Serialization

	/**
	 * Constructor.
	 */
	public FmModel() throws Exception {
		super();

		// fc-19.1.2015 this method needs to be recalled on project opening
		initParticleNames();
		// particleNames.add (LEAVE_LIVE);
		// particleNames.add (LEAVE_DEAD);
		// particleNames.add (TWIG1_LIVE);
		// particleNames.add (TWIG1_DEAD);
		// particleNames.add (TWIG2_LIVE);
		// particleNames.add (TWIG2_DEAD);

		setSettings(new FmInitialParameters(this));

		layerSetIdDispenser = new TicketDispenser();
		speciesIdDispenser = new TicketDispenser();
		// We need a specimen for later FiSpecies instances interconnection
		// ~ speciesSpecimen = new FiSpecies (speciesIdDispenser.next (), null,
		// "", "",
		// ~ FiModel.PINUS_HALEPENSIS, FiSpecies.SPECIES_TAXON_LEVEL);
		//random = new Random();
		// ~ localSpeciesMap = new HashMap<String,FiSpecies> ();

		// buffers were loaded here...

		// Here we get the properties of the localLayerSetModels and
		// localLayerModels
		String userDir = System.getProperty("user.dir", (String) null);
		String dirName = new File(userDir).getAbsolutePath();
		String fileName;

		// layerModels should be loaded first...
		try {
			fileName = dirName + "/data/fireparadox/fuelModels/layerModels";
			// ~ System.out.println("LayerModels in "+fileName);
			FiLocalLayerModelLoader loader = new FiLocalLayerModelLoader(fileName);
			localLayerModels = loader.load();
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiModel.c ()", "Error during loading of layerModels : ", e);
			return;
		}
		try {
			fileName = dirName + "/data/fireparadox/fuelModels/layerSetModels";
			// ~ System.out.println("LayerSetModels in "+fileName);
			FiLocalLayerSetModelLoader loader = new FiLocalLayerSetModelLoader(fileName);
			localLayerSetModels = loader.load(this);
		} catch (Exception e) {
			Log.println(Log.ERROR, "FiModel.c ()", "Error during loading of layerSetModels : ", e);
			return;
		}

		// MOVED UPPER
		// if (networkAvailable) {
		// // If network available, load buffers by connecting to the database
		// loadBuffers ();
		// } else {
		// StatusDispatcher.print (Translator.swap ("FiModel.offlineMode"));
		// }

		try {
			patternList = new FmGeomList();
			try {
				patternList.load();
			} catch (Exception e) {
			} // first time, no file - fc - 11.7.2007

			patternMap = new FmGeomMap(patternList);

			// TODO : FP temporary commented the following line
			// try {patternMap.load ();} catch (Exception e) {} // first time,
			// no file - fc -
			// 11.7.2007

			patternMap.setAdmin(false);
			patternList.setAdmin(false);

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiModel.c()", "Error during loading of the pattern list: ", e);
			throw new Exception(e.getMessage());
		}

	}

	/**
	 * Init the particule names in this method, will be called if a project is
	 * reopened (else, an error occurs).
	 */
	private void initParticleNames() {
		particleNames = new HashSet<String>();
		particleNames.add(LEAVE_LIVE);
		particleNames.add(LEAVE_DEAD);
		particleNames.add(TWIG1_LIVE);
		particleNames.add(TWIG1_DEAD);
		particleNames.add(TWIG2_LIVE);
		particleNames.add(TWIG2_DEAD);

	}

	/**
	 * This tries to connect to the database to load buffers to speed up the
	 * process of building the initial scene, particularly in the 3D editor. It
	 * may be unused in script mode.
	 */
	public void launchDataBaseBuffering() {

		// Check network connection, if yes, loadBuffers else
		// tell the user he is in offline mode
		networkAvailable = Tools.isNetworkAvailable();
		networkAvailable = false;
		Log.println("FiModel: networkAvailable: " + networkAvailable);
		// ~ System.out.println
		// ("FiModel: networkAvailable: "+networkAvailable);

		if (networkAvailable) {
			// If network available, load buffers by connecting to the database
			loadBuffers();
		} else {
			StatusDispatcher.print(Translator.swap("FiModel.offlineMode"));
		}

	}

	/**
	 * Evaluated at project creation time, will not be reevaluated during the
	 * project life time.
	 */
	public boolean isNetworkAvailable() {
		return networkAvailable;
	}

	// ~ public void setListener (CallBack lis) {listener = lis;}

	// ~ synchronized public void setSpeciesLoaded (boolean b) {speciesLoaded =
	// b;}
	// ~ synchronized public boolean isSpeciesLoaded () {return speciesLoaded;}
	// ~ synchronized public void setTeamsLoaded (boolean b) {teamsLoaded = b;}
	// ~ synchronized public boolean isTeamsLoaded () {return teamsLoaded;}

	// ~ synchronized public void setBuffersLoaded (boolean b) {buffersLoaded =
	// b;}
	synchronized public boolean isBuffersLoaded() {
		return networkAvailable
				&& (dbSpeciesList != null && teamMap != null && checkList != null && plantSyntheticMap != null && layerSyntheticMap != null);
	}

	/**
	 * Load information from the database and keep them in buffers.
	 */
	synchronized public void loadBuffers() {
		if (isBuffersLoaded()) {
			return;
		} // buffers were already loaded, continue

		// create the loading task, tell we want to be called at the end
		bufferingTask = new FmBufferingTask(Translator.swap("FiModel.bufferingTask"), this, speciesIdDispenser,
				speciesSpecimen);
		TaskManager.getInstance().add(bufferingTask); // starts the task

	}

	/**
	 * Called by ListenedTo when something happens.
	 */
	public void somethingHappened(ListenedTo l, Object param) {
		if (l.equals(bufferingTask)) {
			Integer rc = (Integer) param; // return code
			// ~ System.out.println
			// ("FiModel somethingHappened in bufferingTask, rc = "+rc);
			if (rc.intValue() == 0) { // ok
				// buffers loading was ok, get the loaded lists
				dbSpeciesList = bufferingTask.getSpeciesList();
				teamMap = bufferingTask.getTeamMap();
				checkList = bufferingTask.getCheckList();
				plantSyntheticMap = bufferingTask.getPlantSyntheticMap();
				layerSyntheticMap = bufferingTask.getLayerSyntheticMap();

				try {
					// fc-13.9.2013 update speciesSpecimen after buffers load
					speciesSpecimen = bufferingTask.getSpeciesList().iterator().next();
				} catch (Exception e) {
				}

				tracePlantSyntheticMap();
				traceLayerSyntheticMap();

			} else { // not ok
				Log.println(Log.ERROR, "FiModel.somethingHappened ()",
						"Error while loading buffers from the database, rc=" + rc);
				networkAvailable = false;
			}
			tellSomethingHappened(rc); // tell the listeners the return code (0
			// = ok)

		}
	}

	/**
	 * Trace to check the data base reading
	 */
	private void tracePlantSyntheticMap() {
		// ~ System.out.println ("FiModel, plantSyntheticMap...");
		// ~ if (plantSyntheticMap == null) {return;}
		// ~ System.out.println ("#entries="+plantSyntheticMap.size ());
		// ~ for (Long shapeId : plantSyntheticMap.keySet ()) {
		// ~ FiPlantSyntheticData data = plantSyntheticMap.get (shapeId);
		// ~ System.out.println ("shapeId="+shapeId+": "+data.toString2 ());
		// ~ }
	}

	/**
	 * Trace to check the data base reading
	 */
	private void traceLayerSyntheticMap() {
		// ~ System.out.println ("FiModel, layerSyntheticMap...");
		// ~ if (layerSyntheticMap == null) {return;}
		// ~ System.out.println ("#entries="+layerSyntheticMap.size ());
		// ~ for (FiLayerSyntheticDataBaseData d : layerSyntheticMap.values ())
		// {
		// ~ System.out.println (d.toString2 ());
		// ~ }
	}

	/**
	 * Build all the FiSpecies from the data base with their good value and name
	 */
	// ~ synchronized public void loadSpecies () {
	// ~ if (isSpeciesLoaded ()) {return;} // was already loaded, continue

	// ~ // fc + ov - 25.9.2007
	// ~ speciesListLoader = new Thread () {
	// ~ public void run () {
	// ~ try {
	// ~ getSpeciesList ();
	// ~ System.out.println ("******* getSpeciesList () is over ********");
	// ~ setSpeciesLoaded (true);
	// ~ if (listener != null) {listener.callBack (this,
	// ~ Translator.swap
	// ("FiSpeciesListLoader.speciesWereCorrectlyLoadedFromTheFuelDataBase"));}
	// // ... may stop some progress bar ...
	// ~ } catch (Exception e) {
	// ~ Log.println (Log.ERROR, "FiModel.c()",
	// "Error during first getSpeciesList () (maybe a network trouble): ", e);
	// ~ Alert.print (Translator.swap
	// ("FiModel.couldNotGetSpeciesListMaybeNetworkErrorSeeLog"));
	// ~ if (listener != null) {listener.callBack (this,
	// ~ Translator.swap
	// ("FiSpeciesListLoader.couldNotGetSpeciesListMaybeNetworkErrorSeeLog"));}
	// // ... may stop some progress bar ...
	// ~ }
	// ~ }
	// ~ };
	// ~ speciesListLoader.start ();
	// ~ }

	/**
	 * fc + ov - 10.7.2007 to retrieve the bdCommunicator,
	 * model.getBDCommunicator ();
	 */
	public FmDBCommunicator getBDCommunicator() {
		if (bdCommunicator == null) {
			// fc - 30.3.2008 - Singleton pattern
			bdCommunicator = FmDBCommunicator.getInstance();
		}
		return bdCommunicator;
	}

	/**
	 * fc + ov - 10.7.2007 to retrieve the bdUpdator, model.getBDUpdator ();
	 */
	public FmDBUpdator getBDUpdator() {
		if (bdUpdator == null) {
			// fc - 30.3.2008 - Singleton pattern
			bdUpdator = FmDBUpdator.getInstance();
		}
		return bdUpdator;
	}

	// /** Creates a MethodProvider for the module.
	// */
	// @Override
	// protected MethodProvider createMethodProvider () {
	// return new FiMethodProvider ();
	// }

	/**
	 * Load the inventory file given in parameter, instanciate the corresponding
	 * trees and add them to a returned stand. No access to the database here,
	 * only to view the scene (possibly without database)
	 */
	public GScene loadDetailedViewOnly(String fileName) throws Exception {
		return new FireDVOLoader(fileName).load(this);
	}

	/**
	 * Load the plots from field parameters (including ICFME experiment)
	 */
	public GScene loadFromFieldParameters(String fileName) throws Exception {
		return new FireDVOLoaderFromFieldParameters(fileName).load(this);
	}

	/**
	 * Load the inventory file given in parameter, instanciate the corresponding
	 * trees and add them to a returned stand. No access to the database here,
	 * only to view the scene (possibly without database) Case when trees are
	 * not given by a list or by number, but using COVER info // PhD 2008-09
	 */
	public GScene loadDetailedViewOnlyCover(String fileName) throws Exception {
		return new FireDVOLoader2(fileName, false).load(this);
	}

	public GScene loadDetailedViewOnlyCoverFull(String fileName) throws Exception { // PhD
		// 2009-02-03
		return new FireDVOLoader2(fileName, true).load(this);
	}

	public GScene loadFromSVS(String fileName) throws Exception {
		return new FireSVSLoader(fileName).load(this);
	}

	/**
	 * Load the file given in parameter, instanciate the corresponding trees by
	 * asking their properties to the database manager and add them to a
	 * returned stand. Needs the data base connection to ba available.
	 */
	public GScene loadDataBaseFile(String fileName) throws Exception {
		return new FireDBFileLoader(fileName).load(this);
	}

	/**
	 * This loader contains no tree, no layer, no polygon...
	 * 
	 * @param xorigin
	 * @param yorigin
	 * @param altitude
	 * @param xdim
	 * @param ydim
	 * @param cellWidth
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public GScene loadFromScratch(double xorigin, double yorigin, double altitude, double xdim, double ydim,
			double cellWidth, String source) throws Exception {
		FmStand initStand = new FmStand(this);
		// Ensure the cell width is at least 10m
		// (We have desactivated the trees registration in cells, see
		// FiStand.makeTreesPlotRegister () due to very big files in input -
		// ICFME)
		if (cellWidth < 10) {
			cellWidth = 10;
		}

		Vertex3d standOrigin = new Vertex3d(xorigin, yorigin, altitude);
		Rectangle.Double rectangle = new Rectangle.Double(xorigin, yorigin, xdim, ydim);

		initStand.setOrigin(standOrigin);
		initStand.setXSize(xdim);
		initStand.setYSize(ydim);
		initStand.setArea(xdim * ydim);

		// added by fp
		initStand.setDate(0);
		initStand.setSourceName(source); // generally fileName

		FmPlot plot = new FmPlot(initStand, "", cellWidth, altitude, rectangle, getSettings());
		initStand.setPlot(plot);
		// System.out.println("plot is set");

		return initStand;
	}

	/**
	 * This method adds a polygon (defined with an id and a collection of
	 * vertices) to a scene
	 * 
	 * @param id
	 * @param vertices
	 * @param current
	 * @return
	 * @throws Exception
	 */
	public GScene addPolygonTo(int id, Collection vertices, GScene current) throws Exception {
		FmStand initStand = (FmStand) current;
		Polygon p = new Polygon(new ArrayList<Vertex3d>(AmapTools.toVertex3dCollection(vertices)));
		p.setItemId(id);
		FmPlot plot = initStand.getPlot();
		plot.add(p);
		return current;
	}

	/**
	 * Create randomly distributed trees
	 */
	// ~ public Collection createTreesByRandomOption (String className,
	// ~ int treeNumber, int treeAge, Rectangle2D.Double bounds)
	// ~ throws Exception {

	// ~ Collection treePop = new ArrayList ();

	// ~ VirtualParameters vParam = new VirtualParameters ();
	// ~ vParam.virtualStandXmin = new Double (bounds.x);
	// ~ vParam.virtualStandXmax = new Double (bounds.x + bounds.width);
	// ~ vParam.virtualStandYmin = new Double (bounds.y);
	// ~ vParam.virtualStandYmax = new Double (bounds.y + bounds.height);
	// ~ vParam.virtualStandTreeNumber = treeNumber;
	// ~ vParam.virtualStandXY = 1;

	// ~ VirtualStand vStand = new VirtualStand ();
	// ~ int returnCode = VirtualStandSimulator.simulateOneStrata (null, vParam,
	// vStand);

	// ~ if (returnCode == 0) {
	// ~ TicketDispenser treeIdDispenser = getTreeIdDispenser ();
	// ~ for (int i = 1; i <= vStand.treeNumber; i = i+1) {

	// ~ FiPlant t = new FireGenericTree (treeIdDispenser.getNext (),
	// ~ null, // stand = null
	// ~ null,
	// ~ vStand.x[i],
	// ~ vStand.y[i],
	// ~ treeAge, // age
	// ~ getSettings ());
	// ~ t.setSpecies (FiSpecies.GENERIC);
	// ~ treePop.add (t);
	// ~ }

	// ~ } else {
	// ~ throw new Exception
	// ("Error in VirtualStandSimulator, return code="+returnCode);
	// ~ }
	// ~ return treePop;
	// ~ }

	/**
	 * Create distributed trees following a neyman scott pattern
	 */
	// ~ public Collection createTreesByNeymanScottOption (String className,
	// ~ int treeNumber, int clusterNumber, int clusterRadius, int treeAge,
	// Rectangle2D.Double bounds)
	// ~ throws Exception {

	// ~ Collection treePop = new ArrayList ();

	// ~ VirtualParameters vParam = new VirtualParameters ();
	// ~ vParam.virtualStandXmin = new Double (bounds.x);
	// ~ vParam.virtualStandXmax = new Double (bounds.x + bounds.width);
	// ~ vParam.virtualStandYmin = new Double (bounds.y);
	// ~ vParam.virtualStandYmax = new Double (bounds.y + bounds.height);
	// ~ vParam.virtualStandTreeNumber = treeNumber;
	// ~ vParam.virtualStandClusterNumber = clusterNumber;
	// ~ vParam.virtualStandClusterRadius = clusterRadius;
	// ~ vParam.virtualStandXY = 1;

	// ~ VirtualStand vStand = new VirtualStand ();
	// ~ int returnCode = VirtualStandSimulator.simulateOneStrata (null, vParam,
	// vStand);

	// ~ if (returnCode == 0) {
	// ~ TicketDispenser treeIdDispenser = getTreeIdDispenser ();
	// ~ for (int i = 1; i <= vStand.treeNumber; i = i+1) {

	// ~ FiPlant t = new FireGenericTree (treeIdDispenser.getNext (),
	// ~ null, // stand = null
	// ~ null,
	// ~ vStand.x[i],
	// ~ vStand.y[i],
	// ~ treeAge, // age
	// ~ getSettings ());
	// ~ t.setSpecies (FiSpecies.GENERIC);
	// ~ treePop.add (t);
	// ~ }

	// ~ } else {
	// ~ throw new Exception
	// ("Error in VirtualStandSimulator, return code="+returnCode);
	// ~ }
	// ~ return treePop;
	// ~ }

	/**
	 * These initializations are done once first stand retrieved and before
	 * returning to Pilot call.
	 */
	@Override
	public Step initializeModel(InitialParameters p) {
		// Some optional initializations here

		// Tell user inits are done.
		StatusDispatcher.print(Translator.swap("FiModel.initsAreDone"));
		return p.getInitScene().getStep();
	}

	/**
	 * When a project is opened (deserialized) from disk (open project), this
	 * method is called. It can be redefined to make some technical
	 * reinitializations in some connected dynamic link library or shared object
	 * (ex : STICS in Safe module). Generally unused : default body does
	 * nothing.
	 */
	@Override
	protected void projectJustOpened() {

		// fc-19.1.2015
		initParticleNames();

		// Check network connection, if yes, loadBuffers else
		// tell the user he is in offline mode
		networkAvailable = Tools.isNetworkAvailable();
		Log.println("FiModel: networkAvailable: " + networkAvailable);
		// ~ System.out.println
		// ("FiModel: networkAvailable: "+networkAvailable);

		if (networkAvailable) {
			loadBuffers();
		} else {
			StatusDispatcher.print(Translator.swap("FiModel.offlineMode"));
		}
	}

	/**
	 * Evolution for FiModel. Called by FiRelay.processEvolution (). Returns the
	 * last Step created, which must be visible.
	 */
	public Step processEvolution(Step stp, EvolutionParameters p) throws Exception {

		int numberOfSteps = ((FmEvolutionParameters) p).numberOfSteps;
		int initialDate = stp.getScene ().getDate ();
		for (int i = 1; i <= numberOfSteps; i++) {
			System.out.println("Evolution: step "+i+" over "+numberOfSteps);
			int newDate = initialDate + i;
			FmStand refStand = (FmStand) stp.getScene ();
			//System.out.println("	Starting EvolutionBase");
			FmStand newStand = (FmStand) refStand.getEvolutionBase(); // with trees
			System.out.println("	EvolutionBase done");
			newStand.setDate (newDate);

			// Stand has to be attacked for beetle spread to be considered.
			// The stand is attacked only after user selects "intervention" -->
			// "peturbation" --> beetle attack
			// if (refStand.isBeetleAttacked()) {
			// beetleSpread(newStand);
			// }

			// here, tree growth over time is modeled.
			//System.out.println("	Processing growth");
			processGrowth(refStand, newStand);
			//System.out.println("	Growth processed");
			// Beetle status over time: advance to next step
			// if (refStand.isBeetleAttacked() & i >= 2) {
			// beetleStatusEvolution(newStand);
			String reason = "Evolution " + numberOfSteps + " steps"; // last step
			// has real reason
			Step newStp = stp.getProject ().processNewStep ( stp, newStand,reason);
			stp = newStp;
		}
		return stp;

	}

	/**
	 * Beetle spread model:tree to tree beetle spread within given newStand.
	 */
	private void beetleSpread(FmStand newStand) {
		// first, we identify the trees which serve as sources for beetle
		// spread. These trees should be in status 1 or 2.
		// The following creates a new instance of an array of FiPlants. It is
		// empty; trees will be added to it.
		List<FmPlant> beetleSources = new ArrayList<FmPlant>();

		for (Tree t : newStand.getTrees()) {
			FmPlant p = (FmPlant) t;

			if (p.getBeetleStatus() == 1 || p.getBeetleStatus() == 2) {
				beetleSources.add(p);
			}
		}

		// for each source tree we will identify trees within the radius that
		// may be potential targets. These trees are currently status 0.
		// get the radius
		double mr = newStand.getMaxSpreadDistance();
		// make a circular search object that will identify trees in range
		boolean torus = false;// torus true enables wrapping at boundaries
		FmPlot plot = newStand.getPlot();
		RoundMask m = new RoundMask(plot, mr, torus);

		double param_a = newStand.getBeetleAttack_a();
		double param_b = newStand.getBeetleAttack_b();
		double param_c = newStand.getBeetleAttack_c();

		// access parameters for dbh-based beetle attack function

		for (FmPlant t : beetleSources) {
			Collection ns = m.getTreesNear(t);
			Iterator i = ns.iterator();
			while (i.hasNext()) {
				FmPlant n = (FmPlant) i.next(); // this is doing two things: ask
				// for next one, and cast to
				// FiPlant
				// The tree is only eligible as a target if it is not currently
				// attacked. In ohter words, its beetleStatus must be zero.
				if (n.getBeetleStatus() != 0) {
					continue;
				} // skip neighbors that cannot serve as targets.

				// each potential target tree,n, is then calculated to see if it
				// is
				// successfully attacked, where probability of attack is a
				// function of dbh.

				// get dbh for tree n
				double dbh = n.getDbh();
				// get a random number
				double nr = rnd.nextDouble();

				double p_attack = param_a / (1d + param_b * Math.exp(-param_c * dbh));
				if (nr <= p_attack) {
					n.setBeetleStatus((byte) 1);
				}

				// The trees which are attacked change their status.
			}
		}

	}

	/**
	 * Model chqnge in beetle attacked trees over time within given newStand.
	 */
	private void beetleStatusEvolution(FmStand newStand) {
		for (Tree t : newStand.getTrees()) {
			FmPlant p = (FmPlant) t;

			// beetle status changes over time only if tree has been attacked
			// (status >= 1).
			// status will go up 1 step but no higher than 4.
			if (p.getBeetleStatus() >= 1) {
				int newStatus = Math.min(4, (int) p.getBeetleStatus() + 1);
				p.setBeetleStatus((byte) newStatus);
			}
		}
	}

	// /** Growing method for FmModel.
	// */
	private void processGrowth(FmStand refStand, FmStand newStand)
			throws Exception {
		
		// here we compute ageDom, hDom, dbhList and dbhubList in the old stand
		int ageTot=0;
		List<Double> hList = new ArrayList();
		List<Double> dbhubList = new ArrayList();//underbark
		for (Iterator i = refStand.getTrees().iterator(); i.hasNext();) {
			FmPlant t = (FmPlant) i.next ();
			hList.add(t.getHeight());
			dbhubList.add(0.847 * t.getDbh() - 0.252);
			ageTot=Math.max(ageTot,t.getAge());
		}
		double hDom = FmStand.computeHDom(hList);
		System.out.println("Evolution: current hDom is "+hDom);
		// now compute delta5_hDom (increase of hDom in 5 years): estimate hDom50 from current hDom
		double ageDom = ageTot - 5d;
		double hDom50 = hDom / Math.exp(Math.pow(1d / (0.04 * 50d), 0.95) - Math.pow(1d / (0.04 * ageDom), 0.95));
		ageDom += 5;
		double hDomP5 = hDom50 * Math.exp(Math.pow(1d / (0.04 * 50d), 0.95) - Math.pow(1d / (0.04 * ageDom), 0.95));
		double delta5_hDom = hDomP5 - hDom;
		double hDomP1 = hDom + delta5_hDom/5;

		double dDomub = FmStand.computeHDom(dbhubList); //before growth
		double BAub=FmStand.computeBasalArea(dbhubList); // before growth
		
		
		
		// after growth to compute dDom, BA and remove dead trees...
		List<Double> dbhList = new ArrayList();
		//System.out.println("BAub="+BAub+", dDomub="+dDomub+",proba for dDomub/3"+Math.pow(Math.exp(-6*1e-5*BAub*BAub*(3d-1d)),1/5));
		for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
			FmPlant t = (FmPlant) i.next ();
			double dbhub= 0.847 * t.getDbh()- 0.252;
			double survivingProbability = Math.pow(Math.exp(-6*1e-5*BAub*BAub*(dDomub/dbhub-1d)),1/5);
			double rand = rnd.nextDouble();
			if (rand > survivingProbability) {
				System.out.println("tree "+t.getId()+" DIED:"+rand+">"+survivingProbability);
				i.remove();
			} else {
				//equation for the evolution of dbh under bark
				double delta5_dbhub = 0.812 + (1d-Math.exp(-0.0152*Math.min(delta5_hDom, 3d)))*
					(39.3-0.576*BAub+49.6*Math.max(Math.min(dbhub/dDomub,1.1),0.3));
				dbhList.add(t.getDbh()+delta5_dbhub/(5 * 0.847));
			}
		}
		double dDom = FmStand.computeHDom(dbhList); // after growth
		double BA=FmStand.computeBasalArea(dbhList); // after growth
		int ageTotP5 = ageTot+5;
		//System.out.println("PROCESS GROWTH:age="+ageTotP5+",dDom="+dDom+",BA)
		// process tree growth
		for (Iterator i = newStand.getTrees().iterator(); i.hasNext();) {
			FmPlant t = (FmPlant) i.next ();
			t.processGrowth(this, ageTotP5, dDom,hDomP1, BA ,delta5_hDom, BAub, dDomub);
		}
		// // Consider each layerSet in refStand, get a grown copy, add it to
		// newStand
		for (FiLayerSet layerSet : refStand.getLayerSets ()) {

			// processGrowth may throw an exception if growth is not possible ->
			// stops the evolution
			FmLayerSet newLayerSet = ((FmLayerSet)layerSet).processGrowth (refStand);
			newStand.addLayerSet (newLayerSet);
		}

	}

	/**
	 * Called after an intervention
	 */
	// @Override
	// public void processPostIntervention (GScene newStand, GScene oldStand) {
	// //~ System.out.println ("FiModel.processPostIntervention ()...");
	// }

	public TicketDispenser getLayerSetIdDispenser() {
		return layerSetIdDispenser;
	}

	public TicketDispenser getSpeciesIdDispenser() {
		return speciesIdDispenser;
	}

//	public Random getRandom() {
//		return random;
//	}

	/**
	 * For convenience.
	 */
	@Override
	public FmInitialParameters getSettings() {
		return (FmInitialParameters) settings;
	}

	public FmGeomMap getPatternMap() {
		return patternMap;
	}

	public FmGeomList getPatternList() {
		return patternList;
	}

	// REMOVED (was tricky)
	// public void setInitStand (FiStand initStand) {this.initStand =
	// initStand;}

	synchronized public LinkedHashMap<Long, FmDBTeam> getTeamMap() throws Exception {
		if (teamMap != null) {
			return teamMap;
		} else {
			throw new Exception("TeamMap was not yet loaded, please synchronize correctly with BufferingTask");
		}
	}

	synchronized public ArrayList<FmDBCheck> getCheckList() throws Exception {
		if (checkList != null) {
			return checkList;
		} else {
			throw new Exception("CheckList was not yet loaded, please synchronize correctly with BufferingTask");
		}
	}

	synchronized public Collection<FiSpecies> getLocalTreeSpeciesList() {
		return localTreeSpeciesList;
	}

	synchronized public Collection<FiSpecies> getLocalLayerSpeciesList() {
		return localLayerSpeciesList;
	}

	synchronized public Collection<FiSpecies> getDbSpeciesList() throws Exception {
		if (dbSpeciesList != null) {
			return dbSpeciesList;
		} else {
			throw new Exception(
					"Data base SpeciesList was not yet loaded, please synchronize correctly with BufferingTask");
		}
	}

	synchronized public Collection<FiSpecies> getSpeciesList() throws Exception {
		if (speciesList != null) {
			return speciesList;
		} else {
			throw new Exception(
					"Data base SpeciesList was not yet loaded, please synchronize correctly with BufferingTask");
		}
	}

	synchronized public Map<Long, FmPlantSyntheticData> getPlantSyntheticMap() throws Exception {
		if (plantSyntheticMap != null) {
			return plantSyntheticMap;
		} else {
			throw new Exception("plantSyntheticMap was not yet loaded, please synchronize correctly with BufferingTask");
		}
	}

	synchronized public Map<Long, FmLayerSyntheticDataBaseData> getLayerSyntheticMap() throws Exception {
		if (layerSyntheticMap != null) {
			return layerSyntheticMap;
		} else {
			throw new Exception("layerSyntheticMap was not yet loaded, please synchronize correctly with BufferingTask");
		}
	}

	/**
	 * The speciesList contains trait, genus and species information. This
	 * method dispatches the entries into 3 collections: traitNames, genusNames
	 * and speciesNames.
	 */
	private void dispatchSpeciesList() {
		// speciesMap = new HashMap<String,FiSpecies> ();
		traitNames = new ArrayList<String>();
		genusNames = new ArrayList<String>();
		speciesNames = new ArrayList<String>();

		// localTreeSpeciesList = new ArrayList <FiSpecies> ();
		// localTreeSpeciesList =

		// the "local species" are already in the map (loaded in a speciesFile
		// by
		// FmInitialParameters)
		localLayerSpeciesList = speciesMap.values(); // new ArrayList
		// <FiSpecies> ();
		System.out.println("speciesMap contains " + speciesMap.size());
		speciesList = new HashSet<FiSpecies>(localTreeSpeciesList);
		speciesList.addAll(localLayerSpeciesList);
		if (dbSpeciesList == null) {
			return;
		} // nothing more to dispatch

		// add database species...
		if (networkAvailable) {
			speciesList.addAll(dbSpeciesList);
			for (FiSpecies s : dbSpeciesList) {
				speciesMap.put(s.getName(), s);
			}
		}

		// build the list of traitNames, genusNames, speciesNames
		for (FiSpecies s : speciesList) {
			if (FiSpecies.TRAIT_TAXON_LEVEL == s.getTaxonomicLevel()) {
				traitNames.add(s.getName());
			} else if (FiSpecies.GENUS_TAXON_LEVEL == s.getTaxonomicLevel()) {
				genusNames.add(s.getName());
			} else if (FiSpecies.SPECIES_TAXON_LEVEL == s.getTaxonomicLevel()) {
				speciesNames.add(s.getName());
			}
		}
	}

	// /** Creation of local species, added into the speciesMap only (for
	// getSpecies (name)).
	// */
	// private FiSpecies createLocalSpecies (String traitName, String
	// speciesName, Boolean type) {
	// String genusName = "";
	// try { // Species "Pinus halepensis" -> Genus "Pinus"
	// String words[] = speciesName.split (".");
	// genusName = words[0];
	// } catch (Exception e) {}
	//
	// int newId = speciesIdDispenser.next ();
	// FiSpecies species = new FiSpecies (newId, speciesSpecimen,
	// traitName, genusName, speciesName, FiSpecies.SPECIES_TAXON_LEVEL);
	// speciesMap.put (speciesName, species);
	// // Update speciesSpecimen for future species connection
	// if (speciesSpecimen == null) {
	// speciesSpecimen = species;
	// }
	// if (type == FiPlant.PLANT) {
	// localTreeSpeciesList.add(species);
	// } else {
	// localLayerSpeciesList.add(species);
	// }
	// return species;
	// }

	/**
	 * Returns the Species names matching the taxonomic level = TRAIT.
	 */
	public Collection<String> getTraitNames() {
		if (traitNames == null) {
			dispatchSpeciesList();
		}
		return traitNames;
	}

	/**
	 * Returns the Species names matching the taxonomic level = GENUS.
	 */
	public Collection<String> getGenusNames() {
		if (genusNames == null) {
			dispatchSpeciesList();
		}
		return genusNames;
	}

	/**
	 * Accessor
	 */
	// ~ public Map<String,FiSpecies> getLocalSpeciesMap () {return
	// localSpeciesMap;}

	/**
	 * Returns the Species names matching the taxonomic level = SPECIES.
	 */
	public Collection<String> getSpeciesNames() {
		if (speciesNames == null) {
			dispatchSpeciesList();
		}
		return speciesNames;
	}

	public FiSpecies getSpeciesSpecimen() {
		return speciesSpecimen;
	} // needed for FiSpecies instances interconnection

	/**
	 * Returns the Species object matching the given name.
	 */
	public FiSpecies getSpecies(String name) { // fc - 17.4.2008
		if (speciesMap == null) {
			dispatchSpeciesList();
		}

		// System.out.println ("speciesMap list " + speciesMap.size ());
		FiSpecies species = speciesMap.get(name); // null if not found
		// code below REPLACED by createLocalSpecies ()
		// ~ // fc - oct 2009 - if the species is not found (maybe network not
		// ~ // available or local species), create it in speciesMap
		// ~ if (species == null) {
		// ~ int newId = speciesIdDispenser.next ();
		// ~ species = new FiSpecies (newId, speciesSpecimen, "", "", name,
		// FiSpecies.SPECIES_TAXON_LEVEL);
		// ~ speciesMap.put (name, species);
		// ~ }
		return species;
	}

	// MOVED to FiStand, made static, takes a parameter of type FiSstand
	// Note: instead of initStand, we could use a passed stand
	// public Collection<FiSpecies> getStandSpeciesList () {
	// try {
	// // fc - 12.7.2007 - we need the list of the species accurately in the
	// stand
	// // asking the first species for the known species list does not work
	// // because it will return the species of the removed trees also
	// Collection<FiSpecies> standSpecies = new HashSet<FiSpecies> ();
	//
	// if (initStand == null) {
	// initStand = (FiStand) ((Step) getProject().getRoot()).getScene();
	// }
	//
	// for (Iterator i = initStand.getTrees ().iterator (); i.hasNext ();) {
	// FiPlant t = (FiPlant) i.next ();
	// standSpecies.add (t.getSpecies ()); // set : duplicates are not kept
	// }
	// return standSpecies;
	// } catch (Exception e) {
	// Log.println(Log.WARNING, "FiModel.getStandSpeciesList ()", "Exception",
	// e);
	// return Collections.EMPTY_LIST;
	// }
	// }

	public LinkedHashMap<Long, FmDBCountry> getCountryMap() throws Exception {
		if ((countryMap != null) && (countryMap.size() > 0)) {
			return countryMap;
		} else {
			try {
				countryMap = new LinkedHashMap();
				countryMap = this.getBDCommunicator().getCountries();
				return countryMap;
			} catch (Exception e) {
				Log.println(Log.ERROR, "FiModel ()", "error while opening COUNTRY data base", e);
				throw new Exception("countryMap cannot be loaded, please check database connection");
			}
		}
	}

	/**
	 * Add a listener to this object.
	 */
	public void addListener(Listener l) {
		if (listeners == null) {
			listeners = new ArrayList<Listener>();
		}
		listeners.add(l);
	}

	/**
	 * Remove a listener to this object.
	 */
	public void removeListener(Listener l) {
		if (listeners == null) {
			return;
		}
		listeners.remove(l);
	}

	/**
	 * Notify all the listeners by calling their somethingHappened (listenedTo,
	 * param) method.
	 */
	public void tellSomethingHappened(Object param) {
		if (listeners == null) {
			return;
		}
		for (Listener l : listeners) {
			l.somethingHappened(this, param);
		}
	}

	/**
	 * return the team logged owner of new data
	 */
	public FmDBTeam getTeamLogged() {
		return teamLogged;
	}

	/**
	 * return the right level of the team logged
	 */
	public int getRightLevel() {
		return rightLevel;
	}

	/**
	 * return the password of the team logged
	 */
	public String getTeamPassword() {
		return teamPassword;
	}

	public void setTeamLogged(FmDBTeam t) {
		teamLogged = t;
	}

	public void setRightLevel(int r) {
		rightLevel = r;
	}

	public void setTeamPassword(String s) {
		teamPassword = s;
	}

	public void setSpeciesMap(Map<String, FiSpecies> speciesMap) {
		this.speciesMap = speciesMap;
	}

}
