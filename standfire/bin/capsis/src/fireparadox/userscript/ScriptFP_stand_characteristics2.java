package fireparadox.userscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Vertex3d;
import capsis.app.C4Script;
import capsis.defaulttype.Tree;
import capsis.kernel.Engine;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.ParamParser;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.layerSet.FmLayerSet;

/**
 * This script is derived from Script01 from F d C: it builds different projects
 * with different stand parameter in Aleppo pine do different thinning and
 * pruning (eventually) and print different properties of the stand sh capsis.sh
 * -p script fireparadox.userscript.ScriptFP_stand_characteristics2
 * 
 * @author F. de Coligny and FP- june 2010
 * 
 */
public class ScriptFP_stand_characteristics2 {

	private static class Parameters {


		public Integer ageTot;
		public Double hDom50;
		public Double stemDensity;
		public Double gibbs;
		public Double liveNeedleMoistureContent;
		public Double deadTwigMoistureContent;
		public Double liveTwigMoistureContent;
		public Integer layerSetAge;
		public Double fertility;
		public Double shrubPercentage;
		public Double shrubCharacteristicSize;
		public Double herbPercentage;
		public Double herbCharacteristicSize;
		public Double liveMC;
		public Double deadMC;
		public Double sceneSize; // m


		public Parameters(int ageTot, double hDom50, double stemDensity, double gibbs, double liveNeedleMoistureContent, double liveTwigMoistureContent, double deadTwigMoistureContent,
				int layerSetAge, double fertility, double shrubPercentage,double shrubCharacteristicSize, double herbPercentage, double herbCharacteristicSize, double liveMC, double deadMC ,double sceneSize) {
			// tree parameters
			this.ageTot = ageTot;
			this.hDom50 = hDom50;
			this.stemDensity = stemDensity;
			this.gibbs = gibbs;
			this.liveNeedleMoistureContent = liveNeedleMoistureContent;
			this.liveTwigMoistureContent = liveTwigMoistureContent;
			this.deadTwigMoistureContent = deadTwigMoistureContent;
			// shrub parameters
			this.layerSetAge = layerSetAge;
			this.fertility = fertility;
			this.shrubPercentage = shrubPercentage;
			this.shrubCharacteristicSize = shrubCharacteristicSize;
			this.herbPercentage = herbPercentage;
			this.herbCharacteristicSize = herbCharacteristicSize;
			this.liveMC = liveMC;
			this.deadMC = deadMC;
			
			this.sceneSize = sceneSize;
		}
	}

