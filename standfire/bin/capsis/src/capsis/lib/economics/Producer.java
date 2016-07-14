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

/**	Objects that can produce Products 
*	@author O. Pain - october 2007
*/
public interface Producer {
	
	public void addProduct (Product p);
	
	public void removeProduct (Product p);
	
	public Collection<Product> getProducts ();
	
	/**	Return a collection of products that this Producer can produce.
	*	Usable for compatibility between Working processes (see WorkingProcess).
	*	Products returned have quantity set to 0 (just for test).
	*/
	//~ public Collection<Product> canProduct ();
	
	
	/**	Return a collection of names of products that this Producer CAN produce.
	*	NOT TRANSLATED HERE : return the translation key, ex: "Product.LOG"
	*/
	public Collection<String> getOutputProductNames ();
}

