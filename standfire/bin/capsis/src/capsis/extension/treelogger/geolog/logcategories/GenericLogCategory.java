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

import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.WoodPiece;
import capsis.extension.treelogger.geolog.GeoLogTreeData;
import capsis.extension.treelogger.geolog.logcategories.TesterLibrary.GenericTester;

/**	
 * GenericProduct : generic product for GeoLog
 * using knotcore, heartwood and juvenile wood rules
 *	@author F. Mothe - april 2006
 */
public class GenericLogCategory extends GeoLogLogCategory {
	
	private static final long serialVersionUID = 20101025L;

	protected double maxKnotDiam_cm;	// (negative means no test)
	protected double minHeartDiam_cm;	// (negative means no test)
	protected double maxJuveDiam_cm;	// (negative means no test)

	private transient GenericLogCategoryPanel guiInterface;
	
	//	Constructor
	public GenericLogCategory (int id, String name, int maxCount, boolean acceptCrown,
			double minLength_m, double maxLength_m,
			double minDiam_cm, double diamRelPos, boolean diamOverBark,
			double maxKnotDiam_cm, double minHeartDiam_cm,
			double maxJuveDiam_cm) {
		super(id, name, TreeLoggerParameters.ANY_SPECIES, maxCount, acceptCrown, minLength_m, maxLength_m,
				minDiam_cm, diamRelPos, diamOverBark);		// end product must be defined in the starter (see the SimpleLoggingStarter class)
		this.maxKnotDiam_cm = maxKnotDiam_cm;
		this.minHeartDiam_cm = minHeartDiam_cm;
		this.maxJuveDiam_cm = maxJuveDiam_cm;
		// System.out.println ("GenericProduct.GenericProduct ()");
	}


	//	Abstract function of GeoLogProduct
	public boolean testLogValid (GeoLogTreeData td) {
		boolean valid = false;
		if (testGeometry(td)) {
			Tester test = getTester(td);
			valid = testMaxLength (test, td);
		}

		return valid;
	}
	
	public double getYieldFromThisPiece(WoodPiece p) throws Exception {
		return 1.;
	}


	@Override
	public GenericLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new GenericLogCategoryPanel(this);
		}
		return guiInterface;
	}


	@Override
	protected GenericTester getTester(GeoLogTreeData td) {
		return new GenericTester(td, td.getLoggingContext().getHeight(), this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		GenericLogCategory refCategory = (GenericLogCategory) obj;
		
		if (refCategory.maxKnotDiam_cm != this.maxKnotDiam_cm) {return false;}
		if (refCategory.minHeartDiam_cm != this.minHeartDiam_cm) {return false;}
		if (refCategory.maxJuveDiam_cm != this.maxJuveDiam_cm) {return false;}

		return true;
	}


}
