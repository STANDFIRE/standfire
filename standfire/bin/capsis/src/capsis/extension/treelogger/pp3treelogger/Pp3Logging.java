/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.treelogger.pp3treelogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import jeeb.lib.util.Log;
import jeeb.lib.util.TicketDispenser;
import jeeb.lib.util.Translator;
import pp3.model.Pp3ModelParameters;
import pp3.model.Pp3Stand;
import pp3.model.Pp3Tree;
import pp3.model.Pp3TreeBiomass;
import pp3.model.Pp3TreeTaper;
import pp3.model.Pp3Volume;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.WoodPiece.Property;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.treelogger.GContour;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.GPieceRing;
import capsis.extensiontype.TreeLoggerImpl;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.util.methodprovider.TreeRadius_cmProvider;


//import capsis.extension.modeltool.woodqualityworkshop.Pp3Logging.products.*;

/**	Pp3Logging : logging job for PP3 trees.
*
*	@author C. Meredieu + T. Labbé - march 2006
*/
public class Pp3Logging extends TreeLogger<Pp3LoggingTreeLoggerParameters, LoggableTree> implements TreeLoggerImpl {

	static {
		Translator.addBundle("capsis.extension.treelogger.pp3treelogger.Pp3Logging");
	}
	
	static public final String AUTHOR = "Céline Meredieu, Thierry Labbé";
	static public final String VERSION = "1.0";
	static public final String DESCRIPTION = "Pp3Logging.description";


	private Step step;
//	private Collection trees;	// is now in the super class
//	private String treeStatus;	// Status of the trees in the collection (ex: "Alive")
//	private Pp3LoggingTreeLoggerParameters params; 	is now in the super class, use the overrided method getTreeLoggerParameters instead

//	private Collection<GPiece> pieces;

	private String errorReport;		// if not null, trouble during run ()
	
	private TicketDispenser pieceIdDispenser;
	private boolean interventionStep;
	private NumberFormat nf2;
	private NumberFormat nf3;
	private NumberFormat nf6;
	private Collection out;

//	public class NormalException extends Exception {
//		public NormalException (String message) {
//			super (message);
//		}
//	}

	private class EmptyRecord {
		public EmptyRecord () {}
		public String toString () {return "\n";}
	}

	private class FreeRecord {
		private String s;
		public FreeRecord (String s) {this.s = s;}
		public String toString () {return s;}
	}

	private class CommentRecord {
		private String s;
		public CommentRecord (String s) {this.s = s;}
		public String toString () {return "#"+s;}
	}


	/**
	 * Constructor for Gui mode
	 */
	public Pp3Logging() {
		super();
	}

	/**	Constructor
	*	May throw exception if something's wrong
	*/
	public Pp3Logging (
//			Step step, 			// we may need previous steps
			Collection trees,	// required
//			String treeStatus,
//			Pp3LoggingTreeLoggerParameters starter) throws Exception {
			Pp3LoggingTreeLoggerParameters params) throws Exception {
		super ();	// we will have an id, a status and an initDate
//		this.step = step;			// now set in the override of the init method
//		this.trees = trees;
		// FgStatus are in fagacees/model/FgStatus_*.properties :
//		this.treeStatus = Translator.swap (treeStatus);				// not necessary never read;
//		if (trees == null || trees.isEmpty ()) {
//			throw new NormalException (Translator.swap ("Pp3Logging.MsgNoTree"));}
		this.setTreeLoggerParameters(params);
		this.init(trees);
		
//		this.params = starter;
//		
//		if (!starter.isCorrect ()) {
//			// This dialog will ensure the starter is complete
//			Pp3LoggingDialog dlg = new Pp3LoggingDialog (starter);
//
//			// dlg is valid if "ok" button and starter.isCorrect ()
//			if (!dlg.isValidDialog ()) {
//					throw new NormalException (Translator.swap ("Pp3Logging.MsgAbort"));}
//			dlg.dispose ();
//		}

		// direct java implementation (TEST), see run ()
		// for a Launcher example, see LogJob
	}

	@Override
	public void init(Collection<?> loggableTrees) {
		super.init(loggableTrees);
		step = findLastStepAmongTrees();
 	}

	/**
	 * Patch to avoid the step as parameter in the constructor - MF2010-04-13
	 * @return
	 */
	private Step findLastStepAmongTrees() {
		Step step = null;
		Step newStep;
		for (LoggableTree o : getLoggableTrees()) {
			Tree t = (Tree) o;
			newStep = t.getScene().getStep();
			if (step == null) {
				step = newStep;
			} else {
				int date = step.getScene().getDate();
				int newDate = newStep.getScene().getDate();
				if (newDate > date) {
					step = newStep;
				} else if (newDate == date && newStep.getScene().isInterventionResult()) {
					step = newStep;
				}
			}
		}
		return step;
	}
	
	/**	
	 * Extension dynamic compatibility mechanism.
	 * This matchWith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public static boolean matchWith(Object referent) {
		MethodProvider mp = ((GModel) referent).getMethodProvider ();
		if (!(mp instanceof TreeRadius_cmProvider)) {
			return false;
		}
		return true;
	}
	
	@Override
	protected void priorToRunning() {
//		int treeCounter = 0;
//		double progressFactor = 0;
//		if (getLoggableTrees().size() > 0) {
//			progressFactor = (double) 100d / getLoggableTrees().size();
//		}
		pieceIdDispenser = new TicketDispenser();
//		int logId = 1;
//		getWoodPieces().clear();		// pieces are now stored in the super class
//		pieces = new ArrayList<GPiece> ();
		
//		double stumpHeight = getTreeLoggerParameters().STUMP_HEIGHT;
//		double topGirth = getTreeLoggerParameters().topGirth;
//		double top1Girth = getTreeLoggerParameters().top1Girth;
//		double log1Length = getTreeLoggerParameters().log1Length;
//		double top2Girth = getTreeLoggerParameters().top2Girth;
//		double log2Length = getTreeLoggerParameters().log2Length;
//		double top3Girth = getTreeLoggerParameters().top3Girth;
//		double log3Length = getTreeLoggerParameters().log3Length;
		TreeList stand = (TreeList) step.getScene ();

		out = new ArrayList ();

		// Coordinates formater : decimal with no more than 3 fraction digits
		nf2 = NumberFormat.getNumberInstance (Locale.ENGLISH);
		nf2.setMaximumFractionDigits (2);
		nf2.setGroupingUsed (false);
		nf3 = NumberFormat.getNumberInstance (Locale.ENGLISH);
		nf3.setMaximumFractionDigits (3);
		nf3.setGroupingUsed (false);
		nf6 = NumberFormat.getNumberInstance (Locale.ENGLISH);
		nf6.setMaximumFractionDigits (6);
		nf6.setGroupingUsed (false);

//		//variables
//		int numberOfLog = 0;
//		double number;
//		double stumpDiameter ;
//		double largeEndD, largeEndDInsideBark ,largeEndH, largeEndDJuvenileWood=0;
//		double smallEndH ,smallEndD ,smallEndHForTopDiameter = 0, smallEndDInsideBark , smallEndDJuvenileWood=0;
//		double logLength = 0,totalLogsLength;
//		double woodVolume,totalVolume,barkVolume, juvenileWoodVolume = 0;
//		double woodMass, woodCarbonMass;
//		StringBuffer b;
//		String pieceOrigin = "";
//		String pieceProduct = "";
//		boolean boolAllJuvenileWoodAtLargeEndH , boolAllJuvenileWoodAtSmallEndH;

		//Header
		out.add (new CommentRecord ("Model : " +  stand.getStep ().getProject ().getModel ().getPackageName ()
		+ ",Project : " + stand.getStep ().getProject ().getName ()
		+ ",Step : " + stand.getStep ().getName ()));
		out.add (new EmptyRecord ());
		out.add (new FreeRecord ("Id,Age,DBH,HT,NumberOfTrees,LogNumber,LogLength,LargeEndD,LargeEndDInsideBark,LargeEndDJuvenileWood,"+
		"LargeEndH,SmallEndD,SmallEndDInsideBark,SmallEndDJuvenileWood,SmallEndH,WoodVolume,JuvenileWoodVolume,BarkVolume,TotalVolume,WoodMass,WoodCarbonMass  "));

		// Find the age of the initial step
/*		Step step0 = stand.getStep ();
		Step step1 = step0;
		while (step0 != null) {
			step1 = step0;
			step0 = (Step) step0.getFather ();
		}
		Pp3Stand stand1 = (Pp3Stand) step1.getStand();
		if (stand1.getAge()> Pp3ModelParameters.JUVENILE_WOOD_AGE)  return;
*/
//		int ageMin = stand.getRootDate ();
//		if (ageMin > Pp3ModelParameters.JUVENILE_WOOD_AGE)  return;
//
//		// Retrieve Steps from root to this step
//		Step lastStep = stand.getStep();
//		Vector steps = stand.getStep ().getProject ().getStepsFromRoot (stand.getStep ());
/*
		for (Iterator w = steps.iterator (); w.hasNext ();) {
			Step step = (Step) w.next ();
			stand = (GTCStand) step.getStand ();
			boolean interventionStep = step.getStand ().isInterventionResult ();
//			Log.println ("step =" + step+" bool =" + interventionStep);
			if ((interventionStep) || (step==lastStep)) {
				Object[] trees = stand.getTrees ().toArray ();

				Pp3TreeDiameterComparator comp = new Pp3TreeDiameterComparator(true);
				Arrays.sort(trees, comp);

				for (int i = 0; i < trees.length; i++) {
					Pp3Tree t = (Pp3Tree) trees[i];
*/
		interventionStep = false;

	}

