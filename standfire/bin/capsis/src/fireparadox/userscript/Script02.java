package fireparadox.userscript;

import java.io.File;

import capsis.app.C4Script;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.fire.exporter.PhysData.PhysDataOptions;
import capsis.lib.fire.exporter.wfds.WFDS;
import capsis.lib.fire.exporter.wfds.WFDSParam;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import capsis.util.NativeBinaryInputStream;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

/**
 * A script to tune the WFDSExport.
 * 
 * <pre>
 *  sh capsis.sh -p script fireparadox.userscript.Script02
 * </pre>
 * 
 * @author F. de Coligny and F. Pimont - September 2013
 * 
 */
public class Script02 {

	public static void main (String[] args) throws Throwable {

		// // One parameter expected: a directory name
		// String name = args[1]; //"A4_1";
		// boolean exportFile = true;

		// Script creation
		C4Script s = new C4Script ("fireparadox");

		// Create the output directory
		// May be located in another directory,
		// see System.getProperty ("user.dir") and s.getRootDir ()
		String outDirName = PathManager.getDir ("tmp") + File.separator + "wfds";
		new File (outDirName).mkdir ();
		System.out.println ("outDir = " + outDirName);

		// Initialisation
		FmInitialParameters i = new FmInitialParameters ((FmModel) s.getModel ());
		i.setInitMode (FmInitialParameters.InitMode.FROM_FIELD_PARAMETERS,0);
		i.fieldParameters = PathManager.getDir ("data") + File.separator + "fireparadox" + File.separator
				+ "fromFieldParameters" + File.separator + "fuelbreak_basic.txt";

		s.init (i);

		FmModel m = (FmModel) s.getModel ();
		// FiInitialParameters settings = m.getSettings();
		FmStand stand = (FmStand) s.getRoot ().getScene ();

		// Get project root step
		Step root = s.getRoot ();
		Step step = root;

		// Load a Grid
		// File gridDir = new File (PathManager.getDir ("data") + File.separator +
		// "standfire"+File.separator+"grid");
		String gridFileName = (new File (PathManager.getDir ("data")) + "/standfire/grid/") + "test2001_0001.xyz";
		String format = NativeBinaryInputStream.X86; // littleEndian
		
		
		// EXPORT TO WFDS OR FIRETEC : OPTION DEFINITION
		FuelMatrixOptions fmo = new FuelMatrixOptions ();
		fmo.fiLayerSetHorizontalDistributionDx = 1d;
		fmo.particleNames = m.particleNames;
		fmo.verbose = true;
		
		PhysDataOptions pdo = new PhysDataOptions ();
		pdo.overlappingPermitted = true;
		pdo.produceTreeCrownVoxel = false;
		pdo.verbose = true;
		
		//File outDir = new File (outDirName);
		WFDSParam p = new WFDSParam();
		p.fileName = "wfds-1";
		p.outDir = outDirName;
				
		// EXPORT:
		WFDS w = new WFDS (p, stand, fmo, pdo);
		System.out.println ("Script02 ended.");
	}
}
