/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2001  Francois de Coligny
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 */
package capsis.extension.workingprocess;

import java.util.Collection;

import capsis.extension.WorkingProcessStarter;

/**	All parameters for MechanicalHarvesting.
*
*	@author O. Pain - october 2007
*/
public class MechanicalHarvestingStarter extends WorkingProcessStarter {
	
	public MechanicalHarvestingStarter (String priceUnit, 
			double price, 
			String fuelUnit, 
			double fuel, 
			String requestedInputProductName, 
			Collection<String> requestedOutputProductNames 
			) {
		super (priceUnit, price, fuelUnit, fuel, requestedInputProductName, requestedOutputProductNames );
	}
	
}
