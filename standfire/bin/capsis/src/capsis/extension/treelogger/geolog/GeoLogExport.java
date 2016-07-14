
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece.Property;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.extension.treelogger.geolog.util.KnottyCoreProfile;
import capsis.extension.treelogger.geolog.util.NumericRecord;
import capsis.extension.treelogger.geolog.util.RecordMaker;
import capsis.extension.treelogger.geolog.util.RecordTableGroup;
import capsis.kernel.PathManager;

/**	GeoLogExport : save outputs of GeoLog as text files
*
*	@author F. Mothe - august 2006
*/

public class GeoLogExport {

	GeoLogTreeLoggerParameters starter;

	RecordMaker <GPiece> maker;

	private boolean filesOpen;
	private FileWriter pieceFile;
	private FileWriter treeFile;
	private String dxpFileName;
	private String axpFileName;
	private String onfFileName;

	private static NumberFormat nf3;
	private static NumberFormat nf2;
	private static NumberFormat nf1;

	private static String sep;

	// TEMPO :
	private boolean maxPrecision;

	private Vector <String> productNames;
	private Vector <String> statusNames;
	// statusMap.get (statusName) = range into statusNames
	private Map <String, Integer> statusMap;
	private DxPTable dxpTable;
	private AxPTable axpTable;
	private ONFTable onfTable;
	private int nbSelectedProducts;
	private int nbNumericVariables;

	/**	Inner classes derived from RecordTableGroup
	*/
	// TODO : extract ByProductTable from the class ?
	abstract class ByProductTable extends RecordTableGroup {
			String groupTitle;

			/**
			 * Constructor.
			 * @param groupTitle
			 */
			public ByProductTable (String groupTitle) {
				super (nbSelectedProducts, nbNumericVariables);
				this.groupTitle = groupTitle;
			}

			public void addRecord (GPiece piece, int productPriority, NumericRecord record) {
				int statusGroupId = getStatusGroupId (piece);

				// The record will be created if it does not exist :
				NumericRecord sum = getRecord (productPriority, statusGroupId);
				double weight = piece.getWithinTreeExpansionFactor();
				sum.cumulate (record, weight);
			}

			// Should return a value in the range 0 - 999 !
			abstract
			public int getGroupId (GPiece piece);

			// Overridable method :
			public String getGroupName (int groupId) {
				return  "" + groupId;
			}

			// Internally, we use a statusGroupId = 1000 * nStatus + groupId
			private int getStatusGroupId (GPiece piece) {
				String status = piece.getTreeStatus();
				Integer nStatus = statusMap.get (status);
				if (nStatus == null) {nStatus = 0;}
				return nStatus * 1000 + getGroupId (piece);
			}

			private String getGroupName_FromStatusGroupId (int statusGroupId) {
				return getGroupName (statusGroupId % 1000);
			}

			private String getStatusName (int statusGroupId) {
				int nStatus = statusGroupId / 1000;
				return statusNames.get (nStatus);
			}

			// Save results to fileName :
			public void save (String fileName) throws IOException {
				System.out.println ("Creating " + fileName);
				FileWriter out = new FileWriter (fileName);
				out.write (groupTitle
						+ sep + "status"
						+ sep + "prod"
						+ sep + "prio"
						+ sep + "nb"
				);
				// out.write (TestRecord.getNames (sep) + "\n");
				out.write (maker.makeTitle (sep) + "\n");

				// for (Integer groupId : getGroupsId ()) {
				Collection <Integer> sortedGroupIds =
						new TreeSet <Integer> (getGroupsId ());
				for (Integer statusGroupId : sortedGroupIds) {
					String groupName = getGroupName_FromStatusGroupId (
							statusGroupId);
					String statusName =getStatusName (statusGroupId);
					for (int p=0; p < getnbRecordsByGroup (); p++) {
						NumericRecord record = getExistingRecord (p, statusGroupId);
						double weight = record.getWeight ();
						if (weight > 0) {
							out.write (groupName
								+ sep + left (statusName, 7)	// max 7 for tab alignment
								+ sep + left (productNames.get (p), 7)	// max 7 for tab alignment
								+ sep + p
								+ sep + nf3.format (weight)
							);
							// System.out.println ("ByProductTable.save (record)");
							maker.finaliseSumRecord (record);

							out.write (record.getValues (sep, nf3) + "\n");
						}
					}
				}
				out.close ();
			}

		}

		// Table of dbh x product records :
		class DxPTable extends ByProductTable {
			public static final double DBH_INTERVAL_cm = 10;

			public DxPTable () {
				super ("dbh_cm");
			}

