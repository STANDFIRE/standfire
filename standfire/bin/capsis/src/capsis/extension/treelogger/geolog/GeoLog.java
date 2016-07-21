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

package capsis.extension.treelogger.geolog;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.TicketDispenser;
import jeeb.lib.util.Translator;
import lerfob.fagacees.FagaceesSpeciesProvider.FgSpecies;
import lerfob.fagacees.model.FgTree;
import pp3.model.Pp3Tree;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.MemoryWatchDog;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceLight;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.extension.treelogger.geolog.util.RecordMaker;
import capsis.extension.treelogger.geolog.util.TreeStatusMaker;
import capsis.extensiontype.TreeLoggerImpl;
import capsis.kernel.GModel;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**	
 * GeoLog is a tree logger designed for generic logging jobs based on simple geometric rules
 * @author F. Mothe - March 2006
 * @author Mathieu Fortin - April 2010 (refactoring)
 * @author Mathieu Fortin - November 2011 (refactoring)
 * @author Mathieu Fortin - January 2013 (refactoring)
 */
public class GeoLog extends TreeLogger<GeoLogTreeLoggerParameters, GeoLogLoggableTree> implements TreeLoggerImpl {

	static {
		Translator.addBundle("capsis.extension.treelogger.geolog.GeoLog");
	}

	static public final String AUTHOR = "Frédéric Mothe, Mathieu Fortin";
	static public final String VERSION = "2.0";
	static public final String DESCRIPTION = "GeoLog.description";

	public static final double DEFAULT_HEIGHT_STUMP_m = 0.3;

	protected static Random random = new Random ();
	private boolean verbose = false;
	private EnumMap <TreeStatusMaker.Status, String> statusNames;
	private GeoLogExport export;
	private Collection<Step> stepsFromRoot;
	private TreeStatusMaker statusMaker;
	private TreeRadius_cmProvider mp;
	private TicketDispenser pieceIdDispenser;
	private SpecialCase previousSpecial;

	// TEMPO : TODO : make specialised versions :
	public static enum SpecialCase {
		FG_OAK, FG_BEECH, PP3, GENERIC, NULL
	}

	/**
	 * Constructor for Gui mode.
	 */
	public GeoLog() {
		super();
	}

