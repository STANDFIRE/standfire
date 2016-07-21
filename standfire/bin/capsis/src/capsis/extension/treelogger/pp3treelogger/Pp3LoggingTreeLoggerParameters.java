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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.simulation.treelogger.TreeLoggerParameters;

//import capsis.extension.modeltool.woodqualityworkshop.Pp3Logging.products.*;

/**	A Starter for Pp3Logging
*
*	@author C. Meredieu + T. Labb� - march 2006
*/
public class Pp3LoggingTreeLoggerParameters extends TreeLoggerParameters<Pp3LoggingTreeLogCategory> {
	
	protected static final String MARITIME_PINE = "Pine";
	
	public boolean pleaseOpenDialog;	// copyright fc - 2006

	public static final double STUMP_HEIGHT = 0.65;
	public static final double TOP_GIRTH = 20;
	public static final double TOP1_GIRTH = 150;
	public static final double LOG1_LENGTH = 2.6;
	public static final double LOG2_LENGTH = 2.1;
	public static final boolean EXPORT_ASKED = true;
	public static final String EXPORT_FILE_NAME = "pp3logging.csv";

	public double topGirth;
	public double top1Girth;
	public double top2Girth;
	public double top3Girth;
	public double log1Length;
	public double log2Length;
	public double log3Length;

	public boolean exportAsked;
	public String exportFileName;

	// Products id:
	/*	public final static int STUMP = 1;
		public final static int SLICING = 2;
		public final static int STAVE = 3;
		public final static int FURNITURE = 4;
		public final static int SAWING = 5;
		public final static int LVL = 6;
		public final static int PARTICLE = 7;
		public final static int FIRE = 8;
		public final static int TOP = 9;

	//	selectedProducts[] contains the products selected by the user
	//	(the first one has the highest priority)
	private static Vector < FgProduct > selectedProducts;
	private static Map < Integer, FgProduct > products;
	public static boolean recordResults;
	public static boolean exportResults;

	// Maximal distance between discs:
	// (0 => 2 discs only)
	public static double discInterval_m;

	// Precision on log length for the logging rules (used by all the FgProducts) :
	// (private to force using setPrecisionLength)
	private static double precisionLength_m = FgProduct.precisionLength_m;*/

	//	initDone is set to true after the first call to setDefaultValues ()
	@SuppressWarnings ("unused")
	private boolean initDone = false;

	private transient Pp3LoggingTreeLoggerParametersDialog guiInterface;
	
	public Pp3LoggingTreeLoggerParameters () {
		super(Pp3Logging.class);
		try {
			initializeDefaultLogCategories();
		} catch (Exception e) {
			System.out.println ("Pp3TreeLoggerParameters.initializeDefaultLogCategories() : invalid type");
		}
//		setDefaultValues (false);

		pleaseOpenDialog = false;	// can be changed to have the dialog opened in interactive mode
	}

	/*public double getPrecisionLength_m () {
		return FgProduct.precisionLength_m;
	}

	public void setPrecisionLength_m (double precisionLength_m) {
		// TODO : verify if we really need this local copy (for serialisation)
		this.precisionLength_m = precisionLength_m;
		FgProduct.precisionLength_m = precisionLength_m;
	}*/

	/**	This method returns true if the current starter is correct
	*	a good execution of Pp3Logging.
	*/
	public boolean isCorrect () {
		if (pleaseOpenDialog) {return false;}		// dialog explicitly required

		// check the properties here
		// ex: top > top1 > top2 etc...

		/*
		if (minSlicingDiam_cm < 10) {return false;}
		if (minSlicingLength_m < 0.1) {return false;}
		if (minFireLength_m < 0.1) {return false;}
		*/

		//if (getNumberOfSelectedProducts () <= 0) {return false;}

		return true;	// if all the tests were passed
	}

