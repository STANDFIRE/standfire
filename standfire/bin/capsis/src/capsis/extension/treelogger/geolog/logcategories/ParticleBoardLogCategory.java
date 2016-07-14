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

/**	GeoLog "particle" product.
*	For particle board production.
*	@author F. Mothe - january 2006
*			Modified M. Fortin - April 2010
*/
public class ParticleBoardLogCategory extends GeoLogLogCategory {

	private static final long serialVersionUID = 20101025L;

	//	Constructor
	public ParticleBoardLogCategory(int id, String species) {
		super(id, Translator.swap("ParticleBoardProduct.name"), species, 3, true, 2.0, 2.2, 7, 1.0, true);
	}

	//	Abstract function of GeoLogProduct
	public boolean testLogValid (GeoLogTreeData td) {
		return testGeometry(td) ;
	}

	//	Returns the volumic yield for making this product (i.e. usefulVolume / v_m3)
	//	(should be overrided for each product)
	public double getYieldFromThisPiece(WoodPiece p) throws Exception {
		return .99;
	}

	@Override
	@Deprecated
	protected Tester getTester(GeoLogTreeData td) {return null;}


}