	/**
	 * Constructor in script mode. May throw exception if something goes wrong.
	 * @param params the parameters of the tree logger
	 * @param oColl a Collection of GeoLogLoggableTree instances
	 */
	public GeoLog(GeoLogTreeLoggerParameters params, Collection<GeoLogLoggableTree> oColl) {
		this();
		setTreeLoggerParameters(params);
		init(oColl);
	}

	
	/**
	 * This method sets whether or not the logging details should be listed. Default is false.
	 * @param verbose a boolean
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	

	
	@Override
	public void setTreeLoggerParameters() {
		try {
			if (this.getLoggableTrees() != null && !this.getLoggableTrees().isEmpty()) {
				params = createDefaultTreeLoggerParameters();
				getTreeLoggerParameters().pleaseOpenDialog = true;		// we are in interactive mode so dialogs are required to make it possible to change log category specifications
			}

			if (!getTreeLoggerParameters().isCorrect()) {
				// This dialog will ensure the starter is complete
				getTreeLoggerParameters().showUI(null);
				if (getTreeLoggerParameters().isParameterDialogCanceled()) {
					return;
				}
			}
		} catch (ClassCastException e) {
			Log.println(Log.ERROR, "GeoLog.setTreeLoggerParameters",
					"The TreeLoggerParameters object is not compatible with this tree logger",
					e);
			throw new InvalidParameterException(e.getMessage());
		} catch (Exception e) {
			Log.println(Log.ERROR, "GeoLog.setTreeLoggerParameters",
					"Error while setting the parameters",
					e);
			throw new InvalidParameterException(e.getMessage());
		}
	}

	@Override
	public void init(Collection<?> trees) {
		super.init(trees);
		statusNames = new EnumMap<TreeStatusMaker.Status, String>(TreeStatusMaker.Status.class);
		for (TreeStatusMaker.Status s : TreeStatusMaker.Status.values ()) {
			statusNames.put (s, Translator.swap("GeoLog.status" + s));
		}
	}

	
	@Override
	protected void priorToRunning() {
		pieceIdDispenser = new TicketDispenser();

		initialiseRandom();

		Tree oldestTree = getOldest(getLoggableTrees());					// We need the oldest tree for computing stepsFromRoot 

		//			System.out.println (getTreeLoggerParameters().getLoggingName () + "...");

		if (!getTreeLoggerParameters().isRecordResultsEnabled()) {
			System.out.println ("Results will be discarded !");
		}

		try {
			export = makeExport(oldestTree);
		} catch (IOException e) {
			System.out.println("Unable to instantiate the GeoLogExport object");
			e.printStackTrace();
		}

		// finds the last step
		Step refStep = null;
		for (LoggableTree tree : getLoggableTrees()) {
			Step step = ((Tree) tree).getScene().getStep();
			if (refStep == null || step.getScene().getDate() > refStep.getScene().getDate()) {
				refStep = step;
			} else {
				if (step.getScene().getDate() == refStep.getScene().getDate() && step.getScene().isInterventionResult()) {
					refStep = step;
				}
			}
		}

		Project scenario = refStep.getProject();
		stepsFromRoot = scenario.getStepsFromRoot (refStep);
		statusMaker = new TreeStatusMaker(stepsFromRoot);
		mp = (TreeRadius_cmProvider) scenario.getModel().getMethodProvider();

		previousSpecial = SpecialCase.NULL;
	}
	
	
	@Override
	protected void posteriorToRunning() {
		if (export != null && getTreeLoggerParameters().isExportResultsEnabled()) {
			try {
				export.close ();	// closes only tree and log files
				export.saveProductTables();
			} catch (IOException e) {
				System.out.println("Error while closing the export instance or saving the product tables!");
			}
		}
	}
	
	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchWith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public static boolean matchWith(Object referent) {
		boolean match = false;
		if (referent instanceof GModel) {
			GModel model = (GModel) referent;
			if (model.getMethodProvider() instanceof TreeRadius_cmProvider) {
				match = true;
			}
		}
		return match;
	}

	private void initialiseRandom() {
		long seed = GeoLogTreeLoggerParameters.randomSeed;
		if (seed == -1L)  {
			seed = random.nextLong();
		}
		random.setSeed(seed);
	}

	//	Returns a new GeoLogExport class using the record factory given by the starter
	//	(the tree and log (i.e. billon) files are opened here)
	//~ protected GeoLogExport makeExport (MethodProvider mp)
	private GeoLogExport makeExport (Tree oldestTree) throws IOException {
		// TODO : problem if special case changes from tree to tree !
		SpecialCase oldestTreeSpecial = getSpecialCase (oldestTree);
		RecordMaker <GPiece> maker = getTreeLoggerParameters().makeRecordMaker (oldestTreeSpecial);
		// TODO : define the jobID
		return new GeoLogExport((LoggableTree) oldestTree,
				-1,
				getTreeLoggerParameters().isExportResultsEnabled(),
				getTreeLoggerParameters(),
				maker,
				statusNames.values ());
	}

	@Override
	public GeoLogTreeLoggerParameters getTreeLoggerParameters() {
		return (GeoLogTreeLoggerParameters) this.params;
	}


	//	Returns the tree status
	private String getTreeStatus (Tree tree, TreeStatusMaker statusMaker) {
		TreeStatusMaker.Status status = statusMaker.getStatus (tree);
		return statusNames.get (status);
	}

	
	/**
	 * This method returns a new GPiece without any disc.
	 * @param tree the tree from which comes this piece
	 * @param pieceId the piece id
	 * @param nLog the rank in the tree
	 * @param botHeight_m the height of the bottom section (m)
	 * @param topHeight_m the height of the top section (m)
	 * @param treeStatus the tree status
	 * @param crownBaseHeight_m_TEMPO the crown base height (m)
	 * @param withinTreeExpansionFactor the within tree expansion factor
	 * @param logCategory the log category of this piece
	 * @param productPriority the priority of the product
	 * @return a GPiece instance
	 */
	private GPiece makePiece(LoggableTree tree,
			int pieceId,
			int nLog,
			double botHeight_m,
			double topHeight_m,
			String treeStatus,
			double crownBaseHeight_m_TEMPO,
			double withinTreeExpansionFactor,
			GeoLogLogCategory logCategory,
			int productPriority) {

		byte numberOfRadius = 1;	// GeoLog manages only circular trees

		// First ring is bark :
		// (in theory we should verify here that bark thickness is not null)
		boolean pieceWithBark = true ;
		// Last ring is pith, but will be set to 0 => false :
		boolean pieceWithPith = false ;

		double h0_mm = botHeight_m * 1000;
		double h1_mm = topHeight_m * 1000;
		// double h1_mm = (topHeight_m <= botHeight_m) ? h0_mm : topHeight_m * 1000;

		double pieceLength_mm = h1_mm - h0_mm;
		if (pieceLength_mm < 0.0) {
			System.out.println ("makePiece () : negative length !");
		}

		GPiece piece = new GPiece(tree,
				pieceId,
				nLog,								// the ticketDispenser object will start from 1 instead of 0
				withinTreeExpansionFactor,
				pieceLength_mm,
				h0_mm,	// pieceY_mm
				pieceWithBark,
				pieceWithPith,
				numberOfRadius,
				treeStatus,
				logCategory);
		piece.setPriority(productPriority);
		String origin = getOrigin(piece.getCrownRatio(crownBaseHeight_m_TEMPO));
		piece.setOrigin(origin);
		return piece;
	}


