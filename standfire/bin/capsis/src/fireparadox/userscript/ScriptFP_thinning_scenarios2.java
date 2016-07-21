package fireparadox.userscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;

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
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;

/**
 * This script is derived from Script01 from F d C: it builds different projects
 * with different stand parameter in Aleppo pine do different thinning and
 * pruning (eventually) and print different properties of the stand sh capsis.sh
 * -p script fireparadox.userscript.ScriptFP_thinning_scenarios2
 * 
 * @author F. de Coligny and FP- june 2010
 * 
 */
public class ScriptFP_thinning_scenarios2 {

	private static class Parameters {

		public int ageTot;
		public double hDom50;
		public double stemDensity;
		public double sceneSize; // m

		public Parameters(int ageTot, double hDom50, double stemDensity,
				double sceneSize) {
			this.ageTot = ageTot;
			this.hDom50 = hDom50;
			this.stemDensity = stemDensity;
			this.sceneSize = sceneSize;
		}
	}

	public static void main(String[] args) throws Exception {

		// parameter definition
		Collection<Parameters> parameterSet = new ArrayList<Parameters>();
		double size = 500d;
		parameterSet.add(new Parameters(30, 8d, 400d, size));
		parameterSet.add(new Parameters(30, 8d, 700d, size));
		parameterSet.add(new Parameters(30, 8d, 1000d, size));
		parameterSet.add(new Parameters(30, 12d, 400d, size));
		parameterSet.add(new Parameters(30, 12d, 750d, size));
		parameterSet.add(new Parameters(30, 12d, 1100d, size));
		parameterSet.add(new Parameters(30, 16d, 400d, size));
		parameterSet.add(new Parameters(30, 16d, 800d, size));
		parameterSet.add(new Parameters(30, 16d, 1200d, size));
		parameterSet.add(new Parameters(60, 8d, 300d, size));
		parameterSet.add(new Parameters(60, 8d, 650d, size));
		parameterSet.add(new Parameters(60, 8d, 900d, size));
		parameterSet.add(new Parameters(60, 12d, 240d, size));
		parameterSet.add(new Parameters(60, 12d, 570d, size));
		parameterSet.add(new Parameters(60, 12d, 900d, size));
		parameterSet.add(new Parameters(60, 16d, 240d, size));
		parameterSet.add(new Parameters(60, 16d, 570d, size));
		parameterSet.add(new Parameters(60, 16d, 900d, size));
		parameterSet.add(new Parameters(80, 8d, 200d, size));
		parameterSet.add(new Parameters(80, 8d, 500d, size));
		parameterSet.add(new Parameters(80, 8d, 800d, size));
		parameterSet.add(new Parameters(80, 12d, 200d, size));
		parameterSet.add(new Parameters(80, 12d, 500d, size));
		parameterSet.add(new Parameters(80, 12d, 800d, size));
		parameterSet.add(new Parameters(80, 16d, 200d, size));
		parameterSet.add(new Parameters(80, 16d, 450d, size));
		parameterSet.add(new Parameters(80, 16d, 700d, size));
			
		
		// Script creation
		C4Script s = new C4Script ("fireparadox");

		String name = "ScriptFP_thinning_scenarios";
		// Create the output directory
		// May be located in another directory,
		// see System.getProperty ("user.dir") and s.getRootDir ()
		String outDir = PathManager.getDir ("data") // .../capsis4/data
				+ File.separator + "fireparadox" + File.separator + "output";
		new File(outDir).mkdir();
		System.out.println("outDir = " + outDir);

		// Create the output file
		String outFileName = outDir + File.separator + name;
		System.out.println("outFileName = " + outFileName);

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
			double stemDensity = p.stemDensity;
			double gHaETFactor = 0d;
			double gibbs = 0d;
			double liveNeedleMoistureContent = 100d;
			double deadTwigMoistureContent = 80d;
			double liveTwigMoistureContent = 10d;

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
					hDom50, ageTot, stemDensity, gHaETFactor, "GIBBS", gibbs,
					liveNeedleMoistureContent, deadTwigMoistureContent,
					liveTwigMoistureContent);

			// int n = initStand.getTrees().size();
			// System.out.println("n=" + n);
			// m.setInitStand(initStand);

			// Get project root step
			Step root = s.getRoot();
			
			// Write in the file when needed
			int thinningCriterion = -1; // no thinning
			int distCriterion = 1;
			double minDist = 0;
			double martellingDist = Math.sqrt(7d
					/ ((TreeList) initStand).getTrees().size()
					* initStand.getArea());
			martellingDist = Math.min(martellingDist, 15d);
			martellingDist = Math.max(martellingDist, 3d);
			
			writeReportLine(p, out, root, thinningCriterion, minDist,
					martellingDist);

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
			double[] minDistList = new double[4];
			minDistList[0] = 1d;
			minDistList[1] = 3d;
			minDistList[2] = 5d;
			minDistList[3] = 10d;
			for (int tc = 0; tc < 1; tc++) {
				for (int md = 0; md < 4; md++) {
					NrgThinner2 t = new NrgThinner2(distCriterion,
							minDistList[md], thinningCriterion + tc,
							martellingDist);
					Step step = s.runIntervener(t, root);

					// Write in the file when needed
					writeReportLine(p, out, step, thinningCriterion + tc,
							minDistList[md], martellingDist);
				}
			}

			// Save the project, possible to reopen it with the interactive
			// pilot
			// (just for debugging, should be removed)
			Engine.getInstance().processSaveAsProject(s.getProject(),
					s.getRootDir() + "tmp/" + name + ".prj");
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
		out
				.write("age	hdom50	N	domainSize	thinningCrit	minDist	martellingDist	Ncomp	cover	gha	hmean	load");
		out.newLine();

	}

	/**
	 * Writes a line in the output file for the given Step
	 */
	static private void writeReportLine(Parameters param, BufferedWriter out,
			Step step, int thinningCriterion, double minDist,
			double martellingDist) throws Exception {
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
		b.append(thinningCriterion);
		b.append('\t');
		b.append(minDist);
		b.append('\t');
		b.append((float) martellingDist);
		b.append('\t');
		
		FmStand stand = (FmStand) step.getScene();
		int n = stand.getTrees().size();
		double stemDensity = n / (param.sceneSize * param.sceneSize) * 10000d;
		b.append((float) stemDensity); // Ncomp
		b.append('\t'); 
		
		// fc-2.2.2015 particleNames
		FiModel m = (FiModel) stand.getStep().getProject().getModel();
		
		double[] properties = FmStand.calcMultiCov(stand
				.getTrees(), null, stand.getPlot(), 0d, null, 
				m.particleNames); // fc-2.2.22015 particleNames
		double cover = properties[0];

		b.append((float) cover); // cover
		b.append('\t'); 
		double load = 0d;
		double gha = 0d;
		double hmean = 0d; 
		for (Tree tree :stand.getTrees()) {
			gha += Math.PI * tree.getDbh() * tree.getDbh() / 4d * 0.0001;
			hmean += tree.getHeight();
			load += ((FiPlant) tree).getTotalThinMass (); // divided by standArea later
		}
		gha *= 1d / (param.sceneSize * param.sceneSize) * 10000d;
		hmean *= 1d / n;
		load *= 1d / (param.sceneSize * param.sceneSize);
		
		b.append((float) gha); // gha
		b.append('\t'); 
		
		b.append((float) hmean); // hmean
		b.append('\t'); 
		
		b.append((float) load); // load
		b.append('\t'); 
	
		out.write(b.toString()); // StringBuffer -> String
		out.newLine();
	}

}