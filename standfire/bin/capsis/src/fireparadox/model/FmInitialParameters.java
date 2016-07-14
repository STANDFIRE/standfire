package fireparadox.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.InitialParameters;
import capsis.kernel.PathManager;
import capsis.lib.fire.FiInitialParameters;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.lib.spatial.VirtualParameters;

/**	FiInitialParameters - List of model settings.
 *	May have defaults or not. May be modified by user action during model
 *	initialization.
 *
 *	@author O. Vigy, E. Rigaud - september 2006
 */
public class FmInitialParameters extends FiInitialParameters implements InitialParameters {

	private FmModel model;
	private GScene initScene;
	
	public String speciesFileName = PathManager.getDir ("data") + "/fireparadox/speciesFile";
	
	// Alternative ways for building the initial scene: only one of 
	// these booleans must be true, see builInitScene ()
	public enum InitMode {
		DETAILED_VIEW_ONLY, FROM_FIELD_PARAMETERS, FROM_SVS_PARAMETERS, FROM_SCRATCH
	}
	private InitMode initMode;

	public String detailedInventoryName;
	public String SVSParameters;
	public String fieldParameters;
	public double xDim;
	public double yDim;
	
	
	// Default values
	public static final double PLOT_ORIGIN_X = 0;
	public static final double PLOT_ORIGIN_Y = 0;
	public static final double PLOT_WIDTH = 50;
	public static final double PLOT_HEIGHT = 50;
	public static final double CELL_WIDTH = 5;

	// Fields
	public String inventoryName = "";
	public double plotWidth = PLOT_WIDTH;	// m.
	public double plotHeight = PLOT_HEIGHT;	// m.
	public double cellWidth = CELL_WIDTH;	// m.

	//Crown cube 3D description
	public static final double X_CUBE_SIZE = 0.25;	//m
	public static final double Y_CUBE_SIZE = 0.25;	//m
	public static final double Z_CUBE_SIZE = 0.25;	//m

	// species map
	// MVR DEFAULT for Bulk density and load computation
	public static final double MVR = 500.;	//kg/m3
	// SVR DEFAULT for LAI computation
	public static final double SVR = 5000; // m2/m3
	// SLA DEFAULT : SLA alive/MVR
	public static final double SLA = 7.0; // m2/kg
	
	
	
	public static Collection<String> selectedFamilies; //families used in MeanBulkDensity computation
 	static {
 		selectedFamilies = new ArrayList<String>();
 		selectedFamilies.add ("Leaves__Needles_INRA");
 		selectedFamilies.add ("Leaves_INRA");
 		selectedFamilies.add ("Needles_INRA");
 		selectedFamilies.add ("Leaves__Twigs_0_2_INRA");
 		selectedFamilies.add ("Twigs_0_2_INRA");
 	}
 	public static Collection<String> selectedLeaveFamilies; // families used in
															// LAI computation
	static {
		selectedLeaveFamilies = new ArrayList<String>();
		selectedLeaveFamilies.add("Leaves__Needles_INRA");
		selectedLeaveFamilies.add("Leaves_INRA");
		selectedLeaveFamilies.add("Needles_INRA");
		selectedLeaveFamilies.add("Leaves__Twigs_0_2_INRA");
	}
	
	// crownScorchModel available
	
	private static Collection<String> crownScorchModels;
	// models available for crownScorch
	static {
		crownScorchModels = new ArrayList<String>();

		crownScorchModels.add("Saveland and Neuenschwander");
		// (Saveland and Neuenschwander 1990)
		// wind=unknown, fire intensity between 70 and 3600 kW/m
		// species= Pinus banksiana,resinosa, strobus, Quercus rubra

		crownScorchModels.add("Van Wagner");
		// (Van Wagner 1973)
		// wind=1m/s, fire intensity between 80 and 1250 kW/m
		// species= Pinus ponderosa

		// crownScorchModels.add ("Van Wagner with wind");//(Van Wagner 1973)

		crownScorchModels.add("Finney");
		// (Finney& Martin 1993;)
		// wind=0-2m/s, fire intensity between 60 and 2350 kW/m
		// species= Sequoia sempervirens, PseudoTsuga mensiesii
		// very similar to Burrows et al 1989) (constante 8.7 instead of 8.9
		// wind=0-2m/s, fire intensity between 80 and 220 kW/m
		// species= Pinus radiata

		crownScorchModels.add("Michaletz and Jonhson");
		// (Michaletz and Jonhson 2006;)
		// no wind factor fire intensity 2500 kW/m
		// species= all, provided parameter are available

	}
	
	// mortality model available
	private static Collection<String> aleppoPineMortality;
	// models available for aleppo pine (rigolot et al 2004)
	static {
		aleppoPineMortality = new ArrayList<String>();
		aleppoPineMortality.add("CVS");
		aleppoPineMortality.add("DBH-CVS");
		aleppoPineMortality.add("DBH-CVS-maxBCN");
	}
	private static Collection<String> stonePineMortality;
	// models available for stone pine (rigolot et al 2004)
	static {
		stonePineMortality = new ArrayList<String>();
		stonePineMortality.add("CVS");
		stonePineMortality.add("CVS-meanBLC");
		stonePineMortality.add("CVS-meanBCN");
	}
	static public HashMap<String, Collection> mortalityModelMap;
	static {
		mortalityModelMap = new HashMap<String, Collection>();
		mortalityModelMap.put("Pinus halepensis", aleppoPineMortality);
		mortalityModelMap.put("Pinus pinea", stonePineMortality);
	}
	
	
	
	// Species dependent settings
	//~ public int speciesNumber = 0;
	//~ public SysSpeSets[] speSet = null;	// will be used a lot -> abbreviated

	// Here we add all the parameters used to simulate virtual stands
	public VirtualParameters vParam;

	
	/** A constructor to init the VirtualParameters object
	 */
	public FmInitialParameters (FmModel model) {
		sceneOriginX = PLOT_ORIGIN_X;	// m.
		sceneOriginY = PLOT_ORIGIN_Y;	// m.
		
		this.model = model;
		vParam = new VirtualParameters ();
	}
	
	public void setInitMode(InitMode initMode, int seed) {
		this.initMode = initMode;
		if (seed < 0) {// a random seed is generated (the model is not determinist anymore)
			this.model.rnd =  new Random();
		} else {
			this.model.rnd =  new Random(seed);
		}
		
	}
	
	@Override
	public void buildInitScene(GModel m) throws Exception {
		this.model = (FmModel) m;
		m.setSettings(this);
		speciesMap = FiSpecies.loadSpeciesMap (speciesFileName);
		model.setSpeciesMap (speciesMap);
		
		if (initMode == InitMode.DETAILED_VIEW_ONLY) {
			initScene = (FmStand) model.loadDetailedViewOnly (detailedInventoryName);
			
		} else if (initMode == InitMode.FROM_FIELD_PARAMETERS) {
			initScene = (FmStand) model.loadFromFieldParameters(fieldParameters);
		
		} else if (initMode == InitMode.FROM_SVS_PARAMETERS) {
			initScene = (FmStand) model.loadFromSVS(SVSParameters);
				
		} else if (initMode == InitMode.FROM_SCRATCH) {
			double xorigin = 0d;
			double yorigin = 0d;
			String source = "from scrach";
			double altitude = 0d;
			cellWidth = 10d;
			initScene = (FmStand) model.loadFromScratch(xorigin, yorigin,
					altitude, xDim, yDim, cellWidth, source);
		}
		
	}

	@Override
	public GScene getInitScene() {
		return initScene;
	}

}



