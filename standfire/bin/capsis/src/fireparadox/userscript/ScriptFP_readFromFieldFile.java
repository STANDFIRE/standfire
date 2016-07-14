package fireparadox.userscript;

import java.io.BufferedWriter;
import java.io.File;

import capsis.app.C4Script;
import capsis.defaulttype.Tree;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.exporter.PhysData.PhysDataOptions;
import capsis.lib.fire.exporter.PhysExporter;
import capsis.lib.fire.exporter.firetec.Firetec;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FuelMatrix.FuelMatrixOptions;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

/**
 * This script is derived from Script01 from F d C: it builds different projects with different
 * stand parameter in Aleppo pine do different thinning and pruning (eventually) and print different
 * properties of the stand
 * 
 * *
 * 
 * <pre>
 *  sh capsis.sh -p script fireparadox.userscript.ScriptFP_readFromFieldFile
 * </pre>
 * sh setmem.sh 4096
 * sh capsis.sh -p script fireparadox.userscript.ScriptFP_readFromFieldFile data/fireparadox/fromFieldParameters/aleppo_pine_virtual_stand.txt tmp
 * 
 * @author F. de Coligny and FP- june 2010
 * 
 */
public class ScriptFP_readFromFieldFile {

	
	public static void main(String[] args) throws Throwable {

		// parameter definition
		//System.out.println ("args length="+args.length+";arg[1]="+args[0]);
		// String name = args[1]; //"A4_1";
		boolean exportFile = true;
		
		// Script creation
		C4Script s = new C4Script ("fireparadox");

		
		// Create the output directory
		// May be located in another directory,
		// see System.getProperty ("user.dir") and s.getRootDir ()
		String outDir = args[2]; //PathManager.getDir ("data") + "/fireparadox/temp/";
		// PathManager.getDir ("data") // .../capsis4/data
		// + File.separator +
		// ".."+File.separator+".."+File.separator+"Documents"+File.separator+"capsisdata"+File.separator+"alea_onf"+File.separator+name;
		
		new File(outDir).mkdir();
		System.out.println("outDir = " + outDir);

		// Create the output file
		// String outFileName = outDir + File.separator + name;
		// System.out.println("outFileName = " + outFileName);

		// BufferedWriter out = new BufferedWriter(new FileWriter(outFileName+".report"));
		// writeReportHeader(name, out);

		// Initialisation

		FmInitialParameters i = new FmInitialParameters((FmModel) s
					.getModel());
			i.setInitMode(FmInitialParameters.InitMode.FROM_FIELD_PARAMETERS,0);
			//i.fieldParameters = PathManager.getDir ("data") + "/fireparadox/fromFieldParameters/fireflux2_wind.txt";
			//i.fieldParameters = PathManager.getDir ("data") + "/fireparadox/fromFieldParameters/fuelbreak_basic_small.txt";
			i.fieldParameters = args[1];
			
		// outDir+".txt";
			// i.fieldParameters = Options.getDir ("data")
			// + File.separator
			// + "fireparadox"
			// + File.separator
			// + "fromFieldParameters"
			// + File.separator
			// + "fieldDataExample_Aleppo_pine_virtual_stand.txt";

			s.init(i);

			FmModel m = (FmModel) s.getModel();
			// FiInitialParameters settings = m.getSettings();
			FmStand initStand = (FmStand) s.getRoot().getScene();

						
			// Get project root step
			Step root = s.getRoot();
			Step step = root;
			// Write in the file when needed
			// writeReportLine(p, out, root);

			// Evolution
			// Step step = s.evolve (new FiEvolutionParameters (2));

			// Write in the file when needed
			//writeReportLine(p, out, step);

			// Create thinner
			// int distCriterion = 0;
			// double minDist = 1;
			// int thinningCriterion = 2;
			// double martellingDist = 7;
			// NrgThinner2 t = new NrgThinner2 (distCriterion, minDist,
			// thinningCriterion, martellingDist);

			 // step = s.runIntervener (t, step);

			// Write in the file when needed
		// writeReportLine(out, step);
			if (exportFile) {
				String dirName = outDir;
				//s.getDir ("data")+ File.separator
				 //+ ".."+ File.separator
				 //+ ".."
				 //+ File.separator
				 //+ "Documents"
				 //+ File.separator
				//+ "MATLAB"
				//+ File.separator
				//+ "script"
				//+ File.separator
				//+ "readfueldata"
				//+ File.separator
				//+ "testexport";
				double minX = initStand.getOrigin().x;
				double minY = initStand.getOrigin().y;
				double sizeX =  initStand.getXSize();
				double sizeY =  initStand.getYSize();
				System.out.println("	exported zone:"+minX+","+sizeX+","+minY+","+sizeY);
				
				// EXPORT TO WFDS OR FIRETEC : OPTION DEFINITION
				FuelMatrixOptions fmo = new FuelMatrixOptions ();
				fmo.fiPlantDiscretization = 0.1; // ratio of discretization of FiPlant in x, y, z direction
				fmo.fiLayerSetHorizontalDistributionDx = 0.5d; // in m
				fmo.fiLayerSetMinDz = 0.2; // in m
				fmo.fiLayerSetVerticalDiscretization = 0.5d; // ratio of discretization of FiLayerSet
				fmo.horizontalDistribVoxelNumberMaximum = 1000000d / fmo.fiLayerSetVerticalDiscretization; // depend on machine memory
				
				fmo.particleNames = m.particleNames; // fc-2.2.2015 particleNames not static any more
//				fmo.particleNames = FmModel.particleNames;
				
				//fmo.thinTwigsIncluded = true;
				fmo.verbose = true;
				
				
				PhysDataOptions pdo = new PhysDataOptions ();
				pdo.overlappingPermitted = true;
				pdo.produceTreeCrownVoxel = false;
				pdo.verbose = true;
	
				String format = PhysExporter.LITTLEENDIAN;
					
				Firetec export = new Firetec (2d, 2d, 15d, 41, 0.1, minX, minY, sizeX, sizeY, (FmStand) step.getScene (),
					fmo, pdo, dirName, format);
			}
		// Engine.getInstance().processSaveAsProject(s.getProject(),
		// outDir + ".prj");
					//s.getRootDir() + "tmp/" + name + ".prj");
		
		// Close the output file
		// out.flush();
		// out.close();
		// System.out.println("Closed " + outFileName);

	}

