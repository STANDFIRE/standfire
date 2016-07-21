package capsis.lib.fire.exporter.wfds;

import java.io.File;
import java.util.ArrayList;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.GModel;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.OFormat;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.exporter.PhysData.PhysDataOptions;
import capsis.lib.fire.exporter.PhysExporter;
import capsis.lib.fire.fuelitem.FuelItem;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.util.CancelException;
import capsis.util.DirectoryExport;

public class WFDS extends PhysExporter implements DirectoryExport, OFormat {
	// fc-2.2.2015 added DirectoryExport upper (was missing)
	
	public WFDSParam p;
	public WFDSWriter w;

	/**
	 * Constructor
	 */
	public WFDS() {
		super();
		p = new WFDSParam();
	}

	/**
	 * Constructor for script mode.
	 * 
	 * <pre>
	 * WFDS export = new WFDS (wfdsGridFile, gridFormat, scene, fuelMatrixOptions, physDataOptions, wfdsOutDir,wfdsFileName)	
	 * </per>
	 * 
	 * @throws Throwable
	 * 
	 */
	public WFDS(WFDSParam p, FiStand stand, FuelMatrixOptions fmo, PhysDataOptions pdo) throws Throwable {

		// String gridFile, String format, FiStand stand, , String outDir,
		// String fileName, String fileNameBd)

		super(stand, fmo, pdo);
		this.p = p;
		// LOAD THE WFDS GRID
		this.grids.addAll(buildGrids());

		// System.out.println ("	particles:" + fmo.particleNames);

		// for each item, BUILD PHYSDATAWFDS ADD THEM TO WFDSWriter
		buildPhysDataWFDS();
		
		this.save(p.outDir);
	}

	public ArrayList<Grid> loadGrids() throws Exception {
		// LOAD THE WFDS GRIDs
		ArrayList<Grid> grs = new ArrayList<Grid>();
		for (int gridNumber = 0; gridNumber < p.gridNumber; gridNumber++) {
			String gridFile = p.firstGridFile;
			gridFile = gridFile.replace("1.xyz", (gridNumber + 1) + ".xyz");
			Grid grid = new Grid();
			grid.readWFDSGrid(gridFile, p.format);
			grs.add(grid);
		}
		return grs;
	}

	public ArrayList<Grid> buildGrids() throws Exception {
		return loadGrids();
	}

	public void buildPhysDataWFDS() throws Exception {
		System.out.println("Fuel items export...");
		System.out.println(p);
		System.out.println(p.outDir);
		System.out.println(grids.size());
		System.out.println(stand.getFuelItems().size());
		System.out.println(fmo.verbose);
		w = new WFDSWriter(p, grids); // here grid is used only to compute mean
										// bulk density for constantBulkDensity
										// mode
		for (FuelItem fi : stand.getFuelItems()) {
			if (fmo.verbose) {
				System.out.println("Fuel item " + fi.getName());
			}
			if (isInExportedZone(fi)) {
				PhysDataWFDS pd = new PhysDataWFDS();
				this.physData = pd;
				//DEBUG
			    //double a = fi.getFuelMass(this);
			    //double b = fi.addFuelTo(this);
				//double c = w.addPhysDataWFDS(pd);
				//System.out.println("EXPORTD BIOMASS IN WFDS IS:"+a+", "+b+", "+c);
				//exportedBiomass[0] += a;
				//exportedBiomass[1] += b;
				//exportedBiomass[2] += c;
				exportedBiomass[0] += fi.getFuelMass(this);// initial biomass to export
				exportedBiomass[1] += fi.addFuelTo(this);
				exportedBiomass[2] += w.addPhysDataWFDS(pd);
			}
		}
		System.out.println("EXPORTED BIOMASS IN WFDS IS:"+exportedBiomass[0]+", "+exportedBiomass[1]+", "+exportedBiomass[2]);
	}

	/**
	 * for the gui
	 */
	public void initExport(GModel m, Step s) throws Exception {
		System.out.println("WFDS.initExport ()...");		

		this.model = (FiModel) m; // fc-6.5.2015
		this.modelParticleNames = ((FiModel) m).particleNames;
		
		if (s.getScene() != null) { // Export mode
			this.stand = (FiStand) s.getScene();
			WFDSDialog dlg = new WFDSDialog(this);
			if (!dlg.isValidDialog()) {
				throw new CancelException(); // fc-6.5.2015
//				return; // fc-6.5.2015
			} // user canceled dialog -> stop
			dlg.dispose();

			// Short circuited, we create 4 files in a directory -> special
			// ~ createRecordSet (s.getStand ());

		} else {
			throw new Exception("WFDS: Unable to recognize mode Import/Export.");

		}

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof FiModel)) {
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "WFDS.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	public void save(String dirName) throws Exception {
		// p.fileName should be defined first
		p.outDir = dirName;

		System.out.println("WFDS.save ()...");
		System.out.println("dirName: " + dirName);

		File dir = new File(dirName);

		if (dir.exists()) {
			// fc-23.5.2013 if dir is an existing regular file (user mistake):
			// parent dir
			if (!dir.isDirectory())
				dir = dir.getParentFile();

		} else {
			boolean creationOk = dir.mkdir();

			if (!creationOk) {
				// fc-23.5.2013 maybe dir is an unexisting regular file (user
				// mistake): parent dir
				dir = dir.getParentFile();

				if (!dir.exists()) {
					creationOk = dir.mkdir();

					if (!creationOk) {
						Log.println(Log.ERROR, "WFDS.save ()",
								"Could not create directory with File.mkdir (), Could not save in " + dirName
										+ " (also tryed with " + dir + ")");
						throw new Exception("could not create directory: " + dir + " due to exception, see Log");
					}
				}
			}
		}

		try {

			System.out.println("WFDS.save ()");
			System.out.println("WFDS dir:" + dir);
			System.out.println("WFDS FileName: " + p.fileName);
			w.save(dir);
		} catch (Exception e) {
			Log.println(Log.ERROR, "Firetec.save ()", "Error in FiretecMatrixWriter, aborted", e);
			throw new Exception("Error in FiretecMatrixWriter, aborted");
		}

	}

	/**
	 * From Extension interface.
	 */
	public String getName() {
		return Translator.swap("WFDS");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "1.1";

	/**
	 * From Extension interface.
	 */
	public String getAuthor() {
		return "F. Pimont, F. de Coligny";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription() {
		return Translator.swap("WFDS.description");
	}

	// //////////////////////////////////////////////// IOFormat stuff

	public boolean isImport() {
		return false;
	}

	public boolean isExport() {
		return true;
	}

	@Override
	public void activate() {
		// TODO FP Auto-generated method stub

	}

}
