package capsis.lib.castanea;

import java.awt.Point;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex3d;
import capsis.commongui.util.Tools;
import capsis.defaulttype.SquareCell;
import capsis.defaulttype.SquareCellHolder;
import capsis.defaulttype.TreeListCell;
import dynaclim.model.DcTree;
import capsis.lib.castanea.FmYearlyResults;

/**
 * FmCell is a square cell. A fluxes level cell. It can be subdivided into other
 * cell instances.
 *
 * @author Hendrik Davi - march 2006
 */
public abstract class FmCell extends SquareCell implements SquareCellHolder, Serializable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see GPlot.clone () for
	// template)

	/**
	 * This class contains immutable instance variables for a logical FmCell.
	 */
	public static class Immutable extends SquareCell.Immutable {

		public int nLin; // relative to SquareCellHolder cell matrix
		public int nCol; // relative to SquareCellHolder cell matrix
		public double nestedCellWidth; // relative to SquareCellHolder, size (in
										// meters) of the nested square cells -
										// read in input file
		public double fmCellWidth; // optional, if -1, consider
									// mother.getCellWith ()
		public int[][] cellIdMatrix; // [line][column], line in [0, nLin[, col
										// in [O, nCol[
	}

	public double altitude;
	public double Xle;
	public double Yle;

	protected FmSpecies[] usedFmSpecies; // Castanea species in this cell

	protected double[][] speciesProportion;

	protected FmCanopy canopy;
	protected FmWood wood;
	protected FmSoil soil;

	// private double[] soilUsefulReserve;

	public double[] stoneContent;

	public double[] soilHeight;

	// fc+hd-28.2.2013
	public double[] litterHeight;
	public double[] topHeight;

	protected FmYearlyResults Yr;

	public double[] meanDbh;
	public double[] treeN; // double in case of average when castanea only
	public double[] meanAge;
	public double[] meanHeight; // fc+hd-28.2.2013 added this
	public double[] projectedAreaOfCanopy;
	public double[] G;
	public double[] TSUMBBcell;
	public double[] g1cell;
	public double[] nccell;
	public double[] LMAcell;
	public double[] coefraccell;
	public double[] GBVmincell;
	public double[] woodStopcell;
	public double[] CRBVcell;
	public double[] potsoiltowoodcell;
	public double[] rootshootcell;
	public int [] dateDeb;
	public double[] slopePotGsCell;

	public int ID;

	public int currentYear; // current year of simulation
	public int birthYear; // first year of simulation
	public int currentDay; // current day of simulation
	public int currentHour; // current hour of simulation


	/**
	 * Constructor for new logical FmCell. Initialization must be completed by
	 * the user model (Dynaclim, PDG...)
	 */
	public FmCell(SquareCellHolder mother, int id, int motherId, Vertex3d origin, int iGrid, int jGrid,
			double nestedCellWidth, double fmCellWidth, // if > 0, this cell
														// width
														// replaces
														// mother.getCellWidth
														// ()
			FmSettings settings, int nbSpecies) {

		super(mother, id, motherId, origin, iGrid, jGrid);

		getImmutable().nestedCellWidth = nestedCellWidth;
		getImmutable().fmCellWidth = fmCellWidth;

		int nbStrat = settings.nbCanopyLayers;

		speciesProportion = new double[nbSpecies][nbStrat];

		canopy = new FmCanopy(nbSpecies,this, settings);
		wood = new FmWood(nbSpecies);
		soil = new FmSoil();
		// soilUsefulReserve = new double[nbSpecies];
		soilHeight = new double[nbSpecies];

		meanDbh = new double[nbSpecies];
		treeN = new double[nbSpecies];
		meanAge = new double[nbSpecies];
		G = new double[nbSpecies];
		TSUMBBcell = new double[nbSpecies];
		g1cell = new double[nbSpecies];
		nccell = new double[nbSpecies];
		LMAcell = new double[nbSpecies];
		coefraccell = new double[nbSpecies];
		GBVmincell = new double[nbSpecies];
		woodStopcell = new double[nbSpecies];
		CRBVcell = new double[nbSpecies];
		potsoiltowoodcell = new double[nbSpecies];
		rootshootcell = new double[nbSpecies];
		dateDeb= new int[nbSpecies];
		slopePotGsCell=  new double[nbSpecies];
		projectedAreaOfCanopy = new double[nbSpecies];
		// basic creation
		Yr = new FmYearlyResults(this, settings, nbSpecies);

	}

	public FmSpecies[] getUsedFmSpecies() {
		return usedFmSpecies;
	}

	public void setUsedFmSpecies(FmSpecies[] usedFmSpecies) {
		this.usedFmSpecies = usedFmSpecies;
	}

	/**
	 * Initialize the cell after construction.
	 */
	abstract public void initialize(FmSettings settings);

	/**
	 * Update the flux level cell for one year wood, canopy and soil objects are
	 * updated by this method.
	 */
	abstract public void updateFluxModel(FmSettings settings);

	/**
	 * Returns tree or average tree height
	 */
	abstract public double getTreesHeight(FmSettings settings);

	/**
	 * Throws an exception if an inconsistency is found. To be called at the end
	 * of a growth period.
	 */
	public void checkState(FmSettings settings) throws Exception {
		if (canopy == null)
			throw new Exception("canopy == null");
		if (wood == null)
			throw new Exception("wood == null");
		if (soil == null)
			throw new Exception("soil == null");

		if (canopy.getLayers() == null || canopy.getLayers().isEmpty())
			throw new Exception("canopy.getLayers () == null || canopy.getLayers ().isEmpty ()");

		if (canopy.getCanopyWaterReserves() == null)
			throw new Exception("canopyWaterReserves == null");

		FmSpecies[] fmSpeciesList = usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];

		for (int sp = 0; sp < nSpecies; sp++) {
			if (canopy.getLAImax()[sp] <= 0)
				throw new Exception("LAImax[" + sp + "] <= 0");
			if (canopy.getWAI()[sp] <= 0)
				throw new Exception("WAI[" + sp + "] <= 0");
			if (canopy.getClumping()[sp] <= 0)
				throw new Exception("clumping[" + sp + "] <= 0");
			if (canopy.getNitrogen()[sp] <= 0)
				throw new Exception("nitrogen[" + sp + "] <= 0");

			if (wood.getBiomassOfTrunk()[sp] <= 0)
				throw new Exception("wood.getBiomassOfTrunk ()[" + sp + "] <= 0");
			if (wood.getBiomassOfAliveWood()[sp] <= 0)
				throw new Exception("wood.getBiomassOfAliveWood ()[" + sp + "] <= 0");
			if (wood.getBiomassOfBranch()[sp] <= 0)
				throw new Exception("wood.getBiomassOfBranch ()[" + sp + "] <= 0");
			if (wood.getBiomassOfCoarseRoot()[sp] <= 0)
				throw new Exception("wood.getBiomassOfCoarseRoot ()[" + sp + "] <= 0");
			if (wood.getBiomassOfFineRoot()[sp] <= 0)
				throw new Exception("wood.getBiomassOfFineRoot ()[" + sp + "] <= 0");
			if (wood.getBiomassOfReserves()[sp] <= 0)
				throw new Exception("wood.getBiomassOfReserves ()[" + sp + "] <= 0");
			// if (wood.getBiomassOfReservesMinimal ()[sp] <= 0) throw new
			// Exception
			// ("wood.getBiomassOfReservesMinimal ()["+sp+"] <= 0");

		}

		if (soil.getHeight() <= 0)
			throw new Exception("soil.getHeight () <= 0");
		if (soil.getUsefulReserve() <= 0)
			throw new Exception("soil.getUsefulReserve () <= 0");

		if (soil.getAlit() <= 0)
			throw new Exception("soil.getAlit () <= 0");
		if (soil.getAsoil() <= 0)
			throw new Exception("soil.getAsoil () <= 0");

		if (soil.getRlit() < 0)
			throw new Exception("soil.getRlit () < 0");
		if (soil.getRlitmin() < 0)
			throw new Exception("soil.getRlitmin () < 0");

		if (soil.getRlitfc() <= 0)
			throw new Exception("soil.getRlitfc () <= 0");
		if (soil.getRsol() <= 0)
			throw new Exception("soil.getRsol () <= 0");
		if (soil.getRsolfc() <= 0)
			throw new Exception("soil.getRsolfc () <= 0");
		if (soil.getRsolwilt() <= 0)
			throw new Exception("soil.getRsolwilt () <= 0");

		if (soil.getRtop() < 0)
			throw new Exception("soil.getRtop () < 0");

		if (soil.getRtopfc() <= 0)
			throw new Exception("soil.getRtopfc () <= 0");
		if (soil.getRtopwilt() <= 0)
			throw new Exception("soil.getRtopwilt () <= 0");
		if (soil.getRtopmin() <= 0)
			throw new Exception("soil.getRtopmin () <= 0");
		if (soil.getRsolmin() <= 0)
			throw new Exception("soil.getRsolmin () <= 0");

		if (soil.getREW() <= 0)
			throw new Exception("soil.getREW () <= 0");

		// private double stomatalControl;
		// private double stressCompteur;
		// private double stressLevel;

		// private Collection soilLayers;

	}

	/**
	 * If optional with was given to the constructor, return this width instead
	 * of the mother.getCellWidth ().
	 */
	public double getWidth() {
		if (getImmutable().fmCellWidth > 0) {
			return getImmutable().fmCellWidth;
		} else {
			return super.getWidth();
		}
	}

	public double[][] getSpeciesProportion() {
		return speciesProportion;
	}

	public FmCanopy getCanopy() {
		return canopy;
	}

	public FmWood getWood() {
		return wood;
	}

	public FmSoil getSoil() {
		return soil;
	}

	public double[] getTreeN() {
		return treeN;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getXle() {
		return Xle;
	}

	public double getYle() {
		return Yle;
	}

	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setTreeN(double[] treeN) {
		this.treeN = treeN;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public void setXle(double Xle) {
		this.Xle = Xle;
	}

	public void setYle(double Yle) {
		this.Yle = Yle;
	}

	public void setSpeciesProportion(double[][] speciesProportion) {
		this.speciesProportion = speciesProportion;
	}

	// public double[] getSoilUsefulReserve () {
	// return soilUsefulReserve;
	// }

	// public void setSoilUsefulReserve (double[] soilUsefulReserve) {
	// this.soilUsefulReserve = soilUsefulReserve;
	// }

	public double[] getSoilHeight() {
		return soilHeight;
	}

	public double[] getProjectedAreaOfCanopy() {
		return projectedAreaOfCanopy;
	}

	public double[] getStoneContent() {
		return stoneContent;
	}

	public void setSoilHeight(double[] soilHeight) {
		this.soilHeight = soilHeight;
	}

	public void setProjectedAreaOfCanopy(double[] projectedAreaOfCanopy) {
		this.projectedAreaOfCanopy = projectedAreaOfCanopy;
	}

	public void setStoneContent(double[] stoneContent) {
		this.stoneContent = stoneContent;
	}

	public double[] getLitterHeight() {
		return litterHeight;
	}

	public void setLitterHeight(double[] litterHeight) {
		this.litterHeight = litterHeight;
	}

	public double[] getTopHeight() {
		return topHeight;
	}

	public void setTopHeight(double[] topHeight) {
		this.topHeight = topHeight;
	}

	public FmYearlyResults getYr() {
		return Yr;
	}

	public void setYr(FmYearlyResults yr) {
		Yr = yr;
	}

	public double[] getMeanDbh() {
		return meanDbh;
	}

	public void setMeanDbh(double[] meanDbh) {
		this.meanDbh = meanDbh;
	}

	public double[] getMeanAge() {
		return meanAge;
	}

	public void setMeanAge(double[] meanAge) {
		this.meanAge = meanAge;
	}

	public double[] getTSUMBBcell() {
		return TSUMBBcell;
	}

	public void setTSUMBBcell(double[] TSUMBBcell) {
		this.TSUMBBcell = TSUMBBcell;
	}

	public void setTSUMBBcellOne(double TSUMBBcell) {
		this.TSUMBBcell[0] = TSUMBBcell;
	}

	public double[] getG1cell() {
		return g1cell;
	}

	public void setG1cell(double[] g1cell) {
		this.g1cell = g1cell;
	}

	public double[] getNccell() {
		return nccell;
	}

	public void setNccell(double[] nccell) {
		this.nccell = nccell;
	}

	public double[] getLMAcell() {
		return LMAcell;
	}

	public void setLMAcell(double[] LMAcell) {
		this.LMAcell = LMAcell;
	}

	public double[] getCoefraccell() {
		return coefraccell;
	}

	public void setCoefraccell(double[] coefraccell) {
		this.coefraccell = coefraccell;
	}

	public double[] getGBVmincell() {
		return GBVmincell;
	}

	public void setGBVmincell(double[] GBVmincell) {
		this.GBVmincell = GBVmincell;
	}

	public double[] getWoodStopcell() {
		return woodStopcell;
	}

	public void setWoodStopcell(double[] woodStopcell) {
		this.woodStopcell = woodStopcell;
	}

	public double[] getCRBVcell() {
		return CRBVcell;
	}

	public void setCRBVcell(double[] CRBVcell) {
		this.CRBVcell = CRBVcell;
	}

	public double[] getPotsoiltowoodcell() {
		return potsoiltowoodcell;
	}

	public void setPotsoiltowoodcell(double[] potsoiltowoodcell) {
		this.potsoiltowoodcell = potsoiltowoodcell;
	}

	public int[] getDateDeb() {
		return dateDeb;
	}

	public void setDateDeb(int[] dateDeb) {
		this.dateDeb = dateDeb;
	}

	public double[] getRootshootcell() {
		return rootshootcell;
	}

	public void setRootshootcell(double[] rootshootcell) {
		this.rootshootcell = rootshootcell;
	}
	public double[] getSlopePotGsCell() {
		return slopePotGsCell;
	}

	public void setSlopePotGsCell(double[] slopePotGsCell) {
		this.slopePotGsCell = slopePotGsCell;
	}


	public double[] getMeanHeight() {
		return meanHeight;
	}

	public void setMeanHeight(double[] meanHeight) {
		this.meanHeight = meanHeight;
	}

	public double[] getG() {
		return G;
	}

	public void setG(double[] g) {
		G = g;
	}

	// public void update (Collection<FmSpecies> fmSpeciesList) {
	// this.fmSpeciesList = fmSpeciesList;
	//
	// canopy = new FmCanopy (this);
	// soil = new FmSoil (this);
	// wood = new FmWood (this);
	//
	// }

	// used nonly with dynaclim to be removed
	/*
	 * public void oneYearEvolution (FmSettings settings, FmClimate climate,
	 * double latitude, double longitude, FmCell cell, int year) {
	 *
	 *
	 * FmSpecies[] fmSpeciesList = usedFmSpecies; int nSpecies =
	 * fmSpeciesList.length;
	 *
	 * // first: the intialization of the flux cell // important only when //
	 * coupling this.updateFluxModel (settings);
	 *
	 * FmModel fmModel = new FmModel (); //int numberOfDays= 366; // check the
	 * safran file... int numberOfDays = cell.getNumberOfDays (year);
	 * FmYearlyResults Yr = this.getYr();
	 *
	 * //FmYearlyResults Yr= new FmYearlyResults (cell, numberOfDays, settings,
	 * year, cell.getID());
	 *
	 * // FmCanopy canopy = cell.getCanopy (); // FmWood wood = cell.getWood ();
	 * // FmSoil soil = cell.getSoil ();
	 *
	 * FmClimate climateTreeCopy = climate.getClimateCopy (); double altitude=
	 * this.getAltitude();
	 *
	 * fmModel.yearlyFmSimulation (settings, climate, climateTreeCopy, this,
	 * latitude, longitude, Yr, altitude, year);
	 *
	 * double[] LAImaxNew = cell.getCanopy ().getLAImax (); double[] BSSmin =
	 * cell.getWood ().getBiomassOfReservesMinimal (); double[]
	 * biomassOfReserves = cell.getWood ().getBiomassOfReserves (); double[]
	 * biomassOfFineRoots = cell.getWood ().getBiomassOfFineRoot (); double[]
	 * biomassOfTrunk = cell.getWood ().getBiomassOfTrunk (); Log.println
	 * (settings.logPrefix+"yearlyResults", X+";"+Y+";"+altitude,
	 * +";"+LAImaxNew[0] + ";" + Yr.getYearlyCanopyPhotosynthesis (0) + ";" +
	 * Yr.getYearlyCanopyEvapoTranspiration (0) + ";" +
	 * Yr.getYearlySoilEvaporation () + ";" + Yr.getYearlyCanopyTranspiration
	 * (0) + ";" + Yr.getYearlyStressLevel () + ";" + Yr.getLateFrostNumber (0)
	 * + ";" + Yr.getYearlyWoodGrowth (0) + ";" + Yr.getYearlyFineRootsGrowth
	 * (0) + ";" + Yr.getYearlyRespiration (0) + ";" + BSSmin[0] + ";" +
	 * biomassOfReserves[0] + ";" + biomassOfFineRoots[0] + ";" +
	 * biomassOfTrunk[0] + ";" + Yr.getBudburstDate (0) + ";" +
	 * Yr.getDayOfEndFall (0) + ";" + Yr.getDayOfWoodStop (0) + ";" +
	 * Yr.getYearlyCanopyDelta13C (0) + ";" + year + ";" + Yr.getYearlyTmoy () +
	 * ";" + Yr.getYearlyTmax () + ";" + Yr.getYearlyTmin () + ";" +
	 * Yr.getYearlyRg () + ";" + Yr.getYearlyPRI ()+";"+Yr.getYearlyDrainage());
	 *
	 * }// end of one year evolution
	 */

	/**
	 * Clone method.
	 */
	public Object clone() {
		try {
			FmCell c = (FmCell) super.clone(); // calls protected Object
												// Object.clone () {}

			c.canopy = (FmCanopy) this.canopy.clone();
			c.wood = (FmWood) this.wood.clone();
			c.soil = (FmSoil) this.soil.clone();

			c.speciesProportion = AmapTools.getCopy(speciesProportion);

			return c;

		} catch (Exception e) {
			Log.println(Log.ERROR, "FmCell.clone ()", "Error while cloning", e);
			return null;
		}

	}

	/**
	 * Create an Immutable object whose class is declared at one level of the
	 * hierarchy. This is called only in constructor for new logical object in
	 * superclass. If an Immutable is declared in subclass, subclass must
	 * redefine this method (same body) to create an Immutable defined in
	 * subclass.
	 */
	protected void createImmutable() {
		immutable = new Immutable();
	}

	/**
	 * A convenient accessor, casts into the correct Immutable description.
	 */
	public Immutable getImmutable() {
		return (Immutable) immutable;
	}

	// ------------------------------------------------------- SquareCellHolder
	// interface
	/**
	 * From SquareCellHolder interface.
	 */
	// implemented in SquareCell - public GPlot getPlot ();

	/**
	 * From SquareCellHolder interface. The cell width for all SquareCells
	 * managed by this holder.
	 */
	public double getCellWidth() {
		return getImmutable().nestedCellWidth;
	}

	/**
	 * From SquareCellHolder interface.
	 */
	public void defineMatrix(int nLin, int nCol) {
		getImmutable().nLin = nLin;
		getImmutable().nCol = nCol;
		getImmutable().cellIdMatrix = new int[nLin][nCol];
	}

	/**
	 * From SquareCellHolder interface.
	 */
	public void setCell(int i, int j, SquareCell c) {
		getImmutable().cellIdMatrix[i][j] = c.getId();
	}

	/**
	 * From SquareCellHolder interface. Square accessor from coordinates in
	 * matrix : getCell [i, j].
	 */
	public SquareCell getCell(int i, int j) {
		int a = Tools.getAModuloB(i, getNLin());
		int b = Tools.getAModuloB(j, getNCol());
		return (SquareCell) getPlot().getCell(getImmutable().cellIdMatrix[a][b]);
	}

	/**
	 * From SquareCellHolder interface. Translation cell id -> [i, j].
	 */
	public Point getIJ(int id) {
		if (id < 1 || id > getNLin() * getNCol()) {
			Log.println(Log.WARNING, "FmCell.getIJ ()", "Request for incorrect id=" + id + ", should be in [" + 1
					+ ", " + getNLin() * getNCol() + "], FmCell is " + toString());
			return new Point(0, 0);
		}
		int l = (id - 1) / getNCol();
		int c = (id - 1) % getNCol();
		return new Point(l, c);
	}

	/**
	 * From SquareCellHolder interface. Number of columns of the cell matrix.
	 */
	public int getNCol() {
		return getImmutable().nCol;
	}

	/**
	 * From SquareCellHolder interface. Number of lines of the cell matrix.
	 */
	public int getNLin() {
		return getImmutable().nLin;
	}

	// ---------------------------------------

	public boolean isTreeLevel() {
		return false;
	}

	// ------------------------------ from CellInformation interface

	/**
	 * Return a String representation of this object.
	 */
	public String toString() {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);

		// Using a StringBuffer and append () is much more efficient than String
		// + String - fc
		StringBuffer b = new StringBuffer(super.toString());

		b.append(" id: ");
		b.append(nf.format(getId()));

		return b.toString();
	}

	// Trace
	//
	public String getTrace() {
		String s = "";
		try {
			s += "FmCell id=" + getId() + " cellIdMatrix\n";
			for (int i = 0; i < getNLin(); i++) {
				for (int j = 0; j < getNCol(); j++) {
					s += "\t" + getImmutable().cellIdMatrix[i][j];
				}
				s += " /\n";
			}
		} catch (Exception e) {
			s = e.toString();
		}
		return s;
	}

	public String bigString() {
		return toString();
	}

	// Only for debugging in inspectors
	// comment in normal use - fc - 24.2.2003
	//
	public int[][] getCellIdMatrix() {
		return getImmutable().cellIdMatrix;
	}

	// Needed for Presage integration, unused elsewhere and not yet implemented
	// here
	// fc - 28.6.2004
	public SquareCell getCell(double x, double y) {
		return null;
	}

	// updateFmSpeciesList only for dynaclim not castanea only
	// public Collection<FmSpecies> updateFmSpeciesList () {
	//
	// // System.out.println ("DcFLCell.updateFmSpeciesList for cell "+getId
	// // ());
	// if (fmSpeciesList == null) {
	// fmSpeciesList = new HashSet<FmSpecies> ();
	// } else { // no dupplicates
	// fmSpeciesList.clear ();
	// }
	//
	// for (Iterator k = getCells ().iterator (); k.hasNext ();) {
	// DcTLCell cell = (DcTLCell) k.next ();
	// for (Iterator i = cell.getTrees ().iterator (); i.hasNext ();) {
	// DcTree t = (DcTree) i.next ();
	// DcSpecies species = (DcSpecies) t.getSpecies ();
	// FmSpecies fms = species.getFmSpecies ();
	// fmSpeciesList.add (fms); // set: no dupplicates
	// }
	// }
	//
	// update (fmSpeciesList);
	//
	// return fmSpeciesList;
	// // System.out.println ("fmSpeciesList="+fmSpeciesList);
	// }

	public Collection getTrees() {
		if (isTreeLevel()) {
			if (trees == null) {
				return new Vector();
			}
			return trees;
		} else {
			Collection trees_ = new Vector();
			TreeListCell sqc;
			for (Iterator ic = getCells().iterator(); ic.hasNext();) {
				sqc = (TreeListCell) ic.next();
				DcTree t;
				for (Iterator it = sqc.getTrees().iterator(); it.hasNext();) {
					t = (DcTree) it.next();
					trees_.add(t);
				}
			}
			return trees_;
		}
	}

	// public FmStandSettings getStandSettings () {return standSettings;}
	// public void setStandSettings (FmStandSettings v) {standSettings = v;}

	// calculation proportion of each species in each layer delete
	// calculation of total LAI static requires all the parameters to be given
	public double getTotalLAI(double[] LAI, FmSettings settings) {
		FmSpecies[] fmSpeciesList = usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		double totalLAI = 0;
		for (int i = 0; i < nSpecies; i++) {
			totalLAI += LAI[i];
		}
		return totalLAI;
	}

	public double[] getWAIfromLAI(double[] LAI, FmSpecies[] fmSpeciesList) {
		int sp = 0;
		double[] WAI = new double[fmSpeciesList.length];
		FmSpecies species=fmSpeciesList[0];
		WAI[sp] = 0.1 * LAI[sp]; // to be improved
		sp = sp + 1;
		// System.out.println (WAI[sp]);
		return WAI;
	}

	// / smal method to get the sum of column in one array 1D
	public double sumArray(double[] array) {
		int k;
		double sum = 0;
		for (k = 0; k < array.length; k++) {
			sum = sum + array[k];
		}
		return sum;
	}

	public int getNumberOfDays(int year) {
		int numberOfDays;

		if ((year % 4 == 0 && year % 100 > 0) || year % 400 == 0) {
			numberOfDays = 366;
		} else {
			numberOfDays = 365;
		}
		return numberOfDays;
	}

	public double getProjectedAreaOfCanopy(double dbh, double slope, double intercept) {
		return Math.max(slope * dbh + intercept, 1);
	}



}