	/**
	 * This method returns the location of the piece, whether it is a stem piece, a crown piece or a mixed piece
	 * @param crownRatio the crown ratio of the piece 
	 * @return the origin (a string either "stem", "crown", or "mixed"
	 */
	private String getOrigin(double crownRatio) {
		String origin;
		if (crownRatio <= 0.0) {
			origin = "stem";
		} else if (crownRatio >= 1.0) {
			origin = "crown";
		} else {
			origin = "mixed";
		}
		return origin;
	}


	private SpecialCase getSpecialCase(Tree tree) {
		SpecialCase special = SpecialCase.GENERIC;
		if (tree instanceof FgTree) {
			if (((FgTree) tree).getOfficialSpeciesForSimulation() == FgSpecies.OAK) {
				special = SpecialCase.FG_OAK;
			} else if (((FgTree) tree).getOfficialSpeciesForSimulation() == FgSpecies.BEECH) {
				special = SpecialCase.FG_BEECH;
			}
		} else if (tree instanceof Pp3Tree) {
			special = SpecialCase.PP3;
		}
		return special;
	}

	// (topHeight_m may be < botHeight_m, which results in a piece with height = 0)
	/**
	 * This method creates a fake woodpiece. 
	 * @param tree the tree from which is made the piece
	 * @param pieceId the piece id
	 * @param stepsFromRoot a collection of Step from the root
	 * @param mp a TreeRadius_cmProvider instance
	 * @param prod a GeoLogLogCategory instance (if null, the prod is set to a BasicLogCategory instance)
	 * @param botHeight_m the height of the bottom section (m)
	 * @param topHeight_m the height of the top section (m)
	 * @param discInterval_m the interval between the disc (set 0 to use the default interval)
	 * @return a GPiece instance
	 */
	public GPiece makeFakePiece(LoggableTree tree, 
			int pieceId,
			Collection <Step> stepsFromRoot, 
			TreeRadius_cmProvider mp, 
			GeoLogLogCategory prod,
			double botHeight_m, 
			double topHeight_m, 
			double discInterval_m) {
		
		if (discInterval_m <= 0.) {
			discInterval_m = GeoLogTreeData.DEFAULT_PRECISION_THICKNESS_m;
		}

		if (prod == null) {
			prod = new GeoLogLogCategory (0, "" + ((Tree) tree).getId (), TreeLoggerParameters.ANY_SPECIES, 1, true, 0, -1, -1, 0, true);
		}

		GeoLogTreeData td;
		if (tree instanceof GeoLogLoggableTree) {
			td = ((GeoLogLoggableTree) tree).getTreeData(stepsFromRoot, mp, getTreeLoggerParameters().isCrownExpansionFactorEnabled);
		} else {
			td = new GenericTreeData((Tree) tree, stepsFromRoot, mp, "unknown");
		}
		
		String treeStatus =  "Vivant";
		double trueTop = (topHeight_m <= botHeight_m) ? botHeight_m : topHeight_m;
		// TODO : clarify why we don't use td.getNumber (botHeight_m, topHeight_m) here !!
		double number = 1.0;
		GPiece piece = makePiece(tree, 
				pieceId, 
				0, 
				botHeight_m, 
				trueTop,
				treeStatus, 
				td.getCrownBaseHeight(), 
				number, 
				prod,
				0);
		td.setDiscsForThisPiece(piece, discInterval_m);
		return piece;
	}

//	public GeoLogTreeLoggerParameters createDefaultTreeLoggerParameters(Object... parameters) throws TreeLoggerException {
//		GeoLogTreeLoggerParameters params;
//		try {
//			String speciesName;
//			if (parameters != null && parameters[0] instanceof Speciable) {
//				speciesName = ((Speciable) parameters[0]).getSpecies().getName().trim().toLowerCase();
//			} else {
//				speciesName = getLoggableTrees().iterator().next().getSpeciesName().trim().toLowerCase();
//			}
//			if (speciesName.compareTo("oak") == 0) {
//				params = GeoLogTreeLoggerParameters.createTreeLoggerParameters(GeoLogSubLoggerType.FAGACEES_LOGGING);
//			} else if (speciesName.compareTo("beech") == 0) {
//				params = GeoLogTreeLoggerParameters.createTreeLoggerParameters(GeoLogSubLoggerType.FG_BEECH);
//			} else {
//				params = GeoLogTreeLoggerParameters.createTreeLoggerParameters(GeoLogSubLoggerType.SIMPLE_LOGGING);
//			}
////			System.out.println("speciesName = " + speciesName + ", TreeLoggerParameters selected = " + params.getLoggingName ());
//			return params;
//		}  catch (Exception e) {
//			try {
//				params = GeoLogTreeLoggerParameters.createTreeLoggerParameters(GeoLogSubLoggerType.SIMPLE_LOGGING);
//				return params;
//			} catch (Exception e2) {
//				Log.println(Log.ERROR, "GeoLog.createLogProcessingStarter", "Unable create the GeologStarter object", e);
//				throw new TreeLoggerException("Unable to create the default parameters for treelogger GeoLog");
//			}
//		}
//	}
	
	
	
