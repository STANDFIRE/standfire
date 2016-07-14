/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003 Francois de Coligny
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
package capsis.lib.fire.exporter.firetec;

import java.io.File;
import java.util.Collection;

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

/**
 * Firetec is a PhyExporter that creates 4 files for the Firetec simulation
 * model. It currently deals with only one fuel family (all selected family are
 * averaged in a given cell)
 * 
 * @author F. de Coligny, O. Vigy - october 2007
 */
public class Firetec extends PhysExporter implements DirectoryExport, OFormat {

	// Default mesh for firetec export
	public static final String DX_DEFAULT = "2.0"; // m
	public static final String DY_DEFAULT = "2.0"; // m
	public static final String DZ_DEFAULT = "15.0"; // m
	public static final String NZ_DEFAULT = "41"; // number of cell
	public static final String AA1_DEFAULT = "0.1"; // cubic factor in mesh
													// stretching

	public static final String RHOF = "rhof.dat";
	public static final String SS = "ss.dat";
	public static final String MOIST = "moist.dat";
	public static final String FUELDEPTH = "fueldepth.dat";
	public static final String TREES = "trees";
	public static final String TOPOFILE = "topofile.dat";

	public static final double RHOF_DEFAULT = 0.000001; //kg/m3
	public static final double SVR_DEFAULT = 5000; //1/m
	public static final float MIN_FUELDEPTH_DEFAULT = 0.01f; //m

	public String baseFileName;
	public String topofileName;
	public String topoFormat;
	public String outputFormat; // NativeBinaryInputStream.X86=littleEndian

	// NativeBinaryInputStream.sparc=bigEndian

	/**
	 *  Default constructor.
	 */
	public Firetec() {
		super();
		baseFileName = TREES;
		topofileName = TOPOFILE;
	}

	/**
	 * Constructor for script mode.
	 * 
	 * <pre>
	 * Firetec export = new Firetec (dx, dy, dz, nz, aa1,  sceneOriginX, sceneOriginY,sceneSizeX, sceneSizeY,
	 * 					 scene, fmo, pdo, firetecOutDir, PhysDataBuilder.BIGENDIAN);
	 * </per>
	 * 
	 * @throws Throwable
	 * 
	 */
	public Firetec(double dx, double dy, double meanDz, int nztot, double aa1, double exportedZoneOriginX,
			double exportedZoneOriginY, double exportedZoneLengthX, double exportedZoneLengthY, FiStand stand,
			FuelMatrixOptions fmo, PhysDataOptions pdo, String dirName, String outputFormat) throws Throwable {

		super(stand, fmo, pdo);
		// grid definition
		Grid grid = new Grid(); // grid definition
		grid.buildFiretecGrid(dx, dy, meanDz, nztot, aa1, exportedZoneOriginX, exportedZoneOriginY,
				exportedZoneLengthX, exportedZoneLengthY);
		this.grids.add(grid); // single grid for firetec
		this.physData = new PhysDataOF(grid.nx, grid.ny, grid.nz, grid);
		Collection<FuelItem> fuelItems = stand.getFuelItems();

		for (FuelItem fi : fuelItems) {
			exportedBiomass[0] += fi.getFuelMass(this);// initial biomass to export
			exportedBiomass[1] += fi.addFuelTo(this);
		}
		((PhysDataOF) this.physData).putDefaultValuesInEmptyCells(grid);
		exportedBiomass[2] = ((PhysDataOF) this.physData).getExportedFuel();
		System.out.println("EXPORTED BIOMASS IN FIRETEC IS:"+exportedBiomass[0]+", "+exportedBiomass[1]+", "+exportedBiomass[2]);
		this.outputFormat = outputFormat;
		baseFileName = TREES;
		topofileName = TOPOFILE;
		
		this.save(dirName);
	}

	/**
	 * for the gui
	 */
	public void initExport(GModel m, Step s) throws Exception {

		System.out.println("Firetec.initExport ()...");
		
		this.model = (FiModel) m; // fc-6.5.2015
		this.modelParticleNames = ((FiModel) m).particleNames;

		if (s.getScene() != null) { // Export mode
			this.stand = (FiStand) s.getScene();
			FiretecDialog dlg = new FiretecDialog(this);
			if (!dlg.isValidDialog()) {
				throw new CancelException(); // fc-6.5.2015
//				return; // fc-6.5.2015
			} // user canceled dialog -> stop
			dlg.dispose();

			// Short circuited, we create 4 files in a directory -> special
			// ~ createRecordSet (s.getStand ());

		} else {
			throw new Exception("Firetec: Unable to recognize mode Import/Export.");

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
			Log.println(Log.ERROR, "Firetec.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**
	 * save several files.
	 */
	public void save(String dirName) throws Exception {

		System.out.println("Firetec.save ()...");
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
						Log.println(Log.ERROR, "Firetec.save ()",
								"Could not create directory with File.mkdir (), Could not save in " + dirName
										+ " (also tryed with " + dir + ")");
						throw new Exception("could not create directory: " + dir + " due to exception, see Log");
					}
				}
			}
		}

		try {

			System.out.println("Firetec.save ()");
			System.out.println("Firetec dir:" + dir);
			System.out.println("Firetec baseFileName: " + baseFileName);
			System.out.println("Firetec topofilename:  " + topofileName);
			System.out.println("Firetec topoFormat:  " + topoFormat);
			System.out.println("Firetec produceTreeCrownVoxel:  " + pdo.produceTreeCrownVoxel);
			System.out.println("Firetec physData:  " + physData);
			System.out.println("Firetec outputFormat:  " + outputFormat);

			new FiretecWriter(dir, baseFileName, topofileName, pdo.produceTreeCrownVoxel, (PhysDataOF) physData,
					outputFormat, grids.get(0)).save();
		} catch (Exception e) {
			Log.println(Log.ERROR, "Firetec.save ()", "Error in FiretecMatrixWriter, aborted", e);
			throw new Exception("Error in FiretecMatrixWriter, aborted");
		}

	}

	// //////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName() {
		return Translator.swap("Firetec");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "1.3";

	/**
	 * From Extension interface.
	 */
	public String getAuthor() {
		return "F. Pimont, F. de Coligny, O. Vigy";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription() {
		return Translator.swap("Firetec.description");
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
