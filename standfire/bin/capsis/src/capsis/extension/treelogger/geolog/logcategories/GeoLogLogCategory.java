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

package capsis.extension.treelogger.geolog.logcategories;


import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogCategoryPanel;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.WoodPiece;
import capsis.extension.treelogger.geolog.GeoLogTreeData;
import capsis.extension.treelogger.geolog.LoggingContext;
import capsis.extension.treelogger.geolog.logcategories.TesterLibrary.DiameterTester;
import capsis.extension.treelogger.geolog.util.LogPriceModel;

/**	
 * GeoLogProduct is the abstract base class of all the GeoLog products.
 * @author F. Mothe - January 2006
 * @author Mathieu Fortin - November 2011 (refactoring)
 */
public class GeoLogLogCategory extends TreeLogCategory {

	private static final long serialVersionUID = 20101025L;
	
	private static double precisionLength_m = 0.001;
	
	private int id;

	// Max number of logs for this product :
	protected int maxCount;	// (negative means no test)

	// If the log may be cut into crown :
	protected boolean acceptCrown;

	// Min and max length of a log :
	protected double minLength_m;
	protected double maxLength_m;	// (negative means no test)

	// Minimal diameter :
	// diamOverBark == true if the log diameter should be measured  over bark
	// diamRelPos indicates where it should be measured (0 = bottom, 1 = top)
	protected double minDiam_cm;	// (negative means no test)
	protected double diamRelPos;
	protected boolean diamOverBark;

	// Price / m3 :
	public LogPriceModel priceModel;

	// TODO : add help info
	protected double minRandAttributes [];
	
	protected transient TreeLogCategoryPanel guiInterface;
	
