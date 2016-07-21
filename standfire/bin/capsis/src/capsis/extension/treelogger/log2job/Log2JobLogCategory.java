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

package capsis.extension.treelogger.log2job;


import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogCategoryPanel;
import repicea.simulation.treelogger.WoodPiece;

/**	Log2JobLogCategory : sample log category class for Log2Job
*
*	@author F. Mothe - april 2010
*/
public class Log2JobLogCategory extends TreeLogCategory {

	private static final long serialVersionUID = 0L;

	public Log2JobLogCategory () {
		super("sample category", false);
	}

	@Override
	public TreeLogCategoryPanel getUI() {
		return null;
	}
	
	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		return 1.;
	}

	@Override 
	public boolean isVisible() {
		return false;
	}

}