//	/** Call run after the constructor
//	*/
//	public void logTrees() throws Exception {
//		int treeCounter = 0;
//		double progressFactor = 0;
//		if (getLoggableTrees().size() > 0) {
//			progressFactor = (double) 100d / getLoggableTrees().size();
//		}
//		try {
//			TicketDispenser pieceIdDispenser = new TicketDispenser();
////		int logId = 1;
////		getWoodPieces().clear();		// pieces are now stored in the super class
////		pieces = new ArrayList<GPiece> ();
//		
////		double stumpHeight = getTreeLoggerParameters().STUMP_HEIGHT;
////		double topGirth = getTreeLoggerParameters().topGirth;
////		double top1Girth = getTreeLoggerParameters().top1Girth;
////		double log1Length = getTreeLoggerParameters().log1Length;
////		double top2Girth = getTreeLoggerParameters().top2Girth;
////		double log2Length = getTreeLoggerParameters().log2Length;
////		double top3Girth = getTreeLoggerParameters().top3Girth;
////		double log3Length = getTreeLoggerParameters().log3Length;
////		TreeList stand = (TreeList) step.getScene ();
//
////		Collection out = new ArrayList ();
//
////		// Coordinates formater : decimal with no more than 3 fraction digits
////		NumberFormat nf2 = NumberFormat.getNumberInstance (Locale.ENGLISH);
////		nf2.setMaximumFractionDigits (2);
////		nf2.setGroupingUsed (false);
////		NumberFormat nf3 = NumberFormat.getNumberInstance (Locale.ENGLISH);
////		nf3.setMaximumFractionDigits (3);
////		nf3.setGroupingUsed (false);
////		NumberFormat nf6 = NumberFormat.getNumberInstance (Locale.ENGLISH);
////		nf6.setMaximumFractionDigits (6);
////		nf6.setGroupingUsed (false);
//
//
//		//Header
//		out.add (new CommentRecord ("Model : " +  stand.getStep ().getProject ().getModel ().getPackageName ()
//		+ ",Project : " + stand.getStep ().getProject ().getName ()
//		+ ",Step : " + stand.getStep ().getName ()));
//		out.add (new EmptyRecord ());
//		out.add (new FreeRecord ("Id,Age,DBH,HT,NumberOfTrees,LogNumber,LogLength,LargeEndD,LargeEndDInsideBark,LargeEndDJuvenileWood,"+
//		"LargeEndH,SmallEndD,SmallEndDInsideBark,SmallEndDJuvenileWood,SmallEndH,WoodVolume,JuvenileWoodVolume,BarkVolume,TotalVolume,WoodMass,WoodCarbonMass  "));
//
////		//variables
////		int numberOfLog = 0;
////		double number;
////		double stumpDiameter ;
////		double largeEndD, largeEndDInsideBark ,largeEndH, largeEndDJuvenileWood=0;
////		double smallEndH ,smallEndD ,smallEndHForTopDiameter = 0, smallEndDInsideBark , smallEndDJuvenileWood=0;
////		double logLength = 0,totalLogsLength;
////		double woodVolume,totalVolume,barkVolume, juvenileWoodVolume = 0;
////		double woodMass, woodCarbonMass;
////		StringBuffer b;
////		String pieceOrigin = "";
////		String pieceProduct = "";
////		boolean boolAllJuvenileWoodAtLargeEndH , boolAllJuvenileWoodAtSmallEndH;
//
//		// Find the age of the initial step
///*		Step step0 = stand.getStep ();
//		Step step1 = step0;
//		while (step0 != null) {
//			step1 = step0;
//			step0 = (Step) step0.getFather ();
//		}
//		Pp3Stand stand1 = (Pp3Stand) step1.getStand();
//		if (stand1.getAge()> Pp3ModelParameters.JUVENILE_WOOD_AGE)  return;
//*/
//		int ageMin = stand.getRootDate ();
//		if (ageMin > Pp3ModelParameters.JUVENILE_WOOD_AGE)  return;
//
//		// Retrieve Steps from root to this step
//		Step lastStep = stand.getStep();
////		Vector steps = stand.getStep ().getProject ().getStepsFromRoot (stand.getStep ());
///*
//		for (Iterator w = steps.iterator (); w.hasNext ();) {
//			Step step = (Step) w.next ();
//			stand = (GTCStand) step.getStand ();
//			boolean interventionStep = step.getStand ().isInterventionResult ();
////			Log.println ("step =" + step+" bool =" + interventionStep);
//			if ((interventionStep) || (step==lastStep)) {
//				Object[] trees = stand.getTrees ().toArray ();
//
//				Pp3TreeDiameterComparator comp = new Pp3TreeDiameterComparator(true);
//				Arrays.sort(trees, comp);
//
//				for (int i = 0; i < trees.length; i++) {
//					Pp3Tree t = (Pp3Tree) trees[i];
//*/
//				boolean interventionStep = false;
////				for (LoggableTree loggableTree : getLoggableTrees()) {
////					Pp3LoggingTreeLogCategory logCategory = getTreeLoggerParameters().getSpeciesLogCategories(Pp3LoggingTreeLoggerParameters.MARITIME_PINE).get(0);	// default product
////
////					Pp3Tree t = (Pp3Tree) loggableTree;
//////					Log.println ("tree =" + t.getId()+ "treeheight = "+ t.getHeight());
////
////					if (((interventionStep) & (t.getNumberOfCutOrDead() > 0))||(step==lastStep)) {
////
////						if (interventionStep) number = t.getNumberOfCutOrDead()/5d;	//OVERSAMPLING
////						else number = t.getNumber()/5d; //OVERSAMPLING
////
////
////						if (number==0) {}
////						else if ((t.getHeight() < 4) ||
////								(Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),stumpHeight) * Math.PI < topGirth)) {
//////							Log.println ("height =" +t.getHeight());
////							logLength = t.getHeight() - stumpHeight;
////							totalVolume = Pp3Volume.calcWithTruncatedConeTreeVolume(t.getDbh (), t.getHeight ());
////
////							if (t.getHeight() < 4) {
////								woodMass = Pp3TreeBiomass.calcStemBiomass(t.getDbh (), t.getHeight (), 0d, (Pp3Stand) stand);
////							}
////							else {
////								smallEndH = t.getHeight ();
////								smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), smallEndH);
////								woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh (), t.getHeight (), stumpHeight, smallEndDInsideBark,stumpHeight);
////								woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
////							}
////							woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
////
//////							Log.println ("height =" +t.getHeight());
////							b = new StringBuffer ();
////							b.append (t.getId ());
////							b.append (",");
////							b.append (t.getAge ());
////							b.append (",");
////							b.append (nf2.format (t.getDbh ()));
////							b.append (",");
////							b.append (nf2.format (t.getHeight ()));
////							b.append (",");
////							b.append (nf2.format (number));
////							b.append (",-1,");
////							b.append (nf2.format (logLength));
////							b.append (",,,,,,,,,,,,");
////							b.append (nf6.format (totalVolume));
////							b.append (",");
////							b.append (nf6.format (woodMass));
////							b.append (",");
////							b.append (nf6.format (woodCarbonMass));
//////							b.append (",");
////							out.add (new FreeRecord (b.toString ()));
////
////						// create one piece
////							numberOfLog = 1;
////							int rankInTree = numberOfLog;
////							double numberOfPieces = number;
////							byte numberOfRadius = 1;
////							String treeStatus = "";
////							pieceOrigin = "-1";
////							pieceProduct = "";
////							double disc1Y_m = 1.3;
////							double disc1RadiusOverBark_mm = t.getDbh () / 2 * 10;
////							double disc1RadiusUnderBark_mm = 0;	// TO BE DEFINED
////							double disc2Y_m = 0;
////							double disc2RadiusOverBark_mm = 0;
////							double disc2RadiusUnderBark_mm = 0;
////							double disc1JuvenileWoodRadius_mm = 0;
////							double disc2JuvenileWoodRadius_mm = 0;
////							double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
////							double pieceY_mm = stumpHeight * 1000;
//////							GPiece piece = createPiece (logId++, rankInTree, numberOfPieces,
////							GPiece piece = createPiece (loggableTree,
////									pieceIdDispenser.next(), 
////									rankInTree, numberOfPieces,
////									numberOfRadius, 
////									treeStatus, 
////									pieceOrigin, 
////									logCategory,
////									disc1Y_m, 
////									disc1RadiusOverBark_mm, 
////									disc1RadiusUnderBark_mm,
////									disc2Y_m, 
////									disc2RadiusOverBark_mm, 
////									disc2RadiusUnderBark_mm,
////									disc1JuvenileWoodRadius_mm, 
////									disc2JuvenileWoodRadius_mm,
////									pieceLength_mm, 
////									pieceY_mm);
//////							piece.setLogCategory(logCategory);
//////							piece.properties.put ("totalVolume_m3", totalVolume);
//////							piece.properties.put ("woodMass_kg", woodMass);
//////							piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
////							piece.setProperty(Property.totalVolume_m3, totalVolume);
////							piece.setProperty(Property.woodMass_kg, woodMass);
////							piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
////
//////							pieces.add (piece);
////							addWoodPiece((LoggableTree) t, piece);
////						} else  {
////					//	TABLE Height & Dbh																							!
////						// from step to root (backwards)
////						int k = -1;
////						ArrayList height = new ArrayList();
////						ArrayList dbh = new ArrayList();
////						int treeId = t.getId();
////						Step stepN = step;
////						Pp3Tree tAtStepN = (Pp3Tree) ((Pp3Stand) stepN.getScene()).getTree(treeId);
////						while (stepN != null) {
////							if (!stepN.getScene ().isInterventionResult ()) {
////								k++;
////								tAtStepN = (Pp3Tree) ((Pp3Stand) stepN.getScene()).getTree(treeId);
////								height.add(new Double(tAtStepN.getHeight()));
////								dbh.add(new Double(tAtStepN.getDbh()));
////							}
////							stepN = (Step) stepN.getFather ();
////						}
////
//////						int ageMin = stand1.getAge();
////						double heightMin = tAtStepN.getHeight();
////						double heightIncr = heightMin;
////						for (int a = ageMin-1 ; a >= 0; a--) {
////							heightIncr = heightIncr - (heightMin / ageMin) ;
////							height.add(new Double(heightIncr));
////							dbh.add(new Double(0d));
////						}
////						Double[] heightTable = new Double[height.size()];
////						height.toArray(heightTable);
////						Double[] dbhTable = new Double[dbh.size()];
////						dbh.toArray(dbhTable);
//////						Log.println ("sizeHeight ="+ height.size()+ "sizedbh =" +dbh.size());
//////						for (int j = 0; j < height.size(); j++) Log.println ("j ="+j+", height ="+heightTable[j].doubleValue());
//////						for (int j = 0; j < dbh.size(); j++) Log.println ("j ="+j+", dbh ="+dbhTable[j].doubleValue());
////
////							stumpDiameter = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),stumpHeight);
////							numberOfLog = 0;
////							totalLogsLength = stumpHeight;
////
////							for (int c = 1; c < 4; c++) {
////								switch (c) {
////									case 1 :
////									{
////										pieceOrigin = "1";
////										logLength=log1Length;
////										smallEndHForTopDiameter = Pp3TreeTaper.calcHeightAtTargetDiameter(t.getDbh(), t.getHeight(),top1Girth/Math.PI,stumpHeight);
////										break;
////									}
////									case 2 :
////									{
////										pieceOrigin = "2";
////										logLength=log2Length;
////										smallEndHForTopDiameter = Pp3TreeTaper.calcHeightAtTargetDiameter(t.getDbh(), t.getHeight(),top2Girth/Math.PI,stumpHeight);
////										break;
////									}
////									case 3 :
////									{
////										pieceOrigin = "3";
////										logLength=log3Length;
////										smallEndHForTopDiameter = Pp3TreeTaper.calcHeightAtTargetDiameter(t.getDbh(), t.getHeight(),top3Girth/Math.PI,stumpHeight);
////										break;
////									}
////								}
////
//////								Log.println ("logLength =" +logLength+ "smallEndHForTopDiameter = " + smallEndHForTopDiameter);
////								while ((smallEndHForTopDiameter - totalLogsLength) > logLength){
//////										Log.println ("totalLogsLength =" +totalLogsLength);
////										numberOfLog += 1;//Log Number (between last log and the target cutting diameter)
////										largeEndH = totalLogsLength ;
////										largeEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
////										largeEndDInsideBark = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
////
////										smallEndH = largeEndH + logLength;
////										smallEndD =  Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),smallEndH);
////										smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),smallEndH);
////
////										// Juvenile Wood dimensions
////										// j = 0 for total age , j = Pp3ModelParameters.JUVENILE_WOOD_AGE part of the trunk where there are only rings with juvenile wood
////										if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
////											largeEndDJuvenileWood = largeEndDInsideBark;
////											boolAllJuvenileWoodAtLargeEndH = true;
////										}else if (largeEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
////											largeEndDJuvenileWood = largeEndDInsideBark;
////											boolAllJuvenileWoodAtLargeEndH = true;
////										} else {
////											int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
////											while (largeEndH < heightTable[j].doubleValue()) {j++ ;}
////											j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
////											double heightJ = heightTable[j].doubleValue();
////											double dbhJ = dbhTable[j].doubleValue();
////											largeEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , largeEndH );
////											boolAllJuvenileWoodAtLargeEndH = false;
////										}
////										if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
////											smallEndDJuvenileWood = smallEndDInsideBark;
////											boolAllJuvenileWoodAtSmallEndH = true;
////										} else if (smallEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
////											smallEndDJuvenileWood = smallEndDInsideBark;
////											boolAllJuvenileWoodAtSmallEndH = true;
////										} else {
////											int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
////											while (smallEndH < heightTable[j].doubleValue()) {j++ ;}
////											j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
////											double heightJ = heightTable[j].doubleValue();
////											double dbhJ = dbhTable[j].doubleValue();
////											smallEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , smallEndH );
////											boolAllJuvenileWoodAtSmallEndH = false;
////										}
////
////										woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////										woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
////										woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
////										totalVolume = Pp3Volume.calcOneLogVolumeHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////										barkVolume = totalVolume - woodVolume ;
////										// Juvenile Wood Volume
////										if ( boolAllJuvenileWoodAtLargeEndH == boolAllJuvenileWoodAtSmallEndH ) {
////											if (boolAllJuvenileWoodAtLargeEndH) {
////												juvenileWoodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////											} else {
////												juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,smallEndDJuvenileWood, logLength);
////											}
////										} else {
////											double heightJuvenileWoodAge = heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue();
////											double logLength1 = smallEndH - heightJuvenileWoodAge ;
////											double logLength2 =  heightJuvenileWoodAge - largeEndH  ;
////											double maxDiameterAllJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), heightJuvenileWoodAge);
////											juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,maxDiameterAllJuvenileWood, logLength2) +
////																Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),heightJuvenileWoodAge,smallEndD,stumpHeight);
////																// Pp3Volume.calcTruncatedConeVolume(maxDiameterAllJuvenileWood,smallEndDJuvenileWood, logLength1);
////										}
////
////										totalLogsLength += logLength;
////
////										b = new StringBuffer ();
////										b.append (t.getId ());
////										b.append (",");
////										b.append (t.getAge ());
////										b.append (",");
////										b.append (nf2.format (t.getDbh ()));
////										b.append (",");
////										b.append (nf2.format (t.getHeight ()));
////										b.append (",");
////										b.append (nf2.format (number));
////										b.append (",");
////										b.append (numberOfLog) ;
////										b.append (",");
////										b.append (nf2.format (logLength));
////										b.append (",");
////										b.append (nf2.format (largeEndD));
////										b.append (",");
////										b.append (nf2.format (largeEndDInsideBark));
////										b.append (",");
////										b.append (nf2.format (largeEndDJuvenileWood));
////										b.append (",");
////										b.append (nf2.format (largeEndH));
////										b.append (",");
////										b.append (nf2.format (smallEndD));
////										b.append (",");
////										b.append (nf2.format (smallEndDInsideBark));
////										b.append (",");
////										b.append (nf2.format (smallEndDJuvenileWood));
////										b.append (",");
////										b.append (nf2.format (smallEndH));
////										b.append (",");
////										b.append (nf6.format (woodVolume));
////										b.append (",");
////										b.append (nf6.format (juvenileWoodVolume));
////										b.append (",");
////										b.append (nf6.format (barkVolume));
////										b.append (",");
////										b.append (nf6.format (totalVolume));
////										b.append (",");
////										b.append (nf6.format (woodMass));
////										b.append (",");
////										b.append (nf6.format (woodCarbonMass));
//////										b.append (",");
////										out.add (new FreeRecord (b.toString ()));
////// create one piece
////							int rankInTree = numberOfLog;
////							double numberOfPieces = number;
////							byte numberOfRadius = 1;
////							String treeStatus = "";
////							//pieceProduct = "";
////							double disc1Y_m = largeEndH;
////							double disc1RadiusOverBark_mm = largeEndD / 2 * 10;
////							double disc1RadiusUnderBark_mm = largeEndDInsideBark / 2 * 10;
////							double disc2Y_m = smallEndH;
////							double disc2RadiusOverBark_mm = smallEndD / 2 * 10;
////							double disc2RadiusUnderBark_mm = smallEndDInsideBark / 2 * 10;
////							double disc1JuvenileWoodRadius_mm = largeEndDJuvenileWood / 2 * 10;
////							double disc2JuvenileWoodRadius_mm = smallEndDJuvenileWood / 2 * 10;
////							double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
////							double pieceY_mm = disc1Y_m * 1000;
//////							GPiece piece = createPiece (logId++, rankInTree, numberOfPieces,
////							GPiece piece = createPiece (loggableTree,
////									pieceIdDispenser.next(),
////									rankInTree,
////									numberOfPieces,
////									numberOfRadius,
////									treeStatus, 
////									pieceOrigin, 
////									logCategory,
////									disc1Y_m, 
////									disc1RadiusOverBark_mm, 
////									disc1RadiusUnderBark_mm,
////									disc2Y_m, 
////									disc2RadiusOverBark_mm, 
////									disc2RadiusUnderBark_mm,
////									disc1JuvenileWoodRadius_mm, 
////									disc2JuvenileWoodRadius_mm,
////									pieceLength_mm, 
////									pieceY_mm);
//////							piece.setLogCategory(logCategory);
//////							piece.properties.put ("totalVolume_m3", totalVolume);
//////							piece.properties.put ("woodVolume_m3", woodVolume);
//////							piece.properties.put ("barkVolume_m3", barkVolume);
//////							piece.properties.put ("juvenileWoodVolume_m3", juvenileWoodVolume);
//////							piece.properties.put ("woodMass_kg", woodMass);
//////							piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
////							piece.setProperty(Property.totalVolume_m3, totalVolume);
////							piece.setProperty(Property.woodVolume_m3, woodVolume);
////							piece.setProperty(Property.barkVolume_m3, barkVolume);
////							piece.setProperty(Property.juvenileWoodVolume_m3, juvenileWoodVolume);
////							piece.setProperty(Property.woodMass_kg, woodMass);
////							piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
////							
//////							pieces.add (piece);
////							addWoodPiece((LoggableTree) t, piece);
////
////								}
////							}
////
//////							Log.println ("totalLogsLength =" +totalLogsLength + "smallEndHForTopDiameter =" +smallEndHForTopDiameter);
////							if ((smallEndHForTopDiameter - totalLogsLength) > 0) {
//////							Log.println ("totalLogsLength =" +totalLogsLength + "smallEndHForTopDiameter =" +smallEndHForTopDiameter);
//////								numberOfLog = -1;
////								numberOfLog += 1;
////								largeEndH = totalLogsLength ;
////								largeEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
////								largeEndDInsideBark = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
////								smallEndH = smallEndHForTopDiameter ;
////								if (topGirth == 0) smallEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),smallEndH);
////								else smallEndD = topGirth / Math.PI;
////								smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),smallEndH);
////
////								logLength = smallEndH - largeEndH;
////
////								// Juvenile Wood dimensions
////								// j = 0 for total age , j = Pp3ModelParameters.JUVENILE_WOOD_AGE part of the trunk where there are only rings with juvenile wood
////								if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
////									largeEndDJuvenileWood = largeEndDInsideBark;
////									boolAllJuvenileWoodAtLargeEndH = true;
////								} else if (largeEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
////									largeEndDJuvenileWood = largeEndDInsideBark;
////									boolAllJuvenileWoodAtLargeEndH = true;
////								} else {
////									int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
////									while (largeEndH < heightTable[j].doubleValue()) {j++ ;}
////									j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
////									double heightJ = heightTable[j].doubleValue();
////									double dbhJ = dbhTable[j].doubleValue();
////									largeEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , largeEndH );
////									boolAllJuvenileWoodAtLargeEndH = false;
////								}
////								if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
////									smallEndDJuvenileWood = smallEndDInsideBark;
////									boolAllJuvenileWoodAtSmallEndH = true;
////								} else if (smallEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
////									smallEndDJuvenileWood = smallEndDInsideBark;
////									boolAllJuvenileWoodAtSmallEndH = true;
////								} else {
////									int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
////									while (smallEndH < heightTable[j].doubleValue()) {j++ ;}
////									j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
////									double heightJ = heightTable[j].doubleValue();
////									double dbhJ = dbhTable[j].doubleValue();
////									smallEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , smallEndH );
////									boolAllJuvenileWoodAtSmallEndH = false;
////								}
////
////								if (logLength != 0) {
////									woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////									woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
////									woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
////									totalVolume = Pp3Volume.calcOneLogVolumeHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////									barkVolume = totalVolume - woodVolume ;
////									// Juvenile Wood Volume
////									if ( boolAllJuvenileWoodAtLargeEndH == boolAllJuvenileWoodAtSmallEndH ) {
////										if (boolAllJuvenileWoodAtLargeEndH) {
////											juvenileWoodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////										} else {
////											juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,smallEndDJuvenileWood, logLength);
////										}
////									} else {
////										double heightJuvenileWoodAge = heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue();
////										double logLength1 = smallEndH - heightJuvenileWoodAge ;
////										double logLength2 =  heightJuvenileWoodAge - largeEndH  ;
////										double maxDiameterAllJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), heightJuvenileWoodAge);
////										juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,maxDiameterAllJuvenileWood, logLength2) +
////															Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),heightJuvenileWoodAge,smallEndD,stumpHeight);
////															// Pp3Volume.calcTruncatedConeVolume(maxDiameterAllJuvenileWood,smallEndDJuvenileWood, logLength1);
////									}
////
////									b = new StringBuffer ();
////									b.append (t.getId ());
////									b.append (",");
////									b.append (t.getAge ());
////									b.append (",");
////									b.append (nf2.format (t.getDbh ()));
////									b.append (",");
////									b.append (nf2.format (t.getHeight ()));
////									b.append (",");
////									b.append (nf2.format (number));
////									b.append (",");
//////									b.append (numberOfLog) ;
////									b.append ("-1") ;
////									b.append (",");
////									b.append (nf2.format (logLength));
////									b.append (",");
////									b.append (nf2.format (largeEndD));
////									b.append (",");
////									b.append (nf2.format (largeEndDInsideBark));
////									b.append (",");
////									b.append (nf2.format (largeEndDJuvenileWood));
////									b.append (",");
////									b.append (nf2.format (largeEndH));
////									b.append (",");
////									b.append (nf2.format (smallEndD));
////									b.append (",");
////									b.append (nf2.format (smallEndDInsideBark));
////									b.append (",");
////									b.append (nf2.format (smallEndDJuvenileWood));
////									b.append (",");
////									b.append (nf2.format (smallEndH));
////									b.append (",");
////									b.append (nf6.format (woodVolume));
////									b.append (",");
////									b.append (nf6.format (juvenileWoodVolume));
////									b.append (",");
////									b.append (nf6.format (barkVolume));
////									b.append (",");
////									b.append (nf6.format (totalVolume));
////									b.append (",");
////									b.append (nf6.format (woodMass));
////									b.append (",");
////									b.append (nf6.format (woodCarbonMass));
//////									b.append (",");
////									out.add (new FreeRecord (b.toString ()));
////// create one piece
////							int rankInTree = numberOfLog;
////							double numberOfPieces = number;
////							byte numberOfRadius = 1;
////							String treeStatus = "";
////							pieceOrigin = "-1";
////							pieceProduct = "";
////							double disc1Y_m = largeEndH;
////							double disc1RadiusOverBark_mm = largeEndD / 2 * 10;
////							double disc1RadiusUnderBark_mm = largeEndDInsideBark / 2 * 10;
////							double disc2Y_m = smallEndH;
////							double disc2RadiusOverBark_mm = smallEndD / 2 * 10;
////							double disc2RadiusUnderBark_mm = smallEndDInsideBark / 2 * 10;
////							double disc1JuvenileWoodRadius_mm = largeEndDJuvenileWood / 2 * 10;
////							double disc2JuvenileWoodRadius_mm = smallEndDJuvenileWood / 2 * 10;
////							double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
////							double pieceY_mm = disc1Y_m * 1000;
////							GPiece piece = createPiece (loggableTree,
////									pieceIdDispenser.next(), 
////									rankInTree, 
////									numberOfPieces,
////									numberOfRadius, 
////									treeStatus, 
////									pieceOrigin, 
////									logCategory,
////									disc1Y_m, 
////									disc1RadiusOverBark_mm, 
////									disc1RadiusUnderBark_mm,
////									disc2Y_m, 
////									disc2RadiusOverBark_mm, 
////									disc2RadiusUnderBark_mm,
////									disc1JuvenileWoodRadius_mm, 
////									disc2JuvenileWoodRadius_mm,
////									pieceLength_mm, 
////									pieceY_mm);
//////							piece.setLogCategory(logCategory);
//////							piece.properties.put ("totalVolume_m3", totalVolume);
//////							piece.properties.put ("woodVolume_m3", woodVolume);
//////							piece.properties.put ("barkVolume_m3", barkVolume);
//////							piece.properties.put ("juvenileWoodVolume_m3", juvenileWoodVolume);
//////							piece.properties.put ("woodMass_kg", woodMass);
//////							piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
////							piece.setProperty(Property.totalVolume_m3, totalVolume);
////							piece.setProperty(Property.woodVolume_m3, woodVolume);
////							piece.setProperty(Property.barkVolume_m3, barkVolume);
////							piece.setProperty(Property.juvenileWoodVolume_m3, juvenileWoodVolume);
////							piece.setProperty(Property.woodMass_kg, woodMass);
////							piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
//////							pieces.add (piece);
////							addWoodPiece((LoggableTree) t, piece); 
////								}
////							}
////// /*
//////							Log.println ("cbh =" +t.getDbh()* Math.PI+ "topGirth = " + topGirth);
////							largeEndD = topGirth /Math.PI;
////							largeEndH = Pp3TreeTaper.calcHeightAtTargetDiameter (t.getDbh(), t.getHeight(),largeEndD, stumpHeight);
////							largeEndDInsideBark = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), largeEndH);
////							smallEndH = t.getHeight ();
////							smallEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(), smallEndH);
////							smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), smallEndH);
////							logLength = smallEndH - largeEndH;
////
////							// Juvenile Wood dimensions
////							// j = 0 for total age , j = Pp3ModelParameters.JUVENILE_WOOD_AGE part of the trunk where there are only rings with juvenile wood
////							if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
////								largeEndDJuvenileWood = largeEndDInsideBark;
////								boolAllJuvenileWoodAtLargeEndH = true;
////							} else if (largeEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
////								largeEndDJuvenileWood = largeEndDInsideBark;
////								boolAllJuvenileWoodAtLargeEndH = true;
////							} else {
////								int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
////								while (largeEndH < heightTable[j].doubleValue()) {j++ ;}
////								j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
////								double heightJ = heightTable[j].doubleValue();
////								double dbhJ = dbhTable[j].doubleValue();
////								largeEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , largeEndH );
////								boolAllJuvenileWoodAtLargeEndH = false;
////							}
////							smallEndDJuvenileWood = smallEndDInsideBark ;
////							boolAllJuvenileWoodAtSmallEndH = true;
////
////							if (logLength != 0) {
////								woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////								woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
////								woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
////								totalVolume = Pp3Volume.calcOneLogVolumeHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////								barkVolume = totalVolume - woodVolume ;
////								// Juvenile Wood Volume
////								if ( boolAllJuvenileWoodAtLargeEndH == boolAllJuvenileWoodAtSmallEndH ) {
////									if (boolAllJuvenileWoodAtLargeEndH) {
////										juvenileWoodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
////									} else {
////										juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,smallEndDJuvenileWood, logLength);
////									}
////								} else {
////									double heightJuvenileWoodAge = heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue();
////									double logLength1 = smallEndH - heightJuvenileWoodAge ;
////									double logLength2 =  heightJuvenileWoodAge - largeEndH  ;
////									double maxDiameterAllJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), heightJuvenileWoodAge);
////									juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,maxDiameterAllJuvenileWood, logLength2) +
////														Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),heightJuvenileWoodAge,smallEndD,stumpHeight);
////														// Pp3Volume.calcTruncatedConeVolume(maxDiameterAllJuvenileWood,smallEndDJuvenileWood, logLength1);
////								}
////								b = new StringBuffer ();
////								b.append (t.getId ());
////								b.append (",");
////								b.append (t.getAge ());
////								b.append (",");
////								b.append (nf2.format (t.getDbh ()));
////								b.append (",");
////								b.append (nf2.format (t.getHeight ()));
////								b.append (",");
////								b.append (nf2.format (number));
////								b.append (",0,") ; //Log Number (upper the target cutting diameter)
////								b.append (nf2.format (logLength));
////								b.append (",");
////								b.append (nf2.format (largeEndD));
////								b.append (",");
////								b.append (nf2.format (largeEndDInsideBark));
////								b.append (",");
////								b.append (nf2.format (largeEndDJuvenileWood));
////								b.append (",");
////								b.append (nf2.format (largeEndH));
////								b.append (",");
////								b.append (nf2.format (smallEndD));
////								b.append (",");
////								b.append (nf2.format (smallEndDInsideBark));
////								b.append (",");
////								b.append (nf2.format (smallEndDJuvenileWood));
////								b.append (",");
////								b.append (nf2.format (smallEndH));
////								b.append (",");
////								b.append (nf6.format (woodVolume));
////								b.append (",");
////								b.append (nf6.format (juvenileWoodVolume));
////								b.append (",");
////								b.append (nf6.format (barkVolume));
////								b.append (",");
////								b.append (nf6.format (totalVolume));
////								b.append (",");
////								b.append (nf6.format (woodMass));
////								b.append (",");
////								b.append (nf6.format (woodCarbonMass));
//////								b.append (",");
////								out.add (new FreeRecord (b.toString ()));
////
////// create one piece
////							numberOfLog += 1;
////							int rankInTree = numberOfLog;
////							double numberOfPieces = number;
////							byte numberOfRadius = 1;
////							String treeStatus = "";
////							pieceOrigin = "0";
////							pieceProduct = "";
////							double disc1Y_m = largeEndH;
////							double disc1RadiusOverBark_mm = largeEndD / 2 * 10;
////							double disc1RadiusUnderBark_mm = largeEndDInsideBark / 2 * 10;
////							double disc2Y_m = smallEndH;
////							double disc2RadiusOverBark_mm = smallEndD / 2 * 10;
////							double disc2RadiusUnderBark_mm = smallEndDInsideBark / 2 * 10;
////							double disc1JuvenileWoodRadius_mm = largeEndDJuvenileWood / 2 * 10;
////							double disc2JuvenileWoodRadius_mm = smallEndDJuvenileWood / 2 * 10;
////							double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
////							double pieceY_mm = disc1Y_m * 1000;
//////							GPiece piece = createPiece (logId++, rankInTree, numberOfPieces,
////							GPiece piece = createPiece (loggableTree,
////									pieceIdDispenser.next(), 
////									rankInTree, 
////									numberOfPieces,
////									numberOfRadius, 
////									treeStatus, 
////									pieceOrigin, 
////									logCategory,
////									disc1Y_m, 
////									disc1RadiusOverBark_mm, 
////									disc1RadiusUnderBark_mm,
////									disc2Y_m, 
////									disc2RadiusOverBark_mm, 
////									disc2RadiusUnderBark_mm,
////									disc1JuvenileWoodRadius_mm, 
////									disc2JuvenileWoodRadius_mm,
////									pieceLength_mm, 
////									pieceY_mm);
//////							piece.setLogCategory(logCategory);
//////							piece.properties.put ("totalVolume_m3", totalVolume);
//////							piece.properties.put ("woodVolume_m3", woodVolume);
//////							piece.properties.put ("barkVolume_m3", barkVolume);
//////							piece.properties.put ("juvenileWoodVolume_m3", juvenileWoodVolume);
//////							piece.properties.put ("woodMass_kg", woodMass);
//////							piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
////							piece.setProperty(Property.totalVolume_m3, totalVolume);
////							piece.setProperty(Property.woodVolume_m3, woodVolume);
////							piece.setProperty(Property.barkVolume_m3, barkVolume);
////							piece.setProperty(Property.juvenileWoodVolume_m3, juvenileWoodVolume);
////							piece.setProperty(Property.woodMass_kg, woodMass);
////							piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
//////							pieces.add (piece);
////							addWoodPiece((LoggableTree) t, piece);
////							}
////// */
////						}
////					}
////					ProgressDispatcher.setValue(++treeCounter);
////					setProgress((int) (++treeCounter * progressFactor));
////				}
///*			}
//		}
//*/
//

