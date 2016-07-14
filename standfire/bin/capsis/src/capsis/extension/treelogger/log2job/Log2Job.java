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

package capsis.extension.treelogger.log2job;

import java.security.InvalidParameterException;
import java.util.Collection;

import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.GPieceRing;
import capsis.extensiontype.TreeLoggerImpl;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**	A java implementation to log the trees
*	WithOUT Launcher
*
*	@author F. de Coligny - december 2005
*/
public class Log2Job extends TreeLogger<Log2JobTreeLoggerParameters, LoggableTree> 
					implements TreeLoggerImpl {
	
	static {
		Translator.addBundle("capsis.extension.treelogger.log2Job.Log2JobLabels");
	}
	
	static public final String AUTHOR = "Francois de Coligny";
	static public final String VERSION = "1.0";
	static public final String DESCRIPTION = "Log2Job.description";

	private int pieceId;
	
	/**
	 * Constructor for Gui mode
	 */
	public Log2Job() throws Exception {
		super();
	}

	/**
	 * 	Constructor in script mode
	 *	May throw exception if something's wrong
	 */
	public Log2Job (Log2JobTreeLoggerParameters params, Collection<LoggableTree> trees) throws Exception {
		super();
		setTreeLoggerParameters(params);
		init(trees);
		// run () has to be called by the script
	}
	


	@Override
	protected void priorToRunning() {
		pieceId = 0;
	}

	
	@Override
	public void setTreeLoggerParameters() {
		params = createDefaultTreeLoggerParameters();
		params.showUI(null);

		if (params.isParameterDialogCanceled()) {
			return;
		}

		if (!getTreeLoggerParameters().isCorrect()) {
			throw new InvalidParameterException("Incorrect parameters");
		}
		
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
	public Log2JobTreeLoggerParameters createDefaultTreeLoggerParameters() {
		Log2JobTreeLoggerParameters params = new Log2JobTreeLoggerParameters ();
		params.initializeDefaultLogCategories();
		return params;
	}

//	/** Call run after the constructor
//	*/
//	@Override
//	public void logTrees() throws Exception {
////		int treeCounter = 0;
//		try {
//			// getWoodPieces() is defined in AbstractTreeLogger:
////			int logId = 0;
////			double progressFactor = (double) 100d / getLoggableTrees().size();
//			for (LoggableTree loggableTree : getLoggableTrees()) {
//				Log2JobLogCategory logCategory = getTreeLoggerParameters().getLogCategoryList().get(0);
//				int nLogs = getTreeLoggerParameters().numberOfLogsInTheTree;
//				Tree t = (Tree) loggableTree;
//				double length_m = t.getHeight() / nLogs;
//				double smallEndDiameter_mm = t.getDbh() * 10;
//
//				for (int j = 1; j <= nLogs; j++) {
//					// Fred M - 24.3.2006 : compatible with GPiece changes
//					double h0_m = (j-1) * length_m;
//					GPiece piece = new GPiece(loggableTree,
//							logId++,	// fc - 1.3.2006
//							j,
//							1d,
//							length_m * 1000,	// pieceLength_mm
//							h0_m * 1000.,		// pieceY_mm
//							true,			// pieceWithBark
//							true,			// pieceWithPith
//							(byte) 1,			// numberOfRadius
//							"alive",
//							logCategory);
//					piece.setOrigin("pieceOrigin");
////					piece.setLogCategory(logCategory);
//
//					// Fred M - 8.3.2006 : minimal content of a GPiece = 2 discs :
//					int discId = 1 ;
//					double centreX_mm = 0;
//					double centreZ_mm = 0;
//					for (int d = 0; d < 2; d++) {
//						double discHeight_mm =  (h0_m + d * length_m) / 1000.0;
//						// Pith:
//						Vertex3d pith = new Vertex3d(centreX_mm, discHeight_mm, centreZ_mm);
//						piece.addPithPoint(pith);
//
//						// Disc:
//						GPieceDisc disc = new GPieceDisc (discId++, 
////								piece.getId(), 
//								discHeight_mm);
//						piece.addDisc (disc);
//						double r_mm = smallEndDiameter_mm / 2;
//						if (d==0)
//							r_mm *= 1.1;
//						// Bark :
//						disc.addRing (new GPieceRing (
//								1, disc.getId(), centreX_mm, centreZ_mm, r_mm) );
//						// Fake ring (because 3 rings are needed) :
//						disc.addRing (new GPieceRing (
//								2, disc.getId(), centreX_mm, centreZ_mm, r_mm/2) );
//						// Pith :
//						disc.addRing (new GPieceRing (
//								3, disc.getId(), centreX_mm, centreZ_mm, 0.0) );
//					}
//
//					//logs.add (piece);
//					getWoodPieces().addObject((LoggableTree) t, piece);
//				}
////				setProgress((int) (++treeCounter * progressFactor));
////			}
////		} catch (Exception e) {
////			Log.println(Log.ERROR, "Log2Job.run", "Error while logging", e);
////			throw e;
////		} 
//
//	}

	@Override
	protected void logThisTree(LoggableTree tree) {
		Log2JobLogCategory logCategory = getTreeLoggerParameters().getLogCategoryList().get(0);
		int nLogs = getTreeLoggerParameters().numberOfLogsInTheTree;
		Tree t = (Tree) tree;
		double length_m = t.getHeight() / nLogs;
		double smallEndDiameter_mm = t.getDbh() * 10;
		for (int j = 1; j <= nLogs; j++) {
			// Fred M - 24.3.2006 : compatible with GPiece changes
			double h0_m = (j-1) * length_m;
			GPiece piece = new GPiece(tree,
					pieceId++,	// fc - 1.3.2006
					j,
					1d,
					length_m * 1000,	// pieceLength_mm
					h0_m * 1000.,		// pieceY_mm
					true,			// pieceWithBark
					true,			// pieceWithPith
					(byte) 1,			// numberOfRadius
					"alive",
					logCategory);
			
			piece.setOrigin("pieceOrigin");

			// Fred M - 8.3.2006 : minimal content of a GPiece = 2 discs :
			int discId = 1 ;
			double centreX_mm = 0;
			double centreZ_mm = 0;
			for (int d = 0; d < 2; d++) {
				double discHeight_mm =  (h0_m + d * length_m) / 1000.0;
				// Pith:
				Vertex3d pith = new Vertex3d(centreX_mm, discHeight_mm, centreZ_mm);
				piece.addPithPoint(pith);

				// Disc:
				GPieceDisc disc = new GPieceDisc (discId++, discHeight_mm);
				piece.addDisc(disc);
				double r_mm = smallEndDiameter_mm * .5;
				if (d==0) {
					r_mm *= 1.1;
				}
				// Bark :
				disc.addRing(new GPieceRing(1, disc.getId(), centreX_mm, centreZ_mm, r_mm));
				// Fake ring (because 3 rings are needed) :
				disc.addRing(new GPieceRing(2, disc.getId(), centreX_mm, centreZ_mm, r_mm * .5));
				// Pith :
				disc.addRing(new GPieceRing(3, disc.getId(), centreX_mm, centreZ_mm, 0.0));
			}

			//logs.add (piece);
			addWoodPiece(tree, piece);
		}
		
	}

	@Override
	public void activate () {}

	@Override
	public LoggableTree getEligible (LoggableTree t) {
		return t;
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.treelogger.TreeLogger#isCompatibleWith(java.lang.Object)
	 */
	@Override
	public boolean isCompatibleWith(Object referent) {
		return false;
	}
	

}
