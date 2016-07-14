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

import jeeb.lib.util.Translator;
import repicea.simulation.treelogger.WoodPiece;
import capsis.extension.treelogger.geolog.GeoLog;
import capsis.extension.treelogger.geolog.GeoLogTreeData;

/**	GeoLog "stump" product.
*	Tree bottom remaining in forest.
*	@author F. Mothe - january 2006
*/
public class StumpLogCategory extends GeoLogLogCategory {

	private transient StumpLogCategoryPanel guiInterface;
	
	/**
	 * Constructor.
	 */
	public StumpLogCategory(int id, String species) {
		// Only one stump per tree, fixed length, no condition on diameter:
		super(id, Translator.swap("StumpProduct.name"), species, 1, true, GeoLog.DEFAULT_HEIGHT_STUMP_m, GeoLog.DEFAULT_HEIGHT_STUMP_m, -1, 0, true);
	}

	
	//	Abstract function of GeoLogProduct
	@Override
	public boolean testLogValid (GeoLogTreeData td) {
		return testCountLength(td);
	}

	@Override
	public StumpLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new StumpLogCategoryPanel(this);
		}
		return guiInterface;
	}

	public double getYieldFromThisPiece(WoodPiece p) throws Exception {
		return 1.;
	}

	@Override
	@Deprecated
	protected Tester getTester(GeoLogTreeData td) {return null;}


	
}
