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
 * GeoLog "fire" product.
 * Fire wood.
 * @author F. Mothe - january 2006
 */
public class FirewoodLogCategory extends GeoLogLogCategory {

	private static final long serialVersionUID = 20101025L;

	/**
	 * Constructor. 
	 */	
	public FirewoodLogCategory(int id, String species) {
		super(id, Translator.swap("FirewoodProduct.name"), species, -1, true, 0.5, 1.0, 7, 1.0, true);
	}

	//	Abstract function of GeoLogProduct
	@Override
	public boolean testLogValid (GeoLogTreeData td) {
		return testGeometry(td) ;
	}
	
	//	Returns the volumic yield for making this product (i.e. usefulVolume / v_m3)
	//	(should be overrided for each product)
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		return .95;
	}

	@Override
	@Deprecated
	protected Tester getTester(GeoLogTreeData td) {return null;}
	

}
