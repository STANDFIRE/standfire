package fireparadox.userscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import capsis.app.C4Script;
import capsis.defaulttype.Tree;
import capsis.kernel.Engine;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiPlant;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

/**
 * This script entails to open a file from SVS model and to export it to firetec format
 * to run it do : sh capsis.sh -p script fireparadox.userscript.ScriptKD_readFromSVSFile and add the name of the file to open
 * 
 * @author F Pimont- aout 2010
 * 
 */
public class ScriptKD_readFromSVSFile {

	
	public static void main(String[] args) throws Throwable {

		// parameter definition
		//System.out.println ("args length="+args.length+";arg[1]="+args[0]);
		String name = args[1]; //name of the file;
		boolean exportFile = true;
		
		// Script creation
		C4Script s = new C4Script ("fireparadox");

		
		// Create the output directory
		// May be located in another directory,
		// see System.getProperty ("user.dir") and s.getRootDir ()
		
		// PATH TO CHECK
		String outDir = PathManager.getDir ("data") // .../capsis4/data
				+ File.separator + ".."+File.separator+".."+File.separator+"Documents"+File.separator+"capsisdata"+File.separator+name;
		new File(outDir).mkdir();
		System.out.println("outDir = " + outDir);

		// Create the output file
		String outFileName = outDir + File.separator + name;
		System.out.println("outFileName = " + outFileName);

		BufferedWriter out = new BufferedWriter(new FileWriter(outFileName+".report"));
		writeReportHeader(name, out);

		// Initialisation

		FmInitialParameters i = new FmInitialParameters((FmModel) s
					.getModel());
			i.setInitMode(FmInitialParameters.InitMode.FROM_SVS_PARAMETERS,0);
			i.SVSParameters = outDir+".txt";
			s.init(i);

			FmModel m = (FmModel) s.getModel();
			FmStand initStand = (FmStand) s.getRoot().getScene();
						
			// Get project root step
			Step root = s.getRoot();
			Step step = root;
			writeReportLine(out, step);
			if (exportFile) {
				String dirName = outDir;
				double minX = initStand.getOrigin().x;
				double minY = initStand.getOrigin().y;
				double maxX = minX + initStand.getXSize();
				double maxY = minY + initStand.getYSize();
				
				
				boolean thinTwigsIncluded = true;
				boolean overlappingPermitted = true;
				double polygonResolution = 1d;
				String format = "NativeBinaryInputStream.X86";
				// TODO fc+fp-10.9.2013
//				FiretecSingleFamily export = new FiretecSingleFamily(2d,
//					2d, 15d, 41, 0.1, minX, minY, maxX - minX, maxY - minY,
//					thinTwigsIncluded, overlappingPermitted, polygonResolution,
//					(FmStand) step.getScene(), format);
//				export.save(dirName);
			}
			Engine.getInstance().processSaveAsProject(s.getProject(),
					outDir + ".prj");
					//s.getRootDir() + "tmp/" + name + ".prj");
		
		// Close the output file
		out.flush();
		out.close();
		System.out.println("Closed " + outFileName);

	}

	/**
	 * Writes a header line in the output file
	 */
	static private void writeReportHeader(String name, BufferedWriter out)
	throws Exception {
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
				m.particleNames); // fc-2.2.2015 particlaNames
		
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
	
		out.write(b.toString()); // StringBuffer -> String
		out.newLine();
	}

}