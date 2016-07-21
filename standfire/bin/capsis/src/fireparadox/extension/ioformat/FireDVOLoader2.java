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

package fireparadox.extension.ioformat;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Record;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex2d;
import jeeb.lib.util.Vertex3d;
import capsis.commongui.util.Tools;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.lib.fire.FiComputeStateProperties;
import capsis.lib.fire.fuelitem.FiSpecies;
import capsis.lib.spatial.GibbsPattern;
import capsis.util.StandRecordSet;
import fireparadox.gui.FmReadOrSetPopSpatializationDlg;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;
import fireparadox.model.plant.FmPlant;

/**
 * FireDVOLoader2 contains records description for tree input scene file in the
 * case when info is given (in the input file) in terms of cover, not a list of
 * trees or of number of trees
 * 
 * @author Ph. Dreyfus- september 2008
 */
public class FireDVOLoader2 extends StandRecordSet {

	static public final double MAX_REL_DIFF = 0.03; // MAXimum acceptable
													// RELative DIFFerence
													// between the cover value
													// obtained and the target
													// cover value

	// static public final double DECALAGE = 1000; // d�calage permettant de
	// distinguer entre les 2 types de crit�res de distance,

	static public final double MAX_POSITIONS_PAR_M2 = 15; // nombre maxi -
															// arbitraire - de
															// positions
	// d'arbres par m�
	// static public final double HEIGHT = -5;
	// static public final double CROWNBASEHEIGHT = -5;
	// static public final double CROWNDIAMETER = -5;
	// static public final double COVERPCT = -5;

	static {
		Translator.addBundle("capsis.extension.ioformat.FireDVOLoader2");
	}

	// Generic keyword record is described in superclass: key = value

	// Fire Paradox Terrain record is described here - a Rectangle with an
	// altitude
	@Import
	static public class TerrainRecord extends Record {
		public TerrainRecord() {
			super();
		}

		public TerrainRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public String name;
		public double cellWidth;
		public double altitude;
		public double xMin;
		public double yMin;
		public double xMax;
		public double yMax;
	}

	// Fire Paradox PolygonRecord record is described here
	@Import
	static public class PolygonRecord extends Record {
		public PolygonRecord() {
			super();
		}

		public PolygonRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public int id;
		public Collection vertices;
	}

	// Fire Paradox WITHOUT X Y Z, COVERAGE record is described here // PhD
	// 2008-09-04 - WITHOUT x, y, z, WITH eff >= it's a class of trees (not a
	// cohort, because eff indiv. trees will be added
	@Import
	static public class CoverNoxyTreeRecord extends Record {
		public CoverNoxyTreeRecord() {
			super();
		}

		public CoverNoxyTreeRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public int pop;
		public String speciesName; // ex: Pinus halepensis
		// ~ public String dbFuelId;
		public double height;
		public double crownBaseHeight;
		public double crownDiameter;
		// public double crownDiameterHeight;
		public double cover_pct;
	}

	// Fire Paradox population information record is described here // PhD
	// 2008-09-04
	@Import
	static public class PopRecord extends Record {
		public PopRecord() {
			super();
		}

		public PopRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public int pop;
		public double Gibbs;
		public double Radius;
		public double distPopi_A;
		public double distWeight_A;
		public double distPopu_B;
		public double distWeight_B;
		public double distPopi_B;
		public double distPopu_C;
		// # Gibbs : 0=al�atoire, 1000 -> +/- r�gulier, <0 -> agr�g�
		//
	}

	boolean fullDlg;

	double Gibbs1 = 0;
	double Radius1 = 0;
	double distPopi_1 = -5;
	double distWeight_1 = -5;

	double Gibbs2 = 0;
	double Radius2 = 0;
	double distPopi_2A = -5;
	double distWeight_2A = -5;
	double distPopu_2B = -5;
	double distWeight_2B = -5;
	double distPopi_2B = -5;
	double distPopu_2C = -5;

	double Gibbs3 = 0;
	double Radius3 = 0;
	double distPopi_3A = -5;
	double distWeight_3A = -5;
	double distPopu_3B = -5;
	double distWeight_3B = -5;
	double distPopi_3B = -5;
	double distPopu_3C = -5;

	double Gibbs4 = 0;
	double Radius4 = 0;
	double distPopi_4A = -5;
	double distWeight_4A = -5;
	double distPopu_4B = -5;
	double distWeight_4B = -5;
	double distPopi_4B = -5;
	double distPopu_4C = -5;

	boolean PopiDEPdePrevPops_2 = false;
	boolean PopiDEPdePrevPops_3 = false;
	boolean PopiDEPdePrevPops_4 = false;

	String Esp_1 = "";
	double Height_1;
	double CrownBaseHeight_1;
	double CrownDiameter_1;
	double CoverPct_1;

	String Esp_2 = "";
	double Height_2;
	double CrownBaseHeight_2;
	double CrownDiameter_2;
	double CoverPct_2;

	String Esp_3 = "";
	double Height_3;
	double CrownBaseHeight_3;
	double CrownDiameter_3;
	double CoverPct_3;

	String Esp_4 = "";
	double Height_4;
	double CrownBaseHeight_4;
	double CrownDiameter_4;
	double CoverPct_4;

	double maxX;
	double minX;
	double maxY;
	double minY;

	int maxId = 0;

	FmStand initStand; // The stand to generate

	private FmModel model;

	/**
	 * Phantom constructor. Only to ask for extension properties (authorName,
	 * version...).
	 */
	public FireDVOLoader2() {
	}

	// Official constructor
	// Format in Export mode needs a Stand in starter (then call save
	// (fileName))
	// Format in Import mode needs fileName in starter (then call load (GModel))
	public FireDVOLoader2(boolean _fullDlg) throws Exception {
		fullDlg = _fullDlg;

	}

