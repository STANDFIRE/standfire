package fireparadox.userscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import capsis.app.C4Script;
import capsis.extension.intervener.NrgThinner2;
import capsis.kernel.Engine;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import fireparadox.model.FmEvolutionParameters;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
 

/**	A script example for FireParadox
 *	@author F. de Coligny - june 2010
 */
public class Script01 {
	
    public static void main (String[] args) throws Exception {
    	
    	// Script creation
		C4Script s = new C4Script ("fireparadox");
		
		// Create the output directory
		// May be located in another directory, 
		// see System.getProperty ("user.dir") and s.getRootDir ()
		String outDir = s.getDataDir ()  // .../capsis4/data
				+ File.separator
				+ "fireparadox"
				+ File.separator
				+ "output";
		new File (outDir).mkdir();
		System.out.println ("ourDir = "+outDir);
				
		// Create the output file
		String outFileName = outDir
				+ File.separator
				+ "Script01";
		System.out.println ("outFileName = "+outFileName);
   	
		BufferedWriter out = new BufferedWriter (new FileWriter (outFileName)); 
		writeReportHeader (out);
		
		// Initialisation
		FmInitialParameters i = new FmInitialParameters ((FmModel) s.getModel ());
		
//		i.setInitMode (FiInitialParameters.InitMode.DETAILED_VIEW_ONLY);
		
		i.setInitMode(FmInitialParameters.InitMode.FROM_FIELD_PARAMETERS,0);
		i.fieldParameters = PathManager.getDir ("data")
				+ File.separator
				+ "fireparadox"
				+ File.separator
				+ "fromFieldParameters"
				+ File.separator
				+ "fieldDataExample_Aleppo_pine_virtual_stand.txt";
		
//		i.setInitMode (FiInitialParameters.InitMode.FROM_SCRATCH);
//		i.xDim = 50;
//		i.yDim = 75;
		
		s.init(i);
		
		// Get project root step
		Step root = s.getRoot();
		
		// Write in the file when needed
		writeReportLine (out, root); 

		// Evolution
		Step step = s.evolve (new FmEvolutionParameters (2)); 
		
		// Write in the file when needed
		writeReportLine (out, step); 
		
		// Create thinner
		int distCriterion = 0;
		double minDist = 1;
		int thinningCriterion = 2;
		double martellingDist = 7;
		NrgThinner2 t = new NrgThinner2 (distCriterion, minDist, 
				thinningCriterion, martellingDist);
		
		step = s.runIntervener (t, step);
		
		// Write in the file when needed
		writeReportLine (out, step); 

		// Evolution
		step = s.evolve (new FmEvolutionParameters (2)); 
		
		// Write in the file when needed
		writeReportLine (out, step); 
 		
		// Save the project, possible to reopen it with the interactive pilot 
		// (just for debugging, should be removed)
		Engine.getInstance ().processSaveAsProject (s.getProject(), 
				s.getRootDir() + "tmp/script01.prj");

		// Close the output file
		out.flush ();
		out.close();
		System.out.println ("Closed "+outFileName);
    
    }
    
    /**	Writes a header line in the output file
     */
    static private void writeReportHeader (BufferedWriter out) throws Exception {
		out.write ("Report of fireparadox.userscript.Script01 run at " + new Date ());
		out.newLine();
		out.newLine();
		out.write ("This is a header line, can be customized");
		out.newLine();
   	
    }
    
    /**	Writes a line in the output file for the given Step
     */
    static private void writeReportLine (BufferedWriter out, Step step) throws Exception {
    	// Appending in a StringBuffer is better than "String1" + "String2"...
    	StringBuffer b = new StringBuffer ();
    	
    	FmStand stand = (FmStand) step.getScene ();
    	int n = stand.getTrees().size ();
    	
    	b.append ("Step ");
     	b.append (stand.getDate ());
     	b.append ('\t');  // this is a tabulation
    	b.append ("NumberOfTrees ");
    	b.append (n);
 	
    	out.write (b.toString ());  // StringBuffer -> String
    	out.newLine();
    }
    
}