			public int getGroupId (GPiece piece) {
//				double dbh_cm = piece.treeInfo.treeDbh;
				double dbh_cm = ((Tree) piece.getTreeFromWhichComesThisPiece()).getDbh();
				return  Math.max ((int) (dbh_cm / DBH_INTERVAL_cm), 0);
			}

			public String getGroupName (int groupId) {
				return  nf2.format (groupId * DBH_INTERVAL_cm);
			}
		}

		// Table of age x product records :
		class AxPTable extends ByProductTable {
			public AxPTable () {
				super ("age");
			}

			public int getGroupId (GPiece piece) {
//				return piece.treeInfo.treeAge;
				return ((Tree) piece.getTreeFromWhichComesThisPiece()).getAge();
			}
		}

		// Table of ONF size classes x product records :
		class ONFTable extends ByProductTable {
			public static final int PE = 0;	// Perche <20 cm
			public static final int PB = 1;	// Petit bois [20, 30[
			public static final int BM = 2;	// Bois moyen [30, 50[
			public static final int GB = 3;	// Gros bois >=50
			// public static int TGB = 4;	// Tr�s gros bois >=70 (peu utilis�)

			public ONFTable () {
				super ("CatONF");
			}

			public int getGroupId (GPiece piece) {
				double dbh_cm = ((Tree) piece.getTreeFromWhichComesThisPiece()).getDbh();
				return (dbh_cm < 20) ? PE
					: (dbh_cm <30) ? PB
					: (dbh_cm <50) ? BM
					: GB
				;
			}

			public String getGroupName (int groupId) {
				String s;
				switch (groupId) {
				case PE : s = "C0-PE"; break;
				case PB : s = "C1-PB"; break;
				case BM : s = "C2-BM"; break;
				case GB : s = "C3-GB"; break;
				default : s = "?";
				}
				return  s;
			}
		}

	// End of inner classes

	/**	Constructor
	 *	may throw exception if something's wrong
	 */
	public GeoLogExport(LoggableTree tree,
			int jobId, boolean openFiles,
			GeoLogTreeLoggerParameters params,
			RecordMaker <GPiece> maker,
			Collection <String> statusNames) throws IOException {
		this.starter = params;
		this.filesOpen = false;
		this.pieceFile = null;
		this.treeFile = null;
		this.sep = "\t";
		this.maxPrecision = true;
		this.maker = maker;
		this.statusNames = new Vector <String> (statusNames);
			this.statusMap = new HashMap <String, Integer> ();
			int nStatus = 0;
			for (String s : statusNames) {
				statusMap.put (s, nStatus ++);
			}

		this.productNames = new Vector <String> ();
		List<GeoLogLogCategory> categories = starter.getSpeciesLogCategories(tree.getSpeciesName());
		this.nbSelectedProducts = categories.size();
		// this.nbNumericVariables = TestRecord.getNbNames ();
		this.nbNumericVariables = maker.getNbValues ();

		for (int p = 0; p < nbSelectedProducts; p++) {
			GeoLogLogCategory prod = categories.get(p);
			productNames.add (prod.getName ());
		}

		initNumberFormat ();

		if (openFiles) {
			openFiles (jobId);
		}

		dxpTable = new DxPTable ();
		axpTable = new AxPTable ();
		onfTable = new ONFTable ();
	}

	boolean openFiles (int jobId) throws IOException {
		if (!filesOpen) {
			String treeFileName = PathManager.getDir("tmp")
					+ File.separator + "GeoLog_Arb_" + jobId + ".txt";
			System.out.println ("Creating " + treeFileName);
			this.treeFile = new FileWriter (treeFileName);
			saveTreeResultsTitle ();

			String pieceFileName = PathManager.getDir("tmp")
					+ File.separator + "GeoLog_Bil_" + jobId + ".txt";
			System.out.println ("Creating " + pieceFileName);
			this.pieceFile = new FileWriter (pieceFileName);
			savePieceResultsTitle ();

			// Files open in ProductTable.save () :
			dxpFileName = PathManager.getDir("tmp")
					+ File.separator + "GeoLog_DxP_" + jobId + ".txt";
			axpFileName = PathManager.getDir("tmp")
					+ File.separator + "GeoLog_AxP_" + jobId + ".txt";
			onfFileName = PathManager.getDir("tmp")
					+ File.separator + "GeoLog_ONF_" + jobId + ".txt";

			// TODO : tests...
			filesOpen = true;
		}
		return filesOpen;
	}

	private void initNumberFormat () {
		int nbMaxDigit = maxPrecision ? 10 : 3;
		nf3 = NumberFormat.getInstance (Locale.US);	// decimal separator = "."
		nf3.setMaximumFractionDigits (nbMaxDigit);
		nf3.setGroupingUsed (false);
		nf2 = (NumberFormat) nf3.clone ();
		nf2.setMaximumFractionDigits (nbMaxDigit-1);
		nf1 = (NumberFormat) nf3.clone ();
		nf1.setMaximumFractionDigits (nbMaxDigit-2);
	}

