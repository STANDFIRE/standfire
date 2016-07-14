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

package capsis.lib.economics;

import java.util.Collection;

/**	The stands implementing this interface can use
*	the BillBook
*	@author O. Pain - september 2007
*/
public interface BillBookCompatible extends Producer {

	public String getSpeciesName ();
	public BillBookSpecies getSpecies ();
	public double getArea ();	// m2

	public int getRotationAge ();
	public int getPlantationAge ();

	public Collection<CropExpense> getCropExpenses ();	// fc + op - 26.11.2007
	public Collection<BillBookLine> getWPOutputProducts ();	// fc + op - 26.11.2007
	
	public String getMacroWPName ();	// fc + op - 19.3.2008
	
	public Collection<AnnualCost> getAnnualCosts ();
	public Collection<VariableCost> getVariableCosts ();
	public void setAnnualCosts (Collection<AnnualCost> v);
	public void setVariableCosts (Collection<VariableCost> v);
}