	private Tree getOldest(Collection<? extends LoggableTree> trees) {
		Tree oldest = null;
		for (Object o : trees) {
			Tree t = (Tree) o;
			if (oldest == null || oldest.getAge () < t.getAge ()) {
				oldest = t;
			}
		}
		return oldest;
	}

	@Override
	protected void logThisTree(GeoLogLoggableTree loggableTree) {
		MemoryWatchDog.checkAvailableMemory();		// check if the memory is

		Tree t = (Tree) loggableTree;

		SpecialCase special = getSpecialCase (t);
		GeoLogTreeData td;
		if (loggableTree instanceof GeoLogLoggableTree) {
			td = ((GeoLogLoggableTree) loggableTree).getTreeData(stepsFromRoot, mp, getTreeLoggerParameters().isCrownExpansionFactorEnabled);
		} else {
			td = new GenericTreeData((Tree) loggableTree, stepsFromRoot, mp, "unknown");
		}
		
		// TODO ; add treeStatus to GeoLogTreeData ?
		String treeStatus = getTreeStatus (t, statusMaker);

		if (special != previousSpecial) {
			System.out.println ("Using " + td.getShortName () + " profiles");
			previousSpecial = special;
		}

		LoggingContext lc = td.getLoggingContext();

		// Table of pieces (and product priorities) for the current tree :
		Vector <Integer> productPriorities = new Vector <Integer> ();

		TicketDispenser pieceWithinTreeIdDispenser = new TicketDispenser();
		pieceWithinTreeIdDispenser.setCurrentValue(0);	// if we want the first piece to be 1

		List<GeoLogLogCategory> logCategories = getTreeLoggerParameters().getSpeciesLogCategories(loggableTree.getSpeciesName());
		if (logCategories == null || logCategories.isEmpty()) {
			return;
		}
		for (int p = 0; p < logCategories.size(); p++) {
			GeoLogLogCategory prod = logCategories.get(p);
			lc.addProduct (prod.getId ());

			// Table of cutting heigths for the current product :
			// (first entry = bottom)
			Vector <Double> heights = new Vector <Double> ();
			heights.add (lc.getHeight ());

			// Search for the cutting heights :
			while (prod.testLogValid(td) ) {
				//System.out.println ("Coupe " + prod.getId() + "=" + prod.getName() +" lg="+lc.getLength() ) ;
				lc.cutLog (prod.getId () );
				heights.add (lc.getHeight ());
			}

			int nbPiecesInProduct = heights.size () - 1;

			// Creation of the pieces :
			for (int n = 0; n < nbPiecesInProduct; n++) {
				double botHeight_m = heights.get (n);
				double topHeight_m = heights.get (n+1);

				double number = td.getCrownExpansionFactor(botHeight_m, topHeight_m);
				GPiece piece = makePiece(loggableTree,
						pieceIdDispenser.next(),
						pieceWithinTreeIdDispenser.next(),
						botHeight_m,
						topHeight_m,
						treeStatus,
						td.getCrownBaseHeight(),
						number,
						prod,
						p);
				td.setDiscsForThisPiece(piece, GeoLogTreeLoggerParameters.discInterval_m);
//				if (loggableTree.getTreeStatusPriorToLogging() == TreeStatusPriorToLogging.Windthrow) {
//					piece.setExpansionFactor(piece.getExpansionFactor() * .5);
//				}
				addWoodPiece(loggableTree, piece);

				productPriorities.add (p);
			}
		}

		Collection<WoodPiece> oPiecesSetForThisTree = getWoodPieces().get((LoggableTree) t);

		// Export the results as external text files :
		if (getTreeLoggerParameters().isExportResultsEnabled()) {
			for (WoodPiece piece : oPiecesSetForThisTree) {
				savePieceResults((GPiece) piece, ((GPiece) piece).getPriority(), td.getCrownBaseHeight());
			}
			RadialProfile profile130 = td.getRadialProfileAtThisHeight(1.3);
			saveTreeResults(t, treeStatus, lc, profile130, td);
		}

		if (verbose) {
			String textInfo = "" + t.getId ();
			for (int p = 0; p < logCategories.size(); p++) {
				GeoLogLogCategory prod = logCategories.get(p);
				textInfo += (p==0?" : ":", ")  + lc.getLogCount (prod.getId ())
						+ " " + GeoLogExport.left (prod.getName (), 3);
			}
			textInfo += " = " + lc.getLogCount ()  + " p.";
			textInfo += " [" + td.getShortName () + "]";
			System.out.println (textInfo);
		}

		if (isSaveMemoryEnabled()) {
//		if (isSaveMemoryEnabled() || !getTreeLoggerParameters().isRecordResultsEnabled()) {
			getWoodPieces().remove((LoggableTree) t);
			for (WoodPiece woodPiece : oPiecesSetForThisTree) {
				addWoodPiece(loggableTree, new GPieceLight((GPiece) woodPiece));	// if the results are not displayed on screen then the collection is cleared to save memory
			}
		}
	}