	//	Returns the number of products selected by the user
	//	(may be 0)
	/*public int getNumberOfSelectedProducts () {
		return selectedProducts.size ();
	}

	//	Returns true if order is within the range of selected products
	public Boolean isSelectedProduct (int order) {
		return order>=0 && order<getNumberOfSelectedProducts () ;
	}

	//	Returns a selected product within the user list
	//	(may be null)
	public FgProduct getSelectedProduct (int order) {
		if ( !isSelectedProduct (order) ) {
			return null;
		}
		return selectedProducts.get (order);
	}

	//	Returns a product (may be unselected)
	public FgProduct getProductById (int id) {
		return products.get (id);
	}

	//	Returns the complete list of products
	public Map < Integer, FgProduct > getProductMap () {
		return products;
	}

	//	Returns the list of selected products
	public Vector < FgProduct > getSelectedProducts () {
		return selectedProducts;
	}

	//	Returns the priorty range of the product
	//	(returns -1 if the object is not found)
	public int getPriority (FgProduct prod) {
		return selectedProducts.indexOf (prod);
	}

	//	Called by Pp3LoggingDialog
	public void setSelectedProducts ( Vector < FgProduct > selectedProducts) {
		this.selectedProducts = selectedProducts;
	}*/

	//	Create the default list of products and priorities
//	public void setDefaultValues (boolean force) {
//		if (!initDone || force) {
//
//			if (!initDone) {
//
//				topGirth = TOP_GIRTH;
//				top1Girth = TOP1_GIRTH;
//				top2Girth = 0;
//				top3Girth = 0;
//				log1Length = LOG1_LENGTH;
//				log2Length = LOG2_LENGTH;
//				log3Length = 0;
//
//				exportAsked = EXPORT_ASKED;
//				exportFileName = EXPORT_FILE_NAME;
//				//	The Map products must be sorted (TreeMap) for initialising
//				//	selectedProducts in the good order (i.e. product id)
//				/*products = new TreeMap <Integer, FgProduct> ();
//				products.put (STUMP, new FgOakStump(STUMP));
//				products.put (SLICING, new FgOakSlicingLog(SLICING));
//				products.put (STAVE, new FgOakStaveLog(STAVE));
//				products.put (FURNITURE, new FgOakFurnitureLog(FURNITURE));
//				products.put (SAWING, new FgOakSawingLog(SAWING));
//				products.put (LVL, new FgOakLvlLog(LVL));
//				products.put (PARTICLE, new FgOakParticleBoardLog(PARTICLE));
//				products.put (FIRE, new FgOakFirewoodLog(FIRE));
//				products.put (TOP, new FgOakTopLog(TOP));*/
//
//				initDone = true;
//			}
//
//			/*recordResults = true;
//			exportResults = true;
//			discInterval_m = 1.0;
//			setPrecisionLength_m (0.001);
//
//			//	Selected products sorted by decreasing default priority:
//			//	(STUMP and TOP should always be in first and last position)
//			selectedProducts = new Vector <FgProduct> ();
//			for (Iterator p=products.keySet ().iterator (); p.hasNext ();) {
//				FgProduct f = products.get (p.next ());
//				selectedProducts.add (f);
//			}*/
//
//		}
//	}
	
	@Override
	public void initializeDefaultLogCategories() {
		
		topGirth = TOP_GIRTH;
		top1Girth = TOP1_GIRTH;
		top2Girth = 0;
		top3Girth = 0;
		log1Length = LOG1_LENGTH;
		log2Length = LOG2_LENGTH;
		log3Length = 0;

		exportAsked = EXPORT_ASKED;
		exportFileName = EXPORT_FILE_NAME;
		getLogCategories().clear();
		List<Pp3LoggingTreeLogCategory> logCategories = new ArrayList<Pp3LoggingTreeLogCategory>();
		getLogCategories().put (MARITIME_PINE, logCategories);
		logCategories.add(new Pp3LoggingTreeLogCategory("Pp3LogCategory"));
	}

	@Override
	public Pp3LoggingTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new Pp3LoggingTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	public static void main(String[] args) {
		Pp3LoggingTreeLoggerParameters params = new Pp3LoggingTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		params.showUI(null);
		System.exit(0);
	}

	
}