//				try {
//					if (getTreeLoggerParameters().exportAsked) {
//						String fileName = PathManager.getDir("tmp")
//						+File.separator
//						+getTreeLoggerParameters().exportFileName;
//						writeFile (out, fileName);
//					}
//				} catch (Exception e) {
//					errorReport = "Error in Pp3Logging during export: "+e;
//					throw e;
//				}
//		} catch (Exception e) {
//			Log.println(Log.ERROR, "Pp3Logging.run", "Erro while logging", e);
//			throw e;
//		} 
//		setResult (pieces);		no longer necessary






		/*double numberOfPieces = 1;	// Fagacees is an individual based model
		byte numberOfRadius = 1;	// Fagacees generates circular trees
		// int nbDiscs = 2;			// Number of discs / piece (minimum 2)

		pieces = new ArrayList <GPiece> ();
		int pieceId = 1;

		int nbProducts = starter.getNumberOfSelectedProducts();
		if (nbProducts == 0) {
			return;
		}

		try {

			FgTree firstTree = (FgTree) trees.iterator ().next ();
			Step refStep = firstTree.getStand ().getStep ();
			Scenario scenario = refStep.getScenario ();
			Collection stepsFromRoot = scenario.getStepsFromRoot (refStep);

			TreeRadius_cmProvider mp =
					(TreeRadius_cmProvider) scenario.getModel ().getMethodProvider ();

			Pp3LoggingExport export = new Pp3LoggingExport (getId (), starter);

		System.out.println ("Pp3Logging...");

			for (Iterator i = trees.iterator (); i.hasNext ();) {
				FgTree t = (FgTree) i.next ();

				// GTree [] history = new TreeHistory (t, stepsFromRoot).getHistory ();
				boolean fullHistory = false;
				boolean startFirstYear = false;
				GTree [] history = new FgTreeHistory (t, stepsFromRoot,
						fullHistory, startFirstYear).getHistory ();

				KnottyCoreProfile knotProfile = new KnottyCoreProfile (history, mp);
				HeartWoodProfile heartProfile = new HeartWoodProfile (history, mp);

				GPieceTreeInfo treeInfo = new GPieceTreeInfo (t.getId (), t.getAge (), treeStatus);

				LoggingContext lc = new LoggingContext (0, t.getHeight (),
						t.getCrownBaseHeight (), nbProducts) ;

				for (int p=0; p<nbProducts; p++) {
					FgProduct prod = starter.getSelectedProduct (p);
					lc.addProduct (prod.getId ());
					while (prod.testLogValid (t, history, mp, knotProfile, heartProfile, lc) ) {
						//System.out.println("Coupe " + prod.getId() + "=" + prod.getName() +" lg="+lc.getLength() ) ;
						lc.cutLog (prod.getId () );
						GPiece piece = new GPiece(pieceId, lc.getLogCount (),
								numberOfPieces, numberOfRadius, treeInfo, prod.getName ());
						addDiscs (piece, lc, starter.discInterval_m, history,
								mp, knotProfile, heartProfile);

						if (starter.recordResults) {
							pieces.add (piece);
						}
						export.savePieceResults (piece, p);
						pieceId++;
					}
				}

				String textInfo = "" + t.getId ();
				for (int p=0; p<nbProducts; p++) {
					FgProduct prod = starter.getSelectedProduct (p);
					textInfo += " " + (p==0?": ":"")  + lc.getLogCount (prod.getId ())
							+ " " + Pp3LoggingExport.left (prod.getName (), 3);
				}
				textInfo += " > " + lc.getLogCount ()  + " p.";
				System.out.println (textInfo);

				// debugRings (history, 1.3,  mp);
				RadialProfile profile130 = new RadialProfile (history, 1.30, mp);
				export.saveTreeResults (t, treeStatus, lc, profile130, knotProfile, heartProfile);

			}

			export.close ();
			setResult (pieces);

		} catch (Exception e) {
			Log.println (Log.ERROR, "Pp3Logging.run()", "Error during logging", e);
			setResult (Translator.swap ("Pp3Logging.MsgError") + " : " + e);
		}

		// TODO : tempo : to not save useless Mb of data :
		trees = null;
		starter = null;*/

		// end of the job: call execute ()