	public static String left (String s, int n) {
		return s.substring (0, Math.max (0, Math.min (s.length (), n))) ;
	}

	public void saveProductTables () throws IOException {
		dxpTable.save (dxpFileName);	// sum
		axpTable.save (axpFileName);	// sum
		onfTable.save (onfFileName);	// sum
	}

	public void savePieceResults (GPiece piece, int productPriority,
			double crownBaseHeight_m_TEMPO) throws IOException {
		// if (!filesOpen) return;

		FileWriter out = pieceFile;
		GPieceDisc disc0 = piece.getBottomDisc();
		GPieceDisc disc1 = piece.getTopDisc();

		double h0_m = disc0.getHeight_m(); //PieceUtil.getHeight_m (disc0);
		double h1_m = disc1.getHeight_m(); // PieceUtil.getHeight_m (disc1);
		double lg_m = h1_m - h0_m;
		int crownPct = (int) (100 * piece.getCrownRatio(crownBaseHeight_m_TEMPO));

		out.write ( piece.getId ()
				+ sep + ((Tree) piece.getTreeFromWhichComesThisPiece()).getId()
				+ sep + piece.getRank()
				+ sep + ((Tree) piece.getTreeFromWhichComesThisPiece()).getAge()
				+ sep + nf3.format (((Tree) piece.getTreeFromWhichComesThisPiece()).getDbh())
				+ sep + left (piece.getTreeStatus(), 3)	// max 7 for tab alignment
				+ sep + left (piece.getLogCategory().getName(), 3)
				+ sep + left (piece.getOrigin(), 1)
				+ sep + crownPct
				+ sep + productPriority
				+ sep + nf2.format (piece.getWithinTreeExpansionFactor())
				+ sep + piece.getNumberOfDiscs()
				+ sep + nf3.format (lg_m)
				+ sep + nf3.format (h0_m)
				+ sep + nf3.format (h1_m)
				+ sep + nf3.format (disc0.getRadius_mm(true) * 2 / 10.0)	// overbark
				+ sep + nf3.format (disc1.getRadius_mm(true) * 2 / 10.0)	// overbark
				+ sep + nf3.format (piece.getProperty(Property.medianDiameter_cm))
				+ sep + nf3.format (disc0.getKnottyCoreRadius_mm() * 2 / 10.0)
				+ sep + nf3.format (disc1.getKnottyCoreRadius_mm() * 2 / 10.0)
				+ sep + nf3.format (disc0.getHeartWoodRadius_mm() * 2 / 10.0)
				+ sep + nf3.format (disc1.getHeartWoodRadius_mm() * 2 / 10.0)
				+ sep + nf3.format (disc0.getBarkWidth_mm())
				+ sep + nf3.format (disc1.getBarkWidth_mm())
				+ sep + nf3.format (disc0.getRingWidth_mm())
				+ sep + nf3.format (disc1.getRingWidth_mm())
		);

		NumericRecord record = maker.makeRecord (piece);
		out.write (record.getValues (sep, nf3) + "\n");

		dxpTable.addRecord (piece, productPriority, record);
		axpTable.addRecord (piece, productPriority, record);
		onfTable.addRecord (piece, productPriority, record);
	}

	private void savePieceResultsTitle () throws IOException {
		FileWriter out = pieceFile;
		out.write ( "#id"
				+ sep + "tree"
				+ sep + "intree"
				+ sep + "age"
				+ sep + "dbh"
				+ sep + "status"
				+ sep + "prod"
				+ sep + "orig"
				+ sep + "crown_%"
				+ sep + "prio"
				+ sep + "number"
				+ sep + "nbdsq"
				+ sep + "lg_m"		// length (m)
				+ sep + "h0_m"	// bottom height (m)
				+ sep + "h1_m"	// top height (m)
				+ sep + "d0_cm"	// bottom diameter over bark (cm)
				+ sep + "d1_cm"	// top diameter over bark (cm)
				+ sep + "dmed_cm"	// diameter over bark at mid-height (cm)
				+ sep + "dkc0_cm"	// bottom knotty core diameter (cm)
				+ sep + "dkc1_cm"	// top knotty core diameter (cm)
				+ sep + "dhw0_cm"	// bottom heartwood diameter (cm)
				+ sep + "dhw1_cm"	// top heartwood diameter (cm)
				+ sep + "bw0_mm"	// bottom bark width (mm)
				+ sep + "bw1_mm"	// top bark width (mm)
				+ sep + "rw0_mm"	// bottom ring width (mm)
				+ sep + "rw1_mm"	// top ring width (mm)
		);

		//out.write (TestRecord.getNames (sep) + "\n");
		out.write (maker.makeTitle (sep) + "\n");
	}