	private void savePieceResults(GPiece piece, int priority, double crownBaseHeight) {
		if (export != null) {
			try {
				export.savePieceResults(piece, priority, crownBaseHeight);
			} catch (IOException e) {
				System.out.println("Error while exporting! Closing the export.");
				export = null;
			}
		}
	}
	
	
	private void saveTreeResults(Tree t, String treeStatus, LoggingContext lc, RadialProfile profile130, GeoLogTreeData td) {
		if (export != null) {
			try {
				export.saveTreeResults(t, treeStatus, lc, profile130, td);
			} catch (IOException e) {
				System.out.println("Error while exporting! Closing the export.");
				export = null;
			}
		}
	}
	
	@Override
	public GeoLogTreeLoggerParameters createDefaultTreeLoggerParameters() {
		GeoLogTreeLoggerParameters params = new GeoLogTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}


	
	/**
	 * This method returns a first Map object whose keys are the steps and values are a second
	 * Map object. The keys of the second Map object are the TreeLogCategory and the values are
	 * the total volume. It returns null if the SetMap instance is empty.
	 * @return a Map<Step, Map<TreeLogCategory, Double>> instance
	 */
	public Map<Step, Map<TreeLogCategory, Double>> getVolumeByTreeLogCategory() {
		Map<Step, Map<TreeLogCategory, Double>> outputMap = new HashMap<Step, Map<TreeLogCategory, Double>>();
		
		Collection<WoodPiece> woodPiecesOfThisTree;
		if (!getWoodPieces().isEmpty()) {
			for (LoggableTree tree : getWoodPieces().keySet()) {
				Step stp = ((Tree) tree).getScene().getStep();
				Map<TreeLogCategory, Double> stepMap = outputMap.get(stp);
				if (stepMap == null) {
					stepMap = new HashMap<TreeLogCategory, Double>();
					outputMap.put(stp, stepMap);
				}
				
				woodPiecesOfThisTree = getWoodPieces().get(tree);
				
				TreeLogCategory logCategory;
				double volumeOfThisLogCategory;
				for (WoodPiece woodPiece : woodPiecesOfThisTree) {
					logCategory = woodPiece.getLogCategory();
					volumeOfThisLogCategory = woodPiece.getWeightedVolumeM3();
					if (stepMap.containsKey(logCategory)) {
						volumeOfThisLogCategory += stepMap.get(logCategory);
					} 
					stepMap.put(logCategory, volumeOfThisLogCategory);
				}
			}
		}
		return outputMap;
	}

	@Override
	public void activate () {}


	@Override
	public GeoLogLoggableTree getEligible (LoggableTree t) {
		if (t instanceof GeoLogLoggableTree) {
			return (GeoLogLoggableTree) t;
		}
		return null;
	}

	@Override
	public boolean isCompatibleWith(Object referent) {
		return referent instanceof GeoLogLoggableTree;
	}

}