//		execute ();
//	}

	@Override	
	protected void posteriorToRunning() {
		try {
			if (getTreeLoggerParameters().exportAsked) {
				String fileName = PathManager.getDir("tmp")
				+File.separator
				+getTreeLoggerParameters().exportFileName;
				writeFile (out, fileName);
			}
		} catch (Exception e) {
			errorReport = "Error in Pp3Logging during export: "+e;
		}
	}
	
		
		
	//	Add nbDiscs (>2) discs and pith points to piece (described by lc)
	//	with disc id=1 at bottom and disc id=nbDiscs at top
	/*private static void addDiscs (GPiece piece, LoggingContext lc, double discInterval_m,
			GTree[] history, TreeRadius_cmProvider mp,
			KnottyCoreProfile knotProfile, HeartWoodProfile heartProfile) {

		double centreX_mm = 0;		// Fagac�es simulates straight pith
		double centreZ_mm = 0;
		boolean firstRingIsBark = true ;
		// Last ring is pith but will be set to 0
		// (because Fagac�es does not gives pith width) :
		boolean lastRingIsPith = true ;

		double h0_mm = lc.getCutLogBottomHeight () * 1000;
		double h1_mm = lc.getHeight () * 1000;
		double pieceLength_mm = h1_mm - h0_mm ;

		int nbDiscs = (discInterval_m < .001)
				? 2 : (int) Math.ceil (pieceLength_mm/1000.0 / discInterval_m) + 1;
		if (nbDiscs<2) {nbDiscs = 2;}

		int discId = 1 ;
		for (int d = 0; d < nbDiscs; d++) {
			double discHeight_mm =  h0_mm + d * pieceLength_mm / (nbDiscs-1);

			// Pith:
			Vertex3d pith = new Vertex3d(centreX_mm, discHeight_mm, centreZ_mm);
			piece.addPithPoint(pith);

			// Disc:
			GPieceDisc disc = new GPieceDisc (discId++, piece.getId(), discHeight_mm,
					firstRingIsBark, lastRingIsPith);
			piece.addDisc (disc);
			addRings (disc, history, mp, knotProfile, heartProfile);
			// debugRings (history, discHeight_mm/1000,  mp);
		}

	}*/


	// disc 1 is mandatory, disc2 is not (values = 0)
	private GPiece createPiece (LoggableTree tree,
			int id, 
			int rankInTree, 
			double numberOfPieces,
			byte numberOfRadius, 
			String treeStatus, 
			String pieceOrigin, 
			TreeLogCategory logCategory,
			double disc1Y_m, 
			double disc1RadiusOverBark_mm, 
			double disc1RadiusUnderBark_mm,
			double disc2Y_m, 
			double disc2RadiusOverBark_mm, 
			double disc2RadiusUnderBark_mm,
			double disc1JuvenileWoodRadius_mm, 
			double disc2JuvenileWoodRadius_mm,
			double pieceLength_mm, 
			double pieceY_mm) {

//		GPieceTreeInfo treeInfo = new GPieceTreeInfo	(((Tree) tree).getId (),
//				((Tree) tree).getAge (),
//				treeStatus,
//				((Tree) tree).getDbh ());
//		treeInfo.species = "maritimePine";

		boolean pieceWithBark = true;


		// Last ring is pith and set to 0 for centreX centreZ
		// Pp3 does not give pith width Radius_mm so set to 0
		// boolean pieceWithPith is set to true because the values are useful for processing
		boolean pieceWithPith = true ;

		GPiece piece = new GPiece (tree,
			id,
			rankInTree,
			numberOfPieces,
			pieceLength_mm,
			pieceY_mm,
			pieceWithBark,
			pieceWithPith,
			numberOfRadius,
//			treeInfo,
			treeStatus,
			logCategory);
		piece.setOrigin(pieceOrigin);
		// disc1
		int discId = 1;
		double centreY_mm = disc1Y_m * 1000;
//		boolean firstRingIsBark = true;
//		boolean lastRingIsPith = false;

		GPieceDisc largeDisc = new GPieceDisc (discId,
//					id,
					centreY_mm);
		piece.addDisc(largeDisc);

		// ring 1 : over bark
		int ringId = 1;
		double centreX_mm = 0;
		double centreZ_mm = 0;
		double radius_mm = disc1RadiusOverBark_mm;
		GPieceRing ring1 = new GPieceRing (
					ringId++,
					discId,
					centreX_mm,
					centreZ_mm,
					radius_mm
					);
		largeDisc.addRing (ring1);

		// ring 2 : under bark
		ringId = 2;
		centreX_mm = 0;
		centreZ_mm = 0;
		radius_mm = disc1RadiusUnderBark_mm;
		GPieceRing ring2 = new GPieceRing (
					ringId++,
					discId,
					centreX_mm,
					centreZ_mm,
					radius_mm
					);
		largeDisc.addRing (ring2);

		// juvenile wood
		if (disc1JuvenileWoodRadius_mm != 0) {
			centreX_mm = 0;
			centreZ_mm = 0;
//			largeDisc.juvenileWoodLimit = new GContour ( centreX_mm, centreZ_mm, disc1JuvenileWoodRadius_mm );
			GContour juvenileWood = new GContour ( centreX_mm, centreZ_mm, disc1JuvenileWoodRadius_mm );
			largeDisc.getContours().put ("juvenileWood", juvenileWood);
		}

		// ring 3 : pith
		ringId = 3;
		centreX_mm = 0;
		centreZ_mm = 0;
		radius_mm = 0;
		GPieceRing ring3 = new GPieceRing (
					ringId++,
					discId,
					centreX_mm,
					centreZ_mm,
					radius_mm
					);
		largeDisc.addRing (ring3);

		// disc2
		if (disc2Y_m != 0) {
			discId = 2;
			centreY_mm = disc2Y_m * 1000;
//			firstRingIsBark = true;
//			lastRingIsPith = false;

			GPieceDisc smallDisc = new GPieceDisc (discId,
//						id,
						centreY_mm);
			piece.addDisc (smallDisc);

		// ring 1 : over bark
		ringId = 1;
		centreX_mm = 0;
		centreZ_mm = 0;
		radius_mm = disc2RadiusOverBark_mm;
		ring1 = new GPieceRing (
					ringId++,
					discId,
					centreX_mm,
					centreZ_mm,
					radius_mm
					);
		smallDisc.addRing (ring1);

		// ring 2 : under bark
		ringId = 2;
		centreX_mm = 0;
		centreZ_mm = 0;
		radius_mm = disc2RadiusUnderBark_mm;
		ring2 = new GPieceRing (
					ringId++,
					discId,
					centreX_mm,
					centreZ_mm,
					radius_mm
					);
		smallDisc.addRing (ring2);

		// juvenile wood
		if (disc2JuvenileWoodRadius_mm != 0) {
			centreX_mm = 0;
			centreZ_mm = 0;
//			smallDisc.juvenileWoodLimit = new GContour ( centreX_mm, centreZ_mm, disc2JuvenileWoodRadius_mm );
			GContour juvenileWood = new GContour ( centreX_mm, centreZ_mm, disc2JuvenileWoodRadius_mm );
			smallDisc.getContours().put ("juvenileWood", juvenileWood);
		}

		// ring 3 : pith
		ringId = 3;
		centreX_mm = 0;
		centreZ_mm = 0;
		radius_mm = 0;
		ring3 = new GPieceRing (
					ringId++,
					discId,
					centreX_mm,
					centreZ_mm,
					radius_mm
					);
		smallDisc.addRing (ring3);

		}

		return piece;
	}


	private void writeFile (Collection out, String fileName) throws Exception {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter (new FileWriter (fileName));
		} catch (Exception e) {
			throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
		}

		for (Iterator i = out.iterator (); i.hasNext ();) {
			Object r = i.next ();
			w.write (r.toString ());
			w.newLine ();
		}
		try {w.close ();} catch (Exception e) {}	// no exception reported if trouble while closing

	}



	/** This is called when the job is over
	*	When using Launcher, it will cal this method
	*	When not using Launcher, call it explicitly
	*	Changes the job status to finished and report to caller
	*/