	public static void main(String[] args) throws Throwable {

		// parameter definition
		String name = "ScriptFP_stand_characteristics2";
		
		//Collection<Parameters> parameterSet = new ArrayList<Parameters>();
		//parameterSet.add(new Parameters(50, 12d, 200d, 1000d, 100d, 80d, 10d, 15, 2, 90d, 4d, 20d,1d,70d,10d, 200d));
		boolean exportFile = false;
		
		// Script creation
		C4Script s = new C4Script ("fireparadox");

		
		// Create the output directory
		// May be located in another directory,
		// see System.getProperty ("user.dir") and s.getRootDir ()
		String dir = PathManager.getDir ("data") // .../capsis4/data
				+ File.separator + ".." + File.separator
				+ ".."
				+ File.separator + "Documents" + File.separator + "capsisdata";

		String inDir = dir + File.separator + "script" + File.separator + name;
		System.out.println("inDir = " + inDir);

		String outDir = dir + File.separator + "scriptoutput";
		new File(outDir).mkdir();
		System.out.println("outDir = " + outDir);

		// Create the output file
		String outFileName = outDir + File.separator + name + args[1];
		System.out.println("outFileName = " + outFileName);

		ParamParser pp = new ParamParser(inDir + File.separator + args[1]);
		List<Parameters> parameterSet = pp.parse(Parameters.class);

		BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
		writeReportHeader(name, out);

		// Initialisation

		for (Parameters p : parameterSet) {
			FmInitialParameters i = new FmInitialParameters((FmModel) s
					.getModel());
			// i.setInitMode (FiInitialParameters.InitMode.DETAILED_VIEW_ONLY);
			// i.setInitMode(FiInitialParameters.InitMode.FROM_FIELD_PARAMETERS);
			// i.fieldParameters = Options.getDir ("data")
			// + File.separator
			// + "fireparadox"
			// + File.separator
			// + "fromFieldParameters"
			// + File.separator
			// + "fieldDataExample_Aleppo_pine_virtual_stand.txt";

			i.setInitMode(FmInitialParameters.InitMode.FROM_SCRATCH,0);
			i.xDim = p.sceneSize;
			i.yDim = p.sceneSize;
			String speciesName = FmModel.PINUS_HALEPENSIS;
			double hDom50 = p.hDom50;
			int ageTot = p.ageTot;
			System.out.println("ageTot = " + ageTot);
			double stemDensity = p.stemDensity;
			double gHaETFactor = 0d;
			double gibbs = p.gibbs;
			double liveNeedleMoistureContent = p.liveNeedleMoistureContent;
			double liveTwigMoistureContent = p.liveTwigMoistureContent;
			double deadTwigMoistureContent = p.deadTwigMoistureContent;
			

			s.init(i);

			FmModel m = (FmModel) s.getModel();
			// FiInitialParameters settings = m.getSettings();
			FmStand initStand = (FmStand) s.getRoot().getScene();

			// the polygon defined here contains the whole scene
			Polygon currentPoly = new Polygon();
			double minX = 0d;
			double maxX = i.xDim;
			double minY = 0d;
			double maxY = i.yDim;
			double altitude = 0d;
			currentPoly.add(new Vertex3d(minX, minY, altitude));
			currentPoly.add(new Vertex3d(maxX, minY, altitude));
			currentPoly.add(new Vertex3d(maxX, maxY, altitude));
			currentPoly.add(new Vertex3d(minX, maxY, altitude));
			currentPoly.ensureTrigonometricOrder();
			currentPoly.setClosed(true);
			// end of polygon definition
			initStand.addTreeGroup2( speciesName, currentPoly,
					hDom50, ageTot, stemDensity, gHaETFactor, "HARDCORE",
					gibbs,
					liveNeedleMoistureContent, deadTwigMoistureContent,
					liveTwigMoistureContent);
			
			int layerSetAge = p.layerSetAge;
			double fertility = p.fertility;
			double shrubPercentage = p.shrubPercentage;
			double shrubCharacteristicSize = p.shrubCharacteristicSize;
			double herbPercentage = p.herbPercentage;
			double herbCharacteristicSize = p.herbCharacteristicSize;
			double liveMC = p.liveMC;
			double deadMC = p.deadMC;
			
			// creation (current age is 0)
			FmLayerSet ls = new FmLayerSet(initStand.maxId, currentPoly, fertility, shrubPercentage, shrubCharacteristicSize, herbPercentage, herbCharacteristicSize, liveMC, deadMC);
			// initial evolution
			ls = ls.processGrowth(initStand, layerSetAge);
			// added to Stand
			initStand.addLayerSet(ls);
			
			// Get project root step
			Step root = s.getRoot();
			Step step = root;
			// Write in the file when needed
			// writeReportLine(p, out, root);

			// Evolution
			// Step step = s.evolve (new FiEvolutionParameters (2));

			// Write in the file when needed
			writeReportLine(p, out, step);

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
				String dirName = outFileName+"_firetec";
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
				boolean thinTwigsIncluded = true;
				boolean overlappingPermitted = true;
				double polygonResolution = 1d;
				String format = "NativeBinaryInputStream.X86";
				// TODO fc+fp-10.9.2013
//
//				FiretecSingleFamily export = new FiretecSingleFamily(2d,
//					2d, 15d, 41, 0.1, minX, minY, maxX - minX, maxY - minY,
//					thinTwigsIncluded, overlappingPermitted, polygonResolution,
//					(FmStand) step.getScene(), format);
//				export.save(dirName);
			}
			Engine.getInstance().processSaveAsProject(s.getProject(),
					outFileName + ".prj");
					//s.getRootDir() + "tmp/" + name + ".prj");
		}
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
		// out.write("Report of fireparadox.userscript." + name + " run at "
		// + new Date());
		// out.newLine();
		// out.newLine();
		out.write("age	hdom50	N	domainSize	Ncomp	cover	gha	hmean	lai	load");
		out.newLine();

	}

	/**
	 * Writes a line in the output file for the given Step
	 */
	static private void writeReportLine(Parameters param, BufferedWriter out,
			Step step) throws Exception {
		// Appending in a StringBuffer is better than "String1" + "String2"...
		StringBuffer b = new StringBuffer();

		b.append(param.ageTot);
		b.append('\t');
		b.append(param.hDom50);
		b.append('\t');
		b.append(param.stemDensity);
		b.append('\t');
		b.append(param.sceneSize);
		b.append('\t');
		
		FmStand stand = (FmStand) step.getScene();
		int n = stand.getTrees().size();
		double stemDensity = n / (param.sceneSize * param.sceneSize) * 10000d;
		b.append(stemDensity); // Ncomp
		b.append('\t'); 
		
		// fc-2.2.2015 particleNames
		FiModel m = (FiModel) stand.getStep().getProject().getModel();
		
		double[] properties = FmStand.calcMultiCov(stand
				.getTrees(), null, stand.getPlot(), 0d, null, 
				m.particleNames); // fc-2.2.2015
		
		double cover = properties[0];

		b.append(cover); // cover
		b.append('\t'); 
		double load = 0d;
		double load2 = 0d;
		double gha = 0d;
		double lai = 0d;
		double hmean = 0d; 
		for (Tree tree :stand.getTrees()) {
			gha += Math.PI * tree.getDbh() * tree.getDbh() / 4d * 0.0001;
			hmean += tree.getHeight();
			load += ((FiPlant) tree).getTotalThinMass (); // divided by standArea later
			double cd = ((FiPlant) tree).getCrownDiameter();
			lai += ((FiPlant) tree).getLai()
					* FiPlant.computeCrownProjectedArea(cd, cd); // leaf
			// area
		}
		gha *= 1d / (param.sceneSize * param.sceneSize) * 10000d;
		hmean *= 1d / n;
		load *= 1d / (param.sceneSize * param.sceneSize);
		load2 *= 1d / (param.sceneSize * param.sceneSize);
		lai *= 1d / (param.sceneSize * param.sceneSize);
		b.append(gha); // gha
		b.append('\t'); 
		
		b.append(hmean); // hmean
		b.append('\t'); 
		
		b.append(lai); // lai
		b.append('\t'); 
		
		b.append(load); // load
		b.append('\t'); 
		
	// b.append(load2); // load
		// b.append('\t');
		
		
		
		

		// b.append("Step ");
		// b.append(stand.getDate());
		// b.append('\t'); // this is a tabulation
		// b.append("NumberOfTrees ");
		// b.append(n);

		out.write(b.toString()); // StringBuffer -> String
		out.newLine();
	}

}