	public void saveTreeResults(Tree tree, String treeStatus,
			LoggingContext lc, RadialProfile profile130,
			GeoLogTreeData td) throws IOException {

		// if (!filesOpen) return;

		FileWriter out = treeFile;
		// Generic method :
		double crownBaseHeight_m;
		if (td.getKnotProfile() instanceof KnottyCoreProfile) {
			crownBaseHeight_m = ( (KnottyCoreProfile)
					td.getKnotProfile() ).getCrownBaseHeight_m (tree.getAge ());
		} else {
			crownBaseHeight_m = -1.;	// tree.getHeight ();
		}

		double db_cm = profile130.getOverBarkRadius_mm () * 2 / 10.0;	// = tree.getDbh ()
		double bw_mm = profile130.getBarkWidth_mm ();
		double dkc_cm = td.getKnotProfile().getRadius_mm (profile130) * 2 / 10.0;
		double dhw_cm = td.getHeartProfile().getRadius_mm (profile130) * 2 / 10.0;

		double nbThisTree = 1d;
		if (tree instanceof Numberable) {
			nbThisTree = ((Numberable) tree).getNumber();
		}

		out.write ( tree.getId ()
				+ sep + tree.getAge ()
				+ sep + left (treeStatus, 7)		// 7 for tab alignment
				+ sep + nf2.format (nbThisTree)
				+ sep + nf3.format (tree.getHeight ())
				+ sep + nf3.format (crownBaseHeight_m)
				+ sep + nf3.format (db_cm)		// diameter over bark at 1.30m (cm)
				+ sep + nf3.format (dkc_cm)		// knotty core diameter at 1.30 m (cm)
				+ sep + nf3.format (dhw_cm)		// heartwood diameter at 1.30 m (cm)
				+ sep + profile130.getNbWoodRings ()
				+ sep + td.getHeartProfile().getNbRings (profile130, false)	// outside
				+ sep + td.getJuvenileWoodProfile().getNbRings (profile130, true)	// inside
				+ sep + nf3.format (bw_mm)		// bark width at 1.30m (mm)
				+ sep + nf3.format (profile130.getRingWidth_mm ())
		);

		for (int n=0; n<GeoLogTreeData.NB_RANDOM_ATTRIBUTES; ++n) {
			out.write (sep + nf3.format (td.getRandomAttribute (n)));	// random attributes
		}

		List<GeoLogLogCategory> logCategories = starter.getSpeciesLogCategories(((LoggableTree) tree).getSpeciesName());
		
		out.write (sep + lc.getLogCount ());		// number of logs
		for (int p = 0; p < logCategories.size(); p++) {
			int idProd = logCategories.get(p).getId();
			out.write (sep + lc.getLogCount (idProd));
		}
		out.write ("\n");
	}

	private void saveTreeResultsTitle () throws IOException {
//		FileWriter out = treeFile;
//		out.write ( "#tree"
//				+ sep + "age"
//				+ sep + "status"
//				+ sep + "number"
//				+ sep + "h_m"			// height (m)
//				+ sep + "hcb_m"		// crown base height (m)
//				+ sep + "dbh_cm"		// diameter over bark at 1.30m (cm)
//				+ sep + "dkc_cm"		// knotty core diameter at 1.30 m (cm)
//				+ sep + "dhw_cm"		// heartwood diameter at 1.30 m (cm)
//				+ sep + "age130"		// number of wood rings at 1.30m
//				+ sep + "nbswr"		// number of sapwood rings at 1.30m
//				+ sep + "nbjwr"		// number of juvenile wood rings at 1.30m
//				+ sep + "bw_mm"		// bark width at 1.30m (mm)
//				+ sep + "rw_mm"		// ring width at 1.30m (mm)
//		);
//
//		for (int n=0; n<GeoLogTreeData.NB_RANDOM_ATTRIBUTES; ++n) {
//			out.write (sep + "attr" + (n+1));	// random attributes
//		}
//
//		out.write (sep + "logs");			// number of logs
//		for (int p=0; p<starter.getNumberOfSelectedProducts (); p++) {
//			String name = starter.getSelectedProduct (p).getName ();
//			out.write (sep + left (name, 7));
//		}
//		out.write ("\n");
	}

	public void close () throws IOException {
		if (filesOpen) {
			pieceFile.close ();
			treeFile.close ();
			filesOpen = false;
		}
	}

}
