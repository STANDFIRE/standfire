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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.Translator;
import lerfob.fagacees.FagaceesSpeciesProvider.FgSpecies;
import repicea.simulation.treelogger.TreeLoggerParameters;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.geolog.logcategories.FgBeechFurnitureLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgBeechPeelingLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgBeechSawingLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgBeechSlicingLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgOakFurnitureLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgOakLvlLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgOakSawingLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgOakSlicingLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FgOakStaveLogCategory;
import capsis.extension.treelogger.geolog.logcategories.FirewoodLogCategory;
import capsis.extension.treelogger.geolog.logcategories.GeoLogLogCategory;
import capsis.extension.treelogger.geolog.logcategories.ParticleBoardLogCategory;
import capsis.extension.treelogger.geolog.logcategories.StumpLogCategory;
import capsis.extension.treelogger.geolog.logcategories.TopLogCategory;
import capsis.extension.treelogger.geolog.species.FgOakDiscMassProvider;
import capsis.extension.treelogger.geolog.species.FgRecordMaker;
import capsis.extension.treelogger.geolog.util.PieceRingWidthRecord;
import capsis.extension.treelogger.geolog.util.PieceVolumeRecord;
import capsis.extension.treelogger.geolog.util.RecordMaker;

/**	
 * GeoLogStarter : abstract starter for GeoLog
 *	@author F. Mothe - january 2006
 */
public class GeoLogTreeLoggerParameters extends TreeLoggerParameters<GeoLogLogCategory> {

	static {
		Translator.addBundle("capsis.extension.treelogger.geolog.GeoLog");
	}

	public static enum GeoLogSubLoggerType {
		FAGACEES_LOGGING("FagaceesLogging"),
		FG_BEECH("FgBeech"), 
		SIMPLE_LOGGING("SimpleLogging");

		private String name;
		
		GeoLogSubLoggerType(String str) {
			name = str;
		}

		public int getIndex() {return this.ordinal();}
		public String getName() {return name;}
	}

	// Products id:
	public final static int STUMP = 0;
	public final static int SLICING = 1;
	public final static int STAVE = 2;
	public final static int FURNITURE = 3;
	public final static int SAWING = 4;
	public final static int LVL = 5;
	public final static int PARTICLE = 6;
	public final static int FIRE = 7;
	public final static int TOP = 8;
	
	public final static int PEELING = 3;

	
	public boolean pleaseOpenDialog;	// copyright fc - 2006


	
	private double precisionLength_m = 0.001;
	
	// Data shared by all child classes :
	private boolean m_bRecordResults;	// changed to private variable MFortin2010-01-14 
	private boolean m_bExportResults = true;	// changed to private variable MFortin2010-01-14
	

	// Maximal distance between discs:
	// (0 => 2 discs only)
	public static double discInterval_m;

	// Precision on log length for the logging rules (used by all the GeoLogProduct) :
	// public static double precisionLength_m = GeoLogProduct.precisionLength_m;

	// Seed of random generator :
	public static long randomSeed;	// -1 means new seed for each simulation
	
	private transient GeoLogTreeLoggerParametersDialog guiInterface;
	
	protected boolean isCrownExpansionFactorEnabled = true;

	
	/**
	 * Protected constructor.
	 */
	public GeoLogTreeLoggerParameters() {
		super(GeoLog.class);
		pleaseOpenDialog = false;	// can be changed to have the dialog opened in interactive mode
	}

	
	@Override
	public boolean isCorrect() {
		if (pleaseOpenDialog) {return false;}		// dialog explicitly required

		// TODO: call isCorrect() for each product
		/*
		if (minSlicingDiam_cm < 10) {return false;}
		if (minSlicingLength_m < 0.1) {return false;}
		if (minFireLength_m < 0.1) {return false;}
		*/

//		if (getNumberOfSelectedProducts () <= 0) {return false;}

		return true;	// if all the tests were passed
	}

	/**
	 * This method returns the precision length (m). 
	 * @return a double
	 */
	protected double getPrecisionLength_m() {
		return precisionLength_m;
	}

	/**
	 * This method sets the precision length (m).
	 * @param precisionLength_m a double 
	 */
	protected void setPrecisionLength_m(double precisionLength_m) {
		this.precisionLength_m = precisionLength_m;
	}

		
	//	Add the products by increasing priority order
	protected void setLogCategories() {
		List<GeoLogLogCategory> categories = new ArrayList<GeoLogLogCategory>(); 
		getLogCategories().put(FgSpecies.OAK.getName(), categories);
		categories.add(new StumpLogCategory(STUMP, FgSpecies.OAK.getName()));
		categories.add(new FgOakSlicingLogCategory(SLICING));
		categories.add(new FgOakStaveLogCategory(STAVE));
		categories.add(new FgOakFurnitureLogCategory(FURNITURE));
		categories.add(new FgOakSawingLogCategory(SAWING));
		categories.add(new FgOakLvlLogCategory(LVL));
		categories.add(new ParticleBoardLogCategory(PARTICLE, FgSpecies.OAK.getName()));
		categories.add(new FirewoodLogCategory(FIRE, FgSpecies.OAK.getName()));
		categories.add(new TopLogCategory(TOP, FgSpecies.OAK.getName()));

		categories = new ArrayList<GeoLogLogCategory>(); 
		getLogCategories().put(FgSpecies.BEECH.getName(), categories);
		categories.add(new StumpLogCategory(STUMP, FgSpecies.BEECH.getName()));
		categories.add(new FgBeechSlicingLogCategory(SLICING));
		categories.add(new FgBeechFurnitureLogCategory(FURNITURE));
		categories.add(new FgBeechPeelingLogCategory(PEELING));
		categories.add(new FgBeechSawingLogCategory(SAWING));
		categories.add(new ParticleBoardLogCategory(PARTICLE, FgSpecies.BEECH.getName()));
		categories.add(new FirewoodLogCategory(FIRE, FgSpecies.BEECH.getName()));
		categories.add(new TopLogCategory(TOP, FgSpecies.BEECH.getName()));
		
		
		addPriceModels ();
	}

