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
package capsis.util.methodprovider;

import capsis.defaulttype.TreeList;
import capsis.kernel.MethodProvider;

/**
 * Current yield increment.
 * Biological increment between step t-1 and step t. 
 * Takes into account only living trees growth and recruitment. 
 * Death or cutting are not processes that produce new biomass.
 * Increment should not be confused with harvesting.
 * 
 * @author B. Courbaud - january 2003
 * reviewed fc-1.2.2011
 */
public interface CurrentYieldIncrementProvider extends MethodProvider {

	/**	Current yield increment. 
	 */
	public double getCurrentYieldIncrement (TreeList prevStand, TreeList stand, double minDbh);


}