//	public int execute () {	// will be called when command is over
//		if (errorReport == null) {
//			setStatus ("Finished");
//			// setResult ("out file is ...");
//		} else {
//			setStatus ("Error");
//			setResult (errorReport);
//		}
//		finished ();	// implmented in Job, tell the "callBack" caller if one
//		return 0;
//	}

	@Override
	public void setTreeLoggerParameters() {
		try {
			params = createDefaultTreeLoggerParameters();
			params.showUI(null);
			if (params.isParameterDialogCanceled()) {
				return;
			}
//			// This dialog will ensure the starter is complete
//			Pp3LoggingTreeLoggerParametersDialog dlg = new Pp3LoggingTreeLoggerParametersDialog (getTreeLoggerParameters());
//			// dlg is valid if "ok" button and starter.isCorrect ()
//			if (!dlg.isValidDialog ()) {
//				getTreeLoggerParameters().setParametersInitializationAborted(true);
//				return;
//			}
		} catch (ClassCastException e) {
			Log.println(Log.ERROR, "Pp3Logging.setTreeLoggerParameters", 
					"The TreeLoggerParameters object is not compatible with this tree logger",
					e);
			throw new InvalidParameterException(e.getMessage());
		} catch (Exception e) {
			Log.println(Log.ERROR, "Pp3Logging.setTreeLoggerParameters",
					"Error while setting the parameters",
					e);
			throw new InvalidParameterException(e.getMessage());
		}
	}

	@Override
	public Pp3LoggingTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new Pp3LoggingTreeLoggerParameters();
	}
	
	@Override
	public Pp3LoggingTreeLoggerParameters getTreeLoggerParameters() {
		return (Pp3LoggingTreeLoggerParameters) this.params;
	}

	@Override
	protected void logThisTree (LoggableTree loggableTree) {

		TreeList stand = (TreeList) step.getScene ();

		
		int ageMin = stand.getRootDate ();
		if (ageMin > Pp3ModelParameters.JUVENILE_WOOD_AGE)  return;

		// Retrieve Steps from root to this step
		Step lastStep = stand.getStep();

		Pp3LoggingTreeLogCategory logCategory = getTreeLoggerParameters().getSpeciesLogCategories(Pp3LoggingTreeLoggerParameters.MARITIME_PINE).get(0);	// default product
		Pp3Tree t = (Pp3Tree) loggableTree;
		
		double stumpHeight = getTreeLoggerParameters().STUMP_HEIGHT;
		double topGirth = getTreeLoggerParameters().topGirth;
		double top1Girth = getTreeLoggerParameters().top1Girth;
		double log1Length = getTreeLoggerParameters().log1Length;
		double top2Girth = getTreeLoggerParameters().top2Girth;
		double log2Length = getTreeLoggerParameters().log2Length;
		double top3Girth = getTreeLoggerParameters().top3Girth;
		double log3Length = getTreeLoggerParameters().log3Length;

		//variables
		int numberOfLog = 0;
		double number;
		double stumpDiameter;
		double largeEndD, largeEndDInsideBark ,largeEndH, largeEndDJuvenileWood=0;
		double smallEndH ,smallEndD ,smallEndHForTopDiameter = 0, smallEndDInsideBark , smallEndDJuvenileWood=0;
		double logLength = 0,totalLogsLength;
		double woodVolume,totalVolume,barkVolume, juvenileWoodVolume = 0;
		double woodMass, woodCarbonMass;
		StringBuffer b;
		String pieceOrigin = "";
		String pieceProduct = "";
		boolean boolAllJuvenileWoodAtLargeEndH , boolAllJuvenileWoodAtSmallEndH;


		if ((interventionStep & t.getNumberOfCutOrDead() > 0) || step == lastStep) {

			if (interventionStep) {
				number = t.getNumberOfCutOrDead()/5d;	//OVERSAMPLING
			} else {
				number = t.getNumber()/5d; //OVERSAMPLING
			}


			if (number==0) {}
			else if ((t.getHeight() < 4) ||
					(Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),stumpHeight) * Math.PI < topGirth)) {
//				Log.println ("height =" +t.getHeight());
				logLength = t.getHeight() - stumpHeight;
				totalVolume = Pp3Volume.calcWithTruncatedConeTreeVolume(t.getDbh (), t.getHeight ());

				if (t.getHeight() < 4) {
					woodMass = Pp3TreeBiomass.calcStemBiomass(t.getDbh (), t.getHeight (), 0d, (Pp3Stand) stand);
				}
				else {
					smallEndH = t.getHeight ();
					smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), smallEndH);
					woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh (), t.getHeight (), stumpHeight, smallEndDInsideBark,stumpHeight);
					woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
				}
				woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;

