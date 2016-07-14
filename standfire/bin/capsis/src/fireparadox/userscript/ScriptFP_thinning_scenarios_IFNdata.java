package fireparadox.userscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Vertex3d;
import capsis.app.C4Script;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.intervener.NrgThinner2;
import capsis.kernel.Engine;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.ParamParser;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

/**
 * This script is derived from Script01 from F d C: it builds different projects
 * with different stand parameter in Aleppo pine do different thinning and
 * pruning (eventually) and print different properties of the stand sh capsis.sh
 * -p script fireparadox.userscript.ScriptFP_thinning_scenarios_IFNdata
 * 
 * @author F. de Coligny and FP- june 2010
 * 
 */
public class ScriptFP_thinning_scenarios_IFNdata {

	private static class Parameters {
		public Integer ageTot;
		public Double hDom;
		public Double gha;
		public Double stemDensity;
		public Double sceneSize; // m


		public Parameters(int ageTot, double hDom, double gha,
				double stemDensity, double sceneSize) {
			// tree parameters
			this.ageTot = ageTot;
			this.hDom = hDom;
			this.gha = gha;
			this.stemDensity = stemDensity;
			this.sceneSize = sceneSize;
		}
	}

	public static void main(String[] args) throws Throwable {

		// parameter definition
		String name = "ScriptFP_thinning_scenarios_IFNdata";
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
		outDir = outDir + File.separator + name;
		new File(outDir).mkdir();
		System.out.println("outDir = " + outDir);

		// Create the output file
		String outFileName = outDir + File.separator + args[1];
		System.out.println("outFileName = " + outFileName);

		ParamParser pp = new ParamParser(inDir + File.separator + args[1]);
		List<Parameters> parameterSet = pp.parse(Parameters.class);

		BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
		writeReportHeader(name, out);

		// Initialisation

		for (Parameters p : parameterSet) {
			FmInitialParameters i = new FmInitialParameters((FmModel) s
					.getModel());
					i.setInitMode(FmInitialParameters.InitMode.FROM_SCRATCH,0);
			i.xDim = p.sceneSize;
			i.yDim = p.sceneSize;
			String speciesName = FmModel.PINUS_HALEPENSIS;
			double hDom = p.hDom;
			int ageTot = p.ageTot;
			System.out.println("ageTot = " + ageTot);
			double stemDensity = p.stemDensity;
			double gHa = p.gha;
			double gibbs = 0d;
			double liveNeedleMoistureContent = 100d;
			double liveTwigMoistureContent = 80d;
			double deadTwigMoistureContent = 10d;
			

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
			//stemDensity=1d;
			initStand.addTreeGroup3(speciesName, currentPoly,
					hDom, ageTot, stemDensity, gHa, "HARDCORE",
					gibbs,
					liveNeedleMoistureContent, deadTwigMoistureContent,
					liveTwigMoistureContent);
		
			
			// Get project root step
			Step root = s.getRoot();
			// computation of initial cover and gha
			FmStand stand = (FmStand) root.getScene();
			int n = stand.getTrees().size();

			double[] properties = FmStand.calcMultiCov(stand
					.getTrees(), null, stand.getPlot(), 0d, null, m.particleNames); // fc-2.2.2015 particleNames
			
			double cover = properties[0];
			double gha = 0d;
			for (Tree tree : stand.getTrees()) {
				gha += Math.PI * tree.getDbh() * tree.getDbh() / 4d * 0.0001;
			}
			gha *= 1d / (p.sceneSize * p.sceneSize) * 10000d;

			
			
			// Write in the file when needed
			int thinningCriterion = -1; // no thinning
			
			double minDist = -1;
			double martellingDist = Math.sqrt(7d
					/ ((TreeList) initStand).getTrees().size()
					* initStand.getArea());
			martellingDist = Math.min(martellingDist, 15d);
			martellingDist = Math.max(martellingDist, 3d);

			writeReportLine(cover, gha, p, out, root, thinningCriterion,
					minDist, martellingDist);

			// Evolution
			// Step step = s.evolve (new FiEvolutionParameters (2));

			// Create thinner
			// distCriterion: 0:stem, 1:crown
			// minDist: distance between stems or crown depending on
			// distCriterion
			// thinningCriterion: 0:randomWalk, 1:keepBigTrees, 2:foresterLike,
			// 3:simulatedAnnealing, 4:optimal
			// martellingDist: martelling distance if thinningCriterion =
			// foresterLike

			thinningCriterion = 2;
			int distCriterion = 3;
			double[] minDistList = new double[8];
			minDistList[0] = 0d;
			minDistList[1] = 1d;
			minDistList[2] = 2d;
			minDistList[3] = 3d;
			minDistList[4] = 4d;
			minDistList[5] = 5d;
			minDistList[6] = 7d;
			minDistList[7] = 10d;
			for (int tc = 0; tc < 1; tc++) {
				for (int md = 0; md < 8; md++) {
					NrgThinner2 t = new NrgThinner2(distCriterion,
							minDistList[md], thinningCriterion + tc,
							martellingDist);
					Step step = s.runIntervener(t, root);

					// Write in the file when needed
					writeReportLine(cover, gha, p, out, step, thinningCriterion
							+ tc, minDistList[md], martellingDist);
				}
			}
			Engine.getInstance().processSaveAsProject(s.getProject(),
					s.getRootDir() + "tmp/script01.prj");

			s.closeProject();
			
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
		out.write("age	hdom	N	cover	gha	minDist	N	gha	cover	lai	load");
		out.newLine();

	}
	
	/**
	 * Writes a line in the output file for the given Step
	 */
	static private void writeReportLine(double cover, double gha,
			Parameters param, BufferedWriter out, Step step,
			int thinningCriterion, double minDist, double martellingDist)
			throws Exception {
		// Appending in a StringBuffer is better than "String1" + "String2"...
		StringBuffer b = new StringBuffer();

		b.append(param.ageTot);
		b.append('\t');
		b.append(param.hDom);
		b.append('\t');
		b.append(param.stemDensity);
		b.append('\t');
		b.append(cover);
		b.append('\t');
		b.append(gha);
		b.append('\t');
		
		b.append(minDist);
		b.append('\t');
		
		FmStand stand = (FmStand) step.getScene();
		int n = stand.getTrees().size();
		double stemDensity2 = n / (param.sceneSize * param.sceneSize) * 10000d;
		b.append((float) stemDensity2); // Ncomp
		b.append('\t'); 
		
		// fc-2.2.2015 particleNames
		FiModel m = (FiModel) stand.getStep().getProject().getModel();
		
		double[] properties = FmStand.calcMultiCov(stand
				.getTrees(), null, stand.getPlot(), 0d, null, 
				m.particleNames); // fc-2.2.2015 particleNames
		
		double cover2 = properties[0];

		double load2 = 0d;
		double gha2 = 0d;
		double lai2 = 0d;
		for (Tree tree :stand.getTrees()) {
			gha2 += Math.PI * tree.getDbh() * tree.getDbh() / 4d * 0.0001;
			load2 += ((FiPlant) tree).getTotalThinMass (); // divided by standArea later
			double cd = ((FiPlant) tree).getCrownDiameter();
			lai2 += ((FiPlant) tree).getLai()
					* FiPlant.computeCrownProjectedArea(cd, cd); // leaf
			// area
		}
		gha2 *= 1d / (param.sceneSize * param.sceneSize) * 10000d;
		lai2 *= 1d / (param.sceneSize * param.sceneSize);
		load2 *= 1d / (param.sceneSize * param.sceneSize);
		
		b.append((float) gha2); // gha
		b.append('\t'); 
		
		b.append((float) cover2); 
		b.append('\t'); 
		
		b.append((float) lai2); 
		b.append('\t'); 
		
		
		b.append((float) load2); // load
		b.append('\t'); 
	
		out.write(b.toString()); // StringBuffer -> String
		out.newLine();
	}

}