	// TODO fix this : Cf FgBeechStarter et FgOakStarter
	//	Returns a factory of GPiece records for the export file
	//	(called by GeoLog)
	protected RecordMaker<GPiece> makeRecordMaker (GeoLog.SpecialCase oldestTreeSpecial) {
		RecordMaker <GPiece> maker;
		if (oldestTreeSpecial == GeoLog.SpecialCase.FG_OAK) {
			boolean withMasses = true;
			boolean withPrices = true;
			boolean withYields = true;
			maker = new FgRecordMaker(this, new FgOakDiscMassProvider(), withMasses, withPrices, withYields);
		} else {
			// TODO : for beech, use the density model
			// Save rw + volume :
			maker = RecordMaker.merge (new PieceRingWidthRecord.Maker(), new PieceVolumeRecord.Maker());
		}
		return maker;
	}

	// Overrided methods :

	//	Action performed when the "options" button is pressed :
	//	(called by GeoLogDialog)
	protected void optionAction() {
		// TODO :
		/*
			FagLogOptionDialog.Starter optionStarter = new FagLogOptionDialog.Starter ();

			// This dialog will ensure the starter is complete
			FagLogOptionDialog dlg = new FagLogOptionDialog (optionStarter);

			// dlg is valid if "ok" button and starter.isCorrect ()
			//if (!dlg.isValidDialog ()) {
			//	throw new NormalException (Translator.swap ("GeoLog.MsgAbort"));
			//}
			dlg.dispose ();
		*/
	}

	// Local methods :
	private void addPriceModel (int prodId, String className) {
		// FIXME set this right!!! MF
//		getSelectedProduct(prodId).priceModel = FgOakLogPriceModel.getModel (className);
	}

	private void addPriceModels () {
		addPriceModel(SLICING, "A");
		addPriceModel(STAVE, "A");
		addPriceModel(FURNITURE, "B");
		addPriceModel(SAWING, "C");
		addPriceModel(LVL, "D");
		addPriceModel(PARTICLE, "D");
		addPriceModel(FIRE, "F");
	}

	
	

//	/**
//	 * This method returns a particular product within the user-defined product list.
//	 * @param order the index of the product (an integer)
//	 * @return a GeoLogLogCategory instance that corresponds to the selected product
//	 * @throws InvalidParameterException if the index is out of bound
//	 */	
//	public GeoLogLogCategory getSelectedProduct(int order) {
//		GeoLogLogCategory selectedProduct = (GeoLogLogCategory) getData ().selectedProducts.get(order);
//		if (selectedProduct == null) {
//			throw new InvalidParameterException("The index of this product does not exist!");
//		}
//		return selectedProduct;
//	}

//	//	Returns the priorty range of the product
//	//	(returns -1 if the object is not found)
//	public int getPriority (GeoLogLogCategory prod) {
//		return getData ().selectedProducts.indexOf (prod);
//	}

	/**
	 * This method provides a default set of log categories
	 * This method is already called in the super constructor
	 */
	@Override
	protected void initializeDefaultLogCategories() {
		getLogCategories().clear();
		discInterval_m = GeoLogTreeData.DEFAULT_PRECISION_THICKNESS_m;
		setPrecisionLength_m(GeoLogTreeData.DEFAULT_PRECISION_LENGTH_m);
		randomSeed = 0;
		setLogCategories();
	}

	
	
	protected boolean isRecordResultsEnabled() {return m_bRecordResults;}		// added MF2010-01-14
	protected boolean isExportResultsEnabled() {return m_bExportResults;}		// added MF2010-01-14
	public void setRecordResultsEnabled(boolean bool) {m_bRecordResults = bool;}		// added MF2010-01-14
	protected void setExportResultsEnabled(boolean bool) {m_bExportResults = bool;}		// added MF2010-01-14

	
	
	@Override
	public GeoLogTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new GeoLogTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override 
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/**
	 * This method makes it possible to enable/disable the crown expansion factor. By default, it is enabled.
	 * @param bool a boolean
	 */
	public void setCrownExpansionFactorEnabled(boolean bool) {this.isCrownExpansionFactorEnabled = bool;}
	
	
	public static void main(String[] args) {
		GeoLogTreeLoggerParameters params = new GeoLogTreeLoggerParameters();
		params.initializeDefaultLogCategories ();
		params.showUI(null);
		params.showUI(null);
		System.exit(0);
	}
	
}