//				Log.println ("height =" +t.getHeight());
				b = new StringBuffer ();
				b.append (t.getId ());
				b.append (",");
				b.append (t.getAge ());
				b.append (",");
				b.append (nf2.format (t.getDbh ()));
				b.append (",");
				b.append (nf2.format (t.getHeight ()));
				b.append (",");
				b.append (nf2.format (number));
				b.append (",-1,");
				b.append (nf2.format (logLength));
				b.append (",,,,,,,,,,,,");
				b.append (nf6.format (totalVolume));
				b.append (",");
				b.append (nf6.format (woodMass));
				b.append (",");
				b.append (nf6.format (woodCarbonMass));
//				b.append (",");
				out.add (new FreeRecord (b.toString ()));

			// create one piece
				numberOfLog = 1;
				int rankInTree = numberOfLog;
				double numberOfPieces = number;
				byte numberOfRadius = 1;
				String treeStatus = "";
				pieceOrigin = "-1";
				pieceProduct = "";
				double disc1Y_m = 1.3;
				double disc1RadiusOverBark_mm = t.getDbh () / 2 * 10;
				double disc1RadiusUnderBark_mm = 0;	// TO BE DEFINED
				double disc2Y_m = 0;
				double disc2RadiusOverBark_mm = 0;
				double disc2RadiusUnderBark_mm = 0;
				double disc1JuvenileWoodRadius_mm = 0;
				double disc2JuvenileWoodRadius_mm = 0;
				double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
				double pieceY_mm = stumpHeight * 1000;
//				GPiece piece = createPiece (logId++, rankInTree, numberOfPieces,
				GPiece piece = createPiece (loggableTree,
						pieceIdDispenser.next(), 
						rankInTree, numberOfPieces,
						numberOfRadius, 
						treeStatus, 
						pieceOrigin, 
						logCategory,
						disc1Y_m, 
						disc1RadiusOverBark_mm, 
						disc1RadiusUnderBark_mm,
						disc2Y_m, 
						disc2RadiusOverBark_mm, 
						disc2RadiusUnderBark_mm,
						disc1JuvenileWoodRadius_mm, 
						disc2JuvenileWoodRadius_mm,
						pieceLength_mm, 
						pieceY_mm);
//				piece.setLogCategory(logCategory);
//				piece.properties.put ("totalVolume_m3", totalVolume);
//				piece.properties.put ("woodMass_kg", woodMass);
//				piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
				piece.setProperty(Property.totalVolume_m3, totalVolume);
				piece.setProperty(Property.woodMass_kg, woodMass);
				piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);

