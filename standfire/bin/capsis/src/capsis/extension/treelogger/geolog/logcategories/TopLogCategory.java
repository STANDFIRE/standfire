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
import capsis.extension.treelogger.geolog.GeoLogTreeData;

/**	
 * GeoLog "toplog" product.
 * Remaining log when all the other pieces are cut.
 * @author F. Mothe - january 2006
 */
public class TopLogCategory extends GeoLogLogCategory {

	private static final long serialVersionUID = 20101025L;

	private transient TopLogCategoryPanel guiInterface;
	
	//	Constructor
	public TopLogCategory(int id, String species) {
		// Only one top-log per tree, no condition on length, no condition on diameter:
		super (id, Translator.swap("TopLogProduct.name"), species, 1, true, 0, -1, -1, 0, true);
	}

	//	Abstract function of GeoLogProduct
	@Override
	public boolean testLogValid (GeoLogTreeData td) {
		return testCountLength(td);
	}


	public double getYieldFromThisPiece(WoodPiece p) throws Exception {
		return 1.;
	}

	
	@Override
	public TopLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new TopLogCategoryPanel(this);
		}
		return guiInterface;
	}

	
	@Override
	@Deprecated
	protected Tester getTester(GeoLogTreeData td) {return null;}



}