	/**
	 * Public constructor for new log categories.
	 */
	public GeoLogLogCategory() {
		this(0, "Unnamed", TreeLoggerParameters.ANY_SPECIES, -1, true, 0.3, -1, 4, -1, true); 
	}

	
	/**
	 * Public constructor.
	 * @param id the id of this log category
	 * @param name the name (already translated)
	 * @param maxCount
	 * @param acceptCrown
	 * @param minLength_m
	 * @param maxLength_m
	 * @param minDiam_cm
	 * @param diamRelPos
	 * @param diamOverBark
	 */
	public GeoLogLogCategory (int id, 
			String name, 
			String species,
			int maxCount, 
			boolean acceptCrown,
			double minLength_m, 
			double maxLength_m,
			double minDiam_cm, 
			double diamRelPos, 
			boolean diamOverBark) {
		super(name);
		this.id = id;
		this.maxCount = maxCount;
		this.acceptCrown = acceptCrown;
		this.minLength_m = minLength_m;
		this.maxLength_m = maxLength_m;
		this.minDiam_cm = minDiam_cm;
		this.diamRelPos = diamRelPos;
		this.diamOverBark = diamOverBark;
		this.priceModel = new LogPriceModel(0.0, 0.0);
		setSpecies(species);
		this.minRandAttributes = new double [GeoLogTreeData.NB_RANDOM_ATTRIBUTES];
		for (int n=0; n<GeoLogTreeData.NB_RANDOM_ATTRIBUTES; ++n) {
			this.minRandAttributes [n] = -1;
		}
		// System.out.println ("Product " + id + "=" + this.name) ;
	}

	
	
	
	//	Tests if a valid log may be cut and set its length.
	//	Calls lc.setLength() and returns true if a valid log may be cut.
	//	(lc.length may be destroyed even the method return false).
	public boolean testLogValid(GeoLogTreeData td) {
		return testGeometry(td) ;
	}


	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		return 1.;
	}

	/**
	 * This method returns a Tester-derived instance which depends on the GeoLogLogCategory derived class. 
	 * @return a Tester instance
	 */
	protected Tester getTester(GeoLogTreeData td) {return null;}
	

	@Override
	public TreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new GeoLogLogCategoryPanel(this);
		}
		return guiInterface;
	}
	
	//	Helper method to estimate the mass of products. Returns the min and max radius
	//	of a region usable for making this product or null if the whole mass of the log
	//	should be prefered.
	//	(should be overrided for each product)
	public double [] getMinMaxUsefulRadius_mm (capsis.extension.treelogger.GPiece piece) {
		return null;
	}



	//	Searches the maximal length between minLength_m and lc.getLength ()
	//	such as test.isValid (length) == true.
	//	Calls lc.setLength() and returns true if a valid log may be cut.
	protected boolean testMaxLength(Tester test, GeoLogTreeData td) {
		LoggingContext lc = td.getLoggingContext();
		return td.testMaxLength(test, minLength_m,	lc.getLength(), precisionLength_m);
	}

	//	17.08.2006 : Tests attributes considering minRandAttributes
	//	Calls lc.setLength() and returns true if a valid log may be cut.
	/**
	 * This method tests the count, length and diameter considering 
	 * maxCount, minLength_m, maxLength_m, minDiam_cm, diamRelPos and 
	 * diamOverBark.
	 * @param td the GeoLogTreeData instance of the tree that is currently sawn by the GeoLog
	 * @return a boolean true if this GeoLogLogCategory can be extracted or false otherwise
	 */
	protected boolean testGeometry(GeoLogTreeData td) {
		LoggingContext lc = td.getLoggingContext();
		boolean valid = false;
		if (testCountLength(td) && testRandomAttributes(td)) {
			DiameterTester test = new DiameterTester(td, lc.getHeight(), minDiam_cm, diamRelPos, diamOverBark);
			valid = testMaxLength(test, td);
		}
		return valid;
	}

	//	Tests attributes considering minRandAttributes
	private boolean testRandomAttributes(GeoLogTreeData td) {
		boolean valid = true;
		for (int n = 0; valid && n < GeoLogTreeData.NB_RANDOM_ATTRIBUTES; ++n) {
			// (always true if minRandAttributes [n] <0)
			valid = td.getRandomAttribute(n) >= minRandAttributes [n];
			//	&& maxRandAttributes [n] < 0 || td.getRandomAttribute (n) <= maxRandAttributes [n];
		}
		return valid;
	}

	//	Tests count and length only
	//	Calls lc.setLength() and returns true if a valid log may be cut.
	protected boolean testCountLength (GeoLogTreeData td) {
		LoggingContext lc = td.getLoggingContext();
		boolean valid = false;
		if (testCount (lc) ) {
			double length = lc.getAvailableLength (acceptCrown);
			// Double test for considering negative minLength_m (should not occur)
			if (length > 0 && length >= minLength_m) {
				if (maxLength_m > 0) {
					length = Math.min (length, maxLength_m);
				}
				valid = true;
				lc.setLength (length) ;
				// System.out.println (name + "::testCountLength true, length=" + lc.getLength ());
			}
		}
		return valid;
	}

	//	Tests count only
	private boolean testCount (LoggingContext lc) {
		return  maxCount < 0 || lc.getLogCount (getId ()) < maxCount;
	}

	public int getId () {return id;}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		GeoLogLogCategory refCategory = (GeoLogLogCategory) obj;
		
		if (refCategory.id != this.id) {return false;}
		if (refCategory.maxCount != this.maxCount) {return false;}
		if (refCategory.acceptCrown != this.acceptCrown) {return false;}
		if (refCategory.minLength_m != this.minLength_m) {return false;}
		if (refCategory.maxLength_m != this.maxLength_m) {return false;}
		if (refCategory.minDiam_cm != this.minDiam_cm) {return false;}
		if (refCategory.diamRelPos != this.diamRelPos) {return false;}
		if (refCategory.diamOverBark != this.diamOverBark) {return false;}
		if (!refCategory.priceModel.equals(this.priceModel)) {return false;}
		for (int i = 0; i < minRandAttributes.length; i++) {
			if (refCategory.minRandAttributes[i] != this.minRandAttributes[i]) {
				return false;
			}
		}
		
		return true;
	}




	
	
}