//				pieces.add (piece);
				addWoodPiece((LoggableTree) t, piece);
			} else  {
		//	TABLE Height & Dbh																							!
			// from step to root (backwards)
			int k = -1;
			ArrayList height = new ArrayList();
			ArrayList dbh = new ArrayList();
			int treeId = t.getId();
			Step stepN = step;
			Pp3Tree tAtStepN = (Pp3Tree) ((Pp3Stand) stepN.getScene()).getTree(treeId);
			while (stepN != null) {
				if (!stepN.getScene ().isInterventionResult ()) {
					k++;
					tAtStepN = (Pp3Tree) ((Pp3Stand) stepN.getScene()).getTree(treeId);
					height.add(new Double(tAtStepN.getHeight()));
					dbh.add(new Double(tAtStepN.getDbh()));
				}
				stepN = (Step) stepN.getFather ();
			}

//			int ageMin = stand1.getAge();
			double heightMin = tAtStepN.getHeight();
			double heightIncr = heightMin;
			for (int a = ageMin-1 ; a >= 0; a--) {
				heightIncr = heightIncr - (heightMin / ageMin) ;
				height.add(new Double(heightIncr));
				dbh.add(new Double(0d));
			}
			Double[] heightTable = new Double[height.size()];
			height.toArray(heightTable);
			Double[] dbhTable = new Double[dbh.size()];
			dbh.toArray(dbhTable);
//			Log.println ("sizeHeight ="+ height.size()+ "sizedbh =" +dbh.size());
//			for (int j = 0; j < height.size(); j++) Log.println ("j ="+j+", height ="+heightTable[j].doubleValue());
//			for (int j = 0; j < dbh.size(); j++) Log.println ("j ="+j+", dbh ="+dbhTable[j].doubleValue());

				stumpDiameter = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),stumpHeight);
				numberOfLog = 0;
				totalLogsLength = stumpHeight;

				for (int c = 1; c < 4; c++) {
					switch (c) {
						case 1 :
						{
							pieceOrigin = "1";
							logLength=log1Length;
							smallEndHForTopDiameter = Pp3TreeTaper.calcHeightAtTargetDiameter(t.getDbh(), t.getHeight(),top1Girth/Math.PI,stumpHeight);
							break;
						}
						case 2 :
						{
							pieceOrigin = "2";
							logLength=log2Length;
							smallEndHForTopDiameter = Pp3TreeTaper.calcHeightAtTargetDiameter(t.getDbh(), t.getHeight(),top2Girth/Math.PI,stumpHeight);
							break;
						}
						case 3 :
						{
							pieceOrigin = "3";
							logLength=log3Length;
							smallEndHForTopDiameter = Pp3TreeTaper.calcHeightAtTargetDiameter(t.getDbh(), t.getHeight(),top3Girth/Math.PI,stumpHeight);
							break;
						}
					}

//					Log.println ("logLength =" +logLength+ "smallEndHForTopDiameter = " + smallEndHForTopDiameter);
					while ((smallEndHForTopDiameter - totalLogsLength) > logLength){
//							Log.println ("totalLogsLength =" +totalLogsLength);
							numberOfLog += 1;//Log Number (between last log and the target cutting diameter)
							largeEndH = totalLogsLength ;
							largeEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
							largeEndDInsideBark = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),largeEndH);

							smallEndH = largeEndH + logLength;
							smallEndD =  Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),smallEndH);
							smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),smallEndH);

							// Juvenile Wood dimensions
							// j = 0 for total age , j = Pp3ModelParameters.JUVENILE_WOOD_AGE part of the trunk where there are only rings with juvenile wood
							if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
								largeEndDJuvenileWood = largeEndDInsideBark;
								boolAllJuvenileWoodAtLargeEndH = true;
							}else if (largeEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
								largeEndDJuvenileWood = largeEndDInsideBark;
								boolAllJuvenileWoodAtLargeEndH = true;
							} else {
								int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
								while (largeEndH < heightTable[j].doubleValue()) {j++ ;}
								j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
								double heightJ = heightTable[j].doubleValue();
								double dbhJ = dbhTable[j].doubleValue();
								largeEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , largeEndH );
								boolAllJuvenileWoodAtLargeEndH = false;
							}
							if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
								smallEndDJuvenileWood = smallEndDInsideBark;
								boolAllJuvenileWoodAtSmallEndH = true;
							} else if (smallEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
								smallEndDJuvenileWood = smallEndDInsideBark;
								boolAllJuvenileWoodAtSmallEndH = true;
							} else {
								int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
								while (smallEndH < heightTable[j].doubleValue()) {j++ ;}
								j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
								double heightJ = heightTable[j].doubleValue();
								double dbhJ = dbhTable[j].doubleValue();
								smallEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , smallEndH );
								boolAllJuvenileWoodAtSmallEndH = false;
							}

							woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
							woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
							woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
							totalVolume = Pp3Volume.calcOneLogVolumeHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
							barkVolume = totalVolume - woodVolume ;
							// Juvenile Wood Volume
							if ( boolAllJuvenileWoodAtLargeEndH == boolAllJuvenileWoodAtSmallEndH ) {
								if (boolAllJuvenileWoodAtLargeEndH) {
									juvenileWoodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
								} else {
									juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,smallEndDJuvenileWood, logLength);
								}
							} else {
								double heightJuvenileWoodAge = heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue();
								double logLength1 = smallEndH - heightJuvenileWoodAge ;
								double logLength2 =  heightJuvenileWoodAge - largeEndH  ;
								double maxDiameterAllJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), heightJuvenileWoodAge);
								juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,maxDiameterAllJuvenileWood, logLength2) +
													Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),heightJuvenileWoodAge,smallEndD,stumpHeight);
													// Pp3Volume.calcTruncatedConeVolume(maxDiameterAllJuvenileWood,smallEndDJuvenileWood, logLength1);
							}

							totalLogsLength += logLength;

							b = new StringBuffer ();
							b.append (t.getId ());
							b.append (",");
							b.append (t.getAge ());
							b.append (",");
							b.append (nf2.format (t.getDbh ()));
							b.append (",");
							b.append (nf2.format (t.getHeight ()));
							b.append (",");
							b.append (nf2.format (number));
							b.append (",");
							b.append (numberOfLog) ;
							b.append (",");
							b.append (nf2.format (logLength));
							b.append (",");
							b.append (nf2.format (largeEndD));
							b.append (",");
							b.append (nf2.format (largeEndDInsideBark));
							b.append (",");
							b.append (nf2.format (largeEndDJuvenileWood));
							b.append (",");
							b.append (nf2.format (largeEndH));
							b.append (",");
							b.append (nf2.format (smallEndD));
							b.append (",");
							b.append (nf2.format (smallEndDInsideBark));
							b.append (",");
							b.append (nf2.format (smallEndDJuvenileWood));
							b.append (",");
							b.append (nf2.format (smallEndH));
							b.append (",");
							b.append (nf6.format (woodVolume));
							b.append (",");
							b.append (nf6.format (juvenileWoodVolume));
							b.append (",");
							b.append (nf6.format (barkVolume));
							b.append (",");
							b.append (nf6.format (totalVolume));
							b.append (",");
							b.append (nf6.format (woodMass));
							b.append (",");
							b.append (nf6.format (woodCarbonMass));
//							b.append (",");
							out.add (new FreeRecord (b.toString ()));
//create one piece
				int rankInTree = numberOfLog;
				double numberOfPieces = number;
				byte numberOfRadius = 1;
				String treeStatus = "";
				//pieceProduct = "";
				double disc1Y_m = largeEndH;
				double disc1RadiusOverBark_mm = largeEndD / 2 * 10;
				double disc1RadiusUnderBark_mm = largeEndDInsideBark / 2 * 10;
				double disc2Y_m = smallEndH;
				double disc2RadiusOverBark_mm = smallEndD / 2 * 10;
				double disc2RadiusUnderBark_mm = smallEndDInsideBark / 2 * 10;
				double disc1JuvenileWoodRadius_mm = largeEndDJuvenileWood / 2 * 10;
				double disc2JuvenileWoodRadius_mm = smallEndDJuvenileWood / 2 * 10;
				double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
				double pieceY_mm = disc1Y_m * 1000;
//				GPiece piece = createPiece (logId++, rankInTree, numberOfPieces,
				GPiece piece = createPiece (loggableTree,
						pieceIdDispenser.next(),
						rankInTree,
						numberOfPieces,
						numberOfRadius,
						treeStatus, 
						pieceOrigin, 
						logCategory,
						disc1Y_m, 
						disc1RadiusOverBark_mm, 
						disc1RadiusUnderBark_mm,
						disc2Y_m, 
						disc2RadiusOverBark_mm, 
						disc2RadiusUnderBark_mm,
						disc1JuvenileWoodRadius_mm, 
						disc2JuvenileWoodRadius_mm,
						pieceLength_mm, 
						pieceY_mm);
//				piece.setLogCategory(logCategory);
//				piece.properties.put ("totalVolume_m3", totalVolume);
//				piece.properties.put ("woodVolume_m3", woodVolume);
//				piece.properties.put ("barkVolume_m3", barkVolume);
//				piece.properties.put ("juvenileWoodVolume_m3", juvenileWoodVolume);
//				piece.properties.put ("woodMass_kg", woodMass);
//				piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
				piece.setProperty(Property.totalVolume_m3, totalVolume);
				piece.setProperty(Property.woodVolume_m3, woodVolume);
				piece.setProperty(Property.barkVolume_m3, barkVolume);
				piece.setProperty(Property.juvenileWoodVolume_m3, juvenileWoodVolume);
				piece.setProperty(Property.woodMass_kg, woodMass);
				piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
				
//				pieces.add (piece);
				addWoodPiece((LoggableTree) t, piece);

					}
				}

//				Log.println ("totalLogsLength =" +totalLogsLength + "smallEndHForTopDiameter =" +smallEndHForTopDiameter);
				if ((smallEndHForTopDiameter - totalLogsLength) > 0) {
//				Log.println ("totalLogsLength =" +totalLogsLength + "smallEndHForTopDiameter =" +smallEndHForTopDiameter);
//					numberOfLog = -1;
					numberOfLog += 1;
					largeEndH = totalLogsLength ;
					largeEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
					largeEndDInsideBark = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),largeEndH);
					smallEndH = smallEndHForTopDiameter ;
					if (topGirth == 0) smallEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(),smallEndH);
					else smallEndD = topGirth / Math.PI;
					smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(),smallEndH);

					logLength = smallEndH - largeEndH;

					// Juvenile Wood dimensions
					// j = 0 for total age , j = Pp3ModelParameters.JUVENILE_WOOD_AGE part of the trunk where there are only rings with juvenile wood
					if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
						largeEndDJuvenileWood = largeEndDInsideBark;
						boolAllJuvenileWoodAtLargeEndH = true;
					} else if (largeEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
						largeEndDJuvenileWood = largeEndDInsideBark;
						boolAllJuvenileWoodAtLargeEndH = true;
					} else {
						int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
						while (largeEndH < heightTable[j].doubleValue()) {j++ ;}
						j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
						double heightJ = heightTable[j].doubleValue();
						double dbhJ = dbhTable[j].doubleValue();
						largeEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , largeEndH );
						boolAllJuvenileWoodAtLargeEndH = false;
					}
					if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
						smallEndDJuvenileWood = smallEndDInsideBark;
						boolAllJuvenileWoodAtSmallEndH = true;
					} else if (smallEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
						smallEndDJuvenileWood = smallEndDInsideBark;
						boolAllJuvenileWoodAtSmallEndH = true;
					} else {
						int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
						while (smallEndH < heightTable[j].doubleValue()) {j++ ;}
						j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
						double heightJ = heightTable[j].doubleValue();
						double dbhJ = dbhTable[j].doubleValue();
						smallEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , smallEndH );
						boolAllJuvenileWoodAtSmallEndH = false;
					}

					if (logLength != 0) {
						woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
						woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
						woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
						totalVolume = Pp3Volume.calcOneLogVolumeHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
						barkVolume = totalVolume - woodVolume ;
						// Juvenile Wood Volume
						if ( boolAllJuvenileWoodAtLargeEndH == boolAllJuvenileWoodAtSmallEndH ) {
							if (boolAllJuvenileWoodAtLargeEndH) {
								juvenileWoodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
							} else {
								juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,smallEndDJuvenileWood, logLength);
							}
						} else {
							double heightJuvenileWoodAge = heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue();
							double logLength1 = smallEndH - heightJuvenileWoodAge ;
							double logLength2 =  heightJuvenileWoodAge - largeEndH  ;
							double maxDiameterAllJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), heightJuvenileWoodAge);
							juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,maxDiameterAllJuvenileWood, logLength2) +
												Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),heightJuvenileWoodAge,smallEndD,stumpHeight);
												// Pp3Volume.calcTruncatedConeVolume(maxDiameterAllJuvenileWood,smallEndDJuvenileWood, logLength1);
						}

						b = new StringBuffer ();
						b.append (t.getId ());
						b.append (",");
						b.append (t.getAge ());
						b.append (",");
						b.append (nf2.format (t.getDbh ()));
						b.append (",");
						b.append (nf2.format (t.getHeight ()));
						b.append (",");
						b.append (nf2.format (number));
						b.append (",");
//						b.append (numberOfLog) ;
						b.append ("-1") ;
						b.append (",");
						b.append (nf2.format (logLength));
						b.append (",");
						b.append (nf2.format (largeEndD));
						b.append (",");
						b.append (nf2.format (largeEndDInsideBark));
						b.append (",");
						b.append (nf2.format (largeEndDJuvenileWood));
						b.append (",");
						b.append (nf2.format (largeEndH));
						b.append (",");
						b.append (nf2.format (smallEndD));
						b.append (",");
						b.append (nf2.format (smallEndDInsideBark));
						b.append (",");
						b.append (nf2.format (smallEndDJuvenileWood));
						b.append (",");
						b.append (nf2.format (smallEndH));
						b.append (",");
						b.append (nf6.format (woodVolume));
						b.append (",");
						b.append (nf6.format (juvenileWoodVolume));
						b.append (",");
						b.append (nf6.format (barkVolume));
						b.append (",");
						b.append (nf6.format (totalVolume));
						b.append (",");
						b.append (nf6.format (woodMass));
						b.append (",");
						b.append (nf6.format (woodCarbonMass));