	/**
	 * Writes a header line in the output file
	 */
	static private void writeReportHeader(String name, BufferedWriter out)
	throws Exception {
		// out.write("Report of fireparadox.userscript." + name + " run at "
		// + new Date());
		// out.newLine();
		// out.newLine();
		out.write("totalCover	totalLoad	Ncomp	treeCover	gha	hmean	treeLoad	treeLAI	shrubLoad	shrubPhyto");
		out.newLine();

	}

	/**
	 * Writes a line in the output file for the given Step
	 */
	static private void writeReportLine(BufferedWriter out,
			Step step) throws Exception {
		// Appending in a StringBuffer is better than "String1" + "String2"...
		StringBuffer b = new StringBuffer();
		FmStand stand = (FmStand) step.getScene();
		double standArea =stand.getXSize() * stand.getYSize();
		int n = stand.getTrees().size();

		// fc-2.2.2015 particleNames
		FiModel m = (FiModel) stand.getStep().getProject().getModel();
		
		double[] properties = FmStand.calcMultiCov(stand
				.getTrees(), stand.getLayerSets(), stand.getPlot(), 2d, null, 
				m.particleNames); // fc-2.2.2015
		
		double cover = properties[0];
		b.append(cover); // totalCover
		b.append('\t');
		
		double load = properties[3]+properties[4];
		b.append(load); // totalLoad
		b.append('\t');
		
		double stemDensity = n / (standArea) * 10000d;
		b.append(stemDensity); // Ncomp
		b.append('\t'); 
		
		double treeCover = properties[2];
		b.append(treeCover); // treeCover
		b.append('\t'); 
		double treeLoad = 0d;
		double gha = 0d;
		double hmean = 0d; 
		for (Tree tree :stand.getTrees()) {
			gha += Math.PI * tree.getDbh() * tree.getDbh() / 4d * 0.0001;
			hmean += tree.getHeight();
			treeLoad += ((FiPlant) tree).getTotalThinMass (); // divided by standArea later
		}
		gha *= 1d / (standArea) * 10000d;
		if (n>0) hmean *= 1d / n;
		treeLoad *= 1d / (standArea);
		
		b.append(gha); // gha
		b.append('\t'); 
		
		b.append(hmean); // hmean
		b.append('\t'); 
		
		b.append(treeLoad); // load
		b.append('\t');
		
		b.append(properties[6]); // LAI
		b.append('\t');
		
		b.append(properties[3]); // shrubload
		b.append('\t');
		
		b.append(properties[5]); // shrubphyto
		b.append('\t');
		
		// b.append("Step ");
		// b.append(stand.getDate());
		// b.append('\t'); // this is a tabulation
		// b.append("NumberOfTrees ");
		// b.append(n);

		out.write(b.toString()); // StringBuffer -> String
		out.newLine();
	}

}