	/**
	 * Direct constructor
	 */
	public FireDVOLoader2(String fileName, boolean _fullDlg) throws Exception {
		createRecordSet(fileName);
		fullDlg = _fullDlg;
	} // for direct use for Import

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof FmModel)) {
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FireDVOLoader2.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	public void createRecordSet(String fileName) throws Exception {
		super.createRecordSet(fileName);
	}

	/**
	 * RecordSet -> FiStand Implementation here. Was initialy described in
	 * FiModel.loadInitStand () To load a stand for another model, recognize
	 * real type of model : if (model instanceof FiModel) -> this code if (model
	 * instanceof BiduleModel) -> other code...
	 */
	public GScene load(GModel model) throws Exception {

		FmModel m = (FmModel) model;

		this.model = m; // fc-2.2.2015 particleNames

		FmInitialParameters settings = m.getSettings();

		// Initializations
		FmStand initStand = new FmStand(m);
		initStand.setSourceName(source); // generally fileName
		initStand.setDate(0);
		int maxId = 0; // fc4.0
		double standWidth = 0;
		double standHeight = 0;
		int nbPops = 0; // PhD
		Vertex3d standOrigin = new Vertex3d(0, 0, 0);

		Map speciesMap = new HashMap();
		int speciesId = 1;
		FmPlot plot = null;

		// Traite d'abord les infos sur les pops
		int nbPopRecord = 0;
		int nbCoverNoxyTreeRecord = 0;
		for (Iterator i = this.iterator(); i.hasNext();) {
			Record record = (Record) i.next();

			if (record instanceof FireDVOLoader2.TerrainRecord) {
				FireDVOLoader2.TerrainRecord r = (FireDVOLoader2.TerrainRecord) record; // cast
																						// to
																						// precise
																						// type
				standWidth = r.xMax - r.xMin;
				standHeight = r.yMax - r.yMin;
				standOrigin = new Vertex3d(r.xMin, r.yMin, r.altitude);
				Rectangle.Double rectangle = new Rectangle.Double(r.xMin, r.yMin, standWidth, standHeight);
				plot = new FmPlot(initStand, r.name, r.cellWidth, r.altitude, rectangle, settings);
				initStand.setPlot(plot);

				initStand.setOrigin(standOrigin);
				initStand.setXSize(standWidth);
				initStand.setYSize(standHeight);
				// initStand.setArea (initStand.getWidth () *
				// initStand.getHeight ()); // !? : et si ce n'est pas un
				// rectangle ? TBC ? dangereux ... et inutile me semble-t-il

				maxX = r.xMax;
				minX = r.xMin;
				maxY = r.yMax;
				minY = r.yMin;

				i.remove();

			} else if (record instanceof FireDVOLoader2.CoverNoxyTreeRecord) { // PhD
																				// 2008-09-16
				FireDVOLoader2.CoverNoxyTreeRecord covr = (FireDVOLoader2.CoverNoxyTreeRecord) record; // cast
																										// to
																										// precise
																										// type
				nbCoverNoxyTreeRecord++; // on ne fait que les compter pour
				// savoir combien de lignes �
				// remplir dans
				// FiReadOrSetPopSpatializationDlg
				// ... et stocker les valeurs pour le cas o� on ouvrirait
				// FiReadOrSetPopSpatializationDlg
				if (covr.pop == 1) {
					Esp_1 = covr.speciesName;
					Height_1 = covr.height;
					CrownBaseHeight_1 = covr.crownBaseHeight;
					CrownDiameter_1 = covr.crownDiameter;
					CoverPct_1 = covr.cover_pct;
				} else if (covr.pop == 2) {
					Esp_2 = covr.speciesName;
					Height_2 = covr.height;
					CrownBaseHeight_2 = covr.crownBaseHeight;
					CrownDiameter_2 = covr.crownDiameter;
					CoverPct_2 = covr.cover_pct;
				} else if (covr.pop == 3) {
					Esp_3 = covr.speciesName;
					Height_3 = covr.height;
					CrownBaseHeight_3 = covr.crownBaseHeight;
					CrownDiameter_3 = covr.crownDiameter;
					CoverPct_3 = covr.cover_pct;
				} else if (covr.pop == 4) {
					Esp_4 = covr.speciesName;
					Height_4 = covr.height;
					CrownBaseHeight_4 = covr.crownBaseHeight;
					CrownDiameter_4 = covr.crownDiameter;
					CoverPct_4 = covr.cover_pct;
				}

			} else if (record instanceof FireDVOLoader2.PopRecord) { // PhD
																		// 2008-09-05
				FireDVOLoader2.PopRecord r = (FireDVOLoader2.PopRecord) record; // cast
																				// to
																				// precise
																				// type
				nbPopRecord++;
				if (r.pop == 1) {
					Gibbs1 = r.Gibbs;
					Radius1 = r.Radius;

					distPopi_1 = r.distPopi_A;
					if (r.distWeight_A != -5)
						distWeight_1 = Math.max(1, r.distWeight_A);

					// if (r.distWeight_A < 1) {
					// JOptionPane.showMessageDialog (MainFrame.getInstance (),
					// Translator.swap ("Pop " + r.pop
					// +" : distWeight_A is < 1  => has been set to 1"),
					// Translator.swap ("Shared.warning"),
					// JOptionPane.WARNING_MESSAGE );
					// }
					// distWeight_1 = Math.max(1, r.distWeight_A);

				} else if (r.pop == 2) {
					Gibbs2 = r.Gibbs;
					Radius2 = r.Radius;

					distPopi_2A = r.distPopi_A;
					if (r.distWeight_A != -5)
						distWeight_2A = Math.max(1, r.distWeight_A);
					distPopu_2B = r.distPopu_B;
					if (r.distWeight_B != -5)
						distWeight_2B = Math.max(1, r.distWeight_B);
					distPopi_2B = r.distPopi_B;
					if (r.distPopu_C != -5)
						distPopu_2C = Math.abs(r.distPopu_C/* +DECALAGE */);

					// if (r.distWeight < 1) {
					// JOptionPane.showMessageDialog (MainFrame.getInstance (),
					// Translator.swap ("Pop " + r.pop
					// +" : distWeight is < 1  => has been set to 1"),
					// Translator.swap ("Shared.warning"),
					// JOptionPane.WARNING_MESSAGE );
					// }
					// distWeight_2 = Math.max(1, r.distWeight);

				} else if (r.pop == 3) {
					Gibbs3 = r.Gibbs;
					Radius3 = r.Radius;

					distPopi_3A = r.distPopi_A;
					if (r.distWeight_A != -5)
						distWeight_3A = Math.max(1, r.distWeight_A);
					distPopu_3B = r.distPopu_B;
					if (r.distWeight_B != -5)
						distWeight_3B = Math.max(1, r.distWeight_B);
					distPopi_3B = r.distPopi_B;
					if (r.distPopu_C != -5)
						distPopu_3C = Math.abs(r.distPopu_C/* +DECALAGE */);

					// if (r.distWeight < 1) {
					// JOptionPane.showMessageDialog (MainFrame.getInstance (),
					// Translator.swap ("Pop " + r.pop
					// +" : distWeight is < 1  => has been set to 1"),
					// Translator.swap ("Shared.warning"),
					// JOptionPane.WARNING_MESSAGE );
					// }
					// distWeight_3 = Math.max(1, r.distWeight);

				} else if (r.pop == 4) {
					Gibbs4 = r.Gibbs;
					Radius4 = r.Radius;

					distPopi_4A = r.distPopi_A;
					if (r.distWeight_A != -5)
						distWeight_4A = Math.max(1, r.distWeight_A);
					distPopu_4B = r.distPopu_B;
					if (r.distWeight_B != -5)
						distWeight_4B = Math.max(1, r.distWeight_B);
					distPopi_4B = r.distPopi_B;
					if (r.distPopu_C != -5)
						distPopu_4C = Math.abs(r.distPopu_C/* +DECALAGE */);

					// if (r.distWeight < 1) {
					// JOptionPane.showMessageDialog (MainFrame.getInstance (),
					// Translator.swap ("Pop " + r.pop
					// +" : distWeight is < 1  => has been set to 1"),
					// Translator.swap ("Shared.warning"),
					// JOptionPane.WARNING_MESSAGE );
					// }
					// distWeight_4 = Math.max(1, r.distWeight);
				}
				i.remove();
			}
		}

		try {
			Log.println("FULLDLG " + fullDlg);
			FmReadOrSetPopSpatializationDlg dlg = new FmReadOrSetPopSpatializationDlg(fullDlg, nbCoverNoxyTreeRecord,
					Esp_1, Height_1, CrownBaseHeight_1, CrownDiameter_1, CoverPct_1, Gibbs1, Radius1, distPopi_1,
					distWeight_1, Esp_2, Height_2, CrownBaseHeight_2, CrownDiameter_2, CoverPct_2, Gibbs2, Radius2,
					distPopi_2A, distWeight_2A, distPopu_2B, distWeight_2B, distPopi_2B, distPopu_2C, Esp_3, Height_3,
					CrownBaseHeight_3, CrownDiameter_3, CoverPct_3, Gibbs3, Radius3, distPopi_3A, distWeight_3A,
					distPopu_3B, distWeight_3B, distPopi_3B, distPopu_3C, Esp_4, Height_4, CrownBaseHeight_4,
					CrownDiameter_4, CoverPct_4, Gibbs4, Radius4, distPopi_4A, distWeight_4A, distPopu_4B,
					distWeight_4B, distPopi_4B, distPopu_4C);

			if (dlg.isValidDialog()) {

				if (fullDlg) {
					Gibbs1 = dlg.getGibbs1();
					Gibbs2 = dlg.getGibbs2();
					Gibbs3 = dlg.getGibbs3();
					Gibbs4 = dlg.getGibbs4();

					Radius1 = 0;
					Radius2 = 0;
					Radius3 = 0;
					Radius4 = 0;

					distPopi_1 = dlg.getDistPopi_1();
					distWeight_1 = dlg.getDistWeight_1();

					distPopi_2A = dlg.getDistPopi_2A();
					distWeight_2A = dlg.getDistWeight_2A();
					distPopu_2B = dlg.getDistPopu_2B();
					distWeight_2B = dlg.getDistWeight_2B();
					distPopi_2B = dlg.getDistPopi_2B();
					distPopu_2C = dlg.getDistPopu_2C();

					distPopi_3A = dlg.getDistPopi_3A();
					distWeight_3A = dlg.getDistWeight_3A();
					distPopu_3B = dlg.getDistPopu_3B();
					distWeight_3B = dlg.getDistWeight_3B();
					distPopi_3B = dlg.getDistPopi_3B();
					distPopu_3C = dlg.getDistPopu_3C();

					distPopi_4A = dlg.getDistPopi_4A();
					distWeight_4A = dlg.getDistWeight_4A();
					distPopu_4B = dlg.getDistPopu_4B();
					distWeight_4B = dlg.getDistWeight_4B();
					distPopi_4B = dlg.getDistPopi_4B();
					distPopu_4C = dlg.getDistPopu_4C();

					PopiDEPdePrevPops_2 = dlg.isPopiDEPdePrevPops_2();
					PopiDEPdePrevPops_3 = dlg.isPopiDEPdePrevPops_3();
					PopiDEPdePrevPops_4 = dlg.isPopiDEPdePrevPops_4();
				} else {

					Gibbs1 = dlg.getGibbs1();
					Gibbs2 = dlg.getGibbs2();
					Gibbs3 = dlg.getGibbs3();
					Gibbs4 = dlg.getGibbs4();

					Radius1 = 0;
					Radius2 = 0;
					Radius3 = 0;
					Radius4 = 0;

					PopiDEPdePrevPops_2 = dlg.isPopiDEPdePrevPops_2();
					PopiDEPdePrevPops_3 = dlg.isPopiDEPdePrevPops_3();
					PopiDEPdePrevPops_4 = dlg.isPopiDEPdePrevPops_4();

					distPopi_1 = dlg.getDistPopi_1();
					distWeight_1 = 5;

					distPopi_2A = -5; // si indep des previous pop, indep
					// d'elle-m�me �galement
					distWeight_2A = -5;
					distPopu_2B = distPopi_1;
					distWeight_2B = 5;
					distPopi_2B = distPopi_1;
					distPopu_2C = -5;

					distPopi_3A = -5;
					distWeight_3A = -5;
					distPopu_3B = distPopi_1;
					distWeight_3B = 5;
					distPopi_3B = distPopi_1;
					distPopu_3C = -5;

					distPopi_4A = -5;
					distWeight_4A = -5;
					distPopu_4B = distPopi_1;
					distWeight_4B = 5;
					distPopi_4B = distPopi_1;
					distPopu_4C = -5;
				}

				Log.println("Gibbs1 : " + Gibbs1);
				Log.println("Radius1 : " + Radius1);
				Log.println("distPopi_1 : " + distPopi_1);
				Log.println("distWeight_1 : " + distWeight_1);

				Log.println("Gibbs2 : " + Gibbs2);
				Log.println("Radius2 : " + Radius2);

				Log.println("PopiDEPdePrevPops_2 : " + PopiDEPdePrevPops_2);

				Log.println("distPopi_2A : " + distPopi_2A);
				Log.println("distWeight_2A : " + distWeight_2A);

				Log.println("distPopu_2B : " + distPopu_2B);
				Log.println("distWeight_2B : " + distWeight_2B);
				Log.println("distPopi_2B : " + distPopi_2B);

				Log.println("distPopu_2C : " + distPopu_2C);

				Log.println("Gibbs3 : " + Gibbs3);
				Log.println("Radius3 : " + Radius3);

				Log.println("PopiDEPdePrevPops_3 : " + PopiDEPdePrevPops_3);

				Log.println("distPopi_3A : " + distPopi_3A);
				Log.println("distWeight_3A : " + distWeight_3A);

				Log.println("distPopu_3B : " + distPopu_3B);
				Log.println("distWeight_3B : " + distWeight_3B);
				Log.println("distPopi_3B : " + distPopi_3B);

				Log.println("distPopu_3C : " + distPopu_3C);

				Log.println("Gibbs4 : " + Gibbs4);
				Log.println("Radius4 : " + Radius4);

				Log.println("PopiDEPdePrevPops_4 : " + PopiDEPdePrevPops_4);

				Log.println("distPopi_4A : " + distPopi_4A);
				Log.println("distWeight_4A : " + distWeight_4A);

				Log.println("distPopu_4B : " + distPopu_4B);
				Log.println("distWeight_4B : " + distWeight_4B);
				Log.println("distPopi_4B : " + distPopi_4B);

				Log.println("distPopu_4C : " + distPopu_4C);

				Height_1 = dlg.getHeight_1();
				CrownBaseHeight_1 = dlg.getCrownBaseHeight_1();
				CrownDiameter_1 = dlg.getCrownDiameter_1();
				CoverPct_1 = dlg.getCoverPct_1();

				Height_2 = dlg.getHeight_2();
				CrownBaseHeight_2 = dlg.getCrownBaseHeight_2();
				CrownDiameter_2 = dlg.getCrownDiameter_2();
				CoverPct_2 = dlg.getCoverPct_2();

				Height_3 = dlg.getHeight_3();
				CrownBaseHeight_3 = dlg.getCrownBaseHeight_3();
				CrownDiameter_3 = dlg.getCrownDiameter_3();
				CoverPct_3 = dlg.getCoverPct_3();

				Height_4 = dlg.getHeight_4();
				CrownBaseHeight_4 = dlg.getCrownBaseHeight_4();
				CrownDiameter_4 = dlg.getCrownDiameter_4();
				CoverPct_4 = dlg.getCoverPct_4();

			} else {
				return null;
			}

		} catch (Exception e) {
			MessageDialog.print(this, "FiReadOrSetPopSpatializationDlg.cproblemWithThisFile");
			MessageDialog.print(this, Translator.swap("FiReadOrSetPopSpatializationDlg.cproblemWithThisFile"));
		}

		// TODO get info from DB

		System.out.println("FireDVOLoader2.load () : # of records : " + size());
		for (Iterator i = this.iterator(); i.hasNext();) {
			Record record = (Record) i.next();

			// fc - 14.5.2007
			if (record instanceof FireDVOLoader2.PolygonRecord) {
				FireDVOLoader2.PolygonRecord r = (FireDVOLoader2.PolygonRecord) record; // cast
																						// to
																						// precise
																						// type

				if (plot == null) {
					throw new Exception("Can not process a Polygon record before a Terrain record");
				}

				// ~ FirePolygon p = new FirePolygon (r.vertices);
				Polygon p = new Polygon(new ArrayList<Vertex3d>(Tools.toVertex3dCollection(r.vertices)));
				plot.add(p);

			} else if (record instanceof FireDVOLoader2.CoverNoxyTreeRecord) { // PhD
																				// 2008-09-16
				FireDVOLoader2.CoverNoxyTreeRecord covr = (FireDVOLoader2.CoverNoxyTreeRecord) record; // cast
																										// to
																										// precise
																										// type
				FiSpecies s = (FiSpecies) m.getSpecies(covr.speciesName); // il
																			// 16/09/09

				speciesMap.put(covr.speciesName, s);

				if (covr.pop == 1) {
					setPop1(/* covr.pop, */initStand, CoverPct_1, CrownDiameter_1, Height_1, CrownBaseHeight_1,
							speciesMap, covr.speciesName, Gibbs1, Radius1, distPopi_1, /*
																						 * distPopu_1
																						 * ,
																						 */distWeight_1);
				} else if (covr.pop == 2) {
					setPopi(covr.pop, initStand, CoverPct_2, CrownDiameter_2, Height_2, CrownBaseHeight_2, speciesMap,
							covr.speciesName, Gibbs2, Radius2, distPopi_2A, distWeight_2A, distPopu_2B, distWeight_2B,
							distPopi_2B, distPopu_2C);
				} else if (covr.pop == 3) {
					setPopi(covr.pop, initStand, CoverPct_3, CrownDiameter_3, Height_3, CrownBaseHeight_3, speciesMap,
							covr.speciesName, Gibbs3, Radius3, distPopi_3A, distWeight_3A, distPopu_3B, distWeight_3B,
							distPopi_3B, distPopu_3C);
				} else if (covr.pop == 4) {
					setPopi(covr.pop, initStand, CoverPct_4, CrownDiameter_4, Height_4, CrownBaseHeight_4, speciesMap,
							covr.speciesName, Gibbs4, Radius4, distPopi_4A, distWeight_4A, distPopu_4B, distWeight_4B,
							distPopi_4B, distPopu_4C);
				}

			} else if (record instanceof FireDVOLoader2.KeyRecord) {
				FireDVOLoader2.KeyRecord r = (FireDVOLoader2.KeyRecord) record; // cast
																				// to
																				// precise
																				// type
				System.out.println(r); // Automatic toString ()

			} else {
				throw new Exception("Unrecognized record: " + record); // automatic
																		// toString
																		// ()
																		// (or
																		// null)
			}
		}

		if (initStand.getPlot() == null) {
			throw new Exception("missing Terrain in file, could not create plot; aborted");
		}

		// Init treeIdDispenser (to get new ids for regeneration)
		m.getTreeIdDispenser().setCurrentValue(maxId);

		// All trees added in stand : plot creation (not for all models)
		// ~ initStand.createPlot (m, 10);

		return initStand;
	}

	// //////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName() {
		return Translator.swap("FireDVOLoader2");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor() {
		return "F. de Coligny";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription() {
		return Translator.swap("FireDVOLoader2.description");
	}

	// //////////////////////////////////////////////// IOFormat stuff
	public boolean isImport() {
		return true;
	}

	public boolean isExport() {
		return false;
	} // PhD 2008-09-16, since the method createRecordSet (FiStand stand) is
		// "empty"

	// ---------------------------------------------------------------------------------------------------------------------------->
	// Generate location of trees listed without x, y, z in the input file >
	// ---------------------------------------------------------------------------------------------------------------------------->
	/**
	 * // Adding trees without X Y info read in input file. PhD 2008-09-04
	 */
	// --------------------------------------------------------------------------------->

	// ///////////////////////
	public void setPopi(int numpop, GScene initStand, double targetcoverpct, double crownDiameter, double height,
			double crownBaseHeight, Map speciesMap, String speciesName, double Gibbs, double Radius, double distPopi_A,
			double distWeight_A, double distPopu_B, double distWeight_B, double distPopi_B, double distPopu_C)
			throws Exception {
		// fc - 18.5.2009 - added throws Exception

		// targetcover from % to m2
		double targetcover = 0.01 * targetcoverpct * initStand.getArea();

		// -> setPopiINDEPdePrevPops
		// pop i ind�pendante des pr�cedentes
		if ((numpop == 2 && !PopiDEPdePrevPops_2) || (numpop == 3 && !PopiDEPdePrevPops_3)
				|| (numpop == 4 && !PopiDEPdePrevPops_4)) { // pop i
															// ind�pendante de
															// pop1
			setPopiINDEPdePrevPops(initStand, targetcover, crownDiameter, numpop, height, crownBaseHeight, speciesMap,
					speciesName, Gibbs, Radius, distPopi_A, distWeight_A);

			// -> setPopiDEPdePrevPops
		} else { // pop i D�pendante des pops pr�c�dentes (R�pulsion) ou de la
					// seule pop1 (Attraction)
			if (distPopu_C != -5 && distPopu_C >= 0) {
				setPopiDEPdePop1ATTRACT(initStand, targetcover, crownDiameter, numpop, height, crownBaseHeight,
						speciesMap, speciesName, Gibbs, Radius, distPopu_C);
			} else {
				setPopiDEPdePrevPops(initStand, targetcover, crownDiameter, numpop, height, crownBaseHeight,
						speciesMap, speciesName, Gibbs, Radius, distPopu_B, distWeight_B, distPopi_B);
			}
			// JOptionPane.showMessageDialog (MainFrame.getInstance (),
			// Translator.swap ("Pop "+numpop+" : termin�. "), Translator.swap
			// ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

		}
	}

	// /////////////////////////
	// ////// Optimisable ?
	// /////////////////////////
	public void setPop1(GScene initStand, double targetcoverpct, double crownDiameter, double height,
			double crownBaseHeight, Map speciesMap, String speciesName, double Gibbs1, double Radius1, double distPopi,
			double distWeight) throws Exception {
		// fc - 18.5.2009 - added throws Exception (F. Pimont added the same in
		// FmPlant 2nd constructor)

		// targetcover from % to m2
		double targetcover = 0.01 * targetcoverpct * initStand.getArea();

		String msg = "Pop 1 -  Gibbs -> x, y";
		StatusDispatcher.print(msg);

		double rad = 0.5 * (crownDiameter + distPopi);
		System.out.println("setPop1 " + "|" + "rad : " + rad);
		double area1tree = Math.PI * rad * rad;
		double cover = 0;
		int sumeff1 = (int) (targetcover / area1tree); // un nombre d'arbres
		// th�orique obtenu en
		// divisant le couvert
		// total � atteindre par
		// la projection de
		// houppier d'un arbre
		// (moyen, tous
		// identiques ... pour
		// l'instant)
		System.out.println("setPop1 " + "|" + "sumeff1 : " + sumeff1);

		// On calcule une collection de x y pour la pop1 :
		Collection vx1y1 = xyGibbs(sumeff1, Gibbs1, Radius1);

		FmStand firestand = (FmStand) initStand;
		// couvert effectif (tenant compte des chevauchements) :
		cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff1 + 1, vx1y1, crownDiameter)
				* firestand.getArea(); // calcCanCov ->
										// couvert en %,
		// getArea -> m�

		int kont = 1;
		// on augmente le nombre de positions d'arbre � simuler en fonction du
		// rapport entre le couvert � atteindre et celui r�sultant de la 1�re
		// collection de x y
		// ... jusqu'� obtenir un �cart maxi de 3%, mais au maximum en 25
		// boucles
		while ((cover / targetcover > 1 + MAX_REL_DIFF || cover / targetcover < 1 - MAX_REL_DIFF) && kont < 25) {
			kont++;
			System.out.println("setPop1 " + "|" + " kont GIBBS : " + kont);
			sumeff1 = (int) (sumeff1 * targetcover / cover);
			System.out.println("setPop1 " + "|" + " sumeff1= " + sumeff1 + " pour kont= " + kont);
			vx1y1 = xyGibbs(sumeff1, Gibbs1, Radius1);
			cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff1 + 1, vx1y1, crownDiameter)
					* firestand.getArea(); // calcCanCov
											// ->
											// couvert
											// en %,
											// getArea
			// -> m�
		}
		System.out
				.println("setPop1 " + "|" + " sumeff1= " + sumeff1 + " pour kont= " + kont + "    et cover= " + cover);

		// On attribue un x y � chaque record de la pop1 (et on cr�e l'arbre
		// dans la m�me boucle)
		if (distPopi <= 0) { // PhD 2009-01-29 // AUCUNE distance mini ou maxi
			int e = 0;
			for (Iterator i = vx1y1.iterator(); i.hasNext();) {
				Vertex2d v = (Vertex2d) i.next();

				e++;
				if (Math.IEEEremainder((double) e, 10d) == 0 || e <= 5 || e == sumeff1)
					msg = "Pop 1 : " + e + " / " + sumeff1;
				StatusDispatcher.print(msg);

				maxId++;
				FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
				if (crownBaseHeight < 0)
					crownBaseHeight = 0.67 * height;

				// int age = -1;
				int age = (int) height * 5; // PhD 2009-07-07
				FmPlant tree = new FmPlant(maxId, firestand, model, // fc-2.2.2015
						age, v.x, v.y, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, 1, // pop=1,moisture=0
						0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d, false); // liveTwigMoisture=0
				// adds tree in stand
				firestand.addTree(tree);
			}
		}

		else if (distPopi > 0) { // Distance mini entre houppiers de la Pop1

			double dist, distmin;
			double ecart, ecartmin;
			// double distminsous;
			boolean found = false;
			double r1x, r1y;

			int added = 0;
			int imparfaits = 0;
			Collection trees1 = new ArrayList();

			int sumeff1plus = (int) ((double) sumeff1 * distWeight); // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			System.out.println("setPop1 " + "|" + "  SUMEFF1 : " + sumeff1 + " distWeight : " + distWeight
					+ " sumeff1plus : " + sumeff1plus);

			// On g�n�re les x y "en sur-abondance" pour la pop1 :
			vx1y1 = xyGibbs(sumeff1plus, Gibbs1, Radius1);

			Collection vx1y1appro = new HashSet();

			// setPop1 - 1er PASSAGE : CONDITION respect�e ...
			for (int e = added; e < sumeff1; e++) {

				if (Math.IEEEremainder((double) e, 10d) == 0 || e <= 5 || e == sumeff1)
					msg = "Pop 1 : " + e + " / " + sumeff1;
				StatusDispatcher.print(msg);

				found = false;
				ecartmin = Double.MAX_VALUE;
				// distminsous = Double.MAX_VALUE;
				Vertex2d kept = null;
				for (Iterator ix1y1 = vx1y1.iterator(); ix1y1.hasNext();) { // BOUCLE
																			// SUR
																			// LES
																			// sumeff1plus
																			// POSITIONS
																			// DISPONIBLES
					Vertex2d v = (Vertex2d) ix1y1.next();

					distmin = Double.MAX_VALUE;
					// Log.println("v.x = "+v.x+ "    -   v.y = "+v.y);
					for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																			// sur
																			// les
																			// arbres
																			// de
																			// la
																			// Pop1
						FmPlant t1 = (FmPlant) i1.next();
						// distance entre cette position candidate et l'arbre de
						// la boucle, MOINS le rayon du houppier de cet arbre
						// <=> distance entre cette position candidate et le
						// bord du houppier de l'arbre de la boucle
						dist = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y) * (t1.getY() - v.y))
								- 0.5 * t1.getCrownDiameter();
						if (dist != 0 && dist < distmin)
							distmin = dist; // on recherche la distance mini
											// (<=> l'arbre le plus proche)
					}
					ecart = distmin - (0.5 * crownDiameter + distPopi); // on
																		// retranche
																		// le
																		// rayon
																		// du
																		// houppier
																		// d'un
																		// arbre
																		// qui
																		// serait
																		// sur
																		// la
																		// position
					// test�e,
					// + la
					// distance
					// mini
					// impos�e
					// entre
					// 2
					// houppiers
					if (ecart >= 0) { // Condition de distance respect�e : aucun
										// arbre n'est plus
						kept = new Vertex2d(v.x, v.y);
						found = true;
						ix1y1.remove();
						break;
					} else { // Condition de distance non atteinte : on vire
						// cette position pour ne pas la r�examiner pour
						// la recherche suivante d'une position qui
						// convienne
						vx1y1appro.add(v); // on la met d'abord dans vx1y1appro
						ix1y1.remove(); // puis on la vire de x1y1
					}
				} // FIN DE LA BOUCLE SUR LES POSITIONS DISPONIBLES
				if (found) { // forc�ment ...
					r1x = kept.x;
					r1y = kept.y;
					FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
					if (crownBaseHeight < 0)
						crownBaseHeight = 0.67 * height;
					maxId++;

					// int age = -1;
					int age = (int) height * 5; // PhD 2009-07-07
					FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
							age, r1x, r1y, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, 1, // pop=1,moisture=0
							0d, // liveMoisture=0
							0d, // deadMoisture=0
							0d, false); // liveTwigMoisture=0
					((FmStand) initStand).addTree(tree);
					trees1.add(tree);

					added++;
					if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5)
						System.out.println("setPop1 " + "|" + "  added 1er passage : " + added + " / sumeff1 : "
								+ sumeff1);
				}
			} // FIN DE LA BOUCLE SUR LES ARBRES � PLACER
			System.out.println("setPop1 " + "|" + "  added 1er passage : " + added + " / sumeff1 : " + sumeff1);

			// 2eme PASSAGE : Approx ...
			for (int e = added; e < sumeff1; e++) {

				if (Math.IEEEremainder((double) e, 10d) == 0 || e <= 5 || e == sumeff1)
					msg = "Pop 1 : " + e + " / " + sumeff1;
				StatusDispatcher.print(msg);

				found = false;
				ecartmin = Double.MAX_VALUE;
				// distminsous = Double.MAX_VALUE;
				Vertex2d kept = null;
				for (Iterator ix1y1appro = vx1y1appro.iterator(); ix1y1appro.hasNext();) { // BOUCLE
																							// SUR
																							// LES
																							// sumeff1plus
																							// POSITIONS
																							// DISPONIBLES
					Vertex2d v = (Vertex2d) ix1y1appro.next();

					distmin = Double.MAX_VALUE;
					// Log.println("v.x = "+v.x+ "    -   v.y = "+v.y);
					for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																			// sur
																			// les
																			// arbres
																			// de
																			// la
																			// Pop1
						FmPlant t1 = (FmPlant) i1.next();
						dist = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y) * (t1.getY() - v.y))
								- 0.5 * t1.getCrownDiameter();
						if (dist != 0 && dist < distmin)
							distmin = dist;
					}
					ecart = distmin - (0.5 * crownDiameter + distPopi);
					if (Math.abs(ecart) < Math.abs(ecartmin)) {
						ecartmin = ecart;
						kept = new Vertex2d(v.x, v.y);
					}
				} // FIN DE LA BOUCLE SUR LES POSITIONS DISPONIBLES

				r1x = kept.x;
				r1y = kept.y;
				if (!found) {
					imparfaits++;
					vx1y1.remove(kept);
				}

				FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
				if (crownBaseHeight < 0)
					crownBaseHeight = 0.67 * height;
				// double crownDiameterHeight = 0.5 * (height +
				// crownBaseHeight);
				maxId++;

				// int age = -1;
				int age = (int) height * 5; // PhD 2009-07-07
				FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
						age, r1x, r1y, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, 1, // pop=1,moisture=0
						0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d, false); // liveTwigMoisture=0
				((FmStand) initStand).addTree(tree);
				trees1.add(tree);

				added++;
				if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5)
					System.out.println("setPop1 " + "|" + "  added apr�s 2e passage : " + added + " / sumeff1 : "
							+ sumeff1);
			} // FIN DE LA BOUCLE SUR LES ARBRES � PLACER

			System.out.println("setPop1 " + "|" + "  added apr�s 2e passage : " + added + " / sumeff1 : " + sumeff1);

			System.out.println("setPop1 " + "|" + "imparfaits : " + imparfaits);
			if (imparfaits > 0)
				JOptionPane.showMessageDialog(
						MainFrame.getInstance(),
						Translator.swap("Pop 1 : " + imparfaits + " trees (among " + sumeff1
								+ ") does not respect the distance conditions." + "\n"
								+ "If necessary, increase distWeight value in input file for this pop" + "\n"
								+ "(but in this case, target cover value and/or Gibbs pattern" + "\n"
								+ "might be not reached)."), Translator.swap("Shared.warning"),
						JOptionPane.WARNING_MESSAGE);

		} // fin de distPopi > 0
		return;

	} // Fin de setPop1

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void setPopiDEPdePrevPops(GScene initStand, double targetcover, double crownDiameter, int numpop,
			double height, double crownBaseHeight, Map speciesMap, String speciesName, double Gibbs, double Radius,
			double distPopu_B, double distWeight_B, double distPopi_B) throws Exception {
		// fc - 18.5.2009 - added throws Exception

		// N.B. : targetcover has been converted to m2 (and is nomore in %)
		// before executing setPopi

		String msg = "Pop " + numpop + " - Gibbs -> x, y";
		StatusDispatcher.print(msg);

		// Build collection of trees in previous pops
		Collection trees = ((FmStand) initStand).getTrees();
		Collection trees1 = new ArrayList(); // previous pop trees
		// for (Iterator i0 = trees.iterator (); i0.hasNext ();) {
		// FmPlant tt = (FmPlant) i0.next ();
		// if (tt.getPop () == 1) trees1.add (tt);
		// }
		// D�sormais : R�pulsion par rapport aux Pops d�j� en place, et non plus
		// seulement par rapport � Pop1
		// N.B. 1 : on continue � "dire" trees1 et Pop1 dans l'�criture de la
		// m�thode
		// N.B. 2 : le cas "Attraction" est d�sormais trait� dans
		// setPopiDEPdePop1ATTRACT, o� l'attraction n'est prise en compte que
		// vis-�-vis de Pop1 (pas de ttes pops d�j� en place)
		int ipop = 0;
		while (ipop < numpop) {
			ipop++;
			for (Iterator i0 = trees.iterator(); i0.hasNext();) {
				FmPlant tt = (FmPlant) i0.next();
				if (tt.getPop() == ipop)
					trees1.add(tt);
			}
		}
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "  NB Trees : " + trees.size() + " NB Pop1 : "
				+ trees1.size());

		Collection treesi = new ArrayList(); // the new pop i trees, that we are
												// trying to create now

		// 1er TIRAGE des xi, yi
		double rad = 0.5 * crownDiameter;
		double area1tree = Math.PI * rad * rad;
		double cover = 0;
		int sumeff = (int) (targetcover / area1tree);
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " sumeff intial : " + sumeff);

		// On calcule x y pour la pop :
		Collection vxiyi = xyGibbs(sumeff, Gibbs, Radius);

		// BOUCLE de TIRAGE des xi, yi de mani�re � mieux approcher le couvert
		// (vou)lu
		FmStand firestand = (FmStand) initStand;
		cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff + 1, vxiyi, crownDiameter)
				* firestand.getArea(); // calcCanCov ->
										// couvert en %,
		// getArea -> m�
		int kont = 1;
		while ((cover / targetcover > 1 + MAX_REL_DIFF || cover / targetcover < 1 - MAX_REL_DIFF) && kont < 25) {
			System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " cover= " + cover + " pour kont= " + kont);
			kont++;
			System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " -  kont GIBBS : " + kont);
			sumeff = (int) (sumeff * targetcover / cover);
			vxiyi = xyGibbs(sumeff, Gibbs, Radius);
			cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff + 1, vxiyi, crownDiameter)
					* firestand.getArea(); // calcCanCov
											// ->
											// couvert
											// en %,
											// getArea
			// -> m�
		}
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " sumeff= " + sumeff + " pour kont= " + kont
				+ "   et cover= " + cover);

		// On est dans le cas o� la Pop est "concern�e" par les prev Pops
		double couvPrevPops_avecDistPopu_B = 0, couvPrevPops_sansDist = 0;
		// On calcule le nombre (sumeffplus) de points x y � simuler en vue de
		// la pop i :

		// Computing prev pops CANOPY COVER (%) ,
		// en incluant la distance de r�pulsion par rapport � la nouvelle pop i
		// � mettre en place :
		couvPrevPops_avecDistPopu_B = FiComputeStateProperties.calcCanCov(firestand, trees1, distPopu_B); // distPopu_B
																											// et
																											// non
																											// pas
																											// 0.5*distPopu_B,
																											// car
																											// on
																											// veut
																											// avoir
																											// la
																											// surface
																											// interdite
																											// aux
																											// TIGES
																											// de
																											// la
																											// pop
																											// i
		int sumeffplus = 0;
		sumeffplus = (int) ((double) sumeff / (1d - 0.01 * couvPrevPops_avecDistPopu_B) + 0.5); // +
																								// 0.5
																								// pour
																								// arrondir
		// (1d - 0.01*couvPrevPops_avecDistPopu_B) est la proportion non
		// couverte par les previous Pops rayon de r�plusion inclus
		couvPrevPops_sansDist = FiComputeStateProperties.calcCanCov((FmStand) initStand, trees1, 0); // uniquement
																										// �
																										// titre
																										// indicatif
																										// -
																										// non
																										// used
																										// dans
																										// les
		// calculs
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "  SUMEFF : " + sumeff
				+ " couvPrevPops_avecDistPopu_B : " + couvPrevPops_sansDist + " MAIS AVEC dist : "
				+ couvPrevPops_avecDistPopu_B + " sumeffplus : " + sumeffplus);

		sumeffplus = (int) ((double) sumeffplus * distWeight_B); // <<<<<<<<<<<<<<<<<<<<<<<<<<
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " distWeight_B : " + distWeight_B
				+ " sumeffplus : " + sumeffplus);

		// n'intervient gu�re si distance de r�pulsion > 0 et significative
		double positionsParM2 = (double) sumeffplus / (initStand.getArea() * (1d - 0.01 * couvPrevPops_avecDistPopu_B));
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " positionsParM2 : " + positionsParM2);
		if (positionsParM2 > MAX_POSITIONS_PAR_M2) { // on limite �
														// MAX_POSITIONS_PAR_M2
			// / m�
			sumeffplus = (int) (MAX_POSITIONS_PAR_M2 * (initStand.getArea() * (1d - 0.01 * couvPrevPops_avecDistPopu_B)));
			System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + " sumeffplus REDUIT pour "
					+ MAX_POSITIONS_PAR_M2 + " positions / m2 : " + sumeffplus);
		}

		if (distPopi_B != -5) {
			// Is it possible to place the pop i trees with this crownDiameter
			// and this distPopi_B within the remaining space let by prev pops ?
			// (test approximatif - sumeff d�j� corrig� par targetcover / cover,
			// voir plus haut)
			vxiyi = xyGibbs(sumeff, Gibbs, Radius);
			System.out.println("____For POP" + numpop + " sumeff =" + sumeff + " crownDiameter : " + crownDiameter
					+ " distPopi_B : " + distPopi_B);
			double couvNecessaryForPopi_avecDistPopi_B = FiComputeStateProperties.calcCanCov(firestand, sumeff + 1,
					vxiyi, crownDiameter + 0.5 * distPopi_B);
			double rapport = couvNecessaryForPopi_avecDistPopi_B / (1d - 0.01 * couvPrevPops_avecDistPopu_B);
			System.out.println("____For POP" + numpop + " Rapp =" + rapport + " (%couvNecessary "
					+ couvNecessaryForPopi_avecDistPopi_B + " +/- available : "
					+ (1d - 0.01 * couvPrevPops_avecDistPopu_B + " )"));
			if (rapport > 2) {
				JOptionPane
						.showMessageDialog(
								MainFrame.getInstance(),
								Translator
										.swap("Pop "
												+ numpop
												+ " : the specified  target %cover, crownDiameter and intra-pop spatial distribution"
												+ "\n"
												+ "should result in placing "
												+ sumeff
												+ " trees"
												+ "\n"
												+ "But after placing previous pops,"
												+ "\n"
												+ "there is not enough remaining space to place these trees with the specified inter-pop and intra-pop min dist :"
												+ "\n"
												+ "%cover necessary is : "
												+ (int) (100d * couvNecessaryForPopi_avecDistPopi_B + 0.5)
												+ " while available %cover is : "
												+ (int) (100d * (1d - 0.01 * couvPrevPops_avecDistPopu_B) + 0.5)
												+ "\n"
												+ "(necessary / available = "
												+ rapport
												+ " )\n"
												+ "Min dist (or target %cover) should be reduced; otherwise, computation will be very long and most trees will not respect min dist anyway."
												+ "\n\n"
												+ "If you want to stop tree generation, press Ctrl+C in Capsis 'terminal' to kill Capsis session, then restart Capsis. Sorry, but I still don't know any clean solution ..."
												+ "\n"), Translator.swap("Shared.warning"), JOptionPane.WARNING_MESSAGE);
			}

		}

		// On g�n�re les x y "en sur-abondance" pour la pop i :
		vxiyi = xyGibbs(sumeffplus, Gibbs, Radius);

		Collection vxiyiappro = new HashSet();

		// On ATTRIBUE un x y A CHACUN des sumeff arbres de la pop i
		double dist, distmin, dist_tiges;
		double ecart, ecartmin;
		double ecarti = Double.MAX_VALUE, ecartmoy;
		double distminsous;
		boolean found = false;

		int added = 0;
		int imparfaits = 0;

		// 1er passage, AVEC Respect de la Condition
		for (int e = added; e < sumeff; e++) { // ON ESSAIE DE TROUVER SUMEFF
												// POSITIONS QUI CONVIENNENT
												// parmi sumeffplus positions
			// tir�es

			if (Math.IEEEremainder((double) (added + 1), 10d) == 0 || added <= 5 || added == sumeff - 1)
				msg = "Pop " + numpop + " : " + (added + 1) + " / " + sumeff;
			StatusDispatcher.print(msg);

			found = false;
			ecartmin = Double.MAX_VALUE;
			distminsous = Double.MAX_VALUE;
			double rix = 0.5 * (maxX - minX); // ... pour le cas o� on ne trouve
												// aucune position xiyi qui
												// convienne ? Pbblt inutile !
												// TBC ??;
			double riy = 0.5 * (maxY - minY);

			Vertex2d kept = null;
			for (Iterator ixiyi = vxiyi.iterator(); ixiyi.hasNext();) { // BOUCLE
																		// SUR
																		// LES
																		// POSITIONS
																		// DISPONIBLES
				Vertex2d v = (Vertex2d) ixiyi.next();

				// distPopu est la distance mini � respecter entre houppiers
				distmin = Double.MAX_VALUE;
				for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																		// sur
																		// les
																		// arbres
																		// de la
																		// Pop1
					FmPlant t1 = (FmPlant) i1.next();
					dist = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y) * (t1.getY() - v.y))
							- 0.5 * t1.getCrownDiameter();
					if (dist < distmin)
						distmin = dist;
				}
				ecart = distmin - (0.5 * crownDiameter + distPopu_B);
				if (distPopi_B >= 0) {
					for (Iterator ii = treesi.iterator(); ii.hasNext();) { // boucle
																			// AUSSI
																			// sur
																			// les
																			// arbres
																			// de
																			// la
																			// Pop
																			// i
						FmPlant ti = (FmPlant) ii.next();
						dist = Math.sqrt((ti.getX() - v.x) * (ti.getX() - v.x) + (ti.getY() - v.y) * (ti.getY() - v.y))
								- 0.5 * ti.getCrownDiameter();
						if (dist < distmin)
							distmin = dist;
					}
					ecarti = distmin - (0.5 * crownDiameter + distPopi_B);
				}
				if (ecart >= 0 && ecarti >= 0) { // Conditions de distance
					// respect�e
					kept = new Vertex2d(v.x, v.y);
					found = true;
					ixiyi.remove();
					break;
				} else { // Condition de distance non atteinte : on vire cette
					// position pour ne pas la r�examiner pour la
					// recherche suivante de position qui convienne
					vxiyiappro.add(v); // on la met d'abord dans vxiyiappro
					ixiyi.remove(); // puis on la vire de xiyi
				}

			} // Recherche AVEC Respect strict de la Condition - FIN DE LA
				// BOUCLE SUR LES POSITIONS DISPONIBLES
			if (found) { // Forc�ment vrai
				rix = kept.x;
				riy = kept.y;
				FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
				if (crownBaseHeight < 0)
					crownBaseHeight = 0.67 * height;
				// double crownDiameterHeight = 0.5 * (height +
				// crownBaseHeight);
				maxId++;

				// int age = -1;
				int age = (int) height * 5; // PhD 2009-07-07
				FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
						age, rix, riy, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, numpop, 0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d, false); // liveTwigMoisture=0
				((FmStand) initStand).addTree(tree);
				treesi.add(tree);

				added++;
				if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5 || added == sumeff - 1)
					System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "  added 1er passage : " + added
							+ " / sumeff : " + sumeff);
			}
		} // FIN DE LA 1�re BOUCLE SUR LES ARBRES � PLACER
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "        added 1er PASSAGE (fini) : " + added
				+ " / sumeff : " + sumeff);

		// Approximatifs ...2�me passage (si on n'a pas pu plac� assez d'arbres
		// respectant parfaitement la condition) en acceptant les positions
		// imparfaites
		for (int e = added; e < sumeff; e++) { // ON ESSAIE DE TROUVER SUMEFF
												// POSITIONS QUI CONVIENNENT
												// parmi sumeffplus positions
			// tir�es (moins celles d�j�
			// utilis�es avec respect strict
			// de la condition)

			if (Math.IEEEremainder((double) (added + 1), 10d) == 0 || added <= 5)
				msg = "Pop " + numpop + " : " + (added + 1) + " / " + sumeff;
			StatusDispatcher.print(msg);

			found = false;
			ecartmin = Double.MAX_VALUE;
			distminsous = Double.MAX_VALUE;
			double rix = 0.5 * (maxX - minX); // ... pour le cas o� on ne trouve
												// aucune position vxiyiappro
												// qui convienne ? Pbblt inutile
												// ! TBC ??;
			double riy = 0.5 * (maxY - minY);

			Vertex2d kept = null;
			// Vertex2d kept = new Vertex2d (rix, riy);
			for (Iterator ixiyiappro = vxiyiappro.iterator(); ixiyiappro.hasNext();) { // BOUCLE
																						// SUR
																						// LES
																						// POSITIONS
																						// DISPONIBLES
				Vertex2d v = (Vertex2d) ixiyiappro.next();

				// distPopu_B est la distance mini � respecter entre houppiers
				distmin = Double.MAX_VALUE;
				for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																		// sur
																		// les
																		// arbres
																		// de la
																		// Pop1
					FmPlant t1 = (FmPlant) i1.next();
					dist = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y) * (t1.getY() - v.y))
							- 0.5 * t1.getCrownDiameter();
					if (dist < distmin)
						distmin = dist;
				} // -> on a trouv� l'arbre de pop1 qui est "le moins trop pr�s"
					// pour cette position et distmin est la distance entre la
					// position et le bord de son houppier
				ecart = distmin - (0.5 * crownDiameter + distPopu_B); // n�gatif
																		// puisque
																		// ce ne
																		// sont
																		// que
																		// des
																		// positions
																		// approx
																		// ; le
																		// houppier
																		// qu'on
																		// mettra
																		// sur
																		// cette
																		// position
																		// ne
				// s'�cartera
				// de t1
				// que
				// d'une
				// distance
				// moindre
				// que
				// distPopu_B
				if (distPopi_B >= 0) {
					for (Iterator ii = treesi.iterator(); ii.hasNext();) { // boucle
																			// AUSSI
																			// sur
																			// les
																			// arbres
																			// de
																			// la
																			// Pop
																			// i
						FmPlant ti = (FmPlant) ii.next();
						dist = Math.sqrt((ti.getX() - v.x) * (ti.getX() - v.x) + (ti.getY() - v.y) * (ti.getY() - v.y))
								- 0.5 * ti.getCrownDiameter();
						if (dist < distmin)
							distmin = dist;
					}
					ecarti = distmin - (0.5 * crownDiameter + distPopi_B);
				}
				if (distPopi_B >= 0) {
					ecartmoy = (ecart + ecarti) / 2d;
				} else {
					ecartmoy = ecart;
				}
				if (Math.abs(ecartmoy) < Math.abs(ecartmin)) { // il faut que ce
																// soit le moins
					// n�gatif
					// possible
					ecartmin = ecartmoy; // si l'�cart � la condition est
											// minimal, on stocke cette
					// position, avant de passer � la
					// suivante
					kept = new Vertex2d(v.x, v.y);
				}

			} // FIN DE LA 2�me BOUCLE (Approximatifs) SUR LES POSITIONS
				// DISPONIBLES

			rix = kept.x;
			riy = kept.y;
			if (!found) {
				imparfaits++;
				vxiyiappro.remove(kept);
			}

			FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
			if (crownBaseHeight < 0)
				crownBaseHeight = 0.67 * height;
			// double crownDiameterHeight = 0.5 * (height + crownBaseHeight);
			maxId++;

			// int age = -1;
			int age = (int) height * 5; // PhD 2009-07-07
			FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
					age, rix, riy, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, numpop, 0d, // liveMoisture=0
					0d, // deadMoisture=0
					0d, false); // liveTwigMoisture=0
			((FmStand) initStand).addTree(tree);
			treesi.add(tree);

			added++;
			if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5)
				System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "  added apr�s 2e passage : " + added
						+ " / sumeff : " + sumeff);
		} // FIN DE LA 2�me BOUCLE SUR LES ARBRES � PLACER
		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "        added apr�s 2e PASSAGE (fini): "
				+ added + " / sumeff : " + sumeff);

		System.out.println("setPopiDEPdePrevPops POP" + numpop + "|" + "imparfaits : " + imparfaits);
		if (imparfaits > 0)
			JOptionPane
					.showMessageDialog(
							MainFrame.getInstance(),
							Translator.swap("Pop " + numpop + " : " + imparfaits + " trees (among " + sumeff
									+ ") does not respect the distance conditions." + "\n"
									+ "If necessary, increase distWeight value in input file for this pop" + "\n"
									+ "(but in this case, target cover value and/or Gibbs pattern" + "\n"
									+ "might be not reached)."), Translator.swap("Shared.warning"),
							JOptionPane.WARNING_MESSAGE);
		return;

	} // fin de setPopiDEPdePrevPops

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void setPopiDEPdePop1ATTRACT(GScene initStand, double targetcover, double crownDiameter, int numpop,
			double height, double crownBaseHeight, Map speciesMap, String speciesName, double Gibbs, double Radius,
			double distPopu_C) throws Exception {
		// fc - 18.5.2009 - added throws Exception

		// N.B. : le cas "R�pulsion" est trait� dans setPopiDEPdePrevPops, o� la
		// r�pulsion est prise en compte vis-�-vis de ttes pops d�j� en place
		// (pas Pop1 uniquement)

		// N.B. : targetcover has been converted to m2 (and is nomore in %)
		// before executing setPopi

		String msg = "Pop " + numpop + " - Gibbs -> x, y";
		StatusDispatcher.print(msg);

		// Build collection of trees in pop1 (in fact, when dealing with pop2,
		// every tree already in the stand is in pop1 ! ... will be useful for
		// pop3, etc.
		Collection trees = ((FmStand) initStand).getTrees();
		Collection trees1 = new ArrayList(); // pop1 trees
		for (Iterator i0 = trees.iterator(); i0.hasNext();) {
			FmPlant tt = (FmPlant) i0.next();
			if (tt.getPop() == 1)
				trees1.add(tt);
		}
		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "  NB Trees : " + trees.size()
				+ " NB Pop1 : " + trees1.size());

		Collection treesi = new ArrayList(); // pop i trees

		// 1er TIRAGE des xi, yi
		double rad = 0.5 * crownDiameter;
		double area1tree = Math.PI * rad * rad;
		double cover = 0;
		int sumeff = (int) (targetcover / area1tree);
		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + " sumeff intial : " + sumeff);

		// On calcule x y pour la pop :
		Collection vxiyi = xyGibbs(sumeff, Gibbs, Radius);

		// BOUCLE de TIRAGE des xi, yi de mani�re � mieux approcher le couvert
		// (vou)lu
		FmStand firestand = (FmStand) initStand;
		cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff + 1, vxiyi, crownDiameter)
				* firestand.getArea(); // calcCanCov ->
										// couvert en %,
		// getArea -> m�
		int kont = 1;
		while ((cover / targetcover > 1 + MAX_REL_DIFF || cover / targetcover < 1 - MAX_REL_DIFF) && kont < 25) {
			System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + " cover= " + cover + " pour kont= "
					+ kont);
			kont++;
			System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + " -  kont GIBBS : " + kont);
			sumeff = (int) (sumeff * targetcover / cover);
			vxiyi = xyGibbs(sumeff, Gibbs, Radius);
			cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff + 1, vxiyi, crownDiameter)
					* firestand.getArea(); // calcCanCov
											// ->
											// couvert
											// en %,
											// getArea
			// -> m�
		}
		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + " sumeff= " + sumeff + " pour kont= " + kont
				+ "   et cover= " + cover);

		// On est dans le cas o� la Pop est "concern�e" par la Pop1
		double couvPop1 = 0;
		// Computing pop1 CANOPY COVER (%) ,
		couvPop1 = FiComputeStateProperties.calcCanCov((FmStand) initStand, trees1, 0);
		// On calcule le nombre de points x y � simuler en vue de la pop i :
		int sumeffplus = 0;
		sumeffplus = (int) ((double) sumeff / (0.01 * couvPop1) + 0.5); // + 0.5
																		// pour
																		// arrondir
		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "  SUMEFF : " + sumeff + " couvPop1 : "
				+ couvPop1 + " sumeffplus : " + sumeffplus);

		// On g�n�re les x y "en sur-abondance" pour la pop i :
		vxiyi = xyGibbs(sumeffplus, Gibbs, Radius);

		Collection vxiyiappro = new HashSet();

		// On ATTRIBUE un x y A CHACUN des sumeff arbres de la pop i
		double dist, distmin, dist_tiges;
		double ecart, ecartmin;
		double ecarti = Double.MAX_VALUE, ecartmoy;
		double distminsous;
		boolean found = false;

		int added = 0;
		int imparfaits = 0;

		// 1er passage, AVEC Respect de la Condition
		for (int e = added; e < sumeff; e++) { // ON ESSAIE DE TROUVER SUMEFF
												// POSITIONS QUI CONVIENNENT
												// parmi sumeffplus positions
			// tir�es

			if (Math.IEEEremainder((double) (added + 1), 10d) == 0 || added <= 5 || added == sumeff - 1)
				msg = "Pop " + numpop + " : " + (added + 1) + " / " + sumeff;
			StatusDispatcher.print(msg);

			found = false;
			ecartmin = Double.MAX_VALUE;
			distminsous = Double.MAX_VALUE;
			double rix = 0.5 * (maxX - minX); // ... pour le cas o� on ne trouve
												// aucune position xiyi qui
												// convienne ? Pbblt inutile !
												// TBC ??;
			double riy = 0.5 * (maxY - minY);

			Vertex2d kept = null;
			for (Iterator ixiyi = vxiyi.iterator(); ixiyi.hasNext();) { // BOUCLE
																		// SUR
																		// LES
																		// POSITIONS
																		// DISPONIBLES
				Vertex2d v = (Vertex2d) ixiyi.next();

				for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																		// sur
																		// les
																		// arbres
																		// de la
																		// Pop1
					FmPlant t1 = (FmPlant) i1.next();
					dist_tiges = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y)
							* (t1.getY() - v.y)); // distances entre les tiges
					if (dist_tiges < (0.5 * crownDiameter + Math.abs(distPopu_C))) { // la
																						// distance
																						// entre
																						// la
																						// TIGE
																						// 1
																						// (pas
																						// le
																						// houppier)
																						// et
																						// le
						// houppier i doit �tre <
						// abs(distPopu_C)
						kept = new Vertex2d(v.x, v.y);
						found = true;
						break; // on arr�te de passer en revue les arbres de
								// pop1
					}
				} // Fin de la boucle sur les arbres de la Pop1
				if (found) {
					ixiyi.remove();
					break; // ok pour cet arbre de pop i - on a vir� la position
					// utilis�e, et on sort de la boucle sur les
					// positions
				} else { // Condition de distance non atteinte : on vire cette
					// position pour ne pas la r�examiner pour la
					// recherche suivante de position qui convienne
					vxiyiappro.add(v); // on la copie d'abord dans vxiyiappro
					ixiyi.remove(); // puis on la vire de xiyi
				}
				// ... sinon, on passe � la position suivante
			} // Recherche AVEC Respect strict de la Condition - FIN DE LA
				// BOUCLE SUR LES POSITIONS DISPONIBLES
			if (found) { // Forc�ment vrai
				rix = kept.x;
				riy = kept.y;
				FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
				if (crownBaseHeight < 0)
					crownBaseHeight = 0.67 * height;
				// double crownDiameterHeight = 0.5 * (height +
				// crownBaseHeight);
				maxId++;

				// int age = -1;
				int age = (int) height * 5; // PhD 2009-07-07
				FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
						age, rix, riy, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, numpop, 0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d, false); // liveTwigMoisture=0

				((FmStand) initStand).addTree(tree);
				treesi.add(tree);

				added++;
				if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5 || added == sumeff - 1)
					System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "  added 1er passage : " + added
							+ " / sumeff : " + sumeff);
			}
		} // FIN DE LA 1�re BOUCLE SUR LES ARBRES � PLACER
		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "        added 1er PASSAGE (fini) : " + added
				+ " / sumeff : " + sumeff);

		// Approximatifs ...2�me passage (si on n'a pas pu plac� assez d'arbres
		// respectant parfaitement la condition) en acceptant les positions
		// imparfaites
		for (int e = added; e < sumeff; e++) { // ON ESSAIE DE TROUVER SUMEFF
												// POSITIONS QUI CONVIENNENT
												// parmi sumeffplus positions
			// tir�es (moins celles d�j�
			// utilis�es avec respect strict
			// de la condition)

			if (Math.IEEEremainder((double) (added + 1), 10d) == 0 || added <= 5)
				msg = "Pop " + numpop + " : " + (added + 1) + " / " + sumeff;
			StatusDispatcher.print(msg);

			found = false;
			ecartmin = Double.MAX_VALUE;
			distminsous = Double.MAX_VALUE;
			double rix = 0.5 * (maxX - minX); // ... pour le cas o� on ne trouve
												// aucune position vxiyiappro
												// qui convienne ? Pbblt inutile
												// ! TBC ??;
			double riy = 0.5 * (maxY - minY);

			Vertex2d kept = null;
			// Vertex2d kept = new Vertex2d (rix, riy);
			for (Iterator ixiyiappro = vxiyiappro.iterator(); ixiyiappro.hasNext();) { // BOUCLE
																						// SUR
																						// LES
																						// POSITIONS
																						// DISPONIBLES
				Vertex2d v = (Vertex2d) ixiyiappro.next();

				for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																		// sur
																		// les
																		// arbres
																		// de la
																		// Pop1
					FmPlant t1 = (FmPlant) i1.next();
					dist_tiges = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y)
							* (t1.getY() - v.y)); // distances entre les tiges
					// condition atteinte si : dist_tiges < (0.5*crownDiameter +
					// Math.abs(distPopu_C)) // la distance entre la TIGE 1 (pas
					// le houppier) et le houppier i doit �tre < abs(distPopu_C)
					ecart = dist_tiges - (0.5 * crownDiameter + Math.abs(distPopu_C)); // ecart
																						// est
																						// positif
																						// puisque
																						// c'est
																						// une
																						// position
																						// pour
																						// laquelle
																						// la
																						// condition
																						// n'est
																						// pas
																						// atteinte
					if (ecart < ecartmin) { // il faut que ecart soit le moins
											// fort possible
						kept = new Vertex2d(v.x, v.y);
						ecartmin = ecart;
					}
				} // Fin de la boucle sur les arbres de la Pop1

			} // FIN DE LA 2�me BOUCLE (Approximatifs) SUR LES POSITIONS
				// DISPONIBLES

			rix = kept.x;
			riy = kept.y;
			if (!found) {
				imparfaits++;
				vxiyiappro.remove(kept);
			}

			FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
			if (crownBaseHeight < 0)
				crownBaseHeight = 0.67 * height;
			// double crownDiameterHeight = 0.5 * (height + crownBaseHeight);
			maxId++;

			// int age = -1;
			int age = (int) height * 5; // PhD 2009-07-07
			FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
					age, rix, riy, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, numpop, 0d, // liveMoisture=0
					0d, // deadMoisture=0
					0d, false); // liveTwigMoisture=0

			((FmStand) initStand).addTree(tree);
			treesi.add(tree);

			added++;
			if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5)
				System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "  added apr�s 2e passage : " + added
						+ " / sumeff : " + sumeff);
		} // FIN DE LA 2�me BOUCLE SUR LES ARBRES � PLACER
		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "        added apr�s 2e PASSAGE (fini): "
				+ added + " / sumeff : " + sumeff);

		System.out.println("setPopiDEPdePop1ATTRACT POP" + numpop + "|" + "imparfaits : " + imparfaits);
		if (imparfaits > 0)
			JOptionPane
					.showMessageDialog(
							MainFrame.getInstance(),
							Translator.swap("Pop " + numpop + " : " + imparfaits + " trees (among " + sumeff
									+ ") does not respect the distance conditions." + "\n"
									+ "If necessary, increase distWeight value in input file for this pop" + "\n"
									+ "(but in this case, target cover value and/or Gibbs pattern" + "\n"
									+ "might be not reached)."), Translator.swap("Shared.warning"),
							JOptionPane.WARNING_MESSAGE);
		return;

	} // fin de setPopiDEPdePop1ATTRACT

	// �����������������������������������������������������������������������
	// �����������������������������������������������������������������������
	// �����������������������������������������������������������������������
	// �����������������������������������������������������������������������
	// �����������������������������������������������������������������������
	public void setPopiINDEPdePrevPops(GScene initStand, double targetcover, double crownDiameter, int numpop,
			double height, double crownBaseHeight, Map speciesMap, String speciesName, double Gibbs, double Radius,
			double distPopi, double distWeight) throws Exception {
		// fc - 18.5.2009 - added throws Exception

		// N.B. : targetcover has been converted to m2 (and is nomore in %)
		// before executing setPopi

		String msg = "Pop " + numpop + " - Gibbs -> x, y";
		StatusDispatcher.print(msg);

		// 1er TIRAGE des xi, yi
		double rad = 0.5 * crownDiameter;
		double area1tree = Math.PI * rad * rad;
		double cover = 0;
		int sumeff = (int) (targetcover / area1tree);

		// On calcule x y pour la pop :
		Collection vxiyi = xyGibbs(sumeff, Gibbs, Radius);

		// BOUCLE de TIRAGE des xi, yi de mani�re � mieux approcher le couvert
		// voulu pour cette pop
		FmStand firestand = (FmStand) initStand;
		cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff + 1, vxiyi, crownDiameter)
				* firestand.getArea(); // calcCanCov ->
										// couvert en %,
		// getArea -> m�
		int kont = 1;
		while ((cover / targetcover > 1 + MAX_REL_DIFF || cover / targetcover < 1 - MAX_REL_DIFF) && kont < 25) {
			kont++;
			System.out.println("setPopiINDEPdePrevPops POP" + numpop + "|" + "  kont GIBBS : " + kont);
			sumeff = (int) (sumeff * targetcover / cover);
			vxiyi = xyGibbs(sumeff, Gibbs, Radius);
			cover = 0.01 * FiComputeStateProperties.calcCanCov(firestand, sumeff + 1, vxiyi, crownDiameter)
					* firestand.getArea(); // calcCanCov
											// ->
											// couvert
											// en %,
											// getArea
			// -> m�
		}

		// On attribue un x y � chaque record de la pop (et on cr�e l'arbre dans
		// la m�me boucle)
		if (distPopi <= 0) { // PhD 2009-01-29 // AUCUNE distance mini ou maxi
			int e = 0;
			for (Iterator i = vxiyi.iterator(); i.hasNext();) {
				Vertex2d v = (Vertex2d) i.next();

				e++;
				if (Math.IEEEremainder((double) e, 10d) == 0 || e <= 5 || e == sumeff)
					msg = "Pop " + numpop + " : " + e + " / " + sumeff;
				StatusDispatcher.print(msg);

				maxId++;
				FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
				if (crownBaseHeight < 0)
					crownBaseHeight = 0.67 * height;

				// int age = -1;
				int age = (int) height * 5; // PhD 2009-07-07
				FmPlant tree = new FmPlant(maxId, firestand, model, // fc-2.2.2015
						age, v.x, v.y, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, numpop, 0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d, false); // liveTwigMoisture=0

				// adds tree in stand
				firestand.addTree(tree);
			}
		}

		// ��������������������������������������������������������������������������

		else if (distPopi > 0) { // Distance mini entre houppiers de la Popi

			double dist, distmin;
			double ecart, ecartmin;
			// double distminsous;
			boolean found = false;
			double rx, ry;

			int added = 0;
			int imparfaits = 0;
			Collection trees1 = new ArrayList();

			int sumeffplus = (int) ((double) sumeff * distWeight); // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			System.out.println("setPopiINDEPdePrevPops " + "|" + "  SUMEFF : " + sumeff + " distWeight : " + distWeight
					+ " sumeffplus : " + sumeffplus);

			// On g�n�re les x y "en sur-abondance" pour la pop1 :
			vxiyi = xyGibbs(sumeff, Gibbs, Radius);

			Collection vxyappro = new HashSet();

			// setPop1 - 1er PASSAGE : CONDITION respect�e ...
			for (int e = added; e < sumeff; e++) {

				if (Math.IEEEremainder((double) e, 10d) == 0 || e <= 5 || e == sumeff)
					msg = "Pop 1 : " + e + " / " + sumeff;
				StatusDispatcher.print(msg);

				found = false;
				ecartmin = Double.MAX_VALUE;
				// distminsous = Double.MAX_VALUE;
				Vertex2d kept = null;
				for (Iterator ixy = vxiyi.iterator(); ixy.hasNext();) { // BOUCLE
																		// SUR
																		// LES
																		// sumeffplus
																		// POSITIONS
																		// DISPONIBLES
					Vertex2d v = (Vertex2d) ixy.next();

					distmin = Double.MAX_VALUE;
					// Log.println("v.x = "+v.x+ "    -   v.y = "+v.y);
					for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																			// sur
																			// les
																			// arbres
																			// de
																			// la
																			// Pop1
						FmPlant t1 = (FmPlant) i1.next();
						// distance entre cette position candidate et l'arbre de
						// la boucle, MOINS le rayon du houppier de cet arbre
						dist = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y) * (t1.getY() - v.y))
								- 0.5 * t1.getCrownDiameter();
						if (dist != 0 && dist < distmin)
							distmin = dist; // on recherche la distance mini
											// (<=> l'arbre le plus proche)
					}
					ecart = distmin - (0.5 * crownDiameter + distPopi); // on
																		// retranche
																		// le
																		// rayon
																		// du
																		// houppier
																		// d'un
																		// arbre
																		// qui
																		// serait
																		// sur
																		// la
																		// position
					// test�e,
					// + la
					// distance
					// mini
					// impos�e
					// entre
					// 2
					// houppiers
					if (ecart >= 0) { // Condition de distance respect�e : aucun
										// arbre n'est plus
						kept = new Vertex2d(v.x, v.y);
						found = true;
						ixy.remove();
						break;
					} else { // Condition de distance non atteinte : on vire
						// cette position pour ne pas la r�examiner pour
						// la recherche suivante d'une position qui
						// convienne
						vxyappro.add(v); // on la met d'abord dans vx1y1appro
						ixy.remove(); // puis on la vire de x1y1
					}
				} // FIN DE LA BOUCLE SUR LES POSITIONS DISPONIBLES
				if (found) { // forc�ment ...
					rx = kept.x;
					ry = kept.y;
					FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
					if (crownBaseHeight < 0)
						crownBaseHeight = 0.67 * height;
					maxId++;

					// int age = -1;
					int age = (int) height * 5; // PhD 2009-07-07
					FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
							age, rx, ry, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, 1, 0d, // liveMoisture=0
							0d, // deadMoisture=0
							0d, false); // liveTwigMoisture=0

					((FmStand) initStand).addTree(tree);
					trees1.add(tree);

					added++;
					if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5)
						System.out.println("setPopiINDEPdePrevPops " + "|" + "  added 1er passage : " + added
								+ " / sumeff : " + sumeff);
				}
			} // FIN DE LA BOUCLE SUR LES ARBRES � PLACER
			System.out.println("setPopiINDEPdePrevPops " + "|" + "  added 1er passage : " + added + " / sumeff : "
					+ sumeff);

			// 2eme PASSAGE : Approx ...
			for (int e = added; e < sumeff; e++) {

				if (Math.IEEEremainder((double) e, 10d) == 0 || e <= 5 || e == sumeff)
					msg = "Pop " + numpop + " : " + e + " / " + sumeff;
				StatusDispatcher.print(msg);

				found = false;
				ecartmin = Double.MAX_VALUE;
				// distminsous = Double.MAX_VALUE;
				Vertex2d kept = null;
				for (Iterator ixyappro = vxyappro.iterator(); ixyappro.hasNext();) { // BOUCLE
																						// SUR
																						// LES
																						// sumeffplus
																						// POSITIONS
																						// DISPONIBLES
					Vertex2d v = (Vertex2d) ixyappro.next();

					distmin = Double.MAX_VALUE;
					// Log.println("v.x = "+v.x+ "    -   v.y = "+v.y);
					for (Iterator i1 = trees1.iterator(); i1.hasNext();) { // boucle
																			// sur
																			// les
																			// arbres
																			// de
																			// la
																			// Pop1
						FmPlant t1 = (FmPlant) i1.next();
						dist = Math.sqrt((t1.getX() - v.x) * (t1.getX() - v.x) + (t1.getY() - v.y) * (t1.getY() - v.y))
								- 0.5 * t1.getCrownDiameter();
						if (dist != 0 && dist < distmin)
							distmin = dist;
					}
					ecart = distmin - (0.5 * crownDiameter + distPopi);
					if (Math.abs(ecart) < Math.abs(ecartmin)) {
						ecartmin = ecart;
						kept = new Vertex2d(v.x, v.y);
					}
				} // FIN DE LA BOUCLE SUR LES POSITIONS DISPONIBLES

				rx = kept.x;
				ry = kept.y;
				if (!found) {
					imparfaits++;
					vxiyi.remove(kept);
				}

				FiSpecies s = (FiSpecies) speciesMap.get(speciesName);
				if (crownBaseHeight < 0)
					crownBaseHeight = 0.67 * height;
				maxId++;

				// int age = -1;
				int age = (int) height * 5; // PhD 2009-07-07
				FmPlant tree = new FmPlant(maxId, (FmStand) initStand, model, // fc-2.2.2015
						age, rx, ry, 0, "" + maxId, 0, height, crownBaseHeight, crownDiameter, s, 1, 0d, // liveMoisture=0
						0d, // deadMoisture=0
						0d, false); // liveTwigMoisture=0

				((FmStand) initStand).addTree(tree);
				trees1.add(tree);

				added++;
				if (Math.IEEEremainder((double) added, 25d) == 0 || added <= 5)
					System.out.println("setPopiINDEPdePrevPops " + "|" + "  added apr�s 2e passage : " + added
							+ " / sumeff : " + sumeff);
			} // FIN DE LA BOUCLE SUR LES ARBRES � PLACER

			System.out.println("setPopiINDEPdePrevPops " + "|" + "  added apr�s 2e passage : " + added + " / sumeff : "
					+ sumeff);

			System.out.println("setPopiINDEPdePrevPops " + "|" + "imparfaits : " + imparfaits);
			if (imparfaits > 0)
				JOptionPane.showMessageDialog(
						MainFrame.getInstance(),
						Translator.swap("Pop " + numpop + " : " + imparfaits + " trees (among " + sumeff
								+ ") does not respect the distance conditions." + "\n"
								+ "If necessary, increase distWeight value in input file for this pop" + "\n"
								+ "(but in this case, target cover value and/or Gibbs pattern" + "\n"
								+ "might be not reached)."), Translator.swap("Shared.warning"),
						JOptionPane.WARNING_MESSAGE);

		} // fin de distPopi > 0

		// ������������������������������������������������������������������������������

		return;
	} // Fin setPopiINDEPdePrevPops

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	public Collection xyGibbs(/* double [] xi, double [] yi, */int sumeff, double Gibbs, double Radius) {
		double[] xi = new double[sumeff + 1];
		double[] yi = new double[sumeff + 1];
		double[] intervalRadius = new double[2]; // 2, car on utilise le 1 mais
													// pas le 0
		double[] intervalCost = new double[2];
		if (Radius <= 0) {
			intervalRadius[1] = Math.sqrt((maxX - minX) * (maxY - minY) / (sumeff * Math.PI)) * 1.3; // 1.3
																										// :
																										// empirique
																										// (apr�s
																										// tests)
		} else {
			intervalRadius[1] = Radius;
		}
		int intervalNumber = 1;
		intervalCost[1] = Gibbs;
		int iterationNumber = (int) Math.floor(3000000 / sumeff); // => sans
																	// calcul
																	// trop long
																	// - 3000000
																	// :
																	// arbitraire
																	// ...;
		GibbsPattern.simulateXY(sumeff, xi, yi, minX + 0.05, maxX - 0.05, minY + 0.05, maxY - 0.05, 0.01,
				intervalNumber, intervalRadius, intervalCost, iterationNumber);

		Collection vxiyi = new HashSet();
		for (int i = 1; i <= sumeff; i++) {
			vxiyi.add(new Vertex2d(xi[i], yi[i]));
		}

		return vxiyi;
	}

}