//						b.append (",");
						out.add (new FreeRecord (b.toString ()));
//create one piece
				int rankInTree = numberOfLog;
				double numberOfPieces = number;
				byte numberOfRadius = 1;
				String treeStatus = "";
				pieceOrigin = "-1";
				pieceProduct = "";
				double disc1Y_m = largeEndH;
				double disc1RadiusOverBark_mm = largeEndD / 2 * 10;
				double disc1RadiusUnderBark_mm = largeEndDInsideBark / 2 * 10;
				double disc2Y_m = smallEndH;
				double disc2RadiusOverBark_mm = smallEndD / 2 * 10;
				double disc2RadiusUnderBark_mm = smallEndDInsideBark / 2 * 10;
				double disc1JuvenileWoodRadius_mm = largeEndDJuvenileWood / 2 * 10;
				double disc2JuvenileWoodRadius_mm = smallEndDJuvenileWood / 2 * 10;
				double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
				double pieceY_mm = disc1Y_m * 1000;
				GPiece piece = createPiece (loggableTree,
						pieceIdDispenser.next(), 
						rankInTree, 
						numberOfPieces,
						numberOfRadius, 
						treeStatus, 
						pieceOrigin, 
						logCategory,
						disc1Y_m, 
						disc1RadiusOverBark_mm, 
						disc1RadiusUnderBark_mm,
						disc2Y_m, 
						disc2RadiusOverBark_mm, 
						disc2RadiusUnderBark_mm,
						disc1JuvenileWoodRadius_mm, 
						disc2JuvenileWoodRadius_mm,
						pieceLength_mm, 
						pieceY_mm);
//				piece.setLogCategory(logCategory);
//				piece.properties.put ("totalVolume_m3", totalVolume);
//				piece.properties.put ("woodVolume_m3", woodVolume);
//				piece.properties.put ("barkVolume_m3", barkVolume);
//				piece.properties.put ("juvenileWoodVolume_m3", juvenileWoodVolume);
//				piece.properties.put ("woodMass_kg", woodMass);
//				piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
				piece.setProperty(Property.totalVolume_m3, totalVolume);
				piece.setProperty(Property.woodVolume_m3, woodVolume);
				piece.setProperty(Property.barkVolume_m3, barkVolume);
				piece.setProperty(Property.juvenileWoodVolume_m3, juvenileWoodVolume);
				piece.setProperty(Property.woodMass_kg, woodMass);
				piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
//				pieces.add (piece);
				addWoodPiece((LoggableTree) t, piece); 
					}
				}
///*
//				Log.println ("cbh =" +t.getDbh()* Math.PI+ "topGirth = " + topGirth);
				largeEndD = topGirth /Math.PI;
				largeEndH = Pp3TreeTaper.calcHeightAtTargetDiameter (t.getDbh(), t.getHeight(),largeEndD, stumpHeight);
				largeEndDInsideBark = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), largeEndH);
				smallEndH = t.getHeight ();
				smallEndD = Pp3TreeTaper.calcDiameterOverBarkAtH (t.getDbh(), t.getHeight(), smallEndH);
				smallEndDInsideBark=Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), smallEndH);
				logLength = smallEndH - largeEndH;

				// Juvenile Wood dimensions
				// j = 0 for total age , j = Pp3ModelParameters.JUVENILE_WOOD_AGE part of the trunk where there are only rings with juvenile wood
				if (height.size()<=Pp3ModelParameters.JUVENILE_WOOD_AGE) {
					largeEndDJuvenileWood = largeEndDInsideBark;
					boolAllJuvenileWoodAtLargeEndH = true;
				} else if (largeEndH >= heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue()) {
					largeEndDJuvenileWood = largeEndDInsideBark;
					boolAllJuvenileWoodAtLargeEndH = true;
				} else {
					int j= Pp3ModelParameters.JUVENILE_WOOD_AGE + 1 ;
					while (largeEndH < heightTable[j].doubleValue()) {j++ ;}
					j = j - Pp3ModelParameters.JUVENILE_WOOD_AGE ;
					double heightJ = heightTable[j].doubleValue();
					double dbhJ = dbhTable[j].doubleValue();
					largeEndDJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (dbhJ ,heightJ , largeEndH );
					boolAllJuvenileWoodAtLargeEndH = false;
				}
				smallEndDJuvenileWood = smallEndDInsideBark ;
				boolAllJuvenileWoodAtSmallEndH = true;

				if (logLength != 0) {
					woodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
					woodMass = woodVolume * Pp3ModelParameters.PARAMETER_WOOD_DENSITY;
					woodCarbonMass = woodMass * Pp3ModelParameters.PARAMETER_STEM_WOOD_CARBON_MASS;
					totalVolume = Pp3Volume.calcOneLogVolumeHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
					barkVolume = totalVolume - woodVolume ;
					// Juvenile Wood Volume
					if ( boolAllJuvenileWoodAtLargeEndH == boolAllJuvenileWoodAtSmallEndH ) {
						if (boolAllJuvenileWoodAtLargeEndH) {
							juvenileWoodVolume = Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),largeEndH,smallEndD,stumpHeight);
						} else {
							juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,smallEndDJuvenileWood, logLength);
						}
					} else {
						double heightJuvenileWoodAge = heightTable[Pp3ModelParameters.JUVENILE_WOOD_AGE].doubleValue();
						double logLength1 = smallEndH - heightJuvenileWoodAge ;
						double logLength2 =  heightJuvenileWoodAge - largeEndH  ;
						double maxDiameterAllJuvenileWood = Pp3TreeTaper.calcDiameterUnderBarkAtH (t.getDbh(), t.getHeight(), heightJuvenileWoodAge);
						juvenileWoodVolume = Pp3Volume.calcTruncatedConeVolume(largeEndDJuvenileWood,maxDiameterAllJuvenileWood, logLength2) +
											Pp3Volume.calcOneLogVolumeUnderBarkHD (t.getDbh(), t.getHeight(),heightJuvenileWoodAge,smallEndD,stumpHeight);
											// Pp3Volume.calcTruncatedConeVolume(maxDiameterAllJuvenileWood,smallEndDJuvenileWood, logLength1);
					}
					b = new StringBuffer ();
					b.append (t.getId ());
					b.append (",");
					b.append (t.getAge ());
					b.append (",");
					b.append (nf2.format (t.getDbh ()));
					b.append (",");
					b.append (nf2.format (t.getHeight ()));
					b.append (",");
					b.append (nf2.format (number));
					b.append (",0,") ; //Log Number (upper the target cutting diameter)
					b.append (nf2.format (logLength));
					b.append (",");
					b.append (nf2.format (largeEndD));
					b.append (",");
					b.append (nf2.format (largeEndDInsideBark));
					b.append (",");
					b.append (nf2.format (largeEndDJuvenileWood));
					b.append (",");
					b.append (nf2.format (largeEndH));
					b.append (",");
					b.append (nf2.format (smallEndD));
					b.append (",");
					b.append (nf2.format (smallEndDInsideBark));
					b.append (",");
					b.append (nf2.format (smallEndDJuvenileWood));
					b.append (",");
					b.append (nf2.format (smallEndH));
					b.append (",");
					b.append (nf6.format (woodVolume));
					b.append (",");
					b.append (nf6.format (juvenileWoodVolume));
					b.append (",");
					b.append (nf6.format (barkVolume));
					b.append (",");
					b.append (nf6.format (totalVolume));
					b.append (",");
					b.append (nf6.format (woodMass));
					b.append (",");
					b.append (nf6.format (woodCarbonMass));
//					b.append (",");
					out.add (new FreeRecord (b.toString ()));

//create one piece
				numberOfLog += 1;
				int rankInTree = numberOfLog;
				double numberOfPieces = number;
				byte numberOfRadius = 1;
				String treeStatus = "";
				pieceOrigin = "0";
				pieceProduct = "";
				double disc1Y_m = largeEndH;
				double disc1RadiusOverBark_mm = largeEndD / 2 * 10;
				double disc1RadiusUnderBark_mm = largeEndDInsideBark / 2 * 10;
				double disc2Y_m = smallEndH;
				double disc2RadiusOverBark_mm = smallEndD / 2 * 10;
				double disc2RadiusUnderBark_mm = smallEndDInsideBark / 2 * 10;
				double disc1JuvenileWoodRadius_mm = largeEndDJuvenileWood / 2 * 10;
				double disc2JuvenileWoodRadius_mm = smallEndDJuvenileWood / 2 * 10;
				double pieceLength_mm = logLength * 1000; // en mm like centre_Y_mm for  piece with two discs
				double pieceY_mm = disc1Y_m * 1000;
//				GPiece piece = createPiece (logId++, rankInTree, numberOfPieces,
				GPiece piece = createPiece (loggableTree,
						pieceIdDispenser.next(), 
						rankInTree, 
						numberOfPieces,
						numberOfRadius, 
						treeStatus, 
						pieceOrigin, 
						logCategory,
						disc1Y_m, 
						disc1RadiusOverBark_mm, 
						disc1RadiusUnderBark_mm,
						disc2Y_m, 
						disc2RadiusOverBark_mm, 
						disc2RadiusUnderBark_mm,
						disc1JuvenileWoodRadius_mm, 
						disc2JuvenileWoodRadius_mm,
						pieceLength_mm, 
						pieceY_mm);
//				piece.setLogCategory(logCategory);
//				piece.properties.put ("totalVolume_m3", totalVolume);
//				piece.properties.put ("woodVolume_m3", woodVolume);
//				piece.properties.put ("barkVolume_m3", barkVolume);
//				piece.properties.put ("juvenileWoodVolume_m3", juvenileWoodVolume);
//				piece.properties.put ("woodMass_kg", woodMass);
//				piece.properties.put ("woodCarbonMass_kg", woodCarbonMass);
				piece.setProperty(Property.totalVolume_m3, totalVolume);
				piece.setProperty(Property.woodVolume_m3, woodVolume);
				piece.setProperty(Property.barkVolume_m3, barkVolume);
				piece.setProperty(Property.juvenileWoodVolume_m3, juvenileWoodVolume);
				piece.setProperty(Property.woodMass_kg, woodMass);
				piece.setProperty(Property.woodCarbonMass_kg, woodCarbonMass);
//				pieces.add (piece);
				addWoodPiece((LoggableTree) t, piece);
				}
//*/
			}
		}

		
	}

	/*
	 * Useless (non-Javadoc)
	 * @see jeeb.lib.defaulttype.Extension#activate()
	 */
	@Override
	public void activate () {}

	@Override
	public LoggableTree getEligible (LoggableTree t) {
		return t;
	}

	/*
	 * Useless method for this class (non-Javadoc)
	 * @see repicea.simulation.treelogger.TreeLogger#isCompatibleWith(java.lang.Object)
	 */
	@Override
	public boolean isCompatibleWith(Object referent) {
		return false;
	}

}

